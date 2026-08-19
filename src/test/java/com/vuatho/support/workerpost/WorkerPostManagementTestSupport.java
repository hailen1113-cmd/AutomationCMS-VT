package com.vuatho.support.workerpost;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.WorkerPostManagementPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/** Thiết lập trạng thái độc lập cho từng testcase Quản lí bài đăng. */
// Lớp hỗ trợ dùng chung, không khai báo @Test và không phải một test suite độc lập.
public abstract class WorkerPostManagementTestSupport extends BaseTest {
    protected WorkerPostManagementPage workerPostManagementPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareWorkerPostManagementPage() {
        requireAuthenticatedSession("Quản lí bài đăng");
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
