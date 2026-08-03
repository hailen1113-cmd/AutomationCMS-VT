package com.vuatho.tests.uniform.inventory.stockadjustment;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.StockAdjustmentTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;

/** Testcase cấu trúc form và thao tác thêm/xóa lô Điều chỉnh tồn. */
public class StockAdjustmentFormTest extends StockAdjustmentTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockAdjustmentFormTest.class,
                "Kho Đồng phục", "Form Điều chỉnh tồn");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_043)
    public void opensWithValidDefaultState() {
        var form = adjustmentPage.formSnapshot();
        Assert.assertEquals(form.date(), LocalDate.now().toString(),
                "Ngày điều chỉnh mặc định không phải ngày hiện tại.");
        Assert.assertTrue(form.dateRequired(), "Ngày điều chỉnh chưa bắt buộc.");
        Assert.assertEquals(form.reason(), "", "Lý do không để trống mặc định.");
        Assert.assertTrue(form.lotCombobox(), "Thiếu ô tìm và thêm lô.");
        Assert.assertEquals(form.lotCount(), 0, "Form mới đã có lô.");
        Assert.assertEquals(form.counter().changed(), 0, "Bộ đếm thay đổi không bằng 0.");
        Assert.assertTrue(form.confirmDisabled(), "Nút xác nhận chưa bị khóa.");
        Assert.assertTrue(form.mainWarehouseNotice(), "Thiếu cảnh báo phạm vi Kho tổng.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_044)
    public void cancelClosesForm() {
        Assert.assertTrue(adjustmentPage.cancelForm(), "Bấm Hủy nhưng popup chưa đóng.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_045)
    public void closeButtonClosesForm() {
        Assert.assertTrue(adjustmentPage.closeForm(), "Bấm dấu X nhưng popup chưa đóng.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "search"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_046)
    public void searchesAndAddsExistingLot() {
        var lot = adjustmentPage.addFirstAvailableLot();
        Assert.assertTrue(lot.code().matches("VT\\d+"), "Không đọc được mã lô.");
        Assert.assertTrue(lot.currentStock() > 0, "Lô được chọn không có tồn dương.");
        Assert.assertTrue(lot.rowText().contains(lot.code()), "Dòng không chứa mã lô.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "search"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_047)
    public void unknownLotShowsNoOption() {
        var search = adjustmentPage.searchUnknownLot();
        Assert.assertEquals(search.keyword(), "__automation_lot_not_found__");
        Assert.assertTrue(search.options().isEmpty(),
                "Mã không tồn tại vẫn trả gợi ý lô.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_048)
    public void addsTwoDifferentLots() {
        var result = adjustmentPage.addTwoDifferentLots();
        Assert.assertNotEquals(result.first().code(), result.second().code(),
                "Hai lần thêm trả cùng một mã lô.");
        Assert.assertEquals(result.rowCount(), 2, "Form không có đủ hai dòng lô.");
        Assert.assertEquals(result.counter().total(), 2, "Bộ đếm tổng lô không bằng 2.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_049)
    public void removingLotRestoresEmptyForm() {
        var result = adjustmentPage.addAndRemoveLot();
        Assert.assertEquals(result.rowCount(), 0, "Xóa nhưng dòng lô vẫn còn.");
        Assert.assertEquals(result.counter().total(), 0, "Bộ đếm tổng lô chưa về 0.");
        Assert.assertTrue(result.confirmDisabled(), "Nút xác nhận chưa bị khóa lại.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "search"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_057)
    public void searchesLotsByProductName() {
        var result = adjustmentPage.searchLotsByProductName();
        Assert.assertEquals(result.keyword(), "Áo thun Media");
        Assert.assertFalse(result.options().isEmpty(),
                "Tìm theo tên sản phẩm không trả lô.");
        Assert.assertTrue(result.options().stream()
                        .allMatch(option -> option.contains("Áo thun Media")),
                "Gợi ý trả lẫn sản phẩm không khớp tên tìm kiếm.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_058)
    public void selectedLotCannotBeAddedTwice() {
        var result = adjustmentPage.selectedLotIsExcludedFromSuggestions();
        Assert.assertTrue(result.options().stream()
                        .noneMatch(option -> option.contains(result.selectedCode())),
                "Lô đã chọn vẫn xuất hiện trong gợi ý.");
        Assert.assertEquals(result.rowCount(), 1,
                "Tìm lại lô làm phát sinh thêm dòng trùng.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_059)
    public void acceptsManualDateAndReason() {
        var result = adjustmentPage.enterManualDateAndReason();
        Assert.assertEquals(result.actualDate(), result.expectedDate(),
                "Ngày điều chỉnh thủ công không được giữ đúng.");
        Assert.assertEquals(result.actualReason(), result.expectedReason(),
                "Lý do điều chỉnh thủ công không được giữ đúng.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "form", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_060)
    public void longLotListCanBeScrolledAndObserved() {
        var result = adjustmentPage.addManyLotsForScrolling();
        Assert.assertTrue(result.lots().size() > 5,
                "Dữ liệu chưa đủ tạo danh sách dài cần cuộn.");
        Assert.assertEquals(result.rowCount(), result.lots().size(),
                "Form không hiển thị đủ toàn bộ lô đã chọn.");
        Assert.assertEquals(result.lots().stream().map(lot -> lot.code()).distinct().count(),
                (long) result.lots().size(), "Danh sách dài xuất hiện mã lô trùng.");
        Assert.assertEquals(result.counter().total(), result.lots().size(),
                "Bộ đếm tổng lô không khớp danh sách dài.");
    }

    @Test(groups = {"uniform", "inventory", "stock-adjustment", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_ADJUST_061)
    public void removingMiddleLotKeepsRemainingRows() {
        var result = adjustmentPage.removeMiddleLotFromMultipleRows();
        Assert.assertEquals(result.remainingCodes().size(), 2,
                "Xóa một lô nhưng số dòng còn lại không bằng 2.");
        Assert.assertTrue(result.remainingCodes().contains(result.firstCode()),
                "Lô đầu bị mất khi xóa lô giữa.");
        Assert.assertTrue(result.remainingCodes().contains(result.lastCode()),
                "Lô cuối bị mất khi xóa lô giữa.");
        Assert.assertFalse(result.remainingCodes().contains(result.removedCode()),
                "Lô giữa vẫn còn sau khi xóa.");
        Assert.assertEquals(result.counter().total(), 2,
                "Bộ đếm tổng lô chưa cập nhật sau khi xóa.");
    }
}
