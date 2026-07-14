package com.vuatho.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

public final class ElementActions {
    private final WebDriver driver;

    public ElementActions(WebDriver driver) {
        this.driver = driver;
    }

    public void click(WebElement element) {
        scrollToCenter(element);
        try {
            element.click();
        } catch (WebDriverException exception) {
            js().executeScript("arguments[0].click();", element);
        }
    }

    public boolean isVisible(By locator) {
        return visibleElements(locator).findAny().isPresent();
    }

    public Optional<WebElement> firstVisible(By locator) {
        return visibleElements(locator).findFirst();
    }

    public String textInTopRightHeader() {
        Object value = js().executeScript(
                "return Array.from(document.querySelectorAll('body *'))"
                        + ".filter(e=>{"
                        + " const r=e.getBoundingClientRect();"
                        + " const s=getComputedStyle(e);"
                        + " return r.width>0 && r.height>0 && r.top>=0 && r.top<120"
                        + "  && r.left>innerWidth*0.65 && s.visibility!=='hidden' && s.display!=='none';"
                        + "})"
                        + ".map(e=>e.innerText||e.textContent||'')"
                        + ".filter(Boolean)"
                        + ".join('\\n');");
        return String.valueOf(value);
    }

    public void scrollToCenter(WebElement element) {
        js().executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    @SuppressWarnings("null")
    private java.util.stream.Stream<WebElement> visibleElements(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return elements.stream().filter(element -> element.isDisplayed());
    }

    private JavascriptExecutor js() {
        return (JavascriptExecutor) driver;
    }
}
