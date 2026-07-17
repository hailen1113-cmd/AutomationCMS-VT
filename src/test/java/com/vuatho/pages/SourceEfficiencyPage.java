package com.vuatho.pages;

import com.vuatho.components.SidebarComponent;
import com.vuatho.utils.OverlayCleaner;
import com.vuatho.utils.PageScroller;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object thao tác và xác nhận trạng thái tải của trang Hiệu quả và chi phí nguồn.
 */
public class SourceEfficiencyPage {
    private static final By MENU = By.cssSelector("a[href='/vuatho/supply-performance']");
    private static final By OVERVIEW_SECTION = By.id("sec-overview");
    private static final By USERS_SECTION = By.id("sec-users");
    private static final By FUNNEL_SECTION = By.id("sec-funnel");
    private static final By ORDERS_SECTION = By.id("sec-orders");
    private static final By LOADING_INDICATORS = By.cssSelector(
            "[role='progressbar'], .ant-spin-spinning, .ant-skeleton, .skeleton");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final SidebarComponent sidebar;
    private boolean loaded;

    /**
     * Khởi tạo SourceEfficiencyPage với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public SourceEfficiencyPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(45));
        this.wait.pollingEvery(Duration.ofMillis(300));
        this.wait.ignoring(StaleElementReferenceException.class);
        this.sidebar = new SidebarComponent(driver);
    }

    /**
     * Mở and wait until loaded trong luồng kiểm thử.
     * @return kết quả open and wait until loaded sau khi xử lý
     */
    public SourceEfficiencyPage openAndWaitUntilLoaded() {
        OverlayCleaner.dismissBlockingOverlays(driver);
        sidebar.ensureExpanded();
        String previousUrl = driver.getCurrentUrl();
        wait.until(webDriver -> menu()).click();

        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> !driver.getCurrentUrl().equals(previousUrl)
                || isVisible(OVERVIEW_SECTION));
        wait.until(webDriver -> driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed));
        wait.until(webDriver -> isVisible(OVERVIEW_SECTION));
        wait.until(webDriver -> isVisible(USERS_SECTION));
        wait.until(webDriver -> isVisible(FUNNEL_SECTION) || isVisible(ORDERS_SECTION));
        loaded = true;
        PageScroller.slowlyToBottom(driver);
        return this;
    }

    /**
     * Kiểm tra điều kiện is loaded.
     * @return kết quả is loaded sau khi xử lý
     */
    public boolean isLoaded() {
        try {
            return loaded && documentIsReady() && isVisible(OVERVIEW_SECTION) && isVisible(USERS_SECTION);
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    /**
     * Thực hiện xử lý document is ready trong luồng kiểm thử.
     * @return kết quả document is ready sau khi xử lý
     */
    private boolean documentIsReady() {
        return "complete".equals(((JavascriptExecutor) driver)
                .executeScript("return document.readyState"));
    }

    /**
     * Kiểm tra điều kiện is visible.
     * @param locator locator xác định phần tử
     * @return kết quả is visible sau khi xử lý
     */
    private boolean isVisible(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    /**
     * Thực hiện xử lý menu trong luồng kiểm thử.
     * @return kết quả menu sau khi xử lý
     */
    private WebElement menu() {
        return driver.findElements(MENU).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }
}
