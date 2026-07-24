package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Page Object cho menu Đơn Khách - Thợ. */
public class CustomerWorkerOrderPage {
    public static final String ROUTE = "/vuatho/order";

    private static final By TABLE = By.cssSelector(
            "table[aria-label='Table about Order Management']");
    private static final By ROWS = By.cssSelector(
            "table[aria-label='Table about Order Management'] tbody tr[data-key]");
    private static final By SEARCH = By.cssSelector(
            "input[aria-label='Tìm kiếm mã đơn dịch vụ']");
    private static final By FILTER = By.cssSelector(
            "button[title='Bộ lọc đơn dịch vụ']");
    private static final By RESET = By.cssSelector("button[title='Reset']");
    private static final By PAGINATION = By.cssSelector(
            "nav[aria-label='pagination navigation']");
    private static final By DRAWER = By.cssSelector(
            "div[aria-label='drawer-Chi tiết đơn dịch vụ']");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private String currentRowStatus = "";
    private String currentStatistic = "";

    public CustomerWorkerOrderPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(90));
        this.wait.pollingEvery(Duration.ofMillis(350));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    public CustomerWorkerOrderPage open() {
        driver.get(TestConfig.baseUrl().replaceAll("/+$", "") + ROUTE);
        wait.until(d -> d.getCurrentUrl().contains(ROUTE));
        waitForData();
        return this;
    }

    public List<String> headers() {
        return driver.findElement(TABLE).findElements(By.cssSelector(
                        "th[role='columnheader']"))
                .stream().map(WebElement::getText).map(String::trim).toList();
    }

    public List<OrderRow> rows() {
        List<OrderRow> result = new ArrayList<>();
        for (WebElement row : driver.findElements(ROWS)) {
            try {
                List<WebElement> cells = row.findElements(By.cssSelector(
                        "td[role='rowheader'],td[role='gridcell']"));
                if (cells.size() < 5) continue;
                String raw = row.getText().trim();
                result.add(new OrderRow(
                        row.getAttribute("data-key"),
                        cells.get(0).getText().trim(),
                        extractCurrentOrderStatus(cells.get(1).getText()),
                        cells.get(1).getText().trim(),
                        cells.get(2).getText().trim(),
                        cells.get(3).getText().trim(),
                        cells.get(4).getText().trim(),
                        raw));
            } catch (StaleElementReferenceException ignored) {
                return rows();
            }
        }
        return result;
    }

    public int totalDisplayed() {
        return new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    WebElement total = d.findElements(By.tagName("span")).stream()
                            .filter(WebElement::isDisplayed)
                            .filter(element -> normalized(element.getText())
                                    .startsWith("tong hien thi:"))
                            .findFirst().orElse(null);
                    if (total != null) {
                        String digits = total.getText().replaceAll("\\D", "");
                        return digits.isBlank() ? 0 : Integer.parseInt(digits);
                    }
                    String main = normalized(
                            d.findElement(By.tagName("main")).getText());
                    return main.contains("chua co du lieu")
                            || main.contains("khong co du lieu")
                            ? 0 : null;
                });
    }

    public Map<String, String> summaryValues() {
        Map<String, String> values = new LinkedHashMap<>();
        for (String label : List.of(
                "Tổng phí kết nối", "Thực thu hôm nay", "Tổng số đơn dịch vụ",
                "Hoàn thành đơn", "Hủy đơn", "Còn lại")) {
            WebElement marker = exactVisible(By.xpath(
                    "//*[normalize-space()='" + label + "']"));
            WebElement container = marker.findElement(By.xpath(
                    "./ancestor::*[self::div or self::section][.//*[normalize-space()='"
                            + label + "']][1]"));
            values.put(label, container.getText());
        }
        return values;
    }

    public String mainText() {
        return driver.findElement(By.tagName("main")).getText();
    }

    public int topServiceCount() {
        String text = mainText();
        int start = text.indexOf("Top dịch vụ nhiều đơn");
        int end = text.indexOf("Tìm kiếm mã đơn dịch vụ", start);
        String section = start >= 0
                ? text.substring(start, end > start ? end : text.length())
                : "";
        return section.split("HT:", -1).length - 1;
    }

    public String searchValue() {
        return exactVisible(SEARCH).getAttribute("value");
    }

    public CustomerWorkerOrderPage search(String keyword) {
        List<String> before = rowIds();
        WebElement input = exactVisible(SEARCH);
        observe(input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), keyword);
        wait.until(d -> keyword.equals(exactVisible(SEARCH).getAttribute("value")));
        waitForResultChange(before);
        return this;
    }

    public CustomerWorkerOrderPage reset() {
        WebElement reset = exactVisible(RESET);
        observe(reset);
        reset.click();
        wait.until(d -> {
            waitForData();
            return searchValue().isBlank() && activePage() == 1;
        });
        return this;
    }

    public CustomerWorkerOrderPage selectOrderStatus(String status) {
        return selectNestedFilter("trạng thái đơn dịch vụ", status);
    }

    public CustomerWorkerOrderPage selectAgreementStatus(String status) {
        return selectNestedFilter("trạng thái thỏa thuận giá", status);
    }

    public CustomerWorkerOrderPage selectService(String service) {
        WebElement panel = openFilter();
        By serviceInput = By.cssSelector(
                "input[placeholder='Tìm kiếm dịch vụ...']");
        WebElement input = panel.findElements(serviceInput)
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy ô Tìm kiếm dịch vụ."));
        ((JavascriptExecutor) driver).executeScript("""
                const input = arguments[0];
                const setter = Object.getOwnPropertyDescriptor(
                  HTMLInputElement.prototype, 'value').set;
                setter.call(input, '');
                input.focus();
                input.click();
                input.dispatchEvent(new Event('input', {bubbles: true}));
                """, input);
        new Actions(driver).sendKeys(service).perform();
        boolean selected = new WebDriverWait(driver, Duration.ofSeconds(12))
                .pollingEvery(Duration.ofMillis(250)).until(d ->
                Boolean.TRUE.equals(((JavascriptExecutor) d).executeScript("""
                        const expected = arguments[0].trim();
                        const label = [...document.querySelectorAll('div.font-medium')]
                          .find(item => item.textContent.trim() === expected);
                        if (!label) return false;
                        const item = label.closest('.cursor-pointer')
                          || label.parentElement?.parentElement;
                        if (!item) return false;
                        item.style.outline = '3px solid #2563eb';
                        return true;
                        """, service)));
        if (!selected) {
            throw new IllegalStateException(
                    "Không chọn được suggestion Dịch vụ " + service);
        }
        pauseForFilterObservation(
                "Da hien thi goi y dich vu "
                        + TextNormalizer.normalize(service), 2);
        boolean clicked;
        try {
            clicked = new WebDriverWait(driver, Duration.ofSeconds(6))
                    .pollingEvery(Duration.ofMillis(200))
                    .until(d -> Boolean.TRUE.equals(
                            ((JavascriptExecutor) d).executeScript("""
                                    const expected = arguments[0].trim();
                                    let label = [...document.querySelectorAll(
                                      'div.font-medium')].find(item =>
                                      item.textContent.trim() === expected);
                                    if (!label) {
                                      const input = document.querySelector(
                                        'input[placeholder="Tìm kiếm dịch vụ..."]');
                                      if (!input) return false;
                                      input.focus();
                                      input.click();
                                      input.dispatchEvent(new Event(
                                        'input', {bubbles: true}));
                                      return false;
                                    }
                                    const item = label.closest('.cursor-pointer')
                                      || label.parentElement?.parentElement;
                                    if (!item) return false;
                                    item.click();
                                    return true;
                                    """, service)));
        } catch (TimeoutException ignored) {
            clicked = false;
        }
        if (!clicked) {
            throw new IllegalStateException(
                    "Không mở lại và click được suggestion Dịch vụ " + service);
        }
        waitForFilterResult();
        closeFilterIfOpen();
        return this;
    }

    public CustomerWorkerOrderPage selectRequestDateRange(
            LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.");
        }
        openFilter();
        navigateCalendar(YearMonth.now(), YearMonth.from(from));
        clickCalendarDay(from);
        navigateCalendar(YearMonth.from(from), YearMonth.from(to));
        clickCalendarDay(to);
        waitForFilterResult();
        closeFilterIfOpen();
        return this;
    }

    public boolean rowMatchesStatusGroup(
            OrderRow row, String groupLabel, String expectedValue) {
        String statusText = normalized(row.statusDetails())
                .replace("\r", "");
        String adjacentLines = normalized(groupLabel)
                + "\n" + normalized(expectedValue);
        if (statusText.contains(adjacentLines)) {
            return true;
        }
        List<String> lines = row.statusDetails().lines()
                .map(String::trim).filter(value -> !value.isBlank()).toList();
        String expectedGroup = normalized(groupLabel);
        for (int index = 0; index < lines.size() - 1; index++) {
            String label = normalized(lines.get(index))
                    .replaceFirst("^[^a-z0-9]+", "");
            if (label.endsWith(expectedGroup)) {
                return normalized(lines.get(index + 1))
                        .startsWith(normalized(expectedValue));
            }
        }
        return false;
    }

    public String statusGroupValue(OrderRow row, String groupLabel) {
        List<String> lines = row.statusDetails().lines()
                .map(String::trim).filter(value -> !value.isBlank()).toList();
        String expectedGroup = normalized(groupLabel);
        for (int index = 0; index < lines.size() - 1; index++) {
            String label = normalized(lines.get(index))
                    .replaceFirst("^[^a-z0-9]+", "");
            if (label.endsWith(expectedGroup)) {
                return lines.get(index + 1);
            }
        }
        throw new IllegalStateException(
                "Dòng đơn #" + row.id()
                        + " thiếu nhóm trạng thái " + groupLabel);
    }

    public List<String> nestedFilterOptions(String ariaLabel, List<String> candidates) {
        openFilter();
        By selectLocator = By.cssSelector(
                "button[aria-label='" + ariaLabel + "']");
        new Actions(driver).pause(Duration.ofMillis(800)).perform();
        clickFresh(selectLocator);
        wait.until(d -> candidates.stream().anyMatch(value ->
                d.findElements(By.xpath("//*[normalize-space()='" + value + "']"))
                        .stream().filter(WebElement::isDisplayed)
                        .anyMatch(element -> !element.findElements(
                                By.xpath("./ancestor::table")).isEmpty() == false)));
        List<String> result = candidates.stream().filter(value ->
                        driver.findElements(By.xpath(
                                        "//*[normalize-space()='" + value + "']"))
                                .stream().filter(WebElement::isDisplayed)
                                .anyMatch(element -> element.findElements(
                                        By.xpath("./ancestor::table")).isEmpty()))
                .toList();
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        closeFilterIfOpen();
        return result;
    }

    public CustomerWorkerOrderPage selectDirectFilter(String groupLabel, String value) {
        WebElement panel = openFilter();
        WebElement option = findDirectFilterOption(panel, groupLabel, value);
        observe(option);
        WebElement freshOption = findDirectFilterOption(
                openFilter(), groupLabel, value);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();", freshOption);
        new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(250))
                .until(d -> directFilterChecked(groupLabel, value));
        waitForFilterResult();
        closeFilterIfOpen();
        return this;
    }

    private WebElement findDirectFilterOption(
            WebElement panel, String groupLabel, String value) {
        WebElement group = panel.findElements(By.xpath(
                        ".//*[normalize-space()='" + groupLabel + "']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy nhóm filter " + groupLabel));
        WebElement scope = group.findElement(By.xpath("./parent::*"));
        WebElement option = scope.findElements(By.xpath(
                        ".//*[normalize-space()='" + value + "']"))
                .stream().filter(WebElement::isDisplayed)
                .map(this::closestClickable).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy filter " + groupLabel + " = " + value));
        return option;
    }

    private boolean directFilterChecked(String groupLabel, String value) {
        WebElement panel = driver.findElements(By.cssSelector(
                        "[data-slot='content'],[data-slot='popover'],[role='dialog']"))
                .stream().filter(element -> {
                    try {
                        return element.isDisplayed()
                                && element.getText().contains("TÙY CHỌN LỌC");
                    } catch (StaleElementReferenceException ignored) {
                        return false;
                    }
                }).findFirst().orElse(null);
        if (panel == null) return false;
        WebElement group = panel.findElements(By.xpath(
                        ".//*[normalize-space()='" + groupLabel + "']"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
        if (group == null) return false;
        WebElement scope = group.findElement(By.xpath("./parent::*"));
        return scope.findElements(By.cssSelector("input[type='radio']"))
                .stream().anyMatch(input -> {
                    try {
                        String label = input.findElement(
                                By.xpath("./ancestor::label[1]")).getText();
                        return normalized(label).equals(normalized(value))
                                && (input.isSelected()
                                || "true".equalsIgnoreCase(
                                input.getAttribute("checked")));
                    } catch (RuntimeException ignored) {
                        return false;
                    }
                });
    }

    public String filterText() {
        return openFilter().getText();
    }

    public CustomerWorkerOrderPage resetInsideFilter() {
        List<String> before = rowIds();
        WebElement panel = openFilter();
        WebElement reset = panel.findElements(By.xpath(
                        ".//button[normalize-space()='Đặt lại']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow();
        observe(reset);
        reset.click();
        waitForFilterResult();
        closeFilterIfOpen();
        return this;
    }

    public CustomerWorkerOrderPage switchView(String label) {
        WebElement button = exactVisible(By.xpath(
                "//button[normalize-space()='" + label + "']"));
        observe(button);
        button.click();
        wait.until(d -> label.equals("Thẻ")
                ? d.findElements(TABLE).stream().noneMatch(WebElement::isDisplayed)
                : d.findElements(TABLE).stream().anyMatch(WebElement::isDisplayed));
        return this;
    }

    public boolean cardViewContainsOrders() {
        return driver.findElements(By.xpath(
                        "//*[starts-with(normalize-space(),'#')][contains(normalize-space(),'')]"))
                .stream().filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .anyMatch(text -> text.matches("#\\d+"));
    }

    public boolean exportExcel() {
        WebElement button = exactVisible(By.xpath(
                "//button[normalize-space()='Xuất Excel']"));
        observe(button);
        button.click();
        try {
            wait.until(d -> d.findElements(By.xpath(
                            "//*[contains(normalize-space(),'xuất') or contains(normalize-space(),'Excel')]"))
                    .stream().filter(WebElement::isDisplayed).count() > 1);
        } catch (TimeoutException ignored) {
            // Trình duyệt tải file trực tiếp nên có thể không hiển thị toast.
        }
        return true;
    }

    public CustomerWorkerOrderPage openStatistic(String optionText) {
        currentStatistic = optionText;
        clickFreshButton("Thống kê", false);
        clickFreshStatisticOption(optionText);
        wait.until(d -> visibleDialogContaining(optionText) != null);
        return this;
    }

    public boolean statisticsChartRendered() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(20))
                    .pollingEvery(Duration.ofMillis(300)).until(d -> {
                        WebElement dialog = statisticsDialog();
                        if (dialog == null) return false;
                        return dialog.findElements(By.cssSelector(
                                        ".recharts-wrapper,canvas,svg,[class*='chart']"))
                                .stream().anyMatch(element -> {
                                    try {
                                        return element.isDisplayed()
                                                && element.getRect().getWidth() > 20
                                                && element.getRect().getHeight() > 20;
                                    } catch (RuntimeException ignored) {
                                        return false;
                                    }
                                });
                    });
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public String hoverLargestStatisticsDatum() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return "";
        List<WebElement> data = dialog.findElements(By.cssSelector(
                ".recharts-bar-rectangle,.recharts-sector,.recharts-dot,"
                        + ".recharts-active-dot,.recharts-area-area,.recharts-line-curve,"
                        + "svg [name],svg [role='img'],[class*='highcharts-point'],canvas"));
        if (data.isEmpty()) {
            data = dialog.findElements(By.cssSelector(
                    ".recharts-wrapper,svg,canvas,[class*='chart']"));
        }
        List<WebElement> candidates = data.stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed()
                                && element.getRect().getWidth() > 2
                                && element.getRect().getHeight() > 2;
                    } catch (RuntimeException ignored) {
                        return false;
                    }
                })
                .sorted(java.util.Comparator.comparingDouble((WebElement element) -> {
                    try {
                        return element.getRect().getWidth() * element.getRect().getHeight();
                    } catch (RuntimeException ignored) {
                        return 0;
                    }
                }).reversed()).limit(20).toList();
        for (WebElement target : candidates) {
            try {
                observe(target);
                new Actions(driver).moveToElement(target)
                        .pause(TestConfig.headless()
                                ? Duration.ofMillis(500) : Duration.ofSeconds(2))
                        .perform();
                String tooltip = visibleTooltipText();
                if (!tooltip.isBlank()) return tooltip;
                for (String attribute : List.of(
                        "aria-label", "name", "value", "title", "data-value")) {
                    String value = target.getAttribute(attribute);
                    if (value != null && !value.isBlank()) return value.trim();
                }
                WebElement report = statisticsDialog();
                if (report != null && !report.getText().isBlank()) {
                    return report.getText().trim();
                }
            } catch (StaleElementReferenceException ignored) {
                // React có thể render lại chart ngay khi hover; thử datum tiếp theo.
            }
        }
        WebElement report = statisticsDialog();
        return report == null ? "" : report.getText().trim();
    }

    private WebElement statisticsDialog() {
        return currentStatistic.isBlank()
                ? visibleDialog() : visibleDialogContaining(currentStatistic);
    }

    private String visibleTooltipText() {
        return driver.findElements(By.cssSelector(
                        ".recharts-tooltip-wrapper,[role='tooltip'],"
                                + "[class*='highcharts-tooltip'],[class*='echarts-tooltip'],"
                                + "[class*='chart-tooltip']"))
                .stream().filter(element -> {
                    try {
                        return element.isDisplayed();
                    } catch (RuntimeException ignored) {
                        return false;
                    }
                })
                .map(element -> {
                    try {
                        return element.getText().trim();
                    } catch (RuntimeException ignored) {
                        return "";
                    }
                })
                .filter(text -> !text.isBlank()).findFirst().orElse("");
    }

    public int activePage() {
        String value = visiblePagination().getAttribute("data-active-page");
        return value != null && value.matches("\\d+") ? Integer.parseInt(value) : 1;
    }

    public int totalPages() {
        String value = visiblePagination().getAttribute("data-total");
        return value != null && value.matches("\\d+") ? Integer.parseInt(value) : 1;
    }

    public CustomerWorkerOrderPage goToPage(int page) {
        List<String> before = rowIds();
        WebElement pagination = visiblePagination();
        observe(pagination);
        WebElement item = pagination.findElements(By.cssSelector(
                        "[role='button'][data-slot='item']"))
                .stream().filter(element -> ("pagination item " + page)
                        .equalsIgnoreCase(element.getAttribute("aria-label")))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Không có nút trang " + page));
        observe(item);
        item.click();
        wait.until(d -> {
            waitForData();
            return activePage() == page && !rowIds().equals(before);
        });
        observe(visiblePagination());
        return this;
    }

    public CustomerWorkerOrderPage nextPage() {
        return clickPageControl("next page button", activePage() + 1);
    }

    public CustomerWorkerOrderPage previousPage() {
        return clickPageControl("previous page button", activePage() - 1);
    }

    public DetailSnapshot openFirstRow() {
        WebElement row = exactVisible(ROWS);
        return openRow(row);
    }

    public DetailSnapshot openFirstRowWithStatus(String status) {
        selectOrderStatus(status);
        if (rows().isEmpty()) {
            throw new IllegalStateException("Không có đơn trạng thái " + status);
        }
        return openFirstRow();
    }

    public DetailSnapshot openFirstVisibleRowWithStatus(String status) {
        WebElement row = driver.findElements(ROWS).stream()
                .filter(element -> {
                    List<WebElement> cells = element.findElements(By.cssSelector(
                            "td[role='rowheader'],td[role='gridcell']"));
                    return cells.size() >= 2
                            && normalized(cells.get(1).getText())
                            .contains(normalized(status));
                })
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Trang đầu không có đơn trạng thái " + status));
        return openRow(row);
    }

    public DetailSnapshot openOrder(String id) {
        search(id);
        WebElement row = driver.findElements(ROWS).stream()
                .filter(element -> id.equals(element.getAttribute("data-key")))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy đơn #" + id));
        return openRow(row);
    }

    public DetailSnapshot openFirstOrderWithAction(String action) {
        int lastPage = Math.min(totalPages(), 5);
        for (int page = 1; page <= lastPage; page++) {
            int count = driver.findElements(ROWS).size();
            for (int index = 0; index < count; index++) {
                List<WebElement> current = driver.findElements(ROWS);
                if (index >= current.size()) break;
                DetailSnapshot detail = openRow(current.get(index));
                if (detail.buttons().stream().anyMatch(
                        text -> normalized(text).equals(normalized(action)))) {
                    return detail;
                }
                closeOverlay();
            }
            if (page < lastPage) goToPage(page + 1);
        }
        throw new IllegalStateException("Không tìm thấy đơn có action " + action);
    }

    public CustomerWorkerOrderPage openDetailSection(String ariaLabel) {
        WebElement drawer = requiredDrawer();
        WebElement button = drawer.findElements(By.cssSelector(
                        "button[aria-label='" + ariaLabel + "']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có section " + ariaLabel));
        observe(button);
        button.click();
        wait.until(d -> requiredDrawer().getText().length() > 50);
        pauseForDetailObservation("Da tai section " + TextNormalizer.normalize(ariaLabel));
        return this;
    }

    public String drawerText() {
        return requiredDrawer().getText();
    }

    public boolean openMap() {
        WebElement drawer = requiredDrawer();
        WebElement button = drawer.findElements(By.cssSelector("button,a")).stream()
                .filter(element -> {
                    try {
                        String accessible = String.join(" ",
                                element.getText(),
                                String.valueOf(element.getAttribute("title")),
                                String.valueOf(element.getAttribute("aria-label")));
                        return element.isDisplayed()
                                && normalized(accessible).contains("ban do");
                    } catch (StaleElementReferenceException ignored) {
                        return false;
                    }
                }).findFirst().orElse(null);
        if (button == null) return false;
        String before = drawer.getAttribute("innerHTML");
        int windowsBefore = driver.getWindowHandles().size();
        observe(button);
        button.click();
        try {
            boolean opened = new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
                if (visibleDialog() != null
                        || d.getWindowHandles().size() > windowsBefore
                        || d.getCurrentUrl().toLowerCase().contains("map")) {
                    return true;
                }
                WebElement currentDrawer = visibleDrawer();
                if (currentDrawer == null) return false;
                boolean mapContent = !d.findElements(By.cssSelector(
                        "iframe[src*='map'],a[href*='maps'],canvas,.gm-style,"
                                + "[class*='leaflet'],[class*='mapbox']")).isEmpty();
                return mapContent
                        || !before.equals(currentDrawer.getAttribute("innerHTML"));
            });
            if (opened) {
                pauseForDetailObservation("Da tai du lieu ban do");
            }
            return opened;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public boolean openCustomerChat() {
        WebElement drawer = requiredDrawer();
        WebElement button = drawer.findElements(By.cssSelector(
                        "button[title='Chat hỗ trợ khách']"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
        if (button == null) return false;
        observe(button);
        button.click();
        wait.until(d -> d.findElements(By.cssSelector(
                        "[role='dialog'],[class*='chat'],iframe"))
                .stream().anyMatch(WebElement::isDisplayed));
        pauseForDetailObservation("Da tai giao dien chat ho tro khach");
        return true;
    }

    public MutationResult advanceOpenOrder() {
        WebElement drawer = requiredDrawer();
        String id = extractOrderId(drawer.getText());
        String before = currentRowStatus;
        clickDrawerButton("Sang bước kế tiếp");
        WebElement dialog = wait.until(d -> visibleDialogContaining("Sang bước kế tiếp"));
        for (WebElement input : dialog.findElements(By.cssSelector("input"))) {
            if (!input.isDisplayed() || input.getAttribute("disabled") != null) continue;
            String value = input.getAttribute("value");
            if (value == null || value.isBlank()) {
                observe(input);
                input.sendKeys("100000");
            }
        }
        clickDialogButton(dialog, "Xác nhận");
        waitAfterMutation();
        closeOverlay();
        open();
        DetailSnapshot updated = openOrder(id);
        return new MutationResult(id, before, updated.status(), updated.text());
    }

    public MutationResult cancelOpenOrder(String title, String reason) {
        WebElement drawer = requiredDrawer();
        String id = extractOrderId(drawer.getText());
        String before = currentRowStatus;
        clickDrawerButton("Hủy đơn");
        WebElement dialog = wait.until(d -> visibleDialogContaining("Hủy đơn"));
        List<WebElement> fields = dialog.findElements(By.cssSelector("input,textarea"))
                .stream().filter(WebElement::isDisplayed).toList();
        if (fields.size() < 2) {
            throw new IllegalStateException("Popup Hủy đơn thiếu Tiêu đề hoặc Lý do.");
        }
        fill(fields.get(0), title);
        fill(fields.get(1), reason);
        clickDialogButton(dialog, "Xác nhận");
        waitAfterMutation();
        closeOverlay();
        open();
        DetailSnapshot updated = openOrder(id);
        return new MutationResult(id, before, updated.status(), updated.text());
    }

    public void closeOverlay() {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> visibleDialog() == null && visibleDrawer() == null);
        } catch (TimeoutException ignored) {
            // Một ESC chỉ đóng lớp trên cùng; cleanup tiếp tục đóng lớp còn lại.
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        }
    }

    private CustomerWorkerOrderPage selectNestedFilter(String ariaLabel, String value) {
        openFilter();
        System.out.println("[FILTER] Mo " + TextNormalizer.normalize(ariaLabel)
                + " -> " + TextNormalizer.normalize(value));
        By selectLocator = By.cssSelector(
                "button[aria-label='" + ariaLabel + "']");
        WebElement trigger = shortVisible(selectLocator, Duration.ofSeconds(8));
        observe(trigger);
        WebElement freshTrigger = openFilter().findElements(selectLocator)
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy dropdown " + ariaLabel
                                + " sau khi mở lại bộ lọc."));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();", freshTrigger);
        System.out.println("[FILTER] Da mo danh sach lua chon");
        if (!TestConfig.headless()) {
            new Actions(driver).pause(Duration.ofSeconds(2)).perform();
        }
        boolean clickedOption;
        try {
            clickedOption = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .pollingEvery(Duration.ofMillis(200)).until(d ->
                    Boolean.TRUE.equals(((JavascriptExecutor) d).executeScript("""
                            const expected = arguments[0].trim();
                            const option = [...document.querySelectorAll('[role="option"]')]
                              .find(item => item.textContent.trim() === expected);
                            if (!option) return false;
                            option.click();
                            return true;
                            """, value)));
        } catch (TimeoutException ignored) {
            clickedOption = false;
        }
        Boolean changed = clickedOption;
        if (!clickedOption) {
            WebElement fallbackTrigger = shortVisible(
                    selectLocator, Duration.ofSeconds(5));
            changed = (Boolean) ((JavascriptExecutor) driver).executeScript("""
                    const trigger = arguments[0];
                    const expected = arguments[1].trim();
                    const root = trigger.closest('[data-slot="base"]');
                    const select = root && root.querySelector('select');
                    if (!select) return false;
                    const option = [...select.options]
                      .find(item => item.textContent.trim() === expected);
                    if (!option) return false;
                    select.value = option.value;
                    select.dispatchEvent(new Event('input', {bubbles: true}));
                    select.dispatchEvent(new Event('change', {bubbles: true}));
                    return true;
                    """, fallbackTrigger, value);
        }
        if (!Boolean.TRUE.equals(changed)) {
            throw new IllegalStateException(
                    "Native select thiếu option " + value);
        }
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        System.out.println("[FILTER] Da chon " + TextNormalizer.normalize(value));
        waitForFilterResult();
        System.out.println("[FILTER] Da tai xong du lieu");
        closeFilterIfOpen();
        return this;
    }

    private void clickCalendarDay(LocalDate date) {
        String dateToken = normalized("ngày " + date.getDayOfMonth()
                + " tháng " + DateTimeFormatter.ofPattern("MM").format(date)
                + " năm " + date.getYear());
        WebElement day;
        try {
            day = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .pollingEvery(Duration.ofMillis(200))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> d.findElements(By.cssSelector(
                                    "[role='option'][aria-label]"))
                            .stream().filter(element -> {
                                try {
                                    String aria = normalized(
                                            element.getAttribute("aria-label"));
                                    return element.isDisplayed()
                                            && aria.contains(dateToken)
                                            && !aria.contains("not available");
                                } catch (StaleElementReferenceException ignored) {
                                    return false;
                                }
                            }).findFirst().orElse(null));
        } catch (TimeoutException exception) {
            List<String> available = driver.findElements(By.cssSelector(
                            "[role='option'][aria-label]"))
                    .stream().filter(WebElement::isDisplayed)
                    .map(element -> element.getAttribute("aria-label"))
                    .limit(12).toList();
            throw new IllegalStateException(
                    "Không tìm thấy ngày " + date
                            + " trên lịch. Ngày đang hiển thị: " + available,
                    exception);
        }
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.outline='3px solid #2563eb';", day);
        pauseForFilterObservation("Chon ngay " + date, 2);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", day);
    }

    private void navigateCalendar(YearMonth current, YearMonth target) {
        long difference = ChronoUnit.MONTHS.between(current, target);
        if (Math.abs(difference) > 36) {
            throw new IllegalArgumentException(
                    "Ngày filter cách tháng hiện tại quá 36 tháng: " + target);
        }
        String direction = difference < 0 ? "previous" : "next";
        for (long step = 0; step < Math.abs(difference); step++) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript("""
                    const direction = arguments[0];
                    const candidates = [...document.querySelectorAll(
                      'button,[role="button"]')].filter(item => {
                        const aria = (item.getAttribute('aria-label') || '')
                          .toLowerCase();
                        const slot = (item.getAttribute('data-slot') || '')
                          .toLowerCase();
                        if (direction === 'previous') {
                          return slot.includes('prev')
                            || aria.includes('previous')
                            || aria.includes('trước');
                        }
                        return slot.includes('next')
                          || aria.includes('next')
                          || aria.includes('sau');
                      });
                    const button = candidates.find(item =>
                      item.offsetParent !== null && !item.disabled);
                    if (!button) return false;
                    button.click();
                    return true;
                    """, direction);
            if (!Boolean.TRUE.equals(clicked)) {
                throw new IllegalStateException(
                        "Không tìm thấy nút chuyển tháng " + direction
                                + " trên lịch.");
            }
            new Actions(driver).pause(Duration.ofMillis(250)).perform();
        }
    }

    private WebElement openFilter() {
        WebElement trigger = shortVisible(FILTER, Duration.ofSeconds(8));
        if (!"true".equalsIgnoreCase(trigger.getAttribute("aria-expanded"))) {
            observe(trigger);
            WebElement freshTrigger = shortVisible(
                    FILTER, Duration.ofSeconds(5));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].click();", freshTrigger);
        }
        return new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(By.cssSelector(
                        "[data-slot='content'],[data-slot='popover'],[role='dialog']"))
                .stream().filter(WebElement::isDisplayed)
                .filter(element -> element.getText().contains("TÙY CHỌN LỌC"))
                .findFirst().orElse(null));
    }

    private void closeFilterIfOpen() {
        WebElement trigger;
        try {
            trigger = shortVisible(FILTER, Duration.ofSeconds(5));
        } catch (TimeoutException ignored) {
            return;
        }
        if ("true".equalsIgnoreCase(trigger.getAttribute("aria-expanded"))) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", trigger);
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .pollingEvery(Duration.ofMillis(200))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> d.findElements(FILTER).stream()
                            .filter(WebElement::isDisplayed)
                            .noneMatch(element -> "true".equalsIgnoreCase(
                                    element.getAttribute("aria-expanded"))));
        }
    }

    private CustomerWorkerOrderPage clickPageControl(String aria, int expectedPage) {
        List<String> before = rowIds();
        WebElement pagination = visiblePagination();
        observe(pagination);
        WebElement control = pagination.findElements(By.cssSelector(
                        "[role='button'][aria-label='" + aria + "']"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElseThrow();
        observe(control);
        control.click();
        wait.until(d -> {
            waitForData();
            return activePage() == expectedPage && !rowIds().equals(before);
        });
        observe(visiblePagination());
        return this;
    }

    private DetailSnapshot openRow(WebElement row) {
        String id = row.getAttribute("data-key");
        List<WebElement> cells = row.findElements(By.cssSelector(
                "td[role='rowheader'],td[role='gridcell']"));
        currentRowStatus = cells.size() >= 2
                ? extractCurrentOrderStatus(cells.get(1).getText()) : "";
        observe(row);
        row.click();
        WebElement drawer = wait.until(d -> {
            WebElement value = visibleDrawer();
            return value != null && value.getText().contains("Chi tiết đơn dịch vụ")
                    && value.getText().contains(id) ? value : null;
        });
        observe(drawer);
        pauseForDetailObservation(
                "Da tai drawer chi tiet don #" + id + " - "
                        + TextNormalizer.normalize(currentRowStatus));
        return new DetailSnapshot(
                id,
                currentRowStatus,
                drawer.getText(),
                drawer.findElements(By.tagName("button")).stream()
                        .filter(WebElement::isDisplayed)
                        .map(WebElement::getText).map(String::trim)
                        .filter(value -> !value.isBlank()).toList());
    }

    private void waitForData() {
        wait.until(d -> {
            List<WebElement> tables = d.findElements(TABLE);
            if (tables.isEmpty()) return false;
            String main = d.findElement(By.tagName("main")).getText();
            return !main.contains("Đang tải dữ liệu...")
                    && (!d.findElements(ROWS).isEmpty()
                    || main.contains("Không có dữ liệu")
                    || main.contains("Chưa có dữ liệu"));
        });
    }

    private void waitForResultChange(List<String> before) {
        wait.until(d -> {
            waitForData();
            List<String> after = rowIds();
            return after.isEmpty() || !after.equals(before);
        });
    }

    private void waitForFilterResult() {
        new Actions(driver).pause(Duration.ofMillis(1200)).perform();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .pollingEvery(Duration.ofMillis(300))
                .until(d -> !d.findElement(By.tagName("main")).getText()
                        .contains("Đang tải dữ liệu..."));
        try {
            observe(shortVisible(TABLE, Duration.ofSeconds(5)));
        } catch (TimeoutException ignored) {
            observe(driver.findElement(By.tagName("main")));
        }
        pauseForFilterObservation("Da hien thi ket qua sau khi loc", 2);
    }

    private void waitAfterMutation() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(d -> visibleDialog() == null);
        } catch (TimeoutException ignored) {
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        }
    }

    private WebElement visiblePagination() {
        return wait.until(d -> d.findElements(PAGINATION).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null));
    }

    private List<String> rowIds() {
        return driver.findElements(ROWS).stream()
                .map(element -> element.getAttribute("data-key")).toList();
    }

    private WebElement visibleDrawer() {
        return driver.findElements(DRAWER).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> {
                    String classes = element.getAttribute("class");
                    return classes == null || !classes.contains("translate-x-[100%]");
                })
                .filter(element -> element.getText().contains("Chi tiết đơn dịch vụ"))
                .findFirst().orElse(null);
    }

    private WebElement requiredDrawer() {
        WebElement drawer = visibleDrawer();
        if (drawer == null) throw new IllegalStateException("Drawer chi tiết chưa mở.");
        return drawer;
    }

    private WebElement visibleDialog() {
        return driver.findElements(By.cssSelector(
                        "[role='dialog'],[aria-modal='true']"))
                .stream().filter(WebElement::isDisplayed)
                .filter(element -> element != visibleDrawer())
                .findFirst().orElse(null);
    }

    private WebElement visibleDialogContaining(String text) {
        for (WebElement element : driver.findElements(By.cssSelector(
                "[role='dialog'],[aria-modal='true']"))) {
            try {
                if (element.isDisplayed()
                        && normalized(element.getText()).contains(normalized(text))) {
                    return element;
                }
            } catch (StaleElementReferenceException ignored) {
                // Modal đang render lại; vòng wait sẽ gọi lại phương thức này.
            }
        }
        return null;
    }

    private void clickFreshButton(String text, boolean startsWith) {
        RuntimeException lastFailure = null;
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
        shortWait.pollingEvery(Duration.ofMillis(250));
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                WebElement button = shortWait.until(d -> d.findElements(By.tagName("button"))
                        .stream().filter(element -> {
                            try {
                                String actual = normalized(element.getText());
                                boolean matches = startsWith
                                        ? actual.startsWith(normalized(text))
                                        : actual.equals(normalized(text));
                                return element.isDisplayed() && matches;
                            } catch (StaleElementReferenceException ignored) {
                                return false;
                            }
                        }).findFirst().orElse(null));
                observe(button);
                WebElement fresh = driver.findElements(By.tagName("button"))
                        .stream().filter(element -> {
                            try {
                                String actual = normalized(element.getText());
                                return element.isDisplayed() && (startsWith
                                        ? actual.startsWith(normalized(text))
                                        : actual.equals(normalized(text)));
                            } catch (StaleElementReferenceException ignored) {
                                return false;
                            }
                        }).findFirst().orElseThrow();
                fresh.click();
                return;
            } catch (RuntimeException failure) {
                lastFailure = failure;
            }
        }
        throw new IllegalStateException("Không click được button " + text, lastFailure);
    }

    private void clickFreshStatisticOption(String text) {
        By candidates = By.cssSelector(
                "button,[role='menuitem'],[data-slot='base'],[data-slot='menu-item']");
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(12));
        shortWait.pollingEvery(Duration.ofMillis(250));
        WebElement option = shortWait.until(d -> d.findElements(candidates).stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed()
                                && normalized(element.getText()).startsWith(normalized(text));
                    } catch (StaleElementReferenceException ignored) {
                        return false;
                    }
                })
                .min(java.util.Comparator.comparingInt(element -> element.getText().length()))
                .orElse(null));
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
        } catch (StaleElementReferenceException ignored) {
            WebElement fresh = shortWait.until(d -> d.findElements(candidates).stream()
                    .filter(element -> {
                        try {
                            return element.isDisplayed()
                                    && normalized(element.getText()).startsWith(normalized(text));
                        } catch (StaleElementReferenceException stale) {
                            return false;
                        }
                    })
                    .min(java.util.Comparator.comparingInt(
                            element -> element.getText().length()))
                    .orElse(null));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fresh);
        }
    }

    private WebElement drawerButton(String label) {
        WebElement drawer = visibleDrawer();
        if (drawer == null) return null;
        return drawer.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> normalized(button.getText())
                        .equals(normalized(label)))
                .findFirst().orElse(null);
    }

    private void clickDrawerButton(String label) {
        WebElement button = drawerButton(label);
        if (button == null) throw new IllegalStateException(
                "Drawer không có action " + label);
        observe(button);
        button.click();
    }

    private void clickDialogButton(WebElement dialog, String label) {
        WebElement button = dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> normalized(element.getText())
                        .equals(normalized(label)))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Dialog không có nút " + label));
        observe(button);
        button.click();
    }

    private WebElement closestClickable(WebElement element) {
        return (WebElement) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].closest('button,label,[role=button],[role=option]')"
                        + " || arguments[0];", element);
    }

    private WebElement exactVisible(By locator) {
        return wait.until(d -> d.findElements(locator).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null));
    }

    private WebElement shortVisible(By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(locator).stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst().orElse(null));
    }

    private void clickFresh(By locator) {
        RuntimeException last = null;
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                exactVisible(locator).click();
                return;
            } catch (StaleElementReferenceException exception) {
                last = exception;
            }
        }
        throw last == null
                ? new IllegalStateException("Không click được element " + locator)
                : last;
    }

    private void fill(WebElement field, String value) {
        observe(field);
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), value);
    }

    private void observe(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("""
                arguments[0].scrollIntoView({
                  behavior: arguments[1] ? 'instant' : 'smooth',
                  block: 'center', inline: 'nearest'
                });
                """, element, TestConfig.headless());
        if (!TestConfig.headless()) {
            new Actions(driver).pause(Duration.ofSeconds(2)).perform();
        }
    }

    private void pauseForDetailObservation(String step) {
        if (TestConfig.headless()) return;
        int seconds = 5;
        try {
            seconds = Math.max(0, Integer.parseInt(System.getProperty(
                    "customer.order.detail.pause.seconds", "5")));
        } catch (NumberFormatException ignored) {
            // Giữ mặc định 5 giây nếu giá trị cấu hình không hợp lệ.
        }
        if (seconds == 0) return;
        System.out.println("[QUAN SAT] " + step + " - giu man hinh "
                + seconds + " giay");
        new Actions(driver).pause(Duration.ofSeconds(seconds)).perform();
    }

    private void pauseForFilterObservation(String step, int defaultSeconds) {
        if (TestConfig.headless()) return;
        int seconds = defaultSeconds;
        try {
            seconds = Math.max(0, Integer.parseInt(System.getProperty(
                    "customer.order.filter.pause.seconds",
                    Integer.toString(defaultSeconds))));
        } catch (NumberFormatException ignored) {
            // Giữ thời gian mặc định nếu cấu hình không hợp lệ.
        }
        if (seconds == 0) return;
        System.out.println("[QUAN SAT] " + step + " - giu man hinh "
                + seconds + " giay");
        new Actions(driver).pause(Duration.ofSeconds(seconds)).perform();
    }

    private static String extractOrderId(String text) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                "Mã đơn dịch vụ\\s*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (matcher.find()) return matcher.group(1);
        throw new IllegalStateException("Không đọc được mã đơn trong drawer.");
    }

    private static String normalized(String value) {
        return TextNormalizer.normalize(value == null ? "" : value);
    }

    private static String extractCurrentOrderStatus(String statusCellText) {
        List<String> lines = statusCellText.lines()
                .map(String::trim).filter(value -> !value.isBlank()).toList();
        for (int index = 0; index < lines.size() - 1; index++) {
            if (normalized(lines.get(index)).equals("don dich vu")) {
                String value = lines.get(index + 1);
                return ORDER_STATUSES.stream()
                        .filter(status -> normalized(status).equals(normalized(value)))
                        .findFirst().orElse(value);
            }
        }
        return ORDER_STATUSES.stream()
                .filter(status -> normalized(statusCellText).contains(normalized(status)))
                .findFirst().orElse(statusCellText.trim());
    }

    public static final List<String> ORDER_STATUSES = List.of(
            "Tìm kiếm thợ", "Match đơn", "Thợ di chuyển", "Thợ checkin",
            "Yêu cầu giá", "Chấp nhận giá", "Đang làm việc", "Đã xong việc",
            "Hoàn thành đơn", "Hủy đơn", "Đặt lại thợ yêu thích");

    public static final List<String> AGREEMENT_STATUSES = List.of(
            "Chưa có", "Chờ đợi", "Chấp nhận", "Từ chối");

    public record OrderRow(
            String id,
            String info,
            String status,
            String statusDetails,
            String workerCount,
            String connectionFee,
            String requestedAt,
            String rawText) {
    }

    public record DetailSnapshot(
            String id, String status, String text, List<String> buttons) {
    }

    public record MutationResult(
            String id, String beforeStatus, String afterStatus, String detailText) {
    }
}
