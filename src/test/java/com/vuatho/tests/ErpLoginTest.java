package com.vuatho.tests;

import com.vuatho.config.GoogleCredentialProvider;
import com.vuatho.config.TestConfig;
import com.vuatho.core.BaseTest;
import com.vuatho.pages.EntryPage;
import com.vuatho.pages.LoginPage;
import com.vuatho.reporting.ConsoleTestListener;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.time.Duration;

public class ErpLoginTest extends BaseTest {
    public static void main(String[] args) {
        System.setProperty("headless", System.getProperty("headless", "false"));
        System.setProperty("interactive", System.getProperty("interactive", "true"));
        System.setProperty("pause.on.failure", System.getProperty("pause.on.failure", "false"));

        TestNG testNG = new TestNG();
        testNG.setDefaultSuiteName("ERP Login Suite");
        testNG.setDefaultTestName("Login with hailen1113@gmail.com");
        testNG.setTestClasses(new Class<?>[]{ErpLoginTest.class});
        testNG.addListener(new ConsoleTestListener());
        testNG.run();

        if (testNG.hasFailure()) {
            System.exit(1);
        }
    }

    @Test(description = "CMS-DASH-001: Login CMS successfully with Google")
    public void loginSuccessfully() {
        EntryPage entryPage = new EntryPage(driver).open();
        if (entryPage.isBlockedByVercel() && TestConfig.interactive()) {
            System.out.println("Hay dang nhap Vercel trong cua so Chrome. Test se cho toi da 2 phut...");
            entryPage.waitForVercelAccess(Duration.ofMinutes(2));
        }
        Assert.assertFalse(entryPage.isBlockedByVercel(),
                "Vercel đang chặn automation. Hãy cấu hình VERCEL_AUTOMATION_BYPASS_SECRET.");

        LoginPage loginPage = new LoginPage(driver);
        if (!loginPage.isDashboardVisibleNow()) {
            loginPage.loginWithGoogle(TestConfig.loginEmail(), GoogleCredentialProvider::password);
        }

        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Đăng nhập không thành công: không tìm thấy Dashboard/Công ty Vua Thợ.");
    }
}
