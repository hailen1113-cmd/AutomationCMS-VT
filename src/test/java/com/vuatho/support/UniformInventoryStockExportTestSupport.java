package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.UniformInventoryStockExportPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/** Setup dùng chung cho testcase Kho tổng → Phiếu xuất kho. */
public abstract class UniformInventoryStockExportTestSupport extends BaseTest {
    protected UniformInventoryStockExportPage stockExportPage;

    /** Đảm bảo đăng nhập và khởi tạo Page Object trước mỗi testcase. */
    @BeforeMethod(alwaysRun = true)
    public void prepareUniformInventoryStockExport() {
        if (driver == null) {
            throw new SkipException("WebDriver không khởi tạo được.");
        }
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver)
                    .openApplicationAndLogin();
            Assert.assertTrue(
                    loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra tab Phiếu xuất kho.");
        }
        stockExportPage = new UniformInventoryStockExportPage(driver);
    }
}
