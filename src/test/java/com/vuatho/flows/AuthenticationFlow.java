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
        LoginPage loginPage = new LoginPage(driver);
        if (loginPage.isDashboardVisibleNow()) {
            return loginPage;
        }

        EntryPage entryPage = new EntryPage(driver).open();
        handleInteractiveVercelLogin(entryPage);
        if (entryPage.isBlockedByVercel()) {
            throw new IllegalStateException(
                    "Vercel is blocking automation. Configure VERCEL_AUTOMATION_BYPASS_SECRET.");
        }

        if (!loginPage.isDashboardVisibleNow()) {
            loginPage.loginWithGoogle(TestConfig.loginEmail(), GoogleCredentialProvider::password);
        }
        return loginPage;
    }

    private void handleInteractiveVercelLogin(EntryPage entryPage) {
        if (!entryPage.isBlockedByVercel() || !TestConfig.interactive()) {
            return;
        }
        System.out.println("Please complete Vercel login in Chrome. Waiting up to 2 minutes...");
        entryPage.waitForVercelAccess(Duration.ofMinutes(2));
    }
}
