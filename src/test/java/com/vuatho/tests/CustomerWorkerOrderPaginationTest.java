package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra phân trang, luôn cuộn tới control trước khi click. */
public class CustomerWorkerOrderPaginationTest extends CustomerWorkerOrderTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(CustomerWorkerOrderPaginationTest.class,
                "Đơn Khách - Thợ", "Phân trang");
    }

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
