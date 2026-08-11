package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        return grid().findElements(DATA_ROWS).stream()
                .filter(WebElement::isDisplayed)
                .map(this::toRow)
                .filter(row -> row != null)
                .toList();
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
        return new DateFilterSnapshot(start, end, rows(), dateButton().getText().trim());
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

    public FilterOptionsSnapshot applyEveryFilterOption(Filter filter) {
        List<String> options = filterOptions(filter);
        List<FilterOptionResult> results = new ArrayList<>();
        for (String option : options) {
            selectFilter(filter, option);
            results.add(new FilterOptionResult(option, selectedFilterText(filter), rows()));
            resetFilters();
        }
        return new FilterOptionsSnapshot(filter, options, results);
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
        List<WebElement> inputs = visibleElements(By.cssSelector("input[type='time']"));
        if (inputs.size() < 2) {
            throw new IllegalStateException("Bộ lọc ngày thiếu hai ô giờ Từ/Đến.");
        }
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
        DateTimeFilterSnapshot filtered = filterSingleDay();
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
        for (int attempt = 1; attempt <= 3; attempt++) {
            click(headerButton(header), "Sắp xếp " + header);
            waitForTable();
            List<TransactionRow> currentRows = rows();
            if (rowsAreSorted(currentRows, header, descending)) {
                return new SortSnapshot(header, currentRows);
            }
        }
        List<TransactionRow> observedRows = rows();
        String observed = normalizeText(header).contains("ngay tao")
                ? observedRows.stream().map(TransactionRow::createdAt).toList().toString()
                : observedRows.stream().map(TransactionRow::amountValue).toList().toString();
        throw new AssertionError("Không thể đưa cột " + header + " về chiều "
                + (descending ? "giảm dần" : "tăng dần") + ". Thứ tự hiện tại: " + observed);
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
        List<String> headers = List.of("Loại giao dịch", "Trạng thái", "Cổng thanh toán");
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
                paginationControlDisabled("next page button"));
    }

    public PaginationControlSnapshot nextControlChangesPage() {
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
        List<TransactionRow> first = rows();
        clickPaginationControl("next page button", "Chuyển sang trang liền sau");
        return new AdjacentPagesSnapshot(first, rows(), activePage());
    }

    public SortedPaginationSnapshot descendingAmountAcrossPages(boolean returnToFirst) {
        sort("Số tiền", true);
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

    public DotsJumpSnapshot jumpWithDots() {
        int before = activePage();
        WebElement dots = visible(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label='dots element']"));
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

    public ResetSnapshot resetFromSecondPage() {
        goToPage(2);
        resetFilters();
        return new ResetSnapshot(isAllTabSelected(), selectedFilterText(Filter.TYPE),
                selectedFilterText(Filter.STATUS), selectedFilterText(Filter.GATEWAY),
                dateButton().getText().trim(), activePage(), rows().size(), driver.getCurrentUrl());
    }

    public DetailSnapshot openFirstDetail() {
        List<WebElement> elements = dataRowElements();
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
                WebElement matched = new WebDriverWait(driver, Duration.ofSeconds(4)).until(d -> {
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
        DetailSnapshot opened = openFirstDetail();
        driver.navigate().back();
        boolean closedAfterBack = wait.until(d ->
                visibleElements(By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        String backUrl = driver.getCurrentUrl();
        driver.navigate().forward();
        WebElement reopened = detailDrawer();
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
                return new RelatedHistorySnapshot(opened.source(), text, true, true);
            }
            WebElement more = drawer.findElements(By.xpath(".//button[normalize-space()='Xem thêm']"))
                    .stream().filter(WebElement::isDisplayed).filter(WebElement::isEnabled)
                    .findFirst().orElse(null);
            if (more == null) {
                return new RelatedHistorySnapshot(opened.source(), text,
                        text.contains("Dòng tiền"), false);
            }
            int beforeLength = text.length();
            click(more, "Tải thêm lịch sử dòng tiền");
            wait.until(d -> elementText(detailDrawer()).length() > beforeLength);
            drawer = detailDrawer();
        }
        String text = elementText(drawer);
        return new RelatedHistorySnapshot(opened.source(), text, true,
                text.toLowerCase(Locale.ROOT).contains("đang xem"));
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
        String userHref = drawer.findElements(By.cssSelector("a[href*='/vuatho/user?id=']"))
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

    public DetailLinksSnapshot detailLinks() {
        DetailSnapshot opened = openFirstDetail();
        WebElement drawer = detailDrawer();
        String userHref = drawer.findElements(By.cssSelector("a[href*='/vuatho/user?id=']"))
                .stream().filter(WebElement::isDisplayed)
                .map(link -> link.getAttribute("href")).findFirst().orElse("");
        List<String> transactionHrefs = drawer.findElements(
                        By.cssSelector("a[href*='/vuatho/transaction?']"))
                .stream().filter(WebElement::isDisplayed)
                .map(link -> link.getAttribute("href")).filter(value -> value != null).toList();
        return new DetailLinksSnapshot(opened.url(), userHref, transactionHrefs,
                elementText(drawer));
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
        var detailWait = new WebDriverWait(driver, Duration.ofSeconds(10))
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
        WebElement drawer = detailDrawer();
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

    private boolean hasDifferentTransactionId(String candidateUrl, String sourceUrl) {
        String candidateId = queryParameter(candidateUrl, "id");
        String sourceId = queryParameter(sourceUrl, "id");
        return !candidateId.isBlank() && !candidateId.equals(sourceId);
    }

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
        WebElement icon = drawer.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed).filter(WebElement::isEnabled)
                .filter(button -> elementText(button).isBlank()).findFirst()
                .orElseThrow(() -> new IllegalStateException("Không thấy icon đóng drawer."));
        click(icon, "Đóng drawer bằng icon");
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

    public ExportSnapshot exportFilteredStatus() {
        selectFilter(Filter.STATUS, "Thành công");
        int visibleRows = rows().size();
        int total = totalDisplayed();
        Path file = exportCurrentView();
        return new ExportSnapshot(file, visibleRows, total, "Thành công");
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
            return new WebDriverWait(driver, TestConfig.exportDownloadTimeout())
                    .pollingEvery(Duration.ofMillis(300))
                    .until(d -> completedFileVersions(directory).entrySet().stream()
                            .filter(entry -> !entry.getValue().equals(before.get(entry.getKey())))
                            .map(Map.Entry::getKey).findFirst().orElse(null));
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
        return grid().findElements(DATA_ROWS).stream().filter(WebElement::isDisplayed)
                .filter(row -> row.findElements(By.cssSelector("th,td")).size() >= 5).toList();
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
        WebElement button = visible(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label='pagination item "
                        + page + "']"));
        click(button, "Chuyển sang trang " + page);
        settle(1200);
        waitForTable();
    }

    private void clickPaginationControl(String ariaLabel, String step) {
        WebElement control = visible(By.cssSelector(
                "nav[aria-label='pagination navigation'] [role='button'][aria-label='"
                        + ariaLabel + "']"));
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

    private String dataSignature() {
        return grid().findElements(DATA_ROWS).stream().filter(WebElement::isDisplayed)
                .map(this::elementText).collect(Collectors.joining("|"));
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
            List<WebElement> grids = d.findElements(GRID).stream().filter(WebElement::isDisplayed).toList();
            if (grids.isEmpty()) {
                return isEmptyState();
            }
            String text = elementText(grids.get(0));
            return !text.contains("Đang tải dữ liệu")
                    && (!grids.get(0).findElements(DATA_ROWS).isEmpty() || isEmptyState());
        });
    }

    private WebElement detailDrawer() {
        By locator = By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        return wait.until(d -> d.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> {
                    String text = elementText(element);
                    return text.contains("Thông tin giao dịch") && text.contains("Số tiền");
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
                for (WebElement button : drawer.findElements(
                        By.xpath(".//button[normalize-space()='Hủy']"))) {
                    if (button.isDisplayed() && button.isEnabled()) {
                        return button;
                    }
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
                                     String selectedText) {}
    public record EmptySnapshot(String type, String gateway, String pageText, boolean empty) {}
    public record ResetSnapshot(boolean allSelected, String type, String status, String gateway,
                                String date, int page, int rows, String url) {}
    public record FilterChangeSnapshot(String firstValue, String secondValue,
                                       List<TransactionRow> firstRows, List<TransactionRow> secondRows,
                                       String selectedText) {}
    public record FilterOptionResult(String value, String selectedText,
                                     List<TransactionRow> rows) {}
    public record FilterOptionsSnapshot(Filter filter, List<String> options,
                                        List<FilterOptionResult> results) {}
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
                                           boolean nextDisabled) {}
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
    public record DotsJumpSnapshot(int pageBefore, int pageAfter,
                                   List<TransactionRow> rows) {}
    public record PaginationSnapshot(List<TransactionRow> pageOne, List<TransactionRow> pageTwo,
                                     List<TransactionRow> returnedPageOne, int activePage) {}
    public record FilterPaginationSnapshot(String expectedStatus, String selectedStatus,
                                           List<TransactionRow> pageOne, List<TransactionRow> pageTwo,
                                           int activePage) {}
    public record DetailSnapshot(TransactionRow source, String url, String drawerText,
                                 int relatedLinks, boolean currentMarked) {}
    public record CloseDetailSnapshot(String openedUrl, String closedUrl, boolean closed) {}
    public record DeepLinkSnapshot(String expectedUrl, String actualUrl, String drawerText) {}
    public record HistoryNavigationSnapshot(String openedUrl, String backUrl, String forwardUrl,
                                            boolean closedAfterBack, String drawerText) {}
    public record RelatedHistorySnapshot(TransactionRow source, String drawerText,
                                         boolean loaded, boolean currentMarked) {}
    public record RelatedExpansionSnapshot(String openedUrl, int beforeCount, int afterCount,
                                           String drawerText) {}
    public record DetailAuditSnapshot(TransactionRow source, String openedUrl, String drawerText,
                                      String userHref, List<String> transactionHrefs,
                                      int beforeRelatedCount, int afterRelatedCount,
                                      boolean currentMarked, boolean closed) {}
    public record DetailLinksSnapshot(String openedUrl, String userHref,
                                      List<String> transactionHrefs, String drawerText) {}
    public record StatusDetailSnapshot(String expectedStatus, String selectedStatus,
                                       TransactionRow source, String openedUrl, String drawerText,
                                       boolean empty, boolean closed, String closedUrl) {}
    public record ProfileNavigationSnapshot(String sourceUrl, String expectedUrl,
                                            String actualUrl, String sourceText) {}
    public record RelatedNavigationSnapshot(String sourceUrl, String expectedUrl,
                                            String actualUrl, String sourceText,
                                            String drawerText) {}
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
