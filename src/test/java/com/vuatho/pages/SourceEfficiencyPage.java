package com.vuatho.pages;

import com.vuatho.components.SidebarComponent;
import com.vuatho.utils.PageScroller;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SourceEfficiencyPage {
    private static final String PAGE_NAME = "Hiệu Quả Nguồn Thợ & Chi Phí";
    private static final By MENU = By.xpath(
            "//a[normalize-space(.)='" + PAGE_NAME + "']"
                    + " | //button[normalize-space(.)='" + PAGE_NAME + "']"
                    + " | //*[@role='menuitem' and normalize-space(.)='" + PAGE_NAME + "']");
    private static final By PAGE_HEADING = By.xpath(
            "//*[normalize-space()='Hiệu quả nguồn thợ & chi phí']"
                    + " | //*[normalize-space()='" + PAGE_NAME + "']");
    private static final By PAGE_CONTENT = By.xpath(
            "//*[contains(translate(normalize-space(.),"
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'lượt tải app')]"
                    + " | //*[contains(translate(normalize-space(.),"
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'xóa app')]"
                    + " | //*[contains(translate(normalize-space(.),"
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'đơn dịch vụ (khách - thợ)')]");
    private static final By LOADING_INDICATORS = By.cssSelector(
            "[role='progressbar'], .ant-spin-spinning, .ant-skeleton, .skeleton");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final SidebarComponent sidebar;
    private boolean loaded;

    public SourceEfficiencyPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(45));
        this.wait.pollingEvery(Duration.ofMillis(300));
        this.wait.ignoring(StaleElementReferenceException.class);
        this.sidebar = new SidebarComponent(driver);
    }

    public SourceEfficiencyPage openAndWaitUntilLoaded() {
        sidebar.ensureExpanded();
        String previousUrl = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(MENU)).click();

        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> !driver.getCurrentUrl().equals(previousUrl)
                || isVisible(PAGE_HEADING));
        wait.until(webDriver -> driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed));
        wait.until(webDriver -> isVisible(PAGE_CONTENT));
        loaded = true;
        PageScroller.slowlyToBottom(driver);
        return this;
    }

    public boolean isLoaded() {
        try {
            return loaded && documentIsReady();
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    private boolean documentIsReady() {
        return "complete".equals(((JavascriptExecutor) driver)
                .executeScript("return document.readyState"));
    }

    private boolean isVisible(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }
}
