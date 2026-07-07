package com.vuatho.tests;

import com.vuatho.config.GoogleCredentialProvider;
import com.vuatho.config.TestConfig;
import com.vuatho.core.BaseTest;
import com.vuatho.pages.DashboardPage;
import com.vuatho.pages.LoginPage;
import com.vuatho.reporting.ConsoleTestListener;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class DashboardTest extends BaseTest {
    private DashboardPage dashboard;

    public static void main(String[] args) {
        System.setProperty("headless", System.getProperty("headless", "false"));
        System.setProperty("interactive", System.getProperty("interactive", "true"));
        System.setProperty("pause.on.failure", System.getProperty("pause.on.failure", "false"));

        TestNG testNG = new TestNG();
        testNG.setDefaultSuiteName("ERP Dashboard Suite");
        testNG.setTestClasses(new Class<?>[]{DashboardTest.class});
        testNG.addListener(new ConsoleTestListener());
        testNG.run();
    }

    @BeforeMethod
    public void openDashboard(Method method) {
        dashboard = new DashboardPage(driver);
        if (!method.getName().equals("dashboardCannotBeAccessedAfterLogout")) {
            dashboard.open();
            if (!dashboard.hasDashboardMarker()) {
                LoginPage loginPage = new LoginPage(driver);
                loginPage.loginWithGoogle(
                        TestConfig.loginEmail(),
                        GoogleCredentialProvider::password);
            }
            Assert.assertTrue(dashboard.isLoaded(),
                    "Không thể tự đăng nhập vào Dashboard.");
        }
    }

    @Test(priority = 2, description = "CMS-DASH-002: Validate URL after login")
    public void dashboardUrlIsCorrect() {
        Assert.assertTrue(dashboard.hasValidDashboardUrl(), "URL Dashboard không đúng sandbox CMS.");
    }

    @Test(priority = 3, description = "CMS-DASH-003: Sidebar menu is displayed")
    public void sidebarMenuIsDisplayed() {
        Assert.assertTrue(dashboard.missingMenuGroups().isEmpty(),
                "Sidebar thiếu menu: " + dashboard.missingMenuGroups());
    }

    @Test(priority = 4, description = "CMS-DASH-004: Logo/app icon is displayed")
    public void logoIsDisplayedAndLoaded() {
        Assert.assertTrue(dashboard.isLogoLoaded(), "Logo sidebar không hiển thị hoặc bị vỡ ảnh.");
    }

    @Test(priority = 5, description = "CMS-DASH-005: Collapse sidebar")
    public void sidebarCanBeCollapsed() {
        double before = dashboard.sidebarWidth();
        dashboard.collapseSidebar();
        Assert.assertTrue(dashboard.sidebarWidth() < before, "Sidebar không thu gọn.");
    }

    @Test(priority = 6, description = "CMS-DASH-006: Expand collapsed sidebar")
    public void sidebarCanBeExpanded() {
        double originalWidth = dashboard.sidebarWidth();
        dashboard.collapseSidebar();
        dashboard.expandSidebar();
        Assert.assertTrue(dashboard.sidebarWidth() >= originalWidth, "Sidebar không mở rộng lại.");
    }

    @Test(priority = 7, description = "CMS-DASH-007: Dashboard menu is active")
    public void dashboardMenuIsActive() {
        Assert.assertTrue(dashboard.isDashboardMenuActive(), "Menu Dashboard chưa được highlight.");
    }

    @Test(priority = 8, description = "CMS-DASH-008: Company header is displayed")
    public void companyHeaderIsDisplayed() {
        Assert.assertTrue(dashboard.hasCompanyHeader(), "Không hiển thị header Công ty Vua Thợ.");
    }

    @Test(priority = 9, description = "CMS-DASH-009: Current user and DEV environment are displayed")
    public void currentUserIsDisplayed() {
        Assert.assertTrue(dashboard.hasCurrentUserAndEnvironment(),
                "Không hiển thị đúng user Hải hoặc môi trường DEV.");
    }

    @Test(priority = 10, description = "CMS-DASH-012: Reload keeps the session")
    public void reloadKeepsSession() {
        dashboard.reload();
        Assert.assertTrue(dashboard.hasDashboardMarker(), "Reload làm mất session Dashboard.");
    }

    @Test(priority = 11, description = "CMS-DASH-010: Logout successfully")
    public void logoutSuccessfully() {
        dashboard.logout();
        Assert.assertTrue(dashboard.isLoginVisible(), "Logout không quay về màn hình login.");
    }

    @Test(priority = 12, dependsOnMethods = "logoutSuccessfully",
            description = "CMS-DASH-011: Dashboard cannot be accessed after logout")
    public void dashboardCannotBeAccessedAfterLogout() {
        dashboard.open();
        Assert.assertFalse(dashboard.hasDashboardMarker(), "Vẫn truy cập được Dashboard sau logout.");
        Assert.assertTrue(dashboard.isLoginVisible(), "Không chuyển về màn hình login.");
    }
}
