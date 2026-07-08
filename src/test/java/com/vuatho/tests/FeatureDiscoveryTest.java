package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.exploration.UiFeatureExplorer;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.navigation.MenuTarget;
import com.vuatho.pages.MenuDestinationPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.vuatho.navigation.MenuTarget.childOf;
import static com.vuatho.navigation.MenuTarget.topLevel;

public class FeatureDiscoveryTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(FeatureDiscoveryTest.class,
                "ERP Feature Discovery", "Read-only UI Feature Inventory");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @DataProvider(name = "representativePages")
    public Object[][] representativePages() {
        return new Object[][]{
                {topLevel("Tài chính")},
                {childOf("Người Dùng", "Quản Lí Người Dùng")},
                {childOf("Đơn Dịch Vụ", "Đơn Khách - Thợ")},
                {childOf("Giao Dịch", "Lịch Sử Giao Dịch")},
                {childOf("Website", "Quản Lí Bài Viết Nội Bộ")},
                {childOf("Marketing", "Thống Kê Thợ - Khách")}
        };
    }

    @Test(dataProvider = "representativePages")
    public void inventoryVisibleFeatures(MenuTarget target) {
        new AuthenticationFlow(driver).openApplicationAndLogin();
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(target, false);
        new UiFeatureExplorer(driver).printInventory(target.toString());
    }
}
