package com.vuatho.pages;

import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page Object của Quản lí kho Đồng phục, phạm vi Kho tổng → Tồn kho. */
public class UniformInventoryPage extends UniformUiPage {
    private final String route;
    private final By warehouseTab;
    private final String warehouseName;
    private static final By SEARCH_INPUT = By.cssSelector(
            "input[placeholder='Tìm mã lô…']");
    private static final By CLEAR_SEARCH = By.cssSelector(
            "[role='button'][aria-label='clear input']");
    private static final By GRID_ROWS = By.cssSelector(
            "main table tbody tr");
    private static final By LIST_ROWS = By.xpath(
            "//main//button[.//span[starts-with(normalize-space(.), 'VT')]]");
    private static final By LOT_DRAWER = By.cssSelector(
            "[aria-label^='drawer-Lô ']");
    private static final Pattern LOT_CODE = Pattern.compile("\\bVT\\d+\\b");
    private static final Pattern DATE = Pattern.compile("\\b\\d{2}/\\d{2}/\\d{4}\\b");

    public UniformInventoryPage(WebDriver driver) {
        this(driver, "main", "Kho tổng");
    }

    /** Cho phép dùng lại các thao tác tồn kho cho từng kho mà không khóa cứng route Kho tổng. */
    protected UniformInventoryPage(WebDriver driver, String warehouseKey, String warehouseName) {
        super(driver);
        this.route = "/vuatho/inventory-uniform?tab=" + warehouseKey;
        this.warehouseTab = By.cssSelector(
                "[role='tab'][data-key='" + warehouseKey + "']");
        this.warehouseName = warehouseName;
    }

    /** Mở đúng Kho tổng và chờ bảng tồn kho có dữ liệu hoặc empty-state. */
    public UniformInventoryPage openStock() {
        openRoute(route);
        wait.until(d -> !visibleElements(GRID_ROWS).isEmpty()
                || normalizedMainText().contains("chua co du lieu"));
        pause("Quan sát tab " + warehouseName + " - Tồn kho");
        return this;
    }

    /** Đọc trạng thái tab và toàn bộ điều khiển chính. */
    public StockScreenSnapshot screenSnapshot() {
        openStock();
        WebElement selectedWarehouseTab = visible(warehouseTab);
        WebElement stockButton = exactButton("Tồn kho");
        return new StockScreenSnapshot(
                driver.getCurrentUrl(),
                "true".equals(selectedWarehouseTab.getAttribute("aria-selected")),
                selected(stockButton),
                normalizedMainText(),
                visibleElements(SEARCH_INPUT).size() == 1,
                hasExactButton("Lưới tháng"),
                hasExactButton("Danh sách"));
    }

    /** Đọc ba thẻ tổng quan và số lô sắp hết. */
    public OverviewSnapshot overviewSnapshot() {
        openStock();
        WebElement totalElement = cardElement("Tổng tồn kho");
        observeElement(totalElement, "Quan sát thẻ Tổng tồn kho");
        WebElement latestInElement = cardElement("Nhập gần nhất");
        observeElement(latestInElement, "Quan sát thẻ Nhập gần nhất");
        WebElement latestOutElement = cardElement("Xuất gần nhất");
        observeElement(latestOutElement, "Quan sát thẻ Xuất gần nhất");
        String totalCard = elementText(totalElement);
        String latestInCard = elementText(latestInElement);
        String latestOutCard = elementText(latestOutElement);
        Matcher totalMatcher = Pattern.compile(
                "([\\d,.]+)\\s*\\R+\\s*cai\\s*[·-]\\s*(\\d+)\\s*lo",
                Pattern.CASE_INSENSITIVE)
                .matcher(TextNormalizer.normalize(totalCard));
        int total = totalMatcher.find() ? number(totalMatcher.group(1)) : -1;
        // Đọc số lô riêng để không phụ thuộc cách xuống dòng của thẻ tổng quan.
        Matcher lotMatcher = Pattern.compile("(\\d+)\\s*lo")
                .matcher(TextNormalizer.normalize(totalCard));
        int positiveLots = lotMatcher.find() ? number(lotMatcher.group(1)) : -1;
        return new OverviewSnapshot(
                total,
                positiveLots,
                firstDate(latestInCard),
                firstSignedQuantity(latestInCard),
                firstDate(latestOutCard),
                firstSignedQuantity(latestOutCard),
                lowStockCountFromBanner());
    }

    /** Đọc các dòng tồn từ Lưới tháng. */
    public List<StockRow> stockRows() {
        List<StockRow> result = new ArrayList<>();
        for (WebElement row : visibleElements(GRID_ROWS)) {
            List<WebElement> cells = row.findElements(By.cssSelector("td"));
            if (cells.size() < 2) {
                continue;
            }
            String product = elementText(cells.get(0));
            Matcher code = LOT_CODE.matcher(product);
            if (!code.find()) {
                continue;
            }
            String lotCode = code.group();
            String name = product.replaceFirst(Pattern.quote(lotCode), "").trim();
            result.add(new StockRow(
                    lotCode,
                    name,
                    number(elementText(cells.get(1))),
                    elementText(row)));
        }
        return result;
    }

    /** Đọc mọi mã lô từ badge trong bảng, kể cả mã tự động không bắt đầu bằng VT. */
    public List<FullStockRow> allStockRows() {
        openStock();
        observeGridTable("Quan sát toàn bộ dữ liệu tồn kho để đối chiếu");
        return readAllStockRows();
    }

    /** Đọc tồn nhiều lô Kho tổng trong một lần tải trang, không chạy bước quan sát bảng. */
    public Map<String, Integer> stockValues(List<String> codes) {
        openStock();
        Map<String, Integer> values = new LinkedHashMap<>();
        for (String code : codes) {
            values.put(code, -1);
        }
        for (FullStockRow row : readAllStockRows()) {
            if (values.containsKey(row.code())) {
                values.put(row.code(), row.stock());
            }
        }
        return values;
    }

    private List<FullStockRow> readAllStockRows() {
        List<FullStockRow> result = new ArrayList<>();
        for (WebElement row : visibleElements(GRID_ROWS)) {
            List<WebElement> cells = row.findElements(By.cssSelector("td"));
            if (cells.size() < 2) {
                continue;
            }
            List<WebElement> badges = cells.get(0).findElements(By.cssSelector("span.font-bold"));
            if (badges.isEmpty()) {
                continue;
            }
            String code = elementText(badges.get(0)).trim();
            String product = elementText(cells.get(0));
            String name = product.replaceFirst(Pattern.quote(code), "").trim();
            result.add(new FullStockRow(code, name, number(elementText(cells.get(1))),
                    cells.subList(2, cells.size()).stream().map(this::elementText).toList(),
                    elementText(row)));
        }
        return result;
    }

    /** Đọc đầy đủ tiêu đề tháng và từng ô biến động của Kho tổng. */
    public DetailedGridSnapshot detailedGridSnapshot() {
        openStock();
        observeGridTable("Quan sát tiêu đề và dữ liệu Lưới tháng của Kho tổng");
        List<String> headers = visibleElements(By.cssSelector("main table thead th"))
                .stream().map(this::elementText).toList();
        List<String> months = headers.stream()
                .filter(value -> value.matches("\\d{2}/\\d{4}"))
                .toList();
        return new DetailedGridSnapshot(headers, months, readAllStockRows());
    }

    /** Đọc mã và số lượng hiển thị trực tiếp trong cảnh báo sắp hết. */
    public List<WarningEntry> lowStockWarningEntries() {
        openStock();
        List<WebElement> labels = visibleElements(By.xpath(
                "//main//span[contains(normalize-space(),'lô sắp hết')]"));
        if (labels.isEmpty()) {
            return List.of();
        }
        WebElement label = labels.get(0);
        WebElement alert = label.findElement(By.xpath("parent::div"));
        observeElement(alert, "Quan sát từng mã trong cảnh báo lô sắp hết");
        List<WebElement> containers = label.findElements(By.xpath("following-sibling::div[1]"));
        if (containers.isEmpty()) {
            return List.of();
        }
        List<WarningEntry> result = new ArrayList<>();
        for (WebElement chip : containers.get(0).findElements(By.xpath("./span"))) {
            List<WebElement> codes = chip.findElements(By.cssSelector("span.font-bold"));
            if (codes.isEmpty()) {
                continue;
            }
            String text = elementText(chip);
            Matcher remaining = Pattern.compile("còn\\s*(\\d+)",
                    Pattern.CASE_INSENSITIVE).matcher(text);
            if (remaining.find()) {
                result.add(new WarningEntry(
                        elementText(codes.get(0)).trim().toUpperCase(),
                        number(remaining.group(1))));
            }
        }
        return result;
    }

    /** Làm nổi bật tab Kho tổng và mục Tồn kho đang được chọn. */
    public StockScreenSnapshot observeSelectedStockTab() {
        StockScreenSnapshot snapshot = screenSnapshot();
        observeElement(visible(By.cssSelector("[role='tab'][data-key='main']")),
                "Quan sát tab Kho tổng đang được chọn");
        observeElement(exactText("Tồn kho"), "Quan sát mục Tồn kho đang được chọn");
        return snapshot;
    }

    /** Làm nổi bật các điều khiển tìm kiếm và chuyển chế độ xem. */
    public StockScreenSnapshot observeStockControls() {
        StockScreenSnapshot snapshot = screenSnapshot();
        observeElement(visible(SEARCH_INPUT), "Quan sát ô Tìm mã lô");
        observeElement(exactText("Lưới tháng"), "Quan sát nút Lưới tháng");
        observeElement(exactText("Danh sách"), "Quan sát nút Danh sách");
        return snapshot;
    }

    /** Cuộn đến dòng cuối rồi trở lại dòng đầu của bảng Kho tổng. */
    public TableScrollSnapshot scrollMainGridToLastRowAndBack() {
        openStock();
        List<WebElement> rows = visibleElements(GRID_ROWS);
        if (rows.isEmpty()) {
            return new TableScrollSnapshot(0, false, false);
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", rows.get(rows.size() - 1));
        highlight(rows.get(rows.size() - 1));
        pause("Cuộn xuống quan sát dòng tồn kho cuối cùng của Kho tổng");
        boolean lastVisible = rows.get(rows.size() - 1).isDisplayed();
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", rows.get(0));
        highlight(rows.get(0));
        pause("Cuộn trở lại dòng tồn kho đầu tiên của Kho tổng");
        boolean firstVisible = rows.get(0).isDisplayed();
        returnToPageTop("Trở về đầu trang sau khi quan sát bảng Kho tổng");
        return new TableScrollSnapshot(rows.size(), lastVisible, firstVisible);
    }

    /** Cuộn ngang đến tháng cuối rồi quay lại cột đầu của bảng Kho tổng. */
    public HorizontalTableScrollSnapshot scrollMainGridHorizontallyAndBack() {
        openStock();
        WebElement table = visible(By.cssSelector("main table"));
        WebElement scroller = table.findElement(By.xpath(
                "ancestor::div[contains(@class,'overflow-x-auto')][1]"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long scrollWidth = ((Number) js.executeScript("return arguments[0].scrollWidth;", scroller)).longValue();
        long clientWidth = ((Number) js.executeScript("return arguments[0].clientWidth;", scroller)).longValue();
        js.executeScript("arguments[0].scrollLeft=arguments[0].scrollWidth;", scroller);
        highlight(table);
        pause("Cuộn ngang đến cột tháng cuối của Kho tổng");
        long right = ((Number) js.executeScript("return arguments[0].scrollLeft;", scroller)).longValue();
        js.executeScript("arguments[0].scrollLeft=0;", scroller);
        pause("Cuộn ngang trở lại cột Sản phẩm của Kho tổng");
        long left = ((Number) js.executeScript("return arguments[0].scrollLeft;", scroller)).longValue();
        return new HorizontalTableScrollSnapshot(scrollWidth > clientWidth, right, left);
    }

    /** Kiểm tra các cột tháng và dữ liệu nhập/xuất thực tế. */
    public GridSnapshot gridSnapshot() {
        openStock();
        List<String> headers = visibleElements(By.cssSelector("main table thead th"))
                .stream().map(this::elementText).toList();
        List<String> months = headers.stream()
                .filter(value -> value.matches("\\d{2}/\\d{4}"))
                .toList();
        List<StockRow> rows = stockRows();
        boolean hasMovement = rows.stream().anyMatch(row ->
                row.rowText().contains("+") || row.rowText().contains("−")
                        || row.rowText().matches("(?s).*-[0-9]+.*"));
        scrollToResult("Cuộn xuống quan sát dữ liệu Lưới tháng");
        return new GridSnapshot(headers, months, rows, hasMovement);
    }

    /** Cuộn xuống bảng tồn để người chạy quan sát dữ liệu đang được assertion. */
    public void observeStockTable(String step) {
        scrollToResult(step);
    }

    /** Danh sách mã lô được hiển thị trong vùng cảnh báo sắp hết. */
    public List<String> lowStockCodes() {
        String main = mainText();
        Matcher banner = Pattern.compile(
                "(\\d+)\\s*lo sap het:(.*?)(?:Ton kho|Phieu)",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
                .matcher(TextNormalizer.normalize(main));
        if (!banner.find()) {
            return List.of();
        }
        return LOT_CODE.matcher(banner.group(2).toUpperCase())
                .results().map(java.util.regex.MatchResult::group).toList();
    }

    /** Chuyển sang Danh sách và đọc các dòng hiển thị. */
    public ListViewSnapshot switchToListView() {
        openStock();
        return activateListView("Chuyển sang chế độ Danh sách");
    }

    /** Đọc trạng thái chọn mặc định của hai nút chế độ xem. */
    public ViewSelectionSnapshot defaultViewSelection() {
        openStock();
        scrollToResult("Cuộn xuống quan sát chế độ xem mặc định");
        return currentViewSelection();
    }

    /** Đọc dữ liệu ở cả hai chế độ để đối chiếu mã lô và số lượng tồn. */
    public ViewDataConsistencySnapshot viewDataConsistency() {
        openStock();
        List<StockRow> gridRows = stockRows();
        ListViewSnapshot list = activateListView(
                "Chuyển sang Danh sách để đối chiếu dữ liệu với Lưới tháng");
        return new ViewDataConsistencySnapshot(
                gridRows,
                list.rows(),
                currentViewSelection());
    }

    /** Chuyển qua lại nhiều lần và chụp dữ liệu sau mỗi lần chuyển. */
    public RepeatedViewSwitchSnapshot switchViewsRepeatedly() {
        openStock();
        List<String> originalCodes = stockRows().stream()
                .map(StockRow::code).toList();
        List<List<String>> listCodeSnapshots = new ArrayList<>();
        List<List<String>> gridCodeSnapshots = new ArrayList<>();
        boolean exclusiveSelection = true;

        for (int index = 1; index <= 3; index++) {
            ListViewSnapshot list = activateListView(
                    "Lần " + index + " chuyển sang chế độ Danh sách");
            listCodeSnapshots.add(list.rows().stream().map(ListRow::code).toList());
            ViewSelectionSnapshot listSelection = currentViewSelection();
            exclusiveSelection &= !listSelection.gridSelected()
                    && listSelection.listSelected();

            click(exactButton("Lưới tháng"),
                    "Lần " + index + " quay lại chế độ Lưới tháng");
            wait.until(d -> selected(exactButton("Lưới tháng")));
            scrollToResult("Quan sát dữ liệu Lưới tháng sau lần chuyển " + index);
            gridCodeSnapshots.add(stockRows().stream().map(StockRow::code).toList());
            ViewSelectionSnapshot gridSelection = currentViewSelection();
            exclusiveSelection &= gridSelection.gridSelected()
                    && !gridSelection.listSelected();
        }
        return new RepeatedViewSwitchSnapshot(
                originalCodes,
                listCodeSnapshots,
                gridCodeSnapshots,
                exclusiveSelection);
    }

    /** Chuyển sang Danh sách trên trạng thái hiện tại, không tải lại route. */
    private ListViewSnapshot activateListView(String step) {
        WebElement button = exactButton("Danh sách");
        click(button, step);
        wait.until(d -> selected(exactButton("Danh sách")));
        List<ListRow> rows = listRows();
        scrollToResult("Quan sát dữ liệu dạng Danh sách");
        return new ListViewSnapshot(
                selected(exactButton("Danh sách")),
                normalizedMainText(),
                rows);
    }

    /** Chuyển Danh sách rồi quay lại Lưới tháng. */
    public ViewSwitchResult switchListAndBackToGrid() {
        switchToListView();
        boolean listSelected = selected(exactButton("Danh sách"));
        click(exactButton("Lưới tháng"), "Quay lại chế độ Lưới tháng");
        wait.until(d -> selected(exactButton("Lưới tháng")));
        scrollToResult("Quan sát dữ liệu Lưới tháng đã khôi phục");
        return new ViewSwitchResult(
                listSelected,
                selected(exactButton("Lưới tháng")),
                !stockRows().isEmpty());
    }

    /** Tìm mã lô và trả dữ liệu đang hiển thị. */
    public SearchResult search(String keyword) {
        openStock();
        int before = stockRows().size();
        return applyGridSearch(keyword, before);
    }

    /** Tìm nguyên văn, không chuẩn hóa từ khóa, để kiểm tra quy tắc nhập mã lô. */
    public SearchResult searchLiteral(String keyword) {
        openStock();
        int before = stockRows().size();
        WebElement input = visible(SEARCH_INPUT);
        fill(input, keyword, "Nhập nguyên văn từ khóa tìm mã lô " + keyword);
        waitForResult();
        List<StockRow> rows = stockRows();
        scrollToResult("Quan sát kết quả tìm kiếm nguyên văn " + keyword);
        return new SearchResult(
                keyword,
                input.getAttribute("value"),
                before,
                rows,
                rows.isEmpty() && (normalizedMainText().contains("chua co du lieu")
                        || normalizedMainText().contains("khong tim thay")));
    }

    /** Tìm một mã có dữ liệu rồi tải lại trang để kiểm tra trạng thái search được đặt lại. */
    public ReloadSearchResult searchThenReload(String keyword) {
        openStock();
        int initialCount = stockRows().size();
        SearchResult filtered = applyGridSearch(keyword, initialCount);
        driver.navigate().refresh();
        waitForLoadingToFinish();
        WebElement input = visible(SEARCH_INPUT);
        pause("Quan sát ô tìm kiếm sau khi tải lại trang");
        List<StockRow> restoredRows = stockRows();
        scrollToResult("Quan sát dữ liệu tồn kho được khôi phục sau khi tải lại trang");
        return new ReloadSearchResult(
                initialCount,
                filtered.rows().size(),
                input.getAttribute("value"),
                restoredRows.size());
    }

    /** Đổi trực tiếp từ từ khóa thứ nhất sang từ khóa thứ hai. */
    public ReplacementSearchResult replaceSearchKeyword(
            String firstKeyword, String secondKeyword) {
        openStock();
        int initialCount = stockRows().size();
        SearchResult first = applyGridSearch(firstKeyword, initialCount);
        SearchResult second = applyGridSearch(secondKeyword, initialCount);
        return new ReplacementSearchResult(first, second);
    }

    /** Tìm mã lô khi đang ở chế độ Danh sách. */
    public ListSearchResult searchInListView(String keyword) {
        openStock();
        activateListView("Chuyển sang Danh sách trước khi tìm mã lô");
        WebElement input = visible(SEARCH_INPUT);
        fill(input, keyword, "Nhập mã lô trong chế độ Danh sách");
        waitForListSearch(keyword);
        List<ListRow> rows = listRows();
        scrollToResult("Cuộn xuống quan sát kết quả tìm trong Danh sách");
        return new ListSearchResult(
                keyword,
                input.getAttribute("value"),
                rows,
                rows.isEmpty() && normalizedMainText().contains("khong tim thay"));
    }

    /** Tìm không có kết quả rồi xóa từ khóa và đọc số dòng được phục hồi. */
    public ClearSearchResult clearSearchAfterEmpty(String keyword) {
        openStock();
        int initialCount = stockRows().size();
        SearchResult empty = applyGridSearch(keyword, initialCount);
        clearCurrentSearch();
        int restored = stockRows().size();
        scrollToResult("Cuộn xuống quan sát dữ liệu phục hồi sau empty-state");
        return new ClearSearchResult(
                initialCount, empty.rows().size(), restored,
                visible(SEARCH_INPUT).getAttribute("value"));
    }

    /** Tìm trong Lưới tháng rồi chuyển view mà không làm mất điều kiện search. */
    public SearchViewSwitchResult searchAndSwitchToList(String keyword) {
        openStock();
        SearchResult gridResult = applyGridSearch(keyword, stockRows().size());
        ListViewSnapshot list = activateListView(
                "Chuyển sang Danh sách khi đang có từ khóa tìm kiếm");
        waitForListSearch(keyword);
        List<ListRow> filteredRows = listRows();
        scrollToResult("Cuộn xuống quan sát kết quả tìm sau khi chuyển view");
        return new SearchViewSwitchResult(
                gridResult,
                visible(SEARCH_INPUT).getAttribute("value"),
                list.selected(),
                filteredRows);
    }

    /** Lấy mã lô đầu tiên, tìm rồi xóa từ khóa và kiểm tra dữ liệu phục hồi. */
    public ClearSearchResult searchThenClear() {
        openStock();
        List<StockRow> initial = stockRows();
        if (initial.isEmpty()) {
            return new ClearSearchResult(0, 0, 0, "");
        }
        String code = initial.get(0).code();
        SearchResult filtered = applyGridSearch(code, initial.size());
        clearCurrentSearch();
        int restored = stockRows().size();
        scrollToResult("Quan sát toàn bộ dữ liệu sau khi xóa từ khóa");
        return new ClearSearchResult(
                initial.size(), filtered.rows().size(), restored,
                visible(SEARCH_INPUT).getAttribute("value"));
    }

    /** Mở drawer của dòng dữ liệu đầu tiên và đối chiếu nội dung. */
    public DetailSnapshot openFirstLotDetail() {
        openStock();
        List<StockRow> rows = stockRows();
        if (rows.isEmpty()) {
            return DetailSnapshot.empty();
        }
        StockRow expected = rows.get(0);
        WebElement row = visibleElements(GRID_ROWS).stream()
                .filter(element -> elementText(element).contains(expected.code()))
                .findFirst().orElseThrow();
        click(row, "Mở chi tiết lô " + expected.code());
        WebElement drawer = visible(LOT_DRAWER);
        scrollInsideDrawer(drawer,
                drawer.findElement(By.cssSelector("h5")),
                "Cuộn về đầu drawer chi tiết lô " + expected.code());
        String content = elementText(drawer);
        Matcher history = Pattern.compile("Lịch sử biến động \\((\\d+)\\)")
                .matcher(content);
        int historyCount = history.find() ? number(history.group(1)) : 0;
        return new DetailSnapshot(
                expected,
                drawer.getAttribute("aria-label"),
                content,
                historyCount,
                !drawer.findElements(By.xpath(
                        ".//button[normalize-space()='Sửa']")).isEmpty());
    }

    /** Mở chi tiết rồi cuộn xuống đúng vùng Lịch sử biến động để quan sát. */
    public DetailSnapshot openFirstLotDetailAndObserveHistory() {
        DetailSnapshot detail = openFirstLotDetail();
        if (detail.expected() == null) {
            return detail;
        }
        WebElement drawer = visible(LOT_DRAWER);
        WebElement historyTitle = drawer.findElements(By.cssSelector("p"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> TextNormalizer.normalize(elementText(element))
                        .contains("lich su bien dong"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Drawer chi tiết thiếu vùng Lịch sử biến động."));
        scrollInsideDrawer(drawer, historyTitle,
                "Cuộn xuống quan sát Lịch sử biến động của lô");
        return detail;
    }

    /** Đọc toàn bộ metadata, tồn và từng dòng lịch sử của lô đang có dữ liệu. */
    public LotBusinessSnapshot openFirstLotBusinessDetail() {
        DetailSnapshot detail = openFirstLotDetail();
        if (detail.expected() == null) {
            return LotBusinessSnapshot.empty();
        }
        WebElement drawer = visible(LOT_DRAWER);
        WebElement priceLabel = exactDrawerText(drawer, "Giá nhập / cái");
        WebElement priceValue = priceLabel.findElement(By.xpath("./following-sibling::*[1]"));
        scrollInsideDrawer(drawer, priceValue,
                "Quan sát giá nhập và thông tin nhập kho của lô");

        WebElement dateLabel = exactDrawerText(drawer, "Ngày nhập");
        WebElement dateValue = dateLabel.findElement(By.xpath("./following-sibling::*[1]"));
        WebElement creatorLabel = exactDrawerText(drawer, "Tạo bởi");
        String creator = creatorLabel.findElements(By.xpath("./following-sibling::*[1]"))
                .stream().map(this::elementText).findFirst().orElse("");

        WebElement stockTitle = exactDrawerText(drawer, "Tồn kho tổng");
        WebElement stockCard = stockTitle.findElement(By.xpath(
                "ancestor::div[contains(@class,'rounded-2xl')][1]"));
        WebElement stockValue = stockCard.findElements(By.cssSelector("p")).stream()
                .filter(element -> element.getAttribute("class") != null
                        && element.getAttribute("class").contains("text-4xl"))
                .findFirst().orElseThrow();
        scrollInsideDrawer(drawer, stockValue,
                "Quan sát tồn kho tổng trong chi tiết lô");

        List<MovementEntry> movements = movementEntries(drawer);
        if (!movements.isEmpty()) {
            List<WebElement> historyRows = movementRowElements(drawer);
            scrollInsideDrawer(drawer, historyRows.get(historyRows.size() - 1),
                    "Cuộn đến phiếu cuối trong lịch sử biến động");
        }
        return new LotBusinessSnapshot(
                detail,
                number(elementText(priceValue)),
                elementText(dateValue),
                true,
                creator,
                number(elementText(stockValue)),
                movements);
    }

    private List<MovementEntry> movementEntries(WebElement drawer) {
        List<MovementEntry> entries = new ArrayList<>();
        for (WebElement row : movementRowElements(drawer)) {
            List<WebElement> cells = row.findElements(By.xpath("./*"));
            if (cells.size() < 4) {
                continue;
            }
            List<String> identityLines = cells.get(0).findElements(By.cssSelector("p"))
                    .stream().map(this::elementText).filter(text -> !text.isBlank()).toList();
            List<String> actorLines = cells.get(1).findElements(By.cssSelector("p"))
                    .stream().map(this::elementText).filter(text -> !text.isBlank()).toList();
            String code = identityLines.isEmpty() ? "" : identityLines.get(0);
            String type = identityLines.size() < 2 ? "" : identityLines.get(1);
            String operator = actorLines.isEmpty()
                    ? "" : actorLines.get(actorLines.size() - 1);
            String note = actorLines.size() < 2
                    ? "" : String.join(" ", actorLines.subList(0, actorLines.size() - 1));
            entries.add(new MovementEntry(
                    code, type, note, operator,
                    elementText(cells.get(2)), number(elementText(cells.get(3))),
                    elementText(row)));
        }
        return entries;
    }

    private List<WebElement> movementRowElements(WebElement drawer) {
        WebElement historyTitle = drawer.findElements(By.cssSelector("p")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> TextNormalizer.normalize(elementText(element))
                        .contains("lich su bien dong"))
                .findFirst().orElseThrow();
        return historyTitle.findElement(By.xpath("parent::div/following-sibling::div[1]"))
                .findElements(By.xpath("./div[contains(@class,'grid')]"));
    }

    private WebElement exactDrawerText(WebElement drawer, String text) {
        return drawer.findElement(By.xpath(
                ".//*[normalize-space()=" + xpathLiteral(text) + "]"));
    }

    /** Mở chế độ sửa và đọc giá trị form nhưng không lưu. */
    public EditSnapshot openEditForm() {
        DetailSnapshot detail = openFirstLotDetail();
        if (detail.expected() == null) {
            return EditSnapshot.empty();
        }
        WebElement drawer = visible(LOT_DRAWER);
        WebElement edit = drawer.findElement(By.xpath(
                ".//button[normalize-space()='Sửa']"));
        click(edit, "Mở form sửa lô " + detail.expected().code());
        drawer = visible(LOT_DRAWER);
        WebElement codeInput = drawer.findElement(By.cssSelector("input"));
        scrollInsideDrawer(drawer, codeInput,
                "Cuộn đến form và quan sát dữ liệu sửa lô");
        String content = elementText(drawer);
        String normalizedContent = TextNormalizer.normalize(content);
        return new EditSnapshot(
                detail.expected(),
                codeInput.getAttribute("value"),
                content,
                drawer.findElements(By.cssSelector("input")).size(),
                "Mã lô".equals(codeInput.getAttribute("aria-label")),
                !drawer.findElements(By.cssSelector("button[title='Huỷ sửa']")).isEmpty(),
                !drawer.findElements(By.xpath(
                        ".//button[normalize-space()='Lưu thay đổi']")).isEmpty(),
                normalizedContent.contains("san pham khong co bien the de sua"),
                normalizedContent.contains("gia nhap")
                        && normalizedContent.contains("ngay nhap")
                        && normalizedContent.contains("tao boi")
                        && normalizedContent.contains("ton kho tong"),
                normalizedContent.contains("lich su bien dong"));
    }

    /** Hủy chế độ sửa, giữ drawer chi tiết và không gửi dữ liệu. */
    public CancelEditResult cancelEdit() {
        EditSnapshot edit = openEditForm();
        if (edit.expected() == null) {
            return new CancelEditResult(false, false, "");
        }
        WebElement drawer = visible(LOT_DRAWER);
        List<WebElement> cancelEditButtons = drawer.findElements(
                By.cssSelector("button[title='Huỷ sửa']"));
        if (cancelEditButtons.isEmpty()) {
            throw new IllegalStateException(
                    "Form sửa lô không hiển thị nút Huỷ sửa theo element thực tế.");
        }
        click(cancelEditButtons.get(0),
                "Hủy sửa lô mà không lưu thay đổi");
        drawer = visible(LOT_DRAWER);
        WebElement detailStock = drawer.findElements(By.cssSelector("p"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> TextNormalizer.normalize(elementText(element))
                        .contains("ton kho tong"))
                .findFirst()
                .orElse(drawer);
        scrollInsideDrawer(drawer, detailStock,
                "Cuộn quan sát chi tiết lô sau khi hủy sửa");
        return new CancelEditResult(
                visibleElements(LOT_DRAWER).size() == 1,
                drawer.findElements(By.cssSelector("input")).isEmpty(),
                elementText(drawer));
    }

    /** Thay đổi mã trên form rồi bấm Huỷ sửa để xác nhận dữ liệu chưa được gửi. */
    public CancelChangedEditResult changeCodeThenCancel() {
        EditSnapshot edit = openEditForm();
        if (edit.expected() == null) {
            return new CancelChangedEditResult("", "", false, false);
        }
        WebElement drawer = visible(LOT_DRAWER);
        WebElement input = drawer.findElement(By.cssSelector("input[aria-label='Mã lô']"));
        String changedCode = "VT" + (800 + System.currentTimeMillis() % 99);
        setReactInputValue(input, changedCode);
        pause("Quan sát mã lô đã thay đổi nhưng chưa lưu");
        click(drawer.findElement(By.cssSelector("button[title='Huỷ sửa']")),
                "Hủy thay đổi mã lô chưa lưu");
        drawer = visible(LOT_DRAWER);
        scrollInsideDrawer(drawer, drawer.findElement(By.cssSelector("h5")),
                "Quan sát mã lô ban đầu sau khi hủy sửa");
        String content = elementText(drawer);
        return new CancelChangedEditResult(
                edit.expected().code(), changedCode,
                content.contains(edit.expected().code()),
                !content.contains(changedCode));
    }

    /** Thử lưu mã không hợp lệ và giữ nguyên dữ liệu nếu form từ chối. */
    public InvalidLotCodeResult submitInvalidLotCode(String candidate) {
        EditSnapshot edit = openEditForm();
        if (edit.expected() == null) {
            return InvalidLotCodeResult.empty();
        }
        WebElement drawer = visible(LOT_DRAWER);
        WebElement input = drawer.findElement(By.cssSelector("input[aria-label='Mã lô']"));
        setReactInputValue(input, candidate);
        pause(candidate.isBlank()
                ? "Quan sát trường Mã lô bị bỏ trống"
                : "Quan sát mã lô trùng đã nhập");
        WebElement save = drawer.findElement(By.xpath(
                ".//button[normalize-space()='Lưu thay đổi']"));
        boolean disabledBeforeSubmit = !save.isEnabled()
                || save.getAttribute("disabled") != null
                || "true".equals(save.getAttribute("aria-disabled"));
        if (!disabledBeforeSubmit) {
            click(save, "Thử lưu mã lô không hợp lệ");
            settle(1_000);
        }
        boolean editStillOpen = visibleElements(LOT_DRAWER).stream().anyMatch(element ->
                !element.findElements(By.cssSelector("input[aria-label='Mã lô']")).isEmpty());
        String feedback = driver.findElement(By.tagName("body")).getText();
        boolean rejected = disabledBeforeSubmit || editStillOpen;
        if (editStillOpen) {
            WebElement currentDrawer = visible(LOT_DRAWER);
            click(currentDrawer.findElement(By.cssSelector("button[title='Huỷ sửa']")),
                    "Hủy form sau khi kiểm tra validation mã lô");
        } else if (!candidate.isBlank() && currentDetailHasCode(candidate)) {
            // Phòng vệ khi hệ thống nhận sai dữ liệu trùng: trả lô đang chọn về mã ban đầu.
            openCurrentDetailEdit(candidate);
            saveCurrentLotCode(edit.expected().code(),
                    "Khôi phục mã lô sau validation", "Lưu lại mã lô ban đầu");
        }
        return new InvalidLotCodeResult(
                edit.expected().code(), candidate, disabledBeforeSubmit,
                editStillOpen, rejected, feedback);
    }

    /** Cuộn vùng nội dung drawer xuống cuối và trở lại đầu, giữ 500 ms ở cả hai vị trí. */
    public DrawerScrollResult scrollDetailToBottomAndBackTop() {
        DetailSnapshot detail = openFirstLotDetail();
        if (detail.expected() == null) {
            return new DrawerScrollResult(false, false, 0);
        }
        WebElement drawer = visible(LOT_DRAWER);
        WebElement scroller = drawer.findElement(By.cssSelector("div.flex-1.overflow-y-auto"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollTop=arguments[0].scrollHeight;", scroller);
        pause("Cuộn xuống cuối drawer chi tiết lô");
        long bottom = ((Number) js.executeScript("return arguments[0].scrollTop;", scroller)).longValue();
        js.executeScript("arguments[0].scrollTop=0;", scroller);
        pause("Cuộn trở lại đầu drawer chi tiết lô");
        long top = ((Number) js.executeScript("return arguments[0].scrollTop;", scroller)).longValue();
        return new DrawerScrollResult(bottom > 0, top == 0, bottom);
    }

    /** Cuộn đến cuối lịch sử rồi đóng drawer bằng nút X ở header. */
    public boolean closeDetailFromBottom() {
        DetailSnapshot detail = openFirstLotDetail();
        if (detail.expected() == null) {
            return false;
        }
        WebElement drawer = visible(LOT_DRAWER);
        WebElement scroller = drawer.findElement(By.cssSelector("div.flex-1.overflow-y-auto"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollTop=arguments[0].scrollHeight;", scroller);
        pause("Quan sát cuối lịch sử trước khi đóng drawer");
        click(drawer.findElements(By.cssSelector(":scope > div:first-child button")).get(0),
                "Đóng drawer từ vị trí cuối lịch sử");
        wait.until(d -> visibleElements(LOT_DRAWER).isEmpty());
        pause("Quan sát bảng tồn kho sau khi đóng drawer");
        return !stockRows().isEmpty();
    }

    /**
     * Đổi mã lô thật, tìm lại dữ liệu đã lưu, sau đó đổi về mã ban đầu.
     * Luồng luôn cố gắng khôi phục dữ liệu nếu assertion trung gian gặp lỗi.
     */
    public LotCodeUpdateResult updateLotCodeAndRestore() {
        EditSnapshot edit = openEditForm();
        if (edit.expected() == null) {
            return LotCodeUpdateResult.empty();
        }
        String originalCode = edit.expected().code();
        // Dữ liệu hiện tại dùng mã ngắn VTxx; giữ mã tạm trong 5 ký tự
        // để không phụ thuộc validation độ dài không được khai báo trên HTML.
        String temporaryCode = "VT" + (900
                + System.currentTimeMillis() % 99);
        boolean savedTemporary = false;
        boolean restored = false;
        try {
            saveCurrentLotCode(temporaryCode,
                    "Nhập mã lô tạm " + temporaryCode,
                    "Lưu thay đổi mã lô thật");
            savedTemporary = currentDetailHasCode(temporaryCode);
            if (!savedTemporary) {
                return new LotCodeUpdateResult(
                        originalCode, temporaryCode, false, false);
            }

            openCurrentDetailEdit(temporaryCode);
            saveCurrentLotCode(originalCode,
                    "Nhập lại mã lô ban đầu " + originalCode,
                    "Khôi phục mã lô ban đầu");
            restored = currentDetailHasCode(originalCode)
                    && findLotByCode(originalCode) != null;
            return new LotCodeUpdateResult(
                    originalCode, temporaryCode, true, restored);
        } finally {
            if (savedTemporary && !restored) {
                try {
                    if (currentDetailHasCode(temporaryCode)) {
                        openCurrentDetailEdit(temporaryCode);
                        saveCurrentLotCode(originalCode,
                                "Khôi phục mã lô sau lỗi kiểm tra",
                                "Xác nhận khôi phục mã lô");
                        findLotByCode(originalCode);
                    }
                } catch (RuntimeException ignored) {
                    // Kết quả testcase vẫn phản ánh lỗi chính; dữ liệu tạm được ghi trong result.
                }
            }
        }
    }

    private void saveCurrentLotCode(
            String code, String fillStep, String saveStep) {
        WebElement drawer = visible(LOT_DRAWER);
        WebElement input = drawer.findElement(By.cssSelector(
                "input[aria-label='Mã lô']"));
        click(input, fillStep);
        // Input React controlled không nhận đủ state khi Selenium clear/sendKeys;
        // gọi native setter và phát input/change để UI lẫn state cùng nhận mã mới.
        ((JavascriptExecutor) driver).executeScript("""
                const input = arguments[0];
                const value = arguments[1];
                const setter = Object.getOwnPropertyDescriptor(
                        window.HTMLInputElement.prototype, 'value').set;
                setter.call(input, value);
                input.dispatchEvent(new Event('input', {bubbles: true}));
                input.dispatchEvent(new Event('change', {bubbles: true}));
                """, input, code);
        wait.until(d -> code.equalsIgnoreCase(input.getAttribute("value")));
        pause("Quan sát mã lô đã nhập " + code);
        WebElement save = drawer.findElement(By.xpath(
                ".//button[normalize-space()='Lưu thay đổi']"));
        click(save, saveStep);
        wait.until(d -> visibleElements(LOT_DRAWER).isEmpty()
                || visibleElements(LOT_DRAWER).stream()
                .noneMatch(element -> !element.findElements(By.cssSelector(
                        "input[aria-label='Mã lô']")).isEmpty()));
        if (!visibleElements(LOT_DRAWER).isEmpty()) {
            pause("Quan sát chi tiết lô sau khi lưu " + code);
        }
    }

    private void setReactInputValue(WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript("""
                const input = arguments[0];
                const setter = Object.getOwnPropertyDescriptor(
                        window.HTMLInputElement.prototype, 'value').set;
                setter.call(input, arguments[1]);
                input.dispatchEvent(new Event('input', {bubbles: true}));
                input.dispatchEvent(new Event('change', {bubbles: true}));
                """, input, value);
        wait.until(d -> value.equals(input.getAttribute("value")));
    }

    private void openCurrentDetailEdit(String code) {
        WebElement drawer = visible(LOT_DRAWER);
        click(drawer.findElement(By.xpath(
                        ".//button[normalize-space()='Sửa']")),
                "Mở form sửa lô " + code);
        visible(LOT_DRAWER).findElement(By.cssSelector(
                "input[aria-label='Mã lô']"));
    }

    private boolean currentDetailHasCode(String code) {
        return visibleElements(LOT_DRAWER).stream().anyMatch(drawer ->
                ("drawer-Lô " + code).equalsIgnoreCase(
                        drawer.getAttribute("aria-label"))
                        && drawer.findElements(By.cssSelector(
                        "input[aria-label='Mã lô']")).isEmpty());
    }

    private StockRow findLotByCode(String code) {
        openStock();
        int initialCount = stockRows().size();
        SearchResult result = applyGridSearch(code, initialCount);
        StockRow exact = result.rows().stream()
                .filter(row -> row.code().equalsIgnoreCase(code))
                .findFirst().orElse(null);
        if (exact != null) {
            scrollToResult("Quan sát lô " + code + " sau khi lưu");
        }
        return exact;
    }

    /** Đóng drawer chi tiết bằng nút X ở header. */
    public boolean closeDetailDrawer() {
        DetailSnapshot detail = openFirstLotDetail();
        if (detail.expected() == null) {
            return false;
        }
        WebElement drawer = visible(LOT_DRAWER);
        List<WebElement> buttons = drawer.findElements(By.cssSelector("button"));
        if (buttons.isEmpty()) {
            return false;
        }
        click(buttons.get(0), "Đóng drawer chi tiết lô");
        wait.until(d -> visibleElements(LOT_DRAWER).isEmpty());
        pause("Quan sát danh sách tồn kho sau khi đóng drawer");
        return !stockRows().isEmpty();
    }

    private List<ListRow> listRows() {
        return visibleElements(LIST_ROWS).stream().map(row -> {
            String text = elementText(row);
            Matcher code = LOT_CODE.matcher(text);
            String lotCode = code.find() ? code.group() : "";
            List<String> parts = row.findElements(By.xpath("./*"))
                    .stream().map(this::elementText).toList();
            int stock = parts.size() > 2 ? number(parts.get(2)) : -1;
            String importDate = parts.size() > 3 ? firstDate(parts.get(3)) : "";
            String latestExport = parts.size() > 4 ? parts.get(4) : "";
            return new ListRow(
                    lotCode, parts, text, stock, importDate, latestExport);
        }).filter(row -> !row.code().isBlank()).toList();
    }

    private ViewSelectionSnapshot currentViewSelection() {
        return new ViewSelectionSnapshot(
                selected(exactButton("Lưới tháng")),
                selected(exactButton("Danh sách")));
    }

    private void waitForSearch(String keyword) {
        String expected = keyword.trim().toUpperCase();
        wait.until(d -> {
            List<StockRow> rows = stockRows();
            return (!rows.isEmpty()
                    && rows.stream().allMatch(row -> row.code().contains(expected)))
                    || (rows.isEmpty()
                    && (normalizedMainText().contains("chua co du lieu")
                    || normalizedMainText().contains("khong tim thay")));
        });
    }

    private void waitForListSearch(String keyword) {
        String expected = keyword.trim().toUpperCase();
        wait.until(d -> {
            List<ListRow> rows = listRows();
            return (!rows.isEmpty()
                    && rows.stream().allMatch(row -> row.code().contains(expected)))
                    || (rows.isEmpty()
                    && normalizedMainText().contains("khong tim thay"));
        });
    }

    private SearchResult applyGridSearch(String keyword, int before) {
        WebElement input = visible(SEARCH_INPUT);
        fill(input, keyword, "Nhập từ khóa tìm mã lô " + keyword);
        waitForSearch(keyword);
        List<StockRow> rows = stockRows();
        scrollToResult("Cuộn xuống quan sát kết quả tìm mã lô " + keyword);
        return new SearchResult(
                keyword,
                input.getAttribute("value"),
                before,
                rows,
                rows.isEmpty() && (normalizedMainText().contains("chua co du lieu")
                        || normalizedMainText().contains("khong tim thay")));
    }

    private void clearCurrentSearch() {
        WebElement input = visible(SEARCH_INPUT);
        List<WebElement> clearButtons = visibleElements(CLEAR_SEARCH);
        if (!clearButtons.isEmpty()) {
            click(clearButtons.get(0), "Xóa từ khóa tìm mã lô");
        } else {
            fill(input, "", "Xóa từ khóa tìm mã lô");
            input.sendKeys(Keys.BACK_SPACE);
        }
        wait.until(d -> visible(SEARCH_INPUT).getAttribute("value").isBlank());
        waitForResult();
    }

    private WebElement exactButton(String text) {
        return visible(By.xpath("//main//button[normalize-space()="
                + xpathLiteral(text) + "]"));
    }

    private boolean hasExactButton(String text) {
        return !visibleElements(By.xpath("//main//button[normalize-space()="
                + xpathLiteral(text) + "]")).isEmpty();
    }

    private boolean selected(WebElement button) {
        String className = button.getAttribute("class");
        return "true".equals(button.getAttribute("aria-selected"))
                || className != null && className.contains("bg-primary-blue")
                && className.contains("text-white");
    }

    private WebElement cardElement(String title) {
        WebElement titleElement = exactText(title);
        return titleElement.findElement(By.xpath(
                "./ancestor::div[contains(@class,'rounded-2xl')][1]"));
    }

    private int lowStockCountFromBanner() {
        Matcher matcher = Pattern.compile("(\\d+)\\s*lo sap het")
                .matcher(normalizedMainText());
        return matcher.find() ? number(matcher.group(1)) : 0;
    }

    private String firstDate(String value) {
        Matcher matcher = DATE.matcher(value);
        return matcher.find() ? matcher.group() : "";
    }

    private int firstSignedQuantity(String value) {
        Matcher matcher = Pattern.compile("[+-]\\s*([\\d,.]+)").matcher(value);
        return matcher.find() ? number(matcher.group(1)) : 0;
    }

    private List<WebElement> visibleDrawerButtons(WebElement drawer) {
        return drawer.findElements(By.cssSelector("button")).stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    private void scrollToResult(String step) {
        WebElement target = visibleElements(By.cssSelector("main table"))
                .stream().findFirst()
                .orElseGet(() -> visibleElements(LIST_ROWS).stream()
                        .findFirst()
                        .orElseGet(() -> visible(By.tagName("main"))));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'start',behavior:'smooth'});"
                        + "window.scrollBy(0,-100);", target);
        pause(step);
    }

    private void observeGridTable(String step) {
        WebElement table = visible(By.cssSelector("main table"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", table);
        highlight(table);
        pause(step);
        List<WebElement> rows = visibleElements(GRID_ROWS);
        if (!rows.isEmpty()) {
            highlight(rows.get(0));
            pause("Quan sát dòng dữ liệu đầu tiên của Kho tổng");
        }
        returnToPageTop("Trở về đầu trang sau khi đọc dữ liệu Kho tổng");
    }

    private void observeElement(WebElement element, String step) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center',inline:'center'});", element);
        highlight(element);
        pause(step);
    }

    private void returnToPageTop(String step) {
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo({top:0,left:0,behavior:'smooth'});"
                        + "document.scrollingElement.scrollTop=0;");
        pause(step);
    }

    /** Cuộn mục tiêu vào giữa drawer và giữ 500 ms để quan sát. */
    private void scrollInsideDrawer(
            WebElement drawer, WebElement target, String step) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[1].scrollIntoView({block:'center',behavior:'smooth'});",
                drawer, target);
        pause(step);
    }

    private int number(String value) {
        String digits = value == null ? "" : value.replaceAll("[^0-9]", "");
        return digits.isBlank() ? 0 : Integer.parseInt(digits);
    }

    public record StockScreenSnapshot(
            String url,
            boolean mainWarehouseSelected,
            boolean stockSectionSelected,
            String normalizedContent,
            boolean searchInput,
            boolean monthlyGridButton,
            boolean listButton) {
    }

    public record OverviewSnapshot(
            int totalStock,
            int positiveLotCount,
            String latestImportDate,
            int latestImportQuantity,
            String latestExportDate,
            int latestExportQuantity,
            int lowStockCount) {
    }

    public record StockRow(String code, String name, int stock, String rowText) {
    }

    public record FullStockRow(
            String code, String name, int stock,
            List<String> monthlyMovements, String rowText) {
    }

    public record DetailedGridSnapshot(
            List<String> headers, List<String> months, List<FullStockRow> rows) {
    }

    public record WarningEntry(String code, int remaining) {
    }

    public record TableScrollSnapshot(
            int rowCount, boolean reachedLastRow, boolean returnedFirstRow) {
    }

    public record HorizontalTableScrollSnapshot(
            boolean overflowAvailable, long rightPosition, long returnedPosition) {
    }

    public record GridSnapshot(
            List<String> headers,
            List<String> months,
            List<StockRow> rows,
            boolean hasMovement) {
    }

    public record ListRow(
            String code,
            List<String> parts,
            String rowText,
            int stock,
            String importDate,
            String latestExport) {
    }

    public record ListViewSnapshot(
            boolean selected,
            String normalizedContent,
            List<ListRow> rows) {
    }

    public record ViewSwitchResult(
            boolean listSelected,
            boolean gridSelectedAfterBack,
            boolean gridHasData) {
    }

    public record ViewSelectionSnapshot(
            boolean gridSelected,
            boolean listSelected) {
    }

    public record ViewDataConsistencySnapshot(
            List<StockRow> gridRows,
            List<ListRow> listRows,
            ViewSelectionSnapshot selection) {
    }

    public record RepeatedViewSwitchSnapshot(
            List<String> originalCodes,
            List<List<String>> listCodeSnapshots,
            List<List<String>> gridCodeSnapshots,
            boolean exclusiveSelection) {
    }

    public record SearchResult(
            String keyword,
            String inputValue,
            int totalBefore,
            List<StockRow> rows,
            boolean emptyState) {
    }

    public record ReplacementSearchResult(
            SearchResult first,
            SearchResult second) {
    }

    public record ListSearchResult(
            String keyword,
            String inputValue,
            List<ListRow> rows,
            boolean emptyState) {
    }

    public record SearchViewSwitchResult(
            SearchResult gridResult,
            String inputValueAfterSwitch,
            boolean listSelected,
            List<ListRow> listRows) {
    }

    public record ClearSearchResult(
            int initialCount,
            int filteredCount,
            int restoredCount,
            String inputValue) {
    }

    public record ReloadSearchResult(
            int initialCount,
            int filteredCount,
            String inputValueAfterReload,
            int restoredCount) {
    }

    public record DetailSnapshot(
            StockRow expected,
            String ariaLabel,
            String content,
            int historyCount,
            boolean editButton) {
        private static DetailSnapshot empty() {
            return new DetailSnapshot(null, "", "", 0, false);
        }
    }

    public record LotBusinessSnapshot(
            DetailSnapshot detail, int importPrice, String importDate,
            boolean creatorField, String creator, int displayedStock,
            List<MovementEntry> movements) {
        private static LotBusinessSnapshot empty() {
            return new LotBusinessSnapshot(DetailSnapshot.empty(), 0, "",
                    false, "", 0, List.of());
        }

        public int netMovement() {
            return movements.stream().mapToInt(entry -> {
                String type = TextNormalizer.normalize(entry.type());
                if (type.contains("nhap kho") || type.contains("dieu chinh tang")) {
                    return entry.quantity();
                }
                if (type.contains("xuat") || type.contains("dieu chinh giam")) {
                    return -entry.quantity();
                }
                return 0;
            }).sum();
        }
    }

    public record MovementEntry(
            String code, String type, String note, String operator,
            String date, int quantity, String rowText) {
        public LocalDate parsedDate() {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    public record EditSnapshot(
            StockRow expected,
            String codeInput,
            String content,
            int inputCount,
            boolean codeInputLabel,
            boolean cancelEditButton,
            boolean saveButton,
            boolean noVariantNotice,
            boolean readOnlyBusinessFields,
            boolean movementHistory) {
        private static EditSnapshot empty() {
            return new EditSnapshot(null, "", "", 0, false,
                    false, false, false, false, false);
        }
    }

    public record CancelEditResult(
            boolean detailStillOpen,
            boolean editInputClosed,
            String detailContent) {
    }

    public record CancelChangedEditResult(
            String originalCode, String changedCode,
            boolean originalStillDisplayed, boolean changedCodeDiscarded) {
    }

    public record InvalidLotCodeResult(
            String originalCode, String candidate,
            boolean saveDisabled, boolean editStillOpen,
            boolean rejected, String feedback) {
        private static InvalidLotCodeResult empty() {
            return new InvalidLotCodeResult("", "", false, false, false, "");
        }
    }

    public record DrawerScrollResult(
            boolean reachedBottom, boolean returnedTop, long bottomPosition) {
    }

    public record LotCodeUpdateResult(
            String originalCode,
            String temporaryCode,
            boolean temporarySaved,
            boolean originalRestored) {
        private static LotCodeUpdateResult empty() {
            return new LotCodeUpdateResult("", "", false, false);
        }
    }
}
