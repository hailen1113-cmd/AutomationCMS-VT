package com.vuatho.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page Object cho Kho bán hàng → Phiếu → Xuất hàng cho nhân sự. */
public final class SalesStockStaffExportPage extends UniformUiPage {
    private static final String ROUTE = "/vuatho/inventory-uniform?tab=sub";
    private static final By DIALOG = By.xpath("//section[@role='dialog'][.//*[normalize-space()='Xuất hàng cho nhân sự']]");
    private static final By LOT_OPTIONS = By.cssSelector("[role='option']");
    private static final Pattern LOT_CODE = Pattern.compile("\\bVT\\d+\\b");
    private static final Pattern STOCK = Pattern.compile("(?i)tồn\\s*(\\d+)\\s*(?:cái)?");
    private static final Pattern SELECTED = Pattern.compile("Lô được chọn:\\s*(\\d+)\\s*/\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL = Pattern.compile("Tổng SL:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    public SalesStockStaffExportPage(WebDriver driver) {
        super(driver);
    }

    /** Mở form xuất hàng từ tab Phiếu của Kho bán hàng. */
    public SalesStockStaffExportPage openForm() {
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
                form.findElement(By.cssSelector("input[aria-label='Ghi chú']")).getAttribute("value"),
                form.findElements(By.cssSelector("input[aria-label='Thêm lô']")).size() == 1,
                selectedCount(), totalQuantity(), confirmButton().isEnabled(), elementText(form));
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
        pause("Quan sát danh sách phiếu sau khi xuất hàng thành công");
        return new SubmissionSnapshot(lot.code(), note, enabledBeforeSubmit, closed);
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

    private WebElement rowForCode(String code) {
        return rowsForCode(code).stream().filter(WebElement::isDisplayed).findFirst().orElseThrow();
    }

    private int selectedCount() { return number(SELECTED, elementText(dialog())); }
    private int totalQuantity() { return number(TOTAL, elementText(dialog())); }
    private WebElement lotCombo() { return dialog().findElement(By.cssSelector("input[aria-label='Thêm lô']")); }
    private WebElement confirmButton() { return dialog().findElement(By.xpath(".//button[normalize-space()='Xuất cho nhân sự']")); }
    private WebElement dialog() { return visible(DIALOG); }
    private boolean dialogVisible() { return driver.findElements(DIALOG).stream().anyMatch(WebElement::isDisplayed); }
    private boolean isSelected(WebElement element) {
        String classes = element.getAttribute("class");
        return "true".equals(element.getAttribute("aria-selected"))
                || (classes != null && (classes.contains("bg-primary") || classes.contains("text-white")));
    }
    private static String match(Pattern pattern, String text) { Matcher matcher = pattern.matcher(text); return matcher.find() ? matcher.group() : ""; }
    private static int number(Pattern pattern, String text) { Matcher matcher = pattern.matcher(text); return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1; }

    public record FormSnapshot(String date, String note, boolean lotCombobox, int selectedLots,
                               int totalQuantity, boolean submitEnabled, String text) { }
    public record SearchSnapshot(String keyword, List<String> options) { }
    public record LotSnapshot(String code, int stock, String rowText) { }
    public record QuantitySnapshot(String code, int stock, String quantity, int selectedLots,
                                   int totalQuantity, boolean submitEnabled, String rowText, String formText) { }
    public record RemovalSnapshot(String code, int selectedLots, int totalQuantity, boolean submitEnabled) { }
    public record ScrollSnapshot(int optionCount, boolean reachedLast, boolean returnedFirst) { }
    public record SubmissionSnapshot(String code, String note, boolean enabledBeforeSubmit, boolean dialogClosed) { }
}
