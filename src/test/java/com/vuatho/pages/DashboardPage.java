package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import com.vuatho.components.SidebarComponent;
import com.vuatho.utils.ElementActions;
import com.vuatho.utils.OverlayCleaner;
import com.vuatho.utils.PageLoadSynchronizer;
import com.vuatho.utils.PageScroller;
import com.vuatho.utils.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public class DashboardPage {
    public static final List<String> EXPECTED_MENU_GROUPS = List.of(
            "Dashboard",
            "Hiệu Quả Nguồn Thợ & Chi Phí",
            "Tài chính",
            "Người Dùng",
            "Đối Tác - Thợ",
            "Bài Kiểm Tra",
            "Nghiệp Vụ",
            "Đơn Dịch Vụ",
            "Đồng Phục",
            "Giao Dịch",
            "Website",
            "App",
            "System",
            "Marketing");

    private static final By DASHBOARD_TEXT = By.xpath("//*[normalize-space()='Dashboard']");
    private static final By DASHBOARD_MENU = By.cssSelector("a[href='/vuatho/dashboard']");
    private static final By DASHBOARD_MENU_TEXT_FALLBACK = By.xpath(
            "//a[normalize-space(.)='Dashboard'] | //button[normalize-space(.)='Dashboard']"
                    + " | //*[@role='menuitem' and normalize-space(.)='Dashboard']");
    private static final By COMPANY_HEADER = By.xpath("//*[normalize-space()='Công ty Vua Thợ']");
    private static final By HOME_CONTENT = By.xpath("//*[normalize-space()='Sơ Đồ Tổ Chức']");
    private static final By DASHBOARD_CONTENT = By.xpath("//*[normalize-space()='Thống Kê Tổng Quan']");
    private static final By GOOGLE_LOGIN = By.xpath("//*[contains(normalize-space(.),'Google')]");
    private static final By LOADING_INDICATORS = By.cssSelector(
            "[role='progressbar'], .ant-spin-spinning, .ant-skeleton, .skeleton");
    private static final By OPEN_OVERLAYS = By.cssSelector(
            ".ant-picker-dropdown, [role='dialog'], .react-datepicker, [data-radix-popper-content-wrapper]");
    private static final List<String> SUMMARY_CARDS = List.of(
            "Đơn dịch vụ",
            "Số lượng người dùng",
            "Số lượng thợ",
            "Nghiệp vụ",
            "Ngành nghề",
            "Nền tảng Vua Thợ");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ElementActions actions;
    private final SidebarComponent sidebar;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = Waits.standard(driver);
        this.actions = new ElementActions(driver);
        this.sidebar = new SidebarComponent(driver);
    }

    public DashboardPage open() {
        driver.get(TestConfig.entryUrl());
        return this;
    }

    public boolean isLoaded() {
        try {
            wait.until(webDriver -> hasDashboardMarker());
            sidebar.ensureExpanded();
            return true;
        } catch (org.openqa.selenium.TimeoutException ignored) {
            return false;
        }
    }

    public boolean hasValidDashboardUrl() {
        String url = driver.getCurrentUrl().toLowerCase();
        return url.contains(TestConfig.baseHost().toLowerCase())
                && url.contains("/vuatho/dashboard")
                && !url.contains("accounts.google.com")
                && !url.contains("login");
    }

    public void openDashboardAndWaitForMetrics() {
        wait.until(webDriver -> isVisible(HOME_CONTENT) || isVisible(DASHBOARD_CONTENT));
        sidebar.ensureExpanded();
        WebElement dashboardMenu = wait.until(webDriver -> dashboardMenu());
        actions.click(dashboardMenu);

        waitForSummaryCards();
        PageScroller.slowlyToBottom(driver);
    }

    public List<String> clickSummaryCardsAndWaitForDestinations() {
        openDashboardAndWaitForSummaryCards();
        for (String card : SUMMARY_CARDS) {
            try {
                clickSummaryCardAndWait(card);
                exerciseChartFilters(card);
            } finally {
                OverlayCleaner.dismissBlockingOverlays(driver);
            }
            openDashboardAndWaitForSummaryCards();
        }
        return SUMMARY_CARDS;
    }

    public void openDashboardAndWaitForSummaryCards() {
        OverlayCleaner.dismissBlockingOverlays(driver);
        driver.get(dashboardUrl());
        wait.until(webDriver -> isVisible(DASHBOARD_CONTENT));
        sidebar.ensureExpanded();
        waitForSummaryCards();
    }

    private void waitForSummaryCards() {
        WebDriverWait metricsWait = Waits.longWait(driver);
        metricsWait.until(webDriver -> hasDashboardMarker());
        metricsWait.until(webDriver -> driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed));
        metricsWait.until(webDriver -> visibleMetricValues() > 0);
        metricsWait.until(webDriver -> SUMMARY_CARDS.stream().allMatch(this::summaryCardIsVisible));
    }

    private void clickSummaryCardAndWait(String label) {
        OverlayCleaner.dismissBlockingOverlays(driver);
        WebElement card = summaryCard(label);
        String previousUrl = driver.getCurrentUrl();
        String previousContent = mainContent().getText();

        actions.scrollToCenter(card);
        wait.until(webDriver -> summaryCard(label).isDisplayed());
        actions.click(summaryCard(label));

        PageLoadSynchronizer.waitForDataToSettle(driver);
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(webDriver ->
                !driver.getCurrentUrl().equals(previousUrl)
                        || !mainContent().getText().equals(previousContent));
        System.out.println("[DASHBOARD CARD LOADED] " + label + " -> " + driver.getCurrentUrl());
    }

    private void exerciseChartFilters(String cardLabel) {
        clickPeriodFilterIfPresent("Tuần", cardLabel);
        clickPeriodFilterIfPresent("Tháng", cardLabel);
        openDateRangeFilterIfPresent(cardLabel);
    }

    private void clickPeriodFilterIfPresent(String filter, String cardLabel) {
        WebElement control = visibleMainControl(filter);
        if (control == null) {
            return;
        }

        String previousState = chartState();
        actions.click(control);
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> !chartState().equals(previousState) || isSelected(visibleMainControl(filter)));
        System.out.println("[DASHBOARD FILTER LOADED] " + cardLabel + " | " + filter);
    }

    private void openDateRangeFilterIfPresent(String cardLabel) {
        WebElement dateRange = visibleDateRangeControl();
        if (dateRange == null) {
            return;
        }

        String previousState = chartState();
        actions.click(dateRange);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3)).until(webDriver -> selectableDateCell(0) != null);
        } catch (TimeoutException ignored) {
            // Some date controls render as native inputs or close immediately after focus.
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            return;
        }
        selectMonthAndYearInDatePicker();
        actions.click(selectableDateCell(0));
        actions.click(selectableDateCell(3));
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> !chartState().equals(previousState) || !overlayIsVisible());
        System.out.println("[DASHBOARD FILTER LOADED] " + cardLabel + " | Date range");
    }

    private void selectMonthAndYearInDatePicker() {
        boolean changedMonth = selectDatePickerDropdownOption(0);
        boolean changedYear = selectDatePickerDropdownOption(1);
        if (!changedMonth) {
            clickDatePickerNavigationIfPresent();
        }
        if (changedMonth || changedYear) {
            PageLoadSynchronizer.waitForDataToSettle(driver);
        }
    }

    public boolean areMetricsDisplayed() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(webDriver -> visibleMetricValues() > 0);
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public List<String> loadedMetrics() {
        return mainContent().findElements(By.xpath(
                        ".//*[not(*) and string-length(normalize-space()) > 0]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isBlank() && text.matches(".*\\d.*"))
                .distinct()
                .toList();
    }

    public boolean summaryCardHasNumericValue(String label) {
        try {
            WebElement card = summaryCard(label);
            return card.isDisplayed() && card.getText().matches("(?s).*\\d.*");
        } catch (NoSuchElementException | StaleElementReferenceException ignored) {
            return false;
        }
    }

    public boolean sectionIsVisible(String title) {
        return driver.findElements(By.xpath("//*[normalize-space()='" + title + "']"))
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    public void selectPeriod(int groupIndex, String label) {
        WebElement control = periodControl(groupIndex, label);
        if (control == null) {
            throw new NoSuchElementException("Missing period control group=" + groupIndex + ", label=" + label);
        }
        actions.click(control);
        wait.until(webDriver -> periodIsSelected(groupIndex, label));
    }

    public boolean periodIsSelected(int groupIndex, String label) {
        WebElement control = periodControl(groupIndex, label);
        if (control == null) {
            return false;
        }
        return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                "const b=arguments[0],c=(b.className||'').toString();"
                        + "return !!b.querySelector('.animate-pulse')"
                        + "||b.getAttribute('aria-pressed')==='true'"
                        + "||b.getAttribute('aria-selected')==='true'"
                        + "||/selected|active|bg-primary-blue/.test(c);",
                control));
    }

    private WebElement periodControl(int groupIndex, String label) {
        return (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const index=arguments[0],label=arguments[1];"
                        + "const labels=['Ngày','Tuần','Tháng','Quý','Năm'];"
                        + "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden';};"
                        + "const name=b=>(b.innerText||b.textContent||'').trim();"
                        + "const groups=[...document.querySelectorAll('main div,[role=main] div')]"
                        + ".filter(e=>{const bs=[...e.children].filter(x=>x.tagName==='BUTTON'&&visible(x));"
                        + "const names=bs.map(name);return labels.every(x=>names.includes(x));})"
                        + ".filter((e,i,a)=>!a.some((other,j)=>j<i&&other.contains(e)));"
                        + "const group=groups[index];if(!group)return null;"
                        + "return [...group.children].find(b=>b.tagName==='BUTTON'&&name(b)===label)||null;",
                groupIndex, label);
    }

    public List<String> missingMenuGroups() {
        String sidebarText = sidebar.text();
        return EXPECTED_MENU_GROUPS.stream()
                .filter(item -> !sidebarText.contains(item))
                .toList();
    }

    public boolean isLogoLoaded() {
        return sidebar.isLogoLoaded();
    }

    public double sidebarWidth() {
        return sidebar.width();
    }

    public void ensureSidebarExpanded() {
        sidebar.ensureExpanded();
    }

    public void collapseSidebar() {
        sidebar.collapse();
    }

    public void expandSidebar() {
        sidebar.expand();
    }

    public boolean isDashboardMenuActive() {
        return sidebar.isDashboardActive()
                || (hasValidDashboardUrl() && hasDashboardMarker() && isVisible(DASHBOARD_TEXT));
    }

    public boolean hasCompanyHeader() {
        return isVisible(COMPANY_HEADER) || hasCurrentUserAndEnvironment();
    }

    public boolean hasCurrentUserAndEnvironment() {
        String accountText = actions.textInTopRightHeader();
        if (accountText.contains("H\u1ea3i") && accountText.contains("DEV")) {
            return true;
        }
        if (isVisible(By.xpath("//*[contains(normalize-space(.),'H\u1ea3i')]"))
                && isVisible(By.xpath("//*[contains(normalize-space(.),'DEV')]"))) {
            return true;
        }
        return isVisible(By.xpath("//*[normalize-space()='Hải']"))
                && isVisible(By.xpath("//*[contains(normalize-space(.),'DEV')]"));
    }

    public void logout() {
        logoutButton().click();
        WebElement confirmButton = confirmLogoutButton();
        if (confirmButton != null) {
            actions.click(confirmButton);
        }
        wait.until(webDriver -> isLoginVisible() || !hasDashboardMarker());
    }

    public boolean isLoginVisible() {
        return isVisible(GOOGLE_LOGIN);
    }

    public boolean hasDashboardMarker() {
        return isVisible(DASHBOARD_CONTENT)
                || (isVisible(DASHBOARD_TEXT) && visibleMetricValues() > 0)
                || (isVisible(HOME_CONTENT) && isVisible(COMPANY_HEADER));
    }

    private WebElement logoutButton() {
        List<WebElement> namedButtons = driver.findElements(By.xpath(
                "//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout')"
                        + " or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout')"
                        + " or contains(@aria-label,'Đăng xuất') or contains(@title,'Đăng xuất')]"));
        if (!namedButtons.isEmpty()) {
            return namedButtons.stream().filter(WebElement::isDisplayed).findFirst().orElseThrow();
        }

        int pageWidth = ((Number) ((JavascriptExecutor) driver)
                .executeScript("return window.innerWidth;")).intValue();
        return driver.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getRect().getX() > pageWidth * 0.75)
                .filter(button -> button.getRect().getY() < 160)
                .max(Comparator.comparingInt(button -> button.getRect().getX()))
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nút logout."));
    }

    private WebElement confirmLogoutButton() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(webDriver -> driver.findElements(By.xpath(
                                    "//button[contains(normalize-space(.),'\u0110\u0103ng xu\u1ea5t')"
                                            + " or contains(translate(normalize-space(.),"
                                            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                            + "'logout')]"))
                            .stream()
                            .filter(WebElement::isDisplayed)
                            .filter(element -> element.getRect().getX() > 200)
                            .findFirst()
                            .orElse(null));
        } catch (TimeoutException ignored) {
            return null;
        }
    }

    private boolean isVisible(By locator) {
        return actions.isVisible(locator);
    }

    private long visibleMetricValues() {
        return loadedMetrics().size();
    }

    private WebElement mainContent() {
        return driver.findElements(By.cssSelector("main, [role='main']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseGet(() -> driver.findElement(By.tagName("body")));
    }

    private WebElement dashboardMenu() {
        WebElement byHref = driver.findElements(DASHBOARD_MENU).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
        if (byHref != null) {
            return byHref;
        }
        return driver.findElements(DASHBOARD_MENU_TEXT_FALLBACK).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    private WebElement visibleMainControl(String label) {
        WebElement periodControl = visiblePeriodControl(label);
        if (periodControl != null) {
            return periodControl;
        }
        return driver.findElements(By.xpath(
                        "//button[normalize-space(.)='" + label + "' or .//*[normalize-space()='" + label + "']]"
                                + " | //*[@role='tab' and normalize-space()='" + label + "']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() > 300)
                .findFirst()
                .orElse(null);
    }

    private WebElement visiblePeriodControl(String label) {
        return (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const label=arguments[0];"
                        + "const labels=['Ngày','Tuần','Tháng','Quý','Năm'];"
                        + "if(!labels.includes(label)) return null;"
                        + "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.x>300&&r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden';};"
                        + "const buttonLabel=b=>(b.innerText||b.textContent||'').trim();"
                        + "const containers=[...document.querySelectorAll('main div,[role=main] div')].filter(visible);"
                        + "for(const container of containers){"
                        + " const buttons=[...container.querySelectorAll('button[type=button]')].filter(visible);"
                        + " const texts=buttons.map(buttonLabel);"
                        + " if(labels.every(x=>texts.includes(x))){"
                        + "  return buttons.find(b=>buttonLabel(b)===label)||null;"
                        + " }"
                        + "}"
                        + "return null;",
                label);
    }

    private WebElement visibleDateRangeControl() {
        WebElement control = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const root=document.querySelector('main,[role=main]') || document.body;"
                        + "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.x>300&&r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden';};"
                        + "const pattern=/\\d{2}\\/\\d{2}\\/\\d{4}\\s*-\\s*\\d{2}\\/\\d{2}\\/\\d{4}/;"
                        + "const candidates=[...root.querySelectorAll('button,input,[role=button]')].filter(visible);"
                        + "return candidates.find(e=>pattern.test(e.innerText||e.value||e.getAttribute('aria-label')||''))||null;");
        return control;
    }

    private boolean overlayIsVisible() {
        return driver.findElements(OPEN_OVERLAYS).stream().anyMatch(WebElement::isDisplayed);
    }

    private boolean selectDatePickerDropdownOption(int dropdownIndex) {
        WebElement dropdown = datePickerDropdown(dropdownIndex);
        if (dropdown == null) {
            return false;
        }

        String before = datePickerText();
        actions.click(dropdown);
        WebElement option = datePickerDropdownOption(before);
        if (option == null) {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            return false;
        }
        actions.click(option);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(webDriver -> !datePickerText().equals(before));
            return true;
        } catch (TimeoutException ignored) {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            return false;
        }
    }

    private WebElement datePickerDropdown(int index) {
        return (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.x>250&&r.y>80&&r.width>0&&r.height>0&&s.display!=='none'"
                        + "&&s.visibility!=='hidden'&&s.pointerEvents!=='none';};"
                        + "const elements=[...document.querySelectorAll('button,[role=button],[aria-haspopup],"
                        + "[data-radix-collection-item],div,span')]"
                        + ".filter(e=>visible(e)&&/(Tháng|202\\d|203\\d)/.test(e.textContent.trim()));"
                        + "const clickable=e=>{let c=e;while(c&&c!==document.body){"
                        + "if(c.matches('button,[role=button],[aria-haspopup],[tabindex]')) return c;"
                        + "c=c.parentElement;}return e;};"
                        + "const unique=[];"
                        + "for(const e of elements.map(clickable)){const r=e.getBoundingClientRect();"
                        + "if(!unique.some(x=>{const xr=x.getBoundingClientRect();"
                        + "return Math.abs(xr.x-r.x)<3&&Math.abs(xr.y-r.y)<3;})) unique.push(e);}"
                        + "unique.sort((a,b)=>a.getBoundingClientRect().x-b.getBoundingClientRect().x);"
                        + "return unique[arguments[0]]||null;",
                index);
    }

    private WebElement datePickerDropdownOption(String previousText) {
        return (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const previous=arguments[0];"
                        + "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden'"
                        + "&&s.pointerEvents!=='none';};"
                        + "const options=[...document.querySelectorAll('[role=option],button,[role=button],"
                        + "[cmdk-item],div,span')]"
                        + ".filter(e=>{const t=e.textContent.trim();return visible(e)&&t&&/(Tháng|202\\d|203\\d|Thg|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)/.test(t)"
                        + "&&!previous.includes(t);});"
                        + "options.sort((a,b)=>a.getBoundingClientRect().y-b.getBoundingClientRect().y"
                        + "||a.getBoundingClientRect().x-b.getBoundingClientRect().x);"
                        + "return options[0]||null;",
                previousText);
    }

    private void clickDatePickerNavigationIfPresent() {
        WebElement next = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.x>250&&r.y>80&&r.width>0&&r.height>0&&s.display!=='none'"
                        + "&&s.visibility!=='hidden'&&s.pointerEvents!=='none';};"
                        + "const buttons=[...document.querySelectorAll('button,[role=button]')].filter(visible);"
                        + "buttons.sort((a,b)=>b.getBoundingClientRect().x-a.getBoundingClientRect().x);"
                        + "return buttons[0]||null;");
        if (next != null) {
            actions.click(next);
        }
    }

    private String datePickerText() {
        Object text = ((JavascriptExecutor) driver).executeScript(
                "const overlays=[...document.querySelectorAll('.ant-picker-dropdown,[role=dialog],"
                        + ".react-datepicker,[data-radix-popper-content-wrapper],body')];"
                        + "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden';};"
                        + "const overlay=overlays.find(visible)||document.body;"
                        + "return overlay.innerText||overlay.textContent||'';");
        return String.valueOf(text);
    }

    private WebElement selectableDateCell(int index) {
        return (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.x>250&&r.y>80&&r.width>0&&r.height>0&&s.display!=='none'"
                        + "&&s.visibility!=='hidden'&&s.pointerEvents!=='none'&&parseFloat(s.opacity||'1')>.35;};"
                        + "const disabled=e=>e.closest('[aria-disabled=true],.disabled,[disabled]')"
                        + "||e.getAttribute('aria-disabled')==='true'||e.disabled;"
                        + "const clickable=e=>{let c=e;while(c&&c!==document.body){"
                        + "if(c.matches('button,[role=button],[tabindex],td,div')) return c;"
                        + "c=c.parentElement;}return e;};"
                        + "const cells=[...document.querySelectorAll('button,[role=button],td,div,span')]"
                        + ".filter(e=>visible(e)&&/^\\d{1,2}$/.test(e.textContent.trim())&&!disabled(e))"
                        + ".map(clickable).filter(visible);"
                        + "const unique=[];"
                        + "for(const cell of cells){const r=cell.getBoundingClientRect();"
                        + "if(!unique.some(x=>{const xr=x.getBoundingClientRect();"
                        + "return Math.abs(xr.x-r.x)<3&&Math.abs(xr.y-r.y)<3;})) unique.push(cell);}"
                        + "unique.sort((a,b)=>a.getBoundingClientRect().y-b.getBoundingClientRect().y"
                        + "||a.getBoundingClientRect().x-b.getBoundingClientRect().x);"
                        + "return unique[arguments[0]]||unique.at(-1)||null;",
                index);
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
            String pressed = currentElement.getAttribute("aria-pressed");
            if (classes.contains("active") || classes.contains("selected")
                    || "active".equalsIgnoreCase(state)
                    || "true".equalsIgnoreCase(selected)
                    || "true".equalsIgnoreCase(pressed)) {
                return true;
            }
            currentElement = currentElement.findElements(By.xpath("..")).stream()
                    .findFirst().orElse(null);
        }
        return false;
    }

    private String chartState() {
        return driver.getCurrentUrl() + "|" + mainContent().getText().hashCode();
    }

    private boolean summaryCardIsVisible(String label) {
        try {
            return summaryCard(label).isDisplayed();
        } catch (NoSuchElementException ignored) {
            return false;
        }
    }

    private WebElement summaryCard(String label) {
        WebElement card = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const label=arguments[0];"
                        + "const root=document.querySelector('main,[role=main]') || document.body;"
                        + "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.width>0 && r.height>0 && s.display!=='none' && s.visibility!=='hidden';};"
                        + "const titledButton=[...root.querySelectorAll('button')].find(button=>"
                        + "visible(button)&&[...button.querySelectorAll('h4')].some(h=>h.textContent.trim()===label));"
                        + "if(titledButton) return titledButton;"
                        + "const text=[...root.querySelectorAll('*')].find(e=>visible(e) && e.textContent.trim()===label);"
                        + "if(!text) return null;"
                        + "let e=text;"
                        + "while(e && e!==root && e!==document.body){"
                        + " const r=e.getBoundingClientRect();"
                        + " if(r.width>=180 && r.height>=80 && r.left>0) return e;"
                        + " e=e.parentElement;"
                        + "}"
                        + "return text;",
                label);
        if (card == null) {
            throw new NoSuchElementException("Khong tim thay dashboard card: " + label);
        }
        return card;
    }

    private String dashboardUrl() {
        String baseUrl = TestConfig.baseUrl();
        if (baseUrl.contains("/vuatho/dashboard")) {
            return baseUrl;
        }
        String trimmedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return trimmedBaseUrl + "/vuatho/dashboard";
    }
}
