package com.vuatho.tests.uniform.inventory.salesstock.stock;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;

/** Testcase drawer chi tiết lô mở từ Kho bán hàng. */
public class SalesStockDetailTest extends SalesStockTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(SalesStockDetailTest.class, "Kho bán hàng", "Chi tiết lô Tồn kho");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_021)
    public void detailMatchesSelectedRow() {
        var detail = salesStockPage.openFirstLotDetail();
        Assert.assertNotNull(detail.expected(), "Không có lô để mở chi tiết.");
        Assert.assertTrue(detail.ariaLabel().contains(detail.expected().code()));
        Assert.assertTrue(TextNormalizer.normalize(detail.content())
                .contains(TextNormalizer.normalize(detail.expected().name())));
        Assert.assertTrue(detail.editButton(), "Chi tiết thiếu nút Sửa.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "validation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_022)
    public void detailShowsBusinessMetadata() {
        var detail = salesStockPage.openFirstLotBusinessDetail();
        Assert.assertTrue(detail.importPrice() > 0, "Giá nhập không hợp lệ.");
        Assert.assertTrue(detail.importDate().matches("\\d{2}/\\d{2}/\\d{4}"), "Ngày nhập sai định dạng.");
        Assert.assertTrue(detail.creatorField(), "Thiếu trường Tạo bởi.");
        Assert.assertTrue(TextNormalizer.normalize(detail.detail().content()).contains("ton kho tong"),
                "Drawer thiếu thông tin Tồn kho tổng.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "history", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_023)
    public void detailShowsMovementHistory() {
        var detail = salesStockPage.openFirstLotDetailAndObserveHistory();
        Assert.assertNotNull(detail.expected(), "Không có lô để xem lịch sử.");
        Assert.assertTrue(detail.historyCount() > 0, "Lô không có lịch sử biến động.");
        String content = TextNormalizer.normalize(detail.content());
        Assert.assertTrue(content.contains("nhap kho") || content.contains("xuat chuyen kho"),
                "Lịch sử thiếu loại biến động.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "edit"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_024)
    public void editFormLoadsCurrentLotCode() {
        var edit = salesStockPage.openEditForm();
        Assert.assertNotNull(edit.expected(), "Không có lô để mở form sửa.");
        Assert.assertEquals(edit.codeInput(), edit.expected().code());
        Assert.assertTrue(edit.cancelEditButton() && edit.saveButton(), "Form thiếu nút Hủy sửa hoặc Lưu thay đổi.");
        Assert.assertTrue(edit.readOnlyBusinessFields(), "Form thiếu dữ liệu nghiệp vụ chỉ đọc.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "edit", "cancel"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_025)
    public void cancelEditReturnsToDetail() {
        var result = salesStockPage.cancelEdit();
        Assert.assertTrue(result.detailStillOpen());
        Assert.assertTrue(result.editInputClosed());
        Assert.assertTrue(TextNormalizer.normalize(result.detailContent()).contains("lich su bien dong"));
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "close"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_026)
    public void closesDetailDrawer() {
        Assert.assertTrue(salesStockPage.closeDetailDrawer(),
                "Đóng drawer nhưng bảng tồn kho chưa hiển thị lại.");
    }

    /** Lưu thay đổi thật rồi trả dữ liệu về mã ban đầu để không làm bẩn sandbox. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "edit", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_027)
    public void updatesLotCodeAndRestoresOriginalData() {
        var result = salesStockPage.updateLotCodeAndRestore();
        Assert.assertFalse(result.originalCode().isBlank(), "Không có lô để thực hiện sửa.");
        Assert.assertTrue(result.temporarySaved(), "Mã lô tạm chưa được lưu thành công.");
        Assert.assertTrue(result.originalRestored(), "Chưa khôi phục được mã lô ban đầu.");
    }

    /** Cuộn đúng vùng overflow của drawer, không chỉ scroll trang phía sau. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_028)
    public void scrollsDetailToBottomAndBackTop() {
        var result = salesStockPage.scrollDetailToBottomAndBackTop();
        Assert.assertTrue(result.reachedBottom(), "Chưa cuộn được vùng nội dung drawer xuống cuối.");
        Assert.assertTrue(result.returnedTop(), "Chưa cuộn drawer trở lại đầu.");
    }

    /** Số ghi trên tiêu đề phải bằng đúng số dòng lịch sử render trong drawer. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "history"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_029)
    public void historyCountMatchesRenderedRows() {
        var detail = salesStockPage.openFirstLotBusinessDetail();
        Assert.assertEquals(detail.movements().size(), detail.detail().historyCount(),
                "Số dòng lịch sử không khớp số trên tiêu đề.");
    }

    /** Kiểm tra dữ liệu nghiệp vụ của từng phiếu thay vì chỉ kiểm tra vùng lịch sử tồn tại. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "history", "validation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_030)
    public void movementRowsContainValidData() {
        var movements = salesStockPage.openFirstLotBusinessDetail().movements();
        Assert.assertFalse(movements.isEmpty(), "Không có lịch sử để kiểm tra.");
        for (var entry : movements) {
            Assert.assertTrue(entry.code().matches("[A-ZĐ]{2,3}-\\d{4}-\\d{3,}"),
                    "Mã phiếu không hợp lệ: " + entry.code());
            Assert.assertFalse(entry.type().isBlank(), "Phiếu thiếu loại biến động.");
            Assert.assertFalse(entry.operator().isBlank(), "Phiếu thiếu người thao tác.");
            Assert.assertTrue(entry.date().matches("\\d{2}/\\d{2}/\\d{4}"), "Ngày phiếu không hợp lệ.");
            Assert.assertTrue(entry.quantity() > 0, "Số lượng biến động phải lớn hơn 0.");
        }
    }

    /** Dòng trước không được cũ hơn dòng đứng sau. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "history", "sorting"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_031)
    public void movementHistoryIsNewestFirst() {
        var movements = salesStockPage.openFirstLotBusinessDetail().movements();
        Assert.assertTrue(movements.size() > 1, "Cần ít nhất hai dòng lịch sử để kiểm tra thứ tự.");
        for (int index = 1; index < movements.size(); index++) {
            Assert.assertFalse(movements.get(index).parsedDate()
                            .isAfter(movements.get(index - 1).parsedDate()),
                    "Lịch sử chưa sắp xếp mới nhất trước.");
        }
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "history"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_032)
    public void movementHistoryHasNoDuplicateReceiptCode() {
        var movements = salesStockPage.openFirstLotBusinessDetail().movements();
        Assert.assertEquals(new HashSet<>(movements.stream().map(entry -> entry.code()).toList()).size(),
                movements.size(), "Lịch sử có mã phiếu trùng lặp.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "history", "summary"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_033)
    public void movementNetMatchesDisplayedTotalStock() {
        var detail = salesStockPage.openFirstLotBusinessDetail();
        Assert.assertEquals(detail.netMovement(), detail.displayedStock(),
                "Tổng nhập trừ xuất không khớp tồn kho tổng.");
    }

    /** Phải sửa giá trị trước khi hủy để chứng minh nút Hủy thực sự loại bỏ dữ liệu chưa lưu. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "edit", "cancel"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_034)
    public void cancelDiscardsChangedLotCode() {
        var result = salesStockPage.changeCodeThenCancel();
        Assert.assertTrue(result.originalStillDisplayed(), "Hủy sửa không khôi phục mã ban đầu.");
        Assert.assertTrue(result.changedCodeDiscarded(), "Mã chưa lưu vẫn xuất hiện sau khi hủy.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "edit", "validation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_035)
    public void cannotSaveBlankLotCode() {
        var result = salesStockPage.submitInvalidLotCode("");
        Assert.assertFalse(result.originalCode().isBlank(), "Không có lô để kiểm tra validation.");
        Assert.assertTrue(result.rejected(), "Hệ thống cho phép lưu mã lô trống.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "edit", "validation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_036)
    public void cannotSaveDuplicateLotCode() {
        var rows = salesStockPage.openStock().stockRows();
        Assert.assertTrue(rows.size() >= 2, "Không đủ hai lô để kiểm tra mã trùng.");
        String first = rows.get(0).code();
        String duplicate = rows.stream().map(row -> row.code())
                .filter(code -> !code.equals(first)).findFirst().orElseThrow();
        var result = salesStockPage.submitInvalidLotCode(duplicate);
        Assert.assertTrue(result.rejected(), "Hệ thống cho phép lưu mã lô trùng: " + duplicate);
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "detail", "close", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_037)
    public void closesDrawerFromBottomHistoryPosition() {
        Assert.assertTrue(salesStockPage.closeDetailFromBottom(),
                "Đóng drawer từ cuối lịch sử nhưng chưa trở lại bảng tồn kho.");
    }
}
