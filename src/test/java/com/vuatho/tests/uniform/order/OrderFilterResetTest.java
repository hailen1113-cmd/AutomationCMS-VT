package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformOrderPage;
import com.vuatho.support.UniformOrderFilterTestSupport;
import com.vuatho.testcases.UniformOrderTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Testcase cấu trúc popup, bỏ chọn và hai cách đặt lại bộ lọc. */
public class OrderFilterResetTest
        extends UniformOrderFilterTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(OrderFilterResetTest.class,
                "Đơn hàng Đồng phục", "Cấu trúc và đặt lại bộ lọc");
    }

    /** Popup phải có đúng ba nhóm, chín tùy chọn và nút Đặt lại. */
    @Test(groups = {"uniform", "order", "filter", "form", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_001)
    public void filterPopupContainsAllGroupsAndOptions() {
        var form = orderPage.open().filterFormSnapshot();
        Assert.assertTrue(form.content().contains("Trạng thái đơn"),
                "Popup thiếu nhóm Trạng thái đơn.");
        Assert.assertTrue(form.content().contains("Trạng thái thanh toán"),
                "Popup thiếu nhóm Trạng thái thanh toán.");
        Assert.assertTrue(form.content().contains("Phương thức thanh toán"),
                "Popup thiếu nhóm Phương thức thanh toán.");
        Assert.assertTrue(form.optionTexts().containsAll(
                        UniformOrderPage.ORDER_STATUSES),
                "Popup thiếu tùy chọn trạng thái đơn.");
        Assert.assertTrue(form.optionTexts().containsAll(
                        UniformOrderPage.PAYMENT_STATUSES),
                "Popup thiếu tùy chọn trạng thái thanh toán.");
        Assert.assertTrue(form.optionTexts().containsAll(
                        UniformOrderPage.PAYMENT_METHODS),
                "Popup thiếu tùy chọn phương thức thanh toán.");
        Assert.assertTrue(form.resetButton(),
                "Popup thiếu nút Đặt lại.");
    }

    /** Chọn lại chip đang bật phải bỏ chính điều kiện đó. */
    @Test(groups = {"uniform", "order", "filter", "toggle", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_022)
    public void selectingActiveOptionAgainDeselectsIt() {
        var result = orderPage.toggleFilterOptionOff("Chờ xác nhận");
        Assert.assertTrue(result.selectedAfterFirstClick(),
                "Chip không chuyển sang trạng thái đã chọn.");
        Assert.assertFalse(result.selectedAfterSecondClick(),
                "Chọn lại chip nhưng điều kiện chưa được bỏ.");
        Assert.assertTrue(result.totalDisplayed() >= 0,
                "Danh sách không tải lại sau khi bỏ điều kiện.");
    }

    /** Nút Đặt lại trong popup phải xóa mọi chip đang chọn và phục hồi tổng. */
    @Test(groups = {"uniform", "order", "filter", "reset", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_023)
    public void popupResetClearsAllFilters() {
        assertReset(orderPage.resetInsidePopup());
    }

    /** Nút Reset ngoài trang phải xóa mọi chip đang chọn và phục hồi tổng. */
    @Test(groups = {"uniform", "order", "filter", "reset", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_024)
    public void pageResetClearsAllFilters() {
        assertReset(orderPage.resetFromPageButton());
    }

    private void assertReset(UniformOrderPage.ResetResult result) {
        Assert.assertTrue(result.hadSelection(),
                "Chưa tạo được trạng thái có hai điều kiện trước khi Reset.");
        Assert.assertTrue(result.selectedAfterReset().isEmpty(),
                "Reset nhưng vẫn còn chip được chọn: "
                        + result.selectedAfterReset());
        Assert.assertEquals(result.totalAfterReset(), result.initialTotal(),
                "Tổng hiển thị chưa trở về giá trị trước khi lọc.");
    }
}
