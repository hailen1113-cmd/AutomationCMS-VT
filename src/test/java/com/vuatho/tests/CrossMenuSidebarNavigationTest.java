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

/**
 * Xác nhận mỗi mục sidebar điều hướng đến đúng route và trang đích tải thành công.
 */
public class CrossMenuSidebarNavigationTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(CrossMenuSidebarNavigationTest.class, "ERP Cross-menu Suite", "ERP Cross-menu Navigation Tests");
    }

    /**
     * Thực hiện xử lý menu pages trong luồng kiểm thử.
     * @return kết quả menu pages sau khi xử lý
     */
    @DataProvider(name = "menuPages")
    public Object[][] menuPages() {
        return MenuCatalog.dataProviderRows();
    }

    /**
     * Cho biết có tái sử dụng cùng một WebDriver giữa các phương thức test hay không.
     * @return kết quả reuse driver between test methods sau khi xử lý
     */
    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    /**
     * Thực thi test “CMS-MENU: Menu destination loads and scrolls to the bottom” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param target giá trị target được truyền vào
     */
    @Test(dataProvider = "menuPages",
            groups = {"smoke", "navigation", "loadpage"},
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
        checks.assertTrue(page.urlMatchesExpectedDestination(),
                "URL sau khi click menu khong khop href. Expected: "
                        + page.expectedDestinationUrl() + " | Actual: " + page.currentUrl());
        checks.assertTrue(page.loadDuration().compareTo(TestConfig.pageLoadSla()) <= 0,
                "Trang load quá SLA: " + page.loadDuration().toMillis() + "ms");
        checks.assertTrue(health.isHealthy(), health.summary());
        checks.assertAll();
        System.out.printf("[PAGE LOADED] %s | %dms | %s%n",
                target, page.loadDuration().toMillis(), health.url());
    }
}
