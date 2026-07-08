package com.vuatho.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class PageLoadSynchronizer {
    private static final By LOADING_INDICATORS = By.cssSelector(
            ".ant-spin-spinning, .ant-skeleton, .skeleton");

    private PageLoadSynchronizer() {
    }

    public static void waitForDataToSettle(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.pollingEvery(Duration.ofMillis(200));
        wait.ignoring(StaleElementReferenceException.class);

        wait.until(webDriver -> "complete".equals(((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")));
        pauseForDebounce();
        wait.until(webDriver -> webDriver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed));
        waitUntilContentIsStable(driver, wait);
    }

    private static void waitUntilContentIsStable(WebDriver driver, WebDriverWait wait) {
        AtomicReference<Integer> previousHash = new AtomicReference<>();
        AtomicLong stableSince = new AtomicLong(System.nanoTime());

        wait.until(webDriver -> {
            int currentHash = webDriver.findElement(By.tagName("body")).getText().hashCode();
            Integer previous = previousHash.getAndSet(currentHash);
            if (previous == null || previous != currentHash) {
                stableSince.set(System.nanoTime());
                return false;
            }
            return System.nanoTime() - stableSince.get() >= Duration.ofMillis(500).toNanos();
        });
    }

    private static void pauseForDebounce() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for filter debounce.", exception);
        }
    }
}
