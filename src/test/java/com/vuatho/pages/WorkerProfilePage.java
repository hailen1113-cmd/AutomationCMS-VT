package com.vuatho.pages;

import com.vuatho.navigation.MenuTarget;
import com.vuatho.testdata.PartnerWorkerTestData;
import com.vuatho.utils.PageLoadSynchronizer;
import com.vuatho.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WorkerProfilePage {
    private static final String SEARCH_PLACEHOLDER = "Tìm kiếm thợ";
    private static final String WORKER_PROFILE_ROUTE = "/vuatho/worker";
    private static final By FILTER_BUTTON = By.cssSelector("button[title='Filter']");
    private static final By LOADING_INDICATORS = By.cssSelector(
            "[role='progressbar'], .ant-spin-spinning, .ant-skeleton, .skeleton");
    private static final By DETAIL_SURFACES = By.cssSelector(
            "[role='dialog'], [aria-label*='drawer'], [class*='modal'], [class*='drawer']");
    private static final By KYC_STATUS_GROUP = By.cssSelector("[role='radiogroup']");
    private static final By DATE_PICKER = By.cssSelector(".react-datepicker");
    private static final By CURRENT_MONTH = By.cssSelector(".react-datepicker__current-month");
    private static final By PREVIOUS_MONTH_BUTTON = By.cssSelector(
            ".react-datepicker__navigation--previous");
    private static final By NEXT_MONTH_BUTTON = By.cssSelector(
            ".react-datepicker__navigation--next");
    private static final By TODAY_IN_CALENDAR = By.cssSelector(
            ".react-datepicker__day--today:not(.react-datepicker__day--outside-month)");
    private static final By PAST_DAY_IN_CALENDAR = By.cssSelector(
            ".react-datepicker__day:not(.react-datepicker__day--outside-month)"
                    + ":not(.react-datepicker__day--today)"
                    + ":not(.react-datepicker__day--disabled)");
    private static final By FUTURE_DAY_IN_CALENDAR = By.cssSelector(
            ".react-datepicker__day:not(.react-datepicker__day--outside-month)"
                    + ":not(.react-datepicker__day--disabled)");
    private static final By SELECTED_DAY_IN_CALENDAR = By.cssSelector(
            ".react-datepicker__day--selected, [role='option'][aria-selected='true']");
    private static final By PAGINATION = By.xpath(
            "//ul[.//*[@aria-label='previous page button'] and .//*[@aria-label='next page button']]");
    private static final By ACTIVE_PAGE = By.cssSelector(
            "li[data-slot='item'][aria-current='true'][aria-label^='pagination item']");
    private static final By PREVIOUS_PAGE_BUTTON = By.cssSelector(
            "li[aria-label='previous page button']");
    private static final By NEXT_PAGE_BUTTON = By.cssSelector(
            "li[aria-label='next page button']");
    private static final By PAGE_ITEMS = By.cssSelector(
            "li[data-slot='item'][aria-label^='pagination item']");
    private static final By TRANSACTION_HISTORY_HEADING = By.xpath(
            "//*[normalize-space()='Lịch sử giao dịch']");
    private static final List<String> KPI_LABELS = List.of(
            "Tổng số thợ",
            "Đang hoạt động",
            "Chờ duyệt KYC",
            "Đã có đồng phục");
    private static final List<String> TABLE_HEADERS = List.of(
            "ID",
            "Thông tin thợ",
            "Trạng thái",
            "Số đơn dịch vụ hoàn thành",
            "Thời gian tạo");
    private static final List<String> KYC_FILTER_LABELS = List.of(
            "Chưa KYC",
            "Chờ KYC",
            "Đã KYC",
            "Từ chối");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private boolean workerDetailOpened;

    /**
     * Kết quả đọc được sau khi mở một giao dịch trong tab trình duyệt mới.
     * @param sourceRowText nội dung dòng giao dịch đã click ở tab hồ sơ thợ
     * @param detailUrl URL trang chi tiết giao dịch
     * @param detailText nội dung hiển thị trong trang chi tiết
     */
    public record TransactionDetailResult(String sourceRowText, String detailUrl, String detailText) {
    }

    /**
     * Kết quả đối chiếu sau khi click một mã đơn dịch vụ trong hồ sơ thợ.
     * @param orderId mã đơn đọc từ dòng nguồn
     * @param sourceRowText nội dung dòng đơn dịch vụ đã click
     * @param detailUrl URL của tab chi tiết
     * @param detailText nội dung hiển thị trong tab chi tiết
     */
    public record ServiceOrderDetailResult(
            String orderId,
            String sourceRowText,
            String detailUrl,
            String detailText) {
    }

    /**
     * Kết quả đối chiếu sau khi click một thợ trong danh sách Đã giới thiệu.
     * @param workerId ID đọc từ href của card nguồn
     * @param workerName tên thợ hiển thị trên card nguồn
     * @param detailUrl URL của tab hồ sơ được mở
     * @param detailText nội dung trang hồ sơ được mở
     */
    public record ReferredWorkerDetailResult(
            String workerId,
            String workerName,
            String detailUrl,
            String detailText) {
    }

    /**
     * Kết quả sau khi mở phần xem chi tiết của một bài đăng trong hồ sơ thợ.
     * @param sourcePostText nội dung card bài đăng đã được click
     * @param detailUrl URL khi phần chi tiết đang mở
     * @param detailVisible cho biết giao diện xem chi tiết đã xuất hiện
     */
    public record WorkerPostDetailResult(
            String sourcePostText,
            String detailUrl,
            boolean detailVisible) {
    }

    /**
     * Trạng thái đối chiếu sau khi thao tác các control trong modal bài đăng.
     */
    public record WorkerPostViewerControlResult(
            String initialCounter,
            String nextCounter,
            String previousCounter,
            String initialTransform,
            String zoomedTransform,
            String restoredZoomTransform,
            String rotatedTransform,
            String restoredRotationTransform) {
    }

    /** Kết quả kiểm tra các control trong popup Thiết lập xử phạt. */
    public record WorkerPenaltyDialogResult(
            boolean requiredFieldsPresent,
            boolean initialStateValid,
            boolean emptySubmissionBlocked,
            boolean testDataEntered,
            boolean permanentDisablesBlockingDays,
            boolean permanentOffEnablesBlockingDays,
            boolean restrictionOptionsMutuallyExclusive,
            boolean cancelledWithoutCreatingViolation,
            boolean topCloseButtonWorks) {
    }

    /** Kết quả sau khi áp dụng xử phạt thật trên sandbox. */
    public record WorkerPenaltyApplyResult(
            String orderId,
            String penaltyTitle,
            boolean submissionPerformed,
            boolean blockedByExistingPenalty,
            boolean dialogClosed,
            boolean violationHistoryChanged,
            boolean newViolationDisplayed) {
    }

    /**
     * Khởi tạo WorkerProfilePage với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public WorkerProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.wait.pollingEvery(Duration.ofMillis(200));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    /**
     * Mở from menu trong luồng kiểm thử.
     * @return kết quả open from menu sau khi xử lý
     */
    public WorkerProfilePage openFromMenu() {
        dismissTransientOverlays();
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(menuTarget(), false);
        try {
            waitUntilPageShellLoaded();
        } catch (TimeoutException exception) {
            navigateToWorkerRoute();
            waitUntilPageShellLoaded();
        }
        return restoreDefaultListIfNeeded();
    }

    /**
     * Chờ until loaded trong luồng kiểm thử.
     * @return kết quả wait until loaded sau khi xử lý
     */
    public WorkerProfilePage waitUntilLoaded() {
        waitUntilPageShellLoaded();
        wait.until(webDriver -> hasWorkerRows());
        return this;
    }

    /**
     * Thực hiện xử lý restore default list if needed trong luồng kiểm thử.
     * @return kết quả restore default list if needed sau khi xử lý
     */
    public WorkerProfilePage restoreDefaultListIfNeeded() {
        waitUntilPageShellLoaded();
        if (hasWorkerRows()) {
            return this;
        }

        clearSearchIfNeeded();
        clickTopResetButtonIfAvailable();
        waitForListToSettle();

        if (!hasWorkerRows()) {
            resetFilterPanelIfAvailable();
            waitForListToSettle();
        }

        wait.until(webDriver -> hasWorkerRows());
        return this;
    }

    /**
     * Chờ until page shell loaded trong luồng kiểm thử.
     * @return kết quả wait until page shell loaded sau khi xử lý
     */
    private WorkerProfilePage waitUntilPageShellLoaded() {
        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> driver.getCurrentUrl().contains(WORKER_PROFILE_ROUTE));
        wait.until(webDriver -> searchInput().isDisplayed());
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> hasExpectedTableHeaders());
        return this;
    }

    /**
     * Thực hiện xử lý navigate to worker route trong luồng kiểm thử.
     */
    private void navigateToWorkerRoute() {
        js().executeScript("window.location.href = window.location.origin + arguments[0];", WORKER_PROFILE_ROUTE);
        wait.until(webDriver -> documentIsReady());
    }

    /**
     * Thực hiện xử lý menu target trong luồng kiểm thử.
     * @return kết quả menu target sau khi xử lý
     */
    public MenuTarget menuTarget() {
        return PartnerWorkerTestData.WORKER_PROFILE;
    }

    /**
     * Kiểm tra điều kiện has search input.
     * @return kết quả has search input sau khi xử lý
     */
    public boolean hasSearchInput() {
        return searchInput().isDisplayed();
    }

    /**
     * Kiểm tra điều kiện has worker rows.
     * @return kết quả has worker rows sau khi xử lý
     */
    public boolean hasWorkerRows() {
        return visibleRowCount() > 0;
    }

    /**
     * Cuộn xuống cuối trang và xác nhận thanh phân trang hồ sơ thợ đang hiển thị.
     * @return {@code true} khi tìm thấy thanh có cả nút Previous và Next
     */
    public boolean hasVisiblePagination() {
        js().executeScript("window.scrollTo(0, document.body.scrollHeight);");
        return wait.until(webDriver -> visibleElement(PAGINATION) != null);
    }

    /**
     * Đọc số trang đang được chọn từ thuộc tính {@code aria-label} của pagination.
     * @return số trang hiện tại
     */
    public int currentWorkerPageNumber() {
        WebElement activePage = wait.until(webDriver -> visibleElement(ACTIVE_PAGE));
        return paginationNumber(activePage);
    }

    /**
     * Đọc số trang cuối từ các page item đang hiển thị trên thanh phân trang.
     * @return số trang lớn nhất; trả về {@code 1} nếu danh sách không phân trang
     */
    public int lastWorkerPageNumber() {
        if (!hasVisiblePagination()) {
            return 1;
        }
        return driver.findElements(PAGE_ITEMS).stream()
                .filter(WebElement::isDisplayed)
                .mapToInt(this::paginationNumber)
                .max()
                .orElse(1);
    }

    /**
     * Kiểm tra nút Previous có bị khóa tại trang hiện tại hay không.
     * @return {@code true} khi nút có {@code aria-disabled=true} hoặc {@code data-disabled=true}
     */
    public boolean previousWorkerPageIsDisabled() {
        return paginationButtonIsDisabled(PREVIOUS_PAGE_BUTTON);
    }

    /**
     * Kiểm tra nút Next có bị khóa tại trang hiện tại hay không.
     * @return {@code true} khi nút có {@code aria-disabled=true} hoặc {@code data-disabled=true}
     */
    public boolean nextWorkerPageIsDisabled() {
        return paginationButtonIsDisabled(NEXT_PAGE_BUTTON);
    }

    /**
     * Chuyển đến một trang hồ sơ cụ thể và chờ số trang cùng dữ liệu bảng cập nhật.
     * @param pageNumber số trang cần mở
     */
    public void openWorkerPage(int pageNumber) {
        hasVisiblePagination();
        if (currentWorkerPageNumber() == pageNumber) {
            return;
        }

        String previousFirstRow = firstWorkerRowText();
        By targetPage = By.cssSelector(
                "li[data-slot='item'][aria-label='pagination item " + pageNumber + "']");
        WebElement pageButton = wait.until(webDriver -> visibleElement(targetPage));
        clickCandidate(pageButton);

        wait.until(webDriver -> currentWorkerPageNumber() == pageNumber);
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> hasWorkerRows()
                && !firstWorkerRowText().equals(previousFirstRow));
    }

    /**
     * Chuyển sang trang kế tiếp và chờ dữ liệu bảng thay đổi.
     */
    public void openNextWorkerPage() {
        int expectedPage = currentWorkerPageNumber() + 1;
        clickPaginationButtonAndWait(NEXT_PAGE_BUTTON, expectedPage);
    }

    /**
     * Quay lại trang trước và chờ dữ liệu bảng thay đổi.
     */
    public void openPreviousWorkerPage() {
        int expectedPage = currentWorkerPageNumber() - 1;
        clickPaginationButtonAndWait(PREVIOUS_PAGE_BUTTON, expectedPage);
    }

    /**
     * Chờ until at least worker rows visible trong luồng kiểm thử.
     * @param minimumRows giá trị minimum rows được truyền vào
     */
    public void waitUntilAtLeastWorkerRowsVisible(int minimumRows) {
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        wait.until(webDriver -> visibleRows().size() >= minimumRows);
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> visibleRows().size() >= minimumRows);
    }

    /**
     * Xóa tìm kiếm/bộ lọc còn sót lại và bảo đảm danh sách mặc định có đủ số dòng cần dùng.
     * @param minimumRows số user tối thiểu cần hiển thị
     */
    public void resetAndLoadDefaultWorkerList(int minimumRows) {
        waitUntilPageShellLoaded();
        clearSearchIfNeeded();
        clickTopResetButtonIfAvailable();
        waitForListToSettle();
        waitUntilAtLeastWorkerRowsVisible(minimumRows);
    }

    /**
     * Kiểm tra điều kiện has kpi summary.
     * @return kết quả has kpi summary sau khi xử lý
     */
    public boolean hasKpiSummary() {
        String text = mainText().toLowerCase();
        return KPI_LABELS.stream()
                .allMatch(label -> text.contains(label.toLowerCase()));
    }

    /**
     * Kiểm tra điều kiện has expected table headers.
     * @return kết quả has expected table headers sau khi xử lý
     */
    public boolean hasExpectedTableHeaders() {
        String text = mainText().toLowerCase();
        return TABLE_HEADERS.stream()
                .allMatch(header -> text.contains(header.toLowerCase()));
    }

    /**
     * Kiểm tra điều kiện has search mode options.
     * @return kết quả has search mode options sau khi xử lý
     */
    public boolean hasSearchModeOptions() {
        List<String> options = new Select(visibleSearchModeSelect()).getOptions().stream()
                .map(WebElement::getText)
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        return options.stream().anyMatch(option -> option.contains("tên"))
                && options.stream().anyMatch(option -> option.contains("sđt") || option.contains("sdt"));
    }

    /**
     * Kích hoạt search mode trong luồng kiểm thử.
     * @param modeLabel giá trị mode label được truyền vào
     */
    public void selectSearchMode(String modeLabel) {
        WebElement selectElement = visibleSearchModeSelect();
        Select select = new Select(selectElement);
        String expected = TextNormalizer.normalize(modeLabel);
        WebElement option = select.getOptions().stream()
                .filter(element -> TextNormalizer.normalize(element.getText()).contains(expected))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy mode tìm kiếm: " + modeLabel));
        select.selectByVisibleText(option.getText());
        wait.until(webDriver -> TextNormalizer.normalize(selectedSearchMode()).contains(expected));
    }

    /**
     * Kích hoạt search mode trong luồng kiểm thử.
     * @return kết quả selected search mode sau khi xử lý
     */
    public String selectedSearchMode() {
        return new Select(visibleSearchModeSelect()).getFirstSelectedOption().getText().trim();
    }

    /**
     * Thực hiện xử lý search and reset trong luồng kiểm thử.
     * @param query giá trị query được truyền vào
     */
    public void searchAndReset(String query) {
        WebElement input = searchInput();
        input.clear();
        PageLoadSynchronizer.prepareForAsyncAction(driver);
        String stateBeforeSearch = PageLoadSynchronizer.mainContentState(driver);
        input.sendKeys(query);
        wait.until(webDriver -> query.equals(searchInput().getAttribute("value")));
        PageLoadSynchronizer.waitForSearchResultsToLoad(driver, stateBeforeSearch);

        input = searchInput();
        PageLoadSynchronizer.prepareForAsyncAction(driver);
        String stateBeforeReset = PageLoadSynchronizer.mainContentState(driver);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        wait.until(webDriver -> searchInput().getAttribute("value").isBlank());
        PageLoadSynchronizer.waitForSearchResultsToLoad(driver, stateBeforeReset);
        waitUntilLoaded();
    }

    /**
     * Thực hiện xử lý search by mode trong luồng kiểm thử.
     * @param modeLabel giá trị mode label được truyền vào
     * @param query giá trị query được truyền vào
     */
    public void searchByMode(String modeLabel, String query) {
        selectSearchMode(modeLabel);
        WebElement input = searchInput();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        wait.until(webDriver -> searchInput().getAttribute("value").isBlank());
        PageLoadSynchronizer.prepareForAsyncAction(driver);
        String stateBeforeSearch = PageLoadSynchronizer.mainContentState(driver);
        input = searchInput();
        input.sendKeys(query);
        wait.until(webDriver -> query.equals(searchInput().getAttribute("value")));
        PageLoadSynchronizer.waitForSearchResultsToLoad(driver, stateBeforeSearch);
        wait.until(webDriver -> hasWorkerRows() || hasNoDataMessage());
    }

    /**
     * Xóa hoặc đặt lại search trong luồng kiểm thử.
     */
    public void clearSearch() {
        WebElement input = searchInput();
        String currentValue = input.getAttribute("value");
        if (currentValue == null || currentValue.isBlank()) {
            waitUntilLoaded();
            return;
        }
        PageLoadSynchronizer.prepareForAsyncAction(driver);
        String stateBeforeReset = PageLoadSynchronizer.mainContentState(driver);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        wait.until(webDriver -> searchInput().getAttribute("value").isBlank());
        PageLoadSynchronizer.waitForSearchResultsToLoad(driver, stateBeforeReset);
        waitUntilLoaded();
    }

    /**
     * Thực hiện xử lý search input is empty trong luồng kiểm thử.
     * @return kết quả search input is empty sau khi xử lý
     */
    public boolean searchInputIsEmpty() {
        return searchInput().getAttribute("value").isBlank();
    }

    /**
     * Trả về first visible worker name từ trạng thái hiện tại.
     * @return kết quả first visible worker name sau khi xử lý
     */
    public String firstVisibleWorkerName() {
        return firstWorkerInfoCellLines().stream()
                .filter(line -> !line.matches(".*\\d{6,}.*"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không đọc được tên thợ ở dòng đầu tiên."));
    }

    /**
     * Trả về first visible worker phone search term từ trạng thái hiện tại.
     * @return kết quả first visible worker phone search term sau khi xử lý
     */
    public String firstVisibleWorkerPhoneSearchTerm() {
        return firstWorkerInfoCellLines().stream()
                .filter(line -> line.matches(".*\\d{6,}.*"))
                .filter(line -> line.replaceAll("[^0-9]", "").length() >= 6)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không đọc được SĐT thợ ở dòng đầu tiên."));
    }

    /**
     * Trả về first visible worker names từ trạng thái hiện tại.
     * @param maxCount giá trị max count được truyền vào
     * @return kết quả first visible worker names sau khi xử lý
     */
    public List<String> firstVisibleWorkerNames(int maxCount) {
        waitUntilAtLeastWorkerRowsVisible(maxCount);
        return visibleRows().stream()
                .map(this::workerInfoCellLines)
                .flatMap(List::stream)
                .filter(line -> !line.matches(".*\\d{6,}.*"))
                .filter(line -> line.length() >= 3)
                .limit(maxCount)
                .toList();
    }

    /**
     * Trả về first visible worker phone search terms từ trạng thái hiện tại.
     * @param maxCount giá trị max count được truyền vào
     * @return kết quả first visible worker phone search terms sau khi xử lý
     */
    public List<String> firstVisibleWorkerPhoneSearchTerms(int maxCount) {
        waitUntilAtLeastWorkerRowsVisible(maxCount);
        return visibleRows().stream()
                .map(this::workerInfoCellLines)
                .flatMap(List::stream)
                .filter(line -> line.matches(".*\\d{6,}.*"))
                .filter(line -> line.replaceAll("[^0-9]", "").length() >= 6)
                .limit(maxCount)
                .toList();
    }

    /**
     * Trả về visible rows contain search term từ trạng thái hiện tại.
     * @param query giá trị query được truyền vào
     * @return kết quả visible rows contain search term sau khi xử lý
     */
    public boolean visibleRowsContainSearchTerm(String query) {
        String expected = normalizeSearchTerm(query);
        return visibleRowTexts().stream()
                .map(this::normalizeSearchTerm)
                .anyMatch(row -> row.contains(expected));
    }

    /**
     * Trả về visible rows all contain search term từ trạng thái hiện tại.
     * @param query giá trị query được truyền vào
     * @return kết quả visible rows all contain search term sau khi xử lý
     */
    public boolean visibleRowsAllContainSearchTerm(String query) {
        String expected = normalizeSearchTerm(query);
        List<String> rows = visibleRowTexts().stream()
                .map(this::normalizeSearchTerm)
                .toList();
        return !rows.isEmpty() && rows.stream().allMatch(row -> row.contains(expected));
    }

    /**
     * Mở filter trong luồng kiểm thử.
     */
    public void openFilter() {
        waitUntilLoaded();
        dismissTransientOverlays();
        if (visibleElement(KYC_STATUS_GROUP) != null) {
            return;
        }
        WebElement button = wait.until(webDriver -> visibleElement(FILTER_BUTTON));
        scrollToCenter(button);
        button.click();
        try {
            wait.until(webDriver -> visibleElement(KYC_STATUS_GROUP) != null);
        } catch (TimeoutException exception) {
            clickCandidate(button);
            wait.until(webDriver -> visibleElement(KYC_STATUS_GROUP) != null);
        }
        wait.until(webDriver -> visibleElement(DATE_PICKER) != null);
    }

    /**
     * Kiểm tra điều kiện has kyc status filter options.
     * @return kết quả has kyc status filter options sau khi xử lý
     */
    public boolean hasKycStatusFilterOptions() {
        String text = filterPanelText().toLowerCase();
        return KYC_FILTER_LABELS.stream()
                .allMatch(label -> text.contains(label.toLowerCase()));
    }

    /**
     * Kiểm tra điều kiện has date filter calendar.
     * @return kết quả has date filter calendar sau khi xử lý
     */
    public boolean hasDateFilterCalendar() {
        return visibleElement(DATE_PICKER) != null
                && visibleElement(PREVIOUS_MONTH_BUTTON) != null
                && visibleElement(NEXT_MONTH_BUTTON) != null
                && visibleElement(CURRENT_MONTH) != null;
    }

    /**
     * Kích hoạt kyc status trong luồng kiểm thử.
     * @param statusLabel giá trị status label được truyền vào
     */
    public void selectKycStatus(String statusLabel) {
        WebElement label = wait.until(webDriver -> visibleKycStatusLabel(statusLabel));
        scrollToCenter(label);
        label.click();
        wait.until(webDriver -> selectedKycStatusLabel().equals(statusLabel));
        PageLoadSynchronizer.waitForDataToSettle(driver);
        waitUntilLoaded();
    }

    /**
     * Trả về visible rows match kyc status từ trạng thái hiện tại.
     * @param statusLabel giá trị status label được truyền vào
     * @return kết quả visible rows match kyc status sau khi xử lý
     */
    public boolean visibleRowsMatchKycStatus(String statusLabel) {
        String expected = TextNormalizer.normalize(statusLabel);
        return visibleRowTexts().stream()
                .map(TextNormalizer::normalize)
                .allMatch(row -> row.contains(expected));
    }

    /**
     * Kích hoạt kyc status label trong luồng kiểm thử.
     * @return kết quả selected kyc status label sau khi xử lý
     */
    public String selectedKycStatusLabel() {
        Object value = js().executeScript(
                "const checked=document.querySelector('[role=\"radiogroup\"] input[type=\"radio\"]:checked');"
                        + "if (!checked) return '';"
                        + "const label=checked.closest('label');"
                        + "return label ? (label.innerText || label.textContent || '').trim() : '';");
        return String.valueOf(value).trim();
    }

    /**
     * Xóa hoặc đặt lại filter trong luồng kiểm thử.
     */
    public void resetFilter() {
        WebElement button = wait.until(webDriver -> visibleFilterButton("Đặt lại"));
        scrollToCenter(button);
        button.click();
        PageLoadSynchronizer.waitForDataToSettle(driver);
        waitUntilLoaded();
    }

    /**
     * Thực hiện xử lý date filter can navigate month trong luồng kiểm thử.
     * @return kết quả date filter can navigate month sau khi xử lý
     */
    public boolean dateFilterCanNavigateMonth() {
        String originalMonth = currentCalendarMonth();
        WebElement next = wait.until(webDriver -> visibleElement(NEXT_MONTH_BUTTON));
        next.click();
        wait.until(webDriver -> !currentCalendarMonth().equals(originalMonth));
        String nextMonth = currentCalendarMonth();

        WebElement previous = wait.until(webDriver -> visibleElement(PREVIOUS_MONTH_BUTTON));
        previous.click();
        wait.until(webDriver -> !currentCalendarMonth().equals(nextMonth));
        return originalMonth.equals(currentCalendarMonth());
    }

    /**
     * Kích hoạt today in date filter trong luồng kiểm thử.
     */
    public void selectTodayInDateFilter() {
        String stateBefore = PageLoadSynchronizer.mainContentState(driver);
        WebElement today = wait.until(webDriver -> visibleElement(TODAY_IN_CALENDAR));
        scrollToCenter(today);
        today.click();
        wait.until(webDriver -> visibleElement(SELECTED_DAY_IN_CALENDAR) != null);
        waitForDateFilterResult(stateBefore);
        waitUntilLoaded();
    }

    /**
     * Kiểm tra điều kiện has selected date filter.
     * @return kết quả has selected date filter sau khi xử lý
     */
    public boolean hasSelectedDateFilter() {
        return visibleElement(SELECTED_DAY_IN_CALENDAR) != null;
    }

    /**
     * Kích hoạt single past date in date filter trong luồng kiểm thử.
     */
    public void selectSinglePastDateInDateFilter() {
        String stateBefore = PageLoadSynchronizer.mainContentState(driver);
        WebElement day = wait.until(webDriver -> visibleCalendarDayByNumber(firstVisibleWorkerCreatedDay()));
        scrollToCenter(day);
        day.click();
        wait.until(webDriver -> hasSelectedDateFilter());
        waitForDateFilterResult(stateBefore);
    }

    /**
     * Kích hoạt multiple past dates in date filter trong luồng kiểm thử.
     */
    public void selectMultiplePastDatesInDateFilter() {
        int createdDay = firstVisibleWorkerCreatedDay();
        int today = todayDayNumber();
        int startDay = Math.max(1, Math.min(createdDay, today - 1));
        int endDay = Math.max(createdDay, today);
        if (startDay == endDay) {
            throw new IllegalStateException("Không đủ ngày quá khứ để chọn khoảng thời gian.");
        }

        String stateBefore = PageLoadSynchronizer.mainContentState(driver);
        WebElement start = wait.until(webDriver -> visibleCalendarDayByNumber(startDay));
        WebElement end = wait.until(webDriver -> visibleCalendarDayByNumber(endDay));
        scrollToCenter(start);
        start.click();
        wait.until(webDriver -> hasSelectedDateFilter());
        scrollToCenter(end);
        end.click();
        wait.until(webDriver -> selectedCalendarDayCount() >= 1);
        waitForDateFilterResult(stateBefore);
    }

    /**
     * Kiểm tra điều kiện has multiple date selection result.
     * @return kết quả has multiple date selection result sau khi xử lý
     */
    public boolean hasMultipleDateSelectionResult() {
        return hasDateFilterResultLoaded();
    }

    /**
     * Kiểm tra điều kiện has date filter result loaded.
     * @return kết quả has date filter result loaded sau khi xử lý
     */
    public boolean hasDateFilterResultLoaded() {
        return hasExpectedTableHeaders()
                && noLoadingIndicatorIsVisible()
                && (hasWorkerRows() || hasNoDataMessage());
    }

    /**
     * Thực hiện xử lý future date is unavailable for filtering trong luồng kiểm thử.
     * @return kết quả future date is unavailable for filtering sau khi xử lý
     */
    public boolean futureDateIsUnavailableForFiltering() {
        WebElement futureDay = firstFutureCalendarDay();
        return futureDay == null
                || "true".equalsIgnoreCase(futureDay.getAttribute("aria-disabled"))
                || String.valueOf(futureDay.getAttribute("class")).contains("disabled");
    }

    /**
     * Thực hiện xử lý latest past date is available for filtering trong luồng kiểm thử.
     * @return kết quả latest past date is available for filtering sau khi xử lý
     */
    public boolean latestPastDateIsAvailableForFiltering() {
        WebElement day = latestPastCalendarDay();
        return day != null && calendarDayNumber(day) < todayDayNumber();
    }

    /**
     * Trả về first worker row text từ trạng thái hiện tại.
     * @return kết quả first worker row text sau khi xử lý
     */
    public String firstWorkerRowText() {
        return firstWorkerRow().getText();
    }

    /**
     * Mở first worker information trong luồng kiểm thử.
     */
    public void openFirstWorkerInformation() {
        waitUntilLoaded();
        dismissTransientOverlays();
        openWorkerInformation(firstWorkerRow());
    }

    /**
     * Mở hồ sơ thợ theo vị trí dòng đang hiển thị trong danh sách.
     * @param rowIndex chỉ số dòng bắt đầu từ {@code 0}
     */
    public void openWorkerInformationAt(int rowIndex) {
        waitUntilLoaded();
        List<WebElement> rows = visibleRows();
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new IllegalArgumentException("Worker row index is out of range: " + rowIndex);
        }
        openWorkerInformation(rows.get(rowIndex));
    }

    /**
     * Đọc nội dung dòng thợ theo vị trí để ghi log testcase.
     * @param rowIndex chỉ số dòng bắt đầu từ {@code 0}
     * @return nội dung dòng đang hiển thị
     */
    public String workerRowTextAt(int rowIndex) {
        List<WebElement> rows = visibleRows();
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new IllegalArgumentException("Worker row index is out of range: " + rowIndex);
        }
        return rows.get(rowIndex).getText().trim();
    }

    /**
     * Tìm đúng dòng chứa tên thợ chỉ định và mở drawer Thông tin thợ của dòng đó.
     * @param workerName tên thợ cần mở
     */
    public void openWorkerInformationByName(String workerName) {
        waitUntilLoaded();
        dismissTransientOverlays();
        WebElement matchingRow = wait.until(webDriver -> visibleWorkerRowByName(workerName));
        openWorkerInformation(matchingRow);
    }

    /**
     * Tìm theo tên, nhấn Enter để kích hoạt tìm kiếm và mở trực tiếp dòng kết quả đúng tên.
     * Method chuyên biệt này không phụ thuộc bộ nhận diện dòng chung của các test inventory.
     * @param workerName tên thợ cần tìm và mở
     */
    public void searchAndOpenWorkerInformationByName(String workerName) {
        waitUntilLoaded();
        dismissTransientOverlays();
        selectSearchMode("ten");
        WebElement input = searchInput();
        WebElement currentMatchingRow = visibleWorkerRowByName(workerName);
        if (TextNormalizer.normalize(input.getAttribute("value"))
                .equals(TextNormalizer.normalize(workerName)) && currentMatchingRow != null) {
            openWorkerInformation(currentMatchingRow);
            return;
        }
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        input.sendKeys(workerName, Keys.ENTER);

        // Khi chạy nhiều case liên tiếp với cùng từ khóa, React có thể giữ lại dòng cũ
        // trong vài nhịp render. Chờ bảng ổn định rồi mới lấy WebElement mới để click.
        PageLoadSynchronizer.waitForDataToSettle(driver);

        WebElement matchingRow = new WebDriverWait(driver, Duration.ofSeconds(30))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(StaleElementReferenceException.class)
                .until(webDriver -> visibleWorkerRowByName(workerName));
        openWorkerInformation(matchingRow);
    }

    /**
     * Thực hiện các phương án click an toàn trên một dòng và chờ drawer chi tiết mở hoàn toàn.
     * @param row dòng hồ sơ thợ cần mở
     */
    private void openWorkerInformation(WebElement row) {
        String previousUrl = driver.getCurrentUrl();
        String previousState = PageLoadSynchronizer.mainContentState(driver);
        List<WebElement> actions = workerInformationActions(row);

        for (WebElement action : actions) {
            clickCandidate(action);
            if (waitForWorkerDetailToLoad(previousUrl, previousState, Duration.ofSeconds(2))) {
                workerDetailOpened = true;
                return;
            }
        }

        doubleClickCandidate(row);
        if (waitForWorkerDetailToLoad(previousUrl, previousState, Duration.ofSeconds(3))) {
            workerDetailOpened = true;
            return;
        }

        clickRowByJavascript(row);
        wait.until(webDriver -> workerDetailIsLoaded(previousUrl, previousState));
        workerDetailOpened = true;
    }

    /**
     * Thực hiện xử lý worker detail is open trong luồng kiểm thử.
     * @return kết quả worker detail is open sau khi xử lý
     */
    public boolean workerDetailIsOpen() {
        return workerDetailOpened
                && workerDetailShellIsVisible()
                && workerDetailTextLooksValid(workerDetailText());
    }

    /**
     * Mở worker detail tab trong luồng kiểm thử.
     * @param tabLabel giá trị tab label được truyền vào
     */
    public void openWorkerDetailTab(String tabLabel) {
        wait.until(webDriver -> workerDetailIsOpen());
        WebElement tab = wait.until(webDriver -> visibleWorkerDetailTab(tabLabel));

        clickCandidate(tab);
        wait.until(webDriver -> workerDetailTabIsSelected(tabLabel));
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> workerDetailTabHasLoaded(tabLabel));
    }

    /**
     * Kiểm tra bảng Lịch sử giao dịch đã xuất hiện trong tab Giao dịch.
     * @return {@code true} khi tiêu đề và bảng giao dịch đều hiển thị
     */
    public boolean hasTransactionHistoryTable() {
        return visibleElement(TRANSACTION_HISTORY_HEADING) != null
                && transactionHistoryTable() != null;
    }

    /**
     * Đếm số giao dịch đang hiển thị ở trang hiện tại.
     * @return số dòng dữ liệu trong bảng Lịch sử giao dịch
     */
    public int transactionHistoryRowCount() {
        return transactionHistoryRows().size();
    }

    /**
     * Click một dòng giao dịch, chuyển sang tab mới, đọc trang chi tiết rồi đóng tab đó.
     * Tab hồ sơ thợ luôn được khôi phục trong khối {@code finally} để các lần click sau
     * tiếp tục thao tác đúng bảng Lịch sử giao dịch.
     * @param rowIndex chỉ số dòng giao dịch, bắt đầu từ {@code 0}
     * @param observationDuration thời gian giữ tab chi tiết mở để người chạy quan sát
     * @return thông tin đối chiếu giữa dòng nguồn và trang chi tiết
     */
    public TransactionDetailResult openTransactionInNewTabAndReturn(int rowIndex, Duration observationDuration) {
        List<WebElement> rows = transactionHistoryRows();
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new IllegalArgumentException("Transaction row index is out of range: " + rowIndex);
        }

        String parentHandle = driver.getWindowHandle();
        Set<String> handlesBeforeClick = new LinkedHashSet<>(driver.getWindowHandles());
        WebElement row = rows.get(rowIndex);
        String sourceRowText = row.getText().trim();
        clickTransactionRow(row);

        String transactionHandle = wait.until(webDriver -> webDriver.getWindowHandles().stream()
                .filter(handle -> !handlesBeforeClick.contains(handle))
                .findFirst()
                .orElse(null));

        try {
            driver.switchTo().window(transactionHandle);
            wait.until(webDriver -> documentIsReady());
            wait.until(webDriver -> webDriver.getCurrentUrl().contains("/vuatho/transaction"));
            wait.until(webDriver -> TextNormalizer.normalize(webDriver.findElement(By.tagName("body")).getText())
                    .contains("chi tiet giao dich"));
            // Giữ tab chi tiết đủ lâu để người chạy test quan sát trước khi tab bị đóng.
            keepWorkerDetailVisible(observationDuration);
            return new TransactionDetailResult(
                    sourceRowText,
                    driver.getCurrentUrl(),
                    driver.findElement(By.tagName("body")).getText().trim());
        } finally {
            if (driver.getWindowHandles().contains(transactionHandle)) {
                driver.close();
            }
            driver.switchTo().window(parentHandle);
            wait.until(webDriver -> workerDetailIsOpen());
            wait.until(webDriver -> workerDetailTabIsSelected("Giao dịch"));
            wait.until(webDriver -> hasTransactionHistoryTable());
        }
    }

    /**
     * Kiểm tra bảng Danh sách đơn dịch vụ trong tab Đơn dịch vụ.
     * @return {@code true} khi bảng đúng aria-label đang hiển thị
     */
    public boolean hasWorkerServiceOrderTable() {
        return workerServiceOrderTable() != null;
    }

    /**
     * Đếm số đơn dịch vụ đang hiển thị ở trang hiện tại.
     * @return số dòng dữ liệu, không gồm header
     */
    public int workerServiceOrderRowCount() {
        return workerServiceOrderRows().size();
    }

    /**
     * Lấy các mã đơn dịch vụ đang hiển thị trong tab Đơn dịch vụ.
     * @return danh sách data-key theo thứ tự bảng
     */
    public List<String> workerServiceOrderIds() {
        return workerServiceOrderRows().stream()
                .map(row -> row.getAttribute("data-key"))
                .filter(value -> value != null && value.matches("\\d+"))
                .toList();
    }

    /**
     * Click mã đơn dịch vụ, kiểm tra tab chi tiết mới rồi đóng tab và quay lại hồ sơ thợ.
     * @param rowIndex chỉ số dòng bắt đầu từ {@code 0}
     * @param observationDuration thời gian giữ tab chi tiết để quan sát
     * @return dữ liệu dùng để đối chiếu mã đơn và trang chi tiết
     */
    public ServiceOrderDetailResult openWorkerServiceOrderInNewTabAndReturn(
            int rowIndex,
            Duration observationDuration) {
        List<WebElement> rows = workerServiceOrderRows();
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new IllegalArgumentException("Service order row index is out of range: " + rowIndex);
        }

        String parentHandle = driver.getWindowHandle();
        Set<String> handlesBeforeClick = new LinkedHashSet<>(driver.getWindowHandles());
        WebElement row = rows.get(rowIndex);
        String orderId = row.getAttribute("data-key").trim();
        String sourceRowText = row.getText().trim();
        WebElement clickableOrderCode = row.findElement(By.cssSelector(
                "td[role='rowheader'] div.cursor-pointer, td[role='rowheader'] [class*='cursor-pointer']"));
        clickCandidate(clickableOrderCode);

        String detailHandle = wait.until(webDriver -> webDriver.getWindowHandles().stream()
                .filter(handle -> !handlesBeforeClick.contains(handle))
                .findFirst()
                .orElse(null));

        try {
            driver.switchTo().window(detailHandle);
            wait.until(webDriver -> documentIsReady());
            wait.until(webDriver -> webDriver.findElement(By.tagName("body")).getText().contains(orderId)
                    || webDriver.getCurrentUrl().contains(orderId));
            keepWorkerDetailVisible(observationDuration);
            return new ServiceOrderDetailResult(
                    orderId,
                    sourceRowText,
                    driver.getCurrentUrl(),
                    driver.findElement(By.tagName("body")).getText().trim());
        } finally {
            if (driver.getWindowHandles().contains(detailHandle)) {
                driver.close();
            }
            driver.switchTo().window(parentHandle);
            wait.until(webDriver -> workerDetailIsOpen());
            wait.until(webDriver -> workerDetailTabIsSelected("Đơn dịch vụ"));
            wait.until(webDriver -> hasWorkerServiceOrderTable());
        }
    }

    /**
     * Kiểm tra danh sách Đã giới thiệu có ít nhất một link hồ sơ thợ.
     * @return {@code true} khi tiêu đề và link hồ sơ đều hiển thị
     */
    public boolean hasReferredWorkerList() {
        return referredWorkerHeading() != null && !referredWorkerLinks().isEmpty();
    }

    /**
     * Đếm số thợ đang hiển thị trong phần Đã giới thiệu.
     * @return số link hồ sơ thợ trong grid
     */
    public int referredWorkerCount() {
        return referredWorkerLinks().size();
    }

    /**
     * Click một thợ đã được giới thiệu, kiểm tra tab mới rồi đóng và quay lại hồ sơ gốc.
     * @param workerIndex chỉ số thợ bắt đầu từ {@code 0}
     * @param observationDuration thời gian giữ tab hồ sơ mới để quan sát
     * @return thông tin ID, tên và trang hồ sơ dùng để đối chiếu
     */
    public ReferredWorkerDetailResult openReferredWorkerInNewTabAndReturn(
            int workerIndex,
            Duration observationDuration) {
        List<WebElement> links = referredWorkerLinks();
        if (workerIndex < 0 || workerIndex >= links.size()) {
            throw new IllegalArgumentException("Referred worker index is out of range: " + workerIndex);
        }

        String parentHandle = driver.getWindowHandle();
        Set<String> handlesBeforeClick = new LinkedHashSet<>(driver.getWindowHandles());
        WebElement workerLink = links.get(workerIndex);
        String href = workerLink.getAttribute("href");
        String workerId = href.replaceFirst(".*[?&]id=(\\d+).*", "$1");
        String workerName = workerLink.findElement(By.cssSelector("span:first-child")).getText().trim();
        clickCandidate(workerLink);

        String detailHandle = wait.until(webDriver -> webDriver.getWindowHandles().stream()
                .filter(handle -> !handlesBeforeClick.contains(handle))
                .findFirst()
                .orElse(null));

        try {
            driver.switchTo().window(detailHandle);
            wait.until(webDriver -> documentIsReady());
            wait.until(webDriver -> webDriver.getCurrentUrl().contains("/vuatho/user")
                    && webDriver.getCurrentUrl().matches(".*[?&]id=" + workerId + "(?:&.*)?$"));
            wait.until(webDriver -> !webDriver.findElement(By.tagName("body")).getText().isBlank());
            keepWorkerDetailVisible(observationDuration);
            return new ReferredWorkerDetailResult(
                    workerId,
                    workerName,
                    driver.getCurrentUrl(),
                    driver.findElement(By.tagName("body")).getText().trim());
        } finally {
            if (driver.getWindowHandles().contains(detailHandle)) {
                driver.close();
            }
            driver.switchTo().window(parentHandle);
            wait.until(webDriver -> workerDetailIsOpen());
            wait.until(webDriver -> workerDetailTabIsSelected("Giới thiệu"));
            wait.until(webDriver -> hasReferredWorkerList());
        }
    }

    /**
     * Kiểm tra tab Bài đăng đã hiển thị ít nhất một card có media để mở chi tiết.
     * @return {@code true} khi danh sách có bài đăng có thể click
     */
    public boolean hasWorkerPostList() {
        return !workerPostCards().isEmpty();
    }

    /**
     * Đếm số bài đăng đang hiển thị ở trang hiện tại.
     * @return số card bài đăng
     */
    public int workerPostCount() {
        return workerPostCards().size();
    }

    /**
     * Click media của một bài đăng, xác nhận giao diện xem chi tiết xuất hiện rồi đóng lại.
     * Phương thức hỗ trợ cả trường hợp chi tiết mở trong overlay và trong tab trình duyệt mới.
     * @param postIndex chỉ số bài đăng bắt đầu từ {@code 0}
     * @param observationDuration thời gian giữ chi tiết mở để người chạy quan sát
     * @return kết quả dùng để xác nhận thao tác click đã mở chi tiết
     */
    public WorkerPostDetailResult openWorkerPostDetailAndReturn(
            int postIndex,
            Duration observationDuration) {
        List<WebElement> cards = workerPostCards();
        if (postIndex < 0 || postIndex >= cards.size()) {
            throw new IllegalArgumentException("Worker post index is out of range: " + postIndex);
        }

        String parentHandle = driver.getWindowHandle();
        Set<String> handlesBeforeClick = new LinkedHashSet<>(driver.getWindowHandles());
        WebElement card = cards.get(postIndex);
        String sourcePostText = card.getText().trim();
        WebElement mediaButton = card.findElements(By.xpath(
                        ".//button[.//img[@alt='profile-post'] or .//video]"))
                .stream()
                .filter(button -> button.getRect().getWidth() > 0 && button.getRect().getHeight() > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Bài đăng thứ " + (postIndex + 1) + " không có media có thể click."));
        String sourceUrl = driver.getCurrentUrl();
        scrollToCenter(mediaButton);
        clickCandidate(mediaButton);

        String detailMode = new WebDriverWait(driver, Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(150))
                .until(webDriver -> {
                    String newHandle = webDriver.getWindowHandles().stream()
                            .filter(handle -> !handlesBeforeClick.contains(handle))
                            .findFirst()
                            .orElse(null);
                    if (newHandle != null) {
                        return "new-tab:" + newHandle;
                    }
                    if (!webDriver.getCurrentUrl().equals(sourceUrl)) {
                        return "same-tab";
                    }
                    if (workerPostDetailDialog() != null) {
                        return "overlay";
                    }
                    return null;
                });

        boolean openedInNewTab = detailMode.startsWith("new-tab:");
        boolean openedInSameTab = "same-tab".equals(detailMode);
        String detailHandle = openedInNewTab ? detailMode.substring("new-tab:".length()) : null;
        try {
            if (openedInNewTab) {
                driver.switchTo().window(detailHandle);
                wait.until(webDriver -> documentIsReady());
            }
            keepWorkerDetailVisible(observationDuration);
            return new WorkerPostDetailResult(
                    sourcePostText,
                    driver.getCurrentUrl(),
                    true);
        } finally {
            if (openedInNewTab) {
                if (driver.getWindowHandles().contains(detailHandle)) {
                    driver.close();
                }
                driver.switchTo().window(parentHandle);
            } else if (openedInSameTab) {
                driver.navigate().back();
                wait.until(webDriver -> documentIsReady());
            } else {
                closeWorkerPostDetailDialog();
            }
            wait.until(webDriver -> workerDetailIsOpen());
            wait.until(webDriver -> workerDetailTabIsSelected("Bài đăng"));
            wait.until(webDriver -> hasWorkerPostList());
        }
    }

    /**
     * Mở bài đăng đầu tiên và kiểm tra điều hướng ảnh, thu phóng, xoay ảnh trong modal.
     * Mỗi control đều được xác nhận bằng bộ đếm hoặc giá trị CSS transform sau khi click.
     * @param observationDuration thời gian giữ giao diện sau mỗi nhóm thao tác để quan sát
     * @return các trạng thái trước và sau thao tác
     */
    public WorkerPostViewerControlResult exerciseFirstWorkerPostViewerControls(Duration observationDuration) {
        List<WebElement> cards = workerPostCards();
        if (cards.isEmpty()) {
            throw new IllegalStateException("Tab Bài đăng không có card nào để kiểm tra control.");
        }

        WebElement mediaButton = cards.get(0).findElements(By.xpath(
                        ".//button[.//img[@alt='profile-post'] or .//video]"))
                .stream()
                .filter(button -> button.getRect().getWidth() > 0 && button.getRect().getHeight() > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Bài đăng đầu tiên không có media."));
        clickCandidate(mediaButton);
        WebElement dialog = wait.until(webDriver -> workerPostDetailDialog());

        try {
            WebElement viewerImage = wait.until(webDriver -> workerPostViewerImage());
            String initialCounter = workerPostMediaCounter(dialog);
            String initialSource = viewerImage.getAttribute("src");
            String initialTransform = workerPostViewerTransform();

            WebElement nextButton = dialog.findElement(By.cssSelector("button.absolute.right-4"));
            clickCandidate(nextButton);
            wait.until(webDriver -> !workerPostMediaCounter(workerPostDetailDialog()).equals(initialCounter)
                    && !workerPostViewerImage().getAttribute("src").equals(initialSource));
            String nextCounter = workerPostMediaCounter(workerPostDetailDialog());
            keepWorkerDetailVisible(observationDuration);

            WebElement currentDialog = workerPostDetailDialog();
            WebElement previousButton = currentDialog.findElement(By.cssSelector("button.absolute.left-4"));
            clickCandidate(previousButton);
            wait.until(webDriver -> workerPostMediaCounter(workerPostDetailDialog()).equals(initialCounter));
            String previousCounter = workerPostMediaCounter(workerPostDetailDialog());

            WebElement zoomInButton = workerPostDetailDialog().findElement(By.cssSelector("button[title='Phóng to']"));
            clickCandidate(zoomInButton);
            wait.until(webDriver -> !workerPostViewerTransform().equals(initialTransform));
            String zoomedTransform = workerPostViewerTransform();
            keepWorkerDetailVisible(observationDuration);

            WebElement zoomOutButton = workerPostDetailDialog().findElement(By.cssSelector("button[title='Thu nhỏ']"));
            clickCandidate(zoomOutButton);
            wait.until(webDriver -> workerPostViewerTransform().equals(initialTransform));
            String restoredZoomTransform = workerPostViewerTransform();

            WebElement rotateRightButton = workerPostDetailDialog().findElement(
                    By.cssSelector("button[title='Xoay phải']"));
            clickCandidate(rotateRightButton);
            wait.until(webDriver -> !workerPostViewerTransform().equals(initialTransform));
            String rotatedTransform = workerPostViewerTransform();
            keepWorkerDetailVisible(observationDuration);

            WebElement rotateLeftButton = workerPostDetailDialog().findElement(
                    By.cssSelector("button[title='Xoay trái']"));
            clickCandidate(rotateLeftButton);
            wait.until(webDriver -> workerPostViewerTransform().equals(initialTransform));
            String restoredRotationTransform = workerPostViewerTransform();

            return new WorkerPostViewerControlResult(
                    initialCounter,
                    nextCounter,
                    previousCounter,
                    initialTransform,
                    zoomedTransform,
                    restoredZoomTransform,
                    rotatedTransform,
                    restoredRotationTransform);
        } finally {
            closeWorkerPostDetailDialog();
            wait.until(webDriver -> workerDetailTabIsSelected("Bài đăng"));
            wait.until(webDriver -> hasWorkerPostList());
        }
    }

    /**
     * Kiểm tra tab Xử lý vi phạm có bảng lịch sử và nút mở popup xử phạt.
     * @return {@code true} khi tiêu đề bảng và nút Xử phạt đều hiển thị
     */
    public boolean hasWorkerViolationHistorySection() {
        return visibleElement(By.xpath("//*[normalize-space()='Lịch sử vi phạm']")) != null
                && visibleElement(By.xpath("//button[normalize-space()='Xử phạt']")) != null;
    }

    /**
     * Kiểm tra thợ hiện tại đã có ít nhất một bản ghi trong bảng Lịch sử vi phạm hay chưa.
     * Dòng trạng thái rỗng "Chưa có hành vi vi phạm" không được tính là một bản ghi.
     *
     * @return {@code true} khi bảng đang chứa dữ liệu xử phạt
     */
    public boolean hasWorkerViolationRecords() {
        String normalizedHistory = TextNormalizer.normalize(workerViolationHistoryText());
        if (normalizedHistory.isBlank()
                || normalizedHistory.contains("chua co hanh vi vi pham")) {
            return false;
        }

        WebElement heading = visibleElement(By.xpath("//*[normalize-space()='Lịch sử vi phạm']"));
        if (heading == null) {
            return false;
        }
        WebElement section = heading.findElements(
                        By.xpath("ancestor::div[.//button[normalize-space()='Xử phạt']][1]"))
                .stream()
                .findFirst()
                .orElse(heading);
        return section.findElements(By.cssSelector("tbody tr")).stream()
                .filter(WebElement::isDisplayed)
                .map(row -> row.getText().trim())
                .anyMatch(text -> !text.isBlank()
                        && !TextNormalizer.normalize(text).contains("chua co hanh vi vi pham"));
    }

    /**
     * Mở popup xử phạt, nhập dữ liệu mẫu, thao tác checkbox/radio rồi hủy bỏ.
     * Không click Áp dụng nên testcase không tạo dữ liệu xử phạt thật.
     * @param orderId mã đơn dịch vụ mẫu
     * @param penaltyTitle tiêu đề xử phạt mẫu
     * @param reason mô tả lý do mẫu
     * @param amount số tiền phạt mẫu
     * @param blockingDays số ngày chặn mẫu
     * @param verifyAlternativeCloseMethods có kiểm tra thêm nút X và backdrop hay không
     * @param observationDuration thời gian giữ popup để người chạy quan sát
     * @return trạng thái các control và kết quả hủy popup
     */
    public WorkerPenaltyDialogResult fillWorkerPenaltyDialogAndCancel(
            String orderId,
            String penaltyTitle,
            String reason,
            String amount,
            String blockingDays,
            boolean verifyAlternativeCloseMethods,
            Duration observationDuration) {
        String historyBefore = workerViolationHistoryText();
        WebElement punishButton = wait.until(webDriver -> visibleElement(
                By.xpath("//button[normalize-space()='Xử phạt']")));
        clickCandidate(punishButton);
        WebElement dialog = wait.until(webDriver -> workerPenaltyDialog());

        String normalizedText = TextNormalizer.normalize(dialog.getText());
        boolean requiredFieldsPresent = normalizedText.contains("ma don dich vu")
                && normalizedText.contains("tieu de xu phat")
                && normalizedText.contains("mo ta ly do")
                && normalizedText.contains("so tien xu phat")
                && normalizedText.contains("thoi han chan")
                && normalizedText.contains("pham vi han che")
                && dialog.findElements(By.cssSelector("input")).size() >= 4
                && !dialog.findElements(By.cssSelector("textarea")).isEmpty()
                && dialog.findElements(By.xpath(".//button[normalize-space()='Áp dụng']")).size() == 1
                && dialog.findElements(By.xpath(".//button[normalize-space()='Hủy bỏ']")).size() == 1;

        WebElement orderInput = penaltyField(dialog, "Nhập mã đơn");
        WebElement titleInput = penaltyField(dialog, "VD: Vi phạm quy định ứng xử");
        WebElement reasonInput = penaltyField(dialog, "Nhập chi tiết hành vi vi phạm");
        WebElement amountInput = penaltyField(dialog, "Nhập số tiền");
        WebElement blockingDaysInput = penaltyField(dialog, "Số ngày");
        boolean initialStateValid = orderInput.getAttribute("value").isBlank()
                && titleInput.getAttribute("value").isBlank()
                && reasonInput.getAttribute("value").isBlank()
                && amountInput.getAttribute("value").isBlank()
                && blockingDaysInput.getAttribute("value").isBlank();

        // Gửi form rỗng chỉ để kiểm tra validation; không có dữ liệu nên không thể tạo xử phạt.
        WebElement applyButton = dialog.findElement(By.xpath(".//button[normalize-space()='Áp dụng']"));
        clickCandidate(applyButton);
        keepWorkerDetailVisible(Duration.ofSeconds(1));
        boolean emptySubmissionBlocked = workerPenaltyDialog() != null
                && workerViolationHistoryText().equals(historyBefore);

        enterPenaltyValue(orderInput, orderId);
        enterPenaltyValue(titleInput, penaltyTitle);
        enterPenaltyValue(reasonInput, reason);
        enterPenaltyValue(amountInput, amount);

        WebElement permanentCheckbox = dialog.findElements(By.cssSelector("input[type='checkbox']"))
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy checkbox Vĩnh viễn."));
        clickFormControl(permanentCheckbox);
        wait.until(webDriver -> permanentCheckbox.isSelected());
        wait.until(webDriver -> !blockingDaysInput.isEnabled());
        boolean permanentDisablesBlockingDays = !blockingDaysInput.isEnabled();
        clickFormControl(permanentCheckbox);
        wait.until(webDriver -> !permanentCheckbox.isSelected());
        wait.until(webDriver -> blockingDaysInput.isEnabled());
        boolean permanentOffEnablesBlockingDays = blockingDaysInput.isEnabled();

        enterPenaltyValue(blockingDaysInput, blockingDays);

        List<WebElement> restrictionRadios = dialog.findElements(By.cssSelector("input[type='radio']"));
        if (restrictionRadios.size() < 2) {
            throw new IllegalStateException("Popup không có đủ hai lựa chọn phạm vi hạn chế.");
        }
        WebElement findJobRadio = restrictionRadios.get(0);
        WebElement loginRadio = restrictionRadios.get(1);
        clickFormControl(findJobRadio);
        wait.until(webDriver -> findJobRadio.isSelected() && !loginRadio.isSelected());
        clickFormControl(loginRadio);
        wait.until(webDriver -> loginRadio.isSelected() && !findJobRadio.isSelected());
        boolean restrictionOptionsMutuallyExclusive = loginRadio.isSelected() && !findJobRadio.isSelected();

        boolean testDataEntered = orderId.equals(orderInput.getAttribute("value"))
                && penaltyTitle.equals(titleInput.getAttribute("value"))
                && reason.equals(reasonInput.getAttribute("value"))
                && !amountInput.getAttribute("value").isBlank()
                && blockingDays.equals(blockingDaysInput.getAttribute("value"));

        keepWorkerDetailVisible(observationDuration);
        WebElement cancelButton = dialog.findElement(By.xpath(".//button[normalize-space()='Hủy bỏ']"));
        clickCandidate(cancelButton);
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> workerPenaltyDialog() == null);
        boolean cancelledWithoutCreatingViolation = hasWorkerViolationHistorySection()
                && workerViolationHistoryText().equals(historyBefore);

        boolean topCloseButtonWorks = true;
        if (verifyAlternativeCloseMethods) {
            topCloseButtonWorks = openAndClosePenaltyDialogWithTopButton();
        }

        return new WorkerPenaltyDialogResult(
                requiredFieldsPresent,
                initialStateValid,
                emptySubmissionBlocked,
                testDataEntered,
                permanentDisablesBlockingDays,
                permanentOffEnablesBlockingDays,
                restrictionOptionsMutuallyExclusive,
                cancelledWithoutCreatingViolation,
                topCloseButtonWorks);
    }

    /**
     * Nhập form hợp lệ và bấm Áp dụng để tạo một bản ghi xử phạt thật trên sandbox.
     * @param orderId mã đơn thuộc user đang mở
     * @param penaltyTitle tiêu đề duy nhất của lần chạy
     * @param reason mô tả lý do
     * @param amount số tiền phạt
     * @param blockingDays số ngày chặn
     * @param observationDuration thời gian giữ form để quan sát trước khi áp dụng
     * @return kết quả đối chiếu popup và lịch sử sau khi lưu
     */
    public WorkerPenaltyApplyResult applyWorkerPenalty(
            String orderId,
            String penaltyTitle,
            String reason,
            String amount,
        String blockingDays,
        Duration observationDuration) {
        String historyBefore = workerViolationHistoryText();
        if (historyBefore.contains(orderId)) {
            System.out.println("[WORKER VIOLATION APPLY] Don " + orderId
                    + " da co trong lich su; khong tao ban phat trung.");
            return new WorkerPenaltyApplyResult(
                    orderId,
                    penaltyTitle,
                    false,
                    false,
                    true,
                    true,
                    true);
        }
        WebElement punishButton = wait.until(webDriver -> visibleElement(
                By.xpath("//button[normalize-space()='Xử phạt']")));
        clickCandidate(punishButton);
        WebElement dialog;
        try {
            dialog = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(webDriver -> workerPenaltyDialog());
        } catch (TimeoutException exception) {
            System.out.println("[WORKER VIOLATION APPLY] He thong dang chan tao xu phat trung cho user nay.");
            return new WorkerPenaltyApplyResult(orderId, penaltyTitle, false, true, false, false, false);
        }

        enterPenaltyValue(penaltyField(dialog, "Nhập mã đơn"), orderId);
        enterPenaltyValue(penaltyField(dialog, "VD: Vi phạm quy định ứng xử"), penaltyTitle);
        enterPenaltyValue(penaltyField(dialog, "Nhập chi tiết hành vi vi phạm"), reason);
        enterPenaltyValue(penaltyField(dialog, "Nhập số tiền"), amount);
        enterPenaltyValue(penaltyField(dialog, "Số ngày"), blockingDays);

        List<WebElement> radios = dialog.findElements(By.cssSelector("input[type='radio']"));
        if (radios.isEmpty()) {
            throw new IllegalStateException("Popup không có phạm vi hạn chế để áp dụng.");
        }
        clickFormControl(radios.get(0));
        wait.until(webDriver -> radios.get(0).isSelected());
        keepWorkerDetailVisible(observationDuration);

        clickCandidate(dialog.findElement(By.xpath(".//button[normalize-space()='Áp dụng']")));
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(webDriver -> workerPenaltyDialog() == null);
        PageLoadSynchronizer.waitForDataToSettle(driver);
        // Bảng lịch sử không tự fetch lại sau khi popup đóng; đổi tab để tải dữ liệu mới từ backend.
        openWorkerDetailTab("Tổng quan");
        openWorkerDetailTab("Xử lý vi phạm");
        String historyAfter = workerViolationHistoryText();
        boolean historyChanged = !historyAfter.equals(historyBefore);
        boolean recordDisplayed = historyAfter.contains(orderId) || historyAfter.contains(penaltyTitle);
        if (!historyChanged) {
            System.out.println("[WORKER VIOLATION APPLY] Popup da dong; bang lich su chua refresh kip du lieu moi.");
        }
        return new WorkerPenaltyApplyResult(
                orderId,
                penaltyTitle,
                true,
                false,
                workerPenaltyDialog() == null,
                historyChanged,
                recordDisplayed);
    }

    /**
     * Kiểm tra button số trang cụ thể có hiển thị trong pagination của bảng Lịch sử giao dịch.
     * @param pageNumber số trang cần kiểm tra
     * @return {@code true} khi tìm thấy button tương ứng
     */
    public boolean hasTransactionHistoryPageButton(int pageNumber) {
        WebElement wrapper = transactionHistoryPaginationWrapper();
        if (wrapper == null) {
            return false;
        }
        scrollToCenter(wrapper);
        return wrapper.findElements(transactionHistoryPageButton(pageNumber)).stream()
                .anyMatch(WebElement::isDisplayed);
    }

    /**
     * Đọc trang đang active từ button có {@code aria-current=true}.
     * @return số trang hiện tại của bảng Lịch sử giao dịch
     */
    public int currentTransactionHistoryPage() {
        WebElement wrapper = requireTransactionHistoryPaginationWrapper();
        WebElement activePage = wrapper.findElement(By.cssSelector(
                "li[data-slot='item'][aria-current='true'][aria-label^='pagination item']"));
        return paginationNumber(activePage);
    }

    /**
     * Click trực tiếp button số trang và chờ trạng thái active cùng dữ liệu bảng thay đổi.
     * @param pageNumber số trang cần chuyển đến
     */
    public void clickTransactionHistoryPage(int pageNumber) {
        int pageBeforeClick = currentTransactionHistoryPage();
        String firstRowBeforeClick = transactionHistoryRows().stream()
                .findFirst()
                .map(row -> row.getText().trim())
                .orElse("");
        WebElement wrapper = requireTransactionHistoryPaginationWrapper();
        WebElement pageButton = wrapper.findElement(transactionHistoryPageButton(pageNumber));

        clickCandidate(pageButton);
        wait.until(webDriver -> currentTransactionHistoryPage() == pageNumber);
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        PageLoadSynchronizer.waitForDataToSettle(driver);
        if (pageBeforeClick != pageNumber && !firstRowBeforeClick.isBlank()) {
            wait.until(webDriver -> transactionHistoryRows().stream()
                    .findFirst()
                    .map(row -> !row.getText().trim().equals(firstRowBeforeClick))
                    .orElse(false));
        }
    }

    /**
     * Mở ten criteria tab trong luồng kiểm thử.
     */
    public void openTenCriteriaTab() {
        openWorkerDetailTab("10 Tiêu chí");
        wait.until(webDriver -> hasTenCriteriaSection());
    }

    /**
     * Kiểm tra điều kiện has ten criteria section.
     * @return kết quả has ten criteria section sau khi xử lý
     */
    public boolean hasTenCriteriaSection() {
        String normalized = TextNormalizer.normalize(workerDetailText());
        return normalized.contains("10 tieu chi")
                && normalized.contains("tieu chi danh gia tho")
                && normalized.contains("trang thai")
                && normalized.contains("thao tac");
    }

    /**
     * Thực hiện xử lý ten criteria row count trong luồng kiểm thử.
     * @return kết quả ten criteria row count sau khi xử lý
     */
    public int tenCriteriaRowCount() {
        return tenCriteriaRows().size();
    }

    /**
     * Thực hiện xử lý ten criteria rows have statuses trong luồng kiểm thử.
     * @return kết quả ten criteria rows have statuses sau khi xử lý
     */
    public boolean tenCriteriaRowsHaveStatuses() {
        return tenCriteriaRows().stream()
                .allMatch(row -> !criteriaStatusFromText(row.getText()).isBlank());
    }

    /**
     * Thực hiện xử lý ten criteria summary counts are visible trong luồng kiểm thử.
     * @return kết quả ten criteria summary counts are visible sau khi xử lý
     */
    public boolean tenCriteriaSummaryCountsAreVisible() {
        String normalized = TextNormalizer.normalize(workerDetailText());
        return normalized.contains("dat")
                && normalized.contains("chua dat")
                && normalized.contains("cho xac nhan");
    }

    /**
     * Thực hiện xử lý criteria indexes with update button trong luồng kiểm thử.
     * @return kết quả criteria indexes with update button sau khi xử lý
     */
    public List<Integer> criteriaIndexesWithUpdateButton() {
        return tenCriteriaRows().stream()
                .filter(row -> criteriaUpdateButton(row) != null)
                .map(this::criteriaIndex)
                .toList();
    }

    /**
     * Kiểm tra điều kiện has visible criteria update button.
     * @return kết quả has visible criteria update button sau khi xử lý
     */
    public boolean hasVisibleCriteriaUpdateButton() {
        return !visibleCriteriaUpdateButtons().isEmpty();
    }

    /**
     * Trả về visible criteria update button count từ trạng thái hiện tại.
     * @return kết quả visible criteria update button count sau khi xử lý
     */
    public int visibleCriteriaUpdateButtonCount() {
        return visibleCriteriaUpdateButtons().size();
    }

    /**
     * Thực hiện xử lý criteria status trong luồng kiểm thử.
     * @param index giá trị index được truyền vào
     * @return kết quả criteria status sau khi xử lý
     */
    public String criteriaStatus(int index) {
        WebElement row = requireCriteriaRow(index);
        return criteriaStatusFromText(row.getText());
    }

    /**
     * Trả về first visible update criteria status từ trạng thái hiện tại.
     * @return kết quả first visible update criteria status sau khi xử lý
     */
    public String firstVisibleUpdateCriteriaStatus() {
        return visibleUpdateCriteriaStatusAt(0);
    }

    /**
     * Kích hoạt criteria update button trong luồng kiểm thử.
     * @param index giá trị index được truyền vào
     */
    public void clickCriteriaUpdateButton(int index) {
        WebElement row = requireCriteriaRow(index);
        WebElement button = criteriaUpdateButton(row);
        if (button == null) {
            throw new IllegalStateException("Không tìm thấy nút Cập nhật ở tiêu chí " + index + ".");
        }
        clickCandidate(button);
        wait.until(webDriver -> criteriaStatusDialog() != null);
    }

    /**
     * Kích hoạt first visible criteria update button trong luồng kiểm thử.
     */
    public void clickFirstVisibleCriteriaUpdateButton() {
        clickVisibleCriteriaUpdateButtonAt(0);
    }

    /**
     * Trả về visible update criteria status at từ trạng thái hiện tại.
     * @param buttonIndex giá trị button index được truyền vào
     * @return kết quả visible update criteria status at sau khi xử lý
     */
    public String visibleUpdateCriteriaStatusAt(int buttonIndex) {
        WebElement row = criteriaRowForButton(visibleCriteriaUpdateButtonAt(buttonIndex));
        return criteriaStatusFromText(row.getText());
    }

    /**
     * Kích hoạt visible criteria update button at trong luồng kiểm thử.
     * @param buttonIndex giá trị button index được truyền vào
     */
    public void clickVisibleCriteriaUpdateButtonAt(int buttonIndex) {
        WebElement button = visibleCriteriaUpdateButtonAt(buttonIndex);
        clickCandidate(button);
        wait.until(webDriver -> criteriaStatusDialog() != null);
    }

    /**
     * Mở criteria status update trong luồng kiểm thử.
     * @param index giá trị index được truyền vào
     */
    public void openCriteriaStatusUpdate(int index) {
        clickCriteriaUpdateButton(index);
    }

    /**
     * Thực hiện xử lý criteria status dialog is open for trong luồng kiểm thử.
     * @param index giá trị index được truyền vào
     * @return kết quả criteria status dialog is open for sau khi xử lý
     */
    public boolean criteriaStatusDialogIsOpenFor(int index) {
        WebElement dialog = criteriaStatusDialog();
        if (dialog == null) {
            return false;
        }
        String dialogText = TextNormalizer.normalize(dialog.getText());
        String criterionTitle = criteriaTitle(index);
        return dialogText.contains("cap nhat trang thai")
                && (criterionTitle.isBlank() || dialogText.contains(TextNormalizer.normalize(criterionTitle)));
    }

    /**
     * Thực hiện xử lý criteria status dialog has options trong luồng kiểm thử.
     * @return kết quả criteria status dialog has options sau khi xử lý
     */
    public boolean criteriaStatusDialogHasOptions() {
        WebElement dialog = criteriaStatusDialog();
        if (dialog == null) {
            return false;
        }
        String normalized = TextNormalizer.normalize(dialog.getText());
        return normalized.contains("dat")
                && normalized.contains("chua dat")
                && normalized.contains("xac nhan")
                && normalized.contains("huy");
    }

    /**
     * Kích hoạt criteria status trong luồng kiểm thử.
     * @param statusLabel giá trị status label được truyền vào
     */
    public void selectCriteriaStatus(String statusLabel) {
        WebElement dialog = requireCriteriaStatusDialog();
        String expected = TextNormalizer.normalize(statusLabel);
        WebElement option = visibleChildren(dialog, By.cssSelector("button, label, [role='radio']"))
                .stream()
                .filter(element -> {
                    String normalized = TextNormalizer.normalize(element.getText());
                    return normalized.equals(expected);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lựa chọn trạng thái " + statusLabel + "."));
        clickCandidate(option);
        wait.until(webDriver -> criteriaStatusDialog() != null);
    }

    /**
     * Thực hiện xử lý criteria confirm button is enabled trong luồng kiểm thử.
     * @return kết quả criteria confirm button is enabled sau khi xử lý
     */
    public boolean criteriaConfirmButtonIsEnabled() {
        WebElement dialog = criteriaStatusDialog();
        if (dialog == null) {
            return false;
        }
        return visibleChildren(dialog, By.cssSelector("button"))
                .stream()
                .filter(button -> TextNormalizer.normalize(button.getText()).contains("xac nhan"))
                .findFirst()
                .map(this::isEnabledButton)
                .orElse(false);
    }

    /**
     * Kiểm tra điều kiện cancel criteria status update.
     */
    public void cancelCriteriaStatusUpdate() {
        clickCriteriaDialogButton("Hủy");
        wait.until(webDriver -> criteriaStatusDialog() == null);
    }

    /**
     * Thực hiện xử lý close criteria status update trong luồng kiểm thử.
     */
    public void closeCriteriaStatusUpdate() {
        WebElement dialog = requireCriteriaStatusDialog();
        WebElement closeButton = visibleChildren(dialog, By.cssSelector("button, [role='button']"))
                .stream()
                .filter(button -> {
                    String normalized = TextNormalizer.normalize(String.join(" ",
                            button.getText(),
                            button.getAttribute("aria-label"),
                            button.getAttribute("title")));
                    return normalized.equals("x")
                            || normalized.contains("close")
                            || normalized.contains("dong");
                })
                .findFirst()
                .orElse(null);
        if (closeButton != null) {
            clickCandidate(closeButton);
        } else {
            dismissTransientOverlays();
        }
        wait.until(webDriver -> criteriaStatusDialog() == null);
    }

    /**
     * Thực hiện xử lý confirm criteria status update trong luồng kiểm thử.
     */
    public void confirmCriteriaStatusUpdate() {
        String stateBefore = workerDetailState();
        clickCriteriaDialogButton("Xác nhận");
        wait.until(webDriver -> criteriaStatusDialog() == null);
        wait.until(webDriver -> documentIsReady());
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> hasTenCriteriaSection());
        wait.until(webDriver -> tenCriteriaRows().size() == 10);
        wait.until(webDriver -> noLoadingIndicatorIsVisible()
                || !workerDetailState().equals(stateBefore));
    }

    /**
     * Cập nhật criteria status trong luồng kiểm thử.
     * @param index giá trị index được truyền vào
     * @param statusLabel giá trị status label được truyền vào
     */
    public void updateCriteriaStatus(int index, String statusLabel) {
        openCriteriaStatusUpdate(index);
        selectCriteriaStatus(statusLabel);
        confirmCriteriaStatusUpdate();
        wait.until(webDriver -> criteriaStatus(index).equals(statusLabel));
    }

    /**
     * Cập nhật first visible criteria status trong luồng kiểm thử.
     * @param statusLabel giá trị status label được truyền vào
     */
    public void updateFirstVisibleCriteriaStatus(String statusLabel) {
        updateVisibleCriteriaStatusAt(0, statusLabel);
    }

    /**
     * Cập nhật visible criteria status at trong luồng kiểm thử.
     * @param buttonIndex giá trị button index được truyền vào
     * @param statusLabel giá trị status label được truyền vào
     */
    public void updateVisibleCriteriaStatusAt(int buttonIndex, String statusLabel) {
        clickVisibleCriteriaUpdateButtonAt(buttonIndex);
        selectCriteriaStatus(statusLabel);
        confirmCriteriaStatusUpdate();
        wait.until(webDriver -> visibleUpdateCriteriaStatusAt(buttonIndex).equals(statusLabel));
    }

    /**
     * Thực hiện xử lý worker detail tab is selected trong luồng kiểm thử.
     * @param tabLabel giá trị tab label được truyền vào
     * @return kết quả worker detail tab is selected sau khi xử lý
     */
    public boolean workerDetailTabIsSelected(String tabLabel) {
        WebElement tab = visibleWorkerDetailTab(tabLabel);
        if (tab == null) {
            return false;
        }
        return "true".equalsIgnoreCase(tab.getAttribute("aria-selected"))
                || "true".equalsIgnoreCase(tab.getAttribute("data-selected"));
    }

    /**
     * Thực hiện xử lý worker detail text trong luồng kiểm thử.
     * @return kết quả worker detail text sau khi xử lý
     */
    public String workerDetailText() {
        String surfaceText = visibleDetailSurfaces().stream()
                .map(WebElement::getText)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .orElse("");
        return surfaceText.isBlank() ? bodyText() : surfaceText;
    }

    /**
     * Thực hiện xử lý keep worker detail visible trong luồng kiểm thử.
     * @param duration giá trị duration được truyền vào
     */
    public void keepWorkerDetailVisible(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while keeping worker detail visible.", exception);
        }
    }

    /**
     * Thực hiện xử lý keep criteria status dialog visible trong luồng kiểm thử.
     * @param duration giá trị duration được truyền vào
     */
    public void keepCriteriaStatusDialogVisible(Duration duration) {
        wait.until(webDriver -> criteriaStatusDialog() != null);
        keepWorkerDetailVisible(duration);
    }

    /**
     * Thực hiện xử lý close worker detail trong luồng kiểm thử.
     */
    public void closeWorkerDetail() {
        WebElement closeButton = visibleWorkerDetailCloseButton();
        if (closeButton != null) {
            clickCandidate(closeButton);
        } else {
            dismissTransientOverlays();
        }
        if (!driver.getCurrentUrl().contains(WORKER_PROFILE_ROUTE)) {
            driver.navigate().back();
        }
        workerDetailOpened = false;
        waitUntilLoaded();
    }

    /**
     * Thực hiện xử lý close worker detail if open trong luồng kiểm thử.
     */
    public void closeWorkerDetailIfOpen() {
        if (workerDetailOpened || workerDetailShellIsVisible()) {
            closeWorkerDetail();
        }
    }

    /**
     * Thực hiện xử lý search input trong luồng kiểm thử.
     * @return kết quả search input sau khi xử lý
     */
    private WebElement searchInput() {
        WebElement input = (WebElement) js().executeScript(
                "const expected=arguments[0].toLocaleLowerCase();"
                        + "return [...document.querySelectorAll('input')].find(e=>{"
                        + " const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + " const p=(e.placeholder||e.getAttribute('aria-label')||'').toLocaleLowerCase();"
                        + " return r.x>300&&r.width>0&&r.height>0&&s.display!=='none'"
                        + "  &&s.visibility!=='hidden'&&p.includes(expected);"
                        + "})||null;",
                SEARCH_PLACEHOLDER);
        if (input == null) {
            throw new IllegalStateException("Không tìm thấy ô tìm kiếm thợ.");
        }
        return input;
    }

    /**
     * Trả về visible search mode select từ trạng thái hiện tại.
     * @return kết quả visible search mode select sau khi xử lý
     */
    private WebElement visibleSearchModeSelect() {
        WebElement select = (WebElement) js().executeScript(
                "return [...document.querySelectorAll('select')].find(e=>{"
                        + " const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + " return r.x>300&&r.width>0&&r.height>0&&s.display!=='none'"
                        + "  &&s.visibility!=='hidden';"
                        + "})||null;");
        if (select == null) {
            throw new IllegalStateException("Không tìm thấy dropdown chọn kiểu tìm kiếm thợ.");
        }
        return select;
    }

    /**
     * Thực hiện xử lý filter panel text trong luồng kiểm thử.
     * @return kết quả filter panel text sau khi xử lý
     */
    private String filterPanelText() {
        Object value = js().executeScript(
                "const group=document.querySelector('[role=\"radiogroup\"]');"
                        + "const panel=group ? group.closest('.px-1,.space-y-3,[data-slot=\"content\"]') : null;"
                        + "return panel ? (panel.innerText || panel.textContent || '').trim() : '';");
        return String.valueOf(value);
    }

    /**
     * Trả về visible kyc status label từ trạng thái hiện tại.
     * @param statusLabel giá trị status label được truyền vào
     * @return kết quả visible kyc status label sau khi xử lý
     */
    private WebElement visibleKycStatusLabel(String statusLabel) {
        return driver.findElements(By.cssSelector("[role='radiogroup'] label")).stream()
                .filter(WebElement::isDisplayed)
                .filter(label -> label.getText().trim().equals(statusLabel))
                .findFirst()
                .orElse(null);
    }

    /**
     * Trả về visible row texts từ trạng thái hiện tại.
     * @return kết quả visible row texts sau khi xử lý
     */
    private List<String> visibleRowTexts() {
        return visibleRows().stream()
                .map(WebElement::getText)
                .filter(text -> text != null && !text.isBlank())
                .toList();
    }

    /**
     * Trả về first worker row từ trạng thái hiện tại.
     * @return kết quả first worker row sau khi xử lý
     */
    private WebElement firstWorkerRow() {
        wait.until(webDriver -> !visibleRows().isEmpty());
        return visibleRows().get(0);
    }

    /**
     * Tìm trực tiếp một table row hiển thị có chứa đúng tên thợ cần mở.
     */
    private WebElement visibleWorkerRowByName(String workerName) {
        String expectedName = TextNormalizer.normalize(workerName);
        return driver.findElements(By.cssSelector("tbody tr, [role='row']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(row -> TextNormalizer.normalize(row.getText()).contains(expectedName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Thực hiện xử lý worker info cell lines trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     * @return kết quả worker info cell lines sau khi xử lý
     */
    private List<String> workerInfoCellLines(WebElement row) {
        List<WebElement> cells = row.findElements(By.cssSelector("td, [role='gridcell']")).stream()
                .filter(WebElement::isDisplayed)
                .toList();
        if (cells.size() < 2) {
            return List.of();
        }
        return java.util.Arrays.stream(cells.get(1).getText().split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    /**
     * Trả về first worker info cell lines từ trạng thái hiện tại.
     * @return kết quả first worker info cell lines sau khi xử lý
     */
    private List<String> firstWorkerInfoCellLines() {
        List<WebElement> cells = firstWorkerRow().findElements(By.cssSelector("td, [role='gridcell']")).stream()
                .filter(WebElement::isDisplayed)
                .toList();
        if (cells.size() < 2) {
            throw new IllegalStateException("Dòng thợ đầu tiên không có cột Thông tin thợ.");
        }
        return java.util.Arrays.stream(cells.get(1).getText().split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    /**
     * Thực hiện xử lý worker information actions trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     * @return kết quả worker information actions sau khi xử lý
     */
    private List<WebElement> workerInformationActions(WebElement row) {
        List<WebElement> cells = row.findElements(By.cssSelector("td, [role='gridcell']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(cell -> cell.getRect().getWidth() > 0 && cell.getRect().getHeight() > 0)
                .toList();
        if (cells.size() >= 2) {
            WebElement infoCell = cells.get(1);
            List<WebElement> children = infoCell.findElements(By.cssSelector("a, button, [role='button'], span, p, div"))
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0)
                    .filter(element -> !element.getText().isBlank())
                    .toList();
            return java.util.stream.Stream.concat(
                            java.util.stream.Stream.concat(children.stream(), java.util.stream.Stream.of(infoCell)),
                            java.util.stream.Stream.of(row))
                    .toList();
        }
        return List.of(row);
    }

    /**
     * Trả về visible filter button từ trạng thái hiện tại.
     * @param label giá trị label được truyền vào
     * @return kết quả visible filter button sau khi xử lý
     */
    private WebElement visibleFilterButton(String label) {
        return driver.findElements(By.xpath("//button[normalize-space(.)='" + label + "']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() > 300)
                .findFirst()
                .orElse(null);
    }

    /**
     * Xóa hoặc đặt lại search if needed trong luồng kiểm thử.
     */
    private void clearSearchIfNeeded() {
        WebElement input = searchInput();
        String currentValue = String.valueOf(input.getAttribute("value"));
        if (currentValue.isBlank()) {
            return;
        }

        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        wait.until(webDriver -> searchInput().getAttribute("value").isBlank());
        waitForListToSettle();
    }

    /**
     * Kích hoạt top reset button if available trong luồng kiểm thử.
     */
    private void clickTopResetButtonIfAvailable() {
        WebElement button = visibleTopResetButton();
        if (button == null) {
            return;
        }
        clickCandidate(button);
        waitForListToSettle();
    }

    /**
     * Xóa hoặc đặt lại filter panel if available trong luồng kiểm thử.
     */
    private void resetFilterPanelIfAvailable() {
        WebElement filterButton = visibleElement(FILTER_BUTTON);
        if (filterButton == null) {
            return;
        }

        clickCandidate(filterButton);
        wait.until(webDriver -> visibleElement(KYC_STATUS_GROUP) != null);
        WebElement resetButton = visibleFilterButton("Đặt lại");
        if (resetButton != null) {
            clickCandidate(resetButton);
        }
        dismissTransientOverlays();
    }

    /**
     * Trả về visible top reset button từ trạng thái hiện tại.
     * @return kết quả visible top reset button sau khi xử lý
     */
    private WebElement visibleTopResetButton() {
        Object button = js().executeScript(
                "const buttons=[...document.querySelectorAll('button')].filter(button=>{"
                        + " const r=button.getBoundingClientRect(),s=getComputedStyle(button);"
                        + " const text=(button.innerText||button.textContent||'').trim().toLocaleLowerCase();"
                        + " const title=(button.getAttribute('title')||button.getAttribute('aria-label')||'')"
                        + "  .trim().toLocaleLowerCase();"
                        + " return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden'"
                        + "  && r.x>300 && r.top>180 && r.top<420"
                        + "  && !text.includes('xuất excel') && !text.includes('xuat excel')"
                        + "  && !title.includes('filter') && !text.includes('bộ lọc');"
                        + "});"
                        + "const explicit=buttons.find(button=>{"
                        + " const title=(button.getAttribute('title')||button.getAttribute('aria-label')||'')"
                        + "  .trim().toLocaleLowerCase();"
                        + " return title.includes('reset')||title.includes('refresh')||title.includes('reload')"
                        + "  || title.includes('đặt lại')||title.includes('dat lai');"
                        + "});"
                        + "if (explicit) return explicit;"
                        + "const iconButtons=buttons.filter(button=>button.querySelector('svg')"
                        + " && (button.innerText||button.textContent||'').trim()==='');"
                        + "return iconButtons.length ? iconButtons[iconButtons.length-1] : null;");
        return button instanceof WebElement element ? element : null;
    }

    /**
     * Chờ for list to settle trong luồng kiểm thử.
     */
    private void waitForListToSettle() {
        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> hasExpectedTableHeaders());
    }

    /**
     * Trả về visible main button từ trạng thái hiện tại.
     * @param label giá trị label được truyền vào
     * @return kết quả visible main button sau khi xử lý
     */
    private WebElement visibleMainButton(String label) {
        return driver.findElements(By.xpath("//button[normalize-space(.)='" + label + "'"
                        + " or .//*[normalize-space()='" + label + "']]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() > 300)
                .findFirst()
                .orElse(null);
    }

    /**
     * Trả về current calendar month từ trạng thái hiện tại.
     * @return kết quả current calendar month sau khi xử lý
     */
    private String currentCalendarMonth() {
        return wait.until(webDriver -> visibleElement(CURRENT_MONTH)).getText().trim();
    }

    /**
     * Trả về visible row count từ trạng thái hiện tại.
     * @return kết quả visible row count sau khi xử lý
     */
    private int visibleRowCount() {
        return visibleRows().size();
    }

    /**
     * Nhấp nút điều hướng phân trang và chờ trang đích cùng dữ liệu mới xuất hiện.
     * @param buttonLocator locator của nút Previous hoặc Next
     * @param expectedPage số trang phải được chọn sau thao tác
     */
    private void clickPaginationButtonAndWait(By buttonLocator, int expectedPage) {
        hasVisiblePagination();
        String previousFirstRow = firstWorkerRowText();
        WebElement button = wait.until(webDriver -> visibleElement(buttonLocator));
        if (paginationButtonIsDisabled(button)) {
            throw new IllegalStateException("Pagination button is disabled before opening page " + expectedPage);
        }
        clickCandidate(button);
        wait.until(webDriver -> currentWorkerPageNumber() == expectedPage);
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> hasWorkerRows()
                && !firstWorkerRowText().equals(previousFirstRow));
    }

    /**
     * Đọc trạng thái disabled của nút phân trang được tìm bằng locator.
     * @param buttonLocator locator nút cần kiểm tra
     * @return {@code true} khi nút đang bị khóa
     */
    private boolean paginationButtonIsDisabled(By buttonLocator) {
        WebElement button = wait.until(webDriver -> visibleElement(buttonLocator));
        return paginationButtonIsDisabled(button);
    }

    /**
     * Đọc trạng thái disabled trực tiếp từ element phân trang.
     * @param button element nút Previous hoặc Next
     * @return {@code true} khi thuộc tính accessibility đánh dấu nút bị khóa
     */
    private boolean paginationButtonIsDisabled(WebElement button) {
        return "true".equalsIgnoreCase(button.getAttribute("aria-disabled"))
                || "true".equalsIgnoreCase(button.getAttribute("data-disabled"));
    }

    /**
     * Tách số trang từ aria-label có dạng {@code pagination item N}.
     * @param pageItem element đại diện cho một trang
     * @return số trang đọc được
     */
    private int paginationNumber(WebElement pageItem) {
        String label = pageItem.getAttribute("aria-label");
        String digits = label == null ? "" : label.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            throw new IllegalStateException("Cannot read page number from aria-label: " + label);
        }
        return Integer.parseInt(digits);
    }

    /**
     * Trả về visible rows từ trạng thái hiện tại.
     * @return kết quả visible rows sau khi xử lý
     */
    @SuppressWarnings("unchecked")
    private List<WebElement> visibleRows() {
        Object rows = js().executeScript(
                "const root=document.querySelector('main,[role=main]')||document.body;"
                        + "return [...root.querySelectorAll('tbody tr,[role=row],.ant-table-row')]"
                        + ".filter(e=>{"
                        + " const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + " const text=(e.innerText||e.textContent||'').trim().toLocaleLowerCase();"
                        + " return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden'"
                        + "  &&text.length>0"
                        + "  &&!text.includes('thông tin thợ')"
                        + "  &&!text.includes('trạng thái')"
                        + "  &&!text.includes('không có dữ liệu')"
                        + "  &&!text.includes('no data');"
                        + "});");
        if (rows instanceof List<?> elements) {
            return ((List<WebElement>) elements).stream()
                    .filter(this::workerRowHasNumericId)
                    .toList();
        }
        return List.of();
    }

    /**
     * Tìm bảng nằm ngay sau tiêu đề Lịch sử giao dịch trong drawer hồ sơ thợ.
     * @return bảng giao dịch đang hiển thị hoặc {@code null}
     */
    private WebElement transactionHistoryTable() {
        WebElement heading = visibleElement(TRANSACTION_HISTORY_HEADING);
        if (heading == null) {
            return null;
        }
        return heading.findElements(By.xpath("following::table[1]")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    /**
     * Lấy các dòng dữ liệu đang hiển thị trong bảng Lịch sử giao dịch.
     * @return danh sách dòng, không gồm header
     */
    private List<WebElement> transactionHistoryRows() {
        WebElement table = wait.until(webDriver -> transactionHistoryTable());
        return table.findElements(By.cssSelector("tbody tr")).stream()
                .filter(WebElement::isDisplayed)
                .filter(row -> !row.getText().isBlank())
                .toList();
    }

    /**
     * Tìm bảng của tab Đơn dịch vụ bằng aria-label ổn định từ DOM.
     * @return bảng đang hiển thị hoặc {@code null}
     */
    private WebElement workerServiceOrderTable() {
        return driver.findElements(By.cssSelector("table[aria-label='Table about order tab']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    /**
     * Lấy các dòng đơn có {@code data-key} là mã đơn dịch vụ.
     * @return danh sách dòng đơn đang hiển thị
     */
    private List<WebElement> workerServiceOrderRows() {
        WebElement table = wait.until(webDriver -> workerServiceOrderTable());
        return table.findElements(By.cssSelector("tbody tr[data-key]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(row -> !row.getText().isBlank())
                .toList();
    }

    /**
     * Tìm tiêu đề Đã giới thiệu đang hiển thị trong tab Giới thiệu.
     * @return heading hoặc {@code null}
     */
    private WebElement referredWorkerHeading() {
        return driver.findElements(By.xpath("//h6[normalize-space()='Đã giới thiệu']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    /**
     * Lấy các link hồ sơ nằm trong grid ngay sau tiêu đề Đã giới thiệu.
     * @return danh sách link có target mở tab mới và href chứa ID thợ
     */
    private List<WebElement> referredWorkerLinks() {
        WebElement heading = referredWorkerHeading();
        if (heading == null) {
            return List.of();
        }
        return heading.findElements(By.xpath(
                        "parent::div/following-sibling::div[1]"
                                + "//a[@target='_blank' and starts-with(@href, '/vuatho/user?id=')]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    /**
     * Lấy các card bài đăng thuộc đúng tabpanel Bài đăng đang hiển thị.
     * @return danh sách card theo thứ tự trên giao diện
     */
    private List<WebElement> workerPostCards() {
        return driver.findElements(By.xpath(
                        "//button[.//img[@alt='profile-post'] or .//video]"))
                .stream()
                .filter(button -> button.getRect().getWidth() > 0
                        && button.getRect().getHeight() > 0)
                .map(button -> button.findElement(By.xpath(
                        "ancestor::div[contains(concat(' ', normalize-space(@class), ' '), ' p-5 ')"
                                + " and contains(concat(' ', normalize-space(@class), ' '), ' bg-white ')][1]")))
                .distinct()
                .toList();
    }

    /**
     * Tìm modal NextUI được mở khi click media của bài đăng.
     * @return dialog đang hiển thị hoặc {@code null}
     */
    private WebElement workerPostDetailDialog() {
        return driver.findElements(By.cssSelector(
                        "section[role='dialog'][aria-modal='true'][data-open='true']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    /** Tìm popup Thiết lập xử phạt đang hiển thị. */
    private WebElement workerPenaltyDialog() {
        return driver.findElements(By.cssSelector("[role='dialog'], section[aria-modal='true']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(dialog -> TextNormalizer.normalize(dialog.getText()).contains("thiet lap xu phat"))
                .findFirst()
                .orElse(null);
    }

    /** Đọc nội dung phần Lịch sử vi phạm để phát hiện thay đổi ngoài ý muốn. */
    private String workerViolationHistoryText() {
        WebElement heading = visibleElement(By.xpath("//*[normalize-space()='Lịch sử vi phạm']"));
        if (heading == null) {
            return "";
        }
        WebElement section = heading.findElements(By.xpath("ancestor::div[.//button[normalize-space()='Xử phạt']][1]"))
                .stream()
                .findFirst()
                .orElse(heading);
        return section.getText().trim();
    }

    /** Mở lại popup và đóng bằng nút X trên header. */
    private boolean openAndClosePenaltyDialogWithTopButton() {
        clickCandidate(wait.until(webDriver -> visibleElement(By.xpath("//button[normalize-space()='Xử phạt']"))));
        WebElement dialog = wait.until(webDriver -> workerPenaltyDialog());
        WebElement closeButton = dialog.findElement(By.xpath(
                ".//h5[normalize-space()='Thiết lập xử phạt']/parent::div/parent::div/button"));
        clickCandidate(closeButton);
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(webDriver -> workerPenaltyDialog() == null);
        return hasWorkerViolationHistorySection();
    }

    /** Tìm input/textarea của popup theo placeholder ổn định. */
    private WebElement penaltyField(WebElement dialog, String placeholder) {
        String expected = TextNormalizer.normalize(placeholder);
        return dialog.findElements(By.cssSelector("input[placeholder], textarea[placeholder]"))
                .stream()
                .filter(field -> TextNormalizer.normalize(field.getAttribute("placeholder")).contains(expected))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy field xử phạt có placeholder: " + placeholder));
    }

    /** Xóa dữ liệu cũ và nhập giá trị mẫu vào field xử phạt. */
    private void enterPenaltyValue(WebElement field, String value) {
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        field.sendKeys(value);
    }

    /** Click input thật; nếu input được custom ẩn thì click label cha. */
    private void clickFormControl(WebElement input) {
        try {
            clickCandidate(input);
        } catch (WebDriverException exception) {
            WebElement label = input.findElements(By.xpath("ancestor::label[1]"))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> exception);
            clickCandidate(label);
        }
    }

    /** Trả về ảnh chính đang hiển thị trong viewer, không lấy ảnh preload ẩn. */
    private WebElement workerPostViewerImage() {
        WebElement dialog = workerPostDetailDialog();
        if (dialog == null) {
            return null;
        }
        return dialog.findElements(By.cssSelector("img[alt='profile-post'][draggable='false']"))
                .stream()
                .findFirst()
                .orElse(null);
    }

    /** Đọc trực tiếp inline transform do các nút zoom/xoay cập nhật. */
    private String workerPostViewerTransform() {
        WebElement image = workerPostViewerImage();
        if (image == null) {
            return "";
        }
        return String.valueOf(js().executeScript("return arguments[0].style.transform || '';", image));
    }

    /** Đọc bộ đếm ảnh dạng 1/2 trong modal. */
    private String workerPostMediaCounter(WebElement dialog) {
        return dialog.findElements(By.tagName("span"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(element -> element.getText().trim())
                .filter(text -> text.matches("\\d+/\\d+"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy bộ đếm media trong modal."));
    }

    /** Đóng riêng modal bài đăng, giữ nguyên drawer hồ sơ thợ phía sau. */
    private void closeWorkerPostDetailDialog() {
        WebElement dialog = workerPostDetailDialog();
        if (dialog == null) {
            return;
        }
        WebElement backdrop = dialog.findElement(By.xpath(
                "parent::div/preceding-sibling::div["
                        + "contains(@class, 'bg-overlay') and contains(@class, 'fixed')][1]"));
        js().executeScript("arguments[0].click();", backdrop);
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> workerPostDetailDialog() == null);
    }

    /**
     * Click link/control của dòng giao dịch; nếu không có control con thì click cả dòng.
     * @param row dòng giao dịch cần mở
     */
    private void clickTransactionRow(WebElement row) {
        WebElement target = row.findElements(By.cssSelector("a[href], button, [role='button']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(row);
        clickCandidate(target);
    }

    /**
     * Tìm pagination hiển thị gần bảng Lịch sử giao dịch nhất theo tọa độ màn hình.
     * Không phụ thuộc pagination là con hay sibling của table trong DOM React.
     * @return wrapper chứa các button số trang hoặc {@code null} nếu không hiển thị
     */
    private WebElement transactionHistoryPaginationWrapper() {
        WebElement table = transactionHistoryTable();
        if (table == null) {
            return null;
        }
        return driver.findElements(By.cssSelector("ul[data-slot='wrapper']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(wrapper -> !wrapper.findElements(By.cssSelector(
                        "li[data-slot='item'][aria-label^='pagination item']")).isEmpty())
                .filter(wrapper -> wrapper.getRect().getY() >= table.getRect().getY())
                .min((left, right) -> Integer.compare(
                        transactionPaginationDistance(left, table),
                        transactionPaginationDistance(right, table)))
                .orElse(null);
    }

    /**
     * Yêu cầu pagination lịch sử giao dịch phải hiển thị trước khi click.
     * @return wrapper pagination đang hiển thị
     */
    private WebElement requireTransactionHistoryPaginationWrapper() {
        WebElement wrapper = wait.until(webDriver -> transactionHistoryPaginationWrapper());
        scrollToCenter(wrapper);
        return wait.until(webDriver -> transactionHistoryPaginationWrapper());
    }

    /**
     * Tính khoảng cách giữa pagination và đáy bảng để loại pagination của danh sách phía sau modal.
     */
    private int transactionPaginationDistance(WebElement wrapper, WebElement table) {
        int tableBottom = table.getRect().getY() + table.getRect().getHeight();
        int verticalDistance = Math.abs(wrapper.getRect().getY() - tableBottom);
        int tableCenter = table.getRect().getX() + table.getRect().getWidth() / 2;
        int wrapperCenter = wrapper.getRect().getX() + wrapper.getRect().getWidth() / 2;
        return verticalDistance + Math.abs(wrapperCenter - tableCenter);
    }

    /**
     * Tạo locator cho button số trang đúng theo DOM pagination đã cung cấp.
     * @param pageNumber số trang cần định vị
     * @return locator của button số trang
     */
    private By transactionHistoryPageButton(int pageNumber) {
        return By.cssSelector(
                "li[data-slot='item'][aria-label='pagination item " + pageNumber + "']");
    }

    /**
     * Thực hiện xử lý worker row has numeric id trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     * @return kết quả worker row has numeric id sau khi xử lý
     */
    private boolean workerRowHasNumericId(WebElement row) {
        return row.findElements(By.cssSelector("td, [role='gridcell']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .map(String::trim)
                .map(text -> text.matches("\\d{4,}"))
                .orElse(false);
    }

    /**
     * Thực hiện xử lý main text trong luồng kiểm thử.
     * @return kết quả main text sau khi xử lý
     */
    private String mainText() {
        PageLoadSynchronizer.waitForDataToSettle(driver);
        return driver.findElements(By.cssSelector("main, [role='main']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .orElseGet(() -> driver.findElement(By.tagName("body")).getText());
    }

    /**
     * Thực hiện xử lý worker detail is loaded trong luồng kiểm thử.
     * @param previousUrl giá trị previous url được truyền vào
     * @param previousState giá trị previous state được truyền vào
     * @return kết quả worker detail is loaded sau khi xử lý
     */
    private boolean workerDetailIsLoaded(String previousUrl, String previousState) {
        return workerDetailShellIsVisible() && workerDetailTextLooksValid(workerDetailText());
    }

    /**
     * Thực hiện xử lý worker detail text looks valid trong luồng kiểm thử.
     * @param text nội dung cần xử lý
     * @return kết quả worker detail text looks valid sau khi xử lý
     */
    private boolean workerDetailTextLooksValid(String text) {
        String normalized = TextNormalizer.normalize(text);
        return normalized.contains("ho va ten")
                || normalized.contains("thong tin ca nhan")
                || normalized.contains("ho so")
                || normalized.contains("so dien thoai")
                || normalized.contains("nganh nghe")
                || normalized.contains("dich vu");
    }

    /**
     * Thực hiện xử lý worker detail shell is visible trong luồng kiểm thử.
     * @return kết quả worker detail shell is visible sau khi xử lý
     */
    private boolean workerDetailShellIsVisible() {
        String normalized = TextNormalizer.normalize(bodyText());
        return normalized.contains("thong tin tho")
                && normalized.contains("tong quan")
                && normalized.contains("nganh nghe");
    }

    /**
     * Kiểm tra điều kiện has no data message.
     * @return kết quả has no data message sau khi xử lý
     */
    private boolean hasNoDataMessage() {
        String normalized = TextNormalizer.normalize(bodyText());
        return normalized.contains("chua co du lieu")
                || normalized.contains("khong co du lieu")
                || normalized.contains("no data");
    }

    /**
     * Trả về visible detail surfaces từ trạng thái hiện tại.
     * @return kết quả visible detail surfaces sau khi xử lý
     */
    private List<WebElement> visibleDetailSurfaces() {
        return driver.findElements(DETAIL_SURFACES).stream()
                .filter(WebElement::isDisplayed)
                .filter(surface -> surface.getRect().getWidth() > 300 && surface.getRect().getHeight() > 200)
                .toList();
    }

    /**
     * Thực hiện xử lý ten criteria rows trong luồng kiểm thử.
     * @return kết quả ten criteria rows sau khi xử lý
     */
    private List<WebElement> tenCriteriaRows() {
        return visibleCriteriaRoots().stream()
                .flatMap(surface -> surface.findElements(By.cssSelector("tbody tr, [role='row']")).stream())
                .filter(WebElement::isDisplayed)
                .filter(row -> row.getRect().getWidth() > 0 && row.getRect().getHeight() > 0)
                .filter(row -> {
                    int index = criteriaIndex(row);
                    return index >= 1 && index <= 10;
                })
                .toList();
    }

    /**
     * Trả về visible criteria roots từ trạng thái hiện tại.
     * @return kết quả visible criteria roots sau khi xử lý
     */
    private List<WebElement> visibleCriteriaRoots() {
        List<WebElement> surfaces = visibleDetailSurfaces();
        if (!surfaces.isEmpty()) {
            return surfaces;
        }
        List<WebElement> mainRoots = driver.findElements(By.cssSelector("main, [role='main']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(root -> root.getRect().getWidth() > 300 && root.getRect().getHeight() > 200)
                .toList();
        if (!mainRoots.isEmpty()) {
            return mainRoots;
        }
        return driver.findElements(By.cssSelector("body")).stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    /**
     * Thực hiện xử lý require criteria row trong luồng kiểm thử.
     * @param index giá trị index được truyền vào
     * @return kết quả require criteria row sau khi xử lý
     */
    private WebElement requireCriteriaRow(int index) {
        return tenCriteriaRows().stream()
                .filter(row -> criteriaIndex(row) == index)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tiêu chí " + index + "."));
    }

    /**
     * Thực hiện xử lý criteria index trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     * @return kết quả criteria index sau khi xử lý
     */
    private int criteriaIndex(WebElement row) {
        return row.findElements(By.cssSelector("td, th, [role='cell'], [role='gridcell']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> text.matches("\\d{1,2}"))
                .map(Integer::parseInt)
                .orElse(-1);
    }

    /**
     * Thực hiện xử lý criteria title trong luồng kiểm thử.
     * @param index giá trị index được truyền vào
     * @return kết quả criteria title sau khi xử lý
     */
    private String criteriaTitle(int index) {
        WebElement row = requireCriteriaRow(index);
        List<WebElement> cells = row.findElements(By.cssSelector("td, [role='cell'], [role='gridcell']")).stream()
                .filter(WebElement::isDisplayed)
                .toList();
        if (cells.size() < 2) {
            return "";
        }
        String[] lines = cells.get(1).getText().split("\\R");
        return lines.length == 0 ? cells.get(1).getText().trim() : lines[0].trim();
    }

    /**
     * Thực hiện xử lý criteria status from text trong luồng kiểm thử.
     * @param text nội dung cần xử lý
     * @return kết quả criteria status from text sau khi xử lý
     */
    private String criteriaStatusFromText(String text) {
        String normalized = TextNormalizer.normalize(text);
        if (normalized.contains("chua dat")) {
            return "Chưa đạt";
        }
        if (normalized.contains("cho xac nhan")) {
            return "Chờ xác nhận";
        }
        if (normalized.contains("dat")) {
            return "Đạt";
        }
        return "";
    }

    /**
     * Thực hiện xử lý criteria update button trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     * @return kết quả criteria update button sau khi xử lý
     */
    private WebElement criteriaUpdateButton(WebElement row) {
        return row.findElements(By.cssSelector("button, [role='button']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(this::isEnabledButton)
                .filter(button -> TextNormalizer.normalize(String.join(" ",
                        button.getText(),
                        button.getAttribute("aria-label"),
                        button.getAttribute("title"))).contains("cap nhat"))
                .findFirst()
                .orElse(null);
    }

    /**
     * Trả về first visible criteria update button từ trạng thái hiện tại.
     * @return kết quả first visible criteria update button sau khi xử lý
     */
    private WebElement firstVisibleCriteriaUpdateButton() {
        return visibleCriteriaUpdateButtonAt(0);
    }

    /**
     * Trả về visible criteria update button at từ trạng thái hiện tại.
     * @param buttonIndex giá trị button index được truyền vào
     * @return kết quả visible criteria update button at sau khi xử lý
     */
    private WebElement visibleCriteriaUpdateButtonAt(int buttonIndex) {
        return wait.until(webDriver -> {
            List<WebElement> buttons = visibleCriteriaUpdateButtons();
            return buttons.size() > buttonIndex ? buttons.get(buttonIndex) : null;
        });
    }

    /**
     * Thực hiện xử lý criteria row for button trong luồng kiểm thử.
     * @param button giá trị button được truyền vào
     * @return kết quả criteria row for button sau khi xử lý
     */
    private WebElement criteriaRowForButton(WebElement button) {
        Object row = js().executeScript(
                "const button=arguments[0];"
                        + "const normalize=text => (text||'').toLocaleLowerCase();"
                        + "let node=button.closest('tr,[role=\"row\"]');"
                        + "if (node) return node;"
                        + "node=button.parentElement;"
                        + "for (let depth=0; node && depth<10; depth++, node=node.parentElement) {"
                        + " const text=normalize(node.innerText || node.textContent || '');"
                        + " if (text.includes('cập nhật') && (text.includes('đạt') || text.includes('chưa đạt'))) {"
                        + "  return node;"
                        + " }"
                        + "}"
                        + "return button.parentElement;",
                button);
        if (row instanceof WebElement element) {
            return element;
        }
        throw new IllegalStateException("Không tìm thấy dòng chứa nút Cập nhật.");
    }

    /**
     * Trả về visible criteria update buttons từ trạng thái hiện tại.
     * @return kết quả visible criteria update buttons sau khi xử lý
     */
    private List<WebElement> visibleCriteriaUpdateButtons() {
        return visibleCriteriaRoots().stream()
                .flatMap(root -> root.findElements(By.cssSelector("button, [role='button']")).stream())
                .filter(WebElement::isDisplayed)
                .filter(this::isEnabledButton)
                .filter(button -> TextNormalizer.normalize(String.join(" ",
                        button.getText(),
                        button.getAttribute("aria-label"),
                        button.getAttribute("title"))).contains("cap nhat"))
                .toList();
    }

    /**
     * Thực hiện xử lý criteria status dialog trong luồng kiểm thử.
     * @return kết quả criteria status dialog sau khi xử lý
     */
    private WebElement criteriaStatusDialog() {
        return driver.findElements(By.cssSelector("[role='dialog'], [class*='modal']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(dialog -> TextNormalizer.normalize(dialog.getText()).contains("cap nhat trang thai"))
                .findFirst()
                .orElse(null);
    }

    /**
     * Thực hiện xử lý require criteria status dialog trong luồng kiểm thử.
     * @return kết quả require criteria status dialog sau khi xử lý
     */
    private WebElement requireCriteriaStatusDialog() {
        WebElement dialog = criteriaStatusDialog();
        if (dialog == null) {
            throw new IllegalStateException("Không tìm thấy popup Cập nhật trạng thái.");
        }
        return dialog;
    }

    /**
     * Kích hoạt criteria dialog button trong luồng kiểm thử.
     * @param label giá trị label được truyền vào
     */
    private void clickCriteriaDialogButton(String label) {
        String expected = TextNormalizer.normalize(label);
        WebElement button = wait.until(webDriver -> {
            WebElement dialog = requireCriteriaStatusDialog();
            return visibleChildren(dialog, By.cssSelector("button"))
                    .stream()
                    .filter(this::isEnabledButton)
                    .filter(element -> TextNormalizer.normalize(String.join(" ",
                            element.getText(),
                            element.getAttribute("aria-label"),
                            element.getAttribute("title"))).contains(expected))
                    .findFirst()
                    .orElse(null);
        });
        clickCandidate(button);
    }

    /**
     * Kiểm tra điều kiện is enabled button.
     * @param button giá trị button được truyền vào
     * @return kết quả is enabled button sau khi xử lý
     */
    private boolean isEnabledButton(WebElement button) {
        return button.isEnabled()
                && !"true".equalsIgnoreCase(button.getAttribute("disabled"))
                && !"true".equalsIgnoreCase(button.getAttribute("aria-disabled"))
                && !"true".equalsIgnoreCase(button.getAttribute("data-disabled"));
    }

    /**
     * Trả về visible children từ trạng thái hiện tại.
     * @param container giá trị container được truyền vào
     * @param locator locator xác định phần tử
     * @return kết quả visible children sau khi xử lý
     */
    private List<WebElement> visibleChildren(WebElement container, By locator) {
        return container.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0)
                .toList();
    }

    /**
     * Trả về visible worker detail tab từ trạng thái hiện tại.
     * @param tabLabel giá trị tab label được truyền vào
     * @return kết quả visible worker detail tab sau khi xử lý
     */
    private WebElement visibleWorkerDetailTab(String tabLabel) {
        String expected = TextNormalizer.normalize(tabLabel);
        return driver.findElements(By.cssSelector("[role='tab']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(tab -> TextNormalizer.normalize(tab.getText()).equals(expected))
                .findFirst()
                .orElse(null);
    }

    /**
     * Thực hiện xử lý worker detail tab has loaded trong luồng kiểm thử.
     * @param tabLabel giá trị tab label được truyền vào
     * @return kết quả worker detail tab has loaded sau khi xử lý
     */
    private boolean workerDetailTabHasLoaded(String tabLabel) {
        if (!workerDetailTabIsSelected(tabLabel) || !noLoadingIndicatorIsVisible()) {
            return false;
        }
        if (TextNormalizer.normalize(tabLabel).equals("bai dang")) {
            return TextNormalizer.normalize(bodyText()).contains("tong so bai dang");
        }
        return !workerDetailText().isBlank();
    }

    /**
     * Thực hiện xử lý worker detail state trong luồng kiểm thử.
     * @return kết quả worker detail state sau khi xử lý
     */
    private String workerDetailState() {
        Object state = js().executeScript(
                "const selected=document.querySelector('[role=\"tab\"][aria-selected=\"true\"],"
                        + "[role=\"tab\"][data-selected=\"true\"]');"
                        + "const panel=document.querySelector('[role=\"tabpanel\"]')"
                        + " || document.querySelector('[role=\"dialog\"]')"
                        + " || document.querySelector('main,[role=main]')"
                        + " || document.body;"
                        + "const text=(panel.innerText||panel.textContent||'').trim();"
                        + "return location.href + '|' + (selected ? selected.textContent.trim() : '')"
                        + " + '|' + text.length + '|'"
                        + " + Array.from(text).reduce((hash,ch)=>((hash*31)+ch.charCodeAt(0))|0,0);");
        return String.valueOf(state);
    }

    /**
     * Trả về visible worker detail close button từ trạng thái hiện tại.
     * @return kết quả visible worker detail close button sau khi xử lý
     */
    private WebElement visibleWorkerDetailCloseButton() {
        Object button = js().executeScript(
                "return [...document.querySelectorAll('button,[role=\"button\"]')].find(button=>{"
                        + " const r=button.getBoundingClientRect(),s=getComputedStyle(button);"
                        + " const text=(button.innerText||button.textContent||button.getAttribute('aria-label')"
                        + "  ||button.getAttribute('title')||'').trim();"
                        + " return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden'"
                        + "  && r.top>=0 && r.top<120 && r.left>window.innerWidth-180"
                        + "  && (text==='×'||text==='x'||text===''||text.toLocaleLowerCase().includes('close'));"
                        + "})||null;");
        return button instanceof WebElement element ? element : null;
    }

    /**
     * Thực hiện xử lý body text trong luồng kiểm thử.
     * @return kết quả body text sau khi xử lý
     */
    private String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    /**
     * Thực hiện xử lý normalize search term trong luồng kiểm thử.
     * @param value giá trị đầu vào
     * @return kết quả normalize search term sau khi xử lý
     */
    private String normalizeSearchTerm(String value) {
        return TextNormalizer.normalize(value).replaceAll("[^a-z0-9]", "");
    }

    /**
     * Trả về visible element từ trạng thái hiện tại.
     * @param locator locator xác định phần tử
     * @return kết quả visible element sau khi xử lý
     */
    private WebElement visibleElement(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    /**
     * Thực hiện xử lý latest past calendar day trong luồng kiểm thử.
     * @return kết quả latest past calendar day sau khi xử lý
     */
    private WebElement latestPastCalendarDay() {
        List<WebElement> days = pastCalendarDays();
        return days.isEmpty() ? null : days.get(days.size() - 1);
    }

    /**
     * Trả về visible calendar day by number từ trạng thái hiện tại.
     * @param dayNumber giá trị day number được truyền vào
     * @return kết quả visible calendar day by number sau khi xử lý
     */
    private WebElement visibleCalendarDayByNumber(int dayNumber) {
        return driver.findElements(By.cssSelector(".react-datepicker__day:not(.react-datepicker__day--outside-month)"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(day -> day.getRect().getX() > 300)
                .filter(day -> !String.valueOf(day.getAttribute("class")).contains("disabled"))
                .filter(day -> calendarDayNumber(day) == dayNumber)
                .findFirst()
                .orElse(null);
    }

    /**
     * Trả về first visible worker created day từ trạng thái hiện tại.
     * @return kết quả first visible worker created day sau khi xử lý
     */
    private int firstVisibleWorkerCreatedDay() {
        String text = firstWorkerRowText();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\b(\\d{2})-\\d{2}-\\d{4}\\b")
                .matcher(text);
        if (!matcher.find()) {
            return latestPastCalendarDay() == null ? todayDayNumber() : calendarDayNumber(latestPastCalendarDay());
        }
        return Integer.parseInt(matcher.group(1));
    }

    /**
     * Thực hiện xử lý past calendar days trong luồng kiểm thử.
     * @return kết quả past calendar days sau khi xử lý
     */
    private List<WebElement> pastCalendarDays() {
        return driver.findElements(PAST_DAY_IN_CALENDAR).stream()
                .filter(WebElement::isDisplayed)
                .filter(day -> day.getRect().getX() > 300)
                .filter(day -> !String.valueOf(day.getAttribute("class")).contains("outside-month"))
                .filter(day -> calendarDayNumber(day) < todayDayNumber())
                .toList();
    }

    /**
     * Trả về first future calendar day từ trạng thái hiện tại.
     * @return kết quả first future calendar day sau khi xử lý
     */
    private WebElement firstFutureCalendarDay() {
        return driver.findElements(FUTURE_DAY_IN_CALENDAR).stream()
                .filter(WebElement::isDisplayed)
                .filter(day -> day.getRect().getX() > 300)
                .filter(day -> !String.valueOf(day.getAttribute("class")).contains("outside-month"))
                .filter(day -> calendarDayNumber(day) > todayDayNumber())
                .findFirst()
                .orElse(null);
    }

    /**
     * Kích hoạt calendar day count trong luồng kiểm thử.
     * @return kết quả selected calendar day count sau khi xử lý
     */
    private int selectedCalendarDayCount() {
        return driver.findElements(SELECTED_DAY_IN_CALENDAR).stream()
                .filter(WebElement::isDisplayed)
                .toList()
                .size();
    }

    /**
     * Thực hiện xử lý today day number trong luồng kiểm thử.
     * @return kết quả today day number sau khi xử lý
     */
    private int todayDayNumber() {
        WebElement today = visibleElement(TODAY_IN_CALENDAR);
        return today == null ? 0 : calendarDayNumber(today);
    }

    /**
     * Thực hiện xử lý calendar day number trong luồng kiểm thử.
     * @param day giá trị day được truyền vào
     * @return kết quả calendar day number sau khi xử lý
     */
    private int calendarDayNumber(WebElement day) {
        try {
            return Integer.parseInt(day.getText().trim());
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    /**
     * Chờ for date filter result trong luồng kiểm thử.
     * @param stateBefore giá trị state before được truyền vào
     */
    private void waitForDateFilterResult(String stateBefore) {
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> hasExpectedTableHeaders());
        wait.until(webDriver -> hasWorkerRows()
                || !PageLoadSynchronizer.mainContentState(driver).equals(stateBefore));
    }

    /**
     * Thực hiện xử lý document is ready trong luồng kiểm thử.
     * @return kết quả document is ready sau khi xử lý
     */
    private boolean documentIsReady() {
        Object state = js().executeScript("return document.readyState");
        return "complete".equals(state);
    }

    /**
     * Thực hiện xử lý no loading indicator is visible trong luồng kiểm thử.
     * @return kết quả no loading indicator is visible sau khi xử lý
     */
    private boolean noLoadingIndicatorIsVisible() {
        return driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed);
    }

    /**
     * Cuộn to center trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     */
    private void scrollToCenter(WebElement element) {
        js().executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", element);
    }

    /**
     * Kích hoạt candidate trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     */
    private void clickCandidate(WebElement element) {
        scrollToCenter(element);
        try {
            element.click();
        } catch (ElementClickInterceptedException exception) {
            js().executeScript("arguments[0].click();", element);
        } catch (WebDriverException exception) {
            js().executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Thực hiện xử lý double click candidate trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     */
    private void doubleClickCandidate(WebElement element) {
        scrollToCenter(element);
        js().executeScript(
                "arguments[0].dispatchEvent(new MouseEvent('dblclick',"
                        + "{bubbles:true,cancelable:true,view:window}));",
                element);
    }

    /**
     * Kích hoạt row by javascript trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     */
    private void clickRowByJavascript(WebElement row) {
        js().executeScript(
                "const row=arguments[0];"
                        + "const candidates=[...row.querySelectorAll('td,[role=gridcell],a,button,[role=button],span,div,p'),row];"
                        + "for (const element of candidates) {"
                        + " const r=element.getBoundingClientRect();"
                        + " if (r.width<=0||r.height<=0) continue;"
                        + " element.dispatchEvent(new MouseEvent('mousedown',{bubbles:true,cancelable:true,view:window}));"
                        + " element.dispatchEvent(new MouseEvent('mouseup',{bubbles:true,cancelable:true,view:window}));"
                        + " element.dispatchEvent(new MouseEvent('click',{bubbles:true,cancelable:true,view:window}));"
                        + "}",
                row);
    }

    /**
     * Chờ for worker detail to load trong luồng kiểm thử.
     * @param previousUrl giá trị previous url được truyền vào
     * @param previousState giá trị previous state được truyền vào
     * @param timeout thời gian chờ tối đa
     * @return kết quả wait for worker detail to load sau khi xử lý
     */
    private boolean waitForWorkerDetailToLoad(String previousUrl, String previousState, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .pollingEvery(Duration.ofMillis(200))
                    .ignoring(StaleElementReferenceException.class)
                    .until(webDriver -> workerDetailIsLoaded(previousUrl, previousState));
            return true;
        } catch (TimeoutException exception) {
            return false;
        }
    }

    /**
     * Thực hiện xử lý dismiss transient overlays trong luồng kiểm thử.
     */
    private void dismissTransientOverlays() {
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        } catch (WebDriverException ignored) {
            // The page may still be transitioning; callers continue with explicit waits.
        }
    }

    /**
     * Thực hiện xử lý js trong luồng kiểm thử.
     * @return kết quả js sau khi xử lý
     */
    private JavascriptExecutor js() {
        return (JavascriptExecutor) driver;
    }
}
