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

    /**
     * Khởi tạo DashboardPage với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = Waits.standard(driver);
        this.actions = new ElementActions(driver);
        this.sidebar = new SidebarComponent(driver);
    }

    /**
     * Mở  trong luồng kiểm thử.
     * @return kết quả open sau khi xử lý
     */
    public DashboardPage open() {
        driver.get(TestConfig.entryUrl());
        return this;
    }

    /**
     * Kiểm tra điều kiện is loaded.
     * @return kết quả is loaded sau khi xử lý
     */
    public boolean isLoaded() {
        try {
            wait.until(webDriver -> hasDashboardMarker());
            sidebar.ensureExpanded();
            return true;
        } catch (org.openqa.selenium.TimeoutException ignored) {
            return false;
        }
    }

    /**
     * Kiểm tra điều kiện has valid dashboard url.
     * @return kết quả has valid dashboard url sau khi xử lý
     */
    public boolean hasValidDashboardUrl() {
        String url = driver.getCurrentUrl().toLowerCase();
        return url.contains(TestConfig.baseHost().toLowerCase())
                && url.contains("/vuatho/dashboard")
                && !url.contains("accounts.google.com")
                && !url.contains("login");
    }

    /**
     * Mở dashboard and wait for metrics trong luồng kiểm thử.
     */
    public void openDashboardAndWaitForMetrics() {
        wait.until(webDriver -> isVisible(HOME_CONTENT) || isVisible(DASHBOARD_CONTENT));
        sidebar.ensureExpanded();
        WebElement dashboardMenu = wait.until(webDriver -> dashboardMenu());
        actions.click(dashboardMenu);

        waitForSummaryCards();
        PageScroller.slowlyToBottom(driver);
    }

    /**
     * Kích hoạt summary cards and wait for destinations trong luồng kiểm thử.
     * @return kết quả click summary cards and wait for destinations sau khi xử lý
     */
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

    /**
     * Mở dashboard and wait for summary cards trong luồng kiểm thử.
     */
    public void openDashboardAndWaitForSummaryCards() {
        OverlayCleaner.dismissBlockingOverlays(driver);
        driver.get(dashboardUrl());
        wait.until(webDriver -> isVisible(DASHBOARD_CONTENT));
        sidebar.ensureExpanded();
        waitForSummaryCards();
    }

    /**
     * Chờ for summary cards trong luồng kiểm thử.
     */
    private void waitForSummaryCards() {
        WebDriverWait metricsWait = Waits.longWait(driver);
        metricsWait.until(webDriver -> hasDashboardMarker());
        metricsWait.until(webDriver -> driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed));
        metricsWait.until(webDriver -> visibleMetricValues() > 0);
        metricsWait.until(webDriver -> SUMMARY_CARDS.stream().allMatch(this::summaryCardIsVisible));
    }

    /**
     * Kích hoạt summary card and wait trong luồng kiểm thử.
     * @param label giá trị label được truyền vào
     */
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

    /**
     * Thực hiện xử lý exercise chart filters trong luồng kiểm thử.
     * @param cardLabel giá trị card label được truyền vào
     */
    private void exerciseChartFilters(String cardLabel) {
        clickPeriodFilterIfPresent("Tuần", cardLabel);
        clickPeriodFilterIfPresent("Tháng", cardLabel);
        openDateRangeFilterIfPresent(cardLabel);
    }

    /**
     * Kích hoạt period filter if present trong luồng kiểm thử.
     * @param filter giá trị filter được truyền vào
     * @param cardLabel giá trị card label được truyền vào
     */
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

    /**
     * Mở date range filter if present trong luồng kiểm thử.
     * @param cardLabel giá trị card label được truyền vào
     */
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

    /**
     * Kích hoạt month and year in date picker trong luồng kiểm thử.
     */
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

    /**
     * Thực hiện xử lý are metrics displayed trong luồng kiểm thử.
     * @return kết quả are metrics displayed sau khi xử lý
     */
    public boolean areMetricsDisplayed() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(webDriver -> visibleMetricValues() > 0);
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    /**
     * Trả về loaded metrics từ trạng thái hiện tại.
     * @return kết quả loaded metrics sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý summary card has numeric value trong luồng kiểm thử.
     * @param label giá trị label được truyền vào
     * @return kết quả summary card has numeric value sau khi xử lý
     */
    public boolean summaryCardHasNumericValue(String label) {
        try {
            WebElement card = summaryCard(label);
            return card.isDisplayed() && card.getText().matches("(?s).*\\d.*");
        } catch (NoSuchElementException | StaleElementReferenceException ignored) {
            return false;
        }
    }

    /**
     * Thực hiện xử lý section is visible trong luồng kiểm thử.
     * @param title giá trị title được truyền vào
     * @return kết quả section is visible sau khi xử lý
     */
    public boolean sectionIsVisible(String title) {
        return driver.findElements(By.xpath("//*[normalize-space()='" + title + "']"))
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    /**
     * Kích hoạt period trong luồng kiểm thử.
     * @param groupIndex giá trị group index được truyền vào
     * @param label giá trị label được truyền vào
     */
    public void selectPeriod(int groupIndex, String label) {
        WebElement control = periodControl(groupIndex, label);
        if (control == null) {
            throw new NoSuchElementException("Missing period control group=" + groupIndex + ", label=" + label);
        }
        actions.click(control);
        wait.until(webDriver -> periodIsSelected(groupIndex, label));
    }

    /**
     * Thực hiện xử lý period is selected trong luồng kiểm thử.
     * @param groupIndex giá trị group index được truyền vào
     * @param label giá trị label được truyền vào
     * @return kết quả period is selected sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý period control trong luồng kiểm thử.
     * @param groupIndex giá trị group index được truyền vào
     * @param label giá trị label được truyền vào
     * @return kết quả period control sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý missing menu groups trong luồng kiểm thử.
     * @return kết quả missing menu groups sau khi xử lý
     */
    public List<String> missingMenuGroups() {
        String sidebarText = sidebar.text();
        return EXPECTED_MENU_GROUPS.stream()
                .filter(item -> !sidebarText.contains(item))
                .toList();
    }

    /**
     * Kiểm tra điều kiện is logo loaded.
     * @return kết quả is logo loaded sau khi xử lý
     */
    public boolean isLogoLoaded() {
        return sidebar.isLogoLoaded();
    }

    /**
     * Thực hiện xử lý sidebar width trong luồng kiểm thử.
     * @return kết quả sidebar width sau khi xử lý
     */
    public double sidebarWidth() {
        return sidebar.width();
    }

    /**
     * Thực hiện xử lý ensure sidebar expanded trong luồng kiểm thử.
     */
    public void ensureSidebarExpanded() {
        sidebar.ensureExpanded();
    }

    /**
     * Thực hiện xử lý collapse sidebar trong luồng kiểm thử.
     */
    public void collapseSidebar() {
        sidebar.collapse();
    }

    /**
     * Thực hiện xử lý expand sidebar trong luồng kiểm thử.
     */
    public void expandSidebar() {
        sidebar.expand();
    }

    /**
     * Kiểm tra điều kiện is dashboard menu active.
     * @return kết quả is dashboard menu active sau khi xử lý
     */
    public boolean isDashboardMenuActive() {
        return sidebar.isDashboardActive()
                || (hasValidDashboardUrl() && hasDashboardMarker() && isVisible(DASHBOARD_TEXT));
    }

    /**
     * Kiểm tra điều kiện has company header.
     * @return kết quả has company header sau khi xử lý
     */
    public boolean hasCompanyHeader() {
        return isVisible(COMPANY_HEADER) || hasCurrentUserAndEnvironment();
    }

    /**
     * Kiểm tra điều kiện has current user and environment.
     * @return kết quả has current user and environment sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý logout trong luồng kiểm thử.
     */
    public void logout() {
        logoutButton().click();
        WebElement confirmButton = confirmLogoutButton();
        if (confirmButton != null) {
            actions.click(confirmButton);
        }
        wait.until(webDriver -> isLoginVisible() || !hasDashboardMarker());
    }

    /**
     * Kiểm tra điều kiện is login visible.
     * @return kết quả is login visible sau khi xử lý
     */
    public boolean isLoginVisible() {
        return isVisible(GOOGLE_LOGIN);
    }

    /**
     * Kiểm tra điều kiện has dashboard marker.
     * @return kết quả has dashboard marker sau khi xử lý
     */
    public boolean hasDashboardMarker() {
        return isVisible(DASHBOARD_CONTENT)
                || (isVisible(DASHBOARD_TEXT) && visibleMetricValues() > 0)
                || (isVisible(HOME_CONTENT) && isVisible(COMPANY_HEADER));
    }

    /**
     * Thực hiện xử lý logout button trong luồng kiểm thử.
     * @return kết quả logout button sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý confirm logout button trong luồng kiểm thử.
     * @return kết quả confirm logout button sau khi xử lý
     */
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

    /**
     * Kiểm tra điều kiện is visible.
     * @param locator locator xác định phần tử
     * @return kết quả is visible sau khi xử lý
     */
    private boolean isVisible(By locator) {
        return actions.isVisible(locator);
    }

    /**
     * Trả về visible metric values từ trạng thái hiện tại.
     * @return kết quả visible metric values sau khi xử lý
     */
    private long visibleMetricValues() {
        return loadedMetrics().size();
    }

    /**
     * Thực hiện xử lý main content trong luồng kiểm thử.
     * @return kết quả main content sau khi xử lý
     */
    private WebElement mainContent() {
        return driver.findElements(By.cssSelector("main, [role='main']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseGet(() -> driver.findElement(By.tagName("body")));
    }

    /**
     * Thực hiện xử lý dashboard menu trong luồng kiểm thử.
     * @return kết quả dashboard menu sau khi xử lý
     */
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

    /**
     * Trả về visible main control từ trạng thái hiện tại.
     * @param label giá trị label được truyền vào
     * @return kết quả visible main control sau khi xử lý
     */
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

    /**
     * Trả về visible period control từ trạng thái hiện tại.
     * @param label giá trị label được truyền vào
     * @return kết quả visible period control sau khi xử lý
     */
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

    /**
     * Trả về visible date range control từ trạng thái hiện tại.
     * @return kết quả visible date range control sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý overlay is visible trong luồng kiểm thử.
     * @return kết quả overlay is visible sau khi xử lý
     */
    private boolean overlayIsVisible() {
        return driver.findElements(OPEN_OVERLAYS).stream().anyMatch(WebElement::isDisplayed);
    }

    /**
     * Kích hoạt date picker dropdown option trong luồng kiểm thử.
     * @param dropdownIndex giá trị dropdown index được truyền vào
     * @return kết quả select date picker dropdown option sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý date picker dropdown trong luồng kiểm thử.
     * @param index giá trị index được truyền vào
     * @return kết quả date picker dropdown sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý date picker dropdown option trong luồng kiểm thử.
     * @param previousText giá trị previous text được truyền vào
     * @return kết quả date picker dropdown option sau khi xử lý
     */
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

    /**
     * Kích hoạt date picker navigation if present trong luồng kiểm thử.
     */
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

    /**
     * Thực hiện xử lý date picker text trong luồng kiểm thử.
     * @return kết quả date picker text sau khi xử lý
     */
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

    /**
     * Kích hoạt date cell trong luồng kiểm thử.
     * @param index giá trị index được truyền vào
     * @return kết quả selectable date cell sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý chart state trong luồng kiểm thử.
     * @return kết quả chart state sau khi xử lý
     */
    private String chartState() {
        return driver.getCurrentUrl() + "|" + mainContent().getText().hashCode();
    }

    /**
     * Thực hiện xử lý summary card is visible trong luồng kiểm thử.
     * @param label giá trị label được truyền vào
     * @return kết quả summary card is visible sau khi xử lý
     */
    private boolean summaryCardIsVisible(String label) {
        try {
            return summaryCard(label).isDisplayed();
        } catch (NoSuchElementException ignored) {
            return false;
        }
    }

    /**
     * Thực hiện xử lý summary card trong luồng kiểm thử.
     * @param label giá trị label được truyền vào
     * @return kết quả summary card sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý dashboard url trong luồng kiểm thử.
     * @return kết quả dashboard url sau khi xử lý
     */
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
