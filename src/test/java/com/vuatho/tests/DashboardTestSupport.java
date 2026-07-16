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

abstract class DashboardTestSupport extends BaseTest {
    protected DashboardPage dashboard;

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

    protected void skipUnlessLogoutTestsAreEnabled() {
        if (!TestConfig.runLogoutTests()) {
            throw new SkipException("Mac dinh tat test logout de giu session dang nhap.");
        }
    }
}
