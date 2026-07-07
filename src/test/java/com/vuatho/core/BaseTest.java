package com.vuatho.core;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import javax.swing.JOptionPane;

public abstract class BaseTest {
    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.createChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) throws IOException {
        if (driver == null) {
            return;
        }
        if (!result.isSuccess()) {
            Path directory = Path.of("target", "screenshots");
            Files.createDirectories(directory);
            byte[] image = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(directory.resolve(result.getMethod().getMethodName() + ".png"), image);

            if (TestConfig.pauseOnFailure() && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Test dang bi loi. Chrome se duoc giu mo cho toi khi ban bam OK.",
                        "ERP Login Test",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        // driver.quit();
    }
}
