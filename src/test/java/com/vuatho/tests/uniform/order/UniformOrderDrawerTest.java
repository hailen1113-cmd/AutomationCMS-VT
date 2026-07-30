package com.vuatho.tests.uniform.order;

import com.vuatho.testcases.UniformOrderTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformOrderPage.DetailSnapshot;
import com.vuatho.support.UniformModuleTestSupport;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra form tạo, chi tiết và chế độ sửa đơn mà không xác nhận mutation. */
public class UniformOrderDrawerTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformOrderDrawerTest.class,
                "Đồng phục", "Form và chi tiết đơn hàng");
    }

    /** Form tạo phải có đủ trường và không tạo đơn khi thiếu dữ liệu bắt buộc. */
    @Test(groups = {"uniform", "order", "drawer", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_001)
    public void createOrderDrawerContainsRequiredControls() {
        String form = uniformOrderPage.open().openCreateDrawer();
        for (String field : new String[]{
                "Chờ xác nhận", "Chưa thanh toán",
                "Chuyển khoản ngân hàng", "Thanh toán trực tiếp tại VP",
                "THÊM COMBO ĐỒNG PHỤC",
                "GHI CHÚ", "HỒ SƠ THỢ", "ĐỊA CHỈ"}) {
            Assert.assertTrue(form.contains(field), "Form tạo đơn thiếu " + field);
        }
        Assert.assertTrue(uniformOrderPage.submitEmptyCreateFormKeepsDrawerOpen(),
                "Form rỗng đã đóng hoặc tạo đơn dù chưa chọn combo và thợ.");
    }

    /** Mỗi trạng thái có dữ liệu phải mở drawer và trả đúng trạng thái. */
    @Test(dataProvider = "statuses",
            groups = {"uniform", "order", "detail", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_002)
    public void detailOpensForEachStatus(String status) {
        uniformOrderPage.open().openFilter();
        uniformOrderPage.chooseFilter(status);
        DetailSnapshot detail;
        try {
            detail = uniformOrderPage.openFirstDetail(status);
        } catch (IllegalStateException noData) {
            throw new SkipException(noData.getMessage());
        }
        Assert.assertTrue(detail.text().contains("#" + detail.id()));
        Assert.assertTrue(detail.text().contains(status),
                "Drawer đơn #" + detail.id() + " sai trạng thái.");
        Assert.assertTrue(detail.text().contains("Thông tin đơn hàng")
                && detail.text().contains("Thông tin người nhận"));
    }

    /** Đơn chờ xác nhận phải hiển thị các hành động nghiệp vụ phù hợp. */
    @Test(groups = {"uniform", "order", "detail", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_003)
    public void pendingOrderShowsExpectedActions() {
        uniformOrderPage.open();
        DetailSnapshot detail = uniformOrderPage.openFirstDetail("Chờ xác nhận");
        for (String action : new String[]{
                "Đã giao hàng cho bên vận chuyển", "Hủy",
                "Xác nhận thanh toán", "Chỉnh sửa"}) {
            Assert.assertTrue(detail.text().contains(action),
                    "Đơn chờ xác nhận thiếu action " + action);
        }
    }

    /** Nút chỉnh sửa phải mở form có combo, người nhận và lưu thay đổi. */
    @Test(groups = {"uniform", "order", "detail", "edit", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_004)
    public void editModeReturnsEditableOrderData() {
        uniformOrderPage.open();
        uniformOrderPage.openFirstDetail("Chờ xác nhận");
        String edit = uniformOrderPage.openEditMode();
        Assert.assertTrue(edit.contains("Lưu thay đổi")
                        || edit.contains("Xác nhận"),
                "Chế độ sửa thiếu nút lưu.");
        Assert.assertTrue(edit.contains("Thông tin đơn hàng"));
        Assert.assertTrue(edit.contains("Thông tin người nhận"));
    }

    @DataProvider(name = "statuses")
    public Object[][] statuses() {
        return new Object[][]{
                {"Chờ xác nhận"},
                {"Đã giao hàng cho bên vận chuyển"},
                {"Đã hoàn tất"},
                {"Đã hủy"}
        };
    }
}
