package com.vuatho.tests.uniform.inventory.stockreceipt;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.StockReceiptTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Testcase validation các trường và tổng tiền của form Nhập kho tổng. */
public class StockReceiptValidationTest extends StockReceiptTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockReceiptValidationTest.class,
                "Kho Đồng phục", "Validation Nhập kho tổng");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_082)
    public void importDateIsRequired() {
        var result = receiptPage.clearRequiredDate();
        Assert.assertEquals(result.actualValue(), "", "Không xóa được ngày nhập.");
        Assert.assertTrue(result.submissionBlocked(),
                "Thiếu ngày nhập nhưng hệ thống vẫn cho đi đến bước xác nhận.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_083)
    public void lotCodeIsRequired() {
        var result = receiptPage.leaveLotCodeBlank();
        Assert.assertEquals(result.actualValue(), "", "Mã lô chưa được xóa trống.");
        Assert.assertTrue(result.summary().validLots() < result.summary().totalLots());
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_084)
    public void acceptsPositiveIntegerQuantity() {
        var result = receiptPage.acceptsPositiveIntegerQuantity();
        Assert.assertEquals(result.actualValue(), "2");
        Assert.assertEquals(result.summary().validLots(), result.summary().totalLots());
        Assert.assertTrue(result.submitEnabled(),
                "Dữ liệu hợp lệ nhưng nút Nhập kho tổng chưa bật.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_085)
    public void rejectsInvalidQuantityFormats() {
        var results = receiptPage.invalidQuantityFormats();
        Assert.assertEquals(results.size(), 4);
        for (var result : results) {
            Assert.assertTrue(!result.actualValue().equals(result.attemptedValue())
                            || result.summary().validLots() < result.summary().totalLots()
                            || !result.submitEnabled(),
                    "Form chấp nhận số lượng không hợp lệ: " + result.attemptedValue());
        }
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_086)
    public void priceIsRequiredAndNumeric() {
        var results = receiptPage.invalidPriceFormats();
        Assert.assertEquals(results.size(), 3);
        for (var result : results) {
            Assert.assertTrue(!result.actualValue().equals(result.attemptedValue())
                            || result.summary().validLots() < result.summary().totalLots()
                            || !result.submitEnabled(),
                    "Form chấp nhận giá nhập không hợp lệ: " + result.attemptedValue());
        }
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_087)
    public void duplicateLotCodesAreBlocked() {
        var result = receiptPage.duplicateLotCodesAreBlocked();
        Assert.assertTrue(result.summary().validLots() < result.summary().totalLots()
                        || !result.submitEnabled(),
                "Hai dòng trùng mã lô vẫn được xác định hợp lệ.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_088)
    public void everyRowMustBeValidBeforeSubmission() {
        var result = receiptPage.allRowsMustBeValid();
        Assert.assertFalse(result.enabledBeforeAllRows(),
                "Còn dòng trống nhưng nút Nhập kho tổng đã bật.");
        Assert.assertTrue(result.enabledAfterAllRows(),
                "Đã nhập đủ tất cả dòng nhưng nút chưa bật.");
        Assert.assertEquals(result.completedSummary().validLots(),
                result.completedSummary().totalLots());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_089)
    public void totalsAreCalculatedCorrectly() {
        var result = receiptPage.calculatedTotals();
        Assert.assertEquals(result.summary().totalLots(), result.rowCount());
        Assert.assertEquals(result.summary().validLots(), result.rowCount());
        Assert.assertEquals(result.summary().totalQuantity(), result.expectedQuantity());
        Assert.assertEquals(result.summary().totalAmount(), result.expectedAmount());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_090)
    public void noteIsOptional() {
        var result = receiptPage.blankNoteIsAccepted();
        Assert.assertEquals(result.actualValue(), "");
        Assert.assertEquals(result.summary().validLots(), result.summary().totalLots());
        Assert.assertTrue(result.submitEnabled(),
                "Để trống ghi chú làm nút Nhập kho tổng bị khóa.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_102)
    public void whitespaceOnlyLotCodeIsInvalid() {
        var result = receiptPage.whitespaceLotCodeIsBlocked();
        Assert.assertTrue(result.summary().validLots() < result.summary().totalLots()
                        || !result.submitEnabled(),
                "Mã lô chỉ có khoảng trắng vẫn được xem là hợp lệ.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_103)
    public void zeroPriceIsValidAndDecimalPriceIsInvalid() {
        var results = receiptPage.zeroPriceAndDecimalPriceResults();
        Assert.assertEquals(results.size(), 2);
        var zero = results.get(0);
        Assert.assertEquals(zero.actualValue(), "0");
        Assert.assertEquals(zero.summary().validLots(), zero.summary().totalLots(),
                "Giá nhập 0 không được xác định hợp lệ.");
        Assert.assertTrue(zero.submitEnabled(), "Giá nhập 0 làm nút xác nhận bị khóa.");

        var decimal = results.get(1);
        Assert.assertTrue(decimal.summary().validLots() < decimal.summary().totalLots()
                        || !decimal.submitEnabled()
                        || !decimal.actualValue().equals(decimal.attemptedValue()),
                "Form chấp nhận giá nhập thập phân: " + decimal.attemptedValue());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "stock-receipt-additional"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_104)
    public void longUnicodeNoteIsPreserved() {
        var result = receiptPage.acceptsLongUnicodeNote();
        Assert.assertEquals(result.actualValue(), result.expectedValue(),
                "Ghi chú dài có tiếng Việt/ký tự đặc biệt bị thay đổi.");
        Assert.assertTrue(result.maximumLength() == null || result.maximumLength().isBlank(),
                "Element phát sinh giới hạn maxlength ngoài mong đợi.");
        Assert.assertTrue(result.submitEnabled(),
                "Ghi chú hợp lệ làm nút Nhập kho tổng bị khóa.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "boundary"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_127)
    public void futureImportDateIsAccepted() {
        var result = receiptPage.acceptsFutureImportDate();
        Assert.assertEquals(result.actualValue(), result.attemptedValue(),
                "Ngày nhập tương lai bị thay đổi hoặc không được nhận.");
        Assert.assertEquals(result.summary().validLots(), result.summary().totalLots());
        Assert.assertTrue(result.submitEnabled(),
                "Ngày nhập tương lai hợp lệ nhưng nút Nhập kho tổng bị khóa.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "boundary"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_128)
    public void longLotCodeIsAcceptedWithoutTruncation() {
        var result = receiptPage.acceptsLongLotCode();
        Assert.assertEquals(result.actualValue(), result.expectedValue(),
                "Mã lô dài bị giới hạn hoặc cắt bớt.");
        Assert.assertTrue(result.maximumLength() == null || result.maximumLength().isBlank(),
                "Element mã lô phát sinh giới hạn maxlength ngoài yêu cầu.");
        Assert.assertEquals(result.summary().validLots(), result.summary().totalLots());
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "boundary"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_129)
    public void unicodeAndSpecialCharactersLotCodeIsAccepted() {
        var result = receiptPage.acceptsUnicodeAndSpecialLotCode();
        Assert.assertEquals(result.actualValue(), result.attemptedValue(),
                "Mã lô Unicode hoặc ký tự đặc biệt bị thay đổi.");
        Assert.assertEquals(result.summary().validLots(), result.summary().totalLots());
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "security"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_130)
    public void htmlLikeLotCodeIsHandledAsPlainText() {
        var result = receiptPage.htmlLikeLotCodeRemainsPlainText();
        Assert.assertTrue(result.actualValue().equalsIgnoreCase(result.expectedValue()),
                "Nội dung giống HTML bị thay đổi ngoài quy tắc chuẩn hóa chữ hoa. expected ["
                        + result.expectedValue() + "] but found [" + result.actualValue() + "]");
        Assert.assertFalse(result.scriptExecuted(),
                "Nội dung nhập trong mã lô đã bị thực thi như mã JavaScript.");
        Assert.assertEquals(result.summary().validLots(), result.summary().totalLots());
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "duplicate"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_131)
    public void duplicateLotCodesIgnoringCaseAreBlocked() {
        var result = receiptPage.duplicateLotCodesIgnoringCaseAreBlocked();
        Assert.assertTrue(result.summary().validLots() < result.summary().totalLots()
                        || !result.submitEnabled(),
                "Hai mã lô chỉ khác chữ hoa chữ thường vẫn được xem là hợp lệ.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "duplicate"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_132)
    public void duplicateLotCodesIgnoringOuterWhitespaceAreBlocked() {
        var result = receiptPage.duplicateLotCodesIgnoringOuterWhitespaceAreBlocked();
        Assert.assertTrue(result.summary().validLots() < result.summary().totalLots()
                        || !result.submitEnabled(),
                "Hai mã lô chỉ khác khoảng trắng bao quanh vẫn được xem là hợp lệ.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "recovery"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_133)
    public void correctingDuplicateLotRestoresValidForm() {
        var result = receiptPage.correctingDuplicateLotRestoresValidity();
        Assert.assertFalse(result.enabledWhileInvalid(),
                "Nút nhập kho vẫn bật khi hai dòng đang trùng mã lô.");
        Assert.assertEquals(result.actualValue(), result.expectedValue());
        Assert.assertEquals(result.correctedSummary().validLots(),
                result.correctedSummary().totalLots());
        Assert.assertTrue(result.enabledAfterCorrection(),
                "Sửa mã lô trùng thành mã khác nhưng form chưa hợp lệ trở lại.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "recovery"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_134)
    public void correctingInvalidQuantityAndPriceRestoresValidForm() {
        var result = receiptPage.correctingInvalidNumbersRestoresValidity();
        Assert.assertFalse(result.enabledWhileInvalid(),
                "Nút nhập kho vẫn bật khi số lượng và giá đang sai.");
        Assert.assertEquals(result.actualValue().replace(",", ""), result.expectedValue(),
                "Giá trị sau khi sửa không đúng sau khi bỏ dấu phân cách hàng nghìn.");
        Assert.assertEquals(result.correctedSummary().validLots(),
                result.correctedSummary().totalLots());
        Assert.assertTrue(result.enabledAfterCorrection(),
                "Sửa số lượng và giá hợp lệ nhưng form chưa được khôi phục.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "recovery", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_135)
    public void multipleInvalidRowsMustAllBeCorrected() {
        var result = receiptPage.correctsMultipleInvalidRowsOneByOne();
        Assert.assertFalse(result.enabledWithTwoErrors());
        Assert.assertFalse(result.enabledWithOneError(),
                "Còn một dòng lỗi nhưng nút Nhập kho tổng đã bật.");
        Assert.assertEquals(result.summary().validLots(), result.summary().totalLots());
        Assert.assertTrue(result.enabledAfterAllCorrections(),
                "Đã sửa hết các dòng lỗi nhưng nút Nhập kho tổng chưa bật.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "boundary", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_136)
    public void largeQuantityAndPriceDoNotOverflowTotals() {
        var result = receiptPage.largeQuantityAndPriceTotals();
        Assert.assertEquals(result.summary().validLots(), result.rowCount());
        Assert.assertEquals(result.summary().totalQuantity(), result.expectedQuantity());
        Assert.assertEquals(result.summary().totalAmount(), result.expectedAmount(),
                "Tổng tiền bị sai hoặc tràn số với số lượng và giá lớn.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "required"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_137)
    public void quantityIsRequired() {
        var result = receiptPage.leaveQuantityBlank();
        Assert.assertEquals(result.actualValue(), "",
                "Không xóa được giá trị số lượng.");
        Assert.assertTrue(result.summary().validLots() < result.summary().totalLots(),
                "Dòng bỏ trống số lượng vẫn được tính là lô hợp lệ.");
        Assert.assertEquals(result.summary().totalQuantity(), 0,
                "Số lượng tổng chưa trở về 0 sau khi bỏ trống số lượng.");
        Assert.assertFalse(result.submitEnabled(),
                "Bỏ trống số lượng nhưng nút Nhập kho tổng vẫn được bật.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "required", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_138)
    public void allRequiredFieldsBlankBlocksSubmission() {
        var result = receiptPage.leaveAllRequiredFieldsBlank();
        Assert.assertEquals(result.date(), "");
        Assert.assertEquals(result.lotCode(), "");
        Assert.assertEquals(result.quantity(), "");
        Assert.assertEquals(result.price(), "");
        Assert.assertEquals(result.summary().validLots(), 0,
                "Vẫn còn lô hợp lệ khi toàn bộ trường bắt buộc đã bị xóa.");
        Assert.assertFalse(result.submitEnabled(),
                "Bỏ trống toàn bộ trường bắt buộc nhưng nút Nhập kho vẫn bật.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "required"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_139)
    public void whitespaceOnlyQuantityIsInvalid() {
        var result = receiptPage.whitespaceQuantityIsBlocked();
        Assert.assertTrue(result.actualValue().isBlank(),
                "Input số lượng không giữ hoặc chuẩn hóa khoảng trắng về rỗng.");
        Assert.assertTrue(result.summary().validLots() < result.summary().totalLots());
        Assert.assertFalse(result.submitEnabled(),
                "Số lượng chỉ có khoảng trắng nhưng nút Nhập kho vẫn bật.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "required"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_140)
    public void whitespaceOnlyPriceIsInvalid() {
        var result = receiptPage.whitespacePriceIsBlocked();
        Assert.assertTrue(result.actualValue().isBlank(),
                "Input giá nhập không giữ hoặc chuẩn hóa khoảng trắng về rỗng.");
        Assert.assertTrue(result.summary().validLots() < result.summary().totalLots());
        Assert.assertFalse(result.submitEnabled(),
                "Giá nhập chỉ có khoảng trắng nhưng nút Nhập kho vẫn bật.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "validation", "required", "recovery", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_141)
    public void restoringAllRequiredFieldsEnablesSubmissionOnlyAtTheEnd() {
        var result = receiptPage.restoreAllRequiredFieldsOneByOne();
        Assert.assertFalse(result.enabledBeforeRecovery());
        Assert.assertFalse(result.enabledAfterDate(),
                "Mới điền ngày nhưng nút Nhập kho đã bật.");
        Assert.assertFalse(result.enabledAfterLot(),
                "Còn thiếu số lượng và giá nhưng nút Nhập kho đã bật.");
        Assert.assertFalse(result.enabledAfterQuantity(),
                "Còn thiếu giá nhập nhưng nút Nhập kho đã bật.");
        Assert.assertEquals(result.summary().validLots(), result.summary().totalLots());
        Assert.assertTrue(result.enabledAfterPrice(),
                "Đã điền đủ trường bắt buộc nhưng nút Nhập kho chưa bật.");
    }
}
