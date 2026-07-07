package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
    private static final By COMPANY_HEADER = By.xpath("//*[normalize-space()='Công ty Vua Thợ']");
    private static final By GOOGLE_LOGIN = By.xpath("//*[contains(normalize-space(.),'Google')]");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.wait.pollingEvery(Duration.ofMillis(200));
    }

    public DashboardPage open() {
        driver.get(TestConfig.entryUrl());
        return this;
    }

    public boolean isLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_TEXT));
            wait.until(ExpectedConditions.visibilityOfElementLocated(COMPANY_HEADER));
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

    public List<String> missingMenuGroups() {
        String sidebarText = sidebar().getText();
        return EXPECTED_MENU_GROUPS.stream()
                .filter(item -> !sidebarText.contains(item))
                .toList();
    }

    public boolean isLogoLoaded() {
        return sidebar().findElements(By.tagName("img")).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(image -> ((Number) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].naturalWidth || 0;", image)).intValue() > 0);
    }

    public double sidebarWidth() {
        return sidebar().getRect().getWidth();
    }

    public void collapseSidebar() {
        double widthBefore = sidebarWidth();
        collapseButton().click();
        wait.until(webDriver -> sidebarWidth() < widthBefore || !dashboardMenu().isDisplayed());
    }

    public void expandSidebar() {
        double widthBefore = sidebarWidth();
        collapseButton().click();
        wait.until(webDriver -> sidebarWidth() > widthBefore && dashboardMenu().isDisplayed());
    }

    public boolean isDashboardMenuActive() {
        WebElement menu = dashboardMenu();
        WebElement current = menu;
        for (int level = 0; level < 4 && current != null; level++) {
            String classes = String.valueOf(current.getAttribute("class")).toLowerCase();
            String ariaCurrent = current.getAttribute("aria-current");
            String selected = current.getAttribute("data-state");
            if (classes.contains("active") || classes.contains("selected")
                    || "page".equalsIgnoreCase(ariaCurrent)
                    || "active".equalsIgnoreCase(selected)) {
                return true;
            }
            current = current.findElement(By.xpath(".."));
        }

        String background = dashboardMenu().getCssValue("background-color");
        return background != null
                && !background.equals("rgba(0, 0, 0, 0)")
                && !background.equals("transparent");
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
        return isVisible(DASHBOARD_TEXT) && isVisible(COMPANY_HEADER);
    }

    private WebElement dashboardMenu() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_TEXT));
    }

    private WebElement sidebar() {
        WebElement menu = dashboardMenu();
        List<WebElement> semanticParents = menu.findElements(By.xpath(
                "ancestor::*[self::aside or self::nav or @role='navigation'"
                        + " or contains(translate(@class,'SIDEBAR','sidebar'),'sidebar')][1]"));
        if (!semanticParents.isEmpty()) {
            return semanticParents.get(0);
        }

        WebElement inferred = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "let e=arguments[0]; while(e && e!==document.body){"
                        + "const r=e.getBoundingClientRect();"
                        + "if(r.height>innerHeight*.7 && r.width>150 && r.width<500) return e;"
                        + "e=e.parentElement;} return null;", menu);
        if (inferred == null) {
            throw new NoSuchElementException("Không xác định được sidebar.");
        }
        return inferred;
    }

    private WebElement collapseButton() {
        List<WebElement> namedButtons = driver.findElements(By.xpath(
                "//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'collapse')"
                        + " or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'collapse')"
                        + " or contains(@aria-label,'Thu gọn') or contains(@title,'Thu gọn')"
                        + " or contains(@aria-label,'Mở rộng') or contains(@title,'Mở rộng')]"));
        if (!namedButtons.isEmpty()) {
            return namedButtons.stream().filter(WebElement::isDisplayed).findFirst().orElseThrow();
        }

        Rectangle sidebarRect = sidebar().getRect();
        return driver.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getText().isBlank())
                .filter(button -> {
                    Rectangle rectangle = button.getRect();
                    return rectangle.getWidth() >= 20 && rectangle.getWidth() <= 56
                            && rectangle.getHeight() >= 20 && rectangle.getHeight() <= 56
                            && rectangle.getY() < 260
                            && rectangle.getX() <= sidebarRect.getX() + sidebarRect.getWidth();
                })
                .min(Comparator.comparingInt(button -> Math.abs(button.getRect().getY() - 140)))
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nút thu gọn sidebar."));
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
}
