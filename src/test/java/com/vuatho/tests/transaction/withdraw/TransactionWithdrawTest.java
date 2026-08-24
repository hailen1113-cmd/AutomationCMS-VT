package com.vuatho.tests.transaction.withdraw;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionCategoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra chuyên biệt nhóm Tiền rút trong Lịch sử giao dịch. */
public class TransactionWithdrawTest extends TransactionCategoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionWithdrawTest.class, "Lịch sử giao dịch", "Tiền rút");
    }

    @Override
    protected TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.WITHDRAW;
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_002)
    public void opensRegularWithdrawalRoute() { verifySubtypeRoute(category().subtypes().get(0)); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_018)
    public void opensRewardToWalletRoute() { verifySubtypeRoute(category().subtypes().get(1)); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_019)
    public void opensDirectRewardWithdrawalRoute() { verifySubtypeRoute(category().subtypes().get(2)); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_020)
    public void opensCostWalletWithdrawalRoute() { verifySubtypeRoute(category().subtypes().get(3)); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_021)
    public void opensCooperationEndWithdrawalRoute() { verifySubtypeRoute(category().subtypes().get(4)); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_022)
    public void opensBankBalanceWithdrawalRoute() { verifySubtypeRoute(category().subtypes().get(5)); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_003)
    public void showsExpectedFiltersAndColumns() {
        verifyLayout();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_004)
    public void rowsHaveValidFormats() {
        verifyRowFormats();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_005)
    public void searchesUserAndRestoresRows() {
        verifySearchAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_006)
    public void filterOptionsKeepCurrentTab() {
        verifyFilterOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_007)
    public void sortsAmountBothDirections() {
        verifyAmountSort();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_008)
    public void opensAndClosesDetail() {
        verifyDetail();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_009)
    public void paginationAndResetKeepCurrentTab() {
        verifyPaginationAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_010)
    public void exportsCurrentRegularWithdrawal() { verifyExportForSubtype(1, this::verifyExport); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_023)
    public void exportsCurrentRewardToWallet() { verifyExportForSubtype(5, this::verifyExport); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_024)
    public void exportsCurrentDirectRewardWithdrawal() { verifyExportForSubtype(13, this::verifyExport); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_025)
    public void exportsCurrentCostWalletWithdrawal() { verifyExportForSubtype(21, this::verifyExport); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_026)
    public void exportsCurrentCooperationEndWithdrawal() { verifyExportForSubtype(23, this::verifyExport); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_027)
    public void exportsCurrentBankBalanceWithdrawal() { verifyExportForSubtype(35, this::verifyExport); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_011)
    public void exportsRegularWithdrawalSearchResults() { verifyExportForSubtype(1, this::verifyExportAfterSearch); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_028)
    public void exportsRewardToWalletSearchResults() { verifyExportForSubtype(5, this::verifyExportAfterSearch); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_029)
    public void exportsDirectRewardSearchResults() { verifyExportForSubtype(13, this::verifyExportAfterSearch); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_030)
    public void exportsCostWalletSearchResults() { verifyExportForSubtype(21, this::verifyExportAfterSearch); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_031)
    public void exportsCooperationEndSearchResults() { verifyExportForSubtype(23, this::verifyExportAfterSearch); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_032)
    public void exportsBankBalanceSearchResults() { verifyExportForSubtype(35, this::verifyExportAfterSearch); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_012)
    public void exportsRegularWithdrawalSuccessResults() { verifyExportForSubtype(1, this::verifyExportAfterStatus); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_033)
    public void exportsRewardToWalletSuccessResults() { verifyExportForSubtype(5, this::verifyExportAfterStatus); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_034)
    public void exportsDirectRewardSuccessResults() { verifyExportForSubtype(13, this::verifyExportAfterStatus); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_035)
    public void exportsCostWalletSuccessResults() { verifyExportForSubtype(21, this::verifyExportAfterStatus); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_036)
    public void exportsCooperationEndSuccessResults() { verifyExportForSubtype(23, this::verifyExportAfterStatus); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_037)
    public void exportsBankBalanceSuccessResults() { verifyExportForSubtype(35, this::verifyExportAfterStatus); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_013)
    public void exportsRegularWithdrawalSelectedDay() { verifyExportForSubtype(1, this::verifyExportAfterDate); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_038)
    public void exportsRewardToWalletSelectedDay() { verifyExportForSubtype(5, this::verifyExportAfterDate); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_039)
    public void exportsDirectRewardSelectedDay() { verifyExportForSubtype(13, this::verifyExportAfterDate); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_040)
    public void exportsCostWalletSelectedDay() { verifyExportForSubtype(21, this::verifyExportAfterDate); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_041)
    public void exportsCooperationEndSelectedDay() { verifyExportForSubtype(23, this::verifyExportAfterDate); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_042)
    public void exportsBankBalanceSelectedDay() { verifyExportForSubtype(35, this::verifyExportAfterDate); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_014)
    public void exportsRegularWithdrawalCombinedSearchSuccessAndDate() { verifyExportForSubtype(1, this::verifyExportAfterSearchStatusAndDate); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_043)
    public void exportsRewardToWalletCombinedSearchSuccessAndDate() { verifyExportForSubtype(5, this::verifyExportAfterSearchStatusAndDate); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_044)
    public void exportsDirectRewardCombinedSearchSuccessAndDate() { verifyExportForSubtype(13, this::verifyExportAfterSearchStatusAndDate); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_045)
    public void exportsCostWalletCombinedSearchSuccessAndDate() { verifyExportForSubtype(21, this::verifyExportAfterSearchStatusAndDate); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_046)
    public void exportsCooperationEndCombinedSearchSuccessAndDate() { verifyExportForSubtype(23, this::verifyExportAfterSearchStatusAndDate); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_047)
    public void exportsBankBalanceCombinedSearchSuccessAndDate() { verifyExportForSubtype(35, this::verifyExportAfterSearchStatusAndDate); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_015)
    public void exportsPendingStatusMatrixCell() {
        verifyStatusMatrixCellOnFirstSubtype("Đang chờ");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_016)
    public void exportsSuccessStatusMatrixCell() {
        verifyStatusMatrixCellOnFirstSubtype("Thành công");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_048)
    public void directRewardAcceptActionIsAvailable() {
        openSubtype(subtypeByType(13));
        assertControl("Chấp nhận", 13);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_049)
    public void regularWithdrawalCreatePaymentActionIsAvailable() {
        openSubtype(subtypeByType(1));
        assertControl("Tạo lệnh chi", 1);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_050)
    public void bankWithdrawalCreatePaymentActionIsAvailable() {
        openSubtype(subtypeByType(35));
        assertControl("Tạo lệnh chi", 35);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_051)
    public void cooperationEndConfirmAndCopyActionsAreAvailable() {
        openSubtype(subtypeByType(23));
        var result = advancedPage().inspectDetailControls(
                List.of("Xác nhận giao dịch", "Sao chép"), "Sao chép");

        Assert.assertTrue(result.openedUrl().contains("type=23"), result.openedUrl());
        Assert.assertTrue(result.controls().stream().allMatch(control ->
                control.present() && control.visible()));
        Assert.assertTrue(result.controls().stream()
                .filter(control -> control.label().equals("Sao chép"))
                .allMatch(com.vuatho.pages.TransactionHistoryPage.DetailControlState::enabled));
        Assert.assertTrue(result.safeActionPerformed());
        Assert.assertTrue(result.closed());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_052)
    public void bankWithdrawalWarrantyOrdersCanBeExpanded() {
        openSubtype(subtypeByType(35));
        var result = advancedPage().expandWarrantyOrders();

        Assert.assertTrue(result.openedUrl().contains("type=35"), result.openedUrl());
        Assert.assertTrue(result.cardText().contains("Đơn còn bảo hành"), result.cardText());
        Assert.assertTrue(result.cardText().contains("Bấm để xem danh sách"), result.cardText());
        Assert.assertTrue(result.afterText().length() >= result.beforeText().length());
        Assert.assertTrue(result.stayedOpen());
        Assert.assertTrue(result.closed());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_053)
    public void bankWithdrawalRejectedTransactionLinkOpensExpectedDetail() {
        openSubtype(subtypeByType(35));
        assertRejectedTransactionLink(35);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_054)
    public void regularWithdrawalRejectedTransactionLinkOpensExpectedDetail() {
        openSubtype(subtypeByType(1));
        assertRejectedTransactionLink(1);
    }

    private void assertControl(String label, int type) {
        var result = advancedPage().inspectDetailControls(List.of(label), null);
        Assert.assertTrue(result.openedUrl().contains("type=" + type), result.openedUrl());
        Assert.assertEquals(result.controls().size(), 1);
        Assert.assertTrue(result.controls().get(0).present());
        Assert.assertTrue(result.controls().get(0).visible());
        Assert.assertTrue(result.closed());
    }

    private void assertRejectedTransactionLink(int type) {
        var result = advancedPage().openRejectedTransactionLink();
        Assert.assertTrue(result.sourceUrl().contains("type=" + type), result.sourceUrl());
        Assert.assertTrue(result.linkText().contains("Xem giao dịch bị từ chối"), result.linkText());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("/vuatho/transaction?"), result.actualUrl());
        Assert.assertTrue(result.actualUrl().contains("id="), result.actualUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_055)
    public void remainingSubtypesShowExpectedLayouts() {
        remainingSubtypes().forEach(subtype -> {
            openSubtype(subtype);
            verifyLayout(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_056)
    public void remainingSubtypeRowsHaveValidFormats() {
        remainingSubtypes().forEach(subtype -> {
            openSubtype(subtype);
            verifyRowFormats();
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_057)
    public void remainingSubtypesSearchAndResetRows() {
        remainingSubtypes().forEach(subtype -> {
            openSubtype(subtype);
            verifySearchAndReset(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_058)
    public void remainingSubtypesKeepFilterOptionsAndRoute() {
        remainingSubtypes().forEach(subtype -> {
            openSubtype(subtype);
            verifyFilterOptions(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_059)
    public void remainingSubtypesSortAmountsBothDirections() {
        remainingSubtypes().forEach(subtype -> {
            openSubtype(subtype);
            verifyAmountSort(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_060)
    public void remainingSubtypesOpenAndCloseDetails() {
        remainingSubtypes().forEach(subtype -> {
            openSubtype(subtype);
            verifyDetail(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_061)
    public void remainingSubtypesKeepPaginationAndResetRoute() {
        remainingSubtypes().forEach(subtype -> {
            openSubtype(subtype);
            verifyPaginationAndReset(subtype);
        });
    }

    private List<TransactionCategoryPage.Subtype> remainingSubtypes() {
        return category().subtypes().stream().skip(1).toList();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_017)
    public void exportsFailedStatusMatrixCell() {
        verifyStatusMatrixCellOnFirstSubtype("Thất bại");
    }
}
