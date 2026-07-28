package com.vuatho.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Page Object của màn hình Quản lí Đơn hàng Đồng phục.
 */
public final class UniformOrderPage extends UniformUiPage {
    public static final String ROUTE = "/vuatho/order-uniform";

    private static final By TABLE = By.cssSelector(
            "[aria-label='Table about Order Uniform Management']");
    private static final By FILTER = By.cssSelector("main button[title='Filter']");
    private static final By RESET = By.cssSelector("main button[title='Reset']");
    private static final By SEARCH = By.cssSelector(
            "main input[placeholder='Tìm kiếm thông tin khách']");

    public UniformOrderPage(WebDriver driver) {
        super(driver);
    }

    /** Mở trang đơn hàng và chờ bảng trả dữ liệu. */
    public UniformOrderPage open() {
        openRoute(ROUTE);
        visible(TABLE);
        waitForResult();
        return this;
    }

    /** Đọc các KPI doanh thu và trạng thái từ đầu trang. */
    public Map<String, String> statistics() {
        String text = mainText();
        Map<String, String> values = new LinkedHashMap<>();
        for (String label : List.of(
                "Doanh thu đã thu", "Doanh thu chưa thu",
                "Chờ xác nhận", "Đang giao", "Hoàn thành", "Đã hủy")) {
            int index = text.indexOf(label);
            if (index >= 0) {
                values.put(label, text.substring(
                        index, Math.min(text.length(), index + 100)).trim());
            }
        }
        return values;
    }

    /** Trả tiêu đề bốn cột của bảng. */
    public List<String> headers() {
        return visible(TABLE).findElements(By.cssSelector(
                        "th,[role='columnheader']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    /** Đọc các dòng đơn hàng đang hiển thị. */
    public List<OrderRow> rows() {
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                List<OrderRow> rows = new ArrayList<>();
                List<WebElement> candidates = visible(TABLE).findElements(By.cssSelector(
                        "tbody tr,[role='rowgroup']:last-of-type [role='row']"));
                for (WebElement row : candidates) {
                    String raw = row.getText().trim();
                    if (!raw.contains("MÃ ĐƠN HÀNG")) {
                        continue;
                    }
                    rows.add(new OrderRow(
                            capture(raw, "MÃ ĐƠN HÀNG\\s*(\\d+)"),
                            capture(raw, "Khách hàng:\\s*([^\\r\\n]+)"),
                            capture(raw, "Số điện thoại:\\s*([^\\r\\n]+)"),
                            capture(raw, "Đơn hàng:\\s*([^\\r\\n]+)"),
                            capture(raw, "Thanh toán:\\s*([^\\r\\n]+)"),
                            capture(raw, "Phương thức thanh toán:\\s*([^\\r\\n]+)"),
                            raw));
                }
                return rows;
            } catch (StaleElementReferenceException ignored) {
                settle(350);
            }
        }
        throw new IllegalStateException("Bảng đơn cập nhật liên tục, không đọc được dữ liệu ổn định.");
    }

    /** Chọn cách tìm theo tên hoặc theo SĐT. */
    public UniformOrderPage selectSearchMode(String mode) {
        WebElement select = visible(By.cssSelector("main select"));
        observeSelect(select, "Chọn kiểu tìm " + mode);
        new Select(select).selectByVisibleText(mode);
        pause("Đã chọn " + mode);
        return this;
    }

    /** Nhập từ khóa tìm kiếm và chờ bảng trả kết quả. */
    public UniformOrderPage search(String keyword) {
        WebElement input = visible(SEARCH);
        fill(input, keyword, "Nhập từ khóa tìm đơn " + keyword);
        settle(1_200);
        waitForResult();
        return this;
    }

    /** Mở popup bộ lọc và trả nội dung của popup. */
    public String openFilter() {
        click(visible(FILTER), "Mở bộ lọc đơn hàng Đồng phục");
        pause("Hiển thị danh sách trạng thái và phương thức");
        return driver.findElement(By.tagName("body")).getText();
    }

    /** Chọn một giá trị trong popup lọc. */
    public UniformOrderPage chooseFilter(String option) {
        RuntimeException lastFailure = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                if (visibleElements(By.cssSelector(
                        "[data-slot='content'][data-open='true']")).isEmpty()) {
                    openFilter();
                }
                WebElement popup = visible(By.cssSelector(
                        "[data-slot='content'][data-open='true']"));
                WebElement choice = popup.findElements(By.xpath(
                                ".//*[normalize-space()=" + xpathLiteral(option)
                                        + " and (self::span or self::div or self::label or self::button)]"))
                        .stream().filter(WebElement::isDisplayed).findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "Không tìm thấy tùy chọn lọc " + option));
                click(choice, "Chọn bộ lọc " + option);
                lastFailure = null;
                break;
            } catch (StaleElementReferenceException stale) {
                lastFailure = stale;
                settle(350);
            }
        }
        if (lastFailure != null) {
            throw lastFailure;
        }
        waitForResult();
        return this;
    }

    /** Reset toàn bộ tìm kiếm, kiểu tìm và bộ lọc. */
    public UniformOrderPage reset() {
        click(visible(RESET), "Đặt lại tìm kiếm và bộ lọc đơn hàng");
        waitForResult();
        return this;
    }

    /** Mở form tạo đơn và trả nội dung hiển thị. */
    public String openCreateDrawer() {
        click(visible(By.xpath("//main//button[normalize-space()='Tạo đơn']")),
                "Mở form tạo đơn Đồng phục");
        WebElement drawer = visible(By.cssSelector(
                "[aria-label='drawer-Tạo mới đơn hàng']"));
        settle(700);
        pause("Hiển thị đầy đủ form tạo đơn");
        return elementText(drawer);
    }

    /**
     * Gửi form rỗng và xác nhận hệ thống không đóng drawer/tạo đơn.
     * Nút hiện tại vẫn enabled nên validation phải được kiểm tra qua kết quả.
     */
    public boolean submitEmptyCreateFormKeepsDrawerOpen() {
        WebElement drawer = visible(By.cssSelector(
                "[aria-label='drawer-Tạo mới đơn hàng']"));
        WebElement button = drawer.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> item.getText().trim().equals("Xác nhận"))
                .findFirst().orElseThrow();
        click(button, "Xác nhận form tạo đơn khi chưa chọn combo và thợ");
        settle(700);
        pause("Quan sát validation form tạo đơn còn thiếu dữ liệu");
        return (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript(
                        "const d=arguments[0],r=d.getBoundingClientRect();"
                                + "return r.left < innerWidth && r.right > 0"
                                + " && d.innerText.includes('Chưa có combo nào được chọn');",
                        drawer);
    }

    /** Mở dòng đơn đầu tiên hoặc dòng đầu tiên có trạng thái yêu cầu. */
    public DetailSnapshot openFirstDetail(String status) {
        WebElement row = visible(TABLE).findElements(By.cssSelector(
                        "tbody tr,[role='rowgroup']:last-of-type [role='row']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> item.getText().contains("MÃ ĐƠN HÀNG"))
                .filter(item -> status == null || status.isBlank()
                        || item.getText().contains("Đơn hàng:\n" + status)
                        || item.getText().contains("Đơn hàng: " + status))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có đơn phù hợp trạng thái " + status));
        String id = capture(row.getText(), "MÃ ĐƠN HÀNG\\s*(\\d+)");
        click(row, "Mở chi tiết đơn #" + id);
        WebElement drawer = visible(By.cssSelector(
                "[aria-label='drawer-Chi tiết đơn đồng phục']"));
        settle(800);
        pause("Hiển thị dữ liệu chi tiết đơn #" + id);
        return new DetailSnapshot(id, elementText(drawer));
    }

    /** Mở chế độ chỉnh sửa của drawer chi tiết. */
    public String openEditMode() {
        WebElement drawer = visible(By.cssSelector(
                "[aria-label='drawer-Chi tiết đơn đồng phục']"));
        WebElement edit = drawer.findElements(By.xpath(
                        ".//button[normalize-space()='Chỉnh sửa']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Đơn hiện tại không hỗ trợ chỉnh sửa."));
        click(edit, "Mở chế độ chỉnh sửa đơn");
        pause("Hiển thị dữ liệu chỉnh sửa đơn");
        return elementText(drawer);
    }

    /** Tổng số bản ghi mà phân trang công bố. */
    public int totalDisplayed() {
        Matcher matcher = Pattern.compile("Tổng hiển thị:\\s*([\\d.]+)")
                .matcher(mainText());
        return matcher.find()
                ? Integer.parseInt(matcher.group(1).replace(".", ""))
                : 0;
    }

    private void observeSelect(WebElement select, String step) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});"
                        + "arguments[0].style.outline='3px solid #2563eb';", select);
        pause(step);
    }

    private static String capture(String text, String expression) {
        Matcher matcher = Pattern.compile(expression).matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    /** Một dòng dữ liệu đơn hàng đã được tách trường phục vụ assertion. */
    public record OrderRow(
            String id,
            String customer,
            String phone,
            String orderStatus,
            String paymentStatus,
            String paymentMethod,
            String raw) {
    }

    /** Dữ liệu của drawer chi tiết vừa mở. */
    public record DetailSnapshot(String id, String text) {
    }
}
