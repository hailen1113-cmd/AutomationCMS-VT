package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.TransactionHistoryPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Chuẩn bị phiên đăng nhập và tab Tất cả của Lịch sử giao dịch. */
public abstract class TransactionHistoryTestSupport extends BaseTest {
    protected TransactionHistoryPage transactionPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareTransactionHistory() {
        if (driver == null) {
            throw new SkipException("WebDriver không khởi tạo được.");
        }
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra Lịch sử giao dịch.");
        }
        transactionPage = new TransactionHistoryPage(driver);
        transactionPage.openAllTab();
    }
}
