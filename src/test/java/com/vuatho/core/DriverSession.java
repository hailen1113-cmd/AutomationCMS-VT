package com.vuatho.core;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.time.Duration;

final class DriverSession {
    // Dùng chung một browser để giữ session ERP đã đăng nhập xuyên suốt các test method.
    // Không quit driver ở @AfterMethod, nếu không các test load page phía sau sẽ mất trạng thái login.
    private static WebDriver sharedDriver;
    private static RuntimeException startupFailure;
    private static boolean shutdownHookRegistered;

    /**
     * Khởi tạo DriverSession với các phụ thuộc cần thiết.
     */
    private DriverSession() {
    }

    static WebDriver acquire() {
        if (startupFailure != null) {
            throw startupFailure;
        }

        // Chỉ mở browser mới khi chưa có browser hoặc browser cũ đã bị đóng/crash.
        if (!isAlive(sharedDriver)) {
            System.out.println("Mo WebDriver moi cho bo test...");
            try {
                sharedDriver = DriverFactory.createChromeDriver();
                configureTimeouts(sharedDriver);
                registerShutdownHook();
            } catch (RuntimeException exception) {
                startupFailure = exception;
                throw exception;
            }
        } else {
            System.out.println("Dung lai WebDriver hien tai cho testcase tiep theo...");
        }
        return sharedDriver;
    }

    static void releaseAfterSuite() {
        if (sharedDriver == null) {
            return;
        }
        // Khi debug local thường cần giữ Chrome mở để kiểm tra màn hình cuối cùng.
        if (TestConfig.keepBrowserOpen()) {
            System.out.println("Giu WebDriver mo sau bo test vi keep.browser.open=true");
            return;
        }
        // Chỉ đóng Chrome sau khi test cuối cùng trong suite đã chạy xong.
        System.out.println("Dong WebDriver sau testcase cuoi trong bo test");
        try {
            sharedDriver.quit();
        } finally {
            sharedDriver = null;
            startupFailure = null;
        }
    }

    /**
     * Thực hiện xử lý register shutdown hook trong luồng kiểm thử.
     */
    private static void registerShutdownHook() {
        if (shutdownHookRegistered) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!TestConfig.keepBrowserOpen()) {
                releaseAfterSuite();
            }
        }, "webdriver-shutdown"));
        shutdownHookRegistered = true;
    }

    /**
     * Thực hiện xử lý configure timeouts trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    private static void configureTimeouts(WebDriver driver) {
        // Dùng explicit wait trong page/action; tắt implicit wait để tránh delay ẩn khó debug.
        driver.manage().timeouts().pageLoadTimeout(TestConfig.pageLoadTimeout());
        driver.manage().timeouts().scriptTimeout(TestConfig.scriptTimeout());
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
    }

    /**
     * Kiểm tra điều kiện is alive.
     * @param candidate giá trị candidate được truyền vào
     * @return kết quả is alive sau khi xử lý
     */
    private static boolean isAlive(WebDriver candidate) {
        if (candidate == null) {
            return false;
        }
        try {
            // Gọi nhẹ vào WebDriver để xác nhận session browser vẫn còn sống.
            candidate.getWindowHandles();
            return true;
        } catch (WebDriverException exception) {
            return false;
        }
    }
}
