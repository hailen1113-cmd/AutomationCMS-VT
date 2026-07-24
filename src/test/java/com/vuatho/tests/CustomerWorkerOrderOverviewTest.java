package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.CustomerWorkerOrderPage.OrderRow;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Kiểm tra dashboard thống kê và bảng dữ liệu Đơn Khách - Thợ. */
public class CustomerWorkerOrderOverviewTest extends CustomerWorkerOrderTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(CustomerWorkerOrderOverviewTest.class,
                "Đơn Khách - Thợ", "Tổng quan và dữ liệu");
    }

    @Test(groups = {"customer-worker-order", "overview", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-001: Tổng số đơn bằng hoàn thành + hủy + còn lại")
    public void orderSummaryCountsAreConsistent() {
        String text = orderPage.mainText();
        int total = numberAfter(text, "Tổng số đơn dịch vụ");
        int completed = numberAfter(text, "Hoàn thành đơn");
        int cancelled = numberAfter(text, "Hủy đơn");
        int remaining = numberAfter(text, "Còn lại");
        Assert.assertEquals(completed + cancelled + remaining, total,
                "Tổng trạng thái không bằng Tổng số đơn dịch vụ.");
        Assert.assertEquals(orderPage.totalDisplayed(), total,
                "Tổng hiển thị không bằng Tổng số đơn dịch vụ.");
    }

    @Test(groups = {"customer-worker-order", "overview", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-002: Thống kê hôm nay và tài chính trả số liệu")
    public void todayAndFinancialStatisticsReturnData() {
        String text = orderPage.mainText();
        for (String label : List.of(
                "Tổng phí kết nối", "Thực thu hôm nay", "Hôm nay *",
                "Đơn đã hoàn thành", "Đơn bị hủy", "Đơn còn lại")) {
            Assert.assertTrue(text.contains(label), "Thiếu thống kê " + label);
        }
        Assert.assertTrue(numberAfter(text, "Tổng phí kết nối") >= 0);
        Assert.assertTrue(numberAfter(text, "Thực thu hôm nay") >= 0);
    }

    @Test(groups = {"customer-worker-order", "overview", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-003: Top dịch vụ trả đủ số liệu trạng thái")
    public void topServicesReturnStatusBreakdown() {
        String text = orderPage.mainText();
        Assert.assertTrue(text.contains("Top dịch vụ nhiều đơn"));
        for (String label : List.of("HT:", "Huỷ:", "Còn:")) {
            Assert.assertTrue(text.contains(label), "Top dịch vụ thiếu " + label);
        }
        Assert.assertTrue(orderPage.topServiceCount() >= 5,
                "Top dịch vụ hiển thị ít hơn 5 thứ hạng.");
    }

    @Test(groups = {"customer-worker-order", "overview", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-004: Bảng trả đúng schema và định dạng từng dòng")
    public void tableRowsReturnValidOrderData() {
        Assert.assertEquals(orderPage.headers(), List.of(
                "Thông tin đơn dịch vụ", "Trạng thái", "Số thợ đã báo giá",
                "Phí kết nối", "Thời gian yêu cầu"));
        Assert.assertFalse(orderPage.rows().isEmpty(), "Bảng không trả đơn.");
        for (OrderRow row : orderPage.rows()) {
            Assert.assertTrue(row.id().matches("\\d+"), "ID không hợp lệ.");
            Assert.assertTrue(row.info().contains(row.id()),
                    "Thông tin dòng thiếu mã đơn #" + row.id());
            Assert.assertFalse(row.status().isBlank(),
                    "Đơn #" + row.id() + " thiếu trạng thái.");
            Assert.assertTrue(row.workerCount().matches("\\d+"),
                    "Số thợ báo giá không hợp lệ ở #" + row.id());
            Assert.assertTrue(row.connectionFee().matches("[\\d.,]+₫"),
                    "Phí kết nối không hợp lệ ở #" + row.id());
            Assert.assertTrue(row.requestedAt().matches(
                            "\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}"),
                    "Thời gian yêu cầu không hợp lệ ở #" + row.id());
        }
    }

    private static int numberAfter(String text, String label) {
        Matcher matcher = Pattern.compile(
                        Pattern.quote(label) + "\\R([\\d.,]+)",
                        Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (!matcher.find()) {
            throw new AssertionError("Không đọc được số sau " + label);
        }
        return Integer.parseInt(matcher.group(1).replaceAll("\\D", ""));
    }
}
