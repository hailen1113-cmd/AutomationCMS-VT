package com.vuatho.tests.transaction.fee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;

/** Kiểm tra xuất Excel của từng loại Phí & Doanh thu. */
public class TransactionFeeExportTest extends TransactionFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionFeeExportTest.class,
                "Lịch sử giao dịch", "Phí & Doanh thu - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_010)
    public void exportsCurrentConnectionFee() {
        verifySubtype(8, this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_196)
    public void exportsCurrentWalletLinkFee() {
        verifySubtype(9, this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_197)
    public void exportsCurrentMaterialSharingFee() {
        verifySubtype(33, this::verifyExport);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_094)
    public void exportsConnectionFeeSearchResults() {
        verifySubtype(8, this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_198)
    public void exportsWalletLinkFeeSearchResults() {
        verifySubtype(9, this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_199)
    public void exportsMaterialSharingFeeSearchResults() {
        verifySubtype(33, this::verifyExportAfterSearch);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_095)
    public void exportsConnectionFeeSelectedDay() {
        verifySubtype(8, this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_200)
    public void exportsWalletLinkFeeSelectedDay() {
        verifySubtype(9, this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_201)
    public void exportsMaterialSharingFeeSelectedDay() {
        verifySubtype(33, this::verifyExportAfterDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_096)
    public void connectionFeeExposesBothInvoiceOptions() {
        openFeeSubtype(subtype(8));
        var result = transactionPage.optionsForFilter("xuất hoá đơn-filter");
        org.testng.Assert.assertEquals(result.options(), List.of("Tất cả", "Có", "Không"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_097)
    public void exportsConnectionFeeSearchAndDateResults() {
        verifySubtype(8, this::verifyExportAfterSearchAndDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_202)
    public void exportsWalletLinkFeeSearchAndDateResults() {
        verifySubtype(9, this::verifyExportAfterSearchAndDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_203)
    public void exportsMaterialSharingFeeSearchAndDateResults() {
        verifySubtype(33, this::verifyExportAfterSearchAndDate);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_098)
    public void exportsConnectionFeeWithInvoice() { invoiceMatrix(8, "Có"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_099)
    public void exportsConnectionFeeWithoutInvoice() { invoiceMatrix(8, "Không"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_100)
    public void exportsMaterialSharingFeeWithInvoice() { invoiceMatrix(33, "Có"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_101)
    public void exportsMaterialSharingFeeWithoutInvoice() { invoiceMatrix(33, "Không"); }

    private void invoiceMatrix(int type, String option) {
        TransactionCategoryPage.Subtype subtype = category().subtypes().stream()
                .filter(candidate -> candidate.type() == type).findFirst()
                .orElseThrow(() -> new IllegalStateException("Thiếu subtype type=" + type));
        openFeeSubtype(subtype);
        verifyExportAfterOption(subtype, "xuất hoá đơn-filter", option);
    }

    private void verifySubtype(int type,
                               Consumer<TransactionCategoryPage.Subtype> verification) {
        TransactionCategoryPage.Subtype subtype = subtype(type);
        openFeeSubtype(subtype);
        verification.accept(subtype);
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(candidate -> candidate.type() == type).findFirst()
                .orElseThrow(() -> new IllegalStateException("Thiếu subtype type=" + type));
    }
}
