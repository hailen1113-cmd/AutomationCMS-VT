package com.vuatho.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

/**
 * Đóng gói click, nhập liệu và đọc trạng thái element với cơ chế chờ và xử lý lỗi thống nhất.
 */
public final class ElementActions {
    private final WebDriver driver;

    /**
     * Khởi tạo ElementActions với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public ElementActions(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Kích hoạt  trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     */
    public void click(WebElement element) {
        scrollToCenter(element);
        try {
            element.click();
        } catch (WebDriverException exception) {
            js().executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Kiểm tra điều kiện is visible.
     * @param locator locator xác định phần tử
     * @return kết quả is visible sau khi xử lý
     */
    public boolean isVisible(By locator) {
        return visibleElements(locator).findAny().isPresent();
    }

    /**
     * Trả về first visible từ trạng thái hiện tại.
     * @param locator locator xác định phần tử
     * @return kết quả first visible sau khi xử lý
     */
    public Optional<WebElement> firstVisible(By locator) {
        return visibleElements(locator).findFirst();
    }

    /**
     * Thực hiện xử lý text in top right header trong luồng kiểm thử.
     * @return kết quả text in top right header sau khi xử lý
     */
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

    /**
     * Cuộn to center trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     */
    public void scrollToCenter(WebElement element) {
        js().executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    /**
     * Trả về visible elements từ trạng thái hiện tại.
     * @param locator locator xác định phần tử
     * @return kết quả visible elements sau khi xử lý
     */
    @SuppressWarnings("null")
    private java.util.stream.Stream<WebElement> visibleElements(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return elements.stream().filter(element -> element.isDisplayed());
    }

    /**
     * Thực hiện xử lý js trong luồng kiểm thử.
     * @return kết quả js sau khi xử lý
     */
    private JavascriptExecutor js() {
        return (JavascriptExecutor) driver;
    }
}
