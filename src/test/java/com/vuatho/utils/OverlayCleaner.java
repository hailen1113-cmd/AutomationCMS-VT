package com.vuatho.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class OverlayCleaner {
    private static final By BODY = By.tagName("body");

    /**
     * Khởi tạo OverlayCleaner với các phụ thuộc cần thiết.
     */
    private OverlayCleaner() {
    }

    /**
     * Thực hiện xử lý dismiss blocking overlays trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public static void dismissBlockingOverlays(WebDriver driver) {
        if (driver == null || !documentIsReady(driver) || !blockingOverlayIsVisible(driver)) {
            return;
        }

        clickTopRightCloseButton(driver);
        sendEscape(driver);
        dispatchEscape(driver);
        waitUntilClear(driver);
    }

    /**
     * Kích hoạt top right close button trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    private static void clickTopRightCloseButton(WebDriver driver) {
        try {
            Object closeButton = ((JavascriptExecutor) driver).executeScript(
                    "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                            + "return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden'"
                            + "&&parseFloat(s.opacity||'1')>.2;};"
                            + "const text=e=>(e.innerText||e.textContent||e.getAttribute('aria-label')"
                            + "||e.getAttribute('title')||'').trim().toLowerCase();"
                            + "const candidates=[...document.querySelectorAll('button,[role=button],"
                            + "[aria-label],[title]')].filter(visible).filter(e=>{"
                            + " const t=text(e), r=e.getBoundingClientRect();"
                            + " return /^(x|×|close|dismiss|đóng|dong)$/.test(t)"
                            + " || t.includes('close') || t.includes('đóng') || t.includes('dong')"
                            + " || (r.top<260 && r.left>innerWidth*.75 && r.width<=80 && r.height<=80);"
                            + "});"
                            + "candidates.sort((a,b)=>b.getBoundingClientRect().left-a.getBoundingClientRect().left"
                            + "||a.getBoundingClientRect().top-b.getBoundingClientRect().top);"
                            + "return candidates[0]||null;");
            if (closeButton instanceof WebElement element) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }
        } catch (WebDriverException ignored) {
            // Best-effort cleanup before the next test action.
        }
    }

    /**
     * Thực hiện xử lý send escape trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    @SuppressWarnings("null")
    private static void sendEscape(WebDriver driver) {
        try {
            driver.findElement(BODY).sendKeys(Keys.ESCAPE);
            driver.findElement(BODY).sendKeys(Keys.ESCAPE);
        } catch (WebDriverException ignored) {
            // Some transient browser states do not allow key dispatch.
        }
    }

    /**
     * Thực hiện xử lý dispatch escape trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    private static void dispatchEscape(WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "for(const type of ['keydown','keyup']){"
                            + "document.dispatchEvent(new KeyboardEvent(type,{key:'Escape',code:'Escape',"
                            + "keyCode:27,which:27,bubbles:true}));}");
        } catch (WebDriverException ignored) {
            // JavaScript dispatch is only a fallback.
        }
    }

    /**
     * Chờ until clear trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    @SuppressWarnings("null")
    private static void waitUntilClear(WebDriver driver) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(webDriver -> !blockingOverlayIsVisible(webDriver));
        } catch (TimeoutException ignored) {
            // Leave the original click/wait to fail with context if the overlay is genuinely stuck.
        }
    }

    /**
     * Thực hiện xử lý blocking overlay is visible trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     * @return kết quả blocking overlay is visible sau khi xử lý
     */
    private static boolean blockingOverlayIsVisible(WebDriver driver) {
        try {
            Object visible = ((JavascriptExecutor) driver).executeScript(
                    "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                            + "return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden'"
                            + "&&s.pointerEvents!=='none'&&parseFloat(s.opacity||'1')>.2;};"
                            + "return [...document.querySelectorAll('[role=dialog],"
                            + ".ant-modal-root,.ant-modal-wrap,.ant-drawer,.ant-drawer-content-wrapper,"
                            + ".ant-picker-dropdown,.react-datepicker,[data-radix-popper-content-wrapper],"
                            + ".fixed.inset-0,[class*=fixed][class*=inset-0]')]"
                            + ".some(e=>visible(e)&&e.getBoundingClientRect().width>innerWidth*.45"
                            + "&&e.getBoundingClientRect().height>innerHeight*.45);");
            return Boolean.TRUE.equals(visible);
        } catch (WebDriverException ignored) {
            return false;
        }
    }

    /**
     * Thực hiện xử lý document is ready trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     * @return kết quả document is ready sau khi xử lý
     */
    private static boolean documentIsReady(WebDriver driver) {
        try {
            Object readyState = ((JavascriptExecutor) driver).executeScript("return document.readyState");
            return "complete".equals(readyState) || "interactive".equals(readyState);
        } catch (WebDriverException ignored) {
            return false;
        }
    }
}
