package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.CustomerWorkerOrderPage;
import com.vuatho.pages.LoginPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Setup/cleanup dùng chung, không chứa testcase và không chạy trực tiếp. */
abstract class CustomerWorkerOrderTestSupport extends BaseTest {
    protected CustomerWorkerOrderPage orderPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareOrderPage() {
        if (driver == null) throw new SkipException("WebDriver không khởi tạo được.");
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra Đơn Khách - Thợ.");
        }
        orderPage = new CustomerWorkerOrderPage(driver).open();
    }

    @AfterMethod(alwaysRun = true)
    public void closeOrderOverlays() {
        if (orderPage == null) return;
        try {
            orderPage.closeOverlay();
        } catch (RuntimeException ignored) {
            // Test kế tiếp mở lại route nên cleanup không che lỗi nghiệp vụ.
        }
    }
}
