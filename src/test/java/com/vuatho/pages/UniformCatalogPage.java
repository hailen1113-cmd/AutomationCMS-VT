package com.vuatho.pages;

import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Page Object của Quản lí Đồng phục: nhóm/combo và sản phẩm đồng phục.
 */
public final class UniformCatalogPage extends UniformUiPage {
    public static final String ROUTE = "/vuatho/uniform";

    private static final By SEARCH = By.cssSelector(
            "main input[aria-label^='Tìm kiếm']");
    private static final By CREATE = By.xpath(
            "//main//button[normalize-space()='Tạo mới']");
    private static final By FILTER = By.cssSelector("main button[title='Filter']");
    private static final By RESET = By.cssSelector("main button[title='Reset']");

    public UniformCatalogPage(WebDriver driver) {
        super(driver);
    }

    /** Mở trang quản lí danh mục đồng phục. */
    public UniformCatalogPage open() {
        openRoute(ROUTE);
        waitForResult();
        return this;
    }

    /** Chọn tab Nhóm Đồng Phục hoặc Đồng Phục. */
    public UniformCatalogPage selectTab(String tab) {
        WebElement control = visible(By.xpath(
                "//*[@role='tab' and normalize-space()=" + xpathLiteral(tab) + "]"));
        click(control, "Chọn tab " + tab);
        waitForResult();
        wait.until(d -> !searchPlaceholder().isBlank());
        return this;
    }

    /** Trả tab đang được chọn. */
    public String selectedTab() {
        return visible(By.cssSelector("[role='tab'][aria-selected='true']")).getText().trim();
    }

    /** Trả placeholder của ô tìm kiếm tại tab hiện tại. */
    public String searchPlaceholder() {
        WebElement input = visible(SEARCH);
        String placeholder = input.getAttribute("placeholder");
        return placeholder == null || placeholder.isBlank()
                ? input.getAttribute("aria-label")
                : placeholder;
    }

    /** Tìm theo tên và chờ danh sách trả dữ liệu. */
    public UniformCatalogPage search(String keyword) {
        WebElement input = visible(SEARCH);
        fill(input, keyword, "Nhập từ khóa " + keyword);
        settle(1_000);
        waitForResult();
        return this;
    }

    /** Trả giá trị hiện tại của ô tìm kiếm. */
    public String searchValue() {
        String value = visible(SEARCH).getAttribute("value");
        return value == null ? "" : value;
    }

    /** Xóa từ khóa trực tiếp trong input, không sử dụng nút Reset. */
    public UniformCatalogPage clearSearchManually() {
        WebElement input = visible(SEARCH);
        click(input, "Đặt con trỏ vào ô tìm kiếm");
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        pause("Chọn toàn bộ từ khóa tìm kiếm");
        input.sendKeys(Keys.DELETE);
        pause("Xóa thủ công từ khóa tìm kiếm");
        wait.until(d -> searchValue().isBlank());
        settle(1_000);
        waitForResult();
        return this;
    }

    /** Reset tìm kiếm và bộ lọc. */
    public UniformCatalogPage reset() {
        click(visible(RESET), "Đặt lại tìm kiếm và bộ lọc");
        waitForResult();
        return this;
    }

    /** Mở bộ lọc và trả nội dung tùy chọn. */
    public String openFilter() {
        click(visible(FILTER), "Mở bộ lọc " + selectedTab());
        pause("Hiển thị các tùy chọn bộ lọc");
        return elementText(filterPopup());
    }

    /** Chọn một tùy chọn đang hiển thị trong popup lọc. */
    public UniformCatalogPage chooseFilter(String option) {
        WebElement popup = filterPopupOrOpen();
        WebElement item = popup.findElements(By.xpath(
                        ".//label[.//*[normalize-space()=" + xpathLiteral(option) + "]]"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseGet(() -> popup.findElements(By.xpath(
                                ".//*[normalize-space()=" + xpathLiteral(option)
                                        + " and (self::span or self::div"
                                        + " or self::button or self::label)]"))
                        .stream().filter(WebElement::isDisplayed).findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "Không tìm thấy tùy chọn lọc " + option)));
        click(item, "Chọn bộ lọc " + option);
        wait.until(d -> {
            List<WebElement> inputs = item.findElements(By.cssSelector(
                    "input[type='radio'],input[type='checkbox']"));
            return inputs.isEmpty()
                    || inputs.stream().anyMatch(WebElement::isSelected);
        });
        // API lọc danh mục phản hồi chậm hơn debounce tìm kiếm; cần chờ cả ở
        // headless vì pause quan sát chỉ giữ màn hình khi có giao diện.
        settle(2_000);
        waitForResult();
        return this;
    }

    /** Trả true khi radio/checkbox của trạng thái tồn kho đang được chọn. */
    public boolean inventoryFilterSelected(String option) {
        WebElement popup = filterPopupOrOpen();
        WebElement label = popup.findElements(By.xpath(
                        ".//label[.//*[normalize-space()=" + xpathLiteral(option) + "]]"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy trạng thái lọc " + option));
        return label.findElements(By.cssSelector(
                        "input[type='radio'],input[type='checkbox']"))
                .stream()
                .anyMatch(WebElement::isSelected);
    }

    /** Trả true khi cả Còn hàng và Hết hàng đều chưa được chọn. */
    public boolean inventoryFilterCleared() {
        return !inventoryFilterSelected("Còn hàng")
                && !inventoryFilterSelected("Hết hàng");
    }

    /** Xác nhận toàn bộ card hiện tại phù hợp với trạng thái tồn kho đã chọn. */
    public boolean displayedCardsMatchInventoryStatus(String status) {
        List<CatalogCard> cards = displayedCards();
        if (cards.isEmpty()) {
            return false;
        }
        boolean expectedOutOfStock = status.equals("Hết hàng");
        return cards.stream().allMatch(card ->
                card.raw().contains("Hết hàng") == expectedOutOfStock);
    }

    /** Mở drawer tạo mới của tab hiện tại. */
    public String openCreateDrawer() {
        click(visible(CREATE), "Mở form tạo mới " + selectedTab());
        String expected = selectedTab().equals("Nhóm Đồng Phục")
                ? "drawer-Tạo mới nhóm đồng phục"
                : "drawer-Tạo mới đồng phục";
        WebElement drawer = visible(By.cssSelector(
                "[aria-label='" + expected + "']"));
        pause("Hiển thị đầy đủ form tạo mới");
        return elementText(drawer);
    }

    /**
     * Đọc trực tiếp cấu trúc điều khiển của drawer tạo nhóm.
     * Không dựa riêng vào nội dung text nên sẽ phát hiện được trường hợp giao diện còn nhãn
     * nhưng input, combobox, công tắc hoặc thuộc tính upload bị thiếu/sai.
     */
    public GroupCreateFormSnapshot groupCreateFormSnapshot() {
        WebElement drawer = groupCreateDrawer();
        WebElement upload = drawer.findElement(By.cssSelector("input[type='file']"));
        List<WebElement> comboboxes = drawer.findElements(
                By.cssSelector("input[role='combobox']"));
        boolean hasOutOfStockToggle = drawer.findElements(By.xpath(
                        ".//*[normalize-space()='Trạng thái hết hàng']/following::button[1]"))
                .stream().anyMatch(WebElement::isDisplayed);
        return new GroupCreateFormSnapshot(
                businessTextInputs(drawer).size(),
                comboboxes.size(),
                acceptsImages(upload),
                upload.getAttribute("multiple") != null,
                hasOutOfStockToggle,
                elementText(drawer).contains("Chưa có đồng phục được chọn"),
                hasVisibleButton(drawer, "Hủy"),
                hasVisibleButton(drawer, "Xác nhận"));
    }

    /**
     * Đọc trực tiếp cấu trúc điều khiển của drawer tạo đồng phục.
     * Hai lựa chọn biến thể được kiểm tra bằng input radio thật và trạng thái chọn ban đầu.
     */
    public UniformCreateFormSnapshot uniformCreateFormSnapshot() {
        WebElement drawer = uniformCreateDrawer();
        WebElement upload = drawer.findElement(By.cssSelector("input[type='file']"));
        List<WebElement> variantChoices = drawer.findElements(
                By.cssSelector("[role='radiogroup'] input[type='radio']"));
        int selectedChoices = (int) variantChoices.stream()
                .filter(WebElement::isSelected)
                .count();
        return new UniformCreateFormSnapshot(
                businessTextInputs(drawer).size(),
                variantChoices.size(),
                selectedChoices,
                acceptsImages(upload),
                upload.getAttribute("multiple") != null,
                elementText(drawer).contains("Tối đa 5 ảnh"),
                hasVisibleButton(drawer, "Hủy"),
                hasVisibleButton(drawer, "Xác nhận"));
    }

    /** Đóng form tạo bằng nút Hủy và xác nhận drawer biến mất. */
    public boolean cancelCreateDrawer() {
        WebElement drawer = currentCreateDrawer();
        click(buttonIn(drawer, "Hủy"), "Hủy form tạo " + selectedTab());
        return waitForOverlayToClose(drawer.getAttribute("aria-label"));
    }

    /** Đóng form tạo bằng nút biểu tượng X ở đầu drawer. */
    public boolean closeCreateDrawerByIcon() {
        WebElement drawer = currentCreateDrawer();
        String label = drawer.getAttribute("aria-label");
        WebElement close = drawer.findElements(By.cssSelector("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getText().isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Drawer " + label + " không có nút đóng biểu tượng."));
        click(close, "Đóng form tạo bằng biểu tượng X");
        return waitForOverlayToClose(label);
    }

    /** Nhập tên nháp vào form tạo hiện tại để kiểm tra Hủy/đóng không lưu. */
    public UniformCatalogPage fillCreateName(String name) {
        WebElement drawer = currentCreateDrawer();
        List<WebElement> fields = businessTextInputs(drawer);
        if (fields.isEmpty()) {
            throw new IllegalStateException("Form tạo thiếu ô tên.");
        }
        fill(fields.get(0), name, "Nhập tên nháp " + name);
        return this;
    }

    /** Chọn tài khoản thanh toán đầu tiên trên form nhóm. */
    public String selectFirstPaymentAccount() {
        WebElement drawer = groupCreateDrawer();
        selectFirstReactOptionByPlaceholder(
                drawer, "Chọn tài khoản nhận tiền",
                "Chọn tài khoản thanh toán đầu tiên");
        pause("Hiển thị tài khoản thanh toán đã chọn");
        return elementText(drawer);
    }

    /** Chọn đồng phục đầu tiên để thêm vào package của nhóm. */
    public String selectFirstUniformForPackage() {
        WebElement drawer = groupCreateDrawer();
        selectFirstReactOptionByPlaceholder(
                drawer, "Chọn đồng phục",
                "Chọn đồng phục đầu tiên cho package");
        pause("Hiển thị đồng phục đã chọn trong package");
        return elementText(drawer);
    }

    /** Bấm công tắc Hết hàng trên form nhóm và xác nhận trạng thái hiển thị đã thay đổi. */
    public boolean toggleGroupOutOfStockDraft() {
        WebElement drawer = groupCreateDrawer();
        WebElement toggle = drawer.findElements(By.xpath(
                        ".//*[normalize-space()='Trạng thái hết hàng']/following::button[1]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Form tạo nhóm thiếu công tắc Trạng thái hết hàng."));
        WebElement knob = toggle.findElement(By.cssSelector("div"));
        String before = knob.getAttribute("class");
        click(toggle, "Đổi trạng thái hết hàng trên form tạo nhóm");
        settle(400);
        pause("Quan sát trạng thái hết hàng vừa chọn");
        String after = knob.getAttribute("class");
        return before != null && !before.equals(after);
    }

    /** Upload một hoặc nhiều ảnh và trả số ảnh preview trong drawer. */
    public int uploadCreateImages(List<Path> files) {
        WebElement drawer = currentCreateDrawer();
        WebElement input = drawer.findElement(By.cssSelector("input[type='file']"));
        String inputId = input.getAttribute("id");
        WebElement uploadSurface = inputId == null || inputId.isBlank()
                ? null
                : drawer.findElements(By.cssSelector(
                                "label[for='" + inputId + "']"))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElse(null);
        if (uploadSurface != null) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});"
                            + "arguments[0].style.outline='3px solid #2563eb';",
                    uploadSurface);
            pause("Quan sát vùng chọn ảnh trước khi tải file");
        }
        String paths = files.stream()
                .map(path -> path.toAbsolutePath().normalize().toString())
                .reduce((left, right) -> left + "\n" + right)
                .orElseThrow(() -> new IllegalArgumentException("Danh sách ảnh rỗng."));
        pause("Chọn " + files.size() + " ảnh tải lên");
        input.sendKeys(paths);
        settle(800);
        drawer.findElements(By.cssSelector("img")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .ifPresent(preview -> ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({behavior:'smooth',block:'center'});"
                                + "arguments[0].style.outline='3px solid #16a34a';",
                        preview));
        pause("Quan sát ảnh thật đã chọn và bản xem trước");
        return uploadedPreviewCount(drawer);
    }

    /** Trả số ảnh preview đang hiển thị trong form tạo hiện tại. */
    public int uploadedPreviewCount() {
        return uploadedPreviewCount(currentCreateDrawer());
    }

    /** Đếm ảnh preview đã tải thành công, không tính thẻ ảnh rỗng hoặc file bị hỏng. */
    public int loadedImagePreviewCount() {
        WebElement drawer = currentCreateDrawer();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (int) drawer.findElements(By.cssSelector("img")).stream()
                .filter(WebElement::isDisplayed)
                .filter(image -> Boolean.TRUE.equals(js.executeScript(
                        "return arguments[0].complete && arguments[0].naturalWidth > 0;",
                        image)))
                .count();
    }

    /** Kiểm tra nút xác nhận của drawer tạo mới có bị khóa khi thiếu dữ liệu. */
    public boolean createConfirmDisabled() {
        WebElement drawer = visible(By.cssSelector("[aria-label^='drawer-Tạo mới']"));
        WebElement confirm = drawer.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getText().trim().equals("Xác nhận"))
                .findFirst()
                .orElseThrow();
        return !confirm.isEnabled();
    }

    /**
     * Bấm xác nhận khi form tạo chưa có dữ liệu và trả nội dung validation.
     * Hai form hiện để nút enabled nên cần xác minh kết quả thay vì trạng thái nút.
     */
    public String submitEmptyCreateForm() {
        String drawerLabel = selectedTab().equals("Nhóm Đồng Phục")
                ? "drawer-Tạo mới nhóm đồng phục"
                : "drawer-Tạo mới đồng phục";
        WebElement drawer = visible(By.cssSelector(
                "[aria-label='" + drawerLabel + "']"));
        WebElement confirm = drawer.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getText().trim().equals("Xác nhận"))
                .findFirst()
                .orElseThrow();
        click(confirm, "Xác nhận form rỗng " + selectedTab());
        settle(600);
        pause("Quan sát validation form " + selectedTab());
        return elementText(drawer);
    }

    /** Chọn Có biến thể và thêm một dòng biến thể trên form tạo đồng phục. */
    public String addVariantDraft() {
        WebElement drawer = visible(By.cssSelector(
                "[aria-label='drawer-Tạo mới đồng phục']"));
        WebElement hasVariant = drawer.findElements(By.xpath(
                        ".//*[@role='radio' or @type='radio']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> "Có biến thể".equals(element.getAttribute("aria-label"))
                        || element.findElements(By.xpath(
                        "./following::*[normalize-space()='Có biến thể'][1]")).size() > 0)
                .findFirst()
                .orElseGet(() -> drawer.findElement(By.xpath(
                        ".//*[normalize-space()='Có biến thể']")));
        click(hasVariant, "Chọn sản phẩm có biến thể");
        WebElement add = visible(By.xpath(
                "//*[@aria-label='drawer-Tạo mới đồng phục']"
                        + "//button[normalize-space()='Thêm biến thể']"));
        click(add, "Thêm một biến thể");
        pause("Hiển thị trường dữ liệu biến thể");
        return drawer.getText();
    }

    /** Thêm thêm một dòng biến thể và trả tổng số dòng hiện tại. */
    public int addAnotherVariantRow() {
        WebElement drawer = uniformCreateDrawer();
        click(buttonIn(drawer, "Thêm biến thể"), "Thêm dòng biến thể");
        settle(400);
        pause("Hiển thị danh sách biến thể sau khi thêm");
        return variantRowCount(drawer);
    }

    /** Xóa dòng biến thể cuối cùng và trả tổng số dòng còn lại. */
    public int removeLastVariantRow() {
        WebElement drawer = uniformCreateDrawer();
        List<WebElement> rows = variantRows(drawer);
        if (rows.isEmpty()) {
            return 0;
        }
        WebElement delete = rows.get(rows.size() - 1)
                .findElements(By.cssSelector("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getText().isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Dòng biến thể không có nút xóa."));
        click(delete, "Xóa dòng biến thể cuối");
        settle(400);
        pause("Hiển thị danh sách biến thể sau khi xóa");
        return variantRowCount(drawer);
    }

    /** Chọn loại Màu sắc hoặc Văn bản cho dòng biến thể đầu tiên. */
    public String chooseFirstVariantType(String type) {
        WebElement drawer = uniformCreateDrawer();
        List<WebElement> rows = variantRows(drawer);
        if (rows.isEmpty()) {
            throw new IllegalStateException("Chưa có dòng biến thể để chọn loại.");
        }
        click(buttonIn(rows.get(0), type), "Chọn loại biến thể " + type);
        settle(400);
        pause("Hiển thị trình nhập giá trị biến thể " + type);
        return elementText(rows.get(0));
    }

    /** Thêm một dòng giá trị cho biến thể Văn bản đầu tiên. */
    public String addFirstTextVariantValueDraft() {
        WebElement drawer = uniformCreateDrawer();
        WebElement row = variantRows(drawer).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Chưa có dòng biến thể."));
        click(buttonIn(row, "Thêm"), "Thêm giá trị biến thể văn bản");
        settle(400);
        pause("Hiển thị dòng giá trị biến thể");
        return elementText(row);
    }

    /** Trả tổng số dòng biến thể trên form tạo sản phẩm. */
    public int variantRowCount() {
        return variantRowCount(uniformCreateDrawer());
    }

    /** Lấy tên item đầu tiên của tab hiện tại từ card dữ liệu. */
    public String firstItemName() {
        String marker = selectedTab().equals("Nhóm Đồng Phục") ? "Số đồng phục" : "Tồn kho";
        WebElement markerElement = visible(By.xpath(
                "//main//*[normalize-space()=" + xpathLiteral(marker) + "][1]"));
        WebElement card = markerElement.findElement(By.xpath(
                "./ancestor::*[contains(@class,'rounded')][1]"));
        return itemNameFromCard(card);
    }

    /** Trả danh sách tên item đang hiển thị dựa trên từng card có giá bán. */
    public List<String> displayedItemNames() {
        List<String> names = new ArrayList<>();
        for (WebElement priceMarker : visibleElements(By.xpath(
                "//main//*[normalize-space()='Giá bán']"))) {
            try {
                WebElement card = priceMarker.findElement(By.xpath(
                        "./ancestor::*[contains(@class,'rounded')][1]"));
                String name = itemNameFromCard(card);
                if (!name.isBlank() && !names.contains(name)) {
                    names.add(name);
                }
            } catch (RuntimeException ignored) {
                // Bỏ qua node trang trí không thuộc card dữ liệu.
            }
        }
        return names;
    }

    /** Đọc dữ liệu nghiệp vụ của từng card đang hiển thị. */
    public List<CatalogCard> displayedCards() {
        List<CatalogCard> cards = new ArrayList<>();
        String quantityLabel = selectedTab().equals("Nhóm Đồng Phục")
                ? "Số đồng phục" : "Tồn kho";
        for (WebElement priceMarker : visibleElements(By.xpath(
                "//main//*[normalize-space()='Giá bán']"))) {
            try {
                WebElement card = priceMarker.findElement(By.xpath(
                        "./ancestor::*[contains(@class,'rounded')][1]"));
                String raw = card.getText().trim();
                String name = itemNameFromCard(card);
                String price = capture(raw,
                        "Giá bán\\s*([\\d.,]+)\\s*(?:VND|₫)");
                String quantity = capture(raw,
                        Pattern.quote(quantityLabel) + "\\s*([\\d.]+)");
                boolean hasImage = !card.findElements(By.cssSelector("img")).isEmpty();
                boolean hasUpdater = raw.contains("Cập nhật bởi");
                if (!name.isBlank()) {
                    cards.add(new CatalogCard(
                            name, price, quantity, hasImage, hasUpdater, raw));
                }
            } catch (RuntimeException ignored) {
                // Bỏ qua node trang trí không thuộc card dữ liệu.
            }
        }
        return cards;
    }

    /**
     * Lấy đúng tiêu đề sản phẩm/nhóm trong card.
     *
     * <p>Không lấy dòng đầu tiên của {@code getText()} vì card đồng phục có thể
     * hiển thị nhãn trạng thái như "Hết hàng" phía trên tiêu đề.</p>
     */
    private String itemNameFromCard(WebElement card) {
        return card.findElements(By.xpath(".//h5[normalize-space()] | .//h6[normalize-space()]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(element -> element.getText().trim())
                .filter(name -> !name.isBlank())
                .findFirst()
                .orElse("");
    }

    /** Mở chi tiết item theo tên từ card kết quả. */
    public String openItemDetail(String name) {
        WebElement label = visible(By.xpath(
                "//main//*[normalize-space()=" + xpathLiteral(name) + "][1]"));
        WebElement clickable = label.findElement(By.xpath(
                "./ancestor::*[contains(@class,'cursor-pointer') or @role='button'][1]"));
        click(clickable, "Mở chi tiết " + name);
        String drawerLabel = selectedTab().equals("Nhóm Đồng Phục")
                ? "drawer-Chi tiết nhóm đồng phục"
                : "drawer-Chi tiết đồng phục";
        WebElement drawer = visible(By.cssSelector("[aria-label='" + drawerLabel + "']"));
        settle(600);
        pause("Hiển thị dữ liệu chi tiết " + name);
        String inputValues = drawer.findElements(By.cssSelector("input,textarea"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(element -> element.getAttribute("value"))
                .filter(value -> value != null && !value.isBlank())
                .reduce("", (left, right) -> left + "\n" + right);
        return elementText(drawer) + inputValues;
    }

    /** Tạo thật một nhóm đồng phục bằng dữ liệu sandbox. */
    public boolean createGroup(String name, String price) {
        open().selectTab("Nhóm Đồng Phục");
        openCreateDrawer();
        WebElement drawer = groupCreateDrawer();
        fill(groupCreateInput(drawer, "Nhập tên nhóm"),
                name, "Nhập tên nhóm " + name);
        fill(groupCreateInput(drawer, "Nhập giá bán"),
                price, "Nhập giá bán nhóm");
        selectFirstReactOptionByPlaceholder(
                drawer, "Chọn tài khoản nhận tiền",
                "Chọn tài khoản thanh toán");
        submitDrawer(drawer, "Xác nhận tạo nhóm đồng phục");
        if (!waitForOverlayToClose("drawer-Tạo mới nhóm đồng phục")) {
            return false;
        }
        waitForResult();
        return itemExists("Nhóm Đồng Phục", name);
    }

    /**
     * Gửi form nhóm với từng trường có thể được chủ động bỏ trống.
     * Dùng cho testcase validation riêng từng trường bắt buộc.
     */
    public GroupCreateSubmissionSnapshot submitGroupCreateDraft(
            String name, String price, boolean selectPaymentAccount) {
        open().selectTab("Nhóm Đồng Phục");
        openCreateDrawer();
        WebElement drawer = groupCreateDrawer();
        if (name != null) {
            fill(groupCreateInput(drawer, "Nhập tên nhóm"),
                    name, "Nhập tên nhóm kiểm tra validation");
        }
        if (price != null) {
            fill(groupCreateInput(drawer, "Nhập giá bán"),
                    price, "Nhập giá bán nhóm kiểm tra validation");
        }
        if (selectPaymentAccount) {
            selectFirstReactOptionByPlaceholder(
                    drawer, "Chọn tài khoản nhận tiền",
                    "Chọn tài khoản thanh toán");
        }
        submitDrawer(drawer, "Xác nhận form nhóm kiểm tra validation");

        List<WebElement> opened = driver.findElements(By.cssSelector(
                        "[aria-label='drawer-Tạo mới nhóm đồng phục']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
        if (!opened.isEmpty()) {
            return new GroupCreateSubmissionSnapshot(
                    true, elementText(opened.get(0)), false);
        }
        waitForResult();
        boolean created = name != null && !name.isBlank()
                && itemExists("Nhóm Đồng Phục", name);
        return new GroupCreateSubmissionSnapshot(false, "", created);
    }

    /** Tạo nhóm với công tắc Hết hàng đã bật và so sánh trạng thái sau khi mở lại chi tiết. */
    public boolean createOutOfStockGroup(String name, String price) {
        open().selectTab("Nhóm Đồng Phục");
        openCreateDrawer();
        WebElement drawer = groupCreateDrawer();
        fill(groupCreateInput(drawer, "Nhập tên nhóm"),
                name, "Nhập tên nhóm hết hàng " + name);
        fill(groupCreateInput(drawer, "Nhập giá bán"),
                price, "Nhập giá bán nhóm hết hàng");
        selectFirstReactOptionByPlaceholder(
                drawer, "Chọn tài khoản nhận tiền",
                "Chọn tài khoản thanh toán");

        WebElement toggle = outOfStockToggle(drawer);
        String before = toggleFingerprint(toggle);
        click(toggle, "Bật trạng thái hết hàng khi tạo nhóm");
        settle(400);
        String selected = toggleFingerprint(outOfStockToggle(groupCreateDrawer()));
        if (before.equals(selected)) {
            return false;
        }

        submitDrawer(drawer, "Xác nhận tạo nhóm ở trạng thái hết hàng");
        if (!waitForOverlayToClose("drawer-Tạo mới nhóm đồng phục")) {
            return false;
        }
        waitForResult();
        if (!itemExists("Nhóm Đồng Phục", name)) {
            return false;
        }
        openItemDetail(name);
        return selected.equals(toggleFingerprint(
                outOfStockToggle(detailDrawer("Nhóm Đồng Phục"))));
    }

    /** Mở form và điền đủ dữ liệu bắt buộc trước bước chọn ảnh của testcase tạo nhóm. */
    public UniformCatalogPage prepareGroupCreate(String name, String price) {
        open().selectTab("Nhóm Đồng Phục");
        openCreateDrawer();
        WebElement drawer = groupCreateDrawer();
        fill(groupCreateInput(drawer, "Nhập tên nhóm"),
                name, "Nhập tên nhóm có ảnh " + name);
        fill(groupCreateInput(drawer, "Nhập giá bán"),
                price, "Nhập giá bán nhóm có ảnh");
        selectFirstReactOptionByPlaceholder(
                drawer, "Chọn tài khoản nhận tiền",
                "Chọn tài khoản thanh toán");
        pause("Quan sát dữ liệu bắt buộc trước khi chọn ảnh");
        return this;
    }

    /** Xác nhận form nhóm đang mở rồi mở lại chi tiết để kiểm tra ảnh được lưu. */
    public boolean submitOpenedGroupAndVerifyImage(String name) {
        WebElement drawer = groupCreateDrawer();
        submitDrawer(drawer, "Xác nhận tạo nhóm có ảnh đại diện");
        if (!waitForOverlayToClose("drawer-Tạo mới nhóm đồng phục")) {
            return false;
        }
        waitForResult();
        if (!itemExists("Nhóm Đồng Phục", name)) {
            return false;
        }
        openItemDetail(name);
        WebElement detail = detailDrawer("Nhóm Đồng Phục");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return detail.findElements(By.cssSelector("img")).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(imageElement -> Boolean.TRUE.equals(js.executeScript(
                        "return arguments[0].complete && arguments[0].naturalWidth > 0;",
                        imageElement)));
    }

    /** Tạo nhóm có package và trả dữ liệu dùng để xác minh đồng phục đã chọn được lưu. */
    public GroupPackageCreationResult createGroupWithPackage(String name, String price) {
        open().selectTab("Nhóm Đồng Phục");
        openCreateDrawer();
        WebElement drawer = groupCreateDrawer();
        fill(groupCreateInput(drawer, "Nhập tên nhóm"),
                name, "Nhập tên nhóm có package " + name);
        fill(groupCreateInput(drawer, "Nhập giá bán"),
                price, "Nhập giá bán nhóm có package");
        selectFirstReactOptionByPlaceholder(
                drawer, "Chọn tài khoản nhận tiền",
                "Chọn tài khoản thanh toán");
        String selectedUniform = selectFirstReactOptionByPlaceholder(
                drawer, "Chọn đồng phục",
                "Chọn đồng phục đầu tiên cho package");
        pause("Quan sát đồng phục đã chọn trong package");

        submitDrawer(drawer, "Xác nhận tạo nhóm có package");
        if (!waitForOverlayToClose("drawer-Tạo mới nhóm đồng phục")) {
            return new GroupPackageCreationResult(false, selectedUniform, false);
        }
        waitForResult();
        if (!itemExists("Nhóm Đồng Phục", name)) {
            return new GroupPackageCreationResult(false, selectedUniform, false);
        }
        String detail = openItemDetail(name);
        return new GroupPackageCreationResult(
                true, selectedUniform, detail.contains(selectedUniform));
    }

    /** Tải lại trang rồi tìm lại item theo tên để xác minh dữ liệu đã được lưu phía máy chủ. */
    public boolean itemPersistsAfterRefresh(String tab, String name) {
        driver.navigate().refresh();
        pause("Quan sát trang sau khi tải lại");
        waitForResult();
        return itemExists(tab, name);
    }

    /** Tạo thật một sản phẩm không có biến thể bằng dữ liệu sandbox. */
    public boolean createUniformWithoutVariant(String name, String price) {
        open().selectTab("Đồng Phục");
        openCreateDrawer();
        WebElement drawer = uniformCreateDrawer();
        List<WebElement> fields = businessTextInputs(drawer);
        fill(fields.get(0), name, "Nhập tên đồng phục " + name);
        fill(fields.get(1), price, "Nhập giá bán đồng phục");
        chooseRadio(drawer, "Không có biến thể");
        submitDrawer(drawer, "Xác nhận tạo đồng phục không biến thể");
        if (!waitForOverlayToClose("drawer-Tạo mới đồng phục")) {
            return false;
        }
        waitForResult();
        return itemExists("Đồng Phục", name);
    }

    /** Tạo thật sản phẩm có một biến thể Văn bản và một giá trị. */
    public boolean createUniformWithTextVariant(String name, String price) {
        open().selectTab("Đồng Phục");
        openCreateDrawer();
        WebElement drawer = uniformCreateDrawer();
        List<WebElement> fields = businessTextInputs(drawer);
        fill(fields.get(0), name, "Nhập tên đồng phục " + name);
        fill(fields.get(1), price, "Nhập giá bán đồng phục");
        addVariantDraft();

        WebElement row = variantRows(drawer).get(0);
        List<WebElement> variantFields = businessTextInputs(row);
        fill(variantFields.get(0), "Kích thước", "Nhập tên biến thể VI");
        fill(variantFields.get(1), "Size", "Nhập tên biến thể EN");
        chooseFirstVariantType("Văn bản");
        addFirstTextVariantValueDraft();
        variantFields = businessTextInputs(row);
        fill(variantFields.get(variantFields.size() - 1),
                "M", "Nhập giá trị biến thể M");

        submitDrawer(drawer, "Xác nhận tạo đồng phục có biến thể");
        if (!waitForOverlayToClose("drawer-Tạo mới đồng phục")) {
            return false;
        }
        waitForResult();
        return itemExists("Đồng Phục", name);
    }

    /**
     * Gửi form đồng phục với từng trường cơ bản có thể được bỏ trống.
     * Variant label nhận Không có biến thể, Có biến thể hoặc null.
     */
    public ItemCreateSubmissionSnapshot submitItemCreateDraft(
            String name, String price, String variantLabel) {
        open().selectTab("Đồng Phục");
        openCreateDrawer();
        WebElement drawer = uniformCreateDrawer();
        List<WebElement> fields = businessTextInputs(drawer);
        if (name != null) {
            fill(fields.get(0), name,
                    "Nhập tên đồng phục kiểm tra validation");
        }
        if (price != null) {
            fill(fields.get(1), price,
                    "Nhập giá bán đồng phục kiểm tra validation");
        }
        if (variantLabel != null) {
            chooseRadio(drawer, variantLabel);
        }
        submitDrawer(drawer, "Xác nhận form đồng phục kiểm tra validation");
        return itemSubmissionSnapshot(name);
    }

    /** Tạo đồng phục có ảnh thật và trả số preview cùng trạng thái ảnh trong chi tiết. */
    public ItemImageCreationResult createUniformWithImages(
            String name, String price, List<Path> images) {
        open().selectTab("Đồng Phục");
        openCreateDrawer();
        WebElement drawer = uniformCreateDrawer();
        List<WebElement> fields = businessTextInputs(drawer);
        fill(fields.get(0), name, "Nhập tên đồng phục có ảnh " + name);
        fill(fields.get(1), price, "Nhập giá bán đồng phục có ảnh");
        int previewCount = uploadCreateImages(images);
        chooseRadio(drawer, "Không có biến thể");
        submitDrawer(drawer, "Xác nhận tạo đồng phục có ảnh");
        if (!waitForOverlayToClose("drawer-Tạo mới đồng phục")) {
            return new ItemImageCreationResult(false, previewCount, 0);
        }
        waitForResult();
        if (!itemExists("Đồng Phục", name)) {
            return new ItemImageCreationResult(false, previewCount, 0);
        }
        openItemDetail(name);
        int detailImages = loadedImageCount(detailDrawer("Đồng Phục"));
        return new ItemImageCreationResult(true, previewCount, detailImages);
    }

    /** Chuyển Có biến thể về Không có biến thể và trả số dòng còn hiển thị. */
    public int switchBackToNoVariantDraft() {
        WebElement drawer = uniformCreateDrawer();
        chooseRadio(drawer, "Không có biến thể");
        settle(400);
        pause("Quan sát form sau khi chuyển về Không có biến thể");
        return variantRowCount(drawer);
    }

    /**
     * Tạo đồng phục có một hoặc hai nhóm biến thể Văn bản và kiểm tra dữ liệu
     * biến thể được hiển thị lại trong drawer chi tiết.
     */
    public ItemVariantCreationResult createUniformWithTextVariantData(
            String name,
            String price,
            List<String> firstValues,
            boolean includeSecondVariant) {
        open().selectTab("Đồng Phục");
        openCreateDrawer();
        WebElement drawer = uniformCreateDrawer();
        List<WebElement> fields = businessTextInputs(drawer);
        fill(fields.get(0), name, "Nhập tên đồng phục nhiều biến thể " + name);
        fill(fields.get(1), price, "Nhập giá bán đồng phục nhiều biến thể");
        addVariantDraft();

        List<String> expected = new ArrayList<>();
        configureTextVariantRow(
                variantRows(drawer).get(0), "Kích thước", "Size", firstValues);
        expected.add("Kích thước");
        expected.add("Size");
        expected.addAll(firstValues);

        if (includeSecondVariant) {
            addAnotherVariantRow();
            configureTextVariantRow(
                    variantRows(drawer).get(1),
                    "Chất liệu", "Material", List.of("Cotton"));
            expected.addAll(List.of("Chất liệu", "Material", "Cotton"));
        }

        submitDrawer(drawer, "Xác nhận tạo đồng phục nhiều biến thể");
        if (!waitForOverlayToClose("drawer-Tạo mới đồng phục")) {
            return new ItemVariantCreationResult(false, false);
        }
        waitForResult();
        if (!itemExists("Đồng Phục", name)) {
            return new ItemVariantCreationResult(false, false);
        }
        String detail = openItemDetail(name);
        boolean detailContainsExpected = expected.stream().allMatch(detail::contains);
        return new ItemVariantCreationResult(true, detailContainsExpected);
    }

    /** Gửi form có một dòng biến thể Văn bản cố ý thiếu dữ liệu. */
    public ItemCreateSubmissionSnapshot submitIncompleteTextVariant(
            String name,
            boolean fillVietnameseName,
            boolean fillEnglishName,
            boolean addValue,
            boolean fillValue) {
        open().selectTab("Đồng Phục");
        openCreateDrawer();
        WebElement drawer = uniformCreateDrawer();
        List<WebElement> fields = businessTextInputs(drawer);
        fill(fields.get(0), name, "Nhập tên đồng phục validation biến thể");
        fill(fields.get(1), "185000", "Nhập giá bán validation biến thể");
        addVariantDraft();
        WebElement row = variantRows(drawer).get(0);
        List<WebElement> variantFields = businessTextInputs(row);
        if (fillVietnameseName) {
            fill(variantFields.get(0), "Kích thước",
                    "Nhập tên biến thể tiếng Việt");
        }
        if (fillEnglishName) {
            fill(variantFields.get(1), "Size",
                    "Nhập tên biến thể tiếng Anh");
        }
        chooseFirstVariantType("Văn bản");
        if (addValue) {
            addFirstTextVariantValueDraft();
            if (fillValue) {
                variantFields = businessTextInputs(row);
                fill(variantFields.get(variantFields.size() - 1),
                        "M", "Nhập giá trị biến thể");
            }
        }
        submitDrawer(drawer, "Xác nhận form thiếu dữ liệu biến thể");
        return itemSubmissionSnapshot(name);
    }

    /** Gửi form có dòng biến thể nhưng chưa chọn loại Màu sắc/Văn bản. */
    public ItemCreateSubmissionSnapshot submitVariantWithoutType(String name) {
        open().selectTab("Đồng Phục");
        openCreateDrawer();
        WebElement drawer = uniformCreateDrawer();
        List<WebElement> fields = businessTextInputs(drawer);
        fill(fields.get(0), name, "Nhập tên đồng phục thiếu loại biến thể");
        fill(fields.get(1), "185000", "Nhập giá bán đồng phục thiếu loại biến thể");
        addVariantDraft();
        WebElement row = variantRows(drawer).get(0);
        List<WebElement> variantFields = businessTextInputs(row);
        fill(variantFields.get(0), "Kích thước", "Nhập tên biến thể tiếng Việt");
        fill(variantFields.get(1), "Size", "Nhập tên biến thể tiếng Anh");
        submitDrawer(drawer, "Xác nhận form chưa chọn loại biến thể");
        return itemSubmissionSnapshot(name);
    }

    /** Đổi tên item thật và xác minh danh sách trả tên mới. */
    public boolean renameItem(String tab, String oldName, String newName) {
        open().selectTab(tab);
        search(oldName);
        openItemDetail(oldName);
        WebElement drawer = detailDrawer(tab);
        List<WebElement> fields = businessTextInputs(drawer);
        if (fields.isEmpty()) {
            throw new IllegalStateException("Drawer chi tiết " + tab + " thiếu ô tên.");
        }
        fill(fields.get(0), newName, "Đổi tên thành " + newName);
        submitDrawer(drawer, "Xác nhận cập nhật " + tab);
        if (!waitForOverlayToClose(drawer.getAttribute("aria-label"))) {
            return false;
        }
        waitForResult();
        return itemExists(tab, newName);
    }

    /** Đổi trạng thái hết hàng của nhóm thật và xác minh trạng thái được lưu. */
    public boolean toggleGroupOutOfStockPersists(String name) {
        open().selectTab("Nhóm Đồng Phục");
        search(name);
        openItemDetail(name);
        WebElement drawer = detailDrawer("Nhóm Đồng Phục");
        WebElement toggle = outOfStockToggle(drawer);
        String before = toggleFingerprint(toggle);
        click(toggle, "Đổi trạng thái hết hàng của nhóm");
        settle(300);
        String changed = toggleFingerprint(outOfStockToggle(drawer));
        if (before.equals(changed)) {
            return false;
        }
        submitDrawer(drawer, "Xác nhận cập nhật trạng thái hết hàng");
        if (!waitForOverlayToClose(drawer.getAttribute("aria-label"))) {
            return false;
        }

        open().selectTab("Nhóm Đồng Phục");
        search(name);
        openItemDetail(name);
        String persisted = toggleFingerprint(
                outOfStockToggle(detailDrawer("Nhóm Đồng Phục")));
        return changed.equals(persisted);
    }

    /** Mở xác nhận xóa rồi bấm Hủy, bảo đảm item vẫn còn. */
    public boolean cancelDeleteItem(String tab, String name) {
        open().selectTab(tab);
        search(name);
        openItemDetail(name);
        WebElement drawer = detailDrawer(tab);
        String deleteLabel = tab.equals("Nhóm Đồng Phục")
                ? "Xóa nhóm đồng phục" : "Xóa đồng phục";
        click(buttonIn(drawer, deleteLabel), "Mở xác nhận " + deleteLabel);
        WebElement dialog = visibleConfirmationDialog();
        WebElement cancel = dialog.findElements(By.cssSelector("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> {
                    String text = button.getText().trim();
                    return text.equals("Hủy") || text.equals("Trở về");
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Popup xóa thiếu nút Hủy/Trở về."));
        click(cancel, "Hủy thao tác xóa");
        wait.until(d -> visibleElements(By.cssSelector("[role='dialog']")).isEmpty());
        closeOverlay();
        return itemExists(tab, name);
    }

    /** Xác nhận xóa thật item sandbox và kiểm tra item biến mất khỏi danh sách. */
    public boolean deleteItem(String tab, String name) {
        open().selectTab(tab);
        search(name);
        if (displayedItemNames().stream().noneMatch(item ->
                TextNormalizer.normalize(item).equals(TextNormalizer.normalize(name)))) {
            return true;
        }
        openItemDetail(name);
        WebElement drawer = detailDrawer(tab);
        String deleteLabel = tab.equals("Nhóm Đồng Phục")
                ? "Xóa nhóm đồng phục" : "Xóa đồng phục";
        click(buttonIn(drawer, deleteLabel), "Mở xác nhận " + deleteLabel);
        WebElement dialog = visibleConfirmationDialog();
        WebElement confirm = dialog.findElements(By.cssSelector("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> {
                    String text = button.getText().trim();
                    return !text.equals("Hủy")
                            && (text.contains("Xóa") || text.contains("Xác nhận"));
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Popup xóa không có nút xác nhận."));
        click(confirm, "Xác nhận xóa thật " + name);
        waitForResult();
        return !itemExists(tab, name);
    }

    /** Tìm item theo tên chính xác trong tab tương ứng. */
    public boolean itemExists(String tab, String name) {
        open().selectTab(tab);
        search(name);
        String expected = TextNormalizer.normalize(name);
        return displayedItemNames().stream().anyMatch(item ->
                TextNormalizer.normalize(item).equals(expected));
    }

    /** Kiểm tra mọi ảnh card đang hiển thị đã tải thành công, không chỉ tồn tại thẻ img. */
    public boolean displayedCardImagesLoaded() {
        List<WebElement> images = visibleElements(By.cssSelector(
                "main div.cursor-pointer img"));
        if (images.isEmpty()) {
            return false;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return images.stream().allMatch(image -> Boolean.TRUE.equals(js.executeScript(
                "return arguments[0].complete && arguments[0].naturalWidth > 0;",
                image)));
    }

    /** Chuyển sang trang kế tiếp thật và trả trạng thái sau điều hướng. */
    public PaginationWindow goToNextPage() {
        PaginationSnapshot before = pagination();
        List<String> namesBefore = displayedItemNames();
        WebElement next = visible(By.cssSelector(
                "main [aria-label='next page button']"));
        if ("true".equals(next.getAttribute("aria-disabled"))) {
            throw new IllegalStateException("Không có trang kế tiếp.");
        }
        click(next, "Chuyển sang trang " + (before.activePage() + 1));
        wait.until(d -> pagination().activePage() == before.activePage() + 1);
        waitForResult();
        return new PaginationWindow(
                before.activePage(), pagination().activePage(),
                namesBefore, displayedItemNames());
    }

    /** Quay lại trang trước thật và trả trạng thái sau điều hướng. */
    public PaginationWindow goToPreviousPage() {
        PaginationSnapshot before = pagination();
        List<String> namesBefore = displayedItemNames();
        WebElement previous = visible(By.cssSelector(
                "main [aria-label='previous page button']"));
        if ("true".equals(previous.getAttribute("aria-disabled"))) {
            throw new IllegalStateException("Không có trang trước.");
        }
        click(previous, "Quay lại trang " + (before.activePage() - 1));
        wait.until(d -> pagination().activePage() == before.activePage() - 1);
        waitForResult();
        return new PaginationWindow(
                before.activePage(), pagination().activePage(),
                namesBefore, displayedItemNames());
    }

    /** Trả tổng số bản ghi mà UI công bố. */
    public int totalDisplayed() {
        String text = mainText();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("Tổng hiển thị:\\s*([\\d.]+)")
                .matcher(text);
        return matcher.find()
                ? Integer.parseInt(matcher.group(1).replace(".", ""))
                : 0;
    }

    /** Đọc trạng thái điều hướng phân trang hiện tại. */
    public PaginationSnapshot pagination() {
        WebElement navigation = visible(By.cssSelector(
                "main nav[aria-label='pagination navigation']"));
        WebElement previous = navigation.findElement(By.cssSelector(
                "[aria-label='previous page button']"));
        WebElement next = navigation.findElement(By.cssSelector(
                "[aria-label='next page button']"));
        int activePage = Integer.parseInt(capture(
                navigation.findElement(By.cssSelector("[aria-current='true']"))
                        .getAttribute("aria-label"),
                "pagination item\\s+(\\d+)"));
        int pageCount = navigation.findElements(By.cssSelector(
                        "[aria-label^='pagination item']"))
                .stream().filter(WebElement::isDisplayed).toList().size();
        return new PaginationSnapshot(
                activePage,
                pageCount,
                "true".equals(previous.getAttribute("aria-disabled")),
                "true".equals(next.getAttribute("aria-disabled")));
    }

    private WebElement currentCreateDrawer() {
        return visible(By.cssSelector("[aria-label^='drawer-Tạo mới']"));
    }

    private WebElement filterPopup() {
        return visible(By.cssSelector(
                "[data-slot='content'][data-open='true'],[role='dialog']"));
    }

    private WebElement filterPopupOrOpen() {
        WebElement opened = driver.findElements(By.cssSelector(
                        "[data-slot='content'][data-open='true'],[role='dialog']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
        if (opened != null) {
            return opened;
        }
        click(visible(FILTER), "Mở bộ lọc " + selectedTab());
        pause("Hiển thị các tùy chọn bộ lọc");
        return filterPopup();
    }

    private WebElement groupCreateDrawer() {
        return visible(By.cssSelector(
                "[aria-label='drawer-Tạo mới nhóm đồng phục']"));
    }

    private WebElement uniformCreateDrawer() {
        return visible(By.cssSelector("[aria-label='drawer-Tạo mới đồng phục']"));
    }

    private WebElement detailDrawer(String tab) {
        String label = tab.equals("Nhóm Đồng Phục")
                ? "drawer-Chi tiết nhóm đồng phục"
                : "drawer-Chi tiết đồng phục";
        return visible(By.cssSelector("[aria-label='" + label + "']"));
    }

    private WebElement buttonIn(WebElement container, String text) {
        return container.findElements(By.xpath(
                        ".//button[normalize-space()=" + xpathLiteral(text) + "]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy nút " + text + "."));
    }

    private boolean hasVisibleButton(WebElement container, String text) {
        return container.findElements(By.xpath(
                        ".//button[normalize-space()=" + xpathLiteral(text) + "]"))
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    private boolean acceptsImages(WebElement upload) {
        String accept = upload.getAttribute("accept");
        return accept != null && accept.toLowerCase().contains("image/");
    }

    private String selectFirstReactOption(
            WebElement drawer, int comboboxIndex, String step) {
        List<WebElement> comboboxes = drawer.findElements(By.cssSelector(
                        "input[role='combobox']"))
                .stream().filter(WebElement::isDisplayed).toList();
        if (comboboxes.size() <= comboboxIndex) {
            throw new IllegalStateException(
                    "Drawer chỉ có " + comboboxes.size()
                            + " combobox, không thể chọn vị trí " + comboboxIndex);
        }
        WebElement combo = comboboxes.get(comboboxIndex);
        return selectFirstReactOption(combo, step);
    }

    /** Chọn theo placeholder để không phụ thuộc index sau khi React Select ẩn input đã chọn. */
    private String selectFirstReactOptionByPlaceholder(
            WebElement drawer, String placeholder, String step) {
        WebElement combo = drawer.findElements(By.cssSelector(
                        "input[role='combobox']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(input -> reactComboboxMatchesPlaceholder(
                        drawer, input, placeholder))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy combobox " + placeholder + "."));
        return selectFirstReactOption(combo, step);
    }

    /** Thao tác chung trên một input React Select đã được định vị chính xác. */
    private String selectFirstReactOption(WebElement combo, String step) {
        click(combo, step);
        String selectedText;
        try {
            WebElement option = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, Duration.ofSeconds(5))
                    .until(d -> d.findElements(By.cssSelector("[role='option']")).stream()
                            .filter(WebElement::isDisplayed)
                            .findFirst().orElse(null));
            selectedText = option.getText().trim();
            click(option, step);
        } catch (TimeoutException noOptionsRendered) {
            combo.sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
            selectedText = "";
        }
        settle(400);
        if (selectedText.isBlank()) {
            selectedText = combo.findElements(By.xpath(
                            "./ancestor::div[contains(@class,'control')][1]"))
                    .stream()
                    .map(WebElement::getText)
                    .map(String::trim)
                    .filter(text -> !text.isBlank())
                    .findFirst()
                    .orElse("");
        }
        return selectedText;
    }

    /** Đối chiếu aria-describedby hoặc nội dung control với placeholder nghiệp vụ. */
    private boolean reactComboboxMatchesPlaceholder(
            WebElement drawer, WebElement combo, String placeholder) {
        String describedBy = combo.getAttribute("aria-describedby");
        if (describedBy != null && !describedBy.isBlank()) {
            boolean describedTextMatches = drawer.findElements(By.xpath(
                            ".//*[@id=" + xpathLiteral(describedBy) + "]"))
                    .stream()
                    .map(WebElement::getText)
                    .anyMatch(text -> text.contains(placeholder));
            if (describedTextMatches) {
                return true;
            }
        }
        return combo.findElements(By.xpath(
                        "./ancestor::div[contains(@class,'control')][1]"))
                .stream()
                .map(WebElement::getText)
                .anyMatch(text -> text.contains(placeholder));
    }

    /** Lấy input nhóm theo aria-label ổn định thay vì vị trí trong danh sách input. */
    private WebElement groupCreateInput(WebElement drawer, String ariaLabel) {
        return drawer.findElements(By.cssSelector(
                        "input[aria-label='" + ariaLabel + "']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Form tạo nhóm thiếu ô " + ariaLabel + "."));
    }

    private int uploadedPreviewCount(WebElement drawer) {
        return (int) drawer.findElements(By.cssSelector("img")).stream()
                .filter(WebElement::isDisplayed)
                .count();
    }

    private int loadedImageCount(WebElement container) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (int) container.findElements(By.cssSelector("img")).stream()
                .filter(WebElement::isDisplayed)
                .filter(image -> Boolean.TRUE.equals(js.executeScript(
                        "return arguments[0].complete && arguments[0].naturalWidth > 0;",
                        image)))
                .count();
    }

    /** Điền tên, loại và các giá trị cho một dòng biến thể Văn bản. */
    private void configureTextVariantRow(
            WebElement row,
            String vietnameseName,
            String englishName,
            List<String> values) {
        List<WebElement> fields = businessTextInputs(row);
        fill(fields.get(0), vietnameseName,
                "Nhập tên biến thể VI " + vietnameseName);
        fill(fields.get(1), englishName,
                "Nhập tên biến thể EN " + englishName);
        click(buttonIn(row, "Văn bản"), "Chọn loại biến thể Văn bản");
        for (String value : values) {
            click(buttonIn(row, "Thêm"), "Thêm giá trị biến thể " + value);
            settle(300);
            fields = businessTextInputs(row);
            WebElement valueField = fields.stream()
                    .filter(field -> {
                        String current = field.getAttribute("value");
                        return current == null || current.isBlank();
                    })
                    .reduce((first, second) -> second)
                    .orElseThrow(() -> new IllegalStateException(
                            "Không tìm thấy ô giá trị biến thể vừa thêm."));
            fill(valueField, value, "Nhập giá trị biến thể " + value);
        }
    }

    /** Đọc trạng thái form đồng phục sau khi bấm Xác nhận. */
    private ItemCreateSubmissionSnapshot itemSubmissionSnapshot(String name) {
        List<WebElement> opened = driver.findElements(By.cssSelector(
                        "[aria-label='drawer-Tạo mới đồng phục']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
        if (!opened.isEmpty()) {
            return new ItemCreateSubmissionSnapshot(
                    true, elementText(opened.get(0)), false);
        }
        waitForResult();
        boolean created = name != null && !name.isBlank()
                && itemExists("Đồng Phục", name);
        return new ItemCreateSubmissionSnapshot(false, "", created);
    }

    private List<WebElement> variantRows(WebElement drawer) {
        return drawer.findElements(By.xpath(
                        ".//label[contains(normalize-space(),'Tên biến thể (VI)')]"
                                + "/ancestor::div[contains(@class,'rounded-xl')][1]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    private int variantRowCount(WebElement drawer) {
        return variantRows(drawer).size();
    }

    private List<WebElement> businessTextInputs(WebElement drawer) {
        return drawer.findElements(By.cssSelector(
                        "input[type='text']:not([role='combobox'])"))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    private void chooseRadio(WebElement drawer, String label) {
        WebElement choice = drawer.findElements(By.xpath(
                        ".//label[.//*[normalize-space()="
                                + xpathLiteral(label) + "]]"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseGet(() -> drawer.findElement(By.xpath(
                        ".//*[normalize-space()=" + xpathLiteral(label) + "]")));
        click(choice, "Chọn " + label);
        settle(300);
    }

    private void submitDrawer(WebElement drawer, String step) {
        click(buttonIn(drawer, "Xác nhận"), step);
        try {
            org.openqa.selenium.Alert alert =
                    new org.openqa.selenium.support.ui.WebDriverWait(
                            driver, Duration.ofSeconds(3))
                            .until(org.openqa.selenium.support.ui.ExpectedConditions
                                    .alertIsPresent());
            System.out.println("[XAC NHAN CANH BAO] "
                    + TextNormalizer.normalize(alert.getText()));
            alert.accept();
            pause("Xác nhận cảnh báo tạo đồng phục");
        } catch (TimeoutException ignored) {
            // Form nhóm và validation lỗi không mở cảnh báo trình duyệt.
        }
        settle(800);
        pause("Quan sát kết quả " + step);
    }

    private boolean waitForOverlayToClose(String ariaLabel) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, Duration.ofSeconds(15))
                    .until(d -> d.findElements(By.cssSelector(
                                    "[aria-label='" + ariaLabel + "']"))
                            .stream().noneMatch(WebElement::isDisplayed));
            return true;
        } catch (TimeoutException exception) {
            return false;
        }
    }

    private WebElement visibleConfirmationDialog() {
        return wait.until(d -> d.findElements(By.cssSelector(
                        "[role='dialog'],[data-slot='base'][data-open='true']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getText().contains("Xóa"))
                .findFirst()
                .orElse(null));
    }

    private WebElement outOfStockToggle(WebElement drawer) {
        return drawer.findElements(By.xpath(
                        ".//*[normalize-space()='Trạng thái hết hàng']"
                                + "/following::button[1]"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Drawer nhóm thiếu công tắc Trạng thái hết hàng."));
    }

    private String toggleFingerprint(WebElement toggle) {
        String childClass = toggle.findElements(By.xpath("./*")).stream()
                .findFirst()
                .map(child -> child.getAttribute("class"))
                .orElse("");
        return toggle.getAttribute("class") + "|" + childClass;
    }

    private static String capture(String text, String expression) {
        Matcher matcher = Pattern.compile(expression,
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    /** Dữ liệu đã tách từ một card nhóm/sản phẩm. */
    public record CatalogCard(
            String name,
            String price,
            String quantity,
            boolean hasImage,
            boolean hasUpdater,
            String raw) {
    }

    /** Trạng thái điều khiển phân trang. */
    public record PaginationSnapshot(
            int activePage,
            int pageCount,
            boolean previousDisabled,
            boolean nextDisabled) {
    }

    /** Cửa sổ dữ liệu trước và sau một lần chuyển trang thật. */
    public record PaginationWindow(
            int beforePage,
            int afterPage,
            List<String> beforeNames,
            List<String> afterNames) {
    }

    /** Cấu trúc điều khiển của drawer tạo nhóm đồng phục. */
    public record GroupCreateFormSnapshot(
            int businessTextInputCount,
            int comboboxCount,
            boolean imageUpload,
            boolean multipleUpload,
            boolean outOfStockToggle,
            boolean emptyPackageMessage,
            boolean cancelButton,
            boolean confirmButton) {
    }

    /** Cấu trúc điều khiển của drawer tạo mới đồng phục. */
    public record UniformCreateFormSnapshot(
            int businessTextInputCount,
            int variantChoiceCount,
            int selectedVariantChoiceCount,
            boolean imageUpload,
            boolean multipleUpload,
            boolean fiveImageHint,
            boolean cancelButton,
            boolean confirmButton) {
    }

    /** Kết quả gửi form nhóm dùng cho validation từng trường bắt buộc. */
    public record GroupCreateSubmissionSnapshot(
            boolean drawerOpen,
            String content,
            boolean created) {
    }

    /** Kết quả tạo nhóm có package và dữ liệu đồng phục được chọn. */
    public record GroupPackageCreationResult(
            boolean created,
            String selectedUniform,
            boolean detailContainsSelectedUniform) {
    }

    /** Kết quả gửi form tạo đồng phục dùng cho validation từng điều kiện. */
    public record ItemCreateSubmissionSnapshot(
            boolean drawerOpen,
            String content,
            boolean created) {
    }

    /** Kết quả tạo đồng phục có ảnh và số ảnh quan sát được. */
    public record ItemImageCreationResult(
            boolean created,
            int previewCount,
            int detailLoadedImageCount) {
    }

    /** Kết quả tạo đồng phục có biến thể và dữ liệu đọc lại từ chi tiết. */
    public record ItemVariantCreationResult(
            boolean created,
            boolean detailContainsExpectedValues) {
    }
}
