package com.vuatho.utils;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class Waits {
    private Waits() {
    }

    public static WebDriverWait standard(WebDriver driver) {
        return withTimeout(driver, TestConfig.defaultWaitTimeout());
    }

    public static WebDriverWait longWait(WebDriver driver) {
        return withTimeout(driver, TestConfig.longWaitTimeout());
    }

    public static WebDriverWait withTimeout(WebDriver driver, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.pollingEvery(Duration.ofMillis(200));
        wait.ignoring(StaleElementReferenceException.class);
        return wait;
    }
}
