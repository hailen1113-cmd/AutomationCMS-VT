package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerProfilePage.WorkerPenaltyApplyResult;
import com.vuatho.pages.WorkerProfilePage.WorkerPenaltyRemovalDialogResult;
import com.vuatho.pages.WorkerProfilePage.WorkerPenaltyRemovalResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/** Kiểm tra thao tác gỡ một lệnh phạt đang hoạt động trong hồ sơ thợ. */
public class WorkerProfileViolationPenaltyRemovalTest extends WorkerProfileTestSupport {
    private static final String TEST_AMOUNT = "1000";
    private static final String TEST_BLOCKING_DAYS = "2";
    private static final int MAX_WORKER_PAGES_TO_SCAN = Integer.getInteger("workerPenalty.maxPages", 5);
    private static final Duration SETUP_VIEW_DURATION = Duration.ofSeconds(1);
    private static final Duration ACTION_VIEW_DURATION = Duration.ofSeconds(
            Integer.getInteger("workerPenalty.stepViewSeconds", 6));
    private static final Duration FINAL_VIEW_DURATION = Duration.ofSeconds(5);
    private static int selectedWorkerPage = 1;
    private static int selectedWorkerIndex = -1;
    private static String selectedWorkerId = "";

    /** Cho phép chạy trực tiếp testcase gỡ phạt từ IDE. */
    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfileViolationPenaltyRemovalTest.class,
                "Bo test go phat trong ho so tho ERP",
                "Tim lenh phat phu hop, go phat va kiem tra lich su");
    }

    @Test(priority = 1, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-REMOVE-001: Kiem tra va huy popup Go phat")
    public void removalDialogCanBeCancelledAndClosedWithoutChangingPenalty() {
        Assert.assertTrue(openWorkerWithActivePenalty(),
                "Khong tim thay hoac tao duoc lenh phat dang hoat dong de kiem tra popup.");

        WorkerPenaltyRemovalDialogResult result =
                workerProfilePage.inspectActiveWorkerPenaltyRemovalDialogAndCancel(ACTION_VIEW_DURATION);

        Assert.assertTrue(result.statusDialogDisplayed(), "Popup Trang thai khong hien thi.");
        Assert.assertTrue(result.removalDialogDisplayed(), "Popup Go phat khong hien thi.");
        Assert.assertTrue(result.penaltySummaryDisplayed(), "Popup Go phat thieu Ma don hoac So tien.");
        Assert.assertTrue(result.paymentOptionsDisplayed(),
                "Popup Go phat thieu Ghi nhan da thu hoac Tru so du tho.");
        Assert.assertTrue(result.confirmationInitiallyAvailable(),
                "Popup Go phat khong hien thi nut xac nhan cho lua chon mac dinh.");
        Assert.assertTrue(result.cancelButtonWorks(), "Nut Huy bo khong dong popup dung.");
        Assert.assertTrue(result.escapeKeyWorks(), "Phim ESC khong dong popup Go phat.");
        Assert.assertTrue(result.activePenaltyPreserved(),
                "Lenh phat bi thay doi sau khi Huy bo hoac dong popup.");
        workerProfilePage.reloadWorkerList();
    }

    @Test(priority = 2, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-REMOVE-002: Go phat theo Ghi nhan da thu")
    public void activePenaltyCanBeRemovedAsAlreadyPaid() {
        Assert.assertTrue(openWorkerWithActivePenalty(),
                "Khong tim thay hoac tao duoc lenh phat dang hoat dong de go phat.");

        WorkerPenaltyRemovalResult result = workerProfilePage.removeActiveWorkerPenalty(
                "ghi nhan da thu", "chi phi", ACTION_VIEW_DURATION);
        assertSuccessfulRemoval(result);
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }

    @Test(priority = 3, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-REMOVE-003: Go phat theo Tru so du tho")
    public void activePenaltyCanBeRemovedByRefundingWorkerBalance() {
        Assert.assertTrue(openWorkerWithActivePenalty(),
                "Khong tim thay hoac tao duoc lenh phat dang hoat dong de tru so du.");

        WorkerPenaltyRemovalResult result = workerProfilePage.removeActiveWorkerPenalty(
                "tru so du tho", "chi phi", ACTION_VIEW_DURATION);
        Assert.assertTrue(result.balanceSourcesDisplayed(),
                "Nhanh Tru so du tho khong hien thi nguon Chi phi va Ky quy.");
        Assert.assertTrue(result.requestedBalanceSourceSelected(),
                "Popup chua chon dung nguon Chi phi.");
        assertSuccessfulRemoval(result);
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }

    @Test(priority = 4, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-REMOVE-004: Go phat va tru nguon Ky quy")
    public void activePenaltyCanBeRemovedByRefundingDepositBalance() {
        Assert.assertTrue(openWorkerWithActivePenalty(),
                "Khong tim thay hoac tao duoc lenh phat dang hoat dong de tru ky quy.");

        WorkerPenaltyRemovalResult result = workerProfilePage.removeActiveWorkerPenalty(
                "tru so du tho", "ky quy", ACTION_VIEW_DURATION);
        Assert.assertTrue(result.balanceSourcesDisplayed(),
                "Nhanh Tru so du tho khong hien thi nguon Chi phi va Ky quy.");
        Assert.assertTrue(result.requestedBalanceSourceSelected(),
                "Popup chua chon dung nguon Ky quy.");
        assertSuccessfulRemoval(result);
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }

    @Test(priority = 5, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-REMOVE-005: Khong hien Go phat khi khong co lenh active")
    public void workerWithoutActivePenaltyDoesNotShowRemovalAction() {
        if (openSelectedWorkerViolationTab()) {
            Assert.assertTrue(workerProfilePage.activeWorkerPenaltyRemainingDays() < 0,
                    "Tho da chon van con lenh phat active sau cac case go phat.");
            Assert.assertFalse(workerProfilePage.hasActiveWorkerPenaltyStatusAction(),
                    "Tho da chon khong co lenh active nhung van hien nut Trang thai de Go phat.");
            return;
        }

        boolean workerWithoutActivePenaltyFound = false;
        workerProfilePage.resetAndLoadDefaultWorkerList(1);

        for (int pageIndex = 0;
                pageIndex < MAX_WORKER_PAGES_TO_SCAN && !workerWithoutActivePenaltyFound;
                pageIndex++) {
            int visibleWorkerCount = workerProfilePage.visibleWorkerCount();
            for (int userIndex = 0; userIndex < visibleWorkerCount; userIndex++) {
                workerProfilePage.openWorkerInformationAt(userIndex);
                workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
                if (workerProfilePage.activeWorkerPenaltyRemainingDays() < 0) {
                    workerWithoutActivePenaltyFound = true;
                    Assert.assertFalse(workerProfilePage.hasActiveWorkerPenaltyStatusAction(),
                            "Tho khong co lenh phat active nhung van hien nut Trang thai de Go phat.");
                    break;
                }
                workerProfilePage.closeWorkerDetail();
            }
            if (workerWithoutActivePenaltyFound || workerProfilePage.nextWorkerPageIsDisabled()) {
                break;
            }
            workerProfilePage.openNextWorkerPage();
        }

        Assert.assertTrue(workerWithoutActivePenaltyFound,
                "Khong tim thay tho khong co lenh phat active trong "
                        + MAX_WORKER_PAGES_TO_SCAN + " trang dau.");
    }

    /** Tìm một thợ có lệnh phạt đang hoạt động; tự tạo setup từ đơn thật nếu cần. */
    private boolean openWorkerWithActivePenalty() {
        boolean activePenaltyFound = false;
        workerProfilePage.resetAndLoadDefaultWorkerList(1);

        if (openSelectedWorkerViolationTabWithoutReset()) {
            int remainingDays = workerProfilePage.activeWorkerPenaltyRemainingDays();
            if (remainingDays >= 0) {
                System.out.println("[WORKER PENALTY REMOVE] Tai su dung tho " + selectedWorkerId
                        + ", con " + remainingDays + " ngay.");
                return true;
            }

            System.out.println("[WORKER PENALTY REMOVE] Tho " + selectedWorkerId
                    + " da het phat; tao lai du lieu setup tren cung tho.");
            boolean setupCreated = createPenaltySetupForCurrentWorker();
            if (!setupCreated) {
                workerProfilePage.closeWorkerDetail();
            }
            return setupCreated;
        }

        for (int pageIndex = 0; pageIndex < MAX_WORKER_PAGES_TO_SCAN && !activePenaltyFound; pageIndex++) {
            int currentPage = pageIndex + 1;
            int visibleWorkerCount = workerProfilePage.visibleWorkerCount();

            for (int userIndex = 0; userIndex < visibleWorkerCount; userIndex++) {
                String workerRow = workerProfilePage.workerRowTextAt(userIndex);
                int violationCount = workerProfilePage.workerRowViolationCountAt(userIndex);
                System.out.println("[WORKER PENALTY REMOVE] Thu tho trang " + currentPage
                        + ", dong " + (userIndex + 1) + ": " + workerRow.replaceAll("\\R", " | "));

                workerProfilePage.openWorkerInformationAt(userIndex);
                workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
                int remainingDays = workerProfilePage.activeWorkerPenaltyRemainingDays();
                if (remainingDays >= 0) {
                    activePenaltyFound = true;
                    rememberSelectedWorker(currentPage, userIndex, workerRow);
                    System.out.println("[WORKER PENALTY REMOVE] Tim thay lenh phat dang hoat dong, con "
                            + remainingDays + " ngay.");
                    break;
                }

                if (violationCount == 0) {
                    activePenaltyFound = createPenaltySetupForCurrentWorker();
                    if (activePenaltyFound) {
                        rememberSelectedWorker(currentPage, userIndex, workerRow);
                        break;
                    }
                }

                workerProfilePage.closeWorkerDetail();
            }

            if (activePenaltyFound || workerProfilePage.nextWorkerPageIsDisabled()) {
                break;
            }
            workerProfilePage.openNextWorkerPage();
        }

        Assert.assertTrue(activePenaltyFound,
                "Khong tim thay hoac tao duoc lenh phat dang hoat dong trong "
                        + MAX_WORKER_PAGES_TO_SCAN + " trang dau.");
        return activePenaltyFound;
    }

    /** Mở lại đúng thợ đã được chọn từ testcase đầu và chuyển tới tab xử lý vi phạm. */
    private boolean openSelectedWorkerViolationTab() {
        workerProfilePage.resetAndLoadDefaultWorkerList(1);
        return openSelectedWorkerViolationTabWithoutReset();
    }

    /** Mở thợ đã chọn khi danh sách mặc định đã được tải. */
    private boolean openSelectedWorkerViolationTabWithoutReset() {
        if (selectedWorkerIndex < 0 || selectedWorkerId.isBlank()) {
            return false;
        }
        if (selectedWorkerPage > 1) {
            workerProfilePage.openWorkerPage(selectedWorkerPage);
        }

        int matchingIndex = selectedWorkerIndex;
        if (matchingIndex >= workerProfilePage.visibleWorkerCount()
                || !workerIdAt(matchingIndex).equals(selectedWorkerId)) {
            matchingIndex = -1;
            for (int index = 0; index < workerProfilePage.visibleWorkerCount(); index++) {
                if (workerIdAt(index).equals(selectedWorkerId)) {
                    matchingIndex = index;
                    break;
                }
            }
        }
        if (matchingIndex < 0) {
            throw new AssertionError("Khong tim lai duoc tho da chon: " + selectedWorkerId);
        }

        selectedWorkerIndex = matchingIndex;
        System.out.println("[WORKER PENALTY REMOVE] Mo lai cung tho " + selectedWorkerId
                + " tai trang " + selectedWorkerPage + ", dong " + (matchingIndex + 1) + ".");
        workerProfilePage.openWorkerInformationAt(matchingIndex);
        workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
        return true;
    }

    /** Lưu định danh thợ phù hợp để toàn bộ class chỉ thao tác trên một người dùng. */
    private void rememberSelectedWorker(int pageNumber, int rowIndex, String workerRow) {
        selectedWorkerPage = pageNumber;
        selectedWorkerIndex = rowIndex;
        selectedWorkerId = workerRow.split("\\R", 2)[0].trim();
        System.out.println("[WORKER PENALTY REMOVE] Da chon tho dung chung: " + selectedWorkerId + ".");
    }

    /** Đọc ID ổn định ở dòng đầu của một hàng thợ. */
    private String workerIdAt(int rowIndex) {
        return workerProfilePage.workerRowTextAt(rowIndex).split("\\R", 2)[0].trim();
    }

    /** Đối chiếu kết quả chung cho cả hai cách xử lý tiền khi gỡ phạt. */
    private void assertSuccessfulRemoval(WorkerPenaltyRemovalResult result) {
        Assert.assertTrue(result.statusDialogDisplayed(), "Popup Trang thai khong hien thi.");
        Assert.assertTrue(result.removalOptionDisplayed(), "Popup Trang thai khong co lua chon Go phat.");
        Assert.assertTrue(result.removalDialogDisplayed(), "Popup xac nhan Go phat khong hien thi.");
        Assert.assertTrue(result.paymentOptionsDisplayed(),
                "Popup Go phat thieu lua chon Ghi nhan da thu hoac Tru so du tho.");
        Assert.assertTrue(result.requestedPaymentOptionSelected(),
                "Popup chua chon dung cach xu ly tien phat.");
        Assert.assertTrue(result.confirmationPerformed(), "Chua bam Xac nhan go phat.");
        Assert.assertTrue(result.removalPerformed(), "Chua thuc hien Go phat thanh cong.");
        Assert.assertTrue(result.statusDialogClosed(), "Popup Trang thai khong dong sau khi Go phat.");
        Assert.assertTrue(result.activePenaltyRemoved(),
                "Tho van hien thi Dang trong thoi gian xu phat sau khi Go phat.");
        Assert.assertTrue(result.removalShownInHistory(),
                "Lich su vi pham chua hien thi trang thai Go phat.");
    }

    /** Tạo lệnh phạt setup từ đơn dịch vụ thật của thợ hiện tại khi chưa có lệnh đang hoạt động. */
    private boolean createPenaltySetupForCurrentWorker() {
        workerProfilePage.openWorkerDetailTab("Đơn dịch vụ");
        if (!workerProfilePage.hasWorkerServiceOrderTable()
                || workerProfilePage.workerServiceOrderIds().isEmpty()) {
            System.out.println("[WORKER PENALTY REMOVE] Tho khong co don dich vu; bo qua.");
            return false;
        }

        String orderId = workerProfilePage.workerServiceOrderIds().get(0);
        workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
        WorkerPenaltyApplyResult setupResult = workerProfilePage.applyWorkerPenalty(
                orderId,
                "AUTOMATION PENALTY REMOVE SETUP " + System.currentTimeMillis(),
                "Du lieu xu phat setup de testcase automation thuc hien go phat.",
                TEST_AMOUNT,
                TEST_BLOCKING_DAYS,
                SETUP_VIEW_DURATION);

        if (setupResult.submissionPerformed()) {
            System.out.println("[WORKER PENALTY REMOVE] Da tao phat setup cho don "
                    + setupResult.orderId() + ".");
            return true;
        }

        int remainingDays = workerProfilePage.activeWorkerPenaltyRemainingDays();
        if (remainingDays >= 0) {
            System.out.println("[WORKER PENALTY REMOVE] Setup bi chan nhung da co lenh phat dang hoat dong.");
            return true;
        }
        System.out.println("[WORKER PENALTY REMOVE] Khong tao duoc phat setup; bo qua.");
        return false;
    }
}
