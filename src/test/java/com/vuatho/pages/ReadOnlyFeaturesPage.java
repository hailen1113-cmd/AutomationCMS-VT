package com.vuatho.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.vuatho.utils.PageLoadSynchronizer;

import java.time.Duration;

public class ReadOnlyFeaturesPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public ReadOnlyFeaturesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.wait.pollingEvery(Duration.ofMillis(200));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    public void searchAndReset(String placeholder, String query) {
        PageLoadSynchronizer.prepareForAsyncAction(driver);
        String stateBeforeSearch = PageLoadSynchronizer.mainContentState(driver);
        WebElement input = visibleInput(placeholder);
        input.clear();
        input.sendKeys(query);
        wait.until(webDriver -> query.equals(visibleInput(placeholder).getAttribute("value")));
        PageLoadSynchronizer.waitForSearchResultsToLoad(driver, stateBeforeSearch);

        PageLoadSynchronizer.prepareForAsyncAction(driver);
        String stateBeforeReset = PageLoadSynchronizer.mainContentState(driver);
        clickMainButton("Reset");
        wait.until(webDriver -> visibleInput(placeholder).getAttribute("value").isBlank());
        PageLoadSynchronizer.waitForSearchResultsToLoad(driver, stateBeforeReset);
    }

    public String switchRouteTab(String tabLabel) {
        String previousState = pageState(tabLabel);
        clickMainButton(tabLabel);
        wait.until(webDriver -> !pageState(tabLabel).equals(previousState)
                || isSelected(mainButton(tabLabel)));
        return pageState(tabLabel);
    }

    public void switchView(String viewLabel) {
        String previousContent = mainText();
        clickMainButton(viewLabel);
        wait.until(webDriver -> isSelected(mainButton(viewLabel))
                || !mainText().equals(previousContent));
    }

    public void goToPaginationPage(String pageNumber) {
        WebElement page = wait.until(webDriver -> visibleElement(By.xpath(
                "//*[@role='button' and normalize-space()='" + pageNumber + "']")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", page);
        page.click();
        wait.until(webDriver -> isSelected(visibleElement(By.xpath(
                "//*[@role='button' and normalize-space()='" + pageNumber + "']"))));
    }

    public void openDropdownAndVerifyOption(String dropdownLabel, String optionLabel) {
        clickMainButton(dropdownLabel);
        wait.until(webDriver -> visibleContentElement(By.xpath(
                "//*[normalize-space()='" + optionLabel + "']")) != null);
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    public boolean inputIsEmpty(String placeholder) {
        return visibleInput(placeholder).getAttribute("value").isBlank();
    }

    public boolean isControlSelected(String label) {
        return isSelected(mainButton(label));
    }

    public void openControl(String label) {
        clickMainButton(label);
    }

    public void closeOverlay() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    public void openFirstTextInput() {
        WebElement input = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "return [...document.querySelectorAll('input')].find(e=>{"
                        + "const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.x>300&&r.width>0&&r.height>0&&s.display!=='none'"
                        + "&&s.visibility!=='hidden';})||null;");
        if (input == null) {
            throw new IllegalStateException("Không tìm thấy filter input trong content area.");
        }
        input.click();
    }

    private void clickMainButton(String label) {
        WebElement button = wait.until(webDriver -> mainButton(label));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", button);
        wait.until(webDriver -> mainButton(label)).click();
    }

    private WebElement mainButton(String label) {
        return visibleContentElement(By.xpath(
                "//button[normalize-space(.)='" + label + "' or .//*[normalize-space()='" + label + "']"
                        + " or @aria-label='" + label + "' or @title='" + label + "']"
                        + " | //*[@role='tab' and normalize-space()='" + label + "']"));
    }

    private WebElement visibleInput(String placeholder) {
        WebElement input = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const expected=arguments[0].toLocaleLowerCase();"
                        + "const inputs=[...document.querySelectorAll('input')].filter(e=>{"
                        + "const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "const p=e.placeholder||e.getAttribute('aria-label')||e.title||e.name||'';"
                        + "return r.x>300&&r.width>0&&r.height>0&&s.display!=='none'"
                        + "&&s.visibility!=='hidden'&&p.trim()!=='';});"
                        + "const label=e=>(e.placeholder||e.getAttribute('aria-label')||e.title||e.name||'')"
                        + ".toLocaleLowerCase();"
                        + "return inputs.find(e=>label(e).includes(expected))"
                        + "||inputs[0]||null;", placeholder);
        if (input == null) {
            throw new IllegalStateException("Không tìm thấy input: " + placeholder);
        }
        return input;
    }

    private WebElement visibleElement(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    private WebElement visibleContentElement(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() > 300)
                .findFirst()
                .orElse(null);
    }

    private boolean isSelected(WebElement element) {
        if (element == null) {
            return false;
        }
        WebElement currentElement = element;
        for (int level = 0; level < 4 && currentElement != null; level++) {
            String classes = String.valueOf(currentElement.getAttribute("class")).toLowerCase();
            String state = String.valueOf(currentElement.getAttribute("data-state"));
            String selected = currentElement.getAttribute("aria-selected");
            String current = currentElement.getAttribute("aria-current");
            String pressed = currentElement.getAttribute("aria-pressed");
            if (classes.contains("active") || classes.contains("selected")
                    || "active".equalsIgnoreCase(state) || "true".equalsIgnoreCase(selected)
                    || "true".equalsIgnoreCase(pressed) || "page".equalsIgnoreCase(current)) {
                return true;
            }
            currentElement = currentElement.findElements(By.xpath("..")).stream()
                    .findFirst().orElse(null);
        }
        return false;
    }

    private String mainText() {
        WebElement main = visibleElement(By.cssSelector("main, [role='main']"));
        return main == null ? "" : main.getText();
    }

    private String pageState(String controlLabel) {
        WebElement control = mainButton(controlLabel);
        String controlState = control == null ? "missing"
                : String.valueOf(control.getAttribute("class"))
                + control.getAttribute("aria-selected")
                + control.getAttribute("aria-pressed")
                + control.getAttribute("data-state");
        return driver.getCurrentUrl() + "|" + controlState + "|" + mainText().hashCode();
    }
}
