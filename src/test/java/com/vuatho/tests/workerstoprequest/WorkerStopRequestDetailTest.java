package com.vuatho.tests.workerstoprequest;

import com.vuatho.testcases.WorkerStopRequestTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.support.workerstoprequest.WorkerStopRequestTestSupport;
import com.vuatho.pages.WorkerStopRequestPage.DetailSnapshot;
import com.vuatho.pages.WorkerStopRequestPage.Status;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra modal chi tiết và điều kiện hiển thị hành động xử lý. */
public class WorkerStopRequestDetailTest extends WorkerStopRequestTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerStopRequestDetailTest.class,
                "Yêu cầu ngưng hợp tác", "Chi tiết yêu cầu");
    }

    @Test(groups = {"worker-stop-request", "detail", "data-interaction"},
            description = WorkerStopRequestTestCases.WORKER_STOP_005)
    public void pendingRequestDetailReturnsRequiredInformation() {
        DetailSnapshot detail = stopRequestPage.openFirstPendingRequest();
        Assert.assertTrue(detail.text().contains("ID: #"), "Chi tiết thiếu ID yêu cầu.");
        for (String label : new String[]{
                "Tổng đơn", "Hoàn thành", "Huỷ", "Ngày đăng ký",
                "Số dư chi phí", "Số dư doanh thu", "Lý do từ thợ"}) {
            Assert.assertTrue(detail.text().contains(label), "Chi tiết thiếu " + label);
        }
        Assert.assertTrue(
                (detail.canApprove() && detail.canReject()) || detail.canSkip(),
                "Yêu cầu chờ xử lý không có nhánh Duyệt/Từ chối hoặc Bỏ qua.");
    }

    @Test(groups = {"worker-stop-request", "detail", "history", "data-interaction"},
            description = WorkerStopRequestTestCases.WORKER_STOP_006)
    public void repeatedRequestDisplaysHistory() {
        DetailSnapshot detail = stopRequestPage.openFirstRepeatedRequest();
        Assert.assertTrue(detail.hasHistory(), "Thợ yêu cầu nhiều lần nhưng modal thiếu lịch sử.");
    }

    @Test(groups = {"worker-stop-request", "detail", "data-interaction"},
            description = WorkerStopRequestTestCases.WORKER_STOP_007)
    public void approvedRequestDisplaysProcessedDetail() {
        stopRequestPage.selectStatus(Status.APPROVED);
        DetailSnapshot detail = stopRequestPage.openFirstRow();
        Assert.assertTrue(detail.text().contains("Đã duyệt"), "Chi tiết thiếu trạng thái Đã duyệt.");
        Assert.assertFalse(detail.canApprove() || detail.canReject() || detail.canSkip(),
                "Yêu cầu đã duyệt vẫn còn action xử lý ban đầu.");
    }

    @Test(groups = {"worker-stop-request", "detail", "validation", "data-interaction"},
            description = WorkerStopRequestTestCases.WORKER_STOP_008)
    public void rejectActionRequiresPredefinedReason() {
        DetailSnapshot detail = stopRequestPage.openFirstPendingRequest();
        if (!detail.canReject()) {
            throw new IllegalStateException(
                    "Yêu cầu đầu tiên chỉ cho phép Bỏ qua, không có dữ liệu để kiểm tra Từ chối.");
        }
        stopRequestPage.chooseRejectAction();
        String text = stopRequestPage.dialogText();
        for (String reason : new String[]{
                "Đơn dịch vụ chưa hoàn tất",
                "Thông tin tài khoản ngân hàng không hợp lệ",
                "Đang bị phạt"}) {
            Assert.assertTrue(text.contains(reason), "Thiếu lý do từ chối: " + reason);
        }
        Assert.assertTrue(stopRequestPage.rejectConfirmationDisabled(),
                "Chưa chọn lý do nhưng nút xác nhận từ chối vẫn khả dụng.");
    }
}
