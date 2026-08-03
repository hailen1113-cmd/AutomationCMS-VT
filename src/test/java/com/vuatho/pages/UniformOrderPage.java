package com.vuatho.pages;

import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page Object của bộ lọc menu Quản lí Đơn hàng Đồng phục. */
public class UniformOrderPage extends UniformUiPage {
    public static final String ROUTE = "/vuatho/order-uniform";
    public static final List<String> ORDER_STATUSES = List.of(
            "Chờ xác nhận",
            "Đã giao hàng cho bên vận chuyển",
            "Đã hoàn tất",
            "Đã hủy");
    public static final List<String> PAYMENT_STATUSES = List.of(
            "Chưa thanh toán",
            "Đã thanh toán");
    public static final List<String> PAYMENT_METHODS = List.of(
            "COD",
            "Chuyển khoản ngân hàng",
            "Thanh toán trực tiếp tại VP");

    private static final By FILTER_TRIGGER =
            By.cssSelector("button[title='Filter']");
    private static final By GLOBAL_RESET =
            By.cssSelector("button[title='Reset']");
    private static final By ORDER_TABLE = By.cssSelector(
            "table[aria-label='Table about Order Uniform Management']");
    private static final By OPEN_FILTER = By.cssSelector(
            "[data-slot='content'][data-open='true']");
    private static final By SEARCH_INPUT = By.cssSelector(
            "input[aria-label='Tìm kiếm thông tin khách']");
    private static final By SEARCH_TYPE = By.cssSelector(
            "select#type[name='type']");
    private static final By ORDER_ROWS = By.cssSelector(
            "table[aria-label='Table about Order Uniform Management']"
                    + " tbody tr[role='row']");
    private static final By CREATE_ORDER_BUTTON = By.xpath(
            "//button[contains(normalize-space(),'Tạo đơn')]");
    private static final By DRAWERS = By.cssSelector(
            "div[aria-label^='drawer-']");

    public UniformOrderPage(WebDriver driver) {
        super(driver);
    }

    /** Mở trang đơn hàng đồng phục và chờ bảng hoặc trạng thái rỗng. */
    public UniformOrderPage open() {
        openRoute(ROUTE);
        wait.until(d -> !d.findElements(ORDER_TABLE).isEmpty()
                || normalizedMainText().contains("khong co du lieu")
                || normalizedMainText().contains("chua co du lieu"));
        pause("Hiển thị danh sách đơn hàng đồng phục");
        return this;
    }

    /** Đọc đầy đủ nhóm và tùy chọn trong popup bộ lọc. */
    public FilterFormSnapshot filterFormSnapshot() {
        WebElement popup = openFilterPopup();
        return new FilterFormSnapshot(
                elementText(popup),
                visibleOptionTexts(popup),
                hasButton(popup, "Đặt lại"));
    }

    /** Chọn các điều kiện; cùng nhóm là OR, khác nhóm là AND. */
    public FilterResult applyFilters(
            List<String> orderStatuses,
            List<String> paymentStatuses,
            List<String> paymentMethods) {
        open();
        WebElement popup = openFilterPopup();
        for (String option : concat(
                orderStatuses, paymentStatuses, paymentMethods)) {
            clickFilterOption(popup, option);
            popup = openFilterPopup();
        }
        waitForFilterResult();
        List<String> selected = selectedOptions(openFilterPopup());
        closeFilterPopupForResultObservation();
        scrollToFilteredResult(
                orderStatuses, paymentStatuses, paymentMethods);
        return currentFilterResult(
                orderStatuses, paymentStatuses, paymentMethods, selected);
    }

    /** Chọn rồi chọn lại cùng chip và xác minh chip trở về chưa chọn. */
    public ToggleResult toggleFilterOptionOff(String option) {
        open();
        WebElement popup = openFilterPopup();
        clickFilterOption(popup, option);
        boolean selectedAfterFirstClick = isOptionSelected(
                openFilterPopup(), option);
        clickFilterOption(openFilterPopup(), option);
        waitForFilterResult();
        boolean selectedAfterSecondClick = isOptionSelected(
                openFilterPopup(), option);
        int total = totalDisplayed();
        closeFilterPopupForResultObservation();
        scrollToFilteredResult(List.of(), List.of(), List.of());
        return new ToggleResult(
                selectedAfterFirstClick, selectedAfterSecondClick,
                total);
    }

    /** Đặt lại ngay trong popup và trả trạng thái lựa chọn còn lại. */
    public ResetResult resetInsidePopup() {
        open();
        int initialTotal = totalDisplayed();
        WebElement popup = openFilterPopup();
        clickFilterOption(popup, "Chờ xác nhận");
        clickFilterOption(openFilterPopup(), "Chưa thanh toán");
        boolean hadSelection = selectedOptions(openFilterPopup()).size() == 2;
        clickPopupButtonStable(
                "Đặt lại", "Đặt lại toàn bộ bộ lọc trong popup");
        waitForFilterResult();
        WebElement reopened = openFilterPopup();
        List<String> selectedAfterReset = selectedOptions(reopened);
        int totalAfterReset = totalDisplayed();
        closeFilterPopupForResultObservation();
        scrollToFilteredResult(List.of(), List.of(), List.of());
        return new ResetResult(
                hadSelection,
                selectedAfterReset,
                initialTotal,
                totalAfterReset);
    }

    /** Đặt lại bằng nút ngoài trang và trả trạng thái lựa chọn còn lại. */
    public ResetResult resetFromPageButton() {
        open();
        int initialTotal = totalDisplayed();
        WebElement popup = openFilterPopup();
        clickFilterOption(popup, "Đã hoàn tất");
        clickFilterOption(openFilterPopup(), "Đã thanh toán");
        boolean hadSelection = selectedOptions(openFilterPopup()).size() == 2;
        clickPageButtonStable(
                FILTER_TRIGGER, "Đóng popup bộ lọc trước khi Reset");
        wait.until(d -> visibleElements(OPEN_FILTER).isEmpty());
        clickPageButtonStable(
                GLOBAL_RESET, "Đặt lại bộ lọc bằng nút Reset ngoài trang");
        waitForFilterResult();
        WebElement reopened = openFilterPopup();
        List<String> selectedAfterReset = selectedOptions(reopened);
        int totalAfterReset = totalDisplayed();
        closeFilterPopupForResultObservation();
        scrollToFilteredResult(List.of(), List.of(), List.of());
        return new ResetResult(
                hadSelection,
                selectedAfterReset,
                initialTotal,
                totalAfterReset);
    }

    /** Đọc tổng hiển thị hiện tại, có thể thay đổi theo bộ lọc. */
    public int totalDisplayed() {
        Matcher matcher = Pattern.compile(
                        "Tổng hiển thị:\\s*([\\d.]+)",
                        Pattern.CASE_INSENSITIVE)
                .matcher(driver.findElement(By.tagName("body")).getText());
        return matcher.find()
                ? Integer.parseInt(matcher.group(1).replace(".", ""))
                : 0;
    }

    /** Đọc placeholder và hai loại tìm kiếm mà người dùng có thể chọn. */
    public SearchFormSnapshot searchFormSnapshot() {
        open();
        WebElement input = visible(SEARCH_INPUT);
        Select type = new Select(visible(SEARCH_TYPE));
        return new SearchFormSnapshot(
                input.getAttribute("placeholder"),
                input.getAttribute("aria-label"),
                type.getOptions().stream()
                        .map(WebElement::getText)
                        .map(String::trim)
                        .toList(),
                type.getFirstSelectedOption().getAttribute("value"));
    }

    /** Lấy khách hàng đầu tiên làm dữ liệu tìm kiếm động, không phụ thuộc ID đơn. */
    public Optional<CustomerSearchData> firstVisibleCustomer() {
        List<WebElement> rows = visibleElements(ORDER_ROWS);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        CustomerSearchData customer = customerFrom(rows.get(0));
        return customer.name().isBlank() || customer.phone().isBlank()
                ? Optional.empty()
                : Optional.of(customer);
    }

    /** Chọn loại, nhập từ khóa và đọc danh sách khách hàng trả về. */
    public SearchResult search(String type, String keyword) {
        selectSearchType(type);
        replaceSearchKeyword(keyword);
        waitForSearchResult(type, keyword);
        SearchResult result = currentSearchResult(type, keyword);
        scrollToSearchResult(type, keyword);
        return result;
    }

    /** Xóa từ khóa hiện tại và đọc lại danh sách đã được khôi phục. */
    public SearchResult clearSearch() {
        String type = new Select(visible(SEARCH_TYPE))
                .getFirstSelectedOption()
                .getAttribute("value");
        replaceSearchKeyword("");
        waitForResult();
        SearchResult result = currentSearchResult(type, "");
        scrollToSearchResult(type, "đã xóa từ khóa");
        return result;
    }

    /** Chuyển dropdown loại tìm kiếm với thời gian quan sát trước và sau thao tác. */
    private void selectSearchType(String type) {
        WebElement selectElement = visible(SEARCH_TYPE);
        click(selectElement, "Mở loại tìm kiếm "
                + ("phone".equals(type) ? "theo SĐT" : "theo tên"));
        new Select(visible(SEARCH_TYPE)).selectByValue(type);
        pause("Đã chọn loại tìm kiếm "
                + ("phone".equals(type) ? "theo SĐT" : "theo tên"));
    }

    /** Thay nội dung ô tìm kiếm và phát sự kiện xóa khi từ khóa rỗng. */
    private void replaceSearchKeyword(String keyword) {
        WebElement input = visible(SEARCH_INPUT);
        fill(input, keyword, keyword.isBlank()
                ? "Xóa từ khóa tìm kiếm"
                : "Nhập từ khóa tìm kiếm " + keyword);
        if (keyword.isBlank()) {
            input.sendKeys(Keys.BACK_SPACE);
        }
    }

    /** Chờ debounce/API hoàn tất tới khi mọi dòng khớp từ khóa hoặc có empty-state. */
    private void waitForSearchResult(String type, String keyword) {
        settle(800);
        waitForLoadingToFinish();
        long[] emptySince = {0L};
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(12))
                    .pollingEvery(java.time.Duration.ofMillis(300))
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        SearchResult result =
                                currentSearchResult(type, keyword);
                        if (!result.customers().isEmpty()
                                && result.allRowsMatch()) {
                            return true;
                        }
                        if (!result.emptyState()) {
                            emptySince[0] = 0L;
                            return false;
                        }
                        if (emptySince[0] == 0L) {
                            emptySince[0] = System.currentTimeMillis();
                        }
                        return System.currentTimeMillis() - emptySince[0]
                                >= 3_000L;
                    });
        } catch (TimeoutException exception) {
            SearchResult actual = currentSearchResult(type, keyword);
            throw new IllegalStateException(
                    "Search không tải đúng sau 12 giây. type="
                            + actual.selectedType()
                            + ", input=" + actual.inputValue()
                            + ", total=" + actual.totalDisplayed()
                            + ", customers=" + actual.customers(),
                    exception);
        }
    }

    /** Cuộn xuống bảng để quan sát rõ dữ liệu sau mỗi thao tác tìm kiếm. */
    private void scrollToSearchResult(String type, String keyword) {
        WebElement target = visibleElements(ORDER_TABLE).stream()
                .findFirst()
                .orElseGet(() -> driver.findElement(By.tagName("main")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'start', behavior:'smooth'});"
                        + "window.scrollBy(0, -80);",
                target);
        highlight(target);
        pause("Quan sát kết quả tìm "
                + ("phone".equals(type) ? "theo SĐT: " : "theo tên: ")
                + keyword);
    }

    /** Đọc dữ liệu hiện tại của bảng và trạng thái điều khiển search. */
    private SearchResult currentSearchResult(String type, String keyword) {
        StaleElementReferenceException lastStale = null;
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                return readCurrentSearchResult(type, keyword);
            } catch (StaleElementReferenceException exception) {
                lastStale = exception;
                settle(200);
            }
        }
        throw lastStale;
    }

    /** Chụp một snapshot DOM; caller sẽ thử lại nếu React đang thay bảng. */
    private SearchResult readCurrentSearchResult(String type, String keyword) {
        List<CustomerSearchData> customers = visibleElements(ORDER_ROWS)
                .stream()
                .map(this::customerFrom)
                .filter(customer -> !customer.name().isBlank()
                        || !customer.phone().isBlank())
                .toList();
        boolean emptyState = customers.isEmpty()
                && (totalDisplayed() == 0
                || normalizedMainText().contains("khong co du lieu")
                || normalizedMainText().contains("chua co du lieu"));
        return new SearchResult(
                type,
                keyword,
                visible(SEARCH_INPUT).getAttribute("value"),
                new Select(visible(SEARCH_TYPE))
                        .getFirstSelectedOption()
                        .getAttribute("value"),
                customers,
                totalDisplayed(),
                emptyState);
    }

    /** Tách riêng tên và số điện thoại từ cột Thông tin đơn hàng. */
    private CustomerSearchData customerFrom(WebElement row) {
        return new CustomerSearchData(
                labelledValue(row, "Khách hàng:"),
                labelledValue(row, "Số điện thoại:"),
                elementText(row));
    }

    /** Đọc phần tử giá trị nằm ngay sau nhãn trong cùng một dòng thông tin. */
    private String labelledValue(WebElement row, String label) {
        return row.findElements(By.xpath(
                        ".//span[contains(normalize-space(.), "
                                + xpathLiteral(label)
                                + ")]/following-sibling::span[1]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(this::elementText)
                .findFirst()
                .orElse("");
    }

    /** Mở drawer tạo đơn từ danh sách và cuộn về đầu form. */
    public UniformOrderPage openCreateOrderDrawer() {
        open();
        click(visible(CREATE_ORDER_BUTTON), "Mở drawer Tạo đơn");
        WebElement drawer = createOrderDrawer();
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollTop=0;", scrollableCreateBody(drawer));
        pause("Quan sát đầu form tạo đơn");
        return this;
    }

    /** Đọc cấu trúc, trường bắt buộc và giá trị mặc định của form. */
    public CreateOrderFormSnapshot createOrderFormSnapshot() {
        WebElement drawer = createOrderDrawer();
        String text = elementText(drawer);
        List<String> placeholders = drawer.findElements(
                        By.cssSelector("input[role='combobox']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(this::comboboxPlaceholder)
                .filter(value -> !value.isBlank())
                .toList();
        long requiredLabels = drawer.findElements(By.cssSelector("label"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(label -> elementText(label).contains("*"))
                .count();
        return new CreateOrderFormSnapshot(
                text,
                placeholders,
                requiredLabels,
                isDrawerChoiceSelected(drawer, "Chờ xác nhận"),
                isDrawerChoiceSelected(drawer, "Chưa thanh toán"),
                hasButton(drawer, "Hủy"),
                hasButton(drawer, "Xác nhận"));
    }

    /** Đóng drawer bằng nút Hủy và xác nhận drawer biến mất. */
    public boolean cancelCreateOrder() {
        WebElement drawer = createOrderDrawer();
        click(buttonIn(drawer, "Hủy"), "Hủy tạo đơn");
        wait.until(d -> visibleCreateOrderDrawers().isEmpty());
        pause("Quan sát danh sách sau khi Hủy");
        return visibleCreateOrderDrawers().isEmpty();
    }

    /** Đóng drawer bằng nút X ở header. */
    public boolean closeCreateOrderByHeader() {
        WebElement drawer = createOrderDrawer();
        WebElement close = drawer.findElements(By.xpath(
                        ".//h5[contains(normalize-space(),'đơn hàng')]"
                                + "/ancestor::div[contains(@class,'justify-between')][1]"
                                + "//button"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy nút đóng drawer tạo đơn."));
        click(close, "Đóng drawer tạo đơn bằng nút X");
        wait.until(d -> visibleCreateOrderDrawers().isEmpty());
        pause("Quan sát danh sách sau khi đóng drawer");
        return visibleCreateOrderDrawers().isEmpty();
    }

    /** Chọn một trạng thái thanh toán và trả trạng thái visual mới. */
    public boolean selectCreatePaymentStatus(String status) {
        WebElement drawer = createOrderDrawer();
        click(buttonIn(drawer, status),
                "Chọn trạng thái thanh toán " + status);
        pause("Quan sát trạng thái thanh toán " + status);
        return isDrawerChoiceSelected(createOrderDrawer(), status);
    }

    /** Chọn phương thức thanh toán và trả trạng thái visual mới. */
    public boolean selectCreatePaymentMethod(String method) {
        WebElement drawer = createOrderDrawer();
        click(buttonIn(drawer, method),
                "Chọn phương thức thanh toán " + method);
        pause("Quan sát phương thức thanh toán " + method);
        return isDrawerChoiceSelected(createOrderDrawer(), method);
    }

    /** Chọn option combo đầu tiên từ dữ liệu thật và chờ chi tiết được render. */
    public ComboSelectionResult selectFirstCreateCombo() {
        WebElement drawer = createOrderDrawer();
        int comboboxesBefore = visibleCreateComboboxes(drawer).size();
        String selected = selectFirstCreateReactOption(
                drawer, "Tìm kiếm combo đồng phục",
                "Chọn combo đồng phục đầu tiên");
        settle(800);
        WebElement refreshed = createOrderDrawer();
        wait.until(d -> !elementText(createOrderDrawer())
                .contains("Chưa có combo nào được chọn"));
        pause("Quan sát chi tiết combo và biến thể đã hiển thị");
        return new ComboSelectionResult(
                selected,
                elementText(refreshed),
                comboboxesBefore,
                visibleCreateComboboxes(refreshed).size(),
                refreshed.findElements(By.cssSelector("input[type='number']"))
                        .stream().filter(WebElement::isDisplayed).count());
    }

    /** Chọn hồ sơ thợ đầu tiên có trong React Select. */
    public String selectFirstCreateWorker() {
        return selectFirstCreateReactOption(
                createOrderDrawer(), "Tìm kiếm hồ sơ thợ",
                "Chọn hồ sơ thợ đầu tiên");
    }

    /** Nhập ghi chú và địa chỉ, sau đó trả lại value thực tế của hai textarea. */
    public TextEntryResult fillCreateTexts(String note, String address) {
        WebElement drawer = createOrderDrawer();
        WebElement noteInput = drawer.findElement(By.cssSelector(
                "textarea[aria-label='Nhập ghi chú cho đơn hàng (nếu có)']"));
        WebElement addressInput = drawer.findElement(By.cssSelector(
                "textarea[aria-label='Nhập địa chỉ giao hàng']"));
        fillCreateText(noteInput, note, "Nhập ghi chú đơn hàng");
        fillCreateText(addressInput, address, "Nhập địa chỉ giao hàng");
        pause("Quan sát ghi chú và địa chỉ đã nhập");
        return new TextEntryResult(
                noteInput.getAttribute("value"),
                addressInput.getAttribute("value"));
    }

    /**
     * Chuỗi dài được gán qua native textarea setter để không làm ChromeDriver
     * treo ở lệnh sendKeys; vẫn phát event React và giữ thời gian quan sát.
     */
    private void fillCreateText(
            WebElement textarea,
            String value,
            String step) {
        if (value.length() <= 1000) {
            fill(textarea, value, step);
            return;
        }
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});"
                        + "const setter=Object.getOwnPropertyDescriptor("
                        + "HTMLTextAreaElement.prototype,'value').set;"
                        + "setter.call(arguments[0],arguments[1]);"
                        + "arguments[0].dispatchEvent(new Event('input',"
                        + "{bubbles:true}));"
                        + "arguments[0].dispatchEvent(new Event('change',"
                        + "{bubbles:true}));",
                textarea,
                value);
        highlight(textarea);
        pause(step + " (dữ liệu dài)");
    }

    /** Đọc đúng validation nằm trong từng vùng bắt buộc của form tạo đơn. */
    public Set<CreateRequiredField> readCreateFieldValidation() {
        WebElement drawer = createOrderDrawer();
        Set<CreateRequiredField> invalidFields = new LinkedHashSet<>();
        if (requiredSectionHasValidation(drawer, "Phương thức thanh toán")) {
            invalidFields.add(CreateRequiredField.PAYMENT_METHOD);
        }
        if (requiredSectionHasValidation(drawer, "Thêm combo đồng phục")) {
            invalidFields.add(CreateRequiredField.COMBO);
        }
        if (requiredSectionHasValidation(drawer, "Hồ sơ thợ")) {
            invalidFields.add(CreateRequiredField.WORKER);
        }
        if (requiredSectionHasValidation(drawer, "Địa chỉ")) {
            invalidFields.add(CreateRequiredField.ADDRESS);
        }
        return Set.copyOf(invalidFields);
    }

    /** Đọc lại dữ liệu còn nằm trên form sau một lần validation thất bại. */
    public CreateFormValues createFormValues() {
        WebElement drawer = createOrderDrawer();
        String note = drawer.findElement(By.cssSelector(
                        "textarea[aria-label='Nhập ghi chú cho đơn hàng (nếu có)']"))
                .getAttribute("value");
        String address = drawer.findElement(By.cssSelector(
                        "textarea[aria-label='Nhập địa chỉ giao hàng']"))
                .getAttribute("value");
        String selectedPayment = PAYMENT_METHODS.stream()
                .filter(method -> isDrawerChoiceSelected(drawer, method))
                .findFirst()
                .orElse("");
        return new CreateFormValues(
                TextNormalizer.normalize(elementText(drawer)),
                selectedPayment,
                note,
                address);
    }

    /** Kiểm tra payload HTML trong textarea không tạo element thật trên DOM. */
    public boolean createFormContainsElementId(String id) {
        return !createOrderDrawer().findElements(By.id(id)).isEmpty();
    }

    /** Bấm Xác nhận và chụp validation/toast mà không giả định nội dung lỗi cố định. */
    public CreateValidationResult submitCreateAndReadValidation() {
        int totalBefore = totalDisplayed();
        WebElement drawer = createOrderDrawer();
        WebElement confirm = buttonIn(drawer, "Xác nhận");
        click(confirm, "Xác nhận tạo đơn");
        settle(800);
        waitForLoadingToFinish();
        boolean drawerOpen = !visibleCreateOrderDrawers().isEmpty();
        String body = driver.findElement(By.tagName("body")).getText();
        Set<CreateRequiredField> invalidFields = drawerOpen
                ? readCreateFieldValidation()
                : Set.of();
        long invalidControls = drawerOpen
                ? createOrderDrawer().findElements(By.cssSelector(
                        "[aria-invalid='true'],"
                                + " .border-danger,"
                                + " .text-danger"))
                        .stream().filter(WebElement::isDisplayed).count()
                : 0;
        pause(drawerOpen
                ? "Quan sát validation form tạo đơn"
                : "Quan sát đơn vừa được tạo");
        return new CreateValidationResult(
                drawerOpen,
                TextNormalizer.normalize(body),
                invalidControls,
                invalidFields,
                totalBefore,
                totalDisplayed());
    }

    /** Điền động mọi control cần thiết và tạo đơn thật trên sandbox. */
    public CreateSubmissionResult createRealOrder(
            String paymentStatus,
            String paymentMethod,
            String address,
            String note) {
        open();
        int totalBefore = totalDisplayed();
        openCreateOrderDrawer();
        selectCreatePaymentStatus(paymentStatus);
        selectCreatePaymentMethod(paymentMethod);
        ComboSelectionResult combo = selectCreateComboWithEnoughStock();
        configureRenderedComboControls();
        String worker = selectFirstCreateWorker();
        fillCreateTexts(note, address);
        CreateValidationResult submitted = submitCreateAndReadValidation();
        if (!submitted.drawerOpen()) {
            waitForResult();
        }
        return new CreateSubmissionResult(
                !submitted.drawerOpen(),
                totalBefore,
                totalDisplayed(),
                combo.selectedCombo(),
                worker,
                submitted.bodyText(),
                paymentStatus,
                paymentMethod,
                address,
                note,
                latestOrderRowText());
    }

    /**
     * Chuẩn bị một form hợp lệ rồi thêm combo thứ hai, đổi Size và tăng số
     * lượng. Case trả về rõ thao tác nào UI hiện tại hỗ trợ để test có thể
     * SKIP minh bạch khi sandbox không còn dữ liệu phù hợp.
     */
    public AdvancedCreateResult createAdvancedOrder() {
        open();
        int totalBefore = totalDisplayed();
        openCreateOrderDrawer();
        selectCreatePaymentStatus("Chưa thanh toán");
        selectCreatePaymentMethod("Chuyển khoản ngân hàng");
        ComboSelectionResult firstCombo = selectCreateComboWithEnoughStock();
        configureRenderedComboControls();
        String changedSize = chooseDifferentAvailableSize();
        String secondCombo = addAnotherCreateCombo();
        String worker = selectFirstCreateWorker();
        fillCreateTexts(
                "Automation nhiều combo, số lượng và Size",
                "789 Đường Automation, TP.HCM");
        boolean ready = !secondCombo.isBlank()
                && !TextNormalizer.normalize(elementText(createOrderDrawer()))
                        .contains("thieu hang");
        if (!ready) {
            return new AdvancedCreateResult(
                    false, totalBefore, totalDisplayed(),
                    firstCombo.selectedCombo(), secondCombo,
                    changedSize, worker,
                    createControlSummary());
        }
        CreateValidationResult submitted = submitCreateAndReadValidation();
        if (!submitted.drawerOpen()) {
            waitForResult();
        }
        return new AdvancedCreateResult(
                !submitted.drawerOpen(), totalBefore, totalDisplayed(),
                firstCombo.selectedCombo(), secondCombo,
                changedSize, worker, submitted.bodyText());
    }

    /** Chọn động một combo/Size thiếu hàng rồi xác minh submit bị chặn. */
    public UnavailableStockResult rejectUnavailableStock() {
        open();
        int totalBefore = totalDisplayed();
        openCreateOrderDrawer();
        selectCreatePaymentStatus("Chưa thanh toán");
        selectCreatePaymentMethod("Chuyển khoản ngân hàng");
        ComboSelectionResult selected = selectFirstCreateCombo();
        boolean shortageFound = TextNormalizer.normalize(
                        selected.drawerContent())
                .contains("thieu hang");
        if (!shortageFound) {
            List<String> sizes = createOrderDrawer()
                    .findElements(By.cssSelector("button"))
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .map(this::elementText)
                    .map(String::trim)
                    .filter(text -> text.matches(
                            "(?i)^(XS|S|M|L|XL|2XL|3XL|4XL|5XL)$"))
                    .distinct()
                    .toList();
            for (String size : sizes) {
                WebElement button = createOrderDrawer()
                        .findElements(By.xpath(
                                ".//button[normalize-space()="
                                        + xpathLiteral(size) + "]"))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElse(null);
                if (button == null) {
                    continue;
                }
                click(button, "Chọn Size để tìm dữ liệu thiếu tồn kho: " + size);
                settle(500);
                shortageFound = TextNormalizer.normalize(
                                elementText(createOrderDrawer()))
                        .contains("thieu hang");
                if (shortageFound) {
                    break;
                }
            }
        }
        if (!shortageFound) {
            return new UnavailableStockResult(
                    false, true, totalBefore, totalDisplayed(),
                    TextNormalizer.normalize(elementText(createOrderDrawer())));
        }
        configureRenderedComboControls();
        selectFirstCreateWorker();
        fillCreateTexts("", "999 Đường Automation, TP.HCM");
        CreateValidationResult submitted = submitCreateAndReadValidation();
        String responseContent = submitted.bodyText();
        boolean drawerOpen = submitted.drawerOpen();
        if (drawerOpen) {
            cancelCreateOrder();
        }
        return new UnavailableStockResult(
                true,
                drawerOpen,
                totalBefore,
                totalDisplayed(),
                responseContent);
    }

    /**
     * Bấm nút Xác nhận lần hai ngay khi có thể và đo chính xác số đơn tăng.
     * Nếu React đã đóng drawer hoặc vô hiệu hóa nút thì xem như UI đã chặn.
     */
    public DoubleSubmitResult submitValidOrderTwice() {
        open();
        int totalBefore = totalDisplayed();
        openCreateOrderDrawer();
        selectCreatePaymentStatus("Chưa thanh toán");
        selectCreatePaymentMethod("Chuyển khoản ngân hàng");
        selectCreateComboWithEnoughStock();
        configureRenderedComboControls();
        selectFirstCreateWorker();
        fillCreateTexts(
                "Automation kiểm tra chống bấm hai lần",
                "246 Đường Automation, TP.HCM");
        WebElement confirm = buttonIn(createOrderDrawer(), "Xác nhận");
        click(confirm, "Xác nhận tạo đơn lần thứ nhất");
        boolean secondClickAccepted = false;
        try {
            List<WebElement> drawers = visibleCreateOrderDrawers();
            if (!drawers.isEmpty()) {
                List<WebElement> buttons = drawers.get(0).findElements(
                                By.xpath(".//button[normalize-space()='Xác nhận']"))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .filter(WebElement::isEnabled)
                        .toList();
                if (!buttons.isEmpty()) {
                    buttons.get(0).click();
                    secondClickAccepted = true;
                }
            }
        } catch (RuntimeException ignored) {
            // Nút/drawer bị React khóa hoặc tháo khỏi DOM chính là cơ chế bảo vệ.
        }
        wait.until(d -> visibleCreateOrderDrawers().isEmpty());
        waitForResult();
        return new DoubleSubmitResult(
                totalBefore,
                totalDisplayed(),
                secondClickAccepted,
                latestOrderRowText());
    }

    /** Mở dòng đơn mới nhất và đọc toàn bộ drawer chi tiết đã lưu. */
    public String openLatestOrderDetail() {
        waitForResult();
        WebElement row = visibleElements(ORDER_ROWS).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có dòng đơn mới nhất để mở chi tiết."));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});",
                row);
        highlight(row);
        pause("Quan sát đơn mới nhất trước khi mở chi tiết");
        click(row, "Mở chi tiết đơn mới nhất");
        WebElement detail = wait.until(d -> d.findElements(DRAWERS).stream()
                .filter(WebElement::isDisplayed)
                .filter(drawer -> !TextNormalizer.normalize(
                                elementText(drawer))
                        .contains("tao moi don hang"))
                .findFirst()
                .orElse(null));
        pause("Quan sát dữ liệu đã lưu trong chi tiết đơn");
        return TextNormalizer.normalize(elementText(detail));
    }

    /** Duyệt combo thật và bỏ combo báo Thiếu hàng trước khi tạo đơn. */
    public ComboSelectionResult selectCreateComboWithEnoughStock() {
        int optionIndex = 0;
        String lastDrawerContent = "";
        while (optionIndex < 8) {
            WebElement drawer = createOrderDrawer();
            WebElement input = waitForCreateCombobox(
                    "Tìm kiếm combo đồng phục");
            String selected = selectReactOptionByIndex(
                    input, optionIndex,
                    "Chọn combo đủ tồn kho thứ " + (optionIndex + 1));
            settle(800);
            WebElement refreshed = createOrderDrawer();
            lastDrawerContent = elementText(refreshed);
            if (!TextNormalizer.normalize(lastDrawerContent)
                    .contains("thieu hang")) {
                return new ComboSelectionResult(
                        selected,
                        lastDrawerContent,
                        2,
                        visibleCreateComboboxes(refreshed).size(),
                        refreshed.findElements(By.cssSelector(
                                        "input[type='number']"))
                                .stream()
                                .filter(WebElement::isDisplayed)
                                .count());
            }
            ComboSelectionResult resolved =
                    tryResolveShortageByVariantButtons(selected);
            if (resolved != null) {
                return resolved;
            }
            pause("Combo thiếu tồn kho, chuẩn bị chọn combo khác");
            WebElement removeLabel = refreshed.findElements(By.xpath(".//*"))
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> TextNormalizer.normalize(
                                    elementText(element))
                            .equals("xoa combo"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Combo thiếu hàng nhưng không có thao tác Xóa combo."));
            WebElement removeCombo = removeLabel.findElements(By.xpath(
                            "./ancestor::button[1]"
                                    + " | ./ancestor::*[@role='button'][1]"))
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(removeLabel);
            click(removeCombo,
                    "Xóa combo đang thiếu tồn kho");
            waitForCreateCombobox("Tìm kiếm combo đồng phục");
            optionIndex++;
        }
        throw new IllegalStateException(
                "Không tìm thấy combo đủ tồn kho để tạo đơn. Form cuối: "
                        + lastDrawerContent);
    }

    /**
     * Xác định lỗi trong đúng section của label, tránh nhận nhầm màu đỏ của
     * trạng thái Chưa thanh toán thành validation của trường khác.
     */
    private boolean requiredSectionHasValidation(
            WebElement drawer,
            String labelText) {
        WebElement label = drawer.findElements(By.cssSelector("label"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> TextNormalizer.normalize(
                                elementText(element))
                        .startsWith(TextNormalizer.normalize(labelText)))
                .findFirst()
                .orElse(null);
        if (label == null) {
            return false;
        }
        WebElement section = label.findElement(By.xpath("./parent::*"));
        boolean hasInvalidMarker = section.findElements(By.cssSelector(
                        "[aria-invalid='true'],"
                                + "[data-invalid='true'],"
                                + "[data-slot='error-message'],"
                                + ".border-danger,"
                                + ".text-danger"))
                .stream()
                .anyMatch(WebElement::isDisplayed);
        String sectionText = TextNormalizer.normalize(elementText(section));
        boolean hasErrorMessage = sectionText.contains("bat buoc")
                || sectionText.contains("vui long")
                || sectionText.contains("khong duoc de trong")
                || sectionText.contains("khong hop le");
        return hasInvalidMarker || hasErrorMessage;
    }

    /** Thử các nút Size/biến thể để tìm cấu hình không còn cảnh báo Thiếu hàng. */
    private ComboSelectionResult tryResolveShortageByVariantButtons(
            String selectedCombo) {
        List<String> variantTexts = createOrderDrawer()
                .findElements(By.cssSelector("button"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(this::elementText)
                .map(String::trim)
                .filter(text -> text.matches(
                        "(?i)^(XS|S|M|L|XL|2XL|3XL|4XL|5XL)$"))
                .distinct()
                .toList();
        for (String variant : variantTexts) {
            List<WebElement> candidates = createOrderDrawer()
                    .findElements(By.xpath(
                            ".//button[normalize-space()="
                                    + xpathLiteral(variant) + "]"))
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .toList();
            for (int index = 0; index < candidates.size(); index++) {
                WebElement candidate = createOrderDrawer()
                        .findElements(By.xpath(
                                ".//button[normalize-space()="
                                        + xpathLiteral(variant) + "]"))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .skip(index)
                        .findFirst()
                        .orElse(null);
                if (candidate == null) {
                    continue;
                }
                click(candidate, "Chọn biến thể Size " + variant);
                settle(600);
                WebElement refreshed = createOrderDrawer();
                String content = elementText(refreshed);
                if (!TextNormalizer.normalize(content)
                        .contains("thieu hang")) {
                    pause("Đã chọn biến thể đủ tồn kho");
                    return new ComboSelectionResult(
                            selectedCombo,
                            content,
                            2,
                            visibleCreateComboboxes(refreshed).size(),
                            refreshed.findElements(By.cssSelector(
                                            "input[type='number']"))
                                    .stream()
                                    .filter(WebElement::isDisplayed)
                                    .count());
                }
            }
        }
        return null;
    }

    /** Chọn một Size khác trạng thái đang chọn nhưng vẫn còn tồn kho. */
    private String chooseDifferentAvailableSize() {
        List<String> sizes = createOrderDrawer()
                .findElements(By.cssSelector("button"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(this::elementText)
                .map(String::trim)
                .filter(text -> text.matches(
                        "(?i)^(XS|S|M|L|XL|2XL|3XL|4XL|5XL)$"))
                .distinct()
                .toList();
        if (sizes.size() < 2) {
            return "";
        }
        for (int index = 1; index < sizes.size(); index++) {
            String size = sizes.get(index);
            WebElement button = createOrderDrawer().findElements(By.xpath(
                            ".//button[normalize-space()="
                                    + xpathLiteral(size) + "]"))
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(null);
            if (button == null) {
                continue;
            }
            click(button, "Chọn Size khác mặc định: " + size);
            settle(500);
            if (!TextNormalizer.normalize(elementText(createOrderDrawer()))
                    .contains("thieu hang")) {
                pause("Quan sát Size khác mặc định đã chọn");
                return size;
            }
        }
        return "";
    }

    /** Chọn thêm một combo khác từ React Select còn hiển thị. */
    private String addAnotherCreateCombo() {
        WebElement drawer = createOrderDrawer();
        WebElement input = visibleCreateComboboxes(drawer).stream()
                .filter(combo -> comboboxPlaceholder(combo)
                        .contains("Tìm kiếm combo đồng phục"))
                .findFirst()
                .orElse(null);
        if (input == null) {
            WebElement addButton = drawer.findElements(By.cssSelector("button"))
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .filter(button -> TextNormalizer.normalize(
                                    elementText(button))
                            .contains("them combo"))
                    .findFirst()
                    .orElse(null);
            if (addButton == null) {
                return "";
            }
            click(addButton, "Mở vùng thêm combo thứ hai");
            try {
                input = waitForCreateCombobox(
                        "Tìm kiếm combo đồng phục");
            } catch (TimeoutException noComboInput) {
                return "";
            }
        }
        String selected;
        try {
            selected = selectFirstReactOption(
                    input, "Thêm combo đồng phục thứ hai");
        } catch (RuntimeException noSecondCombo) {
            return "";
        }
        settle(800);
        if (TextNormalizer.normalize(elementText(createOrderDrawer()))
                .contains("thieu hang")) {
            ComboSelectionResult resolved =
                    tryResolveShortageByVariantButtons(selected);
            if (resolved == null) {
                return "";
            }
        }
        configureRenderedComboControls();
        pause("Quan sát hai combo trong cùng đơn hàng");
        return selected;
    }

    /** Tóm tắt control động để lý do SKIP chỉ ra đúng dữ liệu UI còn thiếu. */
    private String createControlSummary() {
        WebElement drawer = createOrderDrawer();
        String inputs = drawer.findElements(By.cssSelector("input"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(input -> "[type=" + input.getAttribute("type")
                        + ", value=" + input.getAttribute("value")
                        + ", aria=" + input.getAttribute("aria-label")
                        + ", placeholder=" + input.getAttribute("placeholder")
                        + "]")
                .toList()
                .toString();
        String buttons = drawer.findElements(By.cssSelector("button"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(this::elementText)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .toList()
                .toString();
        return "Inputs=" + inputs + "; Buttons=" + buttons;
    }

    /** Chọn option đầu tiên cho các combobox biến thể phát sinh và nhập số lượng hợp lệ. */
    private void configureRenderedComboControls() {
        WebElement drawer = createOrderDrawer();
        List<WebElement> extraComboboxes = visibleCreateComboboxes(drawer)
                .stream()
                .filter(input -> {
                    String placeholder = comboboxPlaceholder(input);
                    return !placeholder.contains("combo đồng phục")
                            && !placeholder.contains("hồ sơ thợ");
                })
                .toList();
        for (int index = 0; index < extraComboboxes.size(); index++) {
            WebElement input = visibleCreateComboboxes(createOrderDrawer())
                    .stream()
                    .filter(combo -> {
                        String placeholder = comboboxPlaceholder(combo);
                        return !placeholder.contains("combo đồng phục")
                                && !placeholder.contains("hồ sơ thợ");
                    })
                    .skip(index)
                    .findFirst()
                    .orElse(null);
            if (input != null && input.getAttribute("value").isBlank()) {
                selectFirstReactOption(input,
                        "Chọn biến thể combo thứ " + (index + 1));
            }
        }
        for (WebElement number : createOrderDrawer().findElements(
                By.cssSelector("input[type='number']"))) {
            if (number.isDisplayed()) {
                fill(number, "1", "Nhập số lượng sản phẩm");
            }
        }
    }

    /** Đọc nguyên văn dòng đơn đầu tiên sau khi bảng tải xong. */
    private String latestOrderRowText() {
        return visibleElements(ORDER_ROWS).stream()
                .findFirst()
                .map(this::elementText)
                .map(TextNormalizer::normalize)
                .orElse("");
    }

    /** Chọn option đầu tiên theo placeholder của React Select. */
    private String selectFirstCreateReactOption(
            WebElement drawer,
            String placeholder,
            String step) {
        WebElement input = waitForCreateCombobox(placeholder);
        return selectFirstReactOption(input, step);
    }

    /** Chờ React render lại combobox theo placeholder sau khi thêm/xóa dữ liệu. */
    private WebElement waitForCreateCombobox(String placeholder) {
        return new org.openqa.selenium.support.ui.WebDriverWait(
                driver, java.time.Duration.ofSeconds(10))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> visibleCreateComboboxes(createOrderDrawer()).stream()
                        .filter(combo -> comboboxPlaceholder(combo)
                                .contains(placeholder))
                        .findFirst()
                        .orElse(null));
    }

    /** Chọn option React Select bằng DOM mới nhất sau khi menu render. */
    private String selectFirstReactOption(WebElement input, String step) {
        click(input, step);
        WebElement option;
        try {
            option = waitForFirstReactOption(3);
        } catch (TimeoutException noInitialOptions) {
            fill(input, "a",
                    "Nhập từ khóa để tải danh sách hồ sơ thợ");
            option = waitForFirstReactOption(8);
        }
        String selected = elementText(option);
        click(option, step + ": " + selected);
        settle(500);
        return selected;
    }

    /** Chọn option React Select theo index để có thể bỏ qua combo thiếu tồn kho. */
    private String selectReactOptionByIndex(
            WebElement input,
            int optionIndex,
            String step) {
        click(input, step);
        List<WebElement> options = new org.openqa.selenium.support.ui.WebDriverWait(
                driver, java.time.Duration.ofSeconds(8))
                .until(d -> {
                    List<WebElement> rendered = d.findElements(
                                    By.cssSelector("[role='option']"))
                            .stream()
                            .filter(WebElement::isDisplayed)
                            .toList();
                    return rendered.size() > optionIndex ? rendered : null;
                });
        WebElement option = options.get(optionIndex);
        String selected = elementText(option);
        click(option, step + ": " + selected);
        settle(500);
        return selected;
    }

    /** Chờ option đầu tiên của React Select đang mở. */
    private WebElement waitForFirstReactOption(int seconds) {
        return new org.openqa.selenium.support.ui.WebDriverWait(
                driver, java.time.Duration.ofSeconds(seconds))
                .until(d -> d.findElements(By.cssSelector("[role='option']"))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElse(null));
    }

    /** Đọc placeholder qua aria-describedby của React Select. */
    private String comboboxPlaceholder(WebElement input) {
        String describedBy = input.getAttribute("aria-describedby");
        if (describedBy != null && !describedBy.isBlank()) {
            List<WebElement> described = driver.findElements(By.id(describedBy));
            if (!described.isEmpty()) {
                return elementText(described.get(0));
            }
        }
        return input.findElements(By.xpath(
                        "./ancestor::div[contains(@class,'control')][1]"))
                .stream()
                .map(this::elementText)
                .findFirst()
                .orElse("");
    }

    /** Trả các React Select đang hiển thị trong drawer hiện tại. */
    private List<WebElement> visibleCreateComboboxes(WebElement drawer) {
        return drawer.findElements(By.cssSelector("input[role='combobox']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    /** Kiểm tra chip/button đã chọn dựa trên class selected của UI. */
    private boolean isDrawerChoiceSelected(WebElement drawer, String text) {
        WebElement button = buttonIn(drawer, text);
        String classes = button.getAttribute("class");
        return classes != null
                && !classes.contains("border-black/10")
                && (classes.contains("bg-")
                || classes.contains("border-warning")
                || classes.contains("border-danger")
                || classes.contains("border-success")
                || classes.contains("border-primary"));
    }

    /** Drawer tạo đơn đang mở; drawer ẩn trong DOM không được tính. */
    private WebElement createOrderDrawer() {
        return wait.until(d -> visibleCreateOrderDrawers().stream()
                .findFirst()
                .orElse(null));
    }

    private List<WebElement> visibleCreateOrderDrawers() {
        return driver.findElements(DRAWERS).stream()
                .filter(WebElement::isDisplayed)
                .filter(drawer -> TextNormalizer.normalize(elementText(drawer))
                        .contains("tao moi don hang"))
                .toList();
    }

    /** Vùng scroll chính của drawer dùng để đưa form về đầu. */
    private WebElement scrollableCreateBody(WebElement drawer) {
        return drawer.findElements(By.cssSelector("div.overflow-scroll"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(drawer);
    }

    /** Đóng popup filter nếu còn mở. */
    @Override
    public void closeOverlay() {
        if (!visibleCreateOrderDrawers().isEmpty()) {
            try {
                click(buttonIn(createOrderDrawer(), "Hủy"),
                        "Đóng drawer tạo đơn sau testcase");
            } catch (RuntimeException ignored) {
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            }
            return;
        }
        if (!visibleElements(OPEN_FILTER).isEmpty()) {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            return;
        }
        super.closeOverlay();
    }

    private WebElement openFilterPopup() {
        List<WebElement> opened = visibleElements(OPEN_FILTER);
        if (!opened.isEmpty()) {
            return opened.get(0);
        }
        click(visible(FILTER_TRIGGER), "Mở bộ lọc đơn hàng đồng phục");
        WebElement popup = visible(OPEN_FILTER);
        pause("Hiển thị đầy đủ tùy chọn bộ lọc");
        return popup;
    }

    private void clickFilterOption(WebElement popup, String option) {
        WebElement button = buttonIn(popup, option);
        String before = selectionFingerprint(button);
        clickPopupButtonStable(option, "Chọn bộ lọc " + option);
        wait.until(d -> {
            try {
                return !selectionFingerprint(
                        buttonIn(openFilterPopup(), option)).equals(before);
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
        pause("Quan sát trạng thái đã chọn " + option);
    }

    /** Click button trong popup bằng element mới nhất sau mỗi lần React render. */
    private void clickPopupButtonStable(String text, String step) {
        WebElement button = buttonIn(openFilterPopup(), text);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});",
                button);
        highlight(button);
        pause(step);
        boolean clicked = false;
        for (int attempt = 0; attempt < 3 && !clicked; attempt++) {
            try {
                buttonIn(openFilterPopup(), text).click();
                clicked = true;
            } catch (StaleElementReferenceException ignored) {
                // React vừa render lại popup; vòng sau lấy element mới.
            }
        }
        if (!clicked) {
            throw new IllegalStateException(
                    "Không click được tùy chọn sau khi popup render lại: "
                            + text);
        }
    }

    /** Click button ngoài trang bằng element mới nhất sau khi popup đóng. */
    private void clickPageButtonStable(By locator, String step) {
        WebElement button = visible(locator);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});",
                button);
        highlight(button);
        pause(step);
        boolean clicked = false;
        for (int attempt = 0; attempt < 3 && !clicked; attempt++) {
            try {
                visible(locator).click();
                clicked = true;
            } catch (StaleElementReferenceException ignored) {
                // Trang vừa render lại; vòng sau lấy button mới.
            }
        }
        if (!clicked) {
            throw new IllegalStateException(
                    "Không click được nút ngoài trang sau khi render lại.");
        }
    }

    private void waitForFilterResult() {
        settle(500);
        waitForLoadingToFinish();
        pause("Quan sát dữ liệu trả về sau khi lọc");
    }

    /** Đóng popup để không che bảng dữ liệu sau khi đã ghi nhận các chip được chọn. */
    private void closeFilterPopupForResultObservation() {
        if (visibleElements(OPEN_FILTER).isEmpty()) {
            return;
        }
        clickPageButtonStable(
                FILTER_TRIGGER, "Đóng bộ lọc để quan sát trạng thái đơn");
        wait.until(d -> visibleElements(OPEN_FILTER).isEmpty());
    }

    /**
     * Cuộn xuống bảng sau mỗi case lọc để nhìn rõ trạng thái và dữ liệu trả về.
     */
    private void scrollToFilteredResult(
            List<String> orderStatuses,
            List<String> paymentStatuses,
            List<String> paymentMethods) {
        WebElement target = visibleElements(ORDER_TABLE).stream()
                .findFirst()
                .orElseGet(() -> driver.findElement(By.tagName("main")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'start', behavior:'smooth'});"
                        + "window.scrollBy(0, -80);",
                target);
        highlight(target);
        pause("Cuộn xuống quan sát kết quả lọc ["
                + filterSummary(
                        orderStatuses, paymentStatuses, paymentMethods)
                + "]");
    }

    private FilterResult currentFilterResult(
            List<String> expectedOrderStatuses,
            List<String> expectedPaymentStatuses,
            List<String> expectedPaymentMethods,
            List<String> selected) {
        List<String> rows = visibleElements(By.cssSelector(
                        "table[aria-label='Table about Order Uniform Management']"
                                + " tbody tr[role='row']"))
                .stream()
                .map(this::elementText)
                .filter(text -> !text.isBlank())
                .toList();
        boolean emptyState = rows.isEmpty()
                && (totalDisplayed() == 0
                || normalizedMainText().contains("khong co du lieu")
                || normalizedMainText().contains("chua co du lieu"));
        return new FilterResult(
                selected,
                rows,
                totalDisplayed(),
                emptyState,
                expectedOrderStatuses,
                expectedPaymentStatuses,
                expectedPaymentMethods);
    }

    /** Ghép điều kiện để console cho biết bảng đang hiển thị bộ lọc nào. */
    private String filterSummary(
            List<String> orderStatuses,
            List<String> paymentStatuses,
            List<String> paymentMethods) {
        String summary = String.join(" + ", concat(
                orderStatuses, paymentStatuses, paymentMethods));
        return summary.isBlank() ? "Không còn điều kiện lọc" : summary;
    }

    private List<String> selectedOptions(WebElement popup) {
        return popup.findElements(By.cssSelector("button"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> !button.getText().trim().equals("Đặt lại"))
                .filter(this::isSelectedButton)
                .map(button -> button.getText().trim())
                .filter(text -> !text.isBlank())
                .toList();
    }

    private boolean isOptionSelected(WebElement popup, String option) {
        return isSelectedButton(buttonIn(popup, option));
    }

    private boolean isSelectedButton(WebElement button) {
        String className = button.getAttribute("class");
        return "true".equals(button.getAttribute("aria-pressed"))
                || "true".equals(button.getAttribute("data-selected"))
                || className != null && !className.contains("bg-white");
    }

    private String selectionFingerprint(WebElement button) {
        return button.getAttribute("class") + "|"
                + button.getAttribute("aria-pressed") + "|"
                + button.getAttribute("data-selected");
    }

    private List<String> visibleOptionTexts(WebElement popup) {
        return popup.findElements(By.cssSelector("button"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(button -> button.getText().trim())
                .filter(text -> !text.isBlank())
                .toList();
    }

    private WebElement buttonIn(WebElement container, String text) {
        return container.findElements(By.xpath(
                        ".//button[normalize-space()=" + xpathLiteral(text) + "]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Bộ lọc thiếu tùy chọn " + text + "."));
    }

    private boolean hasButton(WebElement container, String text) {
        return container.findElements(By.xpath(
                        ".//button[normalize-space()=" + xpathLiteral(text) + "]"))
                .stream().anyMatch(WebElement::isDisplayed);
    }

    @SafeVarargs
    private static List<String> concat(List<String>... groups) {
        return java.util.Arrays.stream(groups)
                .flatMap(List::stream)
                .toList();
    }

    /** Nội dung và option của popup bộ lọc. */
    public record FilterFormSnapshot(
            String content,
            List<String> optionTexts,
            boolean resetButton) {
    }

    /** Dữ liệu và lựa chọn sau khi áp dụng một tổ hợp bộ lọc. */
    public record FilterResult(
            List<String> selectedOptions,
            List<String> rowTexts,
            int totalDisplayed,
            boolean emptyState,
            List<String> expectedOrderStatuses,
            List<String> expectedPaymentStatuses,
            List<String> expectedPaymentMethods) {
        /** Mọi dòng phải thỏa OR trong từng nhóm và AND giữa các nhóm. */
        public boolean allRowsMatch() {
            return rowTexts.stream().allMatch(row ->
                    matchesAny(row, expectedOrderStatuses)
                            && matchesAny(row, expectedPaymentStatuses)
                            && matchesAny(row, expectedPaymentMethods));
        }

        /** Tất cả chip yêu cầu phải đang được chọn. */
        public boolean allExpectedOptionsSelected() {
            return selectedOptions.containsAll(concat(
                    expectedOrderStatuses,
                    expectedPaymentStatuses,
                    expectedPaymentMethods));
        }

        private static boolean matchesAny(String row, List<String> expected) {
            if (expected.isEmpty()) {
                return true;
            }
            String normalized = TextNormalizer.normalize(row);
            return expected.stream()
                    .map(TextNormalizer::normalize)
                    .anyMatch(normalized::contains);
        }
    }

    /** Cấu trúc điều khiển tìm kiếm theo tên hoặc số điện thoại. */
    public record SearchFormSnapshot(
            String placeholder,
            String ariaLabel,
            List<String> typeOptions,
            String selectedType) {
    }

    /** Dữ liệu khách hàng lấy động từ một dòng đơn hàng. */
    public record CustomerSearchData(
            String name,
            String phone,
            String rowText) {
        /** Bỏ mã +84 khỏi số hiển thị để dùng đúng chuỗi số mà search chấp nhận. */
        public String searchablePhone() {
            String digits = phone.replaceAll("\\D", "");
            return digits.startsWith("84") && digits.length() > 2
                    ? digits.substring(2)
                    : digits;
        }

        /** Chọn từ dài nhất trong tên để giảm khả năng kết quả partial quá rộng. */
        public String longestNamePart() {
            return java.util.Arrays.stream(name.trim().split("\\s+"))
                    .max(java.util.Comparator.comparingInt(String::length))
                    .orElse(name);
        }
    }

    /** Kết quả và điều khiển search sau khi React tải lại bảng. */
    public record SearchResult(
            String requestedType,
            String keyword,
            String inputValue,
            String selectedType,
            List<CustomerSearchData> customers,
            int totalDisplayed,
            boolean emptyState) {
        /** Mọi dòng phải khớp đúng trường đang được người dùng lựa chọn. */
        public boolean allRowsMatch() {
            if (keyword.isBlank()) {
                return true;
            }
            return customers.stream().allMatch(customer ->
                    "phone".equals(requestedType)
                            ? canonicalPhone(customer.phone())
                                    .contains(canonicalPhone(keyword))
                            : TextNormalizer.normalize(customer.name())
                                    .contains(TextNormalizer.normalize(keyword)));
        }

        private static String canonicalPhone(String value) {
            String digits = value.replaceAll("\\D", "");
            if (digits.startsWith("84") && digits.length() > 2) {
                return digits.substring(2);
            }
            return digits.startsWith("0") && digits.length() > 1
                    ? digits.substring(1)
                    : digits;
        }
    }

    /** Cấu trúc và giá trị mặc định của drawer tạo đơn. */
    public record CreateOrderFormSnapshot(
            String content,
            List<String> comboboxPlaceholders,
            long requiredLabelCount,
            boolean pendingSelected,
            boolean unpaidSelected,
            boolean cancelButton,
            boolean confirmButton) {
    }

    /** Kết quả render sau khi chọn combo thật. */
    public record ComboSelectionResult(
            String selectedCombo,
            String drawerContent,
            int comboboxesBefore,
            int comboboxesAfter,
            long numberInputCount) {
    }

    /** Nội dung thực tế của ghi chú và địa chỉ. */
    public record TextEntryResult(String note, String address) {
    }

    /** Trạng thái drawer, validation và tổng dữ liệu sau khi Xác nhận. */
    public record CreateValidationResult(
            boolean drawerOpen,
            String bodyText,
            long invalidControlCount,
            Set<CreateRequiredField> invalidFields,
            int totalBefore,
            int totalAfter) {
    }

    /** Bốn vùng được nghiệp vụ xác nhận là bắt buộc khi tạo đơn. */
    public enum CreateRequiredField {
        PAYMENT_METHOD,
        COMBO,
        WORKER,
        ADDRESS
    }

    /** Dữ liệu form còn giữ lại sau khi submit validation thất bại. */
    public record CreateFormValues(
            String drawerContent,
            String paymentMethod,
            String note,
            String address) {
    }

    /** Kết quả tạo đơn thật cùng dữ liệu động đã chọn. */
    public record CreateSubmissionResult(
            boolean created,
            int totalBefore,
            int totalAfter,
            String combo,
            String worker,
            String bodyText,
            String paymentStatus,
            String paymentMethod,
            String address,
            String note,
            String latestRowText) {
    }

    /** Kết quả luồng tạo nâng cao với nhiều combo, Size và số lượng. */
    public record AdvancedCreateResult(
            boolean created,
            int totalBefore,
            int totalAfter,
            String firstCombo,
            String secondCombo,
            String changedSize,
            String worker,
            String content) {
    }

    /** Kết quả chống tạo trùng khi người dùng bấm Xác nhận liên tiếp. */
    public record DoubleSubmitResult(
            int totalBefore,
            int totalAfter,
            boolean secondClickAccepted,
            String latestRowText) {
    }

    /** Kết quả submit khi combo hoặc Size đang thiếu tồn kho. */
    public record UnavailableStockResult(
            boolean shortageFound,
            boolean drawerOpen,
            int totalBefore,
            int totalAfter,
            String content) {
    }

    /** Trạng thái chip sau hai lần chọn liên tiếp. */
    public record ToggleResult(
            boolean selectedAfterFirstClick,
            boolean selectedAfterSecondClick,
            int totalDisplayed) {
    }

    /** Trạng thái trước/sau thao tác đặt lại bộ lọc. */
    public record ResetResult(
            boolean hadSelection,
            List<String> selectedAfterReset,
            int initialTotal,
            int totalAfterReset) {
    }
}
