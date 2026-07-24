package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.CustomerWorkerOrderPage;
import com.vuatho.pages.CustomerWorkerOrderPage.DetailSnapshot;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra drawer chi tiết, section, bản đồ và chat hỗ trợ. */
public class CustomerWorkerOrderDetailTest extends CustomerWorkerOrderTestSupport {
    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(CustomerWorkerOrderDetailTest.class,
                    "Đơn Khách - Thợ", "Chi tiết đơn");
        } else {
            TestNgRunner.runGroup(
                    "Đơn Khách - Thợ", "Chi tiết đơn - " + group,
                    group, CustomerWorkerOrderDetailTest.class);
        }
    }

    @Test(groups = {"customer-worker-order", "detail", "detail-summary", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-017: Drawer trả đầy đủ thông tin đơn và đối soát")
    public void detailReturnsOrderTimelineAndFinancialData() {
        DetailSnapshot detail =
                orderPage.openFirstVisibleRowWithStatus("Hoàn thành đơn");
        for (String label : List.of(
                "Chi tiết đơn dịch vụ", "KHÁCH", "THỢ", "Vấn đề đơn dịch vụ",
                "Mã đơn dịch vụ", "Dịch vụ", "Mô tả vấn đề", "Địa chỉ",
                "Thời gian tạo đơn", "Thời gian yêu cầu", "Giá tham khảo",
                "Tiến trình đơn dịch vụ",
                "Phí kết nối", "Thợ thực nhận", "Biên bản cam kết")) {
            Assert.assertTrue(detail.text().contains(label),
                    "Drawer thiếu " + label);
        }
    }

    @DataProvider(name = "detailStatuses")
    public Object[][] detailStatuses() {
        return CustomerWorkerOrderPage.ORDER_STATUSES.stream()
                .map(value -> new Object[]{value}).toArray(Object[][]::new);
    }

    @Test(dataProvider = "detailStatuses",
            groups = {"customer-worker-order", "detail", "filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-018: Mở được chi tiết theo từng trạng thái có dữ liệu")
    public void detailOpensForEachAvailableStatus(String status) {
        DetailSnapshot detail;
        try {
            detail = orderPage.openFirstVisibleRowWithStatus(status);
        } catch (IllegalStateException noData) {
            throw new SkipException("Không có đơn trạng thái " + status);
        }
        Assert.assertTrue(detail.text().contains("Chi tiết đơn dịch vụ"));
        Assert.assertTrue(detail.text().contains(detail.id()));
    }

    @DataProvider(name = "sections")
    public Object[][] sections() {
        return new Object[][]{
                {"Tổng quan"},
                {"Vấn đề đơn"},
                {"Tiến trình"},
                {"Hóa đơn"},
                {"Biên bản cam kết"}
        };
    }

    @Test(dataProvider = "sections",
            groups = {"customer-worker-order", "detail", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-019: Các section drawer điều hướng và trả nội dung")
    public void eachDetailSectionReturnsContent(String section) {
        orderPage.openFirstRow();
        orderPage.openDetailSection(section);
        Assert.assertFalse(orderPage.drawerText().isBlank(),
                "Section " + section + " không trả nội dung.");
    }

    @Test(groups = {"customer-worker-order", "detail", "map", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-020: Xem bản đồ mở dữ liệu vị trí")
    public void mapActionOpensLocationData() {
        orderPage.openFirstVisibleRowWithStatus("Hoàn thành đơn");
        orderPage.openDetailSection("Tiến trình");
        Assert.assertTrue(orderPage.openMap(),
                "Đơn có tiến trình nhưng không mở được bản đồ.");
    }

    @Test(groups = {"customer-worker-order", "detail", "chat", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-021: Chat hỗ trợ khách mở giao diện chat")
    public void customerSupportChatOpens() {
        orderPage.openFirstRow();
        Assert.assertTrue(orderPage.openCustomerChat(),
                "Không mở được Chat hỗ trợ khách.");
    }
}
