package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.CustomerWorkerOrderPage.DetailSnapshot;
import com.vuatho.pages.CustomerWorkerOrderPage.MutationResult;
import com.vuatho.pages.CustomerWorkerOrderPage.OrderDataUnavailableException;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/** Thao tác thật trên dữ liệu sandbox của Đơn Khách - Thợ. */
public class CustomerWorkerOrderWorkflowTest extends CustomerWorkerOrderTestSupport {
    private static final List<String> ADVANCE_WORKFLOW = List.of(
            "Match đơn", "Thợ di chuyển", "Thợ checkin",
            "Đang làm việc", "Đã xong việc", "Hoàn thành đơn");

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
            groups = {"customer-worker-order", "workflow", "searching-worker", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-022: Đơn Tìm kiếm thợ chỉ cho phép hủy")
    public void searchingWorkerOrderOnlyAllowsCancellation() {
        DetailSnapshot detail = requireWorkflowOrder("Tìm kiếm thợ", "Hủy đơn");
        Assert.assertTrue(detail.buttons().contains("Hủy đơn"),
                "Đơn #" + detail.id() + " không có action Hủy đơn.");
        Assert.assertFalse(detail.buttons().contains("Sang bước kế tiếp"),
                "Đơn Tìm kiếm thợ không được tự ý Sang bước kế tiếp.");
    }

    @Test(priority = 2,
            groups = {"customer-worker-order", "mutation", "advance", "match-to-travelling",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-023: Match đơn sang Thợ di chuyển")
    public void matchedOrderAdvancesToWorkerTravelling() {
        assertRealTransition("Match đơn", "Thợ di chuyển");
    }

    @Test(priority = 3,
            groups = {"customer-worker-order", "mutation", "advance", "travelling-to-checkin",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-024: Thợ di chuyển sang Thợ checkin")
    public void travellingOrderAdvancesToWorkerCheckin() {
        assertRealTransition("Thợ di chuyển", "Thợ checkin");
    }

    @Test(priority = 4,
            groups = {"customer-worker-order", "mutation", "advance", "checkin-to-working",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-025: Thợ checkin sang Đang làm việc")
    public void checkedInOrderAdvancesToWorking() {
        assertRealTransition("Thợ checkin", "Đang làm việc");
    }

    @Test(priority = 5,
            groups = {"customer-worker-order", "mutation", "advance", "working-to-done",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-026: Đang làm việc sang Đã xong việc")
    public void workingOrderAdvancesToWorkDone() {
        assertRealTransition("Đang làm việc", "Đã xong việc");
    }

    @Test(priority = 6,
            groups = {"customer-worker-order", "mutation", "advance", "done-to-completed",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-027: Đã xong việc sang Hoàn thành đơn")
    public void workDoneOrderAdvancesToCompleted() {
        assertRealTransition("Đã xong việc", "Hoàn thành đơn");
    }

    @Test(priority = 10,
            groups = {"customer-worker-order", "mutation", "cancel", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-028: Hủy đơn thật cập nhật trạng thái và lý do")
    public void cancelOrderReallyChangesStatus() {
        DetailSnapshot detail = requireAnyCancelableOrder();
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

    private DetailSnapshot requireWorkflowOrder(String status, String action) {
        orderPage.open();
        try {
            return orderPage.openFirstOrderForWorkflow(status, action);
        } catch (OrderDataUnavailableException exception) {
            throw new AssertionError(
                    "[THIẾU DỮ LIỆU TEST] Không có đơn trạng thái '" + status
                            + "' với action '" + action
                            + "'. Cần tạo/đưa ít nhất một đơn sandbox về trạng thái này.",
                    exception);
        }
    }

    private DetailSnapshot requireAnyCancelableOrder() {
        List<String> attempted = new ArrayList<>();
        for (String status : List.of(
                "Tìm kiếm thợ", "Match đơn", "Thợ di chuyển",
                "Thợ checkin", "Đang làm việc", "Đã xong việc")) {
            orderPage.open();
            try {
                return orderPage.openFirstOrderForWorkflow(status, "Hủy đơn");
            } catch (OrderDataUnavailableException exception) {
                attempted.add(status);
            }
        }
        throw new AssertionError(
                "[THIẾU DỮ LIỆU TEST] Không có đơn nào cho phép Hủy đơn. "
                        + "Đã kiểm tra các trạng thái: " + attempted);
    }

    private DetailSnapshot findOrPrepareOrderAtStatus(String targetStatus) {
        int targetIndex = ADVANCE_WORKFLOW.indexOf(targetStatus);
        if (targetIndex < 0 || targetIndex == ADVANCE_WORKFLOW.size() - 1) {
            throw new IllegalArgumentException(
                    "Trạng thái không thể chuẩn bị để Sang bước: " + targetStatus);
        }

        List<String> unavailable = new ArrayList<>();
        for (int sourceIndex = targetIndex; sourceIndex >= 0; sourceIndex--) {
            String sourceStatus = ADVANCE_WORKFLOW.get(sourceIndex);
            orderPage.open();
            DetailSnapshot source;
            try {
                source = orderPage.openFirstOrderForWorkflow(
                        sourceStatus, "Sang bước kế tiếp");
            } catch (OrderDataUnavailableException exception) {
                unavailable.add(sourceStatus);
                continue;
            }

            String orderId = source.id();
            for (int index = sourceIndex; index < targetIndex; index++) {
                String expectedNext = ADVANCE_WORKFLOW.get(index + 1);
                System.out.printf(
                        "[CHUAN BI DU LIEU] Chuyen don #%s: %s -> %s%n",
                        orderId,
                        TextNormalizer.normalize(ADVANCE_WORKFLOW.get(index)),
                        TextNormalizer.normalize(expectedNext));
                MutationResult preparation = orderPage.advanceOpenOrder();
                Assert.assertEquals(preparation.afterStatus(), expectedNext,
                        "Không chuẩn bị được đơn #" + orderId
                                + " về trạng thái " + expectedNext + ".");
            }

            if (sourceIndex < targetIndex) {
                orderPage.closeOverlay();
                orderPage.open();
                return orderPage.openOrder(orderId);
            }
            return source;
        }

        throw new AssertionError(
                "[THIẾU DỮ LIỆU TEST] Không thể chuẩn bị đơn ở trạng thái '"
                        + targetStatus + "'. Đã tìm từ các trạng thái: " + unavailable
                        + ". Cần tối thiểu một đơn Match đơn có action Sang bước kế tiếp.");
    }

    private void assertRealTransition(String expectedBefore, String expectedAfter) {
        DetailSnapshot detail = findOrPrepareOrderAtStatus(expectedBefore);
        Assert.assertEquals(detail.status(), expectedBefore,
                "Đơn #" + detail.id() + " không đúng trạng thái đầu.");
        Assert.assertTrue(detail.buttons().contains("Sang bước kế tiếp"),
                "Đơn #" + detail.id() + " không có action Sang bước kế tiếp.");

        System.out.printf("[WORKFLOW] Chuyen that don #%s: %s -> %s%n",
                detail.id(),
                TextNormalizer.normalize(expectedBefore),
                TextNormalizer.normalize(expectedAfter));
        MutationResult result = orderPage.advanceOpenOrder();
        Assert.assertEquals(result.beforeStatus(), expectedBefore,
                "Trạng thái trước khi chuyển của đơn #" + result.id() + " không đúng.");
        Assert.assertEquals(result.afterStatus(), expectedAfter,
                "Đơn #" + result.id() + " không chuyển đúng trạng thái.");
    }
}
