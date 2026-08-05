package com.vuatho.tests.uniform.inventory.salesstock.receipts.staffexport;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockStaffExportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra tạo phiếu xuất hàng thật trên dữ liệu sandbox. */
public class StaffExportSubmissionTest extends SalesStockStaffExportTestSupport {
    public static void main(String[] args) { TestNgRunner.run(StaffExportSubmissionTest.class, "Kho bán hàng", "Tạo phiếu xuất hàng cho nhân sự"); }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_074)
    public void submitsOneAvailableLotForStaff() {
        var result = exportPage.submitOneAvailableLot();
        Assert.assertFalse(result.code().isBlank());
        Assert.assertTrue(result.enabledBeforeSubmit());
        Assert.assertTrue(result.dialogClosed(), "Gửi phiếu xuất xong nhưng form vẫn mở.");
        Assert.assertEquals(result.stockAfterExport(), result.stockBeforeExport() - 1,
                "Tồn của lô " + result.code() + " không giảm đúng 1 sau khi xuất.");
        Assert.assertFalse(result.receiptCode().isBlank(),
                "Không tìm thấy phiếu xuất nhân sự vừa tạo trong tab Phiếu.");
        Assert.assertEquals(result.receiptType(), "xuat nhan su");
        Assert.assertTrue(result.receiptLotCodes().contains(result.code()));
        Assert.assertTrue(result.receiptQuantities().contains(1));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_080)
    public void submitsMultipleLotsInOneStaffExportReceipt() {
        var result = exportPage.submitTwoLotsInOneReceipt();
        Assert.assertTrue(result.enabledBeforeSubmit());
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.firstStockAfter(), result.firstStockBefore() - 1);
        Assert.assertEquals(result.secondStockAfter(), result.secondStockBefore() - 1);
        Assert.assertFalse(result.receiptCode().isBlank());
        Assert.assertEquals(result.receiptType(), "xuat nhan su");
        Assert.assertTrue(result.receiptLotCodes().containsAll(
                java.util.List.of(result.firstCode(), result.secondCode())));
        Assert.assertTrue(result.receiptQuantities().containsAll(java.util.List.of(1, 1)));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_081)
    public void cancellingPreparedStaffExportDoesNotChangeStock() {
        var result = exportPage.cancelsPreparedExportWithoutChangingStock();
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.stockAfterCancel(), result.stockBeforeCancel());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_082)
    public void submittingExactAvailableStockLeavesLotAtZero() {
        var result = exportPage.submitsExactAvailableStock();
        Assert.assertTrue(result.enabledBeforeSubmit());
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.stockAfterExport(), 0);
        Assert.assertFalse(result.receiptCode().isBlank());
        Assert.assertTrue(result.receiptQuantities().contains(result.quantity()));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_083)
    public void closingPreparedStaffExportDoesNotChangeStock() {
        var result = exportPage.closesPreparedExportWithoutChangingStock();
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.stockAfterCancel(), result.stockBeforeCancel());
    }
}
