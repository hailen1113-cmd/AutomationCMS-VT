package com.vuatho.pages;

import com.vuatho.utils.Waits;
import com.vuatho.config.TestConfig;
import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/** Page Object riêng cho tab Tất cả của màn hình Lịch sử giao dịch. */
public class TransactionHistoryPage extends UniformUiPage {
    private static final String ROUTE = "/vuatho/transaction?tab=all";
    private static final By GRID = By.cssSelector(
            "[role='grid'][aria-label='Table about Transaction Management']");
    private static final By DATA_ROWS = By.cssSelector("tbody tr");
    private static final DateTimeFormatter ROW_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public TransactionHistoryPage(WebDriver driver) {
        super(driver);
    }

    public void openAllTab() {
        if (driver.getCurrentUrl().contains("/vuatho/transaction")
                && driver.getCurrentUrl().contains("tab=all")
                && !driver.getCurrentUrl().contains("id=")
                && !visibleElements(By.cssSelector("button[title='Reset']")).isEmpty()) {
            resetFilters();
            return;
        }
        openRoute(ROUTE);
        waitForTable();
    }

    public OverviewSnapshot overview() {
        List<String> tabs = List.of("Tất cả", "Tiền nạp", "Tiền rút", "Đơn dịch vụ",
                "Thưởng & KM", "Phí & Doanh thu", "VT Care", "Thợ phụ", "Hệ thống");
        List<String> controls = List.of("Chọn loại giao dịch", "Chọn trạng thái",
                "Chọn cổng thanh toán", "Chọn khoảng ngày giờ", "Xuất Excel");
        return new OverviewSnapshot(driver.getCurrentUrl(), isAllTabSelected(),
                tabs.stream().filter(this::visibleButtonText).toList(),
                controls.stream().filter(this::visibleButtonText).toList(), headers(), rows(),
                totalDisplayed(), paginationVisible());
    }

    public List<String> headers() {
        return grid().findElements(By.cssSelector("thead th"))
                .stream().filter(WebElement::isDisplayed)
                .map(this::elementText).map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    public List<TransactionRow> rows() {
        waitForTable();
        return currentRows();
    }

    private List<TransactionRow> currentRows() {
        if (isEmptyState()) {
            return List.of();
        }
        WebDriverWait stableRows = Waits.withTimeout(driver, Duration.ofSeconds(10));
        return stableRows.until(d -> {
            if (isEmptyState()) {
                return List.of();
            }
            try {
                // React thay toàn bộ tbody khi API/pagination hoàn tất. Luôn lấy lại grid và
                // từng row trong mỗi lần poll để không sử dụng WebElement của render trước.
                List<TransactionRow> result = new ArrayList<>();
                for (WebElement element : grid().findElements(DATA_ROWS)) {
                    if (!element.isDisplayed()) {
                        continue;
                    }
                    TransactionRow row = toRow(element);
                    if (row != null) {
                        result.add(row);
                    }
                }
                return result.isEmpty() ? null : List.copyOf(result);
            } catch (StaleElementReferenceException ignored) {
                return null;
            }
        });
    }

    public List<String> filterOptions(Filter filter) {
        openFilter(filter);
        List<String> options = wait.until(d -> {
            List<String> values = visibleElements(By.cssSelector("li[role='option']")).stream()
                    .map(this::elementText).map(String::trim).filter(s -> !s.isBlank()).toList();
            return values.isEmpty() ? null : values;
        });
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        return options;
    }

    public FilterSnapshot filterByFirstRow(Filter filter) {
        TransactionRow source = firstRowWithValue(filter);
        String value = valueOf(source, filter);
        selectFilter(filter, value);
        List<TransactionRow> filtered = rows();
        return new FilterSnapshot(filter, value, filtered, selectedFilterText(filter),
                driver.getCurrentUrl());
    }

    public CombinedFilterSnapshot combineFiltersFromOneRow() {
        TransactionRow source = rows().stream()
                .filter(row -> !row.type().isBlank() && !row.status().isBlank() && !row.gateway().isBlank())
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Tab Tất cả không có dòng đủ loại, trạng thái và cổng thanh toán."));
        selectFilter(Filter.TYPE, source.type());
        selectFilter(Filter.STATUS, source.status());
        selectFilter(Filter.GATEWAY, source.gateway());
        return new CombinedFilterSnapshot(source, rows(),
                selectedFilterText(Filter.TYPE), selectedFilterText(Filter.STATUS),
                selectedFilterText(Filter.GATEWAY));
    }

    public SpecializedCombinedFilterSnapshot combineStatusAndFirstGateway() {
        TransactionRow source = firstRowWithValue(Filter.STATUS);
        String gateway = filterOptions(Filter.GATEWAY).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Bộ lọc không có cổng thanh toán."));
        selectFilter(Filter.STATUS, source.status());
        selectFilter(Filter.GATEWAY, gateway);
        List<TransactionRow> filtered = rows();
        return new SpecializedCombinedFilterSnapshot(source.status(), gateway, filtered,
                selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                isEmptyState(), mainText());
    }

    public SpecializedCombinedFilterSnapshot combineStatusAndGateway(String status, String gateway) {
        selectFilter(Filter.STATUS, status);
        selectFilter(Filter.GATEWAY, gateway);
        List<TransactionRow> filtered = rows();
        return new SpecializedCombinedFilterSnapshot(status, gateway, filtered,
                selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                isEmptyState(), mainText());
    }

    public CombinedFilterMatrixSnapshot applyEveryStatusGatewayCombination() {
        List<String> statuses = filterOptions(Filter.STATUS);
        List<String> gateways = filterOptions(Filter.GATEWAY);
        List<CombinedFilterOptionResult> results = new ArrayList<>();
        for (String status : statuses) {
            selectFilter(Filter.STATUS, status);
            for (String gateway : gateways) {
                selectFilter(Filter.GATEWAY, gateway);
                // selectFilter() đã chờ bảng ổn định; đọc ngay dữ liệu hiện tại để
                // tránh lặp thêm một chu kỳ wait cho từng tổ hợp.
                List<TransactionRow> filtered = currentRows();
                results.add(new CombinedFilterOptionResult(status, gateway,
                        selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                        filtered, isEmptyState(), mainText()));
            }
            resetFilters();
        }
        return new CombinedFilterMatrixSnapshot(statuses, gateways, results,
                driver.getCurrentUrl());
    }

    public SpecializedCombinedDateFilterSnapshot combineStatusGatewayAndDate(
            String status, String gateway) {
        TransactionRow source = rows().stream()
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Không có dòng để lấy ngày kết hợp bộ lọc."));
        LocalDate date = source.createdAt().toLocalDate();
        try {
            applyDateTimeRange(date, date, null, null);
        } catch (TimeoutException exception) {
            throw new IllegalStateException("Bảng không ổn định sau khi lọc ngày " + date, exception);
        }
        try {
            selectFilter(Filter.STATUS, status);
        } catch (TimeoutException exception) {
            throw new IllegalStateException(
                    "Bảng không ổn định sau khi lọc trạng thái " + status, exception);
        }
        try {
            selectFilter(Filter.GATEWAY, gateway);
        } catch (TimeoutException exception) {
            throw new IllegalStateException(
                    "Bảng không ổn định sau khi lọc cổng thanh toán " + gateway, exception);
        }
        List<TransactionRow> filtered = rows();
        return new SpecializedCombinedDateFilterSnapshot(source, status, gateway, date, filtered,
                selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                dateButton().getText().trim(), isEmptyState(), mainText(), driver.getCurrentUrl());
    }

    public SpecializedResetSnapshot resetStatusGatewayAndDate(String status, String gateway) {
        combineStatusGatewayAndDate(status, gateway);
        resetFilters();
        return new SpecializedResetSnapshot(selectedFilterText(Filter.STATUS),
                selectedFilterText(Filter.GATEWAY), dateButton().getText().trim(),
                activePage(), rows().size(), driver.getCurrentUrl());
    }

    public SpecializedHiddenFilterSnapshot specializedIrrelevantFiltersAreHidden() {
        return new SpecializedHiddenFilterSnapshot(
                visibleElements(By.cssSelector("button[aria-label='loại giao dịch-filter']")).isEmpty(),
                visibleElements(By.cssSelector("button[aria-label='xuất hoá đơn-filter']")).isEmpty(),
                visibleElements(By.cssSelector("button[aria-label='thời hạn BH-filter']")).isEmpty());
    }

    public FutureDateSnapshot futureDatesAreDisabled() {
        click(dateButton(), "Mở bộ lọc ngày giờ");
        String aria = calendarAria(LocalDate.now().plusDays(1), false);
        List<WebElement> dates = driver.findElements(By.cssSelector("[aria-label=" + cssString(aria) + "]"));
        boolean disabled = dates.stream().anyMatch(element -> "true".equals(element.getAttribute("aria-disabled")));
        int disabledCount = driver.findElements(By.cssSelector("[aria-disabled='true'][role='option']")).size();
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        return new FutureDateSnapshot(aria, disabled, disabledCount);
    }

    public DateFilterSnapshot filterRecentRange() {
        TransactionRow source = rows().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Không có giao dịch để lấy ngày lọc."));
        LocalDate end = source.createdAt().toLocalDate();
        LocalDate start = end.minusDays(1);
        click(dateButton(), "Mở bộ lọc ngày giờ");
        clickCalendarDate(start, "Chọn ngày bắt đầu");
        clickCalendarDate(end, "Chọn ngày kết thúc");
        clickStable(By.xpath("//button[normalize-space()='Áp dụng']"), "Áp dụng khoảng ngày");
        waitForTable();
        return new DateFilterSnapshot(start, end, rows(), dateButton().getText().trim(),
                isEmptyState(), mainText());
    }

    public EmptySnapshot findEmptyCombination() {
        List<String> types = filterOptions(Filter.TYPE);
        List<String> gateways = filterOptions(Filter.GATEWAY);
        for (String type : types) {
            for (String gateway : gateways) {
                selectFilter(Filter.TYPE, type);
                selectFilter(Filter.GATEWAY, gateway);
                if (isEmptyState()) {
                    return new EmptySnapshot(type, gateway, mainText(), true);
                }
                resetFilters();
            }
        }
        throw new IllegalStateException("Không tìm được tổ hợp loại giao dịch/cổng thanh toán không có dữ liệu.");
    }

    public ResetSnapshot resetAfterFilters() {
        TransactionRow source = firstRowWithValue(Filter.STATUS);
        selectFilter(Filter.STATUS, source.status());
        if (hasPage(2)) {
            goToPage(2);
        }
        resetFilters();
        return new ResetSnapshot(isAllTabSelected(), selectedFilterText(Filter.TYPE),
                selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                dateButton().getText().trim(), activePage(), rows().size(), driver.getCurrentUrl());
    }

    public ResetSnapshot resetEmptyState() {
        findEmptyCombination();
        resetFilters();
        return new ResetSnapshot(isAllTabSelected(), selectedFilterText(Filter.TYPE),
                selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                dateButton().getText().trim(), activePage(), rows().size(), driver.getCurrentUrl());
    }

    public FilterChangeSnapshot changeStatusFilter() {
        List<String> options = filterOptions(Filter.STATUS);
        if (options.size() < 2) {
            throw new IllegalStateException("Bộ lọc trạng thái cần ít nhất hai lựa chọn.");
        }
        selectFilter(Filter.STATUS, options.get(0));
        List<TransactionRow> firstRows = rows();
        selectFilter(Filter.STATUS, options.get(1));
        return new FilterChangeSnapshot(options.get(0), options.get(1), firstRows, rows(),
                selectedFilterText(Filter.STATUS));
    }

    public FilterChangeSnapshot changeGatewayFilter() {
        List<String> options = filterOptions(Filter.GATEWAY);
        if (options.size() < 2) {
            throw new IllegalStateException("Bộ lọc cổng thanh toán cần ít nhất hai lựa chọn.");
        }
        selectFilter(Filter.GATEWAY, options.get(0));
        List<TransactionRow> firstRows = rows();
        selectFilter(Filter.GATEWAY, options.get(1));
        return new FilterChangeSnapshot(options.get(0), options.get(1), firstRows, rows(),
                selectedFilterText(Filter.GATEWAY));
    }

    public FilterOptionsSnapshot applyEveryFilterOption(Filter filter) {
        List<String> options = filterOptions(filter);
        List<FilterOptionResult> results = new ArrayList<>();
        for (String option : options) {
            selectFilter(filter, option);
            List<TransactionRow> filtered = rows();
            results.add(new FilterOptionResult(option, selectedFilterText(filter), filtered,
                    isEmptyState(), mainText()));
            resetFilters();
        }
        return new FilterOptionsSnapshot(filter, options, results);
    }

    public FilterPopupSemanticsSnapshot filterPopupSemantics(Filter filter) {
        WebElement trigger = filterButton(filter);
        String expandedBefore = String.valueOf(trigger.getAttribute("aria-expanded"));
        String hasPopup = String.valueOf(trigger.getAttribute("aria-haspopup"));
        click(trigger, "Mở semantics " + filter.label);
        FilterPopupSemanticsSnapshot snapshot = wait.until(d -> {
            try {
                WebElement currentListbox = d.findElements(By.cssSelector("ul[role='listbox']"))
                        .stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
                if (currentListbox == null) {
                    return null;
                }
                List<String> currentOptions = currentListbox
                        .findElements(By.cssSelector("li[role='option']"))
                        .stream().filter(WebElement::isDisplayed).map(this::elementText)
                        .map(String::trim).filter(value -> !value.isBlank()).toList();
                return currentOptions.isEmpty() ? null : new FilterPopupSemanticsSnapshot(
                        filter, expandedBefore,
                        String.valueOf(filterButton(filter).getAttribute("aria-expanded")),
                        hasPopup, String.valueOf(currentListbox.getAttribute("role")),
                        String.valueOf(currentListbox.getAttribute("aria-labelledby")),
                        currentOptions);
            } catch (StaleElementReferenceException ignored) {
                return null;
            }
        });
        // React Aria giữ portal listbox trong DOM dù Escape/toggle đã được gửi. Refresh route
        // sau khi chụp semantics để lần đọc popup kế tiếp không nhầm portal của filter trước.
        driver.navigate().refresh();
        waitForTable();
        return snapshot;
    }

    public CalendarNavigationSnapshot calendarPreviousMonthUpdatesBothPanels() {
        click(dateButton(), "Mở popup lịch hai tháng");
        By months = By.cssSelector("[role='listbox'][aria-label^='month']");
        List<String> before = visibleElements(months).stream()
                .map(element -> element.getAttribute("aria-label")).toList();
        clickStable(By.cssSelector("button[aria-label='Previous Month']"),
                "Đi tới tháng trước trong popup ngày");
        List<String> after = wait.until(d -> {
            List<String> labels = visibleElements(months).stream()
                    .map(element -> element.getAttribute("aria-label")).toList();
            return labels.size() >= 2 && !labels.equals(before) ? labels : null;
        });
        int timeInputs = visibleElements(By.cssSelector("input[type='time']")).size();
        String popupText = visibleElements(By.cssSelector("[role='dialog']")).stream()
                .map(this::elementText).collect(Collectors.joining(" "));
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        return new CalendarNavigationSnapshot(before, after, timeInputs, popupText);
    }

    public DateFilterSnapshot filterAcrossVisibleMonths() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.withDayOfMonth(1).minusDays(1);
        applyDateTimeRange(start, end, null, null);
        return new DateFilterSnapshot(start, end, rows(), dateButton().getText().trim(),
                isEmptyState(), mainText());
    }

    public InvalidTimeRangeSnapshot invalidTimeRangeCannotApply() {
        LocalDate date = rows().get(0).createdAt().toLocalDate();
        click(dateButton(), "Mở popup để kiểm tra khoảng giờ không hợp lệ");
        clickCalendarDate(date, "Chọn ngày bắt đầu cho khoảng giờ không hợp lệ");
        clickCalendarDate(date, "Chọn ngày kết thúc cho khoảng giờ không hợp lệ");
        List<WebElement> inputs = visibleElements(By.cssSelector("input[type='time']"));
        setTimeInput(inputs.get(0), "23:59");
        setTimeInput(inputs.get(1), "00:00");
        settle(300);
        boolean applyEnabled = dateApplyButton().isEnabled();
        String popupText = visibleElements(By.cssSelector("[role='dialog']")).stream()
                .map(this::elementText).collect(Collectors.joining(" "));
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        return new InvalidTimeRangeSnapshot(date, "23:59", "00:00", applyEnabled, popupText);
    }

    public CombinedSearchFilterSnapshot combineSearchStatusGatewayAndDateFromSource() {
        TransactionRow source = rows().stream()
                .filter(row -> !row.status().isBlank())
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Không có dòng System đủ trạng thái để kết hợp bộ lọc."));
        String user = cell(source.cells(), headers(), "Người dùng");
        String query = searchQueryFromUser(user);
        fill(visible(By.cssSelector("[aria-label='search-name-phone-filter']")), query,
                "Tìm người dùng trước khi kết hợp bộ lọc System");
        waitForTable();
        LocalDate date = source.createdAt().toLocalDate();
        applyDateTimeRange(date, date, null, null);
        selectFilter(Filter.STATUS, source.status());
        String gateway = filterOptions(Filter.GATEWAY).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Bộ lọc System không có cổng."));
        selectFilter(Filter.GATEWAY, gateway);
        return captureCombinedSearchFilter(source, query, date);
    }

    public CombinedSearchFilterSnapshot resetCombinedSearchStatusGatewayAndDate() {
        CombinedSearchFilterSnapshot applied = combineSearchStatusGatewayAndDateFromSource();
        resetFilters();
        return new CombinedSearchFilterSnapshot(applied.source(),
                visible(By.cssSelector("[aria-label='search-name-phone-filter']")).getAttribute("value"),
                applied.date(),
                selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                dateButton().getText().trim(), rows(), driver.getCurrentUrl(), true,
                isEmptyState(), mainText());
    }

    public CombinedSearchFilterSnapshot combinedFiltersPersistAfterDetail() {
        TransactionRow source = rows().stream().filter(row -> !row.status().isBlank())
                .findFirst().orElseThrow();
        String user = cell(source.cells(), headers(), "Người dùng");
        String query = searchQueryFromUser(user);
        fill(visible(By.cssSelector("[aria-label='search-name-phone-filter']")), query,
                "Tìm người dùng trước khi kiểm tra persistence");
        waitForTable();
        LocalDate date = source.createdAt().toLocalDate();
        applyDateTimeRange(date, date, null, null);
        selectFilter(Filter.STATUS, source.status());
        CombinedSearchFilterSnapshot applied = captureCombinedSearchFilter(source, query, date);
        closeFirstDetail();
        CombinedSearchFilterSnapshot after = captureCombinedSearchFilter(
                applied.source(), applied.query(), applied.date());
        return new CombinedSearchFilterSnapshot(after.source(), after.query(),
                after.date(),
                after.selectedStatus(), after.selectedGateway(), after.selectedDate(),
                after.rows(), after.url(), applied.rows().equals(after.rows()),
                after.empty(), after.pageText());
    }

    private CombinedSearchFilterSnapshot captureCombinedSearchFilter(
            TransactionRow source, String query, LocalDate date) {
        return new CombinedSearchFilterSnapshot(source,
                visible(By.cssSelector("[aria-label='search-name-phone-filter']"))
                        .getAttribute("value"),
                date,
                selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                dateButton().getText().trim(), rows(), driver.getCurrentUrl(), false,
                isEmptyState(), mainText());
    }

    private String searchQueryFromUser(String user) {
        String digits = user.replaceAll("[^0-9]", "");
        return digits.length() >= 6 ? digits.substring(digits.length() - 6) : user.trim();
    }

    public UnchangedFilterSnapshot dismissFilterWithoutSelection(Filter filter) {
        List<String> before = rows().stream().map(TransactionRow::signature).toList();
        String selectedBefore = selectedFilterText(filter);
        openFilter(filter);
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        settle(400);
        return new UnchangedFilterSnapshot(selectedBefore, selectedFilterText(filter),
                before, rows().stream().map(TransactionRow::signature).toList());
    }

    public DateControlSnapshot dateControlDefaults() {
        click(dateButton(), "Mở bộ lọc ngày giờ");
        List<WebElement> inputs = wait.until(d -> {
            List<WebElement> visibleInputs = visibleElements(By.cssSelector("input[type='time']"));
            return visibleInputs.size() >= 2 ? visibleInputs : null;
        });
        DateControlSnapshot snapshot = new DateControlSnapshot(
                inputs.get(0).getAttribute("value"), inputs.get(1).getAttribute("value"),
                !dateApplyButton().isEnabled());
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        return snapshot;
    }

    public DateRequirementSnapshot dateApplyRequiresSelection() {
        click(dateButton(), "Mở bộ lọc ngày giờ");
        boolean initiallyDisabled = !dateApplyButton().isEnabled();
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        return new DateRequirementSnapshot(initiallyDisabled);
    }

    public DateTimeFilterSnapshot filterSingleDay() {
        TransactionRow source = rows().get(0);
        LocalDate date = source.createdAt().toLocalDate();
        applyDateTimeRange(date, date, null, null);
        return new DateTimeFilterSnapshot(date, date, LocalTime.MIN,
                LocalTime.of(23, 59), rows(), dateButton().getText().trim());
    }

    public DateTimeFilterSnapshot filterSourceMinute() {
        TransactionRow source = rows().get(0);
        LocalDate date = source.createdAt().toLocalDate();
        LocalTime start = source.createdAt().toLocalTime().withSecond(0).withNano(0);
        LocalTime end = start.equals(LocalTime.of(23, 59))
                ? start : start.plusMinutes(1);
        if (start.equals(end)) {
            start = start.minusMinutes(1);
        }
        applyDateTimeRange(date, date, start, end);
        return new DateTimeFilterSnapshot(date, date, start, end,
                rows(), dateButton().getText().trim());
    }

    public UnchangedFilterSnapshot dismissDateWithoutApply() {
        List<String> before = rows().stream().map(TransactionRow::signature).toList();
        String selectedBefore = dateButton().getText().trim();
        TransactionRow source = rows().get(0);
        click(dateButton(), "Mở bộ lọc ngày giờ");
        clickCalendarDate(source.createdAt().toLocalDate(), "Chọn một ngày nhưng chưa áp dụng");
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        settle(400);
        return new UnchangedFilterSnapshot(selectedBefore, dateButton().getText().trim(),
                before, rows().stream().map(TransactionRow::signature).toList());
    }

    public CombinedDateFilterSnapshot combineDateAndSelectFilters() {
        TransactionRow source = rows().stream()
                .filter(row -> !row.type().isBlank() && !row.status().isBlank()
                        && !row.gateway().isBlank())
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Không có dòng đủ dữ liệu để kết hợp bộ lọc."));
        LocalDate date = source.createdAt().toLocalDate();
        applyDateTimeRange(date, date, null, null);
        selectFilter(Filter.TYPE, source.type());
        selectFilter(Filter.STATUS, source.status());
        selectFilter(Filter.GATEWAY, source.gateway());
        return new CombinedDateFilterSnapshot(source, date, rows(),
                selectedFilterText(Filter.TYPE), selectedFilterText(Filter.STATUS),
                selectedFilterText(Filter.GATEWAY), dateButton().getText().trim());
    }

    public FilterTotalSnapshot totalAndPaginationAfterStatusFilter() {
        int beforeTotal = totalDisplayed();
        boolean beforePagination = hasPage(2);
        selectFilter(Filter.STATUS, "Thành công");
        return new FilterTotalSnapshot(beforeTotal, totalDisplayed(), beforePagination,
                hasPage(2), rows(), selectedFilterText(Filter.STATUS));
    }

    public DetailFilterPersistenceSnapshot filterPersistsAfterDetail() {
        TransactionRow source = firstRowWithValue(Filter.STATUS);
        return filterPersistsAfterDetail(source.status());
    }

    public DetailFilterPersistenceSnapshot filterPersistsAfterDetail(String status) {
        selectFilter(Filter.STATUS, status);
        List<String> before = rows().stream().map(TransactionRow::signature).toList();
        String listUrlBefore = driver.getCurrentUrl();
        CloseDetailSnapshot detail = closeFirstDetail();
        String browserLocationAfterClose = String.valueOf(
                ((JavascriptExecutor) driver).executeScript("return window.location.href"));
        return new DetailFilterPersistenceSnapshot(status,
                selectedFilterText(Filter.STATUS), before,
                rows().stream().map(TransactionRow::signature).toList(),
                listUrlBefore, detail.openedUrl(), detail.closedUrl(), browserLocationAfterClose);
    }

    public ResetSnapshot resetAllCombinedFiltersAndDate() {
        combineDateAndSelectFilters();
        resetFilters();
        return new ResetSnapshot(isAllTabSelected(), selectedFilterText(Filter.TYPE),
                selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                dateButton().getText().trim(), activePage(), rows().size(), driver.getCurrentUrl());
    }

    public DateClearSnapshot clearAppliedDateFilter() {
        int originalTotal = totalDisplayed();
        filterSingleDay();
        int filteredTotal = totalDisplayed();
        clickStable(By.cssSelector("[role='button'][aria-label='Xóa khoảng ngày giờ']"),
                "Xóa riêng khoảng ngày giờ");
        waitForTable();
        return new DateClearSnapshot(originalTotal, filteredTotal, totalDisplayed(),
                dateButton().getText().trim(), rows().size(),
                visibleElements(By.cssSelector(
                        "[role='button'][aria-label='Xóa khoảng ngày giờ']")).isEmpty());
    }

    public HiddenFilterSnapshot hiddenAllTabFilters() {
        return new HiddenFilterSnapshot(
                visibleElements(By.cssSelector("[aria-label='search-name-phone-filter']")).isEmpty(),
                visibleElements(By.cssSelector("button[aria-label='xuất hoá đơn-filter']")).isEmpty(),
                visibleElements(By.cssSelector("button[aria-label='thời hạn BH-filter']")).isEmpty());
    }

    public SortSnapshot sort(String header, boolean descending) {
        List<String> previousSignatures = rowSignatures(rows());
        for (int attempt = 1; attempt <= 3; attempt++) {
            click(headerButton(header), "Sắp xếp " + header);
            waitForTable();
            List<TransactionRow> currentRows = rows();
            if (rowsAreSorted(currentRows, header, descending)) {
                return new SortSnapshot(header, currentRows);
            }
            List<String> currentSignatures = rowSignatures(currentRows);
            if (currentSignatures.equals(previousSignatures)) {
                List<String> beforeResponse = previousSignatures;
                try {
                    currentRows = Waits.withTimeout(driver, Duration.ofSeconds(8))
                            .pollingEvery(Duration.ofMillis(250))
                            .until(d -> {
                                List<TransactionRow> refreshed = rows();
                                return rowSignatures(refreshed).equals(beforeResponse)
                                        ? null : refreshed;
                            });
                } catch (TimeoutException ignored) {
                    currentRows = rows();
                }
                if (rowsAreSorted(currentRows, header, descending)) {
                    return new SortSnapshot(header, currentRows);
                }
                currentSignatures = rowSignatures(currentRows);
            }
            previousSignatures = currentSignatures;
        }
        List<TransactionRow> observedRows = rows();
        String observed = normalizeText(header).contains("ngay tao")
                ? observedRows.stream().map(TransactionRow::createdAt).toList().toString()
                : observedRows.stream().map(TransactionRow::amountValue).toList().toString();
        throw new AssertionError("Không thể đưa cột " + header + " về chiều "
                + (descending ? "giảm dần" : "tăng dần") + ". Thứ tự hiện tại: " + observed);
    }

    private List<String> rowSignatures(List<TransactionRow> source) {
        return source.stream().map(TransactionRow::signature).toList();
    }

    private boolean rowsAreSorted(List<TransactionRow> currentRows, String header, boolean descending) {
        String normalizedHeader = normalizeText(header);
        if (normalizedHeader.contains("so tien")) {
            List<BigDecimal> values = currentRows.stream().map(TransactionRow::amountValue).toList();
            return isOrdered(values, descending ? Comparator.reverseOrder() : Comparator.naturalOrder());
        }
        if (normalizedHeader.contains("ngay tao")) {
            List<java.time.LocalDateTime> values = currentRows.stream()
                    .map(TransactionRow::createdAt).toList();
            return isOrdered(values, descending ? Comparator.reverseOrder() : Comparator.naturalOrder());
        }
        throw new IllegalArgumentException("Chưa hỗ trợ xác định chiều sort cho cột: " + header);
    }

    private <T> boolean isOrdered(List<T> values, Comparator<? super T> comparator) {
        for (int index = 1; index < values.size(); index++) {
            if (comparator.compare(values.get(index - 1), values.get(index)) > 0) {
                return false;
            }
        }
        return true;
    }

    public NonSortableSnapshot nonSortableHeadersDoNotChangeRows() {
        return nonSortableHeadersDoNotChangeRows(List.of("Loại giao dịch", "Trạng thái", "Cổng thanh toán"));
    }

    public NonSortableSnapshot nonSortableHeadersDoNotChangeRows(List<String> headers) {
        List<String> before = rows().stream().map(TransactionRow::signature).toList();
        List<String> nonSortable = new ArrayList<>();
        for (String header : headers) {
            WebElement button = headerButton(header);
            if (!button.getAttribute("class").contains("cursor-pointer")) {
                nonSortable.add(header);
            }
            click(button, "Thử click cột không hỗ trợ sắp xếp: " + header);
            settle(350);
        }
        return new NonSortableSnapshot(headers, nonSortable, before,
                rows().stream().map(TransactionRow::signature).toList());
    }

    public FirstPageControlSnapshot firstPageControlState() {
        return new FirstPageControlSnapshot(activePage(),
                paginationControlDisabled("previous page button"),
                paginationControlDisabled("next page button"), paginationTotalPages());
    }

    public PaginationControlSnapshot nextControlChangesPage() {
        requirePage(2);
        List<TransactionRow> before = rows();
        clickPaginationControl("next page button", "Chuyển trang bằng Next");
        return new PaginationControlSnapshot(1, activePage(), before, rows());
    }

    public PaginationControlSnapshot previousControlReturnsPage() {
        goToPage(2);
        List<TransactionRow> before = rows();
        clickPaginationControl("previous page button", "Quay lại bằng Previous");
        return new PaginationControlSnapshot(2, activePage(), before, rows());
    }

    public ActivePageSnapshot activeMarkerAfterPageChange() {
        goToPage(2);
        WebElement navigation = visible(By.cssSelector("nav[aria-label='pagination navigation']"));
        String dataActivePage = navigation.getAttribute("data-active-page");
        String ariaCurrent = visible(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-current='true']"))
                .getAttribute("aria-label");
        return new ActivePageSnapshot(activePage(), dataActivePage, ariaCurrent);
    }

    public LastPageSnapshot lastPage() {
        int lastPage = Integer.parseInt(visible(By.cssSelector(
                "nav[aria-label='pagination navigation']")).getAttribute("data-total"));
        goToPage(lastPage);
        return new LastPageSnapshot(lastPage, activePage(), rows(),
                paginationControlDisabled("next page button"));
    }

    public AdjacentPagesSnapshot adjacentPages() {
        requirePage(2);
        List<TransactionRow> first = rows();
        clickPaginationControl("next page button", "Chuyển sang trang liền sau");
        return new AdjacentPagesSnapshot(first, rows(), activePage());
    }

    public SortedPaginationSnapshot descendingAmountAcrossPages(boolean returnToFirst) {
        return sortedAcrossPages("Số tiền", true, returnToFirst);
    }

    public SortedPaginationSnapshot ascendingAmountAcrossPages(boolean returnToFirst) {
        return sortedAcrossPages("Số tiền", false, returnToFirst);
    }

    public SortedPaginationSnapshot descendingCreatedDateAcrossPages(boolean returnToFirst) {
        return sortedAcrossPages("Ngày tạo", true, returnToFirst);
    }

    public SortedPaginationSnapshot ascendingCreatedDateAcrossPages(boolean returnToFirst) {
        return sortedAcrossPages("Ngày tạo", false, returnToFirst);
    }

    private SortedPaginationSnapshot sortedAcrossPages(String header, boolean descending,
                                                        boolean returnToFirst) {
        sort(header, descending);
        List<TransactionRow> first = rows();
        goToPage(2);
        List<TransactionRow> second = rows();
        List<TransactionRow> returned = List.of();
        if (returnToFirst) {
            goToPage(1);
            returned = rows();
        }
        return new SortedPaginationSnapshot(first, second, returned, activePage());
    }

    public DotsRoundTripSnapshot paginationDotsJumpForwardAndBack() {
        int startPage = activePage();
        List<WebElement> forwardDots = visibleElements(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label='dots element']"));
        if (forwardDots.isEmpty()) {
            throw new org.testng.SkipException(
                    "Không đủ số trang để kiểm tra điều hướng hai chiều bằng dấu ba chấm.");
        }
        click(forwardDots.get(forwardDots.size() - 1),
                "Chuyển tới nhóm trang tiếp theo bằng dấu ba chấm");
        settle(1200);
        waitForTable();
        int forwardPage = activePage();

        List<WebElement> backwardDots = visibleElements(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label='dots element']"));
        if (backwardDots.isEmpty()) {
            throw new AssertionError("Không còn dấu ba chấm để quay về nhóm trang trước.");
        }
        click(backwardDots.get(0), "Quay về nhóm trang trước bằng dấu ba chấm");
        settle(1200);
        waitForTable();
        return new DotsRoundTripSnapshot(startPage, forwardPage, activePage(), rows());
    }

    public LastPagePreviousSnapshot previousFromLastPage() {
        int lastPage = paginationTotalPages();
        if (lastPage <= 1) {
            throw new org.testng.SkipException("Danh sách chỉ có một trang.");
        }
        goToPage(lastPage);
        List<String> lastRows = rowSignatures(rows());
        boolean nextDisabled = paginationControlDisabled("next page button");
        boolean previousDisabled = paginationControlDisabled("previous page button");
        clickPaginationControl("previous page button", "Quay lại từ trang cuối");
        return new LastPagePreviousSnapshot(lastPage, activePage(), lastRows,
                rowSignatures(rows()), nextDisabled, previousDisabled);
    }

    public PaginationGeometrySnapshot paginationGeometry() {
        int totalRows = totalDisplayed();
        int totalPages = paginationTotalPages();
        int firstPageRows = rows().size();
        int lastPageRows = firstPageRows;
        if (totalPages > 1) {
            goToPage(totalPages);
            lastPageRows = rows().size();
        }
        return new PaginationGeometrySnapshot(totalRows, totalPages, firstPageRows,
                lastPageRows, activePage());
    }

    public CombinedFilterPaginationSnapshot combinedStatusGatewayPersistsAcrossPages() {
        for (String status : filterOptions(Filter.STATUS)) {
            for (String gateway : filterOptions(Filter.GATEWAY)) {
                resetFilters();
                selectFilter(Filter.STATUS, status);
                selectFilter(Filter.GATEWAY, gateway);
                if (!hasPage(2)) {
                    continue;
                }
                List<TransactionRow> first = rows();
                goToPage(2);
                return new CombinedFilterPaginationSnapshot(status, gateway,
                        selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                        first, rows(), activePage());
            }
        }
        throw new org.testng.SkipException(
                "Không có tổ hợp trạng thái và cổng nào đủ dữ liệu cho trang 2.");
    }

    public EmptyPaginationSnapshot impossibleSearchHidesPagination() {
        String query = "__SYSTEM_NAVIGATION_NO_MATCH_847291__";
        fill(visible(By.cssSelector("[aria-label='search-name-phone-filter']")), query,
                "Nhập từ khóa chắc chắn không khớp để kiểm tra phân trang rỗng");
        waitForTable();
        EmptyPaginationSnapshot snapshot = new EmptyPaginationSnapshot(query,
                isEmptyState(), paginationVisible(), totalDisplayed(), mainText());
        resetFilters();
        return snapshot;
    }

    public PaginationSemanticsSnapshot paginationSemantics() {
        WebElement navigation = visible(By.cssSelector("nav[aria-label='pagination navigation']"));
        List<WebElement> pageItems = navigation.findElements(By.cssSelector(
                "[role='button'][aria-label^='pagination item']"));
        List<WebElement> dots = navigation.findElements(By.cssSelector(
                "[role='button'][aria-label='dots element']"));
        WebElement previous = navigation.findElement(By.cssSelector(
                "[role='button'][aria-label='previous page button']"));
        WebElement next = navigation.findElement(By.cssSelector(
                "[role='button'][aria-label='next page button']"));
        return new PaginationSemanticsSnapshot(navigation.getAttribute("aria-label"),
                previous.getAttribute("role"), previous.getAttribute("aria-label"),
                next.getAttribute("role"), next.getAttribute("aria-label"),
                paginationTotalPages(), pageItems.size(), dots.size(),
                pageItems.stream().allMatch(item -> item.getAttribute("aria-label") != null
                        && !item.getAttribute("aria-label").isBlank()),
                dots.stream().allMatch(item -> "dots element".equals(
                        item.getAttribute("aria-label"))));
    }

    public SortDetailPersistenceSnapshot amountSortPersistsAfterDetailOnSecondPage() {
        sort("Số tiền", true);
        goToPage(2);
        List<TransactionRow> before = rows();
        DetailSnapshot opened = openFirstDetail();
        click(detailCloseButton(), "Đóng chi tiết để kiểm tra giữ sort và trang hiện tại");
        wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new SortDetailPersistenceSnapshot(opened.url(), before, rows(), activePage(),
                driver.getCurrentUrl());
    }

    public FilterFromLaterPageSnapshot filterFromSecondPage() {
        goToPage(2);
        TransactionRow source = firstRowWithValue(Filter.STATUS);
        selectFilter(Filter.STATUS, source.status());
        return new FilterFromLaterPageSnapshot(source.status(),
                selectedFilterText(Filter.STATUS), activePage(), rows());
    }

    public DetailFromLaterPageSnapshot detailFromSecondPage() {
        goToPage(2);
        List<String> before = rows().stream().map(TransactionRow::signature).toList();
        DetailSnapshot opened = openFirstDetail();
        click(detailCloseButton(), "Đóng chi tiết tại trang 2");
        wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new DetailFromLaterPageSnapshot(opened.url(), activePage(), before,
                rows().stream().map(TransactionRow::signature).toList(), driver.getCurrentUrl());
    }

    public ResetSortSnapshot resetAfterAmountSort() {
        List<String> baseline = rows().stream().map(TransactionRow::signature).toList();
        sort("Số tiền", true);
        List<String> sorted = rows().stream().map(TransactionRow::signature).toList();
        resetFilters();
        return new ResetSortSnapshot(baseline, sorted,
                rows().stream().map(TransactionRow::signature).toList(), activePage());
    }

    public RefreshPageSnapshot refreshFromSecondPage() {
        List<TransactionRow> baselineRows = rows();
        goToPage(2);
        int pageBeforeRefresh = activePage();
        driver.navigate().refresh();
        waitForTable();
        return new RefreshPageSnapshot(pageBeforeRefresh, activePage(),
                baselineRows, rows(), driver.getCurrentUrl());
    }

    public ResetSortedPageSnapshot resetAmountSortFromSecondPage() {
        List<String> baseline = rowSignatures(rows());
        List<String> ascending = rowSignatures(sort("Số tiền", false).rows());
        List<String> descending = rowSignatures(sort("Số tiền", true).rows());
        goToPage(2);
        int pageBeforeReset = activePage();
        resetFilters();
        return new ResetSortedPageSnapshot(baseline, ascending, descending,
                rowSignatures(rows()), pageBeforeReset, activePage(), driver.getCurrentUrl());
    }

    public DotsJumpSnapshot jumpWithDots() {
        int before = activePage();
        List<WebElement> candidates = visibleElements(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label='dots element']"));
        if (candidates.isEmpty()) {
            throw new org.testng.SkipException(
                    "Không đủ số trang để hiển thị dấu ba chấm phân trang.");
        }
        WebElement dots = candidates.get(0);
        click(dots, "Nhảy nhóm trang bằng dấu ba chấm");
        settle(1200);
        waitForTable();
        return new DotsJumpSnapshot(before, activePage(), rows());
    }

    public PaginationSnapshot pageTwoAndBack() {
        List<TransactionRow> pageOne = rows();
        goToPage(2);
        List<TransactionRow> pageTwo = rows();
        goToPage(1);
        return new PaginationSnapshot(pageOne, pageTwo, rows(), activePage());
    }

    public FilterPaginationSnapshot filterPersistsAcrossPages() {
        selectFilter(Filter.STATUS, "Thành công");
        List<TransactionRow> first = rows();
        goToPage(2);
        return new FilterPaginationSnapshot("Thành công", selectedFilterText(Filter.STATUS),
                first, rows(), activePage());
    }

    public GatewayPaginationSnapshot gatewayFilterPaginationStates() {
        List<GatewayPaginationCell> results = new ArrayList<>();
        for (String gateway : filterOptions(Filter.GATEWAY)) {
            selectFilter(Filter.GATEWAY, gateway);
            List<TransactionRow> first = rows();
            boolean pageTwoAvailable = hasPage(2);
            List<TransactionRow> second = List.of();
            if (hasPage(2)) {
                goToPage(2);
                second = rows();
            }
            results.add(new GatewayPaginationCell(gateway,
                    selectedFilterText(Filter.GATEWAY), first, second, pageTwoAvailable));
            resetFilters();
        }
        return new GatewayPaginationSnapshot(filterOptions(Filter.GATEWAY), results,
                driver.getCurrentUrl());
    }

    public BrowserPageHistorySnapshot browserBackAndForwardPages() {
        String pageOneUrl = driver.getCurrentUrl();
        List<String> pageOneRows = rowSignatures(rows());
        goToPage(2);
        String pageTwoUrl = driver.getCurrentUrl();
        List<String> pageTwoRows = rowSignatures(rows());

        driver.navigate().back();
        wait.until(d -> d.getCurrentUrl().equals(pageOneUrl));
        waitForTable();
        String backUrl = driver.getCurrentUrl();
        List<String> backRows = rowSignatures(rows());
        int backActivePage = activePage();

        driver.navigate().forward();
        wait.until(d -> d.getCurrentUrl().equals(pageTwoUrl));
        waitForTable();
        return new BrowserPageHistorySnapshot(pageOneUrl, pageTwoUrl, backUrl,
                driver.getCurrentUrl(), pageOneRows, pageTwoRows, backRows,
                rowSignatures(rows()), backActivePage, activePage());
    }

    public RefreshSortedFilterSnapshot refreshSortedFilteredSecondPage() {
        List<TransactionRow> expectedAfterRefresh = rows();
        selectFilter(Filter.STATUS, "Thành công");
        sort("Số tiền", true);
        goToPage(2);
        List<TransactionRow> before = rows();
        driver.navigate().refresh();
        waitForTable();
        return new RefreshSortedFilterSnapshot("Thành công",
                selectedFilterText(Filter.STATUS), expectedAfterRefresh, before, rows(), activePage(),
                driver.getCurrentUrl());
    }

    public ResetSnapshot resetFromSecondPage() {
        goToPage(2);
        resetFilters();
        return new ResetSnapshot(isAllTabSelected(), selectedFilterText(Filter.TYPE),
                selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                dateButton().getText().trim(), activePage(), rows().size(), driver.getCurrentUrl());
    }

    public DetailSnapshot openFirstDetail() {
        List<WebElement> elements = dataRowElements();
        if (elements.isEmpty()) {
            throw new AssertionError("Không có giao dịch hiển thị để mở chi tiết tại "
                    + driver.getCurrentUrl());
        }
        int selectedIndex = -1;
        TransactionRow source = null;
        for (int index = 0; index < elements.size(); index++) {
            TransactionRow candidate = toRow(elements.get(index));
            if (candidate != null && !candidate.gateway().isBlank()) {
                selectedIndex = index;
                source = candidate;
                break;
            }
        }
        if (selectedIndex < 0) {
            selectedIndex = 0;
            source = toRow(elements.get(0));
        }
        if (source == null) {
            throw new IllegalStateException("Không có giao dịch hợp lệ để mở chi tiết.");
        }
        click(elements.get(selectedIndex), "Mở chi tiết giao dịch có thông tin người dùng");
        WebElement drawer = detailDrawer();
        String drawerText = elementText(drawer);
        String lowerDrawerText = drawerText.toLowerCase(Locale.ROOT);
        int relatedSections = lowerDrawerText.contains("dòng tiền của") ? 1 : 0;
        return new DetailSnapshot(source, driver.getCurrentUrl(), drawerText,
                relatedSections, lowerDrawerText.contains("đang xem"));
    }

    private DetailSnapshot openDetailMatching(BiPredicate<WebElement, String> condition,
                                              String expectedContent) {
        String listUrl = driver.getCurrentUrl();
        int rowCount = dataRowElements().size();
        for (int index = 0; index < rowCount; index++) {
            List<WebElement> elements = dataRowElements();
            if (index >= elements.size()) {
                break;
            }
            TransactionRow source = toRow(elements.get(index));
            if (source == null) {
                continue;
            }
            click(elements.get(index), "Mo chi tiet de tim " + expectedContent);
            detailDrawer();
            String openedUrl = driver.getCurrentUrl();
            try {
                WebElement matched = Waits.withTimeout(driver, Duration.ofSeconds(4)).until(d -> {
                    WebElement current = detailDrawer();
                    return condition.test(current, openedUrl) ? current : null;
                });
                String text = elementText(matched);
                String lowerText = text.toLowerCase(Locale.ROOT);
                int relatedSections = lowerText.contains("d\u00f2ng ti\u1ec1n c\u1ee7a") ? 1 : 0;
                return new DetailSnapshot(source, openedUrl, text, relatedSections,
                        lowerText.contains("\u0111ang xem"));
            } catch (TimeoutException ignored) {
                driver.get(listUrl);
                waitForTable();
            }
        }
        throw new AssertionError("Khong tim thay giao dich co " + expectedContent
                + " trong " + rowCount + " dong dang hien thi.");
    }

    public CloseDetailSnapshot closeFirstDetail() {
        DetailSnapshot opened = openFirstDetail();
        WebElement close = detailCloseButton();
        click(close, "Đóng chi tiết giao dịch");
        boolean closed = wait.until(d -> visibleElements(By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new CloseDetailSnapshot(opened.url(), driver.getCurrentUrl(), closed);
    }

    public DeepLinkSnapshot reopenByDeepLink() {
        DetailSnapshot opened = openFirstDetail();
        String deepLink = opened.url();
        WebElement close = detailCloseButton();
        click(close, "Đóng chi tiết trước khi mở deep link");
        driver.get(deepLink);
        WebElement reopened = detailDrawer();
        return new DeepLinkSnapshot(deepLink, driver.getCurrentUrl(), elementText(reopened));
    }

    public DeepLinkSnapshot refreshOpenDetail() {
        DetailSnapshot opened = openFirstDetail();
        String deepLink = opened.url();
        driver.navigate().refresh();
        WebElement reopened = detailDrawer();
        return new DeepLinkSnapshot(deepLink, driver.getCurrentUrl(), elementText(reopened));
    }

    public HistoryNavigationSnapshot backAndForwardDetail() {
        String listUrl = driver.getCurrentUrl();
        DetailSnapshot opened = openFirstDetail();
        driver.navigate().back();
        boolean closedAfterBack;
        try {
            closedAfterBack = Waits.withTimeout(driver, Duration.ofSeconds(8)).until(d ->
                    visibleElements(By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty()
                            && d.getCurrentUrl().equals(listUrl));
        } catch (TimeoutException historyDidNotRestoreList) {
            driver.get(listUrl);
            waitForTable();
            closedAfterBack = visibleElements(By.cssSelector(
                    "[aria-label='drawer-Chi tiết giao dịch']")).isEmpty();
        }
        String backUrl = driver.getCurrentUrl();
        driver.navigate().forward();
        try {
            Waits.withTimeout(driver, Duration.ofSeconds(8)).until(d ->
                    d.getCurrentUrl().equals(opened.url()));
        } catch (TimeoutException historyDidNotRestoreDeepLink) {
            driver.get(opened.url());
        }
        WebElement reopened = restoreDetailDrawerAfterHistory(opened.url());
        return new HistoryNavigationSnapshot(opened.url(), backUrl, driver.getCurrentUrl(),
                closedAfterBack, elementText(reopened));
    }

    public RelatedHistorySnapshot waitForRelatedHistory() {
        DetailSnapshot opened = openFirstDetail();
        By locator = By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']");
        try {
            WebElement loaded = wait.until(d -> d.findElements(locator).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> {
                        String text = elementText(element).toLowerCase(Locale.ROOT);
                        return text.contains("dòng tiền của")
                                && !text.contains("đang tải");
                    }).findFirst().orElse(null));
            String text = elementText(loaded);
            return new RelatedHistorySnapshot(opened.source(), text, true,
                    text.toLowerCase(Locale.ROOT).contains("đang xem"));
        } catch (TimeoutException exception) {
            String text = elementText(detailDrawer());
            return new RelatedHistorySnapshot(opened.source(), text, false,
                    text.toLowerCase(Locale.ROOT).contains("đang xem"));
        }
    }

    public RelatedHistorySnapshot waitForCashFlowTotals() {
        DetailSnapshot opened = openDetailMatching((drawer, url) -> {
            String text = elementText(drawer);
            return text.contains("T\u1ed5ng v\u00e0o")
                    && text.contains("T\u1ed5ng ra")
                    && text.contains("D\u00f2ng ti\u1ec1n r\u00f2ng");
        }, "cac tong dong tien");
        return new RelatedHistorySnapshot(opened.source(), opened.drawerText(), true,
                opened.currentMarked());
    }

    public RelatedHistorySnapshot loadRelatedHistoryUntilCurrent() {
        DetailSnapshot opened = openDetailMatching((drawer, url) -> {
            String text = elementText(drawer).toLowerCase(Locale.ROOT);
            boolean hasCurrent = text.contains("\u0111ang xem");
            boolean canLoadMore = drawer.findElements(
                            By.xpath(".//button[normalize-space()='Xem th\u00eam']"))
                    .stream().anyMatch(button -> button.isDisplayed() && button.isEnabled());
            return hasCurrent || canLoadMore;
        }, "lich su dong tien co giao dich dang xem");
        WebElement drawer = detailDrawer();
        for (int attempt = 0; attempt < 20; attempt++) {
            String text = elementText(drawer);
            if (text.toLowerCase(Locale.ROOT).contains("đang xem")) {
                return new RelatedHistorySnapshot(opened.source(), text, true, true,
                        relatedHistoryContainsCurrent(drawer, opened.url()));
            }
            WebElement more = drawer.findElements(By.xpath(".//button[normalize-space()='Xem thêm']"))
                    .stream().filter(WebElement::isDisplayed).filter(WebElement::isEnabled)
                    .findFirst().orElse(null);
            if (more == null) {
                return new RelatedHistorySnapshot(opened.source(), text,
                        text.contains("Dòng tiền"), false,
                        relatedHistoryContainsCurrent(drawer, opened.url()));
            }
            int beforeLength = text.length();
            click(more, "Tải thêm lịch sử dòng tiền");
            wait.until(d -> elementText(detailDrawer()).length() > beforeLength);
            drawer = detailDrawer();
        }
        String text = elementText(drawer);
        return new RelatedHistorySnapshot(opened.source(), text, true,
                text.toLowerCase(Locale.ROOT).contains("đang xem"),
                relatedHistoryContainsCurrent(drawer, opened.url()));
    }

    private boolean relatedHistoryContainsCurrent(WebElement drawer, String openedUrl) {
        String currentId = queryParameter(openedUrl, "id");
        return !currentId.isBlank() && drawer.findElements(By.cssSelector(
                        "a[href*='/vuatho/transaction?'][href*='id=']"))
                .stream().map(link -> queryParameter(link.getAttribute("href"), "id"))
                .anyMatch(currentId::equals);
    }

    public RelatedExpansionSnapshot expandRelatedHistoryOnce() {
        DetailSnapshot opened = openDetailMatching((drawer, url) -> drawer.findElements(
                        By.xpath(".//button[normalize-space()='Xem thêm']"))
                .stream().anyMatch(button -> button.isDisplayed() && button.isEnabled()),
                "nut Xem them lich su dong tien");
        WebElement drawer = detailDrawer();
        By relatedLinks = By.cssSelector("a[href*='/vuatho/transaction?']");
        int before = (int) drawer.findElements(relatedLinks).stream()
                .filter(WebElement::isDisplayed).count();
        WebElement more = drawer.findElements(By.xpath(".//button[normalize-space()='Xem thêm']"))
                .stream().filter(WebElement::isDisplayed).filter(WebElement::isEnabled)
                .findFirst().orElseThrow(() -> new IllegalStateException("Không thấy nút Xem thêm."));
        click(more, "Tải thêm một trang lịch sử dòng tiền");
        int after = wait.until(d -> {
            WebElement current = detailDrawer();
            int count = (int) current.findElements(relatedLinks).stream()
                    .filter(WebElement::isDisplayed).count();
            return count > before ? count : null;
        });
        return new RelatedExpansionSnapshot(opened.url(), before, after,
                elementText(detailDrawer()));
    }

    public DetailAuditSnapshot auditFirstDetailInOneFlow() {
        DetailSnapshot opened = openFirstDetail();
        WebElement drawer = detailDrawer();
        By relatedLinks = By.cssSelector("a[href*='/vuatho/transaction?']");
        String userHref = drawer.findElements(By.cssSelector(
                        "a[href*='/vuatho/user?id='],a[href*='/vuatho/worker?id=']"))
                .stream().filter(WebElement::isDisplayed)
                .map(link -> link.getAttribute("href")).findFirst().orElse("");
        int before = (int) drawer.findElements(relatedLinks).stream()
                .filter(WebElement::isDisplayed).count();
        int after = before;
        WebElement more = drawer.findElements(By.xpath(".//button[normalize-space()='Xem thêm']"))
                .stream().filter(WebElement::isDisplayed).filter(WebElement::isEnabled)
                .findFirst().orElse(null);
        if (more != null) {
            click(more, "Tải thêm lịch sử trong lần kiểm tra chi tiết tổng hợp");
            after = wait.until(d -> {
                int count = (int) detailDrawer().findElements(relatedLinks).stream()
                        .filter(WebElement::isDisplayed).count();
                return count > before ? count : null;
            });
            drawer = detailDrawer();
        }
        String text = wait.until(d -> {
            String current = elementText(detailDrawer());
            return current.contains("Dòng tiền") && !current.contains("Đang tải") ? current : null;
        });
        List<String> transactionHrefs = drawer.findElements(relatedLinks).stream()
                .filter(WebElement::isDisplayed)
                .map(link -> link.getAttribute("href"))
                .filter(value -> value != null && !value.isBlank()).toList();
        boolean currentMarked = text.toLowerCase(Locale.ROOT).contains("đang xem");
        WebElement close = detailCloseButton();
        click(close, "Đóng chi tiết sau lần kiểm tra tổng hợp");
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new DetailAuditSnapshot(opened.source(), opened.url(), text, userHref,
                transactionHrefs, before, after, currentMarked, closed);
    }

    public DetailActionSnapshot inspectFirstDetailActions() {
        DetailSnapshot opened = openFirstDetail();
        WebElement drawer = detailDrawer();
        List<WebElement> buttons = drawer.findElements(By.tagName("button"));
        boolean cancelVisible = buttons.stream().anyMatch(button ->
                button.isDisplayed() && elementText(button).trim().equals("Hủy"));
        boolean rejectPresent = buttons.stream().anyMatch(button ->
                button.getAttribute("textContent").trim().equals("Từ chối"));
        boolean rejectVisible = buttons.stream().anyMatch(button ->
                button.isDisplayed() && elementText(button).trim().equals("Từ chối"));
        click(detailCloseButton(), "Đóng chi tiết sau khi kiểm tra nút hành động");
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new DetailActionSnapshot(opened.source(), opened.url(), cancelVisible,
                rejectPresent, rejectVisible, closed, driver.getCurrentUrl());
    }

    /**
     * Thu thập các element đặc thù của drawer Hệ thống theo nội dung bắt buộc.
     * Dùng điều kiện nội dung thay vì cố định một ID giao dịch vì dữ liệu sandbox thay đổi.
     */
    public SystemDetailElementSnapshot inspectSystemDetailContaining(String... requiredTexts) {
        List<String> requirements = List.of(requiredTexts);
        DetailSnapshot opened = openDetailMatching((drawer, url) -> {
            String text = normalizeText(elementText(drawer));
            return requirements.stream().map(this::normalizeText).allMatch(text::contains);
        }, "element Hệ thống: " + String.join(", ", requirements));
        WebElement drawer = detailDrawer();
        WebElement userLink = drawer.findElements(By.cssSelector(
                        "a[href*='/vuatho/user?id='],a[href*='/vuatho/worker?id=']"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
        List<DetailElementLink> transactionLinks = detailElementLinks(drawer,
                "a[href*='/vuatho/transaction?']");
        String userHref = userLink == null ? "" : userLink.getAttribute("href");
        String userText = userLink == null ? "" : elementText(userLink).trim();
        click(detailCloseButton(), "Đóng drawer sau khi kiểm tra element Hệ thống");
        boolean closed = wait.until(d -> visibleElements(By.cssSelector(
                "[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new SystemDetailElementSnapshot(opened.source(), opened.url(), opened.drawerText(),
                userHref, userText, transactionLinks, closed, driver.getCurrentUrl());
    }

    /** Kiểm tra các nút đặc thù trong drawer mà không kích hoạt hành động thay đổi dữ liệu. */
    public DetailControlSnapshot inspectDetailControls(List<String> labels, String safeClickLabel) {
        DetailSnapshot opened = openDetailMatching((drawer, url) -> labels.stream().allMatch(label ->
                        drawer.findElements(By.xpath(".//button[normalize-space(.)="
                                        + xpathLiteral(label) + "]"))
                                .stream().anyMatch(WebElement::isDisplayed)),
                "cac nut " + String.join(", ", labels));
        WebElement drawer = detailDrawer();
        List<DetailControlState> controls = labels.stream().map(label -> {
            List<WebElement> matches = drawer.findElements(By.xpath(".//button[normalize-space(.)="
                    + xpathLiteral(label) + "]"));
            boolean present = !matches.isEmpty();
            boolean visible = matches.stream().anyMatch(WebElement::isDisplayed);
            boolean enabled = matches.stream().anyMatch(button -> button.isDisplayed()
                    && button.isEnabled() && button.getAttribute("disabled") == null
                    && !"true".equals(button.getAttribute("data-disabled")));
            return new DetailControlState(label, present, visible, enabled);
        }).toList();

        boolean safeActionPerformed = false;
        if (safeClickLabel != null && !safeClickLabel.isBlank()) {
            WebElement safeButton = drawer.findElements(By.xpath(".//button[normalize-space(.)="
                            + xpathLiteral(safeClickLabel) + "]"))
                    .stream().filter(WebElement::isDisplayed).filter(WebElement::isEnabled)
                    .findFirst().orElseThrow(() -> new AssertionError(
                            "Nút an toàn không sẵn sàng: " + safeClickLabel));
            click(safeButton, "Kiểm tra " + safeClickLabel + " trong drawer giao dịch");
            safeActionPerformed = !visibleElements(By.cssSelector(
                    "[aria-label='drawer-Chi tiết giao dịch']")).isEmpty();
        }
        click(detailCloseButton(), "Đóng drawer sau khi kiểm tra nút đặc thù");
        boolean closed = wait.until(d -> visibleElements(By.cssSelector(
                "[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new DetailControlSnapshot(opened.source(), opened.url(), controls,
                safeClickLabel, safeActionPerformed, closed);
    }

    /** Mở thẻ đơn còn bảo hành và giữ nguyên giao dịch đang xem. */
    public WarrantyOrdersSnapshot expandWarrantyOrders() {
        String label = "Đơn còn bảo hành";
        DetailSnapshot opened = openDetailMatching((drawer, url) -> drawer.findElements(
                        By.xpath(".//button[contains(normalize-space(.)," + xpathLiteral(label) + ")]"))
                .stream().anyMatch(WebElement::isDisplayed), label);
        WebElement drawer = detailDrawer();
        WebElement card = drawer.findElements(By.xpath(
                        ".//button[contains(normalize-space(.)," + xpathLiteral(label) + ")]"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new AssertionError("Thiếu thẻ " + label));
        String cardText = elementText(card);
        String beforeText = elementText(drawer);
        click(card, "Mở danh sách đơn còn bảo hành");
        WebElement expandedDrawer = detailDrawer();
        String afterText = elementText(expandedDrawer);
        boolean stayedOpen = driver.getCurrentUrl().equals(opened.url());
        click(detailCloseButton(), "Đóng drawer sau khi kiểm tra đơn còn bảo hành");
        boolean closed = wait.until(d -> visibleElements(By.cssSelector(
                "[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new WarrantyOrdersSnapshot(opened.source(), opened.url(), cardText,
                beforeText, afterText, stayedOpen, closed);
    }

    /** Mở đúng link giao dịch bị từ chối trong drawer và xác nhận deep-link đích. */
    public RejectedTransactionLinkSnapshot openRejectedTransactionLink() {
        String label = "Xem giao dịch bị từ chối";
        DetailSnapshot opened = openDetailMatching((drawer, url) -> drawer.findElements(
                        By.xpath(".//a[contains(normalize-space(.)," + xpathLiteral(label) + ")]"))
                .stream().anyMatch(WebElement::isDisplayed), label);
        WebElement link = detailDrawer().findElements(By.xpath(
                        ".//a[contains(normalize-space(.)," + xpathLiteral(label) + ")]"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new AssertionError("Thiếu link " + label));
        String href = link.getAttribute("href");
        String text = elementText(link);
        click(link, "Mở giao dịch bị từ chối");
        detailDrawer();
        return new RejectedTransactionLinkSnapshot(opened.url(), href,
                driver.getCurrentUrl(), text, elementText(detailDrawer()));
    }

    /** Thu thập tên truy cập của nút đóng drawer để phát hiện icon không có nhãn. */
    public CloseAccessibilitySnapshot inspectDetailCloseAccessibility() {
        DetailSnapshot opened = openFirstDetail();
        WebElement close = detailCloseButton();
        String ariaLabel = close.getAttribute("aria-label") == null
                ? "" : close.getAttribute("aria-label").trim();
        String title = close.getAttribute("title") == null
                ? "" : close.getAttribute("title").trim();
        String text = elementText(close).trim();
        click(close, "Đóng drawer sau khi kiểm tra nhãn truy cập");
        boolean closed = wait.until(d -> visibleElements(By.cssSelector(
                "[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new CloseAccessibilitySnapshot(opened.url(), ariaLabel, title, text, closed);
    }

    public DetailLinksSnapshot detailLinks() {
        DetailSnapshot opened = openFirstDetail();
        WebElement drawer = detailDrawer();
        String userHref = drawer.findElements(By.cssSelector(
                        "a[href*='/vuatho/user?id='],a[href*='/vuatho/worker?id=']"))
                .stream().filter(WebElement::isDisplayed)
                .map(link -> link.getAttribute("href")).findFirst().orElse("");
        List<String> transactionHrefs = drawer.findElements(
                        By.cssSelector("a[href*='/vuatho/transaction?']"))
                .stream().filter(WebElement::isDisplayed)
                .map(link -> link.getAttribute("href")).filter(value -> value != null).toList();
        return new DetailLinksSnapshot(opened.url(), userHref, transactionHrefs,
                elementText(drawer));
    }

    public OrderDetailElementSnapshot inspectOrderDetailElements() {
        DetailSnapshot opened = openFirstDetail();
        return captureOrderDetailElements(opened.source(), opened.url());
    }

    public OrderDetailElementSnapshot inspectOrderDetailElements(String expectedStatus) {
        selectStatusForDetail(expectedStatus);
        if (detailEmptyStateVisible()) {
            throw new AssertionError("Không có giao dịch trạng thái " + expectedStatus);
        }
        List<WebElement> elements = dataRowElementsWithoutWaiting();
        if (elements.isEmpty()) {
            throw new AssertionError("Không có dòng để mở chi tiết trạng thái " + expectedStatus);
        }
        TransactionRow source = toRow(elements.get(0));
        if (source == null || !expectedStatus.equals(source.status())) {
            throw new AssertionError("Dòng đầu không đúng trạng thái " + expectedStatus);
        }
        click(elements.get(0), "Mở chi tiết Order trạng thái " + expectedStatus);
        detailDrawer();
        return captureOrderDetailElements(source, driver.getCurrentUrl());
    }

    /** Thao tác hai nút Copy và nút QR của phiếu Chi bảo hành đang chờ, không gửi phiếu. */
    public OrderQrCopyInteractionSnapshot exercisePendingWarrantyPaymentQrAndCopy() {
        selectStatusForDetail("Đang chờ");
        List<WebElement> elements = dataRowElementsWithoutWaiting();
        if (elements.isEmpty()) {
            throw new AssertionError("Không có Chi bảo hành Đang chờ để kiểm tra Copy/QR");
        }
        TransactionRow source = toRow(elements.get(0));
        click(elements.get(0), "Mở Chi bảo hành Đang chờ để thao tác Copy/QR");
        WebElement drawer = detailDrawer();
        String openedUrl = driver.getCurrentUrl();

        List<WebElement> copyButtons = drawer.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> "Copy".equals(elementText(button).trim())).toList();
        int copyClicks = 0;
        for (WebElement copyButton : copyButtons) {
            click(copyButton, "Copy dữ liệu chuyển khoản");
            copyClicks++;
        }
        boolean drawerStayedOpenAfterCopy = !visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty()
                && driver.getCurrentUrl().equals(openedUrl);

        WebElement qrButton = drawer.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> "Hiện mã QR".equals(elementText(button).trim()))
                .findFirst().orElseThrow(() -> new AssertionError("Thiếu nút Hiện mã QR"));
        String beforeQrDom = driver.getPageSource();
        String beforeQrText = elementText(driver.findElement(By.tagName("body")));
        click(qrButton, "Hiện mã QR chuyển khoản");
        boolean qrOpened = Waits.withTimeout(driver, Duration.ofSeconds(8)).until(d -> {
            String afterText = elementText(d.findElement(By.tagName("body")));
            return !afterText.equals(beforeQrText) || !d.getPageSource().equals(beforeQrDom);
        });
        String qrViewText = elementText(driver.findElement(By.tagName("body")));
        boolean drawerStayedOpenAfterQr = !visibleElements(By.cssSelector(
                "[aria-label='drawer-Chi tiết giao dịch']")).isEmpty()
                && driver.getCurrentUrl().equals(openedUrl);
        click(detailCloseButton(), "Đóng drawer sau khi kiểm tra Copy/QR");
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new OrderQrCopyInteractionSnapshot(source, openedUrl, copyButtons.size(),
                copyClicks, drawerStayedOpenAfterCopy, qrOpened, qrViewText,
                drawerStayedOpenAfterQr, closed, driver.getCurrentUrl());
    }

    /** Upload bill nháp để kiểm tra validation nút xác nhận, không bấm xác nhận giao dịch. */
    public OrderBillUploadSnapshot uploadPendingWarrantyPaymentBill(Path imageFile) {
        selectStatusForDetail("Đang chờ");
        List<WebElement> elements = dataRowElementsWithoutWaiting();
        if (elements.isEmpty()) {
            throw new AssertionError("Không có Chi bảo hành Đang chờ để kiểm tra upload bill");
        }
        TransactionRow source = toRow(elements.get(0));
        click(elements.get(0), "Mở Chi bảo hành Đang chờ để upload bill");
        WebElement drawer = detailDrawer();
        String openedUrl = driver.getCurrentUrl();
        WebElement confirm = drawer.findElements(By.tagName("button")).stream()
                .filter(button -> "Xác nhận đã chuyển khoản".equals(
                        button.getAttribute("textContent").trim()))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Thiếu nút Xác nhận đã chuyển khoản"));
        boolean disabledBeforeUpload = !confirm.isEnabled()
                || confirm.getAttribute("disabled") != null
                || "true".equals(confirm.getAttribute("data-disabled"));
        WebElement upload = drawer.findElement(By.cssSelector("input[type='file']"));
        String accept = upload.getAttribute("accept");
        upload.sendKeys(imageFile.toAbsolutePath().toString());
        boolean enabledAfterUpload = Waits.withTimeout(driver, Duration.ofSeconds(12)).until(d -> {
            WebElement currentDrawer = detailDrawer();
            return currentDrawer.findElements(By.tagName("button")).stream()
                    .filter(button -> "Xác nhận đã chuyển khoản".equals(
                            button.getAttribute("textContent").trim()))
                    .anyMatch(button -> button.isDisplayed() && button.isEnabled()
                            && button.getAttribute("disabled") == null
                            && !"true".equals(button.getAttribute("data-disabled")));
        });
        click(detailCloseButton(), "Đóng drawer sau khi kiểm tra upload bill");
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new OrderBillUploadSnapshot(source, openedUrl, accept, disabledBeforeUpload,
                enabledAfterUpload, closed, driver.getCurrentUrl());
    }

    /** Bấm Hủy trong footer drawer và xác nhận giao dịch đang chờ không bị thay đổi. */
    public OrderCancelSnapshot cancelFirstPendingOrderDetail() {
        selectStatusForDetail("Đang chờ");
        List<WebElement> elements = dataRowElementsWithoutWaiting();
        if (elements.isEmpty()) {
            throw new AssertionError("Không có giao dịch Đang chờ để kiểm tra Hủy");
        }
        TransactionRow source = toRow(elements.get(0));
        click(elements.get(0), "Mở giao dịch Đang chờ để kiểm tra Hủy");
        String openedUrl = driver.getCurrentUrl();
        WebElement drawer = detailDrawer();
        WebElement cancel = drawer.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> "Hủy".equals(elementText(button).trim()))
                .findFirst().orElseThrow(() -> new AssertionError("Thiếu nút Hủy trong drawer"));
        click(cancel, "Hủy và đóng drawer giao dịch Đang chờ");
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        List<TransactionRow> rowsAfterCancel = currentRows();
        return new OrderCancelSnapshot(source, openedUrl, closed, driver.getCurrentUrl(),
                rowsAfterCancel.contains(source));
    }

    private OrderDetailElementSnapshot captureOrderDetailElements(TransactionRow source,
                                                                   String openedUrl) {
        WebElement drawer = detailDrawer();
        String initialText = normalizeText(elementText(drawer));
        if (initialText.contains("dong tien cua") && !initialText.contains("tong vao")) {
            drawer.findElements(By.xpath(".//*[contains(normalize-space(),'Dòng tiền của')]")).stream()
                    .filter(WebElement::isDisplayed).findFirst().ifPresent(heading ->
                            ((JavascriptExecutor) driver).executeScript(
                                    "arguments[0].scrollIntoView({block:'center'});", heading));
            try {
                drawer = Waits.withTimeout(driver, Duration.ofSeconds(12)).until(d ->
                        d.findElements(By.cssSelector(
                                        "[aria-label='drawer-Chi tiết giao dịch']")).stream()
                                .filter(WebElement::isDisplayed)
                                .filter(element -> {
                                    String text = normalizeText(elementText(element));
                                    return text.contains("tong vao") && text.contains("tong ra")
                                            && text.contains("dong tien rong");
                                }).findFirst().orElse(null));
            } catch (TimeoutException ignored) {
                drawer = detailDrawer();
            }
        }
        String drawerText = elementText(drawer);
        List<WebElement> buttons = drawer.findElements(By.tagName("button"));
        List<String> visibleButtons = buttons.stream().filter(WebElement::isDisplayed)
                .map(this::elementText).map(String::trim).filter(value -> !value.isBlank()).toList();
        boolean rejectPresent = buttons.stream().anyMatch(button ->
                "Từ chối".equals(button.getAttribute("textContent").trim()));
        boolean rejectVisible = buttons.stream().anyMatch(button -> button.isDisplayed()
                && "Từ chối".equals(elementText(button).trim()));
        List<WebElement> confirmButtons = buttons.stream().filter(button ->
                "Xác nhận đã chuyển khoản".equals(button.getAttribute("textContent").trim())).toList();
        boolean confirmVisible = confirmButtons.stream().anyMatch(WebElement::isDisplayed);
        boolean confirmDisabled = !confirmButtons.isEmpty() && confirmButtons.stream().allMatch(button ->
                !button.isEnabled() || button.getAttribute("disabled") != null
                        || "true".equals(button.getAttribute("data-disabled")));
        List<String> imageAccepts = drawer.findElements(By.cssSelector("input[type='file']"))
                .stream().map(input -> input.getAttribute("accept"))
                .filter(value -> value != null).toList();
        int visibleCopyButtons = (int) buttons.stream().filter(WebElement::isDisplayed)
                .filter(button -> "Copy".equals(elementText(button).trim())).count();
        boolean qrButtonVisible = buttons.stream().filter(WebElement::isDisplayed)
                .anyMatch(button -> "Hiện mã QR".equals(elementText(button).trim()));
        List<WebElement> walletConfirmButtons = buttons.stream().filter(button ->
                "Xác nhận trừ ví thợ".equals(button.getAttribute("textContent").trim())).toList();
        boolean walletConfirmVisible = walletConfirmButtons.stream().anyMatch(WebElement::isDisplayed);
        boolean walletConfirmDisabled = !walletConfirmButtons.isEmpty()
                && walletConfirmButtons.stream().allMatch(button -> !button.isEnabled()
                || button.getAttribute("disabled") != null
                || "true".equals(button.getAttribute("data-disabled")));
        List<DetailElementLink> userLinks = detailElementLinks(drawer,
                "a[href*='/vuatho/user?id='],a[href*='/vuatho/worker?id=']");
        List<DetailElementLink> orderLinks = detailElementLinks(drawer,
                "a[href*='/vuatho/order?id=']");
        List<DetailElementLink> transactionLinks = detailElementLinks(drawer,
                "a[href*='/vuatho/transaction?']");
        List<DetailElementLink> withdrawalLinks = detailElementLinks(drawer,
                "a[href*='/vuatho/withdraw-qr-request?id=']");
        boolean currentMarked = drawerText.toLowerCase(Locale.ROOT).contains("đang xem");
        click(detailCloseButton(), "Đóng drawer sau khi kiểm tra element Order");
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new OrderDetailElementSnapshot(source, openedUrl, drawerText, visibleButtons,
                rejectPresent, rejectVisible, !confirmButtons.isEmpty(), confirmVisible,
                confirmDisabled, imageAccepts, visibleCopyButtons, qrButtonVisible,
                userLinks, orderLinks, transactionLinks, withdrawalLinks,
                !walletConfirmButtons.isEmpty(), walletConfirmVisible, walletConfirmDisabled,
                currentMarked, closed, driver.getCurrentUrl());
    }

    private List<DetailElementLink> detailElementLinks(WebElement root, String cssSelector) {
        return root.findElements(By.cssSelector(cssSelector)).stream()
                .filter(WebElement::isDisplayed)
                .map(link -> new DetailElementLink(link.getAttribute("href"),
                        link.getAttribute("target"), elementText(link).trim()))
                .toList();
    }

    public FeeConnectionElementSnapshot auditFeeConnectionElement() {
        DetailSnapshot opened = openDetailMatching((drawer, url) -> {
            String text = elementText(drawer).toLowerCase(Locale.ROOT);
            return text.contains("tổng vào") && text.contains("tổng ra")
                    && text.contains("dòng tiền ròng") && text.contains("đang xem");
        }, "đầy đủ tổng dòng tiền và giao dịch đang xem");
        WebElement drawer = detailDrawer();

        WebElement workerLink = drawer.findElements(By.cssSelector("a[href*='/vuatho/worker?id=']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new AssertionError("Chi tiết Phí kết nối không có link người gửi."));
        WebElement orderLink = drawer.findElements(By.cssSelector("a[href*='/vuatho/order?id=']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new AssertionError("Chi tiết Phí kết nối không có link mã đơn."));

        String incoming = valueAfterLabel(drawer, "Tổng vào");
        String outgoing = valueAfterLabel(drawer, "Tổng ra");
        String net = valueAfterLabel(drawer, "Dòng tiền ròng");
        List<FeeTimelineEntry> timeline = drawer.findElements(
                        By.cssSelector("a[href*='/vuatho/transaction?']"))
                .stream().filter(WebElement::isDisplayed)
                .map(link -> new FeeTimelineEntry(link.getAttribute("href"), elementText(link),
                        elementText(link).toLowerCase(Locale.ROOT).contains("đang xem"),
                        link.getAttribute("target")))
                .toList();

        String workerHref = workerLink.getAttribute("href");
        String workerTarget = workerLink.getAttribute("target");
        String workerName = elementText(workerLink).trim();
        String workerPhone = workerLink.findElements(By.xpath("./following-sibling::p[1]"))
                .stream().filter(WebElement::isDisplayed).map(this::elementText)
                .findFirst().orElse("").trim();
        String cashFlowHeading = drawer.findElements(By.xpath(
                        ".//h4[starts-with(normalize-space(),'Dòng tiền của')]"))
                .stream().filter(WebElement::isDisplayed).map(this::elementText)
                .findFirst().orElse("").trim();
        String orderHref = orderLink.getAttribute("href");
        String orderTarget = orderLink.getAttribute("target");
        String orderText = elementText(orderLink).trim();
        click(detailCloseButton(), "Đóng chi tiết sau khi kiểm tra element Phí kết nối");
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new FeeConnectionElementSnapshot(opened.source(), opened.url(), opened.drawerText(),
                workerHref, workerTarget, workerName, workerPhone, cashFlowHeading,
                orderHref, orderTarget, orderText,
                incoming, outgoing, net, timeline, closed);
    }

    private String valueAfterLabel(WebElement root, String label) {
        return root.findElements(By.xpath(".//*[normalize-space()='" + label
                        + "']/following-sibling::*[1]"))
                .stream().filter(WebElement::isDisplayed).map(this::elementText)
                .filter(value -> !value.isBlank()).findFirst()
                .orElseThrow(() -> new AssertionError("Không tìm thấy giá trị " + label));
    }

    public StatusDetailSnapshot openAndCloseVisibleDetailForStatus(String expectedStatus) {
        List<WebElement> elements = dataRowElements();
        for (int index = 0; index < elements.size(); index++) {
            TransactionRow source = toRow(elements.get(index));
            if (source == null || !source.status().equals(expectedStatus)) {
                continue;
            }
            click(elements.get(index), "Mở chi tiết dòng hiển thị trạng thái " + expectedStatus);
            WebElement drawer = detailDrawer();
            String openedUrl = driver.getCurrentUrl();
            String text = elementText(drawer);
            click(detailCloseButton(), "Đóng chi tiết trạng thái " + expectedStatus);
            boolean closed = wait.until(d -> visibleElements(
                    By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
            return new StatusDetailSnapshot(expectedStatus, expectedStatus, source,
                    openedUrl, text, false, closed, driver.getCurrentUrl());
        }
        return new StatusDetailSnapshot(expectedStatus, expectedStatus, null,
                "", mainText(), true, false, driver.getCurrentUrl());
    }

    public List<StatusDetailSnapshot> openAndCloseEveryVisibleStatus() {
        List<String> statuses = rows().stream().map(TransactionRow::status)
                .filter(status -> status != null && !status.isBlank()).distinct().toList();
        List<StatusDetailSnapshot> results = new ArrayList<>();
        for (String status : statuses) {
            results.add(openAndCloseVisibleDetailForStatus(status));
        }
        return results;
    }

    public StatusDetailSnapshot openAndCloseFirstDetailForStatus(String expectedStatus) {
        selectStatusForDetail(expectedStatus);
        String selectedStatus = selectedFilterText(Filter.STATUS);
        if (detailEmptyStateVisible()) {
            return new StatusDetailSnapshot(expectedStatus, selectedStatus, null,
                    "", mainText(), true, false, driver.getCurrentUrl());
        }
        List<WebElement> elements = dataRowElementsWithoutWaiting();
        if (elements.isEmpty()) {
            return new StatusDetailSnapshot(expectedStatus, selectedStatus, null,
                    "", mainText(), true, false, driver.getCurrentUrl());
        }
        TransactionRow source = toRow(elements.get(0));
        if (source == null) {
            throw new IllegalStateException("Không đọc được dòng trạng thái " + expectedStatus);
        }
        click(elements.get(0), "Mở chi tiết giao dịch trạng thái " + expectedStatus);
        WebElement drawer = detailDrawer();
        String openedUrl = driver.getCurrentUrl();
        String text = elementText(drawer);
        click(detailCloseButton(), "Đóng chi tiết trạng thái " + expectedStatus);
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new StatusDetailSnapshot(expectedStatus, selectedStatus, source,
                openedUrl, text, false, closed, driver.getCurrentUrl());
    }

    private void selectStatusForDetail(String value) {
        var detailWait = Waits.withTimeout(driver, Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(250));
        detailWait.withMessage("Không mở được bộ lọc trạng thái chi tiết").until(d -> {
            try {
                boolean alreadyOpen = d.findElements(By.cssSelector("li[role='option']"))
                        .stream().anyMatch(WebElement::isDisplayed);
                if (!alreadyOpen) {
                    WebElement trigger = d.findElements(By.cssSelector(
                                    "button[aria-label='" + Filter.STATUS.ariaLabel + "']"))
                            .stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
                    if (trigger == null) {
                        return false;
                    }
                    trigger.click();
                }
                return d.findElements(By.cssSelector("li[role='option']"))
                        .stream().anyMatch(WebElement::isDisplayed);
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
        detailWait.withMessage("Không chọn được trạng thái chi tiết: " + value).until(d -> {
            try {
                WebElement current = d.findElements(By.cssSelector("li[role='option']"))
                        .stream().filter(WebElement::isDisplayed)
                        .filter(element -> elementText(element).trim().equals(value))
                        .findFirst().orElse(null);
                if (current == null) {
                    return false;
                }
                current.click();
                return true;
            } catch (StaleElementReferenceException ignored) {
                // React có thể dựng lại option trong lúc menu đang mở; tìm lại ở lần poll kế tiếp.
                return false;
            }
        });
        pause("Lọc trạng thái detail: " + value);
        detailWait.withMessage("Dữ liệu không ổn định sau khi lọc trạng thái: " + value).until(d -> {
            try {
                String bodyText = normalizeText(d.findElement(By.tagName("body")).getText());
                String selected = elementText(filterButton(Filter.STATUS));
                if (bodyText.contains("dang tai du lieu") || !selected.contains(value)) {
                    return false;
                }
                if (bodyText.contains("chua co du lieu")
                        || bodyText.contains("khong co du lieu")
                        || bodyText.contains("khong tim thay")) {
                    return true;
                }
                List<WebElement> visibleRows = d.findElements(DATA_ROWS).stream()
                        .filter(WebElement::isDisplayed).toList();
                if (visibleRows.isEmpty()) {
                    return true;
                }
                String expected = normalizeText(value);
                return visibleRows.stream()
                        .allMatch(row -> normalizeText(elementText(row)).contains(expected));
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
    }

    private boolean detailEmptyStateVisible() {
        String text = normalizeText(mainText());
        return text.contains("chua co du lieu")
                || text.contains("khong co du lieu")
                || text.contains("khong tim thay");
    }

    private List<WebElement> dataRowElementsWithoutWaiting() {
        List<WebElement> grids = driver.findElements(GRID).stream()
                .filter(WebElement::isDisplayed).toList();
        if (grids.isEmpty()) {
            return List.of();
        }
        return grids.get(0).findElements(DATA_ROWS).stream()
                .filter(WebElement::isDisplayed)
                .filter(row -> row.findElements(By.cssSelector("th,td")).size() >= 5).toList();
    }

    public CloseDetailSnapshot closeDetailWithHeaderIcon() {
        DetailSnapshot opened = openFirstDetail();
        WebElement drawer = detailDrawer();
        WebElement icon = wait.until(d -> drawer.findElements(By.cssSelector("button svg.rotate-45"))
                .stream().filter(WebElement::isDisplayed)
                .map(svg -> svg.findElement(By.xpath("..")))
                .filter(WebElement::isEnabled).findFirst().orElse(null));
        click(icon, "Đóng chi tiết bằng icon X");
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new CloseDetailSnapshot(opened.url(), driver.getCurrentUrl(), closed);
    }

    public CloseDetailSnapshot closeDetailWithEscape() {
        DetailSnapshot opened = openFirstDetail();
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new CloseDetailSnapshot(opened.url(), driver.getCurrentUrl(), closed);
    }

    public ProfileNavigationSnapshot openFirstDetailPartyProfile() {
        DetailSnapshot opened = openFirstDetail();
        WebElement profile = wait.until(d -> detailDrawer().findElements(By.cssSelector(
                        "a[href*='/vuatho/user?id='],a[href*='/vuatho/worker?id=']"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElse(null));
        String expectedUrl = profile.getAttribute("href");
        String sourceText = elementText(profile);
        Set<String> handlesBefore = new HashSet<>(driver.getWindowHandles());
        click(profile, "Mở hồ sơ chủ thể từ chi tiết giao dịch");
        wait.until(d -> !driver.getCurrentUrl().equals(opened.url())
                || d.getWindowHandles().stream().anyMatch(handle -> !handlesBefore.contains(handle)));
        driver.getWindowHandles().stream()
                .filter(handle -> !handlesBefore.contains(handle))
                .findFirst().ifPresent(handle -> driver.switchTo().window(handle));
        wait.until(d -> driver.getCurrentUrl().contains("/vuatho/user?id=")
                || driver.getCurrentUrl().contains("/vuatho/worker?id="));
        return new ProfileNavigationSnapshot(opened.url(), expectedUrl,
                driver.getCurrentUrl(), sourceText);
    }

    public RelatedNavigationSnapshot openFirstRelatedTransaction() {
        DetailSnapshot opened = openDetailMatching((drawer, openedUrl) -> drawer
                .findElements(By.cssSelector("a[href*='/vuatho/transaction?']"))
                .stream().filter(WebElement::isDisplayed)
                .map(link -> link.getAttribute("href"))
                .anyMatch(href -> hasDifferentTransactionId(href, openedUrl)),
                "lien ket toi giao dich lien quan");
        WebElement related = wait.until(d -> detailDrawer()
                .findElements(By.cssSelector("a[href*='/vuatho/transaction?']"))
                .stream().filter(WebElement::isDisplayed)
                .filter(link -> {
                    String href = link.getAttribute("href");
                    return hasDifferentTransactionId(href, opened.url());
                }).findFirst().orElse(null));
        String targetHref = related.getAttribute("href");
        String targetText = elementText(related);
        Set<String> handlesBefore = new HashSet<>(driver.getWindowHandles());
        click(related, "Mở giao dịch dòng tiền liên quan");
        wait.until(d -> !driver.getCurrentUrl().equals(opened.url())
                || d.getWindowHandles().stream().anyMatch(handle -> !handlesBefore.contains(handle)));
        driver.getWindowHandles().stream()
                .filter(handle -> !handlesBefore.contains(handle))
                .findFirst().ifPresent(handle -> driver.switchTo().window(handle));
        wait.until(d -> hasDifferentTransactionId(driver.getCurrentUrl(), opened.url()));
        String drawerText = elementText(detailDrawer());
        return new RelatedNavigationSnapshot(opened.url(), targetHref, driver.getCurrentUrl(),
                targetText, drawerText);
    }

    public ExternalDetailLinkSnapshot openPartyProfileAndReturn() {
        DetailSnapshot opened = openDetailMatching((drawer, url) -> drawer.findElements(
                        By.cssSelector("a[href*='/vuatho/user?id='],a[href*='/vuatho/worker?id=']"))
                .stream().anyMatch(WebElement::isDisplayed), "link hồ sơ người gửi");
        WebElement link = detailDrawer().findElements(By.cssSelector(
                        "a[href*='/vuatho/user?id='],a[href*='/vuatho/worker?id=']"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElseThrow();
        return openExternalDetailLinkAndReturn(opened, link, "/vuatho/", false,
                "Mở hồ sơ người gửi từ chi tiết phí");
    }

    public ExternalDetailLinkSnapshot openOrderAndReturn() {
        DetailSnapshot opened = openDetailMatching((drawer, url) -> drawer.findElements(
                        By.cssSelector("a[href*='/vuatho/order?id=']"))
                .stream().anyMatch(WebElement::isDisplayed), "link mã đơn dịch vụ");
        WebElement link = detailDrawer().findElements(By.cssSelector("a[href*='/vuatho/order?id=']"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElseThrow();
        return openExternalDetailLinkAndReturn(opened, link, "/vuatho/order?id=", false,
                "Mở đơn dịch vụ từ chi tiết phí");
    }

    public ExternalDetailLinkSnapshot openRelatedTransactionAndReturn() {
        DetailSnapshot opened = openDetailMatching((drawer, openedUrl) -> drawer
                        .findElements(By.cssSelector("a[href*='/vuatho/transaction?']"))
                        .stream().filter(WebElement::isDisplayed)
                        .map(link -> link.getAttribute("href"))
                        .anyMatch(href -> hasDifferentTransactionId(href, openedUrl)),
                "link giao dịch timeline khác giao dịch đang xem");
        WebElement link = detailDrawer().findElements(
                        By.cssSelector("a[href*='/vuatho/transaction?']"))
                .stream().filter(WebElement::isDisplayed)
                .filter(candidate -> hasDifferentTransactionId(
                        candidate.getAttribute("href"), opened.url()))
                .findFirst().orElseThrow();
        return openExternalDetailLinkAndReturn(opened, link, "/vuatho/transaction?", false,
                "Mở giao dịch liên quan trong timeline");
    }

    private ExternalDetailLinkSnapshot openExternalDetailLinkAndReturn(
            DetailSnapshot opened, WebElement link, String expectedRoute,
            boolean expectDetailDrawer, String step) {
        String sourceHandle = driver.getWindowHandle();
        Set<String> handlesBefore = new HashSet<>(driver.getWindowHandles());
        String expectedUrl = link.getAttribute("href");
        String linkText = elementText(link).trim();
        click(link, step);
        NavigationTarget target = Waits.withTimeout(driver, Duration.ofSeconds(10)).until(d -> {
            String newHandle = d.getWindowHandles().stream()
                    .filter(handle -> !handlesBefore.contains(handle)).findFirst().orElse(null);
            if (newHandle != null) {
                return new NavigationTarget(newHandle, true);
            }
            return !d.getCurrentUrl().equals(opened.url())
                    ? new NavigationTarget(sourceHandle, false) : null;
        });
        if (target.newWindow()) {
            driver.switchTo().window(target.handle());
        }
        wait.until(d -> d.getCurrentUrl().contains(expectedRoute));
        String actualUrl = driver.getCurrentUrl();
        String targetText = expectDetailDrawer
                ? elementText(restoreDetailDrawerAfterHistory(actualUrl))
                : wait.until(d -> {
                    String text = elementText(d.findElement(By.tagName("body"))).trim();
                    return text.isBlank() ? null : text;
                });
        if (target.newWindow()) {
            driver.close();
            driver.switchTo().window(sourceHandle);
        } else {
            driver.navigate().back();
        }
        try {
            Waits.withTimeout(driver, Duration.ofSeconds(8)).until(d ->
                    d.getCurrentUrl().equals(opened.url()));
        } catch (TimeoutException historyDidNotRestoreSource) {
            driver.get(opened.url());
        }
        WebElement restoredDrawer = restoreDetailDrawerAfterHistory(opened.url());
        boolean sourceRestored = restoredDrawer != null;
        return new ExternalDetailLinkSnapshot(opened.url(), expectedUrl, actualUrl,
                linkText, targetText, driver.getCurrentUrl(), sourceRestored);
    }

    /**
     * Browser history đôi khi chỉ phục hồi URL của drawer nhưng React chưa hydrate lại nội dung.
     * Chờ ngắn cho hành vi tự nhiên trước, sau đó refresh đúng deep-link làm fallback ổn định.
     */
    private WebElement restoreDetailDrawerAfterHistory(String expectedUrl) {
        try {
            return Waits.withTimeout(driver, Duration.ofSeconds(8)).until(d ->
                    visibleElements(By.cssSelector(
                            "[aria-label='drawer-Chi tiết giao dịch']")).stream()
                            .filter(element -> {
                                String text = normalizeText(elementText(element));
                                return !text.isBlank() && text.contains("trang thai")
                                        && !text.contains("dang tai");
                            }).findFirst().orElse(null));
        } catch (TimeoutException historyDidNotHydrateDrawer) {
            driver.get(expectedUrl);
            return detailDrawer();
        }
    }

    private boolean hasDifferentTransactionId(String candidateUrl, String sourceUrl) {
        String candidateId = queryParameter(candidateUrl, "id");
        String sourceId = queryParameter(sourceUrl, "id");
        return !candidateId.isBlank() && !candidateId.equals(sourceId);
    }

    private record NavigationTarget(String handle, boolean newWindow) {}

    private String queryParameter(String url, String name) {
        if (url == null) {
            return "";
        }
        int question = url.indexOf('?');
        if (question < 0 || question == url.length() - 1) {
            return "";
        }
        for (String parameter : url.substring(question + 1).split("&")) {
            String[] pair = parameter.split("=", 2);
            if (pair.length == 2 && pair[0].equals(name)) {
                return pair[1];
            }
        }
        return "";
    }

    public CloseDetailSnapshot closeDetailWithIcon() {
        DetailSnapshot opened = openFirstDetail();
        WebElement drawer = detailDrawer();
        click(overlayCloseButton(drawer), "Đóng drawer bằng icon");
        boolean closed = wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        return new CloseDetailSnapshot(opened.url(), driver.getCurrentUrl(), closed);
    }

    public ChangedDetailSnapshot openAnotherTransactionDetail() {
        DetailSnapshot first = openFirstDetail();
        click(detailCloseButton(), "Đóng giao dịch đầu tiên");
        wait.until(d -> visibleElements(
                By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());

        List<WebElement> elements = dataRowElements();
        for (WebElement element : elements) {
            TransactionRow candidate = toRow(element);
            if (candidate == null || candidate.signature().equals(first.source().signature())) {
                continue;
            }
            click(element, "Mở một giao dịch khác");
            WebElement drawer = detailDrawer();
            return new ChangedDetailSnapshot(first.url(), driver.getCurrentUrl(),
                    first.source(), candidate, elementText(drawer));
        }
        throw new IllegalStateException("Không có giao dịch thứ hai phù hợp để kiểm tra đổi chi tiết.");
    }

    public ExportSnapshot exportAll() {
        int visibleRows = rows().size();
        int total = totalDisplayed();
        Path file = exportCurrentView();
        return new ExportSnapshot(file, visibleRows, total, "");
    }

    /** Chuyển tới trang 2 nhưng giữ nguyên bộ lọc để xác minh export không phụ thuộc trang hiện tại. */
    public int goToSecondPageForExport() {
        int total = totalDisplayed();
        if (!hasPage(2)) {
            throw new org.testng.SkipException(
                    "Không có trang 2 để xác minh export toàn bộ dữ liệu; tổng hiện tại=" + total);
        }
        goToPage(2);
        Assert.assertEquals(activePage(), 2);
        return total;
    }

    public ExportSnapshot exportFilteredStatus() {
        selectFilter(Filter.STATUS, "Thành công");
        int visibleRows = rows().size();
        int total = totalDisplayed();
        Path file = exportCurrentView();
        return new ExportSnapshot(file, visibleRows, total, "Thành công");
    }

    public ExportSnapshot exportFilteredGateway() {
        String gateway = "PAYPAL";
        selectFilter(Filter.GATEWAY, gateway);
        return new ExportSnapshot(exportCurrentView(), rows().size(), totalDisplayed(), gateway);
    }

    public StatusGatewayExportSnapshot exportFilteredStatusAndGateway(
            String status, String gateway) {
        selectFilter(Filter.STATUS, status);
        selectFilter(Filter.GATEWAY, gateway);
        int total = totalDisplayed();
        if (total == 0) {
            throw new org.testng.SkipException("Không có dữ liệu thật cho trạng thái="
                    + status + ", cổng=" + gateway
                    + "; không thể xác minh nội dung file export.");
        }
        return new StatusGatewayExportSnapshot(exportCurrentView(), total, status, gateway);
    }

    public DateExportSnapshot exportFilteredDate() {
        DateTimeFilterSnapshot filter = filterSingleDay();
        return new DateExportSnapshot(exportCurrentView(), rows().size(), totalDisplayed(), filter.startDate());
    }

    public CombinedExportSnapshot exportCombinedFilters() {
        TransactionRow source = rows().stream().filter(row -> !row.type().isBlank())
                .findFirst().orElseThrow(() -> new IllegalStateException("Không có loại giao dịch để lọc."));
        LocalDate date = source.createdAt().toLocalDate();
        applyDateTimeRange(date, date, null, null);
        String status = "Thành công";
        String gateway = "PAYPAL";
        selectFilter(Filter.TYPE, source.type());
        selectFilter(Filter.STATUS, status);
        selectFilter(Filter.GATEWAY, gateway);
        return new CombinedExportSnapshot(exportCurrentView(), rows().size(), totalDisplayed(),
                source.type(), status, gateway, date);
    }

    public int totalDisplayed() {
        String text = driver.findElements(By.xpath(
                        "//*[starts-with(normalize-space(.),'Tổng hiển thị:')"
                                + " and not(.//*[starts-with(normalize-space(.),'Tổng hiển thị:')])]"))
                .stream().filter(WebElement::isDisplayed).map(this::elementText)
                .filter(value -> value.matches("(?s)^Tổng hiển thị:\\s*[0-9.,]+$"))
                .findFirst().orElse("0");
        String digits = text.replaceAll("[^0-9]", "");
        return digits.isBlank() ? 0 : Integer.parseInt(digits);
    }

    private Path exportCurrentView() {
        Path directory = Path.of(TestConfig.downloadDirectory()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IllegalStateException("Không tạo được thư mục tải Excel.", exception);
        }
        Map<Path, FileFingerprint> before = completedFileVersions(directory);
        WebElement exportButton = visible(By.xpath(
                "//main//button[normalize-space()='Xuất Excel'"
                        + " or .//*[normalize-space()='Xuất Excel']]"));
        click(exportButton, "Xuất toàn bộ dữ liệu theo bộ lọc");
        try {
            return Waits.withTimeout(driver, TestConfig.exportDownloadTimeout())
                    .pollingEvery(Duration.ofMillis(300))
                    .until(d -> {
                        String uiError = d.findElements(By.cssSelector(
                                        ".Toastify__toast-body[role='alert']"))
                                .stream().filter(WebElement::isDisplayed)
                                .map(WebElement::getText).map(String::trim)
                                .filter(text -> {
                                    String value = TextNormalizer.normalize(text);
                                    return value.contains("loi")
                                            || value.contains("that bai")
                                            || value.contains("khong the");
                                }).findFirst().orElse(null);
                        if (uiError != null) {
                            throw new IllegalStateException(
                                    "UI báo xuất Excel thất bại: " + uiError);
                        }
                        return completedFileVersions(directory).entrySet().stream()
                                .filter(entry -> !entry.getValue().equals(before.get(entry.getKey())))
                                .map(Map.Entry::getKey).findFirst().orElse(null);
                    });
        } catch (TimeoutException exception) {
            throw new IllegalStateException("Xuất Excel không tạo file tải xuống.", exception);
        }
    }

    private Set<Path> completedFiles(Path directory) {
        try (var files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("(?i).+\\.(xlsx|xls|csv)$"))
                    .collect(Collectors.toSet());
        } catch (IOException exception) {
            return Set.of();
        }
    }

    private Map<Path, FileFingerprint> completedFileVersions(Path directory) {
        return completedFiles(directory).stream().collect(Collectors.toMap(
                path -> path,
                path -> {
                    try {
                        return new FileFingerprint(Files.size(path), Files.getLastModifiedTime(path).toMillis());
                    } catch (IOException exception) {
                        return new FileFingerprint(-1, -1);
                    }
                }));
    }

    private TransactionRow firstRowWithValue(Filter filter) {
        return rows().stream().filter(row -> !valueOf(row, filter).isBlank()).findFirst()
                .orElseThrow(() -> new IllegalStateException("Không có dữ liệu cho filter " + filter));
    }

    private String valueOf(TransactionRow row, Filter filter) {
        return switch (filter) {
            case TYPE -> row.type();
            case STATUS -> row.status();
            case GATEWAY -> row.gateway();
        };
    }

    private TransactionRow toRow(WebElement row) {
        List<String> cells = row.findElements(By.cssSelector("th,td")).stream()
                .map(this::elementText).map(String::trim).toList();
        if (cells.size() < 5 || cells.stream().anyMatch(text -> text.contains("Đang tải dữ liệu"))) {
            return null;
        }
        try {
            List<String> currentHeaders = headers();
            return new TransactionRow(
                    cell(cells, currentHeaders, "Loại giao dịch"),
                    cell(cells, currentHeaders, "Trạng thái"),
                    cell(cells, currentHeaders, "Số tiền"),
                    gatewayCell(cells, currentHeaders),
                    LocalDateTimeParser.parse(cell(cells, currentHeaders, "Ngày tạo"), ROW_DATE),
                    cells);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String gatewayCell(List<String> cells, List<String> currentHeaders) {
        String gateway = cell(cells, currentHeaders, "Cổng thanh toán");
        return gateway.isBlank() ? cell(cells, currentHeaders, "Ngân hàng") : gateway;
    }

    private String cell(List<String> cells, List<String> currentHeaders, String header) {
        int index = currentHeaders.indexOf(header);
        return index >= 0 && index < cells.size() ? cells.get(index) : "";
    }

    private List<WebElement> dataRowElements() {
        waitForTable();
        if (isEmptyState()) {
            return List.of();
        }
        WebDriverWait rowWait = Waits.withTimeout(driver, Duration.ofSeconds(10));
        rowWait.withMessage("Bảng giao dịch đã xuất hiện nhưng chưa có dòng dữ liệu hợp lệ tại "
                + driver.getCurrentUrl());
        return rowWait.until(d -> {
            try {
                List<WebElement> grids = d.findElements(GRID);
                if (grids.isEmpty()) {
                    return null;
                }
                List<WebElement> rows = grids.get(0).findElements(DATA_ROWS).stream()
                        .filter(row -> row.findElements(By.cssSelector("th,td")).size() >= 5)
                        .toList();
                return rows.isEmpty() ? null : rows;
            } catch (WebDriverException transientDomRead) {
                return retryTransientDomRead(transientDomRead);
            }
        });
    }

    private void selectFilter(Filter filter, String value) {
        openFilter(filter);
        wait.until(d -> {
            try {
                WebElement option = d.findElements(By.cssSelector("li[role='option']")).stream()
                        .filter(WebElement::isDisplayed)
                        .filter(element -> elementText(element).trim().equals(value))
                        .findFirst().orElse(null);
                if (option == null) {
                    return false;
                }
                option.click();
                return true;
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
        pause("Chọn " + filter.label + ": " + value);
        waitForTable();
    }

    private void openFilter(Filter filter) {
        click(filterButton(filter), "Mở " + filter.label);
        wait.until(d -> visibleElements(By.cssSelector("li[role='option']")).isEmpty() ? null : true);
    }

    private WebElement filterButton(Filter filter) {
        return visible(By.cssSelector("button[aria-label='" + filter.ariaLabel + "']"));
    }

    private String selectedFilterText(Filter filter) {
        return elementText(filterButton(filter)).trim();
    }

    private WebElement dateButton() {
        return visible(By.cssSelector("button[aria-label='Chọn khoảng ngày giờ giao dịch']"));
    }

    private void clickCalendarDate(LocalDate date, String step) {
        navigateCalendarTo(date);
        String full = calendarAria(date, true);
        String stableDate = full.substring(full.indexOf(',') + 2);
        By locator = By.xpath("//*[@role='option' and contains(@aria-label,"
                + xpathLiteral(stableDate) + ") and @aria-disabled='false']");
        wait.until(d -> {
            try {
                WebElement current = d.findElements(locator).stream()
                        .filter(WebElement::isDisplayed).findFirst().orElse(null);
                if (current == null) {
                    return false;
                }
                current.click();
                return true;
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
        pause(step);
    }

    private void navigateCalendarTo(LocalDate date) {
        String full = calendarAria(date, true);
        String stableDate = full.substring(full.indexOf(',') + 2);
        By target = By.xpath("//*[@role='option' and contains(@aria-label,"
                + xpathLiteral(stableDate) + ") and @aria-disabled='false']");
        for (int attempt = 0; attempt < 60; attempt++) {
            if (driver.findElements(target).stream().anyMatch(WebElement::isDisplayed)) {
                return;
            }
            clickStable(By.cssSelector("button[aria-label='Previous Month']"),
                    "Chuyển lịch đến tháng của giao dịch");
        }
        throw new IllegalStateException("Không chuyển được lịch đến tháng " + YearMonth.from(date));
    }

    private void clickStable(By locator, String step) {
        wait.until(d -> {
            try {
                WebElement current = d.findElements(locator).stream()
                        .filter(WebElement::isDisplayed).findFirst().orElse(null);
                if (current == null) {
                    return false;
                }
                current.click();
                return true;
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
        pause(step);
    }

    private void applyDateTimeRange(LocalDate startDate, LocalDate endDate,
                                    LocalTime startTime, LocalTime endTime) {
        click(dateButton(), "Mở bộ lọc ngày giờ");
        clickCalendarDate(startDate, "Chọn ngày bắt đầu");
        clickCalendarDate(endDate, "Chọn ngày kết thúc");
        if (startTime != null && endTime != null) {
            List<WebElement> inputs = visibleElements(By.cssSelector("input[type='time']"));
            setTimeInput(inputs.get(0), startTime.toString());
            setTimeInput(inputs.get(1), endTime.toString());
        }
        clickStable(By.xpath("//button[normalize-space()='Áp dụng']"), "Áp dụng khoảng ngày giờ");
        waitForTable();
    }

    private void setTimeInput(WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "const setter=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;"
                        + "setter.call(arguments[0],arguments[1]);"
                        + "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));"
                        + "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                input, value);
    }

    private WebElement dateApplyButton() {
        return visible(By.xpath("//button[normalize-space()='Áp dụng']"));
    }

    private String calendarAria(LocalDate date, boolean available) {
        String prefix = available ? "Choose " : "Not available ";
        String day = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int number = date.getDayOfMonth();
        int mod100 = number % 100;
        String suffix = mod100 >= 11 && mod100 <= 13 ? "th" : switch (number % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
        return prefix + day + ", " + month + " " + number + suffix + ", " + date.getYear();
    }

    private String cssString(String value) {
        return "'" + value.replace("'", "\\'") + "'";
    }

    private WebElement headerButton(String label) {
        return visible(By.xpath("//*[@role='grid']//thead//button[normalize-space()='" + label + "']"));
    }

    private void goToPage(int page) {
        if (activePage() == page) {
            return;
        }
        requirePage(page);
        if (!hasPage(page)) {
            throw new AssertionError("Phân trang báo có trang " + page
                    + " nhưng không hiển thị control tương ứng.");
        }
        WebElement button = visible(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label='pagination item "
                        + page + "']"));
        click(button, "Chuyển sang trang " + page);
        settle(1200);
        waitForTable();
    }

    private void requirePage(int page) {
        if (paginationTotalPages() < page) {
            throw new org.testng.SkipException("Danh sách hiện tại không đủ trang " + page
                    + " để thực hiện flow phân trang.");
        }
    }

    private void clickPaginationControl(String ariaLabel, String step) {
        WebElement control = visible(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label='"
                        + ariaLabel + "']"));
        if ("true".equals(control.getAttribute("aria-disabled"))
                || "true".equals(control.getAttribute("data-disabled"))) {
            throw new IllegalStateException("Không thể " + step + ": control đang bị khóa.");
        }
        click(control, step);
        settle(1200);
        waitForTable();
    }

    private boolean paginationControlDisabled(String ariaLabel) {
        WebElement control = visible(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label='"
                        + ariaLabel + "']"));
        return "true".equals(control.getAttribute("aria-disabled"))
                || "true".equals(control.getAttribute("data-disabled"));
    }

    private int paginationTotalPages() {
        List<WebElement> navigations = visibleElements(
                By.cssSelector("nav[aria-label='pagination navigation']"));
        if (navigations.isEmpty()) {
            return 1;
        }
        String total = navigations.get(0).getAttribute("data-total");
        if (total != null && total.matches("\\d+")) {
            return Math.max(1, Integer.parseInt(total));
        }
        return navigations.get(0).findElements(By.cssSelector(
                        "[role='button'][aria-label^='pagination item']"))
                .stream().map(element -> element.getAttribute("aria-label"))
                .map(label -> label == null ? "" : label.replaceAll("[^0-9]", ""))
                .filter(value -> !value.isBlank()).mapToInt(Integer::parseInt)
                .max().orElse(1);
    }

    private boolean hasPage(int page) {
        return !visibleElements(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label='pagination item "
                        + page + "']")).isEmpty();
    }

    private int activePage() {
        String aria = visibleElements(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label*='pagination item'][aria-label*='active']"))
                .stream().map(element -> element.getAttribute("aria-label")).findFirst().orElse("1");
        String digits = aria.replaceAll("[^0-9]", "");
        return digits.isBlank() ? 1 : Integer.parseInt(digits);
    }

    private void resetFilters() {
        click(visible(By.cssSelector("button[title='Reset']")), "Reset bộ lọc");
        waitForTable();
    }

    private boolean isEmptyState() {
        String normalized = normalizeText(mainText());
        return normalized.contains("chua co du lieu")
                || normalized.contains("khong co du lieu")
                || normalized.contains("khong tim thay");
    }

    private boolean paginationVisible() {
        return !visibleElements(By.cssSelector("nav[aria-label='pagination navigation']")).isEmpty();
    }

    private boolean visibleButtonText(String text) {
        return !visibleElements(By.xpath("//main//button[normalize-space(.)=" + xpathLiteral(text)
                + " or .//*[normalize-space()=" + xpathLiteral(text) + "]]")).isEmpty();
    }

    private boolean isAllTabSelected() {
        WebElement button = visible(By.xpath("//main//button[normalize-space()='Tất cả']"));
        String classes = (button.getAttribute("class") + " "
                + button.findElement(By.xpath("..")).getAttribute("class")).toLowerCase(Locale.ROOT);
        return driver.getCurrentUrl().contains("tab=all")
                && (classes.contains("primary") || classes.contains("bg-white")
                || classes.contains("shadow"));
    }

    private WebElement grid() {
        return visible(GRID);
    }

    private void waitForTable() {
        settle(650);
        wait.until(d -> {
            try {
                List<WebElement> grids = d.findElements(GRID);
                if (grids.isEmpty()) {
                    return isEmptyState();
                }
                String text = elementText(grids.get(0));
                return !text.contains("Đang tải dữ liệu")
                        && (!grids.get(0).findElements(DATA_ROWS).isEmpty() || isEmptyState());
            } catch (WebDriverException transientDomRead) {
                return retryTransientDomRead(transientDomRead);
            }
        });
    }

    /**
     * React thay thế grid trong lúc chuyển trang có thể làm một lệnh đọc DOM ngắn bị stale
     * hoặc mất kết nối DevTools tạm thời. Chỉ retry các lỗi chuyển tiếp; session đã chết vẫn
     * phải fail ngay để không che khuất lỗi hạ tầng thật.
     */
    private <T> T retryTransientDomRead(WebDriverException error) {
        String message = error.getMessage() == null ? "" : error.getMessage().toLowerCase(Locale.ROOT);
        boolean retryable = error instanceof StaleElementReferenceException
                || message.contains("error communicating with the remote browser")
                || message.contains("target frame detached")
                || message.contains("no such execution context");
        boolean deadSession = message.contains("invalid session id")
                || message.contains("session deleted")
                || message.contains("chrome not reachable")
                || message.contains("disconnected: not connected to devtools");
        if (retryable && !deadSession) {
            return null;
        }
        throw error;
    }

    private WebElement detailDrawer() {
        By locator = By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        return wait.until(d -> d.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> {
                    String text = normalizeText(elementText(element));
                    return !text.isBlank() && !text.contains("dang tai")
                            && text.contains("trang thai");
                }).findFirst().orElse(null));
    }

    private WebElement detailCloseButton() {
        return wait.until(d -> {
            List<WebElement> drawers = d.findElements(
                    By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']"));
            for (WebElement drawer : drawers) {
                if (!drawer.isDisplayed()) {
                    continue;
                }
                try {
                    return overlayCloseButton(drawer);
                } catch (RuntimeException ignored) {
                    return null;
                }
            }
            return null;
        });
    }

    public enum Filter {
        TYPE("loại giao dịch-filter", "loại giao dịch"),
        STATUS("trạng thái-filter", "trạng thái"),
        GATEWAY("cổng thanh toán-filter", "cổng thanh toán");

        private final String ariaLabel;
        private final String label;

        Filter(String ariaLabel, String label) {
            this.ariaLabel = ariaLabel;
            this.label = label;
        }
    }

    public record TransactionRow(String type, String status, String amount, String gateway,
                                 java.time.LocalDateTime createdAt, List<String> cells) {
        public BigDecimal amountValue() {
            String raw = amount == null ? "" : amount.trim()
                    .replace('\u2212', '-')
                    .replace('\u2013', '-');
            boolean negative = raw.contains("-") || (raw.contains("(") && raw.contains(")"));
            String numeric = raw.replaceAll("[^0-9.,]", "");
            if (numeric.isBlank()) {
                return BigDecimal.ZERO;
            }

            int lastSeparator = Math.max(numeric.lastIndexOf(','), numeric.lastIndexOf('.'));
            int fractionLength = lastSeparator < 0 ? 0 : numeric.length() - lastSeparator - 1;
            boolean hasDecimalFraction = fractionLength > 0 && fractionLength <= 2;

            String integerDigits = (hasDecimalFraction ? numeric.substring(0, lastSeparator) : numeric)
                    .replaceAll("[^0-9]", "");
            String fractionDigits = hasDecimalFraction
                    ? numeric.substring(lastSeparator + 1).replaceAll("[^0-9]", "")
                    : "";
            String normalized = integerDigits.isBlank() ? "0" : integerDigits;
            if (!fractionDigits.isBlank()) {
                normalized += "." + fractionDigits;
            }

            BigDecimal value = new BigDecimal(normalized);
            return negative ? value.negate() : value;
        }

        public String signature() {
            return String.join("|", cells);
        }
    }

    public record DateExportSnapshot(Path file, int visibleRows, int totalRows, LocalDate date) {}

    public record CombinedExportSnapshot(Path file, int visibleRows, int totalRows,
                                          String type, String status, String gateway,
                                          LocalDate date) {}

    public record StatusGatewayExportSnapshot(Path file, int totalRows,
                                               String status, String gateway) {}
    private record FileFingerprint(long size, long modifiedAt) {}

    public record OverviewSnapshot(String url, boolean allSelected, List<String> tabs,
                                   List<String> controls, List<String> headers,
                                   List<TransactionRow> rows, int total, boolean pagination) {}
    public record FilterSnapshot(Filter filter, String value, List<TransactionRow> rows,
                                 String selectedText, String url) {}
    public record CombinedFilterSnapshot(TransactionRow source, List<TransactionRow> rows,
                                          String type, String status, String gateway) {}
    public record SpecializedCombinedFilterSnapshot(String status, String gateway,
                                                     List<TransactionRow> rows,
                                                     String selectedStatus,
                                                     String selectedGateway,
                                                      boolean empty, String pageText) {}
    public record CombinedFilterOptionResult(String status, String gateway,
                                             String selectedStatus, String selectedGateway,
                                             List<TransactionRow> rows,
                                             boolean empty, String pageText) {}
    public record CombinedFilterMatrixSnapshot(List<String> statuses, List<String> gateways,
                                               List<CombinedFilterOptionResult> results,
                                               String url) {}
    public record SpecializedCombinedDateFilterSnapshot(TransactionRow source, String status,
                                                         String gateway,
                                                         LocalDate date,
                                                         List<TransactionRow> rows,
                                                         String selectedStatus,
                                                         String selectedGateway,
                                                         String selectedDate, boolean empty,
                                                         String pageText, String url) {}
    public record SpecializedResetSnapshot(String status, String gateway, String date,
                                           int page, int rows, String url) {}
    public record SpecializedHiddenFilterSnapshot(boolean typeHidden, boolean invoiceHidden,
                                                   boolean warrantyHidden) {}
    public record FutureDateSnapshot(String ariaLabel, boolean disabled, int disabledCount) {}
    public record DateFilterSnapshot(LocalDate start, LocalDate end, List<TransactionRow> rows,
                                     String selectedText, boolean empty, String pageText) {}
    public record EmptySnapshot(String type, String gateway, String pageText, boolean empty) {}
    public record ResetSnapshot(boolean allSelected, String type, String status, String gateway,
                                String date, int page, int rows, String url) {}
    public record FilterChangeSnapshot(String firstValue, String secondValue,
                                       List<TransactionRow> firstRows, List<TransactionRow> secondRows,
                                       String selectedText) {}
    public record FilterOptionResult(String value, String selectedText,
                                     List<TransactionRow> rows,
                                     boolean empty, String pageText) {}
    public record FilterOptionsSnapshot(Filter filter, List<String> options,
                                        List<FilterOptionResult> results) {}
    public record FilterPopupSemanticsSnapshot(Filter filter, String expandedBefore,
                                               String expandedAfter, String hasPopup,
                                               String listboxRole, String labelledBy,
                                               List<String> options) {}
    public record CalendarNavigationSnapshot(List<String> monthsBefore,
                                             List<String> monthsAfter,
                                             int timeInputs, String popupText) {}
    public record InvalidTimeRangeSnapshot(LocalDate date, String startTime,
                                           String endTime, boolean applyEnabled,
                                           String popupText) {}
    public record CombinedSearchFilterSnapshot(TransactionRow source, String query,
                                               LocalDate date,
                                               String selectedStatus,
                                               String selectedGateway,
                                               String selectedDate,
                                               List<TransactionRow> rows,
                                               String url, boolean restoredOrPersisted,
                                               boolean empty, String pageText) {}
    public record UnchangedFilterSnapshot(String selectedBefore, String selectedAfter,
                                          List<String> rowsBefore, List<String> rowsAfter) {}
    public record DateControlSnapshot(String startTime, String endTime,
                                      boolean applyDisabled) {}
    public record DateRequirementSnapshot(boolean initiallyDisabled) {}
    public record DateTimeFilterSnapshot(LocalDate startDate, LocalDate endDate,
                                         LocalTime startTime, LocalTime endTime,
                                         List<TransactionRow> rows, String selectedText) {}
    public record CombinedDateFilterSnapshot(TransactionRow source, LocalDate date,
                                             List<TransactionRow> rows, String type,
                                             String status, String gateway, String selectedDate) {}
    public record FilterTotalSnapshot(int beforeTotal, int afterTotal,
                                      boolean beforePagination, boolean afterPagination,
                                      List<TransactionRow> rows, String selectedStatus) {}
    public record DetailFilterPersistenceSnapshot(String expectedStatus, String selectedStatus,
                                                  List<String> rowsBefore, List<String> rowsAfter,
                                                  String listUrlBefore, String openedUrl,
                                                  String closedUrl, String browserLocationAfterClose) {
        /**
         * Giữ tương thích với các testcase cũ vốn đọc URL sau khi đóng chi tiết qua url().
         */
        public String url() {
            return closedUrl;
        }
    }
    public record DateClearSnapshot(int originalTotal, int filteredTotal, int restoredTotal,
                                    String selectedDate, int rows, boolean clearControlGone) {}
    public record HiddenFilterSnapshot(boolean searchHidden, boolean invoiceHidden,
                                       boolean warrantyHidden) {}
    public record SortSnapshot(String header, List<TransactionRow> rows) {}
    public record NonSortableSnapshot(List<String> expectedHeaders, List<String> nonSortableHeaders,
                                      List<String> rowsBefore, List<String> rowsAfter) {}
    public record FirstPageControlSnapshot(int activePage, boolean previousDisabled,
                                           boolean nextDisabled, int totalPages) {}
    public record PaginationControlSnapshot(int pageBefore, int pageAfter,
                                            List<TransactionRow> rowsBefore,
                                            List<TransactionRow> rowsAfter) {}
    public record ActivePageSnapshot(int activePage, String dataActivePage,
                                     String ariaCurrent) {}
    public record LastPageSnapshot(int expectedLastPage, int activePage,
                                   List<TransactionRow> rows, boolean nextDisabled) {}
    public record AdjacentPagesSnapshot(List<TransactionRow> firstPage,
                                        List<TransactionRow> secondPage, int activePage) {}
    public record SortedPaginationSnapshot(List<TransactionRow> firstPage,
                                           List<TransactionRow> secondPage,
                                           List<TransactionRow> returnedFirstPage,
                                           int activePage) {}
    public record FilterFromLaterPageSnapshot(String expectedStatus, String selectedStatus,
                                              int activePage, List<TransactionRow> rows) {}
    public record DetailFromLaterPageSnapshot(String openedUrl, int activePage,
                                              List<String> rowsBefore, List<String> rowsAfter,
                                              String closedUrl) {}
    public record ResetSortSnapshot(List<String> baselineRows, List<String> sortedRows,
                                    List<String> restoredRows, int activePage) {}
    public record RefreshPageSnapshot(int pageBeforeRefresh, int pageAfterRefresh,
                                      List<TransactionRow> baselineRows,
                                      List<TransactionRow> rows, String url) {}
    public record ResetSortedPageSnapshot(List<String> baselineRows,
                                          List<String> ascendingRows,
                                          List<String> descendingRows,
                                          List<String> restoredRows,
                                          int pageBeforeReset, int pageAfterReset,
                                          String url) {}
    public record DotsJumpSnapshot(int pageBefore, int pageAfter,
                                   List<TransactionRow> rows) {}
    public record DotsRoundTripSnapshot(int startPage, int forwardPage,
                                        int backwardPage, List<TransactionRow> rows) {}
    public record LastPagePreviousSnapshot(int lastPage, int pageAfterPrevious,
                                           List<String> lastPageRows,
                                           List<String> previousPageRows,
                                           boolean nextDisabled,
                                           boolean previousDisabled) {}
    public record PaginationGeometrySnapshot(int totalRows, int totalPages,
                                             int firstPageRows, int lastPageRows,
                                             int activePage) {}
    public record CombinedFilterPaginationSnapshot(String status, String gateway,
                                                   String selectedStatus,
                                                   String selectedGateway,
                                                   List<TransactionRow> firstPage,
                                                   List<TransactionRow> secondPage,
                                                   int activePage) {}
    public record EmptyPaginationSnapshot(String query, boolean empty,
                                          boolean paginationVisible, int totalRows,
                                          String pageText) {}
    public record PaginationSemanticsSnapshot(String navigationLabel,
                                              String previousRole,
                                              String previousLabel,
                                              String nextRole, String nextLabel,
                                              int totalPages, int pageItemCount,
                                              int dotsCount,
                                              boolean allPageItemsNamed,
                                              boolean allDotsNamed) {}
    public record SortDetailPersistenceSnapshot(String openedUrl,
                                                List<TransactionRow> rowsBefore,
                                                List<TransactionRow> rowsAfter,
                                                int activePage, String closedUrl) {}
    public record PaginationSnapshot(List<TransactionRow> pageOne, List<TransactionRow> pageTwo,
                                     List<TransactionRow> returnedPageOne, int activePage) {}
    public record FilterPaginationSnapshot(String expectedStatus, String selectedStatus,
                                           List<TransactionRow> pageOne, List<TransactionRow> pageTwo,
                                           int activePage) {}
    public record GatewayPaginationCell(String gateway, String selectedGateway,
                                        List<TransactionRow> pageOne,
                                        List<TransactionRow> pageTwo,
                                        boolean pageTwoAvailable) {}
    public record GatewayPaginationSnapshot(List<String> gateways,
                                            List<GatewayPaginationCell> results,
                                            String url) {}
    public record BrowserPageHistorySnapshot(String pageOneUrl, String pageTwoUrl,
                                             String backUrl, String forwardUrl,
                                             List<String> pageOneRows, List<String> pageTwoRows,
                                             List<String> backRows, List<String> forwardRows,
                                             int backActivePage, int activePage) {}
    public record RefreshSortedFilterSnapshot(String expectedStatus, String selectedStatus,
                                              List<TransactionRow> expectedAfterRefresh,
                                              List<TransactionRow> rowsBefore,
                                              List<TransactionRow> rowsAfter,
                                              int activePage, String url) {}
    public record DetailSnapshot(TransactionRow source, String url, String drawerText,
                                 int relatedLinks, boolean currentMarked) {}
    public record CloseDetailSnapshot(String openedUrl, String closedUrl, boolean closed) {}
    public record DeepLinkSnapshot(String expectedUrl, String actualUrl, String drawerText) {}
    public record HistoryNavigationSnapshot(String openedUrl, String backUrl, String forwardUrl,
                                            boolean closedAfterBack, String drawerText) {}
    public record RelatedHistorySnapshot(TransactionRow source, String drawerText,
                                         boolean loaded, boolean currentMarked,
                                         boolean currentListed) {
        public RelatedHistorySnapshot(TransactionRow source, String drawerText,
                                      boolean loaded, boolean currentMarked) {
            this(source, drawerText, loaded, currentMarked, false);
        }
    }
    public record RelatedExpansionSnapshot(String openedUrl, int beforeCount, int afterCount,
                                           String drawerText) {}
    public record DetailAuditSnapshot(TransactionRow source, String openedUrl, String drawerText,
                                      String userHref, List<String> transactionHrefs,
                                      int beforeRelatedCount, int afterRelatedCount,
                                      boolean currentMarked, boolean closed) {}
    public record DetailActionSnapshot(TransactionRow source, String openedUrl,
                                       boolean cancelVisible, boolean rejectPresent,
                                       boolean rejectVisible, boolean closed,
                                       String closedUrl) {}
    public record SystemDetailElementSnapshot(TransactionRow source, String openedUrl,
                                              String drawerText, String userHref,
                                              String userText,
                                              List<DetailElementLink> transactionLinks,
                                              boolean closed, String closedUrl) {}
    public record DetailControlState(String label, boolean present, boolean visible,
                                     boolean enabled) {}
    public record DetailControlSnapshot(TransactionRow source, String openedUrl,
                                        List<DetailControlState> controls,
                                        String safeClickLabel, boolean safeActionPerformed,
                                        boolean closed) {}
    public record WarrantyOrdersSnapshot(TransactionRow source, String openedUrl,
                                         String cardText, String beforeText, String afterText,
                                         boolean stayedOpen, boolean closed) {}
    public record RejectedTransactionLinkSnapshot(String sourceUrl, String expectedUrl,
                                                  String actualUrl, String linkText,
                                                  String drawerText) {}
    public record CloseAccessibilitySnapshot(String openedUrl, String ariaLabel,
                                             String title, String text, boolean closed) {}
    public record DetailLinksSnapshot(String openedUrl, String userHref,
                                       List<String> transactionHrefs, String drawerText) {}
    public record DetailElementLink(String href, String target, String text) {}
    public record OrderDetailElementSnapshot(TransactionRow source, String openedUrl,
                                             String drawerText, List<String> visibleButtons,
                                             boolean rejectPresent, boolean rejectVisible,
                                             boolean confirmPresent, boolean confirmVisible,
                                             boolean confirmDisabled, List<String> imageAccepts,
                                             int visibleCopyButtons, boolean qrButtonVisible,
                                             List<DetailElementLink> userLinks,
                                             List<DetailElementLink> orderLinks,
                                             List<DetailElementLink> transactionLinks,
                                             List<DetailElementLink> withdrawalLinks,
                                             boolean walletConfirmPresent,
                                             boolean walletConfirmVisible,
                                             boolean walletConfirmDisabled,
                                             boolean currentMarked, boolean closed,
                                             String closedUrl) {}
    public record OrderQrCopyInteractionSnapshot(TransactionRow source, String openedUrl,
                                                 int copyButtonCount, int copyClicks,
                                                 boolean drawerStayedOpenAfterCopy,
                                                 boolean qrOpened, String qrViewText,
                                                 boolean drawerStayedOpenAfterQr,
                                                 boolean closed, String closedUrl) {}
    public record OrderBillUploadSnapshot(TransactionRow source, String openedUrl,
                                          String accept, boolean disabledBeforeUpload,
                                          boolean enabledAfterUpload, boolean closed,
                                          String closedUrl) {}
    public record OrderCancelSnapshot(TransactionRow source, String openedUrl,
                                      boolean closed, String closedUrl,
                                      boolean sourceStillPending) {}
    public record FeeTimelineEntry(String href, String text, boolean current, String target) {}
    public record FeeConnectionElementSnapshot(TransactionRow source, String openedUrl,
                                               String drawerText, String workerHref,
                                               String workerTarget, String workerName,
                                               String workerPhone, String cashFlowHeading,
                                               String orderHref,
                                               String orderTarget, String orderText,
                                               String incoming, String outgoing, String net,
                                               List<FeeTimelineEntry> timeline, boolean closed) {}
    public record StatusDetailSnapshot(String expectedStatus, String selectedStatus,
                                       TransactionRow source, String openedUrl, String drawerText,
                                       boolean empty, boolean closed, String closedUrl) {}
    public record ProfileNavigationSnapshot(String sourceUrl, String expectedUrl,
                                            String actualUrl, String sourceText) {}
    public record RelatedNavigationSnapshot(String sourceUrl, String expectedUrl,
                                            String actualUrl, String sourceText,
                                            String drawerText) {}
    public record ExternalDetailLinkSnapshot(String sourceUrl, String expectedUrl,
                                             String actualUrl, String linkText,
                                             String targetText, String returnedUrl,
                                             boolean sourceRestored) {}
    public record ChangedDetailSnapshot(String firstUrl, String secondUrl,
                                        TransactionRow firstSource, TransactionRow secondSource,
                                        String secondDrawerText) {}
    public record ExportSnapshot(Path file, int visibleRows, int totalRows, String filterValue) {}

    private static final class LocalDateTimeParser {
        private LocalDateTimeParser() {}

        private static java.time.LocalDateTime parse(String value, DateTimeFormatter formatter) {
            return java.time.LocalDateTime.parse(value.trim(), formatter);
        }
    }
}
