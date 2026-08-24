package com.vuatho.pages;

import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page Object riêng cho Kho bán hàng → Tồn kho. */
public final class SalesStockPage extends UniformInventoryPage {
    private static final Pattern NUMBER = Pattern.compile("[\\d,.]+");

    public SalesStockPage(WebDriver driver) {
        super(driver, "sub", "Kho bán hàng");
    }

    /** Đọc ba thẻ tổng quan riêng của Kho bán hàng. */
    public SalesOverviewSnapshot salesOverviewSnapshot() {
        openStock();
        WebElement totalCard = card("Tổng tồn kho");
        observeElement(totalCard, "Quan sát thẻ Tổng tồn kho");
        WebElement exportCard = card("Phiếu xuất hôm nay");
        observeElement(exportCard, "Quan sát thẻ Phiếu xuất hôm nay");
        WebElement lowStockCard = card("Lô sắp hết");
        observeElement(lowStockCard, "Quan sát thẻ Lô sắp hết");
        String total = elementText(totalCard);
        String exports = elementText(exportCard);
        String lowStock = elementText(lowStockCard);
        List<Integer> exportNumbers = numbers(exports);
        return new SalesOverviewSnapshot(
                firstNumber(total),
                exportNumbers.isEmpty() ? -1 : exportNumbers.get(0),
                exportNumbers.size() < 2 ? -1 : exportNumbers.get(1),
                firstNumber(lowStock),
                readLowStockEntries());
    }

    /** Đọc từng mã và số lượng còn lại trong danh sách cảnh báo sắp hết. */
    public List<LowStockEntry> lowStockEntries() {
        openStock();
        WebElement scroller = card("Lô sắp hết").findElement(
                By.cssSelector("div.overflow-y-auto"));
        observeElement(scroller, "Quan sát danh sách cảnh báo lô sắp hết");
        return readLowStockEntries();
    }

    private List<LowStockEntry> readLowStockEntries() {
        WebElement card = card("Lô sắp hết");
        List<LowStockEntry> result = new ArrayList<>();
        for (WebElement row : card.findElements(By.cssSelector("div.divide-y > div"))) {
            String text = elementText(row);
            Matcher matcher = Pattern.compile("([A-Z0-9-]+).*?còn\\s+(\\d+)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(text);
            if (matcher.find()) {
                result.add(new LowStockEntry(matcher.group(1),
                        Integer.parseInt(matcher.group(2)), text));
            }
        }
        return result;
    }

    /** Cuộn hết vùng cảnh báo rồi trở lại đầu, mỗi vị trí đều giữ 500 ms để quan sát. */
    public boolean scrollLowStockListDownAndBackUp() {
        openStock();
        WebElement scroller = card("Lô sắp hết").findElement(
                By.cssSelector("div.overflow-y-auto"));
        highlight(scroller);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long before = ((Number) js.executeScript("return arguments[0].scrollTop;", scroller)).longValue();
        js.executeScript("arguments[0].scrollTop=arguments[0].scrollHeight;", scroller);
        pause("Cuộn xuống cuối danh sách lô sắp hết");
        long bottom = ((Number) js.executeScript("return arguments[0].scrollTop;", scroller)).longValue();
        js.executeScript("arguments[0].scrollTop=0;", scroller);
        pause("Cuộn trở lại đầu danh sách lô sắp hết");
        return bottom > before;
    }

    /** Đọc đầy đủ mọi mã lô, kể cả mã tự động không bắt đầu bằng VT. */
    public List<SalesGridRow> salesGridRows() {
        openStock();
        observeGridTable("Quan sát các dòng dữ liệu dùng để đối chiếu");
        return readSalesGridRows();
    }

    /** Đọc tồn nhiều lô trong một lần tải trang, không scroll/highlight phục vụ đối chiếu kỹ thuật. */
    public Map<String, Integer> salesStockValues(List<String> codes) {
        openStock();
        Map<String, Integer> values = new LinkedHashMap<>();
        boolean includeAll = codes.isEmpty();
        for (String code : codes) {
            values.put(code, 0);
        }
        for (SalesGridRow row : readSalesGridRows()) {
            if (includeAll || values.containsKey(row.code())) {
                values.put(row.code(), row.stock());
            }
        }
        return values;
    }

    private List<SalesGridRow> readSalesGridRows() {
        List<SalesGridRow> result = new ArrayList<>();
        for (WebElement row : visibleElements(By.cssSelector("main table tbody tr"))) {
            List<WebElement> cells = row.findElements(By.cssSelector("td"));
            if (cells.size() < 2) {
                continue;
            }
            List<WebElement> badges = cells.get(0).findElements(By.cssSelector("span.font-bold"));
            if (badges.isEmpty()) {
                continue;
            }
            String code = elementText(badges.get(0)).trim();
            String productCell = elementText(cells.get(0));
            String name = productCell.replaceFirst(Pattern.quote(code), "").trim();
            List<String> movements = cells.subList(2, cells.size()).stream()
                    .map(this::elementText).toList();
            result.add(new SalesGridRow(code, name,
                    integerValue(elementText(cells.get(1))), movements,
                    elementText(row)));
        }
        return result;
    }

    /** Đọc tiêu đề và dữ liệu toàn bộ Lưới tháng. */
    public SalesGridSnapshot salesGridSnapshot() {
        openStock();
        observeGridTable("Quan sát tiêu đề và dữ liệu Lưới tháng");
        List<String> headers = visibleElements(By.cssSelector("main table thead th"))
                .stream().map(this::elementText).toList();
        List<String> months = headers.stream()
                .filter(value -> value.matches("\\d{2}/\\d{4}"))
                .toList();
        return new SalesGridSnapshot(headers, months, readSalesGridRows());
    }

    /** Làm nổi bật lần lượt tab kho và mục Tồn kho để thấy rõ route đang kiểm tra. */
    public StockScreenSnapshot observeSelectedStockTab() {
        StockScreenSnapshot snapshot = screenSnapshot();
        observeElement(visible(By.cssSelector("[role='tab'][data-key='sub']")),
                "Quan sát tab Kho bán hàng đang được chọn");
        observeElement(exactText("Tồn kho"),
                "Quan sát mục Tồn kho đang được chọn");
        return snapshot;
    }

    /** Làm nổi bật lần lượt các điều khiển tìm kiếm và chuyển chế độ xem. */
    public StockScreenSnapshot observeStockControls() {
        StockScreenSnapshot snapshot = screenSnapshot();
        observeElement(visible(By.cssSelector("input[placeholder='Tìm mã lô…']")),
                "Quan sát ô Tìm mã lô");
        observeElement(exactText("Lưới tháng"),
                "Quan sát nút Lưới tháng");
        observeElement(exactText("Danh sách"),
                "Quan sát nút Danh sách");
        return snapshot;
    }

    /** Cuộn đến dòng cuối rồi trở lại dòng đầu để quan sát bảng dài. */
    public GridScrollSnapshot scrollGridToLastRowAndBack() {
        openStock();
        List<WebElement> rows = visibleElements(By.cssSelector("main table tbody tr"));
        if (rows.isEmpty()) {
            return new GridScrollSnapshot(0, false, false);
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", rows.get(rows.size() - 1));
        highlight(rows.get(rows.size() - 1));
        pause("Cuộn xuống quan sát dòng tồn kho cuối cùng");
        boolean lastVisible = rows.get(rows.size() - 1).isDisplayed();
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", rows.get(0));
        highlight(rows.get(0));
        pause("Cuộn trở lại quan sát dòng tồn kho đầu tiên");
        boolean firstVisible = rows.get(0).isDisplayed();
        returnToPageTop("Trở về đầu trang sau khi quan sát bảng Kho bán hàng");
        return new GridScrollSnapshot(rows.size(), lastVisible, firstVisible);
    }

    /** Cuộn thanh ngang của bảng đến tháng cuối rồi trở lại tháng đầu nếu bảng có overflow. */
    public HorizontalGridScrollSnapshot scrollGridHorizontallyAndBack() {
        openStock();
        WebElement table = visible(By.cssSelector("main table"));
        WebElement scroller = table.findElement(By.xpath(
                "ancestor::div[contains(@class,'overflow-x-auto')][1]"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long scrollWidth = ((Number) js.executeScript("return arguments[0].scrollWidth;", scroller)).longValue();
        long clientWidth = ((Number) js.executeScript("return arguments[0].clientWidth;", scroller)).longValue();
        boolean overflow = scrollWidth > clientWidth;
        js.executeScript("arguments[0].scrollLeft=arguments[0].scrollWidth;", scroller);
        highlight(table);
        pause("Cuộn ngang đến cột tháng cuối cùng");
        long right = ((Number) js.executeScript("return arguments[0].scrollLeft;", scroller)).longValue();
        js.executeScript("arguments[0].scrollLeft=0;", scroller);
        pause("Cuộn ngang trở lại cột Sản phẩm");
        long left = ((Number) js.executeScript("return arguments[0].scrollLeft;", scroller)).longValue();
        List<String> months = visibleElements(By.cssSelector("main table thead th"))
                .stream().map(this::elementText)
                .filter(value -> value.matches("\\d{2}/\\d{4}"))
                .toList();
        return new HorizontalGridScrollSnapshot(overflow, right, left,
                new LinkedHashSet<>(months).size());
    }

    private void observeGridTable(String step) {
        WebElement table = visible(By.cssSelector("main table"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", table);
        highlight(table);
        pause(step);
        List<WebElement> rows = visibleElements(By.cssSelector("main table tbody tr"));
        if (!rows.isEmpty()) {
            highlight(rows.get(0));
            pause("Quan sát dòng dữ liệu đầu tiên của Lưới tháng");
        }
        returnToPageTop("Trở về đầu trang sau khi đọc Lưới tháng");
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

    private WebElement card(String title) {
        return exactText(title).findElement(By.xpath(
                "./ancestor::div[contains(@class,'rounded-2xl')][1]"));
    }

    private int firstNumber(String value) {
        List<Integer> values = numbers(value);
        return values.isEmpty() ? -1 : values.get(0);
    }

    private List<Integer> numbers(String value) {
        List<Integer> result = new ArrayList<>();
        Matcher matcher = NUMBER.matcher(TextNormalizer.normalize(value));
        while (matcher.find()) {
            result.add(Integer.parseInt(matcher.group().replaceAll("[,.]", "")));
        }
        return result;
    }

    private int integerValue(String value) {
        String digits = value == null ? "" : value.replaceAll("[^0-9]", "");
        return digits.isBlank() ? 0 : Integer.parseInt(digits);
    }

    public record SalesOverviewSnapshot(
            int totalStock,
            int todayExportCount,
            int monthExportCount,
            int lowStockCount,
            List<LowStockEntry> lowStockEntries) {
    }

    public record LowStockEntry(String code, int remaining, String text) {
    }

    public record SalesGridRow(
            String code, String name, int stock,
            List<String> monthlyMovements, String rowText) {
    }

    public record SalesGridSnapshot(
            List<String> headers, List<String> months, List<SalesGridRow> rows) {
    }

    public record GridScrollSnapshot(
            int rowCount, boolean reachedLastRow, boolean returnedFirstRow) {
    }

    public record HorizontalGridScrollSnapshot(
            boolean overflowAvailable, long rightPosition,
            long returnedPosition, int visibleMonthCount) {
    }
}
