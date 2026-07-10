package com.vuatho.pages;

import com.vuatho.components.SidebarComponent;
import com.vuatho.navigation.MenuTarget;
import com.vuatho.utils.OverlayCleaner;
import com.vuatho.utils.PageScroller;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.List;

public class MenuDestinationPage {
    private static final By LOADING_INDICATORS = By.cssSelector(
            "[role='progressbar'], .ant-spin-spinning, .ant-skeleton, .skeleton");
    private static final By DASHBOARD_MENU = By.cssSelector("a[href='/vuatho/dashboard']");
    private static final By SUPPLY_PERFORMANCE_MENU = By.cssSelector("a[href='/vuatho/supply-performance']");
    private static final By FINANCE_MENU = By.cssSelector("a[href='/vuatho/finance']");
    private static final By USER_MANAGEMENT_MENU = By.cssSelector("a[href='/vuatho/user']");
    private static final By EKYC_MENU = By.cssSelector("a[href='/vuatho/ekyc']");
    private static final By WORKER_PROFILE_MENU = By.cssSelector("a[href='/vuatho/worker']");
    private static final By VIOLATION_WORKER_MENU = By.cssSelector("a[href='/vuatho/violation-worker']");
    private static final By TRAINING_MENU = By.cssSelector("a[href='/vuatho/training']");
    private static final By PROFILE_POST_MENU = By.cssSelector("a[href='/vuatho/profile-post']");
    private static final By STOP_REQUEST_MENU = By.cssSelector("a[href='/vuatho/stop-request']");
    private static final By TESTED_MENU = By.cssSelector("a[href='/vuatho/tested']");
    private static final By SERVICE_MENU = By.cssSelector("a[href='/vuatho/service']");
    private static final By CUSTOMER_WORKER_ORDER_MENU = By.cssSelector("a[href='/vuatho/order']");
    private static final By ASSISTANT_WORKER_ORDER_MENU = By.cssSelector(
            "a[href='/vuatho/assistant-worker-order']");
    private static final By UNIFORM_MENU = By.cssSelector("a[href='/vuatho/uniform']");
    private static final By ORDER_UNIFORM_MENU = By.cssSelector("a[href='/vuatho/order-uniform']");
    private static final By INVENTORY_UNIFORM_MENU = By.cssSelector("a[href='/vuatho/inventory-uniform']");
    private static final By TRANSACTION_MENU = By.cssSelector("a[href='/vuatho/transaction']");
    private static final By WEBSITE_CATEGORY_MENU = By.cssSelector("a[href='/vuatho/category']");
    private static final By WEBSITE_BLOG_MENU = By.cssSelector("a[href='/vuatho/blog']");
    private static final By WEBSITE_MEDIA_BLOG_MENU = By.cssSelector("a[href='/vuatho/media-blog']");
    private static final By WEBSITE_SUPPORT_MENU = By.cssSelector("a[href='/vuatho/support']");
    private static final By WEBSITE_CONFIG_MENU = By.cssSelector("a[href='/vuatho/website-config']");
    private static final By APP_POPUP_MENU = By.cssSelector("a[href='/vuatho/popup-app']");
    private static final By APP_PAYMENT_METHOD_MENU = By.cssSelector("a[href='/vuatho/payment-method']");
    private static final By APP_NOTIFICATION_MENU = By.cssSelector("a[href='/vuatho/notification-app']");
    private static final By APP_VIOLATION_FORM_MENU = By.cssSelector("a[href='/vuatho/violation-form']");
    private static final By APP_VOUCHER_MENU = By.cssSelector("a[href='/vuatho/voucher']");
    private static final By APP_CAMPAIGN_MENU = By.cssSelector("a[href='/vuatho/campaign-app']");
    private static final By APP_CONFIG_MENU = By.cssSelector("a[href='/vuatho/config-app']");
    private static final By USER_PARENT_MENU = By.cssSelector("[aria-label='Người Dùng'] button[type='button']");
    private static final By PARTNER_WORKER_PARENT_MENU = By.cssSelector(
            "[aria-label='Đối Tác - Thợ'] button[type='button']");
    private static final By SERVICE_ORDER_PARENT_MENU = By.cssSelector(
            "[aria-label='Đơn Dịch Vụ'] button[type='button']");
    private static final By UNIFORM_PARENT_MENU = By.cssSelector("[aria-label='Đồng Phục'] button[type='button']");
    private static final By TRANSACTION_PARENT_MENU = By.cssSelector("[aria-label='Giao Dịch'] button[type='button']");
    private static final By WEBSITE_PARENT_MENU = By.cssSelector("[aria-label='Website'] button[type='button']");
    private static final By APP_PARENT_MENU = By.cssSelector("[aria-label='App'] button[type='button']");
    private static final By SYSTEM_PARENT_MENU = By.cssSelector("[aria-label='System'] button[type='button']");
    private static final By SYSTEM_AI_MENU = By.cssSelector("a[href='/vuatho/model-AI']");
    private static final By SYSTEM_SOCIAL_NETWORK_MENU = By.cssSelector("a[href='/vuatho/social-network']");
    private static final By MARKETING_PARENT_MENU = By.cssSelector("[aria-label='Marketing'] button[type='button']");
    private static final By MARKETING_STATISTIC_MENU = By.cssSelector("a[href='/vuatho/marketing-statistic']");
    private static final By MARKETING_PROMOTION_MENU = By.cssSelector("a[href='/vuatho/statistic-promotion']");
    private static final By MARKETING_CAMPAIGNS_MENU = By.cssSelector("a[href='/vuatho/campaigns']");
    private static final By MARKETING_COMPETITION_MENU = By.cssSelector("a[href='/vuatho/competition']");
    private static final By MARKETING_TOA_SANG_MENU = By.cssSelector("a[href='/vuatho/toasangvuatho']");
    private static final By MARKETING_INSURANCE_MENU = By.cssSelector("a[href='/vuatho/insurance']");
    private static final By MARKETING_SOS_MENU = By.cssSelector("a[href='/vuatho/sos-request']");
    private static final By COLLAPSIBLE_PARENT_MENUS = By.cssSelector(
            "button[type='button'][aria-controls][aria-expanded].w-full.h-full");
    private static final By FINANCE_PAGE_MARKER = By.xpath(
            "//*[self::h1 and normalize-space()='Báo Cáo Tài Chính']");
    private static final By USER_MANAGEMENT_PAGE_MARKER = By.cssSelector(
            "table[aria-label='Table about User Management'], input[aria-label='Tìm kiếm người dùng']");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final SidebarComponent sidebar;
    private boolean loaded;
    private Duration loadDuration = Duration.ZERO;
    private String expectedDestinationUrl = "";

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
        OverlayCleaner.dismissBlockingOverlays(driver);
        sidebar.ensureExpanded();
        expandParentMenuIfNeeded(target);

        String previousUrl = driver.getCurrentUrl();
        String previousContent = mainContentText();
        String targetLabel = menuLabel(target.name());
        expectedDestinationUrl = clickSidebarItem(targetLabel);

        waitForDestinationToLoad(targetLabel, previousUrl, previousContent, expectedDestinationUrl);

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

    public String expectedDestinationUrl() {
        return expectedDestinationUrl;
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean urlMatchesExpectedDestination() {
        if (expectedDestinationUrl == null || expectedDestinationUrl.isBlank()) {
            return true;
        }
        return samePath(driver.getCurrentUrl(), expectedDestinationUrl);
    }

    private void expandParentMenuIfNeeded(MenuTarget target) {
        if (!target.hasParent() || visibleSidebarItem(menuLabel(target.name())) != null) {
            return;
        }
        clickSidebarItem(menuLabel(target.parent()));
        wait.until(webDriver -> visibleSidebarItem(menuLabel(target.name())) != null);
    }

    private void waitForDestinationToLoad(
            String targetLabel, String previousUrl, String previousContent, String expectedUrl) {
        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> hasReachedExpectedUrl(expectedUrl)
                || hasNavigatedAwayFrom(previousUrl, previousContent));
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        wait.until(webDriver -> expectedPageMarkerIsVisible(targetLabel) || mainContentText().length() > 20);
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
    }

    private boolean hasNavigatedAwayFrom(String previousUrl, String previousContent) {
        return !driver.getCurrentUrl().equals(previousUrl)
                || !mainContentText().equals(previousContent);
    }

    private boolean noLoadingIndicatorIsVisible() {
        return driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed);
    }

    private boolean expectedPageMarkerIsVisible(String targetLabel) {
        if ("Tài chính".equals(targetLabel)) {
            return driver.findElements(FINANCE_PAGE_MARKER).stream()
                    .anyMatch(WebElement::isDisplayed);
        }
        if ("Quản Lí Người Dùng".equals(targetLabel)) {
            return driver.findElements(USER_MANAGEMENT_PAGE_MARKER).stream()
                    .anyMatch(WebElement::isDisplayed);
        }
        if ("Quản Lí eKYC".equals(targetLabel)) {
            return currentRouteIs("/vuatho/ekyc");
        }
        return false;
    }

    private boolean currentRouteIs(String path) {
        if (driver.getCurrentUrl().contains(path)) {
            return true;
        }
        Object nextPage = ((JavascriptExecutor) driver).executeScript(
                "const data=document.querySelector('#__NEXT_DATA__');"
                        + "return data && data.textContent.includes(arguments[0]);",
                "\"page\":\"" + path + "\"");
        return Boolean.TRUE.equals(nextPage);
    }

    private String clickSidebarItem(String label) {
        OverlayCleaner.dismissBlockingOverlays(driver);
        WebElement item = wait.until(webDriver -> visibleSidebarItem(label));
        String destination = item.getAttribute("href");
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", item);
        wait.until(webDriver -> visibleSidebarItem(label)).click();
        return destination == null ? "" : destination;
    }

    private WebElement visibleSidebarItem(String label) {
        WebElement byKnownHref = visibleSidebarItemByKnownHref(label);
        if (byKnownHref != null) {
            return byKnownHref;
        }
        return driver.findElements(exactText(label)).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() < 500)
                .findFirst()
                .orElse(null);
    }

    private WebElement visibleSidebarItemByKnownHref(String label) {
        if ("Dashboard".equals(label)) {
            return driver.findElements(DASHBOARD_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Hiệu Quả Nguồn Thợ & Chi Phí".equals(label)) {
            return driver.findElements(SUPPLY_PERFORMANCE_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Người Dùng".equals(label)) {
            return driver.findElements(USER_MANAGEMENT_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí eKYC".equals(label)) {
            return driver.findElements(EKYC_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Hồ Sơ Thợ".equals(label)) {
            return driver.findElements(WORKER_PROFILE_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Thợ Vi Phạm".equals(label)) {
            return driver.findElements(VIOLATION_WORKER_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Bài Training".equals(label)) {
            return driver.findElements(TRAINING_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Bài Đăng".equals(label)) {
            return driver.findElements(PROFILE_POST_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Yêu Cầu Ngưng Hợp Tác".equals(label)) {
            return driver.findElements(STOP_REQUEST_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Bài Kiểm Tra".equals(label)) {
            return driver.findElements(TESTED_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Nghiệp Vụ".equals(label)) {
            return driver.findElements(SERVICE_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Đơn Khách - Thợ".equals(label)) {
            return driver.findElements(CUSTOMER_WORKER_ORDER_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Đơn Thợ Phụ".equals(label)) {
            return driver.findElements(ASSISTANT_WORKER_ORDER_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Đồng Phục".equals(label)) {
            return driver.findElements(UNIFORM_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Đơn Hàng Đồng Phục".equals(label)) {
            return driver.findElements(ORDER_UNIFORM_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Kho Đồng Phục".equals(label)) {
            return driver.findElements(INVENTORY_UNIFORM_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Lịch Sử Giao Dịch".equals(label)) {
            return driver.findElements(TRANSACTION_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Danh Mục".equals(label)) {
            return driver.findElements(WEBSITE_CATEGORY_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Bài Viết Nội Bộ".equals(label)) {
            return driver.findElements(WEBSITE_BLOG_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Bài Viết Truyền Thông".equals(label)) {
            return driver.findElements(WEBSITE_MEDIA_BLOG_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Hỗ Trợ Người Dùng".equals(label)) {
            return driver.findElements(WEBSITE_SUPPORT_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Cấu Hình Website".equals(label)) {
            return driver.findElements(WEBSITE_CONFIG_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Popup".equals(label)) {
            return driver.findElements(APP_POPUP_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Phương Thức Thanh Toán".equals(label)) {
            return driver.findElements(APP_PAYMENT_METHOD_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Notification".equals(label)) {
            return driver.findElements(APP_NOTIFICATION_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Hình Thức Vi Phạm".equals(label)) {
            return driver.findElements(APP_VIOLATION_FORM_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Voucher".equals(label)) {
            return driver.findElements(APP_VOUCHER_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Chiến Dịch App".equals(label)) {
            return driver.findElements(APP_CAMPAIGN_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Cấu hình App".equals(label)) {
            return driver.findElements(APP_CONFIG_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí AI".equals(label)) {
            return driver.findElements(SYSTEM_AI_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Mạng Xã Hội".equals(label)) {
            return driver.findElements(SYSTEM_SOCIAL_NETWORK_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Hiệu suất Marketing".equals(label)) {
            return driver.findElements(MARKETING_STATISTIC_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Chương Trình Khuyến Mãi".equals(label)) {
            return driver.findElements(MARKETING_PROMOTION_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Chiến Dịch".equals(label)) {
            return driver.findElements(MARKETING_CAMPAIGNS_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Cuộc Thi".equals(label)) {
            return driver.findElements(MARKETING_COMPETITION_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Tỏa Sáng Vua Thợ".equals(label)) {
            return driver.findElements(MARKETING_TOA_SANG_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Quản Lí Vua Thợ Care".equals(label)) {
            return driver.findElements(MARKETING_INSURANCE_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Yêu Cầu Hỗ Trợ (SOS)".equals(label)) {
            return driver.findElements(MARKETING_SOS_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Tài chính".equals(label)) {
            return driver.findElements(FINANCE_MENU).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getX() < 500)
                    .findFirst()
                    .orElse(null);
        }
        if ("Người Dùng".equals(label)) {
            WebElement byAriaLabel = visibleElement(USER_PARENT_MENU);
            return byAriaLabel != null ? byAriaLabel : visibleParentMenuButton(0);
        }
        if ("Đối Tác - Thợ".equals(label)) {
            WebElement byAriaLabel = visibleElement(PARTNER_WORKER_PARENT_MENU);
            return byAriaLabel != null ? byAriaLabel : visibleParentMenuButton(1);
        }
        if ("Đơn Dịch Vụ".equals(label)) {
            WebElement byAriaLabel = visibleElement(SERVICE_ORDER_PARENT_MENU);
            return byAriaLabel != null ? byAriaLabel : visibleParentMenuButton(2);
        }
        if ("Đồng Phục".equals(label)) {
            WebElement byAriaLabel = visibleElement(UNIFORM_PARENT_MENU);
            return byAriaLabel != null ? byAriaLabel : visibleParentMenuButton(3);
        }
        if ("Giao Dịch".equals(label)) {
            WebElement byAriaLabel = visibleElement(TRANSACTION_PARENT_MENU);
            return byAriaLabel != null ? byAriaLabel : visibleParentMenuButton(4);
        }
        if ("Website".equals(label)) {
            WebElement byAriaLabel = visibleElement(WEBSITE_PARENT_MENU);
            return byAriaLabel != null ? byAriaLabel : visibleParentMenuButton(5);
        }
        if ("App".equals(label)) {
            WebElement byAriaLabel = visibleElement(APP_PARENT_MENU);
            return byAriaLabel != null ? byAriaLabel : visibleParentMenuButton(6);
        }
        if ("System".equals(label)) {
            WebElement byAriaLabel = visibleElement(SYSTEM_PARENT_MENU);
            return byAriaLabel != null ? byAriaLabel : visibleParentMenuButton(7);
        }
        if ("Marketing".equals(label)) {
            WebElement byAriaLabel = visibleElement(MARKETING_PARENT_MENU);
            return byAriaLabel != null ? byAriaLabel : visibleParentMenuButton(8);
        }
        return null;
    }

    private WebElement visibleElement(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() < 500)
                .findFirst()
                .orElse(null);
    }

    private WebElement visibleParentMenuButton(int index) {
        List<WebElement> parentMenus = driver.findElements(COLLAPSIBLE_PARENT_MENUS).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() < 500)
                .toList();
        return parentMenus.size() > index ? parentMenus.get(index) : null;
    }

    private By exactText(String label) {
        return By.xpath("//*[normalize-space()='" + label + "'"
                + " or normalize-space(text())='" + label + "']");
    }

    private String menuLabel(String catalogLabel) {
        if (catalogLabel.contains("Thá»‘ng KÃª Thá»£ - KhÃ¡ch")
                || catalogLabel.contains("Thống Kê Thợ - Khách")) {
            return "Hiệu suất Marketing";
        }
        return catalogLabel;
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

    private boolean hasReachedExpectedUrl(String expectedUrl) {
        return expectedUrl == null || expectedUrl.isBlank()
                || samePath(driver.getCurrentUrl(), expectedUrl);
    }

    private boolean samePath(String actualUrl, String expectedUrl) {
        try {
            URI actual = URI.create(actualUrl);
            URI expected = URI.create(expectedUrl);
            String expectedPath = expected.getPath();
            return expectedPath != null && expectedPath.equals(actual.getPath());
        } catch (IllegalArgumentException ignored) {
            return actualUrl.contains(expectedUrl);
        }
    }
}
