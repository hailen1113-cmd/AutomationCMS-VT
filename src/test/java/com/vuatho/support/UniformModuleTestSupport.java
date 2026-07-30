package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.UniformCatalogPage;
import com.vuatho.pages.UniformInventoryPage;
import com.vuatho.pages.UniformOrderPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Setup/cleanup dùng chung của module Đồng phục.
 *
 * <p>Đây là lớp hỗ trợ, không chứa {@code @Test}. Mỗi file testcase tự mở đúng
 * route nghiệp vụ của mình nên có thể chạy độc lập từ IDE.</p>
 */
public abstract class UniformModuleTestSupport extends BaseTest {
    protected UniformCatalogPage catalogPage;
    protected UniformOrderPage uniformOrderPage;
    protected UniformInventoryPage inventoryPage;

    /** Đảm bảo đã đăng nhập và khởi tạo ba Page Object trước mỗi testcase. */
    @BeforeMethod(alwaysRun = true)
    public void prepareUniformModule() {
        if (driver == null) {
            throw new SkipException("WebDriver không khởi tạo được.");
        }
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        boolean dashboardVisible = loginPage.isDashboardVisible(Duration.ofSeconds(20));
        Assert.assertTrue(dashboardVisible,
                    "Không đăng nhập được trước khi kiểm tra module Đồng phục."
                            + " URL hiện tại: " + driver.getCurrentUrl()
                            + " | Tiêu đề: " + driver.getTitle());
        catalogPage = new UniformCatalogPage(driver);
        uniformOrderPage = new UniformOrderPage(driver);
        inventoryPage = new UniformInventoryPage(driver);
    }

    /** Đóng drawer/dialog còn mở để testcase sau không bị overlay chặn. */
    @AfterMethod(alwaysRun = true)
    public void closeUniformOverlay() {
        if (driver == null) {
            return;
        }
        try {
            if (driver.getCurrentUrl().contains("/order-uniform")) {
                uniformOrderPage.closeOverlay();
            } else if (driver.getCurrentUrl().contains("/inventory-uniform")) {
                inventoryPage.closeOverlay();
            } else if (driver.getCurrentUrl().contains("/uniform")) {
                catalogPage.closeOverlay();
            }
        } catch (RuntimeException ignored) {
            // Test kế tiếp luôn mở lại route nên cleanup không che lỗi nghiệp vụ.
        }
    }
}
