package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformOrderPage;
import com.vuatho.support.UniformOrderCreateTestSupport;
import com.vuatho.testcases.UniformOrderTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Các luồng submit thật, tồn kho và chống tạo trùng của form tạo đơn. */
public class OrderCreateSubmissionTest
        extends UniformOrderCreateTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(OrderCreateSubmissionTest.class,
                "Đơn hàng Đồng phục", "Tạo đơn thật");
    }

    /** Tạo đơn Chờ xác nhận, Chưa thanh toán bằng chuyển khoản. */
    @Test(groups = {"uniform", "order", "create", "mutation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_047)
    public void createUnpaidBankTransferOrder() {
        assertCreated(orderPage.createRealOrder(
                "Chưa thanh toán",
                "Chuyển khoản ngân hàng",
                "123 Đường Automation, TP.HCM",
                "Automation tạo đơn chưa thanh toán"));
    }

    /** Tạo đơn Chờ xác nhận, Đã thanh toán trực tiếp tại văn phòng. */
    @Test(groups = {"uniform", "order", "create", "mutation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_048)
    public void createPaidOfficeOrder() {
        assertCreated(orderPage.createRealOrder(
                "Đã thanh toán",
                "Thanh toán trực tiếp tại VP",
                "456 Đường Automation, TP.HCM",
                "Automation tạo đơn đã thanh toán"));
    }

    /** Bao phủ cặp Chưa thanh toán và thanh toán trực tiếp tại văn phòng. */
    @Test(groups = {"uniform", "order", "create", "mutation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_049)
    public void createUnpaidOfficeOrder() {
        assertCreated(orderPage.createRealOrder(
                "Chưa thanh toán",
                "Thanh toán trực tiếp tại VP",
                "147 Đường Automation, TP.HCM",
                "Automation chưa thanh toán tại văn phòng"));
    }

    /** Bao phủ cặp Đã thanh toán và chuyển khoản ngân hàng. */
    @Test(groups = {"uniform", "order", "create", "mutation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_050)
    public void createPaidBankTransferOrder() {
        assertCreated(orderPage.createRealOrder(
                "Đã thanh toán",
                "Chuyển khoản ngân hàng",
                "258 Đường Automation, TP.HCM",
                "Automation đã thanh toán chuyển khoản"));
    }

    /** Ghi chú là tùy chọn nên bỏ trống vẫn phải tạo đơn thành công. */
    @Test(groups = {"uniform", "order", "create", "mutation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_051)
    public void createOrderWithoutOptionalNote() {
        UniformOrderPage.CreateSubmissionResult result =
                orderPage.createRealOrder(
                        "Chưa thanh toán",
                        "Chuyển khoản ngân hàng",
                        "369 Đường Automation, TP.HCM",
                        "");
        assertCreated(result);
        Assert.assertTrue(result.note().isBlank(),
                "Testcase bỏ trống ghi chú nhưng dữ liệu chuẩn bị không rỗng.");
    }

    /** UI mô tả hỗ trợ nhiều combo nên phải thêm được combo thứ hai. */
    @Test(groups = {"uniform", "order", "create", "mutation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_052)
    public void createOrderWithMultipleCombos() {
        UniformOrderPage.AdvancedCreateResult result =
                orderPage.createAdvancedOrder();
        Assert.assertFalse(result.secondCombo().isBlank(),
                "Sau combo đầu tiên không còn control để thêm combo thứ hai. "
                        + result.content());
        Assert.assertTrue(result.created(),
                "Form nhiều combo hợp lệ nhưng chưa tạo được đơn.");
        Assert.assertTrue(result.totalAfter() >= result.totalBefore() + 1,
                "Tạo đơn nhiều combo nhưng tổng đơn chưa tăng.");
    }

    /** Combo hoặc Size báo thiếu hàng phải giữ drawer và không tạo dữ liệu. */
    @Test(groups = {"uniform", "order", "create", "validation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_053)
    public void cannotCreateWhenSelectedVariantIsOutOfStock() {
        UniformOrderPage.UnavailableStockResult result =
                orderPage.rejectUnavailableStock();
        Assert.assertTrue(result.shortageFound(),
                "Dữ liệu sandbox hiện tại không có combo/Size thiếu tồn kho.");
        Assert.assertTrue(result.drawerOpen(),
                "Combo/Size thiếu tồn kho nhưng drawer đã đóng.");
        Assert.assertEquals(result.totalAfter(), result.totalBefore(),
                "Combo/Size thiếu tồn kho nhưng hệ thống vẫn tạo thêm đơn.");
        Assert.assertTrue(result.content().contains("thieu hang")
                        || result.content().contains("ton kho"),
                "Không hiển thị phản hồi liên quan đến tồn kho.");
    }

    /** Bấm Xác nhận liên tiếp chỉ được tạo đúng một đơn. */
    @Test(groups = {"uniform", "order", "create", "mutation", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_054)
    public void doubleConfirmDoesNotCreateDuplicateOrder() {
        UniformOrderPage.DoubleSubmitResult result =
                orderPage.submitValidOrderTwice();
        Assert.assertEquals(result.totalAfter(), result.totalBefore() + 1,
                "Bấm Xác nhận liên tiếp đã tạo thiếu hoặc trùng đơn.");
        Assert.assertFalse(result.latestRowText().isBlank(),
                "Không đọc được dòng đơn vừa tạo sau thao tác bấm liên tiếp.");
    }

    /** Tạo xong mở lại đơn đầu danh sách và đối chiếu dữ liệu đã lưu. */
    @Test(groups = {"uniform", "order", "create", "detail", "mutation",
            "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_055)
    public void createdOrderPersistsInDetailDrawer() {
        UniformOrderPage.CreateSubmissionResult created =
                orderPage.createRealOrder(
                        "Đã thanh toán",
                        "Chuyển khoản ngân hàng",
                        "159 Đường Automation, TP.HCM",
                        "Automation kiểm tra dữ liệu đã lưu");
        assertCreated(created);
        String detail = orderPage.openLatestOrderDetail();
        Assert.assertTrue(detail.contains("da thanh toan"),
                "Chi tiết đơn không giữ trạng thái Đã thanh toán.");
        Assert.assertTrue(detail.contains("chuyen khoan ngan hang"),
                "Chi tiết đơn không giữ phương thức chuyển khoản.");
        Assert.assertTrue(detail.contains("159 duong automation"),
                "Chi tiết đơn không giữ địa chỉ đã nhập.");
        Assert.assertTrue(detail.contains("automation kiem tra du lieu da luu"),
                "Chi tiết đơn không giữ ghi chú đã nhập.");
    }

    /** Drawer phải đóng, dữ liệu động có giá trị và dòng mới phải xuất hiện. */
    private void assertCreated(UniformOrderPage.CreateSubmissionResult result) {
        Assert.assertTrue(result.created(),
                "Xác nhận hợp lệ nhưng drawer chưa đóng. Nội dung: "
                        + result.bodyText());
        Assert.assertFalse(result.combo().isBlank(),
                "Luồng tạo chưa chọn được combo thật.");
        Assert.assertFalse(result.worker().isBlank(),
                "Luồng tạo chưa chọn được hồ sơ thợ thật.");
        Assert.assertTrue(result.totalAfter() >= result.totalBefore() + 1,
                "Drawer đã đóng nhưng tổng đơn chưa tăng sau khi tạo thật.");
        Assert.assertFalse(result.latestRowText().isBlank(),
                "Tạo thành công nhưng không đọc được dòng đơn mới nhất.");
        Assert.assertTrue(result.latestRowText().contains("cho xac nhan"),
                "Dòng đơn mới không hiển thị trạng thái Chờ xác nhận.");
    }
}
