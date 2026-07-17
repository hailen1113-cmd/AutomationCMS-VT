package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.navigation.MenuTarget;
import com.vuatho.pages.MenuDestinationPage;
import com.vuatho.pages.ReadOnlyFeaturesPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.vuatho.navigation.MenuTarget.childOf;

/**
 * Kiểm tra các control chỉ đọc và thao tác không làm thay đổi dữ liệu trên nhiều menu.
 */
public class CrossMenuReadOnlyInteractionTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(CrossMenuReadOnlyInteractionTest.class,
                "ERP Cross-menu Read-only Features",
                "Safe cross-menu functional tests");
    }

    /**
     * Cho biết có tái sử dụng cùng một WebDriver giữa các phương thức test hay không.
     * @return kết quả reuse driver between test methods sau khi xử lý
     */
    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    /**
     * Thực thi test “CMS-ORDER-VIEW: Order list can switch between table and card views” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "CMS-ORDER-VIEW: Order list can switch between table and card views")
    public void orderViewCanBeSwitched() {
        open(childOf("Đơn Dịch Vụ", "Đơn Khách - Thợ"));
        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);

        features.switchView("Thẻ");
        features.switchView("Bảng");
    }

    /**
     * Thực thi test “CMS-TRANSACTION-TABS: Transaction tabs and pagination work” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "CMS-TRANSACTION-TABS: Transaction tabs and pagination work")
    public void transactionTabsAndPaginationWork() {
        open(childOf("Giao Dịch", "Lịch Sử Giao Dịch"));
        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);

        features.openDropdownAndVerifyOption("Tiền nạp", "Nạp thường");
        features.goToPaginationPage("2");
    }

    /**
     * Thực thi test “CMS-MARKETING-TABS: Client/worker and period tabs work” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "CMS-MARKETING-TABS: Client/worker and period tabs work")
    public void marketingAudienceAndPeriodTabsWork() {
        open(childOf("Marketing", "Thống Kê Thợ - Khách"));
        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);

        String workerUrl = features.switchRouteTab("THỢ");
        String clientUrl = features.switchRouteTab("KHÁCH");
        Assert.assertNotEquals(clientUrl, workerUrl,
                "Client and worker tabs did not change route.");

        features.switchView("Tuần");
        features.switchView("Tháng");
    }

    /**
     * Mở  trong luồng kiểm thử.
     * @param target giá trị target được truyền vào
     */
    private void open(MenuTarget target) {
        new AuthenticationFlow(driver).openApplicationAndLogin();
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(target, false);
    }
}
