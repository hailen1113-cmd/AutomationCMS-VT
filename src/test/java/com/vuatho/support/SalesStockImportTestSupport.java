package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.SalesStockImportPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Chuẩn bị phiên đăng nhập cho nghiệp vụ Kho bán hàng → Nhập hàng. */
public abstract class SalesStockImportTestSupport extends BaseTest {
    protected SalesStockImportPage importPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareSalesStockImport() {
        if (driver == null) {
            throw new SkipException("WebDriver không khởi tạo được.");
        }
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra Nhập hàng.");
        }
        importPage = new SalesStockImportPage(driver);
    }
}
