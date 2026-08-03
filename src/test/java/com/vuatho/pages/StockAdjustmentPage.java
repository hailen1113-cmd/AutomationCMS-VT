package com.vuatho.pages;

import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page Object cho nghiệp vụ Kho tổng → Điều chỉnh tồn. */
public class StockAdjustmentPage extends UniformUiPage {
    private static final String ROUTE = "/vuatho/inventory-uniform?tab=main";
    private static final By DIALOG = By.xpath(
            "//section[@role='dialog'][.//*[normalize-space()='Điều chỉnh tồn']]");
    private static final By LOT_OPTIONS = By.cssSelector("[role='option']");
    private static final Pattern LOT_CODE = Pattern.compile("\\bVT\\d+\\b");
    private static final Pattern CURRENT_STOCK = Pattern.compile(
            "(?i)tồn hiện tại\\s*(\\d+)");
    private static final Pattern OPTION_STOCK = Pattern.compile("(?i)tồn\\s+(\\d+)");
    private static final Pattern CHANGED_COUNTER = Pattern.compile(
            "Lô thay đổi:\\s*(\\d+)\\s*/\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    public StockAdjustmentPage(WebDriver driver) {
        super(driver);
    }

    /** Mở Kho tổng và popup Điều chỉnh tồn. */
    public StockAdjustmentPage openForm() {
        openRoute(ROUTE);
        click(exactMainButton("Điều chỉnh tồn"), "Mở form Điều chỉnh tồn");
        visible(DIALOG);
        pause("Quan sát toàn bộ form Điều chỉnh tồn");
        return this;
    }

    /** Đọc cấu trúc và trạng thái mặc định của form. */
    public FormSnapshot formSnapshot() {
        openForm();
        WebElement dialog = dialog();
        WebElement date = dialog.findElement(By.cssSelector(
                "input[aria-label='Ngày điều chỉnh']"));
        WebElement reason = dialog.findElement(By.cssSelector(
                "input[aria-label='Lý do điều chỉnh']"));
        return new FormSnapshot(
                date.getAttribute("value"),
                "true".equals(date.getAttribute("aria-required")),
                reason.getAttribute("value"),
                dialog.findElements(By.cssSelector(
                        "input[role='combobox'][aria-label='Thêm lô cần điều chỉnh']"))
                        .size() == 1,
                lotRows().size(),
                changedCounter(),
                !confirmButton().isEnabled(),
                TextNormalizer.normalize(elementText(dialog))
                        .contains("khong anh huong kho ban hang"));
    }

    /** Bấm Hủy và xác nhận popup đóng. */
    public boolean cancelForm() {
        openForm();
        click(dialogButton("Hủy"), "Hủy điều chỉnh tồn");
        return wait.until(d -> !dialogVisible());
    }

    /** Bấm dấu X và xác nhận popup đóng. */
    public boolean closeForm() {
        openForm();
        WebElement close = dialog().findElement(By.cssSelector(
                "button[aria-label='Close']"));
        click(close, "Đóng form Điều chỉnh tồn bằng dấu X");
        return wait.until(d -> !dialogVisible());
    }

    /** Tìm một mã không tồn tại và đọc số gợi ý trả về. */
    public SearchSnapshot searchUnknownLot() {
        openForm();
        WebElement combo = lotCombo();
        fill(combo, "__automation_lot_not_found__", "Tìm mã lô không tồn tại");
        settle(800);
        pause("Quan sát danh sách gợi ý không có dữ liệu");
        return new SearchSnapshot(combo.getAttribute("value"), optionTexts());
    }

    /** Tìm lô bằng tên sản phẩm thay vì mã lô. */
    public SearchSnapshot searchLotsByProductName() {
        openForm();
        WebElement combo = lotCombo();
        String keyword = "Áo thun Media";
        fill(combo, keyword, "Tìm lô theo tên sản phẩm " + keyword);
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty());
        pause("Quan sát các lô trả về theo tên sản phẩm");
        return new SearchSnapshot(combo.getAttribute("value"), optionTexts());
    }

    /** Thêm một lô rồi tìm lại chính mã đó để kiểm tra chống chọn trùng. */
    public DuplicateLotSnapshot selectedLotIsExcludedFromSuggestions() {
        openForm();
        LotSnapshot selected = addAvailableLot(true);
        WebElement combo = lotCombo();
        fill(combo, selected.code(), "Tìm lại lô đã thêm " + selected.code());
        settle(800);
        List<String> options = optionTexts();
        pause("Quan sát lô đã chọn không xuất hiện lại trong gợi ý");
        return new DuplicateLotSnapshot(selected.code(), options, lotRows().size());
    }

    /** Nhập thủ công ngày và lý do, không submit dữ liệu. */
    public ManualMetadataSnapshot enterManualDateAndReason() {
        openForm();
        LocalDate targetDate = LocalDate.now().minusDays(1);
        WebElement date = dialog().findElement(By.cssSelector(
                "input[aria-label='Ngày điều chỉnh']"));
        highlight(date);
        pause("Chọn ngày điều chỉnh thủ công " + targetDate);
        date.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        date.sendKeys(targetDate.format(DateTimeFormatter.ofPattern("MMddyyyy")));
        settle(300);
        String reasonText = "Kiểm kê automation có dấu";
        WebElement reason = dialog().findElement(By.cssSelector(
                "input[aria-label='Lý do điều chỉnh']"));
        fill(reason, reasonText, "Nhập lý do điều chỉnh thủ công");
        pause("Quan sát ngày và lý do đã nhập trên form");
        return new ManualMetadataSnapshot(targetDate.toString(),
                date.getAttribute("value"), reasonText,
                reason.getAttribute("value"));
    }

    /** Thêm lô đầu tiên có tồn lớn hơn 0. */
    public LotSnapshot addFirstAvailableLot() {
        if (!dialogVisible()) {
            openForm();
        }
        return addAvailableLot(true);
    }

    /** Thêm hai lô khác nhau vào cùng form. */
    public MultiLotSnapshot addTwoDifferentLots() {
        openForm();
        LotSnapshot first = addAvailableLot(true);
        LotSnapshot second = addAvailableLot(false);
        return new MultiLotSnapshot(first, second, lotRows().size(), changedCounter());
    }

    /** Thêm toàn bộ lô đang có để kiểm tra vùng cuộn dài trong popup. */
    public ManyLotsSnapshot addManyLotsForScrolling() {
        openForm();
        List<LotSnapshot> lots = new ArrayList<>();
        while (lots.size() < 100) {
            WebElement combo = lotCombo();
            try {
                fill(combo, "VT", "Tìm các lô còn lại để thêm vào phiếu");
            } catch (ElementNotInteractableException exhausted) {
                if (lots.size() <= 5) {
                    throw exhausted;
                }
                pause("Quan sát ô chọn lô đã khóa sau khi chọn hết dữ liệu");
                break;
            }
            settle(800);
            List<WebElement> options = visibleElements(LOT_OPTIONS);
            if (options.isEmpty()) {
                pause("Quan sát không còn lô nào chưa được chọn");
                break;
            }
            WebElement chosen = options.get(0);
            String optionText = elementText(chosen);
            String code = match(LOT_CODE, optionText);
            int current = number(OPTION_STOCK, optionText);
            clickFreshOption(code, chosen,
                    "Thêm lô " + code + " vào danh sách dài");
            wait.until(d -> !rowElementsForCode(code).isEmpty());
            WebElement row = rowForCode(code);
            int renderedCurrent = number(CURRENT_STOCK, elementText(row));
            observeLotRow(row, "Quan sát lô vừa thêm " + code);
            lots.add(new LotSnapshot(code,
                    renderedCurrent >= 0 ? renderedCurrent : current,
                    actualInput(code).getAttribute("value"), elementText(row)));
        }
        if (lots.isEmpty()) {
            throw new IllegalStateException("Không có lô để kiểm tra danh sách dài.");
        }
        WebElement lastRow = rowForCode(lots.get(lots.size() - 1).code());
        observeLotRow(lastRow,
                "Cuộn đến lô cuối danh sách dài " + lots.get(lots.size() - 1).code());
        return new ManyLotsSnapshot(lots, lotRows().size(), changedCounter());
    }

    /** Thêm ba lô rồi xóa lô ở giữa danh sách. */
    public RemoveMiddleSnapshot removeMiddleLotFromMultipleRows() {
        openForm();
        LotSnapshot first = addAvailableLot(false);
        LotSnapshot middle = addAvailableLot(false);
        LotSnapshot last = addAvailableLot(false);
        WebElement remove = rowForCode(middle.code()).findElement(
                By.cssSelector("button[title='Xoá lô này']"));
        click(remove, "Xóa lô giữa danh sách " + middle.code());
        wait.until(d -> rowElementsForCode(middle.code()).isEmpty());
        observeLotRow(rowForCode(last.code()),
                "Quan sát các lô còn lại sau khi xóa lô giữa");
        List<String> remainingCodes = lotRows().stream()
                .map(this::elementText).map(text -> match(LOT_CODE, text)).toList();
        return new RemoveMiddleSnapshot(first.code(), middle.code(), last.code(),
                remainingCodes, changedCounter());
    }

    /** Thêm rồi xóa lô khỏi form. */
    public RemoveSnapshot addAndRemoveLot() {
        openForm();
        LotSnapshot selected = addAvailableLot(true);
        WebElement row = rowForCode(selected.code());
        WebElement remove = row.findElement(By.cssSelector("button[title='Xoá lô này']"));
        click(remove, "Xóa lô " + selected.code() + " khỏi phiếu điều chỉnh");
        wait.until(d -> lotRows().isEmpty());
        pause("Quan sát form đã trở về trạng thái chưa có lô");
        return new RemoveSnapshot(selected.code(), lotRows().size(),
                changedCounter(), !confirmButton().isEnabled());
    }

    /** Nhập số thực tế lệch tồn hiện tại và đọc chênh lệch. */
    public QuantitySnapshot enterQuantityDelta(int delta) {
        openForm();
        LotSnapshot lot = addAvailableLot(true);
        int actual = lot.currentStock() + delta;
        setActual(lot.code(), Integer.toString(actual));
        return quantitySnapshot(lot.code(), lot.currentStock(), actual);
    }

    /** Nhập số bằng tồn hiện tại. */
    public QuantitySnapshot enterUnchangedQuantity() {
        openForm();
        LotSnapshot lot = addAvailableLot(true);
        setActual(lot.code(), Integer.toString(lot.currentStock()));
        return quantitySnapshot(lot.code(), lot.currentStock(), lot.currentStock());
    }

    /** Thử tuần tự các định dạng sai trên cùng một lô. */
    public List<InvalidQuantitySnapshot> invalidQuantityFormats() {
        openForm();
        LotSnapshot lot = addAvailableLot(true);
        List<InvalidQuantitySnapshot> results = new ArrayList<>();
        for (String value : List.of("", "abc", "-1", "1.5")) {
            setActual(lot.code(), value);
            WebElement input = actualInput(lot.code());
            results.add(new InvalidQuantitySnapshot(
                    value, input.getAttribute("value"), confirmButton().isEnabled()));
        }
        return results;
    }

    /** Chỉ bật xác nhận sau khi mọi lô đã có số thực tế. */
    public MultiLotCompletionSnapshot completeAllSelectedLots() {
        openForm();
        LotSnapshot changedLot = addAvailableLot(true);
        LotSnapshot unchangedLot = addAvailableLot(false);
        setActual(changedLot.code(), Integer.toString(changedLot.currentStock() + 1));
        boolean enabledWithMissingLot = confirmButton().isEnabled();
        Counter counterBeforeCompletion = changedCounter();
        setActual(unchangedLot.code(), Integer.toString(unchangedLot.currentStock()));
        return new MultiLotCompletionSnapshot(changedLot.code(), unchangedLot.code(),
                enabledWithMissingLot, counterBeforeCompletion,
                confirmButton().isEnabled(), changedCounter());
    }

    /** Số 0 là giá trị hợp lệ khi tồn hiện tại của lô cũng bằng 0. */
    public ZeroStockSnapshot acceptsZeroForZeroStockLot() {
        openForm();
        LotSnapshot zeroLot = addLotWithExactStock(0);
        LotSnapshot changedLot = addAvailableLot(true);
        setActual(zeroLot.code(), "0");
        boolean enabledBeforeAllLots = confirmButton().isEnabled();
        setActual(changedLot.code(), Integer.toString(changedLot.currentStock() + 1));
        return new ZeroStockSnapshot(zeroLot.code(), zeroLot.currentStock(),
                actualInput(zeroLot.code()).getAttribute("value"),
                enabledBeforeAllLots, confirmButton().isEnabled(), changedCounter());
    }

    /** Nhập 0 cho lô đang có tồn dương và đọc chênh lệch giảm toàn bộ. */
    public QuantitySnapshot reducePositiveStockToZero() {
        openForm();
        LotSnapshot lot = addAvailableLot(true);
        setActual(lot.code(), "0");
        return quantitySnapshot(lot.code(), lot.currentStock(), 0);
    }

    /** Xóa ngày sau khi đã tạo một thay đổi hợp lệ. */
    public RequiredDateSnapshot clearRequiredDateAndSubmit() {
        openForm();
        LotSnapshot lot = addAvailableLot(true);
        setActual(lot.code(), Integer.toString(lot.currentStock() + 1));
        WebElement date = dialog().findElement(By.cssSelector(
                "input[aria-label='Ngày điều chỉnh']"));
        highlight(date);
        pause("Xóa ngày điều chỉnh bắt buộc");
        date.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        settle(300);
        pause("Quan sát trạng thái form khi thiếu ngày điều chỉnh");
        String clearedDate = date.getAttribute("value");
        boolean confirmEnabledBeforeSubmit = confirmButton().isEnabled();
        click(confirmButton(), "Thử xác nhận khi thiếu ngày điều chỉnh");
        settle(1_000);
        boolean submissionBlocked = dialogVisible();
        if (!submissionBlocked) {
            int current = currentStock(lot.code());
            if (current != lot.currentStock()) {
                openForm();
                addLotByCode(lot.code());
                submitCurrentForm(lot.code(), lot.currentStock(),
                        "Automation khôi phục tồn sau kiểm tra thiếu ngày");
            }
        } else {
            pause("Quan sát form chặn xác nhận do thiếu ngày");
        }
        return new RequiredDateSnapshot(clearedDate,
                confirmEnabledBeforeSubmit, submissionBlocked);
    }

    /** Không nhập lý do nhưng tạo thay đổi hợp lệ. */
    public OptionalReasonSnapshot leaveReasonBlank() {
        openForm();
        LotSnapshot lot = addAvailableLot(true);
        setActual(lot.code(), Integer.toString(lot.currentStock() + 1));
        String reason = dialog().findElement(By.cssSelector(
                "input[aria-label='Lý do điều chỉnh']")).getAttribute("value");
        pause("Quan sát nút xác nhận khi để trống lý do");
        return new OptionalReasonSnapshot(reason, confirmButton().isEnabled());
    }

    /** Điều chỉnh tăng một đơn vị rồi điều chỉnh giảm để khôi phục tồn ban đầu. */
    public SubmissionSnapshot submitIncreaseAndRestore() {
        openForm();
        LotSnapshot lot = addAvailableLot(true);
        int increased = lot.currentStock() + 1;
        submitCurrentForm(lot.code(), increased,
                "Automation kiểm tra điều chỉnh tăng tồn");
        int stockAfterIncrease = currentStock(lot.code());

        openForm();
        LotSnapshot restoreLot = addLotByCode(lot.code());
        submitCurrentForm(restoreLot.code(), lot.currentStock(),
                "Automation khôi phục tồn sau kiểm tra");
        int stockAfterRestore = currentStock(lot.code());
        return new SubmissionSnapshot(lot.code(), lot.currentStock(), increased,
                stockAfterIncrease, stockAfterRestore);
    }

    /**
     * Submit một phiếu có hai lô: một lô tăng, một lô giảm; dùng ngày thủ công,
     * kiểm tra phiếu vừa sinh rồi tạo phiếu thứ hai để khôi phục cả hai lô.
     */
    public MultiSubmissionSnapshot submitMultipleLotsAndRestore() {
        openForm();
        LotSnapshot increasedLot = addAvailableLot(true);
        LotSnapshot decreasedLot = addAvailableLot(true);
        int increasedTarget = increasedLot.currentStock() + 1;
        int decreasedTarget = decreasedLot.currentStock() - 1;
        setActual(increasedLot.code(), Integer.toString(increasedTarget));
        setActual(decreasedLot.code(), Integer.toString(decreasedTarget));
        Counter submittedCounter = changedCounter();
        LocalDate adjustmentDate = LocalDate.now().minusDays(1);
        setAdjustmentDate(adjustmentDate);
        submitPreparedForm("Automation điều chỉnh nhiều lô tăng và giảm",
                "Xác nhận điều chỉnh thật hai lô");

        int increasedStock = -1;
        int decreasedStock = -1;
        String voucherText = "";
        RuntimeException verificationFailure = null;
        try {
            increasedStock = currentStock(increasedLot.code());
            decreasedStock = currentStock(decreasedLot.code());
            voucherText = adjustmentVoucherText(
                    increasedLot.code(), decreasedLot.code());
        } catch (RuntimeException exception) {
            verificationFailure = exception;
        } finally {
            openForm();
            addLotByCode(increasedLot.code());
            addLotByCode(decreasedLot.code());
            setActual(increasedLot.code(), Integer.toString(increasedLot.currentStock()));
            setActual(decreasedLot.code(), Integer.toString(decreasedLot.currentStock()));
            submitPreparedForm("Automation khôi phục nhiều lô sau kiểm tra",
                    "Xác nhận khôi phục tồn của hai lô");
        }
        if (verificationFailure != null) {
            throw verificationFailure;
        }

        return new MultiSubmissionSnapshot(
                increasedLot.code(), decreasedLot.code(),
                increasedLot.currentStock(), decreasedLot.currentStock(),
                increasedTarget, decreasedTarget,
                increasedStock, decreasedStock,
                currentStock(increasedLot.code()), currentStock(decreasedLot.code()),
                submittedCounter, adjustmentDate, voucherText);
    }

    private void submitCurrentForm(String code, int actual, String reason) {
        setActual(code, Integer.toString(actual));
        submitPreparedForm(reason, "Xác nhận điều chỉnh tồn thật cho " + code);
    }

    private void submitPreparedForm(String reason, String confirmationStep) {
        fill(dialog().findElement(By.cssSelector(
                        "input[aria-label='Lý do điều chỉnh']")),
                reason, "Nhập lý do điều chỉnh");
        WebElement confirm = confirmButton();
        if (!confirm.isEnabled()) {
            throw new IllegalStateException("Nút xác nhận điều chỉnh chưa được bật.");
        }
        click(confirm, confirmationStep);
        wait.until(d -> !dialogVisible());
        waitForResult();
    }

    private void setAdjustmentDate(LocalDate targetDate) {
        WebElement date = dialog().findElement(By.cssSelector(
                "input[aria-label='Ngày điều chỉnh']"));
        highlight(date);
        pause("Chọn ngày điều chỉnh thủ công " + targetDate);
        date.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        date.sendKeys(targetDate.format(DateTimeFormatter.ofPattern("MMddyyyy")));
        settle(300);
        if (!targetDate.toString().equals(date.getAttribute("value"))) {
            throw new IllegalStateException("Không nhập được ngày điều chỉnh thủ công.");
        }
    }

    private String adjustmentVoucherText(String firstCode, String secondCode) {
        openRoute(ROUTE);
        click(exactMainButton("Phiếu"), "Mở danh sách phiếu sau điều chỉnh nhiều lô");
        waitForResult();
        click(exactMainButton("Điều chỉnh tồn"),
                "Lọc phiếu Điều chỉnh tồn vừa tạo");
        waitForResult();
        WebElement row = visible(By.xpath("//main//*[normalize-space()="
                + xpathLiteral(firstCode) + "]"
                + "/ancestor::div[contains(@class,'grid')][1]"));
        String text = elementText(row);
        if (!text.contains(secondCode)) {
            throw new IllegalStateException(
                    "Phiếu mới không chứa đủ hai lô đã điều chỉnh.");
        }
        observeLotRow(row, "Quan sát phiếu điều chỉnh chứa hai lô vừa tạo");
        return text;
    }

    private int currentStock(String code) {
        openRoute(ROUTE);
        WebElement row = visible(By.xpath("//main//tr[.//*[normalize-space()="
                + xpathLiteral(code) + "]]"));
        List<WebElement> cells = row.findElements(By.xpath("./*"));
        if (cells.size() < 2) {
            throw new IllegalStateException("Không đọc được tồn hiện tại của " + code);
        }
        highlight(cells.get(1));
        pause("Quan sát tồn hiện tại của " + code);
        return Integer.parseInt(elementText(cells.get(1)).replaceAll("\\D", ""));
    }

    private LotSnapshot addAvailableLot(boolean requirePositiveStock) {
        WebElement combo = lotCombo();
        fill(combo, "VT", "Tìm lô có dữ liệu để điều chỉnh");
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty());
        List<WebElement> options = visibleElements(LOT_OPTIONS);
        WebElement chosen = options.stream().filter(option -> {
            Matcher matcher = OPTION_STOCK.matcher(elementText(option));
            return !requirePositiveStock || matcher.find()
                    && Integer.parseInt(matcher.group(1)) > 0;
        }).findFirst().orElse(options.get(0));
        String optionText = elementText(chosen);
        String code = match(LOT_CODE, optionText);
        int current = number(OPTION_STOCK, optionText);
        clickFreshOption(code, chosen,
                "Chọn lô " + code + " từ danh sách gợi ý");
        wait.until(d -> !rowElementsForCode(code).isEmpty());
        WebElement row = rowForCode(code);
        int renderedCurrent = number(CURRENT_STOCK, elementText(row));
        observeLotRow(row, "Quan sát thông tin tồn và số thực tế của " + code);
        return new LotSnapshot(code,
                renderedCurrent >= 0 ? renderedCurrent : current,
                actualInput(code).getAttribute("value"), elementText(row));
    }

    private LotSnapshot addLotByCode(String code) {
        WebElement combo = lotCombo();
        fill(combo, code, "Tìm lại lô " + code + " để khôi phục tồn");
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty());
        WebElement option = visibleElements(LOT_OPTIONS).stream()
                .filter(item -> elementText(item).contains(code))
                .findFirst().orElseThrow();
        clickFreshOption(code, option, "Chọn lại lô " + code);
        wait.until(d -> !rowElementsForCode(code).isEmpty());
        WebElement row = rowForCode(code);
        observeLotRow(row, "Quan sát lô " + code + " vừa được thêm lại");
        return new LotSnapshot(code, number(CURRENT_STOCK, elementText(row)),
                actualInput(code).getAttribute("value"), elementText(row));
    }

    /** Chọn một lô có đúng số tồn yêu cầu từ dữ liệu thật. */
    private LotSnapshot addLotWithExactStock(int expectedStock) {
        WebElement combo = lotCombo();
        fill(combo, "VT", "Tìm lô có tồn hiện tại bằng " + expectedStock);
        wait.until(d -> !visibleElements(LOT_OPTIONS).isEmpty());
        WebElement chosen = visibleElements(LOT_OPTIONS).stream()
                .filter(option -> number(OPTION_STOCK, elementText(option)) == expectedStock)
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Không có lô tồn bằng " + expectedStock + " để kiểm tra."));
        String code = match(LOT_CODE, elementText(chosen));
        clickFreshOption(code, chosen, "Chọn lô tồn bằng 0: " + code);
        wait.until(d -> !rowElementsForCode(code).isEmpty());
        WebElement row = rowForCode(code);
        observeLotRow(row, "Quan sát lô tồn bằng 0 vừa được thêm");
        return new LotSnapshot(code, number(CURRENT_STOCK, elementText(row)),
                actualInput(code).getAttribute("value"), elementText(row));
    }

    private void setActual(String code, String value) {
        WebElement input = actualInput(code);
        highlight(input);
        pause("Nhập số thực tế " + (value.isBlank() ? "trống" : value)
                + " cho " + code);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        if (!value.isEmpty()) {
            input.sendKeys(value);
        }
        settle(300);
        pause("Quan sát chênh lệch sau khi nhập số thực tế");
    }

    /**
     * React render lại listbox trong lúc giữ màn hình quan sát; vì vậy sau 500 ms
     * phải tìm lại option theo mã lô rồi mới click element còn hiệu lực.
     */
    private void clickFreshOption(
            String code, WebElement observedOption, String step) {
        highlight(observedOption);
        pause(step);
        wait.until(d -> visibleElements(LOT_OPTIONS).stream()
                .filter(item -> elementText(item).contains(code))
                .findFirst()
                .map(item -> {
                    try {
                        item.click();
                        return true;
                    } catch (RuntimeException exception) {
                        return false;
                    }
                }).orElse(false));
    }

    /** Cuộn dòng lô vào giữa popup, bỏ highlight cũ và giữ màn hình 500 ms. */
    private void observeLotRow(WebElement row, String step) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center',behavior:'smooth'});",
                row);
        highlight(row);
        pause(step);
    }

    private QuantitySnapshot quantitySnapshot(String code, int current, int actual) {
        String rowText = elementText(rowForCode(code));
        Counter counter = changedCounter();
        return new QuantitySnapshot(code, current, actual,
                actualInput(code).getAttribute("value"), rowText,
                counter, confirmButton().isEnabled());
    }

    private List<String> optionTexts() {
        return visibleElements(LOT_OPTIONS).stream().map(this::elementText).toList();
    }

    private List<WebElement> lotRows() {
        return dialog().findElements(By.xpath(
                ".//input[@aria-label='Số thực tế']"
                        + "/ancestor::div[contains(@class,'grid')][1]"));
    }

    private List<WebElement> rowElementsForCode(String code) {
        return dialog().findElements(By.xpath(
                ".//*[normalize-space()=" + xpathLiteral(code) + "]"
                        + "/ancestor::div[contains(@class,'grid')][1]"));
    }

    private WebElement rowForCode(String code) {
        return rowElementsForCode(code).stream().filter(WebElement::isDisplayed)
                .findFirst().orElseThrow();
    }

    private WebElement actualInput(String code) {
        return rowForCode(code).findElement(By.cssSelector(
                "input[aria-label='Số thực tế']"));
    }

    private Counter changedCounter() {
        Matcher matcher = CHANGED_COUNTER.matcher(elementText(dialog()));
        return matcher.find()
                ? new Counter(Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)))
                : new Counter(-1, -1);
    }

    private WebElement lotCombo() {
        return dialog().findElement(By.cssSelector(
                "input[role='combobox'][aria-label='Thêm lô cần điều chỉnh']"));
    }

    private WebElement confirmButton() {
        return dialogButton("Xác nhận điều chỉnh");
    }

    private WebElement dialogButton(String text) {
        return dialog().findElement(By.xpath(
                ".//button[normalize-space()=" + xpathLiteral(text) + "]"));
    }

    private WebElement dialog() {
        return visible(DIALOG);
    }

    private boolean dialogVisible() {
        return driver.findElements(DIALOG).stream().anyMatch(WebElement::isDisplayed);
    }

    private WebElement exactMainButton(String text) {
        return visible(By.xpath("//main//button[normalize-space()="
                + xpathLiteral(text) + "]"));
    }

    private static String match(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text == null ? "" : text);
        return matcher.find() ? matcher.group() : "";
    }

    private static int number(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text == null ? "" : text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    public record FormSnapshot(String date, boolean dateRequired, String reason,
                               boolean lotCombobox, int lotCount, Counter counter,
                               boolean confirmDisabled, boolean mainWarehouseNotice) {
    }

    public record SearchSnapshot(String keyword, List<String> options) {
    }

    public record DuplicateLotSnapshot(String selectedCode, List<String> options,
                                       int rowCount) {
    }

    public record ManualMetadataSnapshot(String expectedDate, String actualDate,
                                         String expectedReason, String actualReason) {
    }

    public record LotSnapshot(String code, int currentStock, String actualValue,
                              String rowText) {
    }

    public record MultiLotSnapshot(LotSnapshot first, LotSnapshot second,
                                   int rowCount, Counter counter) {
    }

    public record ManyLotsSnapshot(List<LotSnapshot> lots, int rowCount,
                                   Counter counter) {
    }

    public record RemoveMiddleSnapshot(String firstCode, String removedCode,
                                       String lastCode, List<String> remainingCodes,
                                       Counter counter) {
    }

    public record RemoveSnapshot(String code, int rowCount, Counter counter,
                                 boolean confirmDisabled) {
    }

    public record QuantitySnapshot(String code, int currentStock, int expectedActual,
                                   String actualValue, String rowText, Counter counter,
                                   boolean confirmEnabled) {
    }

    public record InvalidQuantitySnapshot(String attemptedValue, String actualValue,
                                          boolean confirmEnabled) {
    }

    public record MultiLotCompletionSnapshot(
            String changedCode, String unchangedCode,
            boolean enabledWithMissingLot, Counter counterBeforeCompletion,
            boolean enabledAfterCompletion, Counter counterAfterCompletion) {
    }

    public record ZeroStockSnapshot(String code, int currentStock,
                                    String actualValue, boolean enabledBeforeAllLots,
                                    boolean enabledAfterAllLots, Counter counter) {
    }

    public record RequiredDateSnapshot(String date,
                                       boolean confirmEnabledBeforeSubmit,
                                       boolean submissionBlocked) {
    }

    public record OptionalReasonSnapshot(String reason, boolean confirmEnabled) {
    }

    public record SubmissionSnapshot(String code, int initialStock, int expectedIncreased,
                                     int stockAfterIncrease, int stockAfterRestore) {
    }

    public record MultiSubmissionSnapshot(
            String increasedCode, String decreasedCode,
            int increasedInitial, int decreasedInitial,
            int increasedTarget, int decreasedTarget,
            int increasedStock, int decreasedStock,
            int increasedRestored, int decreasedRestored,
            Counter submittedCounter, LocalDate adjustmentDate,
            String voucherText) {
    }

    public record Counter(int changed, int total) {
    }
}
