package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerStopRequestPage.RequestRow;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra phân trang danh sách yêu cầu ngưng hợp tác. */
public class WorkerStopRequestPaginationTest extends WorkerStopRequestTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerStopRequestPaginationTest.class,
                "Yêu cầu ngưng hợp tác", "Phân trang danh sách");
    }

    @Test(groups = {"worker-stop-request", "pagination", "data-interaction"},
            description = "WORKER-STOP-REQUEST-004: Sang trang 2 đổi dữ liệu và quay lại trang 1")
    public void paginationChangesAndRestoresData() {
        List<String> firstPage = stopRequestPage.rows().stream().map(RequestRow::id).toList();
        Assert.assertTrue(stopRequestPage.totalPages() >= 2,
                "Dữ liệu hiện tại không có trang 2.");

        stopRequestPage.goToPage(2);
        Assert.assertEquals(stopRequestPage.activePage(), 2, "Không sang được trang 2.");
        Assert.assertNotEquals(
                stopRequestPage.rows().stream().map(RequestRow::id).toList(),
                firstPage,
                "Trang 2 không đổi dữ liệu.");

        stopRequestPage.goToPage(1);
        Assert.assertEquals(
                stopRequestPage.rows().stream().map(RequestRow::id).toList(),
                firstPage,
                "Quay lại trang 1 không phục hồi dữ liệu.");
    }
}
