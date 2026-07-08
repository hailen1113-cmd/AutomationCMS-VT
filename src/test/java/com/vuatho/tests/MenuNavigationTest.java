package com.vuatho.tests;

import com.vuatho.config.TestConfig;
import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.MenuDestinationPage;
import com.vuatho.navigation.MenuTarget;
import com.vuatho.quality.PageHealthChecker;
import com.vuatho.quality.PageHealthReport;
import com.vuatho.testdata.MenuCatalog;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.Duration;

public class MenuNavigationTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(MenuNavigationTest.class, "ERP Menu Suite", "ERP Menu Navigation Tests");
    }

    @DataProvider(name = "menuPages")
    public Object[][] menuPages() {
        return MenuCatalog.dataProviderRows();
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @Test(dataProvider = "menuPages",
            description = "CMS-MENU: Menu destination loads and scrolls to the bottom")
    public void menuDestinationLoadsSuccessfully(MenuTarget target) {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Không thể đăng nhập trước khi kiểm tra menu.");

        PageHealthChecker healthChecker = new PageHealthChecker(driver);
        healthChecker.startObservation();
        MenuDestinationPage page = new MenuDestinationPage(driver)
                .openAndWaitUntilLoaded(target);
        PageHealthReport health = healthChecker.inspect();

        SoftAssert checks = new SoftAssert();
        checks.assertTrue(page.isLoaded(), "Trang " + target + " chưa load thành công.");
        checks.assertTrue(page.loadDuration().compareTo(TestConfig.pageLoadSla()) <= 0,
                "Trang load quá SLA: " + page.loadDuration().toMillis() + "ms");
        checks.assertTrue(health.isHealthy(), health.summary());
        checks.assertAll();
        System.out.printf("[PAGE LOADED] %s | %dms | %s%n",
                target, page.loadDuration().toMillis(), health.url());
    }
}
