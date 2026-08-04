package com.vuatho.pages;

import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page Object cho nghiệp vụ Kho tổng → Nhập kho tổng. */
public class StockReceiptPage extends UniformUiPage {
    private static final String ROUTE = "/vuatho/inventory-uniform?tab=main";
    private static final By DIALOG = By.xpath(
            "//section[@role='dialog'][.//*[normalize-space()='Tạo phiếu nhập kho tổng']]");
    private static final By PRODUCT_OPTIONS = By.cssSelector("[role='option']");
    private static final By RECEIPT_ROWS = By.xpath(
            "//main//*[normalize-space()='Mã phiếu']"
                    + "/ancestor::div[contains(@class,'grid')][1]"
                    + "/following-sibling::div[1]/div[contains(@class,'grid')]");
    private static final Pattern VALID_COUNTER = Pattern.compile(
            "Lô hợp lệ:\\s*(\\d+)\\s*/\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL_QUANTITY = Pattern.compile(
            "Tổng SL:\\s*([\\d.,]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL_AMOUNT = Pattern.compile(
            "Tổng:\\s*([\\d.,]+)\\s*đ", Pattern.CASE_INSENSITIVE);
    private static final Pattern RECEIPT_CODE = Pattern.compile(
            "\\bNK-\\d{4}-\\d{3,}\\b", Pattern.CASE_INSENSITIVE);

    public StockReceiptPage(WebDriver driver) {
        super(driver);
    }

    /** Mở form Nhập kho tổng từ tab Tồn kho. */
    public StockReceiptPage openForm() {
        openRoute(ROUTE);
        click(exactMainButton("Nhập kho"), "Mở form Nhập kho tổng");
        visible(DIALOG);
        pause("Quan sát toàn bộ form Nhập kho tổng");
        return this;
    }

    /** Đọc trạng thái mặc định và các điều khiển chính. */
    public FormSnapshot formSnapshot() {
        openForm();
        WebElement date = field("Ngày nhập");
        WebElement note = field("Ghi chú");
        boolean dateRequired = date.getAttribute("required") != null
                || "true".equals(date.getAttribute("aria-required"))
                || elementText(dialog()).contains("Ngày nhập*");
        return new FormSnapshot(
                date.getAttribute("value"),
                dateRequired,
                note.getAttribute("value"),
                dialog().findElements(By.cssSelector(
                        "input[role='combobox'][aria-label='Thêm sản phẩm']")).size() == 1,
                productCount(), rowCount(), summary(), !submitButton().isEnabled(),
                TextNormalizer.normalize(elementText(dialog()))
                        .contains("chua co san pham nao"));
    }

    public boolean cancelForm() {
        openForm();
        click(dialogButton("Hủy"), "Hủy tạo phiếu Nhập kho tổng");
        return wait.until(d -> !dialogVisible());
    }

    public boolean closeForm() {
        openForm();
        WebElement close = dialog().findElement(By.cssSelector(
                "button[aria-label='Close']"));
        click(close, "Đóng form Nhập kho tổng bằng dấu X");
        return wait.until(d -> !dialogVisible());
    }

    public SearchSnapshot searchUnknownProduct() {
        openForm();
        WebElement combo = productCombo();
        String keyword = "__automation_product_not_found__";
        fill(combo, keyword, "Tìm sản phẩm không tồn tại");
        settle(800);
        pause("Quan sát danh sách gợi ý không có sản phẩm");
        return new SearchSnapshot(keyword, combo.getAttribute("value"), optionTexts());
    }

    /** Cuộn trong listbox và chọn một sản phẩm nằm ở cuối danh sách. */
    public ProductListSelectionSnapshot scrollToBottomAndSelectProduct() {
        openForm();
        WebElement combo = productCombo();
        click(combo, "Mở danh sách sản phẩm có thể cuộn");
        wait.until(d -> !visibleElements(PRODUCT_OPTIONS).isEmpty());
        List<WebElement> options = visibleElements(PRODUCT_OPTIONS);
        WebElement scrollContainer = productListScrollContainer(options.get(0));
        long maximumScroll = maximumScroll(scrollContainer);
        scrollProductList(scrollContainer, maximumScroll,
                "Cuộn xuống cuối danh sách sản phẩm");

        List<WebElement> refreshedOptions = visibleElements(PRODUCT_OPTIONS);
        WebElement bottomOption = refreshedOptions.get(refreshedOptions.size() - 1);
        String selectedName = elementText(bottomOption).trim();
        clickFreshProductOption(selectedName, bottomOption,
                "Chọn sản phẩm phía dưới " + selectedName);
        wait.until(d -> productCount() == 1);
        scrollToLastRow("Cuộn quan sát dòng nhập kho của sản phẩm phía dưới");
        return new ProductListSelectionSnapshot(
                maximumScroll, selectedName, productCount(), rowCount());
    }

    /** Cuộn hai chiều trong listbox và kiểm tra nội dung không bị mất. */
    public ProductListRoundTripSnapshot scrollProductListDownAndBackUp() {
        openForm();
        WebElement combo = productCombo();
        click(combo, "Mở danh sách sản phẩm để cuộn hai chiều");
        wait.until(d -> !visibleElements(PRODUCT_OPTIONS).isEmpty());
        List<WebElement> initialOptions = visibleElements(PRODUCT_OPTIONS);
        int initialCount = initialOptions.size();
        String initialValue = combo.getAttribute("value");
        WebElement scrollContainer = productListScrollContainer(initialOptions.get(0));
        long maximumScroll = maximumScroll(scrollContainer);

        scrollProductList(scrollContainer, maximumScroll,
                "Cuộn xuống cuối danh sách sản phẩm");
        long bottomPosition = scrollPosition(scrollContainer);
        scrollProductList(scrollContainer, 0,
                "Cuộn lên lại đầu danh sách sản phẩm");
        long returnedPosition = scrollPosition(scrollContainer);
        List<WebElement> returnedOptions = visibleElements(PRODUCT_OPTIONS);
        if (!returnedOptions.isEmpty()) {
            highlight(returnedOptions.get(0));
            pause("Quan sát option đầu danh sách sau khi cuộn trở lại");
        }
        return new ProductListRoundTripSnapshot(
                maximumScroll, bottomPosition, returnedPosition,
                initialCount, returnedOptions.size(), initialValue,
                combo.getAttribute("value"), !returnedOptions.isEmpty());
    }

    /** Tìm bằng từ khóa có dữ liệu và chọn kết quả đầu tiên. */
    public ExistingProductSearchSnapshot searchAndSelectExistingProduct() {
        openForm();
        String keyword = "AUTO";
        WebElement combo = productCombo();
        fill(combo, keyword, "Tìm sản phẩm tồn tại bằng từ khóa " + keyword);
        wait.until(d -> !visibleElements(PRODUCT_OPTIONS).isEmpty());
        List<WebElement> options = visibleElements(PRODUCT_OPTIONS);
        int optionCount = options.size();
        WebElement option = options.get(0);
        String selectedName = elementText(option).trim();
        clickFreshProductOption(selectedName, option,
                "Chọn sản phẩm tồn tại " + selectedName);
        wait.until(d -> productCount() == 1);
        scrollToLastRow("Quan sát sản phẩm tìm kiếm đã được thêm");
        return new ExistingProductSearchSnapshot(
                keyword, optionCount, selectedName, productCount(), rowCount());
    }

    /** Xóa từ khóa không có kết quả và kiểm tra gợi ý được khôi phục. */
    public SearchResetSnapshot clearSearchRestoresSuggestions() {
        openForm();
        WebElement combo = productCombo();
        click(combo, "Mở danh sách gợi ý ban đầu");
        wait.until(d -> !visibleElements(PRODUCT_OPTIONS).isEmpty());
        int initialCount = visibleElements(PRODUCT_OPTIONS).size();
        String unknown = "__automation_product_not_found__";
        fill(combo, unknown, "Nhập từ khóa không có kết quả");
        wait.until(d -> visibleElements(PRODUCT_OPTIONS).isEmpty());
        pause("Quan sát danh sách không có gợi ý");
        clearInput(combo, "Xóa toàn bộ từ khóa tìm kiếm");
        wait.until(d -> !visibleElements(PRODUCT_OPTIONS).isEmpty());
        int restoredCount = visibleElements(PRODUCT_OPTIONS).size();
        highlight(visibleElements(PRODUCT_OPTIONS).get(0));
        pause("Quan sát danh sách gợi ý đã khôi phục");
        return new SearchResetSnapshot(
                initialCount, restoredCount, combo.getAttribute("value"),
                !visibleElements(PRODUCT_OPTIONS).isEmpty());
    }

    /** Dùng Arrow Down và Enter để chọn option trong combobox. */
    public KeyboardSelectionSnapshot selectProductWithKeyboard() {
        openForm();
        WebElement combo = productCombo();
        fill(combo, "AUTO", "Tìm sản phẩm trước khi chọn bằng bàn phím");
        wait.until(d -> !visibleElements(PRODUCT_OPTIONS).isEmpty());
        int optionCount = visibleElements(PRODUCT_OPTIONS).size();
        highlight(combo);
        combo.sendKeys(Keys.ARROW_DOWN);
        pause("Di chuyển focus xuống option đầu tiên");
        combo.sendKeys(Keys.ENTER);
        pause("Chọn option đang focus bằng phím Enter");
        wait.until(d -> productCount() == 1);
        scrollToLastRow("Quan sát sản phẩm được thêm bằng bàn phím");
        return new KeyboardSelectionSnapshot(optionCount, productCount(), rowCount());
    }

    /** Chọn sản phẩm không biến thể và đọc số dòng được tạo. */
    public VariantProductSnapshot addProductWithoutVariant() {
        openForm();
        String selectedName = addProductMatching("Nón bảo hiểm nửa đầu");
        String cardText = selectedProductCardText(0);
        scrollToLastRow("Quan sát sản phẩm không biến thể và dòng nhập kho");
        return new VariantProductSnapshot(
                selectedName, rowCount(), cardText,
                TextNormalizer.normalize(cardText).contains("khong bien the"));
    }

    /** Chọn dữ liệu test nhiều biến thể và đọc thuộc tính trên card. */
    public VariantProductSnapshot addProductWithMultipleVariants() {
        WebElement card = prepareBulkProduct();
        String cardText = elementText(card);
        int rows = variantRows(card).size();
        String selectedName = card.findElements(By.xpath(
                        ".//p[contains(@class,'font-bold')][1]"))
                .stream().map(this::elementText).filter(text -> !text.isBlank())
                .findFirst().orElse(cardText);
        scrollToLastRow("Cuộn quan sát các dòng của sản phẩm nhiều biến thể");
        String normalized = TextNormalizer.normalize(cardText);
        boolean metadataVisible = normalized.contains("size")
                || normalized.contains("kich thuoc")
                || normalized.contains("mau sac");
        return new VariantProductSnapshot(
                selectedName, rows, cardText, metadataVisible);
    }

    /** Chọn chip lô cũ và đọc mã lô, giá được tự điền. */
    public OldLotSnapshot selectOldLotAutofillsValues() {
        openForm();
        addProductMatching("Nón bảo hiểm nửa đầu");
        List<WebElement> oldLots = visibleElements(By.cssSelector(
                "button[title^='Giá nhập cũ:']"));
        if (oldLots.isEmpty()) {
            throw new IllegalStateException("Sản phẩm test không có lô cũ để chọn.");
        }
        WebElement oldLot = oldLots.get(0);
        String oldLotText = elementText(oldLot).trim();
        String expectedCode = oldLotText.split("\\s|\u00b7")[0].trim();
        long expectedPrice = longNumber(oldLot.getAttribute("title"));
        click(oldLot, "Chọn lô cũ " + expectedCode);
        wait.until(d -> expectedCode.equals(rowInput(0, "Mã lô").getAttribute("value")));
        String actualPriceText = rowInput(0, "Giá nhập / cái").getAttribute("value");
        long actualPrice = longNumber(actualPriceText);
        scrollToLastRow("Quan sát mã lô và giá cũ đã tự điền");
        return new OldLotSnapshot(
                expectedCode, rowInput(0, "Mã lô").getAttribute("value"),
                expectedPrice, actualPrice, actualPriceText);
    }

    /** Đọc các điều khiển Điền nhanh của card sản phẩm nhiều biến thể. */
    public BulkControlsSnapshot bulkFillControls() {
        WebElement card = prepareBulkProduct();
        WebElement quantity = bulkInput(card, "Số lượng");
        WebElement price = bulkInput(card, "Giá nhập / cái");
        WebElement apply = bulkApplyButton(card);
        observeElement(card, "Quan sát vùng Điền nhanh và tất cả dòng biến thể");
        return new BulkControlsSnapshot(
                variantRows(card).size(), quantity.isDisplayed(), price.isDisplayed(),
                apply.isDisplayed(), quantity.getAttribute("value"),
                price.getAttribute("value"));
    }

    /** Áp dụng riêng số lượng nhanh cho mọi dòng của một sản phẩm. */
    public BulkValuesSnapshot applyBulkQuantityToAllVariants() {
        WebElement card = prepareBulkProduct();
        setBulkValue(card, "Số lượng", "3", "Nhập nhanh số lượng 3");
        click(bulkApplyButton(card), "Áp dụng nhanh số lượng cho tất cả biến thể");
        waitForBulkValues(card, "Số lượng", 3);
        observeElement(card, "Quan sát tất cả biến thể nhận số lượng 3");
        return bulkValuesSnapshot(card);
    }

    /** Áp dụng riêng giá nhập nhanh cho mọi dòng của một sản phẩm. */
    public BulkValuesSnapshot applyBulkPriceToAllVariants() {
        WebElement card = prepareBulkProduct();
        setBulkValue(card, "Giá nhập / cái", "2000", "Nhập nhanh giá 2.000đ");
        click(bulkApplyButton(card), "Áp dụng nhanh giá cho tất cả biến thể");
        waitForBulkValues(card, "Giá nhập / cái", 2000);
        observeElement(card, "Quan sát tất cả biến thể nhận giá 2.000đ");
        return bulkValuesSnapshot(card);
    }

    /** Điền mã lô từng dòng, áp dụng nhanh số lượng/giá và đọc tổng. */
    public BulkSummarySnapshot applyBulkValuesAndCalculateSummary() {
        WebElement card = prepareBulkProduct();
        fillLotCodes(card, 120);
        applyBulkValues(card, 2, 15_000, "Áp dụng nhanh số lượng và giá nhập");
        Summary result = summary();
        scrollToFooter("Quan sát tổng số lượng và thành tiền sau điền nhanh");
        return new BulkSummarySnapshot(
                variantRows(card).size(), 2, 15_000, result, submitButton().isEnabled());
    }

    /** Áp dụng lần hai phải ghi đè giá trị lần đầu trên toàn bộ dòng. */
    public BulkOverwriteSnapshot reapplyBulkValuesOverwritesAllRows() {
        WebElement card = prepareBulkProduct();
        applyBulkValues(card, 1, 1000, "Áp dụng điền nhanh lần đầu");
        BulkValuesSnapshot first = bulkValuesSnapshot(card);
        applyBulkValues(card, 4, 2500, "Áp dụng lại điền nhanh với giá trị mới");
        BulkValuesSnapshot second = bulkValuesSnapshot(card);
        return new BulkOverwriteSnapshot(first, second);
    }

    /** Sau điền nhanh, sửa một dòng không được làm thay đổi các dòng còn lại. */
    public ManualOverrideSnapshot manualRowOverrideAfterBulkFill() {
        WebElement card = prepareBulkProduct();
        applyBulkValues(card, 2, 1000, "Điền nhanh dữ liệu nền cho tất cả biến thể");
        List<WebElement> rows = variantRows(card);
        int changedIndex = Math.min(1, rows.size() - 1);
        WebElement quantity = rows.get(changedIndex).findElement(
                By.cssSelector("input[aria-label='Số lượng']"));
        fill(quantity, "9", "Sửa thủ công số lượng một biến thể thành 9");
        settle(300);
        List<Integer> values = rowNumbers(card, "Số lượng");
        observeElement(rows.get(changedIndex), "Quan sát duy nhất dòng được sửa thủ công");
        return new ManualOverrideSnapshot(changedIndex, 2, 9, values);
    }

    /** Điền nhanh trong card đầu tiên không được lan sang card sản phẩm khác. */
    public BulkIsolationSnapshot bulkFillDoesNotAffectOtherProduct() {
        openForm();
        addProductMatching("Đồ bảo hộ");
        addAnyUnselectedProduct();
        List<WebElement> productCards = selectedProductCards();
        if (productCards.size() < 2 || bulkCards().isEmpty()) {
            return new BulkIsolationSnapshot(false, List.of(), List.of());
        }
        WebElement first = bulkCards().get(0);
        WebElement second = productCards.stream()
                .filter(card -> !card.equals(first))
                .findFirst()
                .orElse(productCards.get(1));
        applyBulkValues(first, 5, 3000,
                "Điền nhanh chỉ card sản phẩm thứ nhất");
        observeElement(second, "Quan sát card thứ hai vẫn chưa nhận dữ liệu điền nhanh");
        return new BulkIsolationSnapshot(
                true, rowNumbers(first, "Số lượng"), rowNumbers(second, "Số lượng"));
    }

    /** Các định dạng số lượng nhanh không hợp lệ không được đổ xuống từng dòng. */
    public List<InvalidBulkSnapshot> invalidBulkQuantitiesAreNotApplied() {
        WebElement card = prepareBulkProduct();
        List<InvalidBulkSnapshot> results = new ArrayList<>();
        for (String attempted : List.of("0", "-1", "1.5", "abc")) {
            clearVariantField(card, "Số lượng");
            setBulkValue(card, "Số lượng", attempted,
                    "Thử số lượng điền nhanh không hợp lệ " + attempted);
            click(bulkApplyButton(card),
                    "Thử áp dụng số lượng nhanh " + attempted);
            settle(300);
            List<Integer> quantities = rowNumbers(card, "Số lượng");
            results.add(new InvalidBulkSnapshot(
                    attempted, bulkInput(card, "Số lượng").getAttribute("value"), quantities));
            observeElement(card, "Quan sát các dòng sau giá trị không hợp lệ " + attempted);
        }
        return results;
    }

    /** Xóa một biến thể sau điền nhanh phải cập nhật số dòng và tổng. */
    public BulkRemoveSnapshot removeVariantAfterBulkFill() {
        WebElement card = prepareBulkProduct();
        fillLotCodes(card, 140);
        applyBulkValues(card, 2, 1000, "Điền nhanh trước khi xóa biến thể");
        int beforeRows = variantRows(card).size();
        Summary beforeSummary = summary();
        WebElement remove = card.findElement(By.cssSelector(
                "button[title='Xoá biến thể này']"));
        click(remove, "Xóa một biến thể sau khi đã điền nhanh");
        wait.until(d -> variantRows(card).size() == beforeRows - 1);
        Summary afterSummary = summary();
        scrollToFooter("Quan sát bộ đếm và tổng sau khi xóa biến thể");
        return new BulkRemoveSnapshot(
                beforeRows, variantRows(card).size(), beforeSummary, afterSummary);
    }

    /** Mã lô nhập riêng kết hợp Điền nhanh phải tạo trạng thái form hợp lệ. */
    public BulkSummarySnapshot lotCodesCombinedWithBulkFillEnableSubmission() {
        WebElement card = prepareBulkProduct();
        fillLotCodes(card, 160);
        applyBulkValues(card, 3, 1200,
                "Kết hợp mã lô riêng với Điền nhanh");
        Summary result = summary();
        scrollToFooter("Quan sát nút Nhập kho tổng được bật");
        return new BulkSummarySnapshot(
                variantRows(card).size(), 3, 1200, result, submitButton().isEnabled());
    }

    /** Chọn lần lượt hai chip lô cũ của cùng dòng và đọc giá trị cuối. */
    public OldLotSwitchSnapshot switchBetweenOldLots() {
        WebElement card = prepareBulkProduct();
        WebElement firstRow = variantRows(card).get(0);
        List<WebElement> chips = firstRow.findElements(By.cssSelector(
                "button[title^='Giá nhập cũ:']"));
        if (chips.size() < 2) {
            return new OldLotSwitchSnapshot(false, "", "", "", 0, 0);
        }
        String firstCode = oldLotCode(chips.get(0));
        long firstPrice = longNumber(chips.get(0).getAttribute("title"));
        click(chips.get(0), "Chọn lô cũ thứ nhất " + firstCode);
        String secondCode = oldLotCode(chips.get(1));
        long secondPrice = longNumber(chips.get(1).getAttribute("title"));
        click(chips.get(1), "Chuyển sang lô cũ thứ hai " + secondCode);
        WebElement code = firstRow.findElement(By.cssSelector("input[aria-label='Mã lô']"));
        WebElement price = firstRow.findElement(By.cssSelector(
                "input[aria-label='Giá nhập / cái']"));
        wait.until(d -> secondCode.equals(code.getAttribute("value")));
        observeElement(firstRow, "Quan sát mã lô và giá sau khi chuyển chip lô cũ");
        return new OldLotSwitchSnapshot(
                true, firstCode, secondCode, code.getAttribute("value"),
                secondPrice, longNumber(price.getAttribute("value")));
    }

    /** Gỡ sản phẩm đầu tiên và giữ nguyên sản phẩm thứ hai. */
    public RemoveOneOfTwoSnapshot removeOneOfTwoProducts() {
        openForm();
        ProductSnapshot first = addAvailableProduct();
        ProductSnapshot second = addAvailableProduct();
        int rowsBefore = rowCount();
        List<WebElement> removeButtons = removeProductButtons();
        click(removeButtons.get(0), "Gỡ sản phẩm đầu tiên " + first.name());
        wait.until(d -> productCount() == 1);
        String remainingText = elementText(dialog());
        scrollToLastRow("Quan sát sản phẩm thứ hai vẫn còn sau khi gỡ sản phẩm đầu");
        return new RemoveOneOfTwoSnapshot(
                first.name(), second.name(), rowsBefore, rowCount(), productCount(),
                remainingText.contains(second.name()), remainingText.contains(first.name()));
    }

    /** Cuộn xuống và lên trong vùng chứa các dòng nhập kho. */
    public RowListScrollSnapshot scrollLongRowListDownAndBackUp() {
        openForm();
        WebElement scrollContainer = rowListContainer();
        int attempts = 0;
        while ((maximumScroll(scrollContainer) <= 0 || rowCount() < 2)
                && attempts++ < 10) {
            addAnyUnselectedProduct();
            scrollContainer = rowListContainer();
        }
        List<WebElement> rows = dialog().findElements(By.cssSelector("input[aria-label='Mã lô']"));
        long maximumScroll = maximumScroll(scrollContainer);
        scrollProductList(scrollContainer, maximumScroll,
                "Cuộn xuống cuối danh sách dòng nhập kho");
        long bottomPosition = scrollPosition(scrollContainer);
        scrollProductList(scrollContainer, 0,
                "Cuộn lên lại đầu danh sách dòng nhập kho");
        long returnedPosition = scrollPosition(scrollContainer);
        highlight(rows.get(0));
        pause("Quan sát dòng đầu tiên sau khi cuộn trở lại");
        return new RowListScrollSnapshot(
                maximumScroll, bottomPosition, returnedPosition, rowCount(), productCount());
    }

    public ProductSnapshot addFirstProduct() {
        if (!dialogVisible()) {
            openForm();
        }
        return addAvailableProduct();
    }

    public MultiProductSnapshot addTwoProducts() {
        openForm();
        ProductSnapshot first = addAvailableProduct();
        ProductSnapshot second = addAvailableProduct();
        return new MultiProductSnapshot(
                first, second, productCount(), rowCount(), summary());
    }

    public RemoveProductSnapshot addAndRemoveProduct() {
        openForm();
        ProductSnapshot added = addAvailableProduct();
        List<WebElement> removeButtons = removeProductButtons();
        click(removeButtons.get(0), "Gỡ sản phẩm " + added.name() + " khỏi phiếu");
        wait.until(d -> productCount() == 0);
        pause("Quan sát form rỗng sau khi gỡ sản phẩm");
        return new RemoveProductSnapshot(
                productCount(), rowCount(), summary(), !submitButton().isEnabled());
    }

    public RemoveVariantSnapshot removeOneVariantRow() {
        openForm();
        addAvailableProduct();
        int before = rowCount();
        List<WebElement> removeButtons = visibleElements(By.cssSelector(
                "button[title='Xoá biến thể này']"));
        if (removeButtons.isEmpty()) {
            throw new IllegalStateException("Sản phẩm đã thêm không có nút Xoá biến thể này.");
        }
        click(removeButtons.get(0), "Xóa một dòng biến thể khỏi sản phẩm nhập kho");
        wait.until(d -> rowCount() == before - 1);
        pause("Quan sát số dòng lô sau khi xóa biến thể");
        return new RemoveVariantSnapshot(before, rowCount(), summary());
    }

    public DuplicateProductSnapshot selectedProductIsExcluded() {
        openForm();
        ProductSnapshot selected = addAvailableProduct();
        WebElement combo = productCombo();
        fill(combo, selected.name(), "Tìm lại sản phẩm đã thêm");
        settle(800);
        List<String> options = optionTexts();
        pause("Quan sát sản phẩm đã chọn không xuất hiện lại trong gợi ý");
        return new DuplicateProductSnapshot(
                selected.name(), options, productCount(), rowCount());
    }

    public MetadataSnapshot enterManualDateAndNote() {
        openForm();
        LocalDate expectedDate = LocalDate.now().minusDays(1);
        WebElement date = field("Ngày nhập");
        setControlledValue(date, expectedDate.toString(),
                "Chọn thủ công ngày nhập " + expectedDate);
        String expectedNote = "Automation nhập kho có dấu";
        WebElement note = field("Ghi chú");
        fill(note, expectedNote, "Nhập ghi chú phiếu nhập kho");
        pause("Quan sát ngày và ghi chú đã nhập");
        return new MetadataSnapshot(
                expectedDate.toString(), date.getAttribute("value"),
                expectedNote, note.getAttribute("value"));
    }

    public ValidationSnapshot clearRequiredDate() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        WebElement date = field("Ngày nhập");
        setControlledValue(date, "", "Xóa ngày nhập bắt buộc");
        boolean enabledBeforeClick = submitButton().isEnabled();
        if (enabledBeforeClick) {
            click(submitButton(), "Thử Nhập kho tổng khi thiếu ngày nhập");
            settle(500);
        }
        boolean confirmationOpened = !visibleElements(By.xpath(
                "//section[@role='dialog']//button[normalize-space()='Xác nhận']"))
                .isEmpty();
        boolean blocked = dialogVisible() && !confirmationOpened;
        return new ValidationSnapshot(
                "", date.getAttribute("value"), summary(),
                enabledBeforeClick, elementText(dialog()), blocked);
    }

    public ValidationSnapshot leaveLotCodeBlank() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        setRowValue(0, "Mã lô", "", "Để trống mã lô");
        return validationSnapshot("", rowInput(0, "Mã lô").getAttribute("value"));
    }

    /** Bỏ trống riêng số lượng trong khi các trường còn lại vẫn hợp lệ. */
    public ValidationSnapshot leaveQuantityBlank() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        setRowValue(0, "Số lượng", "", "Để trống riêng số lượng");
        return validationSnapshot(
                "", rowInput(0, "Số lượng").getAttribute("value"));
    }

    /** Xóa đồng thời ngày nhập và toàn bộ dữ liệu bắt buộc của dòng đầu tiên. */
    public RequiredFieldsSnapshot leaveAllRequiredFieldsBlank() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        WebElement date = field("Ngày nhập");
        setControlledValue(date, "", "Để trống ngày nhập");
        setRowValue(0, "Mã lô", "", "Để trống mã lô");
        setRowValue(0, "Số lượng", "", "Để trống số lượng");
        setRowValue(0, "Giá nhập / cái", "", "Để trống giá nhập");
        scrollToFooter("Cuộn cuối form quan sát trạng thái khi bỏ trống toàn bộ trường bắt buộc");
        return new RequiredFieldsSnapshot(
                date.getAttribute("value"),
                rowInput(0, "Mã lô").getAttribute("value"),
                rowInput(0, "Số lượng").getAttribute("value"),
                rowInput(0, "Giá nhập / cái").getAttribute("value"),
                summary(), submitButton().isEnabled());
    }

    public ValidationSnapshot whitespaceQuantityIsBlocked() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        setRowValue(0, "Số lượng", "   ",
                "Nhập số lượng chỉ có khoảng trắng");
        return validationSnapshot(
                "   ", rowInput(0, "Số lượng").getAttribute("value"));
    }

    public ValidationSnapshot whitespacePriceIsBlocked() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        setRowValue(0, "Giá nhập / cái", "   ",
                "Nhập giá chỉ có khoảng trắng");
        return validationSnapshot(
                "   ", rowInput(0, "Giá nhập / cái").getAttribute("value"));
    }

    /** Điền lại từng trường bắt buộc và ghi nhận trạng thái nút sau mỗi bước. */
    public RequiredFieldsRecoverySnapshot restoreAllRequiredFieldsOneByOne() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        WebElement date = field("Ngày nhập");
        setControlledValue(date, "", "Xóa ngày nhập để tạo trạng thái lỗi");
        setRowValue(0, "Mã lô", "", "Xóa mã lô để tạo trạng thái lỗi");
        setRowValue(0, "Số lượng", "", "Xóa số lượng để tạo trạng thái lỗi");
        setRowValue(0, "Giá nhập / cái", "", "Xóa giá nhập để tạo trạng thái lỗi");
        boolean enabledBeforeRecovery = submitButton().isEnabled();

        setControlledValue(date, LocalDate.now().toString(), "Điền lại ngày nhập");
        boolean enabledAfterDate = submitButton().isEnabled();
        setRowValue(0, "Mã lô", uniqueLotCode(340), "Điền lại mã lô");
        boolean enabledAfterLot = submitButton().isEnabled();
        setRowValue(0, "Số lượng", "2", "Điền lại số lượng");
        boolean enabledAfterQuantity = submitButton().isEnabled();
        setRowValue(0, "Giá nhập / cái", "1500", "Điền lại giá nhập cuối cùng");
        scrollToFooter("Cuộn cuối form quan sát nút bật sau khi điền đủ trường bắt buộc");
        return new RequiredFieldsRecoverySnapshot(
                enabledBeforeRecovery, enabledAfterDate, enabledAfterLot,
                enabledAfterQuantity, submitButton().isEnabled(), summary());
    }

    public ValidationSnapshot acceptsPositiveIntegerQuantity() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        setRowValue(0, "Số lượng", "2", "Nhập số lượng nguyên dương 2");
        return validationSnapshot("2", rowInput(0, "Số lượng").getAttribute("value"));
    }

    public List<ValidationSnapshot> invalidQuantityFormats() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        List<ValidationSnapshot> results = new ArrayList<>();
        for (String attempted : List.of("0", "-1", "1.5", "abc")) {
            setRowValue(0, "Số lượng", attempted,
                    "Thử số lượng không hợp lệ " + attempted);
            results.add(validationSnapshot(
                    attempted, rowInput(0, "Số lượng").getAttribute("value")));
        }
        return results;
    }

    public List<ValidationSnapshot> invalidPriceFormats() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        List<ValidationSnapshot> results = new ArrayList<>();
        for (String attempted : List.of("", "-1", "abc")) {
            setRowValue(0, "Giá nhập / cái", attempted,
                    "Thử giá nhập không hợp lệ " + (attempted.isEmpty() ? "trống" : attempted));
            results.add(validationSnapshot(
                    attempted, rowInput(0, "Giá nhập / cái").getAttribute("value")));
        }
        return results;
    }

    public ValidationSnapshot duplicateLotCodesAreBlocked() {
        openForm();
        addAvailableProduct();
        if (rowCount() < 2) {
            addAvailableProduct();
        }
        fillAllRowsValid();
        String duplicate = uniqueLotCode(77);
        setRowValue(0, "Mã lô", duplicate, "Nhập mã lô trùng thứ nhất");
        setRowValue(1, "Mã lô", duplicate, "Nhập cùng mã lô cho dòng thứ hai");
        return validationSnapshot(duplicate, rowInput(1, "Mã lô").getAttribute("value"));
    }

    public CompletionSnapshot allRowsMustBeValid() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        int incompleteIndex = rowCount() - 1;
        setRowValue(incompleteIndex, "Số lượng", "",
                "Xóa số lượng của dòng cuối để kiểm tra form chưa hoàn tất");
        Summary incomplete = summary();
        boolean enabledBefore = submitButton().isEnabled();
        fillRowValid(incompleteIndex, uniqueLotCode(20), 1, 1000);
        Summary completed = summary();
        pause("Quan sát tất cả dòng đã hợp lệ và nút nhập kho được bật");
        return new CompletionSnapshot(
                enabledBefore, submitButton().isEnabled(), incomplete, completed);
    }

    public TotalsSnapshot calculatedTotals() {
        openForm();
        addAvailableProduct();
        if (rowCount() < 2) {
            addAvailableProduct();
        }
        int rows = rowCount();
        for (int index = 0; index < rows; index++) {
            fillRowValid(index, uniqueLotCode(index + 30), 2, 15_000);
        }
        Summary summary = summary();
        scrollToFooter("Cuộn xuống quan sát tổng số lượng và thành tiền");
        return new TotalsSnapshot(rows, rows * 2, rows * 30_000L, summary);
    }

    public ValidationSnapshot blankNoteIsAccepted() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        WebElement note = field("Ghi chú");
        setControlledValue(note, "", "Để trống ghi chú tùy chọn");
        return validationSnapshot("", note.getAttribute("value"));
    }

    public ValidationSnapshot whitespaceLotCodeIsBlocked() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        setRowValue(0, "Mã lô", "   ", "Nhập mã lô chỉ có khoảng trắng");
        return validationSnapshot("   ", rowInput(0, "Mã lô").getAttribute("value"));
    }

    public List<ValidationSnapshot> zeroPriceAndDecimalPriceResults() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        List<ValidationSnapshot> results = new ArrayList<>();
        for (String attempted : List.of("0", "1.5")) {
            setRowValue(0, "Giá nhập / cái", attempted,
                    "Thử giá nhập không hợp lệ " + attempted);
            results.add(validationSnapshot(
                    attempted, rowInput(0, "Giá nhập / cái").getAttribute("value")));
        }
        return results;
    }

    public LongNoteSnapshot acceptsLongUnicodeNote() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        String expected = "Kiểm kê kho đồng phục - áôứộêđ - !@#$%^&*() <> "
                + "ghi chú tự động ".repeat(12).trim();
        WebElement note = field("Ghi chú");
        fill(note, expected, "Nhập ghi chú dài có tiếng Việt và ký tự đặc biệt");
        pause("Quan sát nội dung ghi chú dài được giữ nguyên");
        return new LongNoteSnapshot(
                expected, note.getAttribute("value"), note.getAttribute("maxlength"),
                submitButton().isEnabled());
    }

    /** Kiểm tra ngày tương lai là dữ liệu hợp lệ theo nghiệp vụ nhập kho. */
    public ValidationSnapshot acceptsFutureImportDate() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        String expected = LocalDate.now().plusDays(7).toString();
        WebElement date = field("Ngày nhập");
        setControlledValue(date, expected, "Chọn ngày nhập trong tương lai " + expected);
        return validationSnapshot(expected, date.getAttribute("value"));
    }

    /** Kiểm tra form không tự giới hạn hoặc cắt ngắn mã lô. */
    public LongLotCodeSnapshot acceptsLongLotCode() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        String expected = "LO-DAI-" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-".repeat(8);
        WebElement lot = rowInput(0, "Mã lô");
        setRowValue(0, "Mã lô", expected, "Nhập mã lô dài " + expected.length() + " ký tự");
        return new LongLotCodeSnapshot(
                expected, lot.getAttribute("value"), lot.getAttribute("maxlength"),
                summary(), submitButton().isEnabled());
    }

    /** Kiểm tra mã lô chấp nhận Unicode và các ký tự đặc biệt. */
    public ValidationSnapshot acceptsUnicodeAndSpecialLotCode() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        String expected = "LÔ-ÁO_ĐỒNG-PHỤC.!@#$%^&()[]{}+=_2026";
        setRowValue(0, "Mã lô", expected,
                "Nhập mã lô Unicode và ký tự đặc biệt");
        return validationSnapshot(expected, rowInput(0, "Mã lô").getAttribute("value"));
    }

    /** Kiểm tra chuỗi giống HTML không được thực thi khi nhập vào mã lô. */
    public TextSafetySnapshot htmlLikeLotCodeRemainsPlainText() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        String expected = "<script>window.__stockReceiptLotExecuted=true</script>";
        ((JavascriptExecutor) driver).executeScript(
                "delete window.__stockReceiptLotExecuted;");
        setRowValue(0, "Mã lô", expected,
                "Nhập nội dung giống HTML vào mã lô");
        Object marker = ((JavascriptExecutor) driver).executeScript(
                "return window.__stockReceiptLotExecuted || null;");
        return new TextSafetySnapshot(
                expected, rowInput(0, "Mã lô").getAttribute("value"),
                marker != null, summary(), submitButton().isEnabled());
    }

    public ValidationSnapshot duplicateLotCodesIgnoringCaseAreBlocked() {
        return duplicateLotVariant("AUTO-LOT-CASE", "auto-lot-case",
                "Nhập hai mã lô chỉ khác chữ hoa chữ thường");
    }

    public ValidationSnapshot duplicateLotCodesIgnoringOuterWhitespaceAreBlocked() {
        return duplicateLotVariant("AUTO-LOT-SPACE", "  AUTO-LOT-SPACE  ",
                "Nhập hai mã lô chỉ khác khoảng trắng bao quanh");
    }

    /** Sửa mã lô bị trùng và kiểm tra toàn bộ form trở lại hợp lệ. */
    public RecoverySnapshot correctingDuplicateLotRestoresValidity() {
        openForm();
        addAvailableProduct();
        if (rowCount() < 2) {
            addAvailableProduct();
        }
        fillAllRowsValid();
        String duplicate = uniqueLotCode(310);
        setRowValue(0, "Mã lô", duplicate, "Nhập mã lô thứ nhất");
        setRowValue(1, "Mã lô", duplicate, "Tạo mã lô trùng ở dòng thứ hai");
        boolean enabledWhileInvalid = submitButton().isEnabled();
        Summary invalidSummary = summary();
        String corrected = uniqueLotCode(311);
        setRowValue(1, "Mã lô", corrected, "Sửa dòng thứ hai thành mã lô khác");
        pause("Quan sát form hợp lệ trở lại sau khi sửa mã lô trùng");
        return new RecoverySnapshot(
                enabledWhileInvalid, submitButton().isEnabled(),
                invalidSummary, summary(), corrected,
                rowInput(1, "Mã lô").getAttribute("value"));
    }

    /** Sửa lần lượt số lượng và giá không hợp lệ về dữ liệu hợp lệ. */
    public RecoverySnapshot correctingInvalidNumbersRestoresValidity() {
        openForm();
        addAvailableProduct();
        fillAllRowsValid();
        setRowValue(0, "Số lượng", "0", "Nhập số lượng không hợp lệ bằng 0");
        setRowValue(0, "Giá nhập / cái", "", "Để trống giá nhập");
        boolean enabledWhileInvalid = submitButton().isEnabled();
        Summary invalidSummary = summary();
        setRowValue(0, "Số lượng", "3", "Sửa số lượng thành 3");
        setRowValue(0, "Giá nhập / cái", "2500", "Sửa giá nhập thành 2500");
        pause("Quan sát form hợp lệ trở lại sau khi sửa số lượng và giá");
        return new RecoverySnapshot(
                enabledWhileInvalid, submitButton().isEnabled(),
                invalidSummary, summary(), "3/2500",
                rowInput(0, "Số lượng").getAttribute("value") + "/"
                        + rowInput(0, "Giá nhập / cái").getAttribute("value"));
    }

    /** Hai dòng cùng lỗi phải được sửa hết trước khi nút gửi được bật. */
    public MultiRowRecoverySnapshot correctsMultipleInvalidRowsOneByOne() {
        openForm();
        addAvailableProduct();
        if (rowCount() < 2) {
            addAvailableProduct();
        }
        fillAllRowsValid();
        setRowValue(0, "Mã lô", "", "Xóa mã lô dòng thứ nhất");
        setRowValue(1, "Số lượng", "", "Xóa số lượng dòng thứ hai");
        boolean enabledWithTwoErrors = submitButton().isEnabled();
        setRowValue(0, "Mã lô", uniqueLotCode(320), "Sửa lỗi dòng thứ nhất");
        boolean enabledWithOneError = submitButton().isEnabled();
        setRowValue(1, "Số lượng", "2", "Sửa lỗi còn lại ở dòng thứ hai");
        scrollToFooter("Cuộn cuối form quan sát nút nhập kho sau khi sửa hết lỗi");
        return new MultiRowRecoverySnapshot(
                enabledWithTwoErrors, enabledWithOneError,
                submitButton().isEnabled(), summary());
    }

    /** Kiểm tra phép tính với dữ liệu lớn vẫn chính xác trong miền số nguyên an toàn. */
    public TotalsSnapshot largeQuantityAndPriceTotals() {
        openForm();
        addAvailableProduct();
        int rows = rowCount();
        int quantity = 1_000_000;
        int price = 9_000_000;
        for (int index = 0; index < rows; index++) {
            fillRowValid(index, uniqueLotCode(330 + index), quantity, price);
        }
        Summary result = summary();
        scrollToFooter("Cuộn cuối form quan sát tổng tiền với số lượng và giá lớn");
        return new TotalsSnapshot(
                rows, rows * quantity, (long) rows * quantity * price, result);
    }

    private ValidationSnapshot duplicateLotVariant(
            String first, String second, String step) {
        openForm();
        addAvailableProduct();
        if (rowCount() < 2) {
            addAvailableProduct();
        }
        fillAllRowsValid();
        setRowValue(0, "Mã lô", first, step + " - dòng thứ nhất");
        setRowValue(1, "Mã lô", second, step + " - dòng thứ hai");
        return validationSnapshot(second, rowInput(1, "Mã lô").getAttribute("value"));
    }

    /** Tạo phiếu nhập kho thật và đối chiếu phiếu cùng lô vừa sinh. */
    public SubmissionSnapshot submitRealReceipt() {
        openForm();
        ProductSnapshot product = addAvailableProduct();
        List<String> codes = new ArrayList<>();
        for (int index = 0; index < rowCount(); index++) {
            String code = uniqueLotCode(index + 50);
            codes.add(code);
            fillRowValid(index, code, 1, 1000);
        }
        Summary submittedSummary = summary();
        String note = "Automation tạo phiếu nhập kho thật";
        fill(field("Ghi chú"), note, "Nhập ghi chú nhận diện phiếu thật");
        scrollToFooter("Cuộn cuối form quan sát dữ liệu trước khi nhập kho");
        click(submitButton(), "Nhập kho tổng thật cho " + product.name());
        settle(500);
        List<WebElement> confirmations = visibleElements(By.xpath(
                "//section[@role='dialog']//button[normalize-space()='Xác nhận']"));
        if (!confirmations.isEmpty()) {
            click(confirmations.get(confirmations.size() - 1),
                    "Xác nhận tạo phiếu Nhập kho tổng thật");
        }
        wait.until(d -> !dialogVisible());
        pause("Quan sát danh sách sau khi tạo phiếu nhập kho thành công");

        click(exactMainButton("Phiếu"), "Mở tab Phiếu để kiểm tra phiếu vừa tạo");
        wait.until(d -> TextNormalizer.normalize(mainText()).contains("ma phieu"));
        pause("Quan sát dữ liệu phiếu Nhập kho vừa tạo");
        String receiptText = mainText();
        boolean receiptContainsLot = codes.stream().allMatch(receiptText::contains);

        click(exactMainButton("Tồn kho"), "Quay lại Tồn kho để kiểm tra lô mới");
        wait.until(d -> !visibleElements(By.cssSelector(
                "input[placeholder='Tìm mã lô…']")).isEmpty());
        WebElement search = visible(By.cssSelector("input[placeholder='Tìm mã lô…']"));
        fill(search, codes.get(0), "Tìm lô vừa nhập kho " + codes.get(0));
        settle(900);
        pause("Quan sát tồn kho của lô vừa nhập");
        String stockText = mainText();
        boolean stockContainsLot = stockText.contains(codes.get(0));
        return new SubmissionSnapshot(
                product.name(), codes, submittedSummary,
                receiptContainsLot, stockContainsLot, stockText);
    }

    /** Tạo phiếu thật có hai sản phẩm và kiểm tra tất cả lô ở Phiếu/Tồn kho. */
    public MultiSubmissionSnapshot submitMultipleProductsReal() {
        openForm();
        MultiProductPreparation preparation = prepareValidMultipleProducts();
        int products = preparation.productCount();
        List<String> codes = preparation.lotCodes();
        Summary submittedSummary = preparation.summary();
        fill(field("Ghi chú"), "Automation nhập kho nhiều sản phẩm",
                "Nhập ghi chú phiếu nhiều sản phẩm");
        String receiptText = submitCurrentReceipt(false,
                "Nhập kho thật nhiều sản phẩm");
        boolean receiptContainsAll = codes.stream().allMatch(receiptText::contains);
        int stockMatches = verifyLotsInStock(codes);
        return new MultiSubmissionSnapshot(
                products, codes, submittedSummary,
                receiptContainsAll, stockMatches, receiptText);
    }

    /**
     * Tìm linh động hai sản phẩm khác nhau mà toàn bộ biến thể đều nhập kho được.
     * Dữ liệu nghiệp vụ có thể chứa sản phẩm/biến thể cấu hình dở dang, vì vậy
     * không gửi phiếu khi bộ đếm hợp lệ chưa bằng tổng số dòng.
     */
    private MultiProductPreparation prepareValidMultipleProducts() {
        Set<String> rejectedProducts = new LinkedHashSet<>();
        Summary lastSummary = summary();
        for (int attempt = 0; attempt < 4; attempt++) {
            ProductSnapshot first = addSubmissionProduct(rejectedProducts);
            Set<String> excludedForSecond = new LinkedHashSet<>(rejectedProducts);
            excludedForSecond.add(first.name());
            ProductSnapshot second = addSubmissionProduct(excludedForSecond);

            List<String> codes = fillRowsForSubmission(1, 1000, 60 + attempt * 20);
            lastSummary = summary();
            if (productCount() == 2
                    && lastSummary.totalLots() == rowCount()
                    && lastSummary.validLots() == lastSummary.totalLots()) {
                return new MultiProductPreparation(
                        productCount(), codes, lastSummary);
            }

            rejectedProducts.add(first.name());
            rejectedProducts.add(second.name());
            pause("Bỏ bộ sản phẩm có biến thể chưa hợp lệ "
                    + lastSummary.validLots() + "/" + lastSummary.totalLots());
            removeAllProducts();
        }
        throw new IllegalStateException(
                "Không tìm được hai sản phẩm có toàn bộ biến thể hợp lệ. "
                        + "Đã loại: " + rejectedProducts + ", summary cuối=" + lastSummary);
    }

    /** Tạo phiếu thật với giá nhập 0 và kiểm tra lô đã sinh. */
    public ZeroPriceSubmissionSnapshot submitZeroPriceReceiptReal() {
        openForm();
        addAvailableProduct();
        List<String> codes = fillRowsForSubmission(1, 0, 70);
        Summary submittedSummary = summary();
        String receiptText = submitCurrentReceipt(false,
                "Nhập kho thật với giá nhập bằng 0");
        boolean receiptContainsAll = codes.stream().allMatch(receiptText::contains);
        int stockMatches = verifyLotsInStock(codes);
        return new ZeroPriceSubmissionSnapshot(
                codes, submittedSummary, receiptContainsAll, stockMatches);
    }

    /** Tìm lô cũ linh động, nhập thêm và đối chiếu tồn trước/sau. */
    public OldLotSubmissionSnapshot submitExistingLotAndVerifyIncrease() {
        openForm();
        OldLotCandidate candidate = findOldLotCandidate();
        if (candidate == null) {
            return new OldLotSubmissionSnapshot(false, "", "", 0, 0, 0, false);
        }
        click(dialogButton("Hủy"), "Hủy form tạm sau khi tìm được lô cũ");
        wait.until(d -> !dialogVisible());
        int beforeQuantity = stockQuantity(candidate.code());

        openForm();
        addProductMatching(candidate.productName());
        fillAllRowsValid();
        WebElement oldLotButton = visibleElements(By.cssSelector(
                        "button[title^='Giá nhập cũ:']"))
                .stream()
                .filter(button -> elementText(button).trim().startsWith(candidate.code()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm lại được lô cũ " + candidate.code()));
        click(oldLotButton, "Chọn lại lô cũ " + candidate.code());
        int rowIndex = rowIndexByLotCode(candidate.code());
        int addedQuantity = 2;
        setRowValue(rowIndex, "Số lượng", Integer.toString(addedQuantity),
                "Nhập thêm " + addedQuantity + " cái vào lô cũ " + candidate.code());
        String receiptText = submitCurrentReceipt(false,
                "Nhập kho thật vào lô cũ " + candidate.code());
        boolean receiptContainsLot = receiptText.contains(candidate.code());
        click(exactMainButton("Tồn kho"), "Quay lại Tồn kho để đối chiếu lô cũ");
        int afterQuantity = stockQuantity(candidate.code());
        return new OldLotSubmissionSnapshot(
                true, candidate.productName(), candidate.code(),
                beforeQuantity, afterQuantity, addedQuantity, receiptContainsLot);
    }

    /** Double-click nút gửi và kiểm tra chỉ một phiếu/một lần tăng tồn. */
    public DoubleClickSubmissionSnapshot doubleClickCreatesOnlyOneReceipt() {
        openForm();
        addAvailableProduct();
        List<String> codes = fillRowsForSubmission(1, 1000, 80);
        String firstCode = codes.get(0);
        String receiptText = submitCurrentReceipt(true,
                "Nhấp đôi nút Nhập kho tổng");
        int receiptRowCount = receiptRowsContaining(firstCode);
        click(exactMainButton("Tồn kho"), "Mở Tồn kho kiểm tra lô sau khi nhấp đôi");
        int stockQuantity = stockQuantity(firstCode);
        return new DoubleClickSubmissionSnapshot(
                firstCode, receiptRowCount, stockQuantity, codes.size());
    }

    /** Nhập kho thật toàn bộ biến thể bằng Điền nhanh và đối chiếu từng lô. */
    public RealBulkSubmissionSnapshot submitMultiVariantWithBulkFillReal() {
        WebElement card = prepareBulkProduct();
        int rows = variantRows(card).size();
        List<String> codes = fillLotCodesAndReturn(card, 200);
        applyBulkValues(card, 2, 1500,
                "Điền nhanh trước khi nhập kho thật nhiều biến thể");
        Summary submitted = summary();
        submitCurrentReceipt(false,
                "Nhập kho thật sản phẩm nhiều biến thể");
        boolean receiptCreated = receiptRowsContaining(codes.get(0)) == 1;
        click(exactMainButton("Tồn kho"), "Đối chiếu tồn của tất cả biến thể vừa nhập");
        List<Integer> quantities = stockQuantities(codes);
        return new RealBulkSubmissionSnapshot(
                rows, codes, submitted, receiptCreated, quantities);
    }

    /** Tạo phiếu với ngày nhập thủ công và đọc dòng phiếu vừa sinh. */
    public ManualDateSubmissionSnapshot submitWithManualDateReal() {
        openForm();
        LocalDate date = LocalDate.now().minusDays(1);
        setControlledValue(field("Ngày nhập"), date.toString(),
                "Chọn ngày nhập thủ công " + date);
        fill(field("Ghi chú"), "Automation ngày nhập thủ công",
                "Nhập ghi chú nhận diện ngày thủ công");
        addSubmissionProduct();
        List<String> codes = fillRowsForSubmission(1, 1000, 220);
        submitCurrentReceipt(false, "Tạo phiếu thật với ngày nhập thủ công");
        String rowText = receiptRowText(codes.get(0));
        String expectedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return new ManualDateSubmissionSnapshot(
                date.toString(), expectedDate, codes.get(0), rowText,
                !rowText.isBlank());
    }

    /** Tạo một phiếu chứa cả lô cũ và các lô mới rồi đối chiếu tồn. */
    public MixedLotSubmissionSnapshot submitOldAndNewLotsTogetherReal() {
        WebElement card = prepareBulkProduct();
        List<WebElement> chips = variantRows(card).get(0).findElements(By.cssSelector(
                "button[title^='Giá nhập cũ:']"));
        if (chips.isEmpty()) {
            return new MixedLotSubmissionSnapshot(
                    false, "", List.of(), 0, 0, 0, false, List.of());
        }
        String oldCode = oldLotCode(chips.get(0));
        click(dialogButton("Hủy"), "Đóng form tạm trước khi đọc tồn lô cũ");
        wait.until(d -> !dialogVisible());
        int oldBefore = stockQuantity(oldCode);

        card = prepareBulkProduct();
        WebElement oldChip = card.findElements(By.cssSelector(
                        "button[title^='Giá nhập cũ:']"))
                .stream().filter(chip -> oldLotCode(chip).equals(oldCode))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Không tìm lại được chip lô cũ " + oldCode));
        click(oldChip, "Chọn lô cũ " + oldCode + " cho dòng đầu");
        int oldIndex = rowIndexByLotCode(oldCode);
        int oldAdded = 2;
        setRowValue(oldIndex, "Số lượng", Integer.toString(oldAdded),
                "Nhập thêm vào lô cũ " + oldCode);

        List<String> newCodes = new ArrayList<>();
        for (int index = 0; index < rowCount(); index++) {
            if (index == oldIndex) {
                continue;
            }
            String code = uniqueLotCode(240 + index);
            newCodes.add(code);
            fillRowValid(index, code, 1, 1000);
        }
        submitCurrentReceipt(false,
                "Nhập kho thật đồng thời lô cũ và lô mới");
        boolean receiptCreated = !newCodes.isEmpty()
                && receiptRowsContaining(newCodes.get(0)) == 1;
        click(exactMainButton("Tồn kho"), "Đối chiếu lô cũ và lô mới sau submission");
        int oldAfter = stockQuantity(oldCode);
        List<Integer> newQuantities = stockQuantities(newCodes);
        return new MixedLotSubmissionSnapshot(
                true, oldCode, newCodes, oldBefore, oldAfter,
                oldAdded, receiptCreated, newQuantities);
    }

    /** Nhập số lượng lớn cho các lô mới và đối chiếu số tồn chính xác. */
    public ExactQuantitySubmissionSnapshot submitNewLotsWithExactQuantityReal() {
        openForm();
        addSubmissionProduct();
        int expectedQuantity = 7;
        List<String> codes = fillRowsForSubmission(expectedQuantity, 1000, 260);
        String receiptText = submitCurrentReceipt(false,
                "Nhập kho thật với số lượng 7 cho từng lô mới");
        click(exactMainButton("Tồn kho"), "Đối chiếu chính xác số tồn từng lô mới");
        return new ExactQuantitySubmissionSnapshot(
                codes, expectedQuantity, stockQuantities(codes),
                codes.stream().allMatch(receiptText::contains));
    }

    /** Tạo phiếu và đọc mã, loại, ngày cùng chi tiết lô trên chính dòng phiếu. */
    public ReceiptMetadataSnapshot submitAndReadReceiptMetadataReal() {
        openForm();
        addSubmissionProduct();
        List<String> codes = fillRowsForSubmission(2, 3500, 280);
        submitCurrentReceipt(false, "Tạo phiếu thật để kiểm tra metadata");
        String rowText = receiptRowText(codes.get(0));
        Matcher code = RECEIPT_CODE.matcher(rowText);
        String receiptCode = code.find() ? code.group() : "";
        String expectedDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return new ReceiptMetadataSnapshot(
                receiptCode, rowText, expectedDate,
                TextNormalizer.normalize(rowText).contains("nhap kho"),
                rowText.contains(expectedDate),
                codes.stream().allMatch(rowText::contains));
    }

    /** Xóa một biến thể rồi chỉ tạo tồn cho các dòng còn lại. */
    public RemovedVariantSubmissionSnapshot removeVariantThenSubmitReal() {
        WebElement card = prepareBulkProduct();
        int rowsBefore = variantRows(card).size();
        List<WebElement> removes = card.findElements(By.cssSelector(
                "button[title='Xoá biến thể này']"));
        click(removes.get(removes.size() - 1),
                "Xóa biến thể cuối trước khi nhập kho thật");
        wait.until(d -> rowCount() == rowsBefore - 1);
        card = bulkCards().get(0);
        List<String> codes = fillLotCodesAndReturn(card, 300);
        applyBulkValues(card, 2, 1000,
                "Điền nhanh các biến thể còn lại trước khi gửi");
        String receiptText = submitCurrentReceipt(false,
                "Nhập kho thật sau khi xóa một biến thể");
        click(exactMainButton("Tồn kho"), "Đối chiếu các lô còn lại sau khi gửi");
        return new RemovedVariantSubmissionSnapshot(
                rowsBefore, codes.size(), codes,
                codes.stream().allMatch(receiptText::contains),
                stockQuantities(codes));
    }

    /** Tạo hai phiếu liên tiếp và xác nhận form lần hai đã được đặt lại. */
    public SequentialSubmissionSnapshot submitTwiceWithoutFormLeakReal() {
        openForm();
        addSubmissionProduct();
        List<String> firstCodes = fillRowsForSubmission(1, 1000, 320);
        fill(field("Ghi chú"), "Phiếu thứ nhất",
                "Nhập ghi chú phiếu thứ nhất");
        submitCurrentReceipt(false, "Tạo phiếu nhập kho thứ nhất");

        openForm();
        boolean reset = field("Ghi chú").getAttribute("value").isBlank()
                && productCount() == 0 && rowCount() == 0;
        addSubmissionProduct();
        List<String> secondCodes = fillRowsForSubmission(1, 1000, 340);
        submitCurrentReceipt(false, "Tạo phiếu nhập kho thứ hai");
        int firstReceiptRows = receiptRowsContaining(firstCodes.get(0));
        int secondReceiptRows = receiptRowsContaining(secondCodes.get(0));
        return new SequentialSubmissionSnapshot(
                reset, firstCodes, secondCodes,
                firstReceiptRows, secondReceiptRows);
    }

    private List<String> fillRowsForSubmission(int quantity, int price, int saltStart) {
        List<String> codes = new ArrayList<>();
        for (int index = 0; index < rowCount(); index++) {
            String code = uniqueLotCode(saltStart + index);
            codes.add(code);
            fillRowValid(index, code, quantity, price);
        }
        pause("Quan sát toàn bộ dòng chuẩn bị gửi thật");
        return codes;
    }

    private String submitCurrentReceipt(boolean doubleClick, String step) {
        scrollToFooter("Cuộn cuối form kiểm tra dữ liệu trước khi gửi");
        WebElement button = submitButton();
        Summary beforeSubmission = summary();
        if (!button.isEnabled()
                || beforeSubmission.validLots() != beforeSubmission.totalLots()) {
            throw new IllegalStateException(
                    "Form chưa hợp lệ trước khi gửi: " + beforeSubmission);
        }
        if (doubleClick) {
            highlight(button);
            pause(step);
            new Actions(driver).doubleClick(button).perform();
        } else {
            click(button, step);
        }
        settle(500);
        List<WebElement> confirmations = visibleElements(By.xpath(
                "//section[@role='dialog']//button[normalize-space()='Xác nhận']"));
        if (!confirmations.isEmpty()) {
            click(confirmations.get(confirmations.size() - 1),
                    "Xác nhận tạo phiếu Nhập kho tổng");
        }
        try {
            wait.until(d -> !dialogVisible());
        } catch (TimeoutException timeout) {
            String alerts = visibleElements(By.cssSelector("[role='alert']"))
                    .stream().map(this::elementText).filter(text -> !text.isBlank())
                    .reduce((left, right) -> left + " | " + right).orElse("");
            String currentDialog = dialogVisible() ? elementText(dialog()) : "";
            throw new AssertionError(
                    "Gửi phiếu nhưng form không đóng. "
                            + "Summary=" + beforeSubmission
                            + ", Alert=" + alerts
                            + ", Dialog=" + currentDialog,
                    timeout);
        }
        pause("Quan sát danh sách sau khi gửi phiếu nhập kho");
        click(exactMainButton("Phiếu"), "Mở tab Phiếu kiểm tra kết quả submission");
        wait.until(d -> TextNormalizer.normalize(mainText()).contains("ma phieu"));
        pause("Quan sát phiếu nhập kho vừa tạo");
        return mainText();
    }

    private int verifyLotsInStock(List<String> codes) {
        click(exactMainButton("Tồn kho"), "Mở Tồn kho đối chiếu các lô vừa nhập");
        int matches = 0;
        for (String code : codes) {
            if (stockQuantity(code) >= 0) {
                matches++;
            }
        }
        return matches;
    }

    private int stockQuantity(String code) {
        WebElement search = visible(By.cssSelector("input[placeholder='Tìm mã lô…']"));
        fill(search, code, "Tìm tồn kho của lô " + code);
        settle(900);
        WebElement row = visible(By.xpath(
                "//main//tr[.//td[contains(normalize-space(.), "
                        + xpathLiteral(code) + ")]]"));
        List<WebElement> cells = row.findElements(By.tagName("td"));
        if (cells.size() < 2) {
            throw new IllegalStateException("Dòng tồn kho không có cột số lượng cho " + code);
        }
        highlight(cells.get(1));
        pause("Quan sát số tồn của lô " + code);
        return number(elementText(cells.get(1)));
    }

    private OldLotCandidate findOldLotCandidate() {
        int previousOldLotCount = 0;
        for (int attempt = 0; attempt < 10; attempt++) {
            ProductSnapshot product = addAvailableProduct();
            List<WebElement> oldLots = visibleElements(By.cssSelector(
                    "button[title^='Giá nhập cũ:']"));
            if (oldLots.size() > previousOldLotCount) {
                WebElement oldLot = oldLots.get(oldLots.size() - 1);
                String text = elementText(oldLot).trim();
                String code = text.split("\\s|\u00b7")[0].trim();
                highlight(oldLot);
                pause("Tìm thấy lô cũ " + code + " của " + product.name());
                return new OldLotCandidate(product.name(), code);
            }
            previousOldLotCount = oldLots.size();
        }
        return null;
    }

    private int rowIndexByLotCode(String code) {
        List<WebElement> inputs = dialog().findElements(By.cssSelector("input[aria-label='Mã lô']"));
        for (int index = 0; index < inputs.size(); index++) {
            if (code.equals(inputs.get(index).getAttribute("value"))) {
                return index;
            }
        }
        throw new IllegalStateException("Không xác định được dòng của lô cũ " + code);
    }

    private int receiptRowsContaining(String lotCode) {
        return wait.until(d -> {
            int matchingRows = (int) visibleElements(RECEIPT_ROWS).stream()
                    .filter(row -> elementText(row).contains(lotCode))
                    .count();
            return matchingRows > 0 ? matchingRows : null;
        });
    }

    private String receiptRowText(String lotCode) {
        return wait.until(d -> visibleElements(RECEIPT_ROWS).stream()
                .filter(row -> elementText(row).contains(lotCode))
                .findFirst().map(this::elementText).orElse(null));
    }

    private List<Integer> stockQuantities(List<String> codes) {
        List<Integer> quantities = new ArrayList<>();
        for (String code : codes) {
            quantities.add(stockQuantity(code));
        }
        return quantities;
    }

    private ProductSnapshot addAvailableProduct() {
        int productsBefore = productCount();
        int rowsBefore = rowCount();
        WebElement combo = productCombo();
        fill(combo, "AUTO", "Tìm sản phẩm test để thêm vào phiếu");
        wait.until(d -> !visibleElements(PRODUCT_OPTIONS).isEmpty());
        WebElement option = visibleElements(PRODUCT_OPTIONS).get(0);
        String name = elementText(option).trim();
        clickFreshProductOption(name, option, "Thêm sản phẩm " + name);
        wait.until(d -> productCount() == productsBefore + 1);
        int addedRows = rowCount() - rowsBefore;
        scrollToLastRow("Cuộn quan sát các dòng nhập kho của " + name);
        return new ProductSnapshot(name, addedRows, productCount(), rowCount(), summary());
    }

    /** Thêm option chưa chọn đầu tiên, không giới hạn vào fixture AUTO. */
    private ProductSnapshot addAnyUnselectedProduct() {
        int productsBefore = productCount();
        int rowsBefore = rowCount();
        WebElement combo = productCombo();
        click(combo, "Mở toàn bộ sản phẩm chưa được chọn");
        if (!combo.getAttribute("value").isBlank()) {
            clearInput(combo, "Xóa từ khóa để xem toàn bộ sản phẩm");
            click(combo, "Mở lại danh sách sản phẩm chưa chọn");
        }
        wait.until(d -> !visibleElements(PRODUCT_OPTIONS).isEmpty());
        WebElement option = visibleElements(PRODUCT_OPTIONS).get(0);
        String name = elementText(option).trim();
        clickFreshProductOption(name, option, "Thêm sản phẩm kế tiếp " + name);
        wait.until(d -> productCount() == productsBefore + 1);
        return new ProductSnapshot(
                name, rowCount() - rowsBefore, productCount(), rowCount(), summary());
    }

    /**
     * Chọn dữ liệu nghiệp vụ cho submission thật, không dùng fixture AUTO
     * chuyên kiểm tra cấu hình lỗi như NO-VALUE/NEGATIVE/DECIMAL.
     */
    private ProductSnapshot addSubmissionProduct() {
        return addSubmissionProduct(Set.of());
    }

    private ProductSnapshot addSubmissionProduct(Set<String> excludedNames) {
        int productsBefore = productCount();
        int rowsBefore = rowCount();
        WebElement combo = productCombo();
        fill(combo, "AUTO-UNIFORM",
                "Tìm sản phẩm test đồng phục có cấu hình hợp lệ");
        wait.until(d -> !visibleElements(PRODUCT_OPTIONS).isEmpty());
        WebElement option = visibleElements(PRODUCT_OPTIONS).stream()
                .filter(item -> isStableSubmissionProduct(elementText(item)))
                .filter(item -> excludedNames.stream().noneMatch(excluded ->
                        TextNormalizer.normalize(excluded).equals(
                                TextNormalizer.normalize(elementText(item)))))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có sản phẩm nghiệp vụ phù hợp để submission thật."));
        String name = elementText(option).trim();
        clickFreshProductOption(name, option,
                "Thêm sản phẩm nghiệp vụ " + name);
        wait.until(d -> productCount() == productsBefore + 1);
        int addedRows = rowCount() - rowsBefore;
        scrollToLastRow("Cuộn quan sát các dòng nhập kho của " + name);
        return new ProductSnapshot(name, addedRows, productCount(), rowCount(), summary());
    }

    private void removeAllProducts() {
        while (productCount() > 0) {
            int before = productCount();
            List<WebElement> buttons = removeProductButtons();
            click(buttons.get(buttons.size() - 1),
                    "Gỡ sản phẩm không phù hợp để thử dữ liệu khác");
            wait.until(d -> productCount() == before - 1);
        }
    }

    private boolean isStableSubmissionProduct(String optionText) {
        String normalized = optionText == null
                ? ""
                : optionText.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()
                || (normalized.startsWith("AUTO-")
                        && !normalized.startsWith("AUTO-UNIFORM"))) {
            return false;
        }
        return List.of(
                        "NO-VALUE", "EMPTY-VALUE", "NEGATIVE", "DECIMAL",
                        "OVERSIZED", "INVALID", "NO-MATCH")
                .stream().noneMatch(normalized::contains);
    }

    private String addProductMatching(String keyword) {
        int productsBefore = productCount();
        WebElement combo = productCombo();
        fill(combo, keyword, "Tìm sản phẩm " + keyword);
        wait.until(d -> !visibleElements(PRODUCT_OPTIONS).isEmpty());
        WebElement option = visibleElements(PRODUCT_OPTIONS).stream()
                .filter(item -> TextNormalizer.normalize(elementText(item))
                        .contains(TextNormalizer.normalize(keyword)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy sản phẩm phù hợp từ khóa " + keyword));
        String name = elementText(option).trim();
        clickFreshProductOption(name, option, "Thêm sản phẩm " + name);
        wait.until(d -> productCount() == productsBefore + 1);
        return name;
    }

    private VariantProductSnapshot addProductWithMinimumRows(int minimumRows) {
        int attempts = 0;
        while (attempts++ < 10) {
            int rowsBefore = rowCount();
            ProductSnapshot product = addAvailableProduct();
            int addedRows = rowCount() - rowsBefore;
            int cardIndex = productCount() - 1;
            String cardText = selectedProductCardText(cardIndex);
            if (addedRows >= minimumRows) {
                return new VariantProductSnapshot(
                        product.name(), addedRows, cardText, true);
            }
        }
        return new VariantProductSnapshot("", 0, "", false);
    }

    private String selectedProductCardText(int index) {
        List<WebElement> cards = selectedProductCards();
        if (index < 0 || index >= cards.size()) {
            throw new IllegalStateException("Không có card sản phẩm tại vị trí " + index);
        }
        return elementText(cards.get(index));
    }

    private List<WebElement> selectedProductCards() {
        return removeProductButtons().stream()
                .map(button -> button.findElement(By.xpath(
                        "./ancestor::div[contains(@class,'rounded-2xl')][1]")))
                .toList();
    }

    /** React render lại listbox trong thời gian quan sát nên phải click option mới theo tên. */
    private void clickFreshProductOption(
            String name, WebElement observedOption, String step) {
        try {
            highlight(observedOption);
        } catch (RuntimeException staleDuringHighlight) {
            visibleElements(PRODUCT_OPTIONS).stream()
                    .filter(item -> elementText(item).trim().equals(name))
                    .findFirst().ifPresent(this::highlight);
        }
        pause(step);
        wait.until(d -> visibleElements(PRODUCT_OPTIONS).stream()
                .filter(item -> elementText(item).trim().equals(name))
                .findFirst()
                .map(item -> {
                    try {
                        item.click();
                        return true;
                    } catch (RuntimeException staleOption) {
                        return false;
                    }
                }).orElse(false));
    }

    private void fillAllRowsValid() {
        for (int index = 0; index < rowCount(); index++) {
            fillRowValid(index, uniqueLotCode(index), 1, 1000);
        }
        pause("Quan sát toàn bộ dòng nhập kho đã nhập đủ dữ liệu");
    }

    private void fillRowValid(int index, String code, int quantity, int price) {
        setRowValue(index, "Mã lô", code, "Nhập mã lô " + code);
        setRowValue(index, "Số lượng", Integer.toString(quantity),
                "Nhập số lượng " + quantity + " cho " + code);
        setRowValue(index, "Giá nhập / cái", Integer.toString(price),
                "Nhập giá " + price + " cho " + code);
    }

    private void setRowValue(int index, String ariaLabel, String value, String step) {
        WebElement input = rowInput(index, ariaLabel);
        if (value.isEmpty()) {
            clearInput(input, step);
        } else {
            fill(input, value, step);
        }
        settle(200);
    }

    /** Xóa giá trị input và phát sự kiện bàn phím để React cập nhật state. */
    private void clearInput(WebElement input, String step) {
        click(input, step);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        wait.until(d -> "".equals(input.getAttribute("value")));
    }

    private void setControlledValue(WebElement input, String value, String step) {
        click(input, step);
        ((JavascriptExecutor) driver).executeScript("""
                const input = arguments[0];
                const value = arguments[1];
                const setter = Object.getOwnPropertyDescriptor(
                        window.HTMLInputElement.prototype, 'value').set;
                setter.call(input, value);
                input.dispatchEvent(new Event('input', {bubbles: true}));
                input.dispatchEvent(new Event('change', {bubbles: true}));
                """, input, value);
        wait.until(d -> value.equals(input.getAttribute("value")));
        pause("Quan sát giá trị đã nhập " + value);
    }

    private ValidationSnapshot validationSnapshot(String attempted, String actual) {
        Summary summary = summary();
        scrollToFooter("Cuộn quan sát trạng thái validation và nút Nhập kho tổng");
        return new ValidationSnapshot(
                attempted, actual, summary, submitButton().isEnabled(),
                elementText(dialog()), !submitButton().isEnabled());
    }

    private Summary summary() {
        String text = dialog().findElements(By.cssSelector("footer,[data-slot='footer']"))
                .stream().filter(WebElement::isDisplayed).map(this::elementText)
                .findFirst().orElse(elementText(dialog()));
        Matcher valid = VALID_COUNTER.matcher(text);
        Matcher quantity = TOTAL_QUANTITY.matcher(text);
        Matcher amount = TOTAL_AMOUNT.matcher(text);
        return new Summary(
                valid.find() ? Integer.parseInt(valid.group(1)) : -1,
                valid.find(0) ? Integer.parseInt(valid.group(2)) : -1,
                quantity.find() ? number(quantity.group(1)) : -1,
                amount.find() ? longNumber(amount.group(1)) : -1);
    }

    private int productCount() {
        return removeProductButtons().size();
    }

    private int rowCount() {
        return dialog().findElements(By.cssSelector("input[aria-label='Mã lô']")).size();
    }

    private WebElement rowInput(int index, String ariaLabel) {
        List<WebElement> lotInputs = dialog().findElements(By.cssSelector(
                "input[aria-label='Mã lô']"));
        if (index < 0 || index >= lotInputs.size()) {
            throw new IllegalStateException("Không có dòng " + index + " cho trường " + ariaLabel);
        }
        WebElement row = variantRow(lotInputs.get(index));
        return row.findElement(By.cssSelector(
                "input[aria-label=" + cssLiteral(ariaLabel) + "]"));
    }

    private WebElement prepareBulkProduct() {
        openForm();
        addProductMatching("Đồ bảo hộ");
        List<WebElement> cards = bulkCards();
        if (cards.isEmpty()) {
            throw new IllegalStateException(
                    "Sản phẩm nhiều biến thể không hiển thị vùng Điền nhanh.");
        }
        WebElement card = cards.get(0);
        if (variantRows(card).size() < 2) {
            throw new IllegalStateException(
                    "Vùng Điền nhanh cần ít nhất hai dòng biến thể để kiểm tra.");
        }
        return card;
    }

    private List<WebElement> bulkCards() {
        return visibleElements(By.xpath(
                "//section[@role='dialog']//button[normalize-space()='Áp dụng tất cả']"))
                .stream()
                .map(button -> button.findElement(By.xpath(
                        "./ancestor::div[contains(@class,'rounded-2xl')][1]")))
                .toList();
    }

    private WebElement bulkApplyButton(WebElement card) {
        return card.findElement(By.xpath(
                ".//button[normalize-space()='Áp dụng tất cả']"));
    }

    private WebElement bulkInput(WebElement card, String ariaLabel) {
        List<WebElement> inputs = card.findElements(By.cssSelector(
                "input[aria-label=" + cssLiteral(ariaLabel) + "]"));
        if (inputs.size() <= variantRows(card).size()) {
            throw new IllegalStateException(
                    "Không tìm thấy input Điền nhanh " + ariaLabel);
        }
        return inputs.get(0);
    }

    private List<WebElement> variantRows(WebElement card) {
        return card.findElements(By.cssSelector("input[aria-label='Mã lô']"))
                .stream().map(this::variantRow).toList();
    }

    private WebElement variantRow(WebElement lotInput) {
        return lotInput.findElement(By.xpath(
                "./ancestor::div[contains(@class,'grid') "
                        + "and contains(@style,'grid-template-columns')][1]"));
    }

    private void setBulkValue(
            WebElement card, String ariaLabel, String value, String step) {
        WebElement input = bulkInput(card, ariaLabel);
        if (value.isEmpty()) {
            clearInput(input, step);
        } else {
            fill(input, value, step);
        }
        settle(200);
    }

    private void applyBulkValues(
            WebElement card, int quantity, int price, String step) {
        setBulkValue(card, "Số lượng", Integer.toString(quantity),
                "Nhập nhanh số lượng " + quantity);
        setBulkValue(card, "Giá nhập / cái", Integer.toString(price),
                "Nhập nhanh giá " + price);
        click(bulkApplyButton(card), step);
        waitForBulkValues(card, "Số lượng", quantity);
        waitForBulkValues(card, "Giá nhập / cái", price);
    }

    private void waitForBulkValues(
            WebElement card, String ariaLabel, int expected) {
        wait.until(d -> {
            List<Integer> values = rowNumbers(card, ariaLabel);
            return !values.isEmpty() && values.stream().allMatch(value -> value == expected);
        });
    }

    private List<Integer> rowNumbers(WebElement card, String ariaLabel) {
        return variantRows(card).stream()
                .map(row -> row.findElement(By.cssSelector(
                        "input[aria-label=" + cssLiteral(ariaLabel) + "]")))
                .map(input -> number(input.getAttribute("value")))
                .toList();
    }

    private void fillLotCodes(WebElement card, int saltStart) {
        fillLotCodesAndReturn(card, saltStart);
    }

    private List<String> fillLotCodesAndReturn(WebElement card, int saltStart) {
        List<WebElement> rows = variantRows(card);
        List<String> codes = new ArrayList<>();
        for (int index = 0; index < rows.size(); index++) {
            String code = uniqueLotCode(saltStart + index);
            codes.add(code);
            WebElement input = rows.get(index).findElement(
                    By.cssSelector("input[aria-label='Mã lô']"));
            fill(input, code, "Nhập mã lô riêng " + code);
        }
        return codes;
    }

    private void clearVariantField(WebElement card, String ariaLabel) {
        for (WebElement row : variantRows(card)) {
            WebElement input = row.findElement(By.cssSelector(
                    "input[aria-label=" + cssLiteral(ariaLabel) + "]"));
            if (!input.getAttribute("value").isBlank()) {
                clearInput(input, "Xóa dữ liệu dòng trước lần thử tiếp theo");
            }
        }
    }

    private BulkValuesSnapshot bulkValuesSnapshot(WebElement card) {
        return new BulkValuesSnapshot(
                variantRows(card).size(),
                rowNumbers(card, "Số lượng"),
                rowNumbers(card, "Giá nhập / cái"), summary());
    }

    private String oldLotCode(WebElement chip) {
        return elementText(chip).trim().split("\\s|\u00b7")[0].trim();
    }

    private void observeElement(WebElement element, String step) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element);
        highlight(element);
        pause(step);
    }

    private List<WebElement> removeProductButtons() {
        return dialog().findElements(By.xpath(
                ".//button[normalize-space()='Gỡ sản phẩm']"))
                .stream().filter(WebElement::isDisplayed).toList();
    }

    private List<String> optionTexts() {
        return visibleElements(PRODUCT_OPTIONS).stream()
                .map(this::elementText).filter(text -> !text.isBlank()).toList();
    }

    private WebElement productListScrollContainer(WebElement option) {
        Object result = ((JavascriptExecutor) driver).executeScript("""
                let node = arguments[0];
                while (node && node !== document.body) {
                    const style = window.getComputedStyle(node);
                    const overflow = style.overflowY;
                    if ((overflow === 'auto' || overflow === 'scroll')
                            && node.scrollHeight > node.clientHeight + 1) {
                        return node;
                    }
                    node = node.parentElement;
                }
                return null;
                """, option);
        if (!(result instanceof WebElement container)) {
            throw new IllegalStateException(
                    "Danh sách gợi ý sản phẩm không có vùng cuộn.");
        }
        return container;
    }

    private WebElement rowListContainer() {
        return dialog().findElement(By.xpath(
                ".//div[contains(@class,'max-h-[48vh]') and contains(@class,'overflow-y-auto')]"));
    }

    private void scrollProductList(WebElement container, long position, String step) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollTop = arguments[1];", container, position);
        wait.until(d -> Math.abs(scrollPosition(container) - position) <= 2);
        highlight(container);
        pause(step);
    }

    private long maximumScroll(WebElement container) {
        return ((Number) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].scrollHeight - arguments[0].clientHeight;", container))
                .longValue();
    }

    private long scrollPosition(WebElement container) {
        return ((Number) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].scrollTop;", container)).longValue();
    }

    private WebElement productCombo() {
        return dialog().findElement(By.cssSelector(
                "input[role='combobox'][aria-label='Thêm sản phẩm']"));
    }

    private WebElement field(String ariaLabel) {
        return dialog().findElement(By.cssSelector(
                "input[aria-label=" + cssLiteral(ariaLabel) + "]"));
    }

    private WebElement submitButton() {
        return dialogButton("Nhập kho tổng");
    }

    private WebElement dialogButton(String text) {
        return dialog().findElement(By.xpath(
                ".//button[normalize-space()=" + xpathLiteral(text) + "]"));
    }

    private WebElement exactMainButton(String text) {
        return visible(By.xpath("//main//button[normalize-space()="
                + xpathLiteral(text) + "]"));
    }

    private WebElement dialog() {
        return visible(DIALOG);
    }

    private boolean dialogVisible() {
        return !visibleElements(DIALOG).isEmpty();
    }

    private void scrollToLastRow(String step) {
        List<WebElement> rows = dialog().findElements(By.cssSelector(
                "input[aria-label='Mã lô']"));
        if (!rows.isEmpty()) {
            WebElement target = rows.get(rows.size() - 1);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", target);
            highlight(target);
            pause(step);
        }
    }

    private void scrollToFooter(String step) {
        WebElement target = submitButton();
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", target);
        highlight(target);
        pause(step);
    }

    private String uniqueLotCode(int salt) {
        long value = (System.nanoTime() + salt) & 0xFFFFFFFL;
        return "AT" + Long.toString(value, 36).toUpperCase(Locale.ROOT);
    }

    private static String cssLiteral(String value) {
        return "'" + value.replace("'", "\\'") + "'";
    }

    private static int number(String value) {
        String digits = value == null ? "" : value.replaceAll("[^0-9]", "");
        return digits.isBlank() ? 0 : Integer.parseInt(digits);
    }

    private static long longNumber(String value) {
        String digits = value == null ? "" : value.replaceAll("[^0-9]", "");
        return digits.isBlank() ? 0 : Long.parseLong(digits);
    }

    public record FormSnapshot(
            String date, boolean dateRequired, String note, boolean productCombo,
            int productCount, int rowCount, Summary summary,
            boolean submitDisabled, boolean emptyState) {
    }

    public record SearchSnapshot(String keyword, String actualValue, List<String> options) {
    }

    public record ProductListSelectionSnapshot(
            long maximumScroll, String selectedName, int productCount, int rowCount) {
    }

    public record ProductListRoundTripSnapshot(
            long maximumScroll, long bottomPosition, long returnedPosition,
            int initialOptionCount, int returnedOptionCount,
            String initialInputValue, String returnedInputValue, boolean listStillOpen) {
    }

    public record ExistingProductSearchSnapshot(
            String keyword, int optionCount, String selectedName,
            int productCount, int rowCount) {
    }

    public record SearchResetSnapshot(
            int initialOptionCount, int restoredOptionCount,
            String inputValue, boolean suggestionsVisible) {
    }

    public record KeyboardSelectionSnapshot(
            int optionCount, int productCount, int rowCount) {
    }

    public record VariantProductSnapshot(
            String selectedName, int rowCount, String cardText, boolean metadataVisible) {
    }

    public record OldLotSnapshot(
            String expectedCode, String actualCode,
            long expectedPrice, long actualPrice, String actualPriceText) {
    }

    public record RemoveOneOfTwoSnapshot(
            String removedName, String remainingName,
            int rowsBefore, int rowsAfter, int productCount,
            boolean remainingVisible, boolean removedVisible) {
    }

    public record RowListScrollSnapshot(
            long maximumScroll, long bottomPosition, long returnedPosition,
            int rowCount, int productCount) {
    }

    public record ProductSnapshot(
            String name, int addedRows, int productCount, int totalRows, Summary summary) {
    }

    public record MultiProductSnapshot(
            ProductSnapshot first, ProductSnapshot second,
            int productCount, int rowCount, Summary summary) {
    }

    public record RemoveProductSnapshot(
            int productCount, int rowCount, Summary summary, boolean submitDisabled) {
    }

    public record RemoveVariantSnapshot(int beforeRows, int afterRows, Summary summary) {
    }

    public record DuplicateProductSnapshot(
            String selectedName, List<String> options, int productCount, int rowCount) {
    }

    public record MetadataSnapshot(
            String expectedDate, String actualDate, String expectedNote, String actualNote) {
    }

    public record ValidationSnapshot(
            String attemptedValue, String actualValue, Summary summary,
            boolean submitEnabled, String dialogText, boolean submissionBlocked) {
    }

    public record LongNoteSnapshot(
            String expectedValue, String actualValue,
            String maximumLength, boolean submitEnabled) {
    }

    public record LongLotCodeSnapshot(
            String expectedValue, String actualValue, String maximumLength,
            Summary summary, boolean submitEnabled) {
    }

    public record TextSafetySnapshot(
            String expectedValue, String actualValue, boolean scriptExecuted,
            Summary summary, boolean submitEnabled) {
    }

    public record RecoverySnapshot(
            boolean enabledWhileInvalid, boolean enabledAfterCorrection,
            Summary invalidSummary, Summary correctedSummary,
            String expectedValue, String actualValue) {
    }

    public record MultiRowRecoverySnapshot(
            boolean enabledWithTwoErrors, boolean enabledWithOneError,
            boolean enabledAfterAllCorrections, Summary summary) {
    }

    public record RequiredFieldsSnapshot(
            String date, String lotCode, String quantity, String price,
            Summary summary, boolean submitEnabled) {
    }

    public record RequiredFieldsRecoverySnapshot(
            boolean enabledBeforeRecovery, boolean enabledAfterDate,
            boolean enabledAfterLot, boolean enabledAfterQuantity,
            boolean enabledAfterPrice, Summary summary) {
    }

    public record CompletionSnapshot(
            boolean enabledBeforeAllRows, boolean enabledAfterAllRows,
            Summary incompleteSummary, Summary completedSummary) {
    }

    public record TotalsSnapshot(
            int rowCount, int expectedQuantity, long expectedAmount, Summary summary) {
    }

    public record SubmissionSnapshot(
            String productName, List<String> lotCodes, Summary submittedSummary,
            boolean receiptContainsAllLots, boolean stockContainsFirstLot, String stockText) {
    }

    public record MultiSubmissionSnapshot(
            int productCount, List<String> lotCodes, Summary submittedSummary,
            boolean receiptContainsAllLots, int stockMatchCount, String receiptText) {
    }

    public record ZeroPriceSubmissionSnapshot(
            List<String> lotCodes, Summary submittedSummary,
            boolean receiptContainsAllLots, int stockMatchCount) {
    }

    public record OldLotSubmissionSnapshot(
            boolean candidateAvailable, String productName, String lotCode,
            int quantityBefore, int quantityAfter, int addedQuantity,
            boolean receiptContainsLot) {
    }

    public record DoubleClickSubmissionSnapshot(
            String lotCode, int receiptRowCount,
            int stockQuantity, int submittedLotCount) {
    }

    public record BulkControlsSnapshot(
            int rowCount, boolean quantityVisible, boolean priceVisible,
            boolean applyButtonVisible, String initialQuantity, String initialPrice) {
    }

    public record BulkValuesSnapshot(
            int rowCount, List<Integer> quantities,
            List<Integer> prices, Summary summary) {
    }

    public record BulkSummarySnapshot(
            int rowCount, int quantityPerRow, int pricePerRow,
            Summary summary, boolean submitEnabled) {
    }

    public record BulkOverwriteSnapshot(
            BulkValuesSnapshot first, BulkValuesSnapshot second) {
    }

    public record ManualOverrideSnapshot(
            int changedIndex, int originalValue,
            int changedValue, List<Integer> quantities) {
    }

    public record BulkIsolationSnapshot(
            boolean candidateAvailable, List<Integer> firstQuantities,
            List<Integer> secondQuantities) {
    }

    public record InvalidBulkSnapshot(
            String attemptedValue, String actualBulkValue,
            List<Integer> rowQuantities) {
    }

    public record BulkRemoveSnapshot(
            int rowsBefore, int rowsAfter,
            Summary summaryBefore, Summary summaryAfter) {
    }

    public record OldLotSwitchSnapshot(
            boolean candidateAvailable, String firstCode, String secondCode,
            String actualCode, long expectedPrice, long actualPrice) {
    }

    public record RealBulkSubmissionSnapshot(
            int rowCount, List<String> lotCodes, Summary submittedSummary,
            boolean receiptCreated, List<Integer> stockQuantities) {
    }

    public record ManualDateSubmissionSnapshot(
            String inputDate, String expectedDisplayDate, String lotCode,
            String receiptRowText, boolean receiptCreated) {
    }

    public record MixedLotSubmissionSnapshot(
            boolean candidateAvailable, String oldLotCode, List<String> newLotCodes,
            int oldQuantityBefore, int oldQuantityAfter, int oldAddedQuantity,
            boolean receiptCreated, List<Integer> newLotQuantities) {
    }

    public record ExactQuantitySubmissionSnapshot(
            List<String> lotCodes, int expectedQuantity,
            List<Integer> actualQuantities, boolean receiptContainsAllLots) {
    }

    public record ReceiptMetadataSnapshot(
            String receiptCode, String rowText, String expectedDate,
            boolean receiptTypeVisible, boolean receiptDateVisible,
            boolean receiptContainsAllLots) {
    }

    public record RemovedVariantSubmissionSnapshot(
            int rowsBefore, int rowsSubmitted, List<String> lotCodes,
            boolean receiptContainsAllLots, List<Integer> stockQuantities) {
    }

    public record SequentialSubmissionSnapshot(
            boolean secondFormReset, List<String> firstLotCodes,
            List<String> secondLotCodes, int firstReceiptRows,
            int secondReceiptRows) {
    }

    private record OldLotCandidate(String productName, String code) {
    }

    private record MultiProductPreparation(
            int productCount, List<String> lotCodes, Summary summary) {
    }

    public record Summary(int validLots, int totalLots, int totalQuantity, long totalAmount) {
    }
}
