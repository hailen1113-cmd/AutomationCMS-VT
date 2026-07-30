package com.vuatho.tests.customerworkerorder;

import com.vuatho.testcases.CustomerWorkerOrderTestCases;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.customerworkerorder.CustomerWorkerOrderTestSupport;
import com.vuatho.pages.CustomerWorkerOrderPage.OrderRow;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kiểm tra tổng quan dashboard và dữ liệu bảng của Đơn Khách - Thợ.
 *
 * <p>Nhóm này chỉ đọc dữ liệu: đối soát các KPI tổng, thống kê hôm nay,
 * bảng xếp hạng dịch vụ, tiêu đề cột và dữ liệu từng dòng. Không mở tiến trình
 * và không thay đổi trạng thái đơn.</p>
 */
public class CustomerWorkerOrderOverviewTest extends CustomerWorkerOrderTestSupport {
    /** Entry point chạy riêng nhóm testcase Tổng quan và dữ liệu. */
    public static void main(String[] args) {
        TestNgRunner.run(CustomerWorkerOrderOverviewTest.class,
                "Đơn Khách - Thợ", "Tổng quan và dữ liệu");
    }

    /** Kiểm tra tổng đơn bằng tổng ba nhóm hoàn thành, hủy và còn lại. */
    @Test(groups = {"customer-worker-order", "overview", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_021)
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

    /** Kiểm tra các KPI hôm nay và tài chính có nhãn, có giá trị số hợp lệ. */
    @Test(groups = {"customer-worker-order", "overview", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_022)
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

    /** Kiểm tra bảng xếp hạng dịch vụ trả đủ phân rã trạng thái. */
    @Test(groups = {"customer-worker-order", "overview", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_023)
    public void topServicesReturnStatusBreakdown() {
        String text = orderPage.mainText();
        Assert.assertTrue(text.contains("Top dịch vụ nhiều đơn"));
        for (String label : List.of("HT:", "Huỷ:", "Còn:")) {
            Assert.assertTrue(text.contains(label), "Top dịch vụ thiếu " + label);
        }
        Assert.assertTrue(orderPage.topServiceCount() >= 5,
                "Top dịch vụ hiển thị ít hơn 5 thứ hạng.");
    }

    /** Kiểm tra schema và định dạng dữ liệu thực tế của từng dòng đơn. */
    @Test(groups = {"customer-worker-order", "overview", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_024)
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

    /** Trích số nguyên đầu tiên đứng sau một nhãn KPI trong text dashboard. */
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
