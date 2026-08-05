package com.vuatho.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page Object cho Kho bán hàng → Phiếu → Xuất hàng cho nhân sự. */
public final class SalesStockStaffExportPage extends UniformUiPage {
    private static final String ROUTE = "/vuatho/inventory-uniform?tab=sub";
    private static final By DIALOG = By.xpath("//section[@role='dialog'][.//*[normalize-space()='Xuất hàng cho nhân sự']]");
    private static final By LOT_OPTIONS = By.cssSelector("[role='option']");
    private static final Pattern LOT_CODE = Pattern.compile("\\bVT\\d+\\b");
    private static final Pattern STOCK = Pattern.compile(
            "(?i)tồn(?:\\s+kho\\s+bán\\s+hàng)?\\s*(\\d+)\\s*(?:cái)?");
    private static final Pattern SELECTED = Pattern.compile("Lô được chọn:\\s*(\\d+)\\s*/\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL = Pattern.compile("Tổng SL:\\s*([\\d,]+)", Pattern.CASE_INSENSITIVE);

    public SalesStockStaffExportPage(WebDriver driver) {
        super(driver);
    }

    /** Mở form xuất hàng từ tab Phiếu của Kho bán hàng. */
    public SalesStockStaffExportPage openForm() {
        if (dialogVisible()) {
            click(dialogButton("Hủy"), "Hủy form xuất còn mở từ testcase trước");
            wait.until(d -> !dialogVisible());
        }
        openRoute(ROUTE);
        WebElement receipts = visible(By.xpath("//main//button[normalize-space()='Phiếu']"));
        if (!isSelected(receipts)) {
            click(receipts, "Chọn tab Phiếu của Kho bán hàng");
            waitForResult();
        }
        click(visible(By.xpath("//main//button[normalize-space()='Xuất hàng']")),
                "Mở form Xuất hàng cho nhân sự");
        visible(DIALOG);
        pause("Quan sát form Xuất hàng cho nhân sự");
        return this;
    }

    /** Kiểm tra cấu trúc ban đầu của form, không tạo phiếu. */
    public FormSnapshot formSnapshot() {
        openForm();
        WebElement form = dialog();
        return new FormSnapshot(
                form.findElement(By.cssSelector("input[aria-label='Ngày xuất']")).getAttribute("value"),
                "true".equals(form.findElement(By.cssSelector("input[aria-label='Ngày xuất']"))
                        .getAttribute("aria-required")),
                form.findElement(By.cssSelector("input[aria-label='Ghi chú']")).getAttribute("value"),
                form.findElements(By.cssSelector("input[aria-label='Thêm lô']")).size() == 1,
                selectedCount(), totalQuantity(), confirmButton().isEnabled(), elementText(form));
    }

    /** Nhập lần lượt ngày xuất quá khứ và tương lai để xác nhận form chấp nhận cả hai. */
    public DateRangeSnapshot acceptsPastAndFutureDates() {
        openForm();
        LocalDate past = LocalDate.now().minusDays(1);
        LocalDate future = LocalDate.now().plusDays(1);
        setExportDate(past);
        String actualPast = exportDateInput().getAttribute("value");
        setExportDate(future);
        String actualFuture = exportDateInput().getAttribute("value");
        return new DateRangeSnapshot(past.toString(), actualPast, future.toString(), actualFuture);
    }

    /** Kiểm tra ghi chú dài Unicode và ký tự đặc biệt không bị cắt hoặc biến đổi. */
    public NoteSnapshot entersLongUnicodeNote() {
        openForm();
        String note = "Xuất nhân sự — ghi chú Unicode: tiếng Việt, 中文, !@#$%^&*()_+-=[]{};:,.? ".repeat(80);
        WebElement input = dialog().findElement(By.cssSelector("input[aria-label='Ghi chú']"));
        setReactInputValue(input, note);
        return new NoteSnapshot(note, noteInput().getAttribute("value"));
    }

    /** Nhập số có dấu phân tách hàng nghìn và đọc tổng số lượng đã chuẩn hóa. */
    public FormattedQuantitySnapshot entersThousandsSeparatedQuantity() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1,111");
        WebElement input = rowForCode(lot.code()).findElement(
                By.cssSelector("input[aria-label='Số lượng xuất']"));
        String error = rowForCode(lot.code()).findElements(
                        By.cssSelector("[data-slot='error-message']"))
                .stream().map(this::elementText).findFirst().orElse("");
        return new FormattedQuantitySnapshot(lot.code(), input.getAttribute("value"),
                totalQuantity(), error);
    }

    /** Tìm nhanh hai lô còn hàng cùng sản phẩm, rồi gỡ cả sản phẩm để xác nhận toàn bộ lô bị xóa. */
    public ProductRemovalSnapshot removesProductAndAllItsLots() {
        openForm();
        LotPair pair = findAvailableSameProductPair();
        selectLotByExactCode(pair.firstCode());
        WebElement productCard = productCardForCode(pair.firstCode());
        String productName = productName(productCard);
        selectLotByExactCode(pair.secondCode());

        productCard = productCardForCode(pair.firstCode());
        int lotsBeforeRemoval = lotCount(productCard);
        click(productCard.findElement(By.xpath(".//button[normalize-space()='Gỡ sản phẩm']")),
                "Gỡ toàn bộ lô của sản phẩm " + productName);
        boolean removed = wait.until(d -> productCards().stream()
                .noneMatch(card -> productName(card).equals(productName)));
        return new ProductRemovalSnapshot(productName, lotsBeforeRemoval, removed,
                selectedCount(), totalQuantity(), confirmButton().isEnabled());
    }

    /** Tìm lô bằng mã hoặc tên sản phẩm để quan sát danh sách gợi ý. */
    public SearchSnapshot searchLots(String keyword) {
        openForm();
        fill(lotCombo(), keyword, "Tìm lô xuất theo " + keyword);
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty());
        List<String> options = visibleElements(LOT_OPTIONS).stream().map(this::elementText).toList();
        pause("Quan sát kết quả tìm lô xuất theo " + keyword);
        return new SearchSnapshot(keyword, options);
    }

    /** Thêm một lô còn hàng, nhập số lượng 1 và đọc lại tổng trên form. */
    public QuantitySnapshot addAvailableLotAndSetOne() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        return quantitySnapshot(lot);
    }

    /** Thêm rồi xóa lô để form trở về trạng thái chưa thể xác nhận. */
    public RemovalSnapshot addAndRemoveLot() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        WebElement remove = rowForCode(lot.code()).findElement(By.cssSelector("button[title='Xoá lô này']"));
        click(remove, "Xóa lô " + lot.code() + " khỏi phiếu xuất");
        wait.until(d -> rowsForCode(lot.code()).isEmpty());
        pause("Quan sát form sau khi xóa lô đã chọn");
        return new RemovalSnapshot(lot.code(), selectedCount(), totalQuantity(), confirmButton().isEnabled());
    }

    /** Nhập 0 để kiểm tra lô chưa hợp lệ không thể xuất. */
    public QuantitySnapshot setZeroQuantity() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "0");
        return quantitySnapshot(lot);
    }

    /** Nhập số lượng lớn hơn tồn hiện có để kiểm tra chặn xuất vượt tồn. */
    public QuantitySnapshot setQuantityAboveStock() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), Integer.toString(lot.stock() + 1));
        return quantitySnapshot(lot);
    }

    /** Để trống số lượng của lô đã chọn phải khóa cả xuất lẫn thao tác thêm lô. */
    public ValidationStateSnapshot blankQuantityLocksForm() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        return validationState(lot.code());
    }

    /** Nhập dữ liệu không phải số nguyên hợp lệ và đọc lại trạng thái mà form chấp nhận. */
    public ValidationStateSnapshot entersInvalidQuantity(String value) {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), value);
        return validationState(lot.code());
    }

    /** Một dòng lỗi trong phiếu nhiều lô phải khóa toàn bộ phiếu và combobox Thêm lô. */
    public MultiLotValidationSnapshot oneInvalidLotLocksWholeReceipt() {
        openForm();
        LotPair pair = findAvailableSameProductPair();
        selectLotByExactCode(pair.firstCode());
        selectLotByExactCode(pair.secondCode());
        setQuantity(pair.firstCode(), "1");
        setQuantity(pair.secondCode(), "0");
        return new MultiLotValidationSnapshot(pair.firstCode(), pair.secondCode(),
                rowQuantity(pair.firstCode()), rowQuantity(pair.secondCode()),
                selectedCount(), totalQuantity(), confirmButton().isEnabled(), lotCombo().isEnabled());
    }

    /** Sau khi sửa dòng lỗi về số lượng hợp lệ, toàn bộ phiếu phải được mở khóa lại. */
    public MultiLotValidationSnapshot correctingInvalidLotUnlocksWholeReceipt() {
        openForm();
        LotPair pair = findAvailableSameProductPair();
        selectLotByExactCode(pair.firstCode());
        selectLotByExactCode(pair.secondCode());
        setQuantity(pair.firstCode(), "1");
        setQuantity(pair.secondCode(), "0");
        setQuantity(pair.secondCode(), "1");
        return new MultiLotValidationSnapshot(pair.firstCode(), pair.secondCode(),
                rowQuantity(pair.firstCode()), rowQuantity(pair.secondCode()),
                selectedCount(), totalQuantity(), confirmButton().isEnabled(), lotCombo().isEnabled());
    }

    /** Xóa ngày xuất bắt buộc sau khi đã có dữ liệu hợp lệ phải chặn xác nhận. */
    public ValidationStateSnapshot blankExportDateLocksForm() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        WebElement dateInput = exportDateInput();
        dateInput.click();
        dateInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        settle(300);
        return validationState(lot.code());
    }

    /** Cuộn danh sách gợi ý lô xuống cuối rồi quay lại đầu để người chạy quan sát. */
    public ScrollSnapshot scrollLotSuggestionsDownAndBack() {
        openForm();
        fill(lotCombo(), "VT", "Mở danh sách lô để kiểm tra vùng cuộn");
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty());
        int optionCount = visibleElements(LOT_OPTIONS).size();
        WebElement listbox = visible(By.cssSelector("[role='listbox']"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement scrollBox = (WebElement) js.executeScript(
                "let e=arguments[0]; while(e){const s=getComputedStyle(e);"
                        + "if((s.overflowY==='auto'||s.overflowY==='scroll')&&e.scrollHeight>e.clientHeight)return e;"
                        + "e=e.parentElement;} return arguments[0];", listbox);
        js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollBox);
        highlight(scrollBox);
        pause("Cuộn xuống cuối danh sách lô gợi ý");
        boolean lastVisible = ((Number) js.executeScript("return arguments[0].scrollTop;", scrollBox)).longValue() > 0;
        js.executeScript("arguments[0].scrollTop = 0;", scrollBox);
        highlight(scrollBox);
        pause("Cuộn trở lại đầu danh sách lô gợi ý");
        boolean firstVisible = ((Number) js.executeScript("return arguments[0].scrollTop;", scrollBox)).longValue() == 0;
        return new ScrollSnapshot(optionCount, lastVisible, firstVisible);
    }

    /** Lập phiếu xuất thật với một lô còn hàng và ghi chú nhận diện. */
    public SubmissionSnapshot submitOneAvailableLot() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        String note = "Xuất nhân sự automation " + System.currentTimeMillis();
        fill(dialog().findElement(By.cssSelector("input[aria-label='Ghi chú']")), note,
                "Nhập ghi chú nhận diện phiếu xuất");
        WebElement submit = confirmButton();
        boolean enabledBeforeSubmit = submit.isEnabled();
        click(submit, "Xác nhận xuất một lô cho nhân sự");
        boolean closed = wait.until(d -> !dialogVisible());
        int stockAfterExport = new SalesStockPage(driver).salesGridRows().stream()
                .filter(row -> row.code().equals(lot.code()))
                .map(SalesStockPage.SalesGridRow::stock)
                .findFirst().orElse(-1);
        SalesStockReceiptPage.ReceiptRow createdReceipt = new SalesStockReceiptPage(driver)
                .filter("Xuất nhân sự").rows().stream()
                .filter(row -> row.lotCodes().contains(lot.code()))
                .findFirst().orElse(SalesStockReceiptPage.ReceiptRow.empty());
        pause("Quan sát phiếu xuất nhân sự vừa tạo và tồn kho lô sau khi xuất");
        return new SubmissionSnapshot(lot.code(), note, lot.stock(), stockAfterExport,
                enabledBeforeSubmit, closed, createdReceipt.code(), createdReceipt.normalizedType(),
                createdReceipt.lotCodes(), createdReceipt.quantities(), createdReceipt.text());
    }

    /** Xuất hai lô cùng sản phẩm trong một phiếu và đối chiếu tồn kho cùng phiếu vừa tạo. */
    public MultiSubmissionSnapshot submitTwoLotsInOneReceipt() {
        Set<String> receiptCodesBefore = new HashSet<>(new SalesStockReceiptPage(driver)
                .filter("Xuất nhân sự").rows().stream()
                .map(SalesStockReceiptPage.ReceiptRow::code).toList());
        openForm();
        LotPair pair = findAvailableSameProductPair();
        selectLotByExactCode(pair.firstCode());
        selectLotByExactCode(pair.secondCode());
        int firstStockBefore = stockInSelectedRow(pair.firstCode());
        int secondStockBefore = stockInSelectedRow(pair.secondCode());
        setQuantity(pair.firstCode(), "1");
        setQuantity(pair.secondCode(), "1");
        String note = "Xuất nhiều lô automation " + System.currentTimeMillis();
        fill(noteInput(), note, "Nhập ghi chú nhận diện phiếu xuất nhiều lô");
        boolean enabledBeforeSubmit = confirmButton().isEnabled();
        click(confirmButton(), "Xác nhận xuất hai lô cho nhân sự");
        boolean dialogClosed = wait.until(d -> !dialogVisible());

        List<SalesStockPage.SalesGridRow> stockAfterExport = new SalesStockPage(driver).salesGridRows();
        int firstStockAfter = stockAfterExport.stream().filter(row -> row.code().equals(pair.firstCode()))
                .map(SalesStockPage.SalesGridRow::stock).findFirst().orElse(-1);
        int secondStockAfter = stockAfterExport.stream().filter(row -> row.code().equals(pair.secondCode()))
                .map(SalesStockPage.SalesGridRow::stock).findFirst().orElse(-1);
        SalesStockReceiptPage.ReceiptRow createdReceipt = new SalesStockReceiptPage(driver)
                .filter("Xuất nhân sự").rows().stream()
                .filter(row -> !receiptCodesBefore.contains(row.code()))
                .filter(row -> row.lotCodes().containsAll(List.of(pair.firstCode(), pair.secondCode())))
                .findFirst().orElse(SalesStockReceiptPage.ReceiptRow.empty());
        return new MultiSubmissionSnapshot(pair.firstCode(), firstStockBefore, firstStockAfter,
                pair.secondCode(), secondStockBefore, secondStockAfter, enabledBeforeSubmit,
                dialogClosed, createdReceipt.code(), createdReceipt.normalizedType(),
                createdReceipt.lotCodes(), createdReceipt.quantities(), note, createdReceipt.text());
    }

    /** Hủy form sau khi đã nhập dữ liệu và xác nhận tồn của lô không thay đổi. */
    public CancellationSnapshot cancelsPreparedExportWithoutChangingStock() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        click(dialogButton("Hủy"), "Hủy phiếu xuất đã nhập dữ liệu");
        boolean dialogClosed = wait.until(d -> !dialogVisible());
        int stockAfterCancel = new SalesStockPage(driver).salesGridRows().stream()
                .filter(row -> row.code().equals(lot.code()))
                .map(SalesStockPage.SalesGridRow::stock)
                .findFirst().orElse(-1);
        return new CancellationSnapshot(lot.code(), lot.stock(), stockAfterCancel, dialogClosed);
    }

    /** Xuất đúng toàn bộ tồn của một lô và xác nhận tồn sau submit bằng 0. */
    public ExactStockSubmissionSnapshot submitsExactAvailableStock() {
        Set<String> receiptCodesBefore = new HashSet<>(new SalesStockReceiptPage(driver)
                .filter("Xuất nhân sự").rows().stream()
                .map(SalesStockReceiptPage.ReceiptRow::code).toList());
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), Integer.toString(lot.stock()));
        boolean enabledBeforeSubmit = confirmButton().isEnabled();
        click(confirmButton(), "Xuất đúng toàn bộ tồn của lô " + lot.code());
        boolean dialogClosed = wait.until(d -> !dialogVisible());
        int stockAfterExport = new SalesStockPage(driver).salesGridRows().stream()
                .filter(row -> row.code().equals(lot.code()))
                .map(SalesStockPage.SalesGridRow::stock).findFirst().orElse(-1);
        SalesStockReceiptPage.ReceiptRow createdReceipt = new SalesStockReceiptPage(driver)
                .filter("Xuất nhân sự").rows().stream()
                .filter(row -> !receiptCodesBefore.contains(row.code()))
                .filter(row -> row.lotCodes().contains(lot.code()))
                .findFirst().orElse(SalesStockReceiptPage.ReceiptRow.empty());
        return new ExactStockSubmissionSnapshot(lot.code(), lot.stock(), stockAfterExport,
                enabledBeforeSubmit, dialogClosed, createdReceipt.code(), createdReceipt.quantities());
    }

    /** Đóng bằng nút X sau khi nhập dữ liệu và đối chiếu tồn lô không đổi. */
    public CancellationSnapshot closesPreparedExportWithoutChangingStock() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        click(dialog().findElement(By.cssSelector("button[aria-label='Close']")),
                "Đóng form xuất đã nhập bằng nút X");
        boolean dialogClosed = wait.until(d -> !dialogVisible());
        int stockAfterClose = new SalesStockPage(driver).salesGridRows().stream()
                .filter(row -> row.code().equals(lot.code()))
                .map(SalesStockPage.SalesGridRow::stock)
                .findFirst().orElse(-1);
        return new CancellationSnapshot(lot.code(), lot.stock(), stockAfterClose, dialogClosed);
    }

    private LotSnapshot addAvailableLot() {
        fill(lotCombo(), "VT", "Tìm lô còn hàng để xuất cho nhân sự");
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty());
        WebElement option = visibleElements(LOT_OPTIONS).stream()
                .filter(item -> number(STOCK, elementText(item)) > 0)
                .findFirst().orElseThrow(() -> new IllegalStateException("Không có lô còn hàng để xuất."));
        String code = match(LOT_CODE, elementText(option));
        int stock = number(STOCK, elementText(option));
        highlight(option);
        pause("Chọn lô " + code + " còn " + stock + " cái");
        wait.until(d -> visibleElements(LOT_OPTIONS).stream()
                .filter(item -> elementText(item).contains(code))
                .findFirst().map(item -> {
                    try { item.click(); return true; } catch (RuntimeException ignored) { return false; }
                }).orElse(false));
        wait.until(d -> !rowsForCode(code).isEmpty());
        closeLotSuggestions();
        WebElement row = rowForCode(code);
        highlight(row);
        pause("Quan sát lô " + code + " đã được thêm vào phiếu xuất");
        return new LotSnapshot(code, stock, elementText(row));
    }

    private void setQuantity(String code, String quantity) {
        WebElement input = rowForCode(code).findElement(By.cssSelector("input[aria-label='Số lượng xuất']"));
        highlight(input);
        pause("Nhập số lượng xuất " + quantity + " cho lô " + code);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        input.sendKeys(quantity);
        settle(300);
        pause("Quan sát tổng số lượng và trạng thái xác nhận sau khi nhập");
    }

    private int stockInSelectedRow(String code) {
        return number(STOCK, elementText(rowForCode(code)));
    }

    private ValidationStateSnapshot validationState(String code) {
        return new ValidationStateSnapshot(code, rowQuantity(code), selectedCount(), totalQuantity(),
                confirmButton().isEnabled(), lotCombo().isEnabled(), exportDateInput().getAttribute("value"));
    }

    private String rowQuantity(String code) {
        return rowForCode(code).findElement(By.cssSelector("input[aria-label='Số lượng xuất']"))
                .getAttribute("value");
    }

    private LotPair findAvailableSameProductPair() {
        fill(lotCombo(), "VT", "Mở danh sách lô còn hàng để tìm nhanh hai lô cùng sản phẩm");
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty());
        WebElement listbox = visible(By.cssSelector("[role='listbox']"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement scrollBox = (WebElement) js.executeScript(
                "let e=arguments[0]; while(e){const s=getComputedStyle(e);"
                        + "if((s.overflowY==='auto'||s.overflowY==='scroll')&&e.scrollHeight>e.clientHeight)return e;"
                        + "e=e.parentElement;} return arguments[0];", listbox);
        js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", scrollBox);
        List<LotOption> options = visibleElements(LOT_OPTIONS).stream()
                .filter(option -> number(STOCK, elementText(option)) > 0)
                .map(option -> new LotOption(match(LOT_CODE, elementText(option)), productNameFromOption(elementText(option))))
                .filter(option -> !option.code().isBlank() && !option.productName().isBlank())
                .toList();
        for (int i = 0; i < options.size(); i++) {
            for (int j = i + 1; j < options.size(); j++) {
                if (options.get(i).productName().equals(options.get(j).productName())) {
                    closeLotSuggestions();
                    return new LotPair(options.get(i).code(), options.get(j).code());
                }
            }
        }
        throw new IllegalStateException("Không có hai lô còn hàng cùng sản phẩm để kiểm tra.");
    }

    private static String productNameFromOption(String optionText) {
        return optionText.replaceFirst("^\\s*VT\\d+\\s*", "")
                .replaceFirst("(?i)\\s*tồn\\s*\\d+.*$", "")
                .split("\\s*·\\s*", 2)[0].trim();
    }

    private void setExportDate(LocalDate targetDate) {
        String value = targetDate.toString();
        setReactInputValue(exportDateInput(), value);
        wait.until(d -> value.equals(exportDateInput().getAttribute("value")));
    }

    /** Gán giá trị một lần và phát event native để React nhận thay đổi, không gửi từng phím. */
    private void setReactInputValue(WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "const input=arguments[0], value=arguments[1];"
                        + "const setter=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;"
                        + "setter.call(input,value);"
                        + "input.dispatchEvent(new Event('input',{bubbles:true}));"
                        + "input.dispatchEvent(new Event('change',{bubbles:true}));",
                input, value);
        settle(300);
    }

    private QuantitySnapshot quantitySnapshot(LotSnapshot lot) {
        WebElement row = rowForCode(lot.code());
        String value = row.findElement(By.cssSelector("input[aria-label='Số lượng xuất']")).getAttribute("value");
        return new QuantitySnapshot(lot.code(), lot.stock(), value, selectedCount(), totalQuantity(),
                confirmButton().isEnabled(), elementText(row), elementText(dialog()));
    }

    private List<WebElement> rowsForCode(String code) {
        return dialog().findElements(By.xpath(".//*[normalize-space()=" + xpathLiteral(code)
                + "]/ancestor::div[contains(@class,'grid')][1]"));
    }

    private WebElement productCardForCode(String code) {
        return rowForCode(code).findElement(
                By.xpath("ancestor::div[contains(@class,'rounded-2xl')][1]"));
    }

    private List<WebElement> productCards() {
        return dialog().findElements(By.xpath(
                ".//div[contains(@class,'rounded-2xl')][.//button[normalize-space()='Gỡ sản phẩm']]"))
                .stream().filter(WebElement::isDisplayed).toList();
    }

    private int lotCount(WebElement productCard) {
        return productCard.findElements(By.cssSelector("input[aria-label='Số lượng xuất']")).size();
    }

    private void selectLotByExactCode(String code) {
        fill(lotCombo(), code, "Tìm trực tiếp lô " + code);
        wait.until(d -> visibleElements(LOT_OPTIONS).stream()
                .anyMatch(option -> elementText(option).contains(code)));
        clickFreshOption(code, "Chọn lô " + code);
        wait.until(d -> !rowsForCode(code).isEmpty());
    }

    /** Chọn lại option theo mã vừa tìm được để tránh element cũ sau khi React render danh sách. */
    private void clickFreshOption(String code, String step) {
        wait.until(d -> visibleElements(LOT_OPTIONS).stream()
                .filter(option -> elementText(option).contains(code))
                .findFirst()
                .map(option -> {
                    highlight(option);
                    pause(step);
                    try {
                        option.click();
                        return true;
                    } catch (RuntimeException ignored) {
                        return false;
                    }
                }).orElse(false));
        closeLotSuggestions();
    }

    /** Đóng danh sách gợi ý sau khi đã chọn để card sản phẩm vừa thêm hiển thị rõ ràng. */
    private void closeLotSuggestions() {
        WebElement input = lotCombo();
        if ("true".equals(input.getAttribute("aria-expanded"))) {
            input.sendKeys(Keys.ESCAPE);
            wait.until(d -> "false".equals(lotCombo().getAttribute("aria-expanded")));
        }
        pause("Đóng danh sách gợi ý và quan sát sản phẩm/lô vừa chọn");
    }

    private String productName(WebElement productCard) {
        String header = elementText(productCard.findElement(By.xpath("./div[1]//p[1]")));
        return header.replaceFirst("\\s*\\(\\d+\\s*lô\\)\\s*$", "").trim();
    }

    private WebElement rowForCode(String code) {
        return rowsForCode(code).stream().filter(WebElement::isDisplayed).findFirst().orElseThrow();
    }

    private int selectedCount() { return number(SELECTED, elementText(dialog())); }
    private int totalQuantity() { return number(TOTAL, elementText(dialog())); }
    private WebElement exportDateInput() { return dialog().findElement(By.cssSelector("input[aria-label='Ngày xuất']")); }
    private WebElement noteInput() { return dialog().findElement(By.cssSelector("input[aria-label='Ghi chú']")); }
    private WebElement lotCombo() { return dialog().findElement(By.cssSelector("input[aria-label='Thêm lô']")); }
    private WebElement confirmButton() { return dialog().findElement(By.xpath(".//button[normalize-space()='Xuất cho nhân sự']")); }
    private WebElement dialogButton(String text) { return dialog().findElement(By.xpath(".//button[normalize-space()=" + xpathLiteral(text) + "]")); }
    private WebElement dialog() { return visible(DIALOG); }
    private boolean dialogVisible() { return driver.findElements(DIALOG).stream().anyMatch(WebElement::isDisplayed); }
    private boolean isSelected(WebElement element) {
        String classes = element.getAttribute("class");
        return "true".equals(element.getAttribute("aria-selected"))
                || (classes != null && (classes.contains("bg-primary") || classes.contains("text-white")));
    }
    private static String match(Pattern pattern, String text) { Matcher matcher = pattern.matcher(text); return matcher.find() ? matcher.group() : ""; }
    private static int number(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1).replaceAll("[^\\d]", "")) : -1;
    }

    public record FormSnapshot(String date, boolean dateRequired, String note, boolean lotCombobox, int selectedLots,
                               int totalQuantity, boolean submitEnabled, String text) { }
    public record DateRangeSnapshot(String expectedPast, String actualPast,
                                    String expectedFuture, String actualFuture) { }
    public record NoteSnapshot(String expected, String actual) { }
    public record FormattedQuantitySnapshot(String code, String quantity,
                                            int totalQuantity, String error) { }
    public record ProductRemovalSnapshot(String productName, int lotsBeforeRemoval, boolean productRemoved,
                                         int selectedLots,
                                         int totalQuantity, boolean submitEnabled) { }
    public record SearchSnapshot(String keyword, List<String> options) { }
    public record LotSnapshot(String code, int stock, String rowText) { }
    public record QuantitySnapshot(String code, int stock, String quantity, int selectedLots,
                                   int totalQuantity, boolean submitEnabled, String rowText, String formText) { }
    public record RemovalSnapshot(String code, int selectedLots, int totalQuantity, boolean submitEnabled) { }
    public record ScrollSnapshot(int optionCount, boolean reachedLast, boolean returnedFirst) { }
    public record SubmissionSnapshot(String code, String note, int stockBeforeExport, int stockAfterExport,
                                     boolean enabledBeforeSubmit, boolean dialogClosed, String receiptCode,
                                     String receiptType, List<String> receiptLotCodes,
                                     List<Integer> receiptQuantities, String receiptText) { }
    public record MultiSubmissionSnapshot(String firstCode, int firstStockBefore, int firstStockAfter,
                                          String secondCode, int secondStockBefore, int secondStockAfter,
                                          boolean enabledBeforeSubmit, boolean dialogClosed, String receiptCode,
                                          String receiptType, List<String> receiptLotCodes,
                                          List<Integer> receiptQuantities, String note, String receiptText) { }
    public record CancellationSnapshot(String code, int stockBeforeCancel, int stockAfterCancel,
                                       boolean dialogClosed) { }
    public record ValidationStateSnapshot(String code, String quantity, int selectedLots,
                                          int totalQuantity, boolean submitEnabled,
                                          boolean lotComboboxEnabled, String exportDate) { }
    public record MultiLotValidationSnapshot(String validCode, String invalidCode,
                                             String validQuantity, String invalidQuantity,
                                             int selectedLots, int totalQuantity,
                                             boolean submitEnabled, boolean lotComboboxEnabled) { }
    public record ExactStockSubmissionSnapshot(String code, int quantity, int stockAfterExport,
                                               boolean enabledBeforeSubmit, boolean dialogClosed,
                                               String receiptCode, List<Integer> receiptQuantities) { }
    private record LotOption(String code, String productName) { }
    private record LotPair(String firstCode, String secondCode) { }
}
