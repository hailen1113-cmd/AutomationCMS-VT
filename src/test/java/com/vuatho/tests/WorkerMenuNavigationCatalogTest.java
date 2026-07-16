package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.MenuDestinationPage;
import com.vuatho.testdata.PartnerWorkerCase;
import com.vuatho.testdata.PartnerWorkerTestData;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;

public class WorkerMenuNavigationCatalogTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerMenuNavigationCatalogTest.class,
                "ERP Worker Menu Suite",
                "Bộ testcase menu liên quan đến thợ");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @DataProvider(name = "partnerWorkerCases", parallel = false)
    public Object[][] partnerWorkerCases() {
        return PartnerWorkerTestData.dataProviderRows();
    }

    @Test(dataProvider = "partnerWorkerCases",
            groups = {"partner-worker"},
            description = "Danh sách kịch bản Đối Tác - Thợ")
    public void runWorkerMenuNavigationCase(PartnerWorkerCase testCase) {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Không thể đăng nhập trước khi kiểm tra Đối Tác - Thợ.");

        MenuDestinationPage page = new MenuDestinationPage(driver)
                .openAndWaitUntilLoaded(testCase.page(), false);

        Assert.assertTrue(page.isLoaded(),
                testCase.id() + ": Trang chưa load thành công: " + testCase.page());
        Assert.assertTrue(page.urlMatchesExpectedDestination(),
                testCase.id() + ": URL sau khi click menu không khớp. Expected: "
                        + page.expectedDestinationUrl() + " | Actual: " + page.currentUrl());
    }
}
