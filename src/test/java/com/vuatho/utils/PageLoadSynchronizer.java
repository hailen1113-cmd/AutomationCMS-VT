package com.vuatho.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class PageLoadSynchronizer {
    private static final By LOADING_INDICATORS = By.cssSelector(
            "[role='progressbar'], .ant-spin-spinning, .ant-skeleton, .skeleton");
    private static final Duration SEARCH_STABLE_WINDOW = Duration.ofMillis(900);
    private static final Duration SEARCH_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration POST_RESULT_PAUSE = Duration.ofSeconds(2);

    private PageLoadSynchronizer() {
    }

    public static void prepareForAsyncAction(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
                "if (window.__automationNetworkTrackerInstalled) return;"
                        + "window.__automationNetworkTrackerInstalled = true;"
                        + "window.__automationPendingRequests = 0;"
                        + "window.__automationStartedRequests = 0;"
                        + "const inc = () => { window.__automationPendingRequests++;"
                        + " window.__automationStartedRequests++; };"
                        + "const dec = () => window.__automationPendingRequests = Math.max(0,"
                        + "(window.__automationPendingRequests || 0) - 1);"
                        + "const originalFetch = window.fetch;"
                        + "if (originalFetch) {"
                        + " window.fetch = function() {"
                        + "  inc();"
                        + "  return originalFetch.apply(this, arguments).finally(dec);"
                        + " };"
                        + "}"
                        + "const originalSend = XMLHttpRequest.prototype.send;"
                        + "XMLHttpRequest.prototype.send = function() {"
                        + " inc();"
                        + " this.addEventListener('loadend', dec, {once:true});"
                        + " return originalSend.apply(this, arguments);"
                        + "};");
    }

    public static void waitForDataToSettle(WebDriver driver) {
        WebDriverWait wait = Waits.standard(driver);

        wait.until(webDriver -> "complete".equals(((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")));
        pauseForDebounce();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3)).until(webDriver ->
                    webDriver.findElements(LOADING_INDICATORS).stream()
                            .noneMatch(WebElement::isDisplayed));
        } catch (org.openqa.selenium.TimeoutException ignored) {
            // Some dashboard cards retain a visible skeleton decoration even after
            // their route/content has updated. Callers still verify the resulting
            // URL or DOM state, so this indicator is not a completion authority.
        }
    }

    public static String mainContentState(WebDriver driver) {
        Object state = ((JavascriptExecutor) driver).executeScript(
                "const root = document.querySelector('main,[role=main]') || document.body;"
                        + "const text = (root.innerText || root.textContent || '').trim();"
                        + "const rows = root.querySelectorAll('tbody tr,[role=row],.ant-table-row').length;"
                        + "const cards = root.querySelectorAll('[class*=card],[class*=item]').length;"
                        + "return location.href + '|' + rows + '|' + cards + '|' + text.length + '|'"
                        + "+ Array.from(text).reduce((hash,ch)=>((hash*31)+ch.charCodeAt(0))|0,0);");
        return String.valueOf(state);
    }

    public static void waitForSearchResultsToLoad(WebDriver driver, String previousState) {
        waitForDocumentReady(driver);
        pauseForDebounce();

        long startedAt = System.nanoTime();
        long initialRequestCount = startedRequests(driver);
        long deadline = System.nanoTime() + SEARCH_TIMEOUT.toNanos();
        long stableSince = -1;
        String lastState = null;

        while (System.nanoTime() < deadline) {
            String currentState = mainContentState(driver);
            boolean stateChanged = !currentState.equals(previousState);
            boolean asyncObserved = startedRequests(driver) > initialRequestCount;
            boolean observationWindowPassed = System.nanoTime() - startedAt >= Duration.ofSeconds(2).toNanos();
            // The ERP keeps polling/analytics requests alive after a result table has
            // rendered. DOM stability plus the page loading indicator are therefore
            // the completion signal; network activity is only an observation signal.
            boolean quiet = noLoadingIndicatorIsVisible(driver);

            if (quiet && (stateChanged || asyncObserved || observationWindowPassed)) {
                if (currentState.equals(lastState)) {
                    if (stableSince > 0
                            && System.nanoTime() - stableSince >= SEARCH_STABLE_WINDOW.toNanos()) {
                        pauseAfterResults();
                        return;
                    }
                } else {
                    lastState = currentState;
                    stableSince = System.nanoTime();
                }
            } else {
                stableSince = -1;
                lastState = currentState;
            }

            sleep(Duration.ofMillis(200), "Interrupted while waiting for search results.");
        }

        throw new IllegalStateException("Search results did not finish loading within "
                + SEARCH_TIMEOUT.toSeconds() + " seconds.");
    }

    private static void waitForDocumentReady(WebDriver driver) {
        Waits.standard(driver).until(webDriver -> "complete".equals(((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")));
    }

    private static boolean noLoadingIndicatorIsVisible(WebDriver driver) {
        return driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed);
    }

    private static long startedRequests(WebDriver driver) {
        try {
            Object started = ((JavascriptExecutor) driver)
                    .executeScript("return window.__automationStartedRequests || 0;");
            return started instanceof Number number ? number.longValue() : 0;
        } catch (WebDriverException exception) {
            return 0;
        }
    }

    private static void pauseAfterResults() {
        sleep(POST_RESULT_PAUSE, "Interrupted during post-search result pause.");
    }

    private static void pauseForDebounce() {
        sleep(Duration.ofMillis(500), "Interrupted while waiting for filter debounce.");
    }

    private static void sleep(Duration duration, String message) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(message, exception);
        }
    }
}
