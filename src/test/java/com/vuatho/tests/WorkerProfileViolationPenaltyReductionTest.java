package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerProfilePage.WorkerPenaltyApplyResult;
import com.vuatho.pages.WorkerProfilePage.WorkerPenaltyReductionResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/** Kiểm tra thao tác giảm số ngày của một lệnh phạt đang hoạt động. */
public class WorkerProfileViolationPenaltyReductionTest extends WorkerProfileTestSupport {
    private static final String TEST_AMOUNT = "1000";
    private static final String TEST_BLOCKING_DAYS = "2";
    private static final String TEST_REDUCTION_DAYS = "1";
    private static final int MAX_WORKER_PAGES_TO_SCAN = Integer.getInteger("workerPenalty.maxPages", 5);
    private static final Duration POPUP_VIEW_DURATION = Duration.ofSeconds(6);
    private static final Duration FINAL_VIEW_DURATION = Duration.ofSeconds(5);

    /** Cho phép chạy trực tiếp testcase giảm phạt từ IDE. */
    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfileViolationPenaltyReductionTest.class,
                "Bo test giam phat trong ho so tho ERP",
                "Tim lenh phat phu hop, giam phat va kiem tra so ngay con lai");
    }

    @Test(priority = 1, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-REDUCE-001: Giam phat va kiem tra so ngay con lai")
    public void activePenaltyCanBeReducedAndRemainingDaysAreUpdated() {
        int remainingDaysBefore = -1;
        int reductionDays = Integer.parseInt(TEST_REDUCTION_DAYS);
        workerProfilePage.resetAndLoadDefaultWorkerList(1);

        for (int pageIndex = 0;
                pageIndex < MAX_WORKER_PAGES_TO_SCAN && remainingDaysBefore <= reductionDays;
                pageIndex++) {
            int currentPage = workerProfilePage.currentWorkerPageNumber();
            int visibleWorkerCount = workerProfilePage.visibleWorkerCount();

            for (int userIndex = 0; userIndex < visibleWorkerCount; userIndex++) {
                String workerRow = workerProfilePage.workerRowTextAt(userIndex);
                int violationCount = workerProfilePage.workerRowViolationCountAt(userIndex);
                System.out.println("[WORKER PENALTY REDUCE] Thu tho trang " + currentPage
                        + ", dong " + (userIndex + 1) + ": " + workerRow.replaceAll("\\R", " | "));

                workerProfilePage.openWorkerInformationAt(userIndex);
                if (violationCount == 0) {
                    workerProfilePage.openWorkerDetailTab("Đơn dịch vụ");
                    if (!workerProfilePage.hasWorkerServiceOrderTable()
                            || workerProfilePage.workerServiceOrderIds().isEmpty()) {
                        System.out.println("[WORKER PENALTY REDUCE] Tho khong co don dich vu; bo qua.");
                        workerProfilePage.closeWorkerDetail();
                        continue;
                    }

                    String orderId = workerProfilePage.workerServiceOrderIds().get(0);
                    workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
                    WorkerPenaltyApplyResult setupResult = workerProfilePage.applyWorkerPenalty(
                            orderId,
                            "AUTOMATION PENALTY REDUCE SETUP " + System.currentTimeMillis(),
                            "Du lieu xu phat duoc tao boi testcase automation tren sandbox.",
                            TEST_AMOUNT,
                            TEST_BLOCKING_DAYS,
                            Duration.ofSeconds(1));
                    if (setupResult.submissionPerformed()) {
                        remainingDaysBefore = Integer.parseInt(TEST_BLOCKING_DAYS);
                        System.out.println("[WORKER PENALTY REDUCE] Da tao phat setup cho don "
                                + setupResult.orderId() + " de giam that.");
                        break;
                    }
                    remainingDaysBefore = workerProfilePage.activeWorkerPenaltyReductionRemainingDays();
                    System.out.println("[WORKER PENALTY REDUCE] Setup bi chan, kiem tra active hien tai: "
                            + remainingDaysBefore);
                    if (remainingDaysBefore > reductionDays) {
                        break;
                    }
                    System.out.println("[WORKER PENALTY REDUCE] Khong tao duoc phat setup; bo qua.");
                    workerProfilePage.closeWorkerDetail();
                    continue;
                }

                workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
                remainingDaysBefore = workerProfilePage.activeWorkerPenaltyReductionRemainingDays();
                System.out.println("[WORKER PENALTY REDUCE] So ngay con lai trong popup: "
                        + remainingDaysBefore);

                if (remainingDaysBefore > reductionDays) {
                    break;
                }
                workerProfilePage.closeWorkerDetail();
            }

            if (remainingDaysBefore > reductionDays || workerProfilePage.nextWorkerPageIsDisabled()) {
                break;
            }
            workerProfilePage.openNextWorkerPage();
        }

        Assert.assertTrue(remainingDaysBefore > reductionDays,
                "Khong tim thay tho dang bi phat va con ngay lon hon " + reductionDays
                        + " trong " + MAX_WORKER_PAGES_TO_SCAN + " trang dau.");

        WorkerPenaltyReductionResult result = workerProfilePage.reduceActiveWorkerPenalty(
                TEST_REDUCTION_DAYS,
                remainingDaysBefore - reductionDays,
                POPUP_VIEW_DURATION);

        Assert.assertTrue(result.statusDialogDisplayed(),
                "Popup Trang thai khong hien thi lua chon Giam phat.");
        Assert.assertTrue(result.reductionDialogDisplayed(),
                "Popup Giam phat khong hien thi input va nut Ap dung.");
        Assert.assertTrue(result.reductionDataEntered(), "Khong nhap duoc so ngay can giam.");
        Assert.assertTrue(result.submissionPerformed(), "Chua thuc hien thao tac Ap dung giam phat.");
        Assert.assertTrue(result.dialogClosed(), "Popup Giam phat khong dong sau khi Ap dung.");
        Assert.assertTrue(result.remainingDaysUpdated(),
                "Thoi han con lai khong giam dung 1 ngay sau khi Ap dung.");
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }
}
