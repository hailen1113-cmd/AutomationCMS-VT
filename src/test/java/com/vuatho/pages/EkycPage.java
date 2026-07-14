package com.vuatho.pages;

import com.vuatho.config.TestConfig;
import com.vuatho.navigation.MenuTarget;
import com.vuatho.testdata.EkycInformationField;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EkycPage {
    private static final By EKYC_MENU = By.cssSelector("a[href='/vuatho/ekyc']");
    private static final By SEARCH_INPUT = By.cssSelector("input[aria-label='Tìm kiếm người dùng']");
    private static final By FILTER_BUTTON = By.cssSelector("button[title='Filter']");
    private static final By RESET_BUTTON = By.cssSelector("button[title='Reset']");
    private static final By TABLE = By.cssSelector("table[aria-label='Table about eKYC Management']");
    private static final By ROWS = By.cssSelector("table[aria-label='Table about eKYC Management'] tbody tr[role='row']");
    private static final By PAGINATION = By.cssSelector("nav[aria-label='pagination navigation']");
    private static final By PREVIOUS_PAGE = By.cssSelector("[aria-label='previous page button']");
    private static final By NEXT_PAGE = By.cssSelector("[aria-label='next page button']");
    private static final By DRAWER = By.cssSelector("div[aria-label='drawer-Chi tiết eKYC']");
    private static final By LOADING_INDICATORS = By.cssSelector(
            "[role='progressbar'], .ant-spin-spinning, .ant-skeleton, .skeleton");
    private static final List<String> STATISTIC_LABELS = List.of(
            "Tổng yêu cầu", "Đang chờ duyệt", "Đã duyệt", "Đã từ chối");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public enum KycSide {
        FRONT("mat truoc"),
        BACK("mat sau"),
        SELFIE("anh chan dung");

        private final String normalizedLabel;

        KycSide(String normalizedLabel) {
            this.normalizedLabel = normalizedLabel;
        }
    }

    public EkycPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.defaultWaitTimeout());
        this.wait.pollingEvery(Duration.ofMillis(300));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    public EkycPage openFromMenu() {
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(
                MenuTarget.childOf("Người Dùng", "Quản Lí eKYC"),
                false);
        return waitUntilLoaded();
    }

    public EkycPage openDirectly() {
        driver.get(resolveAppUrl("/vuatho/ekyc"));
        return waitUntilLoaded();
    }

    public EkycPage waitUntilLoaded() {
        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> currentRouteContains("/vuatho/ekyc"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(TABLE));
        wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
        wait.until(ExpectedConditions.visibilityOfElementLocated(FILTER_BUTTON));
        wait.until(ExpectedConditions.visibilityOfElementLocated(RESET_BUTTON));
        wait.until(ExpectedConditions.visibilityOfElementLocated(PAGINATION));
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        wait.until(webDriver -> !visibleRows().isEmpty());
        pauseAfterLoad();
        return this;
    }

    public boolean hasRequiredControls() {
        return isDisplayed(SEARCH_INPUT)
                && isDisplayed(FILTER_BUTTON)
                && isDisplayed(RESET_BUTTON)
                && isDisplayed(TABLE)
                && isDisplayed(PAGINATION);
    }

    public boolean menuIsActive() {
        return driver.findElements(EKYC_MENU).stream()
                .filter(WebElement::isDisplayed)
                .map(element -> element.getAttribute("class"))
                .anyMatch(classes -> classes != null
                        && (classes.contains("text-indigo-500") || classes.contains("font-semibold")));
    }

    public boolean hasSystemErrorText() {
        String text = bodyText().toLowerCase(Locale.ROOT);
        return text.contains("404")
                || text.contains("500")
                || text.contains("page not found")
                || text.contains("internal server error")
                || text.contains("undefined")
                || text.contains("nan")
                || text.contains("infinity");
    }

    public List<String> headerTexts() {
        return driver.findElements(By.cssSelector("table[aria-label='Table about eKYC Management'] thead th"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .toList();
    }

    public List<WebElement> visibleRows() {
        return driver.findElements(ROWS).stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    public List<String> rowIds() {
        return visibleRows().stream()
                .map(row -> row.getAttribute("data-key"))
                .filter(id -> id != null && !id.isBlank())
                .toList();
    }

    public boolean hasUniqueRowIds() {
        List<String> ids = rowIds();
        Set<String> unique = new LinkedHashSet<>(ids);
        return ids.size() == unique.size();
    }

    public String firstApplicantId() {
        return rowIds().stream().findFirst().orElse("");
    }

    public String cellText(WebElement row, String suffix) {
        return row.findElements(By.cssSelector("td[data-key$='" + suffix + "']")).stream()
                .findFirst()
                .map(WebElement::getText)
                .map(String::trim)
                .orElse("");
    }

    public List<String> statusTexts() {
        return visibleRows().stream()
                .map(row -> cellText(row, "status"))
                .filter(text -> !text.isBlank())
                .toList();
    }

    public boolean pendingRowsAreBeforeNonPendingRows() {
        boolean sawNonPending = false;
        for (String status : statusTexts()) {
            boolean pending = normalize(status).contains("pending");
            if (!pending) {
                sawNonPending = true;
            }
            if (pending && sawNonPending) {
                return false;
            }
        }
        return true;
    }

    public boolean firstRowDataKeyMatchesIdCell() {
        WebElement row = visibleRows().get(0);
        String dataKey = row.getAttribute("data-key");
        String idCell = digitsOnly(cellText(row, "id"));
        return dataKey != null && !dataKey.isBlank() && dataKey.equals(idCell);
    }

    public boolean firstRowHasExpectedCells() {
        WebElement row = visibleRows().get(0);
        return !cellText(row, "id").isBlank()
                && !cellText(row, "user_info").toLowerCase(Locale.ROOT).contains("undefined")
                && !cellText(row, "type").isBlank()
                && !cellText(row, "status").isBlank()
                && !cellText(row, "created").isBlank();
    }

    public boolean createdDatesHaveExpectedFormat() {
        Pattern pattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4}\\s+\\d{2}:\\d{2}");
        return visibleRows().stream()
                .map(row -> cellText(row, "created"))
                .filter(text -> !text.isBlank())
                .allMatch(text -> pattern.matcher(text).find());
    }

    public EkycPage reset() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(RESET_BUTTON));
        String before = tableFingerprint();
        button.click();
        waitForTableReadyAfter(before);
        return this;
    }

    public EkycPage search(String keyword) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        String before = tableFingerprint();
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(keyword);
        waitForTableReadyAfter(before);
        return this;
    }

    public EkycPage clearSearch() {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        String before = tableFingerprint();
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        waitForTableReadyAfter(before);
        return this;
    }

    public boolean rowsContainNormalized(String keyword) {
        String normalizedKeyword = normalize(keyword);
        return visibleRows().stream()
                .map(WebElement::getText)
                .map(EkycPage::normalize)
                .anyMatch(text -> text.contains(normalizedKeyword));
    }

    public boolean noRowsOrNoDataVisible() {
        return visibleRows().isEmpty()
                || normalize(bodyText()).contains("no data")
                || normalize(bodyText()).contains("khong co du lieu");
    }

    public int activePage() {
        return driver.findElements(By.cssSelector("[aria-label^='pagination item'][aria-current='true'],"
                        + "[aria-label^='pagination item'][data-active='true']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> text.matches("\\d+"))
                .map(Integer::parseInt)
                .orElse(0);
    }

    public boolean previousIsDisabled() {
        return isDisabled(PREVIOUS_PAGE);
    }

    public boolean nextIsDisabled() {
        return isDisabled(NEXT_PAGE);
    }

    public EkycPage goToNextPage() {
        clickAndWaitForTable(NEXT_PAGE);
        return this;
    }

    public EkycPage goToPreviousPage() {
        clickAndWaitForTable(PREVIOUS_PAGE);
        return this;
    }

    public EkycPage goToPage(int page) {
        clickAndWaitForTable(By.cssSelector("[aria-label='pagination item " + page + "'],"
                + "[aria-label='pagination item " + page + " active']"));
        return this;
    }

    public EkycPage openFirstDrawer() {
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        WebElement row = visibleRows().get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", row);
        try {
            row.click();
        } catch (ElementClickInterceptedException exception) {
            wait.until(webDriver -> noLoadingIndicatorIsVisible());
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", row);
        }
        wait.until(webDriver -> drawerIsOpen());
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        pauseAfterLoad();
        return this;
    }

    public EkycPage closeDrawerWithCancel() {
        WebElement drawer = driver.findElement(DRAWER);
        drawer.findElements(By.xpath(".//button[normalize-space()='Hủy']")).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow()
                .click();
        wait.until(webDriver -> !drawerIsOpen());
        pauseAfterLoad();
        return this;
    }

    public boolean drawerIsOpen() {
        return driver.findElements(DRAWER).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(drawer -> !drawer.getAttribute("class").contains("translate-x-[100%]"))
                .orElse(false);
    }

    public boolean drawerHasTitle() {
        return driver.findElements(DRAWER).stream()
                .anyMatch(drawer -> drawer.getText().contains("Chi tiết eKYC"));
    }

    public String drawerText() {
        return driver.findElements(DRAWER).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(WebElement::getText)
                .orElse("");
    }

    public EkycPage markAllKycSidesValid() {
        WebElement drawer = visibleDrawer();
        List<WebElement> validButtons = drawer.findElements(By.xpath(".//button[normalize-space()='Hợp lệ']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
        if (validButtons.size() < 3) {
            throw new IllegalStateException("Expected 3 Hop le buttons but found " + validButtons.size());
        }
        for (WebElement button : validButtons) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", button);
            button.click();
            pauseAfterLoad();
        }
        return this;
    }

    public EkycPage rejectFirstKycSide() {
        WebElement drawer = visibleDrawer();
        WebElement rejectButton = drawer.findElements(By.xpath(".//button[normalize-space()='Từ chối']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Tu choi button is visible in eKYC drawer."));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", rejectButton);
        rejectButton.click();
        pauseAfterLoad();
        return this;
    }

    public EkycPage rejectFirstKycSideAndSelectReason() {
        try {
            rejectFirstKycSide();
        } catch (RuntimeException exception) {
            clickFirstDrawerButtonContaining("Tu choi");
            pauseAfterLoad();
        }
        waitUntilRejectReasonsAreVisible();
        selectFirstRejectReason();
        return this;
    }

    public EkycPage markAvailableKycSidesValid() {
        List<KycSide> sides = availableKycSides();
        if (sides.isEmpty()) {
            throw new IllegalStateException("No KYC side decision controls are visible.");
        }
        for (KycSide side : sides) {
            clickKycSideAction(side, "hop le");
            pauseAfterLoad();
        }
        return this;
    }

    public EkycPage markAvailableKycSidesValidExcept(KycSide rejectedSide) {
        for (KycSide side : availableKycSides()) {
            if (side != rejectedSide) {
                clickKycSideAction(side, "hop le");
                pauseAfterLoad();
            }
        }
        return this;
    }

    public EkycPage rejectKycSideAndSelectReason(KycSide side) {
        clickKycSideAction(side, "tu choi");
        waitUntilRejectReasonsAreVisible();
        selectFirstRejectReason();
        return this;
    }

    public EkycPage rejectKycSideWithoutReason(KycSide side) {
        clickKycSideAction(side, "tu choi");
        waitUntilRejectReasonsAreVisible();
        pauseAfterLoad();
        return this;
    }

    public EkycPage selectFirstRejectReason() {
        WebElement reason = wait.until(webDriver -> firstRejectReasonInput());
        scrollIntoView(reason);
        WebElement target = clickableRejectReasonTarget(reason);
        try {
            target.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
        }
        wait.until(webDriver -> reasonIsSelected(reason));
        pauseAfterLoad();
        return this;
    }

    public boolean hasRejectReasons() {
        return !rejectReasonInputs().isEmpty();
    }

    public EkycPage submitDrawerDecision() {
        scrollToDrawerBottom();
        clickFirstDrawerButtonContaining("Xac nhan");
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        pauseAfterLoad();
        return this;
    }

    public boolean hasDrawerAction(String... labels) {
        String normalizedDrawer = normalize(drawerText());
        for (String label : labels) {
            if (normalizedDrawer.contains(normalize(label))) {
                return true;
            }
        }
        return labelsContainEditAction(labels) && firstEditIconButton() != null
                || labelsContainClearAction(labels) && firstClearActionButton() != null;
    }

    public boolean hasEditInformationAction() {
        return hasKycInformationPreview()
                || hasDrawerAction("Sửa", "Chỉnh sửa", "Cập nhật", "Sua", "Chinh sua", "Cap nhat")
                || firstEditIconButton() != null;
    }

    public EkycPage openEditInformationIfAvailable() {
        WebElement preview = firstKycPreviewTarget();
        if (preview != null) {
            return openKycInformationEditorFromPreview(preview);
        }
        try {
            clickFirstDrawerButtonContaining("Sửa", "Chỉnh sửa", "Cap nhat", "Cập nhật");
        } catch (IllegalStateException exception) {
            WebElement button = firstEditIconButton();
            if (button == null) {
                throw exception;
            }
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", button);
            button.click();
        }
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        waitUntilKycInformationEditorIsOpen();
        pauseAfterLoad();
        return this;
    }

    public EkycPage openClearKycIfAvailable() {
        WebElement preview = firstKycPreviewTarget();
        if (preview != null) {
            return openKycInformationEditorFromPreview(preview);
        }
        try {
            clickFirstDrawerButtonContaining("Xóa", "Xoa", "Clear", "Delete");
        } catch (IllegalStateException exception) {
            WebElement button = firstClearActionButton();
            if (button == null) {
                throw exception;
            }
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", button);
            button.click();
        }
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        waitUntilKycInformationEditorIsOpen();
        pauseAfterLoad();
        return this;
    }

    public boolean hasKycInformationPreview() {
        return firstKycPreviewTarget() != null;
    }

    public EkycPage openKycInformationEditorFromPreview(WebElement preview) {
        scrollIntoView(preview);
        try {
            preview.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", preview);
        }
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        waitUntilKycInformationEditorIsOpen();
        pauseAfterLoad();
        return this;
    }

    public boolean kycInformationEditorIsOpen() {
        String text = normalize(bodyText());
        return text.contains("xac minh thong tin cccd")
                || text.contains("thong tin chi tiet") && !visibleEditableKycInputs().isEmpty();
    }

    public EkycPage editFirstAvailableKycInformationField() {
        chooseBirthDateIfAvailable();
        chooseGenderIfAvailable();
        List<WebElement> inputs = wait.until(webDriver -> {
            List<WebElement> editableInputs = visibleEditableKycInputs();
            return editableInputs.isEmpty() ? null : editableInputs;
        });
        for (WebElement input : inputs) {
            scrollIntoView(input);
            String value = editValueFor(input);
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.DELETE);
            input.sendKeys(value);
            wait.until(webDriver -> value.equals(input.getAttribute("value")));
        }
        pauseAfterLoad();
        return this;
    }

    public EkycPage editKycInformationFields(List<EkycInformationField> fields) {
        for (EkycInformationField field : fields) {
            WebElement control = editorControlByLabel(field.normalizedLabel());
            if (control == null) {
                continue;
            }
            if (field == EkycInformationField.BIRTH_DATE) {
                chooseBirthDateIfAvailable();
            } else if (field == EkycInformationField.GENDER) {
                chooseGenderIfAvailable();
            } else {
                scrollIntoView(control);
                setInputValue(control, field.updateValue());
            }
        }
        pauseAfterLoad();
        return this;
    }

    public EkycPage clearAllKycInformationFields() {
        clearBirthDateIfAvailable();
        clearGenderIfAvailable();
        List<WebElement> inputs = wait.until(webDriver -> {
            List<WebElement> editableInputs = visibleEditableKycInputs();
            return editableInputs.isEmpty() ? null : editableInputs;
        });
        for (WebElement input : inputs) {
            clearControlValue(input);
        }
        pauseAfterLoad();
        return this;
    }

    public EkycPage clearKycInformationFields(List<EkycInformationField> fields) {
        for (EkycInformationField field : fields) {
            WebElement control = editorControlByLabel(field.normalizedLabel());
            if (control != null) {
                clearControlValue(control);
            }
        }
        pauseAfterLoad();
        return this;
    }

    public EkycPage clearKycInformationFieldMatching(String normalizedCaseText) {
        boolean cleared = false;
        if (normalizedCaseText.contains("ngay sinh")
                || normalizedCaseText.contains("birthdate")
                || normalizedCaseText.contains("birth date")) {
            cleared = clearBirthDateIfAvailable();
        }
        if (normalizedCaseText.contains("gioi tinh")
                || normalizedCaseText.contains("gender")) {
            cleared = clearGenderIfAvailable() || cleared;
        }

        for (WebElement input : visibleEditableKycInputs()) {
            if (inputMatchesCaseText(input, normalizedCaseText)) {
                clearControlValue(input);
                cleared = true;
            }
        }

        if (!cleared) {
            WebElement first = wait.until(webDriver -> visibleEditableKycInputs().stream()
                    .findFirst()
                    .orElse(null));
            clearControlValue(first);
        }
        pauseAfterLoad();
        return this;
    }

    public EkycPage saveKycInformationChanges() {
        scrollToEditorBottom();
        clickVisibleButtonContaining("Luu thay doi");
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        pauseAfterLoad();
        return this;
    }

    public EkycPage cancelKycInformationChanges() {
        scrollToEditorBottom();
        clickVisibleButtonContaining("Huy");
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        pauseAfterLoad();
        return this;
    }

    public List<String> statisticLabels() {
        return STATISTIC_LABELS;
    }

    public long statisticAll(String label) {
        WebElement card = statisticCard(label);
        return card.findElements(By.cssSelector("p.text-4xl")).stream()
                .findFirst()
                .map(WebElement::getText)
                .map(EkycPage::parseLong)
                .orElseThrow();
    }

    public long statisticToday(String label) {
        WebElement card = statisticCard(label);
        return card.findElements(By.xpath(".//p[contains(normalize-space(),'mới trong ngày')]/span")).stream()
                .findFirst()
                .map(WebElement::getText)
                .map(EkycPage::parseLong)
                .orElse(0L);
    }

    public double statisticPercent(String label) {
        WebElement card = statisticCard(label);
        return card.findElements(By.xpath(".//*[normalize-space()='Tỷ trọng hệ thống']/following-sibling::*[1]"))
                .stream()
                .findFirst()
                .map(WebElement::getText)
                .map(EkycPage::parsePercent)
                .orElse(Double.NaN);
    }

    public int totalRequestBarCount() {
        return statisticCard("Tổng yêu cầu").findElements(By.cssSelector(".recharts-bar-rectangle")).size();
    }

    public boolean statisticCardsDoNotContainInvalidNumbers() {
        return STATISTIC_LABELS.stream()
                .map(label -> statisticCard(label).getText().toLowerCase(Locale.ROOT))
                .noneMatch(text -> text.contains("nan")
                        || text.contains("infinity")
                        || text.contains("undefined"));
    }

    private WebElement statisticCard(String label) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//p[normalize-space()='" + label + "']/ancestor::div[contains(@class,'min-h-[160px]')][1]")));
    }

    private WebElement visibleDrawer() {
        return driver.findElements(DRAWER).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("eKYC drawer is not visible."));
    }

    private List<KycSide> availableKycSides() {
        return List.of(KycSide.FRONT, KycSide.BACK, KycSide.SELFIE).stream()
                .filter(side -> sideSection(side) != null)
                .toList();
    }

    private WebElement sideSection(KycSide side) {
        return visibleDrawer().findElements(By.cssSelector(".space-y-3, [class*='space-y-3']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(section -> normalize(section.getText()).contains(side.normalizedLabel))
                .filter(section -> section.findElements(By.cssSelector("button")).stream()
                        .anyMatch(button -> normalize(button.getText()).contains("hop le")
                                || normalize(button.getText()).contains("tu choi")))
                .findFirst()
                .orElse(null);
    }

    private void clickKycSideAction(KycSide side, String normalizedAction) {
        WebElement section = sideSection(side);
        if (section == null) {
            throw new IllegalStateException("KYC side is not visible: " + side);
        }
        WebElement button = section.findElements(By.cssSelector("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(candidate -> normalize(candidate.getText()).contains(normalizedAction))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "KYC action not found: " + normalizedAction + " for " + side));
        scrollIntoView(button);
        try {
            button.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        }
    }

    private void clickDrawerButton(String exactText) {
        WebElement button = visibleDrawer().findElements(By.xpath(".//button[normalize-space()='" + exactText + "']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Drawer button not found: " + exactText));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", button);
        button.click();
    }

    private void clickFirstDrawerButtonContaining(String... labels) {
        List<WebElement> buttons = visibleDrawer().findElements(By.cssSelector("button")).stream()
                .filter(WebElement::isDisplayed)
                .toList();
        for (String label : labels) {
            String normalizedLabel = normalize(label);
            for (WebElement button : buttons) {
                if (normalize(button.getText()).contains(normalizedLabel)) {
                    scrollIntoView(button);
                    try {
                        button.click();
                    } catch (ElementClickInterceptedException exception) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                    }
                    return;
                }
            }
        }
        throw new IllegalStateException("No drawer button found for labels: " + String.join(", ", labels));
    }

    private void waitUntilRejectReasonsAreVisible() {
        wait.until(webDriver -> hasRejectReasons()
                || normalize(drawerText()).contains("ly do tu choi"));
    }

    private WebElement firstRejectReasonInput() {
        return rejectReasonInputs().stream()
                .findFirst()
                .orElse(null);
    }

    private List<WebElement> rejectReasonInputs() {
        return visibleDrawer().findElements(By.cssSelector("input[type='radio'], [role='radio']")).stream()
                .filter(element -> !"true".equals(element.getAttribute("disabled"))
                        && !"true".equals(element.getAttribute("aria-disabled")))
                .toList();
    }

    private void waitUntilKycInformationEditorIsOpen() {
        wait.until(webDriver -> kycInformationEditorIsOpen());
    }

    private List<WebElement> visibleEditableKycInputs() {
        return driver.findElements(By.cssSelector("input, textarea")).stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .filter(element -> {
                    String type = String.valueOf(element.getAttribute("type")).toLowerCase(Locale.ROOT);
                    return !type.equals("hidden")
                            && !type.equals("radio")
                            && !type.equals("checkbox")
                            && !type.equals("file");
                })
                .filter(element -> {
                    String text = normalize(String.join(" ",
                            element.getAttribute("placeholder"),
                            element.getAttribute("name"),
                            element.getAttribute("aria-label"),
                            element.getAttribute("value")));
                    return text.contains("ho ten")
                            || text.contains("so giay to")
                            || text.contains("cccd")
                            || text.contains("passport")
                            || text.contains("que quan")
                            || text.contains("thuong tru")
                            || text.contains("quoc tich");
                })
                .toList();
    }

    private void chooseBirthDateIfAvailable() {
        WebElement field = editorControlByLabel("ngay sinh");
        if (field == null) {
            return;
        }
        scrollIntoView(field);
        String before = String.valueOf(field.getAttribute("value"));
        clickElement(field);
        pauseAfterLoad();
        WebElement day = visibleCalendarDay();
        if (day != null) {
            clickElement(day);
            wait.until(webDriver -> {
                String value = String.valueOf(field.getAttribute("value"));
                return !value.isBlank() && !value.equals(before);
            });
        } else {
            setInputValue(field, "14/07/2000");
        }
        pauseAfterLoad();
    }

    private void chooseGenderIfAvailable() {
        WebElement field = editorControlByLabel("gioi tinh");
        if (field == null) {
            return;
        }
        scrollIntoView(field);
        clickElement(field);
        pauseAfterLoad();
        WebElement option = visibleOptionContaining("Nam");
        if (option == null) {
            option = visibleOptionContaining("Nu");
        }
        if (option == null) {
            option = visibleOptionContaining("Nữ");
        }
        if (option == null) {
            option = visibleTextElement("Nam", "Nu", "Nữ");
        }
        if (option == null) {
            throw new IllegalStateException("Gender dropdown is open but no Nam/Nu option is visible.");
        }
        clickElement(option);
        pauseAfterLoad();
    }

    private boolean clearBirthDateIfAvailable() {
        WebElement field = editorControlByLabel("ngay sinh");
        if (field == null) {
            return false;
        }
        clearControlValue(field);
        return true;
    }

    private boolean clearGenderIfAvailable() {
        WebElement field = editorControlByLabel("gioi tinh");
        if (field == null) {
            return false;
        }
        clearControlValue(field);
        return true;
    }

    private boolean inputMatchesCaseText(WebElement input, String normalizedCaseText) {
        String inputText = normalize(String.join(" ",
                input.getAttribute("placeholder"),
                input.getAttribute("name"),
                input.getAttribute("aria-label"),
                input.getAttribute("value")));
        return fieldMentionMatches(normalizedCaseText, inputText, "ho ten", "name")
                || fieldMentionMatches(normalizedCaseText, inputText, "so giay to", "cccd", "passport", "document")
                || fieldMentionMatches(normalizedCaseText, inputText, "quoc tich", "nationality")
                || fieldMentionMatches(normalizedCaseText, inputText, "que quan", "noi sinh", "origin")
                || fieldMentionMatches(normalizedCaseText, inputText, "thuong tru", "noi cu tru", "residence");
    }

    private boolean fieldMentionMatches(String caseText, String inputText, String... aliases) {
        boolean caseMentionsField = false;
        boolean inputIsField = false;
        for (String alias : aliases) {
            String normalizedAlias = normalize(alias);
            caseMentionsField = caseMentionsField || caseText.contains(normalizedAlias);
            inputIsField = inputIsField || inputText.contains(normalizedAlias);
        }
        return caseMentionsField && inputIsField;
    }

    private void clearControlValue(WebElement control) {
        scrollIntoView(control);
        clickElement(control);
        try {
            control.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            control.sendKeys(Keys.DELETE);
        } catch (RuntimeException ignored) {
            // Some custom select controls do not accept keyboard input; JS fallback below clears visible values.
        }
        ((JavascriptExecutor) driver).executeScript(
                "const el = arguments[0];"
                        + "const setNativeValue = (node, value) => {"
                        + "  const proto = node instanceof HTMLTextAreaElement ? HTMLTextAreaElement.prototype : HTMLInputElement.prototype;"
                        + "  const descriptor = Object.getOwnPropertyDescriptor(proto, 'value');"
                        + "  if (descriptor && descriptor.set) descriptor.set.call(node, value); else node.value = value;"
                        + "  node.dispatchEvent(new Event('input', {bubbles:true}));"
                        + "  node.dispatchEvent(new Event('change', {bubbles:true}));"
                        + "};"
                        + "if (el.matches('input, textarea')) setNativeValue(el, '');"
                        + "const nested = el.querySelector && el.querySelector('input, textarea');"
                        + "if (nested) setNativeValue(nested, '');"
                        + "if (el.isContentEditable) {"
                        + "  el.textContent = '';"
                        + "  el.dispatchEvent(new InputEvent('input', {bubbles:true, inputType:'deleteContent'}));"
                        + "}"
                        + "el.setAttribute('data-automation-cleared', 'true');",
                control);
        pauseAfterLoad();
    }

    private WebElement editorControlByLabel(String normalizedLabel) {
        Object control = ((JavascriptExecutor) driver).executeScript(
                "const wanted = arguments[0];"
                        + "const norm = text => (text || '').normalize('NFD').replace(/[\\u0300-\\u036f]/g, '')"
                        + "  .replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase().trim();"
                        + "const visible = el => {"
                        + "  const r = el.getBoundingClientRect();"
                        + "  const style = getComputedStyle(el);"
                        + "  return r.width > 0 && r.height > 0 && style.display !== 'none' && style.visibility !== 'hidden';"
                        + "};"
                        + "const labels = Array.from(document.querySelectorAll('label, p, span, div'))"
                        + "  .filter(el => visible(el) && norm(el.innerText || el.textContent) === wanted);"
                        + "for (const label of labels) {"
                        + "  let scope = label.parentElement;"
                        + "  for (let i = 0; scope && i < 4; i++, scope = scope.parentElement) {"
                        + "    const controls = Array.from(scope.querySelectorAll('input, textarea, button, [role=\"button\"], [role=\"combobox\"], [aria-haspopup=\"listbox\"]'))"
                        + "      .filter(el => visible(el) && !el.disabled && el !== label);"
                        + "    if (controls.length) return controls[0];"
                        + "  }"
                        + "  const next = label.nextElementSibling;"
                        + "  if (next) {"
                        + "    const nested = next.matches('input, textarea, button, [role=\"button\"], [role=\"combobox\"], [aria-haspopup=\"listbox\"]')"
                        + "      ? next : next.querySelector('input, textarea, button, [role=\"button\"], [role=\"combobox\"], [aria-haspopup=\"listbox\"]');"
                        + "    if (nested && visible(nested) && !nested.disabled) return nested;"
                        + "  }"
                        + "}"
                        + "return null;",
                normalizedLabel);
        return control instanceof WebElement element ? element : null;
    }

    private WebElement visibleCalendarDay() {
        return driver.findElements(By.cssSelector(
                        "[role='gridcell'] button, [role='gridcell'], .react-datepicker__day, td button"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .filter(element -> !isFloatingSupportElement(element))
                .filter(element -> {
                    String text = element.getText().trim();
                    String classes = String.valueOf(element.getAttribute("class")).toLowerCase(Locale.ROOT);
                    return text.matches("\\d{1,2}")
                            && !classes.contains("disabled")
                            && !classes.contains("outside")
                            && !classes.contains("muted");
                })
                .filter(element -> {
                    int day = Integer.parseInt(element.getText().trim());
                    return day >= 1 && day <= 28;
                })
                .findFirst()
                .orElse(null);
    }

    private WebElement visibleOptionContaining(String label) {
        String normalizedLabel = normalize(label);
        return driver.findElements(By.cssSelector(
                        "[role='option'], [data-slot='listbox'] li, [data-slot='listbox'] *, li,"
                                + " [class*='popover'] *, [class*='dropdown'] *, [class*='listbox'] *"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .filter(element -> !isFloatingSupportElement(element))
                .filter(element -> normalize(element.getText()).equals(normalizedLabel)
                        || normalize(element.getText()).contains(normalizedLabel))
                .findFirst()
                .orElse(null);
    }

    private WebElement visibleTextElement(String... labels) {
        Object option = ((JavascriptExecutor) driver).executeScript(
                "const labels = Array.from(arguments);"
                        + "const norm = text => (text || '').normalize('NFD').replace(/[\\u0300-\\u036f]/g, '')"
                        + "  .replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase().trim();"
                        + "const wanted = labels.map(norm);"
                        + "const visible = el => {"
                        + "  const r = el.getBoundingClientRect();"
                        + "  const style = getComputedStyle(el);"
                        + "  return r.width > 0 && r.height > 0 && style.display !== 'none' && style.visibility !== 'hidden';"
                        + "};"
                        + "const candidates = Array.from(document.querySelectorAll('div, span, li, button, [role=\"option\"]'))"
                        + "  .filter(el => visible(el) && wanted.includes(norm(el.innerText || el.textContent)));"
                        + "return candidates.sort((a, b) => a.children.length - b.children.length)[0] || null;",
                (Object[]) labels);
        return option instanceof WebElement element && !isFloatingSupportElement(element) ? element : null;
    }

    private void setInputValue(WebElement input, String value) {
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
        wait.until(webDriver -> value.equals(input.getAttribute("value")));
    }

    private String editValueFor(WebElement input) {
        String text = normalize(String.join(" ",
                input.getAttribute("placeholder"),
                input.getAttribute("name"),
                input.getAttribute("aria-label")));
        if (text.contains("so giay to") || text.contains("cccd") || text.contains("passport")) {
            return "000000000001";
        }
        if (text.contains("quoc tich")) {
            return "Viet Nam";
        }
        if (text.contains("que quan")) {
            return "Automation Origin";
        }
        if (text.contains("thuong tru")) {
            return "Automation Residence";
        }
        return "Automation KYC Test";
    }

    private void clickVisibleButtonContaining(String label) {
        String normalizedLabel = normalize(label);
        WebElement button = driver.findElements(By.cssSelector("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .filter(candidate -> !isFloatingSupportElement(candidate))
                .filter(candidate -> normalize(candidate.getText()).contains(normalizedLabel))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Button not found: " + label));
        scrollIntoView(button);
        try {
            button.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        }
    }

    private WebElement clickableRejectReasonTarget(WebElement reason) {
        WebElement target = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].closest('label') || arguments[0].parentElement || arguments[0];",
                reason);
        return target == null ? reason : target;
    }

    private boolean reasonIsSelected(WebElement reason) {
        Object selected = ((JavascriptExecutor) driver).executeScript(
                "const input = arguments[0];"
                        + "const label = input.closest('label');"
                        + "return !!(input.checked || input.getAttribute('aria-checked') === 'true'"
                        + " || (label && label.getAttribute('aria-checked') === 'true'));",
                reason);
        return Boolean.TRUE.equals(selected);
    }

    private void scrollToDrawerBottom() {
        WebElement drawer = visibleDrawer();
        ((JavascriptExecutor) driver).executeScript(
                "const drawer = arguments[0];"
                        + "drawer.scrollTop = drawer.scrollHeight;"
                        + "const scrollers = Array.from(drawer.querySelectorAll('*')).filter(el => "
                        + "el.scrollHeight > el.clientHeight);"
                        + "for (const el of scrollers) el.scrollTop = el.scrollHeight;"
                        + "window.scrollTo(0, document.body.scrollHeight);",
                drawer);
        pauseAfterLoad();
    }

    private void scrollToEditorBottom() {
        ((JavascriptExecutor) driver).executeScript(
                "const scrollers = Array.from(document.querySelectorAll('*')).filter(el => "
                        + "el.scrollHeight > el.clientHeight);"
                        + "for (const el of scrollers) el.scrollTop = el.scrollHeight;"
                        + "window.scrollTo(0, document.body.scrollHeight);");
        pauseAfterLoad();
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                element);
    }

    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private boolean isFloatingSupportElement(WebElement element) {
        Object floating = ((JavascriptExecutor) driver).executeScript(
                "let el = arguments[0];"
                        + "while (el && el !== document.body) {"
                        + "  const style = getComputedStyle(el);"
                        + "  const rect = el.getBoundingClientRect();"
                        + "  const text = (el.innerText || el.getAttribute('aria-label') || el.getAttribute('title') || '').toLowerCase();"
                        + "  const cls = (el.className || '').toString().toLowerCase();"
                        + "  const isBottomFloating = (style.position === 'fixed' || style.position === 'sticky')"
                        + "    && rect.bottom > window.innerHeight - 180 && rect.width <= 180 && rect.height <= 180;"
                        + "  const looksLikeSupport = text.includes('support') || text.includes('chat') || text.includes('hotline')"
                        + "    || text.includes('headset') || cls.includes('support') || cls.includes('chat') || cls.includes('hotline');"
                        + "  if (isBottomFloating && looksLikeSupport) return true;"
                        + "  el = el.parentElement;"
                        + "}"
                        + "return false;",
                element);
        return Boolean.TRUE.equals(floating);
    }

    private WebElement firstKycPreviewTarget() {
        for (KycSide side : List.of(KycSide.FRONT, KycSide.BACK, KycSide.SELFIE)) {
            WebElement section = sideSection(side);
            if (section == null) {
                continue;
            }
            WebElement target = previewTargetIn(section);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    private WebElement previewTargetIn(WebElement section) {
        Object target = ((JavascriptExecutor) driver).executeScript(
                "const section = arguments[0];"
                        + "const blockedText = /hop le|tu choi|xoay|rotate/i;"
                        + "const visible = el => {"
                        + "  const r = el.getBoundingClientRect();"
                        + "  const style = getComputedStyle(el);"
                        + "  return r.width > 80 && r.height > 80 && style.visibility !== 'hidden'"
                        + "    && style.display !== 'none';"
                        + "};"
                        + "const usable = el => !el.closest('button') && !blockedText.test(el.innerText || '');"
                        + "const media = Array.from(section.querySelectorAll('img, canvas, picture')).filter(el => {"
                        + "  const r = el.getBoundingClientRect();"
                        + "  return r.width > 40 && r.height > 40 && usable(el);"
                        + "});"
                        + "if (media.length) return media.sort((a, b) => {"
                        + "  const ar = a.getBoundingClientRect();"
                        + "  const br = b.getBoundingClientRect();"
                        + "  return (br.width * br.height) - (ar.width * ar.height);"
                        + "})[0];"
                        + "const candidates = Array.from(section.querySelectorAll('div, figure')).filter(el => {"
                        + "  if (!visible(el) || !usable(el)) return false;"
                        + "  const text = (el.innerText || '').trim().toLowerCase();"
                        + "  return !text || !blockedText.test(text);"
                        + "});"
                        + "return candidates.sort((a, b) => {"
                        + "  const ar = a.getBoundingClientRect();"
                        + "  const br = b.getBoundingClientRect();"
                        + "  return (br.width * br.height) - (ar.width * ar.height);"
                        + "})[0] || null;",
                section);
        return target instanceof WebElement element ? element : null;
    }

    private WebElement firstEditIconButton() {
        return visibleDrawer().findElements(By.cssSelector("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getText().trim().isBlank())
                .filter(button -> !button.findElements(By.cssSelector("svg")).isEmpty())
                .filter(button -> {
                    String classes = String.valueOf(button.getAttribute("class"));
                    return classes.contains("rounded-full") || classes.contains("rounded-lg");
                })
                .findFirst()
                .orElse(null);
    }

    private WebElement firstClearActionButton() {
        return visibleDrawer().findElements(By.cssSelector("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> {
                    String text = normalize(String.join(" ",
                            button.getText(),
                            button.getAttribute("title"),
                            button.getAttribute("aria-label"),
                            button.getAttribute("data-testid")));
                    return text.contains("xoa")
                            || text.contains("clear")
                            || text.contains("delete")
                            || text.contains("trash");
                })
                .findFirst()
                .orElse(null);
    }

    private boolean labelsContainEditAction(String... labels) {
        for (String label : labels) {
            String normalizedLabel = normalize(label);
            if (normalizedLabel.contains("sua")
                    || normalizedLabel.contains("chinh sua")
                    || normalizedLabel.contains("cap nhat")) {
                return true;
            }
        }
        return false;
    }

    private boolean labelsContainClearAction(String... labels) {
        for (String label : labels) {
            String normalizedLabel = normalize(label);
            if (normalizedLabel.contains("xoa")
                    || normalizedLabel.contains("clear")
                    || normalizedLabel.contains("delete")) {
                return true;
            }
        }
        return false;
    }

    private void clickAndWaitForTable(By locator) {
        String before = tableFingerprint();
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        waitForTableReadyAfter(before);
    }

    private void waitForTableReadyAfter(String before) {
        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        wait.until(webDriver -> !tableFingerprint().isBlank());
        wait.until(webDriver -> !tableFingerprint().equals(before) || noLoadingIndicatorIsVisible());
        pauseAfterLoad();
    }

    private String tableFingerprint() {
        return driver.findElements(TABLE).stream()
                .findFirst()
                .map(WebElement::getText)
                .orElse("");
    }

    private boolean isDisplayed(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    private boolean isDisabled(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .map(element -> "true".equals(element.getAttribute("aria-disabled"))
                        || "true".equals(element.getAttribute("data-disabled")))
                .orElse(false);
    }

    private boolean noLoadingIndicatorIsVisible() {
        return driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed);
    }

    private boolean documentIsReady() {
        return "complete".equals(((JavascriptExecutor) driver)
                .executeScript("return document.readyState"));
    }

    private boolean currentRouteContains(String path) {
        if (driver.getCurrentUrl().contains(path)) {
            return true;
        }
        Object nextPage = ((JavascriptExecutor) driver).executeScript(
                "const data=document.querySelector('#__NEXT_DATA__');"
                        + "return data && data.textContent.includes(arguments[0]);",
                "\"page\":\"" + path + "\"");
        return Boolean.TRUE.equals(nextPage);
    }

    private String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    private String resolveAppUrl(String path) {
        URI base = URI.create(TestConfig.baseUrl());
        return base.resolve(path.startsWith("/") ? path.substring(1) : path).toString();
    }

    private void pauseAfterLoad() {
        try {
            Thread.sleep(Duration.ofSeconds(2).toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting after eKYC load.", exception);
        }
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).replace('đ', 'd').trim();
    }

    private static String digitsOnly(String text) {
        Matcher matcher = Pattern.compile("\\d+").matcher(text == null ? "" : text);
        return matcher.find() ? matcher.group() : "";
    }

    private static long parseLong(String text) {
        String digits = (text == null ? "" : text).replaceAll("[^0-9]", "");
        return digits.isBlank() ? 0L : Long.parseLong(digits);
    }

    private static double parsePercent(String text) {
        String normalized = text == null ? "" : text.replace(",", ".");
        Matcher matcher = Pattern.compile("-?\\d+(?:\\.\\d+)?").matcher(normalized);
        return matcher.find() ? Double.parseDouble(matcher.group()) : Double.NaN;
    }
}
