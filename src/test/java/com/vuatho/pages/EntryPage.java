package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

/**
 * Page Object nhận diện trang đầu vào và quyết định tiếp tục ở màn hình đăng nhập hay Dashboard.
 */
public class EntryPage {
    private final WebDriver driver;

    /**
     * Khởi tạo EntryPage với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public EntryPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Mở  trong luồng kiểm thử.
     * @return kết quả open sau khi xử lý
     */
    public EntryPage open() {
        driver.get(TestConfig.entryUrl());
        waitForDocumentReady();
        return this;
    }

    /**
     * Thực hiện xử lý title trong luồng kiểm thử.
     * @return kết quả title sau khi xử lý
     */
    public String title() {
        return driver.getTitle();
    }

    /**
     * Kiểm tra điều kiện is on expected domain.
     * @return kết quả is on expected domain sau khi xử lý
     */
    public boolean isOnExpectedDomain() {
        return driver.getCurrentUrl().contains(TestConfig.baseHost());
    }

    /**
     * Kiểm tra điều kiện is blocked by vercel.
     * @return kết quả is blocked by vercel sau khi xử lý
     */
    public boolean isBlockedByVercel() {
        String title = title().toLowerCase();
        String source = driver.getPageSource().toLowerCase();
        return (title.contains("login") && title.contains("vercel"))
                || source.contains("vercel authentication");
    }

    /**
     * Chờ for vercel access trong luồng kiểm thử.
     * @param timeout thời gian chờ tối đa
     * @return kết quả wait for vercel access sau khi xử lý
     */
    public boolean waitForVercelAccess(Duration timeout) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, timeout)
                    .until(webDriver -> !isBlockedByVercel());
            waitForDocumentReady();
            return true;
        } catch (org.openqa.selenium.TimeoutException ignored) {
            return false;
        }
    }

    /**
     * Chờ for document ready trong luồng kiểm thử.
     */
    private void waitForDocumentReady() {
        new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(20))
                .until(webDriver -> "complete".equals(
                        ((org.openqa.selenium.JavascriptExecutor) webDriver)
                                .executeScript("return document.readyState")));
    }
}
