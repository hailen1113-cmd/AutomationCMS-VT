package com.vuatho.flows;

import com.vuatho.config.GoogleCredentialProvider;
import com.vuatho.config.TestConfig;
import com.vuatho.pages.EntryPage;
import com.vuatho.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;

import java.time.Duration;

/**
 * Đóng gói luồng mở ứng dụng, kiểm tra phiên hiện tại và đăng nhập Google khi cần.
 */
public class AuthenticationFlow {
    private final WebDriver driver;

    /**
     * Khởi tạo AuthenticationFlow với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public AuthenticationFlow(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Mở application and login trong luồng kiểm thử.
     * @return kết quả open application and login sau khi xử lý
     */
    public LoginPage openApplicationAndLogin() {
        LoginPage loginPage = new LoginPage(driver);
        if (loginPage.isDashboardVisibleNow()) {
            return loginPage;
        }

        EntryPage entryPage = new EntryPage(driver).open();
        handleInteractiveVercelLogin(entryPage);
        if (entryPage.isBlockedByVercel()) {
            if (!TestConfig.hasVercelBypassSecret() && !TestConfig.interactive()) {
                throw new SkipException(
                        "Vercel protection is blocking automation. Set VERCEL_AUTOMATION_BYPASS_SECRET.");
            }
            throw new IllegalStateException(
                    "Vercel is blocking automation. Configure VERCEL_AUTOMATION_BYPASS_SECRET.");
        }

        if (!loginPage.isDashboardVisibleNow()) {
            loginPage.loginWithGoogle(TestConfig.loginEmail(), GoogleCredentialProvider::password);
        }
        return loginPage;
    }

    /**
     * Thực hiện xử lý handle interactive vercel login trong luồng kiểm thử.
     * @param entryPage giá trị entry page được truyền vào
     */
    private void handleInteractiveVercelLogin(EntryPage entryPage) {
        if (!entryPage.isBlockedByVercel() || !TestConfig.interactive()) {
            return;
        }
        System.out.println("Please complete Vercel login in Chrome. Waiting up to 2 minutes...");
        entryPage.waitForVercelAccess(Duration.ofMinutes(2));
    }
}
