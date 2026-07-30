package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Các thao tác Selenium dùng chung của ba màn hình thuộc menu Đồng phục.
 *
 * <p>Mọi thao tác click/nhập đều cuộn element vào giữa màn hình và giữ lại hai
 * giây khi chạy có giao diện để người chạy quan sát được hành động.</p>
 */
abstract class UniformUiPage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected UniformUiPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(45));
        this.wait.pollingEvery(Duration.ofMillis(300));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    /** Mở một route của module Đồng phục và chờ main tải xong. */
    protected void openRoute(String route) {
        driver.get(TestConfig.baseUrl().replaceAll("/+$", "") + route);
        wait.until(d -> d.getCurrentUrl().contains(route));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("main")));
        waitForLoadingToFinish();
    }

    /** Trả nội dung vùng main hiện tại. */
    public String mainText() {
        return visible(By.tagName("main")).getText();
    }

    /** Trả nội dung đã chuẩn hóa để assertion không phụ thuộc hoa/thường hoặc dấu. */
    public String normalizedMainText() {
        return TextNormalizer.normalize(mainText());
    }

    /** Tìm element đang hiển thị đầu tiên. */
    protected WebElement visible(By locator) {
        return wait.until(d -> d.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null));
    }

    /** Tìm tất cả element đang hiển thị. */
    protected List<WebElement> visibleElements(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    /** Đọc cả nội dung trong vùng cuộn/transition mà WebElement#getText có thể bỏ sót. */
    protected String elementText(WebElement element) {
        String innerText = element.getAttribute("innerText");
        return innerText == null || innerText.isBlank()
                ? element.getText()
                : innerText.trim();
    }

    /** Tìm element có text chính xác và đang hiển thị. */
    protected WebElement exactText(String text) {
        return visible(By.xpath("//*[normalize-space()=" + xpathLiteral(text) + "]"));
    }

    /** Click có cuộn và thời gian quan sát. */
    protected void click(WebElement element, String step) {
        observe(element, step);
        try {
            element.click();
        } catch (RuntimeException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    /** Nhập dữ liệu có cuộn và thời gian quan sát. */
    protected void fill(WebElement input, String value, String step) {
        observe(input, step);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), value);
    }

    /** Đóng drawer/dialog đang mở mà không xác nhận thay đổi dữ liệu. */
    public void closeOverlay() {
        for (String label : List.of("Hủy", "Đóng")) {
            List<WebElement> buttons = visibleElements(By.xpath(
                    "//button[normalize-space()=" + xpathLiteral(label) + "]"));
            if (!buttons.isEmpty()) {
                buttons.get(buttons.size() - 1).click();
                return;
            }
        }
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    /** Chờ spinner/loading của React biến mất và dữ liệu hoặc empty-state xuất hiện. */
    protected void waitForLoadingToFinish() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .pollingEvery(Duration.ofMillis(250))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                String text = d.findElement(By.tagName("main")).getText();
                // Một số bảng giữ node "Đang tải dữ liệu" ẩn trong DOM sau khi
                // kết quả đã render. Không chờ node đó biến mất vì sẽ làm mỗi
                // route đứng hết timeout; các hàm đọc bảng sẽ kiểm tra dữ liệu.
                return !text.isBlank();
            });
        } catch (TimeoutException ignored) {
            // Assertion nghiệp vụ sau đó sẽ báo nội dung thực tế nếu trang không tải.
        }
    }

    /** Chờ kết quả danh sách ổn định sau tìm kiếm/lọc/chuyển tab. */
    protected void waitForResult() {
        settle(800);
        waitForLoadingToFinish();
        pause("Đã tải dữ liệu trả về");
    }

    /** Chờ ngắn cho debounce/API React hoàn tất trước khi đọc DOM mới. */
    protected void settle(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    /** Giữ màn hình hai giây ở chế độ có giao diện. */
    protected void pause(String step) {
        if (TestConfig.headless()) {
            return;
        }
        try {
            Thread.sleep(2_000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void observe(WebElement element, String step) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'center'});"
                        + "arguments[0].style.outline='3px solid #2563eb';",
                element);
        pause(step);
    }

    protected static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        return "concat('" + value.replace("'", "',\"'\",'") + "')";
    }
}
