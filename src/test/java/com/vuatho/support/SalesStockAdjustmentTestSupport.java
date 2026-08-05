package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.SalesStockAdjustmentPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Chuẩn bị phiên đăng nhập cho nghiệp vụ Kho bán hàng → Điều chỉnh tồn. */
public abstract class SalesStockAdjustmentTestSupport extends BaseTest {
    protected SalesStockAdjustmentPage adjustmentPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareSalesStockAdjustment() {
        if (driver == null) {
            throw new SkipException("WebDriver không khởi tạo được.");
        }
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra Điều chỉnh tồn Kho bán hàng.");
        }
        adjustmentPage = new SalesStockAdjustmentPage(driver);
    }
}
