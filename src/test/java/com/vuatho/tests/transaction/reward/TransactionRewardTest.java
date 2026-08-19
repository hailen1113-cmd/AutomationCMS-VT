package com.vuatho.tests.transaction.reward;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionCategoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra chuyên biệt nhóm Thưởng và khuyến mãi trong Lịch sử giao dịch. */
public class TransactionRewardTest extends TransactionCategoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionRewardTest.class, "Lịch sử giao dịch", "Thưởng và khuyến mãi");
    }

    @Override
    protected TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.REWARD;
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_002)
    public void opensVoucherRefundRoute() {
        verifySubtypeRoute(category().subtypes().get(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_031)
    public void opensCampaignRefundRoute() {
        verifySubtypeRoute(category().subtypes().get(1));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_003)
    public void showsExpectedFiltersAndColumns() {
        verifyLayout();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_004)
    public void rowsHaveValidFormats() {
        verifyRowFormats();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_005)
    public void searchesUserAndRestoresRows() {
        verifySearchAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_006)
    public void filterOptionsKeepCurrentTab() {
        verifyFilterOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_007)
    public void sortsAmountBothDirections() {
        verifyAmountSort();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_008)
    public void opensAndClosesDetail() {
        verifyDetail();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_009)
    public void paginationAndResetKeepCurrentTab() {
        verifyPaginationAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_010)
    public void exportsCurrentVoucherRefund() {
        verifyExportForSubtype(12, this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_032)
    public void exportsCurrentCampaignRefund() {
        verifyExportForSubtype(18, this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_011)
    public void exportsVoucherRefundSearchResults() {
        verifyExportForSubtype(12, this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_033)
    public void exportsCampaignRefundSearchResults() {
        verifyExportForSubtype(18, this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_012)
    public void exportsVoucherRefundSuccessResults() {
        verifyExportForSubtype(12, this::verifyExportAfterStatus);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_034)
    public void exportsCampaignRefundSuccessResults() {
        verifyExportForSubtype(18, this::verifyExportAfterStatus);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_013)
    public void exportsVoucherRefundPaypalResults() {
        verifyExportForSubtype(12, this::verifyExportAfterGateway);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_035)
    public void exportsCampaignRefundPaypalResults() {
        verifyExportForSubtype(18, this::verifyExportAfterGateway);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_014)
    public void exportsVoucherRefundSelectedDay() {
        verifyExportForSubtype(12, this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_036)
    public void exportsCampaignRefundSelectedDay() {
        verifyExportForSubtype(18, this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_015)
    public void exportsVoucherRefundCombinedFilters() {
        verifyExportForSubtype(12, this::verifyExportAfterCombinedFilters);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_037)
    public void exportsCampaignRefundCombinedFilters() {
        verifyExportForSubtype(18, this::verifyExportAfterCombinedFilters);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_016)
    public void exportsPendingMomoMatrixCell() { verifyMatrix("Đang chờ", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_017)
    public void exportsPendingPaypalMatrixCell() { verifyMatrix("Đang chờ", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_018)
    public void exportsPendingOnepayMatrixCell() { verifyMatrix("Đang chờ", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_019)
    public void exportsPendingBankingMatrixCell() { verifyMatrix("Đang chờ", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_020)
    public void exportsPendingNeoxMatrixCell() { verifyMatrix("Đang chờ", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_021)
    public void exportsSuccessMomoMatrixCell() { verifyMatrix("Thành công", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_022)
    public void exportsSuccessPaypalMatrixCell() { verifyMatrix("Thành công", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_023)
    public void exportsSuccessOnepayMatrixCell() { verifyMatrix("Thành công", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_024)
    public void exportsSuccessBankingMatrixCell() { verifyMatrix("Thành công", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_025)
    public void exportsSuccessNeoxMatrixCell() { verifyMatrix("Thành công", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_026)
    public void exportsFailedMomoMatrixCell() { verifyMatrix("Thất bại", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_027)
    public void exportsFailedPaypalMatrixCell() { verifyMatrix("Thất bại", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_028)
    public void exportsFailedOnepayMatrixCell() { verifyMatrix("Thất bại", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_029)
    public void exportsFailedBankingMatrixCell() { verifyMatrix("Thất bại", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_030)
    public void exportsFailedNeoxMatrixCell() { verifyMatrix("Thất bại", "NEOX"); }

    private void verifyMatrix(String status, String gateway) {
        verifyExportMatrixCellOnFirstSubtype(status, gateway);
    }
}
