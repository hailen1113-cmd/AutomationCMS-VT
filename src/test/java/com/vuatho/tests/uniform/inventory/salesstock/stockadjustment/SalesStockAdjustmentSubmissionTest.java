package com.vuatho.tests.uniform.inventory.salesstock.stockadjustment;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockAdjustmentTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Xác nhận điều chỉnh tồn thật trên sandbox và luôn khôi phục tồn ban đầu. */
public class SalesStockAdjustmentSubmissionTest extends SalesStockAdjustmentTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(SalesStockAdjustmentSubmissionTest.class,
                "Kho bán hàng", "Xác nhận Điều chỉnh tồn");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_114)
    public void adjustsOneLotAndRestoresStock() {
        var result = adjustmentPage.submitOneLotAndRestore();
        Assert.assertEquals(result.stockAfterSubmit(), result.expectedStock(),
                "Tồn Kho bán hàng không cập nhật đúng sau khi xác nhận.");
        Assert.assertEquals(result.stockAfterRestore(), result.initialStock(),
                "Tồn Kho bán hàng không được khôi phục về số ban đầu.");
        Assert.assertTrue(result.receiptText().contains("Điều chỉnh tồn")
                        && result.receiptText().contains(result.code())
                        && result.receiptText().contains("DC-")
                        && result.receiptText().contains(LocalDate.now()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))),
                "Phiếu điều chỉnh mới thiếu mã DC, loại phiếu, mã lô hoặc ngày tạo.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_115)
    public void adjustsTwoLotsAndRestoresStocks() {
        var result = adjustmentPage.submitTwoLotsAndRestore();
        Assert.assertNotEquals(result.firstCode(), result.secondCode(), "Hai lô điều chỉnh bị trùng.");
        Assert.assertEquals(result.firstAfterSubmit(), result.firstExpected(),
                "Lô thứ nhất không cập nhật đúng sau khi xác nhận.");
        Assert.assertEquals(result.secondAfterSubmit(), result.secondExpected(),
                "Lô thứ hai không cập nhật đúng sau khi xác nhận.");
        Assert.assertEquals(result.firstAfterRestore(), result.firstInitial(),
                "Lô thứ nhất không được khôi phục tồn ban đầu.");
        Assert.assertEquals(result.secondAfterRestore(), result.secondInitial(),
                "Lô thứ hai không được khôi phục tồn ban đầu.");
        Assert.assertTrue(result.receiptText().contains("Điều chỉnh tồn")
                        && result.receiptText().contains(result.firstCode())
                        && result.receiptText().contains(result.secondCode())
                        && result.receiptText().contains("DC-"),
                "Phiếu điều chỉnh nhiều lô không chứa mã DC, loại phiếu và hai mã lô.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_116)
    public void adjustsPositiveStockToZeroAndRestoresIt() {
        var result = adjustmentPage.submitPositiveLotToZeroAndRestore();
        Assert.assertEquals(result.stockAfterSubmit(), 0, "Xác nhận không đưa tồn lô về 0.");
        Assert.assertEquals(result.stockAfterRestore(), result.initialStock(), "Không khôi phục được tồn ban đầu.");
        assertAdjustmentReceipt(result.receiptText(), result.code());
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_117)
    public void adjustsThousandsSeparatedActualQuantityAndRestoresIt() {
        var result = adjustmentPage.submitThousandsSeparatedActualAndRestore();
        Assert.assertEquals(result.stockAfterSubmit(), result.expectedStock(),
                "Số thực tế có dấu phẩy không được ghi nhận đúng sau submit.");
        Assert.assertEquals(result.stockAfterRestore(), result.initialStock(), "Không khôi phục được tồn ban đầu.");
        assertAdjustmentReceipt(result.receiptText(), result.code());
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_118)
    public void adjustsThreeLotsAndRestoresAllStocks() {
        var result = adjustmentPage.submitThreeLotsAndRestore();
        Assert.assertEquals(result.stocksAfterSubmit(), result.expectedStocks(),
                "Ba lô không cập nhật đúng tồn sau submit.");
        Assert.assertEquals(result.stocksAfterRestore(), result.initialStocks(),
                "Ba lô không được khôi phục đúng tồn ban đầu.");
        for (String code : result.codes()) {
            assertAdjustmentReceipt(result.receiptText(), code);
        }
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_119)
    public void increasesZeroStockLotAndRestoresIt() {
        var result = adjustmentPage.submitZeroStockLotIncreaseAndRestore();
        Assert.assertEquals(result.stockAfterSubmit(), 1, "Lô tồn 0 không tăng đúng lên 1.");
        Assert.assertEquals(result.stockAfterRestore(), 0, "Lô không được khôi phục về tồn 0.");
        assertAdjustmentReceipt(result.receiptText(), result.code());
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_120)
    public void submitsPastDateAndShowsValidReceiptMetadata() {
        var result = adjustmentPage.submitPastDateAndRestore();
        Assert.assertTrue(result.receiptText().matches("(?s).*\\b\\d{2}/\\d{2}/\\d{4}\\b.*"),
                "Phiếu không hiển thị ngày tạo hợp lệ.");
        Assert.assertTrue(result.receiptText().matches("(?s).*\\b\\d{2}:\\d{2}\\b.*"),
                "Phiếu không hiển thị giờ tạo hợp lệ.");
        Assert.assertTrue(result.receiptText().matches("(?s).*\\b\\d{2}:\\d{2}\\b\\s+\\S.*"),
                "Phiếu không hiển thị người tạo sau thời gian tạo.");
        assertAdjustmentReceipt(result.receiptText(), result.code());
        Assert.assertEquals(result.stockAfterRestore(), result.initialStock(),
                "Không khôi phục được tồn sau kiểm tra metadata phiếu.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_121)
    public void salesAdjustmentDoesNotChangeMainWarehouse() {
        var result = adjustmentPage.submitWithoutChangingMainWarehouse();
        Assert.assertEquals(result.mainStockAfter(), result.mainStockBefore(),
                "Điều chỉnh Kho bán hàng làm thay đổi tồn Kho tổng.");
        Assert.assertEquals(result.salesStockAfterRestore(), result.salesStockInitial(),
                "Không khôi phục được tồn Kho bán hàng sau kiểm tra phạm vi.");
    }

    private void assertAdjustmentReceipt(String text, String code) {
        Assert.assertTrue(text.contains("Điều chỉnh tồn") && text.contains("DC-")
                        && text.contains(code) && !text.contains("Xuất nhân sự"),
                "Phiếu không đúng DC/Điều chỉnh tồn hoặc bị lẫn phiếu Xuất nhân sự: " + code);
    }
}
