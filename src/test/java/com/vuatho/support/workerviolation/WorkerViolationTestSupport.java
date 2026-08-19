package com.vuatho.support.workerviolation;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.WorkerViolationPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/** Khoi tao va don dep trang Quan li tho vi pham cho tung testcase. */
public abstract class WorkerViolationTestSupport extends BaseTest {
    protected WorkerViolationPage workerViolationPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareWorkerViolationPage() {
        requireAuthenticatedSession("Quản lí thợ vi phạm");

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
