package com.vuatho.tests.uniform.inventory.stockreceipt;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.StockReceiptTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.time.LocalDate;

/** Testcase cấu trúc, tìm kiếm và thao tác dòng trên form Nhập kho tổng. */
public class StockReceiptFormTest extends StockReceiptTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockReceiptFormTest.class,
                "Kho Đồng phục", "Form Nhập kho tổng");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_072)
    public void opensWithValidDefaultState() {
        var form = receiptPage.formSnapshot();
        Assert.assertEquals(form.date(), LocalDate.now().toString(),
                "Ngày nhập mặc định không phải ngày hiện tại.");
        Assert.assertTrue(form.dateRequired(), "Ngày nhập chưa được đánh dấu bắt buộc.");
        Assert.assertEquals(form.note(), "", "Ghi chú không để trống mặc định.");
        Assert.assertTrue(form.productCombo(), "Thiếu ô tìm và thêm sản phẩm.");
        Assert.assertEquals(form.productCount(), 0, "Form mới đã có sản phẩm.");
        Assert.assertEquals(form.rowCount(), 0, "Form mới đã có dòng lô.");
        Assert.assertEquals(form.summary().validLots(), 0);
        Assert.assertEquals(form.summary().totalLots(), 0);
        Assert.assertEquals(form.summary().totalQuantity(), 0);
        Assert.assertEquals(form.summary().totalAmount(), 0L);
        Assert.assertTrue(form.submitDisabled(), "Nút Nhập kho tổng chưa bị khóa.");
        Assert.assertTrue(form.emptyState(), "Thiếu thông báo form chưa có sản phẩm.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_073)
    public void cancelClosesForm() {
        Assert.assertTrue(receiptPage.cancelForm(), "Bấm Hủy nhưng form chưa đóng.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_074)
    public void closeButtonClosesForm() {
        Assert.assertTrue(receiptPage.closeForm(), "Bấm dấu X nhưng form chưa đóng.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "search"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_075)
    public void unknownProductShowsNoSuggestion() {
        var result = receiptPage.searchUnknownProduct();
        Assert.assertEquals(result.actualValue(), result.keyword(),
                "Ô tìm kiếm không giữ đúng từ khóa đã nhập.");
        Assert.assertTrue(result.options().isEmpty(),
                "Sản phẩm không tồn tại vẫn trả gợi ý.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_076)
    public void addingProductCreatesReceiptRows() {
        var result = receiptPage.addFirstProduct();
        Assert.assertFalse(result.name().isBlank(), "Không đọc được tên sản phẩm đã thêm.");
        Assert.assertTrue(result.addedRows() > 0,
                "Thêm sản phẩm nhưng không tạo dòng mã lô/số lượng/giá.");
        Assert.assertEquals(result.productCount(), 1);
        Assert.assertEquals(result.totalRows(), result.addedRows());
        Assert.assertEquals(result.summary().totalLots(), result.totalRows(),
                "Bộ đếm tổng lô không khớp số dòng sản phẩm.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_077)
    public void addsTwoDifferentProducts() {
        var result = receiptPage.addTwoProducts();
        Assert.assertNotEquals(result.first().name(), result.second().name(),
                "Hai lần thêm trả cùng một sản phẩm.");
        Assert.assertEquals(result.productCount(), 2);
        Assert.assertTrue(result.rowCount() >= 2,
                "Hai sản phẩm không tạo đủ dòng nhập kho.");
        Assert.assertEquals(result.summary().totalLots(), result.rowCount());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_078)
    public void removingProductRestoresEmptyForm() {
        var result = receiptPage.addAndRemoveProduct();
        Assert.assertEquals(result.productCount(), 0);
        Assert.assertEquals(result.rowCount(), 0);
        Assert.assertEquals(result.summary().totalLots(), 0);
        Assert.assertTrue(result.submitDisabled());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_079)
    public void removingVariantUpdatesLotCount() {
        var result = receiptPage.removeOneVariantRow();
        Assert.assertEquals(result.afterRows(), result.beforeRows() - 1,
                "Xóa biến thể nhưng số dòng không giảm một.");
        Assert.assertEquals(result.summary().totalLots(), result.afterRows(),
                "Bộ đếm tổng lô chưa cập nhật sau khi xóa biến thể.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "search"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_080)
    public void selectedProductCannotBeAddedTwice() {
        var result = receiptPage.selectedProductIsExcluded();
        Assert.assertTrue(result.options().stream()
                        .noneMatch(option -> option.equalsIgnoreCase(result.selectedName())),
                "Sản phẩm đã chọn vẫn xuất hiện trong gợi ý.");
        Assert.assertEquals(result.productCount(), 1,
                "Tìm lại sản phẩm làm phát sinh sản phẩm trùng.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_081)
    public void acceptsManualDateAndNote() {
        var result = receiptPage.enterManualDateAndNote();
        Assert.assertEquals(result.actualDate(), result.expectedDate());
        Assert.assertEquals(result.actualNote(), result.expectedNote());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "search", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_092)
    public void scrollsToBottomAndSelectsLowerProduct() {
        var result = receiptPage.scrollToBottomAndSelectProduct();
        Assert.assertTrue(result.maximumScroll() > 0,
                "Danh sách sản phẩm không phát sinh vùng cuộn.");
        Assert.assertFalse(result.selectedName().isBlank(),
                "Không đọc được sản phẩm phía dưới đã chọn.");
        Assert.assertEquals(result.productCount(), 1,
                "Chọn option phía dưới nhưng sản phẩm không được thêm.");
        Assert.assertTrue(result.rowCount() > 0,
                "Sản phẩm phía dưới không tạo dòng nhập kho.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "search", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_093)
    public void scrollingDownAndBackUpKeepsProductList() {
        var result = receiptPage.scrollProductListDownAndBackUp();
        Assert.assertTrue(result.maximumScroll() > 0,
                "Danh sách sản phẩm không phát sinh vùng cuộn.");
        Assert.assertTrue(result.bottomPosition() > 0,
                "Danh sách chưa cuộn xuống phía dưới.");
        Assert.assertTrue(result.returnedPosition() <= 2,
                "Danh sách chưa cuộn trở lại vị trí đầu.");
        Assert.assertTrue(result.listStillOpen(),
                "Danh sách gợi ý bị đóng sau khi cuộn.");
        Assert.assertEquals(result.returnedOptionCount(), result.initialOptionCount(),
                "Số option thay đổi sau khi cuộn xuống và lên.");
        Assert.assertEquals(result.returnedInputValue(), result.initialInputValue(),
                "Cuộn danh sách làm thay đổi nội dung ô tìm kiếm.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "search", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_094)
    public void searchesAndSelectsExistingProduct() {
        var result = receiptPage.searchAndSelectExistingProduct();
        Assert.assertTrue(result.optionCount() > 0, "Từ khóa có dữ liệu nhưng không trả gợi ý.");
        Assert.assertFalse(result.selectedName().isBlank());
        Assert.assertEquals(result.productCount(), 1);
        Assert.assertTrue(result.rowCount() > 0);
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "search", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_095)
    public void clearingSearchRestoresSuggestions() {
        var result = receiptPage.clearSearchRestoresSuggestions();
        Assert.assertTrue(result.initialOptionCount() > 0);
        Assert.assertTrue(result.restoredOptionCount() > 0,
                "Xóa từ khóa nhưng danh sách gợi ý không xuất hiện lại.");
        Assert.assertEquals(result.inputValue(), "");
        Assert.assertTrue(result.suggestionsVisible());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "search", "keyboard", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_096)
    public void selectsProductUsingKeyboard() {
        var result = receiptPage.selectProductWithKeyboard();
        Assert.assertTrue(result.optionCount() > 0);
        Assert.assertEquals(result.productCount(), 1,
                "Nhấn Arrow Down và Enter nhưng sản phẩm chưa được thêm.");
        Assert.assertTrue(result.rowCount() > 0);
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form", "variant", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_097)
    public void productWithoutVariantCreatesOneRow() {
        var result = receiptPage.addProductWithoutVariant();
        Assert.assertFalse(result.selectedName().isBlank());
        Assert.assertEquals(result.rowCount(), 1,
                "Sản phẩm không biến thể không tạo đúng một dòng.");
        Assert.assertTrue(result.metadataVisible(),
                "Card không hiển thị nhãn Không biến thể.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form", "variant", "scroll", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_098)
    public void multiVariantProductShowsAllRowsAndMetadata() {
        var result = receiptPage.addProductWithMultipleVariants();
        if (result.selectedName().isBlank()) {
            throw new SkipException(
                    "Không có sản phẩm từ hai biến thể trở lên trong dữ liệu hiện tại.");
        }
        Assert.assertFalse(result.selectedName().isBlank());
        Assert.assertTrue(result.rowCount() >= 2,
                "Sản phẩm nhiều biến thể không hiển thị đủ dòng.");
        Assert.assertTrue(result.metadataVisible(),
                "Các dòng không hiển thị màu sắc/kích thước.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form", "old-lot", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_099)
    public void oldLotAutofillsCodeAndPrice() {
        var result = receiptPage.selectOldLotAutofillsValues();
        Assert.assertEquals(result.actualCode(), result.expectedCode(),
                "Chọn lô cũ nhưng mã lô không được tự điền.");
        Assert.assertFalse(result.actualPriceText().isBlank(),
                "Chọn lô cũ nhưng giá nhập không được tự điền.");
        Assert.assertEquals(result.actualPrice(), result.expectedPrice(),
                "Giá tự điền không khớp giá nhập cũ.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_100)
    public void removingOneOfTwoProductsPreservesTheOther() {
        var result = receiptPage.removeOneOfTwoProducts();
        Assert.assertEquals(result.productCount(), 1);
        Assert.assertTrue(result.rowsAfter() > 0 && result.rowsAfter() < result.rowsBefore());
        Assert.assertTrue(result.remainingVisible(), "Sản phẩm còn lại bị mất.");
        Assert.assertFalse(result.removedVisible(), "Sản phẩm đã gỡ vẫn còn hiển thị.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "form", "scroll", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_101)
    public void scrollsLongReceiptRowsDownAndBackUp() {
        var result = receiptPage.scrollLongRowListDownAndBackUp();
        Assert.assertTrue(result.maximumScroll() > 0, "Danh sách dòng nhập kho chưa có vùng cuộn.");
        Assert.assertTrue(result.bottomPosition() > 0);
        Assert.assertTrue(result.returnedPosition() <= 2);
        Assert.assertTrue(result.productCount() >= 1,
                "Chưa thêm được sản phẩm để kiểm tra danh sách dài.");
        Assert.assertTrue(result.rowCount() >= 2,
                "Chưa có đủ dòng để kiểm tra cuộn danh sách.");
    }
}
