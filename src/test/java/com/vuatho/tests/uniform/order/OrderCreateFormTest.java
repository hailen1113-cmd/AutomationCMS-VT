package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformOrderCreateTestSupport;
import com.vuatho.testcases.UniformOrderTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Testcase cấu trúc và tương tác không submit của drawer tạo đơn. */
public class OrderCreateFormTest extends UniformOrderCreateTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(OrderCreateFormTest.class,
                "Đơn hàng Đồng phục", "Form tạo đơn");
    }

    /** Kiểm tra đủ vùng form, trường bắt buộc và trạng thái mặc định. */
    @Test(groups = {"uniform", "order", "create", "form", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_035)
    public void createDrawerShowsCompleteFormAndDefaults() {
        var form = orderPage.openCreateOrderDrawer()
                .createOrderFormSnapshot();
        Assert.assertTrue(form.content().contains("Thông tin trạng thái"));
        Assert.assertTrue(form.content().contains("Thông tin đơn hàng"));
        Assert.assertTrue(form.content().contains("Thông tin người nhận"));
        Assert.assertTrue(form.comboboxPlaceholders().containsAll(List.of(
                "Tìm kiếm combo đồng phục", "Tìm kiếm hồ sơ thợ")));
        Assert.assertEquals(form.requiredLabelCount(), 3L,
                "Form phải có ba trường bắt buộc: combo, thợ và địa chỉ.");
        Assert.assertTrue(form.pendingSelected(),
                "Đơn mới chưa mặc định Chờ xác nhận.");
        Assert.assertTrue(form.unpaidSelected(),
                "Đơn mới chưa mặc định Chưa thanh toán.");
        Assert.assertTrue(form.cancelButton() && form.confirmButton(),
                "Drawer thiếu nút Hủy hoặc Xác nhận.");
    }

    /** Hủy form không được tạo thêm đơn. */
    @Test(groups = {"uniform", "order", "create", "close", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_036)
    public void cancelClosesCreateDrawer() {
        orderPage.open();
        int before = orderPage.totalDisplayed();
        orderPage.openCreateOrderDrawer();
        Assert.assertTrue(orderPage.cancelCreateOrder(),
                "Nút Hủy chưa đóng drawer.");
        Assert.assertEquals(orderPage.totalDisplayed(), before,
                "Hủy form nhưng tổng đơn đã thay đổi.");
    }

    /** Nút X ở header phải đóng drawer như thao tác Hủy. */
    @Test(groups = {"uniform", "order", "create", "close", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_037)
    public void headerCloseClosesCreateDrawer() {
        orderPage.open();
        int before = orderPage.totalDisplayed();
        orderPage.openCreateOrderDrawer();
        Assert.assertTrue(orderPage.closeCreateOrderByHeader(),
                "Nút X chưa đóng drawer.");
        Assert.assertEquals(orderPage.totalDisplayed(), before,
                "Đóng drawer nhưng tổng đơn đã thay đổi.");
    }

    /** Hai trạng thái thanh toán phải chuyển visual selected tương ứng. */
    @Test(groups = {"uniform", "order", "create", "status", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_039)
    public void paymentStatusesCanBeChanged() {
        orderPage.openCreateOrderDrawer();
        Assert.assertTrue(orderPage.selectCreatePaymentStatus("Đã thanh toán"));
        Assert.assertTrue(orderPage.selectCreatePaymentStatus("Chưa thanh toán"));
    }

    /** Hai phương thức có trên form phải đều chọn được. */
    @Test(groups = {"uniform", "order", "create", "payment", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_040)
    public void paymentMethodsCanBeSelected() {
        orderPage.openCreateOrderDrawer();
        Assert.assertTrue(orderPage.selectCreatePaymentMethod(
                "Chuyển khoản ngân hàng"));
        Assert.assertTrue(orderPage.selectCreatePaymentMethod(
                "Thanh toán trực tiếp tại VP"));
    }

    /** Chọn dữ liệu combo thật phải thay empty-state bằng chi tiết sản phẩm. */
    @Test(groups = {"uniform", "order", "create", "combo", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_041)
    public void selectingComboRendersProductDetails() {
        orderPage.openCreateOrderDrawer();
        var result = orderPage.selectFirstCreateCombo();
        Assert.assertFalse(result.selectedCombo().isBlank(),
                "Không chọn được combo từ dữ liệu thật.");
        Assert.assertFalse(result.drawerContent()
                        .contains("Chưa có combo nào được chọn"),
                "Chọn combo nhưng form vẫn hiển thị empty-state.");
        Assert.assertTrue(result.comboboxesAfter() != result.comboboxesBefore()
                        || result.numberInputCount() > 0
                        || result.drawerContent().contains("Số lượng"),
                "Chọn combo nhưng chưa render control biến thể hoặc số lượng.");
    }

    /** React Select hồ sơ thợ phải trả và chọn được dữ liệu thật. */
    @Test(groups = {"uniform", "order", "create", "worker", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_042)
    public void selectingWorkerUsesRealProfile() {
        orderPage.openCreateOrderDrawer();
        String worker = orderPage.selectFirstCreateWorker();
        Assert.assertFalse(worker.isBlank(),
                "Không chọn được hồ sơ thợ từ dữ liệu thật.");
    }

    /** Ghi chú tùy chọn và địa chỉ bắt buộc phải giữ đúng nội dung. */
    @Test(groups = {"uniform", "order", "create", "input", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_043)
    public void noteAndAddressKeepEnteredValues() {
        orderPage.openCreateOrderDrawer();
        var result = orderPage.fillCreateTexts(
                "Automation tạo đơn đồng phục",
                "123 Đường Automation, TP.HCM");
        Assert.assertEquals(result.note(),
                "Automation tạo đơn đồng phục");
        Assert.assertEquals(result.address(),
                "123 Đường Automation, TP.HCM");
    }
}
