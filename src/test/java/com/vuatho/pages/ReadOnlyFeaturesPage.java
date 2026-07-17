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

    /**
     * Khởi tạo ReadOnlyFeaturesPage với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public ReadOnlyFeaturesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.wait.pollingEvery(Duration.ofMillis(200));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    /**
     * Thực hiện xử lý search and reset trong luồng kiểm thử.
     * @param placeholder giá trị placeholder được truyền vào
     * @param query giá trị query được truyền vào
     */
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

    /**
     * Thực hiện xử lý switch route tab trong luồng kiểm thử.
     * @param tabLabel giá trị tab label được truyền vào
     * @return kết quả switch route tab sau khi xử lý
     */
    public String switchRouteTab(String tabLabel) {
        String previousState = pageState(tabLabel);
        clickMainButton(tabLabel);
        wait.until(webDriver -> !pageState(tabLabel).equals(previousState)
                || isSelected(mainButton(tabLabel)));
        return pageState(tabLabel);
    }

    /**
     * Thực hiện xử lý switch view trong luồng kiểm thử.
     * @param viewLabel giá trị view label được truyền vào
     */
    public void switchView(String viewLabel) {
        String previousContent = mainText();
        clickMainButton(viewLabel);
        wait.until(webDriver -> isSelected(mainButton(viewLabel))
                || !mainText().equals(previousContent));
    }

    /**
     * Thực hiện xử lý go to pagination page trong luồng kiểm thử.
     * @param pageNumber giá trị page number được truyền vào
     */
    public void goToPaginationPage(String pageNumber) {
        WebElement page = wait.until(webDriver -> visibleElement(By.xpath(
                "//*[@role='button' and normalize-space()='" + pageNumber + "']")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", page);
        page.click();
        wait.until(webDriver -> isSelected(visibleElement(By.xpath(
                "//*[@role='button' and normalize-space()='" + pageNumber + "']"))));
    }

    /**
     * Mở dropdown and verify option trong luồng kiểm thử.
     * @param dropdownLabel giá trị dropdown label được truyền vào
     * @param optionLabel giá trị option label được truyền vào
     */
    public void openDropdownAndVerifyOption(String dropdownLabel, String optionLabel) {
        clickMainButton(dropdownLabel);
        wait.until(webDriver -> visibleContentElement(By.xpath(
                "//*[normalize-space()='" + optionLabel + "']")) != null);
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    /**
     * Thực hiện xử lý input is empty trong luồng kiểm thử.
     * @param placeholder giá trị placeholder được truyền vào
     * @return kết quả input is empty sau khi xử lý
     */
    public boolean inputIsEmpty(String placeholder) {
        return visibleInput(placeholder).getAttribute("value").isBlank();
    }

    /**
     * Kiểm tra điều kiện is control selected.
     * @param label giá trị label được truyền vào
     * @return kết quả is control selected sau khi xử lý
     */
    public boolean isControlSelected(String label) {
        return isSelected(mainButton(label));
    }

    /**
     * Mở control trong luồng kiểm thử.
     * @param label giá trị label được truyền vào
     */
    public void openControl(String label) {
        clickMainButton(label);
    }

    /**
     * Thực hiện xử lý close overlay trong luồng kiểm thử.
     */
    public void closeOverlay() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    /**
     * Mở first text input trong luồng kiểm thử.
     */
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

    /**
     * Kích hoạt main button trong luồng kiểm thử.
     * @param label giá trị label được truyền vào
     */
    private void clickMainButton(String label) {
        WebElement button = wait.until(webDriver -> mainButton(label));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", button);
        wait.until(webDriver -> mainButton(label)).click();
    }

    /**
     * Thực hiện xử lý main button trong luồng kiểm thử.
     * @param label giá trị label được truyền vào
     * @return kết quả main button sau khi xử lý
     */
    private WebElement mainButton(String label) {
        return visibleContentElement(By.xpath(
                "//button[normalize-space(.)='" + label + "' or .//*[normalize-space()='" + label + "']"
                        + " or @aria-label='" + label + "' or @title='" + label + "']"
                        + " | //*[@role='tab' and normalize-space()='" + label + "']"));
    }

    /**
     * Trả về visible input từ trạng thái hiện tại.
     * @param placeholder giá trị placeholder được truyền vào
     * @return kết quả visible input sau khi xử lý
     */
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

    /**
     * Trả về visible element từ trạng thái hiện tại.
     * @param locator locator xác định phần tử
     * @return kết quả visible element sau khi xử lý
     */
    private WebElement visibleElement(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    /**
     * Trả về visible content element từ trạng thái hiện tại.
     * @param locator locator xác định phần tử
     * @return kết quả visible content element sau khi xử lý
     */
    private WebElement visibleContentElement(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() > 300)
                .findFirst()
                .orElse(null);
    }

    /**
     * Kiểm tra điều kiện is selected.
     * @param element phần tử cần thao tác
     * @return kết quả is selected sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý main text trong luồng kiểm thử.
     * @return kết quả main text sau khi xử lý
     */
    private String mainText() {
        WebElement main = visibleElement(By.cssSelector("main, [role='main']"));
        return main == null ? "" : main.getText();
    }

    /**
     * Thực hiện xử lý page state trong luồng kiểm thử.
     * @param controlLabel giá trị control label được truyền vào
     * @return kết quả page state sau khi xử lý
     */
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
