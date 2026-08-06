package com.vuatho.tests.uniform.inventory.salesstock.receipts.stockimport;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockImportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Kiểm tra tạo phiếu nhập chuyển kho thật trên sandbox. */
public class SalesStockImportSubmissionTest extends SalesStockImportTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(SalesStockImportSubmissionTest.class, "Kho bán hàng", "Tạo phiếu Nhập hàng");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_150)
    public void submitsOneLotAndMovesStockBetweenWarehouses() {
        var result = importPage.submitOneLot();
        Assert.assertTrue(result.enabledBeforeSubmit());
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.mainAfter(), result.mainBefore() - 1);
        Assert.assertEquals(result.salesAfter(), result.salesBefore() + 1);
        Assert.assertEquals(result.mainBefore() + result.salesBefore(), result.mainAfter() + result.salesAfter());
        Assert.assertFalse(result.receipt().code().isBlank());
        Assert.assertTrue(result.receipt().code().startsWith("CK-"));
        Assert.assertEquals(result.receipt().normalizedType(), "nhap chuyen kho");
        Assert.assertTrue(result.receipt().lotCodes().contains(result.code()));
        Assert.assertTrue(result.receipt().quantities().contains(1));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_151)
    public void submitsTwoSameProductLotsInOneReceipt() {
        var result = importPage.submitTwoSameProductLots();
        Assert.assertTrue(result.enabledBeforeSubmit());
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.firstMainAfter(), result.firstMainBefore() - 1);
        Assert.assertEquals(result.secondMainAfter(), result.secondMainBefore() - 1);
        Assert.assertEquals(result.firstSalesAfter(), result.firstSalesBefore() + 1);
        Assert.assertEquals(result.secondSalesAfter(), result.secondSalesBefore() + 1);
        Assert.assertTrue(result.receipt().lotCodes().containsAll(List.of(result.firstCode(), result.secondCode())));
        Assert.assertEquals(result.receipt().quantities().stream().mapToInt(Integer::intValue).sum(), 2);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_154)
    public void createdReceiptShowsCompleteMetadata() {
        var result = importPage.submitOneLot();
        var receipt = result.receipt();
        Assert.assertTrue(receipt.code().startsWith("CK-"));
        Assert.assertEquals(receipt.normalizedType(), "nhap chuyen kho");
        Assert.assertTrue(receipt.lotCodes().contains(result.code()));
        Assert.assertEquals(receipt.date(), LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Assert.assertTrue(receipt.time().matches("\\d{2}:\\d{2}"));
        Assert.assertFalse(receipt.operator().isBlank());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_165)
    public void submitsQuantityGreaterThanOne() {
        var result = importPage.submitQuantityGreaterThanOne();
        Assert.assertEquals(result.quantity(), 2);
        Assert.assertTrue(result.enabledBeforeSubmit());
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.mainAfter(), result.mainBefore() - 2);
        Assert.assertEquals(result.salesAfter(), result.salesBefore() + 2);
        Assert.assertTrue(result.receipt().quantities().contains(2));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_166)
    public void submittingExactMainStockLeavesMainLotAtZero() {
        var result = importPage.submitExactMainStock();
        Assert.assertTrue(result.quantity() > 0);
        Assert.assertEquals(result.quantity(), result.mainBefore());
        Assert.assertTrue(result.enabledBeforeSubmit());
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.mainAfter(), 0);
        Assert.assertEquals(result.salesAfter(), result.salesBefore() + result.quantity());
        Assert.assertTrue(result.receipt().quantities().contains(result.quantity()));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_167)
    public void submitsMultipleLotsWithDifferentQuantities() {
        var result = importPage.submitLotsWithDifferentQuantities();
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.quantities(), List.of(1, 2));
        for (int index = 0; index < result.codes().size(); index++) {
            int quantity = result.quantities().get(index);
            Assert.assertEquals(result.mainAfter().get(index), result.mainBefore().get(index) - quantity);
            Assert.assertEquals(result.salesAfter().get(index), result.salesBefore().get(index) + quantity);
        }
        Assert.assertTrue(result.receipt().lotCodes().containsAll(result.codes()));
        Assert.assertEquals(result.receipt().quantities().stream().mapToInt(Integer::intValue).sum(), 3);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_168)
    public void submitsLotsFromDifferentProducts() {
        var result = importPage.submitTwoDifferentProducts();
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.firstMainAfter(), result.firstMainBefore() - 1);
        Assert.assertEquals(result.secondMainAfter(), result.secondMainBefore() - 1);
        Assert.assertEquals(result.firstSalesAfter(), result.firstSalesBefore() + 1);
        Assert.assertEquals(result.secondSalesAfter(), result.secondSalesBefore() + 1);
        Assert.assertTrue(result.receipt().lotCodes().containsAll(List.of(result.firstCode(), result.secondCode())));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_169)
    public void submitsReceiptWithPastDate() {
        var result = importPage.submitWithPastDate();
        Assert.assertEquals(result.inputDate(), result.expectedDate());
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.receipt().date(), LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                "Cột metadata của danh sách phiếu phải hiển thị ngày tạo phiếu, không phải Ngày nhập nghiệp vụ.");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_170)
    public void submitsReceiptWithFutureDate() {
        var result = importPage.submitWithFutureDate();
        Assert.assertEquals(result.inputDate(), result.expectedDate());
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.receipt().date(), LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                "Cột metadata của danh sách phiếu phải hiển thị ngày tạo phiếu, không phải Ngày nhập nghiệp vụ.");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_171)
    public void unicodeNoteDoesNotPreventSubmission() {
        var result = importPage.submitWithUnicodeNote();
        Assert.assertEquals(result.actualNote(), result.expectedNote());
        Assert.assertTrue(result.dialogClosed());
        Assert.assertFalse(result.receipt().code().isBlank());
        Assert.assertTrue(result.receipt().lotCodes().contains(result.code()));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_172)
    public void doubleClickCreatesOnlyOneReceipt() {
        var result = importPage.doubleClickCreatesOneReceipt();
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.createdReceiptCount(), 1,
                "Nhấp đôi nút xác nhận đã tạo nhiều hơn một phiếu nhập.");
        Assert.assertEquals(result.createdReceiptCodes().stream().distinct().count(), 1);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_173)
    public void submitShowsProtectedTransitionState() {
        var result = importPage.observesSubmitTransition();
        Assert.assertTrue(result.loadingOrClosed());
        Assert.assertTrue(result.dialogClosed());
        Assert.assertFalse(result.receiptCode().isBlank());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_174)
    public void reopeningAfterSubmitStartsWithCleanForm() {
        var result = importPage.reopensCleanAfterSuccessfulSubmit();
        Assert.assertFalse(result.receiptCode().isBlank());
        Assert.assertEquals(result.note(), "");
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertEquals(result.quantityInputs(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_175)
    public void removedLotIsNotIncludedInCreatedReceipt() {
        var result = importPage.removeOneLotBeforeSubmit();
        Assert.assertTrue(result.dialogClosed());
        Assert.assertFalse(result.receipt().code().isBlank());
        Assert.assertFalse(result.receipt().lotCodes().contains(result.removedCode()));
        Assert.assertTrue(result.receipt().lotCodes().contains(result.submittedCode()));
        Assert.assertEquals(result.receipt().quantities(), List.of(1));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_176)
    public void multiLotTransferPreservesCombinedStock() {
        var result = importPage.submitLotsWithDifferentQuantities();
        Assert.assertTrue(result.dialogClosed());
        for (int index = 0; index < result.codes().size(); index++) {
            int combinedBefore = result.mainBefore().get(index) + result.salesBefore().get(index);
            int combinedAfter = result.mainAfter().get(index) + result.salesAfter().get(index);
            Assert.assertEquals(combinedAfter, combinedBefore,
                    "Tổng tồn hai kho không được bảo toàn cho lô " + result.codes().get(index));
        }
    }
}
