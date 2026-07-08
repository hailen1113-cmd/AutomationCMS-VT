package com.vuatho.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class OverlayFeaturesPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ReadOnlyFeaturesPage controls;

    public OverlayFeaturesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.controls = new ReadOnlyFeaturesPage(driver);
    }

    public OverlayFeaturesPage open(String controlLabel) {
        controls.openControl(controlLabel);
        return this;
    }

    public boolean hasText(String text) {
        return wait.until(webDriver -> contentElements(By.xpath(
                "//*[normalize-space()='" + text + "' or contains(normalize-space(.),'" + text + "')]")) > 0);
    }

    public boolean hasButton(String label) {
        return wait.until(webDriver -> contentElements(By.xpath(
                "//button[normalize-space(.)='" + label + "'"
                        + " or @aria-label='" + label + "' or @title='" + label + "']")) > 0);
    }

    public boolean hasInput(String placeholder) {
        return wait.until(webDriver -> contentElements(By.xpath(
                "//input[contains(@placeholder,'" + placeholder + "')]")) > 0);
    }

    public boolean hasSelectContaining(String optionText) {
        return wait.until(webDriver -> contentElements(By.xpath(
                "//select[contains(normalize-space(.),'" + optionText + "')]")) > 0);
    }

    public long visibleRadioCount() {
        return driver.findElements(By.cssSelector("input[type='radio']")).stream()
                .filter(this::hasRenderedArea)
                .filter(element -> element.getRect().getX() > 300)
                .count();
    }

    public void close() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    private long contentElements(By locator) {
        return driver.findElements(locator).stream()
                .filter(this::hasRenderedArea)
                .filter(element -> element.getRect().getX() > 300)
                .count();
    }

    private boolean hasRenderedArea(WebElement element) {
        return element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0;
    }
}
