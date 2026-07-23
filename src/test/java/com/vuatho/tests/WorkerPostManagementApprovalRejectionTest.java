package com.vuatho.tests;

import com.vuatho.config.TestConfig;
import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.time.Instant;

/**
 * Test thay đổi trạng thái dữ liệu bài đăng trên ERP sandbox.
 * Nếu không cấu hình marker, mỗi case tự chọn bài Chờ duyệt đầu tiên bằng
 * timestamp duy nhất. Có thể khóa mutation bằng run.worker.post.mutations=false.
 */
public class WorkerPostManagementApprovalRejectionTest extends WorkerPostManagementTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerPostManagementApprovalRejectionTest.class,
                "Quản lý bài đăng thợ", "Kiểm tra, duyệt và từ chối bài đăng");
    }

    @Test(groups = {"worker-post-management", "read-only", "moderation", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-018: Dialog Từ chối bắt buộc lý do và Hủy được")
    public void rejectDialogRequiresReasonAndCanBeCancelled() {
        int beforeTotal = workerPostManagementPage.totalPosts();
        workerPostManagementPage.openFirstRejectDialog();
        Assert.assertTrue(workerPostManagementPage.rejectDialogIsOpen(),
                "Click Từ chối nhưng dialog không mở.");
        Assert.assertTrue(workerPostManagementPage.rejectReasonIsRequired(),
                "Dialog không thể hiện Lý do từ chối là bắt buộc.");

        workerPostManagementPage.cancelRejectDialog();
        Assert.assertFalse(workerPostManagementPage.rejectDialogIsOpen(),
                "Click Hủy nhưng dialog vẫn mở.");
        Assert.assertEquals(workerPostManagementPage.totalPosts(), beforeTotal,
                "Hủy dialog nhưng tổng bài Chờ duyệt bị thay đổi.");
    }

    @Test(groups = {"worker-post-management", "mutation", "approve", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-020: Duyệt bài chuyển bài sang Đã duyệt")
    public void approvingDedicatedPostMovesItToApproved() {
        requireMutationsEnabled();
        String marker = markerOrFirstPending(TestConfig.workerPostApproveMarker());
        System.out.println("[WORKER POST MUTATION] Duyệt thật bài có marker: " + marker);
        Assert.assertTrue(workerPostManagementPage.approvePostByMarker(marker),
                "Duyệt xong nhưng không tìm thấy bài trong tab Đã duyệt: " + marker);
    }

    @Test(groups = {"worker-post-management", "mutation", "reject", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-021: Từ chối bài lưu lý do và chuyển tab")
    public void rejectingDedicatedPostMovesItToRejectedWithReason() {
        requireMutationsEnabled();
        String marker = markerOrFirstPending(TestConfig.workerPostRejectMarker());
        String reason = "AUTOMATION_REJECT_" + Instant.now().toEpochMilli();
        System.out.println("[WORKER POST MUTATION] Từ chối thật bài có marker: "
                + marker + ", lý do=" + reason);
        Assert.assertTrue(workerPostManagementPage.rejectPostByMarker(marker, reason),
                "Từ chối xong nhưng bài/lý do không xuất hiện tại tab Từ chối.");
    }

    private void requireMutationsEnabled() {
        if (!TestConfig.runWorkerPostMutations()) {
            System.out.println("[WORKER POST MUTATION] Case được tìm thấy nhưng đang khóa. "
                    + "Bật run.worker.post.mutations để click thật.");
            throw new SkipException(
                    "Mutation test bị khóa bởi -Drun.worker.post.mutations=false.");
        }
    }

    private String markerOrFirstPending(String configuredMarker) {
        return configuredMarker == null || configuredMarker.isBlank()
                ? workerPostManagementPage.firstPendingPostMarker()
                : configuredMarker;
    }
}
