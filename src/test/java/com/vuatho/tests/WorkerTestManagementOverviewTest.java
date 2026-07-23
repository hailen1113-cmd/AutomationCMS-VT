package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerTestManagementPage.TestRow;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra trang danh sách và dữ liệu tổng quan của menu Bài kiểm tra. */
public class WorkerTestManagementOverviewTest extends WorkerTestManagementTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerTestManagementOverviewTest.class,
                "Bài kiểm tra", "Tổng quan danh sách");
    }

    @Test(groups = {"worker-test-management", "overview", "data-interaction"},
            description = "WORKER-TESTED-001: Trang trả đúng schema và có dữ liệu bài kiểm tra")
    public void tableReturnsExpectedSchemaAndData() {
        Assert.assertEquals(workerTestPage.columnHeaders(), List.of(
                "ID", "Tên nghiệp vụ", "Tài khoản", "Số lần làm bài",
                "Tỉ lệ đúng (lần cuối)", "Trạng thái", "Thời gian tạo"));
        Assert.assertFalse(workerTestPage.rows().isEmpty(),
                "Bảng Bài kiểm tra không trả dữ liệu.");
        Assert.assertTrue(workerTestPage.totalDisplayed() >= workerTestPage.rows().size(),
                "Tổng hiển thị nhỏ hơn số dòng trên trang.");
        Assert.assertTrue(workerTestPage.totalPages() >= 1,
                "Tổng số trang không hợp lệ.");
        int pageSize = workerTestPage.rows().size();
        int expectedPages = (int) Math.ceil(
                workerTestPage.totalDisplayed() / (double) pageSize);
        Assert.assertEquals(workerTestPage.totalPages(), expectedPages,
                "Tổng số trang không khớp Tổng hiển thị và số dòng mỗi trang.");
    }

    @Test(groups = {"worker-test-management", "overview", "data-interaction"},
            description = "WORKER-TESTED-002: Mỗi dòng trả dữ liệu và định dạng hợp lệ")
    public void everyVisibleRowReturnsValidValues() {
        for (TestRow row : workerTestPage.rows()) {
            Assert.assertTrue(row.key().matches("\\d+"), "data-key không hợp lệ: " + row.key());
            Assert.assertEquals(row.id(), row.key(), "ID hiển thị khác data-key.");
            Assert.assertFalse(row.service().isBlank(), "Bài #" + row.id() + " thiếu nghiệp vụ.");
            Assert.assertFalse(row.account().isBlank(), "Bài #" + row.id() + " thiếu tài khoản.");
            Assert.assertTrue(row.attempts().matches("\\d+"),
                    "Số lần làm bài không hợp lệ ở #" + row.id() + ": " + row.attempts());
            Assert.assertNotNull(row.status(), "Trạng thái không hợp lệ ở #" + row.id());
            boolean scoreIsValid = row.score().matches("\\d{1,3}%")
                    || (row.score().isBlank()
                    && (row.status() == com.vuatho.pages.WorkerTestManagementPage.Status.INITIALIZED
                    || row.status() == com.vuatho.pages.WorkerTestManagementPage.Status.IN_PROGRESS
                    || row.status() == com.vuatho.pages.WorkerTestManagementPage.Status.PENDING));
            Assert.assertTrue(scoreIsValid,
                    "Tỉ lệ đúng không hợp lệ ở #" + row.id() + ": " + row.score());
            Assert.assertTrue(row.createdAt().matches(
                            "\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}"),
                    "Thời gian tạo không hợp lệ ở #" + row.id() + ": " + row.createdAt());
        }
    }
}
