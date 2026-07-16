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
import java.util.List;

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

    public WorkerProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.wait.pollingEvery(Duration.ofMillis(200));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

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

    public WorkerProfilePage waitUntilLoaded() {
        waitUntilPageShellLoaded();
        wait.until(webDriver -> hasWorkerRows());
        return this;
    }

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

    private WorkerProfilePage waitUntilPageShellLoaded() {
        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> driver.getCurrentUrl().contains(WORKER_PROFILE_ROUTE));
        wait.until(webDriver -> searchInput().isDisplayed());
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> hasExpectedTableHeaders());
        return this;
    }

    private void navigateToWorkerRoute() {
        js().executeScript("window.location.href = window.location.origin + arguments[0];", WORKER_PROFILE_ROUTE);
        wait.until(webDriver -> documentIsReady());
    }

    public MenuTarget menuTarget() {
        return PartnerWorkerTestData.WORKER_PROFILE;
    }

    public boolean hasSearchInput() {
        return searchInput().isDisplayed();
    }

    public boolean hasWorkerRows() {
        return visibleRowCount() > 0;
    }

    public void waitUntilAtLeastWorkerRowsVisible(int minimumRows) {
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        wait.until(webDriver -> visibleRows().size() >= minimumRows);
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> visibleRows().size() >= minimumRows);
    }

    public boolean hasKpiSummary() {
        String text = mainText().toLowerCase();
        return KPI_LABELS.stream()
                .allMatch(label -> text.contains(label.toLowerCase()));
    }

    public boolean hasExpectedTableHeaders() {
        String text = mainText().toLowerCase();
        return TABLE_HEADERS.stream()
                .allMatch(header -> text.contains(header.toLowerCase()));
    }

    public boolean hasSearchModeOptions() {
        List<String> options = new Select(visibleSearchModeSelect()).getOptions().stream()
                .map(WebElement::getText)
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        return options.stream().anyMatch(option -> option.contains("tên"))
                && options.stream().anyMatch(option -> option.contains("sđt") || option.contains("sdt"));
    }

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

    public String selectedSearchMode() {
        return new Select(visibleSearchModeSelect()).getFirstSelectedOption().getText().trim();
    }

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

    public boolean searchInputIsEmpty() {
        return searchInput().getAttribute("value").isBlank();
    }

    public String firstVisibleWorkerName() {
        return firstWorkerInfoCellLines().stream()
                .filter(line -> !line.matches(".*\\d{6,}.*"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không đọc được tên thợ ở dòng đầu tiên."));
    }

    public String firstVisibleWorkerPhoneSearchTerm() {
        return firstWorkerInfoCellLines().stream()
                .filter(line -> line.matches(".*\\d{6,}.*"))
                .filter(line -> line.replaceAll("[^0-9]", "").length() >= 6)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không đọc được SĐT thợ ở dòng đầu tiên."));
    }

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

    public boolean visibleRowsContainSearchTerm(String query) {
        String expected = normalizeSearchTerm(query);
        return visibleRowTexts().stream()
                .map(this::normalizeSearchTerm)
                .anyMatch(row -> row.contains(expected));
    }

    public boolean visibleRowsAllContainSearchTerm(String query) {
        String expected = normalizeSearchTerm(query);
        List<String> rows = visibleRowTexts().stream()
                .map(this::normalizeSearchTerm)
                .toList();
        return !rows.isEmpty() && rows.stream().allMatch(row -> row.contains(expected));
    }

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

    public boolean hasKycStatusFilterOptions() {
        String text = filterPanelText().toLowerCase();
        return KYC_FILTER_LABELS.stream()
                .allMatch(label -> text.contains(label.toLowerCase()));
    }

    public boolean hasDateFilterCalendar() {
        return visibleElement(DATE_PICKER) != null
                && visibleElement(PREVIOUS_MONTH_BUTTON) != null
                && visibleElement(NEXT_MONTH_BUTTON) != null
                && visibleElement(CURRENT_MONTH) != null;
    }

    public void selectKycStatus(String statusLabel) {
        WebElement label = wait.until(webDriver -> visibleKycStatusLabel(statusLabel));
        scrollToCenter(label);
        label.click();
        wait.until(webDriver -> selectedKycStatusLabel().equals(statusLabel));
        PageLoadSynchronizer.waitForDataToSettle(driver);
        waitUntilLoaded();
    }

    public boolean visibleRowsMatchKycStatus(String statusLabel) {
        String expected = TextNormalizer.normalize(statusLabel);
        return visibleRowTexts().stream()
                .map(TextNormalizer::normalize)
                .allMatch(row -> row.contains(expected));
    }

    public String selectedKycStatusLabel() {
        Object value = js().executeScript(
                "const checked=document.querySelector('[role=\"radiogroup\"] input[type=\"radio\"]:checked');"
                        + "if (!checked) return '';"
                        + "const label=checked.closest('label');"
                        + "return label ? (label.innerText || label.textContent || '').trim() : '';");
        return String.valueOf(value).trim();
    }

    public void resetFilter() {
        WebElement button = wait.until(webDriver -> visibleFilterButton("Đặt lại"));
        scrollToCenter(button);
        button.click();
        PageLoadSynchronizer.waitForDataToSettle(driver);
        waitUntilLoaded();
    }

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

    public void selectTodayInDateFilter() {
        String stateBefore = PageLoadSynchronizer.mainContentState(driver);
        WebElement today = wait.until(webDriver -> visibleElement(TODAY_IN_CALENDAR));
        scrollToCenter(today);
        today.click();
        wait.until(webDriver -> visibleElement(SELECTED_DAY_IN_CALENDAR) != null);
        waitForDateFilterResult(stateBefore);
        waitUntilLoaded();
    }

    public boolean hasSelectedDateFilter() {
        return visibleElement(SELECTED_DAY_IN_CALENDAR) != null;
    }

    public void selectSinglePastDateInDateFilter() {
        String stateBefore = PageLoadSynchronizer.mainContentState(driver);
        WebElement day = wait.until(webDriver -> visibleCalendarDayByNumber(firstVisibleWorkerCreatedDay()));
        scrollToCenter(day);
        day.click();
        wait.until(webDriver -> hasSelectedDateFilter());
        waitForDateFilterResult(stateBefore);
    }

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

    public boolean hasMultipleDateSelectionResult() {
        return hasDateFilterResultLoaded();
    }

    public boolean hasDateFilterResultLoaded() {
        return hasExpectedTableHeaders()
                && noLoadingIndicatorIsVisible()
                && (hasWorkerRows() || hasNoDataMessage());
    }

    public boolean futureDateIsUnavailableForFiltering() {
        WebElement futureDay = firstFutureCalendarDay();
        return futureDay == null
                || "true".equalsIgnoreCase(futureDay.getAttribute("aria-disabled"))
                || String.valueOf(futureDay.getAttribute("class")).contains("disabled");
    }

    public boolean latestPastDateIsAvailableForFiltering() {
        WebElement day = latestPastCalendarDay();
        return day != null && calendarDayNumber(day) < todayDayNumber();
    }

    public String firstWorkerRowText() {
        return firstWorkerRow().getText();
    }

    public void openFirstWorkerInformation() {
        waitUntilLoaded();
        dismissTransientOverlays();
        String previousUrl = driver.getCurrentUrl();
        String previousState = PageLoadSynchronizer.mainContentState(driver);
        WebElement row = firstWorkerRow();
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

    public boolean workerDetailIsOpen() {
        return workerDetailOpened && workerDetailTextLooksValid(workerDetailText());
    }

    public void openWorkerDetailTab(String tabLabel) {
        wait.until(webDriver -> workerDetailIsOpen());
        WebElement tab = wait.until(webDriver -> visibleWorkerDetailTab(tabLabel));

        clickCandidate(tab);
        wait.until(webDriver -> workerDetailTabIsSelected(tabLabel));
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> workerDetailTabHasLoaded(tabLabel));
    }

    public void openTenCriteriaTab() {
        openWorkerDetailTab("10 Tiêu chí");
        wait.until(webDriver -> hasTenCriteriaSection());
    }

    public boolean hasTenCriteriaSection() {
        String normalized = TextNormalizer.normalize(workerDetailText());
        return normalized.contains("10 tieu chi")
                && normalized.contains("tieu chi danh gia tho")
                && normalized.contains("trang thai")
                && normalized.contains("thao tac");
    }

    public int tenCriteriaRowCount() {
        return tenCriteriaRows().size();
    }

    public boolean tenCriteriaRowsHaveStatuses() {
        return tenCriteriaRows().stream()
                .allMatch(row -> !criteriaStatusFromText(row.getText()).isBlank());
    }

    public boolean tenCriteriaSummaryCountsAreVisible() {
        String normalized = TextNormalizer.normalize(workerDetailText());
        return normalized.contains("dat")
                && normalized.contains("chua dat")
                && normalized.contains("cho xac nhan");
    }

    public List<Integer> criteriaIndexesWithUpdateButton() {
        return tenCriteriaRows().stream()
                .filter(row -> criteriaUpdateButton(row) != null)
                .map(this::criteriaIndex)
                .toList();
    }

    public boolean hasVisibleCriteriaUpdateButton() {
        return !visibleCriteriaUpdateButtons().isEmpty();
    }

    public int visibleCriteriaUpdateButtonCount() {
        return visibleCriteriaUpdateButtons().size();
    }

    public String criteriaStatus(int index) {
        WebElement row = requireCriteriaRow(index);
        return criteriaStatusFromText(row.getText());
    }

    public String firstVisibleUpdateCriteriaStatus() {
        return visibleUpdateCriteriaStatusAt(0);
    }

    public void clickCriteriaUpdateButton(int index) {
        WebElement row = requireCriteriaRow(index);
        WebElement button = criteriaUpdateButton(row);
        if (button == null) {
            throw new IllegalStateException("Không tìm thấy nút Cập nhật ở tiêu chí " + index + ".");
        }
        clickCandidate(button);
        wait.until(webDriver -> criteriaStatusDialog() != null);
    }

    public void clickFirstVisibleCriteriaUpdateButton() {
        clickVisibleCriteriaUpdateButtonAt(0);
    }

    public String visibleUpdateCriteriaStatusAt(int buttonIndex) {
        WebElement row = criteriaRowForButton(visibleCriteriaUpdateButtonAt(buttonIndex));
        return criteriaStatusFromText(row.getText());
    }

    public void clickVisibleCriteriaUpdateButtonAt(int buttonIndex) {
        WebElement button = visibleCriteriaUpdateButtonAt(buttonIndex);
        clickCandidate(button);
        wait.until(webDriver -> criteriaStatusDialog() != null);
    }

    public void openCriteriaStatusUpdate(int index) {
        clickCriteriaUpdateButton(index);
    }

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

    public void cancelCriteriaStatusUpdate() {
        clickCriteriaDialogButton("Hủy");
        wait.until(webDriver -> criteriaStatusDialog() == null);
    }

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

    public void updateCriteriaStatus(int index, String statusLabel) {
        openCriteriaStatusUpdate(index);
        selectCriteriaStatus(statusLabel);
        confirmCriteriaStatusUpdate();
        wait.until(webDriver -> criteriaStatus(index).equals(statusLabel));
    }

    public void updateFirstVisibleCriteriaStatus(String statusLabel) {
        updateVisibleCriteriaStatusAt(0, statusLabel);
    }

    public void updateVisibleCriteriaStatusAt(int buttonIndex, String statusLabel) {
        clickVisibleCriteriaUpdateButtonAt(buttonIndex);
        selectCriteriaStatus(statusLabel);
        confirmCriteriaStatusUpdate();
        wait.until(webDriver -> visibleUpdateCriteriaStatusAt(buttonIndex).equals(statusLabel));
    }

    public boolean workerDetailTabIsSelected(String tabLabel) {
        WebElement tab = visibleWorkerDetailTab(tabLabel);
        if (tab == null) {
            return false;
        }
        return "true".equalsIgnoreCase(tab.getAttribute("aria-selected"))
                || "true".equalsIgnoreCase(tab.getAttribute("data-selected"));
    }

    public String workerDetailText() {
        String surfaceText = visibleDetailSurfaces().stream()
                .map(WebElement::getText)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .orElse("");
        return surfaceText.isBlank() ? bodyText() : surfaceText;
    }

    public void keepWorkerDetailVisible(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while keeping worker detail visible.", exception);
        }
    }

    public void keepCriteriaStatusDialogVisible(Duration duration) {
        wait.until(webDriver -> criteriaStatusDialog() != null);
        keepWorkerDetailVisible(duration);
    }

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

    public void closeWorkerDetailIfOpen() {
        if (workerDetailOpened || workerDetailShellIsVisible()) {
            closeWorkerDetail();
        }
    }

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

    private String filterPanelText() {
        Object value = js().executeScript(
                "const group=document.querySelector('[role=\"radiogroup\"]');"
                        + "const panel=group ? group.closest('.px-1,.space-y-3,[data-slot=\"content\"]') : null;"
                        + "return panel ? (panel.innerText || panel.textContent || '').trim() : '';");
        return String.valueOf(value);
    }

    private WebElement visibleKycStatusLabel(String statusLabel) {
        return driver.findElements(By.cssSelector("[role='radiogroup'] label")).stream()
                .filter(WebElement::isDisplayed)
                .filter(label -> label.getText().trim().equals(statusLabel))
                .findFirst()
                .orElse(null);
    }

    private List<String> visibleRowTexts() {
        return visibleRows().stream()
                .map(WebElement::getText)
                .filter(text -> text != null && !text.isBlank())
                .toList();
    }

    private WebElement firstWorkerRow() {
        wait.until(webDriver -> !visibleRows().isEmpty());
        return visibleRows().get(0);
    }

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

    private WebElement visibleFilterButton(String label) {
        return driver.findElements(By.xpath("//button[normalize-space(.)='" + label + "']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() > 300)
                .findFirst()
                .orElse(null);
    }

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

    private void clickTopResetButtonIfAvailable() {
        WebElement button = visibleTopResetButton();
        if (button == null) {
            return;
        }
        clickCandidate(button);
        waitForListToSettle();
    }

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

    private void waitForListToSettle() {
        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> hasExpectedTableHeaders());
    }

    private WebElement visibleMainButton(String label) {
        return driver.findElements(By.xpath("//button[normalize-space(.)='" + label + "'"
                        + " or .//*[normalize-space()='" + label + "']]"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() > 300)
                .findFirst()
                .orElse(null);
    }

    private String currentCalendarMonth() {
        return wait.until(webDriver -> visibleElement(CURRENT_MONTH)).getText().trim();
    }

    private int visibleRowCount() {
        return visibleRows().size();
    }

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

    private boolean workerRowHasNumericId(WebElement row) {
        return row.findElements(By.cssSelector("td, [role='gridcell']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .map(String::trim)
                .map(text -> text.matches("\\d{4,}"))
                .orElse(false);
    }

    private String mainText() {
        PageLoadSynchronizer.waitForDataToSettle(driver);
        return driver.findElements(By.cssSelector("main, [role='main']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .orElseGet(() -> driver.findElement(By.tagName("body")).getText());
    }

    private boolean workerDetailIsLoaded(String previousUrl, String previousState) {
        boolean stateChanged = (!previousUrl.isBlank() && !driver.getCurrentUrl().equals(previousUrl))
                || (!previousState.isBlank()
                && !PageLoadSynchronizer.mainContentState(driver).equals(previousState));
        return (stateChanged || workerDetailShellIsVisible()) && workerDetailTextLooksValid(workerDetailText());
    }

    private boolean workerDetailTextLooksValid(String text) {
        String normalized = TextNormalizer.normalize(text);
        return normalized.contains("ho va ten")
                || normalized.contains("thong tin ca nhan")
                || normalized.contains("ho so")
                || normalized.contains("so dien thoai")
                || normalized.contains("nganh nghe")
                || normalized.contains("dich vu");
    }

    private boolean workerDetailShellIsVisible() {
        String normalized = TextNormalizer.normalize(bodyText());
        return normalized.contains("thong tin tho")
                && normalized.contains("tong quan")
                && normalized.contains("nganh nghe");
    }

    private boolean hasNoDataMessage() {
        String normalized = TextNormalizer.normalize(bodyText());
        return normalized.contains("chua co du lieu")
                || normalized.contains("khong co du lieu")
                || normalized.contains("no data");
    }

    private List<WebElement> visibleDetailSurfaces() {
        return driver.findElements(DETAIL_SURFACES).stream()
                .filter(WebElement::isDisplayed)
                .filter(surface -> surface.getRect().getWidth() > 300 && surface.getRect().getHeight() > 200)
                .toList();
    }

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

    private WebElement requireCriteriaRow(int index) {
        return tenCriteriaRows().stream()
                .filter(row -> criteriaIndex(row) == index)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tiêu chí " + index + "."));
    }

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

    private WebElement firstVisibleCriteriaUpdateButton() {
        return visibleCriteriaUpdateButtonAt(0);
    }

    private WebElement visibleCriteriaUpdateButtonAt(int buttonIndex) {
        return wait.until(webDriver -> {
            List<WebElement> buttons = visibleCriteriaUpdateButtons();
            return buttons.size() > buttonIndex ? buttons.get(buttonIndex) : null;
        });
    }

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

    private WebElement criteriaStatusDialog() {
        return driver.findElements(By.cssSelector("[role='dialog'], [class*='modal']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(dialog -> TextNormalizer.normalize(dialog.getText()).contains("cap nhat trang thai"))
                .findFirst()
                .orElse(null);
    }

    private WebElement requireCriteriaStatusDialog() {
        WebElement dialog = criteriaStatusDialog();
        if (dialog == null) {
            throw new IllegalStateException("Không tìm thấy popup Cập nhật trạng thái.");
        }
        return dialog;
    }

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

    private boolean isEnabledButton(WebElement button) {
        return button.isEnabled()
                && !"true".equalsIgnoreCase(button.getAttribute("disabled"))
                && !"true".equalsIgnoreCase(button.getAttribute("aria-disabled"))
                && !"true".equalsIgnoreCase(button.getAttribute("data-disabled"));
    }

    private List<WebElement> visibleChildren(WebElement container, By locator) {
        return container.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0)
                .toList();
    }

    private WebElement visibleWorkerDetailTab(String tabLabel) {
        String expected = TextNormalizer.normalize(tabLabel);
        return driver.findElements(By.cssSelector("[role='tab']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(tab -> TextNormalizer.normalize(tab.getText()).equals(expected))
                .findFirst()
                .orElse(null);
    }

    private boolean workerDetailTabHasLoaded(String tabLabel) {
        return workerDetailTabIsSelected(tabLabel)
                && noLoadingIndicatorIsVisible()
                && !workerDetailText().isBlank();
    }

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

    private WebElement visibleWorkerDetailCloseButton() {
        Object button = js().executeScript(
                "return [...document.querySelectorAll('button,[role=\"button\"]')].find(button=>{"
                        + " const r=button.getBoundingClientRect(),s=getComputedStyle(button);"
                        + " const text=(button.innerText||button.textContent||button.getAttribute('aria-label')"
                        + "  ||button.getAttribute('title')||'').trim();"
                        + " return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden'"
                        + "  && r.top>=0 && r.top<120 && r.left>window.innerWidth-180 && text!==''"
                        + "  && (text==='×'||text==='x'||text===''||text.toLocaleLowerCase().includes('close'));"
                        + "})||null;");
        return button instanceof WebElement element ? element : null;
    }

    private String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    private String normalizeSearchTerm(String value) {
        return TextNormalizer.normalize(value).replaceAll("[^a-z0-9]", "");
    }

    private WebElement visibleElement(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    private WebElement latestPastCalendarDay() {
        List<WebElement> days = pastCalendarDays();
        return days.isEmpty() ? null : days.get(days.size() - 1);
    }

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

    private List<WebElement> pastCalendarDays() {
        return driver.findElements(PAST_DAY_IN_CALENDAR).stream()
                .filter(WebElement::isDisplayed)
                .filter(day -> day.getRect().getX() > 300)
                .filter(day -> !String.valueOf(day.getAttribute("class")).contains("outside-month"))
                .filter(day -> calendarDayNumber(day) < todayDayNumber())
                .toList();
    }

    private WebElement firstFutureCalendarDay() {
        return driver.findElements(FUTURE_DAY_IN_CALENDAR).stream()
                .filter(WebElement::isDisplayed)
                .filter(day -> day.getRect().getX() > 300)
                .filter(day -> !String.valueOf(day.getAttribute("class")).contains("outside-month"))
                .filter(day -> calendarDayNumber(day) > todayDayNumber())
                .findFirst()
                .orElse(null);
    }

    private int selectedCalendarDayCount() {
        return driver.findElements(SELECTED_DAY_IN_CALENDAR).stream()
                .filter(WebElement::isDisplayed)
                .toList()
                .size();
    }

    private int todayDayNumber() {
        WebElement today = visibleElement(TODAY_IN_CALENDAR);
        return today == null ? 0 : calendarDayNumber(today);
    }

    private int calendarDayNumber(WebElement day) {
        try {
            return Integer.parseInt(day.getText().trim());
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private void waitForDateFilterResult(String stateBefore) {
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> hasExpectedTableHeaders());
        wait.until(webDriver -> hasWorkerRows()
                || !PageLoadSynchronizer.mainContentState(driver).equals(stateBefore));
    }

    private boolean documentIsReady() {
        Object state = js().executeScript("return document.readyState");
        return "complete".equals(state);
    }

    private boolean noLoadingIndicatorIsVisible() {
        return driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed);
    }

    private void scrollToCenter(WebElement element) {
        js().executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", element);
    }

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

    private void doubleClickCandidate(WebElement element) {
        scrollToCenter(element);
        js().executeScript(
                "arguments[0].dispatchEvent(new MouseEvent('dblclick',"
                        + "{bubbles:true,cancelable:true,view:window}));",
                element);
    }

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

    private void dismissTransientOverlays() {
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        } catch (WebDriverException ignored) {
            // The page may still be transitioning; callers continue with explicit waits.
        }
    }

    private JavascriptExecutor js() {
        return (JavascriptExecutor) driver;
    }
}
