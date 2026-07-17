package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerProfilePage.ServiceOrderDetailResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Kiểm tra các mã đơn trong bảng Danh sách đơn dịch vụ của hồ sơ thợ có thể mở chi tiết.
 */
public class WorkerProfileServiceOrderListTest extends WorkerProfileTestSupport {
    private static final int MAX_ORDERS_TO_OPEN = 3;
    private static final Duration ORDER_TABLE_VIEW_DURATION = Duration.ofSeconds(4);
    private static final Duration ORDER_DETAIL_VIEW_DURATION = Duration.ofSeconds(6);
    private static final Duration FINAL_VIEW_DURATION = Duration.ofSeconds(6);

    /** Cho phép chạy trực tiếp testcase từ IDE. */
    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfileServiceOrderListTest.class,
                "Bo test don dich vu trong ho so tho ERP",
                "Click cac ma don va kiem tra tab chi tiet");
    }

    /**
     * Mở tối đa ba mã đơn đầu tiên, đối chiếu đúng mã ở tab mới và quay lại danh sách sau mỗi lần.
     */
    @Test(groups = { "partner-worker", "worker-profile", "worker-service-order" },
            description = "WORKER-PROFILE-ORDER-001: Mo cac don dich vu dau tien trong tab moi")
    public void firstServiceOrdersOpenMatchingDetailsInNewTabs() {
        workerProfilePage.openFirstWorkerInformation();
        workerProfilePage.openWorkerDetailTab("Đơn dịch vụ");

        Assert.assertTrue(workerProfilePage.hasWorkerServiceOrderTable(),
                "Khong tim thay bang Danh sach don dich vu.");
        int rowCount = workerProfilePage.workerServiceOrderRowCount();
        Assert.assertTrue(rowCount > 0,
                "Bang Danh sach don dich vu khong co don nao de click.");
        workerProfilePage.keepWorkerDetailVisible(ORDER_TABLE_VIEW_DURATION);

        int ordersToOpen = Math.min(MAX_ORDERS_TO_OPEN, rowCount);
        for (int index = 0; index < ordersToOpen; index++) {
            System.out.println("[WORKER SERVICE ORDER] CLICK don thu " + (index + 1) + ".");
            ServiceOrderDetailResult result = workerProfilePage
                    .openWorkerServiceOrderInNewTabAndReturn(index, ORDER_DETAIL_VIEW_DURATION);

            Assert.assertTrue(result.orderId().matches("\\d+"),
                    "Khong doc duoc ma don dich vu o dong thu " + (index + 1) + ".");
            Assert.assertTrue(result.sourceRowText().contains(result.orderId()),
                    "Dong nguon khong chua ma don " + result.orderId() + ".");
            Assert.assertTrue(result.detailText().contains(result.orderId())
                            || result.detailUrl().contains(result.orderId()),
                    "Tab chi tiet khong hien thi dung ma don " + result.orderId() + ".");
            System.out.println("[WORKER SERVICE ORDER] Da mo dung chi tiet don "
                    + result.orderId() + " va quay lai danh sach.");
        }

        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }
}
