package com.vuatho.core;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.nio.file.Path;
import java.util.logging.Level;

public final class DriverFactory {
    private DriverFactory() {
    }

    public static WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        Path profileDirectory = Path.of(TestConfig.seleniumProfileDirectory())
                .toAbsolutePath()
                .normalize();

        options.addArguments("--user-data-dir=" + profileDirectory);
        options.addArguments("--profile-directory=Default");
        options.addArguments("--disable-notifications");
        options.addArguments("--no-sandbox");
        LoggingPreferences logging = new LoggingPreferences();
        logging.enable(LogType.BROWSER, Level.SEVERE);
        options.setCapability("goog:loggingPrefs", logging);
        if (TestConfig.headless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1440,1000");
        } else {
            options.addArguments("--start-maximized");
        }

        WebDriver driver = new ChromeDriver(options);
        if (!TestConfig.headless()) {
            driver.manage().window().maximize();
        }
        return driver;
    }
}
