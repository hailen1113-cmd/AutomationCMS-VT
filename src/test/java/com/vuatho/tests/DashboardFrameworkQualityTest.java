package com.vuatho.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DashboardFrameworkQualityTest {
    private static final Path TEST_SOURCE = Path.of("src", "test", "java", "com", "vuatho");

    @Test(description = "CAR-004: Dashboard locators do not depend on dynamic IDs")
    public void dashboardDoesNotUseDynamicIdLocators() throws IOException {
        String source = source("pages", "DashboardPage.java")
                + source("components", "SidebarComponent.java");
        Assert.assertFalse(source.contains("By.id("),
                "Dashboard automation must not depend on generated element IDs");
    }

    @Test(description = "CAR-005: Dashboard locators avoid full XPath and long Tailwind chains")
    public void dashboardAvoidsBrittleLocators() throws IOException {
        String source = source("pages", "DashboardPage.java")
                + source("components", "SidebarComponent.java");
        Assert.assertFalse(source.contains("/html/body"), "Full XPath locator found");
        Assert.assertFalse(source.contains("nth-child("), "Position-dependent CSS locator found");
        Assert.assertFalse(source.contains("min-w-fit.rounded-full.shadow-lg"),
                "Long Tailwind class chain found");
    }

    @Test(description = "CAR-007: Dashboard flow does not use Thread.sleep")
    public void dashboardFlowUsesExplicitWaits() throws IOException {
        String source = source("pages", "DashboardPage.java")
                + source("components", "SidebarComponent.java")
                + source("tests", "DashboardTest.java");
        Assert.assertFalse(source.contains("Thread.sleep"),
                "Dashboard flow must use condition-based waits");
    }

    @Test(description = "CAR-008: Independent tests continue after one failure")
    public void suiteContinuesAfterIndependentFailure() throws IOException {
        String suite = Files.readString(Path.of("dashboard-testng.xml"));
        Assert.assertTrue(suite.contains("configfailurepolicy=\"continue\""));
    }

    @Test(description = "CAR-009: Browser lifecycle is controlled at suite level")
    public void browserLifecycleIsCentralized() throws IOException {
        String lifecycle = source("core", "DriverLifecycleListener.java");
        String session = source("core", "DriverSession.java");
        Assert.assertTrue(lifecycle.contains("onExecutionFinish"));
        Assert.assertTrue(lifecycle.contains("releaseAfterSuite"));
        Assert.assertTrue(session.contains("sharedDriver.quit()"));
    }

    @Test(description = "CAR-020: Failure evidence includes screenshot reporting")
    public void failureEvidenceIsConfigured() throws IOException {
        String listener = source("reporting", "ConsoleTestListener.java");
        String screenshots = source("reporting", "ScreenshotManager.java");
        Assert.assertTrue(listener.contains("ScreenshotManager"));
        Assert.assertTrue(screenshots.contains("getScreenshotAs"));
        Assert.assertTrue(screenshots.contains("getCurrentUrl"));
    }

    private String source(String folder, String file) throws IOException {
        return Files.readString(TEST_SOURCE.resolve(folder).resolve(file));
    }
}
