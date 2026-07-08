package com.vuatho.quality;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PageHealthChecker {
    private static final By MAIN_CONTENT = By.cssSelector("main, [role='main']");
    private static final By ERROR_MESSAGES = By.xpath(
            "//*[normalize-space()='Internal Server Error'"
                    + " or normalize-space()='Application error'"
                    + " or normalize-space()='Something went wrong'"
                    + " or normalize-space()='404 - Page Not Found'"
                    + " or normalize-space()='500 - Internal Server Error']");

    private final WebDriver driver;

    public PageHealthChecker(WebDriver driver) {
        this.driver = driver;
    }

    public void startObservation() {
        browserErrors();
    }

    public PageHealthReport inspect() {
        List<String> problems = new ArrayList<>();
        String url = driver.getCurrentUrl();

        validateUrl(url, problems);
        validateDocument(problems);
        validateMainContent(problems);
        validateErrorPage(problems);
        validateImages(problems);
        validateBrowserConsole(problems);

        return new PageHealthReport(url, List.copyOf(problems));
    }

    private void validateUrl(String url, List<String> problems) {
        String expectedHost = URI.create(TestConfig.baseUrl()).getHost();
        String actualHost = URI.create(url).getHost();
        if (!expectedHost.equalsIgnoreCase(actualHost)) {
            problems.add("Unexpected host: " + actualHost + " (expected " + expectedHost + ")");
        }
        if (url.contains("login") || url.contains("accounts.google.com")) {
            problems.add("Page redirected to authentication: " + url);
        }
    }

    private void validateDocument(List<String> problems) {
        Object state = ((JavascriptExecutor) driver).executeScript("return document.readyState");
        if (!"complete".equals(state)) {
            problems.add("Document is not ready: " + state);
        }
        if (driver.getTitle().isBlank()) {
            problems.add("Page title is empty");
        }
    }

    private void validateMainContent(List<String> problems) {
        String text = driver.findElements(MAIN_CONTENT).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .orElse("")
                .trim();
        if (text.length() < 20) {
            problems.add("Main content is missing or nearly empty");
        }
    }

    private void validateErrorPage(List<String> problems) {
        List<String> errors = driver.findElements(ERROR_MESSAGES).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .distinct()
                .toList();
        if (!errors.isEmpty()) {
            problems.add("Visible application error: " + errors);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateImages(List<String> problems) {
        List<String> brokenImages = (List<String>) ((JavascriptExecutor) driver).executeScript(
                "return [...document.images].filter(img=>img.complete && img.naturalWidth===0)"
                        + ".map(img=>img.getAttribute('src') || '<empty src>');");
        brokenImages = brokenImages.stream().distinct().toList();
        if (!brokenImages.isEmpty()) {
            problems.add("Broken images: " + brokenImages);
        }
    }

    private void validateBrowserConsole(List<String> problems) {
        List<String> errors = browserErrors().stream()
                .map(LogEntry::getMessage)
                .distinct()
                .toList();
        if (!errors.isEmpty()) {
            problems.add("Severe browser console errors: " + errors);
        }
    }

    private List<LogEntry> browserErrors() {
        try {
            return driver.manage().logs().get(LogType.BROWSER).getAll().stream()
                    .filter(entry -> entry.getLevel().intValue() >= Level.SEVERE.intValue())
                    .toList();
        } catch (RuntimeException unsupportedLogging) {
            return List.of();
        }
    }
}
