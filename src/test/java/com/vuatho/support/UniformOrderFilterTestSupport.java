package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.UniformOrderPage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.List;

/**
 * Setup và assertion dùng chung cho testcase bộ lọc Đơn hàng Đồng phục.
 *
 * <p>Lớp không chứa {@code @Test}; từng testcase dùng ID cố định trong catalog.</p>
 */
public abstract class UniformOrderFilterTestSupport extends BaseTest {
    protected UniformOrderPage orderPage;

    /** Đảm bảo session đăng nhập và khởi tạo Page Object đúng route. */
    @BeforeMethod(alwaysRun = true)
    public void prepareUniformOrderModule() {
        requireAuthenticatedSession("Đơn hàng Đồng phục");
        orderPage = new UniformOrderPage(driver);
    }

    /** Đóng popup bộ lọc để testcase sau luôn bắt đầu từ UI sạch. */
    @AfterMethod(alwaysRun = true)
    public void closeUniformOrderFilter() {
        if (driver == null || orderPage == null) {
            return;
        }
        try {
            orderPage.closeOverlay();
        } catch (RuntimeException ignored) {
            // Test sau luôn mở lại route nên cleanup không che lỗi nghiệp vụ.
        }
    }

    /** Áp dụng và kiểm tra lựa chọn, dữ liệu trả về hoặc empty-state hợp lệ. */
    protected void assertFilter(
            List<String> orderStatuses,
            List<String> paymentStatuses,
            List<String> paymentMethods) {
        var result = orderPage.applyFilters(
                orderStatuses, paymentStatuses, paymentMethods);
        assertFilterResult(result);
    }

    /**
     * Kiểm tra tổ hợp bộ lọc và bắt buộc có ít nhất một dòng dữ liệu thật.
     * Điều này tránh case combination PASS rỗng do {@code allMatch}
     * trên danh sách không có phần tử.
     */
    protected void assertFilterWithData(
            List<String> orderStatuses,
            List<String> paymentStatuses,
            List<String> paymentMethods) {
        var result = orderPage.applyFilters(
                orderStatuses, paymentStatuses, paymentMethods);
        assertFilterResult(result);
        Assert.assertFalse(result.rowTexts().isEmpty(),
                "Tổ hợp bộ lọc không trả dữ liệu thật: trạng thái đơn="
                        + orderStatuses
                        + ", trạng thái thanh toán=" + paymentStatuses
                        + ", phương thức thanh toán=" + paymentMethods);
    }

    /**
     * Tìm linh động một cặp trạng thái và phương thức thanh toán có dữ liệu,
     * không phụ thuộc cứng vào ID của một đơn hàng cụ thể.
     */
    protected void assertAvailablePaymentStatusAndMethod() {
        for (String paymentStatus : UniformOrderPage.PAYMENT_STATUSES) {
            for (String paymentMethod : UniformOrderPage.PAYMENT_METHODS) {
                var result = orderPage.applyFilters(
                        List.of(),
                        List.of(paymentStatus),
                        List.of(paymentMethod));
                assertFilterResult(result);
                if (!result.rowTexts().isEmpty()) {
                    return;
                }
            }
        }
        Assert.fail("Không tìm thấy tổ hợp trạng thái thanh toán + "
                + "phương thức thanh toán nào có dữ liệu thật.");
    }

    /** Assertion dùng chung cho chip đã chọn và dữ liệu bảng. */
    private void assertFilterResult(UniformOrderPage.FilterResult result) {
        Assert.assertTrue(result.allExpectedOptionsSelected(),
                "Các chip yêu cầu chưa được chọn đầy đủ. Đang chọn: "
                        + result.selectedOptions());
        Assert.assertTrue(result.allRowsMatch(),
                "Có dòng dữ liệu không khớp tổ hợp bộ lọc.");
        Assert.assertTrue(!result.rowTexts().isEmpty() || result.emptyState(),
                "Bộ lọc không trả dòng dữ liệu nhưng cũng không hiển thị trạng thái rỗng.");
        Assert.assertTrue(result.totalDisplayed() >= result.rowTexts().size(),
                "Tổng hiển thị nhỏ hơn số dòng đang có trên trang.");
    }

    protected void assertSingleOrderStatus(String value) {
        assertFilter(List.of(value), List.of(), List.of());
    }

    protected void assertSinglePaymentStatus(String value) {
        assertFilter(List.of(), List.of(value), List.of());
    }

    protected void assertSinglePaymentMethod(String value) {
        assertFilter(List.of(), List.of(), List.of(value));
    }
}
