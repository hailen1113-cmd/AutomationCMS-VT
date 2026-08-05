package com.vuatho.tests.uniform.inventory.salesstock.stockadjustment;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockAdjustmentTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Testcase số thực tế, chênh lệch và điều kiện xác nhận Điều chỉnh tồn Kho bán hàng. */
public class SalesStockAdjustmentValidationTest extends SalesStockAdjustmentTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(SalesStockAdjustmentValidationTest.class,
                "Kho bán hàng", "Validation Điều chỉnh tồn");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_100)
    public void positiveDeltaIsCalculated() {
        var result = adjustmentPage.enterQuantityDelta(1);
        Assert.assertEquals(result.actualValue(), Integer.toString(result.expectedActual()));
        Assert.assertTrue(result.rowText().contains("+1"), "Chênh lệch dương không hiển thị +1.");
        Assert.assertEquals(result.counter().changed(), 1);
        Assert.assertTrue(result.confirmEnabled(), "Nút xác nhận chưa được bật.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_101)
    public void negativeDeltaIsCalculated() {
        var result = adjustmentPage.enterQuantityDelta(-1);
        Assert.assertEquals(result.actualValue(), Integer.toString(result.expectedActual()));
        Assert.assertTrue(result.rowText().contains("−1") || result.rowText().contains("-1"), "Chênh lệch âm không hiển thị -1.");
        Assert.assertEquals(result.counter().changed(), 1);
        Assert.assertTrue(result.confirmEnabled(), "Nút xác nhận chưa được bật.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_102)
    public void unchangedQuantityIsNotCounted() {
        var result = adjustmentPage.enterUnchangedQuantity();
        Assert.assertEquals(result.counter().changed(), 0, "Số không đổi vẫn bị tính là lô thay đổi.");
        Assert.assertFalse(result.confirmEnabled(), "Không có chênh lệch nhưng nút xác nhận vẫn bật.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_103)
    public void allSelectedLotsRequireActualQuantity() {
        var result = adjustmentPage.completeAllSelectedLots();
        Assert.assertFalse(result.enabledWithMissingLot(), "Còn lô chưa nhập nhưng nút xác nhận đã bật.");
        Assert.assertEquals(result.counterBeforeCompletion().total(), 2);
        Assert.assertTrue(result.enabledAfterCompletion(), "Đã nhập đủ số thực tế nhưng nút xác nhận chưa bật.");
        Assert.assertEquals(result.counterAfterCompletion().changed(), 1);
        Assert.assertEquals(result.counterAfterCompletion().total(), 2);
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_104)
    public void positiveStockCanBeReducedToZero() {
        var result = adjustmentPage.reducePositiveStockToZero();
        Assert.assertTrue(result.currentStock() > 0, "Lô kiểm tra không có tồn dương.");
        Assert.assertEquals(result.actualValue(), "0", "Form không giữ số thực tế bằng 0.");
        Assert.assertEquals(result.counter().changed(), 1);
        Assert.assertTrue(result.confirmEnabled(), "Giảm tồn về 0 nhưng nút xác nhận chưa bật.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_105)
    public void dateIsRequired() {
        var result = adjustmentPage.clearRequiredDateAndSubmit();
        Assert.assertEquals(result.date(), "", "Không xóa được ngày điều chỉnh.");
        Assert.assertTrue(result.submissionBlocked(), "Thiếu ngày nhưng hệ thống vẫn xác nhận điều chỉnh.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_106)
    public void reasonIsOptional() {
        var result = adjustmentPage.leaveReasonBlank();
        Assert.assertEquals(result.reason(), "", "Lý do không để trống.");
        Assert.assertTrue(result.confirmEnabled(), "Để trống lý do làm nút xác nhận bị khóa.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_107)
    public void rejectsInvalidActualQuantityFormats() {
        var results = adjustmentPage.invalidQuantityFormats();
        Assert.assertEquals(results.size(), 4, "Chưa thử đủ định dạng không hợp lệ.");
        for (var result : results) {
            Assert.assertTrue(!result.actualValue().equals(result.attemptedValue()) || !result.confirmEnabled(),
                    "Form chấp nhận số thực tế không hợp lệ: " + result.attemptedValue());
        }
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_109)
    public void zeroIsValidForZeroStockLot() {
        var result = adjustmentPage.acceptsZeroForZeroStockLot();
        Assert.assertEquals(result.currentStock(), 0, "Lô kiểm tra không có tồn bằng 0.");
        Assert.assertEquals(result.actualValue(), "0", "Form không giữ số thực tế bằng 0.");
        Assert.assertFalse(result.enabledBeforeAllLots(), "Còn lô chưa nhập nhưng xác nhận đã bật.");
        Assert.assertTrue(result.enabledAfterAllLots(), "Số 0 hợp lệ nhưng form không cho xác nhận.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_110)
    public void thousandsSeparatedActualQuantityIsCalculated() {
        var result = adjustmentPage.acceptsThousandsSeparatedActualQuantity();
        Assert.assertEquals(result.actualValue(), result.expectedValue(), "Số có dấu phẩy không được giữ đúng.");
        Assert.assertTrue(result.rowText().contains("+32,123"), "Chênh lệch số hàng nghìn hiển thị không đúng.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_111)
    public void clearingThenRestoringActualQuantityLocksAndUnlocksConfirmation() {
        var result = adjustmentPage.clearingAndRestoringActualQuantity();
        Assert.assertTrue(result.enabledWhenComplete(), "Đã nhập đủ nhưng xác nhận chưa bật.");
        Assert.assertTrue(result.disabledAfterClear(), "Xóa số thực tế nhưng xác nhận chưa bị khóa.");
        Assert.assertTrue(result.enabledAfterRestore(), "Nhập lại số hợp lệ nhưng xác nhận chưa bật lại.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_112)
    public void acceptsPastAndFutureDates() {
        var result = adjustmentPage.acceptsPastAndFutureDates();
        Assert.assertEquals(result.actualPast(), result.expectedPast(), "Không giữ được ngày quá khứ.");
        Assert.assertEquals(result.actualFuture(), result.expectedFuture(), "Không giữ được ngày tương lai.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "validation"}, description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_113)
    public void acceptsLongUnicodeReason() {
        var result = adjustmentPage.acceptsLongUnicodeReason();
        Assert.assertEquals(result.actualReason(), result.expectedReason(),
                "Lý do dài có Unicode hoặc ký tự đặc biệt bị mất dữ liệu.");
    }
}
