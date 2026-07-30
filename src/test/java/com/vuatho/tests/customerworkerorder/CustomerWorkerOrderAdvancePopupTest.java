package com.vuatho.tests.customerworkerorder;

import com.vuatho.testcases.CustomerWorkerOrderTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.support.customerworkerorder.CustomerWorkerOrderTestSupport;
import com.vuatho.pages.CustomerWorkerOrderPage.AdvanceQuoteSnapshot;
import com.vuatho.pages.CustomerWorkerOrderPage.DetailSnapshot;
import com.vuatho.pages.CustomerWorkerOrderPage.OrderDataUnavailableException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Kiểm tra popup báo giá xuất hiện khi chọn Sang bước kế tiếp.
 *
 * <p>Bao phủ dữ liệu ban đầu, nhập/thêm/xóa dòng báo giá, validation, nút Hủy
 * và dấu X. Các case trong file này không bấm Xác nhận để tránh vô tình đổi
 * trạng thái đơn; thao tác xác nhận và kết quả chuyển bước thật nằm trong
 * {@link CustomerWorkerOrderWorkflowTest}.</p>
 */
public class CustomerWorkerOrderAdvancePopupTest extends CustomerWorkerOrderTestSupport {
    /** Các trạng thái có thể xuất hiện action Sang bước kế tiếp và popup báo giá. */
    private static final List<String> QUOTE_STATUSES = List.of(
            "Match đơn", "Thợ di chuyển", "Thợ checkin", "Đang làm việc");

    /** Chạy toàn bộ file hoặc riêng group truyền qua {@code customer.order.group}. */
    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(CustomerWorkerOrderAdvancePopupTest.class,
                    "Đơn Khách - Thợ", "Popup Sang bước kế tiếp");
        } else {
            TestNgRunner.runGroup(
                    "Đơn Khách - Thợ", "Popup Sang bước kế tiếp - " + group,
                    group, CustomerWorkerOrderAdvancePopupTest.class);
        }
    }

    /** Kiểm tra popup trả đủ nhãn, dòng dịch vụ/giá và ba action chính. */
    @Test(priority = 1,
            groups = {"customer-worker-order", "advance-popup", "popup-content",
                    "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_001)
    public void advancePopupReturnsQuoteDataAndControls() {
        openOrderWithQuotePopup();
        AdvanceQuoteSnapshot popup = orderPage.openAdvanceQuotePopup();

        for (String label : List.of(
                "Sang bước kế tiếp", "Chi tiết báo giá", "Dịch vụ", "Giá tiền")) {
            Assert.assertTrue(popup.text().contains(label),
                    "Popup thiếu nội dung " + label);
        }
        Assert.assertFalse(popup.services().isEmpty(),
                "Popup không trả dữ liệu dịch vụ.");
        Assert.assertEquals(popup.prices().size(), popup.services().size(),
                "Số trường giá tiền không khớp số dịch vụ.");
        Assert.assertTrue(popup.buttons().containsAll(
                        List.of("Thêm báo giá", "Hủy", "Xác nhận")),
                "Popup thiếu nút Thêm báo giá/Hủy/Xác nhận.");
    }

    /** Thêm một dòng rồi xóa chính dòng đó, bảo đảm số field trở về ban đầu. */
    @Test(priority = 2,
            groups = {"customer-worker-order", "advance-popup", "quote-row",
                    "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_002)
    public void quoteRowCanBeAddedAndRemoved() {
        openOrderWithQuotePopup();
        AdvanceQuoteSnapshot before = orderPage.openAdvanceQuotePopup();
        int addedCount = orderPage.addAdvanceQuoteRow();

        Assert.assertEquals(addedCount, before.services().size() + 1,
                "Thêm báo giá không tạo đúng một dòng.");
        AdvanceQuoteSnapshot added = orderPage.currentAdvanceQuoteSnapshot();
        Assert.assertEquals(added.prices().size(), added.services().size(),
                "Dòng báo giá mới thiếu trường dịch vụ hoặc giá tiền.");

        int removedCount = orderPage.removeLastAdvanceQuoteRow();
        Assert.assertEquals(removedCount, before.services().size(),
                "Xóa báo giá không trả về đúng số dòng ban đầu.");
    }

    /** Submit dòng trống và xác nhận popup hiển thị validation bắt buộc. */
    @Test(priority = 3,
            groups = {"customer-worker-order", "advance-popup", "validation",
                    "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_003)
    public void blankQuoteShowsRequiredValidation() {
        openOrderWithQuotePopup();
        orderPage.openAdvanceQuotePopup();

        String validation = orderPage.submitBlankAdvanceQuoteAndReadValidation();

        Assert.assertEquals(validation, "Vui lòng nhập đầy đủ thông tin");
        Assert.assertTrue(orderPage.cancelAdvanceQuotePopup(),
                "Drawer bị đóng sau khi hủy popup validation.");
    }

    /** Bấm Hủy rồi đọc lại đơn để chứng minh trạng thái không bị thay đổi. */
    @Test(priority = 4,
            groups = {"customer-worker-order", "advance-popup", "cancel-popup",
                    "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_004)
    public void cancelPopupKeepsOrderStatus() {
        DetailSnapshot before = openOrderWithQuotePopup();
        orderPage.openAdvanceQuotePopup();

        Assert.assertTrue(orderPage.cancelAdvanceQuotePopup(),
                "Nút Hủy popup làm đóng luôn drawer chi tiết.");
        orderPage.closeOverlay();
        orderPage.open();
        DetailSnapshot after = orderPage.openOrder(before.id());
        Assert.assertEquals(after.status(), before.status(),
                "Hủy popup vẫn làm thay đổi trạng thái đơn #" + before.id() + ".");
    }

    /** Nhập dữ liệu vào dòng mới và đối chiếu cả dịch vụ lẫn giá đã định dạng. */
    @Test(priority = 5,
            groups = {"customer-worker-order", "advance-popup", "quote-input",
                    "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_005)
    public void newQuoteRowAcceptsServiceAndPrice() {
        openOrderWithQuotePopup();
        AdvanceQuoteSnapshot before = orderPage.openAdvanceQuotePopup();
        orderPage.addAdvanceQuoteRow();

        AdvanceQuoteSnapshot entered = orderPage.fillLastAdvanceQuoteRow(
                "Dịch vụ automation", "123456");

        Assert.assertEquals(
                entered.services().get(entered.services().size() - 1),
                "Dịch vụ automation",
                "Trường dịch vụ không giữ đúng dữ liệu vừa nhập.");
        Assert.assertEquals(
                entered.prices().get(entered.prices().size() - 1)
                        .replaceAll("\\D", ""),
                "123456",
                "Trường giá tiền không giữ/định dạng đúng dữ liệu vừa nhập.");
        Assert.assertEquals(entered.services().size(),
                before.services().size() + 1,
                "Nhập liệu làm sai số lượng dòng báo giá.");
    }

    /** Đóng bằng dấu X và đọc lại đơn để bảo đảm không phát sinh mutation. */
    @Test(priority = 6,
            groups = {"customer-worker-order", "advance-popup", "close-popup",
                    "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_006)
    public void closeIconKeepsOrderStatus() {
        DetailSnapshot before = openOrderWithQuotePopup();
        orderPage.openAdvanceQuotePopup();

        Assert.assertTrue(orderPage.closeAdvanceQuotePopupByIcon(),
                "Dấu X làm đóng luôn drawer chi tiết.");
        orderPage.closeOverlay();
        orderPage.open();
        DetailSnapshot after = orderPage.openOrder(before.id());
        Assert.assertEquals(after.status(), before.status(),
                "Đóng popup bằng dấu X vẫn làm thay đổi trạng thái đơn #"
                        + before.id() + ".");
    }

    /** Tìm linh động đơn ở một trong các trạng thái có thể mở popup báo giá. */
    private DetailSnapshot openOrderWithQuotePopup() {
        List<String> unavailable = new ArrayList<>();
        for (String status : QUOTE_STATUSES) {
            orderPage.open();
            try {
                return orderPage.openFirstOrderForWorkflow(
                        status, "Sang bước kế tiếp");
            } catch (OrderDataUnavailableException exception) {
                unavailable.add(status);
            }
        }
        throw new AssertionError(
                "[THIẾU DỮ LIỆU TEST] Không có đơn nào mở được popup báo giá. "
                        + "Đã kiểm tra: " + unavailable);
    }
}
