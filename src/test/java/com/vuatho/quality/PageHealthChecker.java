package com.vuatho.quality;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PageHealthChecker {
    private static final By MAIN_CONTENT = By.cssSelector("main, [role='main']");
    private static final By ERROR_MESSAGES = By.xpath(
            "//*[normalize-space()='Internal Server Error'"
                    + " or normalize-space()='Application error'"
                    + " or normalize-space()='Something went wrong'"
                    + " or normalize-space()='404 - Page Not Found'"
                    + " or normalize-space()='500 - Internal Server Error']");

    private final WebDriver driver;

    /**
     * Khởi tạo PageHealthChecker với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public PageHealthChecker(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Thực hiện xử lý start observation trong luồng kiểm thử.
     */
    public void startObservation() {
        browserErrors();
    }

    /**
     * Thu thập  trong luồng kiểm thử.
     * @return kết quả inspect sau khi xử lý
     */
    public PageHealthReport inspect() {
        List<String> problems = new ArrayList<>();
        String url = driver.getCurrentUrl();

        validateUrl(url, problems);
        validateDocument(problems);
        validateMainContent(problems);
        validateErrorPage(problems);
        validateImages(problems);
        validateBrowserConsole(problems);

        return new PageHealthReport(url, List.copyOf(problems));
    }

    /**
     * Thực hiện xử lý validate url trong luồng kiểm thử.
     * @param url giá trị url được truyền vào
     * @param problems giá trị problems được truyền vào
     */
    private void validateUrl(String url, List<String> problems) {
        String expectedHost = TestConfig.baseHost();
        String actualHost = URI.create(url).getHost();
        if (!expectedHost.equalsIgnoreCase(actualHost)) {
            problems.add("Unexpected host: " + actualHost + " (expected " + expectedHost + ")");
        }
        if (url.contains("login") || url.contains("accounts.google.com")) {
            problems.add("Page redirected to authentication: " + url);
        }
    }

    /**
     * Thực hiện xử lý validate document trong luồng kiểm thử.
     * @param problems giá trị problems được truyền vào
     */
    private void validateDocument(List<String> problems) {
        Object state = ((JavascriptExecutor) driver).executeScript("return document.readyState");
        if (!"complete".equals(state)) {
            problems.add("Document is not ready: " + state);
        }
        if (driver.getTitle().isBlank()) {
            problems.add("Page title is empty");
        }
    }

    /**
     * Thực hiện xử lý validate main content trong luồng kiểm thử.
     * @param problems giá trị problems được truyền vào
     */
    private void validateMainContent(List<String> problems) {
        String text = driver.findElements(MAIN_CONTENT).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .orElse("")
                .trim();
        if (text.length() < 20) {
            problems.add("Main content is missing or nearly empty");
        }
    }

    /**
     * Thực hiện xử lý validate error page trong luồng kiểm thử.
     * @param problems giá trị problems được truyền vào
     */
    private void validateErrorPage(List<String> problems) {
        List<String> errors = driver.findElements(ERROR_MESSAGES).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .distinct()
                .toList();
        if (!errors.isEmpty()) {
            problems.add("Visible application error: " + errors);
        }
    }

    /**
     * Thực hiện xử lý validate images trong luồng kiểm thử.
     * @param problems giá trị problems được truyền vào
     */
    @SuppressWarnings("unchecked")
    private void validateImages(List<String> problems) {
        List<String> brokenImages = (List<String>) ((JavascriptExecutor) driver).executeScript(
                "return [...document.images].filter(img=>img.complete && img.naturalWidth===0)"
                        + ".map(img=>img.currentSrc || img.getAttribute('src') || '').filter(Boolean);");
        brokenImages = brokenImages.stream().distinct().toList();
        String expectedHost = TestConfig.baseHost();
        brokenImages = brokenImages.stream()
                .filter(src -> sameHost(src, expectedHost))
                .toList();
        if (!brokenImages.isEmpty()) {
            problems.add("Broken images: " + brokenImages);
        }
    }

    /**
     * Thực hiện xử lý same host trong luồng kiểm thử.
     * @param source giá trị source được truyền vào
     * @param expectedHost giá trị expected host được truyền vào
     * @return kết quả same host sau khi xử lý
     */
    private boolean sameHost(String source, String expectedHost) {
        try {
            String host = URI.create(source).getHost();
            return host == null || expectedHost.equalsIgnoreCase(host);
        } catch (IllegalArgumentException ignored) {
            return true;
        }
    }

    /**
     * Thực hiện xử lý validate browser console trong luồng kiểm thử.
     * @param problems giá trị problems được truyền vào
     */
    private void validateBrowserConsole(List<String> problems) {
        List<String> errors = browserErrors().stream()
                .map(LogEntry::getMessage)
                .filter(message -> !isThirdPartyAuthNoise(message))
                .distinct()
                .toList();
        if (!errors.isEmpty()) {
            problems.add("Severe browser console errors: " + errors);
        }
    }

    /**
     * Kiểm tra điều kiện is third party auth noise.
     * @param message giá trị message được truyền vào
     * @return kết quả is third party auth noise sau khi xử lý
     */
    private boolean isThirdPartyAuthNoise(String message) {
        return message.contains("accounts.google.com")
                || message.contains("ssl.gstatic.com")
                || message.contains("Cross-Origin-Opener-Policy policy would block")
                || message.contains("All created TinyMCE editors are configured to be read-only");
    }

    /**
     * Thực hiện xử lý browser errors trong luồng kiểm thử.
     * @return kết quả browser errors sau khi xử lý
     */
    private List<LogEntry> browserErrors() {
        try {
            return driver.manage().logs().get(LogType.BROWSER).getAll().stream()
                    .filter(entry -> entry.getLevel().intValue() >= Level.SEVERE.intValue())
                    .toList();
        } catch (RuntimeException unsupportedLogging) {
            return List.of();
        }
    }
}
