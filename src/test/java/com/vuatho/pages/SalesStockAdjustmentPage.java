package com.vuatho.pages;

import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Page Object cho Kho bán hàng → Điều chỉnh tồn. */
public final class SalesStockAdjustmentPage extends StockAdjustmentPage {
    private static final String ROUTE = "/vuatho/inventory-uniform?tab=sub";
    private static final By DIALOG = By.xpath(
            "//section[@role='dialog'][.//*[normalize-space()='Điều chỉnh tồn']]");
    private static final Pattern CHANGED_COUNTER = Pattern.compile(
            "Lô thay đổi:\\s*(\\d+)\\s*/\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOT_CODE = Pattern.compile("\\b[A-Z0-9-]{3,}\\b");
    private static final Pattern OPTION_STOCK = Pattern.compile("(?i)tồn\\s+(\\d+)");

    public SalesStockAdjustmentPage(WebDriver driver) {
        super(driver);
    }

    /** Mở form điều chỉnh tồn từ tab Tồn kho của Kho bán hàng. */
    public SalesStockAdjustmentPage openForm() {
        if (dialogVisible()) {
            click(dialogButton("Hủy"), "Hủy form điều chỉnh còn mở từ testcase trước");
            wait.until(d -> !dialogVisible());
        }
        openRoute(ROUTE);
        WebElement stock = visible(By.xpath("//main//button[normalize-space()='Tồn kho']"));
        if (!isSelected(stock)) {
            click(stock, "Chọn tab Tồn kho của Kho bán hàng");
            waitForResult();
        }
        click(visible(By.xpath("//main//button[normalize-space()='Điều chỉnh tồn']")),
                "Mở form Điều chỉnh tồn Kho bán hàng");
        visible(DIALOG);
        pause("Quan sát form Điều chỉnh tồn Kho bán hàng");
        return this;
    }

    /** Đọc trạng thái mặc định của form, không thay đổi dữ liệu kho. */
    public SalesFormSnapshot salesFormSnapshot() {
        openForm();
        WebElement form = dialog();
        WebElement date = form.findElement(By.cssSelector("input[aria-label='Ngày điều chỉnh']"));
        WebElement reason = form.findElement(By.cssSelector("input[aria-label='Lý do điều chỉnh']"));
        String text = elementText(form);
        Matcher counter = CHANGED_COUNTER.matcher(text);
        int changed = counter.find() ? Integer.parseInt(counter.group(1)) : -1;
        int total = counter.find(0) ? Integer.parseInt(counter.group(2)) : -1;
        String normalized = TextNormalizer.normalize(text);
        return new SalesFormSnapshot(
                date.getAttribute("value"),
                "true".equals(date.getAttribute("aria-required")),
                reason.getAttribute("value"),
                form.findElements(By.cssSelector(
                        "input[role='combobox'][aria-label='Thêm lô cần điều chỉnh']")).size() == 1,
                changed,
                total,
                !confirmButton().isEnabled(),
                normalized.contains("kiem ke kho ban hang")
                        && normalized.contains("khong anh huong kho tong"),
                normalized.contains("chua co lo nao"));
    }

    /** Bấm Hủy và xác nhận form đóng. */
    public boolean cancelForm() {
        openForm();
        click(dialogButton("Hủy"), "Hủy điều chỉnh tồn Kho bán hàng");
        return wait.until(d -> !dialogVisible());
    }

    /** Bấm dấu X và xác nhận form đóng. */
    public boolean closeForm() {
        openForm();
        click(dialog().findElement(By.cssSelector("button[aria-label='Close']")),
                "Đóng form Điều chỉnh tồn Kho bán hàng bằng dấu X");
        return wait.until(d -> !dialogVisible());
    }

    /** Xác nhận tăng một lô rồi điều chỉnh lại để trả tồn về ban đầu. */
    public SubmissionSnapshot submitOneLotAndRestore() {
        QuantitySnapshot lot = enterQuantityDelta(1);
        submitPreparedAdjustment();
        int afterIncrease = salesStock(lot.code());
        String receipt = adjustmentReceiptText(lot.code());

        openForm();
        addLotByCode(lot.code());
        setActual(lot.code(), lot.currentStock());
        submitPreparedAdjustment();
        return new SubmissionSnapshot(lot.code(), lot.currentStock(), lot.expectedActual(),
                afterIncrease, salesStock(lot.code()), receipt);
    }

    /** Xác nhận tăng/giảm hai lô trong cùng phiếu rồi khôi phục cả hai. */
    public MultiSubmissionSnapshot submitTwoLotsAndRestore() {
        MultiLotSnapshot selected = addTwoDifferentLots();
        LotSnapshot first = selected.first();
        LotSnapshot second = selected.second();
        int firstTarget = first.currentStock() + 1;
        int secondTarget = second.currentStock() > 0 ? second.currentStock() - 1 : 1;
        setActual(first.code(), firstTarget);
        setActual(second.code(), secondTarget);
        submitPreparedAdjustment();
        int firstAfter = -1;
        int secondAfter = -1;
        String receipt = "";
        try {
            firstAfter = salesStock(first.code());
            secondAfter = salesStock(second.code());
            receipt = adjustmentReceiptText(first.code());
        } finally {
            openForm();
            addLotByCode(first.code());
            addLotByCode(second.code());
            setActual(first.code(), first.currentStock());
            setActual(second.code(), second.currentStock());
            submitPreparedAdjustment();
        }
        return new MultiSubmissionSnapshot(first.code(), second.code(),
                first.currentStock(), second.currentStock(), firstTarget, secondTarget,
                firstAfter, secondAfter, salesStock(first.code()), salesStock(second.code()), receipt);
    }

    /** Xác nhận đưa một lô tồn dương về 0 rồi khôi phục tồn ban đầu. */
    public SubmissionSnapshot submitPositiveLotToZeroAndRestore() {
        QuantitySnapshot lot = reducePositiveStockToZero();
        submitPreparedAdjustment();
        int afterSubmit = salesStock(lot.code());
        String receipt = adjustmentReceiptText(lot.code());
        openForm();
        addLotByCode(lot.code());
        setActual(lot.code(), lot.currentStock());
        submitPreparedAdjustment();
        return new SubmissionSnapshot(lot.code(), lot.currentStock(), 0,
                afterSubmit, salesStock(lot.code()), receipt);
    }

    /** Xác nhận số thực tế lớn có dấu phẩy rồi khôi phục tồn ban đầu. */
    public SubmissionSnapshot submitThousandsSeparatedActualAndRestore() {
        FormattedQuantitySnapshot lot = acceptsThousandsSeparatedActualQuantity();
        int target = Integer.parseInt(lot.expectedValue().replace(",", ""));
        submitPreparedAdjustment();
        int afterSubmit = salesStock(lot.code());
        String receipt = adjustmentReceiptText(lot.code());
        openForm();
        addLotByCode(lot.code());
        setActual(lot.code(), lot.currentStock());
        submitPreparedAdjustment();
        return new SubmissionSnapshot(lot.code(), lot.currentStock(), target,
                afterSubmit, salesStock(lot.code()), receipt);
    }

    /** Xác nhận ba lô thay đổi trong cùng phiếu rồi khôi phục toàn bộ tồn. */
    public ThreeLotSubmissionSnapshot submitThreeLotsAndRestore() {
        openForm();
        LotSnapshot first = addPositiveLot();
        LotSnapshot second = addPositiveLot();
        LotSnapshot third = addPositiveLot();
        closeSuggestions();
        int firstTarget = first.currentStock() + 1;
        int secondTarget = second.currentStock() - 1;
        int thirdTarget = third.currentStock() + 2;
        setActual(first.code(), firstTarget);
        setActual(second.code(), secondTarget);
        setActual(third.code(), thirdTarget);
        submitPreparedAdjustment();
        int firstAfter = -1;
        int secondAfter = -1;
        int thirdAfter = -1;
        String receipt = "";
        try {
            firstAfter = salesStock(first.code());
            secondAfter = salesStock(second.code());
            thirdAfter = salesStock(third.code());
            receipt = adjustmentReceiptText(first.code());
        } finally {
            openForm();
            addLotByCode(first.code());
            addLotByCode(second.code());
            addLotByCode(third.code());
            setActual(first.code(), first.currentStock());
            setActual(second.code(), second.currentStock());
            setActual(third.code(), third.currentStock());
            submitPreparedAdjustment();
        }
        return new ThreeLotSubmissionSnapshot(
                List.of(first.code(), second.code(), third.code()),
                List.of(firstTarget, secondTarget, thirdTarget),
                List.of(firstAfter, secondAfter, thirdAfter),
                List.of(salesStock(first.code()), salesStock(second.code()), salesStock(third.code())),
                List.of(first.currentStock(), second.currentStock(), third.currentStock()), receipt);
    }

    /** Xác nhận tăng một lô đang tồn 0 lên 1 rồi khôi phục về 0. */
    public SubmissionSnapshot submitZeroStockLotIncreaseAndRestore() {
        openForm();
        LotSnapshot lot = addLotWithStock(0);
        closeSuggestions();
        setActual(lot.code(), 1);
        submitPreparedAdjustment();
        int afterSubmit = salesStock(lot.code());
        String receipt = adjustmentReceiptText(lot.code());
        openForm();
        addLotByCode(lot.code());
        setActual(lot.code(), 0);
        submitPreparedAdjustment();
        return new SubmissionSnapshot(lot.code(), 0, 1,
                afterSubmit, salesStock(lot.code()), receipt);
    }

    /** Submit bằng ngày quá khứ và đối chiếu ngày/metadata trên phiếu vừa tạo. */
    public MetadataSubmissionSnapshot submitPastDateAndRestore() {
        QuantitySnapshot lot = enterQuantityDelta(1);
        LocalDate date = LocalDate.now().minusDays(1);
        WebElement dateInput = dialog().findElement(By.cssSelector("input[aria-label='Ngày điều chỉnh']"));
        dateInput.clear();
        dateInput.sendKeys(date.format(DateTimeFormatter.ofPattern("MMddyyyy")));
        WebElement reason = dialog().findElement(By.cssSelector("input[aria-label='Lý do điều chỉnh']"));
        String reasonText = "Automation kiểm tra metadata phiếu điều chỉnh";
        fill(reason, reasonText, "Nhập lý do trước khi xác nhận");
        submitPreparedAdjustment();
        String receipt = adjustmentReceiptText(lot.code());
        openForm();
        addLotByCode(lot.code());
        setActual(lot.code(), lot.currentStock());
        submitPreparedAdjustment();
        return new MetadataSubmissionSnapshot(lot.code(), date.toString(), reasonText,
                receipt, salesStock(lot.code()), lot.currentStock());
    }

    /** Điều chỉnh Kho bán hàng rồi xác nhận tồn cùng mã ở Kho tổng không đổi. */
    public WarehouseScopeSubmissionSnapshot submitWithoutChangingMainWarehouse() {
        openForm();
        LotSnapshot lot = addPositiveLot();
        closeSuggestions();
        click(dialogButton("Hủy"), "Đóng form để đọc tồn Kho tổng trước submit");
        wait.until(d -> !dialogVisible());
        int mainBefore = mainWarehouseStock(lot.code());

        openForm();
        addLotByCode(lot.code());
        setActual(lot.code(), lot.currentStock() + 1);
        submitPreparedAdjustment();
        int mainAfter = mainWarehouseStock(lot.code());
        openForm();
        addLotByCode(lot.code());
        setActual(lot.code(), lot.currentStock());
        submitPreparedAdjustment();
        return new WarehouseScopeSubmissionSnapshot(lot.code(), mainBefore, mainAfter,
                salesStock(lot.code()), lot.currentStock());
    }

    private WebElement dialog() {
        return visible(DIALOG);
    }

    private boolean dialogVisible() {
        return !visibleElements(DIALOG).isEmpty();
    }

    private WebElement dialogButton(String label) {
        return dialog().findElement(By.xpath(".//button[normalize-space()="
                + xpathLiteral(label) + "]"));
    }

    private WebElement confirmButton() {
        return dialogButton("Xác nhận điều chỉnh");
    }

    private void addLotByCode(String code) {
        WebElement combo = dialog().findElement(By.cssSelector(
                "input[role='combobox'][aria-label='Thêm lô cần điều chỉnh']"));
        fill(combo, code, "Tìm lô " + code + " để điều chỉnh");
        wait.until(d -> d.findElements(By.cssSelector("[role='option']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> elementText(item).contains(code))
                .findFirst()
                .map(item -> {
                    try {
                        item.click();
                        return true;
                    } catch (RuntimeException ignored) {
                        return false;
                    }
                }).orElse(false));
        wait.until(d -> !rowsForCode(code).isEmpty());
        closeSuggestions();
    }

    private LotSnapshot addPositiveLot() {
        WebElement combo = dialog().findElement(By.cssSelector(
                "input[role='combobox'][aria-label='Thêm lô cần điều chỉnh']"));
        fill(combo, "VT", "Tìm lô còn tồn để điều chỉnh");
        WebElement option = wait.until(d -> d.findElements(By.cssSelector("[role='option']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> stockInOption(elementText(item)) > 0)
                .findFirst().orElse(null));
        String text = elementText(option);
        String code = lotCode(text);
        int stock = stockInOption(text);
        wait.until(d -> d.findElements(By.cssSelector("[role='option']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> elementText(item).contains(code))
                .findFirst()
                .map(item -> {
                    try {
                        item.click();
                        return true;
                    } catch (RuntimeException ignored) {
                        return false;
                    }
                }).orElse(false));
        wait.until(d -> !rowsForCode(code).isEmpty());
        return new LotSnapshot(code, stock,
                rowForCode(code).findElement(By.cssSelector("input[aria-label='Số thực tế']")).getAttribute("value"),
                elementText(rowForCode(code)));
    }

    private LotSnapshot addLotWithStock(int expectedStock) {
        WebElement combo = dialog().findElement(By.cssSelector(
                "input[role='combobox'][aria-label='Thêm lô cần điều chỉnh']"));
        fill(combo, "VT", "Tìm lô có tồn " + expectedStock);
        WebElement option = wait.until(d -> d.findElements(By.cssSelector("[role='option']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> stockInOption(elementText(item)) == expectedStock)
                .findFirst().orElse(null));
        String text = elementText(option);
        String code = lotCode(text);
        wait.until(d -> d.findElements(By.cssSelector("[role='option']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> elementText(item).contains(code))
                .findFirst().map(item -> {
                    try { item.click(); return true; }
                    catch (RuntimeException ignored) { return false; }
                }).orElse(false));
        wait.until(d -> !rowsForCode(code).isEmpty());
        return new LotSnapshot(code, expectedStock, "", elementText(rowForCode(code)));
    }

    private int stockInOption(String value) {
        Matcher matcher = OPTION_STOCK.matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    private String lotCode(String value) {
        Matcher matcher = LOT_CODE.matcher(value);
        if (!matcher.find()) {
            throw new IllegalStateException("Không đọc được mã lô từ gợi ý: " + value);
        }
        return matcher.group();
    }

    private void setActual(String code, int value) {
        WebElement input = rowForCode(code).findElement(By.cssSelector("input[aria-label='Số thực tế']"));
        fill(input, Integer.toString(value), "Nhập số thực tế cho lô " + code);
        settle(400);
    }

    private void submitPreparedAdjustment() {
        if (!confirmButton().isEnabled()) {
            throw new IllegalStateException("Nút xác nhận điều chỉnh chưa được bật.");
        }
        click(confirmButton(), "Xác nhận điều chỉnh tồn");
        wait.until(d -> !dialogVisible());
        waitForResult();
    }

    private int salesStock(String code) {
        return new SalesStockPage(driver).salesGridRows().stream()
                .filter(row -> row.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lô " + code + " trong Kho bán hàng."))
                .stock();
    }

    private int mainWarehouseStock(String code) {
        return new UniformInventoryPage(driver).openStock().stockRows().stream()
                .filter(row -> row.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy lô " + code + " trong Kho tổng để đối chiếu phạm vi."))
                .stock();
    }

    private String adjustmentReceiptText(String code) {
        openRoute(ROUTE);
        click(visible(By.xpath("//main//button[normalize-space()='Phiếu']")),
                "Mở danh sách phiếu Kho bán hàng");
        waitForResult();
        WebElement receipt = visible(By.xpath("//main//div[contains(@class,'grid')]["
                + ".//*[normalize-space()='Điều chỉnh tồn'] and .//*[normalize-space()="
                + xpathLiteral(code) + "]]"));
        return elementText(receipt);
    }

    private WebElement rowForCode(String code) {
        return rowsForCode(code).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy dòng lô " + code + " trong form."));
    }

    private List<WebElement> rowsForCode(String code) {
        return dialog().findElements(By.xpath(".//span[normalize-space()=" + xpathLiteral(code)
                + "]/ancestor::div[contains(@class,'grid')][1]"));
    }

    private void closeSuggestions() {
        WebElement combo = dialog().findElement(By.cssSelector("input[role='combobox']"));
        if ("true".equals(combo.getAttribute("aria-expanded"))) {
            combo.sendKeys(org.openqa.selenium.Keys.ESCAPE);
            wait.until(d -> !"true".equals(combo.getAttribute("aria-expanded")));
        }
    }

    private boolean isSelected(WebElement element) {
        String classes = element.getAttribute("class");
        return classes != null && classes.contains("bg-primary-blue");
    }

    public record SalesFormSnapshot(String date, boolean dateRequired, String reason,
                                    boolean lotCombobox, int changedLots, int totalLots,
                                    boolean confirmDisabled, boolean salesWarehouseScope,
                                    boolean emptyState) { }

    public record SubmissionSnapshot(String code, int initialStock, int expectedStock,
                                     int stockAfterSubmit, int stockAfterRestore,
                                     String receiptText) { }

    public record MultiSubmissionSnapshot(String firstCode, String secondCode,
                                          int firstInitial, int secondInitial,
                                          int firstExpected, int secondExpected,
                                          int firstAfterSubmit, int secondAfterSubmit,
                                          int firstAfterRestore, int secondAfterRestore,
                                          String receiptText) { }

    public record ThreeLotSubmissionSnapshot(List<String> codes, List<Integer> expectedStocks,
                                             List<Integer> stocksAfterSubmit,
                                             List<Integer> stocksAfterRestore,
                                             List<Integer> initialStocks,
                                             String receiptText) { }

    public record MetadataSubmissionSnapshot(String code, String expectedDate, String reason,
                                             String receiptText, int stockAfterRestore,
                                             int initialStock) { }

    public record WarehouseScopeSubmissionSnapshot(String code, int mainStockBefore,
                                                   int mainStockAfter, int salesStockAfterRestore,
                                                   int salesStockInitial) { }
}
