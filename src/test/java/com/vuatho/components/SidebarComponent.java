package com.vuatho.components;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public class SidebarComponent {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration TOGGLE_TIMEOUT = Duration.ofSeconds(4);
    private static final By DASHBOARD_TEXT = By.xpath("//*[normalize-space()='Dashboard']");
    private static final By DASHBOARD_MENU = By.cssSelector("a[href='/vuatho/dashboard']");
    private static final By MENU_ROOT = By.cssSelector("[aria-label='menu']");
    private static final By SIDEBAR_TOGGLE = By.cssSelector(
            "button[type='button'].h-8.w-8");
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebDriverWait toggleWait;
    private WebElement cachedRoot;

    /**
     * Khởi tạo SidebarComponent với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public SidebarComponent(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        this.toggleWait = new WebDriverWait(driver, TOGGLE_TIMEOUT);
    }

    /**
     * Thực hiện xử lý text trong luồng kiểm thử.
     * @return kết quả text sau khi xử lý
     */
    public String text() {
        return root().getText();
    }

    /**
     * Kiểm tra điều kiện is logo loaded.
     * @return kết quả is logo loaded sau khi xử lý
     */
    public boolean isLogoLoaded() {
        return driver.findElements(By.cssSelector("img, svg")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() < 350 && element.getRect().getY() < 200)
                .filter(element -> element.getRect().getWidth() >= 20
                        && element.getRect().getHeight() >= 20)
                .anyMatch(element -> !"img".equalsIgnoreCase(element.getTagName())
                        || ((Number) ((JavascriptExecutor) driver)
                                .executeScript("return arguments[0].naturalWidth || 0;", element)).intValue() > 0);
    }

    /**
     * Thực hiện xử lý width trong luồng kiểm thử.
     * @return kết quả width sau khi xử lý
     */
    public double width() {
        return root().getRect().getWidth();
    }

    /**
     * Thực hiện xử lý ensure expanded trong luồng kiểm thử.
     */
    public void ensureExpanded() {
        if (root().getRect().getWidth() > 180 && dashboardMenuIsVisible()) {
            return;
        }
        clickToggleButton();
        toggleWait.until(webDriver -> root().getRect().getWidth() > 180 && dashboardMenuIsVisible());
    }

    /**
     * Thực hiện xử lý collapse trong luồng kiểm thử.
     */
    public void collapse() {
        WebElement sidebar = root();
        double widthBefore = sidebar.getRect().getWidth();
        clickToggleButton();
        toggleWait.until(webDriver -> sidebar.getRect().getWidth() < widthBefore);
    }

    /**
     * Thực hiện xử lý expand trong luồng kiểm thử.
     */
    public void expand() {
        WebElement sidebar = root();
        double widthBefore = sidebar.getRect().getWidth();
        clickToggleButton();
        toggleWait.until(webDriver -> sidebar.getRect().getWidth() > widthBefore);
    }

    /**
     * Kiểm tra điều kiện is dashboard active.
     * @return kết quả is dashboard active sau khi xử lý
     */
    public boolean isDashboardActive() {
        WebElement menu = dashboardMenu();
        WebElement current = menu;
        for (int level = 0; level < 4 && current != null; level++) {
            String classes = String.valueOf(current.getAttribute("class")).toLowerCase();
            String ariaCurrent = current.getAttribute("aria-current");
            String selected = current.getAttribute("data-state");
            if (classes.contains("active") || classes.contains("selected")
                    || "page".equalsIgnoreCase(ariaCurrent) || "active".equalsIgnoreCase(selected)) {
                return true;
            }
            current = current.findElement(By.xpath(".."));
        }
        String background = menu.getCssValue("background-color");
        return background != null && !background.equals("rgba(0, 0, 0, 0)")
                && !background.equals("transparent");
    }

    /**
     * Thực hiện xử lý root trong luồng kiểm thử.
     * @return kết quả root sau khi xử lý
     */
    private WebElement root() {
        if (cachedRoot != null) {
            try {
                cachedRoot.getTagName();
                return cachedRoot;
            } catch (StaleElementReferenceException ignored) {
                cachedRoot = null;
            }
        }

        WebElement labeledMenu = driver.findElements(MENU_ROOT).stream().findFirst().orElse(null);
        if (labeledMenu != null) {
            cachedRoot = labeledMenu;
            return cachedRoot;
        }

        WebElement menu = dashboardMenu();
        List<WebElement> semanticParents = menu.findElements(By.xpath(
                "ancestor::*[self::aside or self::nav or @role='navigation'"
                        + " or contains(translate(@class,'SIDEBAR','sidebar'),'sidebar')][1]"));
        if (!semanticParents.isEmpty()) {
            cachedRoot = semanticParents.get(0);
            return cachedRoot;
        }
        WebElement inferred = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "let e=arguments[0]; while(e && e!==document.body){const r=e.getBoundingClientRect();"
                        + "if(r.height>innerHeight*.7 && r.width>150 && r.width<500) return e;"
                        + "e=e.parentElement;} return null;",
                menu);
        if (inferred == null) {
            throw new NoSuchElementException("Không xác định được sidebar.");
        }
        cachedRoot = inferred;
        return cachedRoot;
    }

    /**
     * Thực hiện xử lý dashboard menu trong luồng kiểm thử.
     * @return kết quả dashboard menu sau khi xử lý
     */
    private WebElement dashboardMenu() {
        return wait.until(webDriver -> visibleDashboardMenu());
    }

    /**
     * Thực hiện xử lý dashboard menu is visible trong luồng kiểm thử.
     * @return kết quả dashboard menu is visible sau khi xử lý
     */
    private boolean dashboardMenuIsVisible() {
        return visibleDashboardMenu() != null;
    }

    /**
     * Trả về visible dashboard menu từ trạng thái hiện tại.
     * @return kết quả visible dashboard menu sau khi xử lý
     */
    private WebElement visibleDashboardMenu() {
        WebElement byHref = driver.findElements(DASHBOARD_MENU).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() < 500)
                .findFirst()
                .orElse(null);
        if (byHref != null) {
            return byHref;
        }
        return driver.findElements(DASHBOARD_TEXT).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() < 500)
                .findFirst()
                .orElse(null);
    }

    /**
     * Kích hoạt toggle button trong luồng kiểm thử.
     */
    private void clickToggleButton() {
        WebElement button = toggleButton();
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", button);
    }

    /**
     * Thực hiện xử lý toggle button trong luồng kiểm thử.
     * @return kết quả toggle button sau khi xử lý
     */
    private WebElement toggleButton() {
        WebElement styledToggle = driver.findElements(SIDEBAR_TOGGLE).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getRect().getX() < 120 && button.getRect().getY() < 220)
                .findFirst()
                .orElse(null);
        if (styledToggle != null) {
            return styledToggle;
        }

        List<WebElement> namedButtons = driver.findElements(By.xpath(
                "//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'collapse')"
                        + " or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'collapse')"
                        + " or contains(@aria-label,'Thu gọn') or contains(@title,'Thu gọn')"
                        + " or contains(@aria-label,'Mở rộng') or contains(@title,'Mở rộng')]"));
        if (!namedButtons.isEmpty()) {
            return namedButtons.stream().filter(WebElement::isDisplayed).findFirst().orElseThrow();
        }
        return driver.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed).filter(button -> button.getText().isBlank())
                .filter(button -> {
                    Rectangle rectangle = button.getRect();
                    return rectangle.getWidth() >= 20 && rectangle.getWidth() <= 56
                            && rectangle.getHeight() >= 20 && rectangle.getHeight() <= 56
                            && rectangle.getY() < 180
                            && rectangle.getX() < 100;
                })
                .min(Comparator.comparingInt(button -> Math.abs(button.getRect().getY() - 90)))
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nút thu gọn sidebar."));
    }
}
