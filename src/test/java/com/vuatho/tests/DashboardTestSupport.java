package com.vuatho.tests;

import com.vuatho.config.GoogleCredentialProvider;
import com.vuatho.config.TestConfig;
import com.vuatho.core.BaseTest;
import com.vuatho.pages.DashboardPage;
import com.vuatho.pages.LoginPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

/**
 * Cung cấp thiết lập, dữ liệu và thao tác dùng chung cho các test Dashboard.
 */
abstract class DashboardTestSupport extends BaseTest {
    protected DashboardPage dashboard;

    /**
     * Cho biết có tái sử dụng cùng một WebDriver giữa các phương thức test hay không.
     * @return kết quả reuse driver between test methods sau khi xử lý
     */
    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    /**
     * Mở dashboard trong luồng kiểm thử.
     * @param method giá trị method được truyền vào
     */
    @BeforeMethod
    public void openDashboard(Method method) {
        dashboard = new DashboardPage(driver);
        if (!method.getName().equals("dashboardCannotBeAccessedAfterLogout")) {
            if (!dashboard.hasDashboardMarker()) {
                dashboard.open();
            }
            if (!dashboard.hasDashboardMarker()) {
                if (TestConfig.loginPassword().isBlank() && !TestConfig.interactive()) {
                    throw new SkipException(
                            "Can session dang nhap hoac GOOGLE_PASSWORD/ERP_PASSWORD de kiem tra Dashboard.");
                }
                LoginPage loginPage = new LoginPage(driver);
                loginPage.loginWithGoogle(
                        TestConfig.loginEmail(),
                        GoogleCredentialProvider::password);
            }
            Assert.assertTrue(dashboard.isLoaded(),
                    "Khong the tu dang nhap vao Dashboard.");
            if (!dashboard.hasValidDashboardUrl()) {
                dashboard.openDashboardAndWaitForSummaryCards();
            }
            Assert.assertTrue(dashboard.hasValidDashboardUrl(),
                    "Test Dashboard dang chay sai route: " + driver.getCurrentUrl());
        }
    }

    /**
     * Thực hiện xử lý skip unless logout tests are enabled trong luồng kiểm thử.
     */
    protected void skipUnlessLogoutTestsAreEnabled() {
        if (!TestConfig.runLogoutTests()) {
            throw new SkipException("Mac dinh tat test logout de giu session dang nhap.");
        }
    }
}
