package com.vuatho.tests.uniform.inventory.uniformstock.stockreceipt;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.StockReceiptTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/** Testcase cho vùng Điền nhanh của sản phẩm có nhiều biến thể. */
public class StockReceiptBulkFillTest extends StockReceiptTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockReceiptBulkFillTest.class,
                "Kho Đồng phục", "Điền nhanh phiếu nhập kho");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "bulk-fill"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_109)
    public void displaysBulkFillControlsForMultiVariantProduct() {
        var result = receiptPage.bulkFillControls();
        Assert.assertTrue(result.rowCount() >= 2);
        Assert.assertTrue(result.quantityVisible());
        Assert.assertTrue(result.priceVisible());
        Assert.assertTrue(result.applyButtonVisible());
        Assert.assertTrue(result.initialQuantity().isBlank());
        Assert.assertTrue(result.initialPrice().isBlank());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "bulk-fill"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_110)
    public void appliesBulkQuantityToEveryVariant() {
        var result = receiptPage.applyBulkQuantityToAllVariants();
        Assert.assertEquals(result.quantities().size(), result.rowCount());
        Assert.assertTrue(result.quantities().stream().allMatch(value -> value == 3));
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "bulk-fill"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_111)
    public void appliesBulkPriceToEveryVariant() {
        var result = receiptPage.applyBulkPriceToAllVariants();
        Assert.assertEquals(result.prices().size(), result.rowCount());
        Assert.assertTrue(result.prices().stream().allMatch(value -> value == 2000));
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "bulk-fill"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_112)
    public void bulkQuantityAndPriceUpdateTotalsCorrectly() {
        var result = receiptPage.applyBulkValuesAndCalculateSummary();
        Assert.assertEquals(result.summary().validLots(), result.rowCount());
        Assert.assertEquals(result.summary().totalLots(), result.rowCount());
        Assert.assertEquals(result.summary().totalQuantity(),
                result.rowCount() * result.quantityPerRow());
        Assert.assertEquals(result.summary().totalAmount(),
                (long) result.rowCount() * result.quantityPerRow() * result.pricePerRow());
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "bulk-fill"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_113)
    public void reapplyingBulkValuesOverwritesEveryVariant() {
        var result = receiptPage.reapplyBulkValuesOverwritesAllRows();
        Assert.assertTrue(result.first().quantities().stream().allMatch(value -> value == 1));
        Assert.assertTrue(result.first().prices().stream().allMatch(value -> value == 1000));
        Assert.assertTrue(result.second().quantities().stream().allMatch(value -> value == 4));
        Assert.assertTrue(result.second().prices().stream().allMatch(value -> value == 2500));
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "bulk-fill"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_114)
    public void manualOverrideChangesOnlyOneVariantAfterBulkFill() {
        var result = receiptPage.manualRowOverrideAfterBulkFill();
        for (int index = 0; index < result.quantities().size(); index++) {
            int expected = index == result.changedIndex()
                    ? result.changedValue() : result.originalValue();
            Assert.assertEquals((int) result.quantities().get(index), expected,
                    "Sai số lượng tại dòng biến thể " + index);
        }
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "bulk-fill"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_115)
    public void bulkFillOneProductDoesNotAffectAnotherProduct() {
        var result = receiptPage.bulkFillDoesNotAffectOtherProduct();
        if (!result.candidateAvailable()) {
            throw new SkipException("Không tìm được sản phẩm thứ hai trong dữ liệu hiện tại.");
        }
        Assert.assertTrue(result.firstQuantities().stream().allMatch(value -> value == 5));
        Assert.assertTrue(result.secondQuantities().stream().allMatch(value -> value == 0));
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "bulk-fill", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_116)
    public void invalidBulkQuantitiesAreSanitizedOrBlocked() {
        var results = receiptPage.invalidBulkQuantitiesAreNotApplied();
        Assert.assertEquals(results.size(), 4);
        results.forEach(result -> {
            boolean sanitized = !result.attemptedValue()
                    .equals(result.actualBulkValue());
            boolean blocked = result.rowQuantities().stream()
                    .allMatch(value -> value == 0);
            Assert.assertTrue(sanitized || blocked,
                    "Giá trị không hợp lệ được giữ nguyên và áp dụng: attempted="
                            + result.attemptedValue()
                            + ", actualBulk=" + result.actualBulkValue()
                            + ", rowQuantities=" + result.rowQuantities());
        });
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "bulk-fill"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_117)
    public void removingVariantAfterBulkFillUpdatesTotals() {
        var result = receiptPage.removeVariantAfterBulkFill();
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore() - 1);
        Assert.assertEquals(result.summaryAfter().totalLots(), result.rowsAfter());
        Assert.assertEquals(result.summaryAfter().validLots(), result.rowsAfter());
        Assert.assertEquals(result.summaryAfter().totalQuantity(), result.rowsAfter() * 2);
        Assert.assertEquals(result.summaryAfter().totalAmount(), (long) result.rowsAfter() * 2000);
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "bulk-fill"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_118)
    public void individualLotCodesCombinedWithBulkFillEnableSubmission() {
        var result = receiptPage.lotCodesCombinedWithBulkFillEnableSubmission();
        Assert.assertEquals(result.summary().validLots(), result.rowCount());
        Assert.assertEquals(result.summary().totalLots(), result.rowCount());
        Assert.assertEquals(result.summary().totalQuantity(), result.rowCount() * 3);
        Assert.assertEquals(result.summary().totalAmount(), (long) result.rowCount() * 3600);
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "old-lot"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_119)
    public void switchingOldLotUpdatesCodeAndPrice() {
        var result = receiptPage.switchBetweenOldLots();
        if (!result.candidateAvailable()) {
            throw new SkipException("Dòng biến thể hiện tại không có ít nhất hai lô cũ.");
        }
        Assert.assertNotEquals(result.firstCode(), result.secondCode());
        Assert.assertEquals(result.actualCode(), result.secondCode());
        Assert.assertEquals(result.actualPrice(), result.expectedPrice());
    }
}
