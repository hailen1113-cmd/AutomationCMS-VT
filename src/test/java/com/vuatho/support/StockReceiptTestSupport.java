package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.StockReceiptPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Chuẩn bị đăng nhập, Page Object và cleanup cho testcase Nhập kho tổng. */
public abstract class StockReceiptTestSupport extends BaseTest {
    protected StockReceiptPage receiptPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareStockReceipt() {
        if (driver == null) {
            throw new SkipException("WebDriver không khởi tạo được.");
        }
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver)
                    .openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra Nhập kho tổng.");
        }
        receiptPage = new StockReceiptPage(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void closeStockReceiptOverlay() {
        if (driver == null || receiptPage == null) {
            return;
        }
        try {
            receiptPage.closeOverlay();
        } catch (RuntimeException ignored) {
            // Testcase sau mở lại route nên cleanup không che lỗi nghiệp vụ chính.
        }
    }
}
