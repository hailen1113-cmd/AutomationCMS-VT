package com.vuatho.core;

import com.vuatho.config.TestConfig;
import com.vuatho.reporting.ScreenshotManager;
import com.vuatho.utils.OverlayCleaner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import java.io.IOException;

public abstract class BaseTest {
    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public final void setUpBaseDriver() {
        // Mỗi test method dùng lại browser cấp suite nếu browser đó vẫn còn sống.
        driver = DriverSession.acquire();
        OverlayCleaner.dismissBlockingOverlays(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) throws IOException {
        if (driver == null) {
            return;
        }

        try {
            // Không quit Chrome ở đây. Chỉ lưu bằng chứng khi fail rồi để test tiếp theo chạy tiếp.
            if (result.getStatus() == ITestResult.FAILURE) {
                captureScreenshotIfEnabled(result);
                FailurePause.awaitConfirmation();
            }
        } finally {
            driver = null;
        }
    }

    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    // Không đặt driver.quit() trong BaseTest.
    // DriverLifecycleListener sẽ đóng browser một lần duy nhất khi toàn bộ TestNG execution kết thúc.
    private void captureScreenshotIfEnabled(ITestResult result) throws IOException {
        if (!TestConfig.captureScreenshots()) {
            return;
        }
        try {
            ScreenshotManager.capture(driver, result);
        } catch (WebDriverException exception) {
            System.out.println("Could not capture failure screenshot because WebDriver is not available: "
                    + exception.getMessage());
        }
    }
}
