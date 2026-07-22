package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.WorkerViolationPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Khoi tao va don dep trang Quan li tho vi pham cho tung testcase. */
abstract class WorkerViolationTestSupport extends BaseTest {
    protected WorkerViolationPage workerViolationPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareWorkerViolationPage() {
        if (driver == null) throw new SkipException("WebDriver khong khoi tao duoc.");

        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Khong dang nhap duoc truoc khi kiem tra Quan li tho vi pham.");
        }

        workerViolationPage = new WorkerViolationPage(driver);
        if (driver.getCurrentUrl().contains(WorkerViolationPage.ROUTE)) {
            workerViolationPage.refresh();
        } else {
            workerViolationPage.openFromMenu();
        }
    }

    @AfterMethod(alwaysRun = true)
    public void closeWorkerViolationPopup() {
        if (workerViolationPage == null) return;
        try {
            workerViolationPage.closeDialog();
        } catch (RuntimeException ignored) {
            // Test sau se refresh trang, khong de viec don dep che mat loi nghiep vu.
        }
    }
}
