package com.vuatho.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object của Kho Đồng phục, gồm Kho tổng và Kho bán hàng.
 */
public final class UniformInventoryPage extends UniformUiPage {
    public static final String ROUTE = "/vuatho/inventory-uniform";

    public UniformInventoryPage(WebDriver driver) {
        super(driver);
    }

    /** Mở Kho tổng. */
    public UniformInventoryPage open() {
        openRoute(ROUTE + "?tab=main");
        waitForResult();
        return this;
    }

    /** Chuyển giữa Kho tổng và Kho bán hàng. */
    public UniformInventoryPage selectWarehouse(String warehouse) {
        WebElement tab = visible(By.xpath(
                "//*[@role='tab' and normalize-space()="
                        + xpathLiteral(warehouse) + "]"));
        click(tab, "Chọn " + warehouse);
        waitForResult();
        wait.until(d -> elementText(d.findElement(By.tagName("main")))
                .toUpperCase().contains("TỔNG TỒN KHO"));
        return this;
    }

    /** Trả tên tab kho đang được chọn. */
    public String selectedWarehouse() {
        return visible(By.cssSelector("[role='tab'][aria-selected='true']"))
                .getText().trim();
    }

    /** Chuyển giữa dữ liệu Tồn kho và Phiếu. */
    public UniformInventoryPage selectSection(String section) {
        List<WebElement> controls = visibleElements(By.xpath(
                "//main//button[normalize-space()=" + xpathLiteral(section) + "]"));
        if (controls.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy section " + section);
        }
        click(controls.get(0), "Mở section " + section + " của " + selectedWarehouse());
        waitForResult();
        return this;
    }

    /** Chuyển giữa Lưới tháng và Danh sách tại section tồn kho. */
    public UniformInventoryPage selectViewMode(String mode) {
        WebElement control = visible(By.xpath(
                "//main//button[normalize-space()=" + xpathLiteral(mode) + "]"));
        click(control, "Chọn chế độ xem " + mode);
        waitForResult();
        return this;
    }

    /** Nhập mã lô tìm kiếm nếu ô tìm kiếm tồn kho đang hiển thị. */
    public UniformInventoryPage searchLot(String code) {
        WebElement input = visible(By.cssSelector(
                "main input[placeholder*='mã lô'],main input[placeholder*='Mã lô']"));
        fill(input, code, "Nhập mã lô " + code);
        input.sendKeys(Keys.ENTER);
        waitForResult();
        return this;
    }

    /** Chọn một loại phiếu trong danh sách phiếu. */
    public UniformInventoryPage selectReceiptType(String type) {
        List<WebElement> buttons = visibleElements(By.xpath(
                "//main//button[normalize-space()=" + xpathLiteral(type) + "]"));
        if (buttons.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy loại phiếu " + type);
        }
        click(buttons.get(buttons.size() - 1), "Lọc phiếu theo " + type);
        waitForResult();
        return this;
    }

    /** Mở dialog nghiệp vụ kho nhưng không xác nhận thay đổi dữ liệu. */
    public DialogSnapshot openActionDialog(String action) {
        List<WebElement> buttons = visibleElements(By.xpath(
                "//main//button[normalize-space()=" + xpathLiteral(action) + "]"));
        if (buttons.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy thao tác " + action);
        }
        click(buttons.get(0), "Mở thao tác " + action + " tại " + selectedWarehouse());
        WebElement overlay = visible(By.cssSelector(
                "[role='dialog'],[aria-label^='drawer-']"));
        pause("Hiển thị đầy đủ popup " + action);
        return new DialogSnapshot(action, elementText(overlay), confirmDisabled(overlay));
    }

    /**
     * Bấm submit khi form chưa có lô; form phải ở lại để người dùng bổ sung dữ liệu.
     * Thao tác này không thể tạo phiếu vì chưa có sản phẩm/lô.
     */
    public boolean submitEmptyDialogKeepsFormOpen() {
        WebElement overlay = visible(By.cssSelector(
                "[role='dialog'],[aria-label^='drawer-']"));
        WebElement submit = overlay.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> {
                    String text = button.getText().trim();
                    return text.equals("Xác nhận")
                            || text.equals("Xác nhận điều chỉnh")
                            || text.equals("Nhập kho")
                            || text.equals("Nhập kho tổng")
                            || text.equals("Xuất cho nhân sự")
                            || text.equals("Nhập về Kho bán hàng");
                })
                .findFirst().orElseThrow();
        if (!submit.isEnabled()) {
            return true;
        }
        click(submit, "Kiểm tra validation khi chưa chọn lô");
        settle(500);
        return !visibleElements(By.cssSelector(
                "[role='dialog'],[aria-label^='drawer-']")).isEmpty();
    }

    /** Kiểm tra các nút nghiệp vụ đang có trên kho hiện tại. */
    public boolean hasAction(String action) {
        return !visibleElements(By.xpath(
                "//main//button[normalize-space()=" + xpathLiteral(action) + "]"))
                .isEmpty();
    }

    /** Lấy mã lô đầu tiên đang xuất hiện trên section tồn kho. */
    public String firstLotCode() {
        String text = mainText();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\b(?:SP|LO|LÔ)[-_]?\\d+\\b",
                        java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

    /** Trả danh sách tiêu đề cột đang hiển thị. */
    public List<String> visibleHeaders() {
        return visibleElements(By.cssSelector(
                "main th,main [role='columnheader']"))
                .stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private boolean confirmDisabled(WebElement overlay) {
        return overlay.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> {
                    String text = button.getText().trim();
                    return text.equals("Xác nhận")
                            || text.equals("Xác nhận điều chỉnh")
                            || text.equals("Nhập kho")
                            || text.equals("Nhập kho tổng")
                            || text.equals("Xuất cho nhân sự")
                            || text.equals("Nhập về Kho bán hàng");
                })
                .findFirst()
                .map(button -> !button.isEnabled())
                .orElse(false);
    }

    /** Nội dung và trạng thái nút xác nhận của popup nghiệp vụ kho. */
    public record DialogSnapshot(String action, String text, boolean confirmDisabled) {
    }
}
