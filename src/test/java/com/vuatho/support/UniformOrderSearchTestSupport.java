package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.UniformOrderPage;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;


/** Setup và assertion dùng chung cho testcase tìm kiếm Đơn hàng Đồng phục. */
public abstract class UniformOrderSearchTestSupport extends BaseTest {
    protected UniformOrderPage orderPage;

    /** Đảm bảo có session đăng nhập và mở đúng Page Object của module. */
    @BeforeMethod(alwaysRun = true)
    public void prepareUniformOrderSearch() {
        requireAuthenticatedSession("tìm kiếm Đơn hàng Đồng phục");
        orderPage = new UniformOrderPage(driver);
    }

    /** Lấy khách hàng thật từ dòng đầu hoặc SKIP rõ ràng nếu bảng không có dữ liệu. */
    protected UniformOrderPage.CustomerSearchData customerOrSkip() {
        return orderPage.firstVisibleCustomer().orElseThrow(() ->
                new SkipException(
                        "Không có dòng chứa đủ tên và SĐT để chuẩn bị dữ liệu search."));
    }

    /** Kiểm tra loại search, từ khóa và toàn bộ dòng đang hiển thị. */
    protected void assertSearchResult(
            UniformOrderPage.SearchResult result,
            String expectedType) {
        Assert.assertEquals(result.selectedType(), expectedType,
                "Dropdown chưa giữ đúng loại tìm kiếm.");
        Assert.assertEquals(result.inputValue(), result.keyword(),
                "Ô tìm kiếm chưa giữ đúng từ khóa đã nhập.");
        Assert.assertFalse(result.customers().isEmpty(),
                "Từ khóa lấy từ dữ liệu thật nhưng không trả kết quả. Loại="
                        + expectedType + ", từ khóa=" + result.keyword());
        Assert.assertTrue(result.allRowsMatch(),
                "Có dòng không khớp trường và từ khóa tìm kiếm.");
        Assert.assertTrue(result.totalDisplayed() >= result.customers().size(),
                "Tổng hiển thị nhỏ hơn số dòng đang có trong bảng.");
    }
}
