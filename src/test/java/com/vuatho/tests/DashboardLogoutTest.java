package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DashboardLogoutTest extends DashboardTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(DashboardLogoutTest.class,
                "Bo test logout Dashboard ERP",
                "Kiem tra logout va chan truy cap sau logout");
    }

    @Test(priority = 1, description = "CMS-DASH-010: Logout thanh cong")
    public void logoutSuccessfully() {
        skipUnlessLogoutTestsAreEnabled();
        dashboard.logout();
        Assert.assertTrue(dashboard.isLoginVisible(), "Logout khong quay ve man hinh login.");
    }

    @Test(priority = 2, dependsOnMethods = "logoutSuccessfully",
            description = "CMS-DASH-011: Khong truy cap duoc Dashboard sau logout")
    public void dashboardCannotBeAccessedAfterLogout() {
        skipUnlessLogoutTestsAreEnabled();
        dashboard.open();
        Assert.assertFalse(dashboard.hasDashboardMarker(), "Van truy cap duoc Dashboard sau logout.");
        Assert.assertTrue(dashboard.isLoginVisible(), "Khong chuyen ve man hinh login.");
    }
}
