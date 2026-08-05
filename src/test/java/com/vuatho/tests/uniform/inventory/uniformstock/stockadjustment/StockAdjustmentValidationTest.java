package com.vuatho.tests.uniform.inventory.uniformstock.stockadjustment;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.StockAdjustmentTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Testcase validation số thực tế, ngày và lý do của Điều chỉnh tồn. */
public class StockAdjustmentValidationTest extends StockAdjustmentTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockAdjustmentValidationTest.class,
                "Kho Đồng phục", "Validation Điều chỉnh tồn");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_050)
    public void positiveDeltaIsCalculated() {
        var result = adjustmentPage.enterQuantityDelta(1);
        Assert.assertEquals(result.actualValue(),
                Integer.toString(result.expectedActual()));
        Assert.assertTrue(result.rowText().contains("+1"),
                "Chênh lệch dương không hiển thị +1.");
        Assert.assertEquals(result.counter().changed(), 1);
        Assert.assertTrue(result.confirmEnabled(), "Nút xác nhận chưa được bật.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_051)
    public void negativeDeltaIsCalculated() {
        var result = adjustmentPage.enterQuantityDelta(-1);
        Assert.assertEquals(result.actualValue(),
                Integer.toString(result.expectedActual()));
        Assert.assertTrue(result.rowText().contains("−1")
                        || result.rowText().contains("-1"),
                "Chênh lệch âm không hiển thị -1.");
        Assert.assertEquals(result.counter().changed(), 1);
        Assert.assertTrue(result.confirmEnabled(), "Nút xác nhận chưa được bật.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_052)
    public void unchangedQuantityIsNotCounted() {
        var result = adjustmentPage.enterUnchangedQuantity();
        Assert.assertEquals(result.counter().changed(), 0,
                "Số không đổi vẫn bị tính là lô thay đổi.");
        Assert.assertFalse(result.confirmEnabled(),
                "Không có chênh lệch nhưng nút xác nhận vẫn bật.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_053)
    public void rejectsInvalidActualQuantityFormats() {
        var results = adjustmentPage.invalidQuantityFormats();
        Assert.assertEquals(results.size(), 4, "Chưa thử đủ định dạng không hợp lệ.");
        for (var result : results) {
            Assert.assertTrue(!result.actualValue().equals(result.attemptedValue())
                            || !result.confirmEnabled(),
                    "Form chấp nhận số thực tế không hợp lệ: "
                            + result.attemptedValue());
        }
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_054)
    public void dateIsRequired() {
        var result = adjustmentPage.clearRequiredDateAndSubmit();
        Assert.assertEquals(result.date(), "", "Không xóa được ngày điều chỉnh.");
        Assert.assertTrue(result.submissionBlocked(),
                "Hệ thống vẫn xác nhận điều chỉnh khi thiếu ngày bắt buộc.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_055)
    public void reasonIsOptional() {
        var result = adjustmentPage.leaveReasonBlank();
        Assert.assertEquals(result.reason(), "", "Lý do không để trống.");
        Assert.assertTrue(result.confirmEnabled(),
                "Để trống lý do làm nút xác nhận bị khóa.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_062)
    public void allSelectedLotsRequireActualQuantity() {
        var result = adjustmentPage.completeAllSelectedLots();
        Assert.assertFalse(result.enabledWithMissingLot(),
                "Còn lô chưa nhập số thực tế nhưng nút xác nhận đã bật.");
        Assert.assertEquals(result.counterBeforeCompletion().total(), 2,
                "Bộ đếm chưa nhận đủ hai lô.");
        Assert.assertTrue(result.enabledAfterCompletion(),
                "Đã nhập đủ số thực tế nhưng nút xác nhận chưa bật.");
        Assert.assertEquals(result.counterAfterCompletion().changed(), 1,
                "Bộ đếm không nhận đúng một lô có chênh lệch.");
        Assert.assertEquals(result.counterAfterCompletion().total(), 2,
                "Bộ đếm tổng lô thay đổi sau khi nhập đủ.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_063)
    public void zeroIsValidForZeroStockLot() {
        var result = adjustmentPage.acceptsZeroForZeroStockLot();
        Assert.assertEquals(result.currentStock(), 0, "Lô kiểm tra không có tồn bằng 0.");
        Assert.assertEquals(result.actualValue(), "0", "Form không giữ số thực tế bằng 0.");
        Assert.assertFalse(result.enabledBeforeAllLots(),
                "Còn lô chưa nhập nhưng nút xác nhận đã bật.");
        Assert.assertTrue(result.enabledAfterAllLots(),
                "Số 0 hợp lệ nhưng form không cho xác nhận sau khi nhập đủ.");
        Assert.assertEquals(result.counter().changed(), 1,
                "Lô tồn 0 nhập 0 bị tính sai là có chênh lệch.");
        Assert.assertEquals(result.counter().total(), 2,
                "Bộ đếm không nhận đủ hai lô.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_065)
    public void positiveStockCanBeReducedToZero() {
        var result = adjustmentPage.reducePositiveStockToZero();
        Assert.assertTrue(result.currentStock() > 0,
                "Lô kiểm tra không có tồn dương.");
        Assert.assertEquals(result.actualValue(), "0",
                "Form không giữ số thực tế bằng 0.");
        String negativeDifference = "−" + result.currentStock();
        Assert.assertTrue(result.rowText().contains(negativeDifference)
                        || result.rowText().contains("-" + result.currentStock()),
                "Chênh lệch không giảm đúng toàn bộ tồn hiện tại.");
        Assert.assertEquals(result.counter().changed(), 1,
                "Bộ đếm không ghi nhận lô giảm về 0.");
        Assert.assertTrue(result.confirmEnabled(),
                "Giảm tồn về 0 nhưng nút xác nhận chưa bật.");
    }
}
