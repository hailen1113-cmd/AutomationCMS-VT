package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerStopRequestPage.RequestRow;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/** Kiểm tra dữ liệu tổng quan và cấu trúc bảng yêu cầu ngưng hợp tác. */
public class WorkerStopRequestOverviewTest extends WorkerStopRequestTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerStopRequestOverviewTest.class,
                "Yêu cầu ngưng hợp tác", "Tổng quan và dữ liệu bảng");
    }

    @Test(groups = {"worker-stop-request", "data-interaction"},
            description = "WORKER-STOP-REQUEST-001: Thống kê và bảng trả về dữ liệu hợp lệ")
    public void statisticsAndTableReturnValidData() {
        Map<String, Integer> statistics = stopRequestPage.statistics();
        Assert.assertEquals(statistics.size(), 5, "Không đủ 5 thẻ thống kê.");
        Assert.assertTrue(statistics.values().stream().allMatch(value -> value >= 0),
                "Thống kê có số âm: " + statistics);
        Assert.assertEquals(
                stopRequestPage.columnHeaders(),
                List.of("Thông tin thợ", "Lý do", "Thái độ", "Trạng thái", "Thời gian yêu cầu"),
                "Cột dữ liệu không đúng.");

        List<RequestRow> rows = stopRequestPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Bảng không trả về dữ liệu.");
        Assert.assertTrue(rows.stream().allMatch(row ->
                        !row.id().isBlank()
                                && !row.workerName().isBlank()
                                && !row.phone().isBlank()
                                && row.status() != null
                                && row.requestedAt().matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}")),
                "Có dòng thiếu thông tin hoặc sai định dạng.");
    }
}
