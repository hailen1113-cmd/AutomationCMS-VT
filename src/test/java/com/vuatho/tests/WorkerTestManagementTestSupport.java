package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.WorkerTestManagementPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Setup/cleanup dùng chung, không chứa testcase và không chạy trực tiếp. */
abstract class WorkerTestManagementTestSupport extends BaseTest {
    protected WorkerTestManagementPage workerTestPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareWorkerTestPage() {
        if (driver == null) {
            throw new SkipException("WebDriver không khởi tạo được.");
        }
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra menu Bài kiểm tra.");
        }
        workerTestPage = new WorkerTestManagementPage(driver).open();
    }

    @AfterMethod(alwaysRun = true)
    public void closeWorkerTestDrawer() {
        if (workerTestPage == null) return;
        try {
            workerTestPage.closeDrawer();
        } catch (RuntimeException ignored) {
            // Test kế tiếp luôn mở lại route nên cleanup không che lỗi nghiệp vụ.
        }
    }
}
