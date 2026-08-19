package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionInsuranceTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Kiểm tra xuất Excel VT Care. */
public class TransactionInsuranceExportTest extends TransactionInsuranceTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionInsuranceExportTest.class,
                "Lịch sử giao dịch", "VT Care - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_010)
    public void exportsBothSubtypesInOneRun() {
        verifySubtype(subtype(25), this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_154)
    public void exportsBothSubtypesInOneRunType26() {
        verifySubtype(subtype(26), this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_039)
    public void exportsSearchResultsForBothSubtypes() {
        verifySubtype(subtype(25), this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_155)
    public void exportsSearchResultsForBothSubtypesType26() {
        verifySubtype(subtype(26), this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_040)
    public void exportsStatusResultsForBothSubtypes() {
        verifySubtype(subtype(25), this::verifyExportAfterStatus);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_156)
    public void exportsStatusResultsForBothSubtypesType26() {
        verifySubtype(subtype(26), this::verifyExportAfterStatus);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_041)
    public void exportsGatewayResultsForBothSubtypes() {
        verifySubtype(subtype(25), this::verifyExportAfterGateway);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_157)
    public void exportsGatewayResultsForBothSubtypesType26() {
        verifySubtype(subtype(26), this::verifyExportAfterGateway);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_042)
    public void exportsSelectedDayForBothSubtypes() {
        verifySubtype(subtype(25), this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_158)
    public void exportsSelectedDayForBothSubtypesType26() {
        verifySubtype(subtype(26), this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_043)
    public void exportsCombinedFiltersForBothSubtypes() {
        verifySubtype(subtype(25), this::verifyExportAfterCombinedFilters);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_159)
    public void exportsCombinedFiltersForBothSubtypesType26() {
        verifySubtype(subtype(26), this::verifyExportAfterCombinedFilters);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_044)
    public void exportsPendingMomoMatrixCell() {
        verifyMatrixCell(subtype(25), "Đang chờ", "MOMO");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_160)
    public void exportsPendingMomoMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Đang chờ", "MOMO");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_045)
    public void exportsPendingPaypalMatrixCell() {
        verifyMatrixCell(subtype(25), "Đang chờ", "PAYPAL");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_161)
    public void exportsPendingPaypalMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Đang chờ", "PAYPAL");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_046)
    public void exportsPendingOnepayMatrixCell() {
        verifyMatrixCell(subtype(25), "Đang chờ", "ONEPAY");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_162)
    public void exportsPendingOnepayMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Đang chờ", "ONEPAY");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_047)
    public void exportsPendingBankingMatrixCell() {
        verifyMatrixCell(subtype(25), "Đang chờ", "BANKING");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_163)
    public void exportsPendingBankingMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Đang chờ", "BANKING");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_048)
    public void exportsPendingNeoxMatrixCell() {
        verifyMatrixCell(subtype(25), "Đang chờ", "NEOX");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_164)
    public void exportsPendingNeoxMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Đang chờ", "NEOX");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_049)
    public void exportsSuccessMomoMatrixCell() {
        verifyMatrixCell(subtype(25), "Thành công", "MOMO");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_165)
    public void exportsSuccessMomoMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Thành công", "MOMO");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_050)
    public void exportsSuccessPaypalMatrixCell() {
        verifyMatrixCell(subtype(25), "Thành công", "PAYPAL");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_166)
    public void exportsSuccessPaypalMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Thành công", "PAYPAL");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_051)
    public void exportsSuccessOnepayMatrixCell() {
        verifyMatrixCell(subtype(25), "Thành công", "ONEPAY");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_167)
    public void exportsSuccessOnepayMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Thành công", "ONEPAY");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_052)
    public void exportsSuccessBankingMatrixCell() {
        verifyMatrixCell(subtype(25), "Thành công", "BANKING");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_168)
    public void exportsSuccessBankingMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Thành công", "BANKING");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_053)
    public void exportsSuccessNeoxMatrixCell() {
        verifyMatrixCell(subtype(25), "Thành công", "NEOX");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_169)
    public void exportsSuccessNeoxMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Thành công", "NEOX");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_054)
    public void exportsFailedMomoMatrixCell() {
        verifyMatrixCell(subtype(25), "Thất bại", "MOMO");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_170)
    public void exportsFailedMomoMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Thất bại", "MOMO");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_055)
    public void exportsFailedPaypalMatrixCell() {
        verifyMatrixCell(subtype(25), "Thất bại", "PAYPAL");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_171)
    public void exportsFailedPaypalMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Thất bại", "PAYPAL");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_056)
    public void exportsFailedOnepayMatrixCell() {
        verifyMatrixCell(subtype(25), "Thất bại", "ONEPAY");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_172)
    public void exportsFailedOnepayMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Thất bại", "ONEPAY");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_057)
    public void exportsFailedBankingMatrixCell() {
        verifyMatrixCell(subtype(25), "Thất bại", "BANKING");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_173)
    public void exportsFailedBankingMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Thất bại", "BANKING");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_058)
    public void exportsFailedNeoxMatrixCell() {
        verifyMatrixCell(subtype(25), "Thất bại", "NEOX");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_174)
    public void exportsFailedNeoxMatrixCellType26() {
        verifyMatrixCell(subtype(26), "Thất bại", "NEOX");
    }

    private void verifyMatrixCell(TransactionCategoryPage.Subtype subtype,
                                  String status, String gateway) {
        verifySubtype(subtype, selected -> verifyExportAfterStatusAndGateway(
                selected, status, gateway));
    }

    private void verifySubtype(TransactionCategoryPage.Subtype subtype,
                               Consumer<TransactionCategoryPage.Subtype> verification) {
            openInsuranceSubtype(subtype);
            verification.accept(subtype);
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
