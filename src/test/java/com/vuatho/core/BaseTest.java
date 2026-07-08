package com.vuatho.core;

import com.vuatho.reporting.ScreenshotManager;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.time.Duration;

public abstract class BaseTest {
    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        if (reuseDriverBetweenTestMethods() && driver != null) {
            System.out.println("Reusing the current WebDriver for the next test case...");
            return;
        }
        System.out.println("Opening a new WebDriver for the next test case...");
        driver = DriverFactory.createChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) throws IOException {
        if (driver == null) {
            return;
        }

        try {
            if (!result.isSuccess()) {
                ScreenshotManager.capture(driver, result.getMethod().getMethodName());
                FailurePause.awaitConfirmation();
            }
        } finally {
            if (!reuseDriverBetweenTestMethods()) {
                closeDriver("after: " + result.getMethod().getMethodName());
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (reuseDriverBetweenTestMethods()) {
            closeDriver("after the final test case in " + getClass().getSimpleName());
        }
    }

    protected boolean reuseDriverBetweenTestMethods() {
        return false;
    }

    private void closeDriver(String reason) {
        if (driver == null) {
            return;
        }
        System.out.println("Closing WebDriver " + reason);
        try {
            driver.quit();
        } finally {
            driver = null;
        }
    }
}
