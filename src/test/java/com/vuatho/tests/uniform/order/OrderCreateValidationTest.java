package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformOrderPage;
import com.vuatho.support.UniformOrderCreateTestSupport;
import com.vuatho.testcases.UniformOrderTestCases;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Testcase validation từng trường bắt buộc của form tạo đơn. */
public class OrderCreateValidationTest
        extends UniformOrderCreateTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(OrderCreateValidationTest.class,
                "Đơn hàng Đồng phục", "Validation tạo đơn");
    }

    /** Form trống phải giữ drawer và đánh dấu các trường bắt buộc. */
    @Test(groups = {"uniform", "order", "create", "validation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_038)
    public void emptyFormShowsRequiredValidation() {
        orderPage.openCreateOrderDrawer();
        assertRequiredValidation(
                orderPage.submitCreateAndReadValidation(),
                UniformOrderPage.CreateRequiredField.PAYMENT_METHOD,
                UniformOrderPage.CreateRequiredField.COMBO,
                UniformOrderPage.CreateRequiredField.WORKER,
                UniformOrderPage.CreateRequiredField.ADDRESS);
    }

    /** Đã có thợ và địa chỉ nhưng thiếu combo vẫn không được tạo. */
    @Test(groups = {"uniform", "order", "create", "validation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_044)
    public void comboIsRequired() {
        orderPage.openCreateOrderDrawer();
        orderPage.selectCreatePaymentMethod("Chuyển khoản ngân hàng");
        orderPage.selectFirstCreateWorker();
        orderPage.fillCreateTexts("", "123 Đường Automation, TP.HCM");
        assertRequiredValidation(
                orderPage.submitCreateAndReadValidation(),
                UniformOrderPage.CreateRequiredField.COMBO);
    }

    /** Đã có combo và địa chỉ nhưng thiếu hồ sơ thợ vẫn không được tạo. */
    @Test(groups = {"uniform", "order", "create", "validation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_045)
    public void workerIsRequired() {
        orderPage.openCreateOrderDrawer();
        orderPage.selectCreatePaymentMethod("Chuyển khoản ngân hàng");
        orderPage.selectCreateComboWithEnoughStock();
        orderPage.fillCreateTexts("", "123 Đường Automation, TP.HCM");
        assertRequiredValidation(
                orderPage.submitCreateAndReadValidation(),
                UniformOrderPage.CreateRequiredField.WORKER);
    }

    /** Đã có combo và thợ nhưng bỏ trống địa chỉ vẫn không được tạo. */
    @Test(groups = {"uniform", "order", "create", "validation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_046)
    public void addressIsRequired() {
        orderPage.openCreateOrderDrawer();
        orderPage.selectCreatePaymentMethod("Chuyển khoản ngân hàng");
        orderPage.selectCreateComboWithEnoughStock();
        orderPage.selectFirstCreateWorker();
        assertRequiredValidation(
                orderPage.submitCreateAndReadValidation(),
                UniformOrderPage.CreateRequiredField.ADDRESS);
    }

    /** Đủ combo, thợ và địa chỉ nhưng thiếu phương thức vẫn phải bị chặn. */
    @Test(groups = {"uniform", "order", "create", "validation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_056)
    public void paymentMethodIsRequired() {
        orderPage.openCreateOrderDrawer();
        orderPage.selectCreateComboWithEnoughStock();
        orderPage.selectFirstCreateWorker();
        orderPage.fillCreateTexts("", "123 Đường Automation, TP.HCM");
        assertRequiredValidation(
                orderPage.submitCreateAndReadValidation(),
                UniformOrderPage.CreateRequiredField.PAYMENT_METHOD);
    }

    /** Địa chỉ chỉ có whitespace phải được xem như chưa nhập. */
    @Test(groups = {"uniform", "order", "create", "validation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_057)
    public void whitespaceOnlyAddressIsInvalid() {
        prepareValidRequiredFieldsExceptAddress();
        orderPage.fillCreateTexts("", "   \n\t   ");
        assertRequiredValidation(
                orderPage.submitCreateAndReadValidation(),
                UniformOrderPage.CreateRequiredField.ADDRESS);
    }

    /** Nhập địa chỉ hợp lệ sau lỗi phải xóa trạng thái validation của field. */
    @Test(groups = {"uniform", "order", "create", "validation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_058)
    public void validationClearsAfterCorrectingAddress() {
        prepareValidRequiredFieldsExceptAddress();
        assertRequiredValidation(
                orderPage.submitCreateAndReadValidation(),
                UniformOrderPage.CreateRequiredField.ADDRESS);
        orderPage.fillCreateTexts("", "456 Đường Automation, TP.HCM");
        Assert.assertTrue(orderPage.readCreateFieldValidation().isEmpty(),
                "Đã sửa địa chỉ hợp lệ nhưng validation chưa biến mất.");
    }

    /** Submit lỗi địa chỉ không được xóa payment, combo, thợ và ghi chú. */
    @Test(groups = {"uniform", "order", "create", "validation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_059)
    public void validationPreservesOtherValidValues() {
        orderPage.openCreateOrderDrawer();
        orderPage.selectCreatePaymentMethod("Chuyển khoản ngân hàng");
        String combo = orderPage.selectCreateComboWithEnoughStock()
                .selectedCombo();
        String worker = orderPage.selectFirstCreateWorker();
        String note = "Ghi chú phải được giữ sau validation";
        orderPage.fillCreateTexts(note, "");
        assertRequiredValidation(
                orderPage.submitCreateAndReadValidation(),
                UniformOrderPage.CreateRequiredField.ADDRESS);

        UniformOrderPage.CreateFormValues values = orderPage.createFormValues();
        Assert.assertEquals(values.paymentMethod(),
                "Chuyển khoản ngân hàng");
        Assert.assertEquals(values.note(), note);
        Assert.assertTrue(values.drawerContent().contains(
                        TextNormalizer.normalize(combo)),
                "Validation đã làm mất combo được chọn.");
        Assert.assertTrue(values.drawerContent().contains(
                        TextNormalizer.normalize(worker)),
                "Validation đã làm mất hồ sơ thợ được chọn.");
    }

    /** Form không có giới hạn nghiệp vụ nên chuỗi dài phải được giữ nguyên. */
    @Test(groups = {"uniform", "order", "create", "validation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_060)
    public void longAddressAndNoteAreNotTruncated() {
        String longNote = "Ghi chú dài — Automation 123! ".repeat(200);
        String longAddress = "Địa chỉ dài, phường/xã; quận-huyện #123. ".repeat(200);
        orderPage.openCreateOrderDrawer();
        UniformOrderPage.TextEntryResult values = orderPage.fillCreateTexts(
                longNote, longAddress);
        Assert.assertEquals(values.note(), longNote,
                "Ghi chú dài đã bị cắt ký tự.");
        Assert.assertEquals(values.address(), longAddress,
                "Địa chỉ dài đã bị cắt ký tự.");
    }

    /** HTML/ký tự đặc biệt được nhận như text và không tạo node script. */
    @Test(groups = {"uniform", "order", "create", "validation", "security",
            "data-interaction"}, description = UniformOrderTestCases.UNI_ORD_061)
    public void htmlAndSpecialCharactersRemainPlainText() {
        String note = "<script id=\"order-validation-xss\">alert('x')</script> ™ ✓";
        String address = "<b>123 Đường A & B</b> — P.1/Q.2 #@$%^&*()";
        orderPage.openCreateOrderDrawer();
        UniformOrderPage.TextEntryResult values = orderPage.fillCreateTexts(
                note, address);
        Assert.assertEquals(values.note(), note);
        Assert.assertEquals(values.address(), address);
        Assert.assertFalse(orderPage.createFormContainsElementId(
                        "order-validation-xss"),
                "Payload HTML trong ghi chú đã tạo element thật trên DOM.");
    }

    /** Chuẩn bị tất cả field bắt buộc trừ địa chỉ để cô lập đúng validation. */
    private void prepareValidRequiredFieldsExceptAddress() {
        orderPage.openCreateOrderDrawer();
        orderPage.selectCreatePaymentMethod("Chuyển khoản ngân hàng");
        orderPage.selectCreateComboWithEnoughStock();
        orderPage.selectFirstCreateWorker();
    }
}
