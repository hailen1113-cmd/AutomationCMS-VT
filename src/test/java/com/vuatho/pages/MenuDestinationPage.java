package com.vuatho.pages;

import com.vuatho.components.SidebarComponent;
import com.vuatho.navigation.MenuTarget;
import com.vuatho.utils.PageScroller;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MenuDestinationPage {
    private static final By LOADING_INDICATORS = By.cssSelector(
            "[role='progressbar'], .ant-spin-spinning, .ant-skeleton, .skeleton");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final SidebarComponent sidebar;
    private boolean loaded;
    private Duration loadDuration = Duration.ZERO;

    public MenuDestinationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(45));
        this.wait.pollingEvery(Duration.ofMillis(300));
        this.wait.ignoring(StaleElementReferenceException.class);
        this.sidebar = new SidebarComponent(driver);
    }

    public MenuDestinationPage openAndWaitUntilLoaded(MenuTarget target) {
        return openAndWaitUntilLoaded(target, true);
    }

    public MenuDestinationPage openAndWaitUntilLoaded(MenuTarget target, boolean scrollToBottom) {
        long startedAt = System.nanoTime();
        sidebar.ensureExpanded();
        expandParentMenuIfNeeded(target);

        String previousUrl = driver.getCurrentUrl();
        String previousContent = mainContentText();
        clickSidebarItem(target.name());

        waitForDestinationToLoad(previousUrl, previousContent);

        loaded = true;
        loadDuration = Duration.ofNanos(System.nanoTime() - startedAt);
        if (scrollToBottom) {
            PageScroller.slowlyToBottom(driver);
        }
        return this;
    }

    public boolean isLoaded() {
        return loaded && documentIsReady();
    }

    public Duration loadDuration() {
        return loadDuration;
    }

    private void expandParentMenuIfNeeded(MenuTarget target) {
        if (!target.hasParent() || visibleSidebarItem(target.name()) != null) {
            return;
        }
        clickSidebarItem(target.parent());
        wait.until(webDriver -> visibleSidebarItem(target.name()) != null);
    }

    private void waitForDestinationToLoad(String previousUrl, String previousContent) {
        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> hasNavigatedAwayFrom(previousUrl, previousContent));
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        wait.until(webDriver -> mainContentText().length() > 20);
    }

    private boolean hasNavigatedAwayFrom(String previousUrl, String previousContent) {
        return !driver.getCurrentUrl().equals(previousUrl)
                || !mainContentText().equals(previousContent);
    }

    private boolean noLoadingIndicatorIsVisible() {
        return driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed);
    }

    private void clickSidebarItem(String label) {
        WebElement item = wait.until(webDriver -> visibleSidebarItem(label));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", item);
        wait.until(webDriver -> visibleSidebarItem(label)).click();
    }

    private WebElement visibleSidebarItem(String label) {
        return driver.findElements(exactText(label)).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() < 500)
                .findFirst()
                .orElse(null);
    }

    private By exactText(String label) {
        return By.xpath("//*[normalize-space()='" + label + "'"
                + " or normalize-space(text())='" + label + "']");
    }

    private String mainContentText() {
        return driver.findElements(By.cssSelector("main, [role='main']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .orElseGet(() -> driver.findElement(By.tagName("body")).getText())
                .trim();
    }

    private boolean documentIsReady() {
        return "complete".equals(((JavascriptExecutor) driver)
                .executeScript("return document.readyState"));
    }
}
