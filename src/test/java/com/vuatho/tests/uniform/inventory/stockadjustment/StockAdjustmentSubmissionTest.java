package com.vuatho.tests.uniform.inventory.stockadjustment;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.StockAdjustmentTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Testcase xác nhận điều chỉnh tồn thật trên dữ liệu sandbox. */
public class StockAdjustmentSubmissionTest extends StockAdjustmentTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockAdjustmentSubmissionTest.class,
                "Kho Đồng phục", "Xác nhận Điều chỉnh tồn thật");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_056)
    public void adjustsStockAndRestoresOriginalQuantity() {
        var result = adjustmentPage.submitIncreaseAndRestore();
        Assert.assertEquals(result.stockAfterIncrease(), result.expectedIncreased(),
                "Tồn kho không tăng đúng sau lần điều chỉnh đầu.");
        Assert.assertEquals(result.stockAfterRestore(), result.initialStock(),
                "Tồn kho không trở về số ban đầu sau lần điều chỉnh khôi phục.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_064)
    public void adjustsMultipleLotsAndRestoresAllQuantities() {
        var result = adjustmentPage.submitMultipleLotsAndRestore();
        Assert.assertNotEquals(result.increasedCode(), result.decreasedCode(),
                "Hai thao tác điều chỉnh dùng trùng một lô.");
        Assert.assertEquals(result.submittedCounter().changed(), 2,
                "Bộ đếm không ghi nhận đủ hai lô có chênh lệch.");
        Assert.assertEquals(result.submittedCounter().total(), 2,
                "Bộ đếm tổng lô không bằng 2.");
        Assert.assertEquals(result.increasedStock(), result.increasedTarget(),
                "Lô tăng không cập nhật đúng tồn.");
        Assert.assertEquals(result.decreasedStock(), result.decreasedTarget(),
                "Lô giảm không cập nhật đúng tồn.");
        Assert.assertTrue(result.voucherText().contains(result.increasedCode())
                        && result.voucherText().contains(result.decreasedCode()),
                "Phiếu điều chỉnh không chứa đủ hai lô.");
        Assert.assertEquals(result.increasedRestored(), result.increasedInitial(),
                "Lô tăng chưa được khôi phục tồn ban đầu.");
        Assert.assertEquals(result.decreasedRestored(), result.decreasedInitial(),
                "Lô giảm chưa được khôi phục tồn ban đầu.");
    }
}
