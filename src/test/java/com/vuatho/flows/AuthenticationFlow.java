package com.vuatho.flows;

import com.vuatho.config.GoogleCredentialProvider;
import com.vuatho.config.TestConfig;
import com.vuatho.pages.EntryPage;
import com.vuatho.pages.LoginPage;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

public class AuthenticationFlow {
    private final WebDriver driver;

    public AuthenticationFlow(WebDriver driver) {
        this.driver = driver;
    }

    public LoginPage openApplicationAndLogin() {
        EntryPage entryPage = new EntryPage(driver).open();
        handleInteractiveVercelLogin(entryPage);
        if (entryPage.isBlockedByVercel()) {
            throw new IllegalStateException(
                    "Vercel đang chặn automation. Hãy cấu hình VERCEL_AUTOMATION_BYPASS_SECRET.");
        }

        LoginPage loginPage = new LoginPage(driver);
        if (!loginPage.isDashboardVisibleNow()) {
            loginPage.loginWithGoogle(TestConfig.loginEmail(), GoogleCredentialProvider::password);
        }
        return loginPage;
    }

    private void handleInteractiveVercelLogin(EntryPage entryPage) {
        if (!entryPage.isBlockedByVercel() || !TestConfig.interactive()) {
            return;
        }
        System.out.println("Hay dang nhap Vercel trong cua so Chrome. Test se cho toi da 2 phut...");
        entryPage.waitForVercelAccess(Duration.ofMinutes(2));
    }
}
