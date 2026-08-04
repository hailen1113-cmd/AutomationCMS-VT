package com.vuatho.tests.uniform.inventory.salesstock.receipt.export;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockStaffExportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra các điều kiện số lượng trước khi xuất hàng cho nhân sự. */
public class ValidationTest extends SalesStockStaffExportTestSupport {
    public static void main(String[] args) { TestNgRunner.run(ValidationTest.class, "Kho bán hàng", "Validation xuất hàng cho nhân sự"); }

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
}
