package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformOrderPage.OrderRow;
import com.vuatho.support.UniformModuleTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/** Kiểm tra dashboard và cấu trúc dữ liệu Đơn hàng Đồng phục. */
public class UniformOrderOverviewTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformOrderOverviewTest.class,
                "Đồng phục", "Tổng quan đơn hàng");
    }

    /** KPI, cột bảng và dữ liệu đơn hàng phải được trả đầy đủ. */
    @Test(groups = {"uniform", "order", "data-interaction"},
            description = "UNIFORM-ORDER-001: Dashboard và bảng đơn trả dữ liệu hợp lệ")
    public void dashboardAndOrderTableReturnValidData() {
        uniformOrderPage.open();
        Map<String, String> statistics = uniformOrderPage.statistics();
        Assert.assertEquals(statistics.size(), 6,
                "Không đủ KPI doanh thu và trạng thái: " + statistics.keySet());
        Assert.assertTrue(statistics.get("Doanh thu đã thu").contains("₫"));
        Assert.assertTrue(statistics.get("Doanh thu chưa thu").contains("₫"));

        Assert.assertEquals(uniformOrderPage.headers(), List.of(
                "Thông tin đơn hàng", "Thông tin đặt hàng",
                "Trạng thái", "Lịch sử thao tác"));
        Assert.assertTrue(uniformOrderPage.totalDisplayed() > 0,
                "Danh sách đơn không có tổng dữ liệu.");

        List<OrderRow> rows = uniformOrderPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Bảng đơn không trả dòng dữ liệu.");
        Assert.assertTrue(rows.stream().allMatch(row ->
                        !row.id().isBlank()
                                && !row.customer().isBlank()
                                && !row.phone().isBlank()
                                && !row.orderStatus().isBlank()
                                && !row.paymentStatus().isBlank()),
                "Có dòng đơn thiếu thông tin bắt buộc.");
    }
}
