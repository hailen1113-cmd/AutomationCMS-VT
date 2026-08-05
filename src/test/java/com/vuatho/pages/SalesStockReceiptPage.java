package com.vuatho.pages;

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

/** Page Object của Kho bán hàng → Phiếu. */
public final class SalesStockReceiptPage extends UniformUiPage {
    private static final String ROUTE = "/vuatho/inventory-uniform?tab=sub";
    private static final By SALES_TAB = By.cssSelector("[role='tab'][data-key='sub']");
    private static final By ROWS = By.xpath(
            "//main//*[normalize-space()='Mã phiếu']"
                    + "/ancestor::div[contains(@class,'grid')][1]"
                    + "/following-sibling::div[1]/div[contains(@class,'grid')]");
    private static final Pattern RECEIPT_CODE = Pattern.compile("\\b[A-Z]{2,4}-\\d{4}-\\d{3,}\\b");
    private static final Pattern QUANTITY = Pattern.compile("(?i)\\bx\\s*(\\d+)\\b");
    private static final Pattern DATE = Pattern.compile("\\b\\d{2}/\\d{2}/\\d{4}\\b");
    private static final Pattern TIME = Pattern.compile("\\b\\d{2}:\\d{2}\\b");
    private static final Pattern PAGINATION = Pattern.compile(
            "Trang\\s+(\\d+)/(\\d+)\\s*[^\\d]+\\s*(\\d+)\\s+phiếu",
            Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<String> FILTERS = List.of(
            "Tất cả", "Nhập từ kho tổng", "Xuất đơn", "Xuất nhân sự", "Điều chỉnh tồn");

    public SalesStockReceiptPage(WebDriver driver) {
        super(driver);
    }

    /** Mở đúng Kho bán hàng và chọn tab Phiếu. */
    public SalesStockReceiptPage openReceipts() {
        openRoute(ROUTE);
        WebElement button = mainButton("Phiếu");
        if (!selected(button)) {
            click(button, "Chọn tab Phiếu của Kho bán hàng");
            waitForResult();
        }
        wait.until(d -> !visibleElements(ROWS).isEmpty()
                || normalizedMainText().contains("chua co du lieu"));
        return this;
    }

    /** Đọc trạng thái điều khiển và tiêu đề của tab Phiếu. */
    public ReceiptScreenSnapshot screenSnapshot() {
        openReceipts();
        List<String> headers = visibleElements(By.xpath(
                "//main//*[normalize-space()='Mã phiếu']"
                        + "/ancestor::div[contains(@class,'grid')][1]/*"))
                .stream().map(this::elementText).toList();
        observeControl(mainButton("Phiếu"), "Quan sát tab Phiếu đang được chọn");
        observeControl(filterButton("Tất cả"), "Quan sát bộ lọc Tất cả mặc định");
        return new ReceiptScreenSnapshot(
                driver.getCurrentUrl(),
                "true".equals(visible(SALES_TAB).getAttribute("aria-selected")),
                selected(mainButton("Phiếu")),
                selected(filterButton("Tất cả")),
                headers,
                FILTERS.stream().allMatch(this::hasFilterButton),
                hasMainButton("Xuất hàng"),
                hasMainButton("Nhập hàng"),
                mainButton("Xuất hàng").isEnabled(),
                mainButton("Nhập hàng").isEnabled());
    }

    /** Đọc dữ liệu các phiếu đang hiển thị. */
    public List<ReceiptRow> rows() {
        return visibleElements(ROWS).stream()
                .map(this::parseRow)
                .filter(row -> !row.code().isBlank())
                .toList();
    }

    /** Đọc và quan sát toàn bộ dữ liệu nghiệp vụ trên trang đầu. */
    public List<ReceiptRow> observedRows() {
        openReceipts();
        List<ReceiptRow> result = rows();
        observeRowsDownAndBack("Quan sát dữ liệu các phiếu Kho bán hàng");
        return result;
    }

    /** Kiểm tra thứ tự ngày giờ của các phiếu trên trang hiện tại. */
    public List<LocalDateTime> receiptTimes() {
        return observedRows().stream()
                .filter(row -> !row.date().isBlank() && !row.time().isBlank())
                .map(row -> LocalDateTime.parse(row.date() + " " + row.time(), DATE_TIME))
                .toList();
    }

    /** Cuộn đến phiếu cuối, trở lại phiếu đầu rồi trả trang về đầu. */
    public ScrollSnapshot scrollLastReceiptAndBack() {
        openReceipts();
        List<WebElement> elements = visibleElements(ROWS);
        if (elements.isEmpty()) {
            return new ScrollSnapshot(0, false, false);
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement last = elements.get(elements.size() - 1);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", last);
        highlight(last);
        pause("Cuộn xuống quan sát phiếu cuối trang");
        boolean lastVisible = last.isDisplayed();
        WebElement first = elements.get(0);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", first);
        highlight(first);
        pause("Cuộn trở lại quan sát phiếu đầu trang");
        boolean firstVisible = first.isDisplayed();
        returnToTop("Trở về đầu trang sau khi quan sát danh sách phiếu");
        return new ScrollSnapshot(elements.size(), lastVisible, firstVisible);
    }

    /** Chuyển qua Tồn kho rồi trở lại Phiếu để bảo đảm từng tab hiển thị đúng nội dung riêng. */
    public TabSwitchSnapshot switchStockAndBackToReceipts() {
        openReceipts();
        List<String> initialCodes = rows().stream().map(ReceiptRow::code).toList();
        click(mainButton("Tồn kho"), "Chuyển sang tab Tồn kho từ tab Phiếu");
        waitForResult();
        wait.until(d -> selected(mainButton("Tồn kho")));
        boolean stockSelected = selected(mainButton("Tồn kho"));
        boolean stockControlsVisible = !visibleElements(By.cssSelector("input[placeholder='Tìm mã lô…']")).isEmpty()
                && hasMainButton("Lưới tháng") && hasMainButton("Danh sách");
        click(mainButton("Phiếu"), "Quay lại tab Phiếu từ tab Tồn kho");
        waitForResult();
        wait.until(d -> selected(mainButton("Phiếu")));
        List<String> restoredCodes = rows().stream().map(ReceiptRow::code).toList();
        observeRowsDownAndBack("Quan sát dữ liệu Phiếu sau khi chuyển qua lại hai tab");
        return new TabSwitchSnapshot(stockSelected, stockControlsVisible,
                selected(mainButton("Phiếu")), initialCodes, restoredCodes);
    }

    /** Bấm lại tab Phiếu đang mở để kiểm tra trạng thái và danh sách không bị nhân đôi hay mất dữ liệu. */
    public ReceiptTabStabilitySnapshot reselectReceiptsTab() {
        openReceipts();
        List<String> initialCodes = rows().stream().map(ReceiptRow::code).toList();
        PaginationInfo initialPage = paginationInfo();
        click(mainButton("Phiếu"), "Chọn lại tab Phiếu đang hiển thị");
        waitForResult();
        List<String> repeatedCodes = rows().stream().map(ReceiptRow::code).toList();
        observeRowsDownAndBack("Quan sát danh sách sau khi chọn lại tab Phiếu");
        return new ReceiptTabStabilitySnapshot(selected(mainButton("Phiếu")), initialCodes,
                repeatedCodes, initialPage, paginationInfo());
    }

    /** Chọn một loại phiếu và đọc kết quả. */
    public FilterSnapshot filter(String name) {
        openReceipts();
        return activateFilter(name);
    }

    /** Chuyển qua tất cả bộ lọc rồi quay lại Tất cả. */
    public FilterSequenceSnapshot cycleFiltersAndRestoreAll() {
        openReceipts();
        List<String> initialCodes = rows().stream().map(ReceiptRow::code).toList();
        PaginationInfo initialPage = paginationInfo();
        List<FilterSnapshot> results = new ArrayList<>();
        for (String name : FILTERS.subList(1, FILTERS.size())) {
            results.add(activateFilter(name));
        }
        FilterSnapshot restored = activateFilter("Tất cả");
        return new FilterSequenceSnapshot(initialCodes, initialPage, results, restored);
    }

    /** Đối chiếu tổng số phiếu của Tất cả với tổng từng loại. */
    public FilterTotalsSnapshot filterTotals() {
        openReceipts();
        PaginationInfo all = paginationInfo();
        List<FilterSnapshot> results = new ArrayList<>();
        for (String name : FILTERS.subList(1, FILTERS.size())) {
            results.add(activateFilter(name));
        }
        activateFilter("Tất cả");
        return new FilterTotalsSnapshot(all, results);
    }

    /** Đọc trạng thái phân trang đầu tiên. */
    public PaginationSnapshot firstPagePagination() {
        openReceipts();
        PaginationInfo info = paginationInfo();
        scrollToBottom("Cuộn cuối trang quan sát phân trang phiếu Kho bán hàng");
        PaginationSnapshot result = new PaginationSnapshot(
                info,
                disabled(mainButton("Trước")),
                disabled(mainButton("Sau")),
                rows().stream().map(ReceiptRow::code).toList());
        returnToTop("Trở về đầu trang sau khi quan sát phân trang");
        return result;
    }

    /** Sang trang 2 rồi quay lại trang 1 và đối chiếu dữ liệu. */
    public PaginationJourney nextAndPrevious() {
        openReceipts();
        PaginationInfo initial = paginationInfo();
        List<String> firstCodes = rows().stream().map(ReceiptRow::code).toList();
        if (initial.totalPages() <= 1) {
            return PaginationJourney.empty(initial, firstCodes);
        }
        scrollToBottom("Cuộn cuối trang trước khi bấm Sau");
        click(mainButton("Sau"), "Sang trang phiếu thứ 2");
        wait.until(d -> paginationInfo().currentPage() == 2);
        waitForResult();
        List<String> secondCodes = rows().stream().map(ReceiptRow::code).toList();
        observeRowsDownAndBack("Quan sát dữ liệu trang phiếu thứ 2");
        scrollToBottom("Cuộn cuối trang 2 trước khi bấm Trước");
        click(mainButton("Trước"), "Quay lại trang phiếu đầu tiên");
        wait.until(d -> paginationInfo().currentPage() == 1);
        waitForResult();
        List<String> restoredCodes = rows().stream().map(ReceiptRow::code).toList();
        returnToTop("Trở về đầu trang sau khi kiểm tra phân trang");
        return new PaginationJourney(initial, firstCodes, secondCodes,
                paginationInfo(), restoredCodes);
    }

    /** Sang trang khi đang lọc và kiểm tra loại phiếu vẫn được giữ nguyên. */
    public FilteredPaginationSnapshot paginateWhileFiltered() {
        openReceipts();
        for (String filter : List.of("Xuất đơn", "Nhập từ kho tổng", "Xuất nhân sự", "Điều chỉnh tồn")) {
            FilterSnapshot first = activateFilter(filter);
            if (first.pagination().totalPages() <= 1) {
                continue;
            }
            List<String> firstCodes = first.rows().stream().map(ReceiptRow::code).toList();
            scrollToBottom("Cuộn cuối trang lọc " + filter + " trước khi bấm Sau");
            click(mainButton("Sau"), "Sang trang 2 khi đang lọc " + filter);
            wait.until(d -> paginationInfo().currentPage() == 2);
            waitForResult();
            List<ReceiptRow> secondRows = rows();
            observeRowsDownAndBack("Quan sát trang 2 vẫn giữ bộ lọc " + filter);
            return new FilteredPaginationSnapshot(filter, expectedType(filter),
                    firstCodes, paginationInfo(), secondRows, selectedFilters());
        }
        return FilteredPaginationSnapshot.empty();
    }

    /** Duyệt đến trang cuối rồi quay lại trang đầu để kiểm tra đầy đủ các biên phân trang. */
    public FullPaginationJourney paginateAllAndReturn() {
        openReceipts();
        PaginationInfo initial = paginationInfo();
        List<Integer> visitedPages = new ArrayList<>(List.of(initial.currentPage()));
        Set<String> allCodes = new LinkedHashSet<>(rows().stream().map(ReceiptRow::code).toList());
        while (!disabled(mainButton("Sau"))) {
            int before = paginationInfo().currentPage();
            scrollToBottom("Cuộn cuối trang " + before + " trước khi bấm Sau");
            click(mainButton("Sau"), "Sang trang phiếu tiếp theo");
            wait.until(d -> paginationInfo().currentPage() > before);
            waitForResult();
            List<ReceiptRow> currentRows = rows();
            allCodes.addAll(currentRows.stream().map(ReceiptRow::code).toList());
            visitedPages.add(paginationInfo().currentPage());
            observeRowsDownAndBack("Quan sát dữ liệu trang phiếu " + paginationInfo().currentPage());
        }
        scrollToBottom("Cuộn cuối trang quan sát nút Sau ở trang cuối");
        boolean nextDisabledOnLast = disabled(mainButton("Sau"));
        while (!disabled(mainButton("Trước"))) {
            int before = paginationInfo().currentPage();
            scrollToBottom("Cuộn cuối trang " + before + " trước khi bấm Trước");
            click(mainButton("Trước"), "Quay lại trang phiếu trước");
            wait.until(d -> paginationInfo().currentPage() < before);
            waitForResult();
        }
        List<String> restoredCodes = rows().stream().map(ReceiptRow::code).toList();
        boolean previousDisabledOnFirst = disabled(mainButton("Trước"));
        returnToTop("Trở về đầu trang sau khi duyệt toàn bộ phân trang");
        return new FullPaginationJourney(initial, visitedPages, new ArrayList<>(allCodes),
                nextDisabledOnLast, paginationInfo(), restoredCodes, previousDisabledOnFirst);
    }

    /** Đổi bộ lọc khi đang ở trang 2 và kiểm tra kết quả mới quay về trang đầu. */
    public FilterChangeSnapshot changeFilterFromSecondPage() {
        openReceipts();
        for (String source : List.of("Xuất đơn", "Nhập từ kho tổng", "Xuất nhân sự")) {
            FilterSnapshot sourceResult = activateFilter(source);
            if (sourceResult.pagination().totalPages() <= 1) {
                continue;
            }
            scrollToBottom("Cuộn cuối trang lọc " + source + " trước khi sang trang 2");
            click(mainButton("Sau"), "Sang trang 2 của bộ lọc " + source);
            wait.until(d -> paginationInfo().currentPage() == 2);
            waitForResult();
            String target = "Xuất đơn".equals(source) ? "Nhập từ kho tổng" : "Xuất đơn";
            FilterSnapshot targetResult = activateFilter(target);
            return new FilterChangeSnapshot(source, 2, target, targetResult);
        }
        return FilterChangeSnapshot.empty();
    }

    private FilterSnapshot activateFilter(String name) {
        WebElement button = filterButton(name);
        click(button, "Lọc phiếu theo " + name);
        waitForResult();
        wait.until(d -> selected(filterButton(name)));
        List<ReceiptRow> filteredRows = rows();
        observeRowsDownAndBack("Quan sát kết quả lọc phiếu theo " + name);
        String text = normalizedMainText();
        return new FilterSnapshot(name, selected(filterButton(name)),
                filteredRows,
                filteredRows.isEmpty() && (text.contains("chua co du lieu") || text.contains("khong co")),
                paginationInfo(), selectedFilters());
    }

    private ReceiptRow parseRow(WebElement row) {
        List<WebElement> cells = row.findElements(By.xpath("./*"));
        if (cells.size() < 4) {
            return ReceiptRow.empty();
        }
        String code = firstMatch(RECEIPT_CODE, elementText(cells.get(0)));
        String type = elementText(cells.get(1));
        String detail = elementText(cells.get(2));
        String metadata = elementText(cells.get(3));
        List<String> lotCodes = cells.get(2).findElements(By.cssSelector("span.font-bold"))
                .stream().map(this::elementText).filter(value -> !value.isBlank()).toList();
        List<Integer> quantities = QUANTITY.matcher(detail).results()
                .map(match -> Integer.parseInt(match.group(1))).toList();
        String date = firstMatch(DATE, metadata);
        String time = firstMatch(TIME, metadata);
        String operator = metadata.lines().map(String::trim)
                .filter(line -> !line.isBlank()
                        && !DATE.matcher(line).matches()
                        && !TIME.matcher(line).matches())
                .reduce((first, second) -> second).orElse("");
        return new ReceiptRow(code, type, normalizeText(type),
                lotCodes, quantities, date, time, operator, elementText(row));
    }

    private void observeRowsDownAndBack(String step) {
        List<WebElement> elements = visibleElements(ROWS);
        if (elements.isEmpty()) {
            pause(step + " - trạng thái không có dữ liệu");
            returnToTop("Trở về đầu trang sau khi quan sát trạng thái rỗng");
            return;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement last = elements.get(elements.size() - 1);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", last);
        highlight(last);
        pause(step + " - dòng cuối");
        WebElement first = elements.get(0);
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", first);
        highlight(first);
        pause(step + " - dòng đầu");
        returnToTop("Trở về đầu trang sau khi quan sát dữ liệu phiếu");
    }

    private void observeControl(WebElement element, String step) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center',inline:'center'});", element);
        highlight(element);
        pause(step);
    }

    private void scrollToBottom(String step) {
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo({top:document.scrollingElement.scrollHeight,behavior:'smooth'});");
        pause(step);
    }

    private void returnToTop(String step) {
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo({top:0,behavior:'smooth'});document.scrollingElement.scrollTop=0;");
        pause(step);
    }

    private PaginationInfo paginationInfo() {
        Matcher matcher = PAGINATION.matcher(mainText());
        return matcher.find()
                ? new PaginationInfo(Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)))
                : new PaginationInfo(1, 1, rows().size());
    }

    private List<String> selectedFilters() {
        return FILTERS.stream().filter(name -> selected(filterButton(name))).toList();
    }

    private String expectedType(String filter) {
        return switch (filter) {
            case "Nhập từ kho tổng" -> "nhap chuyen kho";
            case "Xuất đơn" -> "xuat don";
            case "Xuất nhân sự" -> "xuat nhan su";
            case "Điều chỉnh tồn" -> "dieu chinh";
            default -> "";
        };
    }

    private WebElement mainButton(String text) {
        return visible(By.xpath("//main//button[normalize-space()=" + xpathLiteral(text) + "]"));
    }

    private boolean hasMainButton(String text) {
        return !visibleElements(By.xpath("//main//button[normalize-space()=" + xpathLiteral(text) + "]")).isEmpty();
    }

    private WebElement filterButton(String text) {
        return visible(By.xpath("//main//button[normalize-space()='Tất cả']/parent::div"
                + "//button[normalize-space()=" + xpathLiteral(text) + "]"));
    }

    private boolean hasFilterButton(String text) {
        return !visibleElements(By.xpath("//main//button[normalize-space()='Tất cả']/parent::div"
                + "//button[normalize-space()=" + xpathLiteral(text) + "]")).isEmpty();
    }

    private boolean selected(WebElement button) {
        String classes = button.getAttribute("class");
        return "true".equals(button.getAttribute("aria-selected"))
                || "true".equals(button.getAttribute("data-selected"))
                || classes != null && classes.contains("bg-primary-blue") && classes.contains("text-white");
    }

    private boolean disabled(WebElement button) {
        return !button.isEnabled() || "true".equals(button.getAttribute("disabled"))
                || "true".equals(button.getAttribute("aria-disabled"));
    }

    private String firstMatch(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value == null ? "" : value);
        return matcher.find() ? matcher.group() : "";
    }

    public record ReceiptScreenSnapshot(String url, boolean salesTabSelected,
            boolean receiptTabSelected, boolean allFilterSelected, List<String> headers,
            boolean allFiltersVisible, boolean exportButtonVisible, boolean importButtonVisible,
            boolean exportButtonEnabled, boolean importButtonEnabled) {}

    public record ReceiptRow(String code, String type, String normalizedType,
            List<String> lotCodes, List<Integer> quantities, String date, String time,
            String operator, String text) {
        public static ReceiptRow empty() {
            return new ReceiptRow("", "", "", List.of(), List.of(), "", "", "", "");
        }
    }

    public record ScrollSnapshot(int rowCount, boolean reachedLast, boolean returnedFirst) {}
    public record TabSwitchSnapshot(boolean stockSelected, boolean stockControlsVisible,
            boolean receiptSelected, List<String> initialCodes, List<String> restoredCodes) {}
    public record ReceiptTabStabilitySnapshot(boolean receiptSelected, List<String> initialCodes,
            List<String> repeatedCodes, PaginationInfo initialPage, PaginationInfo repeatedPage) {}
    public record PaginationInfo(int currentPage, int totalPages, int totalItems) {}
    public record FilterSnapshot(String filter, boolean selected, List<ReceiptRow> rows,
            boolean emptyState, PaginationInfo pagination, List<String> selectedFilters) {}
    public record FilterSequenceSnapshot(List<String> initialCodes, PaginationInfo initialPage,
            List<FilterSnapshot> filters, FilterSnapshot restored) {}
    public record FilterTotalsSnapshot(PaginationInfo all, List<FilterSnapshot> filters) {}
    public record PaginationSnapshot(PaginationInfo info, boolean previousDisabled,
            boolean nextDisabled, List<String> codes) {}
    public record PaginationJourney(PaginationInfo initial, List<String> firstCodes,
            List<String> secondCodes, PaginationInfo restored, List<String> restoredCodes) {
        static PaginationJourney empty(PaginationInfo initial, List<String> codes) {
            return new PaginationJourney(initial, codes, List.of(), initial, codes);
        }
    }
    public record FilteredPaginationSnapshot(String filter, String expectedType,
            List<String> firstCodes, PaginationInfo secondPage, List<ReceiptRow> secondRows,
            List<String> selectedFilters) {
        static FilteredPaginationSnapshot empty() {
            return new FilteredPaginationSnapshot("", "", List.of(),
                    new PaginationInfo(0, 0, 0), List.of(), List.of());
        }
    }
    public record FullPaginationJourney(PaginationInfo initial, List<Integer> visitedPages,
            List<String> allCodes, boolean nextDisabledOnLast, PaginationInfo restored,
            List<String> restoredCodes, boolean previousDisabledOnFirst) {}
    public record FilterChangeSnapshot(String sourceFilter, int sourcePage, String targetFilter,
            FilterSnapshot targetResult) {
        static FilterChangeSnapshot empty() {
            return new FilterChangeSnapshot("", 0, "", new FilterSnapshot("", false,
                    List.of(), false, new PaginationInfo(0, 0, 0), List.of()));
        }
    }
}
