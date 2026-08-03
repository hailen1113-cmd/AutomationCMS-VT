package com.vuatho.pages;

import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page Object của Kho tổng → Phiếu xuất kho. */
public class UniformInventoryStockExportPage extends UniformUiPage {
    private static final String ROUTE = "/vuatho/inventory-uniform?tab=main";
    private static final By MAIN_WAREHOUSE_TAB = By.cssSelector(
            "[role='tab'][data-key='main']");
    private static final By STOCK_EXPORT_ROWS = By.xpath(
            "//main//*[normalize-space()='Mã phiếu']"
                    + "/ancestor::div[contains(@class,'grid')][1]"
                    + "/following-sibling::div[1]/div[contains(@class,'grid')]");
    private static final Pattern STOCK_EXPORT_CODE = Pattern.compile(
            "\\b[A-ZĐ]{2,3}-\\d{4}-\\d{3,}\\b");
    private static final Pattern LOT_CODE = Pattern.compile("\\bVT\\d+\\b");
    private static final Pattern QUANTITY = Pattern.compile("(?i)\\bx\\s*(\\d+)\\b");
    private static final Pattern DATE = Pattern.compile("\\b\\d{2}/\\d{2}/\\d{4}\\b");
    private static final Pattern TIME = Pattern.compile("\\b\\d{2}:\\d{2}\\b");
    private static final Pattern PAGINATION = Pattern.compile(
            "Trang\\s+(\\d+)/(\\d+)\\s*[·-]\\s*(\\d+)\\s+phiếu",
            Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public UniformInventoryStockExportPage(WebDriver driver) {
        super(driver);
    }

    /** Mở route Kho tổng, chọn Phiếu và chờ dữ liệu hoặc empty-state. */
    public UniformInventoryStockExportPage openStockExports() {
        openRoute(ROUTE);
        WebElement stockExportButton = exactMainButton("Phiếu");
        if (!selected(stockExportButton)) {
            click(stockExportButton, "Chọn tab Phiếu xuất kho");
            waitForResult();
        }
        wait.until(d -> !visibleElements(STOCK_EXPORT_ROWS).isEmpty()
                || normalizedMainText().contains("chua co du lieu"));
        observeStockExportRows("Cuộn xuống quan sát danh sách phiếu");
        return this;
    }

    /** Chụp trạng thái và các điều khiển chính của tab Phiếu xuất kho. */
    public StockExportScreenSnapshot screenSnapshot() {
        openStockExports();
        List<String> headers = visibleElements(By.xpath(
                "//main//*[normalize-space()='Mã phiếu']"
                        + "/ancestor::div[contains(@class,'grid')][1]/*"))
                .stream().map(this::elementText).toList();
        return new StockExportScreenSnapshot(
                driver.getCurrentUrl(),
                "true".equals(visible(MAIN_WAREHOUSE_TAB)
                        .getAttribute("aria-selected")),
                selected(exactMainButton("Phiếu")),
                selected(exactFilterButton("Tất cả")),
                headers,
                List.of("Tất cả", "Nhập kho", "Chuyển sang bán", "Điều chỉnh tồn")
                        .stream().allMatch(this::hasExactFilterButton),
                hasExactMainButton("Điều chỉnh tồn"),
                hasExactMainButton("Nhập kho"));
    }

    /** Đọc các phiếu đang hiển thị ở bộ lọc hiện tại. */
    public List<StockExportRow> rows() {
        return visibleElements(STOCK_EXPORT_ROWS).stream()
                .map(this::parseRow)
                .filter(row -> !row.code().isBlank())
                .toList();
    }

    /** Chọn một loại phiếu và đọc dữ liệu trả về. */
    public StockExportFilterSnapshot filter(String filterName) {
        openStockExports();
        return activateFilter(filterName);
    }

    /** Chuyển tuần tự ba bộ lọc rồi quay về Tất cả. */
    public FilterSequenceSnapshot cycleFiltersAndRestoreAll() {
        openStockExports();
        List<String> initialCodes = rows().stream().map(StockExportRow::code).toList();
        PaginationInfo initialInfo = paginationInfo();
        List<StockExportFilterSnapshot> filters = new ArrayList<>();
        for (String name : List.of(
                "Nhập kho", "Chuyển sang bán", "Điều chỉnh tồn")) {
            filters.add(activateFilter(name));
        }
        StockExportFilterSnapshot restored = activateFilter("Tất cả");
        scrollToPageBottom(
                "Cuộn cuối trang quan sát phân trang đã khôi phục của Tất cả");
        return new FilterSequenceSnapshot(
                initialCodes, initialInfo, filters, restored);
    }

    /** Đọc tổng số phiếu của Tất cả và từng loại để đối chiếu phân hoạch dữ liệu. */
    public FilterTotalsSnapshot filterTotals() {
        openStockExports();
        scrollToPageBottom(
                "Cuộn cuối trang quan sát tổng số phiếu của Tất cả");
        PaginationInfo all = paginationInfo();
        List<StockExportFilterSnapshot> filters = new ArrayList<>();
        for (String name : List.of(
                "Nhập kho", "Chuyển sang bán", "Điều chỉnh tồn")) {
            StockExportFilterSnapshot result = activateFilter(name);
            scrollToPageBottom(
                    "Cuộn cuối trang quan sát tổng số phiếu " + name);
            filters.add(result);
        }
        activateFilter("Tất cả");
        return new FilterTotalsSnapshot(all, filters);
    }

    /** Chọn loại có nhiều trang, cuộn cuối trang và kiểm tra trang kế tiếp. */
    public FilteredPaginationSnapshot paginateWhileFiltered() {
        openStockExports();
        for (String name : List.of(
                "Chuyển sang bán", "Nhập kho", "Điều chỉnh tồn")) {
            StockExportFilterSnapshot first = activateFilter(name);
            if (first.pagination().totalPages() <= 1) {
                continue;
            }
            List<String> firstCodes = first.rows().stream()
                    .map(StockExportRow::code).toList();
            scrollToPageBottom(
                    "Cuộn đến cuối trang lọc " + name + " trước khi bấm Sang trang");
            click(exactMainButton("Sau"),
                    "Sang trang kế tiếp khi đang lọc " + name);
            wait.until(d -> paginationInfo().currentPage() == 2);
            waitForResult();
            List<StockExportRow> secondRows = rows();
            observeStockExportRows(
                    "Quan sát trang 2 vẫn giữ bộ lọc " + name);
            return new FilteredPaginationSnapshot(
                    name,
                    expectedType(name),
                    first.pagination(),
                    firstCodes,
                    paginationInfo(),
                    secondRows,
                    selectedFilterNames());
        }
        return FilteredPaginationSnapshot.empty();
    }

    /** Từ trang 2 đổi sang loại khác và đọc trạng thái trang được reset. */
    public FilterChangeFromLaterPageSnapshot changeFilterFromLaterPage() {
        openStockExports();
        for (String source : List.of(
                "Chuyển sang bán", "Nhập kho", "Điều chỉnh tồn")) {
            StockExportFilterSnapshot sourceResult = activateFilter(source);
            if (sourceResult.pagination().totalPages() <= 1) {
                continue;
            }
            scrollToPageBottom(
                    "Cuộn cuối trang lọc " + source + " trước khi sang trang 2");
            click(exactMainButton("Sau"),
                    "Sang trang 2 của bộ lọc " + source);
            wait.until(d -> paginationInfo().currentPage() == 2);
            waitForResult();
            String target = "Nhập kho".equals(source)
                    ? "Chuyển sang bán" : "Nhập kho";
            StockExportFilterSnapshot targetResult = activateFilter(target);
            return new FilterChangeFromLaterPageSnapshot(
                    source, target, 2, targetResult);
        }
        return FilterChangeFromLaterPageSnapshot.empty();
    }

    /** Chọn lại cùng loại và chụp dữ liệu trước/sau để kiểm tra tính ổn định. */
    public RepeatedFilterSnapshot repeatSameFilter() {
        openStockExports();
        StockExportFilterSnapshot first = activateFilter("Nhập kho");
        StockExportFilterSnapshot second = activateFilter("Nhập kho");
        scrollToPageBottom(
                "Cuộn cuối trang quan sát phân trang sau khi chọn lại Nhập kho");
        return new RepeatedFilterSnapshot(first, second);
    }

    /** Đọc trạng thái phân trang tại trang đầu. */
    public PaginationSnapshot firstPagePagination() {
        openStockExports();
        PaginationInfo info = paginationInfo();
        observePagination("Cuộn xuống quan sát phân trang của danh sách phiếu");
        return new PaginationSnapshot(
                info,
                disabled(exactMainButton("Trước")),
                disabled(exactMainButton("Sau")),
                rows().stream().map(StockExportRow::code).toList());
    }

    /** Duyệt lần lượt đến trang cuối rồi quay lại trang đầu. */
    public PaginationJourney paginateAllAndReturn() {
        openStockExports();
        PaginationInfo initial = paginationInfo();
        List<String> firstCodes = rows().stream().map(StockExportRow::code).toList();
        Set<String> allCodes = new LinkedHashSet<>(firstCodes);
        List<Integer> visitedPages = new ArrayList<>();
        visitedPages.add(initial.currentPage());

        while (!disabled(exactMainButton("Sau"))) {
            int before = paginationInfo().currentPage();
            scrollToPageBottom(
                    "Cuộn đến cuối trang " + before + " trước khi bấm Sang trang");
            click(exactMainButton("Sau"), "Sang trang phiếu kế tiếp");
            wait.until(d -> paginationInfo().currentPage() > before);
            waitForResult();
            observeStockExportRows("Quan sát dữ liệu trang phiếu "
                    + paginationInfo().currentPage());
            visitedPages.add(paginationInfo().currentPage());
            rows().stream().map(StockExportRow::code).forEach(allCodes::add);
        }
        scrollToPageBottom(
                "Cuộn cuối trang quan sát nút Sau bị khóa ở trang cuối");
        boolean nextDisabledOnLast = disabled(exactMainButton("Sau"));

        while (!disabled(exactMainButton("Trước"))) {
            int before = paginationInfo().currentPage();
            scrollToPageBottom(
                    "Cuộn đến cuối trang " + before + " trước khi bấm Trở về");
            click(exactMainButton("Trước"), "Quay lại trang phiếu trước");
            wait.until(d -> paginationInfo().currentPage() < before);
            waitForResult();
        }
        observeStockExportRows("Quan sát dữ liệu đã khôi phục ở trang phiếu đầu");
        scrollToPageBottom(
                "Cuộn cuối trang quan sát phân trang đã trở về trang đầu");
        return new PaginationJourney(
                initial,
                visitedPages,
                new ArrayList<>(allCodes),
                nextDisabledOnLast,
                paginationInfo(),
                rows().stream().map(StockExportRow::code).toList(),
                disabled(exactMainButton("Trước")));
    }

    private StockExportFilterSnapshot activateFilter(String filterName) {
        WebElement button = exactFilterButton(filterName);
        click(button, "Lọc phiếu theo " + filterName);
        waitForResult();
        wait.until(d -> selected(exactFilterButton(filterName)));
        List<StockExportRow> filteredRows = rows();
        observeStockExportRows("Cuộn xuống quan sát phiếu lọc theo " + filterName);
        String normalized = normalizedMainText();
        return new StockExportFilterSnapshot(
                filterName,
                selected(exactFilterButton(filterName)),
                filteredRows,
                filteredRows.isEmpty()
                        && (normalized.contains("chua co du lieu")
                        || normalized.contains("khong co")),
                paginationInfo(),
                selectedFilterNames());
    }

    private List<String> selectedFilterNames() {
        return List.of("Tất cả", "Nhập kho", "Chuyển sang bán", "Điều chỉnh tồn")
                .stream()
                .filter(name -> selected(exactFilterButton(name)))
                .toList();
    }

    private String expectedType(String filterName) {
        return switch (filterName) {
            case "Nhập kho" -> "nhap kho";
            case "Chuyển sang bán" -> "xuat chuyen kho";
            case "Điều chỉnh tồn" -> "dieu chinh";
            default -> "";
        };
    }

    private StockExportRow parseRow(WebElement row) {
        List<WebElement> cells = row.findElements(By.xpath("./*"));
        if (cells.size() < 4) {
            return StockExportRow.empty();
        }
        String code = firstMatch(STOCK_EXPORT_CODE, elementText(cells.get(0)));
        String type = elementText(cells.get(1));
        String detail = elementText(cells.get(2));
        String metadata = elementText(cells.get(3));
        List<String> lotCodes = LOT_CODE.matcher(detail).results()
                .map(java.util.regex.MatchResult::group).toList();
        List<Integer> quantities = QUANTITY.matcher(detail).results()
                .map(result -> Integer.parseInt(result.group(1))).toList();
        String date = firstMatch(DATE, metadata);
        String time = firstMatch(TIME, metadata);
        String operator = metadata.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank()
                        && !DATE.matcher(line).matches()
                        && !TIME.matcher(line).matches())
                .reduce((first, second) -> second).orElse("");
        return new StockExportRow(
                code, type, lotCodes, quantities, date, time, operator,
                elementText(row));
    }

    private PaginationInfo paginationInfo() {
        Matcher matcher = PAGINATION.matcher(mainText());
        if (!matcher.find()) {
            return new PaginationInfo(0, 0, 0);
        }
        return new PaginationInfo(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));
    }

    private WebElement exactMainButton(String text) {
        return visible(By.xpath("//main//button[normalize-space()="
                + xpathLiteral(text) + "]"));
    }

    private boolean hasExactMainButton(String text) {
        return !visibleElements(By.xpath("//main//button[normalize-space()="
                + xpathLiteral(text) + "]")).isEmpty();
    }

    private WebElement exactFilterButton(String text) {
        return visible(By.xpath(
                "//main//button[normalize-space()='Tất cả']/parent::div"
                        + "//button[normalize-space()=" + xpathLiteral(text) + "]"));
    }

    private boolean hasExactFilterButton(String text) {
        return !visibleElements(By.xpath(
                "//main//button[normalize-space()='Tất cả']/parent::div"
                        + "//button[normalize-space()=" + xpathLiteral(text) + "]"))
                .isEmpty();
    }

    private boolean selected(WebElement button) {
        String classes = button.getAttribute("class");
        return "true".equals(button.getAttribute("aria-selected"))
                || classes != null && classes.contains("bg-primary-blue")
                && classes.contains("text-white");
    }

    private boolean disabled(WebElement button) {
        return !button.isEnabled()
                || "true".equals(button.getAttribute("disabled"))
                || "true".equals(button.getAttribute("aria-disabled"));
    }

    private String firstMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text == null ? "" : text);
        return matcher.find() ? matcher.group() : "";
    }

    private void observeStockExportRows(String step) {
        WebElement target = visibleElements(STOCK_EXPORT_ROWS).stream()
                .findFirst().orElseGet(() -> visible(By.tagName("main")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'start',behavior:'smooth'});"
                        + "window.scrollBy(0,-140);", target);
        pause(step);
    }

    private void observePagination(String step) {
        scrollToPageBottom(step);
    }

    /** Cuộn hẳn xuống cuối trang và giữ màn hình 500 ms trước thao tác phân trang. */
    private void scrollToPageBottom(String step) {
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo({top:document.documentElement.scrollHeight,"
                        + "behavior:'smooth'});");
        wait.until(d -> Boolean.TRUE.equals(
                ((JavascriptExecutor) d).executeScript(
                        "const root=document.scrollingElement"
                                + "||document.documentElement;"
                                + "return Math.ceil(root.scrollTop+window.innerHeight)"
                                + ">=root.scrollHeight-3;")));
        pause(step);
    }

    public record StockExportScreenSnapshot(
            String url,
            boolean mainWarehouseSelected,
            boolean stockExportSelected,
            boolean allSelected,
            List<String> headers,
            boolean filterButtons,
            boolean adjustStockButton,
            boolean importStockButton) {
    }

    public record StockExportRow(
            String code,
            String type,
            List<String> lotCodes,
            List<Integer> quantities,
            String date,
            String time,
            String operator,
            String rowText) {
        private static StockExportRow empty() {
            return new StockExportRow("", "", List.of(), List.of(),
                    "", "", "", "");
        }

        public LocalDateTime timestamp() {
            return LocalDateTime.parse(date + " " + time, DATE_TIME);
        }

        public String normalizedType() {
            return TextNormalizer.normalize(type);
        }
    }

    public record StockExportFilterSnapshot(
            String filter,
            boolean selected,
            List<StockExportRow> rows,
            boolean emptyState,
            PaginationInfo pagination,
            List<String> selectedFilters) {
    }

    public record FilterSequenceSnapshot(
            List<String> initialCodes,
            PaginationInfo initialPagination,
            List<StockExportFilterSnapshot> filters,
            StockExportFilterSnapshot restored) {
    }

    public record FilterTotalsSnapshot(
            PaginationInfo all,
            List<StockExportFilterSnapshot> filters) {
    }

    public record FilteredPaginationSnapshot(
            String filter,
            String expectedType,
            PaginationInfo firstPage,
            List<String> firstCodes,
            PaginationInfo secondPage,
            List<StockExportRow> secondRows,
            List<String> selectedFilters) {
        private static FilteredPaginationSnapshot empty() {
            return new FilteredPaginationSnapshot(
                    "", "", new PaginationInfo(0, 0, 0), List.of(),
                    new PaginationInfo(0, 0, 0), List.of(), List.of());
        }
    }

    public record FilterChangeFromLaterPageSnapshot(
            String sourceFilter,
            String targetFilter,
            int sourcePage,
            StockExportFilterSnapshot targetResult) {
        private static FilterChangeFromLaterPageSnapshot empty() {
            return new FilterChangeFromLaterPageSnapshot(
                    "", "", 0,
                    new StockExportFilterSnapshot(
                            "", false, List.of(), false,
                            new PaginationInfo(0, 0, 0), List.of()));
        }
    }

    public record RepeatedFilterSnapshot(
            StockExportFilterSnapshot first,
            StockExportFilterSnapshot second) {
    }

    public record PaginationInfo(
            int currentPage,
            int totalPages,
            int totalItems) {
    }

    public record PaginationSnapshot(
            PaginationInfo info,
            boolean previousDisabled,
            boolean nextDisabled,
            List<String> codes) {
    }

    public record PaginationJourney(
            PaginationInfo initial,
            List<Integer> visitedPages,
            List<String> allCodes,
            boolean nextDisabledOnLast,
            PaginationInfo restored,
            List<String> restoredCodes,
            boolean previousDisabledAfterReturn) {
    }
}
