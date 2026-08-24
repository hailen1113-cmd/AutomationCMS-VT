package com.vuatho.tests.transaction.reward;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionCategoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra chuyên biệt nhóm Thưởng và khuyến mãi trong Lịch sử giao dịch. */
public class TransactionRewardOverviewAndExportTest extends TransactionCategoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionRewardOverviewAndExportTest.class,
                "Lịch sử giao dịch", "Thưởng và khuyến mãi - Tổng quan và xuất Excel");
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

    // Retired from execution: REWARD-099/100 cover name and phone search on both subtypes.
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_005, enabled = false)
    public void searchesUserAndRestoresRows() {
        verifySearchAndReset();
    }

    // Retired from execution: REWARD-045/046/050 cover filter options and reset more deeply.
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_006, enabled = false)
    public void filterOptionsKeepCurrentTab() {
        verifyFilterOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_007)
    public void sortsAmountBothDirections() {
        verifyAmountSort();
    }

    // Retired from execution: REWARD-063/064/065 cover opening and closing the drawer.
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_008, enabled = false)
    public void opensAndClosesDetail() {
        verifyDetail();
    }

    // Retired from execution: REWARD-056/062/101/102 cover pagination and reset.
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_009, enabled = false)
    public void paginationAndResetKeepCurrentTab() {
        verifyPaginationAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_010)
    public void exportsCurrentVoucherRefund() {
        verifyExportForSubtype(12, this::verifyCompleteExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_032)
    public void exportsCurrentCampaignRefund() {
        verifyExportForSubtype(18, this::verifyCompleteExport);
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

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_038)
    public void campaignRefundShowsExpectedLayout() {
        openSubtype(campaignRefund());
        verifyLayout(campaignRefund());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_039)
    public void campaignRefundRowsHaveValidFormats() {
        openSubtype(campaignRefund());
        verifyRowFormats();
    }

    // Retired from execution: REWARD-099/100 already exercise both reward subtypes.
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_040, enabled = false)
    public void campaignRefundSearchesAndResetsRows() {
        openSubtype(campaignRefund());
        verifySearchAndReset(campaignRefund());
    }

    // Retired from execution: REWARD-045/046/050 already exercise both reward subtypes.
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_041, enabled = false)
    public void campaignRefundKeepsFilterOptionsAndRoute() {
        openSubtype(campaignRefund());
        verifyFilterOptions(campaignRefund());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_042)
    public void campaignRefundSortsAmountsBothDirections() {
        openSubtype(campaignRefund());
        verifyAmountSort(campaignRefund());
    }

    // Retired from execution: REWARD-063/064/065 already exercise both reward subtypes.
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_043, enabled = false)
    public void campaignRefundOpensAndClosesDetail() {
        openSubtype(campaignRefund());
        verifyDetail(campaignRefund());
    }

    // Retired from execution: REWARD-056/062/101/102 already exercise both reward subtypes.
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_044, enabled = false)
    public void campaignRefundKeepsPaginationAndResetRoute() {
        openSubtype(campaignRefund());
        verifyPaginationAndReset(campaignRefund());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_075)
    public void exportsCampaignPendingMomoMatrixCell() { verifyCampaignMatrix("Đang chờ", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_076)
    public void exportsCampaignPendingPaypalMatrixCell() { verifyCampaignMatrix("Đang chờ", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_077)
    public void exportsCampaignPendingOnepayMatrixCell() { verifyCampaignMatrix("Đang chờ", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_078)
    public void exportsCampaignPendingBankingMatrixCell() { verifyCampaignMatrix("Đang chờ", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_079)
    public void exportsCampaignPendingNeoxMatrixCell() { verifyCampaignMatrix("Đang chờ", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_080)
    public void exportsCampaignSuccessMomoMatrixCell() { verifyCampaignMatrix("Thành công", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_081)
    public void exportsCampaignSuccessPaypalMatrixCell() { verifyCampaignMatrix("Thành công", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_082)
    public void exportsCampaignSuccessOnepayMatrixCell() { verifyCampaignMatrix("Thành công", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_083)
    public void exportsCampaignSuccessBankingMatrixCell() { verifyCampaignMatrix("Thành công", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_084)
    public void exportsCampaignSuccessNeoxMatrixCell() { verifyCampaignMatrix("Thành công", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_085)
    public void exportsCampaignFailedMomoMatrixCell() { verifyCampaignMatrix("Thất bại", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_086)
    public void exportsCampaignFailedPaypalMatrixCell() { verifyCampaignMatrix("Thất bại", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_087)
    public void exportsCampaignFailedOnepayMatrixCell() { verifyCampaignMatrix("Thất bại", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_088)
    public void exportsCampaignFailedBankingMatrixCell() { verifyCampaignMatrix("Thất bại", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_089)
    public void exportsCampaignFailedNeoxMatrixCell() { verifyCampaignMatrix("Thất bại", "NEOX"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_110)
    public void exportsVoucherSearchAndSelectedDay() {
        verifyExportForSubtype(12, this::verifyExportAfterSearchAndDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_111)
    public void exportsCampaignSearchAndSelectedDay() {
        verifyExportForSubtype(18, this::verifyExportAfterSearchAndDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_112)
    public void exportsVoucherSearchStatusAndSelectedDay() {
        verifyExportForSubtype(12, this::verifyExportAfterSearchStatusAndDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_113)
    public void exportsCampaignSearchStatusAndSelectedDay() {
        verifyExportForSubtype(18, this::verifyExportAfterSearchStatusAndDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_114)
    public void resetThenExportsVoucherBaseline() {
        verifyExportForSubtype(12, this::verifyExportAfterReset);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_115)
    public void resetThenExportsCampaignBaseline() {
        verifyExportForSubtype(18, this::verifyExportAfterReset);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_116)
    public void repeatedVoucherExportsCreateFreshFiles() {
        verifyExportForSubtype(12, this::verifyRepeatedExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_117)
    public void repeatedCampaignExportsCreateFreshFiles() {
        verifyExportForSubtype(18, this::verifyRepeatedExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_118)
    public void voucherExportFromSecondPageContainsAllRows() {
        verifyExportForSubtype(12, this::verifyExportFromSecondPage);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_119)
    public void campaignExportFromSecondPageContainsAllRows() {
        verifyExportForSubtype(18, this::verifyExportFromSecondPage);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_120)
    public void emptyVoucherResultsExportEmptyWorkbook() {
        verifyExportForSubtype(12, this::verifyEmptyResultExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_121)
    public void emptyCampaignResultsExportEmptyWorkbook() {
        verifyExportForSubtype(18, this::verifyEmptyResultExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_131)
    public void voucherExportContainsFirstVisibleRowExactly() {
        verifyExportForSubtype(12, this::verifyExportContainsFirstVisibleRow);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_132)
    public void campaignExportContainsFirstVisibleRowExactly() {
        verifyExportForSubtype(18, this::verifyExportContainsFirstVisibleRow);
    }

    private TransactionCategoryPage.Subtype campaignRefund() {
        return subtypeByType(18);
    }

    private void verifyMatrix(String status, String gateway) {
        verifyExportMatrixCellOnFirstSubtype(status, gateway);
    }

    private void verifyCampaignMatrix(String status, String gateway) {
        verifyExportForSubtype(18,
                subtype -> verifyExportAfterStatusAndGateway(subtype, status, gateway));
    }
}
