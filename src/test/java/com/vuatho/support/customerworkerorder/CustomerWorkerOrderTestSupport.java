package com.vuatho.support.customerworkerorder;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.CustomerWorkerOrderPage;
import com.vuatho.pages.LoginPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Lớp nền dùng chung cho các testcase Đơn Khách - Thợ.
 *
 * <p>Đây không phải file chạy test: lớp chỉ đảm nhiệm đăng nhập khi cần, mở
 * route {@code /vuatho/order}, khởi tạo Page Object trước mỗi method và dọn
 * popup/drawer sau khi chạy. Việc cleanup không được phép che mất lỗi nghiệp
 * vụ của testcase vừa hoàn thành.</p>
 */
public abstract class CustomerWorkerOrderTestSupport extends BaseTest {
    protected CustomerWorkerOrderPage orderPage;

    /** Đăng nhập nếu cần, mở route đơn và tạo Page Object trước mỗi testcase. */
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

    /** Dọn overlay sau mỗi testcase nhưng không che exception nghiệp vụ trước đó. */
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
