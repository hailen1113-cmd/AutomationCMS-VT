package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.WorkerPostManagementPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Thiết lập trạng thái độc lập cho từng testcase Quản lí bài đăng. */
// Lớp hỗ trợ dùng chung, không khai báo @Test và không phải một test suite độc lập.
abstract class WorkerPostManagementTestSupport extends BaseTest {
    protected WorkerPostManagementPage workerPostManagementPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareWorkerPostManagementPage() {
        if (driver == null) throw new SkipException("WebDriver không khởi tạo được.");

        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra Quản lí bài đăng.");
        }
        workerPostManagementPage = new WorkerPostManagementPage(driver).openPendingDirectly();
    }

    @AfterMethod(alwaysRun = true)
    public void closeWorkerPostManagementMediaDialog() {
        if (workerPostManagementPage == null) return;
        try {
            workerPostManagementPage.closeMediaDialog();
        } catch (RuntimeException ignored) {
            // Test sau luôn mở lại route pending nên không để cleanup che lỗi nghiệp vụ.
        }
    }
}
