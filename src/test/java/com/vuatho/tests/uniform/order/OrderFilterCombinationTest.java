package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformOrderPage;
import com.vuatho.support.UniformOrderFilterTestSupport;
import com.vuatho.testcases.UniformOrderTestCases;
import org.testng.annotations.Test;

import java.util.List;

/** Testcase kết hợp nhiều nhóm và nhiều lựa chọn trong bộ lọc. */
public class OrderFilterCombinationTest
        extends UniformOrderFilterTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(OrderFilterCombinationTest.class,
                "Đơn hàng Đồng phục", "Kết hợp bộ lọc");
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_011)
    public void pendingAndUnpaid() {
        assertFilterWithData(List.of("Chờ xác nhận"),
                List.of("Chưa thanh toán"), List.of());
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_012)
    public void shippingAndPaid() {
        assertFilterWithData(List.of("Đã giao hàng cho bên vận chuyển"),
                List.of("Đã thanh toán"), List.of());
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_013)
    public void completedAndPaid() {
        assertFilterWithData(List.of("Đã hoàn tất"),
                List.of("Đã thanh toán"), List.of());
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_014)
    public void cancelledAndUnpaid() {
        assertFilterWithData(List.of("Đã hủy"),
                List.of("Chưa thanh toán"), List.of());
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_015)
    public void pendingAndBankTransfer() {
        assertFilterWithData(List.of("Chờ xác nhận"), List.of(),
                List.of("Chuyển khoản ngân hàng"));
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_016)
    public void shippingAndCod() {
        assertFilterWithData(List.of("Đã giao hàng cho bên vận chuyển"), List.of(),
                List.of("COD"));
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_017)
    public void completedAndOfficePayment() {
        assertFilterWithData(List.of("Đã hoàn tất"), List.of(),
                List.of("Thanh toán trực tiếp tại VP"));
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_018)
    public void pendingUnpaidAndBankTransfer() {
        assertFilterWithData(
                List.of("Chờ xác nhận"),
                List.of("Chưa thanh toán"),
                List.of("Chuyển khoản ngân hàng"));
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_019)
    public void shippingPaidAndCod() {
        assertFilterWithData(
                List.of("Đã giao hàng cho bên vận chuyển"),
                List.of("Đã thanh toán"),
                List.of("COD"));
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_020)
    public void completedPaidAndOfficePayment() {
        assertFilterWithData(
                List.of("Đã hoàn tất"),
                List.of("Đã thanh toán"),
                List.of("Thanh toán trực tiếp tại VP"));
    }

    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_021)
    public void cancelledUnpaidAndOfficePayment() {
        assertFilterWithData(
                List.of("Đã hủy"),
                List.of("Chưa thanh toán"),
                List.of("Thanh toán trực tiếp tại VP"));
    }

    /** Nhiều trạng thái cùng nhóm phải được áp dụng theo điều kiện OR. */
    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_025)
    public void allOrderStatusesCanBeSelectedTogether() {
        assertFilterWithData(
                UniformOrderPage.ORDER_STATUSES, List.of(), List.of());
    }

    /** Ba phương thức cùng nhóm phải được áp dụng theo điều kiện OR. */
    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_026)
    public void allPaymentMethodsCanBeSelectedTogether() {
        assertFilterWithData(
                List.of(), List.of(), UniformOrderPage.PAYMENT_METHODS);
    }

    /** Kết hợp hai nhóm thanh toán và tự tìm tổ hợp đang có dữ liệu thật. */
    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_062)
    public void paymentStatusAndPaymentMethodWithAvailableData() {
        assertAvailablePaymentStatusAndMethod();
    }
}
