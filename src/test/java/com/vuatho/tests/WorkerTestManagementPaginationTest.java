package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra phân trang có cuộn tới control trước và sau mỗi thao tác. */
public class WorkerTestManagementPaginationTest extends WorkerTestManagementTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerTestManagementPaginationTest.class,
                "Bài kiểm tra", "Phân trang");
    }

    @Test(groups = {"worker-test-management", "pagination", "data-interaction"},
            description = "WORKER-TESTED-007: Trang kế và trang trước trả đúng bộ dữ liệu")
    public void nextAndPreviousPageReturnExpectedData() {
        Assert.assertTrue(workerTestPage.totalPages() > 1,
                "Không đủ dữ liệu để kiểm tra phân trang.");
        List<String> pageOne = workerTestPage.rows().stream().map(row -> row.id()).toList();

        workerTestPage.goToNextPage();
        Assert.assertEquals(workerTestPage.activePage(), 2);
        List<String> pageTwo = workerTestPage.rows().stream().map(row -> row.id()).toList();
        Assert.assertNotEquals(pageTwo, pageOne, "Trang 2 không trả bộ dữ liệu mới.");

        workerTestPage.goToPreviousPage();
        Assert.assertEquals(workerTestPage.activePage(), 1);
        Assert.assertEquals(
                workerTestPage.rows().stream().map(row -> row.id()).toList(),
                pageOne,
                "Quay lại trang 1 không phục hồi đúng dữ liệu.");
    }

    @Test(groups = {"worker-test-management", "pagination", "data-interaction"},
            description = "WORKER-TESTED-008: Trang cuối trả dữ liệu và không vượt tổng hiển thị")
    public void lastPageReturnsRemainingRecords() {
        int totalPages = workerTestPage.totalPages();
        int totalDisplayed = workerTestPage.totalDisplayed();
        Assert.assertTrue(totalPages > 1, "Không đủ dữ liệu để mở trang cuối.");

        workerTestPage.goToPage(totalPages);
        Assert.assertEquals(workerTestPage.activePage(), totalPages);
        Assert.assertFalse(workerTestPage.rows().isEmpty(), "Trang cuối không có dữ liệu.");
        int expectedLastPageRows = totalDisplayed % 20;
        if (expectedLastPageRows == 0) expectedLastPageRows = 20;
        Assert.assertEquals(workerTestPage.rows().size(), expectedLastPageRows,
                "Số dòng trang cuối không khớp Tổng hiển thị.");
    }
}
