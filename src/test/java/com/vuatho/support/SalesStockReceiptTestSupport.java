package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.SalesStockReceiptPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Chuẩn bị phiên đăng nhập cho testcase Kho bán hàng → Phiếu. */
public abstract class SalesStockReceiptTestSupport extends BaseTest {
    protected SalesStockReceiptPage receiptPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareSalesStockReceipts() {
        if (driver == null) {
            throw new SkipException("WebDriver không khởi tạo được.");
        }
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra Phiếu Kho bán hàng.");
        }
        receiptPage = new SalesStockReceiptPage(driver);
    }
}
