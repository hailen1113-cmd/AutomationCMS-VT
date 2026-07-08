package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.DashboardPage;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.SourceEfficiencyPage;
import com.vuatho.quality.PageHealthChecker;
import com.vuatho.quality.PageHealthReport;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class ErpLoginTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(
                "ERP End-to-End Suite",
                "Login, Dashboard and Menu Navigation",
                ErpLoginTest.class,
                MenuNavigationTest.class,
                ReadOnlyFeatureTest.class,
                DeepReadOnlyFeatureTest.class,
                FilterBehaviorTest.class);
    }

    @Test(priority = 1, description = "CMS-DASH-001: Login CMS successfully with Google")
    public void loginSuccessfully() {
        PageHealthChecker healthChecker = new PageHealthChecker(driver);
        healthChecker.startObservation();
        LoginPage loginPage = openAndLogin();

        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Đăng nhập không thành công: không tìm thấy Dashboard/Công ty Vua Thợ.");
        assertHealthy(healthChecker.inspect());
    }

    @Test(priority = 2, description = "CMS-DASH-LOAD-001: Dashboard loads successfully")
    public void dashboardLoadsSuccessfully() {
        openAndLogin();

        PageHealthChecker healthChecker = new PageHealthChecker(driver);
        healthChecker.startObservation();
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.openDashboardAndWaitForMetrics();
        List<String> metrics = dashboard.loadedMetrics();
        System.out.printf("%n[DASHBOARD METRICS] Loaded %d values:%n", metrics.size());
        metrics.forEach(metric -> System.out.println("  - " + metric));

        Assert.assertFalse(metrics.isEmpty(),
                "Dashboard đã mở nhưng các chỉ số chưa hiển thị.");
        assertHealthy(healthChecker.inspect());
    }

    @Test(priority = 3,
            description = "CMS-SOURCE-001: Source efficiency and cost page loads successfully")
    public void sourceEfficiencyPageLoadsSuccessfully() {
        openAndLogin();
        DashboardPage dashboard = new DashboardPage(driver);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard chưa sẵn sàng để mở menu.");

        PageHealthChecker healthChecker = new PageHealthChecker(driver);
        healthChecker.startObservation();
        SourceEfficiencyPage sourceEfficiencyPage = new SourceEfficiencyPage(driver)
                .openAndWaitUntilLoaded();

        Assert.assertTrue(sourceEfficiencyPage.isLoaded(),
                "Trang Hiệu Quả Nguồn Thợ & Chi Phí chưa load thành công.");
        assertHealthy(healthChecker.inspect());
        System.out.println("[PAGE LOADED] Hiệu Quả Nguồn Thợ & Chi Phí");
    }

    private LoginPage openAndLogin() {
        return new AuthenticationFlow(driver).openApplicationAndLogin();
    }

    private void assertHealthy(PageHealthReport health) {
        Assert.assertTrue(health.isHealthy(), health.summary());
    }
}
