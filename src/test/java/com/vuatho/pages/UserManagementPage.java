package com.vuatho.pages;

import com.vuatho.navigation.MenuTarget;
import com.vuatho.utils.PageLoadSynchronizer;
import com.vuatho.utils.TextNormalizer;
import com.vuatho.utils.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UserManagementPage {
    private static final By SEARCH_INPUT = By.cssSelector("input[aria-label='Tìm kiếm người dùng']");
    private static final By TABLE = By.cssSelector("table[aria-label='Table about User Management']");
    private static final By ROWS = By.cssSelector(
            "table[aria-label='Table about User Management'] tbody tr[role='row'], "
                    + "table[aria-label='Table about User Management'] tbody tr, "
                    + "main tbody tr, main [role='row']");
    private static final By LOADING_INDICATORS = By.cssSelector(
            "[role='progressbar'], .ant-spin-spinning, .ant-skeleton, .skeleton");
    private static final By NEXT_PAGE_BUTTON = By.cssSelector(
            "[aria-label='next page button'], [aria-label='next page'], button[aria-label*='next']");
    private static final By DETAIL_SURFACES = By.cssSelector(
            "[role='dialog'], [aria-label*='drawer'], [class*='modal'], [class*='drawer']");
    private static final By DIALOGS = By.cssSelector("[role='dialog'], [class*='modal']");
    private static final MenuTarget USER_MANAGEMENT_MENU_TARGET =
            MenuTarget.childOf("Người Dùng", "Quản Lí Người Dùng");
    private static final String USER_MANAGEMENT_ROUTE = "/vuatho/user";
    private static final List<String> DETAIL_TEXT_MARKERS = List.of(
            "thong tin nguoi dung",
            "chi tiet nguoi dung",
            "thong tin ca nhan",
            "user id",
            "so dien thoai",
            "ho ten");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public UserManagementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = Waits.standard(driver);
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    public UserManagementPage openFromMenu() {
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(
                USER_MANAGEMENT_MENU_TARGET, false);
        return waitUntilLoaded();
    }

    public UserManagementPage waitUntilLoaded() {
        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> currentRouteContains(USER_MANAGEMENT_ROUTE));
        wait.until(webDriver -> isDisplayed(TABLE) || isDisplayed(SEARCH_INPUT));
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        wait.until(webDriver -> hasUserRows() || bodyHasNoDataMessage());
        return this;
    }

    public boolean hasUserRows() {
        return !visibleUserRows().isEmpty();
    }

    public String firstUserRowText() {
        return firstUserRow().getText();
    }

    public UserManagementPage openFirstUserDetail() {
        return openUserDetailAtIndex(0);
    }

    public UserManagementPage openUserWithPendingNameUpdateRequest() {
        int inspectedUsers = 0;
        int inspectedPages = 0;
        do {
            waitUntilLoaded();
            int rowCount = visibleUserRows().size();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                inspectedUsers++;
                openUserDetailAtIndex(rowIndex);
                if (hasPendingNameUpdateRequest()) {
                    System.out.printf("[UserManagement] Found pending name update after inspecting %d user(s).%n",
                            inspectedUsers);
                    return this;
                }
                System.out.printf("[UserManagement] User %d has no pending name update. Trying next user.%n",
                        inspectedUsers);
                backToUserList();
            }
            inspectedPages++;
        } while (openNextUserListPageIfAvailable() && inspectedPages < 10);

        throw new IllegalStateException("Không tìm thấy user có yêu cầu cập nhật họ tên đang chờ duyệt sau khi duyệt "
                + inspectedUsers + " user.");
    }

    public UserManagementPage openUserWithPendingAvatarUpdateRequest() {
        int inspectedUsers = 0;
        int inspectedPages = 0;
        do {
            waitUntilLoaded();
            int rowCount = visibleUserRows().size();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                inspectedUsers++;
                openUserDetailAtIndex(rowIndex);
                if (hasPendingAvatarUpdateRequest()) {
                    System.out.printf("[UserManagement] Found pending avatar update after inspecting %d user(s).%n",
                            inspectedUsers);
                    return this;
                }
                System.out.printf("[UserManagement] User %d has no pending avatar update. Trying next user.%n",
                        inspectedUsers);
                backToUserList();
            }
            inspectedPages++;
        } while (openNextUserListPageIfAvailable() && inspectedPages < 10);

        throw new IllegalStateException("Không tìm thấy user có yêu cầu cập nhật ảnh đại diện đang chờ duyệt sau khi duyệt "
                + inspectedUsers + " user.");
    }

    private UserManagementPage openUserDetailAtIndex(int rowIndex) {
        String previousState = pageState();
        String previousUrl = driver.getCurrentUrl();
        WebElement row = userRowAt(rowIndex);
        WebElement action = informationAction(row);

        scrollIntoView(action);
        System.out.println("[UserManagement] Clicking user information: " + shortText(action));
        click(action);
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> detailIsOpen(previousState, previousUrl));
        return this;
    }

    public boolean userDetailIsOpen() {
        return detailIsOpen("", "");
    }

    public String detailText() {
        return visibleDetailSurfaces().stream()
                .map(WebElement::getText)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .orElseGet(this::bodyText);
    }

    public boolean hasPendingNameUpdateRequest() {
        WebElement card = nameUpdateCard();
        return card != null && TextNormalizer.normalize(card.getText()).contains("cho duyet");
    }

    public boolean hasPendingAvatarUpdateRequest() {
        WebElement card = avatarUpdateCard();
        return card != null && TextNormalizer.normalize(card.getText()).contains("cho duyet");
    }

    public UserManagementPage approveNameUpdateRequest() {
        WebElement card = requireNameUpdateCard();
        clickButtonInsideByText(card, "Chấp nhận");
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> !hasPendingNameUpdateRequest() || confirmationOrToastIsVisible());
        return this;
    }

    public UserManagementPage rejectNameUpdateRequestWithDefaultReason() {
        openRejectNameUpdateDialog();
        selectFirstRejectReason();
        confirmRejectDialog();
        return this;
    }

    public UserManagementPage rejectNameUpdateRequestWithOtherReason(String reason) {
        openRejectNameUpdateDialog();
        selectOtherRejectReason();
        enterOtherRejectReason(reason);
        confirmRejectDialog();
        return this;
    }

    public UserManagementPage approveAvatarUpdateRequest() {
        WebElement card = requireAvatarUpdateCard();
        clickButtonInsideByText(card, "Chấp nhận");
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> !hasPendingAvatarUpdateRequest() || confirmationOrToastIsVisible());
        return this;
    }

    public UserManagementPage rejectAvatarUpdateRequestWithDefaultReason() {
        openRejectAvatarUpdateDialog();
        selectFirstRejectReason();
        confirmRejectDialog();
        return this;
    }

    public UserManagementPage rejectAvatarUpdateRequestWithOtherReason(String reason) {
        openRejectAvatarUpdateDialog();
        selectOtherRejectReason();
        enterOtherRejectReason(reason);
        confirmRejectDialog();
        return this;
    }

    private WebElement firstUserRow() {
        return userRowAt(0);
    }

    private WebElement userRowAt(int index) {
        wait.until(webDriver -> !visibleUserRows().isEmpty());
        List<WebElement> rows = visibleUserRows();
        if (rows.isEmpty() || index >= rows.size()) {
            throw new IllegalStateException("Không tìm thấy user nào trong Quản Lí Người Dùng.");
        }
        return rows.get(index);
    }

    private void backToUserList() {
        driver.navigate().back();
        waitUntilLoaded();
    }

    private boolean openNextUserListPageIfAvailable() {
        WebElement nextButton = driver.findElements(NEXT_PAGE_BUTTON).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getRect().getWidth() > 0 && button.getRect().getHeight() > 0)
                .filter(button -> !"true".equalsIgnoreCase(button.getAttribute("disabled")))
                .filter(button -> !"true".equalsIgnoreCase(button.getAttribute("aria-disabled")))
                .findFirst()
                .orElse(null);
        if (nextButton == null) {
            return false;
        }
        String previousState = pageState();
        scrollIntoView(nextButton);
        click(nextButton);
        wait.until(webDriver -> !pageState().equals(previousState));
        waitUntilLoaded();
        return true;
    }

    private WebElement requireNameUpdateCard() {
        WebElement card = nameUpdateCard();
        if (card == null) {
            throw new IllegalStateException("Không tìm thấy card cập nhật họ tên đang chờ duyệt.");
        }
        return card;
    }

    private WebElement requireAvatarUpdateCard() {
        WebElement card = avatarUpdateCard();
        if (card == null) {
            throw new IllegalStateException("Không tìm thấy card cập nhật ảnh đại diện đang chờ duyệt.");
        }
        return card;
    }

    private WebElement nameUpdateCard() {
        return visibleMainElements(By.xpath(
                        "//*[self::div or self::section or self::article]"
                                + "[.//*[normalize-space()='Họ tên'] or .//*[contains(normalize-space(),'Họ tên')]]"
                                + "[.//button[normalize-space()='Chấp nhận'] or .//button[normalize-space()='Từ chối']]"))
                .stream()
                .filter(element -> TextNormalizer.normalize(element.getText()).contains("ho ten"))
                .findFirst()
                .orElse(null);
    }

    private WebElement avatarUpdateCard() {
        return visibleMainElements(By.xpath(
                        "//*[self::div or self::section or self::article]"
                                + "[.//*[normalize-space()='Ảnh đại diện'] or .//*[contains(normalize-space(),'Ảnh đại diện')]]"
                                + "[.//button[normalize-space()='Chấp nhận'] or .//button[normalize-space()='Từ chối']]"))
                .stream()
                .filter(element -> TextNormalizer.normalize(element.getText()).contains("anh dai dien"))
                .findFirst()
                .orElse(null);
    }

    private void openRejectNameUpdateDialog() {
        WebElement card = requireNameUpdateCard();
        clickButtonInsideByText(card, "Từ chối");
        wait.until(webDriver -> rejectDialog() != null);
    }

    private void openRejectAvatarUpdateDialog() {
        WebElement card = requireAvatarUpdateCard();
        clickButtonInsideByText(card, "Từ chối");
        wait.until(webDriver -> rejectDialog() != null);
    }

    private void selectFirstRejectReason() {
        WebElement dialog = requireRejectDialog();
        WebElement option = visibleChildren(dialog, By.cssSelector("label, [role='radio']")).stream()
                .filter(element -> !TextNormalizer.normalize(element.getText()).contains("ly do khac"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lý do từ chối mặc định."));
        click(option);
    }

    private void selectOtherRejectReason() {
        WebElement dialog = requireRejectDialog();
        WebElement option = visibleChildren(dialog, By.cssSelector("label, [role='radio']")).stream()
                .filter(element -> TextNormalizer.normalize(element.getText()).contains("ly do khac"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy option Lý do khác."));
        click(option);
        wait.until(webDriver -> otherReasonInput() != null);
    }

    private void enterOtherRejectReason(String reason) {
        WebElement input = otherReasonInput();
        if (input == null) {
            throw new IllegalStateException("Không tìm thấy ô nhập lý do từ chối khác.");
        }
        input.clear();
        input.sendKeys(reason);
        wait.until(webDriver -> reason.equals(otherReasonInput().getAttribute("value")));
    }

    private void confirmRejectDialog() {
        WebElement dialog = requireRejectDialog();
        clickButtonInsideByText(dialog, "Xác nhận");
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> rejectDialog() == null || confirmationOrToastIsVisible());
    }

    private WebElement rejectDialog() {
        return driver.findElements(DIALOGS).stream()
                .filter(WebElement::isDisplayed)
                .filter(dialog -> TextNormalizer.normalize(dialog.getText()).contains("ly do tu choi"))
                .findFirst()
                .orElse(null);
    }

    private WebElement requireRejectDialog() {
        WebElement dialog = rejectDialog();
        if (dialog == null) {
            throw new IllegalStateException("Không tìm thấy popup Lý do từ chối.");
        }
        return dialog;
    }

    private WebElement otherReasonInput() {
        WebElement dialog = rejectDialog();
        if (dialog == null) {
            return null;
        }
        return visibleChildren(dialog, By.cssSelector("input, textarea")).stream()
                .filter(input -> input.getRect().getWidth() > 0 && input.getRect().getHeight() > 0)
                .findFirst()
                .orElse(null);
    }

    private void clickButtonInside(WebElement container, String label) {
        WebElement button = visibleChildren(container, By.xpath(
                        ".//button[normalize-space()='" + label + "' or .//*[normalize-space()='" + label + "']]"))
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy nút " + label + "."));
        scrollIntoView(button);
        click(button);
    }

    private void clickButtonInsideByText(WebElement container, String label) {
        String expectedLabel = TextNormalizer.normalize(label);
        WebElement button = visibleChildren(container, By.cssSelector("button"))
                .stream()
                .filter(this::isEnabledButton)
                .filter(element -> buttonTextMatches(element, expectedLabel))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("KhÃ´ng tÃ¬m tháº¥y nÃºt " + label + "."));
        scrollIntoView(button);
        click(button);
    }

    private boolean buttonTextMatches(WebElement button, String expectedLabel) {
        String actualLabel = TextNormalizer.normalize(String.join(" ",
                button.getText(),
                button.getAttribute("aria-label"),
                button.getAttribute("title")));
        if (actualLabel.isBlank()) {
            return false;
        }
        return actualLabel.equals(expectedLabel)
                || actualLabel.contains(expectedLabel);
    }

    private boolean isEnabledButton(WebElement button) {
        return button.isEnabled()
                && !"true".equalsIgnoreCase(button.getAttribute("disabled"))
                && !"true".equalsIgnoreCase(button.getAttribute("aria-disabled"));
    }

    private List<WebElement> visibleMainElements(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0)
                .toList();
    }

    private List<WebElement> visibleChildren(WebElement container, By locator) {
        return container.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0)
                .toList();
    }

    private boolean confirmationOrToastIsVisible() {
        String normalized = TextNormalizer.normalize(bodyText());
        return normalized.contains("thanh cong")
                || normalized.contains("da chap nhan")
                || normalized.contains("da tu choi")
                || normalized.contains("cap nhat");
    }

    private List<WebElement> visibleUserRows() {
        return rawUserRows().stream()
                .filter(this::isUsableUserRow)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<WebElement> rawUserRows() {
        Set<WebElement> rows = new LinkedHashSet<>(driver.findElements(ROWS));
        Object jsRows = ((JavascriptExecutor) driver).executeScript(
                "const root=document.querySelector('main,[role=main]')||document.body;"
                        + "return [...root.querySelectorAll('tbody tr,[role=row]')].filter(row=>{"
                        + " const r=row.getBoundingClientRect(), s=getComputedStyle(row);"
                        + " const text=(row.innerText||row.textContent||'').trim();"
                        + " return r.width>0 && r.height>0 && s.display!=='none'"
                        + " && s.visibility!=='hidden' && text.length>0;"
                        + "});");
        if (jsRows instanceof List<?> elements) {
            rows.addAll((List<WebElement>) elements);
        }
        return new ArrayList<>(rows);
    }

    private boolean isUsableUserRow(WebElement row) {
        try {
            String text = row.getText();
            return row.isDisplayed()
                    && text != null
                    && !text.isBlank()
                    && !looksLikeHeaderRow(text)
                    && !TextNormalizer.normalize(text).contains("khong co du lieu");
        } catch (WebDriverException exception) {
            return false;
        }
    }

    private boolean looksLikeHeaderRow(String text) {
        String normalized = TextNormalizer.normalize(text);
        return normalized.contains("thong tin nguoi dung")
                && normalized.contains("trang thai")
                && normalized.contains("thoi gian tao");
    }

    private WebElement informationAction(WebElement row) {
        WebElement userInfoCell = userInformationCell(row);
        if (userInfoCell != null) {
            return userInfoCell;
        }

        List<WebElement> controls = row.findElements(By.cssSelector("a, button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0)
                .toList();
        WebElement preferred = controls.stream()
                .filter(this::looksLikeInformationAction)
                .findFirst()
                .orElse(null);
        if (preferred != null) {
            return preferred;
        }
        WebElement userLink = controls.stream()
                .filter(element -> String.valueOf(element.getAttribute("href")).contains("/vuatho/user"))
                .findFirst()
                .orElse(null);
        if (userLink != null) {
            return userLink;
        }
        if (!controls.isEmpty()) {
            return controls.get(controls.size() - 1);
        }
        return row;
    }

    private WebElement userInformationCell(WebElement row) {
        WebElement byDataKey = row.findElements(By.cssSelector(
                        "td[data-key$='user_info'], td[data-key*='user_info'], "
                                + "td[data-key*='user'], td[data-key*='name']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(cell -> !cell.getText().isBlank())
                .findFirst()
                .orElse(null);
        if (byDataKey != null) {
            return deepestClickableContent(byDataKey);
        }

        List<WebElement> cells = row.findElements(By.cssSelector("td")).stream()
                .filter(WebElement::isDisplayed)
                .filter(cell -> !cell.getText().isBlank())
                .toList();
        if (cells.size() >= 2) {
            return deepestClickableContent(cells.get(1));
        }
        return null;
    }

    private WebElement deepestClickableContent(WebElement cell) {
        return cell.findElements(By.cssSelector("a, button, [role='button'], span, p, div")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> !element.getText().isBlank())
                .filter(element -> element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0)
                .filter(this::looksLikeInformationAction)
                .findFirst()
                .orElse(cell);
    }

    private boolean looksLikeInformationAction(WebElement element) {
        String text = TextNormalizer.normalize(String.join(" ",
                element.getText(),
                element.getAttribute("aria-label"),
                element.getAttribute("title"),
                element.getAttribute("href")));
        return text.contains("xem")
                || text.contains("chi tiet")
                || text.contains("thong tin")
                || text.contains("view")
                || text.contains("detail");
    }

    private boolean detailIsOpen(String previousState, String previousUrl) {
        if (!previousState.isBlank() && !pageState().equals(previousState)
                && hasDetailText(bodyText())) {
            return true;
        }
        if (!previousUrl.isBlank() && !driver.getCurrentUrl().equals(previousUrl)
                && currentRouteContains(USER_MANAGEMENT_ROUTE)
                && bodyText().length() > 100) {
            return true;
        }
        return visibleDetailSurfaces().stream()
                .map(WebElement::getText)
                .anyMatch(this::hasDetailText);
    }

    private List<WebElement> visibleDetailSurfaces() {
        return driver.findElements(DETAIL_SURFACES).stream()
                .filter(WebElement::isDisplayed)
                .filter(surface -> surface.getRect().getWidth() > 300 && surface.getRect().getHeight() > 200)
                .toList();
    }

    private boolean hasDetailText(String text) {
        String normalized = TextNormalizer.normalize(text);
        return DETAIL_TEXT_MARKERS.stream().anyMatch(normalized::contains);
    }

    private void click(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'center'});", element);
    }

    private boolean isDisplayed(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    private boolean documentIsReady() {
        Object state = ((JavascriptExecutor) driver).executeScript("return document.readyState");
        return "complete".equals(state);
    }

    private boolean currentRouteContains(String route) {
        return driver.getCurrentUrl().contains(route);
    }

    private boolean noLoadingIndicatorIsVisible() {
        return driver.findElements(LOADING_INDICATORS).stream()
                .noneMatch(WebElement::isDisplayed);
    }

    private boolean bodyHasNoDataMessage() {
        String normalized = TextNormalizer.normalize(bodyText());
        return normalized.contains("khong co du lieu")
                || normalized.contains("no data")
                || normalized.contains("khong tim thay");
    }

    private String pageState() {
        return driver.getCurrentUrl() + "|" + bodyText().hashCode();
    }

    private String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    private String shortText(WebElement element) {
        String text = element.getText();
        if (text == null || text.isBlank()) {
            text = String.valueOf(element.getAttribute("outerHTML"));
        }
        text = text.replaceAll("\\s+", " ").trim();
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }
}
