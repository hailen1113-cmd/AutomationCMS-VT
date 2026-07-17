package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Kiểm tra các thành phần giao diện cốt lõi của Dashboard sau khi đăng nhập.
 */
public class DashboardCoreUiTest extends DashboardTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(DashboardCoreUiTest.class,
                "Bo test UI Dashboard ERP",
                "Kiem tra UI Dashboard co ban");
    }

    /**
     * Thực thi test “CMS-DASH-002: URL Dashboard dung sau dang nhap” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 1, description = "CMS-DASH-002: URL Dashboard dung sau dang nhap")
    public void dashboardUrlIsCorrect() {
        Assert.assertTrue(dashboard.hasValidDashboardUrl(), "URL Dashboard khong dung sandbox CMS.");
    }

    /**
     * Thực thi test “CMS-DASH-003: Sidebar menu hien thi” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 2, description = "CMS-DASH-003: Sidebar menu hien thi")
    public void sidebarMenuIsDisplayed() {
        Assert.assertTrue(dashboard.missingMenuGroups().isEmpty(),
                "Sidebar thieu menu: " + dashboard.missingMenuGroups());
    }

    /**
     * Thực thi test “CMS-DASH-004: Logo/app icon hien thi” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 3, description = "CMS-DASH-004: Logo/app icon hien thi")
    public void logoIsDisplayedAndLoaded() {
        Assert.assertTrue(dashboard.isLogoLoaded(), "Logo sidebar khong hien thi hoac bi vo anh.");
    }

    /**
     * Thực thi test “CMS-DASH-005: Thu gon sidebar” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 4, description = "CMS-DASH-005: Thu gon sidebar")
    public void sidebarCanBeCollapsed() {
        dashboard.ensureSidebarExpanded();
        double before = dashboard.sidebarWidth();
        dashboard.collapseSidebar();
        Assert.assertTrue(dashboard.sidebarWidth() < before, "Sidebar khong thu gon.");
    }

    /**
     * Thực thi test “CMS-DASH-006: Mo rong sidebar sau khi thu gon” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 5, description = "CMS-DASH-006: Mo rong sidebar sau khi thu gon")
    public void sidebarCanBeExpanded() {
        dashboard.ensureSidebarExpanded();
        double originalWidth = dashboard.sidebarWidth();
        dashboard.collapseSidebar();
        dashboard.expandSidebar();
        Assert.assertTrue(dashboard.sidebarWidth() >= originalWidth, "Sidebar khong mo rong lai.");
    }

    /**
     * Thực thi test “CMS-DASH-007: Menu Dashboard dang active” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 6, description = "CMS-DASH-007: Menu Dashboard dang active")
    public void dashboardMenuIsActive() {
        Assert.assertTrue(dashboard.isDashboardMenuActive(), "Menu Dashboard chua duoc highlight.");
    }

    /**
     * Thực thi test “CMS-DASH-008: Header cong ty hien thi” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 7, description = "CMS-DASH-008: Header cong ty hien thi")
    public void companyHeaderIsDisplayed() {
        Assert.assertTrue(dashboard.hasCompanyHeader(), "Khong hien thi header Cong ty Vua Tho.");
    }

    /**
     * Thực thi test “CMS-DASH-009: User hien tai va moi truong DEV hien thi” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 8, description = "CMS-DASH-009: User hien tai va moi truong DEV hien thi")
    public void currentUserIsDisplayed() {
        Assert.assertTrue(dashboard.hasCurrentUserAndEnvironment(),
                "Khong hien thi dung user Hai hoac moi truong DEV.");
    }

    /**
     * Thực thi test “CMS-DASH-012: Session con hieu luc khong can reload” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 9, description = "CMS-DASH-012: Session con hieu luc khong can reload")
    public void sessionRemainsAvailableWithoutReload() {
        Assert.assertTrue(dashboard.hasDashboardMarker(), "Session Dashboard khong con hieu luc.");
    }
}
