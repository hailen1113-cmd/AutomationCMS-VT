package com.vuatho.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput.ScrollOrigin;

import java.time.Duration;

public final class PageScroller {
    private static final int SCROLL_STEP_PIXELS = 350;
    private static final Duration STEP_DELAY = Duration.ofMillis(200);
    private static final Duration MAX_SCROLL_TIME = Duration.ofSeconds(45);

    private PageScroller() {
    }

    public static void slowlyToBottom(WebDriver driver) {
        JavascriptExecutor javascript = (JavascriptExecutor) driver;
        WebElement scrollContainer = findScrollContainer(javascript);
        long deadline = System.nanoTime() + MAX_SCROLL_TIME.toNanos();
        int stableBottomChecks = 0;
        int steps = 0;
        long totalHeight = scrollHeight(javascript, scrollContainer);
        long viewportHeight = clientHeight(javascript, scrollContainer);

        System.out.printf("[SCROLL] Starting slow scroll: viewport=%dpx, content=%dpx%n",
                viewportHeight, totalHeight);

        while (System.nanoTime() < deadline && stableBottomChecks < 3) {
            long positionBefore = scrollTop(javascript, scrollContainer);
            long heightBefore = scrollHeight(javascript, scrollContainer);

            new Actions(driver)
                    .scrollFromOrigin(ScrollOrigin.fromElement(scrollContainer),
                            0, SCROLL_STEP_PIXELS)
                    .perform();
            pause();
            steps++;

            long positionAfter = scrollTop(javascript, scrollContainer);
            long heightAfter = scrollHeight(javascript, scrollContainer);
            viewportHeight = clientHeight(javascript, scrollContainer);
            boolean reachedBottom = positionAfter + viewportHeight >= heightAfter - 2;
            boolean pageDidNotMove = positionAfter == positionBefore && heightAfter == heightBefore;

            stableBottomChecks = reachedBottom || pageDidNotMove ? stableBottomChecks + 1 : 0;
            if (steps % 5 == 0 || reachedBottom) {
                System.out.printf("[SCROLL] Step %d: %d/%dpx%n",
                        steps, positionAfter + viewportHeight, heightAfter);
            }
        }

        scrollLastContentIntoView(javascript);
        pause();

        System.out.printf("[SCROLL] Reached page bottom at %dpx%n",
                scrollTop(javascript, scrollContainer));
    }

    private static WebElement findScrollContainer(JavascriptExecutor javascript) {
        return (WebElement) javascript.executeScript(
                "const vw=window.innerWidth, vh=window.innerHeight;"
                        + "const all=[document.scrollingElement,...document.querySelectorAll('main,[role=main],main *,[role=main] *')];"
                        + "const candidates=[...new Set(all)].filter(e=>{"
                        + "if(!e || e.closest('aside,nav')) return false;"
                        + "const r=e.getBoundingClientRect(), s=getComputedStyle(e);"
                        + "const canScroll=e.scrollHeight>e.clientHeight+20;"
                        + "const large=r.width>=vw*.5 && (r.height>=vh*.5 || e===document.scrollingElement);"
                        + "return canScroll && large && (e===document.scrollingElement || /(auto|scroll)/.test(s.overflowY));});"
                        + "candidates.sort((a,b)=>(b.scrollHeight-b.clientHeight)-(a.scrollHeight-a.clientHeight));"
                        + "return candidates[0] || document.scrollingElement || document.documentElement;");
    }

    private static void scrollLastContentIntoView(JavascriptExecutor javascript) {
        javascript.executeScript(
                "const root=document.querySelector('main,[role=main]') || document.body;"
                        + "const visible=[...root.querySelectorAll('*')].filter(e=>{"
                        + "const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.width>0 && r.height>0 && s.display!=='none' && s.visibility!=='hidden';});"
                        + "const last=visible.sort((a,b)=>a.getBoundingClientRect().bottom-b.getBoundingClientRect().bottom).at(-1);"
                        + "if(last) last.scrollIntoView({behavior:'smooth',block:'end'});");
    }

    private static long scrollTop(JavascriptExecutor javascript, WebElement container) {
        return number(javascript.executeScript("return arguments[0].scrollTop;", container));
    }

    private static long scrollHeight(JavascriptExecutor javascript, WebElement container) {
        return number(javascript.executeScript("return arguments[0].scrollHeight;", container));
    }

    private static long clientHeight(JavascriptExecutor javascript, WebElement container) {
        return number(javascript.executeScript("return arguments[0].clientHeight;", container));
    }

    private static long number(Object value) {
        return ((Number) value).longValue();
    }

    private static void pause() {
        try {
            Thread.sleep(STEP_DELAY.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Scrolling was interrupted.", exception);
        }
    }
}
