package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Test dieu huong chi tiet thợ, refresh va phuc hoi trang thai danh sach. */
public class WorkerViolationDetailStateTest extends WorkerViolationTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerViolationDetailStateTest.class,
                "Bo test chi tiet tho vi pham", "Kiem tra detail va page state");
    }

    @Test(groups = {"violation-worker", "detail"},
            description = "VIOLATION-WORKER-DETAIL-001: Click dong mo xac nhan xem thong tin tho")
    public void rowOpensWorkerInformationConfirmation() {
        Assert.assertTrue(workerViolationPage.openFirstWorkerConfirmation(),
                "Click dong khong mo popup xac nhan thong tin tho.");
        String text = workerViolationPage.dialogText();
        Assert.assertTrue(text.contains("thong tin tho") || text.contains("xac nhan"),
                "Popup click dong khong dung luong xem thong tin tho: " + text);
    }

    @Test(groups = {"violation-worker", "detail"},
            description = "VIOLATION-WORKER-DETAIL-002: Huy xem chi tiet giu nguyen danh sach")
    public void cancelWorkerInformationKeepsList() {
        Assert.assertTrue(workerViolationPage.openFirstWorkerConfirmation(), "Khong mo duoc popup thong tin tho.");
        Assert.assertTrue(workerViolationPage.clickDialogAction("Hủy"), "Popup thieu nut Huy.");
        Assert.assertTrue(workerViolationPage.hasExpectedHeaders(), "Huy popup lam mat danh sach.");
    }

    @Test(groups = {"violation-worker", "state"},
            description = "VIOLATION-WORKER-STATE-001: Refresh tai lai trang thanh cong")
    public void browserRefreshRestoresLoadedPage() {
        long before = workerViolationPage.totalDisplayed();
        workerViolationPage.refresh();
        Assert.assertTrue(workerViolationPage.isLoaded(), "Trang khong tai lai thanh cong.");
        Assert.assertEquals(workerViolationPage.totalDisplayed(), before, "Refresh lam thay doi tong du lieu bat thuong.");
    }

    @Test(groups = {"violation-worker", "state"},
            description = "VIOLATION-WORKER-STATE-002: Clear search phuc hoi danh sach")
    public void clearSearchRestoresResults() {
        long original = workerViolationPage.totalDisplayed();
        workerViolationPage.search("__AUTOMATION_EMPTY_019283__");
        Assert.assertTrue(workerViolationPage.hasEmptyState(), "Du lieu test empty state khong rong.");
        workerViolationPage.clearSearch();
        Assert.assertEquals(workerViolationPage.totalDisplayed(), original,
                "Xoa tu khoa khong phuc hoi tong danh sach.");
    }
}
