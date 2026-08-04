package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.SalesStockPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Setup và cleanup dùng chung cho testcase Kho bán hàng → Tồn kho. */
public abstract class SalesStockTestSupport extends BaseTest {
    protected SalesStockPage salesStockPage;

    /** Đảm bảo có phiên đăng nhập và khởi tạo Page Object đúng kho. */
    @BeforeMethod(alwaysRun = true)
    public void prepareSalesStock() {
        if (driver == null) {
            throw new SkipException("WebDriver không khởi tạo được.");
        }
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra Kho bán hàng.");
        }
        salesStockPage = new SalesStockPage(driver);
    }

    /** Đóng drawer còn mở để testcase sau bắt đầu từ trạng thái sạch. */
    @AfterMethod(alwaysRun = true)
    public void closeSalesStockOverlay() {
        if (driver == null || salesStockPage == null) {
            return;
        }
        try {
            salesStockPage.closeOverlay();
        } catch (RuntimeException ignored) {
            // Testcase sau mở lại route nên cleanup không che lỗi nghiệp vụ.
        }
    }
}
