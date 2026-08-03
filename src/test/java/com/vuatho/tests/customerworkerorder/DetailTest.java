package com.vuatho.tests.customerworkerorder;

import com.vuatho.testcases.CustomerWorkerOrderTestCases;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.customerworkerorder.CustomerWorkerOrderTestSupport;
import com.vuatho.pages.CustomerWorkerOrderPage;
import com.vuatho.pages.CustomerWorkerOrderPage.DetailSnapshot;
import com.vuatho.pages.CustomerWorkerOrderPage.MediaViewerSnapshot;
import com.vuatho.pages.CustomerWorkerOrderPage.OrderDataUnavailableException;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Kiểm tra drawer Chi tiết đơn dịch vụ và các nội dung điều hướng bên trong.
 *
 * <p>Test tự tìm đơn phù hợp theo trạng thái thay vì phụ thuộc một ID cố định,
 * sau đó kiểm tra thông tin khách/thợ, tài chính, tiến trình, các section,
 * bản đồ và chat hỗ trợ. Nhóm này chỉ xem dữ liệu, không xác nhận chuyển bước
 * hoặc hủy đơn.</p>
 */
public class DetailTest extends CustomerWorkerOrderTestSupport {
    /** Chạy toàn bộ file hoặc riêng group truyền qua {@code customer.order.group}. */
    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(DetailTest.class,
                    "Đơn Khách - Thợ", "Chi tiết đơn");
        } else {
            TestNgRunner.runGroup(
                    "Đơn Khách - Thợ", "Chi tiết đơn - " + group,
                    group, DetailTest.class);
        }
    }

    /** Đối soát các section thông tin, tiến trình và tài chính trong drawer. */
    @Test(groups = {"customer-worker-order", "detail", "detail-summary", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_007)
    public void detailReturnsOrderTimelineAndFinancialData() {
        DetailSnapshot detail =
                orderPage.openFirstRowWithStatus("Hoàn thành đơn");
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

    /**
     * Cung cấp các trạng thái cần kiểm tra.
     *
     * <p>Mặc định chạy toàn bộ; có thể truyền
     * {@code customer.order.detail.status} để chạy riêng một trạng thái khi
     * debug thao tác trên web.</p>
     */
    @DataProvider(name = "detailStatuses")
    public Object[][] detailStatuses() {
        String requested = System.getProperty(
                "customer.order.detail.status", "").trim();
        return CustomerWorkerOrderPage.ORDER_STATUSES.stream()
                .filter(value -> requested.isBlank()
                        || TextNormalizer.normalize(value)
                        .equals(TextNormalizer.normalize(requested)))
                .map(value -> new Object[]{value}).toArray(Object[][]::new);
    }

    /** Mở drawer theo từng trạng thái; trạng thái không có data được skip có lý do. */
    @Test(dataProvider = "detailStatuses",
            groups = {"customer-worker-order", "detail", "filter", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_008)
    public void detailOpensForEachAvailableStatus(String status) {
        DetailSnapshot detail;
        try {
            detail = orderPage.openFirstRowWithStatus(status);
        } catch (IllegalStateException noData) {
            throw new SkipException("Không có đơn trạng thái " + status);
        }
        Assert.assertTrue(detail.text().contains("Chi tiết đơn dịch vụ"));
        Assert.assertTrue(detail.text().contains(detail.id()));
        Assert.assertEquals(detail.status(), status,
                "Drawer đơn #" + detail.id()
                        + " không đúng trạng thái đã chọn trên bộ lọc.");
    }

    /** Cung cấp các section điều hướng cần mở và kiểm tra nội dung. */
    @DataProvider(name = "sections")
    public Object[][] sections() {
        return new Object[][]{
                {"Tổng quan", "KHÁCH"},
                {"Vấn đề đơn", "Vấn đề đơn dịch vụ"},
                {"Tiến trình", "Tiến trình đơn dịch vụ"},
                {"Hóa đơn", "Chi phí dịch vụ|Chưa có thông tin chi phí"},
                {"Biên bản cam kết", "Biên bản cam kết"},
                {"Đánh giá & Báo cáo", "Đánh giá"}
        };
    }

    /** Điều hướng từng section và xác nhận đúng section/nội dung được đưa vào view. */
    @Test(dataProvider = "sections",
            groups = {"customer-worker-order", "detail", "detail-sections", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_009)
    public void eachDetailSectionReturnsContent(
            String section, String expectedContent) {
        try {
            orderPage.openFirstOrderWithDetailSection(section);
        } catch (OrderDataUnavailableException noData) {
            throw new SkipException(noData.getMessage());
        }
        String content = orderPage.detailSectionText(section);
        boolean containsExpectedContent = java.util.Arrays.stream(
                        expectedContent.split("\\|"))
                .anyMatch(content::contains);
        Assert.assertTrue(containsExpectedContent,
                "Điều hướng " + section + " không đưa đúng section vào view. "
                        + "Nội dung thực tế: " + content);
    }

    /** Mở action bản đồ và xác nhận UI vị trí xuất hiện nếu đơn hỗ trợ. */
    @Test(groups = {"customer-worker-order", "detail", "map", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_010)
    public void mapActionOpensLocationData() {
        orderPage.openFirstRowWithStatus("Hoàn thành đơn");
        orderPage.openDetailSection("Tiến trình");
        List<String> mapLinks = orderPage.detailMapLinks();
        Assert.assertFalse(mapLinks.isEmpty(),
                "Chi tiết đơn không trả link tọa độ Google Maps.");
        Assert.assertTrue(mapLinks.stream().allMatch(
                        link -> link.matches(
                                "https://www\\.google\\.com/maps\\?q=-?\\d+\\.\\d+,-?\\d+\\.\\d+")),
                "Link bản đồ không chứa cặp tọa độ hợp lệ: " + mapLinks);
        Assert.assertTrue(orderPage.openMap(),
                "Đơn có tiến trình nhưng không mở được bản đồ.");
    }

    /** Mở chat hỗ trợ khách và xác nhận giao diện chat được render. */
    @Test(groups = {"customer-worker-order", "detail", "chat", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_011)
    public void customerSupportChatOpens() {
        orderPage.openFirstRow();
        Assert.assertTrue(orderPage.openCustomerChat(),
                "Không mở được Chat hỗ trợ khách.");
    }

    /** Cung cấp hai loại hồ sơ có link điều hướng từ phần Tổng quan. */
    @DataProvider(name = "detailProfiles")
    public Object[][] detailProfiles() {
        return new Object[][]{
                {"Khách", "/vuatho/user?id="},
                {"Thợ", "/vuatho/worker?id="}
        };
    }

    /** Bấm xem chi tiết Khách/Thợ, kiểm tra route rồi quay lại đúng drawer. */
    @Test(dataProvider = "detailProfiles",
            groups = {"customer-worker-order", "detail", "profile-navigation",
                    "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_012)
    public void customerAndWorkerProfileLinksOpenCorrectRoutes(
            String role, String expectedPath) {
        try {
            orderPage.openFirstOrderWithProfile(role);
        } catch (OrderDataUnavailableException noData) {
            throw new SkipException(noData.getMessage());
        }
        String openedUrl = orderPage.openDetailProfile(role);
        Assert.assertTrue(openedUrl.contains(expectedPath),
                "Link chi tiết " + role + " mở sai route: " + openedUrl);
        Assert.assertTrue(orderPage.drawerText().contains("Chi tiết đơn dịch vụ"),
                "Quay lại từ hồ sơ " + role + " làm mất drawer đơn.");
    }

    /** Cung cấp hai loại media có thể mở viewer trong phần Vấn đề đơn. */
    @DataProvider(name = "detailMedia")
    public Object[][] detailMedia() {
        return new Object[][]{
                {"Hình ảnh", "img"},
                {"Video", "video"}
        };
    }

    /** Tìm đơn có media, click thumbnail và kiểm tra viewer ảnh/video thật. */
    @Test(dataProvider = "detailMedia",
            groups = {"customer-worker-order", "detail", "media-viewer",
                    "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_013)
    public void imageAndVideoThumbnailsOpenViewer(
            String mediaType, String expectedTag) {
        try {
            orderPage.openFirstOrderWithMedia(mediaType);
        } catch (OrderDataUnavailableException noData) {
            throw new SkipException(noData.getMessage());
        }
        MediaViewerSnapshot viewer =
                orderPage.openDetailMediaViewer(mediaType);
        Assert.assertEquals(viewer.tagName(), expectedTag,
                "Viewer " + mediaType + " render sai loại thẻ.");
        Assert.assertFalse(viewer.source().isBlank(),
                "Viewer " + mediaType + " không có URL tài nguyên.");
        orderPage.closeDetailMediaViewer();
    }

    /** Mở popup chi tiết báo giá và kiểm tra popup trả nội dung nghiệp vụ. */
    @Test(groups = {"customer-worker-order", "detail", "quote-detail",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_014)
    public void quoteDetailButtonOpensPopup() {
        orderPage.openFirstRowWithStatus("Hoàn thành đơn");
        orderPage.openDetailSection("Hóa đơn");
        String popup = orderPage.openQuoteDetailPopup();
        Assert.assertTrue(TextNormalizer.normalize(popup).contains("bao gia"),
                "Popup không trả nội dung nhận diện báo giá: " + popup);
        Assert.assertTrue(orderPage.closeDetailDialog(),
                "Đóng popup báo giá làm mất drawer chi tiết.");
    }

    /** Chuyển qua lại hai tab feedback và kiểm tra selected/content tương ứng. */
    @Test(groups = {"customer-worker-order", "detail", "feedback-tabs",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_015)
    public void ratingAndReportTabsReturnTheirContent() {
        orderPage.openFirstRowWithStatus("Hoàn thành đơn");
        orderPage.openDetailSection("Đánh giá & Báo cáo");
        String report = orderPage.selectFeedbackTab("Báo cáo");
        Assert.assertTrue(report.contains("Báo cáo"),
                "Tab Báo cáo không trả đúng nội dung.");
        String rating = orderPage.selectFeedbackTab("Đánh giá");
        Assert.assertTrue(rating.contains("Đánh giá"),
                "Tab Đánh giá không trả đúng nội dung.");
    }
}
