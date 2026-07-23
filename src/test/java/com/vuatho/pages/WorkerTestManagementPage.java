package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Page Object cho menu Bài kiểm tra của thợ. */
public class WorkerTestManagementPage {
    public static final String ROUTE = "/vuatho/tested";

    private static final By TABLE = By.cssSelector(
            "table[aria-label='Table about Tested Management']");
    private static final By DATA_ROWS = By.cssSelector(
            "table[aria-label='Table about Tested Management'] tbody tr[data-key]");
    private static final By SEARCH = By.cssSelector(
            "input[aria-label='Tìm theo tên tài khoản hoặc ID']");
    private static final By FILTER = By.cssSelector("button[title='Filter']");
    private static final By RESET = By.cssSelector("button[title='Reset']");
    private static final By PAGINATION = By.cssSelector(
            "nav[aria-label='pagination navigation'][data-slot='base']");
    private static final By DRAWERS = By.cssSelector("div[aria-label^='drawer-']");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WorkerTestManagementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(90));
        this.wait.pollingEvery(Duration.ofMillis(300));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    public WorkerTestManagementPage open() {
        driver.get(TestConfig.baseUrl().replaceAll("/+$", "") + ROUTE);
        wait.until(d -> d.getCurrentUrl().contains(ROUTE));
        waitForTable();
        return this;
    }

    public List<String> columnHeaders() {
        return driver.findElement(TABLE)
                .findElements(By.cssSelector("th[role='columnheader']"))
                .stream()
                .map(WebElement::getText)
                .map(String::trim)
                .toList();
    }

    public List<TestRow> rows() {
        List<TestRow> result = new ArrayList<>();
        for (WebElement row : driver.findElements(DATA_ROWS)) {
            List<WebElement> cells = row.findElements(By.cssSelector(
                    "td[role='rowheader'],td[role='gridcell']"));
            if (cells.size() < 7) continue;
            result.add(new TestRow(
                    row.getAttribute("data-key"),
                    cells.get(0).getText().trim(),
                    cells.get(1).getText().trim(),
                    cells.get(2).getText().trim(),
                    cells.get(3).getText().trim(),
                    cells.get(4).getText().trim(),
                    Status.fromLabel(cells.get(5).getText()).orElse(null),
                    cells.get(6).getText().trim()));
        }
        return result;
    }

    public int totalDisplayed() {
        WebElement label = exactVisible(By.xpath(
                "//span[starts-with(normalize-space(),'Tổng hiển thị:')]"));
        String digits = label.getText().replaceAll("\\D", "");
        if (digits.isBlank()) {
            throw new IllegalStateException("Không đọc được Tổng hiển thị.");
        }
        return Integer.parseInt(digits);
    }

    public String searchValue() {
        return driver.findElement(SEARCH).getAttribute("value");
    }

    public WorkerTestManagementPage search(String keyword) {
        List<String> before = rowIds();
        WebElement input = driver.findElement(SEARCH);
        observe(input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), keyword);
        wait.until(d -> keyword.equals(d.findElement(SEARCH).getAttribute("value")));
        waitForReturnedData(before, null);
        return this;
    }

    public WorkerTestManagementPage selectStatus(Status status) {
        List<String> before = rowIds();
        openFilter();
        WebElement option = wait.until(d -> findVisibleStatusOption(status));
        observe(option);
        option.click();
        waitForReturnedData(before, status);
        closeFilterIfOpen();
        return this;
    }

    public WorkerTestManagementPage resetStatusFilter() {
        List<String> before = rowIds();
        openFilter();
        WebElement reset = wait.until(d -> findVisibleClickableByText("Đặt lại"));
        observe(reset);
        reset.click();
        waitForReturnedData(before, null);
        closeFilterIfOpen();
        return this;
    }

    public WorkerTestManagementPage reset() {
        WebElement reset = exactVisible(RESET);
        observe(reset);
        reset.click();
        wait.until(d -> {
            waitForTable();
            return d.findElement(SEARCH).getAttribute("value").isBlank()
                    && activePage() == 1;
        });
        return this;
    }

    public boolean hasEmptyState() {
        String text = driver.findElement(TABLE).getText();
        return rows().isEmpty() && (text.contains("Chưa có dữ liệu")
                || text.contains("Không có dữ liệu")
                || text.contains("No data"));
    }

    public int activePage() {
        String page = visiblePagination().getAttribute("data-active-page");
        return page != null && page.matches("\\d+") ? Integer.parseInt(page) : 1;
    }

    public int totalPages() {
        String total = visiblePagination().getAttribute("data-total");
        return total != null && total.matches("\\d+") ? Integer.parseInt(total) : 1;
    }

    public WorkerTestManagementPage goToPage(int page) {
        List<String> before = rowIds();
        WebElement pagination = visiblePagination();
        observe(pagination);
        WebElement control = pagination.findElements(By.cssSelector(
                        "[role='button'][data-slot='item']"))
                .stream()
                .filter(item -> ("pagination item " + page)
                        .equalsIgnoreCase(item.getAttribute("aria-label")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy nút trang " + page));
        observe(control);
        control.click();
        wait.until(d -> {
            waitForTable();
            return activePage() == page
                    && (rows().isEmpty() || !rowIds().equals(before));
        });
        observe(visiblePagination());
        return this;
    }

    public WorkerTestManagementPage goToNextPage() {
        return clickPaginationControl("next page button", activePage() + 1);
    }

    public WorkerTestManagementPage goToPreviousPage() {
        return clickPaginationControl("previous page button", activePage() - 1);
    }

    public DetailSnapshot openFirstRow() {
        WebElement row = driver.findElements(DATA_ROWS).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Danh sách bài kiểm tra không có dữ liệu."));
        return openRow(row);
    }

    public DetailSnapshot openFirstRowInStatus(Status status) {
        selectStatus(status);
        return openFirstRow();
    }

    public DetailSnapshot openRowById(String id) {
        WebElement row = driver.findElements(DATA_ROWS).stream()
                .filter(item -> id.equals(item.getAttribute("data-key")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy bài kiểm tra #" + id + " trên trang hiện tại."));
        return openRow(row);
    }

    public void closeDrawer() {
        WebElement drawer = visibleDrawer();
        if (drawer == null) return;
        WebElement close = drawer.findElements(By.cssSelector("button[type='button']"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
        if (close != null) {
            observe(close);
            close.click();
        } else {
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        }
        wait.until(d -> visibleDrawer() == null);
    }

    private WorkerTestManagementPage clickPaginationControl(
            String ariaLabel, int expectedPage) {
        List<String> before = rowIds();
        WebElement pagination = visiblePagination();
        observe(pagination);
        WebElement control = pagination.findElements(
                        By.cssSelector("[role='button'][aria-label='" + ariaLabel + "']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy " + ariaLabel));
        observe(control);
        control.click();
        wait.until(d -> {
            waitForTable();
            return activePage() == expectedPage
                    && (rows().isEmpty() || !rowIds().equals(before));
        });
        observe(visiblePagination());
        return this;
    }

    private DetailSnapshot openRow(WebElement row) {
        String id = row.getAttribute("data-key");
        observe(row);
        row.click();
        WebElement drawer = wait.until(d -> {
            WebElement candidate = visibleDrawer();
            if (candidate == null) return null;
            String text = candidate.getText();
            return text.contains("Chi tiết bài kiểm tra")
                    && text.contains("Nội dung bài kiểm tra")
                    && text.length() > 80 ? candidate : null;
        });
        observe(drawer);
        String text = drawer.getText();
        long visibleFormFields = drawer.findElements(By.cssSelector(
                        "input,textarea,select,[contenteditable='true']"))
                .stream().filter(WebElement::isDisplayed).count();
        List<String> visibleButtonTexts = drawer.findElements(By.tagName("button"))
                .stream().filter(WebElement::isDisplayed)
                .map(WebElement::getText).map(String::trim)
                .filter(value -> !value.isBlank()).toList();
        return new DetailSnapshot(
                id,
                text,
                countOccurrences(text, "Câu "),
                countOccurrences(text, "Đáp án:"),
                countOccurrences(text, "Lần "),
                visibleFormFields,
                visibleButtonTexts);
    }

    private WebElement findVisibleStatusOption(Status status) {
        return findVisibleClickableByText(status.label());
    }

    private WebElement findVisibleClickableByText(String label) {
        String expected = TextNormalizer.normalize(label);
        for (WebElement element : driver.findElements(By.xpath(
                "//*[self::button or self::label or @role='button' or @role='option']"))) {
            if (!element.isDisplayed()) continue;
            if (TextNormalizer.normalize(element.getText()).equals(expected)) {
                return element;
            }
        }
        for (WebElement element : driver.findElements(By.xpath(
                "//*[normalize-space()='" + label + "']"))) {
            if (!element.isDisplayed()) continue;
            WebElement clickable = (WebElement) ((JavascriptExecutor) driver)
                    .executeScript(
                            "return arguments[0].closest("
                                    + "'button,label,[role=button],[role=option]') || arguments[0];",
                            element);
            if (clickable != null && clickable.isDisplayed()) return clickable;
        }
        return null;
    }

    private void openFilter() {
        WebElement trigger = exactVisible(FILTER);
        if ("true".equalsIgnoreCase(trigger.getAttribute("aria-expanded"))) {
            return;
        }
        observe(trigger);
        trigger.click();
        wait.until(d -> "true".equalsIgnoreCase(
                d.findElement(FILTER).getAttribute("aria-expanded")));
    }

    private void closeFilterIfOpen() {
        WebElement trigger = exactVisible(FILTER);
        if (!"true".equalsIgnoreCase(trigger.getAttribute("aria-expanded"))) {
            return;
        }
        observe(trigger);
        trigger.click();
        wait.until(d -> !"true".equalsIgnoreCase(
                d.findElement(FILTER).getAttribute("aria-expanded")));
    }

    private void waitForReturnedData(List<String> before, Status expectedStatus) {
        wait.until(d -> {
            waitForTable();
            List<TestRow> current = rows();
            boolean statusMatches = expectedStatus == null
                    || current.isEmpty()
                    || current.stream().allMatch(row -> row.status() == expectedStatus);
            boolean resultChanged = current.isEmpty() || !rowIds().equals(before);
            return statusMatches && resultChanged;
        });
    }

    private void waitForTable() {
        wait.until(d -> {
            List<WebElement> tables = d.findElements(TABLE);
            if (tables.isEmpty()) return false;
            WebElement table = tables.get(0);
            boolean loading = !table.findElements(By.cssSelector(
                    "tbody[data-loading='true'],[role='progressbar']")).isEmpty();
            String text = table.getText();
            return !loading && (!d.findElements(DATA_ROWS).isEmpty()
                    || text.contains("Chưa có dữ liệu")
                    || text.contains("Không có dữ liệu")
                    || text.contains("No data"));
        });
    }

    private List<String> rowIds() {
        return driver.findElements(DATA_ROWS).stream()
                .map(row -> row.getAttribute("data-key")).toList();
    }

    private WebElement visiblePagination() {
        return wait.until(d -> d.findElements(PAGINATION).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null));
    }

    private WebElement visibleDrawer() {
        return driver.findElements(DRAWERS).stream()
                .filter(WebElement::isDisplayed)
                .filter(drawer -> {
                    String classes = drawer.getAttribute("class");
                    return classes == null || !classes.contains("translate-x-[100%]");
                })
                .filter(drawer -> drawer.getText().contains("Chi tiết bài kiểm tra"))
                .findFirst().orElse(null);
    }

    private WebElement exactVisible(By locator) {
        return wait.until(d -> d.findElements(locator).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null));
    }

    private void observe(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("""
                arguments[0].scrollIntoView({
                  behavior: 'smooth', block: 'center', inline: 'nearest'
                });
                """, element);
        if (!TestConfig.headless()) {
            new Actions(driver).pause(Duration.ofSeconds(2)).perform();
        }
    }

    private static int countOccurrences(String text, String token) {
        int count = 0;
        int start = 0;
        while ((start = text.indexOf(token, start)) >= 0) {
            count++;
            start += token.length();
        }
        return count;
    }

    public enum Status {
        INITIALIZED("Khởi tạo"),
        IN_PROGRESS("Đang làm"),
        PENDING("Chờ xử lý"),
        PASSED("Đã đậu"),
        FAILED("Đã rớt");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public static Optional<Status> fromLabel(String value) {
            String actual = TextNormalizer.normalize(value);
            for (Status status : values()) {
                if (TextNormalizer.normalize(status.label).equals(actual)) {
                    return Optional.of(status);
                }
            }
            return Optional.empty();
        }
    }

    public record TestRow(
            String key,
            String id,
            String service,
            String account,
            String attempts,
            String score,
            Status status,
            String createdAt) {
    }

    public record DetailSnapshot(
            String id,
            String text,
            int questionCount,
            int answerCount,
            int attemptHistoryCount,
            long visibleFormFieldCount,
            List<String> visibleButtonTexts) {
    }
}
