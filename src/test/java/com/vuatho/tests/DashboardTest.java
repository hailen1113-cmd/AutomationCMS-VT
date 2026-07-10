package com.vuatho.tests;

import com.vuatho.config.GoogleCredentialProvider;
import com.vuatho.config.TestConfig;
import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.DashboardPage;
import com.vuatho.pages.LoginPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

public class DashboardTest extends BaseTest {
    private DashboardPage dashboard;

    public static void main(String[] args) {
        if (Boolean.getBoolean("dashboard.only")) {
            TestNgRunner.run(DashboardTest.class, "ERP Dashboard Suite", "ERP Dashboard Test");
            return;
        }
        System.out.println("DashboardTest.main() is running the full ERP suite. "
                + "Use -Ddashboard.only=true to run only dashboard checks.");
        ErpFullFlowTest.runSuite();
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @BeforeMethod
    public void openDashboard(Method method) {
        dashboard = new DashboardPage(driver);
        if (!method.getName().equals("dashboardCannotBeAccessedAfterLogout")) {
            if (!dashboard.hasDashboardMarker()) {
                dashboard.open();
            }
            if (!dashboard.hasDashboardMarker()) {
                LoginPage loginPage = new LoginPage(driver);
                loginPage.loginWithGoogle(
                        TestConfig.loginEmail(),
                        GoogleCredentialProvider::password);
            }
            Assert.assertTrue(dashboard.isLoaded(),
                    "Không thể tự đăng nhập vào Dashboard.");
            if (!dashboard.hasValidDashboardUrl()) {
                dashboard.openDashboardAndWaitForSummaryCards();
            }
            Assert.assertTrue(dashboard.hasValidDashboardUrl(),
                    "Dashboard test is running on the wrong route: " + driver.getCurrentUrl());
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
        dashboard.ensureSidebarExpanded();
        double before = dashboard.sidebarWidth();
        dashboard.collapseSidebar();
        Assert.assertTrue(dashboard.sidebarWidth() < before, "Sidebar không thu gọn.");
    }

    @Test(priority = 6, description = "CMS-DASH-006: Expand collapsed sidebar")
    public void sidebarCanBeExpanded() {
        dashboard.ensureSidebarExpanded();
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

    @Test(priority = 10,
            description = "CMS-DASH-CARDS-001: Dashboard summary cards navigate and load successfully")
    public void dashboardSummaryCardsLoadDestinations() {
        List<String> cards = dashboard.clickSummaryCardsAndWaitForDestinations();

        Assert.assertFalse(cards.isEmpty(), "No dashboard summary cards were loaded.");
    }

    @Test(priority = 11, description = "CMS-DASH-012: Session remains available without page reload")
    public void sessionRemainsAvailableWithoutReload() {
        Assert.assertTrue(dashboard.hasDashboardMarker(), "Dashboard session is no longer available.");
    }

    @DataProvider(name = "overviewCards", parallel = false)
    public Object[][] overviewCards() {
        return new Object[][]{
                {"OVD-001", "Đơn dịch vụ"},
                {"OVD-002", "Số lượng người dùng"},
                {"OVD-003", "Số lượng thợ"},
                {"OVD-004", "Nghiệp vụ"},
                {"OVD-005", "Ngành nghề"},
                {"OVD-006", "Nền tảng Vua Thợ"}
        };
    }

    @Test(priority = 11, dataProvider = "overviewCards",
            description = "Workbook OVD-001..006: overview card is visible and has a numeric value")
    public void overviewCardHasNumericValue(String caseId, String label) {
        Assert.assertTrue(dashboard.summaryCardHasNumericValue(label),
                caseId + " missing or does not contain a numeric value: " + label);
    }

    @DataProvider(name = "dashboardSections", parallel = false)
    public Object[][] dashboardSections() {
        return new Object[][]{
                {"GCC-001", "Vua Thợ Trên Toàn Cầu"},
                {"UST-001", "Danh Sách Người Dùng"},
                {"INS-001", "Danh Sách Ngành Nghề"}
        };
    }

    @Test(priority = 11, dataProvider = "dashboardSections",
            description = "Workbook dashboard section is displayed")
    public void dashboardSectionIsDisplayed(String caseId, String title) {
        Assert.assertTrue(dashboard.sectionIsVisible(title),
                caseId + " section is not displayed: " + title);
    }

    @DataProvider(name = "periodSelections", parallel = false)
    public Object[][] periodSelections() {
        return new Object[][]{
                {"UST-004", 0, "Ngày"}, {"UST-005", 0, "Tuần"},
                {"UST-006", 0, "Tháng"}, {"UST-007", 0, "Quý"},
                {"UST-008", 0, "Năm"}, {"INS-004", 1, "Ngày"},
                {"INS-005", 1, "Tuần"}, {"INS-006", 1, "Tháng"},
                {"INS-007", 1, "Quý"}, {"INS-008", 1, "Năm"}
        };
    }

    @Test(priority = 11, dataProvider = "periodSelections",
            description = "Workbook period selector changes its active state")
    public void dashboardPeriodCanBeSelected(String caseId, int groupIndex, String label) {
        dashboard.selectPeriod(groupIndex, label);
        Assert.assertTrue(dashboard.periodIsSelected(groupIndex, label),
                caseId + " period did not become active: " + label);
    }

    @Test(priority = 12, description = "CMS-DASH-010: Logout successfully")
    public void logoutSuccessfully() {
        skipUnlessLogoutTestsAreEnabled();
        dashboard.logout();
        Assert.assertTrue(dashboard.isLoginVisible(), "Logout không quay về màn hình login.");
    }

    @Test(priority = 13, dependsOnMethods = "logoutSuccessfully",
            description = "CMS-DASH-011: Dashboard cannot be accessed after logout")
    public void dashboardCannotBeAccessedAfterLogout() {
        skipUnlessLogoutTestsAreEnabled();
        dashboard.open();
        Assert.assertFalse(dashboard.hasDashboardMarker(), "Vẫn truy cập được Dashboard sau logout.");
        Assert.assertTrue(dashboard.isLoginVisible(), "Không chuyển về màn hình login.");
    }
    private void skipUnlessLogoutTestsAreEnabled() {
        if (!TestConfig.runLogoutTests()) {
            throw new SkipException("Logout tests are disabled by default to preserve the logged-in session.");
        }
    }
}
