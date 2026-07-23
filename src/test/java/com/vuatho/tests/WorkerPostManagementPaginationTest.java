package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra các cách chuyển trang và khôi phục trang đầu của danh sách bài đăng. */
public class WorkerPostManagementPaginationTest extends WorkerPostManagementTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerPostManagementPaginationTest.class,
                "Quản lý bài đăng thợ", "Phân trang bài đăng");
    }

    @Test(groups = {"worker-post-management", "read-only", "pagination", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-008: Phân trang đổi dữ liệu và Reset quay về trang đầu")
    public void paginationAndResetWork() {
        List<String> firstPageWorkers = workerPostManagementPage.visibleWorkerHrefs();
        Assert.assertFalse(firstPageWorkers.isEmpty(), "Trang đầu không có dữ liệu.");
        Assert.assertEquals(workerPostManagementPage.activePage(), 1,
                "Danh sách không bắt đầu ở trang 1.");
        Assert.assertTrue(workerPostManagementPage.previousPageDisabled(),
                "Nút Previous phải bị khóa tại trang đầu.");
        Assert.assertTrue(workerPostManagementPage.lastVisiblePageNumber() >= 2,
                "Dữ liệu hiện tại không có trang 2 để kiểm tra.");

        workerPostManagementPage.goToPage(2);
        Assert.assertEquals(workerPostManagementPage.activePage(), 2,
                "Click trang 2 nhưng page không đổi.");
        Assert.assertNotEquals(workerPostManagementPage.visibleWorkerHrefs(), firstPageWorkers,
                "Trang 2 vẫn hiển thị dữ liệu của trang 1.");

        workerPostManagementPage.resetList();
        Assert.assertEquals(workerPostManagementPage.activePage(), 1,
                "Reset không quay về trang đầu.");
        Assert.assertEquals(workerPostManagementPage.visibleWorkerHrefs(), firstPageWorkers,
                "Reset không phục hồi dữ liệu trang đầu.");
    }

    @Test(groups = {"worker-post-management", "read-only", "pagination", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-014: Next và Previous đổi đúng trang")
    public void nextAndPreviousPaginationControlsWork() {
        List<String> firstPage = workerPostManagementPage.visibleWorkerHrefs();
        workerPostManagementPage.goToNextPage();
        Assert.assertEquals(workerPostManagementPage.activePage(), 2,
                "Next không chuyển sang trang 2.");
        Assert.assertNotEquals(workerPostManagementPage.visibleWorkerHrefs(), firstPage,
                "Next không đổi dữ liệu.");

        workerPostManagementPage.goToPreviousPage();
        Assert.assertEquals(workerPostManagementPage.activePage(), 1,
                "Previous không quay lại trang 1.");
        Assert.assertEquals(workerPostManagementPage.visibleWorkerHrefs(), firstPage,
                "Previous không phục hồi dữ liệu trang đầu.");
        Assert.assertTrue(workerPostManagementPage.previousPageDisabled(),
                "Previous không bị khóa khi đã về trang đầu.");
    }
}
