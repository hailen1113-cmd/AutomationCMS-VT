package com.vuatho.tools;

import com.vuatho.config.GoogleCredentialProvider;
import com.vuatho.config.TestConfig;
import com.vuatho.core.BaseTest;
import com.vuatho.pages.DashboardPage;
import com.vuatho.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DashboardDomCaptureTest extends BaseTest {
    @Test(description = "Capture the complete live Dashboard DOM for evidence-based analysis")
    public void captureDashboardOuterHtml() throws IOException {
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.open();
        if (!dashboard.hasDashboardMarker()) {
            new LoginPage(driver).loginWithGoogle(
                    TestConfig.loginEmail(),
                    GoogleCredentialProvider::password);
        }
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard must be loaded before capturing its DOM.");
        dashboard.openDashboardAndWaitForSummaryCards();
        Assert.assertTrue(dashboard.hasValidDashboardUrl(),
                "Dashboard route must be active before capturing its DOM.");

        Path output = Path.of("target", "analysis", "dashboard-outerHTML.html");
        Files.createDirectories(output.getParent());
        Files.writeString(output, driver.getPageSource(), StandardCharsets.UTF_8);
        Assert.assertTrue(Files.size(output) > 0, "Captured Dashboard outerHTML must not be empty.");
        System.out.println("[DOM CAPTURED] " + output.toAbsolutePath()
                + " | URL=" + driver.getCurrentUrl()
                + " | bytes=" + Files.size(output));
    }
}
