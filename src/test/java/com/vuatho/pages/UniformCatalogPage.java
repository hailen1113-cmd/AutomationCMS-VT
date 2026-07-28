package com.vuatho.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
        return driver.findElement(By.tagName("body")).getText();
    }

    /** Chọn một tùy chọn đang hiển thị trong popup lọc. */
    public UniformCatalogPage chooseFilter(String option) {
        WebElement popup = visible(By.cssSelector(
                "[data-slot='content'][data-open='true'],[role='dialog']"));
        WebElement item = popup.findElements(By.xpath(
                        ".//*[normalize-space()=" + xpathLiteral(option)
                                + " and (self::span or self::div or self::button or self::label)]"))
                .stream().filter(WebElement::isDisplayed).findFirst()
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
        WebElement drawer = visible(By.cssSelector(
                "[aria-label='" + expected + "']"));
        pause("Hiển thị đầy đủ form tạo mới");
        return elementText(drawer);
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
                String raw = card.getText().trim();
                String name = raw.lines().map(String::trim)
                        .filter(line -> !line.isBlank()).findFirst().orElse("");
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
}
