package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerStopRequestPage.Action;
import com.vuatho.pages.WorkerStopRequestPage.DetailSnapshot;
import com.vuatho.pages.WorkerStopRequestPage.Status;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Thay đổi trạng thái thật trên dữ liệu sandbox của yêu cầu ngưng hợp tác. */
public class WorkerStopRequestApprovalRejectionTest extends WorkerStopRequestTestSupport {
    private static final String REJECT_REASON = "Đơn dịch vụ chưa hoàn tất";

    public static void main(String[] args) {
        String group = System.getProperty("worker.stop.action.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.runGroup(
                    "Yêu cầu ngưng hợp tác",
                    "Duyệt, từ chối và quay lại làm việc",
                    "data-interaction",
                    WorkerStopRequestApprovalRejectionTest.class);
        } else {
            TestNgRunner.runGroup(
                    "Yêu cầu ngưng hợp tác",
                    "Mutation " + group,
                    group,
                    WorkerStopRequestApprovalRejectionTest.class);
        }
    }

    @Test(groups = {"worker-stop-request", "mutation", "approve", "data-interaction"},
            description = "WORKER-STOP-REQUEST-009: Duyệt thật chuyển yêu cầu sang Đã duyệt")
    public void approveRequestMovesItToApproved() {
        DetailSnapshot request = stopRequestPage.openFirstPendingWithAction(Action.APPROVE);
        System.out.println("Duyệt thật yêu cầu #" + request.requestId());
        stopRequestPage.approveOpenRequest();
        Assert.assertTrue(
                stopRequestPage.requestDetailHasStatus(request.requestId(), Status.APPROVED),
                "Chi tiết yêu cầu #" + request.requestId() + " không có trạng thái Đã duyệt.");
    }

    @Test(groups = {"worker-stop-request", "mutation", "reject", "data-interaction"},
            description = "WORKER-STOP-REQUEST-010: Từ chối thật chuyển yêu cầu sang Đã từ chối")
    public void rejectRequestMovesItToRejected() {
        DetailSnapshot request = stopRequestPage.openFirstPendingWithAction(Action.REJECT);
        System.out.println("Từ chối thật yêu cầu #" + request.requestId()
                + " với lý do: " + REJECT_REASON);
        stopRequestPage.rejectOpenRequest(REJECT_REASON);
        Assert.assertTrue(
                stopRequestPage.requestDetailHasStatus(request.requestId(), Status.REJECTED),
                "Chi tiết yêu cầu #" + request.requestId()
                        + " không có trạng thái Đã từ chối.");
    }

    @Test(groups = {"worker-stop-request", "mutation", "skip", "requires-zero-balance"},
            description = "WORKER-STOP-REQUEST-011: Bỏ qua thật chuyển yêu cầu sang Đã bỏ qua")
    public void skipZeroBalanceRequestMovesItToSkipped() {
        DetailSnapshot request = stopRequestPage.openFirstPendingWithAction(Action.SKIP);
        stopRequestPage.skipOpenRequest();
        Assert.assertTrue(
                stopRequestPage.requestExistsInStatus(request.requestId(), Status.SKIPPED),
                "Yêu cầu #" + request.requestId() + " không xuất hiện trong Đã bỏ qua.");
    }

    @Test(groups = {"worker-stop-request", "mutation", "back-to-work", "data-interaction"},
            description = "WORKER-STOP-REQUEST-012: Mở khóa thật loại bỏ action Quay lại làm việc")
    public void backToWorkRemovesUnlockAction() {
        DetailSnapshot request = stopRequestPage.openFirstApprovedForBackToWork();
        System.out.println("Mở khóa quay lại làm việc cho yêu cầu #" + request.requestId());
        stopRequestPage.backToWorkOpenRequest();
        DetailSnapshot updated =
                stopRequestPage.openRequestInStatus(request.requestId(), Status.APPROVED);
        Assert.assertFalse(updated.canBackToWork(),
                "Yêu cầu #" + request.requestId()
                        + " vẫn còn action Quay lại làm việc sau khi mở khóa.");
    }
}
