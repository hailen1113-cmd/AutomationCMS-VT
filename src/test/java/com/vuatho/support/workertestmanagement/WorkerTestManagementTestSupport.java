package com.vuatho.support.workertestmanagement;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.WorkerTestManagementPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/** Setup/cleanup dùng chung, không chứa testcase và không chạy trực tiếp. */
public abstract class WorkerTestManagementTestSupport extends BaseTest {
    protected WorkerTestManagementPage workerTestPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareWorkerTestPage() {
        requireAuthenticatedSession("menu Bài kiểm tra");
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
