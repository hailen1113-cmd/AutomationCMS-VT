package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Thao tác dùng chung cho tám nhóm chuyên biệt của màn hình Lịch sử giao dịch. */
public class TransactionCategoryPage extends UniformUiPage {
    private static final String ALL_ROUTE = "/vuatho/transaction?tab=all";
    private static final By GRID = By.cssSelector(
            "[role='grid'][aria-label='Table about Transaction Management']");
    private static final By DATA_ROWS = By.cssSelector("tbody tr");
    private static final Pattern PHONE = Pattern.compile("\\+?[0-9]{9,12}");

    private final Category category;

    public TransactionCategoryPage(WebDriver driver, Category category) {
        super(driver);
        this.category = category;
    }

    public void openRepresentative() {
        open(category.subtypes().get(0));
    }

    public void open(Subtype subtype) {
        openRoute(subtype.route());
        waitForTable();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public String activeGroupText() {
        By locator = groupButtonLocator(category.label());
        WebElement button = wait.until(d -> d.findElements(locator).stream().findFirst().orElse(null));
        return elementText(button);
    }

    public List<String> groupOptions() {
        if (category == Category.SYSTEM) {
            return List.of(category.label());
        }
        WebElement button = groupButton(category.label());
        // Có thể popup của testcase trước vẫn còn mở trong SPA. Chỉ click khi
        // trigger đang đóng để không vô tình đóng menu rồi chờ đủ 45 giây.
        if (!"true".equals(button.getAttribute("aria-expanded"))) {
            click(button, "Mở nhóm giao dịch " + category.label());
        }
        button = groupButton(category.label());
        String controls = button.getAttribute("aria-controls");
        WebElement menu = wait.until(d -> d.findElements(By.id(controls)).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null));
        List<String> options = wait.until(d -> {
            List<String> values = menu.findElements(By.cssSelector("[role='menuitemradio']")).stream()
                    .filter(WebElement::isDisplayed)
                    .map(this::elementText)
                    .map(this::normalizeOptionLabel)
                    .filter(value -> !value.isBlank())
                    .toList();
            return values.isEmpty() ? null : values;
        });
        menu.sendKeys(Keys.ESCAPE);
        wait.until(d -> d.findElements(By.id(controls)).stream().noneMatch(WebElement::isDisplayed));
        return options;
    }

    public DropdownSemanticsSnapshot dropdownSemantics() {
        WebElement trigger = groupButton(category.label());
        String expandedBefore = trigger.getAttribute("aria-expanded");
        click(trigger, "Mở dropdown " + category.label());
        trigger = groupButton(category.label());
        String controls = trigger.getAttribute("aria-controls");
        WebElement menu = wait.until(d -> d.findElements(By.id(controls)).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null));
        List<DropdownOption> options = menu.findElements(By.cssSelector("[role='menuitemradio']"))
                .stream().filter(WebElement::isDisplayed)
                .map(option -> new DropdownOption(elementText(option).trim(),
                        option.getAttribute("data-key"), option.getAttribute("aria-checked"),
                        option.getAttribute("data-selected"))).toList();
        DropdownSemanticsSnapshot result = new DropdownSemanticsSnapshot(
                expandedBefore, trigger.getAttribute("aria-expanded"), trigger.getAttribute("aria-haspopup"),
                controls, menu.getAttribute("id"), menu.getAttribute("aria-label"), options);
        click(groupButton(category.label()), "Đóng dropdown " + category.label());
        wait.until(d -> d.findElements(By.id(controls)).stream().noneMatch(WebElement::isDisplayed));
        return result;
    }

    public DropdownCloseSnapshot closeDropdownWithEscape() {
        WebElement trigger = groupButton(category.label());
        click(trigger, "Mở dropdown để kiểm tra đóng bằng Escape");
        String controls = groupButton(category.label()).getAttribute("aria-controls");
        WebElement menu = wait.until(d -> d.findElements(By.id(controls)).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null));
        menu.sendKeys(Keys.ESCAPE);
        boolean menuClosed = wait.until(d -> d.findElements(By.id(controls)).stream()
                .noneMatch(WebElement::isDisplayed));
        return new DropdownCloseSnapshot(groupButton(category.label()).getAttribute("aria-expanded"),
                menuClosed, currentUrl(), activeGroupText());
    }

    public DropdownCloseSnapshot closeDropdownWithTrigger() {
        WebElement trigger = groupButton(category.label());
        click(trigger, "Mở dropdown để kiểm tra đóng bằng trigger");
        String controls = groupButton(category.label()).getAttribute("aria-controls");
        wait.until(d -> d.findElements(By.id(controls)).stream().anyMatch(WebElement::isDisplayed));
        click(groupButton(category.label()), "Đóng dropdown bằng trigger");
        boolean menuClosed = wait.until(d -> d.findElements(By.id(controls)).stream()
                .noneMatch(WebElement::isDisplayed));
        return new DropdownCloseSnapshot(groupButton(category.label()).getAttribute("aria-expanded"),
                menuClosed, currentUrl(), activeGroupText());
    }

    public DropdownSelectionSnapshot selectSubtypeFromDropdown(Subtype subtype) {
        click(groupButton(category.label()), "Mở dropdown để chọn " + subtype.label());
        By optionLocator = By.cssSelector("[role='menuitemradio'][data-key='" + subtype.type() + "']");
        click(visible(optionLocator), "Chọn loại " + subtype.label());
        waitForTable();
        wait.until(d -> currentUrl().contains("type=" + subtype.type()));
        String triggerText = activeGroupText();
        click(groupButton(category.label()), "Mở lại dropdown để kiểm tra trạng thái chọn");
        WebElement selected = visible(optionLocator);
        long selectedCount = visibleElements(By.cssSelector("[role='menuitemradio']")).stream()
                .filter(option -> "true".equals(option.getAttribute("aria-checked")))
                .count();
        String controls = groupButton(category.label()).getAttribute("aria-controls");
        DropdownSelectionSnapshot result = new DropdownSelectionSnapshot(currentUrl(), triggerText,
                selected.getAttribute("aria-checked"), selected.getAttribute("data-selected"),
                elementText(selected).trim(), selectedCount, false);
        click(groupButton(category.label()), "Đóng dropdown sau khi kiểm tra " + subtype.label());
        boolean menuClosed = wait.until(d -> d.findElements(By.id(controls)).stream()
                .noneMatch(WebElement::isDisplayed));
        return new DropdownSelectionSnapshot(result.url(), result.triggerText(), result.checked(),
                result.selected(), result.optionText(), result.selectedCount(), menuClosed);
    }

    public SubtypeChangeSnapshot chooseSubtypeFromDropdown(Subtype subtype) {
        String expectedType = "type=" + subtype.type();
        if (currentUrl().contains("tab=" + subtype.tab())
                && currentUrl().contains(expectedType)
                && activeGroupText().contains(subtype.label())) {
            return new SubtypeChangeSnapshot(currentUrl(), activeGroupText());
        }
        click(groupButton(category.label()), "Mở dropdown để chuyển sang " + subtype.label());
        By optionLocator = By.cssSelector("[role='menuitemradio'][data-key='" + subtype.type() + "']");
        click(visible(optionLocator), "Chọn loại " + subtype.label());
        wait.until(d -> currentUrl().contains("tab=" + subtype.tab())
                && currentUrl().contains(expectedType));
        waitForTable();
        wait.until(d -> activeGroupText().contains(subtype.label()));
        return new SubtypeChangeSnapshot(currentUrl(), activeGroupText());
    }

    public List<String> headers() {
        return grid().findElements(By.cssSelector("thead th")).stream()
                .filter(WebElement::isDisplayed)
                .map(this::elementText)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    public LayoutSnapshot layout() {
        Map<String, Boolean> controls = new LinkedHashMap<>();
        for (String ariaLabel : category.filterAriaLabels()) {
            controls.put(ariaLabel, !visibleElements(By.cssSelector(
                    "[aria-label=" + cssString(ariaLabel) + "]")).isEmpty());
        }
        controls.put("Xuất Excel", visibleButton("Xuất Excel"));
        controls.put("Reset", !visibleElements(By.cssSelector("button[title='Reset']")).isEmpty());
        return new LayoutSnapshot(currentUrl(), headers(), controls, rows().size());
    }

    public List<TransactionRow> rows() {
        waitForTable();
        return grid().findElements(DATA_ROWS).stream()
                .filter(WebElement::isDisplayed)
                .map(this::toRow)
                .filter(row -> row != null)
                .toList();
    }

    public SearchSnapshot searchByFirstUser() {
        List<TransactionRow> before = rows();
        TransactionRow source = before.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Không có giao dịch để kiểm tra tìm kiếm."));
        String userText = source.value("Người dùng");
        Matcher matcher = PHONE.matcher(userText);
        String query = matcher.find() ? matcher.group() : firstSearchToken(userText);
        WebElement input = visible(By.cssSelector("[aria-label='search-name-phone-filter']"));
        fill(input, query, "Tìm giao dịch theo người dùng thật");
        waitForTable();
        List<TransactionRow> filtered = rows();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        waitForTable();
        return new SearchSnapshot(query, before, filtered, rows(), currentUrl());
    }

    public boolean hasSearchableUser() {
        return rows().stream()
                .map(row -> row.value("Người dùng"))
                .map(this::normalizeText)
                .anyMatch(value -> value.matches(".*[a-z0-9].*"));
    }

    public EmptySearchSnapshot unmatchedSearchAndReset() {
        return unmatchedSearchAndReset("NO_ASSISTANT_TRANSACTION_987654321");
    }

    public EmptySearchSnapshot unmatchedSearchAndReset(String query) {
        List<TransactionRow> before = rows();
        WebElement input = visible(By.cssSelector("[aria-label='search-name-phone-filter']"));
        fill(input, query, "Tìm từ khóa chắc chắn không có kết quả");
        waitForTable();
        String emptyText = mainText();
        boolean empty = isEmptyState();
        resetFilters();
        return new EmptySearchSnapshot(query, empty, emptyText, before, rows(),
                currentUrl(), activeGroupText());
    }

    public FilterSnapshot firstAvailableFilter() {
        String ariaLabel = category.selectFilterAriaLabels().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Nhóm không có select filter."));
        WebElement button = visible(By.cssSelector("button[aria-label=" + cssString(ariaLabel) + "]"));
        click(button, "Mở bộ lọc " + ariaLabel);
        List<String> options = wait.until(d -> {
            List<String> values = visibleElements(By.cssSelector("li[role='option']")).stream()
                    .map(this::elementText).map(String::trim).filter(value -> !value.isBlank()).toList();
            return values.isEmpty() ? null : values;
        });
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        String beforeUrl = currentUrl();
        resetFilters();
        return new FilterSnapshot(ariaLabel, options, beforeUrl, currentUrl(), activeGroupText());
    }

    public SortSnapshot sortAmountBothDirections() {
        List<BigDecimal> ascending = sortAmount(false);
        List<BigDecimal> descending = sortAmount(true);
        return new SortSnapshot(ascending, descending, currentUrl());
    }

    public DetailSnapshot openAndCloseFirstDetail() {
        List<WebElement> elements = dataRowElements();
        if (elements.isEmpty()) {
            throw new IllegalStateException("Không có giao dịch để mở chi tiết.");
        }
        String source = elementText(elements.get(0));
        click(elements.get(0), "Mở chi tiết giao dịch " + category.label());
        WebElement drawer = wait.until(d -> d.findElements(
                        By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> {
                    String text = elementText(element);
                    return text.contains("Thông tin giao dịch") && text.contains("Số tiền");
                }).findFirst().orElse(null));
        String openedUrl = currentUrl();
        String drawerText = elementText(drawer);
        List<WebElement> drawerButtons = drawer.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed).filter(WebElement::isEnabled).toList();
        WebElement close = drawerButtons.stream()
                .filter(button -> "Hủy".equals(elementText(button).trim()))
                .findFirst().orElseGet(() -> drawerButtons.stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Không thấy nút đóng chi tiết.")));
        click(close, "Đóng chi tiết giao dịch");
        boolean closed = wait.until(d -> d.findElements(
                        By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).stream()
                .noneMatch(WebElement::isDisplayed));
        return new DetailSnapshot(source, openedUrl, drawerText, closed, currentUrl());
    }

    public PaginationSnapshot paginationAndReset() {
        int beforePage = activePage();
        boolean previousDisabled = paginationDisabled("previous page button");
        boolean nextDisabled = paginationDisabled("next page button");
        int afterNext = beforePage;
        if (!nextDisabled) {
            click(visible(By.cssSelector(
                    "nav[aria-label='pagination navigation'] [aria-label='next page button']")),
                    "Chuyển trang giao dịch");
            waitForTable();
            afterNext = activePage();
        }
        resetFilters();
        return new PaginationSnapshot(beforePage, afterNext, activePage(), previousDisabled,
                nextDisabled, currentUrl(), activeGroupText());
    }

    public ExportSnapshot exportCurrentSubtype() {
        int visibleRows = rows().size();
        Path directory = Path.of(TestConfig.downloadDirectory()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IllegalStateException("Không tạo được thư mục tải Excel.", exception);
        }
        Map<Path, FileFingerprint> before = completedFileVersions(directory);
        click(visible(By.xpath("//main//button[normalize-space()='Xuất Excel'"
                + " or .//*[normalize-space()='Xuất Excel']]")), "Xuất Excel " + category.label());
        try {
            Path file = new WebDriverWait(driver, TestConfig.exportDownloadTimeout())
                    .pollingEvery(Duration.ofMillis(300))
                    .until(d -> {
                        Path downloaded = completedFileVersions(directory).entrySet().stream()
                                .filter(entry -> !entry.getValue().equals(before.get(entry.getKey())))
                                .map(Map.Entry::getKey).findFirst().orElse(null);
                        if (downloaded != null) {
                            return downloaded;
                        }
                        String uiError = visibleElements(By.cssSelector(
                                        ".Toastify__toast-body[role='alert']"))
                                .stream().map(this::elementText)
                                .filter(text -> {
                                    String normalized = normalizeText(text);
                                    return normalized.contains("loi")
                                            || normalized.contains("that bai")
                                            || normalized.contains("khong the");
                                })
                                .findFirst().orElse(null);
                        if (uiError != null) {
                            throw new IllegalStateException(
                                    "UI báo xuất Excel thất bại: " + uiError);
                        }
                        return null;
                    });
            return new ExportSnapshot(file, visibleRows, currentUrl());
        } catch (TimeoutException exception) {
            throw new IllegalStateException("Xuất Excel không tạo file tải xuống.", exception);
        }
    }

    private List<BigDecimal> sortAmount(boolean descending) {
        for (int attempt = 0; attempt < 3; attempt++) {
            click(headerButton("Số tiền"), "Sắp xếp số tiền " + (descending ? "giảm" : "tăng"));
            waitForTable();
            List<BigDecimal> values = rows().stream().map(row -> row.amount("Số tiền")).toList();
            Comparator<BigDecimal> comparator = descending
                    ? Comparator.reverseOrder() : Comparator.naturalOrder();
            if (isOrdered(values, comparator)) {
                return values;
            }
        }
        throw new AssertionError("Không thể sắp xếp số tiền đúng chiều trên " + category.label());
    }

    private boolean isOrdered(List<BigDecimal> values, Comparator<BigDecimal> comparator) {
        for (int index = 1; index < values.size(); index++) {
            if (comparator.compare(values.get(index - 1), values.get(index)) > 0) {
                return false;
            }
        }
        return true;
    }

    private WebElement headerButton(String header) {
        return visible(By.xpath("//*[@role='grid']//thead//button[normalize-space()="
                + xpathLiteral(header) + "]"));
    }

    private TransactionRow toRow(WebElement element) {
        List<String> cells = element.findElements(By.cssSelector("th,td")).stream()
                .map(this::elementText).map(String::trim).toList();
        if (cells.isEmpty() || cells.stream().anyMatch(value -> value.contains("Đang tải dữ liệu"))) {
            return null;
        }
        List<String> currentHeaders = headers();
        if (cells.size() != currentHeaders.size()) {
            return null;
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (int index = 0; index < currentHeaders.size(); index++) {
            values.put(currentHeaders.get(index), cells.get(index));
        }
        return new TransactionRow(values, cells);
    }

    private List<WebElement> dataRowElements() {
        waitForTable();
        return grid().findElements(DATA_ROWS).stream().filter(WebElement::isDisplayed)
                .filter(element -> toRow(element) != null).toList();
    }

    private WebElement groupButton(String label) {
        return visible(groupButtonLocator(label));
    }

    private By groupButtonLocator(String label) {
        return By.xpath("//main//button[starts-with(normalize-space(.),"
                + xpathLiteral(label) + ")]");
    }

    private boolean visibleButton(String label) {
        return !visibleElements(By.xpath("//main//button[normalize-space()=" + xpathLiteral(label)
                + " or .//*[normalize-space()=" + xpathLiteral(label) + "]]")).isEmpty();
    }

    private void resetFilters() {
        click(visible(By.cssSelector("button[title='Reset']")), "Reset bộ lọc " + category.label());
        waitForTable();
    }

    private int activePage() {
        String aria = visibleElements(By.cssSelector(
                        "nav[aria-label='pagination navigation'] [aria-label*='pagination item'][aria-label*='active']"))
                .stream().map(element -> element.getAttribute("aria-label")).findFirst().orElse("1");
        String digits = aria.replaceAll("[^0-9]", "");
        return digits.isBlank() ? 1 : Integer.parseInt(digits);
    }

    private boolean paginationDisabled(String ariaLabel) {
        List<WebElement> elements = visibleElements(By.cssSelector(
                "nav[aria-label='pagination navigation'] [aria-label=" + cssString(ariaLabel) + "]"));
        if (elements.isEmpty()) {
            return true;
        }
        WebElement element = elements.get(0);
        return "true".equals(element.getAttribute("aria-disabled"))
                || "true".equals(element.getAttribute("data-disabled"));
    }

    private void waitForTable() {
        settle(450);
        wait.until(d -> {
            List<WebElement> grids = d.findElements(GRID).stream().filter(WebElement::isDisplayed).toList();
            if (grids.isEmpty()) {
                return false;
            }
            String text = elementText(grids.get(0));
            return !text.contains("Đang tải dữ liệu")
                    && (!grids.get(0).findElements(DATA_ROWS).isEmpty() || isEmptyState());
        });
    }

    private boolean isEmptyState() {
        String text = normalizeText(mainText());
        return text.contains("chua co du lieu") || text.contains("khong co du lieu");
    }

    private WebElement grid() {
        return visible(GRID);
    }

    private String firstSearchToken(String value) {
        return value.trim().split("\\s+")[0];
    }

    private String normalizeOptionLabel(String value) {
        String actual = value.trim().replaceAll("\\s+", " ");
        return category.subtypes().stream()
                .map(Subtype::label)
                .sorted(Comparator.comparingInt(String::length).reversed())
                .filter(label -> actual.equals(label) || actual.startsWith(label + " "))
                .findFirst()
                .orElse(actual);
    }

    private String cssString(String value) {
        return "'" + value.replace("'", "\\'") + "'";
    }

    private Map<Path, FileFingerprint> completedFileVersions(Path directory) {
        try (var files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("(?i).+\\.(xlsx|xls|csv)$"))
                    .collect(Collectors.toMap(path -> path, path -> {
                        try {
                            return new FileFingerprint(Files.size(path),
                                    Files.getLastModifiedTime(path).toMillis());
                        } catch (IOException exception) {
                            return new FileFingerprint(-1, -1);
                        }
                    }));
        } catch (IOException exception) {
            return Map.of();
        }
    }

    public enum Category {
        DEPOSIT("Tiền nạp", List.of(
                new Subtype("Nạp thường", "deposit", 0),
                new Subtype("Bên thứ 3", "deposit", 10),
                new Subtype("Nạp từ DN", "deposit", 19),
                new Subtype("Nạp vào ký quỹ", "deposit", 20),
                new Subtype("Nạp ký quỹ qua NH", "deposit", 34)),
                List.of("Người dùng", "Loại giao dịch", "Trạng thái", "Số tiền", "Cổng thanh toán", "Ngày tạo"),
                List.of("search-name-phone-filter", "trạng thái-filter", "cổng thanh toán-filter", "Chọn khoảng ngày giờ giao dịch")),
        WITHDRAW("Tiền rút", List.of(
                new Subtype("Rút thường", "withdraw", 1),
                new Subtype("Rút thưởng về ví", "withdraw", 5),
                new Subtype("Rút thưởng trực tiếp", "withdraw", 13),
                new Subtype("Rút từ ví chi phí", "withdraw", 21),
                new Subtype("Rút ngưng hợp tác", "withdraw", 23),
                new Subtype("Rút số dư về NH", "withdraw", 35)),
                List.of("Người dùng", "Loại giao dịch", "Trạng thái", "Số tiền", "Ngân hàng", "Ngày tạo"),
                List.of("search-name-phone-filter", "trạng thái-filter", "Chọn khoảng ngày giờ giao dịch")),
        ORDER("Đơn dịch vụ", List.of(
                new Subtype("Đơn dịch vụ", "order", 2),
                new Subtype("DN thanh toán đơn", "order", 22),
                new Subtype("Phí bảo hành", "order", 24),
                new Subtype("Thu bảo hành", "order", 24),
                new Subtype("Chi bảo hành", "order", 37),
                new Subtype("Phí xử phạt", "order", 15)),
                List.of("Người dùng", "Loại giao dịch", "Trạng thái", "Số tiền", "Cổng thanh toán", "Ngày tạo"),
                List.of("search-name-phone-filter", "trạng thái-filter", "cổng thanh toán-filter", "Chọn khoảng ngày giờ giao dịch")),
        REWARD("Thưởng & KM", List.of(
                new Subtype("Hoàn Voucher", "reward", 12),
                new Subtype("Hoàn chiến dịch", "reward", 18)),
                List.of("Người dùng", "Loại giao dịch", "Trạng thái", "Số tiền", "Cổng thanh toán", "Ngày tạo"),
                List.of("search-name-phone-filter", "trạng thái-filter", "cổng thanh toán-filter", "Chọn khoảng ngày giờ giao dịch")),
        FEE("Phí & Doanh thu", List.of(
                new Subtype("Phí kết nối", "fee", 8),
                new Subtype("Phí liên kết ví", "fee", 9),
                new Subtype("Phí chia sẻ vật tư", "fee", 33)),
                List.of("Người dùng", "Loại giao dịch", "Trạng thái", "Số tiền", "Ngày tạo"),
                List.of("search-name-phone-filter", "xuất hoá đơn-filter", "Chọn khoảng ngày giờ giao dịch")),
        INSURANCE("VT Care", List.of(
                new Subtype("Trừ phí VT Care", "insurance", 25),
                new Subtype("Hoàn phí VT Care", "insurance", 26)),
                List.of("Người dùng", "Loại giao dịch", "Trạng thái", "Số tiền", "Cổng thanh toán", "Ngày tạo"),
                List.of("search-name-phone-filter", "trạng thái-filter", "cổng thanh toán-filter", "Chọn khoảng ngày giờ giao dịch")),
        ASSISTANT("Thợ phụ", List.of(
                new Subtype("Phí nền tảng", "assistant", 30),
                new Subtype("Tiền phạt", "assistant", 31)),
                List.of("Người dùng", "Loại giao dịch", "Trạng thái", "Số tiền", "Cổng thanh toán", "Ngày tạo"),
                List.of("search-name-phone-filter", "trạng thái-filter", "cổng thanh toán-filter", "Chọn khoảng ngày giờ giao dịch")),
        SYSTEM("Hệ thống", List.of(new Subtype("Hệ thống", "system", 7)),
                List.of("Người dùng", "Trạng thái", "Số tiền", "Cổng thanh toán", "Ngày tạo"),
                List.of("search-name-phone-filter", "trạng thái-filter", "cổng thanh toán-filter", "Chọn khoảng ngày giờ giao dịch"));

        private final String label;
        private final List<Subtype> subtypes;
        private final List<String> headers;
        private final List<String> filterAriaLabels;

        Category(String label, List<Subtype> subtypes, List<String> headers,
                 List<String> filterAriaLabels) {
            this.label = label;
            this.subtypes = subtypes;
            this.headers = headers;
            this.filterAriaLabels = filterAriaLabels;
        }

        public String label() { return label; }
        public List<Subtype> subtypes() { return subtypes; }
        public List<String> headers() { return headers; }
        public List<String> filterAriaLabels() { return filterAriaLabels; }
        public List<String> selectFilterAriaLabels() {
            return filterAriaLabels.stream()
                    .filter(label -> label.endsWith("-filter") && !label.startsWith("search-"))
                    .toList();
        }
    }

    public record Subtype(String label, String tab, int type) {
        public String route() {
            return "/vuatho/transaction?tab=" + tab + "&type=" + type;
        }
    }

    public record TransactionRow(Map<String, String> values, List<String> cells) {
        public String value(String header) { return values.getOrDefault(header, ""); }
        public BigDecimal amount(String header) {
            String raw = value(header).replace('−', '-').replace('–', '-');
            boolean negative = raw.contains("-");
            String digits = raw.replaceAll("[^0-9]", "");
            BigDecimal amount = digits.isBlank() ? BigDecimal.ZERO : new BigDecimal(digits);
            return negative ? amount.negate() : amount;
        }
        public String signature() { return String.join("|", cells); }
    }

    public record LayoutSnapshot(String url, List<String> headers,
                                 Map<String, Boolean> controls, int visibleRows) {}
    public record SearchSnapshot(String query, List<TransactionRow> before,
                                 List<TransactionRow> filtered, List<TransactionRow> restored,
                                 String url) {}
    public record EmptySearchSnapshot(String query, boolean empty, String pageText,
                                      List<TransactionRow> before, List<TransactionRow> restored,
                                      String url, String activeText) {}
    public record FilterSnapshot(String ariaLabel, List<String> options,
                                 String beforeResetUrl, String afterResetUrl, String activeText) {}
    public record DropdownOption(String label, String key, String checked, String selected) {}
    public record DropdownSemanticsSnapshot(String expandedBefore, String expandedAfter, String hasPopup,
                                             String controls, String menuId, String menuLabel,
                                            List<DropdownOption> options) {}
    public record DropdownCloseSnapshot(String expandedAfter, boolean menuClosed,
                                        String url, String activeText) {}
    public record DropdownSelectionSnapshot(String url, String triggerText, String checked,
                                             String selected, String optionText,
                                             long selectedCount, boolean menuClosed) {}
    public record SubtypeChangeSnapshot(String url, String triggerText) {}
    public record SortSnapshot(List<BigDecimal> ascending, List<BigDecimal> descending, String url) {}
    public record DetailSnapshot(String source, String openedUrl, String drawerText,
                                 boolean closed, String closedUrl) {}
    public record PaginationSnapshot(int beforePage, int afterNextPage, int afterResetPage,
                                     boolean previousDisabled, boolean nextDisabled,
                                     String url, String activeText) {}
    public record ExportSnapshot(Path file, int visibleRows, String url) {}
    private record FileFingerprint(long size, long modifiedAt) {}
}
