package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformOrderFilterTestSupport;
import com.vuatho.testcases.UniformOrderTestCases;
import org.testng.annotations.Test;

/**
 * Testcase từng tùy chọn riêng của bộ lọc Đơn hàng Đồng phục.
 *
 * <p>Mỗi method có ID riêng để Terminal hiển thị chính xác điều kiện đang chạy.</p>
 */
public class OrderFilterTest extends UniformOrderFilterTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(OrderFilterTest.class,
                "Đơn hàng Đồng phục", "Bộ lọc từng điều kiện");
    }

    /** Chỉ trả đơn Chờ xác nhận. */
    @Test(groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_002)
    public void filterPendingConfirmationOrders() {
        assertSingleOrderStatus("Chờ xác nhận");
    }

    /** Chỉ trả đơn đã giao cho bên vận chuyển. */
    @Test(groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_003)
    public void filterHandedToCarrierOrders() {
        assertSingleOrderStatus("Đã giao hàng cho bên vận chuyển");
    }

    /** Chỉ trả đơn đã hoàn tất. */
    @Test(groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_004)
    public void filterCompletedOrders() {
        assertSingleOrderStatus("Đã hoàn tất");
    }

    /** Chỉ trả đơn đã hủy. */
    @Test(groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_005)
    public void filterCancelledOrders() {
        assertSingleOrderStatus("Đã hủy");
    }

    /** Chỉ trả đơn chưa thanh toán. */
    @Test(groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_006)
    public void filterUnpaidOrders() {
        assertSinglePaymentStatus("Chưa thanh toán");
    }

    /** Chỉ trả đơn đã thanh toán. */
    @Test(groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_007)
    public void filterPaidOrders() {
        assertSinglePaymentStatus("Đã thanh toán");
    }

    /** Chỉ trả đơn thanh toán COD. */
    @Test(groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_008)
    public void filterCodOrders() {
        assertSinglePaymentMethod("COD");
    }

    /** Chỉ trả đơn chuyển khoản ngân hàng. */
    @Test(groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_009)
    public void filterBankTransferOrders() {
        assertSinglePaymentMethod("Chuyển khoản ngân hàng");
    }

    /** Chỉ trả đơn thanh toán trực tiếp tại văn phòng. */
    @Test(groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_010)
    public void filterOfficePaymentOrders() {
        assertSinglePaymentMethod("Thanh toán trực tiếp tại VP");
    }
}
