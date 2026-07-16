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

public class LoginDashboardSourceAccessTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(
                "ERP Login and Dashboard Suite",
                "Login and Dashboard Checks",
                LoginDashboardSourceAccessTest.class);
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @Test(priority = 1, description = "CMS-DASH-001: Login CMS successfully with Google")
    public void loginSuccessfully() {
        PageHealthChecker healthChecker = new PageHealthChecker(driver);
        healthChecker.startObservation();
        LoginPage loginPage = openAndLogin();

        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Login did not reach Dashboard.");
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

        Assert.assertFalse(metrics.isEmpty(), "Dashboard opened but metrics were not displayed.");
        assertHealthy(healthChecker.inspect());
    }

    @Test(priority = 3,
            description = "CMS-SOURCE-001: Source efficiency and cost page loads successfully")
    public void sourceEfficiencyPageLoadsSuccessfully() {
        openAndLogin();
        DashboardPage dashboard = new DashboardPage(driver);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard is not ready before opening source efficiency page.");

        PageHealthChecker healthChecker = new PageHealthChecker(driver);
        healthChecker.startObservation();
        SourceEfficiencyPage sourceEfficiencyPage = new SourceEfficiencyPage(driver)
                .openAndWaitUntilLoaded();

        Assert.assertTrue(sourceEfficiencyPage.isLoaded(), "Source efficiency page did not load successfully.");
        assertHealthy(healthChecker.inspect());
        System.out.println("[PAGE LOADED] Source efficiency and cost");
    }

    private LoginPage openAndLogin() {
        return new AuthenticationFlow(driver).openApplicationAndLogin();
    }

    private void assertHealthy(PageHealthReport health) {
        Assert.assertTrue(health.isHealthy(), health.summary());
    }
}
