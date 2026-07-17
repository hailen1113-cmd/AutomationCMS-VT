package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.UserProfilePage;
import com.vuatho.testdata.UserProfileCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Cung cấp thiết lập, dữ liệu và thao tác dùng chung cho các test hồ sơ người dùng.
 */
abstract class UserProfileTestSupport extends BaseTest {
    protected UserProfilePage userProfilePage;

    /**
     * Cho biết có tái sử dụng cùng một WebDriver giữa các phương thức test hay không.
     * @return kết quả reuse driver between test methods sau khi xử lý
     */
    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    /**
     * Thực hiện xử lý prepare authenticated session trong luồng kiểm thử.
     */
    @BeforeMethod(alwaysRun = true)
    public void prepareAuthenticatedSession() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Khong dang nhap duoc truoc khi kiem tra ho so nguoi dung.");
        userProfilePage = new UserProfilePage(driver);
    }

    /**
     * Thực hiện xử lý matches configured case trong luồng kiểm thử.
     * @param testCase test case đang thực thi
     * @return kết quả matches configured case sau khi xử lý
     */
    protected boolean matchesConfiguredCase(UserProfileCase testCase) {
        String configuredCaseId = configured("user.case.id", "USER_CASE_ID");
        return configuredCaseId.isBlank() || testCase.id().equalsIgnoreCase(configuredCaseId);
    }

    /**
     * Thực hiện xử lý configured trong luồng kiểm thử.
     * @param property giá trị property được truyền vào
     * @param environmentVariable giá trị environment variable được truyền vào
     * @return kết quả configured sau khi xử lý
     */
    protected String configured(String property, String environmentVariable) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        return value == null ? "" : value.trim();
    }
}
