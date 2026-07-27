package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra các chế độ hiển thị và menu thống kê của Đơn Khách - Thợ. */
public class CustomerWorkerOrderViewModeTest
        extends CustomerWorkerOrderTestSupport {

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

    @Test(groups = {"customer-worker-order", "view", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-012: Chuyển Bảng sang Thẻ và quay lại")
    public void tableAndCardViewsReturnOrders() {
        orderPage.switchView("Thẻ");
        Assert.assertTrue(orderPage.cardViewContainsOrders(),
                "Chế độ Thẻ không trả đơn.");
        orderPage.switchView("Bảng");
        Assert.assertFalse(orderPage.rows().isEmpty(),
                "Quay lại Bảng không trả đơn.");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "statistics-menu", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-014: Menu Thống kê hiển thị đúng hai báo cáo")
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
