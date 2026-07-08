package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.exploration.UiFeatureExplorer;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.navigation.MenuTarget;
import com.vuatho.pages.MenuDestinationPage;
import com.vuatho.pages.ReadOnlyFeaturesPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.vuatho.navigation.MenuTarget.childOf;

public class DeepFeatureDiscoveryTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(DeepFeatureDiscoveryTest.class,
                "ERP Deep Feature Discovery", "Read-only overlays and filters");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @DataProvider(name = "overlays")
    public Object[][] overlays() {
        return new Object[][]{
                {childOf("Người Dùng", "Quản Lí Người Dùng"), "Filter"},
                {childOf("Đơn Dịch Vụ", "Đơn Khách - Thợ"), "Bộ lọc đơn dịch vụ"},
                {childOf("Đơn Dịch Vụ", "Đơn Khách - Thợ"), "Thống kê"},
                {childOf("Giao Dịch", "Lịch Sử Giao Dịch"), "Chọn trạng thái"},
                {childOf("Website", "Quản Lí Bài Viết Nội Bộ"), "Filter"},
                {childOf("Marketing", "Thống Kê Thợ - Khách"), "Bộ lọc"}
        };
    }

    @Test(dataProvider = "overlays")
    public void inventoryOverlayFeatures(MenuTarget target, String controlLabel) {
        new AuthenticationFlow(driver).openApplicationAndLogin();
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(target, false);

        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);
        features.openControl(controlLabel);
        new UiFeatureExplorer(driver).printInventory(target + " > " + controlLabel);
        features.closeOverlay();
    }
}
