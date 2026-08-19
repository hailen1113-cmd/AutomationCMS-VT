package com.vuatho.tests.transaction.system;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionCategoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra chuyên biệt nhóm Hệ thống trong Lịch sử giao dịch. */
public class TransactionSystemTest extends TransactionCategoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionSystemTest.class, "Lịch sử giao dịch", "Hệ thống");
    }

    @Override
    protected TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.SYSTEM;
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_002)
    public void opensSystemRoute() {
        verifySubtypeRoute(category().subtypes().get(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_003)
    public void showsExpectedFiltersAndColumns() {
        verifyLayout();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_004)
    public void rowsHaveValidFormats() {
        verifyRowFormats();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_005)
    public void searchesUserAndRestoresRows() {
        verifySearchAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_006)
    public void filterOptionsKeepCurrentTab() {
        verifyFilterOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_007)
    public void sortsAmountBothDirections() {
        verifyAmountSort();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_008)
    public void opensAndClosesDetail() {
        verifyDetail();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_009)
    public void paginationAndResetKeepCurrentTab() {
        verifyPaginationAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_010)
    public void exportsCurrentSubtype() {
        verifyExportForSubtype(7, this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_011)
    public void exportsSearchResults() {
        verifyExportForSubtype(7, this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_012)
    public void exportsStatusResults() {
        verifyExportForSubtype(7, this::verifyExportAfterStatus);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_013)
    public void exportsGatewayResults() {
        verifyExportForSubtype(7, this::verifyExportAfterGateway);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_014)
    public void exportsSelectedDay() {
        verifyExportForSubtype(7, this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_015)
    public void exportsCombinedFilters() {
        verifyExportForSubtype(7, this::verifyExportAfterCombinedFilters);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_016)
    public void exportsPendingMomoMatrixCell() { verifyMatrix("Đang chờ", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_017)
    public void exportsPendingPaypalMatrixCell() { verifyMatrix("Đang chờ", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_018)
    public void exportsPendingOnepayMatrixCell() { verifyMatrix("Đang chờ", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_019)
    public void exportsPendingBankingMatrixCell() { verifyMatrix("Đang chờ", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_020)
    public void exportsPendingNeoxMatrixCell() { verifyMatrix("Đang chờ", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_021)
    public void exportsSuccessMomoMatrixCell() { verifyMatrix("Thành công", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_022)
    public void exportsSuccessPaypalMatrixCell() { verifyMatrix("Thành công", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_023)
    public void exportsSuccessOnepayMatrixCell() { verifyMatrix("Thành công", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_024)
    public void exportsSuccessBankingMatrixCell() { verifyMatrix("Thành công", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_025)
    public void exportsSuccessNeoxMatrixCell() { verifyMatrix("Thành công", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_026)
    public void exportsFailedMomoMatrixCell() { verifyMatrix("Thất bại", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_027)
    public void exportsFailedPaypalMatrixCell() { verifyMatrix("Thất bại", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_028)
    public void exportsFailedOnepayMatrixCell() { verifyMatrix("Thất bại", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_029)
    public void exportsFailedBankingMatrixCell() { verifyMatrix("Thất bại", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_030)
    public void exportsFailedNeoxMatrixCell() { verifyMatrix("Thất bại", "NEOX"); }

    private void verifyMatrix(String status, String gateway) {
        verifyExportMatrixCellOnFirstSubtype(status, gateway);
    }
}
