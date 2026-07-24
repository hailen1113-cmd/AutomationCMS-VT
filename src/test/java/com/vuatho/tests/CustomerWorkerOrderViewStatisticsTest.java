package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra Bảng/Thẻ, xuất Excel và biểu đồ thống kê. */
public class CustomerWorkerOrderViewStatisticsTest extends CustomerWorkerOrderTestSupport {
    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(CustomerWorkerOrderViewStatisticsTest.class,
                    "Đơn Khách - Thợ", "Hiển thị, Excel và thống kê");
        } else {
            TestNgRunner.runGroup(
                    "Đơn Khách - Thợ", "Hiển thị, Excel và thống kê - " + group,
                    group, CustomerWorkerOrderViewStatisticsTest.class);
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

    @Test(groups = {"customer-worker-order", "export", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-013: Xuất Excel thực thi thành công")
    public void exportExcelStartsDownload() {
        Assert.assertTrue(orderPage.exportExcel(),
                "Không thực thi được Xuất Excel.");
    }

    @DataProvider(name = "statistics")
    public Object[][] statistics() {
        return new Object[][]{
                {"Trạng thái đơn"},
                {"Bảo hành 5K"}
        };
    }

    @Test(dataProvider = "statistics",
            groups = {"customer-worker-order", "statistics", "chart", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-014: Mỗi báo cáo render chart và hover datum trả ngữ cảnh dữ liệu")
    public void statisticsChartsRenderAndReturnTooltip(String statistic) {
        orderPage.openStatistic(statistic);
        Assert.assertTrue(orderPage.statisticsChartRendered(),
                "Biểu đồ " + statistic + " không render.");
        Assert.assertFalse(orderPage.hoverLargestStatisticsDatum().isBlank(),
                "Hover biểu đồ " + statistic + " không trả ngữ cảnh dữ liệu.");
    }
}
