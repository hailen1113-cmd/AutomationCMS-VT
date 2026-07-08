package com.vuatho.exploration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Map;

public class UiFeatureExplorer {
    private final WebDriver driver;

    public UiFeatureExplorer(WebDriver driver) {
        this.driver = driver;
    }

    @SuppressWarnings("unchecked")
    public List<UiControl> visibleControls() {
        List<Map<String, String>> controls = (List<Map<String, String>>) ((JavascriptExecutor) driver)
                .executeScript(
                        "const selector='button,a,input,select,textarea,[role=tab],[role=button],[role=combobox]';"
                                + "return [...document.querySelectorAll(selector)].filter(e=>{"
                                + "const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                                + "return r.width>0&&r.height>0&&s.visibility!=='hidden'&&s.display!=='none';})"
                                + ".map(e=>({tag:e.tagName.toLowerCase(),role:e.getAttribute('role')||'',"
                                + "type:e.getAttribute('type')||'',label:(e.innerText||e.getAttribute('aria-label')||"
                                + "e.getAttribute('placeholder')||e.getAttribute('title')||e.getAttribute('name')||'')"
                                + ".trim().replace(/\\s+/g,' ').slice(0,120)}))"
                                + ".filter(x=>x.label||x.tag==='input'||x.tag==='select'||x.tag==='textarea');");
        return controls.stream()
                .map(item -> new UiControl(item.get("tag"), item.get("role"),
                        item.get("label"), item.get("type")))
                .distinct()
                .toList();
    }

    public void printInventory(String pageName) {
        List<UiControl> controls = visibleControls();
        System.out.printf("%n[FEATURE INVENTORY] %s | %s | %d controls%n",
                pageName, driver.getCurrentUrl(), controls.size());
        controls.forEach(control -> System.out.println("  " + control));
    }
}
