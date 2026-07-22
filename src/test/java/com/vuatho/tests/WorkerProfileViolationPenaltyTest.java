package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerProfilePage.WorkerConnectionPenaltyApplyResult;
import com.vuatho.pages.WorkerProfilePage.WorkerConnectionPenaltyDialogResult;
import com.vuatho.pages.WorkerProfilePage.WorkerPenaltyDialogResult;
import com.vuatho.pages.WorkerProfilePage.WorkerPenaltyApplyResult;
import com.vuatho.pages.WorkerProfilePage.WorkerPenaltyInvalidInputResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Kiểm tra popup Thiết lập xử phạt trong tab Xử lý vi phạm của hồ sơ thợ.
 */
public class WorkerProfileViolationPenaltyTest extends WorkerProfileTestSupport {
    private static final int MAX_WORKER_PAGES_TO_SCAN = Integer.getInteger("workerPenalty.maxPages", 2);
    private static final String TEST_PENALTY_TITLE = "AUTOMATION TEST - KHONG AP DUNG";
    private static final String TEST_REASON = "Du lieu kiem thu popup xu phat, testcase se bam Huy bo.";
    private static final String APPLIED_REASON =
            "Du lieu xu phat duoc tao boi testcase automation tren sandbox.";
    private static final String TEST_AMOUNT = "1000";
    private static final String TEST_BLOCKING_DAYS = "2";
    private static final Duration POPUP_VIEW_DURATION = Duration.ofSeconds(
            Integer.getInteger("workerPenalty.stepViewSeconds", 1));
    private static final Duration FINAL_VIEW_DURATION = Duration.ofSeconds(
            Integer.getInteger("workerPenalty.finalViewSeconds", 1));
    private static int selectedWorkerPage = 1;
    private static int selectedWorkerIndex = -1;
    private static String selectedWorkerId = "";
    private static String selectedOrderId = "";
    private static String createdPenaltyTitle = "";

    @Override
    protected boolean preserveWorkerProfileStateBetweenMethods() {
        return true;
    }

    /** Cho phép chạy trực tiếp testcase từ IDE. */
    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfileViolationPenaltyTest.class,
                "Bo test xu ly vi pham trong ho so tho ERP",
                "Mo va kiem tra popup thiet lap xu phat");
    }

    /**
     * Kiểm tra popup xử phạt của một user và hủy mà không tạo dữ liệu thật.
     */
    @Test(priority = 1, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-CANCEL-001: Kiem tra va huy popup xu phat tren 1 user")
    public void penaltyDialogShowsExpectedControlsAndCanBeCancelled() {
        Assert.assertTrue(openWorkerWithServiceOrderForPenalty(),
                "Khong tim thay tho khong co lenh active va co don chua dung de xu phat.");

        System.out.println("[WORKER VIOLATION] CLICK Xu phat mot lan tren user da chon.");
        WorkerPenaltyDialogResult result = workerProfilePage.fillWorkerPenaltyDialogAndCancel(
                selectedOrderId,
                TEST_PENALTY_TITLE,
                TEST_REASON,
                TEST_AMOUNT,
                TEST_BLOCKING_DAYS,
                true,
                POPUP_VIEW_DURATION);

        Assert.assertTrue(result.requiredFieldsPresent(), "Popup thieu field/button bat buoc.");
        Assert.assertTrue(result.initialStateValid(), "Popup khong rong khi moi mo.");
        Assert.assertTrue(result.emptySubmissionBlocked(), "Form rong khong bi validation chan.");
        Assert.assertTrue(result.eachRequiredFieldBlocked(),
                "Co field bat buoc bi bo trong nhung form van duoc chap nhan.");
        Assert.assertTrue(result.nonNumericValuesBlocked(),
                "So tien hoac thoi han dang chu khong bi validation chan.");
        Assert.assertTrue(result.testDataEntered(), "Popup chua nhan du du lieu mau.");
        Assert.assertTrue(result.permanentDisablesBlockingDays(),
                "Bat Vinh vien khong khoa So ngay.");
        Assert.assertTrue(result.permanentOffEnablesBlockingDays(),
                "Tat Vinh vien khong mo lai So ngay.");
        Assert.assertTrue(result.restrictionOptionsMutuallyExclusive(),
                "Hai pham vi han che khong loai tru nhau.");
        Assert.assertTrue(result.cancelledWithoutCreatingViolation(), "Popup khong Huy bo dung.");
        Assert.assertTrue(result.cancelledFormReset(),
                "Du lieu cu van con khi mo lai popup sau khi Huy bo.");
        Assert.assertTrue(result.topCloseButtonWorks(), "Nut X cua popup khong dong dung.");

        System.out.println("[WORKER VIOLATION] Da kiem tra mot user va Huy bo, khong tao xu phat.");
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }

    /**
     * Tìm động một thợ có đơn phù hợp, áp dụng xử phạt và kiểm tra bản ghi lịch sử mới.
     */
    @Test(priority = 2, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-VALIDATE-002: Chan du lieu xu phat khong hop le")
    public void invalidPenaltyValuesAreBlockedWithoutDirtyingFixture() {
        Assert.assertTrue(openSelectedWorkerViolationTab() || openWorkerWithServiceOrderForPenalty(),
                "Khong tim thay tho va don phu hop de kiem tra validation.");

        WorkerPenaltyInvalidInputResult result = workerProfilePage.validateWorkerPenaltyInvalidInputs(
                selectedOrderId,
                "999999999999",
                "AUTOMATION INVALID PENALTY",
                "Du lieu dung de kiem tra validation, khong duoc phep luu.",
                TEST_AMOUNT,
                TEST_BLOCKING_DAYS);

        Assert.assertTrue(result.fixtureRemainsUsable(),
                "Validation da tao lenh active va cleanup khong khoi phuc duoc fixture.");
        Assert.assertTrue(result.invalidOrderBlocked(), "Ma don khong ton tai van duoc chap nhan.");
        Assert.assertTrue(result.negativeAmountBlocked(), "So tien am van duoc chap nhan.");
        Assert.assertTrue(result.negativeDaysBlocked(), "So ngay am van duoc chap nhan.");
        Assert.assertTrue(result.whitespaceTitleBlocked(), "Tieu de chi co khoang trang van duoc chap nhan.");
        Assert.assertTrue(result.whitespaceReasonBlocked(), "Ly do chi co khoang trang van duoc chap nhan.");
        Assert.assertTrue(result.dialogCancelled(), "Popup validation khong dong dung sau khi Huy bo.");
    }

    @Test(priority = 3, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-APPLY-003: Ap dung xu phat va kiem tra lich su")
    public void validPenaltyCanBeAppliedAndAppearsInHistory() {
        Assert.assertTrue(openSelectedWorkerViolationTab() || openWorkerWithServiceOrderForPenalty(),
                "Khong tim thay tho khong co lenh active va co don chua dung de xu phat.");

        String uniqueTitle = "AUTOMATION PENALTY " + System.currentTimeMillis();
        createdPenaltyTitle = uniqueTitle;
        System.out.println("[WORKER VIOLATION APPLY] Ap dung mot lan cho ma don "
                + selectedOrderId + ".");
        WorkerPenaltyApplyResult appliedResult = workerProfilePage.applyWorkerPenalty(
                selectedOrderId,
                uniqueTitle,
                APPLIED_REASON,
                TEST_AMOUNT,
                TEST_BLOCKING_DAYS,
                POPUP_VIEW_DURATION);

        Assert.assertTrue(appliedResult.submissionPerformed(),
                "He thong khong thuc hien Ap dung xu phat.");
        Assert.assertTrue(appliedResult.dialogClosed(), "Popup khong dong sau khi bam Ap dung.");
        Assert.assertTrue(appliedResult.violationHistoryChanged(),
                "Lich su vi pham khong thay doi sau khi Ap dung.");
        Assert.assertTrue(appliedResult.newViolationDisplayed(),
                "Khong tim thay ma don/tieu de xu phat moi trong Lich su vi pham.");
        Assert.assertTrue(appliedResult.singleHistoryRecordDisplayed(),
                "Mot lan Ap dung da tao trung nhieu ban ghi co cung tieu de.");
        Assert.assertTrue(appliedResult.activePenaltyDisplayed(),
                "Khong thay the lenh phat dang hoat dong sau khi Ap dung.");
        Assert.assertTrue(appliedResult.savedDetailsDisplayed(),
                "Du lieu tren the active hoac Lich su vi pham khong khop form da Ap dung.");
        System.out.println("[WORKER VIOLATION APPLY] Da tao va doi chieu xu phat cho don "
                + appliedResult.orderId() + ".");
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }

    @Test(priority = 4, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-PERSIST-004: Kiem tra luu ben va chan phat trung")
    public void activePenaltyPersistsAndBlocksDuplicateCreation() {
        // Đóng drawer và tải lại danh sách để kiểm tra dữ liệu từ backend, không chỉ trạng thái DOM cũ.
        workerProfilePage.closeWorkerDetailIfOpen();
        Assert.assertTrue(openSelectedWorkerViolationTab() || openWorkerWithActivePenalty(),
                "Khong mo lai duoc tho co lenh phat dang hoat dong.");
        Assert.assertTrue(workerProfilePage.activeWorkerPenaltyRemainingDays() >= 0,
                "Lenh phat active khong con sau khi mo lai ho so.");
        Assert.assertFalse(workerProfilePage.workerPenaltyCreationIsAvailable(),
                "Tho dang bi phat van co the mo form tao them lenh phat.");
        System.out.println("[WORKER VIOLATION HISTORY SNAPSHOT] "
                + workerProfilePage.workerViolationHistorySnapshot().replaceAll("\\R", " | "));

        if (!createdPenaltyTitle.isBlank()) {
            Assert.assertTrue(workerProfilePage.activeWorkerPenaltyContains(
                            selectedOrderId, createdPenaltyTitle, APPLIED_REASON, TEST_AMOUNT, "Chặn tìm việc"),
                    "The lenh phat sau khi mo lai khong khop du lieu da tao.");
            Assert.assertTrue(workerProfilePage.workerViolationHistoryContainsAll(
                            selectedOrderId, createdPenaltyTitle, APPLIED_REASON, TEST_AMOUNT, "Chặn tìm việc"),
                    "Lich su sau khi mo lai khong khop du lieu da tao.");
        }
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }

    @Test(priority = 5, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-FEE-005: Kiem tra popup Phi phat ket noi")
    public void connectionPenaltyDialogSupportsPresetsValidationAndCancel() {
        Assert.assertTrue(openWorkerWithActivePenalty(),
                "Khong tim thay tho co lenh phat dang hoat dong de kiem tra Muc phat.");

        WorkerConnectionPenaltyDialogResult result =
                workerProfilePage.inspectWorkerConnectionPenaltyDialogAndCancel(POPUP_VIEW_DURATION);

        Assert.assertTrue(result.dialogDisplayed(), "Popup Phi phat ket noi khong hien thi.");
        Assert.assertTrue(result.explanationDisplayed(),
                "Popup thieu thong tin phi mac dinh 15% hoac muc toi thieu 16%.");
        Assert.assertTrue(result.requiredFieldsDisplayed(),
                "Popup thieu field Phan tram, Thoi han hoac button bat buoc.");
        Assert.assertTrue(result.presetOptionsDisplayed(),
                "Popup thieu preset 18%, 20%, 23% hoac 25%.");
        Assert.assertTrue(result.initialStateValid(),
                "Gia tri ban dau cua phan tram hoac thoi han khong hop le.");
        Assert.assertTrue(result.emptySubmissionBlocked(), "Form rong khong bi validation chan.");
        Assert.assertTrue(result.presetOptionsSelectable(), "Co preset phan tram khong chon duoc.");
        Assert.assertTrue(result.belowMinimumBlocked(), "Muc 15% khong bi validation toi thieu chan.");
        Assert.assertTrue(result.nonNumericPercentageBlocked(),
                "Phan tram dang chu khong bi validation chan.");
        Assert.assertTrue(result.invalidDurationBlocked(),
                "Thoi han bang 0 hoac dang chu khong bi validation chan.");
        Assert.assertTrue(result.cancelledValuesNotPersisted(),
                "Bam Bo qua nhung gia tri Phi phat ket noi vua sua van bi luu.");
        Assert.assertTrue(result.cancelledWithoutChangingPenalty(),
                "Nut Bo qua khong dong popup hoac lam thay doi lenh phat.");
    }

    @Test(priority = 6, groups = { "partner-worker", "worker-profile", "worker-violation" },
            description = "WORKER-PROFILE-VIOLATION-FEE-006: Ap dung that Phi phat ket noi")
    public void validConnectionPenaltyCanBeAppliedAndPersisted() {
        Assert.assertTrue(openWorkerWithActivePenalty(),
                "Khong tim thay tho co lenh phat dang hoat dong de Ap dung Muc phat.");

        WorkerConnectionPenaltyApplyResult result = workerProfilePage.applyWorkerConnectionPenalty(
                "16", "1", POPUP_VIEW_DURATION);

        Assert.assertTrue(result.dialogDisplayed(), "Popup Phi phat ket noi khong hien thi.");
        Assert.assertTrue(result.requestedValuesEntered(), "Chua nhap du 16% va thoi han 1 ngay.");
        Assert.assertTrue(result.submissionPerformed(), "Chua bam Ap dung Phi phat ket noi.");
        Assert.assertTrue(result.dialogClosed(), "Popup khong dong sau khi bam Ap dung.");
        Assert.assertTrue(result.configuredValuesPersisted(),
                "Mo lai popup khong thay muc toi thieu 16% va thoi han 1 ngay da luu.");
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }

    /** Tìm một lần thợ không có lệnh active và có đơn chưa dùng để tạo phạt. */
    private boolean openWorkerWithServiceOrderForPenalty() {
        workerProfilePage.resetAndLoadDefaultWorkerList(1);
        for (int pageIndex = 0; pageIndex < MAX_WORKER_PAGES_TO_SCAN; pageIndex++) {
            int currentPage = pageIndex + 1;
            java.util.List<String> workerRows = workerProfilePage.visibleWorkerRowTexts();
            for (int userIndex : prioritizedWorkerIndices(workerRows)) {
                // Ưu tiên dòng có đơn hoàn thành; vẫn fallback sang các dòng khác nếu cần.
                String workerRow = workerRows.get(userIndex);
                System.out.println("[WORKER VIOLATION FIXTURE] Thu tho "
                        + workerIdFromRow(workerRow) + ", so don hoan thanh="
                        + completedServiceOrderCountFromWorkerRow(workerRow) + ".");
                workerProfilePage.openWorkerInformationFromSnapshotAt(userIndex);
                workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
                if (!workerProfilePage.hasWorkerViolationHistorySection()
                        || workerProfilePage.activeWorkerPenaltyRemainingDays() >= 0) {
                    workerProfilePage.closeWorkerDetail();
                    continue;
                }

                workerProfilePage.openWorkerDetailTab("Đơn dịch vụ");
                if (!workerProfilePage.hasWorkerServiceOrderTable()
                        || workerProfilePage.workerServiceOrderIds().isEmpty()) {
                    workerProfilePage.closeWorkerDetail();
                    continue;
                }

                java.util.List<String> orderIds = workerProfilePage.workerServiceOrderIds();
                workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
                selectedOrderId = orderIds.stream()
                        .filter(orderId -> !workerProfilePage.workerViolationHistoryContains(orderId))
                        .findFirst()
                        .orElse("");
                if (selectedOrderId.isBlank()) {
                    workerProfilePage.closeWorkerDetail();
                    continue;
                }
                if (!workerProfilePage.workerPenaltyCreationIsAvailable()) {
                    selectedOrderId = "";
                    workerProfilePage.closeWorkerDetail();
                    continue;
                }

                rememberSelectedWorker(currentPage, userIndex, workerRow);
                System.out.println("[WORKER VIOLATION] Tim thay tho " + selectedWorkerId
                        + " va ma don " + selectedOrderId + "; dung chung cho flow phat.");
                return true;
            }
            if (workerProfilePage.nextWorkerPageIsDisabled()) {
                break;
            }
            workerProfilePage.openNextWorkerPage();
        }
        return false;
    }

    /** Tái sử dụng đúng một thợ có lệnh phạt active; nếu chạy riêng thì tự tìm động. */
    private boolean openWorkerWithActivePenalty() {
        if (workerProfilePage.workerDetailIsOpen()
                && workerProfilePage.activeWorkerPenaltyRemainingDays() >= 0) {
            return true;
        }
        workerProfilePage.resetAndLoadDefaultWorkerList(1);
        if (openSelectedWorkerViolationTabWithoutReset()
                && workerProfilePage.activeWorkerPenaltyRemainingDays() >= 0) {
            return true;
        }
        if (workerProfilePage.workerDetailIsOpen()) {
            workerProfilePage.closeWorkerDetail();
        }

        for (int pageIndex = 0; pageIndex < MAX_WORKER_PAGES_TO_SCAN; pageIndex++) {
            int currentPage = pageIndex + 1;
            java.util.List<String> workerRows = workerProfilePage.visibleWorkerRowTexts();
            for (int userIndex : prioritizedWorkerIndices(workerRows)) {
                String workerRow = workerRows.get(userIndex);
                workerProfilePage.openWorkerInformationFromSnapshotAt(userIndex);
                workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
                if (workerProfilePage.activeWorkerPenaltyRemainingDays() >= 0) {
                    rememberSelectedWorker(currentPage, userIndex, workerRow);
                    return true;
                }
                workerProfilePage.closeWorkerDetail();
            }
            if (workerProfilePage.nextWorkerPageIsDisabled()) {
                break;
            }
            workerProfilePage.openNextWorkerPage();
        }
        return false;
    }

    /** Tải lại danh sách rồi mở đúng thợ đã lưu. */
    private boolean openSelectedWorkerViolationTab() {
        if (workerProfilePage.workerDetailIsOpen()) {
            return true;
        }
        workerProfilePage.resetAndLoadDefaultWorkerList(1);
        return openSelectedWorkerViolationTabWithoutReset();
    }

    /** Mở lại đúng thợ đã lưu khi TestNG khởi tạo lại trang giữa hai test method. */
    private boolean openSelectedWorkerViolationTabWithoutReset() {
        if (selectedWorkerIndex < 0 || selectedWorkerId.isBlank()) {
            return false;
        }
        if (selectedWorkerPage > 1) {
            workerProfilePage.openWorkerPage(selectedWorkerPage);
        }
        java.util.List<String> workerRows = workerProfilePage.visibleWorkerRowTexts();
        int matchingIndex = selectedWorkerIndex;
        if (matchingIndex >= workerRows.size()
                || !workerIdFromRow(workerRows.get(matchingIndex)).equals(selectedWorkerId)) {
            matchingIndex = -1;
            for (int index = 0; index < workerRows.size(); index++) {
                if (workerIdFromRow(workerRows.get(index)).equals(selectedWorkerId)) {
                    matchingIndex = index;
                    break;
                }
            }
        }
        if (matchingIndex < 0) {
            return false;
        }
        selectedWorkerIndex = matchingIndex;
        workerProfilePage.openWorkerInformationFromSnapshotAt(matchingIndex);
        workerProfilePage.openWorkerDetailTab("Xử lý vi phạm");
        return true;
    }

    private void rememberSelectedWorker(int pageNumber, int rowIndex, String workerRow) {
        selectedWorkerPage = pageNumber;
        selectedWorkerIndex = rowIndex;
        selectedWorkerId = workerRow.split("\\R", 2)[0].trim();
        System.out.println("[WORKER VIOLATION] Da chon tho dung chung: " + selectedWorkerId
                + " tai trang " + pageNumber + ", dong " + (rowIndex + 1) + ".");
    }

    private String workerIdFromRow(String workerRow) {
        return workerRow.split("\\R", 2)[0].trim();
    }

    /** Chỉ mở thợ có đơn hoàn thành; dòng 0 đơn không thể cung cấp mã đơn hợp lệ để phạt. */
    private java.util.List<Integer> prioritizedWorkerIndices(java.util.List<String> workerRows) {
        java.util.List<Integer> indices = new java.util.ArrayList<>();
        for (int index = 0; index < workerRows.size(); index++) {
            if (completedServiceOrderCountFromWorkerRow(workerRows.get(index)) > 0) {
                indices.add(index);
            }
        }
        return indices;
    }

    /** Đọc cột Số đơn dịch vụ hoàn thành từ text row đã chụp. */
    private int completedServiceOrderCountFromWorkerRow(String workerRow) {
        String[] lines = workerRow.split("\\R");
        for (int index = 1; index < lines.length; index++) {
            if (lines[index].trim().matches("\\d{2}-\\d{2}-\\d{4}.*")
                    && lines[index - 1].trim().matches("\\d+")) {
                return Integer.parseInt(lines[index - 1].trim());
            }
        }
        return -1;
    }

}
