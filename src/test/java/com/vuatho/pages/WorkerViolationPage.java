package com.vuatho.pages;

import com.vuatho.testdata.PartnerWorkerTestData;
import com.vuatho.config.TestConfig;
import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page object read-only cho menu Quan li tho vi pham. */
public class WorkerViolationPage {
    public static final String ROUTE = "/vuatho/violation-worker";
    public static final List<String> HEADERS = List.of(
            "ID", "Thông tin thợ", "Số lần vi phạm", "Tổng tiền phạt",
            "Số ngày xử phạt còn lại", "Trạng thái", "Lần xử lí gần nhất");

    private static final By SEARCH = By.cssSelector("input[aria-label='Tìm kiếm thợ']");
    private static final By SELECTS = By.cssSelector("select");
    private static final By DIALOGS = By.cssSelector("[role='dialog'], [aria-modal='true']");
    private static final Pattern NUMBER = Pattern.compile("([0-9][0-9.,]*)");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WorkerViolationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.wait.pollingEvery(Duration.ofMillis(250));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    public WorkerViolationPage openFromMenu() {
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(PartnerWorkerTestData.VIOLATION_WORKER, false);
        return waitUntilLoaded();
    }

    public WorkerViolationPage openDirectly() {
        String url = driver.getCurrentUrl();
        int index = url.indexOf("/vuatho/");
        String origin = index >= 0 ? url.substring(0, index) : url.replaceAll("/+$", "");
        driver.get(origin + ROUTE);
        return waitUntilLoaded();
    }

    public WorkerViolationPage waitUntilLoaded() {
        wait.until(d -> d.getCurrentUrl().contains(ROUTE));
        wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH));
        wait.until(d -> selectElements().size() >= 3);
        waitForResults();
        return this;
    }

    public boolean isLoaded() {
        return driver.getCurrentUrl().contains(ROUTE)
                && visible(SEARCH)
                && bodyText().contains("quan li tho vi pham")
                && hasExpectedHeaders();
    }

    public String bodyText() {
        return normalized(driver.findElement(By.tagName("body")).getText());
    }

    public boolean hasExpectedHeaders() {
        String text = bodyText();
        return HEADERS.stream().map(WorkerViolationPage::normalized).allMatch(text::contains);
    }

    public List<String> visibleHeaderLabels() {
        String text = bodyText();
        return HEADERS.stream().filter(header -> text.contains(normalized(header))).toList();
    }

    public boolean hasSummaryCards() {
        String text = bodyText();
        return text.contains("tho bi phat") && text.contains("tong tien phat") && text.contains("ti le thu hoi");
    }

    public long summaryValueAfter(String label) {
        String raw = driver.findElement(By.tagName("body")).getText();
        Pattern pattern = Pattern.compile(Pattern.quote(label) + "\\s*\\R?\\s*([0-9][0-9.,]*)",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(raw);
        if (!matcher.find()) {
            return -1;
        }
        return parseLong(matcher.group(1));
    }

    public double recoveryPercentage() {
        Matcher matcher = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)%").matcher(
                visibleTextContaining("Tỉ lệ thu hồi").orElse(""));
        return matcher.find() ? Double.parseDouble(matcher.group(1).replace(',', '.')) : -1;
    }

    public List<String> filterOptions(int index) {
        return new Select(select(index)).getOptions().stream().map(this::optionText).toList();
    }

    public String selectedFilter(int index) {
        return optionText(new Select(select(index)).getFirstSelectedOption());
    }

    public WorkerViolationPage selectFilter(int index, String label) {
        String before = resultFingerprint();
        Select control = new Select(select(index));
        WebElement option = control.getOptions().stream()
                .filter(item -> normalized(optionText(item)).equals(normalized(label)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Khong co tuy chon: " + label));
        // Native select bi an nen Chrome khong cho click option truc tiep.
        // Gan value va phat dung event de React cap nhat state nhu thao tac UI.
        ((JavascriptExecutor) driver).executeScript("""
                arguments[0].value = arguments[1];
                arguments[0].dispatchEvent(new Event('input', {bubbles: true}));
                arguments[0].dispatchEvent(new Event('change', {bubbles: true}));
                """, control.getWrappedElement(), option.getAttribute("value"));
        waitForResultChangeOrSettled(before);
        return this;
    }

    public String searchValue() {
        return driver.findElement(SEARCH).getAttribute("value");
    }

    public WorkerViolationPage search(String query) {
        String before = resultFingerprint();
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(SEARCH));
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), query);
        waitForResultChangeOrSettled(before);
        return this;
    }

    public WorkerViolationPage clearSearch() {
        return search("");
    }

    public WorkerViolationPage reset() {
        String before = resultFingerprint();
        WebElement button = firstVisible(By.cssSelector("button[aria-label='Reset'], button[title='Reset']"));
        if (button == null) {
            button = visibleButtons().stream()
                    .filter(item -> normalized(item.getAccessibleName()).equals("reset"))
                    .findFirst().orElse(null);
        }
        if (button == null) {
            throw new IllegalStateException("Khong tim thay nut Reset.");
        }
        click(button);
        waitForResultChangeOrSettled(before);
        return this;
    }

    /** Moi chuoi la toan bo noi dung mot dong, du de assert ma khong phu thuoc CSS noi bo. */
    public List<String> rowTexts() {
        List<String> rows = new ArrayList<>();
        for (WebElement grid : dataRows()) {
            try {
                String raw = grid.getText().trim();
                if (!raw.isBlank()) rows.add(raw);
            } catch (StaleElementReferenceException ignored) {
                return rowTexts();
            }
        }
        return rows.stream().distinct().toList();
    }

    public int displayedRowCount() {
        return rowTexts().size();
    }

    public long totalDisplayed() {
        Matcher matcher = Pattern.compile("tong hien thi\\s*:?\\s*([0-9.,]+)").matcher(bodyText());
        return matcher.find() ? parseLong(matcher.group(1)) : -1;
    }

    public Optional<RowSeed> firstRowSeed() {
        return rowTexts().stream().findFirst().map(raw -> {
            String[] lines = raw.split("\\R+");
            String id = lines.length == 0 ? "" : lines[0].replaceAll("\\D", "");
            Matcher phone = Pattern.compile("(?:\\+?84|0)[0-9 ]{8,12}").matcher(raw);
            Matcher name = Pattern.compile("(?i)H[ỌO] T[EÊ]N\\s*:\\s*([^\\r\\n]+)").matcher(raw);
            return new RowSeed(id, name.find() ? name.group(1).trim() : "", phone.find() ? phone.group().replace(" ", "") : "", raw);
        });
    }

    public boolean rowsContain(String query) {
        String expected = normalized(query).replace(" ", "");
        return rowTexts().stream().allMatch(row -> normalized(row).replace(" ", "").contains(expected));
    }

    public boolean hasEmptyState() {
        String text = bodyText();
        return displayedRowCount() == 0 && (totalDisplayed() == 0 || text.contains("khong co") || text.contains("khong tim thay"));
    }

    public List<Long> numericColumnValues(String header) {
        int columnIndex = HEADERS.indexOf(header);
        if (columnIndex < 0) return List.of();
        List<Long> values = new ArrayList<>();
        for (String row : rowTexts()) {
            String[] lines = row.split("\\R+");
            if (columnIndex < lines.length) {
                Matcher matcher = NUMBER.matcher(lines[columnIndex]);
                if (matcher.find()) values.add(parseLong(matcher.group(1)));
            }
        }
        return values;
    }

    public WorkerViolationPage sortBy(String header) {
        String before = resultFingerprint();
        WebElement button = visibleButtons().stream()
                .filter(item -> normalized(item.getText()).equals(normalized(header)))
                .findFirst().orElseThrow(() -> new IllegalStateException("Khong tim thay cot: " + header));
        click(button);
        waitForResultChangeOrSettled(before);
        return this;
    }

    public List<Integer> availablePages() {
        List<Integer> pages = new ArrayList<>();
        for (WebElement item : driver.findElements(By.cssSelector("[aria-label^='pagination item']"))) {
            Matcher matcher = Pattern.compile("pagination item\\s+(\\d+)").matcher(item.getAttribute("aria-label"));
            if (matcher.find()) pages.add(Integer.parseInt(matcher.group(1)));
        }
        return pages.stream().distinct().toList();
    }

    public int activePage() {
        WebElement active = firstVisible(By.cssSelector("[aria-label^='pagination item'][aria-label*='active']"));
        if (active == null) return 1;
        Matcher matcher = Pattern.compile("(\\d+)").matcher(active.getAttribute("aria-label"));
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 1;
    }

    public boolean previousDisabled() { return paginationDisabled("previous page button"); }
    public boolean nextDisabled() { return paginationDisabled("next page button"); }

    public WorkerViolationPage scrollToPagination() {
        WebElement pagination = paginationAnchor();
        if (pagination == null) throw new IllegalStateException("Khong tim thay khu vuc pagination de scroll.");
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior:'smooth', block:'center', inline:'nearest'});",
                pagination);
        wait.until(d -> {
            WebElement current = paginationAnchor();
            if (current == null) return false;
            Object visibleInViewport = ((JavascriptExecutor) d).executeScript("""
                    const rect = arguments[0].getBoundingClientRect();
                    return rect.top >= 0 && rect.bottom <= window.innerHeight;
                    """, current);
            return Boolean.TRUE.equals(visibleInViewport);
        });
        observePaginationStep("Da scroll den pagination");
        return this;
    }

    public WorkerViolationPage goToPage(int page) {
        scrollToPagination();
        String before = resultFingerprint();
        WebElement item = firstVisible(By.cssSelector("[aria-label^='pagination item " + page + "']"));
        if (item == null) throw new IllegalStateException("Trang " + page + " khong kha dung.");
        click(item);
        wait.until(d -> activePage() == page);
        waitForResultChangeOrSettled(before);
        scrollToPagination();
        return this;
    }

    public WorkerViolationPage nextPage() {
        return clickPagination("next page button");
    }

    public WorkerViolationPage previousPage() {
        return clickPagination("previous page button");
    }

    public WorkerViolationPage openStatistics() {
        WebElement button = visibleButtons().stream()
                .filter(item -> normalized(item.getText()).contains("xem chi tiet"))
                .findFirst().orElseThrow(() -> new IllegalStateException("Khong tim thay Xem chi tiet."));
        click(button);
        wait.until(d -> statisticsDialog() != null);
        observeStatisticsStep("Da mo popup thong ke");
        return this;
    }

    public String statisticsText() {
        WebElement dialog = statisticsDialog();
        return dialog == null ? "" : normalized(dialog.getText());
    }

    public boolean isStatisticsDialogOpen() {
        return statisticsDialog() != null;
    }

    public boolean statisticsHasPeriodControls() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return false;
        List<String> labels = dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(WorkerViolationPage::normalized)
                .toList();
        return labels.contains("tuan nay") && labels.contains("thang nay") && labels.contains("tuy chinh");
    }

    public Optional<StatisticsDateRange> statisticsDateRange() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return Optional.empty();
        Matcher matcher = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s*(?:→|–|-)\\s*(\\d{2}/\\d{2}/\\d{4})")
                .matcher(dialog.getText());
        if (!matcher.find()) return Optional.empty();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        return Optional.of(new StatisticsDateRange(
                LocalDate.parse(matcher.group(1), formatter),
                LocalDate.parse(matcher.group(2), formatter)));
    }

    public Optional<StatisticsMoney> statisticsMoney() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return Optional.empty();
        String raw = dialog.getText();
        Long total = moneyAfter(raw, "tong(?: tien phat)? phat sinh");
        Long collected = moneyAfter(raw, "da thu");
        Long uncollected = moneyAfter(raw, "chua thu");
        if (total == null || collected == null || uncollected == null) return Optional.empty();
        return Optional.of(new StatisticsMoney(total, collected, uncollected));
    }

    public boolean statisticsChartIsRendered() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return false;
        Object result = ((JavascriptExecutor) driver).executeScript("""
                const root = arguments[0];
                const explicit = root.querySelector('.recharts-wrapper, canvas, [class*="chart"]');
                if (explicit && explicit.getBoundingClientRect().width > 150
                    && explicit.getBoundingClientRect().height > 80) return true;
                return [...root.querySelectorAll('svg')].some(svg => {
                  const rect = svg.getBoundingClientRect();
                  return rect.width > 250 && rect.height > 100;
                });
                """, dialog);
        return Boolean.TRUE.equals(result);
    }

    public boolean statisticsCustomDateControlsVisible() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return false;
        String text = normalized(dialog.getText());
        boolean hasDateInput = dialog.findElements(By.cssSelector(
                        "input[type='date'], input[placeholder*='/'], [role='dialog'] input, [role='grid']"))
                .stream().anyMatch(WebElement::isDisplayed);
        return hasDateInput || text.contains("tu ngay") || text.contains("den ngay")
                || text.contains("chon ngay") || text.contains("ap dung");
    }

    public Optional<CustomDateState> statisticsCustomDateState() {
        List<WebElement> inputs = customDateInputs();
        if (inputs.size() != 2) return Optional.empty();
        try {
            return Optional.of(new CustomDateState(
                    parseIsoDate(inputs.get(0).getAttribute("value")),
                    parseIsoDate(inputs.get(1).getAttribute("value")),
                    parseIsoDate(inputs.get(0).getAttribute("min")),
                    parseIsoDate(inputs.get(0).getAttribute("max")),
                    parseIsoDate(inputs.get(1).getAttribute("min")),
                    parseIsoDate(inputs.get(1).getAttribute("max"))));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    public WorkerViolationPage setStatisticsCustomDateRange(LocalDate from, LocalDate to) {
        attemptStatisticsCustomDateRange(from, to);
        wait.until(d -> statisticsCustomDateState()
                .map(state -> from.equals(state.from()) && to.equals(state.to()))
                .orElse(false));
        return this;
    }

    public WorkerViolationPage attemptStatisticsCustomDateRange(LocalDate from, LocalDate to) {
        if (!statisticsCustomDateControlsVisible()) {
            throw new IllegalStateException("Control ngay tuy chinh chua hien thi.");
        }
        setDateInputValue(0, from);
        wait.until(d -> customDateInputs().size() == 2);
        setDateInputValue(1, to);
        observeStatisticsStep("Da nhap range " + from + " -> " + to);
        return this;
    }

    public boolean statisticsCustomApplyEnabled() {
        WebElement apply = statisticsCustomApplyButton();
        return apply != null && apply.isEnabled() && apply.getAttribute("disabled") == null
                && !"true".equalsIgnoreCase(apply.getAttribute("aria-disabled"));
    }

    public WorkerViolationPage applyStatisticsCustomDateRange() {
        WebElement apply = statisticsCustomApplyButton();
        if (apply == null) throw new IllegalStateException("Khong tim thay nut Ap dung range tuy chinh.");
        if (!statisticsCustomApplyEnabled()) throw new IllegalStateException("Nut Ap dung dang bi khoa.");
        click(apply);
        wait.until(d -> statisticsDialog() != null && !isLoading() && statisticsChartIsRendered());
        observeStatisticsStep("Da ap dung range tuy chinh");
        return this;
    }

    public List<String> statisticsChartDateLabels() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return List.of();
        return dialog.findElements(By.cssSelector(".recharts-xAxis .recharts-cartesian-axis-tick-value"))
                .stream().filter(WebElement::isDisplayed).map(WebElement::getText).map(String::trim)
                .filter(value -> value.matches("\\d{2}/\\d{2}"))
                .toList();
    }

    public boolean statisticsChartCovers(LocalDate from, LocalDate to) {
        List<String> labels = statisticsChartDateLabels();
        if (labels.isEmpty()) return false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        return labels.get(0).equals(from.format(formatter))
                && labels.get(labels.size() - 1).equals(to.format(formatter))
                && labels.size() == ChronoUnit.DAYS.between(from, to) + 1;
    }

    public boolean statisticsHasCoreContent() {
        String text = statisticsText();
        return text.contains("tien phat theo ngay") && text.contains("tuan nay") && text.contains("thang nay")
                && text.contains("tuy chinh") && text.contains("da thu") && text.contains("chua thu");
    }

    public WorkerViolationPage selectStatisticsPeriod(String label) {
        WebElement dialog = statisticsDialog();
        if (dialog == null) throw new IllegalStateException("Popup thong ke chua mo.");
        String beforeRange = statisticsDateRange().map(StatisticsDateRange::toString).orElse("");
        WebElement button = dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> normalized(item.getText()).equals(normalized(label)))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Khong co ky thong ke: " + label));
        click(button);
        wait.until(d -> statisticsDialog() != null && !isLoading());
        if (normalized(label).equals("tuy chinh")) {
            wait.until(d -> statisticsCustomDateControlsVisible());
        } else {
            wait.until(d -> statisticsDateRange().isPresent());
            String afterRange = statisticsDateRange().map(StatisticsDateRange::toString).orElse("");
            if (!beforeRange.isBlank() && normalized(label).equals("thang nay")) {
                wait.until(d -> !afterRange.equals(beforeRange)
                        || statisticsDateRange().map(StatisticsDateRange::inclusiveDays).orElse(0L) >= 28);
            }
        }
        observeStatisticsStep("Da chon " + label);
        return this;
    }

    /** Giu man hinh o moi buoc de nguoi chay co thoi gian quan sat du lieu. */
    private void observeStatisticsStep(String step) {
        if (TestConfig.headless()) return;
        long delayMillis;
        try {
            delayMillis = Long.parseLong(System.getProperty("statistics.step.delay.ms", "4000"));
        } catch (NumberFormatException ignored) {
            delayMillis = 4000;
        }
        delayMillis = Math.max(0, Math.min(delayMillis, 30_000));
        if (delayMillis == 0) return;
        System.out.println("[QUAN SAT] " + step + " - dung " + delayMillis + "ms");
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bi gian doan khi dung quan sat popup thong ke.", exception);
        }
    }

    public void closeDialog() {
        WebElement dialog = firstVisible(DIALOGS);
        if (dialog == null) return;
        List<WebElement> close = dialog.findElements(
                        By.cssSelector("button[aria-label='Close'], button[aria-label='Dismiss']"))
                .stream().filter(WebElement::isDisplayed).toList();
        if (!close.isEmpty()) click(close.get(0)); else dialog.sendKeys(Keys.ESCAPE);
        wait.until(d -> firstVisible(DIALOGS) == null);
    }

    public boolean openFirstWorkerConfirmation() {
        WebElement row = firstDataGrid();
        if (row == null) return false;
        click(row);
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> firstVisible(DIALOGS) != null);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public String dialogText() {
        WebElement dialog = firstVisible(DIALOGS);
        return dialog == null ? "" : normalized(dialog.getText());
    }

    public boolean clickDialogAction(String label) {
        WebElement dialog = firstVisible(DIALOGS);
        if (dialog == null) return false;
        WebElement button = dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> normalized(item.getText()).equals(normalized(label)))
                .findFirst().orElse(null);
        if (button == null) return false;
        click(button);
        return true;
    }

    public WorkerViolationPage refresh() {
        driver.navigate().refresh();
        return waitUntilLoaded();
    }

    private WorkerViolationPage clickPagination(String aria) {
        scrollToPagination();
        String before = resultFingerprint();
        WebElement control = firstVisible(By.cssSelector("[aria-label='" + aria + "']"));
        if (control == null) throw new IllegalStateException("Khong tim thay pagination: " + aria);
        click(control);
        waitForResultChangeOrSettled(before);
        scrollToPagination();
        return this;
    }

    private WebElement paginationAnchor() {
        WebElement active = firstVisible(By.cssSelector("[aria-label^='pagination item'][aria-label*='active']"));
        if (active != null) return active;
        WebElement next = firstVisible(By.cssSelector("[aria-label='next page button']"));
        return next != null ? next : firstVisible(By.cssSelector("[aria-label^='pagination item']"));
    }

    private void observePaginationStep(String step) {
        if (TestConfig.headless()) return;
        long delayMillis;
        try {
            delayMillis = Long.parseLong(System.getProperty("pagination.step.delay.ms", "3000"));
        } catch (NumberFormatException ignored) {
            delayMillis = 3000;
        }
        delayMillis = Math.max(0, Math.min(delayMillis, 30_000));
        if (delayMillis == 0) return;
        System.out.println("[QUAN SAT PHAN TRANG] " + step + " - dung " + delayMillis + "ms");
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bi gian doan khi quan sat pagination.", exception);
        }
    }

    private boolean paginationDisabled(String aria) {
        WebElement item = firstVisible(By.cssSelector("[aria-label='" + aria + "']"));
        if (item == null) return true;
        return !item.isEnabled() || "true".equals(item.getAttribute("aria-disabled"))
                || normalized(item.getAttribute("class")).contains("disabled");
    }

    private WebElement select(int index) {
        List<WebElement> items = selectElements();
        if (items.size() <= index) throw new IllegalStateException("Thieu bo loc thu " + index);
        return items.get(index);
    }

    private List<WebElement> selectElements() {
        // Component select ve nut rieng va giu native <select> an trong DOM.
        // Native control moi la nguon option/value on dinh de test, khong loc isDisplayed().
        return driver.findElements(SELECTS);
    }

    private WebElement statisticsDialog() {
        return driver.findElements(DIALOGS).stream().filter(WebElement::isDisplayed)
                .filter(item -> normalized(item.getText()).contains("tien phat theo ngay"))
                .findFirst().orElse(null);
    }

    private List<WebElement> customDateInputs() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return List.of();
        return dialog.findElements(By.cssSelector("input[type='date']")).stream()
                .filter(WebElement::isDisplayed).toList();
    }

    private WebElement statisticsCustomApplyButton() {
        WebElement dialog = statisticsDialog();
        if (dialog == null) return null;
        return dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> normalized(button.getText()).equals("ap dung"))
                .findFirst().orElse(null);
    }

    private void setDateInputValue(int index, LocalDate value) {
        List<WebElement> inputs = customDateInputs();
        if (inputs.size() <= index) throw new IllegalStateException("Thieu input ngay thu " + index);
        ((JavascriptExecutor) driver).executeScript("""
                const input = arguments[0];
                const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
                setter.call(input, arguments[1]);
                input.dispatchEvent(new Event('input', {bubbles: true}));
                input.dispatchEvent(new Event('change', {bubbles: true}));
                """, inputs.get(index), value.toString());
    }

    private static LocalDate parseIsoDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private WebElement firstDataGrid() {
        return dataRows().stream().findFirst().orElse(null);
    }

    /**
     * Tim cell danh tinh theo nhan nghiep vu, sau do leo len ancestor nho nhat co du
     * thong tin vi pham. Cach nay khong phu thuoc class Tailwind sinh dong.
     */
    private List<WebElement> dataRows() {
        String script = """
                const compact = value => (value || '').replace(/\\s+/g, ' ').trim();
                const identities = [...document.querySelectorAll('div')].filter(element => {
                  const text = compact(element.innerText).toUpperCase();
                  return element.offsetParent !== null && text.length < 350
                    && (text.includes('HỌ TÊN') || text.includes('HO TEN'))
                    && (text.includes('SĐT') || text.includes('SDT'));
                });
                const rows = [];
                for (const identity of identities) {
                  let node = identity;
                  for (let level = 0; node && level < 6; level++, node = node.parentElement) {
                    const text = compact(node.innerText).toUpperCase();
                    const violation = /[0-9]+\\s*LẦN/.test(text) || /[0-9]+\\s*LAN/.test(text);
                    const state = text.includes('ĐANG BỊ PHẠT') || text.includes('DA GO HET')
                      || text.includes('ĐÃ GỠ HẾT') || text.includes('VĨNH VIỄN') || text.includes('VINH VIEN');
                    if (violation && state && text.length < 1000) {
                      if (!rows.includes(node)) rows.push(node);
                      break;
                    }
                  }
                }
                return rows;
                """;
        Object result = ((JavascriptExecutor) driver).executeScript(script);
        if (!(result instanceof List<?> values)) return List.of();
        return values.stream().filter(WebElement.class::isInstance).map(WebElement.class::cast).toList();
    }

    private String optionText(WebElement option) {
        String value = option.getDomProperty("textContent");
        return value == null ? "" : value.trim();
    }

    private void waitForResultChangeOrSettled(String before) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3)).pollingEvery(Duration.ofMillis(100))
                    .until(d -> isLoading() || !resultFingerprint().equals(before));
        } catch (RuntimeException ignored) {
            // A valid filter can return exactly the same first page; the settled-state wait below is authoritative.
        }
        waitForResults();
    }

    private void waitForResults() {
        wait.until(d -> !isLoading());
        // Footer Tong hien thi co the bi an khi search chi con mot ban ghi.
        // Header + loading ket thuc la contract on dinh cho ca list, one-result va empty state.
        wait.until(d -> visible(SEARCH) && hasExpectedHeaders());
    }

    private boolean isLoading() {
        String text = bodyText();
        return text.contains("dang tai du lieu") || driver.findElements(By.cssSelector("[role='progressbar']"))
                .stream().anyMatch(WebElement::isDisplayed);
    }

    private String resultFingerprint() {
        return activePage() + "|" + totalDisplayed() + "|" + rowTexts().stream().findFirst().orElse("");
    }

    private Optional<String> visibleTextContaining(String value) {
        String expected = normalized(value);
        return driver.findElements(By.xpath("//*[self::div or self::section]"))
                .stream().filter(WebElement::isDisplayed).map(WebElement::getText)
                .filter(text -> normalized(text).contains(expected)).min((a, b) -> Integer.compare(a.length(), b.length()));
    }

    private List<WebElement> visibleButtons() {
        return driver.findElements(By.tagName("button")).stream().filter(WebElement::isDisplayed).toList();
    }

    private WebElement firstVisible(By by) {
        return driver.findElements(by).stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
    }

    private boolean visible(By by) { return firstVisible(by) != null; }

    private void click(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (RuntimeException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", element);
        }
    }

    private static String normalized(String value) {
        return TextNormalizer.normalize(value).replaceAll("\\s+", " ");
    }

    private static long parseLong(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        return digits.isBlank() ? 0 : Long.parseLong(digits);
    }

    private static Long moneyAfter(String raw, String normalizedLabelRegex) {
        String text = normalized(raw);
        Matcher matcher = Pattern.compile(normalizedLabelRegex + "\\s*:?\\s*([0-9][0-9.,]*)\\s*d?")
                .matcher(text);
        return matcher.find() ? parseLong(matcher.group(1)) : null;
    }

    public record RowSeed(String id, String name, String phone, String rawText) { }

    public record StatisticsDateRange(LocalDate from, LocalDate to) {
        public long inclusiveDays() {
            return ChronoUnit.DAYS.between(from, to) + 1;
        }
    }

    public record StatisticsMoney(long total, long collected, long uncollected) { }

    public record CustomDateState(
            LocalDate from,
            LocalDate to,
            LocalDate fromMin,
            LocalDate fromMax,
            LocalDate toMin,
            LocalDate toMax) { }
}
