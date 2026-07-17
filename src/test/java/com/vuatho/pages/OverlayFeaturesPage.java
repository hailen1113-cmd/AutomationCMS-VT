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

    /**
     * Khởi tạo OverlayFeaturesPage với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public OverlayFeaturesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.controls = new ReadOnlyFeaturesPage(driver);
    }

    /**
     * Mở  trong luồng kiểm thử.
     * @param controlLabel giá trị control label được truyền vào
     * @return kết quả open sau khi xử lý
     */
    public OverlayFeaturesPage open(String controlLabel) {
        controls.openControl(controlLabel);
        return this;
    }

    /**
     * Kiểm tra điều kiện has text.
     * @param text nội dung cần xử lý
     * @return kết quả has text sau khi xử lý
     */
    public boolean hasText(String text) {
        return wait.until(webDriver -> contentElements(By.xpath(
                "//*[normalize-space()='" + text + "' or contains(normalize-space(.),'" + text + "')]")) > 0);
    }

    /**
     * Kiểm tra điều kiện has button.
     * @param label giá trị label được truyền vào
     * @return kết quả has button sau khi xử lý
     */
    public boolean hasButton(String label) {
        return wait.until(webDriver -> contentElements(By.xpath(
                "//button[normalize-space(.)='" + label + "'"
                        + " or @aria-label='" + label + "' or @title='" + label + "']")) > 0);
    }

    /**
     * Kiểm tra điều kiện has input.
     * @param placeholder giá trị placeholder được truyền vào
     * @return kết quả has input sau khi xử lý
     */
    public boolean hasInput(String placeholder) {
        return wait.until(webDriver -> contentElements(By.xpath(
                "//input[contains(@placeholder,'" + placeholder + "')]")) > 0);
    }

    /**
     * Kiểm tra điều kiện has select containing.
     * @param optionText giá trị option text được truyền vào
     * @return kết quả has select containing sau khi xử lý
     */
    public boolean hasSelectContaining(String optionText) {
        return wait.until(webDriver -> contentElements(By.xpath(
                "//select[contains(normalize-space(.),'" + optionText + "')]")) > 0);
    }

    /**
     * Trả về visible radio count từ trạng thái hiện tại.
     * @return kết quả visible radio count sau khi xử lý
     */
    public long visibleRadioCount() {
        return driver.findElements(By.cssSelector("input[type='radio']")).stream()
                .filter(this::hasRenderedArea)
                .filter(element -> element.getRect().getX() > 300)
                .count();
    }

    /**
     * Thực hiện xử lý close trong luồng kiểm thử.
     */
    public void close() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    /**
     * Thực hiện xử lý content elements trong luồng kiểm thử.
     * @param locator locator xác định phần tử
     * @return kết quả content elements sau khi xử lý
     */
    private long contentElements(By locator) {
        return driver.findElements(locator).stream()
                .filter(this::hasRenderedArea)
                .filter(element -> element.getRect().getX() > 300)
                .count();
    }

    /**
     * Kiểm tra điều kiện has rendered area.
     * @param element phần tử cần thao tác
     * @return kết quả has rendered area sau khi xử lý
     */
    private boolean hasRenderedArea(WebElement element) {
        return element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0;
    }
}
