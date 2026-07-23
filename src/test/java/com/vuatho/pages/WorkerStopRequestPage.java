package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Page Object cho menu Yêu cầu ngưng hợp tác của thợ. */
public class WorkerStopRequestPage {
    public static final String ROUTE = "/vuatho/stop-request";

    private static final By TABLE = By.cssSelector(
            "table[aria-label='Table about stop cooperation requests']");
    private static final By DATA_ROWS = By.cssSelector(
            "table[aria-label='Table about stop cooperation requests'] tbody tr[data-key]");
    private static final By SEARCH = By.cssSelector(
            "input[aria-label='Tìm kiếm thợ theo tên']");
    private static final By RESET = By.cssSelector("button[title='Reset']");
    private static final By PAGINATION = By.cssSelector(
            "nav[aria-label='pagination navigation'][data-slot='base']");
    private static final By DIALOG = By.cssSelector(
            "section[role='dialog'],div[role='dialog']");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WorkerStopRequestPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(90));
        this.wait.pollingEvery(Duration.ofMillis(300));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    public WorkerStopRequestPage open() {
        driver.get(TestConfig.baseUrl().replaceAll("/+$", "") + ROUTE);
        wait.until(d -> d.getCurrentUrl().contains(ROUTE));
        waitForTable();
        return this;
    }

    public Map<String, Integer> statistics() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String label : List.of(
                "Tổng yêu cầu", "Chờ xử lý", "Đã duyệt", "Đã từ chối", "Đã bỏ qua")) {
            WebElement heading = exactVisible(By.xpath(
                    "//p[normalize-space()='" + label + "']"));
            WebElement card = heading.findElement(By.xpath("./parent::*"));
            int value = card.findElements(By.cssSelector("span")).stream()
                    .map(WebElement::getText).map(String::trim)
                    .filter(text -> text.matches("\\d+"))
                    .mapToInt(Integer::parseInt).findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Thẻ thống kê không có số liệu: " + label));
            result.put(label, value);
        }
        return result;
    }

    public List<String> columnHeaders() {
        return driver.findElement(TABLE)
                .findElements(By.cssSelector("th[role='columnheader']"))
                .stream().map(WebElement::getText).map(String::trim).toList();
    }

    public List<RequestRow> rows() {
        List<RequestRow> result = new ArrayList<>();
        for (WebElement row : driver.findElements(DATA_ROWS)) {
            List<WebElement> cells = row.findElements(By.cssSelector(
                    "td[role='rowheader'],td[role='gridcell']"));
            if (cells.size() < 5) continue;
            List<String> worker = cells.get(0).getText().lines().toList();
            result.add(new RequestRow(
                    row.getAttribute("data-key"),
                    worker.isEmpty() ? "" : worker.get(0).trim(),
                    worker.size() < 2 ? "" : worker.get(1).trim(),
                    cells.get(1).getText().trim(),
                    cells.get(2).getText().trim(),
                    Status.fromLabel(cells.get(3).getText()).orElse(null),
                    cells.get(4).getText().trim(),
                    cells.get(0).getText().contains("Đã yêu cầu")));
        }
        return result;
    }

    public WorkerStopRequestPage search(String keyword) {
        List<String> before = rowIds();
        WebElement input = driver.findElement(SEARCH);
        observe(input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), keyword);
        wait.until(d -> keyword.equals(d.findElement(SEARCH).getAttribute("value")));
        wait.until(d -> {
            waitForTable();
            List<String> current = rowIds();
            return current.isEmpty() || !current.equals(before);
        });
        return this;
    }

    public WorkerStopRequestPage selectStatus(Status status) {
        List<String> before = rowIds();
        WebElement button = exactVisible(By.xpath(
                "//button[normalize-space()='" + status.label() + "']"));
        observe(button);
        button.click();
        wait.until(d -> {
            waitForTable();
            List<RequestRow> current = rows();
            return current.isEmpty() || (current.stream()
                    .allMatch(row -> row.status() == status)
                    && (!rowIds().equals(before) || status == Status.PENDING));
        });
        return this;
    }

    public WorkerStopRequestPage reset() {
        WebElement button = exactVisible(RESET);
        observe(button);
        button.click();
        wait.until(d -> {
            waitForTable();
            return d.findElement(SEARCH).getAttribute("value").isBlank()
                    && activePage() == 1;
        });
        return this;
    }

    public int activePage() {
        String page = visiblePagination().getAttribute("data-active-page");
        return page != null && page.matches("\\d+") ? Integer.parseInt(page) : 1;
    }

    public int totalPages() {
        String total = visiblePagination().getAttribute("data-total");
        return total != null && total.matches("\\d+") ? Integer.parseInt(total) : 1;
    }

    public WorkerStopRequestPage goToPage(int page) {
        List<String> before = rowIds();
        WebElement control = visiblePagination().findElements(By.cssSelector(
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
            return activePage() == page && !rowIds().equals(before);
        });
        observe(visiblePagination());
        return this;
    }

    public DetailSnapshot openFirstRow() {
        return openRow(driver.findElements(DATA_ROWS).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Danh sách không có yêu cầu.")));
    }

    public DetailSnapshot openFirstRepeatedRequest() {
        WebElement row = driver.findElements(DATA_ROWS).stream()
                .filter(item -> TextNormalizer.normalize(item.getText())
                        .contains("da yeu cau"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Trang hiện tại không có thợ yêu cầu nhiều lần."));
        return openRow(row);
    }

    public DetailSnapshot openFirstPendingRequest() {
        selectStatus(Status.PENDING);
        return openFirstRow();
    }

    public DetailSnapshot openFirstPendingWithAction(Action action) {
        selectStatus(Status.PENDING);
        return openFirstRequestWithAction(action);
    }

    public DetailSnapshot openFirstApprovedForBackToWork() {
        selectStatus(Status.APPROVED);
        return openFirstRequestWithAction(Action.BACK_TO_WORK);
    }

    public String dialogText() {
        return requiredDialog().getText();
    }

    public boolean dialogHasButton(String label) {
        return requiredDialog().findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(button -> normalized(button.getText())
                        .equals(normalized(label)));
    }

    public WorkerStopRequestPage chooseRejectAction() {
        clickDialogButton("Từ chối");
        wait.until(d -> {
            String text = dialogText();
            return text.contains("Lý do từ chối")
                    && text.contains("Đơn dịch vụ chưa hoàn tất")
                    && text.contains("Thông tin tài khoản ngân hàng không hợp lệ")
                    && text.contains("Đang bị phạt");
        });
        return this;
    }

    public WorkerStopRequestPage approveOpenRequest() {
        clickDialogButton("Duyệt");
        wait.until(d -> dialogHasButton("Xác nhận duyệt"));
        clickDialogButton("Xác nhận duyệt");
        waitAfterMutation();
        return this;
    }

    public WorkerStopRequestPage rejectOpenRequest(String reason) {
        chooseRejectAction();
        clickDialogButtonContaining(reason);
        wait.until(d -> dialogHasButton("Xác nhận từ chối"));
        clickDialogButton("Xác nhận từ chối");
        waitAfterMutation();
        return this;
    }

    public WorkerStopRequestPage skipOpenRequest() {
        clickDialogButton("Bỏ qua");
        wait.until(d -> dialogHasButton("Xác nhận bỏ qua"));
        clickDialogButton("Xác nhận bỏ qua");
        waitAfterMutation();
        return this;
    }

    public WorkerStopRequestPage backToWorkOpenRequest() {
        clickDialogButton("Quay lại làm việc");
        wait.until(d -> dialogHasButton("Xác nhận mở khoá"));
        clickDialogButton("Xác nhận mở khoá");
        waitAfterMutation();
        return this;
    }

    public boolean requestExistsInStatus(String requestId, Status status) {
        open();
        selectStatus(status);
        int lastPage = totalPages();
        for (int page = 1; page <= lastPage; page++) {
            if (rows().stream().anyMatch(row -> row.id().equals(requestId)
                    && row.status() == status)) {
                return true;
            }
            if (page < lastPage) goToPage(page + 1);
        }
        return false;
    }

    public boolean requestDetailHasStatus(String requestId, Status status) {
        driver.get(TestConfig.baseUrl().replaceAll("/+$", "")
                + ROUTE + "?id=" + requestId);
        wait.until(d -> d.getCurrentUrl().contains(ROUTE + "?id=" + requestId));
        WebElement drawer = wait.until(d -> d.findElements(By.cssSelector(
                        "[aria-label='drawer-Chi tiết yêu cầu']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> {
                    String classes = item.getAttribute("class");
                    return classes != null && !classes.contains("translate-x-[100%]")
                            && item.getText().contains(status.label());
                })
                .findFirst().orElse(null));
        return drawer.getText().contains(status.label());
    }

    public DetailSnapshot openRequestInStatus(String requestId, Status status) {
        open();
        selectStatus(status);
        int lastPage = totalPages();
        for (int page = 1; page <= lastPage; page++) {
            WebElement row = driver.findElements(DATA_ROWS).stream()
                    .filter(item -> requestId.equals(item.getAttribute("data-key")))
                    .findFirst().orElse(null);
            if (row != null) return openRow(row);
            if (page < lastPage) goToPage(page + 1);
        }
        throw new IllegalStateException(
                "Không tìm thấy yêu cầu #" + requestId + " trong " + status.label());
    }

    public boolean rejectConfirmationDisabled() {
        WebElement button = dialogButton("Chọn hành động");
        return button == null || button.getAttribute("disabled") != null
                || "true".equalsIgnoreCase(button.getAttribute("aria-disabled"));
    }

    public void closeDialog() {
        if (visibleDialog() != null) {
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            wait.until(d -> visibleDialog() == null);
        }
    }

    public String searchValue() {
        return driver.findElement(SEARCH).getAttribute("value");
    }

    private DetailSnapshot openRow(WebElement row) {
        String rowId = row.getAttribute("data-key");
        observe(row);
        row.click();
        WebElement dialog = wait.until(d -> {
            WebElement candidate = visibleDialog();
            return candidate != null && candidate.getText().contains("ID: #")
                    ? candidate : null;
        });
        String text = dialog.getText();
        return new DetailSnapshot(
                rowId,
                text,
                text.contains("Lịch sử yêu cầu"),
                dialogHasButton("Duyệt"),
                dialogHasButton("Từ chối"),
                dialogHasButton("Bỏ qua"),
                dialogHasButton("Quay lại làm việc"));
    }

    private DetailSnapshot openFirstRequestWithAction(Action action) {
        int lastPage = totalPages();
        for (int page = activePage(); page <= lastPage; page++) {
            int rowCount = driver.findElements(DATA_ROWS).size();
            for (int index = 0; index < rowCount; index++) {
                List<WebElement> currentRows = driver.findElements(DATA_ROWS);
                if (index >= currentRows.size()) break;
                DetailSnapshot detail = openRow(currentRows.get(index));
                boolean matches = switch (action) {
                    case APPROVE -> detail.canApprove();
                    case REJECT -> detail.canReject();
                    case SKIP -> detail.canSkip();
                    case BACK_TO_WORK -> detail.canBackToWork();
                };
                if (matches) return detail;
                closeDialog();
            }
            if (page < lastPage) goToPage(page + 1);
        }
        throw new IllegalStateException(
                "Không tìm thấy yêu cầu có action " + action);
    }

    private void waitAfterMutation() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(d -> visibleDialog() == null);
        } catch (TimeoutException ignored) {
            // Kết quả nghiệp vụ được xác nhận lại bằng requestId ở trạng thái đích.
            closeDialog();
        }
    }

    private void clickDialogButton(String label) {
        WebElement button = dialogButton(label);
        if (button == null) {
            throw new IllegalStateException("Dialog không có nút " + label);
        }
        observe(button);
        button.click();
    }

    private void clickDialogButtonContaining(String label) {
        String expected = normalized(label);
        WebElement button = requiredDialog().findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> normalized(item.getText()).startsWith(expected))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Dialog không có lựa chọn " + label));
        observe(button);
        button.click();
    }

    private WebElement dialogButton(String label) {
        WebElement dialog = visibleDialog();
        if (dialog == null) return null;
        return dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> normalized(button.getText())
                        .equals(normalized(label)))
                .findFirst().orElse(null);
    }

    private void waitForTable() {
        wait.until(d -> {
            WebElement table = d.findElement(TABLE);
            boolean loading = !table.findElements(By.cssSelector(
                    "tbody[data-loading='true']")).isEmpty();
            return !loading && (!d.findElements(DATA_ROWS).isEmpty()
                    || table.getText().contains("Chưa có dữ liệu"));
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

    private WebElement requiredDialog() {
        WebElement dialog = visibleDialog();
        if (dialog == null) throw new IllegalStateException("Dialog chi tiết chưa mở.");
        return dialog;
    }

    private WebElement visibleDialog() {
        return driver.findElements(DIALOG).stream()
                .filter(WebElement::isDisplayed)
                .filter(dialog -> dialog.getText().contains("yêu cầu ngưng hợp tác"))
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
        new Actions(driver).pause(Duration.ofSeconds(2)).perform();
    }

    private static String normalized(String value) {
        return TextNormalizer.normalize(value == null ? "" : value);
    }

    public enum Status {
        PENDING("Chờ xử lý"),
        APPROVED("Đã duyệt"),
        REJECTED("Đã từ chối"),
        SKIPPED("Đã bỏ qua");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public static Optional<Status> fromLabel(String label) {
            String actual = normalized(label);
            for (Status status : values()) {
                if (normalized(status.label).equals(actual)) return Optional.of(status);
            }
            return Optional.empty();
        }
    }

    public enum Action {
        APPROVE,
        REJECT,
        SKIP,
        BACK_TO_WORK
    }

    public record RequestRow(
            String id,
            String workerName,
            String phone,
            String reason,
            String attitude,
            Status status,
            String requestedAt,
            boolean repeated) {
    }

    public record DetailSnapshot(
            String requestId,
            String text,
            boolean hasHistory,
            boolean canApprove,
            boolean canReject,
            boolean canSkip,
            boolean canBackToWork) {
    }
}
