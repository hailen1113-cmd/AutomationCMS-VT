package com.vuatho.tests.uniform.inventory.salesstock.receipts.stockimport;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockImportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra validation ngày và số lượng trên form Nhập hàng. */
public class SalesStockImportValidationTest extends SalesStockImportTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(SalesStockImportValidationTest.class, "Kho bán hàng", "Validation Nhập hàng");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_137)
    public void acceptsPastAndFutureImportDates() {
        var result = importPage.acceptsPastAndFutureDates();
        Assert.assertEquals(result.actualPast(), result.expectedPast());
        Assert.assertEquals(result.actualFuture(), result.expectedFuture());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_138)
    public void zeroQuantityCannotBeSubmitted() {
        var result = importPage.setQuantity("0");
        Assert.assertEquals(result.quantity(), "0");
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_139)
    public void blankQuantityRemainsEmptyAndBlocksSubmission() {
        var result = importPage.setQuantity("");
        Assert.assertEquals(result.quantity(), "",
                "Ô chưa nhập phải giữ value rỗng; số 0 hiển thị chỉ là placeholder.");
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_140)
    public void exactMainStockIsValid() {
        var result = importPage.setExactStock();
        Assert.assertEquals(result.quantity(), Integer.toString(result.stock()));
        Assert.assertEquals(result.totalQuantity(), result.stock());
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_141)
    public void quantityAboveMainStockIsRejected() {
        var result = importPage.setQuantityAboveStock();
        Assert.assertFalse(result.submitEnabled());
        Assert.assertTrue(result.error().contains("Vượt tồn kho tổng")
                || result.rowText().contains("Vượt tồn kho tổng"));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_142)
    public void negativeSignIsBlocked() {
        var result = importPage.setQuantity("-1");
        Assert.assertEquals(result.quantity(), "1");
        Assert.assertFalse(result.quantity().contains("-"));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_143)
    public void decimalPointIsBlocked() {
        var result = importPage.setQuantity("1.5");
        Assert.assertEquals(result.quantity(), "15");
        Assert.assertFalse(result.quantity().contains("."));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_144)
    public void decimalCommaIsBlocked() {
        var result = importPage.setQuantity("1,5");
        Assert.assertEquals(result.quantity(), "15");
        Assert.assertFalse(result.quantity().contains(","));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_145)
    public void lettersAndSpecialCharactersAreRejected() {
        var result = importPage.setQuantity("abc@#");
        Assert.assertTrue(result.quantity().isBlank() || result.quantity().equals("0"));
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_146)
    public void thousandsSeparatorIsParsedAsInteger() {
        var result = importPage.setQuantity("1,111");
        Assert.assertEquals(result.quantity(), "1,111");
        Assert.assertEquals(result.totalQuantity(), 1_111);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_147)
    public void blankAdditionalLotDoesNotBlockValidLot() {
        var result = importPage.blankAdditionalLotDoesNotBlockValidLot();
        Assert.assertEquals(result.firstQuantity(), "1");
        Assert.assertEquals(result.secondQuantity(), "",
                "Lô chưa nhập số lượng phải có value rỗng, không phải giá trị 0.");
        Assert.assertEquals(result.selectedLots(), 1);
        Assert.assertEquals(result.totalQuantity(), 1);
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_148)
    public void invalidMultiLotLocksAndCorrectionUnlocks() {
        var corrected = importPage.invalidLotLocksThenCorrectionUnlocks();
        var invalid = corrected.previous();
        Assert.assertFalse(invalid.submitEnabled());
        Assert.assertTrue(invalid.comboEnabled());
        Assert.assertTrue(corrected.submitEnabled());
        Assert.assertTrue(corrected.comboEnabled());
        Assert.assertEquals(corrected.selectedLots(), 2);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_149)
    public void blankRequiredDateCannotBeSubmitted() {
        var result = importPage.blankDateLocksSubmission();
        Assert.assertEquals(result.date(), "");
        Assert.assertFalse(result.submitEnabled(),
                "Lỗi UI: đã bỏ trống Ngày nhập bắt buộc nhưng vẫn cho tạo phiếu.");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_177)
    public void leadingZerosAreNormalizedToInteger() {
        var result = importPage.setQuantity("0001");
        Assert.assertEquals(result.quantity(), "1");
        Assert.assertEquals(result.totalQuantity(), 1);
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_178)
    public void plusSignIsBlockedFromQuantity() {
        var result = importPage.setQuantity("+1");
        Assert.assertEquals(result.quantity(), "1");
        Assert.assertFalse(result.quantity().contains("+"));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_179)
    public void scientificNotationIsNotAccepted() {
        var result = importPage.setQuantity("1e3");
        Assert.assertFalse(result.quantity().toLowerCase().contains("e"));
        Assert.assertEquals(result.quantity(), "13");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_180)
    public void oversizedIntegerCannotBeSubmitted() {
        String oversized = "999999999999999999999999";
        var result = importPage.entersOversizedQuantity();
        Assert.assertFalse(result.submitEnabled());
        Assert.assertTrue(!oversized.equals(result.quantity())
                        || result.error().contains("Vượt tồn kho tổng")
                        || result.rowText().contains("Vượt tồn kho tổng"),
                "Số nguyên cực lớn không được chấp nhận nguyên trạng mà không có lỗi");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_181)
    public void surroundingSpacesAreRemoved() {
        var result = importPage.setQuantity(" 1 ");
        Assert.assertEquals(result.quantity(), "1");
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_182)
    public void correctingZeroQuantityUnlocksSubmission() {
        var result = importPage.repairsQuantity("0", "1");
        Assert.assertEquals(result.invalid().quantity(), "0");
        Assert.assertFalse(result.invalid().submitEnabled());
        Assert.assertEquals(result.corrected().quantity(), "1");
        Assert.assertTrue(result.corrected().submitEnabled());
        Assert.assertEquals(result.totalQuantity(), 1);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_183)
    public void correctingCharactersUnlocksSubmission() {
        var result = importPage.repairsQuantity("abc@#", "1");
        Assert.assertFalse(result.invalid().submitEnabled());
        Assert.assertTrue(result.invalid().quantity().isBlank() || result.invalid().quantity().equals("0"));
        Assert.assertEquals(result.corrected().quantity(), "1");
        Assert.assertTrue(result.corrected().submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_184)
    public void removingInvalidRowUnlocksRemainingReceipt() {
        var result = importPage.removesInvalidRowAndUnlocksReceipt();
        Assert.assertTrue(result.disabledBeforeRemoval());
        Assert.assertTrue(result.invalidRowRemoved());
        Assert.assertEquals(result.selectedLots(), 1);
        Assert.assertEquals(result.totalQuantity(), 1);
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_185)
    public void allBlankLotsKeepSubmissionLocked() {
        var result = importPage.allBlankLotsKeepSubmissionLocked();
        Assert.assertEquals(result.firstQuantity(), "");
        Assert.assertEquals(result.secondQuantity(), "");
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_186)
    public void explicitZeroLotDoesNotBlockValidLot() {
        var result = importPage.validLotWithExplicitZeroLot();
        Assert.assertEquals(result.firstQuantity(), "1");
        Assert.assertEquals(result.secondQuantity(), "0");
        Assert.assertEquals(result.selectedLots(), 1);
        Assert.assertEquals(result.totalQuantity(), 1);
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_187)
    public void restoringDateKeepsValidReceiptSubmittable() {
        var result = importPage.restoresRequiredDate();
        Assert.assertEquals(result.blankDate(), "");
        Assert.assertEquals(result.actualRestoredDate(), result.expectedRestoredDate());
        Assert.assertTrue(result.enabledAfterRestore());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_188)
    public void validDateDoesNotOverrideInvalidQuantity() {
        var result = importPage.validDateDoesNotOverrideInvalidQuantity();
        Assert.assertEquals(result.actualDate(), result.expectedDate());
        Assert.assertEquals(result.quantity(), Integer.toString(result.stock() + 1));
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_189)
    public void multipleExactStockQuantitiesAreValid() {
        var result = importPage.multipleExactStocksAreValid();
        Assert.assertEquals(result.firstQuantity(), Integer.toString(result.firstStock()));
        Assert.assertEquals(result.secondQuantity(), Integer.toString(result.secondStock()));
        Assert.assertEquals(result.selectedLots(), 2);
        Assert.assertEquals(result.totalQuantity(), result.firstStock() + result.secondStock());
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_190)
    public void crossingStockBoundaryUpdatesValidationImmediately() {
        var result = importPage.changesAboveExactAndBelowStock();
        Assert.assertFalse(result.above().submitEnabled());
        Assert.assertEquals(result.above().quantity(), Integer.toString(result.stock() + 1));
        Assert.assertTrue(result.exact().submitEnabled());
        Assert.assertEquals(result.exact().quantity(), Integer.toString(result.stock()));
        Assert.assertTrue(result.below().submitEnabled());
        Assert.assertEquals(result.below().quantity(), Integer.toString(result.stock() - 1));
        Assert.assertEquals(result.finalTotal(), result.stock() - 1);
    }
}
