package com.vuatho.tests.uniform.inventory.salesstock.receipt.read;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockReceiptTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;

/** Kiểm tra hiển thị và dữ liệu của tab Phiếu Kho bán hàng. */
public class OverviewTest extends SalesStockReceiptTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(OverviewTest.class, "Kho bán hàng", "Tổng quan tab Phiếu");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_050)
    public void showsReceiptTabControlsAndHeaders() {
        var screen = receiptPage.screenSnapshot();
        Assert.assertTrue(screen.url().contains("tab=sub"));
        Assert.assertTrue(screen.salesTabSelected());
        Assert.assertTrue(screen.receiptTabSelected());
        Assert.assertTrue(screen.allFilterSelected());
        Assert.assertEquals(screen.headers().stream().map(String::toUpperCase).toList(),
                java.util.List.of("MÃ PHIẾU", "LOẠI", "CHI TIẾT", "NGÀY"));
        Assert.assertTrue(screen.allFiltersVisible());
        Assert.assertTrue(screen.exportButtonVisible());
        Assert.assertTrue(screen.importButtonVisible());
        Assert.assertTrue(screen.exportButtonEnabled(), "Nút Xuất hàng đang bị khóa.");
        Assert.assertTrue(screen.importButtonEnabled(), "Nút Nhập hàng đang bị khóa.");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_051)
    public void receiptRowsContainValidBusinessData() {
        var rows = receiptPage.observedRows();
        Assert.assertFalse(rows.isEmpty(), "Tab Phiếu không có dữ liệu để kiểm tra.");
        Assert.assertEquals(new HashSet<>(rows.stream().map(row -> row.code()).toList()).size(), rows.size(),
                "Trang hiện tại có mã phiếu trùng nhau.");
        for (var row : rows) {
            Assert.assertTrue(row.code().matches("[A-Z]{2,4}-\\d{4}-\\d{3,}"), "Mã phiếu sai: " + row.code());
            Assert.assertFalse(row.type().isBlank(), "Phiếu thiếu loại: " + row.code());
            assertReceiptCodeMatchesType(row.code(), row.normalizedType());
            Assert.assertFalse(row.lotCodes().isEmpty(), "Phiếu thiếu mã lô: " + row.code());
            Assert.assertEquals(row.quantities().size(), row.lotCodes().size(), "Số lượng chi tiết không khớp: " + row.code());
            Assert.assertTrue(row.quantities().stream().allMatch(value -> value > 0), "Phiếu có số lượng không hợp lệ: " + row.code());
            Assert.assertTrue(row.date().matches("\\d{2}/\\d{2}/\\d{4}"), "Ngày sai: " + row.code());
            Assert.assertTrue(row.time().matches("\\d{2}:\\d{2}"), "Giờ sai: " + row.code());
            Assert.assertFalse(row.operator().isBlank(), "Phiếu thiếu người thao tác: " + row.code());
        }
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_052)
    public void receiptsAreSortedNewestFirst() {
        var times = receiptPage.receiptTimes();
        Assert.assertTrue(times.size() >= 2, "Không đủ phiếu để kiểm tra thứ tự thời gian.");
        for (int index = 1; index < times.size(); index++) {
            Assert.assertFalse(times.get(index).isAfter(times.get(index - 1)),
                    "Danh sách phiếu không sắp xếp mới nhất trước.");
        }
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_053)
    public void scrollsToLastReceiptAndReturnsToTop() {
        var result = receiptPage.scrollLastReceiptAndBack();
        Assert.assertTrue(result.rowCount() > 0);
        Assert.assertTrue(result.reachedLast());
        Assert.assertTrue(result.returnedFirst());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_063)
    public void switchingStockAndReceiptsRestoresReceiptData() {
        var result = receiptPage.switchStockAndBackToReceipts();
        Assert.assertTrue(result.stockSelected());
        Assert.assertTrue(result.stockControlsVisible(),
                "Tab Tồn kho không hiển thị đúng điều khiển riêng sau khi chuyển tab.");
        Assert.assertTrue(result.receiptSelected());
        Assert.assertEquals(result.restoredCodes(), result.initialCodes(),
                "Quay lại tab Phiếu nhưng dữ liệu trang đầu thay đổi.");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_064)
    public void reselectingReceiptsTabKeepsListStable() {
        var result = receiptPage.reselectReceiptsTab();
        Assert.assertTrue(result.receiptSelected());
        Assert.assertEquals(result.repeatedCodes(), result.initialCodes(),
                "Bấm lại tab Phiếu làm thay đổi hoặc nhân đôi danh sách.");
        Assert.assertEquals(result.repeatedPage(), result.initialPage(),
                "Bấm lại tab Phiếu làm thay đổi trạng thái phân trang.");
    }

    private void assertReceiptCodeMatchesType(String code, String normalizedType) {
        if (code.startsWith("CK-")) {
            Assert.assertTrue(normalizedType.contains("nhap chuyen kho"), "Sai loại phiếu: " + code);
        } else if (code.startsWith("XD-")) {
            Assert.assertTrue(normalizedType.contains("xuat don"), "Sai loại phiếu: " + code);
        } else if (code.startsWith("XNS-")) {
            Assert.assertTrue(normalizedType.contains("xuat nhan su"), "Sai loại phiếu: " + code);
        }
    }
}
