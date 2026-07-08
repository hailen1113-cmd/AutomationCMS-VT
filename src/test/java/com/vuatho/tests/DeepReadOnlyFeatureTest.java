package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.navigation.MenuTarget;
import com.vuatho.pages.MenuDestinationPage;
import com.vuatho.pages.OverlayFeaturesPage;
import com.vuatho.pages.ReadOnlyFeaturesPage;
import com.vuatho.utils.PageLoadSynchronizer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.vuatho.navigation.MenuTarget.childOf;

public class DeepReadOnlyFeatureTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(DeepReadOnlyFeatureTest.class,
                "ERP Deep Read-only Features", "Filter and Statistics Contracts");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @Test(description = "CMS-USER-FILTER: User filter exposes status and date controls")
    public void userFilterHasExpectedControls() {
        open(childOf("Người Dùng", "Quản Lí Người Dùng"));
        OverlayFeaturesPage panel = new OverlayFeaturesPage(driver).open("Filter");

        Assert.assertTrue(panel.visibleRadioCount() >= 3, "Thiếu lựa chọn trạng thái KYC.");
        Assert.assertTrue(panel.hasButton("Previous Month"), "Thiếu điều hướng tháng trước.");
        Assert.assertTrue(panel.hasButton("Next Month"), "Thiếu điều hướng tháng sau.");
        Assert.assertTrue(panel.hasButton("Đặt lại"), "Thiếu nút Đặt lại.");
        panel.close();
    }

    @Test(description = "CMS-ORDER-FILTER: Order filter exposes service/status/agreement controls")
    public void orderFilterHasExpectedControls() {
        open(childOf("Đơn Dịch Vụ", "Đơn Khách - Thợ"));
        OverlayFeaturesPage panel = new OverlayFeaturesPage(driver).open("Bộ lọc đơn dịch vụ");

        Assert.assertTrue(panel.hasInput("Tìm kiếm dịch vụ"), "Thiếu tìm kiếm dịch vụ.");
        Assert.assertTrue(panel.hasButton("Chọn trạng thái đơn dịch vụ"), "Thiếu trạng thái đơn.");
        Assert.assertTrue(panel.hasButton("Chọn trạng thái thỏa thuận giá"),
                "Thiếu trạng thái thỏa thuận giá.");
        Assert.assertTrue(panel.visibleRadioCount() >= 4, "Thiếu bộ chọn khoảng ngày.");
        panel.close();
    }

    @Test(description = "CMS-ORDER-STATS: Order statistics modal exposes its report cards")
    public void orderStatisticsHasExpectedCards() {
        open(childOf("Đơn Dịch Vụ", "Đơn Khách - Thợ"));
        OverlayFeaturesPage modal = new OverlayFeaturesPage(driver).open("Thống kê");

        Assert.assertTrue(modal.hasText("Thống kê đơn theo trạng thái"),
                "Thiếu thống kê đơn theo trạng thái.");
        Assert.assertTrue(modal.hasText("Bảo hành 5K"), "Thiếu thống kê Bảo hành 5K.");
        modal.close();
    }

    @Test(description = "CMS-TRANSACTION-FILTER: Transaction status filter exposes all statuses")
    public void transactionStatusHasExpectedOptions() {
        open(childOf("Giao Dịch", "Lịch Sử Giao Dịch"));
        OverlayFeaturesPage dropdown = new OverlayFeaturesPage(driver).open("Chọn trạng thái");

        Assert.assertTrue(dropdown.hasSelectContaining("Đang chờ"), "Thiếu trạng thái Đang chờ.");
        Assert.assertTrue(dropdown.hasSelectContaining("Thành công"), "Thiếu trạng thái Thành công.");
        Assert.assertTrue(dropdown.hasSelectContaining("Thất bại"), "Thiếu trạng thái Thất bại.");
        dropdown.close();
    }

    @Test(description = "CMS-BLOG-FILTER: Blog filter exposes language/category/date controls")
    public void blogFilterHasExpectedControls() {
        open(childOf("Website", "Quản Lí Bài Viết Nội Bộ"));
        OverlayFeaturesPage panel = new OverlayFeaturesPage(driver).open("Filter");

        Assert.assertTrue(panel.hasButton("Tiếng Việt"), "Thiếu lựa chọn Tiếng Việt.");
        Assert.assertTrue(panel.hasButton("English"), "Thiếu lựa chọn English.");
        Assert.assertTrue(panel.hasButton("Tất cả danh mục"), "Thiếu bộ lọc danh mục.");
        Assert.assertTrue(panel.hasButton("Previous Month"), "Thiếu bộ lọc ngày đăng.");
        panel.close();
    }

    @Test(description = "CMS-MARKETING-FILTER: Marketing filter exposes demographic controls")
    public void marketingFilterHasExpectedControls() {
        open(childOf("Marketing", "Thống Kê Thợ - Khách"));
        OverlayFeaturesPage panel = new OverlayFeaturesPage(driver).open("Bộ lọc");

        Assert.assertTrue(panel.hasText("Giới tính"), "Thiếu bộ lọc giới tính.");
        Assert.assertTrue(panel.hasText("Độ tuổi"), "Thiếu bộ lọc độ tuổi.");
        Assert.assertTrue(panel.hasButton("Chọn tỉnh thành"), "Thiếu chọn tỉnh thành.");
        Assert.assertTrue(panel.hasButton("Chọn quận huyện"), "Thiếu chọn quận huyện.");
        Assert.assertTrue(panel.hasText("Ngành nghề"), "Thiếu bộ lọc ngành nghề.");
        panel.close();
    }

    @Test(description = "CMS-PROFILE-POST-FILTER: Profile post reset waits for data reload")
    public void profilePostResetWaitsForReload() {
        open(childOf("Đối Tác - Thợ", "Quản Lí Bài Đăng"));
        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);

        features.openControl("Reset");
        PageLoadSynchronizer.waitForDataToSettle(driver);
    }

    @Test(description = "CMS-PROMOTION-FILTER: Promotion filter control opens safely")
    public void promotionFilterControlCanBeOpened() {
        open(childOf("Marketing", "Chương Trình Khuyến Mãi"));
        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);

        features.openFirstTextInput();
        features.closeOverlay();
        PageLoadSynchronizer.waitForDataToSettle(driver);
    }

    private void open(MenuTarget target) {
        new AuthenticationFlow(driver).openApplicationAndLogin();
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(target, false);
    }
}
