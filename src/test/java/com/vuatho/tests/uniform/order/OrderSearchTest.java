package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformOrderSearchTestSupport;
import com.vuatho.testcases.UniformOrderTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Testcase tìm kiếm khách hàng trong menu Quản lí Đơn hàng Đồng phục.
 *
 * <p>Tên và SĐT hợp lệ được lấy động từ bảng; không phụ thuộc ID cố định.</p>
 */
public class OrderSearchTest extends UniformOrderSearchTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(OrderSearchTest.class,
                "Đơn hàng Đồng phục", "Tìm kiếm thông tin khách");
    }

    /** Ô search phải có đúng placeholder và hai loại tìm kiếm. */
    @Test(groups = {"uniform", "order", "search", "form", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_027)
    public void searchControlContainsNameAndPhoneTypes() {
        var form = orderPage.searchFormSnapshot();
        Assert.assertEquals(form.placeholder(), "Tìm kiếm thông tin khách");
        Assert.assertEquals(form.ariaLabel(), "Tìm kiếm thông tin khách");
        Assert.assertEquals(form.typeOptions(),
                List.of("theo tên", "theo SĐT"));
        Assert.assertEquals(form.selectedType(), "name",
                "Loại tìm kiếm mặc định phải là theo tên.");
    }

    /** Dùng nguyên họ tên của dòng thật để tìm lại khách hàng. */
    @Test(groups = {"uniform", "order", "search", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_028)
    public void searchByExactCustomerName() {
        orderPage.open();
        var customer = customerOrSkip();
        assertSearchResult(orderPage.search("name", customer.name()), "name");
    }

    /** Dùng từ dài nhất trong tên để kiểm tra tìm kiếm một phần. */
    @Test(groups = {"uniform", "order", "search", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_029)
    public void searchByPartialCustomerName() {
        orderPage.open();
        var customer = customerOrSkip();
        assertSearchResult(
                orderPage.search("name", customer.longestNamePart()), "name");
    }

    /** Chuyển SĐT hiển thị +84 về dạng nội địa rồi tìm chính xác. */
    @Test(groups = {"uniform", "order", "search", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_030)
    public void searchByExactCustomerPhone() {
        orderPage.open();
        var customer = customerOrSkip();
        assertSearchResult(
                orderPage.search("phone", customer.phone()), "phone");
    }

    /** Dùng sáu số cuối để kiểm tra tìm kiếm một phần SĐT. */
    @Test(groups = {"uniform", "order", "search", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_031)
    public void searchByPartialCustomerPhone() {
        orderPage.open();
        var customer = customerOrSkip();
        String phone = customer.searchablePhone();
        String partial = phone.substring(Math.max(0, phone.length() - 6));
        assertSearchResult(orderPage.search("phone", partial), "phone");
    }

    /** Cả hai loại search phải hiển thị empty-state với từ khóa không tồn tại. */
    @Test(groups = {"uniform", "order", "search", "empty", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_032)
    public void nonexistentNameAndPhoneShowEmptyState() {
        orderPage.open();
        var byName = orderPage.search(
                "name", "__automation_uniform_order_no_match__");
        Assert.assertTrue(byName.emptyState(),
                "Tên không tồn tại nhưng bảng chưa hiển thị trạng thái rỗng.");

        var byPhone = orderPage.search("phone", "000000000000000");
        Assert.assertTrue(byPhone.emptyState(),
                "SĐT không tồn tại nhưng bảng chưa hiển thị trạng thái rỗng.");
    }

    /** Đổi loại ngay trong cùng phiên phải dùng đúng trường mới được chọn. */
    @Test(groups = {"uniform", "order", "search", "type", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_033)
    public void switchFromNameToPhoneSearch() {
        orderPage.open();
        var customer = customerOrSkip();
        assertSearchResult(orderPage.search("name", customer.name()), "name");
        assertSearchResult(
                orderPage.search("phone", customer.phone()), "phone");
    }

    /** Sau khi xóa từ khóa, tổng dữ liệu phải trở về trước lúc search. */
    @Test(groups = {"uniform", "order", "search", "reset", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_034)
    public void clearingKeywordRestoresOriginalList() {
        orderPage.open();
        int initialTotal = orderPage.totalDisplayed();
        var customer = customerOrSkip();
        var searched = orderPage.search("name", customer.name());
        Assert.assertEquals(searched.inputValue(), customer.name(),
                "Chưa nhập được từ khóa trước khi kiểm tra thao tác xóa.");

        var restored = orderPage.clearSearch();
        Assert.assertEquals(restored.inputValue(), "",
                "Ô tìm kiếm chưa được xóa.");
        Assert.assertEquals(restored.totalDisplayed(), initialTotal,
                "Xóa từ khóa chưa khôi phục tổng dữ liệu ban đầu.");
        Assert.assertTrue(
                !restored.customers().isEmpty() || restored.emptyState(),
                "Sau khi xóa từ khóa, bảng không có dữ liệu hoặc empty-state.");
    }
}
