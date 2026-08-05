package com.vuatho.tests.uniform.inventory.salesstock.stockadjustment;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockAdjustmentTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;

/** Testcase cấu trúc form Điều chỉnh tồn thuộc Kho bán hàng. */
public class SalesStockAdjustmentFormTest extends SalesStockAdjustmentTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(SalesStockAdjustmentFormTest.class,
                "Kho bán hàng", "Form Điều chỉnh tồn");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_091)
    public void opensWithSalesWarehouseScopeAndEmptyState() {
        var form = adjustmentPage.salesFormSnapshot();
        Assert.assertEquals(form.date(), LocalDate.now().toString(),
                "Ngày điều chỉnh mặc định không phải ngày hiện tại.");
        Assert.assertTrue(form.dateRequired(), "Ngày điều chỉnh chưa bắt buộc.");
        Assert.assertEquals(form.reason(), "", "Lý do không để trống mặc định.");
        Assert.assertTrue(form.lotCombobox(), "Thiếu ô thêm lô cần điều chỉnh.");
        Assert.assertEquals(form.changedLots(), 0, "Số lô thay đổi ban đầu không bằng 0.");
        Assert.assertEquals(form.totalLots(), 0, "Tổng lô ban đầu không bằng 0.");
        Assert.assertTrue(form.confirmDisabled(), "Chưa chọn lô nhưng nút xác nhận vẫn bật.");
        Assert.assertTrue(form.salesWarehouseScope(),
                "Thiếu mô tả kiểm kê Kho bán hàng, không ảnh hưởng Kho tổng.");
        Assert.assertTrue(form.emptyState(), "Thiếu trạng thái chưa có lô ban đầu.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_092)
    public void cancelClosesForm() {
        Assert.assertTrue(adjustmentPage.cancelForm(), "Bấm Hủy nhưng popup chưa đóng.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_093)
    public void closeButtonClosesForm() {
        Assert.assertTrue(adjustmentPage.closeForm(), "Bấm dấu X nhưng popup chưa đóng.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "form", "search"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_094)
    public void unknownLotShowsNoOption() {
        var result = adjustmentPage.searchUnknownLot();
        Assert.assertEquals(result.keyword(), "__automation_lot_not_found__");
        Assert.assertTrue(result.options().isEmpty(), "Mã lô không tồn tại vẫn trả gợi ý.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "form", "search"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_095)
    public void searchesLotsByProductName() {
        var result = adjustmentPage.searchLotsByProductName();
        Assert.assertFalse(result.options().isEmpty(), "Tìm theo tên sản phẩm không trả lô.");
        Assert.assertTrue(result.options().stream().allMatch(option -> option.contains("Áo thun Media")),
                "Gợi ý trả lẫn sản phẩm không khớp tên tìm kiếm.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_096)
    public void selectedLotCannotBeAddedTwice() {
        var result = adjustmentPage.selectedLotIsExcludedFromSuggestions();
        Assert.assertTrue(result.options().stream().noneMatch(option -> option.contains(result.selectedCode())),
                "Lô đã chọn vẫn xuất hiện trong gợi ý.");
        Assert.assertEquals(result.rowCount(), 1, "Tìm lại lô làm phát sinh dòng trùng.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_097)
    public void acceptsManualDateAndReason() {
        var result = adjustmentPage.enterManualDateAndReason();
        Assert.assertEquals(result.actualDate(), result.expectedDate(), "Ngày điều chỉnh thủ công không được giữ đúng.");
        Assert.assertEquals(result.actualReason(), result.expectedReason(), "Lý do điều chỉnh không được giữ đúng.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_098)
    public void addsTwoDifferentLots() {
        var result = adjustmentPage.addTwoDifferentLots();
        Assert.assertNotEquals(result.first().code(), result.second().code(), "Hai lần thêm trả cùng một mã lô.");
        Assert.assertEquals(result.rowCount(), 2, "Form không có đủ hai dòng lô.");
        Assert.assertEquals(result.counter().total(), 2, "Bộ đếm tổng lô không bằng 2.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_099)
    public void removingLotRestoresEmptyForm() {
        var result = adjustmentPage.addAndRemoveLot();
        Assert.assertEquals(result.rowCount(), 0, "Xóa nhưng dòng lô vẫn còn.");
        Assert.assertEquals(result.counter().total(), 0, "Bộ đếm tổng lô chưa về 0.");
        Assert.assertTrue(result.confirmDisabled(), "Nút xác nhận chưa bị khóa lại.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "stock-adjustment", "form", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_108)
    public void longLotListCanBeScrolledAndObserved() {
        var result = adjustmentPage.addManyLotsForScrolling();
        Assert.assertTrue(result.lots().size() > 5, "Dữ liệu chưa đủ tạo danh sách dài cần cuộn.");
        Assert.assertEquals(result.rowCount(), result.lots().size(), "Form không hiển thị đủ lô đã chọn.");
        Assert.assertEquals(result.counter().total(), result.lots().size(), "Bộ đếm tổng lô không khớp danh sách.");
    }
}
