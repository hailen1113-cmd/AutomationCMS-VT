package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

public class EntryPage {
    private final WebDriver driver;

    public EntryPage(WebDriver driver) {
        this.driver = driver;
    }

    public EntryPage open() {
        driver.get(TestConfig.entryUrl());
        waitForDocumentReady();
        return this;
    }

    public String title() {
        return driver.getTitle();
    }

    public boolean isOnExpectedDomain() {
        return driver.getCurrentUrl().contains(TestConfig.baseHost());
    }

    public boolean isBlockedByVercel() {
        String title = title().toLowerCase();
        String source = driver.getPageSource().toLowerCase();
        return (title.contains("login") && title.contains("vercel"))
                || source.contains("vercel authentication");
    }

    public boolean waitForVercelAccess(Duration timeout) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, timeout)
                    .until(webDriver -> !isBlockedByVercel());
            waitForDocumentReady();
            return true;
        } catch (org.openqa.selenium.TimeoutException ignored) {
            return false;
        }
    }

    private void waitForDocumentReady() {
        new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(20))
                .until(webDriver -> "complete".equals(
                        ((org.openqa.selenium.JavascriptExecutor) webDriver)
                                .executeScript("return document.readyState")));
    }
}
