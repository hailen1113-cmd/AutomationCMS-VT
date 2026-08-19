package com.vuatho.tests.transaction.deposit;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionDepositTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Kiểm tra xuất Excel của từng loại Tiền nạp. */
public class TransactionDepositExportTest extends TransactionDepositTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionDepositExportTest.class,
                "Lịch sử giao dịch", "Tiền nạp - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_010)
    public void exportsCurrentSubtype() {
        verifySubtype(subtype(0), this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_092)
    public void exportsCurrentSubtypeType10() {
        verifySubtype(subtype(10), this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_093)
    public void exportsCurrentSubtypeType19() {
        verifySubtype(subtype(19), this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_094)
    public void exportsCurrentSubtypeType20() {
        verifySubtype(subtype(20), this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_095)
    public void exportsCurrentSubtypeType34() {
        verifySubtype(subtype(34), this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_068)
    public void exportsSearchResults() {
        verifySubtype(subtype(0), this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_096)
    public void exportsSearchResultsType10() {
        verifySubtype(subtype(10), this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_097)
    public void exportsSearchResultsType19() {
        verifySubtype(subtype(19), this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_098)
    public void exportsSearchResultsType20() {
        verifySubtype(subtype(20), this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_099)
    public void exportsSearchResultsType34() {
        verifySubtype(subtype(34), this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_069)
    public void exportsStatusResults() {
        verifySubtype(subtype(0), this::verifyExportAfterStatus);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_100)
    public void exportsStatusResultsType10() {
        verifySubtype(subtype(10), this::verifyExportAfterStatus);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_101)
    public void exportsStatusResultsType19() {
        verifySubtype(subtype(19), this::verifyExportAfterStatus);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_102)
    public void exportsStatusResultsType20() {
        verifySubtype(subtype(20), this::verifyExportAfterStatus);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_103)
    public void exportsStatusResultsType34() {
        verifySubtype(subtype(34), this::verifyExportAfterStatus);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_070)
    public void exportsGatewayResults() {
        verifySubtype(subtype(0), this::verifyExportAfterGateway);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_104)
    public void exportsGatewayResultsType10() {
        verifySubtype(subtype(10), this::verifyExportAfterGateway);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_105)
    public void exportsGatewayResultsType19() {
        verifySubtype(subtype(19), this::verifyExportAfterGateway);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_106)
    public void exportsGatewayResultsType20() {
        verifySubtype(subtype(20), this::verifyExportAfterGateway);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_107)
    public void exportsGatewayResultsType34() {
        verifySubtype(subtype(34), this::verifyExportAfterGateway);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_071)
    public void exportsSelectedDay() {
        verifySubtype(subtype(0), this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_108)
    public void exportsSelectedDayType10() {
        verifySubtype(subtype(10), this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_109)
    public void exportsSelectedDayType19() {
        verifySubtype(subtype(19), this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_110)
    public void exportsSelectedDayType20() {
        verifySubtype(subtype(20), this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_111)
    public void exportsSelectedDayType34() {
        verifySubtype(subtype(34), this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_072)
    public void exportsCombinedFilters() {
        verifySubtype(subtype(0), this::verifyExportAfterCombinedFilters);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_112)
    public void exportsCombinedFiltersType10() {
        verifySubtype(subtype(10), this::verifyExportAfterCombinedFilters);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_113)
    public void exportsCombinedFiltersType19() {
        verifySubtype(subtype(19), this::verifyExportAfterCombinedFilters);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_114)
    public void exportsCombinedFiltersType20() {
        verifySubtype(subtype(20), this::verifyExportAfterCombinedFilters);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_115)
    public void exportsCombinedFiltersType34() {
        verifySubtype(subtype(34), this::verifyExportAfterCombinedFilters);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_073)
    public void exportsPendingMomoMatrixCell() { verifyMatrixCell("Đang chờ", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_074)
    public void exportsPendingPaypalMatrixCell() { verifyMatrixCell("Đang chờ", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_075)
    public void exportsPendingOnepayMatrixCell() { verifyMatrixCell("Đang chờ", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_076)
    public void exportsPendingBankingMatrixCell() { verifyMatrixCell("Đang chờ", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_077)
    public void exportsPendingNeoxMatrixCell() { verifyMatrixCell("Đang chờ", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_078)
    public void exportsSuccessMomoMatrixCell() { verifyMatrixCell("Thành công", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_079)
    public void exportsSuccessPaypalMatrixCell() { verifyMatrixCell("Thành công", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_080)
    public void exportsSuccessOnepayMatrixCell() { verifyMatrixCell("Thành công", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_081)
    public void exportsSuccessBankingMatrixCell() { verifyMatrixCell("Thành công", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_082)
    public void exportsSuccessNeoxMatrixCell() { verifyMatrixCell("Thành công", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_083)
    public void exportsCancelledMomoMatrixCell() { verifyMatrixCell("Đã hủy", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_084)
    public void exportsCancelledPaypalMatrixCell() { verifyMatrixCell("Đã hủy", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_085)
    public void exportsCancelledOnepayMatrixCell() { verifyMatrixCell("Đã hủy", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_086)
    public void exportsCancelledBankingMatrixCell() { verifyMatrixCell("Đã hủy", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_087)
    public void exportsCancelledNeoxMatrixCell() { verifyMatrixCell("Đã hủy", "NEOX"); }

    private void verifyMatrixCell(String status, String gateway) {
        verifyExportMatrixCellOnFirstSubtype(status, gateway);
    }

    private void verifySubtype(TransactionCategoryPage.Subtype subtype,
                               Consumer<TransactionCategoryPage.Subtype> verification) {
            openDepositSubtype(subtype);
            verification.accept(subtype);
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
