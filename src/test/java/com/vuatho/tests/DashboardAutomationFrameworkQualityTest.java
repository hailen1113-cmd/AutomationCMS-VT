package com.vuatho.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Kiểm tra các quy ước chất lượng của framework và Page Object dành cho Dashboard.
 */
public class DashboardAutomationFrameworkQualityTest {
    private static final Path TEST_SOURCE = Path.of("src", "test", "java", "com", "vuatho");

    /**
     * Thực thi test “CAR-004: Dashboard locators do not depend on dynamic IDs” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "CAR-004: Dashboard locators do not depend on dynamic IDs")
    public void dashboardDoesNotUseDynamicIdLocators() throws IOException {
        String source = source("pages", "DashboardPage.java")
                + source("components", "SidebarComponent.java");
        Assert.assertFalse(source.contains("By.id("),
                "Dashboard automation must not depend on generated element IDs");
    }

    /**
     * Thực thi test “CAR-005: Dashboard locators avoid full XPath and long Tailwind chains” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "CAR-005: Dashboard locators avoid full XPath and long Tailwind chains")
    public void dashboardAvoidsBrittleLocators() throws IOException {
        String source = source("pages", "DashboardPage.java")
                + source("components", "SidebarComponent.java");
        Assert.assertFalse(source.contains("/html/body"), "Full XPath locator found");
        Assert.assertFalse(source.contains("nth-child("), "Position-dependent CSS locator found");
        Assert.assertFalse(source.contains("min-w-fit.rounded-full.shadow-lg"),
                "Long Tailwind class chain found");
    }

    /**
     * Thực thi test “CAR-007: Dashboard flow does not use Thread.sleep” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "CAR-007: Dashboard flow does not use Thread.sleep")
    public void dashboardFlowUsesExplicitWaits() throws IOException {
        String source = source("pages", "DashboardPage.java")
                + source("components", "SidebarComponent.java")
                + source("tests", "DashboardTestSupport.java")
                + source("tests", "DashboardCoreUiTest.java")
                + source("tests", "DashboardSummaryCardsTest.java")
                + source("tests", "DashboardWorkbookUiTest.java")
                + source("tests", "DashboardLogoutTest.java");
        Assert.assertFalse(source.contains("Thread.sleep"),
                "Dashboard flow must use condition-based waits");
    }

    /**
     * Thực thi test “CAR-008: Independent tests continue after one failure” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "CAR-008: Independent tests continue after one failure")
    public void suiteContinuesAfterIndependentFailure() throws IOException {
        String suite = Files.readString(Path.of("dashboard-testng.xml"));
        Assert.assertTrue(suite.contains("configfailurepolicy=\"continue\""));
    }

    /**
     * Thực thi test “CAR-009: Browser lifecycle is controlled at suite level” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "CAR-009: Browser lifecycle is controlled at suite level")
    public void browserLifecycleIsCentralized() throws IOException {
        String lifecycle = source("core", "DriverLifecycleListener.java");
        String session = source("core", "DriverSession.java");
        Assert.assertTrue(lifecycle.contains("onExecutionFinish"));
        Assert.assertTrue(lifecycle.contains("releaseAfterSuite"));
        Assert.assertTrue(session.contains("sharedDriver.quit()"));
    }

    /**
     * Thực thi test “CAR-020: Failure evidence includes screenshot reporting” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "CAR-020: Failure evidence includes screenshot reporting")
    public void failureEvidenceIsConfigured() throws IOException {
        String listener = source("reporting", "ConsoleTestListener.java");
        String screenshots = source("reporting", "ScreenshotManager.java");
        Assert.assertTrue(listener.contains("ScreenshotManager"));
        Assert.assertTrue(screenshots.contains("getScreenshotAs"));
        Assert.assertTrue(screenshots.contains("getCurrentUrl"));
    }

    /**
     * Thực hiện xử lý source trong luồng kiểm thử.
     * @param folder giá trị folder được truyền vào
     * @param file giá trị file được truyền vào
     * @return kết quả source sau khi xử lý
     */
    private String source(String folder, String file) throws IOException {
        return Files.readString(TEST_SOURCE.resolve(folder).resolve(file));
    }
}
