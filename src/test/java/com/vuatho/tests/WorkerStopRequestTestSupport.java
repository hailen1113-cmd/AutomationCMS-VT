package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.WorkerStopRequestPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Setup/cleanup dùng chung, không chứa testcase. */
abstract class WorkerStopRequestTestSupport extends BaseTest {
    protected WorkerStopRequestPage stopRequestPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareStopRequestPage() {
        if (driver == null) throw new SkipException("WebDriver không khởi tạo được.");
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra Yêu cầu ngưng hợp tác.");
        }
        stopRequestPage = new WorkerStopRequestPage(driver).open();
    }

    @AfterMethod(alwaysRun = true)
    public void closeStopRequestDialog() {
        if (stopRequestPage == null) return;
        try {
            stopRequestPage.closeDialog();
        } catch (RuntimeException ignored) {
            // Test kế tiếp luôn mở lại route nên cleanup không che lỗi nghiệp vụ.
        }
    }
}
