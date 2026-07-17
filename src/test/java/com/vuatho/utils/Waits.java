package com.vuatho.utils;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Tạo các điều kiện chờ Selenium dùng chung, giúp thao tác chỉ chạy khi giao diện đã sẵn sàng.
 */
public final class Waits {
    private Waits() {
    }

    /**
     * Thực hiện xử lý standard trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     * @return kết quả standard sau khi xử lý
     */
    public static WebDriverWait standard(WebDriver driver) {
        return withTimeout(driver, TestConfig.defaultWaitTimeout());
    }

    /**
     * Thực hiện xử lý long wait trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     * @return kết quả long wait sau khi xử lý
     */
    public static WebDriverWait longWait(WebDriver driver) {
        return withTimeout(driver, TestConfig.longWaitTimeout());
    }

    /**
     * Thực hiện xử lý with timeout trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     * @param timeout thời gian chờ tối đa
     * @return kết quả with timeout sau khi xử lý
     */
    public static WebDriverWait withTimeout(WebDriver driver, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.pollingEvery(Duration.ofMillis(200));
        wait.ignoring(StaleElementReferenceException.class);
        return wait;
    }
}
