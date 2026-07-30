package com.vuatho.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final Map<String, Integer> unfilteredTotals = new HashMap<>();

    public UniformCatalogPage(WebDriver driver) {
        super(driver);
    }

    /** Mở trang quản lí danh mục đồng phục. */
    public UniformCatalogPage open() {
        openRoute(ROUTE);
        waitForResult();
        rememberUnfilteredTotal();
        return this;
    }

    /** Chọn tab Nhóm Đồng Phục hoặc Đồng Phục. */
    public UniformCatalogPage selectTab(String tab) {
        String dataKey = switch (tab) {
            case "Nhóm Đồng Phục" -> "group";
            case "Đồng Phục" -> "uniform";
            default -> throw new IllegalArgumentException("Tab không được hỗ trợ: " + tab);
        };
        By locator = By.cssSelector("main [role='tab'][data-key='" + dataKey + "']");
        WebElement control = visible(locator);
        if ("true".equals(control.getAttribute("aria-selected"))) {
            rememberUnfilteredTotal();
            return this;
        }
        click(control, "Chọn tab " + tab);
        try {
            waitForSelectedTab(locator);
        } catch (TimeoutException firstAttempt) {
            // React Aria đôi lúc bỏ qua click khi animation của lần mở route chưa kết thúc.
            click(visible(locator), "Chọn lại tab " + tab);
            waitForSelectedTab(locator);
        }
        waitForResult();
        String expectedSearchLabel = tab.equals("Nhóm Đồng Phục")
                ? "Tìm kiếm nhóm đồng phục"
                : "Tìm kiếm đồng phục";
        wait.until(d -> expectedSearchLabel.equals(searchPlaceholder()));
        rememberUnfilteredTotal();
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

    /** Trả nội dung hiện tại của ô tìm kiếm mà không đọc dữ liệu nhạy cảm khác. */
    public String searchValue() {
        String value = visible(SEARCH).getAttribute("value");
        return value == null ? "" : value;
    }

    /** Đếm số tab được DOM đánh dấu đang chọn; tại mọi thời điểm phải đúng một tab. */
    public int selectedTabCount() {
        return driver.findElements(By.cssSelector(
                "main [role='tab'][aria-selected='true']")).size();
    }

    /** Tìm theo tên và chờ danh sách trả dữ liệu. */
    public UniformCatalogPage search(String keyword) {
        int before = totalDisplayed();
        WebElement input = visible(SEARCH);
        fill(input, keyword, "Nhập từ khóa " + keyword);
        String expected = keyword.toLowerCase(java.util.Locale.ROOT);
        new WebDriverWait(driver, Duration.ofSeconds(12))
                .pollingEvery(Duration.ofMillis(250))
                .until(d -> searchValue().equals(keyword)
                        && (totalDisplayed() != before
                        || displayedItemNames().stream().allMatch(name ->
                        name.toLowerCase(java.util.Locale.ROOT).contains(expected))));
        waitForResult();
        return this;
    }

    /** Reset tìm kiếm và bộ lọc. */
    public UniformCatalogPage reset() {
        click(visible(RESET), "Đặt lại tìm kiếm và bộ lọc");
        Integer expectedTotal = unfilteredTotals.get(selectedTab());
        if (expectedTotal != null) {
            new WebDriverWait(driver, Duration.ofSeconds(12))
                    .pollingEvery(Duration.ofMillis(250))
                    .until(d -> searchValue().isBlank()
                            && totalDisplayed() == expectedTotal);
        }
        waitForResult();
        return this;
    }

    /** Mở bộ lọc và trả nội dung tùy chọn. */
    public String openFilter() {
        click(visible(FILTER), "Mở bộ lọc " + selectedTab());
        WebElement popup = activeFilterPopup();
        pause("Hiển thị các tùy chọn bộ lọc");
        return elementText(popup);
    }

    /** Đếm radio tồn kho trong popup đang mở để kiểm tra khả năng truy cập bằng DOM. */
    public int visibleInventoryFilterRadioCount() {
        WebElement popup = activeFilterPopup();
        return popup.findElements(By.cssSelector("input[type='radio']")).stream()
                .toList()
                .size();
    }

    /** Đóng popup lọc mà không áp dụng thêm thay đổi. */
    public UniformCatalogPage closeFilter() {
        click(visible(FILTER), "Đóng bộ lọc " + selectedTab());
        wait.until(d -> d.findElements(By.cssSelector(
                        "[data-slot='content'][data-open='true']"))
                .stream()
                .noneMatch(element -> {
                    String text = element.getAttribute("innerText");
                    return text != null && text.contains("Trạng thái tồn kho");
                }));
        return this;
    }

    /** Chọn một tùy chọn đang hiển thị trong popup lọc. */
    public UniformCatalogPage chooseFilter(String option) {
        WebElement popup = activeFilterPopup();
        WebElement item = popup.findElements(By.xpath(
                        ".//*[normalize-space()=" + xpathLiteral(option)
                                + " and (self::span or self::div or self::button or self::label)]"))
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy tùy chọn lọc " + option));
        click(item, "Chọn bộ lọc " + option);
        waitForResult();
        return this;
    }

    /** Mở drawer tạo mới của tab hiện tại. */
    public String openCreateDrawer() {
        click(visible(CREATE), "Mở form tạo mới " + selectedTab());
        String expected = selectedTab().equals("Nhóm Đồng Phục")
                ? "drawer-Tạo mới nhóm đồng phục"
                : "drawer-Tạo mới đồng phục";
        WebElement drawer = activeDrawer("drawer-Tạo mới");
        if (!expected.equals(drawer.getAttribute("aria-label"))) {
            throw new IllegalStateException("Mở sai drawer: "
                    + drawer.getAttribute("aria-label"));
        }
        pause("Hiển thị đầy đủ form tạo mới");
        return elementText(drawer);
    }

    /** Trả aria-label của drawer tạo mới đang hiển thị. */
    public String visibleCreateDrawerLabel() {
        return activeDrawer("drawer-Tạo mới")
                .getAttribute("aria-label");
    }

    /** Đọc ràng buộc input upload của drawer mà không tải file lên. */
    public List<UploadConstraint> createDrawerUploadConstraints() {
        WebElement drawer = activeDrawer("drawer-Tạo mới");
        return drawer.findElements(By.cssSelector("input[type='file']")).stream()
                .map(input -> new UploadConstraint(
                        input.getAttribute("accept"),
                        "true".equals(input.getAttribute("multiple"))))
                .toList();
    }

    /** Hủy drawer tạo mới và chờ overlay đóng hoàn toàn. */
    public UniformCatalogPage cancelCreateDrawer() {
        WebElement drawer = activeDrawer("drawer-Tạo mới");
        WebElement cancel = drawer.findElements(By.tagName("button")).stream()
                .filter(button -> button.getText().trim().equals("Hủy"))
                .findFirst()
                .orElseThrow();
        click(cancel, "Hủy form tạo mới");
        wait.until(d -> d.findElements(By.cssSelector("[aria-label^='drawer-Tạo mới']"))
                .stream().noneMatch(this::drawerIsOpen));
        return this;
    }

    /** Cho biết có drawer danh mục nào còn hiển thị hay không. */
    public boolean hasVisibleDrawer() {
        return driver.findElements(By.cssSelector("[aria-label^='drawer-']")).stream()
                .anyMatch(this::drawerIsOpen);
    }

    /** Đóng drawer chi tiết và chờ danh sách nhận lại tương tác. */
    public UniformCatalogPage closeDrawer() {
        closeOverlay();
        wait.until(d -> d.findElements(By.cssSelector("[aria-label^='drawer-']"))
                .stream().noneMatch(this::drawerIsOpen));
        return this;
    }

    /** Kiểm tra nút xác nhận của drawer tạo mới có bị khóa khi thiếu dữ liệu. */
    public boolean createConfirmDisabled() {
        WebElement drawer = activeDrawer("drawer-Tạo mới");
        WebElement confirm = drawer.findElements(By.tagName("button")).stream()
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
        WebElement drawer = activeDrawer("drawer-Tạo mới");
        if (!drawerLabel.equals(drawer.getAttribute("aria-label"))) {
            throw new IllegalStateException("Mở sai drawer: "
                    + drawer.getAttribute("aria-label"));
        }
        WebElement confirm = drawer.findElements(By.tagName("button")).stream()
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
        WebElement drawer = activeDrawer("drawer-Tạo mới");
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

    /**
     * Tạo mới tối thiểu một nhóm hoặc sản phẩm bằng UI và chờ dữ liệu xuất hiện.
     * Tên truyền vào phải là dữ liệu automation duy nhất để cleanup an toàn.
     */
    public UniformCatalogPage createMinimalItem(
            String tab, String name, String price) {
        open().selectTab(tab);
        openCreateDrawer();
        WebElement drawer = activeDrawer("drawer-Tạo mới");
        fill(labeledInput(drawer, itemNameLabel(tab)), name,
                "Nhập tên dữ liệu CRUD " + name);
        fill(labeledInput(drawer, "Giá bán"), price,
                "Nhập giá bán " + price);

        if ("Nhóm Đồng Phục".equals(tab)) {
            chooseFirstSelectOption(drawer, "Tài khoản thanh toán");
        } else {
            chooseRadio(drawer, "Không có biến thể");
        }

        drawer = activeDrawer("drawer-Tạo mới");
        WebElement confirm = actionButton(drawer, List.of("Xác nhận"));
        if ("Nhóm Đồng Phục".equals(tab)) {
            click(confirm, "Xác nhận tạo mới " + name);
        } else {
            clickAndAcceptOptionalAlert(
                    confirm, "Xác nhận tạo mới " + name);
        }
        waitForDrawerToClose("drawer-Tạo mới");
        waitForResult();
        waitUntilItemPresence(tab, name, true);
        return this;
    }

    /** Cập nhật tên và giá qua drawer chi tiết rồi xác minh dữ liệu mới xuất hiện. */
    public UniformCatalogPage updateItem(
            String tab,
            String currentName,
            String updatedName,
            String updatedPrice) {
        open().selectTab(tab);
        search(currentName);
        openItemDetail(currentName);
        WebElement drawer = activeDrawer("drawer-Chi tiết");
        WebElement currentNameInput = labeledInput(drawer, itemNameLabel(tab));
        boolean inlineEdit = currentNameInput.isEnabled()
                && !"true".equals(safeAttribute(currentNameInput, "readonly"))
                && !"true".equals(safeAttribute(currentNameInput, "disabled"));
        if (!inlineEdit) {
            click(actionButton(drawer, List.of("Chỉnh sửa", "Sửa")),
                    "Mở chế độ sửa " + currentName);
            settle(400);
        }
        drawer = activeDrawer("drawer-");
        fill(labeledInput(drawer, itemNameLabel(tab)), updatedName,
                "Đổi tên thành " + updatedName);
        fill(labeledInput(drawer, "Giá bán"), updatedPrice,
                "Đổi giá bán thành " + updatedPrice);
        drawer = activeDrawer("drawer-");
        click(actionButton(
                        drawer,
                        List.of("Lưu thay đổi", "Cập nhật", "Xác nhận")),
                "Lưu thay đổi " + updatedName);
        settle(700);
        if (hasVisibleDrawer()) {
            closeDrawer();
        }
        waitForResult();
        waitUntilItemPresence(tab, updatedName, true);
        return this;
    }

    /** Mở lại route và trả nội dung chi tiết hiện đang lưu của một item. */
    public String readItemDetail(String tab, String name) {
        open().selectTab(tab);
        search(name);
        return openItemDetail(name);
    }

    /** Xóa item qua drawer chi tiết và xác minh dữ liệu không còn trong danh sách. */
    public UniformCatalogPage deleteItem(String tab, String name) {
        open().selectTab(tab);
        search(name);
        openItemDetail(name);
        WebElement drawer = activeDrawer("drawer-Chi tiết");
        String specificDelete = "Nhóm Đồng Phục".equals(tab)
                ? "Xóa nhóm đồng phục"
                : "Xóa đồng phục";
        click(actionButton(drawer, List.of(specificDelete, "Xóa")),
                "Yêu cầu xóa " + name);
        settle(500);
        confirmDeleteIfRequested();
        waitForDrawerToClose("drawer-");
        waitForResult();
        waitUntilItemPresence(tab, name, false);
        return this;
    }

    /** Kiểm tra item theo tên đầy đủ trên đúng tab. */
    public boolean itemExists(String tab, String name) {
        open().selectTab(tab);
        search(name);
        String expected = normalizeForComparison(name);
        return displayedItemNames().stream()
                .map(UniformCatalogPage::normalizeForComparison)
                .anyMatch(expected::equals);
    }

    /**
     * Cleanup best-effort chỉ áp dụng cho dữ liệu có prefix AUTO-.
     * Không bao giờ xóa dữ liệu nghiệp vụ có sẵn khi testcase fail.
     */
    public void deleteAutomationItemIfPresent(String tab, String name) {
        if (name == null || !name.startsWith("AUTO-")) {
            throw new IllegalArgumentException(
                    "Cleanup chỉ cho phép xóa dữ liệu AUTO-: " + name);
        }
        try {
            if (itemExists(tab, name)) {
                deleteItem(tab, name);
            }
        } catch (RuntimeException cleanupError) {
            System.err.println("[CRUD CLEANUP] Không thể xóa " + name
                    + ": " + cleanupError.getMessage());
        }
    }

    /** Lấy tên item đầu tiên của tab hiện tại từ card dữ liệu. */
    public String firstItemName() {
        String marker = selectedTab().equals("Nhóm Đồng Phục") ? "Số đồng phục" : "Tồn kho";
        WebElement markerElement = visible(By.xpath(
                "//main//*[normalize-space()=" + xpathLiteral(marker) + "][1]"));
        WebElement card = markerElement.findElement(By.xpath(
                "./ancestor::*[contains(@class,'rounded')][1]"));
        List<String> lines = card.getText().lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
        return lines.isEmpty() ? "" : lines.get(0);
    }

    /** Trả danh sách tên item đang hiển thị dựa trên từng card có giá bán. */
    public List<String> displayedItemNames() {
        List<String> names = new ArrayList<>();
        for (WebElement priceMarker : visibleElements(By.xpath(
                "//main//*[normalize-space()='Giá bán']"))) {
            try {
                WebElement card = priceMarker.findElement(By.xpath(
                        "./ancestor::*[contains(@class,'rounded')][1]"));
                if (!isInViewport(card)) {
                    continue;
                }
                String name = card.getText().lines()
                        .map(String::trim)
                        .filter(line -> !line.isBlank())
                        .findFirst().orElse("");
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
                if (!isInViewport(card)) {
                    continue;
                }
                String raw = card.getText().trim();
                String name = raw.lines().map(String::trim)
                        .filter(line -> !line.isBlank()).findFirst().orElse("");
                String price = capture(raw,
                        "Giá bán\\s*([\\d.,]+)\\s*(?:VND|₫)");
                String quantity = capture(raw,
                        Pattern.quote(quantityLabel) + "\\s*([\\d.]+)");
                boolean hasImage = !card.findElements(By.cssSelector("img")).isEmpty();
                boolean hasAuditInfo = raw.contains("Cập nhật bởi")
                        || raw.contains("Tạo bởi");
                if (!name.isBlank()) {
                    cards.add(new CatalogCard(
                            name, price, quantity, hasImage, hasAuditInfo, raw));
                }
            } catch (RuntimeException ignored) {
                // Bỏ qua node trang trí không thuộc card dữ liệu.
            }
        }
        return cards;
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
        WebElement drawer = activeDrawer("drawer-Chi tiết");
        if (!drawerLabel.equals(drawer.getAttribute("aria-label"))) {
            throw new IllegalStateException("Mở sai drawer: "
                    + drawer.getAttribute("aria-label"));
        }
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

    /** Sang trang kế tiếp khi điều khiển phân trang cho phép. */
    public UniformCatalogPage nextPage() {
        WebElement next = visible(By.cssSelector(
                "main nav[aria-label='pagination navigation']"
                        + " [aria-label='next page button']"));
        if ("true".equals(next.getAttribute("aria-disabled"))) {
            throw new IllegalStateException("Trang kế tiếp đang bị khóa.");
        }
        int currentPage = pagination().activePage();
        click(next, "Sang trang kế tiếp");
        wait.until(d -> pagination().activePage() == currentPage + 1);
        waitForResult();
        return this;
    }

    /** Trở về trang trước khi điều khiển phân trang cho phép. */
    public UniformCatalogPage previousPage() {
        WebElement previous = visible(By.cssSelector(
                "main nav[aria-label='pagination navigation']"
                        + " [aria-label='previous page button']"));
        if ("true".equals(previous.getAttribute("aria-disabled"))) {
            throw new IllegalStateException("Trang trước đang bị khóa.");
        }
        int currentPage = pagination().activePage();
        click(previous, "Trở về trang trước");
        wait.until(d -> pagination().activePage() == currentPage - 1);
        waitForResult();
        return this;
    }

    private String itemNameLabel(String tab) {
        return "Nhóm Đồng Phục".equals(tab) ? "Tên nhóm" : "Tên đồng phục";
    }

    private WebElement labeledInput(WebElement scope, String labelText) {
        List<WebElement> labels = scope.findElements(By.xpath(
                ".//label[contains(normalize-space(.),"
                        + xpathLiteral(labelText) + ")]"));
        for (WebElement label : labels) {
            String forAttribute = label.getAttribute("for");
            if (forAttribute != null && !forAttribute.isBlank()) {
                List<WebElement> linked = scope.findElements(By.xpath(
                        ".//*[@id=" + xpathLiteral(forAttribute) + "]"));
                if (!linked.isEmpty() && linked.get(0).isDisplayed()) {
                    return linked.get(0);
                }
            }
            List<WebElement> nested = label.findElements(By.cssSelector(
                    "input:not([type='file']), textarea"));
            if (!nested.isEmpty() && nested.get(0).isDisplayed()) {
                return nested.get(0);
            }
            List<WebElement> following = label.findElements(By.xpath(
                    "./following::*[(self::input and not(@type='file'))"
                            + " or self::textarea][1]"));
            if (!following.isEmpty() && following.get(0).isDisplayed()) {
                return following.get(0);
            }
        }

        String expected = normalizeForComparison(labelText);
        return scope.findElements(By.cssSelector(
                        "input:not([type='file']), textarea"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(input -> {
                    String metadata = String.join(" ",
                            safeAttribute(input, "aria-label"),
                            safeAttribute(input, "placeholder"),
                            safeAttribute(input, "name"));
                    return normalizeForComparison(metadata).contains(expected);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy input theo label: " + labelText));
    }

    private void chooseFirstSelectOption(WebElement scope, String labelText) {
        WebElement control = labeledControl(scope, labelText);
        /*
         * react-select đặt input thật chỉ rộng vài pixel bên trong control.
         * Click input có thể không mở menu, vì vậy click container rồi điều
         * khiển combobox bằng bàn phím như người dùng thật.
         */
        WebElement selectContainer = control.findElement(By.xpath(
                "./ancestor::div[contains(@class,'-control')][1]"));
        new Actions(driver)
                .moveToElement(selectContainer)
                .click()
                .sendKeys(Keys.ARROW_DOWN)
                .perform();
        WebElement option;
        try {
            option = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> d.findElements(By.cssSelector(
                                    "[role='option'],"
                                            + " [id*='-option-'],"
                                            + " [class*='-menu'] [class*='-option']"))
                            .stream()
                            .filter(WebElement::isDisplayed)
                            .filter(WebElement::isEnabled)
                            .filter(element -> !elementText(element).isBlank())
                            .findFirst()
                            .orElse(null));
        } catch (TimeoutException timeout) {
            String selectDom = driver.findElements(By.cssSelector(
                            "[id^='react-select'], [class*='-menu']"))
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .map(element -> safeAttribute(element, "outerHTML"))
                    .reduce("", (left, right) -> left + "\n" + right);
            throw new IllegalStateException(
                    "Không mở được react-select " + labelText
                            + " | aria-expanded="
                            + safeAttribute(control, "aria-expanded")
                            + " | DOM=" + selectDom,
                    timeout);
        }
        option.click();
    }

    private WebElement labeledControl(WebElement scope, String labelText) {
        List<WebElement> labels = scope.findElements(By.xpath(
                ".//label[contains(normalize-space(.),"
                        + xpathLiteral(labelText) + ")]"
                        + " | .//*[self::span or self::div]"
                        + "[normalize-space()=" + xpathLiteral(labelText) + "]"));
        for (WebElement label : labels) {
            List<WebElement> controls = label.findElements(By.xpath(
                    "./following::*[(self::button or @role='combobox')][1]"));
            if (!controls.isEmpty() && controls.get(0).isDisplayed()) {
                return controls.get(0);
            }
        }
        throw new IllegalStateException(
                "Không tìm thấy control theo label: " + labelText);
    }

    private void chooseRadio(WebElement scope, String option) {
        WebElement radio = scope.findElements(By.xpath(
                        ".//*[@aria-label=" + xpathLiteral(option)
                                + " or normalize-space()=" + xpathLiteral(option) + "]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy radio: " + option));
        /*
         * UI hiển thị native alert ngay sau khi chọn loại sản phẩm. Không dùng
         * helper click() tại đây vì helper còn chạy JavaScript animation trong
         * lúc alert đang chặn document.
         */
        radio.click();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.alertIsPresent())
                    .accept();
        } catch (TimeoutException ignored) {
            // Một số phiên bản UI không hiển thị cảnh báo này.
        }
        settle(300);
    }

    private void clickAndAcceptOptionalAlert(WebElement element, String step) {
        System.out.println("[QUAN SAT] "
                + normalizeForComparison(step)
                + " - xử lý cảnh báo xác nhận nếu có");
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});"
                        + "arguments[0].click();",
                element);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.alertIsPresent())
                    .accept();
        } catch (TimeoutException ignored) {
            // UI có thể bỏ cảnh báo ở phiên bản sau nhưng vẫn tạo dữ liệu.
        }
        finishFiniteAnimations();
    }

    private WebElement actionButton(WebElement scope, List<String> labels) {
        for (String label : labels) {
            List<WebElement> buttons = scope.findElements(By.xpath(
                    ".//button[normalize-space()=" + xpathLiteral(label) + "]"));
            Optional<WebElement> visibleButton = buttons.stream()
                    .filter(WebElement::isDisplayed)
                    .filter(WebElement::isEnabled)
                    .findFirst();
            if (visibleButton.isPresent()) {
                return visibleButton.get();
            }
        }
        throw new IllegalStateException(
                "Không tìm thấy button action: " + labels
                        + " | Drawer: " + elementText(scope));
    }

    private void confirmDeleteIfRequested() {
        List<WebElement> dialogs = driver.findElements(By.cssSelector(
                        "[role='dialog'], [aria-modal='true']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(dialog -> normalizeForComparison(elementText(dialog))
                        .contains("xoa"))
                .toList();
        if (dialogs.isEmpty()) {
            return;
        }
        WebElement dialog = dialogs.get(dialogs.size() - 1);
        click(actionButton(dialog, List.of("Xóa", "Xác nhận")),
                "Xác nhận xóa dữ liệu automation");
    }

    private void waitUntilItemPresence(
            String tab, String name, boolean expectedPresent) {
        String expectedName = normalizeForComparison(name);
        open().selectTab(tab);
        search(name);
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .pollingEvery(Duration.ofMillis(400))
                .until(d -> displayedItemNames().stream()
                        .map(UniformCatalogPage::normalizeForComparison)
                        .anyMatch(expectedName::equals) == expectedPresent);
    }

    private void waitForDrawerToClose(String ariaLabelPrefix) {
        wait.until(d -> d.findElements(By.cssSelector(
                        "[aria-label^='" + ariaLabelPrefix + "']"))
                .stream()
                .noneMatch(this::drawerIsOpen));
    }

    private static String safeAttribute(WebElement element, String name) {
        String value = element.getAttribute(name);
        return value == null ? "" : value;
    }

    private static String normalizeForComparison(String value) {
        return com.vuatho.utils.TextNormalizer.normalize(
                value == null ? "" : value);
    }

    private void waitForSelectedTab(By locator) {
        new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .until(d -> "true".equals(
                        d.findElement(locator).getAttribute("aria-selected")));
    }

    private WebElement activeFilterPopup() {
        return wait.until(d -> d.findElements(By.cssSelector(
                        "[data-slot='content'][data-open='true']"))
                .stream()
                .filter(element -> element.getAttribute("innerText") != null)
                .filter(element -> element.getAttribute("innerText")
                        .contains("Trạng thái tồn kho"))
                .findFirst()
                .orElse(null));
    }

    private WebElement activeDrawer(String ariaLabelPrefix) {
        By locator = By.cssSelector("[aria-label^='" + ariaLabelPrefix + "']");
        WebElement drawer = wait.until(d -> d.findElements(locator).stream()
                .filter(this::drawerIsOpen)
                .findFirst()
                .orElse(null));
        /*
         * Khi Chrome bị minimize/occluded, animation mở drawer có thể đứng ở
         * 0 ms dù React đã bỏ class translate-x-[100%]. Kết thúc transition
         * sau khi node đã mount để control thật sự nằm trong viewport.
         */
        finishFiniteAnimations();
        wait.until(d -> isInViewport(drawer));
        return drawer;
    }

    private boolean drawerIsOpen(WebElement drawer) {
        String classes = drawer.getAttribute("class");
        return classes != null && !classes.contains("translate-x-[100%]");
    }

    private void rememberUnfilteredTotal() {
        if (searchValue().isBlank()) {
            unfilteredTotals.put(selectedTab(), totalDisplayed());
        }
    }

    private boolean isInViewport(WebElement element) {
        return Boolean.TRUE.equals(((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("""
                        const rect = arguments[0].getBoundingClientRect();
                        return rect.width > 0 && rect.height > 0
                            && rect.bottom > 0 && rect.right > 0
                            && rect.top < window.innerHeight
                            && rect.left < window.innerWidth;
                        """, element));
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
            boolean hasAuditInfo,
            String raw) {
    }

    /** Trạng thái điều khiển phân trang. */
    public record PaginationSnapshot(
            int activePage,
            int pageCount,
            boolean previousDisabled,
            boolean nextDisabled) {
    }

    /** Ràng buộc an toàn của một input upload trong drawer tạo mới. */
    public record UploadConstraint(String accept, boolean multiple) {
    }
}
