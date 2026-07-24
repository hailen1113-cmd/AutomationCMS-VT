package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.CustomerWorkerOrderPage.DetailSnapshot;
import com.vuatho.pages.CustomerWorkerOrderPage.MutationResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Thao tác thật trên dữ liệu sandbox của Đơn Khách - Thợ. */
public class CustomerWorkerOrderWorkflowTest extends CustomerWorkerOrderTestSupport {
    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(CustomerWorkerOrderWorkflowTest.class,
                    "Đơn Khách - Thợ", "Xử lý đơn thật");
        } else {
            TestNgRunner.runGroup(
                    "Đơn Khách - Thợ", "Xử lý đơn thật - " + group, group,
                    CustomerWorkerOrderWorkflowTest.class);
        }
    }

    @Test(priority = 1,
            groups = {"customer-worker-order", "mutation", "advance", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-022: Sang bước kế tiếp thật cập nhật trạng thái")
    public void advanceOrderReallyChangesStatus() {
        DetailSnapshot detail =
                orderPage.openFirstOrderWithAction("Sang bước kế tiếp");
        System.out.println("Sang buoc that don #" + detail.id());
        MutationResult result = orderPage.advanceOpenOrder();
        Assert.assertNotEquals(result.afterStatus(), result.beforeStatus(),
                "Trạng thái đơn #" + result.id() + " không thay đổi.");
    }

    @Test(priority = 2,
            groups = {"customer-worker-order", "mutation", "cancel", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-023: Hủy đơn thật cập nhật trạng thái và lý do")
    public void cancelOrderReallyChangesStatus() {
        DetailSnapshot detail = orderPage.openFirstOrderWithAction("Hủy đơn");
        System.out.println("Huy that don #" + detail.id());
        MutationResult result = orderPage.cancelOpenOrder(
                "Automation test",
                "Dữ liệu sandbox dùng để kiểm thử luồng hủy đơn");
        Assert.assertEquals(result.afterStatus(), "Hủy đơn",
                "Đơn #" + result.id() + " chưa chuyển sang Hủy đơn.");
        Assert.assertTrue(result.detailText().contains("Automation test")
                        || result.detailText().contains("Dữ liệu sandbox"),
                "Chi tiết đơn hủy không ghi nhận tiêu đề/lý do automation.");
    }
}
