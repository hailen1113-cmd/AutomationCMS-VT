package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.UserManagementPage;
import com.vuatho.testdata.UserManagementCase;
import com.vuatho.testdata.UserManagementFeature;
import com.vuatho.testdata.UserManagementTestData;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class UserManagementTest extends BaseTest {
    private static final String NAME_OTHER_REJECT_REASON =
            "Automation rejects the full-name update with another reason.";
    private static final String AVATAR_OTHER_REJECT_REASON =
            "Automation rejects the avatar update with another reason.";

    private UserManagementPage userManagementPage;

    public static void main(String[] args) {
        TestNgRunner.run(UserManagementTest.class,
                "ERP User Management Suite",
                "User management testcase suite");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareAuthenticatedSession() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Cannot sign in before checking user management.");
        userManagementPage = new UserManagementPage(driver);
    }

    @DataProvider(name = "userManagementCases", parallel = false)
    public Object[][] userManagementCases() {
        List<UserManagementCase> cases = UserManagementTestData.cases().stream()
                .filter(this::matchesConfiguredCase)
                .toList();
        Object[][] rows = new Object[cases.size()][1];
        for (int index = 0; index < cases.size(); index++) {
            rows[index][0] = cases.get(index);
        }
        return rows;
    }

    @Test(dataProvider = "userManagementCases",
            groups = {"user-management"},
            description = "User management testcase catalog")
    public void runUserManagementCase(UserManagementCase testCase) {
        userManagementPage.openFromMenu();
        if (!userManagementPage.hasUserRows()) {
            throw new SkipException(testCase.id() + ": No user row is available.");
        }

        if (testCase.viewsInformation()) {
            viewFirstUserInformation(testCase);
            return;
        }
        if (testCase.feature() == UserManagementFeature.NAME_UPDATE) {
            runNameUpdateCase(testCase);
            return;
        }
        if (testCase.feature() == UserManagementFeature.AVATAR_UPDATE) {
            runAvatarUpdateCase(testCase);
            return;
        }

        throw new SkipException(testCase.id() + ": Unsupported user management testcase.");
    }

    private void viewFirstUserInformation(UserManagementCase testCase) {
        String firstRowText = userManagementPage.firstUserRowText();
        userManagementPage.openFirstUserDetail();
        Assert.assertTrue(userManagementPage.userDetailIsOpen(),
                testCase.id() + ": User information detail should open. Row: " + firstRowText);
        Assert.assertFalse(userManagementPage.detailText().isBlank(),
                testCase.id() + ": User information detail opened but has no visible content.");
    }

    private void runNameUpdateCase(UserManagementCase testCase) {
        userManagementPage.openUserWithPendingNameUpdateRequest();
        if (!userManagementPage.hasPendingNameUpdateRequest()) {
            throw new SkipException(testCase.id() + ": No pending full-name update request is available.");
        }
        if (testCase.approvesUpdate()) {
            userManagementPage.approveNameUpdateRequest();
        } else if (testCase.rejectsUpdate()) {
            rejectNameUpdate(testCase);
        } else {
            throw new SkipException(testCase.id() + ": Unsupported full-name update decision.");
        }
    }

    private void rejectNameUpdate(UserManagementCase testCase) {
        if (testCase.usesOtherRejectReason()) {
            userManagementPage.rejectNameUpdateRequestWithOtherReason(NAME_OTHER_REJECT_REASON);
        } else {
            userManagementPage.rejectNameUpdateRequestWithDefaultReason();
        }
    }

    private void runAvatarUpdateCase(UserManagementCase testCase) {
        userManagementPage.openUserWithPendingAvatarUpdateRequest();
        if (!userManagementPage.hasPendingAvatarUpdateRequest()) {
            throw new SkipException(testCase.id() + ": No pending avatar update request is available.");
        }
        if (testCase.approvesUpdate()) {
            userManagementPage.approveAvatarUpdateRequest();
        } else if (testCase.rejectsUpdate()) {
            rejectAvatarUpdate(testCase);
        } else {
            throw new SkipException(testCase.id() + ": Unsupported avatar update decision.");
        }
    }

    private void rejectAvatarUpdate(UserManagementCase testCase) {
        if (testCase.usesOtherRejectReason()) {
            userManagementPage.rejectAvatarUpdateRequestWithOtherReason(AVATAR_OTHER_REJECT_REASON);
        } else {
            userManagementPage.rejectAvatarUpdateRequestWithDefaultReason();
        }
    }

    private boolean matchesConfiguredCase(UserManagementCase testCase) {
        String configuredCaseId = configured("user.case.id", "USER_CASE_ID");
        return configuredCaseId.isBlank() || testCase.id().equalsIgnoreCase(configuredCaseId);
    }

    private String configured(String property, String environmentVariable) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        return value == null ? "" : value.trim();
    }
}
