package com.vuatho.tests.customerworkerorder;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.customerworkerorder.CustomerWorkerOrderTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Kiểm tra phân trang của bảng Đơn Khách - Thợ.
 *
 * <p>Trước mỗi thao tác, Page Object cuộn control phân trang vào vùng nhìn
 * thấy để người chạy quan sát được. Test đối chiếu số trang, danh sách ID và
 * số dòng ở trang cuối; không thay đổi dữ liệu đơn.</p>
 */
public class CustomerWorkerOrderPaginationTest extends CustomerWorkerOrderTestSupport {
    /** Entry point chạy riêng các testcase phân trang. */
    public static void main(String[] args) {
        TestNgRunner.run(CustomerWorkerOrderPaginationTest.class,
                "Đơn Khách - Thợ", "Phân trang");
    }

    /** Đi tới trang 2 rồi quay lại trang 1 và đối chiếu danh sách ID hai chiều. */
    @Test(groups = {"customer-worker-order", "pagination", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-015: Trang kế và trang trước trả đúng dữ liệu")
    public void nextAndPreviousPagesReturnExpectedOrders() {
        List<String> first = orderPage.rows().stream().map(row -> row.id()).toList();
        orderPage.nextPage();
        Assert.assertEquals(orderPage.activePage(), 2);
        Assert.assertNotEquals(
                orderPage.rows().stream().map(row -> row.id()).toList(), first);
        orderPage.previousPage();
        Assert.assertEquals(orderPage.activePage(), 1);
        Assert.assertEquals(
                orderPage.rows().stream().map(row -> row.id()).toList(), first);
    }

    /** Đi tới trang cuối và đối chiếu số dòng còn lại với Tổng hiển thị. */
    @Test(groups = {"customer-worker-order", "pagination", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-016: Trang cuối khớp Tổng hiển thị")
    public void lastPageReturnsRemainingOrders() {
        int total = orderPage.totalDisplayed();
        int last = orderPage.totalPages();
        Assert.assertEquals(last, (int) Math.ceil(total / 20.0));
        orderPage.goToPage(last);
        int expected = total % 20 == 0 ? 20 : total % 20;
        Assert.assertEquals(orderPage.rows().size(), expected);
    }
}
