package com.vuatho.pages;

import com.vuatho.utils.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.interactions.Actions;

import java.time.LocalDate;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page Object cho Kho bán hàng → Phiếu → Nhập hàng từ Kho tổng. */
public final class SalesStockImportPage extends UniformUiPage {
    private static final String ROUTE = "/vuatho/inventory-uniform?tab=sub";
    private static final By DIALOG = By.xpath("//section[@role='dialog'][.//*[normalize-space()='Nhập hàng']]");
    private static final By LOT_OPTIONS = By.cssSelector("[role='option']");
    private static final Pattern LOT_CODE = Pattern.compile("\\b(?:VT\\d+|AT[A-Z0-9]+)\\b");
    private static final Pattern STOCK = Pattern.compile("(?i)tồn(?:\\s+kho\\s+tổng)?\\s*([\\d,]+)\\s*(?:cái)?");
    private static final Pattern SELECTED = Pattern.compile("Lô được chọn:\\s*(\\d+)\\s*/\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL = Pattern.compile("Tổng SL:\\s*([\\d,]+)", Pattern.CASE_INSENSITIVE);

    public SalesStockImportPage(WebDriver driver) {
        super(driver);
    }

    public SalesStockImportPage openForm() {
        if (dialogVisible()) {
            click(dialogButton("Hủy"), "Hủy form nhập còn mở từ testcase trước");
            wait.until(d -> !dialogVisible());
        }
        boolean alreadyOnSalesWarehouse = driver.getCurrentUrl().contains("/vuatho/inventory-uniform")
                && driver.getCurrentUrl().contains("tab=sub")
                && !visibleElements(By.tagName("main")).isEmpty();
        if (!alreadyOnSalesWarehouse) {
            openRoute(ROUTE);
        }
        WebElement receipts = visible(By.xpath("//main//button[normalize-space()='Phiếu']"));
        if (!isSelected(receipts)) {
            click(receipts, "Chọn tab Phiếu của Kho bán hàng");
            waitForResult();
        }
        click(visible(By.xpath("//main//button[normalize-space()='Nhập hàng']")),
                "Mở form Nhập hàng từ Kho tổng");
        visible(DIALOG);
        pause("Quan sát form Nhập hàng");
        return this;
    }

    public FormSnapshot formSnapshot() {
        openForm();
        WebElement form = dialog();
        WebElement date = dateInput();
        return new FormSnapshot(date.getAttribute("value"),
                "true".equals(date.getAttribute("aria-required")), noteInput().getAttribute("value"),
                form.findElements(By.cssSelector("input[aria-label='Thêm lô']")).size() == 1,
                selectedCount(), totalQuantity(), confirmButton().isEnabled(), elementText(form));
    }

    public CloseSnapshot cancelEmptyForm() {
        openForm();
        click(dialogButton("Hủy"), "Hủy form Nhập hàng");
        return new CloseSnapshot(wait.until(d -> !dialogVisible()));
    }

    public CloseSnapshot closeEmptyForm() {
        openForm();
        click(dialog().findElement(By.cssSelector("button[aria-label='Close']")), "Đóng form Nhập hàng bằng dấu X");
        return new CloseSnapshot(wait.until(d -> !dialogVisible()));
    }

    public SearchSnapshot searchLots(String keyword) {
        openForm();
        fill(lotCombo(), keyword, "Tìm lô nhập theo " + keyword);
        settle(900);
        List<String> options = visibleElements(LOT_OPTIONS).stream().map(this::elementText).toList();
        return new SearchSnapshot(keyword, options,
                elementText(driver.findElement(By.tagName("body"))));
    }

    public SelectionSnapshot selectAvailableLot() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        return selectionSnapshot(lot);
    }

    public QuantitySnapshot selectLotAndSetOne() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        return quantitySnapshot(lot);
    }

    public DuplicateSnapshot selectedLotIsExcludedFromSuggestions() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        fill(lotCombo(), lot.code(), "Tìm lại lô vừa chọn " + lot.code());
        settle(700);
        boolean duplicated = visibleElements(LOT_OPTIONS).stream()
                .anyMatch(option -> exactCode(elementText(option)).equals(lot.code()));
        return new DuplicateSnapshot(lot.code(), duplicated);
    }

    public MultiSelectionSnapshot selectsTwoLotsInOneDropdownSession() {
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        return new MultiSelectionSnapshot(pair.firstCode(), pair.secondCode(),
                selectedCount(), productLotCount(pair.firstCode()),
                "false".equals(lotCombo().getAttribute("aria-expanded")));
    }

    public RemovalSnapshot addAndRemoveLot() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        click(rowForCode(lot.code()).findElement(By.cssSelector("button[title='Xoá lô này']")),
                "Xóa lô " + lot.code() + " khỏi phiếu nhập");
        wait.until(d -> rowsForCode(lot.code()).isEmpty());
        return new RemovalSnapshot(lot.code(), selectedCount(), totalQuantity(), confirmButton().isEnabled());
    }

    public ProductRemovalSnapshot removesProductAndAllLots() {
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        WebElement card = productCardForCode(pair.firstCode());
        int before = productLotCount(pair.firstCode());
        click(card.findElement(By.xpath(".//button[normalize-space()='Gỡ sản phẩm']")),
                "Gỡ sản phẩm có nhiều lô khỏi phiếu nhập");
        boolean removed = wait.until(d -> rowsForCode(pair.firstCode()).isEmpty()
                && rowsForCode(pair.secondCode()).isEmpty());
        return new ProductRemovalSnapshot(before, removed, selectedCount(), totalQuantity(), confirmButton().isEnabled());
    }

    public ScrollSnapshot scrollSuggestionsDownAndBack() {
        openForm();
        fill(lotCombo(), "VT", "Mở danh sách lô nhập để kiểm tra cuộn");
        wait.until(d -> visibleElements(LOT_OPTIONS).size() > 1);
        WebElement listbox = visible(By.cssSelector("[role='listbox']"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement scrollBox = (WebElement) js.executeScript(
                "let e=arguments[0];while(e){const s=getComputedStyle(e);if((s.overflowY==='auto'||s.overflowY==='scroll')&&e.scrollHeight>e.clientHeight)return e;e=e.parentElement;}return arguments[0];",
                listbox);
        js.executeScript("arguments[0].scrollTop=arguments[0].scrollHeight;", scrollBox);
        pause("Cuộn xuống cuối danh sách lô nhập");
        long bottom = ((Number) js.executeScript("return arguments[0].scrollTop;", scrollBox)).longValue();
        js.executeScript("arguments[0].scrollTop=0;", scrollBox);
        pause("Cuộn trở lại đầu danh sách lô nhập");
        long top = ((Number) js.executeScript("return arguments[0].scrollTop;", scrollBox)).longValue();
        return new ScrollSnapshot(visibleElements(LOT_OPTIONS).size(), bottom > 0, top == 0);
    }

    public NoteSnapshot entersLongUnicodeNote() {
        openForm();
        String note = "Nhập hàng — tiếng Việt, 中文, !@#$%^&*()_+-=[]{};:,.? ".repeat(80);
        setReactInputValue(noteInput(), note);
        return new NoteSnapshot(note, noteInput().getAttribute("value"), noteInput().getAttribute("maxlength"));
    }

    public SearchJourneySnapshot clearingKeywordRestoresSuggestions() {
        openForm();
        fill(lotCombo(), "VT20", "Tìm chính xác VT20 trước khi xóa từ khóa");
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty());
        int filteredCount = visibleElements(LOT_OPTIONS).size();
        lotCombo().sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        wait.until(d -> visibleElements(LOT_OPTIONS).size() > filteredCount);
        return new SearchJourneySnapshot(filteredCount, visibleElements(LOT_OPTIONS).size(),
                lotCombo().getAttribute("value"), visibleElements(LOT_OPTIONS).stream().map(this::elementText).toList());
    }

    public KeywordReplacementSnapshot replacingKeywordRefreshesResults() {
        openForm();
        fill(lotCombo(), "VT20", "Tìm lô theo từ khóa thứ nhất");
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty());
        List<String> first = visibleElements(LOT_OPTIONS).stream().map(this::elementText).toList();
        fill(lotCombo(), "VT01", "Thay bằng từ khóa thứ hai");
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty()
                && visibleElements(LOT_OPTIONS).stream().allMatch(option -> elementText(option).contains("VT01")));
        List<String> second = visibleElements(LOT_OPTIONS).stream().map(this::elementText).toList();
        return new KeywordReplacementSnapshot(first, second, lotCombo().getAttribute("value"));
    }

    public KeyboardSelectionSnapshot selectsLotUsingKeyboard() {
        openForm();
        fill(lotCombo(), "VT", "Mở gợi ý để chọn lô bằng bàn phím");
        wait.until(d -> visibleElements(LOT_OPTIONS).stream().anyMatch(option -> optionStock(option) > 0));
        String expected = visibleElements(LOT_OPTIONS).stream().filter(option -> optionStock(option) > 0)
                .map(option -> exactCode(elementText(option))).findFirst().orElseThrow();
        lotCombo().sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
        wait.until(d -> !rowsForCode(expected).isEmpty());
        closeSuggestions();
        return new KeyboardSelectionSnapshot(expected, !rowsForCode(expected).isEmpty(),
                "false".equals(lotCombo().getAttribute("aria-expanded")), selectedCount(), totalQuantity());
    }

    public DifferentProductsSnapshot selectsLotsFromDifferentProducts() {
        openForm();
        LotPair pair = findDifferentProductPair();
        selectPairWithoutClosingBetween(pair);
        return differentProductsSnapshot(pair);
    }

    public PreserveLotsSnapshot removingOneLotPreservesSibling() {
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        click(rowForCode(pair.firstCode()).findElement(By.cssSelector("button[title='Xoá lô này']")),
                "Xóa một lô nhưng giữ lô cùng sản phẩm còn lại");
        wait.until(d -> rowsForCode(pair.firstCode()).isEmpty());
        return new PreserveLotsSnapshot(pair.firstCode(), pair.secondCode(), rowsForCode(pair.firstCode()).isEmpty(),
                !rowsForCode(pair.secondCode()).isEmpty(), productLotCount(pair.secondCode()));
    }

    public PreserveProductsSnapshot removingProductPreservesOtherProduct() {
        openForm();
        LotPair pair = findDifferentProductPair();
        selectPairWithoutClosingBetween(pair);
        int cardsBefore = productCards().size();
        click(productCardForCode(pair.firstCode()).findElement(By.xpath(".//button[normalize-space()='Gỡ sản phẩm']")),
                "Gỡ một sản phẩm nhưng giữ sản phẩm khác");
        wait.until(d -> rowsForCode(pair.firstCode()).isEmpty());
        return new PreserveProductsSnapshot(cardsBefore, productCards().size(),
                rowsForCode(pair.firstCode()).isEmpty(), !rowsForCode(pair.secondCode()).isEmpty());
    }

    public ResetSnapshot reopeningFormClearsDraft() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        setReactInputValue(noteInput(), "Bản nháp không được giữ lại");
        click(dialogButton("Hủy"), "Hủy bản nháp Nhập hàng");
        wait.until(d -> !dialogVisible());
        openForm();
        return new ResetSnapshot(noteInput().getAttribute("value"), selectedCount(), totalQuantity(),
                dialog().findElements(By.cssSelector("input[aria-label='Số lượng chuyển']")).size(),
                confirmButton().isEnabled());
    }

    public SelectedLotsScrollSnapshot scrollLongSelectedLotList() {
        openForm();
        List<LotOption> candidates = availableOptions(8, 1);
        selectLotsWithoutClosing(candidates.stream().map(LotOption::code).toList());
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement scrollBox = (WebElement) js.executeScript(
                "let e=arguments[0];while(e){const s=getComputedStyle(e);if((s.overflowY==='auto'||s.overflowY==='scroll')&&e.scrollHeight>e.clientHeight)return e;e=e.parentElement;}return arguments[0];",
                rowForCode(candidates.get(candidates.size() - 1).code()));
        js.executeScript("arguments[0].scrollTop=arguments[0].scrollHeight;", scrollBox);
        pause("Cuộn xuống cuối danh sách lô đã chọn");
        long bottom = ((Number) js.executeScript("return arguments[0].scrollTop;", scrollBox)).longValue();
        js.executeScript("arguments[0].scrollTop=0;", scrollBox);
        pause("Cuộn trở lại đầu danh sách lô đã chọn");
        long top = ((Number) js.executeScript("return arguments[0].scrollTop;", scrollBox)).longValue();
        return new SelectedLotsScrollSnapshot(candidates.size(), bottom > 0, top == 0,
                candidates.stream().allMatch(option -> !rowsForCode(option.code()).isEmpty()));
    }

    public MultipleQuantitySnapshot totalsDifferentLotQuantities() {
        openForm();
        List<LotOption> candidates = availableOptions(3, 3);
        selectLotsWithoutClosing(candidates.stream().map(LotOption::code).toList());
        setQuantity(candidates.get(0).code(), "1");
        setQuantity(candidates.get(1).code(), "2");
        setQuantity(candidates.get(2).code(), "3");
        return new MultipleQuantitySnapshot(candidates.stream().map(LotOption::code).toList(),
                List.of(rowQuantity(candidates.get(0).code()), rowQuantity(candidates.get(1).code()),
                        rowQuantity(candidates.get(2).code())), selectedCount(), totalQuantity(), confirmButton().isEnabled());
    }

    public DropdownStateSnapshot closesDropdownWithoutLosingSelection() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        openSuggestions("Mở lại danh sách lô bằng nút mũi tên");
        boolean opened = "true".equals(lotCombo().getAttribute("aria-expanded"));
        closeSuggestions();
        return new DropdownStateSnapshot(lot.code(), opened,
                "false".equals(lotCombo().getAttribute("aria-expanded")), !rowsForCode(lot.code()).isEmpty(),
                selectedCount(), totalQuantity());
    }

    public DateRangeSnapshot acceptsPastAndFutureDates() {
        openForm();
        LocalDate past = LocalDate.now().minusDays(1);
        LocalDate future = LocalDate.now().plusDays(1);
        setDate(past);
        String actualPast = dateInput().getAttribute("value");
        setDate(future);
        return new DateRangeSnapshot(past.toString(), actualPast, future.toString(), dateInput().getAttribute("value"));
    }

    public QuantitySnapshot setQuantity(String value) {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), value);
        return quantitySnapshot(lot);
    }

    public QuantitySnapshot setQuantityAboveStock() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), Integer.toString(lot.stock() + 1));
        return quantitySnapshot(lot);
    }

    public QuantitySnapshot setExactStock() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), Integer.toString(lot.stock()));
        return quantitySnapshot(lot);
    }

    public MultiValidationSnapshot blankAdditionalLotDoesNotBlockValidLot() {
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        setQuantity(pair.firstCode(), "1");
        return multiValidation(pair);
    }

    public MultiValidationSnapshot invalidLotLocksThenCorrectionUnlocks() {
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        setQuantity(pair.firstCode(), "1");
        int secondStock = stockInRow(pair.secondCode());
        setQuantity(pair.secondCode(), Integer.toString(secondStock + 1));
        MultiValidationSnapshot invalid = multiValidation(pair);
        setQuantity(pair.secondCode(), Integer.toString(secondStock));
        MultiValidationSnapshot corrected = multiValidation(pair);
        return corrected.withPrevious(invalid);
    }

    public ValidationSnapshot blankDateLocksSubmission() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        WebElement date = dateInput();
        date.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        settle(400);
        return new ValidationSnapshot(date.getAttribute("value"), confirmButton().isEnabled());
    }

    public RawValidationSnapshot entersOversizedQuantity() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "999999999999999999999999");
        WebElement row = rowForCode(lot.code());
        String error = row.findElements(By.cssSelector("[data-slot='error-message']"))
                .stream().map(this::elementText).findFirst().orElse("");
        return new RawValidationSnapshot(lot.code(), rowQuantity(lot.code()),
                confirmButton().isEnabled(), error, elementText(row));
    }

    public RecoverySnapshot repairsQuantity(String invalidValue, String validValue) {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), invalidValue);
        RawValidationSnapshot invalid = rawValidation(lot.code());
        setQuantity(lot.code(), validValue);
        RawValidationSnapshot corrected = rawValidation(lot.code());
        return new RecoverySnapshot(invalid, corrected, selectedCount(), totalQuantity());
    }

    public RemovedInvalidRowSnapshot removesInvalidRowAndUnlocksReceipt() {
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        setQuantity(pair.firstCode(), "1");
        setQuantity(pair.secondCode(), Integer.toString(stockInRow(pair.secondCode()) + 1));
        boolean disabledBeforeRemoval = !confirmButton().isEnabled();
        click(rowForCode(pair.secondCode()).findElement(By.cssSelector("button[title='Xoá lô này']")),
                "Xóa dòng vượt tồn khỏi phiếu");
        wait.until(d -> rowsForCode(pair.secondCode()).isEmpty());
        return new RemovedInvalidRowSnapshot(pair.firstCode(), pair.secondCode(), disabledBeforeRemoval,
                rowsForCode(pair.secondCode()).isEmpty(), selectedCount(), totalQuantity(), confirmButton().isEnabled());
    }

    public MultiValidationSnapshot allBlankLotsKeepSubmissionLocked() {
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        return multiValidation(pair);
    }

    public MultiValidationSnapshot validLotWithExplicitZeroLot() {
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        setQuantity(pair.firstCode(), "1");
        setQuantity(pair.secondCode(), "0");
        return multiValidation(pair);
    }

    public DateRecoverySnapshot restoresRequiredDate() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        WebElement date = dateInput();
        date.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        settle(300);
        String blankDate = date.getAttribute("value");
        boolean enabledWhenBlank = confirmButton().isEnabled();
        LocalDate restored = LocalDate.now();
        setDate(restored);
        return new DateRecoverySnapshot(blankDate, enabledWhenBlank, restored.toString(),
                dateInput().getAttribute("value"), confirmButton().isEnabled());
    }

    public CombinedValidationSnapshot validDateDoesNotOverrideInvalidQuantity() {
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), Integer.toString(lot.stock() + 1));
        LocalDate date = LocalDate.now().plusDays(1);
        setDate(date);
        return new CombinedValidationSnapshot(date.toString(), dateInput().getAttribute("value"),
                rowQuantity(lot.code()), lot.stock(), confirmButton().isEnabled());
    }

    public MultiExactStockSnapshot multipleExactStocksAreValid() {
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        int firstStock = stockInRow(pair.firstCode());
        int secondStock = stockInRow(pair.secondCode());
        setQuantity(pair.firstCode(), Integer.toString(firstStock));
        setQuantity(pair.secondCode(), Integer.toString(secondStock));
        return new MultiExactStockSnapshot(pair.firstCode(), firstStock, rowQuantity(pair.firstCode()),
                pair.secondCode(), secondStock, rowQuantity(pair.secondCode()), selectedCount(),
                totalQuantity(), confirmButton().isEnabled());
    }

    public BoundaryJourneySnapshot changesAboveExactAndBelowStock() {
        openForm();
        LotSnapshot lot = addAvailableLotAtLeast(2);
        setQuantity(lot.code(), Integer.toString(lot.stock() + 1));
        RawValidationSnapshot above = rawValidation(lot.code());
        setQuantity(lot.code(), Integer.toString(lot.stock()));
        RawValidationSnapshot exact = rawValidation(lot.code());
        setQuantity(lot.code(), Integer.toString(lot.stock() - 1));
        RawValidationSnapshot below = rawValidation(lot.code());
        return new BoundaryJourneySnapshot(lot.stock(), above, exact, below, totalQuantity());
    }

    private RawValidationSnapshot rawValidation(String code) {
        WebElement row = rowForCode(code);
        String error = row.findElements(By.cssSelector("[data-slot='error-message']"))
                .stream().map(this::elementText).findFirst().orElse("");
        return new RawValidationSnapshot(code, rowQuantity(code), confirmButton().isEnabled(), error, elementText(row));
    }

    public SubmissionSnapshot submitOneLot() {
        Map<String, Integer> salesBeforeRows = allSalesStockValues();
        Set<String> receiptsBefore = receiptCodes();
        openForm();
        LotSnapshot lot = addAvailableLot();
        int salesBefore = stockOf(salesBeforeRows, lot.code());
        setQuantity(lot.code(), "1");
        boolean enabled = confirmButton().isEnabled();
        click(confirmButton(), "Nhập thật một lô về Kho bán hàng");
        boolean closed = wait.until(d -> !dialogVisible());
        int salesAfter = salesStock(lot.code());
        int mainAfter = mainStock(lot.code());
        SalesStockReceiptPage.ReceiptRow receipt = newReceipt(receiptsBefore, List.of(lot.code()));
        return new SubmissionSnapshot(lot.code(), lot.stock(), mainAfter, salesBefore, salesAfter,
                enabled, closed, receipt);
    }

    public MultiSubmissionSnapshot submitTwoSameProductLots() {
        Map<String, Integer> salesBeforeRows = allSalesStockValues();
        Set<String> receiptsBefore = receiptCodes();
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        int firstMain = stockInRow(pair.firstCode());
        int secondMain = stockInRow(pair.secondCode());
        int firstSales = stockOf(salesBeforeRows, pair.firstCode());
        int secondSales = stockOf(salesBeforeRows, pair.secondCode());
        setQuantity(pair.firstCode(), "1");
        setQuantity(pair.secondCode(), "1");
        boolean enabled = confirmButton().isEnabled();
        click(confirmButton(), "Nhập thật hai lô cùng sản phẩm về Kho bán hàng");
        boolean closed = wait.until(d -> !dialogVisible());
        Map<String, Integer> salesAfter = salesStockValues(List.of(pair.firstCode(), pair.secondCode()));
        Map<String, Integer> mainAfter = mainStockValues(List.of(pair.firstCode(), pair.secondCode()));
        int firstSalesAfter = stockOf(salesAfter, pair.firstCode());
        int secondSalesAfter = stockOf(salesAfter, pair.secondCode());
        int firstMainAfter = stockOf(mainAfter, pair.firstCode());
        int secondMainAfter = stockOf(mainAfter, pair.secondCode());
        SalesStockReceiptPage.ReceiptRow receipt = newReceipt(receiptsBefore,
                List.of(pair.firstCode(), pair.secondCode()));
        return new MultiSubmissionSnapshot(pair.firstCode(), firstMain, firstMainAfter, firstSales, firstSalesAfter,
                pair.secondCode(), secondMain, secondMainAfter, secondSales, secondSalesAfter, enabled, closed, receipt);
    }

    public CancellationSnapshot cancelPreparedImport(boolean closeByX) {
        Map<String, Integer> salesBeforeRows = allSalesStockValues();
        openForm();
        LotSnapshot lot = addAvailableLot();
        int salesBefore = stockOf(salesBeforeRows, lot.code());
        setQuantity(lot.code(), "1");
        WebElement closer = closeByX
                ? dialog().findElement(By.cssSelector("button[aria-label='Close']")) : dialogButton("Hủy");
        click(closer, closeByX ? "Đóng phiếu nhập đã chuẩn bị bằng X" : "Hủy phiếu nhập đã chuẩn bị");
        boolean closed = wait.until(d -> !dialogVisible());
        int salesAfter = salesStock(lot.code());
        int mainAfter = mainStock(lot.code());
        return new CancellationSnapshot(lot.code(), lot.stock(), mainAfter, salesBefore, salesAfter, closed);
    }

    public TransferSnapshot submitQuantityGreaterThanOne() {
        return submitSingleQuantity(2, false);
    }

    public TransferSnapshot submitExactMainStock() {
        return submitSingleQuantity(0, true);
    }

    public VariableSubmissionSnapshot submitLotsWithDifferentQuantities() {
        Map<String, Integer> salesBeforeRows = allSalesStockValues();
        Set<String> receiptsBefore = receiptCodes();
        openForm();
        List<LotOption> lots = availableOptions(2, 2);
        selectLotsWithoutClosing(lots.stream().map(LotOption::code).toList());
        List<Integer> mainBefore = lots.stream().map(lot -> stockInRow(lot.code())).toList();
        List<Integer> salesBefore = lots.stream().map(lot -> stockOf(salesBeforeRows, lot.code())).toList();
        List<Integer> quantities = List.of(1, 2);
        setQuantity(lots.get(0).code(), "1");
        setQuantity(lots.get(1).code(), "2");
        click(confirmButton(), "Nhập thật nhiều lô với số lượng khác nhau");
        boolean closed = wait.until(d -> !dialogVisible());
        List<String> codes = lots.stream().map(LotOption::code).toList();
        Map<String, Integer> salesAfterValues = salesStockValues(codes);
        Map<String, Integer> mainAfterValues = mainStockValues(codes);
        List<Integer> salesAfter = codes.stream().map(code -> stockOf(salesAfterValues, code)).toList();
        List<Integer> mainAfter = codes.stream().map(code -> stockOf(mainAfterValues, code)).toList();
        SalesStockReceiptPage.ReceiptRow receipt = newReceipt(receiptsBefore,
                lots.stream().map(LotOption::code).toList());
        return new VariableSubmissionSnapshot(codes, quantities,
                mainBefore, mainAfter, salesBefore, salesAfter, closed, receipt);
    }

    public MultiSubmissionSnapshot submitTwoDifferentProducts() {
        Map<String, Integer> salesBeforeRows = allSalesStockValues();
        Set<String> receiptsBefore = receiptCodes();
        openForm();
        LotPair pair = findDifferentProductPair();
        selectPairWithoutClosingBetween(pair);
        int firstMain = stockInRow(pair.firstCode());
        int secondMain = stockInRow(pair.secondCode());
        int firstSales = stockOf(salesBeforeRows, pair.firstCode());
        int secondSales = stockOf(salesBeforeRows, pair.secondCode());
        setQuantity(pair.firstCode(), "1");
        setQuantity(pair.secondCode(), "1");
        click(confirmButton(), "Nhập thật lô thuộc hai sản phẩm khác nhau");
        boolean closed = wait.until(d -> !dialogVisible());
        Map<String, Integer> salesAfter = salesStockValues(List.of(pair.firstCode(), pair.secondCode()));
        Map<String, Integer> mainAfter = mainStockValues(List.of(pair.firstCode(), pair.secondCode()));
        int firstSalesAfter = stockOf(salesAfter, pair.firstCode());
        int secondSalesAfter = stockOf(salesAfter, pair.secondCode());
        int firstMainAfter = stockOf(mainAfter, pair.firstCode());
        int secondMainAfter = stockOf(mainAfter, pair.secondCode());
        SalesStockReceiptPage.ReceiptRow receipt = newReceipt(receiptsBefore,
                List.of(pair.firstCode(), pair.secondCode()));
        return new MultiSubmissionSnapshot(pair.firstCode(), firstMain, firstMainAfter, firstSales, firstSalesAfter,
                pair.secondCode(), secondMain, secondMainAfter, secondSales, secondSalesAfter,
                true, closed, receipt);
    }

    public DatedSubmissionSnapshot submitWithPastDate() {
        return submitWithDate(LocalDate.now().minusDays(1));
    }

    public DatedSubmissionSnapshot submitWithFutureDate() {
        return submitWithDate(LocalDate.now().plusDays(1));
    }

    public NoteSubmissionSnapshot submitWithUnicodeNote() {
        Set<String> receiptsBefore = receiptCodes();
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        String note = "Nhập chuyển kho — tiếng Việt 中文 !@#$%^&* " + System.currentTimeMillis();
        setReactInputValue(noteInput(), note);
        String noteBeforeSubmit = noteInput().getAttribute("value");
        click(confirmButton(), "Tạo phiếu thật có ghi chú Unicode và ký tự đặc biệt");
        boolean closed = wait.until(d -> !dialogVisible());
        SalesStockReceiptPage.ReceiptRow receipt = newReceipt(receiptsBefore, List.of(lot.code()));
        return new NoteSubmissionSnapshot(lot.code(), note, noteBeforeSubmit, closed, receipt);
    }

    public DuplicateSubmissionSnapshot doubleClickCreatesOneReceipt() {
        Set<String> receiptsBefore = receiptCodes();
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        WebElement submit = confirmButton();
        highlight(submit);
        pause("Nhấp đôi nút xác nhận Nhập hàng");
        new Actions(driver).doubleClick(submit).perform();
        boolean closed = wait.until(d -> !dialogVisible());
        List<SalesStockReceiptPage.ReceiptRow> created = new SalesStockReceiptPage(driver)
                .filteredRowsFast("Nhập từ kho tổng").stream()
                .filter(row -> !receiptsBefore.contains(row.code()) && row.lotCodes().contains(lot.code()))
                .toList();
        return new DuplicateSubmissionSnapshot(lot.code(), closed, created.size(),
                created.stream().map(SalesStockReceiptPage.ReceiptRow::code).toList());
    }

    public LoadingSubmissionSnapshot observesSubmitTransition() {
        Set<String> receiptsBefore = receiptCodes();
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        WebElement submit = confirmButton();
        highlight(submit);
        pause("Xác nhận và quan sát trạng thái đang chuyển");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "window.__importSubmitProtected=false;window.__importSubmitState='';"
                        + "const b=arguments[0];"
                        + "const mark=()=>{const t=(b.innerText||'');"
                        + "if(b.disabled||b.getAttribute('data-disabled')==='true'||t.includes('Đang chuyển')){"
                        + "window.__importSubmitProtected=true;window.__importSubmitState=t||'disabled';}};"
                        + "new MutationObserver(mark).observe(b,{attributes:true,childList:true,subtree:true,characterData:true});"
                        + "mark();", submit);
        new Actions(driver).click(submit).perform();
        boolean loadingOrClosed;
        try {
            loadingOrClosed = Waits.withTimeout(driver, Duration.ofSeconds(5))
                    .until(d -> Boolean.TRUE.equals(((JavascriptExecutor) d)
                            .executeScript("return window.__importSubmitProtected===true;")));
        } catch (TimeoutException ignored) {
            loadingOrClosed = false;
        }
        String stateText = String.valueOf(js.executeScript(
                "return window.__importSubmitState||'Không quan sát được trạng thái bảo vệ';"));
        boolean closed = wait.until(d -> !dialogVisible());
        SalesStockReceiptPage.ReceiptRow receipt = newReceipt(receiptsBefore, List.of(lot.code()));
        return new LoadingSubmissionSnapshot(lot.code(), loadingOrClosed, stateText, closed, receipt.code());
    }

    public ResetAfterSubmissionSnapshot reopensCleanAfterSuccessfulSubmit() {
        SubmissionSnapshot submitted = submitOneLot();
        openForm();
        return new ResetAfterSubmissionSnapshot(submitted.receipt().code(), noteInput().getAttribute("value"),
                selectedCount(), totalQuantity(),
                dialog().findElements(By.cssSelector("input[aria-label='Số lượng chuyển']")).size(),
                confirmButton().isEnabled());
    }

    public RemovedLotSubmissionSnapshot removeOneLotBeforeSubmit() {
        Set<String> receiptsBefore = receiptCodes();
        openForm();
        LotPair pair = findSameProductPair();
        selectPairWithoutClosingBetween(pair);
        click(rowForCode(pair.firstCode()).findElement(By.cssSelector("button[title='Xoá lô này']")),
                "Xóa lô thứ nhất trước khi tạo phiếu thật");
        wait.until(d -> rowsForCode(pair.firstCode()).isEmpty());
        setQuantity(pair.secondCode(), "1");
        click(confirmButton(), "Tạo phiếu chỉ với lô còn lại");
        boolean closed = wait.until(d -> !dialogVisible());
        SalesStockReceiptPage.ReceiptRow receipt = newReceipt(receiptsBefore, List.of(pair.secondCode()));
        return new RemovedLotSubmissionSnapshot(pair.firstCode(), pair.secondCode(), closed, receipt);
    }

    private TransferSnapshot submitSingleQuantity(int requestedQuantity, boolean exactStock) {
        Map<String, Integer> salesBeforeRows = allSalesStockValues();
        Set<String> receiptsBefore = receiptCodes();
        openForm();
        LotSnapshot lot = exactStock ? addSmallestAvailableLot() : addAvailableLotAtLeast(requestedQuantity);
        int quantity = exactStock ? lot.stock() : requestedQuantity;
        int salesBefore = stockOf(salesBeforeRows, lot.code());
        setQuantity(lot.code(), Integer.toString(quantity));
        boolean enabled = confirmButton().isEnabled();
        click(confirmButton(), exactStock
                ? "Nhập đúng toàn bộ tồn Kho tổng của lô " + lot.code()
                : "Nhập thật " + quantity + " cái của lô " + lot.code());
        boolean closed = wait.until(d -> !dialogVisible());
        int salesAfter = salesStock(lot.code());
        int mainAfter = mainStock(lot.code());
        SalesStockReceiptPage.ReceiptRow receipt = newReceipt(receiptsBefore, List.of(lot.code()));
        return new TransferSnapshot(lot.code(), quantity, lot.stock(), mainAfter, salesBefore, salesAfter,
                enabled, closed, receipt);
    }

    private DatedSubmissionSnapshot submitWithDate(LocalDate target) {
        Set<String> receiptsBefore = receiptCodes();
        openForm();
        LotSnapshot lot = addAvailableLot();
        setQuantity(lot.code(), "1");
        setDate(target);
        String inputDate = dateInput().getAttribute("value");
        click(confirmButton(), "Tạo phiếu thật với ngày nhập " + target);
        boolean closed = wait.until(d -> !dialogVisible());
        SalesStockReceiptPage.ReceiptRow receipt = newReceipt(receiptsBefore, List.of(lot.code()));
        return new DatedSubmissionSnapshot(lot.code(), target.toString(), inputDate, closed, receipt);
    }

    private LotSnapshot addAvailableLot() {
        fill(lotCombo(), "VT", "Tìm lô còn hàng trong Kho tổng");
        wait.until(d -> visibleElements(LOT_OPTIONS).stream().anyMatch(option -> optionStock(option) > 0));
        WebElement option = visibleElements(LOT_OPTIONS).stream().filter(item -> optionStock(item) > 0).findFirst().orElseThrow();
        String code = exactCode(elementText(option));
        int stock = optionStock(option);
        clickFreshOption(code, false);
        wait.until(d -> !rowsForCode(code).isEmpty());
        closeSuggestions();
        return new LotSnapshot(code, stock, elementText(rowForCode(code)));
    }

    private LotSnapshot addAvailableLotAtLeast(int minimumStock) {
        fill(lotCombo(), "VT", "Tìm lô có đủ tồn để tạo phiếu");
        wait.until(d -> visibleElements(LOT_OPTIONS).stream().anyMatch(option -> optionStock(option) >= minimumStock));
        WebElement option = visibleElements(LOT_OPTIONS).stream()
                .filter(item -> optionStock(item) >= minimumStock).findFirst().orElseThrow();
        return selectAvailableOption(option);
    }

    private LotSnapshot addSmallestAvailableLot() {
        fill(lotCombo(), "VT", "Tìm lô có tồn nhỏ nhất để nhập hết tồn");
        wait.until(d -> visibleElements(LOT_OPTIONS).stream().anyMatch(option -> optionStock(option) > 0));
        WebElement option = visibleElements(LOT_OPTIONS).stream().filter(item -> optionStock(item) > 0)
                .min(java.util.Comparator.comparingInt(this::optionStock)).orElseThrow();
        return selectAvailableOption(option);
    }

    private LotSnapshot selectAvailableOption(WebElement option) {
        String code = exactCode(elementText(option));
        int stock = optionStock(option);
        clickFreshOption(code, false);
        wait.until(d -> !rowsForCode(code).isEmpty());
        closeSuggestions();
        return new LotSnapshot(code, stock, elementText(rowForCode(code)));
    }

    private LotPair findSameProductPair() {
        fill(lotCombo(), "VT", "Tìm hai lô cùng sản phẩm trong một lần mở danh sách");
        wait.until(d -> visibleElements(LOT_OPTIONS).size() > 1);
        List<LotOption> options = visibleElements(LOT_OPTIONS).stream()
                .filter(option -> optionStock(option) > 0)
                .map(option -> new LotOption(exactCode(elementText(option)), productFromOption(elementText(option))))
                .filter(option -> !option.code().isBlank() && !option.product().isBlank()).toList();
        for (int i = 0; i < options.size(); i++) {
            for (int j = i + 1; j < options.size(); j++) {
                if (options.get(i).product().equals(options.get(j).product())) {
                    return new LotPair(options.get(i).code(), options.get(j).code());
                }
            }
        }
        throw new IllegalStateException("Không có hai lô còn hàng cùng sản phẩm trong Kho tổng.");
    }

    private LotPair findDifferentProductPair() {
        fill(lotCombo(), "VT", "Tìm hai lô thuộc hai sản phẩm khác nhau");
        wait.until(d -> visibleElements(LOT_OPTIONS).size() > 1);
        List<LotOption> options = visibleElements(LOT_OPTIONS).stream()
                .filter(option -> optionStock(option) > 0)
                .map(option -> new LotOption(exactCode(elementText(option)), productFromOption(elementText(option))))
                .filter(option -> !option.code().isBlank() && !option.product().isBlank()).toList();
        for (int i = 0; i < options.size(); i++) {
            for (int j = i + 1; j < options.size(); j++) {
                if (!options.get(i).product().equals(options.get(j).product())) {
                    return new LotPair(options.get(i).code(), options.get(j).code());
                }
            }
        }
        throw new IllegalStateException("Không có hai lô còn hàng thuộc hai sản phẩm khác nhau.");
    }

    private List<LotOption> availableOptions(int count, int minimumStock) {
        fill(lotCombo(), "VT", "Chuẩn bị danh sách lô còn hàng");
        wait.until(d -> visibleElements(LOT_OPTIONS).size() >= count);
        List<LotOption> options = visibleElements(LOT_OPTIONS).stream()
                .filter(option -> optionStock(option) >= minimumStock)
                .map(option -> new LotOption(exactCode(elementText(option)), productFromOption(elementText(option))))
                .filter(option -> !option.code().isBlank()).limit(count).toList();
        if (options.size() < count) {
            throw new IllegalStateException("Không đủ " + count + " lô có tồn tối thiểu " + minimumStock + ".");
        }
        return options;
    }

    private void selectLotsWithoutClosing(List<String> codes) {
        for (String code : codes) {
            fill(lotCombo(), code, "Chọn liên tục lô " + code);
            wait.until(d -> visibleElements(LOT_OPTIONS).stream()
                    .anyMatch(option -> exactCode(elementText(option)).equals(code)));
            clickFreshOption(code, false);
            wait.until(d -> !rowsForCode(code).isEmpty());
        }
        closeSuggestions();
    }

    private DifferentProductsSnapshot differentProductsSnapshot(LotPair pair) {
        return new DifferentProductsSnapshot(pair.firstCode(), pair.secondCode(),
                productName(productCardForCode(pair.firstCode())), productName(productCardForCode(pair.secondCode())),
                productCards().size(), !rowsForCode(pair.firstCode()).isEmpty(), !rowsForCode(pair.secondCode()).isEmpty());
    }

    private void selectPairWithoutClosingBetween(LotPair pair) {
        clickFreshOption(pair.firstCode(), false);
        wait.until(d -> !rowsForCode(pair.firstCode()).isEmpty());
        fill(lotCombo(), pair.secondCode(), "Tìm tiếp lô thứ hai khi dropdown vẫn mở");
        wait.until(d -> visibleElements(LOT_OPTIONS).stream()
                .anyMatch(option -> exactCode(elementText(option)).equals(pair.secondCode())));
        clickFreshOption(pair.secondCode(), false);
        wait.until(d -> !rowsForCode(pair.secondCode()).isEmpty());
        closeSuggestions();
    }

    private void clickFreshOption(String code, boolean closeAfter) {
        wait.until(d -> visibleElements(LOT_OPTIONS).stream()
                .filter(option -> exactCode(elementText(option)).equals(code)).findFirst()
                .map(option -> {
                    highlight(option);
                    pause("Chọn lô " + code + " trong danh sách gợi ý");
                    try {
                        option.click();
                        return true;
                    } catch (RuntimeException ignored) {
                        return false;
                    }
                })
                .orElse(false));
        if (closeAfter) closeSuggestions();
    }

    /** Mở danh sách bằng đúng nút mũi tên của combobox thay vì click vào input chỉ để focus. */
    private void openSuggestions(String step) {
        if (!"true".equals(lotCombo().getAttribute("aria-expanded"))) {
            click(lotSuggestionToggle(), step);
            wait.until(d -> "true".equals(lotCombo().getAttribute("aria-expanded")));
        }
        visible(By.cssSelector("[role='listbox']"));
        pause("Quan sát danh sách gợi ý đang mở");
    }

    private void closeSuggestions() {
        if ("true".equals(lotCombo().getAttribute("aria-expanded"))) {
            click(lotSuggestionToggle(), "Đóng danh sách lô bằng nút mũi tên");
            wait.until(d -> "false".equals(lotCombo().getAttribute("aria-expanded")));
        }
        pause("Đóng danh sách gợi ý để quan sát các lô đã chọn");
    }

    private void setQuantity(String code, String value) {
        WebElement input = rowForCode(code).findElement(By.cssSelector("input[aria-label='Số lượng chuyển']"));
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        if (!value.isEmpty()) input.sendKeys(value);
        settle(350);
    }

    private SelectionSnapshot selectionSnapshot(LotSnapshot lot) {
        return new SelectionSnapshot(lot.code(), lot.stock(), selectedCount(), totalQuantity(),
                confirmButton().isEnabled(), rowQuantity(lot.code()), elementText(rowForCode(lot.code())));
    }

    private QuantitySnapshot quantitySnapshot(LotSnapshot lot) {
        String rowText = elementText(rowForCode(lot.code()));
        String error = rowForCode(lot.code()).findElements(By.cssSelector("[data-slot='error-message']"))
                .stream().map(this::elementText).findFirst().orElse("");
        return new QuantitySnapshot(lot.code(), lot.stock(), rowQuantity(lot.code()), selectedCount(),
                totalQuantity(), confirmButton().isEnabled(), error, rowText);
    }

    private MultiValidationSnapshot multiValidation(LotPair pair) {
        return new MultiValidationSnapshot(pair.firstCode(), pair.secondCode(), rowQuantity(pair.firstCode()),
                rowQuantity(pair.secondCode()), selectedCount(), totalQuantity(), confirmButton().isEnabled(),
                lotCombo().isEnabled(), null);
    }

    private int mainStock(String code) {
        return stockOf(mainStockValues(List.of(code)), code);
    }

    private int salesStock(String code) {
        return stockOf(salesStockValues(List.of(code)), code);
    }

    private Map<String, Integer> salesStockValues(List<String> codes) {
        return new SalesStockPage(driver).salesStockValues(codes);
    }

    private Map<String, Integer> allSalesStockValues() {
        return new SalesStockPage(driver).salesStockValues(List.of());
    }

    private Map<String, Integer> mainStockValues(List<String> codes) {
        return new UniformInventoryPage(driver).stockValues(codes);
    }

    private static int stockOf(Map<String, Integer> values, String code) {
        return values.getOrDefault(code, 0);
    }

    private Set<String> receiptCodes() {
        return new HashSet<>(new SalesStockReceiptPage(driver).filteredRowsFast("Nhập từ kho tổng").stream()
                .map(SalesStockReceiptPage.ReceiptRow::code).toList());
    }

    private SalesStockReceiptPage.ReceiptRow newReceipt(Set<String> before, List<String> codes) {
        return new SalesStockReceiptPage(driver).filteredRowsFast("Nhập từ kho tổng").stream()
                .filter(row -> !before.contains(row.code()) && row.lotCodes().containsAll(codes))
                .findFirst().orElse(SalesStockReceiptPage.ReceiptRow.empty());
    }

    private void setDate(LocalDate date) {
        setReactInputValue(dateInput(), date.toString());
        wait.until(d -> date.toString().equals(dateInput().getAttribute("value")));
    }

    private void setReactInputValue(WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "const i=arguments[0],v=arguments[1],s=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;s.call(i,v);i.dispatchEvent(new Event('input',{bubbles:true}));i.dispatchEvent(new Event('change',{bubbles:true}));",
                input, value);
        settle(350);
    }

    private int productLotCount(String code) {
        return productCardForCode(code).findElements(By.cssSelector("input[aria-label='Số lượng chuyển']")).size();
    }
    private List<WebElement> productCards() { return dialog().findElements(By.xpath(".//div[contains(@class,'rounded-2xl')][.//button[normalize-space()='Gỡ sản phẩm']]")).stream().filter(WebElement::isDisplayed).toList(); }
    private String productName(WebElement card) { String header = elementText(card.findElement(By.xpath("./div[1]//p[1]"))); return header.replaceFirst("\\s*\\(\\d+\\s*lô\\)\\s*$", "").trim(); }
    private int stockInRow(String code) { return number(STOCK, elementText(rowForCode(code))); }
    private int optionStock(WebElement option) { return number(STOCK, elementText(option)); }
    private String rowQuantity(String code) { return rowForCode(code).findElement(By.cssSelector("input[aria-label='Số lượng chuyển']")).getAttribute("value"); }
    private List<WebElement> rowsForCode(String code) { return dialog().findElements(By.xpath(".//*[normalize-space()=" + xpathLiteral(code) + "]/ancestor::div[contains(@class,'grid')][1]")); }
    private WebElement rowForCode(String code) { return rowsForCode(code).stream().filter(WebElement::isDisplayed).findFirst().orElseThrow(); }
    private WebElement productCardForCode(String code) { return rowForCode(code).findElement(By.xpath("ancestor::div[contains(@class,'rounded-2xl')][1]")); }
    private int selectedCount() { return number(SELECTED, elementText(dialog())); }
    private int totalQuantity() { return number(TOTAL, elementText(dialog())); }
    private WebElement dateInput() { return dialog().findElement(By.cssSelector("input[aria-label='Ngày nhập']")); }
    private WebElement noteInput() { return dialog().findElement(By.cssSelector("input[aria-label='Ghi chú']")); }
    private WebElement lotCombo() { return dialog().findElement(By.cssSelector("input[aria-label='Thêm lô']")); }
    private WebElement lotSuggestionToggle() {
        return dialog().findElements(By.cssSelector(
                        "button[aria-label='Show suggestions']:not([data-visible])"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy nút mũi tên mở/đóng danh sách lô."));
    }
    private WebElement confirmButton() { return dialog().findElement(By.xpath(".//button[normalize-space()='Nhập về Kho bán hàng']")); }
    private WebElement dialogButton(String text) { return dialog().findElement(By.xpath(".//button[normalize-space()=" + xpathLiteral(text) + "]")); }
    private WebElement dialog() { return visible(DIALOG); }
    private boolean dialogVisible() { return driver.findElements(DIALOG).stream().anyMatch(WebElement::isDisplayed); }
    private boolean isSelected(WebElement element) { String classes = element.getAttribute("class"); return "true".equals(element.getAttribute("aria-selected")) || classes != null && classes.contains("bg-primary"); }
    private static String exactCode(String text) { Matcher matcher = LOT_CODE.matcher(text); return matcher.find() ? matcher.group() : ""; }
    private static String productFromOption(String text) { return text.replaceFirst("^\\s*(?:VT\\d+|AT[A-Z0-9]+)\\s*", "").replaceFirst("(?i)\\s*tồn\\s*[\\d,]+.*$", "").split("\\s*·\\s*", 2)[0].trim(); }
    private static int number(Pattern pattern, String text) { Matcher matcher = pattern.matcher(text); return matcher.find() ? Integer.parseInt(matcher.group(1).replaceAll("[^\\d]", "")) : -1; }

    public record FormSnapshot(String date, boolean dateRequired, String note, boolean lotCombobox,
                               int selectedLots, int totalQuantity, boolean submitEnabled, String text) {}
    public record CloseSnapshot(boolean dialogClosed) {}
    public record SearchSnapshot(String keyword, List<String> options, String formText) {}
    public record LotSnapshot(String code, int stock, String rowText) {}
    public record SelectionSnapshot(String code, int stock, int selectedLots, int totalQuantity,
                                    boolean submitEnabled, String quantity, String rowText) {}
    public record QuantitySnapshot(String code, int stock, String quantity, int selectedLots,
                                   int totalQuantity, boolean submitEnabled, String error, String rowText) {}
    public record DuplicateSnapshot(String code, boolean duplicateOptionVisible) {}
    public record MultiSelectionSnapshot(String firstCode, String secondCode, int selectedLots,
                                         int productLotCount, boolean dropdownClosed) {}
    public record RemovalSnapshot(String code, int selectedLots, int totalQuantity, boolean submitEnabled) {}
    public record ProductRemovalSnapshot(int lotsBefore, boolean removed, int selectedLots,
                                         int totalQuantity, boolean submitEnabled) {}
    public record ScrollSnapshot(int optionCount, boolean reachedBottom, boolean returnedTop) {}
    public record NoteSnapshot(String expected, String actual, String maxlength) {}
    public record SearchJourneySnapshot(int filteredCount, int restoredCount, String keyword, List<String> options) {}
    public record KeywordReplacementSnapshot(List<String> firstOptions, List<String> secondOptions, String keyword) {}
    public record KeyboardSelectionSnapshot(String code, boolean selected, boolean dropdownClosed,
                                             int selectedLots, int totalQuantity) {}
    public record DifferentProductsSnapshot(String firstCode, String secondCode, String firstProduct,
                                            String secondProduct, int productCards, boolean firstVisible,
                                            boolean secondVisible) {}
    public record PreserveLotsSnapshot(String removedCode, String remainingCode, boolean removed,
                                       boolean siblingVisible, int remainingLots) {}
    public record PreserveProductsSnapshot(int cardsBefore, int cardsAfter, boolean removedProductAbsent,
                                           boolean otherProductVisible) {}
    public record ResetSnapshot(String note, int selectedLots, int totalQuantity, int quantityInputs,
                                boolean submitEnabled) {}
    public record SelectedLotsScrollSnapshot(int lotCount, boolean reachedBottom, boolean returnedTop,
                                             boolean allLotsPreserved) {}
    public record MultipleQuantitySnapshot(List<String> codes, List<String> quantities, int selectedLots,
                                           int totalQuantity, boolean submitEnabled) {}
    public record DropdownStateSnapshot(String code, boolean opened, boolean closed,
                                        boolean selectedLotPreserved, int selectedLots, int totalQuantity) {}
    public record DateRangeSnapshot(String expectedPast, String actualPast, String expectedFuture, String actualFuture) {}
    public record MultiValidationSnapshot(String firstCode, String secondCode, String firstQuantity,
                                          String secondQuantity, int selectedLots, int totalQuantity,
                                          boolean submitEnabled, boolean comboEnabled,
                                          MultiValidationSnapshot previous) {
        MultiValidationSnapshot withPrevious(MultiValidationSnapshot value) {
            return new MultiValidationSnapshot(firstCode, secondCode, firstQuantity, secondQuantity,
                    selectedLots, totalQuantity, submitEnabled, comboEnabled, value);
        }
    }
    public record ValidationSnapshot(String date, boolean submitEnabled) {}
    public record RawValidationSnapshot(String code, String quantity, boolean submitEnabled,
                                        String error, String rowText) {}
    public record RecoverySnapshot(RawValidationSnapshot invalid, RawValidationSnapshot corrected,
                                   int selectedLots, int totalQuantity) {}
    public record RemovedInvalidRowSnapshot(String validCode, String removedCode,
                                            boolean disabledBeforeRemoval, boolean invalidRowRemoved,
                                            int selectedLots, int totalQuantity, boolean submitEnabled) {}
    public record DateRecoverySnapshot(String blankDate, boolean enabledWhenBlank,
                                       String expectedRestoredDate, String actualRestoredDate,
                                       boolean enabledAfterRestore) {}
    public record CombinedValidationSnapshot(String expectedDate, String actualDate, String quantity,
                                             int stock, boolean submitEnabled) {}
    public record MultiExactStockSnapshot(String firstCode, int firstStock, String firstQuantity,
                                          String secondCode, int secondStock, String secondQuantity,
                                          int selectedLots, int totalQuantity, boolean submitEnabled) {}
    public record BoundaryJourneySnapshot(int stock, RawValidationSnapshot above,
                                          RawValidationSnapshot exact, RawValidationSnapshot below,
                                          int finalTotal) {}
    public record SubmissionSnapshot(String code, int mainBefore, int mainAfter, int salesBefore,
                                     int salesAfter, boolean enabledBeforeSubmit, boolean dialogClosed,
                                     SalesStockReceiptPage.ReceiptRow receipt) {}
    public record MultiSubmissionSnapshot(String firstCode, int firstMainBefore, int firstMainAfter,
                                          int firstSalesBefore, int firstSalesAfter, String secondCode,
                                          int secondMainBefore, int secondMainAfter, int secondSalesBefore,
                                          int secondSalesAfter, boolean enabledBeforeSubmit, boolean dialogClosed,
                                          SalesStockReceiptPage.ReceiptRow receipt) {}
    public record CancellationSnapshot(String code, int mainBefore, int mainAfter, int salesBefore,
                                       int salesAfter, boolean dialogClosed) {}
    public record TransferSnapshot(String code, int quantity, int mainBefore, int mainAfter,
                                   int salesBefore, int salesAfter, boolean enabledBeforeSubmit,
                                   boolean dialogClosed, SalesStockReceiptPage.ReceiptRow receipt) {}
    public record VariableSubmissionSnapshot(List<String> codes, List<Integer> quantities,
                                             List<Integer> mainBefore, List<Integer> mainAfter,
                                             List<Integer> salesBefore, List<Integer> salesAfter,
                                             boolean dialogClosed, SalesStockReceiptPage.ReceiptRow receipt) {}
    public record DatedSubmissionSnapshot(String code, String expectedDate, String inputDate,
                                          boolean dialogClosed, SalesStockReceiptPage.ReceiptRow receipt) {}
    public record NoteSubmissionSnapshot(String code, String expectedNote, String actualNote,
                                         boolean dialogClosed, SalesStockReceiptPage.ReceiptRow receipt) {}
    public record DuplicateSubmissionSnapshot(String code, boolean dialogClosed, int createdReceiptCount,
                                              List<String> createdReceiptCodes) {}
    public record LoadingSubmissionSnapshot(String code, boolean loadingOrClosed, String stateText,
                                            boolean dialogClosed, String receiptCode) {}
    public record ResetAfterSubmissionSnapshot(String receiptCode, String note, int selectedLots,
                                               int totalQuantity, int quantityInputs, boolean submitEnabled) {}
    public record RemovedLotSubmissionSnapshot(String removedCode, String submittedCode,
                                               boolean dialogClosed, SalesStockReceiptPage.ReceiptRow receipt) {}
    private record LotOption(String code, String product) {}
    private record LotPair(String firstCode, String secondCode) {}
}
