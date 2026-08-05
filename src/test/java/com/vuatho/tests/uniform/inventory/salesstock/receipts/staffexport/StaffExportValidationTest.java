package com.vuatho.tests.uniform.inventory.salesstock.receipts.staffexport;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockStaffExportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra các điều kiện số lượng trước khi xuất hàng cho nhân sự. */
public class StaffExportValidationTest extends SalesStockStaffExportTestSupport {
    public static void main(String[] args) { TestNgRunner.run(StaffExportValidationTest.class, "Kho bán hàng", "Validation xuất hàng cho nhân sự"); }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_072)
    public void zeroQuantityDoesNotAllowSubmission() {
        var result = exportPage.setZeroQuantity();
        Assert.assertEquals(result.quantity(), "0");
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_073)
    public void quantityAboveAvailableStockDoesNotAllowSubmission() {
        var result = exportPage.setQuantityAboveStock();
        Assert.assertEquals(result.quantity(), Integer.toString(result.stock() + 1));
        Assert.assertFalse(result.submitEnabled(), "Form không chặn số lượng xuất vượt tồn của lô " + result.code());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_084)
    public void blankQuantityLocksSubmissionAndAddingLots() {
        var result = exportPage.blankQuantityLocksForm();
        Assert.assertEquals(result.quantity(), "");
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_085)
    public void negativeQuantityIsNotAccepted() {
        var result = exportPage.entersInvalidQuantity("-1");
        Assert.assertFalse(result.quantity().contains("-"));
        Assert.assertNotEquals(result.quantity(), "-1");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_086)
    public void decimalQuantityIsNotAccepted() {
        var result = exportPage.entersInvalidQuantity("1.5");
        Assert.assertFalse(result.quantity().contains("."));
        Assert.assertFalse(result.quantity().contains(","));
        Assert.assertNotEquals(result.quantity(), "1.5");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_087)
    public void nonNumericCharactersDoNotAllowSubmission() {
        var result = exportPage.entersInvalidQuantity("abc@#");
        Assert.assertFalse(result.submitEnabled());
        Assert.assertFalse(result.quantity().matches(".*[A-Za-z@#].*"));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_088)
    public void oneInvalidLotLocksWholeMultiLotReceipt() {
        var result = exportPage.oneInvalidLotLocksWholeReceipt();
        Assert.assertEquals(result.validQuantity(), "1");
        Assert.assertEquals(result.invalidQuantity(), "0");
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_089)
    public void correctingInvalidLotUnlocksWholeMultiLotReceipt() {
        var result = exportPage.correctingInvalidLotUnlocksWholeReceipt();
        Assert.assertEquals(result.validQuantity(), "1");
        Assert.assertEquals(result.invalidQuantity(), "1");
        Assert.assertTrue(result.submitEnabled());
        Assert.assertTrue(result.lotComboboxEnabled());
        Assert.assertEquals(result.totalQuantity(), 2);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_090)
    public void blankExportDateDoesNotAllowSubmission() {
        var result = exportPage.blankExportDateLocksForm();
        Assert.assertEquals(result.exportDate(), "");
        Assert.assertFalse(result.submitEnabled());
    }
}
