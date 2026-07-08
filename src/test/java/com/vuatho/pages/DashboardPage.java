package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import com.vuatho.components.SidebarComponent;
import com.vuatho.utils.PageScroller;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public class DashboardPage {
    public static final List<String> EXPECTED_MENU_GROUPS = List.of(
            "Dashboard",
            "Hiệu Quả Nguồn Thợ & Chi Phí",
            "Tài chính",
            "Người Dùng",
            "Đối Tác - Thợ",
            "Bài Kiểm Tra",
            "Nghiệp Vụ",
            "Đơn Dịch Vụ",
            "Đồng Phục",
            "Giao Dịch",
            "Website",
            "App",
            "System",
            "Marketing");

    private static final By DASHBOARD_TEXT = By.xpath("//*[normalize-space()='Dashboard']");
    private static final By DASHBOARD_MENU = By.xpath(
            "//a[normalize-space(.)='Dashboard'] | //button[normalize-space(.)='Dashboard']"
                    + " | //*[@role='menuitem' and normalize-space(.)='Dashboard']");
    private static final By COMPANY_HEADER = By.xpath("//*[normalize-space()='Công ty Vua Thợ']");
    private static final By HOME_CONTENT = By.xpath("//*[normalize-space()='Sơ Đồ Tổ Chức']");
    private static final By DASHBOARD_CONTENT = By.xpath("//*[normalize-space()='Thống Kê Tổng Quan']");
    private static final By GOOGLE_LOGIN = By.xpath("//*[contains(normalize-space(.),'Google')]");
    private static final By LOADING_INDICATORS = By.cssSelector(
            "[role='progressbar'], .ant-spin-spinning, .ant-skeleton, .skeleton");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final SidebarComponent sidebar;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.wait.pollingEvery(Duration.ofMillis(200));
        this.wait.ignoring(StaleElementReferenceException.class);
        this.sidebar = new SidebarComponent(driver);
    }

    public DashboardPage open() {
        driver.get(TestConfig.entryUrl());
        return this;
    }

    public boolean isLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(COMPANY_HEADER));
            wait.until(webDriver -> isVisible(HOME_CONTENT) || isVisible(DASHBOARD_CONTENT));
            sidebar.ensureExpanded();
            return true;
        } catch (org.openqa.selenium.TimeoutException ignored) {
            return false;
        }
    }

    public boolean hasValidDashboardUrl() {
        String url = driver.getCurrentUrl().toLowerCase();
        return url.startsWith("https://erp-sandbox.vuatho.com")
                && !url.contains("accounts.google.com")
                && !url.contains("login");
    }

    public void openDashboardAndWaitForMetrics() {
        wait.until(webDriver -> isVisible(HOME_CONTENT) || isVisible(DASHBOARD_CONTENT));
        sidebar.ensureExpanded();
        wait.until(ExpectedConditions.elementToBeClickable(DASHBOARD_MENU)).click();

        WebDriverWait metricsWait = new WebDriverWait(driver, Duration.ofSeconds(45));
        metricsWait.pollingEvery(Duration.ofMillis(300));
        metricsWait.ignoring(StaleElementReferenceException.class);
        metricsWait.until(webDriver -> hasDashboardMarker());
        metricsWait.until(webDriver -> driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed));
        metricsWait.until(webDriver -> visibleMetricValues() > 0);
        PageScroller.slowlyToBottom(driver);
    }

    public boolean areMetricsDisplayed() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(webDriver -> visibleMetricValues() > 0);
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public List<String> loadedMetrics() {
        return mainContent().findElements(By.xpath(
                        ".//*[not(*) and string-length(normalize-space()) > 0]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isBlank() && text.matches(".*\\d.*"))
                .distinct()
                .toList();
    }

    public List<String> missingMenuGroups() {
        String sidebarText = sidebar.text();
        return EXPECTED_MENU_GROUPS.stream()
                .filter(item -> !sidebarText.contains(item))
                .toList();
    }

    public boolean isLogoLoaded() {
        return sidebar.isLogoLoaded();
    }

    public double sidebarWidth() {
        return sidebar.width();
    }

    public void collapseSidebar() {
        sidebar.collapse();
    }

    public void expandSidebar() {
        sidebar.expand();
    }

    public boolean isDashboardMenuActive() {
        return sidebar.isDashboardActive();
    }

    public boolean hasCompanyHeader() {
        return isVisible(COMPANY_HEADER);
    }

    public boolean hasCurrentUserAndEnvironment() {
        return isVisible(By.xpath("//*[normalize-space()='Hải']"))
                && isVisible(By.xpath("//*[contains(normalize-space(.),'DEV')]"));
    }

    public void reload() {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_TEXT));
    }

    public void logout() {
        logoutButton().click();
        wait.until(webDriver -> isLoginVisible() || !hasDashboardMarker());
    }

    public boolean isLoginVisible() {
        return isVisible(GOOGLE_LOGIN);
    }

    public boolean hasDashboardMarker() {
        return isVisible(DASHBOARD_CONTENT)
                || (isVisible(HOME_CONTENT) && isVisible(COMPANY_HEADER));
    }

    private WebElement logoutButton() {
        List<WebElement> namedButtons = driver.findElements(By.xpath(
                "//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout')"
                        + " or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout')"
                        + " or contains(@aria-label,'Đăng xuất') or contains(@title,'Đăng xuất')]"));
        if (!namedButtons.isEmpty()) {
            return namedButtons.stream().filter(WebElement::isDisplayed).findFirst().orElseThrow();
        }

        int pageWidth = ((Number) ((JavascriptExecutor) driver)
                .executeScript("return window.innerWidth;")).intValue();
        return driver.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getRect().getX() > pageWidth * 0.75)
                .filter(button -> button.getRect().getY() < 160)
                .max(Comparator.comparingInt(button -> button.getRect().getX()))
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nút logout."));
    }

    private boolean isVisible(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    private long visibleMetricValues() {
        return loadedMetrics().size();
    }

    private WebElement mainContent() {
        return driver.findElements(By.cssSelector("main, [role='main']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseGet(() -> driver.findElement(By.tagName("body")));
    }
}
