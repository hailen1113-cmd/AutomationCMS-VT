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
    private static final By USE_ANOTHER_ACCOUNT = By.xpath(
            "//*[contains(normalize-space(.),'Use another account')]"
                    + " | //*[contains(normalize-space(.),'Sử dụng một tài khoản khác')]");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.wait.pollingEvery(Duration.ofMillis(200));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

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

        chooseAccountOrEnterEmail(email);

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

    public boolean isDashboardVisibleNow() {
        return isOnErp();
    }

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

    private void chooseAccountOrEnterEmail(String email) {
        wait.until(webDriver -> isOnErp()
                || firstVisible(GOOGLE_EMAIL) != null
                || firstVisible(GOOGLE_PASSWORD) != null
                || firstVisible(GOOGLE_ACCOUNT) != null
                || firstVisible(USE_ANOTHER_ACCOUNT) != null);

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

    private WebElement waitForPasswordOrReturnToErp() {
        try {
            wait.until(webDriver -> isOnErp() || firstVisible(GOOGLE_PASSWORD) != null);
            return isOnErp() ? null : firstVisible(GOOGLE_PASSWORD);
        } catch (WebDriverException ignored) {
            return null;
        }
    }

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

    private void enterGooglePassword(WebElement passwordInput, String password) {
        replaceText(passwordInput, password);
        clickGoogleNext(GOOGLE_PASSWORD_NEXT);
    }

    private void clickGoogleNext(By locator) {
        WebElement next = wait.until(ExpectedConditions.elementToBeClickable(locator));
        clickElement(next);
    }

    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

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

    private boolean switchToAppWindowIfPresent() {
        for (String window : driver.getWindowHandles()) {
            driver.switchTo().window(window);
            if (currentHostIsBase()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasVisibleText(String text) {
        return driver.findElements(By.xpath("//*[normalize-space()='" + text + "']")).stream()
                .anyMatch(WebElement::isDisplayed);
    }

    private WebElement firstVisible(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

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

    private boolean currentHostIsBase() {
        try {
            String host = URI.create(driver.getCurrentUrl()).getHost();
            return TestConfig.baseHost().equalsIgnoreCase(host);
        } catch (IllegalArgumentException | WebDriverException exception) {
            return false;
        }
    }
}
