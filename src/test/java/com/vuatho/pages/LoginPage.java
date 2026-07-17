package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

public class LoginPage {
    private static final By GOOGLE_LOGIN_BUTTON = By.xpath(
            "//button[contains(normalize-space(.),'Google')]"
                    + " | //a[contains(normalize-space(.),'Google')]"
                    + " | //*[@role='button' and contains(normalize-space(.),'Google')]");
    private static final By GOOGLE_EMAIL = By.cssSelector(
            "#identifierId, input[type='email'], input[name='identifier']");
    private static final By GOOGLE_EMAIL_NEXT = By.id("identifierNext");
    private static final By GOOGLE_PASSWORD = By.cssSelector(
            "input[name='Passwd'], input[type='password']");
    private static final By GOOGLE_PASSWORD_NEXT = By.id("passwordNext");
    private static final By GOOGLE_ACCOUNT = By.cssSelector("[data-identifier]");
    private static final By GOOGLE_REJECTED_SIGN_IN = By.xpath(
            "//*[contains(normalize-space(.),\"Couldn't sign you in\")]"
                    + " | //*[contains(normalize-space(.),'This browser or app may not be secure')]");
    private static final By USE_ANOTHER_ACCOUNT = By.xpath(
            "//*[contains(normalize-space(.),'Use another account')]"
                    + " | //*[contains(normalize-space(.),'Sử dụng một tài khoản khác')]");

    private final WebDriver driver;
    private final WebDriverWait wait;

    /**
     * Khởi tạo LoginPage với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.wait.pollingEvery(Duration.ofMillis(200));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    /**
     * Thực hiện xử lý login with google trong luồng kiểm thử.
     * @param email giá trị email được truyền vào
     * @param passwordSupplier giá trị password supplier được truyền vào
     */
    public void loginWithGoogle(String email, Supplier<String> passwordSupplier) {
        String erpWindow = driver.getWindowHandle();
        Set<String> windowsBeforeClick = driver.getWindowHandles();

        wait.until(webDriver -> isOnErp() || firstVisible(GOOGLE_LOGIN_BUTTON) != null);
        if (isOnErp()) {
            return;
        }

        wait.until(ExpectedConditions.elementToBeClickable(GOOGLE_LOGIN_BUTTON)).click();
        switchToGoogleWindow(windowsBeforeClick);

        if (isDashboardVisibleNow()) {
            return;
        }

        failFastIfGoogleRejectsAutomatedBrowser();
        chooseAccountOrEnterEmail(email);

        failFastIfGoogleRejectsAutomatedBrowser();
        WebElement passwordInput = waitForPasswordOrReturnToErp();
        if (passwordInput != null) {
            String password = passwordSupplier.get();
            if (password == null || password.isBlank()) {
                waitForManualGoogleCompletion();
            } else {
                enterGooglePassword(passwordInput, password);
            }
        }

        returnToErpWindow(erpWindow);
    }

    /**
     * Kiểm tra điều kiện is dashboard visible.
     * @param timeout thời gian chờ tối đa
     * @return kết quả is dashboard visible sau khi xử lý
     */
    public boolean isDashboardVisible(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(webDriver ->
                    webDriver.getCurrentUrl().contains("dashboard")
                            || hasVisibleText("Dashboard")
                            || hasVisibleText("Công ty Vua Thợ"));
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    /**
     * Kiểm tra điều kiện is dashboard visible now.
     * @return kết quả is dashboard visible now sau khi xử lý
     */
    public boolean isDashboardVisibleNow() {
        return isOnErp();
    }

    /**
     * Thực hiện xử lý switch to google window trong luồng kiểm thử.
     * @param windowsBeforeClick giá trị windows before click được truyền vào
     */
    private void switchToGoogleWindow(Set<String> windowsBeforeClick) {
        wait.until(webDriver ->
                webDriver.getWindowHandles().size() > windowsBeforeClick.size()
                        || webDriver.getCurrentUrl().contains("accounts.google.com")
                        || hasVisibleText("Dashboard")
                        || hasVisibleText("Công ty Vua Thợ"));

        for (String window : driver.getWindowHandles()) {
            if (!windowsBeforeClick.contains(window)) {
                driver.switchTo().window(window);
                return;
            }
        }
    }

    /**
     * Kích hoạt account or enter email trong luồng kiểm thử.
     * @param email giá trị email được truyền vào
     */
    private void chooseAccountOrEnterEmail(String email) {
        wait.until(webDriver -> isOnErp()
                || googleRejectedAutomatedBrowser()
                || firstVisible(GOOGLE_EMAIL) != null
                || firstVisible(GOOGLE_PASSWORD) != null
                || firstVisible(GOOGLE_ACCOUNT) != null
                || firstVisible(USE_ANOTHER_ACCOUNT) != null);
        failFastIfGoogleRejectsAutomatedBrowser();

        if (isOnErp() || firstVisible(GOOGLE_PASSWORD) != null) {
            return;
        }

        WebElement matchingAccount = googleAccountFor(email);
        if (matchingAccount != null) {
            clickElement(matchingAccount);
            return;
        }

        WebElement emailInput = firstVisible(GOOGLE_EMAIL);
        if (emailInput == null) {
            WebElement useAnotherAccount = wait.until(
                    ExpectedConditions.elementToBeClickable(USE_ANOTHER_ACCOUNT));
            useAnotherAccount.click();
            emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(GOOGLE_EMAIL));
        }

        replaceText(emailInput, email);
        wait.until(ExpectedConditions.attributeToBe(GOOGLE_EMAIL, "value", email));
        clickGoogleNext(GOOGLE_EMAIL_NEXT);
    }

    /**
     * Chờ for password or return to erp trong luồng kiểm thử.
     * @return kết quả wait for password or return to erp sau khi xử lý
     */
    private WebElement waitForPasswordOrReturnToErp() {
        try {
            wait.until(webDriver -> isOnErp()
                    || googleRejectedAutomatedBrowser()
                    || firstVisible(GOOGLE_PASSWORD) != null);
            failFastIfGoogleRejectsAutomatedBrowser();
            return isOnErp() ? null : firstVisible(GOOGLE_PASSWORD);
        } catch (WebDriverException ignored) {
            return null;
        }
    }

    /**
     * Thực hiện xử lý fail fast if google rejects automated browser trong luồng kiểm thử.
     */
    private void failFastIfGoogleRejectsAutomatedBrowser() {
        if (!googleRejectedAutomatedBrowser()) {
            return;
        }
        throw new IllegalStateException(
                "Google rejected automated sign-in. Run GoogleSessionSetup.main(), sign in manually, "
                        + "close that Chrome window, then rerun tests with -Dheadless=false "
                        + "or -Dselenium.profile.dir=.selenium/chrome-profile.");
    }

    /**
     * Thực hiện xử lý google rejected automated browser trong luồng kiểm thử.
     * @return kết quả google rejected automated browser sau khi xử lý
     */
    private boolean googleRejectedAutomatedBrowser() {
        return firstVisible(GOOGLE_REJECTED_SIGN_IN) != null
                || driver.getCurrentUrl().contains("accounts.google.com")
                && driver.getCurrentUrl().contains("/signin/rejected");
    }

    /**
     * Chờ for manual google completion trong luồng kiểm thử.
     */
    private void waitForManualGoogleCompletion() {
        if (TestConfig.headless()) {
            throw new IllegalStateException(
                    "Chua co GOOGLE_PASSWORD va browser dang chay headless. "
                            + "Hay set GOOGLE_PASSWORD hoac chay -Dheadless=false -Dinteractive=true.");
        }
        System.out.println("Google dang yeu cau mat khau/xac minh. Hay hoan tat thu cong trong Chrome...");
        try {
            new WebDriverWait(driver, Duration.ofMinutes(2))
                    .until(webDriver -> isOnErp() || firstVisible(GOOGLE_PASSWORD) == null);
        } catch (TimeoutException timeout) {
            throw new IllegalStateException(
                    "Chua hoan tat dang nhap Google thu cong trong 2 phut.", timeout);
        }
    }

    /**
     * Cập nhật google password trong luồng kiểm thử.
     * @param passwordInput giá trị password input được truyền vào
     * @param password giá trị password được truyền vào
     */
    private void enterGooglePassword(WebElement passwordInput, String password) {
        replaceText(passwordInput, password);
        clickGoogleNext(GOOGLE_PASSWORD_NEXT);
    }

    /**
     * Kích hoạt google next trong luồng kiểm thử.
     * @param locator locator xác định phần tử
     */
    private void clickGoogleNext(By locator) {
        WebElement next = wait.until(ExpectedConditions.elementToBeClickable(locator));
        clickElement(next);
    }

    /**
     * Kích hoạt element trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     */
    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Thực hiện xử lý replace text trong luồng kiểm thử.
     * @param input giá trị input được truyền vào
     * @param value giá trị đầu vào
     */
    private void replaceText(WebElement input, String value) {
        input.click();
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value='';"
                        + "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));"
                        + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
    }

    /**
     * Thực hiện xử lý return to erp window trong luồng kiểm thử.
     * @param erpWindow giá trị erp window được truyền vào
     */
    private void returnToErpWindow(String erpWindow) {
        try {
            new WebDriverWait(driver, Duration.ofMinutes(2)).until(webDriver -> {
                try {
                    return switchToAppWindowIfPresent()
                            || !webDriver.getWindowHandles().contains(webDriver.getWindowHandle());
                } catch (NoSuchWindowException ignored) {
                    return true;
                }
            });
        } catch (TimeoutException ignored) {
            // Google may be waiting for a manual CAPTCHA/MFA confirmation.
        }

        if (driver.getWindowHandles().contains(erpWindow)) {
            driver.switchTo().window(erpWindow);
        } else {
            driver.switchTo().window(driver.getWindowHandles().iterator().next());
        }
    }

    /**
     * Thực hiện xử lý switch to app window if present trong luồng kiểm thử.
     * @return kết quả switch to app window if present sau khi xử lý
     */
    private boolean switchToAppWindowIfPresent() {
        for (String window : driver.getWindowHandles()) {
            driver.switchTo().window(window);
            if (currentHostIsBase()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra điều kiện has visible text.
     * @param text nội dung cần xử lý
     * @return kết quả has visible text sau khi xử lý
     */
    private boolean hasVisibleText(String text) {
        return driver.findElements(By.xpath("//*[normalize-space()='" + text + "']")).stream()
                .anyMatch(WebElement::isDisplayed);
    }

    /**
     * Trả về first visible từ trạng thái hiện tại.
     * @param locator locator xác định phần tử
     * @return kết quả first visible sau khi xử lý
     */
    private WebElement firstVisible(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    /**
     * Thực hiện xử lý google account for trong luồng kiểm thử.
     * @param email giá trị email được truyền vào
     * @return kết quả google account for sau khi xử lý
     */
    private WebElement googleAccountFor(String email) {
        WebElement byDataIdentifier = driver.findElements(GOOGLE_ACCOUNT).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> email.equalsIgnoreCase(element.getAttribute("data-identifier")))
                .findFirst()
                .orElse(null);
        if (byDataIdentifier != null) {
            return byDataIdentifier;
        }

        return driver.findElements(By.xpath("//*[contains(normalize-space(.),'" + email + "')]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(this::clickableAccountContainer)
                .findFirst()
                .orElse(null);
    }

    /**
     * Kích hoạt account container trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     * @return kết quả clickable account container sau khi xử lý
     */
    private WebElement clickableAccountContainer(WebElement element) {
        return (WebElement) ((JavascriptExecutor) driver).executeScript(
                "let e=arguments[0];"
                        + "while(e && e!==document.body){"
                        + " const role=e.getAttribute('role');"
                        + " if(role==='link' || role==='button' || role==='option') return e;"
                        + " if(e.tabIndex>=0) return e;"
                        + " e=e.parentElement;"
                        + "}"
                        + "return arguments[0];",
                element);
    }

    /**
     * Kiểm tra điều kiện is on erp.
     * @return kết quả is on erp sau khi xử lý
     */
    private boolean isOnErp() {
        try {
            String url = driver.getCurrentUrl();
            if (currentHostIsBase()
                    && !url.contains("login")
                    && firstVisible(GOOGLE_LOGIN_BUTTON) == null) {
                return true;
            }
            return currentHostIsBase()
                    && (hasVisibleText("Dashboard") || hasVisibleText("Công ty Vua Thợ"));
        } catch (NoSuchWindowException ignored) {
            return true;
        } catch (WebDriverException ignored) {
            return false;
        }
    }

    /**
     * Trả về current host is base từ trạng thái hiện tại.
     * @return kết quả current host is base sau khi xử lý
     */
    private boolean currentHostIsBase() {
        try {
            String host = URI.create(driver.getCurrentUrl()).getHost();
            return TestConfig.baseHost().equalsIgnoreCase(host);
        } catch (IllegalArgumentException | WebDriverException exception) {
            return false;
        }
    }
}
