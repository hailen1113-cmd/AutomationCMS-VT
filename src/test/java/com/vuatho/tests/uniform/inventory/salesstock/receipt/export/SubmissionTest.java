package com.vuatho.tests.uniform.inventory.salesstock.receipt.export;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockStaffExportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra tạo phiếu xuất hàng thật trên dữ liệu sandbox. */
public class SubmissionTest extends SalesStockStaffExportTestSupport {
    public static void main(String[] args) { TestNgRunner.run(SubmissionTest.class, "Kho bán hàng", "Tạo phiếu xuất hàng cho nhân sự"); }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_074)
    public void submitsOneAvailableLotForStaff() {
        var result = exportPage.submitOneAvailableLot();
        Assert.assertFalse(result.code().isBlank());
        Assert.assertTrue(result.enabledBeforeSubmit());
        Assert.assertTrue(result.dialogClosed(), "Gửi phiếu xuất xong nhưng form vẫn mở.");
    }
}
