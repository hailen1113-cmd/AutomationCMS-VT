package com.vuatho.tests.uniform.inventory.stock;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformInventoryStockTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;

/** Testcase drawer chi tiết và chế độ sửa của một lô tồn kho thật. */
public class StockDetailTest
        extends UniformInventoryStockTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockDetailTest.class,
                "Kho Đồng phục", "Chi tiết lô Tồn kho");
    }

    /** Dòng được bấm và drawer phải cùng mã, tên sản phẩm và số tồn. */
    @Test(groups = {"uniform", "inventory", "stock", "detail", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_013)
    public void detailMatchesSelectedStockRow() {
        var detail = inventoryPage.openFirstLotDetail();
        Assert.assertNotNull(detail.expected(),
                "Không có lô dữ liệu để mở chi tiết.");
        Assert.assertTrue(detail.ariaLabel().contains(detail.expected().code()),
                "Tiêu đề drawer không khớp mã lô.");
        String normalized = TextNormalizer.normalize(detail.content());
        Assert.assertTrue(normalized.contains(
                        TextNormalizer.normalize(detail.expected().name())),
                "Chi tiết không khớp tên sản phẩm của dòng đã chọn.");
        Assert.assertTrue(normalized.contains("ton kho tong"),
                "Chi tiết thiếu số tồn kho tổng.");
        Assert.assertTrue(detail.editButton(), "Chi tiết thiếu nút Sửa.");
    }

    /** Lô có dữ liệu thật phải hiển thị các phiếu biến động bên trong drawer. */
    @Test(groups = {"uniform", "inventory", "stock", "history", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_014)
    public void detailShowsMovementHistory() {
        var detail = inventoryPage.openFirstLotDetailAndObserveHistory();
        Assert.assertNotNull(detail.expected(),
                "Không có lô dữ liệu để mở lịch sử.");
        Assert.assertTrue(detail.historyCount() > 0,
                "Lô được chọn không có lịch sử biến động để kiểm tra.");
        String normalized = TextNormalizer.normalize(detail.content());
        Assert.assertTrue(normalized.contains("nhap kho")
                        || normalized.contains("xuat chuyen kho"),
                "Lịch sử không hiển thị loại biến động nhập/xuất.");
    }

    /** Bấm Sửa chỉ đọc form và đối chiếu dữ liệu, không lưu thay đổi. */
    @Test(groups = {"uniform", "inventory", "stock", "edit", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_015)
    public void editFormLoadsCurrentLotData() {
        var edit = inventoryPage.openEditForm();
        Assert.assertNotNull(edit.expected(),
                "Không có lô dữ liệu để mở form sửa.");
        Assert.assertEquals(edit.codeInput(), edit.expected().code(),
                "Form sửa không tải đúng mã lô hiện tại.");
        Assert.assertEquals(edit.inputCount(), 1,
                "Chế độ sửa phải chỉ có một ô nhập Mã lô.");
        Assert.assertTrue(edit.codeInputLabel(),
                "Ô nhập sửa không có aria-label Mã lô đúng element.");
        Assert.assertTrue(edit.cancelEditButton() && edit.saveButton(),
                "Form sửa thiếu nút Huỷ sửa hoặc Lưu thay đổi.");
        Assert.assertTrue(edit.noVariantNotice(),
                "Form sửa thiếu thông báo sản phẩm không có biến thể để sửa.");
        Assert.assertTrue(edit.readOnlyBusinessFields(),
                "Chế độ sửa không giữ đủ giá nhập, ngày nhập, người tạo và tồn kho.");
        Assert.assertTrue(edit.movementHistory(),
                "Chế độ sửa không còn hiển thị lịch sử biến động.");
    }

    /** Huỷ sửa phải trở lại chi tiết, không submit thay đổi nào. */
    @Test(groups = {"uniform", "inventory", "stock", "edit", "cancel",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_016)
    public void cancelEditReturnsToUnchangedDetail() {
        var result = inventoryPage.cancelEdit();
        Assert.assertTrue(result.detailStillOpen(),
                "Huỷ sửa làm đóng cả drawer chi tiết.");
        Assert.assertTrue(result.editInputClosed(),
                "Huỷ sửa nhưng form nhập vẫn còn hiển thị.");
        Assert.assertTrue(TextNormalizer.normalize(result.detailContent())
                        .contains("lich su bien dong"),
                "Huỷ sửa không trở lại nội dung chi tiết lô.");
    }

    /** Nút X đóng drawer và dữ liệu bảng vẫn còn nguyên. */
    @Test(groups = {"uniform", "inventory", "stock", "detail", "close",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_017)
    public void headerCloseReturnsToStockList() {
        Assert.assertTrue(inventoryPage.closeDetailDrawer(),
                "Đóng drawer nhưng danh sách tồn kho chưa hiển thị lại.");
    }

    @Test(groups = {"uniform", "inventory", "stock", "detail", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_DETAIL_066)
    public void detailShowsValidImportInformation() {
        var detail = inventoryPage.openFirstLotBusinessDetail();
        Assert.assertNotNull(detail.detail().expected(), "Không có lô để kiểm tra.");
        Assert.assertTrue(detail.importPrice() > 0, "Giá nhập không hợp lệ.");
        Assert.assertTrue(detail.importDate().matches("\\d{2}/\\d{2}/\\d{4}"),
                "Ngày nhập không đúng định dạng.");
        Assert.assertTrue(detail.creatorField(), "Thiếu trường Tạo bởi.");
    }

    @Test(groups = {"uniform", "inventory", "stock", "detail", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_DETAIL_067)
    public void detailStockMatchesSelectedRow() {
        var detail = inventoryPage.openFirstLotBusinessDetail();
        Assert.assertEquals(detail.displayedStock(), detail.detail().expected().stock(),
                "Tồn kho trong drawer không khớp dòng được chọn.");
    }

    @Test(groups = {"uniform", "inventory", "stock", "detail", "history"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_DETAIL_068)
    public void historyCountAndEntriesAreValid() {
        var detail = inventoryPage.openFirstLotBusinessDetail();
        Assert.assertFalse(detail.movements().isEmpty(), "Lịch sử không có dữ liệu.");
        Assert.assertEquals(detail.movements().size(), detail.detail().historyCount(),
                "Số dòng lịch sử không khớp số ghi trên tiêu đề.");
        Assert.assertEquals(new HashSet<>(detail.movements().stream()
                        .map(entry -> entry.code()).toList()).size(),
                detail.movements().size(), "Lịch sử xuất hiện mã phiếu trùng.");
        for (var entry : detail.movements()) {
            Assert.assertTrue(entry.code().matches("[A-ZĐ]{2,3}-\\d{4}-\\d{3,}"),
                    "Mã phiếu lịch sử không hợp lệ: " + entry.code());
            Assert.assertFalse(entry.type().isBlank(), "Phiếu thiếu loại biến động.");
            Assert.assertFalse(entry.operator().isBlank(), "Phiếu thiếu người thao tác.");
            Assert.assertTrue(entry.date().matches("\\d{2}/\\d{2}/\\d{4}"),
                    "Ngày biến động không hợp lệ.");
            Assert.assertTrue(entry.quantity() > 0, "Số lượng biến động không hợp lệ.");
        }
    }

    @Test(groups = {"uniform", "inventory", "stock", "detail", "history", "sorting"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_DETAIL_069)
    public void historyIsSortedNewestFirst() {
        var movements = inventoryPage.openFirstLotBusinessDetail().movements();
        Assert.assertTrue(movements.size() > 1,
                "Cần ít nhất hai phiếu để kiểm tra thứ tự.");
        for (int index = 1; index < movements.size(); index++) {
            Assert.assertFalse(movements.get(index).parsedDate()
                            .isAfter(movements.get(index - 1).parsedDate()),
                    "Lịch sử không được sắp xếp mới nhất trước.");
        }
    }

    @Test(groups = {"uniform", "inventory", "stock", "detail", "history", "summary"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_DETAIL_070)
    public void movementNetMatchesCurrentStock() {
        var detail = inventoryPage.openFirstLotBusinessDetail();
        Assert.assertEquals(detail.netMovement(), detail.displayedStock(),
                "Tổng nhập trừ xuất không khớp tồn kho hiện tại.");
    }

    /** Sửa mã lô thật, kiểm tra dữ liệu đã lưu rồi khôi phục mã ban đầu. */
    @Test(groups = {"uniform", "inventory", "stock", "detail", "edit",
            "mutation", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_DETAIL_071)
    public void updatesLotCodeAndRestoresOriginalData() {
        var result = inventoryPage.updateLotCodeAndRestore();
        Assert.assertFalse(result.originalCode().isBlank(),
                "Không có lô dữ liệu để thực hiện sửa.");
        Assert.assertTrue(result.temporarySaved(),
                "Mã lô tạm chưa được lưu thành công: "
                        + result.temporaryCode());
        Assert.assertTrue(result.originalRestored(),
                "Đã sửa mã lô nhưng chưa khôi phục được mã ban đầu: "
                        + result.originalCode());
    }
}
