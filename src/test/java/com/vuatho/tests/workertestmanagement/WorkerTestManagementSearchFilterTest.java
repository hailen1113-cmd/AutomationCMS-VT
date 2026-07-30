package com.vuatho.tests.workertestmanagement;

import com.vuatho.testcases.WorkerTestManagementTestCases;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.workertestmanagement.WorkerTestManagementTestSupport;
import com.vuatho.pages.WorkerTestManagementPage.Status;
import com.vuatho.pages.WorkerTestManagementPage.TestRow;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra tìm kiếm, bộ lọc trạng thái và Reset của menu Bài kiểm tra. */
public class WorkerTestManagementSearchFilterTest extends WorkerTestManagementTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerTestManagementSearchFilterTest.class,
                "Bài kiểm tra", "Tìm kiếm và bộ lọc");
    }

    @Test(groups = {"worker-test-management", "search", "data-interaction"},
            description = WorkerTestManagementTestCases.WORKER_TEST_010)
    public void searchByIdReturnsMatchingRecord() {
        String id = workerTestPage.rows().get(0).id();
        workerTestPage.search(id);
        Assert.assertFalse(workerTestPage.rows().isEmpty(), "Tìm ID không trả dữ liệu.");
        Assert.assertTrue(workerTestPage.rows().stream().allMatch(row -> row.id().contains(id)),
                "Kết quả tìm kiếm chứa ID không khớp.");
    }

    @Test(groups = {"worker-test-management", "search", "data-interaction"},
            description = WorkerTestManagementTestCases.WORKER_TEST_011)
    public void searchByAccountReturnsMatchingRecords() {
        String account = workerTestPage.rows().get(0).account();
        workerTestPage.search(account);
        String expected = TextNormalizer.normalize(account);
        Assert.assertFalse(workerTestPage.rows().isEmpty(),
                "Tìm tên tài khoản không trả dữ liệu.");
        Assert.assertTrue(workerTestPage.rows().stream()
                        .map(TestRow::account)
                        .map(TextNormalizer::normalize)
                        .allMatch(value -> value.contains(expected)),
                "Kết quả tìm kiếm chứa tài khoản không khớp.");
    }

    @Test(groups = {"worker-test-management", "search", "reset", "data-interaction"},
            description = WorkerTestManagementTestCases.WORKER_TEST_012)
    public void noResultAndResetRestoreDefaultData() {
        int defaultTotal = workerTestPage.totalDisplayed();
        workerTestPage.search("AUTOMATION_NOT_FOUND_987654321");
        Assert.assertTrue(workerTestPage.rows().isEmpty(),
                "Từ khóa không tồn tại vẫn trả dữ liệu.");
        Assert.assertTrue(workerTestPage.hasEmptyState(),
                "Bảng rỗng nhưng không hiển thị empty state.");

        workerTestPage.reset();
        Assert.assertTrue(workerTestPage.searchValue().isBlank(),
                "Reset không xóa từ khóa.");
        Assert.assertEquals(workerTestPage.activePage(), 1,
                "Reset không đưa về trang 1.");
        Assert.assertFalse(workerTestPage.rows().isEmpty(),
                "Reset không phục hồi dữ liệu.");
        Assert.assertEquals(workerTestPage.totalDisplayed(), defaultTotal,
                "Reset không phục hồi tổng số bản ghi.");
    }

    @DataProvider(name = "testedStatuses")
    public Object[][] testedStatuses() {
        return new Object[][]{
                {Status.INITIALIZED},
                {Status.IN_PROGRESS},
                {Status.PENDING},
                {Status.PASSED},
                {Status.FAILED}
        };
    }

    @Test(
            dataProvider = "testedStatuses",
            groups = {"worker-test-management", "filter", "data-interaction"},
            description = WorkerTestManagementTestCases.WORKER_TEST_013)
    public void eachStatusFilterReturnsMatchingData(Status status) {
        int defaultTotal = workerTestPage.totalDisplayed();
        workerTestPage.selectStatus(status);
        List<TestRow> rows = workerTestPage.rows();
        if (rows.isEmpty()) {
            Assert.assertTrue(workerTestPage.hasEmptyState(),
                    "Bộ lọc " + status.label()
                            + " không có dòng nhưng cũng không hiển thị empty state.");
            return;
        }
        Assert.assertTrue(rows.stream().allMatch(row -> row.status() == status),
                "Bộ lọc " + status.label() + " trả sai trạng thái.");
        Assert.assertTrue(workerTestPage.totalDisplayed() <= defaultTotal,
                "Tổng sau lọc lớn hơn tổng dữ liệu mặc định.");
    }

    @Test(groups = {"worker-test-management", "search", "filter", "data-interaction"},
            description = WorkerTestManagementTestCases.WORKER_TEST_014)
    public void statusAndAccountSearchReturnIntersection() {
        workerTestPage.selectStatus(Status.PASSED);
        String account = workerTestPage.rows().get(0).account();
        workerTestPage.search(account);
        String expected = TextNormalizer.normalize(account);
        Assert.assertFalse(workerTestPage.rows().isEmpty(),
                "Kết hợp bộ lọc và tìm kiếm không trả dữ liệu.");
        Assert.assertTrue(workerTestPage.rows().stream().allMatch(row ->
                        row.status() == Status.PASSED
                                && TextNormalizer.normalize(row.account()).contains(expected)),
                "Kết quả không thuộc giao của trạng thái và tên tài khoản.");
    }

    @Test(groups = {"worker-test-management", "filter", "reset", "data-interaction"},
            description = WorkerTestManagementTestCases.WORKER_TEST_015)
    public void filterPanelResetRestoresDefaultData() {
        int defaultTotal = workerTestPage.totalDisplayed();
        workerTestPage.selectStatus(Status.FAILED);
        Assert.assertTrue(workerTestPage.totalDisplayed() < defaultTotal,
                "Bộ lọc Đã rớt không thu hẹp dữ liệu.");
        workerTestPage.resetStatusFilter();
        Assert.assertEquals(workerTestPage.totalDisplayed(), defaultTotal,
                "Đặt lại trong Filter không phục hồi tổng dữ liệu.");
        Assert.assertTrue(workerTestPage.rows().stream()
                        .map(TestRow::status).distinct().count() > 1,
                "Đặt lại trong Filter vẫn giữ một trạng thái.");
    }
}
