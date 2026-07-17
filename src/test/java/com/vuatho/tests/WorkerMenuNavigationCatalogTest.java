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

/**
 * Xác nhận catalog menu Đối tác - Thợ đầy đủ và từng menu điều hướng đúng trang.
 */
public class WorkerMenuNavigationCatalogTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerMenuNavigationCatalogTest.class,
                "ERP Worker Menu Suite",
                "Bộ testcase menu liên quan đến thợ");
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
     * Thực hiện xử lý partner worker cases trong luồng kiểm thử.
     * @return kết quả partner worker cases sau khi xử lý
     */
    @DataProvider(name = "partnerWorkerCases", parallel = false)
    public Object[][] partnerWorkerCases() {
        return PartnerWorkerTestData.dataProviderRows();
    }

    /**
     * Thực thi test “Danh sách kịch bản Đối Tác - Thợ” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param testCase test case đang thực thi
     */
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
