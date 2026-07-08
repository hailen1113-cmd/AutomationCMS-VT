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

public class ReadOnlyFeatureTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(ReadOnlyFeatureTest.class,
                "ERP Read-only Features", "Safe Functional Tests");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @Test(description = "CMS-USER-SEARCH: Search input can be reset")
    public void userSearchCanBeReset() {
        open(childOf("Người Dùng", "Quản Lí Người Dùng"));
        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);

        features.searchAndReset("Tìm kiếm người dùng", "__automation_no_match__");

        Assert.assertTrue(features.inputIsEmpty("Tìm kiếm người dùng"),
                "Reset không xóa nội dung tìm kiếm người dùng.");
    }

    @Test(description = "CMS-ORDER-VIEW: Order list can switch between table and card views")
    public void orderViewCanBeSwitched() {
        open(childOf("Đơn Dịch Vụ", "Đơn Khách - Thợ"));
        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);

        features.switchView("Thẻ");
        features.switchView("Bảng");
    }

    @Test(description = "CMS-TRANSACTION-TABS: Transaction tabs and pagination work")
    public void transactionTabsAndPaginationWork() {
        open(childOf("Giao Dịch", "Lịch Sử Giao Dịch"));
        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);

        features.openDropdownAndVerifyOption("Tiền nạp", "Nạp thường");
        features.goToPaginationPage("2");
    }

    @Test(description = "CMS-MARKETING-TABS: Client/worker and period tabs work")
    public void marketingAudienceAndPeriodTabsWork() {
        open(childOf("Marketing", "Thống Kê Thợ - Khách"));
        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);

        String workerUrl = features.switchRouteTab("THỢ");
        String clientUrl = features.switchRouteTab("KHÁCH");
        Assert.assertNotEquals(clientUrl, workerUrl,
                "Tab KHÁCH và THỢ không thay đổi route.");

        features.switchView("Tuần");
        features.switchView("Tháng");
    }

    private void open(MenuTarget target) {
        new AuthenticationFlow(driver).openApplicationAndLogin();
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(target, false);
    }
}
