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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final By CALENDAR_DAYS = By.cssSelector(
            "[role='option'][aria-label],"
                    + "[role='gridcell'] [aria-label],"
                    + "[data-slot='cell-button'][aria-label]");

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
        pauseForFilterObservation("Da dat lai bo loc", 2);
        waitForData();
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
        revealRequestDateCalendar();
        navigateCalendar(YearMonth.now(), YearMonth.from(from));
        clickCalendarDay(from);
        if (!from.equals(to)) {
            openFilter();
            revealRequestDateCalendar();
            navigateCalendar(YearMonth.from(from), YearMonth.from(to));
            clickCalendarDay(to);
        }
        waitForFilterResult();
        closeFilterIfOpen();
        return this;
    }

    private void revealRequestDateCalendar() {
        WebElement panel = openFilter();
        WebElement heading = panel.findElements(By.xpath(
                        ".//*[normalize-space()='Thời gian yêu cầu']"))
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Bộ lọc thiếu khu vực Thời gian yêu cầu."));
        WebElement section = heading.findElement(
                By.xpath("./ancestor::section[1]"));
        ((JavascriptExecutor) driver).executeScript("""
                arguments[0].scrollIntoView({
                  behavior: arguments[1] ? 'instant' : 'smooth',
                  block: 'center', inline: 'nearest'
                });
                """, section, TestConfig.headless());
        pauseForFilterObservation(
                "Da cuon xuong bo loc Thoi gian yeu cau", 2);
        new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .until(d -> d.findElements(CALENDAR_DAYS).stream()
                        .anyMatch(element -> {
                            try {
                                return element.isDisplayed();
                            } catch (StaleElementReferenceException ignored) {
                                return false;
                            }
                        }));
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
        pauseLocally(Duration.ofMillis(800));
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
        System.out.println("[FILTER] Chon "
                + TextNormalizer.normalize(groupLabel) + " -> "
                + TextNormalizer.normalize(value));
        closeFilterIfOpen();
        openFilter();
        boolean revealed = new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .until(d -> Boolean.TRUE.equals(
                        locateDirectFilterOption(
                                groupLabel, value, false)));
        if (!revealed) {
            throw new IllegalStateException(
                    "Không tìm thấy filter " + groupLabel + " = " + value);
        }
        pauseForFilterObservation(
                "Chon " + TextNormalizer.normalize(groupLabel)
                        + " -> " + TextNormalizer.normalize(value), 2);
        openFilter();
        new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .until(d -> {
                    if (visibleFilterPanel() == null) {
                        openFilter();
                    }
                    return Boolean.TRUE.equals(locateDirectFilterOption(
                            groupLabel, value, true));
                });
        waitForFilterResult();
        openFilter();
        new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(250))
                .until(d -> directFilterChecked(groupLabel, value));
        closeFilterIfOpen();
        return this;
    }

    private Boolean locateDirectFilterOption(
            String groupLabel, String value, boolean click) {
        return (Boolean) ((JavascriptExecutor) driver).executeScript("""
                const groupLabel = arguments[0].trim();
                const value = arguments[1].trim();
                const click = arguments[2];
                const panels = [...document.querySelectorAll(
                  '[data-slot="content"],[data-slot="popover"],[role="dialog"]')]
                  .filter(item => item.offsetParent !== null);
                for (const panel of panels) {
                  const group = [...panel.querySelectorAll('label')]
                    .find(item => item.textContent.trim() === groupLabel);
                  if (!group) continue;
                  const labels = [...group.parentElement.querySelectorAll(
                    'label:has(input[type="radio"])')];
                  const option = labels.find(item =>
                    item.textContent.trim() === value);
                  if (!option) continue;
                  option.scrollIntoView({
                    behavior: arguments[3] ? 'instant' : 'smooth',
                    block: 'center', inline: 'nearest'
                  });
                  option.style.outline = '3px solid #2563eb';
                  if (click) option.click();
                  return true;
                }
                return false;
                """, groupLabel, value, click, TestConfig.headless());
    }

    private boolean directFilterChecked(String groupLabel, String value) {
        return Boolean.TRUE.equals(
                ((JavascriptExecutor) driver).executeScript("""
                        const groupLabel = arguments[0].trim();
                        const value = arguments[1].trim();
                        const panels = [...document.querySelectorAll(
                          '[data-slot="content"],[data-slot="popover"],[role="dialog"]')]
                          .filter(item => item.offsetParent !== null);
                        for (const panel of panels) {
                          const group = [...panel.querySelectorAll('label')]
                            .find(item => item.textContent.trim() === groupLabel);
                          if (!group) continue;
                          const scope = group.parentElement;
                          const labels = [...scope.querySelectorAll(
                            'label:has(input[type="radio"])')];
                          const selected = labels.find(item =>
                            item.textContent.trim() === value);
                          if (!selected) continue;
                          const input = selected.querySelector(
                            'input[type="radio"]');
                          return Boolean(input?.checked
                            || selected.dataset.selected === 'true'
                            || selected.getAttribute('data-selected') === 'true');
                        }
                        return false;
                        """, groupLabel, value));
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
        pauseForViewObservation(
                "Da chuyen sang che do "
                        + TextNormalizer.normalize(label));
        return this;
    }

    public boolean cardViewContainsOrders() {
        return driver.findElements(By.xpath(
                        "//*[starts-with(normalize-space(),'#')][contains(normalize-space(),'')]"))
                .stream().filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .anyMatch(text -> text.matches("#\\d+"));
    }

    public List<String> excelExportMenuOptions() {
        WebElement button = exactVisible(By.xpath(
                "//button[normalize-space()='Xuất Excel']"));
        observe(button);
        button.click();
        List<String> options = new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .until(d -> {
                    List<String> values = d.findElements(By.xpath(
                                    "//button[.//*[normalize-space()='Xuất chi tiết đơn hàng'"
                                            + " or normalize-space()='Xuất tổng hợp theo ngày']]"))
                            .stream().filter(WebElement::isDisplayed)
                            .map(WebElement::getText).map(String::trim)
                            .distinct().toList();
                    return values.size() == 2 ? values : null;
                });
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        return options;
    }

    public String exportExcel(String optionText) {
        Path directory = Path.of(TestConfig.downloadDirectory())
                .toAbsolutePath().normalize();
        ensureDownloadDirectory(directory);
        Set<String> before = completedDownloadSnapshot(directory);

        WebElement menu = exactVisible(By.xpath(
                "//button[normalize-space()='Xuất Excel']"));
        observe(menu);
        menu.click();
        WebElement option = new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(d -> d.findElements(By.xpath(
                                "//button[.//*[normalize-space()='"
                                        + optionText + "']]"))
                        .stream().filter(WebElement::isDisplayed)
                        .findFirst().orElse(null));
        observe(option);
        option.click();

        return waitForNewDownload(directory, before);
    }

    public String exportCurrentStatisticsExcel() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) {
            throw new IllegalStateException("Popup thống kê chưa được mở.");
        }
        Path directory = Path.of(TestConfig.downloadDirectory())
                .toAbsolutePath().normalize();
        ensureDownloadDirectory(directory);
        Set<String> before = completedDownloadSnapshot(directory);
        WebElement export = dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> normalized(button.getText())
                        .equals("xuat excel"))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Popup thống kê thiếu nút Xuất Excel."));
        observe(export);
        export.click();
        return waitForNewDownload(directory, before);
    }

    private void ensureDownloadDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Không tạo được thư mục tải Excel: " + directory,
                    exception);
        }
    }

    private String waitForNewDownload(
            Path directory, Set<String> before) {
        try {
            return new WebDriverWait(
                    driver, TestConfig.exportDownloadTimeout())
                    .pollingEvery(Duration.ofMillis(300))
                    .until(d -> completedDownloadSnapshot(directory).stream()
                            .filter(file -> !before.contains(file))
                            .findFirst().orElse(null));
        } catch (TimeoutException exception) {
            return "";
        }
    }

    private Set<String> completedDownloadSnapshot(Path directory) {
        try (var files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .matches("(?i).+\\.(xlsx|xls|csv)$"))
                    .map(path -> {
                        try {
                            return path.getFileName() + "|"
                                    + Files.size(path) + "|"
                                    + Files.getLastModifiedTime(path).toMillis();
                        } catch (IOException ignored) {
                            return "";
                        }
                    })
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toSet());
        } catch (IOException exception) {
            return Set.of();
        }
    }

    public CustomerWorkerOrderPage openStatistic(String optionText) {
        currentStatistic = optionText;
        clickFreshButton("Thống kê", false);
        clickFreshStatisticOption(optionText);
        wait.until(d -> statisticsDialog() != null);
        return this;
    }

    public List<String> statisticsMenuOptions() {
        clickFreshButton("Thống kê", false);
        List<String> options = new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .until(d -> {
                    List<String> values = d.findElements(By.tagName("button"))
                            .stream().filter(WebElement::isDisplayed)
                            .map(WebElement::getText).map(String::trim)
                            .filter(text -> text.startsWith("Trạng thái đơn")
                                    || text.startsWith("Bảo hành 5K"))
                            .distinct().toList();
                    return values.size() == 2 ? values : null;
                });
        pauseForViewObservation(
                "Menu Thong ke dang hien thi 2 lua chon");
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        return options;
    }

    public String statisticsText() {
        return new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    WebElement dialog = statisticsDialog();
                    if (dialog == null) return null;
                    String text = dialog.getText().trim();
                    return text.isBlank() ? null : text;
                });
    }

    public String waitStatisticsTextMatches(String regex) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(15))
                    .pollingEvery(Duration.ofMillis(250))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        WebElement dialog = statisticsDialog();
                        if (dialog == null) return null;
                        String text = dialog.getText().trim();
                        return text.matches(regex) ? text : null;
                    });
        } catch (TimeoutException ignored) {
            return "";
        }
    }

    public List<String> statisticsInputValues() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(8))
                    .pollingEvery(Duration.ofMillis(200))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        WebElement dialog = statisticsDialog();
                        if (dialog == null) return null;
                        @SuppressWarnings("unchecked")
                        List<String> values = (List<String>)
                                ((JavascriptExecutor) d).executeScript("""
                                        return [...arguments[0].querySelectorAll(
                                          'input[type="text"]')]
                                          .filter(item => item.getClientRects().length > 0)
                                          .map(item => item.value);
                                        """, dialog);
                        return values;
                    });
        } catch (TimeoutException ignored) {
            return List.of();
        }
    }

    public CustomerWorkerOrderPage setStatisticsCustomDateRange(
            String from, String to) {
        WebElement dialog = statisticsDialog();
        if (dialog == null) {
            throw new IllegalStateException("Popup thống kê chưa được mở.");
        }
        List<WebElement> inputs = dialog.findElements(
                        By.cssSelector("input[type='text']"))
                .stream().filter(WebElement::isDisplayed).toList();
        if (inputs.size() == 1) {
            typeDateValue(inputs.get(0), from + " - " + to);
        } else if (inputs.size() == 2) {
            typeDateValue(inputs.get(0), from);
            dialog = statisticsDialog();
            inputs = dialog.findElements(By.cssSelector("input[type='text']"))
                    .stream().filter(WebElement::isDisplayed).toList();
            typeDateValue(inputs.get(1), to);
        } else {
            throw new IllegalStateException(
                    "Số ô ngày không hợp lệ: " + inputs.size());
        }

        List<String> expected = inputs.size() == 1
                ? List.of(from + " - " + to) : List.of(from, to);
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .pollingEvery(Duration.ofMillis(250))
                .until(d -> statisticsInputValues().equals(expected));
        pauseForStatisticsObservation(
                "Da nhap khoang ngay " + from + " - " + to);
        return this;
    }

    public CustomerWorkerOrderPage selectStatusStatisticsDateRangeFromCalendar(
            LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.");
        }
        if (!normalized(currentStatistic).contains("trang thai")) {
            throw new IllegalStateException(
                    "Calendar khoảng ngày chỉ thuộc popup Trạng thái.");
        }
        WebElement dialog = statisticsDialog();
        if (dialog == null) {
            throw new IllegalStateException("Popup thống kê chưa được mở.");
        }
        WebElement input = dialog.findElements(
                        By.cssSelector("input[type='text']"))
                .stream().filter(WebElement::isDisplayed)
                .findFirst().orElseThrow();
        observe(input);
        boolean opened = false;
        for (String selector : List.of(
                "svg.cursor-pointer", "[data-slot='trigger']",
                "[data-slot='input-wrapper']")) {
            dialog = statisticsDialog();
            if (dialog == null) break;
            WebElement trigger = dialog.findElements(By.cssSelector(selector))
                    .stream().filter(WebElement::isDisplayed)
                    .findFirst().orElse(null);
            if (trigger == null) continue;
            try {
                new Actions(driver).moveToElement(trigger).click().perform();
            } catch (RuntimeException nativeClickFailed) {
                ((JavascriptExecutor) driver).executeScript(
                        """
                        arguments[0].dispatchEvent(new MouseEvent('click', {
                          bubbles: true, cancelable: true, view: window
                        }));
                        """, trigger);
            }
            try {
                new WebDriverWait(driver, Duration.ofSeconds(3))
                        .pollingEvery(Duration.ofMillis(150))
                        .until(d -> d.findElements(CALENDAR_DAYS)
                                .stream().anyMatch(WebElement::isDisplayed));
                opened = true;
                break;
            } catch (TimeoutException ignored) {
                // Thử trigger tiếp theo của DateRangePicker.
            }
        }
        if (!opened) {
            throw new IllegalStateException(
                    "Click icon ngày nhưng calendar Trạng thái không mở.");
        }
        new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(d -> d.findElements(CALENDAR_DAYS)
                        .stream().anyMatch(WebElement::isDisplayed));
        navigateCalendar(YearMonth.now(), YearMonth.from(from));
        clickCalendarDay(from);
        navigateCalendar(YearMonth.from(from), YearMonth.from(to));
        clickCalendarDay(to);

        String expected = formatDate(from) + " - " + formatDate(to);
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .pollingEvery(Duration.ofMillis(250))
                .until(d -> statisticsInputValues().equals(List.of(expected)));
        pauseForStatisticsObservation(
                "Da chon tren lich " + expected);
        return this;
    }

    public String statisticsAppliedRangeText() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return "";
        return dialog.findElements(By.xpath(
                        ".//*[starts-with(normalize-space(),'*Áp dụng từ')]"))
                .stream().filter(WebElement::isDisplayed)
                .map(WebElement::getText).map(String::trim)
                .min(java.util.Comparator.comparingInt(String::length))
                .orElse("");
    }

    public List<String> enterRawStatisticsDateRange(
            String from, String to) {
        WebElement dialog = statisticsDialog();
        if (dialog == null) {
            throw new IllegalStateException("Popup thống kê chưa được mở.");
        }
        List<WebElement> inputs = dialog.findElements(
                        By.cssSelector("input[type='text']"))
                .stream().filter(WebElement::isDisplayed).toList();
        if (inputs.size() == 1) {
            typeRawDateValue(inputs.get(0), from + " - " + to);
        } else if (inputs.size() == 2) {
            typeRawDateValue(inputs.get(0), from);
            dialog = statisticsDialog();
            inputs = dialog.findElements(By.cssSelector("input[type='text']"))
                    .stream().filter(WebElement::isDisplayed).toList();
            typeRawDateValue(inputs.get(1), to);
        } else {
            throw new IllegalStateException(
                    "Số ô ngày không hợp lệ: " + inputs.size());
        }
        pauseLocally(Duration.ofSeconds(1));
        return statisticsInputValues();
    }

    public String statisticsDateValidationText() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return "";
        return (String) ((JavascriptExecutor) driver).executeScript("""
                const root = arguments[0];
                const messages = [];
                for (const input of root.querySelectorAll('input')) {
                  if (input.validationMessage) {
                    messages.push(input.validationMessage);
                  }
                  if (input.getAttribute('aria-invalid') === 'true') {
                    messages.push('aria-invalid');
                  }
                }
                for (const item of root.querySelectorAll(
                  '[role="alert"],[data-slot="error-message"],'
                    + '[class*="text-danger"],[class*="text-red"]')) {
                  if (item.getClientRects().length && item.textContent.trim()) {
                    messages.push(item.textContent.trim());
                  }
                }
                return [...new Set(messages)].join(' | ');
                """, dialog);
    }

    private void typeDateValue(WebElement input, String value) {
        observe(input);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
        pauseLocally(Duration.ofMillis(700));
    }

    private void typeRawDateValue(WebElement input, String value) {
        observe(input);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.BACK_SPACE);
        if (!value.isBlank()) {
            input.sendKeys(value);
        }
        input.sendKeys(Keys.TAB);
        pauseLocally(Duration.ofMillis(700));
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String statisticsBlockText(String label) {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return "";
        List<WebElement> markers = dialog.findElements(By.xpath(
                ".//*[normalize-space()='" + label + "']"));
        if (markers.stream().noneMatch(WebElement::isDisplayed)) {
            markers = dialog.findElements(By.xpath(
                    ".//*[starts-with(normalize-space(),'" + label + "')]"));
        }
        WebElement marker = markers.stream().filter(WebElement::isDisplayed)
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Popup thống kê thiếu khối " + label));
        return (String) ((JavascriptExecutor) driver).executeScript("""
                let node = arguments[0];
                while (node && node !== arguments[1]) {
                  const text = (node.innerText || '').trim();
                  const classes = node.className || '';
                  if (node !== arguments[0]
                      && text.includes(arguments[2])
                      && (classes.includes('rounded')
                          || classes.includes('border'))) {
                    return text;
                  }
                  node = node.parentElement;
                }
                return arguments[0].parentElement?.innerText || '';
                """, marker, dialog, label);
    }

    public CustomerWorkerOrderPage clickStatisticsButton(String label) {
        WebElement dialog = statisticsDialog();
        if (dialog == null) {
            throw new IllegalStateException("Popup thống kê chưa được mở.");
        }
        WebElement button = dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> normalized(element.getText())
                        .equals(normalized(label)))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Popup thống kê thiếu button " + label));
        observe(button);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();", button);
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> statisticsButtonSelected(label));
        String normalizedLabel = normalized(label);
        if (normalizedLabel.equals("don hoan thanh")
                || normalizedLabel.equals("don huy")) {
            String expected = normalizedLabel.equals("don hoan thanh")
                    ? "Tổng số đơn hoàn thành" : "Tổng số đơn hủy";
            waitStatisticsTextMatches("(?s).*" + expected + ".*");
        }
        pauseLocally(Duration.ofMillis(800));
        pauseForStatisticsObservation("Da chon " + TextNormalizer.normalize(label));
        return this;
    }

    public boolean statisticsButtonSelected(String label) {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return false;
        return dialog.findElements(By.tagName("button")).stream()
                .anyMatch(element -> {
                    try {
                        if (!element.isDisplayed()
                                || !normalized(element.getText())
                                .equals(normalized(label))) {
                            return false;
                        }
                        String classes = element.getAttribute("class");
                        return classes != null
                                && (classes.contains("bg-primary-blue")
                                || classes.contains("text-primary-blue"));
                    } catch (StaleElementReferenceException ignored) {
                        return false;
                    }
                });
    }

    public int statisticsChartCount() {
        int minimumCount = normalized(currentStatistic).contains("bao hanh")
                ? 2 : 0;
        return statisticsElementCount(
                ".recharts-wrapper", null, minimumCount);
    }

    public int statisticsBarCount(String dataName) {
        return statisticsElementCount(
                ".recharts-bar-rectangle path[name='" + dataName + "']", 1, 1);
    }

    public String hoverStatisticsBar(String dataName) {
        WebElement path = null;
        boolean observed = false;
        for (int attempt = 0; attempt < 3 && !observed; attempt++) {
            path = tallestStatisticsBar(dataName);
            if (path == null) return "";
            try {
                observe(path);
                observed = true;
            } catch (StaleElementReferenceException ignored) {
                // Chart có thể render lại khi vừa mở popup; lấy path mới.
            }
        }
        if (!observed) return "";
        boolean moved = false;
        for (int attempt = 0; attempt < 3 && !moved; attempt++) {
            path = tallestStatisticsBar(dataName);
            if (path == null) return "";
            try {
                new Actions(driver).moveToElement(path)
                        .pause(TestConfig.headless()
                                ? Duration.ofMillis(500)
                                : Duration.ofSeconds(2))
                        .perform();
                moved = true;
            } catch (StaleElementReferenceException ignored) {
                // Chart có thể render lại khi vừa cuộn tới; lấy path mới.
            }
        }
        if (!moved) return "";
        path = tallestStatisticsBar(dataName);
        if (path == null) return "";
        if (visibleTooltipText().isBlank()) {
            ((JavascriptExecutor) driver).executeScript("""
                    const path = arguments[0];
                    const target = path.parentElement || path;
                    const rect = path.getBoundingClientRect();
                    const init = {
                      bubbles: true,
                      cancelable: true,
                      clientX: rect.left + rect.width / 2,
                      clientY: rect.top + rect.height / 2,
                      view: window
                    };
                    target.dispatchEvent(new MouseEvent('mouseover', init));
                    target.dispatchEvent(new MouseEvent('mouseenter', init));
                    target.dispatchEvent(new MouseEvent('mousemove', init));
                    path.dispatchEvent(new MouseEvent('mouseover', init));
                    path.dispatchEvent(new MouseEvent('mousemove', init));
                    const svg = path.closest('svg');
                    if (svg) {
                      svg.dispatchEvent(new MouseEvent('mouseover', init));
                      svg.dispatchEvent(new MouseEvent('mousemove', init));
                    }
                    """, path);
        }
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(8))
                    .pollingEvery(Duration.ofMillis(200))
                    .until(d -> {
                        String tooltip = visibleTooltipText();
                        return tooltip.isBlank() ? null : tooltip;
                    });
        } catch (TimeoutException ignored) {
            return "";
        }
    }

    private WebElement tallestStatisticsBar(String dataName) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(8))
                    .pollingEvery(Duration.ofMillis(150))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        WebElement dialog = statisticsDialog();
                        if (dialog == null) return null;
                        return (WebElement) ((JavascriptExecutor) d)
                                .executeScript("""
                                        const bars = [...arguments[0]
                                          .querySelectorAll(
                                            '.recharts-bar-rectangle path[name="'
                                              + arguments[1] + '"]')]
                                          .filter(item =>
                                            item.getBoundingClientRect().height > 1)
                                          .sort((a, b) =>
                                            b.getBoundingClientRect().height
                                              - a.getBoundingClientRect().height);
                                        return bars[0] || null;
                                        """, dialog, dataName);
                    });
        } catch (TimeoutException ignored) {
            return null;
        }
    }

    private int statisticsElementCount(
            String selector, Integer minimumHeight, int minimumCount) {
        try {
            Long result = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .pollingEvery(Duration.ofMillis(200))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        WebElement dialog = statisticsDialog();
                        if (dialog == null) return null;
                        Long observedCount = (Long) ((JavascriptExecutor) d)
                                .executeScript("""
                                const minimumHeight = arguments[2];
                                return [...arguments[0].querySelectorAll(arguments[1])]
                                  .filter(item => {
                                    const rect = item.getBoundingClientRect();
                                    return rect.width > 0 && rect.height > 0
                                      && (minimumHeight === null
                                        || rect.height > minimumHeight);
                                  }).length;
                                """, dialog, selector, minimumHeight);
                        return observedCount >= minimumCount
                                ? observedCount : null;
                    });
            return result.intValue();
        } catch (TimeoutException ignored) {
            return 0;
        }
    }

    public CustomerWorkerOrderPage scrollStatisticsTo(String text) {
        WebElement dialog = statisticsDialog();
        if (dialog == null) {
            throw new IllegalStateException("Popup thống kê chưa được mở.");
        }
        WebElement marker = dialog.findElements(By.xpath(
                        ".//*[normalize-space()='" + text + "']"))
                .stream().filter(WebElement::isDisplayed)
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Popup thống kê thiếu nội dung " + text));
        observe(marker);
        pauseForStatisticsObservation(
                "Da cuon den " + TextNormalizer.normalize(text));
        return this;
    }

    public CustomerWorkerOrderPage closeStatistics() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return this;
        WebElement close = dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getText().isBlank())
                .max(java.util.Comparator.comparingInt(
                        button -> button.getRect().getX()))
                .orElseThrow(() -> new IllegalStateException(
                        "Popup thống kê thiếu nút đóng."));
        observe(close);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();", close);
        new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(d -> statisticsDialog() == null);
        currentStatistic = "";
        return this;
    }

    private WebElement statisticsDialog() {
        if (currentStatistic.isBlank()) return visibleDialog();
        String heading = normalized(currentStatistic).contains("bao hanh")
                ? "Thống kê Bảo hành 5K"
                : "Thống kê trạng thái đơn dịch vụ";
        return visibleDialogContaining(heading);
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

    public DetailSnapshot openFirstOrderForWorkflow(String status, String requiredAction) {
        reset();
        selectOrderStatus(status);

        if (rows().isEmpty()) {
            throw new OrderDataUnavailableException(
                    "Không có đơn nào ở trạng thái '" + status + "'.");
        }
        int lastPage = Math.max(1, totalPages());
        for (int page = 1; page <= lastPage; page++) {
            int count = driver.findElements(ROWS).size();
            for (int index = 0; index < count; index++) {
                List<WebElement> current = driver.findElements(ROWS);
                if (index >= current.size()) break;
                DetailSnapshot detail = openRow(current.get(index), false);
                boolean statusMatches = normalized(detail.status())
                        .equals(normalized(status));
                boolean actionMatches = detail.buttons().stream()
                        .anyMatch(text -> normalized(text)
                                .equals(normalized(requiredAction)));
                if (statusMatches && actionMatches) {
                    highlightForObservation(requiredDrawer());
                    pauseForDetailObservation(
                            "Da chon don #" + detail.id() + " - "
                                    + TextNormalizer.normalize(status)
                                    + " - action "
                                    + TextNormalizer.normalize(requiredAction));
                    System.out.println("[TIM DON] Da chon don #"
                            + detail.id() + " - "
                            + TextNormalizer.normalize(status));
                    return detail;
                }
                closeOverlay();
            }
            System.out.println("[TIM DON] Trang " + page
                    + " khong co don phu hop");
            if (page < lastPage) {
                nextPage();
            }
        }
        throw new OrderDataUnavailableException(
                "Không tìm thấy đơn trạng thái '" + status
                        + "' có action '" + requiredAction + "'.");
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
        pauseForWorkflowObservation("Đã mở popup Sang bước kế tiếp");
        for (WebElement input : dialog.findElements(By.cssSelector("input,textarea"))) {
            if (!input.isDisplayed() || input.getAttribute("disabled") != null) continue;
            String value = input.getAttribute("value");
            if (value == null || value.isBlank()) {
                observe(input);
                input.sendKeys("textarea".equalsIgnoreCase(input.getTagName())
                        ? "Dịch vụ automation" : "100000");
            }
        }
        pauseForWorkflowObservation("Đã kiểm tra và nhập dữ liệu báo giá");
        clickDialogButton(dialog, "Xác nhận");
        waitAfterMutation();
        pauseForWorkflowObservation("Đã xác nhận, hệ thống cập nhật tiến trình đơn");
        closeOverlay();
        open();
        DetailSnapshot updated = openOrder(id);
        return new MutationResult(id, before, updated.status(), updated.text());
    }

    public AdvanceQuoteSnapshot openAdvanceQuotePopup() {
        clickDrawerButton("Sang bước kế tiếp");
        WebElement dialog = wait.until(d -> visibleDialogContaining("Sang bước kế tiếp"));
        pauseForWorkflowObservation("Đã mở popup Sang bước kế tiếp");
        return advanceQuoteSnapshot(dialog);
    }

    public AdvanceQuoteSnapshot currentAdvanceQuoteSnapshot() {
        return advanceQuoteSnapshot(requiredAdvanceDialog());
    }

    public int addAdvanceQuoteRow() {
        WebElement dialog = requiredAdvanceDialog();
        int before = advanceServiceFields(dialog).size();
        clickDialogButton(dialog, "Thêm báo giá");
        int after = wait.until(d -> {
            WebElement current = visibleDialogContaining("Sang bước kế tiếp");
            if (current == null) return null;
            int count = advanceServiceFields(current).size();
            return count == before + 1 ? count : null;
        });
        pauseForWorkflowObservation("Đã thêm một dòng báo giá");
        return after;
    }

    public int removeLastAdvanceQuoteRow() {
        WebElement dialog = requiredAdvanceDialog();
        List<WebElement> services = advanceServiceFields(dialog);
        if (services.size() < 2) {
            throw new IllegalStateException(
                    "Cần ít nhất hai dòng báo giá để kiểm tra xóa dòng.");
        }
        int before = services.size();
        WebElement row = services.get(services.size() - 1).findElement(By.xpath(
                "./ancestor::div[contains(@class,'grid-cols-9')][1]"));
        WebElement remove = row.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Dòng báo giá cuối không có nút xóa."));
        observe(remove);
        remove.click();
        int after = wait.until(d -> {
            WebElement current = visibleDialogContaining("Sang bước kế tiếp");
            if (current == null) return null;
            int count = advanceServiceFields(current).size();
            return count == before - 1 ? count : null;
        });
        pauseForWorkflowObservation("Đã xóa dòng báo giá vừa thêm");
        return after;
    }

    public AdvanceQuoteSnapshot fillLastAdvanceQuoteRow(
            String service, String price) {
        WebElement dialog = requiredAdvanceDialog();
        List<WebElement> services = advanceServiceFields(dialog);
        List<WebElement> prices = advancePriceFields(dialog);
        if (services.isEmpty() || services.size() != prices.size()) {
            throw new IllegalStateException(
                    "Popup không có cặp trường dịch vụ/giá tiền hợp lệ.");
        }
        WebElement serviceField = services.get(services.size() - 1);
        WebElement priceField = prices.get(prices.size() - 1);
        clearField(serviceField);
        serviceField.sendKeys(service);
        pauseForWorkflowObservation("Đã nhập dịch vụ cho dòng báo giá mới");
        clearField(priceField);
        priceField.sendKeys(price);
        pauseForWorkflowObservation("Đã nhập giá tiền cho dòng báo giá mới");
        return currentAdvanceQuoteSnapshot();
    }

    public String submitBlankAdvanceQuoteAndReadValidation() {
        WebElement dialog = requiredAdvanceDialog();
        List<WebElement> services = advanceServiceFields(dialog);
        List<WebElement> prices = advancePriceFields(dialog);
        if (services.isEmpty() || prices.isEmpty()) {
            throw new IllegalStateException(
                    "Popup Sang bước kế tiếp không có trường dịch vụ/giá tiền.");
        }
        clearField(services.get(0));
        clearField(prices.get(0));
        pauseForWorkflowObservation("Đã để trống dịch vụ và giá tiền");
        clickDialogButton(requiredAdvanceDialog(), "Xác nhận");
        String validation = wait.until(d -> {
            WebElement current = visibleDialogContaining("Sang bước kế tiếp");
            if (current == null) return null;
            return current.findElements(By.xpath(
                            ".//*[normalize-space()='Vui lòng nhập đầy đủ thông tin']"))
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .map(WebElement::getText)
                    .findFirst().orElse(null);
        });
        pauseForWorkflowObservation("Đã hiển thị validation dữ liệu báo giá");
        return validation;
    }

    public boolean cancelAdvanceQuotePopup() {
        WebElement dialog = requiredAdvanceDialog();
        clickDialogButton(dialog, "Hủy");
        wait.until(d -> visibleDialogContaining("Sang bước kế tiếp") == null);
        pauseForWorkflowObservation("Đã hủy popup, không chuyển trạng thái đơn");
        return visibleDrawer() != null;
    }

    public boolean closeAdvanceQuotePopupByIcon() {
        WebElement dialog = requiredAdvanceDialog();
        WebElement close = dialog.findElements(By.xpath(
                        ".//h5[normalize-space()='Sang bước kế tiếp']"
                                + "/following-sibling::button[1]"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Popup Sang bước kế tiếp thiếu nút đóng X."));
        observe(close);
        close.click();
        wait.until(d -> visibleDialogContaining("Sang bước kế tiếp") == null);
        pauseForWorkflowObservation(
                "Đã đóng popup bằng dấu X, không chuyển trạng thái đơn");
        return visibleDrawer() != null;
    }

    private AdvanceQuoteSnapshot advanceQuoteSnapshot(WebElement dialog) {
        List<String> services = advanceServiceFields(dialog).stream()
                .map(element -> String.valueOf(element.getAttribute("value")))
                .toList();
        List<String> prices = advancePriceFields(dialog).stream()
                .map(element -> String.valueOf(element.getAttribute("value")))
                .toList();
        List<String> buttons = dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        return new AdvanceQuoteSnapshot(
                dialog.getText(), services, prices, buttons);
    }

    private WebElement requiredAdvanceDialog() {
        WebElement dialog = visibleDialogContaining("Sang bước kế tiếp");
        if (dialog == null) {
            throw new IllegalStateException(
                    "Popup Sang bước kế tiếp chưa được mở.");
        }
        return dialog;
    }

    private List<WebElement> advanceServiceFields(WebElement dialog) {
        return dialog.findElements(By.cssSelector(
                        "textarea[aria-label='Nhập dịch vụ']"))
                .stream().filter(WebElement::isDisplayed).toList();
    }

    private List<WebElement> advancePriceFields(WebElement dialog) {
        return dialog.findElements(By.cssSelector(
                        "input[aria-label='Nhập giá tiền']"))
                .stream().filter(WebElement::isDisplayed).toList();
    }

    private void clearField(WebElement field) {
        observe(field);
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        field.sendKeys(Keys.BACK_SPACE);
    }

    public MutationResult cancelOpenOrder(String title, String reason) {
        WebElement drawer = requiredDrawer();
        String id = extractOrderId(drawer.getText());
        String before = currentRowStatus;
        clickDrawerButton("Hủy đơn");
        WebElement dialog = wait.until(d -> visibleDialogContaining("Hủy đơn"));
        pauseForWorkflowObservation("Đã mở popup Hủy đơn");
        List<WebElement> fields = dialog.findElements(By.cssSelector("input,textarea"))
                .stream().filter(WebElement::isDisplayed).toList();
        if (fields.size() < 2) {
            throw new IllegalStateException("Popup Hủy đơn thiếu Tiêu đề hoặc Lý do.");
        }
        fill(fields.get(0), title);
        fill(fields.get(1), reason);
        pauseForWorkflowObservation("Đã nhập tiêu đề và lý do hủy đơn");
        clickDialogButton(dialog, "Xác nhận");
        waitAfterMutation();
        pauseForWorkflowObservation("Đã xác nhận hủy và hệ thống cập nhật đơn");
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
        closeFilterIfOpen();
        WebElement panel = openFilter();
        System.out.println("[FILTER] Mo " + TextNormalizer.normalize(ariaLabel)
                + " -> " + TextNormalizer.normalize(value));
        WebElement trigger = visibleNestedFilterTrigger(panel, ariaLabel);
        highlightForObservation(trigger);
        pauseForFilterObservation(
                "Mo " + TextNormalizer.normalize(ariaLabel), 2);
        WebElement freshTrigger = new WebDriverWait(
                driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    WebElement currentPanel = visibleFilterPanel();
                    return currentPanel == null ? null
                            : visibleNestedFilterTrigger(
                            currentPanel, ariaLabel);
                });
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();", freshTrigger);
        new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    WebElement currentPanel = visibleFilterPanel();
                    if (currentPanel == null) return false;
                    WebElement currentTrigger = visibleNestedFilterTrigger(
                            currentPanel, ariaLabel);
                    return "true".equalsIgnoreCase(
                            currentTrigger.getAttribute("aria-expanded"));
                });
        WebElement option = new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> visibleNestedFilterOption(value));
        System.out.println("[FILTER] Da mo danh sach lua chon");
        pauseForFilterObservation(
                "Danh sach " + TextNormalizer.normalize(ariaLabel)
                        + " da mo", 2);
        highlightForObservation(option);
        pauseForFilterObservation(
                "Chon " + TextNormalizer.normalize(value), 2);
        WebElement selectionTrigger = new WebDriverWait(
                driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    WebElement currentPanel = visibleFilterPanel();
                    return currentPanel == null ? null
                            : visibleNestedFilterTrigger(
                            currentPanel, ariaLabel);
                });
        if (!selectOnlyNestedFilterValue(selectionTrigger, value)) {
            throw new IllegalStateException(
                    "Không cập nhật được giá trị bộ lọc " + ariaLabel
                            + " = " + value);
        }
        WebElement selectedTrigger = new WebDriverWait(
                driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    WebElement currentPanel = visibleFilterPanel();
                    if (currentPanel == null) return null;
                    WebElement currentTrigger = visibleNestedFilterTrigger(
                            currentPanel, ariaLabel);
                    return normalized(currentTrigger.getText())
                            .contains(normalized(value))
                            ? currentTrigger : null;
                });
        highlightForObservation(selectedTrigger);
        pauseForFilterObservation(
                "Da chon " + TextNormalizer.normalize(value), 2);
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        System.out.println("[FILTER] Da chon " + TextNormalizer.normalize(value));
        closeFilterIfOpen();
        waitForFilterResult();
        waitForRowsMatchingStatus(value);
        System.out.println("[FILTER] Da tai xong du lieu");
        return this;
    }

    private void waitForRowsMatchingStatus(String expectedStatus) {
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    List<OrderRow> currentRows = rows();
                    if (!currentRows.isEmpty()) {
                        return currentRows.stream().allMatch(row ->
                                normalized(row.status())
                                        .equals(normalized(expectedStatus)));
                    }
                    String text = normalized(
                            d.findElement(By.tagName("main")).getText());
                    return text.contains("chua co du lieu")
                            || text.contains("khong co du lieu");
                });
    }

    private WebElement visibleNestedFilterTrigger(
            WebElement panel, String ariaLabel) {
        return panel.findElements(By.cssSelector("button[aria-label]")).stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed()
                                && ariaLabel.equals(
                                element.getAttribute("aria-label"));
                    } catch (StaleElementReferenceException ignored) {
                        return false;
                    }
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy bộ chọn " + ariaLabel));
    }

    private WebElement visibleNestedFilterOption(String value) {
        return driver.findElements(By.cssSelector(
                        "[role='listbox'] [role='option'],"
                                + "[data-slot='listbox'] [data-key]"))
                .stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed()
                                && !String.valueOf(element.getAttribute("class"))
                                .contains("react-datepicker")
                                && normalized(element.getText())
                                .equals(normalized(value));
                    } catch (StaleElementReferenceException ignored) {
                        return false;
                    }
                })
                .findFirst().orElse(null);
    }

    private boolean selectOnlyNestedFilterValue(
            WebElement trigger, String value) {
        return Boolean.TRUE.equals(
                ((JavascriptExecutor) driver).executeScript("""
                        const trigger = arguments[0];
                        const expected = arguments[1];
                        const normalize = value => (value || '')
                          .normalize('NFD')
                          .replace(/[\\u0300-\\u036f]/g, '')
                          .replace(/đ/g, 'd').replace(/Đ/g, 'D')
                          .trim().toLowerCase();
                        const root = trigger.closest('[data-slot="base"]');
                        if (!root) return false;
                        const select = [...root.querySelectorAll(
                          '[data-testid="hidden-select-container"] select')]
                          .find(item =>
                           [...item.options].some(option =>
                             normalize(option.textContent)
                               === normalize(expected)));
                        if (!select) return false;
                        const target = [...select.options].find(option =>
                          normalize(option.textContent)
                            === normalize(expected));
                        if (!target) return false;
                        [...select.options].forEach(option => {
                          option.selected = option === target;
                        });
                        select.dispatchEvent(
                          new Event('input', {bubbles: true}));
                        select.dispatchEvent(
                          new Event('change', {bubbles: true}));
                        return true;
                        """, trigger, value));
    }

    private void highlightForObservation(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("""
                arguments[0].style.outline = '3px solid #2563eb';
                arguments[0].style.outlineOffset = '-3px';
                """, element);
    }

    private void clickCalendarDay(LocalDate date) {
        String monthToken = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String dayClass = String.format(
                "react-datepicker__day--%03d", date.getDayOfMonth());
        new WebDriverWait(
                driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .until(d -> Boolean.TRUE.equals(((JavascriptExecutor) d)
                        .executeScript("""
                                const monthToken = arguments[0];
                                const dayClass = arguments[1];
                                const months = [...document.querySelectorAll(
                                  '.react-datepicker__month[aria-label]')];
                                const month = months.find(item =>
                                  (item.getAttribute('aria-label') || '')
                                    .includes(monthToken));
                                if (!month) return null;
                                const day = [...month.querySelectorAll(
                                  '.react-datepicker__day')].find(item =>
                                  item.classList.contains(dayClass)
                                  && !item.classList.contains(
                                    'react-datepicker__day--outside-month')
                                  && !item.classList.contains(
                                    'react-datepicker__day--disabled'));
                                if (!day) return null;
                                day.scrollIntoView({
                                  behavior: arguments[2] ? 'instant' : 'smooth',
                                  block: 'center', inline: 'nearest'
                                });
                                day.style.outline = '3px solid #2563eb';
                                return true;
                                """, monthToken, dayClass,
                                TestConfig.headless())));
        pauseForFilterObservation("Chon ngay " + date, 2);
        Boolean clicked = (Boolean) ((JavascriptExecutor) driver)
                .executeScript("""
                        const monthToken = arguments[0];
                        const dayClass = arguments[1];
                        const month = [...document.querySelectorAll(
                          '.react-datepicker__month[aria-label]')].find(item =>
                          (item.getAttribute('aria-label') || '')
                            .includes(monthToken));
                        if (!month) return false;
                        const day = [...month.querySelectorAll(
                          '.react-datepicker__day')].find(item =>
                          item.classList.contains(dayClass)
                          && !item.classList.contains(
                            'react-datepicker__day--outside-month')
                          && !item.classList.contains(
                            'react-datepicker__day--disabled'));
                        if (!day) return false;
                        day.click();
                        return true;
                        """, monthToken, dayClass);
        if (!Boolean.TRUE.equals(clicked)) {
            throw new IllegalStateException(
                    "Ngày " + date + " biến mất trước khi click.");
        }
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
            pauseLocally(Duration.ofMillis(250));
        }
    }

    private WebElement openFilter() {
        WebElement existing = visibleFilterPanel();
        if (existing != null) return existing;

        WebElement trigger = shortVisible(FILTER, Duration.ofSeconds(8));
        observe(trigger);
        existing = visibleFilterPanel();
        if (existing != null) return existing;

        WebElement freshTrigger = shortVisible(
                FILTER, Duration.ofSeconds(5));
        freshTrigger.click();
        return new WebDriverWait(driver, Duration.ofSeconds(8))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> visibleFilterPanel());
    }

    private WebElement visibleFilterPanel() {
        for (WebElement element : driver.findElements(By.cssSelector(
                "[data-slot='content'],[data-slot='popover'],[role='dialog']"))) {
            try {
                if (element.isDisplayed()
                        && normalized(element.getText())
                        .contains("tuy chon loc")) {
                    return element;
                }
            } catch (StaleElementReferenceException ignored) {
                // React đang thay popover; caller sẽ thử lại với DOM mới.
            }
        }
        return null;
    }

    private void closeFilterIfOpen() {
        WebElement trigger;
        try {
            trigger = shortVisible(FILTER, Duration.ofSeconds(5));
        } catch (TimeoutException ignored) {
            return;
        }
        if (visibleFilterPanel() != null
                || "true".equalsIgnoreCase(
                trigger.getAttribute("aria-expanded"))) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", trigger);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5))
                        .pollingEvery(Duration.ofMillis(200))
                        .ignoring(StaleElementReferenceException.class)
                        .until(d -> visibleFilterPanel() == null);
            } catch (TimeoutException ignored) {
                new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            }
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
        return openRow(row, true);
    }

    private DetailSnapshot openRow(WebElement row, boolean observation) {
        String id = row.getAttribute("data-key");
        List<WebElement> cells = row.findElements(By.cssSelector(
                "td[role='rowheader'],td[role='gridcell']"));
        currentRowStatus = cells.size() >= 2
                ? extractCurrentOrderStatus(cells.get(1).getText()) : "";
        if (observation) {
            observe(row);
            WebElement freshRow = driver.findElements(ROWS).stream()
                    .filter(element -> id.equals(
                            element.getAttribute("data-key")))
                    .findFirst().orElseThrow(() -> new IllegalStateException(
                            "Dòng đơn #" + id + " biến mất trước khi click."));
            freshRow.click();
        } else {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].click();", row);
        }
        WebElement drawer = wait.until(d -> {
            WebElement value = visibleDrawer();
            return value != null && value.getText().contains("Chi tiết đơn dịch vụ")
                    && value.getText().contains(id) ? value : null;
        });
        if (observation) {
            observe(drawer);
            pauseForDetailObservation(
                    "Da tai drawer chi tiet don #" + id + " - "
                            + TextNormalizer.normalize(currentRowStatus));
        }
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
        pauseLocally(Duration.ofMillis(1200));
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
        for (WebElement element : driver.findElements(DRAWER)) {
            try {
                String classes = element.getAttribute("class");
                if (element.isDisplayed()
                        && (classes == null || !classes.contains("translate-x-[100%]"))
                        && element.getText().contains("Chi tiết đơn dịch vụ")) {
                    return element;
                }
            } catch (StaleElementReferenceException ignored) {
                // React vừa thay drawer; vòng poll kế tiếp sẽ lấy element mới.
            }
        }
        return null;
    }

    private WebElement requiredDrawer() {
        WebElement drawer = visibleDrawer();
        if (drawer == null) throw new IllegalStateException("Drawer chi tiết chưa mở.");
        return drawer;
    }

    private WebElement visibleDialog() {
        for (WebElement element : driver.findElements(By.cssSelector(
                "[role='dialog'],[aria-modal='true']"))) {
            try {
                if (!element.isDisplayed()) continue;
                String ariaLabel = element.getAttribute("aria-label");
                if (!"drawer-Chi tiết đơn dịch vụ".equals(ariaLabel)) {
                    return element;
                }
            } catch (StaleElementReferenceException ignored) {
                // React vừa thay dialog; vòng poll kế tiếp sẽ lấy element mới.
            }
        }
        return null;
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
            pauseLocally(Duration.ofSeconds(2));
        }
    }

    private void pauseForDetailObservation(String step) {
        if (TestConfig.headless()) return;
        int seconds = 2;
        try {
            seconds = Math.max(0, Integer.parseInt(System.getProperty(
                    "customer.order.detail.pause.seconds", "2")));
        } catch (NumberFormatException ignored) {
            // Giữ mặc định 2 giây nếu giá trị cấu hình không hợp lệ.
        }
        if (seconds == 0) return;
        System.out.println("[QUAN SAT] " + step + " - giu man hinh "
                + seconds + " giay");
        pauseLocally(Duration.ofSeconds(seconds));
    }

    private void pauseForWorkflowObservation(String step) {
        if (TestConfig.headless()) return;
        int seconds = 2;
        try {
            seconds = Math.max(0, Integer.parseInt(System.getProperty(
                    "customer.order.workflow.pause.seconds", "2")));
        } catch (NumberFormatException ignored) {
            // Giữ mặc định 2 giây nếu cấu hình không hợp lệ.
        }
        if (seconds == 0) return;
        System.out.println("[QUAN SAT WORKFLOW] "
                + TextNormalizer.normalize(step) + " - giu man hinh "
                + seconds + " giay");
        pauseLocally(Duration.ofSeconds(seconds));
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
        pauseLocally(Duration.ofSeconds(seconds));
    }

    private void pauseForStatisticsObservation(String step) {
        if (TestConfig.headless()) return;
        int seconds = 3;
        try {
            seconds = Math.max(0, Integer.parseInt(System.getProperty(
                    "customer.order.statistics.pause.seconds", "3")));
        } catch (NumberFormatException ignored) {
            // Giữ mặc định 3 giây khi cấu hình không hợp lệ.
        }
        if (seconds == 0) return;
        System.out.println("[QUAN SAT] " + step + " - giu man hinh "
                + seconds + " giay");
        pauseLocally(Duration.ofSeconds(seconds));
    }

    private void pauseForViewObservation(String step) {
        if (TestConfig.headless()) return;
        int seconds = 5;
        try {
            seconds = Math.max(0, Integer.parseInt(System.getProperty(
                    "customer.order.view.pause.seconds", "5")));
        } catch (NumberFormatException ignored) {
            // Giữ mặc định 5 giây khi cấu hình không hợp lệ.
        }
        if (seconds == 0) return;
        System.out.println("[QUAN SAT] " + step + " - giu man hinh "
                + seconds + " giay");
        pauseLocally(Duration.ofSeconds(seconds));
    }

    private void pauseLocally(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Luồng testcase bị ngắt trong thời gian quan sát.", exception);
        }
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

    public record AdvanceQuoteSnapshot(
            String text, List<String> services, List<String> prices,
            List<String> buttons) {
    }

    public static final class OrderDataUnavailableException extends RuntimeException {
        public OrderDataUnavailableException(String message) {
            super(message);
        }

        public OrderDataUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
