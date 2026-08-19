package com.vuatho.support.workerstoprequest;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.WorkerStopRequestPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/** Setup/cleanup dùng chung, không chứa testcase. */
public abstract class WorkerStopRequestTestSupport extends BaseTest {
    protected WorkerStopRequestPage stopRequestPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareStopRequestPage() {
        requireAuthenticatedSession("Yêu cầu ngưng hợp tác");
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
