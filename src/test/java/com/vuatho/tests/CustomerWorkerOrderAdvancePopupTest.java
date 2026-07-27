package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.CustomerWorkerOrderPage.AdvanceQuoteSnapshot;
import com.vuatho.pages.CustomerWorkerOrderPage.DetailSnapshot;
import com.vuatho.pages.CustomerWorkerOrderPage.OrderDataUnavailableException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/** Kiểm tra popup báo giá khi chuyển bước của Đơn Khách - Thợ. */
public class CustomerWorkerOrderAdvancePopupTest extends CustomerWorkerOrderTestSupport {
    private static final List<String> QUOTE_STATUSES = List.of(
            "Match đơn", "Thợ di chuyển", "Thợ checkin", "Đang làm việc");

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

    @Test(priority = 1,
            groups = {"customer-worker-order", "advance-popup", "popup-content",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-029: Popup hiển thị đầy đủ dữ liệu báo giá")
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

    @Test(priority = 2,
            groups = {"customer-worker-order", "advance-popup", "quote-row",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-030: Thêm và xóa dòng báo giá")
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

    @Test(priority = 3,
            groups = {"customer-worker-order", "advance-popup", "validation",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-031: Bắt buộc nhập đủ dịch vụ và giá tiền")
    public void blankQuoteShowsRequiredValidation() {
        openOrderWithQuotePopup();
        orderPage.openAdvanceQuotePopup();

        String validation = orderPage.submitBlankAdvanceQuoteAndReadValidation();

        Assert.assertEquals(validation, "Vui lòng nhập đầy đủ thông tin");
        Assert.assertTrue(orderPage.cancelAdvanceQuotePopup(),
                "Drawer bị đóng sau khi hủy popup validation.");
    }

    @Test(priority = 4,
            groups = {"customer-worker-order", "advance-popup", "cancel-popup",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-032: Hủy popup không chuyển trạng thái đơn")
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

    @Test(priority = 5,
            groups = {"customer-worker-order", "advance-popup", "quote-input",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-033: Nhập dịch vụ và giá tiền cho dòng báo giá mới")
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

    @Test(priority = 6,
            groups = {"customer-worker-order", "advance-popup", "close-popup",
                    "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-034: Đóng popup bằng dấu X không chuyển trạng thái đơn")
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
