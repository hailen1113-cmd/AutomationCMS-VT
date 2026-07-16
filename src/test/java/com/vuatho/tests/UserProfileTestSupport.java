package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.UserProfilePage;
import com.vuatho.testdata.UserProfileCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

abstract class UserProfileTestSupport extends BaseTest {
    protected UserProfilePage userProfilePage;

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareAuthenticatedSession() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Khong dang nhap duoc truoc khi kiem tra ho so nguoi dung.");
        userProfilePage = new UserProfilePage(driver);
    }

    protected boolean matchesConfiguredCase(UserProfileCase testCase) {
        String configuredCaseId = configured("user.case.id", "USER_CASE_ID");
        return configuredCaseId.isBlank() || testCase.id().equalsIgnoreCase(configuredCaseId);
    }

    protected String configured(String property, String environmentVariable) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        return value == null ? "" : value.trim();
    }
}
