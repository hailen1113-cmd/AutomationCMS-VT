package com.vuatho.tests.customerworkerorder;

import com.vuatho.testcases.CustomerWorkerOrderTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.support.customerworkerorder.CustomerWorkerOrderTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Kiểm tra các control đổi cách hiển thị trên màn hình danh sách.
 *
 * <p>File này chỉ bao phủ chuyển Bảng/Thẻ và nội dung menu Thống kê. Việc kiểm
 * tra dữ liệu bên trong hai popup thống kê được tách sang
 * {@link CustomerWorkerOrderStatusStatisticsTest} và
 * {@link CustomerWorkerOrderWarrantyStatisticsTest} để tránh trùng case.</p>
 */
public class CustomerWorkerOrderViewModeTest
        extends CustomerWorkerOrderTestSupport {

    /** Chạy toàn bộ file hoặc riêng group truyền qua {@code customer.order.group}. */
    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(CustomerWorkerOrderViewModeTest.class,
                    "Đơn Khách - Thợ", "Chế độ hiển thị");
        } else {
            TestNgRunner.runGroup("Đơn Khách - Thợ",
                    "Chế độ hiển thị - " + group,
                    group, CustomerWorkerOrderViewModeTest.class);
        }
    }

    /** Chuyển Bảng sang Thẻ rồi quay lại và kiểm tra cả hai chế độ có dữ liệu. */
    @Test(groups = {"customer-worker-order", "view", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_060)
    public void tableAndCardViewsReturnOrders() {
        orderPage.switchView("Thẻ");
        Assert.assertTrue(orderPage.cardViewContainsOrders(),
                "Chế độ Thẻ không trả đơn.");
        orderPage.switchView("Bảng");
        Assert.assertFalse(orderPage.rows().isEmpty(),
                "Quay lại Bảng không trả đơn.");
    }

    /** Kiểm tra menu Thống kê chỉ chứa hai báo cáo được thiết kế. */
    @Test(groups = {"customer-worker-order", "statistics",
            "statistics-menu", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_061)
    public void statisticsMenuContainsExactlyTwoReports() {
        List<String> options = orderPage.statisticsMenuOptions();
        Assert.assertEquals(options.size(), 2);
        Assert.assertTrue(options.get(0).startsWith("Trạng thái đơn"));
        Assert.assertTrue(options.get(0)
                .contains("Thống kê đơn theo trạng thái"));
        Assert.assertTrue(options.get(1).startsWith("Bảo hành 5K"));
        Assert.assertTrue(options.get(1)
                .contains("Số lượng đơn và doanh thu bảo hành"));
    }
}
