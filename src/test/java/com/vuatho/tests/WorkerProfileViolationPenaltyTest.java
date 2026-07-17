package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerProfilePage.WorkerPenaltyDialogResult;
import com.vuatho.pages.WorkerProfilePage.WorkerPenaltyApplyResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Kiểm tra popup Thiết lập xử phạt trong tab Xử lý vi phạm của hồ sơ thợ.
 */
public class WorkerProfileViolationPenaltyTest extends WorkerProfileTestSupport {
    private static final int USERS_TO_TEST = 3;
    private static final String TEST_ORDER_ID = "18395";
    private static final String TEST_PENALTY_TITLE = "AUTOMATION TEST - KHONG AP DUNG";
    private static final String TEST_REASON = "Du lieu kiem thu popup xu phat, testcase se bam Huy bo.";
    private static final String TEST_AMOUNT = "1000";
    private static final String TEST_BLOCKING_DAYS = "1";
    private static final Duration POPUP_VIEW_DURATION = Duration.ofSeconds(6);
    private static final Duration FINAL_VIEW_DURATION = Duration.ofSeconds(5);

    /** Cho phép chạy trực tiếp testcase từ IDE. */
    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfileViolationPenaltyTest.class,
                "Bo test xu ly vi pham trong ho so tho ERP",
                "Mo va kiem tra popup thiet lap xu phat");
    }

    /**
     * Lần lượt kiểm tra popup xử phạt của ba user đầu danh sách và hủy mà không tạo dữ liệu thật.
     */
    @Test(priority = 1, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-CANCEL-001: Kiem tra va huy popup xu phat tren 3 user")
    public void penaltyDialogShowsExpectedControlsAndCanBeCancelled() {
        workerProfilePage.resetAndLoadDefaultWorkerList(USERS_TO_TEST);

        for (int userIndex = 0; userIndex < USERS_TO_TEST; userIndex++) {
            String workerRow = workerProfilePage.workerRowTextAt(userIndex);
            System.out.println("[WORKER VIOLATION] USER " + (userIndex + 1) + ": "
                    + workerRow.replaceAll("\\R", " | "));
            workerProfilePage.openWorkerInformationAt(userIndex);
            Assert.assertTrue(workerProfilePage.workerDetailIsOpen(),
                    "Khong mo duoc chi tiet user thu " + (userIndex + 1) + ".");

            workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
            Assert.assertTrue(workerProfilePage.hasWorkerViolationHistorySection(),
                    "User thu " + (userIndex + 1)
                            + " khong co bang Lich su vi pham hoac nut Xu phat.");

            System.out.println("[WORKER VIOLATION] CLICK Xu phat user thu " + (userIndex + 1) + ".");
            WorkerPenaltyDialogResult result = workerProfilePage.fillWorkerPenaltyDialogAndCancel(
                    TEST_ORDER_ID,
                    TEST_PENALTY_TITLE,
                    TEST_REASON,
                    TEST_AMOUNT,
                    TEST_BLOCKING_DAYS,
                    userIndex == 0,
                    POPUP_VIEW_DURATION);

            Assert.assertTrue(result.requiredFieldsPresent(),
                    "Popup user thu " + (userIndex + 1) + " thieu field/button bat buoc.");
            Assert.assertTrue(result.initialStateValid(),
                    "Popup user thu " + (userIndex + 1) + " khong rong khi moi mo.");
            Assert.assertTrue(result.emptySubmissionBlocked(),
                    "Form rong cua user thu " + (userIndex + 1) + " khong bi validation chan.");
            Assert.assertTrue(result.testDataEntered(),
                    "User thu " + (userIndex + 1) + " chua nhan du du lieu mau.");
            Assert.assertTrue(result.permanentDisablesBlockingDays(),
                    "Bat Vinh vien khong khoa So ngay cua user thu " + (userIndex + 1) + ".");
            Assert.assertTrue(result.permanentOffEnablesBlockingDays(),
                    "Tat Vinh vien khong mo lai So ngay cua user thu " + (userIndex + 1) + ".");
            Assert.assertTrue(result.restrictionOptionsMutuallyExclusive(),
                    "Hai pham vi han che cua user thu " + (userIndex + 1) + " khong loai tru nhau.");
            Assert.assertTrue(result.cancelledWithoutCreatingViolation(),
                    "Popup user thu " + (userIndex + 1) + " khong Huy bo dung.");
            Assert.assertTrue(result.topCloseButtonWorks(),
                    "Nut X cua popup user thu " + (userIndex + 1) + " khong dong dung.");

            System.out.println("[WORKER VIOLATION] User thu " + (userIndex + 1)
                    + " hop le; da Huy bo, khong tao xu phat.");
            workerProfilePage.closeWorkerDetail();
        }

        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }

    /**
     * Lấy mã đơn thật của user đầu danh sách, áp dụng xử phạt và kiểm tra bản ghi lịch sử mới.
     */
    @Test(priority = 2, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-APPLY-002: Ap dung xu phat va kiem tra lich su")
    public void validPenaltyCanBeAppliedAndAppearsInHistory() {
        workerProfilePage.resetAndLoadDefaultWorkerList(3);
        WorkerPenaltyApplyResult appliedResult = null;

        // Thử lần lượt 3 user đầu để tránh pass giả khi một user đang bị chặn tạo xử phạt trùng.
        for (int userIndex = 0; userIndex < 3 && appliedResult == null; userIndex++) {
            String workerRow = workerProfilePage.workerRowTextAt(userIndex);
            System.out.println("[WORKER VIOLATION APPLY] USER: " + workerRow.replaceAll("\\R", " | "));
            workerProfilePage.openWorkerInformationAt(userIndex);

            // Chỉ tạo dữ liệu trên thợ chưa từng bị xử phạt; nếu đã có lịch sử thì chuyển thợ kế tiếp.
            workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
            Assert.assertTrue(workerProfilePage.hasWorkerViolationHistorySection(),
                    "Khong mo duoc Lich su vi pham truoc khi Ap dung.");
            if (workerProfilePage.hasWorkerViolationRecords()) {
                System.out.println("[WORKER VIOLATION APPLY] Tho da co lich su xu phat; chuyen user ke tiep.");
                workerProfilePage.closeWorkerDetail();
                continue;
            }

            workerProfilePage.openWorkerDetailTab("Đơn dịch vụ");

            if (!workerProfilePage.hasWorkerServiceOrderTable()
                    || workerProfilePage.workerServiceOrderIds().isEmpty()) {
                workerProfilePage.closeWorkerDetail();
                continue;
            }
            String orderId = workerProfilePage.workerServiceOrderIds().get(0);
            workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");

            String uniqueTitle = "AUTOMATION PENALTY " + System.currentTimeMillis();
            System.out.println("[WORKER VIOLATION APPLY] Ap dung cho ma don " + orderId + ".");
            WorkerPenaltyApplyResult result = workerProfilePage.applyWorkerPenalty(
                    orderId,
                    uniqueTitle,
                    "Du lieu xu phat duoc tao boi testcase automation tren sandbox.",
                    TEST_AMOUNT,
                    TEST_BLOCKING_DAYS,
                    POPUP_VIEW_DURATION);

            if (result.submissionPerformed()) {
                appliedResult = result;
            } else {
                workerProfilePage.closeWorkerDetail();
            }
        }

        Assert.assertNotNull(appliedResult,
                "Ca 3 user dau deu khong co don hop le hoac dang bi chan tao xu phat.");
        Assert.assertTrue(appliedResult.dialogClosed(), "Popup khong dong sau khi bam Ap dung.");
        Assert.assertTrue(appliedResult.violationHistoryChanged(),
                "Lich su vi pham khong thay doi sau khi Ap dung.");
        Assert.assertTrue(appliedResult.newViolationDisplayed(),
                "Khong tim thay ma don/tieu de xu phat moi trong Lich su vi pham.");
        System.out.println("[WORKER VIOLATION APPLY] Da tao va doi chieu xu phat cho don "
                + appliedResult.orderId() + ".");
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }
}
