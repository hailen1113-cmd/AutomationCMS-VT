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
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UserProfilePage {
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

    /**
     * Khởi tạo UserProfilePage với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public UserProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = Waits.standard(driver);
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    /**
     * Mở from menu trong luồng kiểm thử.
     * @return kết quả open from menu sau khi xử lý
     */
    public UserProfilePage openFromMenu() {
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(
                USER_MANAGEMENT_MENU_TARGET, false);
        return waitUntilLoaded();
    }

    /**
     * Chờ until loaded trong luồng kiểm thử.
     * @return kết quả wait until loaded sau khi xử lý
     */
    public UserProfilePage waitUntilLoaded() {
        wait.until(webDriver -> documentIsReady());
        wait.until(webDriver -> currentRouteContains(USER_MANAGEMENT_ROUTE));
        wait.until(webDriver -> isDisplayed(TABLE) || isDisplayed(SEARCH_INPUT));
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        wait.until(webDriver -> hasUserRows() || bodyHasNoDataMessage());
        return this;
    }

    /**
     * Thực hiện xử lý restore default list if needed trong luồng kiểm thử.
     * @return kết quả restore default list if needed sau khi xử lý
     */
    public UserProfilePage restoreDefaultListIfNeeded() {
        waitUntilLoaded();
        clearSearch();
        return waitUntilLoaded();
    }

    /**
     * Kiểm tra điều kiện has user rows.
     * @return kết quả has user rows sau khi xử lý
     */
    public boolean hasUserRows() {
        return !visibleUserRows().isEmpty();
    }

    /**
     * Chờ until at least user rows visible trong luồng kiểm thử.
     * @param minimumRows giá trị minimum rows được truyền vào
     */
    public void waitUntilAtLeastUserRowsVisible(int minimumRows) {
        wait.until(webDriver -> noLoadingIndicatorIsVisible());
        wait.until(webDriver -> visibleUserRows().size() >= minimumRows);
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> visibleUserRows().size() >= minimumRows);
    }

    /**
     * Trả về first user row text từ trạng thái hiện tại.
     * @return kết quả first user row text sau khi xử lý
     */
    public String firstUserRowText() {
        return firstUserRow().getText();
    }

    /**
     * Kiểm tra điều kiện has search input.
     * @return kết quả has search input sau khi xử lý
     */
    public boolean hasSearchInput() {
        return searchInput().isDisplayed();
    }

    /**
     * Kiểm tra điều kiện has search mode options.
     * @return kết quả has search mode options sau khi xử lý
     */
    public boolean hasSearchModeOptions() {
        List<String> options = new Select(visibleSearchModeSelect()).getOptions().stream()
                .map(WebElement::getText)
                .map(TextNormalizer::normalize)
                .toList();
        return options.stream().anyMatch(option -> option.contains("ten"))
                && options.stream().anyMatch(option -> option.contains("sdt"));
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
                .orElseThrow(() -> new IllegalStateException("Khong tim thay mode tim kiem nguoi dung: " + modeLabel));
        scrollIntoView(selectElement);
        click(selectElement);
        try {
            option.click();
        } catch (WebDriverException exception) {
            select.selectByVisibleText(option.getText());
        }
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
        searchFor(query);
        clearSearch();
    }

    /**
     * Thực hiện xử lý search by mode trong luồng kiểm thử.
     * @param modeLabel giá trị mode label được truyền vào
     * @param query giá trị query được truyền vào
     */
    public void searchByMode(String modeLabel, String query) {
        selectSearchMode(modeLabel);
        searchFor(query);
    }

    /**
     * Thực hiện xử lý search for trong luồng kiểm thử.
     * @param query giá trị query được truyền vào
     */
    public void searchFor(String query) {
        WebElement input = searchInput();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        wait.until(webDriver -> searchInput().getAttribute("value").isBlank());
        PageLoadSynchronizer.prepareForAsyncAction(driver);
        String stateBeforeSearch = PageLoadSynchronizer.mainContentState(driver);
        input = searchInput();
        input.sendKeys(query);
        wait.until(webDriver -> query.equals(searchInput().getAttribute("value")));
        PageLoadSynchronizer.waitForSearchResultsToLoad(driver, stateBeforeSearch);
        wait.until(webDriver -> hasUserRows() || bodyHasNoDataMessage());
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
     * Trả về first user search term từ trạng thái hiện tại.
     * @return kết quả first user search term sau khi xử lý
     */
    public String firstUserSearchTerm() {
        return List.of(firstUserRowText().split("\\R")).stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.matches("\\d{5,}"))
                .filter(line -> !line.matches(".*\\d{2}[-/]\\d{2}[-/]\\d{4}.*"))
                .filter(line -> line.length() >= 3)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Khong doc duoc gia tri tim kiem o dong nguoi dung dau tien."));
    }

    /**
     * Trả về first visible user name từ trạng thái hiện tại.
     * @return kết quả first visible user name sau khi xử lý
     */
    public String firstVisibleUserName() {
        return firstUserInfoCellLines().stream()
                .filter(line -> !line.matches(".*\\d{6,}.*"))
                .filter(line -> line.length() >= 3)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Khong doc duoc ten nguoi dung dau tien dang hien thi."));
    }

    /**
     * Trả về first visible user phone search term từ trạng thái hiện tại.
     * @return kết quả first visible user phone search term sau khi xử lý
     */
    public String firstVisibleUserPhoneSearchTerm() {
        return firstUserInfoCellLines().stream()
                .filter(line -> line.matches(".*\\d{6,}.*"))
                .filter(line -> line.replaceAll("[^0-9]", "").length() >= 6)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Khong doc duoc SDT nguoi dung dau tien dang hien thi."));
    }

    /**
     * Trả về first visible user names từ trạng thái hiện tại.
     * @param maxCount giá trị max count được truyền vào
     * @return kết quả first visible user names sau khi xử lý
     */
    public List<String> firstVisibleUserNames(int maxCount) {
        waitUntilAtLeastUserRowsVisible(maxCount);
        return visibleUserRows().stream()
                .map(this::userInfoCellLines)
                .flatMap(List::stream)
                .map(String::trim)
                .filter(line -> !line.matches(".*\\d{6,}.*"))
                .filter(line -> line.length() >= 3)
                .limit(maxCount)
                .toList();
    }

    /**
     * Trả về first visible user phone search terms từ trạng thái hiện tại.
     * @param maxCount giá trị max count được truyền vào
     * @return kết quả first visible user phone search terms sau khi xử lý
     */
    public List<String> firstVisibleUserPhoneSearchTerms(int maxCount) {
        waitUntilAtLeastUserRowsVisible(maxCount);
        return visibleUserRows().stream()
                .map(this::userInfoCellLines)
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
        return visibleUserRows().stream()
                .map(WebElement::getText)
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
        List<String> rows = visibleUserRows().stream()
                .map(WebElement::getText)
                .map(this::normalizeSearchTerm)
                .toList();
        return !rows.isEmpty() && rows.stream().allMatch(row -> row.contains(expected));
    }

    /**
     * Mở first user detail trong luồng kiểm thử.
     * @return kết quả open first user detail sau khi xử lý
     */
    public UserProfilePage openFirstUserDetail() {
        return openUserDetailAtIndex(0);
    }

    /**
     * Mở user detail tab trong luồng kiểm thử.
     * @param tabLabel giá trị tab label được truyền vào
     * @return kết quả open user detail tab sau khi xử lý
     */
    public UserProfilePage openUserDetailTab(String tabLabel) {
        WebElement tab = userDetailTab(tabLabel);
        String previousState = detailSurfaceState();
        System.out.println("[UserProfile] Mo tab chi tiet nguoi dung: " + tabLabel);
        scrollIntoView(tab);
        PageLoadSynchronizer.prepareForAsyncAction(driver);
        click(tab);
        waitForUserDetailTabToLoad(tabLabel, previousState);
        return this;
    }

    /**
     * Cuộn current user detail tab to bottom then top trong luồng kiểm thử.
     * @param tabLabel giá trị tab label được truyền vào
     * @return kết quả scroll current user detail tab to bottom then top sau khi xử lý
     */
    public UserProfilePage scrollCurrentUserDetailTabToBottomThenTop(String tabLabel) {
        scrollUserDetailToBottomThenTop(tabLabel);
        return this;
    }

    /**
     * Thực hiện xử lý user detail tab is selected trong luồng kiểm thử.
     * @param tabLabel giá trị tab label được truyền vào
     * @return kết quả user detail tab is selected sau khi xử lý
     */
    public boolean userDetailTabIsSelected(String tabLabel) {
        WebElement tab = userDetailTab(tabLabel);
        String normalizedClass = TextNormalizer.normalize(String.valueOf(tab.getAttribute("class")));
        String normalizedAriaSelected = TextNormalizer.normalize(String.valueOf(tab.getAttribute("aria-selected")));
        String normalizedAriaCurrent = TextNormalizer.normalize(String.valueOf(tab.getAttribute("aria-current")));
        return "true".equals(normalizedAriaSelected)
                || "page".equals(normalizedAriaCurrent)
                || normalizedClass.contains("active")
                || normalizedClass.contains("selected")
                || normalizedClass.contains("current")
                || normalizedClass.contains("primary");
    }

    /**
     * Mở user with pending name update request trong luồng kiểm thử.
     * @return kết quả open user with pending name update request sau khi xử lý
     */
    public UserProfilePage openUserWithPendingNameUpdateRequest() {
        int inspectedUsers = 0;
        int inspectedPages = 0;
        do {
            waitUntilLoaded();
            int rowCount = visibleUserRows().size();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                inspectedUsers++;
                openUserDetailAtIndex(rowIndex);
                if (hasPendingNameUpdateRequest()) {
                    System.out.printf("[UserProfile] Tim thay yeu cau cap nhat ho ten sau khi kiem tra %d nguoi dung.%n",
                            inspectedUsers);
                    return this;
                }
                System.out.printf("[UserProfile] Nguoi dung %d khong co yeu cau cap nhat ho ten. Thu nguoi tiep theo.%n",
                        inspectedUsers);
                backToUserList();
            }
            inspectedPages++;
        } while (openNextUserListPageIfAvailable() && inspectedPages < 10);

        throw new IllegalStateException("Không tìm thấy user có yêu cầu cập nhật họ tên đang chờ duyệt sau khi duyệt "
                + inspectedUsers + " user.");
    }

    /**
     * Mở user with pending avatar update request trong luồng kiểm thử.
     * @return kết quả open user with pending avatar update request sau khi xử lý
     */
    public UserProfilePage openUserWithPendingAvatarUpdateRequest() {
        int inspectedUsers = 0;
        int inspectedPages = 0;
        do {
            waitUntilLoaded();
            int rowCount = visibleUserRows().size();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                inspectedUsers++;
                openUserDetailAtIndex(rowIndex);
                if (hasPendingAvatarUpdateRequest()) {
                    System.out.printf("[UserProfile] Tim thay yeu cau cap nhat anh dai dien sau khi kiem tra %d nguoi dung.%n",
                            inspectedUsers);
                    return this;
                }
                System.out.printf("[UserProfile] Nguoi dung %d khong co yeu cau cap nhat anh dai dien. Thu nguoi tiep theo.%n",
                        inspectedUsers);
                backToUserList();
            }
            inspectedPages++;
        } while (openNextUserListPageIfAvailable() && inspectedPages < 10);

        throw new IllegalStateException("Không tìm thấy user có yêu cầu cập nhật ảnh đại diện đang chờ duyệt sau khi duyệt "
                + inspectedUsers + " user.");
    }

    /**
     * Mở user detail at index trong luồng kiểm thử.
     * @param rowIndex giá trị row index được truyền vào
     * @return kết quả open user detail at index sau khi xử lý
     */
    private UserProfilePage openUserDetailAtIndex(int rowIndex) {
        String previousState = pageState();
        String previousUrl = driver.getCurrentUrl();
        WebElement row = userRowAt(rowIndex);
        WebElement action = informationAction(row);

        scrollIntoView(action);
        System.out.println("[UserProfile] Bam thong tin nguoi dung: " + shortText(action));
        click(action);
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> detailIsOpen(previousState, previousUrl));
        return this;
    }

    /**
     * Thực hiện xử lý user detail is open trong luồng kiểm thử.
     * @return kết quả user detail is open sau khi xử lý
     */
    public boolean userDetailIsOpen() {
        return detailIsOpen("", "");
    }

    /**
     * Thực hiện xử lý detail text trong luồng kiểm thử.
     * @return kết quả detail text sau khi xử lý
     */
    public String detailText() {
        return visibleDetailSurfaces().stream()
                .map(WebElement::getText)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .orElseGet(this::bodyText);
    }

    /**
     * Thực hiện xử lý user detail has visible content trong luồng kiểm thử.
     * @return kết quả user detail has visible content sau khi xử lý
     */
    public boolean userDetailHasVisibleContent() {
        String normalized = TextNormalizer.normalize(requireVisibleDetailSurface().getText());
        return normalized.length() > 40
                && !normalized.contains("dang tai");
    }

    /**
     * Kiểm tra điều kiện has pending name update request.
     * @return kết quả has pending name update request sau khi xử lý
     */
    public boolean hasPendingNameUpdateRequest() {
        WebElement card = nameUpdateCard();
        return card != null && TextNormalizer.normalize(card.getText()).contains("cho duyet");
    }

    /**
     * Kiểm tra điều kiện has pending avatar update request.
     * @return kết quả has pending avatar update request sau khi xử lý
     */
    public boolean hasPendingAvatarUpdateRequest() {
        WebElement card = avatarUpdateCard();
        return card != null && TextNormalizer.normalize(card.getText()).contains("cho duyet");
    }

    /**
     * Thực hiện xử lý approve name update request trong luồng kiểm thử.
     * @return kết quả approve name update request sau khi xử lý
     */
    public UserProfilePage approveNameUpdateRequest() {
        WebElement card = requireNameUpdateCard();
        clickButtonInsideByText(card, "Chấp nhận");
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> !hasPendingNameUpdateRequest() || confirmationOrToastIsVisible());
        return this;
    }

    /**
     * Thực hiện xử lý reject name update request with default reason trong luồng kiểm thử.
     * @return kết quả reject name update request with default reason sau khi xử lý
     */
    public UserProfilePage rejectNameUpdateRequestWithDefaultReason() {
        openRejectNameUpdateDialog();
        selectFirstRejectReason();
        confirmRejectDialog();
        return this;
    }

    /**
     * Thực hiện xử lý reject name update request with other reason trong luồng kiểm thử.
     * @param reason giá trị reason được truyền vào
     * @return kết quả reject name update request with other reason sau khi xử lý
     */
    public UserProfilePage rejectNameUpdateRequestWithOtherReason(String reason) {
        openRejectNameUpdateDialog();
        selectOtherRejectReason();
        enterOtherRejectReason(reason);
        confirmRejectDialog();
        return this;
    }

    /**
     * Thực hiện xử lý approve avatar update request trong luồng kiểm thử.
     * @return kết quả approve avatar update request sau khi xử lý
     */
    public UserProfilePage approveAvatarUpdateRequest() {
        WebElement card = requireAvatarUpdateCard();
        clickButtonInsideByText(card, "Chấp nhận");
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> !hasPendingAvatarUpdateRequest() || confirmationOrToastIsVisible());
        return this;
    }

    /**
     * Thực hiện xử lý reject avatar update request with default reason trong luồng kiểm thử.
     * @return kết quả reject avatar update request with default reason sau khi xử lý
     */
    public UserProfilePage rejectAvatarUpdateRequestWithDefaultReason() {
        openRejectAvatarUpdateDialog();
        selectFirstRejectReason();
        confirmRejectDialog();
        return this;
    }

    /**
     * Thực hiện xử lý reject avatar update request with other reason trong luồng kiểm thử.
     * @param reason giá trị reason được truyền vào
     * @return kết quả reject avatar update request with other reason sau khi xử lý
     */
    public UserProfilePage rejectAvatarUpdateRequestWithOtherReason(String reason) {
        openRejectAvatarUpdateDialog();
        selectOtherRejectReason();
        enterOtherRejectReason(reason);
        confirmRejectDialog();
        return this;
    }

    /**
     * Trả về first user row từ trạng thái hiện tại.
     * @return kết quả first user row sau khi xử lý
     */
    private WebElement firstUserRow() {
        return userRowAt(0);
    }

    /**
     * Thực hiện xử lý user row at trong luồng kiểm thử.
     * @param index giá trị index được truyền vào
     * @return kết quả user row at sau khi xử lý
     */
    private WebElement userRowAt(int index) {
        wait.until(webDriver -> !visibleUserRows().isEmpty());
        List<WebElement> rows = visibleUserRows();
        if (rows.isEmpty() || index >= rows.size()) {
            throw new IllegalStateException("Không tìm thấy user nào trong Quản Lí Người Dùng.");
        }
        return rows.get(index);
    }

    /**
     * Thực hiện xử lý back to user list trong luồng kiểm thử.
     */
    private void backToUserList() {
        driver.navigate().back();
        waitUntilLoaded();
    }

    /**
     * Mở next user list page if available trong luồng kiểm thử.
     * @return kết quả open next user list page if available sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý require name update card trong luồng kiểm thử.
     * @return kết quả require name update card sau khi xử lý
     */
    private WebElement requireNameUpdateCard() {
        WebElement card = nameUpdateCard();
        if (card == null) {
            throw new IllegalStateException("Không tìm thấy card cập nhật họ tên đang chờ duyệt.");
        }
        return card;
    }

    /**
     * Thực hiện xử lý require avatar update card trong luồng kiểm thử.
     * @return kết quả require avatar update card sau khi xử lý
     */
    private WebElement requireAvatarUpdateCard() {
        WebElement card = avatarUpdateCard();
        if (card == null) {
            throw new IllegalStateException("Không tìm thấy card cập nhật ảnh đại diện đang chờ duyệt.");
        }
        return card;
    }

    /**
     * Thực hiện xử lý name update card trong luồng kiểm thử.
     * @return kết quả name update card sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý avatar update card trong luồng kiểm thử.
     * @return kết quả avatar update card sau khi xử lý
     */
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

    /**
     * Mở reject name update dialog trong luồng kiểm thử.
     */
    private void openRejectNameUpdateDialog() {
        WebElement card = requireNameUpdateCard();
        clickButtonInsideByText(card, "Từ chối");
        wait.until(webDriver -> rejectDialog() != null);
    }

    /**
     * Mở reject avatar update dialog trong luồng kiểm thử.
     */
    private void openRejectAvatarUpdateDialog() {
        WebElement card = requireAvatarUpdateCard();
        clickButtonInsideByText(card, "Từ chối");
        wait.until(webDriver -> rejectDialog() != null);
    }

    /**
     * Kích hoạt first reject reason trong luồng kiểm thử.
     */
    private void selectFirstRejectReason() {
        WebElement dialog = requireRejectDialog();
        WebElement option = visibleChildren(dialog, By.cssSelector("label, [role='radio']")).stream()
                .filter(element -> !TextNormalizer.normalize(element.getText()).contains("ly do khac"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lý do từ chối mặc định."));
        click(option);
    }

    /**
     * Kích hoạt other reject reason trong luồng kiểm thử.
     */
    private void selectOtherRejectReason() {
        WebElement dialog = requireRejectDialog();
        WebElement option = visibleChildren(dialog, By.cssSelector("label, [role='radio']")).stream()
                .filter(element -> TextNormalizer.normalize(element.getText()).contains("ly do khac"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy option Lý do khác."));
        click(option);
        wait.until(webDriver -> otherReasonInput() != null);
    }

    /**
     * Cập nhật other reject reason trong luồng kiểm thử.
     * @param reason giá trị reason được truyền vào
     */
    private void enterOtherRejectReason(String reason) {
        WebElement input = otherReasonInput();
        if (input == null) {
            throw new IllegalStateException("Không tìm thấy ô nhập lý do từ chối khác.");
        }
        input.clear();
        input.sendKeys(reason);
        wait.until(webDriver -> reason.equals(otherReasonInput().getAttribute("value")));
    }

    /**
     * Thực hiện xử lý confirm reject dialog trong luồng kiểm thử.
     */
    private void confirmRejectDialog() {
        WebElement dialog = requireRejectDialog();
        clickButtonInsideByText(dialog, "Xác nhận");
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> rejectDialog() == null || confirmationOrToastIsVisible());
    }

    /**
     * Thực hiện xử lý reject dialog trong luồng kiểm thử.
     * @return kết quả reject dialog sau khi xử lý
     */
    private WebElement rejectDialog() {
        return driver.findElements(DIALOGS).stream()
                .filter(WebElement::isDisplayed)
                .filter(dialog -> TextNormalizer.normalize(dialog.getText()).contains("ly do tu choi"))
                .findFirst()
                .orElse(null);
    }

    /**
     * Thực hiện xử lý require reject dialog trong luồng kiểm thử.
     * @return kết quả require reject dialog sau khi xử lý
     */
    private WebElement requireRejectDialog() {
        WebElement dialog = rejectDialog();
        if (dialog == null) {
            throw new IllegalStateException("Không tìm thấy popup Lý do từ chối.");
        }
        return dialog;
    }

    /**
     * Thực hiện xử lý other reason input trong luồng kiểm thử.
     * @return kết quả other reason input sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý user detail tab trong luồng kiểm thử.
     * @param tabLabel giá trị tab label được truyền vào
     * @return kết quả user detail tab sau khi xử lý
     */
    private WebElement userDetailTab(String tabLabel) {
        String expectedLabel = TextNormalizer.normalize(tabLabel);
        WebElement detailSurface = requireVisibleDetailSurface();
        List<WebElement> candidates = visibleChildren(detailSurface,
                By.cssSelector("button, a, [role='tab'], [role='button'], [tabindex]"));
        WebElement directCandidate = candidates.stream()
                .filter(element -> userDetailTabTextMatches(element, expectedLabel))
                .findFirst()
                .orElse(null);
        if (directCandidate != null) {
            return directCandidate;
        }

        WebElement jsCandidate = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const root = arguments[0], expected = arguments[1];"
                        + "const normalize=t=>(t||'').toLocaleLowerCase()"
                        + ".normalize('NFD').replace(/[\\u0300-\\u036f]/g,'')"
                        + ".replace(/đ/g,'d').replace(/[^a-z0-9 ]/g,' ').replace(/\\s+/g,' ').trim();"
                        + "const visible=e=>{const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden';};"
                        + "const matches=e=>{const text=normalize(e.innerText||e.textContent);"
                        + "return text===expected||text.startsWith(expected+' ')||text.includes(expected);};"
                        + "return [...root.querySelectorAll('*')].filter(e=>visible(e)&&matches(e))"
                        + ".filter(e=>normalize(e.innerText||e.textContent).length<=80)"
                        + ".sort((a,b)=>{"
                        + " const ap=a.matches('button,a,[role=tab],[role=button],[tabindex]')?0:1;"
                        + " const bp=b.matches('button,a,[role=tab],[role=button],[tabindex]')?0:1;"
                        + " const ar=a.getBoundingClientRect(),br=b.getBoundingClientRect();"
                        + " return ap-bp || (ar.width*ar.height)-(br.width*br.height);"
                        + "})[0]||null;",
                detailSurface, expectedLabel);
        if (jsCandidate == null) {
            throw new IllegalStateException("Khong tim thay tab chi tiet nguoi dung: " + tabLabel);
        }
        return jsCandidate;
    }

    /**
     * Thực hiện xử lý user detail tab text matches trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     * @param expectedLabel giá trị expected label được truyền vào
     * @return kết quả user detail tab text matches sau khi xử lý
     */
    private boolean userDetailTabTextMatches(WebElement element, String expectedLabel) {
        String actualLabel = TextNormalizer.normalize(String.join(" ",
                element.getText(),
                element.getAttribute("aria-label"),
                element.getAttribute("title")));
        if (actualLabel.isBlank()) {
            return false;
        }
        return actualLabel.equals(expectedLabel)
                || actualLabel.startsWith(expectedLabel + " ")
                || actualLabel.contains(expectedLabel);
    }

    /**
     * Chờ for user detail tab to load trong luồng kiểm thử.
     * @param tabLabel giá trị tab label được truyền vào
     * @param previousState giá trị previous state được truyền vào
     */
    private void waitForUserDetailTabToLoad(String tabLabel, String previousState) {
        PageLoadSynchronizer.waitForDataToSettle(driver);
        wait.until(webDriver -> userDetailHasVisibleContent());
        wait.until(webDriver -> userDetailTabIsSelected(tabLabel) || !detailSurfaceState().equals(previousState));
        waitUntilDetailSurfaceStable();
    }

    /**
     * Chờ until detail surface stable trong luồng kiểm thử.
     */
    private void waitUntilDetailSurfaceStable() {
        wait.until(webDriver -> {
            String firstState = detailSurfaceState();
            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Bi gian doan khi doi noi dung tab nguoi dung on dinh.", exception);
            }
            return firstState.equals(detailSurfaceState()) && noLoadingIndicatorIsVisible();
        });
    }

    /**
     * Cuộn user detail to bottom then top trong luồng kiểm thử.
     * @param tabLabel giá trị tab label được truyền vào
     */
    private void scrollUserDetailToBottomThenTop(String tabLabel) {
        WebElement scrollContainer = userDetailScrollContainer();
        System.out.println("[UserProfile] Scroll xuong cuoi tab: " + tabLabel);
        scrollDetailContainer(scrollContainer, "bottom");
        pauseForScroll();
        PageLoadSynchronizer.waitForDataToSettle(driver);

        System.out.println("[UserProfile] Scroll lai dau tab: " + tabLabel);
        scrollDetailContainer(scrollContainer, "top");
        pauseForScroll();
        waitUntilDetailSurfaceStable();
    }

    /**
     * Thực hiện xử lý user detail scroll container trong luồng kiểm thử.
     * @return kết quả user detail scroll container sau khi xử lý
     */
    private WebElement userDetailScrollContainer() {
        return (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const surface=arguments[0];"
                        + "const candidates=[];"
                        + "let current=surface;"
                        + "while(current){candidates.push(current);current=current.parentElement;}"
                        + "candidates.push(document.scrollingElement||document.documentElement);"
                        + "const scrollable=candidates.find(e=>{"
                        + " if(!e) return false;"
                        + " const s=getComputedStyle(e);"
                        + " return e.scrollHeight>e.clientHeight+20 && (e===document.scrollingElement || /(auto|scroll)/.test(s.overflowY));"
                        + "});"
                        + "return scrollable||document.scrollingElement||document.documentElement;",
                requireVisibleDetailSurface());
    }

    /**
     * Cuộn detail container trong luồng kiểm thử.
     * @param scrollContainer giá trị scroll container được truyền vào
     * @param position giá trị position được truyền vào
     */
    private void scrollDetailContainer(WebElement scrollContainer, String position) {
        ((JavascriptExecutor) driver).executeScript(
                "const container=arguments[0], position=arguments[1], surface=arguments[2];"
                        + "if(position==='bottom'){"
                        + " container.scrollTop=container.scrollHeight;"
                        + " const visible=[...surface.querySelectorAll('*')].filter(e=>{"
                        + "  const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + "  return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden';"
                        + " });"
                        + " const last=visible.sort((a,b)=>a.getBoundingClientRect().bottom-b.getBoundingClientRect().bottom).at(-1);"
                        + " if(last) last.scrollIntoView({block:'end',inline:'nearest'});"
                        + "} else {"
                        + " container.scrollTop=0;"
                        + " surface.scrollIntoView({block:'start',inline:'nearest'});"
                        + "}",
                scrollContainer, position, requireVisibleDetailSurface());
    }

    /**
     * Thực hiện xử lý pause for scroll trong luồng kiểm thử.
     */
    private void pauseForScroll() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bi gian doan khi scroll tab nguoi dung.", exception);
        }
    }

    /**
     * Thực hiện xử lý detail surface state trong luồng kiểm thử.
     * @return kết quả detail surface state sau khi xử lý
     */
    private String detailSurfaceState() {
        return requireVisibleDetailSurface().getText().replaceAll("\\s+", " ").trim();
    }

    /**
     * Kích hoạt button inside trong luồng kiểm thử.
     * @param container giá trị container được truyền vào
     * @param label giá trị label được truyền vào
     */
    private void clickButtonInside(WebElement container, String label) {
        WebElement button = visibleChildren(container, By.xpath(
                        ".//button[normalize-space()='" + label + "' or .//*[normalize-space()='" + label + "']]"))
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy nút " + label + "."));
        scrollIntoView(button);
        click(button);
    }

    /**
     * Kích hoạt button inside by text trong luồng kiểm thử.
     * @param container giá trị container được truyền vào
     * @param label giá trị label được truyền vào
     */
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

    /**
     * Thực hiện xử lý button text matches trong luồng kiểm thử.
     * @param button giá trị button được truyền vào
     * @param expectedLabel giá trị expected label được truyền vào
     * @return kết quả button text matches sau khi xử lý
     */
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

    /**
     * Kiểm tra điều kiện is enabled button.
     * @param button giá trị button được truyền vào
     * @return kết quả is enabled button sau khi xử lý
     */
    private boolean isEnabledButton(WebElement button) {
        return button.isEnabled()
                && !"true".equalsIgnoreCase(button.getAttribute("disabled"))
                && !"true".equalsIgnoreCase(button.getAttribute("aria-disabled"));
    }

    /**
     * Trả về visible main elements từ trạng thái hiện tại.
     * @param locator locator xác định phần tử
     * @return kết quả visible main elements sau khi xử lý
     */
    private List<WebElement> visibleMainElements(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0)
                .toList();
    }

    /**
     * Thực hiện xử lý search input trong luồng kiểm thử.
     * @return kết quả search input sau khi xử lý
     */
    private WebElement searchInput() {
        return wait.until(webDriver -> visibleMainElements(SEARCH_INPUT).stream()
                .findFirst()
                .orElse(null));
    }

    /**
     * Trả về visible search mode select từ trạng thái hiện tại.
     * @return kết quả visible search mode select sau khi xử lý
     */
    private WebElement visibleSearchModeSelect() {
        WebElement nearestSelect = visibleSearchModeSelectNearSearchInput();
        if (nearestSelect != null) {
            return nearestSelect;
        }

        WebElement select = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "return [...document.querySelectorAll('select')].find(e=>{"
                        + " const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + " const text=(e.innerText||e.textContent||'').toLocaleLowerCase()"
                        + "  .normalize('NFD').replace(/[\\u0300-\\u036f]/g,'') + ' tÃªn';"
                        + " return r.width>0&&r.height>0&&s.display!=='none'"
                        + "  &&s.visibility!=='hidden'&&text.includes('sdt')&&text.includes('tên');"
                        + "})||null;");
        if (select == null) {
            throw new IllegalStateException("Khong tim thay dropdown chon kieu tim kiem nguoi dung.");
        }
        return select;
    }

    /**
     * Trả về visible search mode select near search input từ trạng thái hiện tại.
     * @return kết quả visible search mode select near search input sau khi xử lý
     */
    private WebElement visibleSearchModeSelectNearSearchInput() {
        return (WebElement) ((JavascriptExecutor) driver).executeScript(
                "const visible=e=>{"
                        + " const r=e.getBoundingClientRect(),s=getComputedStyle(e);"
                        + " return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden';"
                        + "};"
                        + "const normalize=t=>(t||'').toLocaleLowerCase()"
                        + " .normalize('NFD').replace(/[\\u0300-\\u036f]/g,'');"
                        + "const input=[...document.querySelectorAll('input')].find(e=>"
                        + " visible(e)&&normalize(e.placeholder||e.getAttribute('aria-label')).includes('tim kiem nguoi dung'));"
                        + "const selects=[...document.querySelectorAll('select')].filter(visible);"
                        + "if(!selects.length) return null;"
                        + "if(!input) return selects[0];"
                        + "const ir=input.getBoundingClientRect();"
                        + "return selects.sort((a,b)=>{"
                        + " const ar=a.getBoundingClientRect(),br=b.getBoundingClientRect();"
                        + " return (Math.abs(ar.y-ir.y)+Math.abs(ar.x-ir.x))"
                        + "  -(Math.abs(br.y-ir.y)+Math.abs(br.x-ir.x));"
                        + "})[0];");
    }

    /**
     * Trả về first user info cell lines từ trạng thái hiện tại.
     * @return kết quả first user info cell lines sau khi xử lý
     */
    private List<String> firstUserInfoCellLines() {
        return userInfoCellLines(firstUserRow());
    }

    /**
     * Thực hiện xử lý user info cell lines trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     * @return kết quả user info cell lines sau khi xử lý
     */
    private List<String> userInfoCellLines(WebElement row) {
        WebElement infoCell = userInformationTextCell(row);
        if (infoCell == null) {
            return List.of();
        }
        return List.of(infoCell.getText().split("\\R")).stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    /**
     * Thực hiện xử lý user information text cell trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     * @return kết quả user information text cell sau khi xử lý
     */
    private WebElement userInformationTextCell(WebElement row) {
        WebElement byDataKey = row.findElements(By.cssSelector(
                        "td[data-key$='user_info'], td[data-key*='user_info'], "
                                + "td[data-key*='user'], td[data-key*='name']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(cell -> !cell.getText().isBlank())
                .findFirst()
                .orElse(null);
        if (byDataKey != null) {
            return byDataKey;
        }

        List<WebElement> cells = row.findElements(By.cssSelector("td")).stream()
                .filter(WebElement::isDisplayed)
                .filter(cell -> !cell.getText().isBlank())
                .toList();
        return cells.size() >= 2 ? cells.get(1) : null;
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
     * Thực hiện xử lý confirmation or toast is visible trong luồng kiểm thử.
     * @return kết quả confirmation or toast is visible sau khi xử lý
     */
    private boolean confirmationOrToastIsVisible() {
        String normalized = TextNormalizer.normalize(bodyText());
        return normalized.contains("thanh cong")
                || normalized.contains("da chap nhan")
                || normalized.contains("da tu choi")
                || normalized.contains("cap nhat");
    }

    /**
     * Trả về visible user rows từ trạng thái hiện tại.
     * @return kết quả visible user rows sau khi xử lý
     */
    private List<WebElement> visibleUserRows() {
        return rawUserRows().stream()
                .filter(this::isUsableUserRow)
                .toList();
    }

    /**
     * Thực hiện xử lý raw user rows trong luồng kiểm thử.
     * @return kết quả raw user rows sau khi xử lý
     */
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

    /**
     * Kiểm tra điều kiện is usable user row.
     * @param row giá trị row được truyền vào
     * @return kết quả is usable user row sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý looks like header row trong luồng kiểm thử.
     * @param text nội dung cần xử lý
     * @return kết quả looks like header row sau khi xử lý
     */
    private boolean looksLikeHeaderRow(String text) {
        String normalized = TextNormalizer.normalize(text);
        return normalized.contains("thong tin nguoi dung")
                && normalized.contains("trang thai")
                && normalized.contains("thoi gian tao");
    }

    /**
     * Thực hiện xử lý information action trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     * @return kết quả information action sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý user information cell trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     * @return kết quả user information cell sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý deepest clickable content trong luồng kiểm thử.
     * @param cell giá trị cell được truyền vào
     * @return kết quả deepest clickable content sau khi xử lý
     */
    private WebElement deepestClickableContent(WebElement cell) {
        return cell.findElements(By.cssSelector("a, button, [role='button'], span, p, div")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> !element.getText().isBlank())
                .filter(element -> element.getRect().getWidth() > 0 && element.getRect().getHeight() > 0)
                .filter(this::looksLikeInformationAction)
                .findFirst()
                .orElse(cell);
    }

    /**
     * Thực hiện xử lý looks like information action trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     * @return kết quả looks like information action sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý detail is open trong luồng kiểm thử.
     * @param previousState giá trị previous state được truyền vào
     * @param previousUrl giá trị previous url được truyền vào
     * @return kết quả detail is open sau khi xử lý
     */
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
     * Thực hiện xử lý require visible detail surface trong luồng kiểm thử.
     * @return kết quả require visible detail surface sau khi xử lý
     */
    private WebElement requireVisibleDetailSurface() {
        return visibleDetailSurfaces().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Khong tim thay man chi tiet nguoi dung dang hien thi."));
    }

    /**
     * Kiểm tra điều kiện has detail text.
     * @param text nội dung cần xử lý
     * @return kết quả has detail text sau khi xử lý
     */
    private boolean hasDetailText(String text) {
        String normalized = TextNormalizer.normalize(text);
        return DETAIL_TEXT_MARKERS.stream().anyMatch(normalized::contains);
    }

    /**
     * Kích hoạt  trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     */
    private void click(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    /**
     * Cuộn into view trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     */
    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'center'});", element);
    }

    /**
     * Kiểm tra điều kiện is displayed.
     * @param locator locator xác định phần tử
     * @return kết quả is displayed sau khi xử lý
     */
    private boolean isDisplayed(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    /**
     * Thực hiện xử lý document is ready trong luồng kiểm thử.
     * @return kết quả document is ready sau khi xử lý
     */
    private boolean documentIsReady() {
        Object state = ((JavascriptExecutor) driver).executeScript("return document.readyState");
        return "complete".equals(state);
    }

    /**
     * Trả về current route contains từ trạng thái hiện tại.
     * @param route giá trị route được truyền vào
     * @return kết quả current route contains sau khi xử lý
     */
    private boolean currentRouteContains(String route) {
        return driver.getCurrentUrl().contains(route);
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
     * Thực hiện xử lý body has no data message trong luồng kiểm thử.
     * @return kết quả body has no data message sau khi xử lý
     */
    private boolean bodyHasNoDataMessage() {
        String normalized = TextNormalizer.normalize(bodyText());
        return normalized.contains("khong co du lieu")
                || normalized.contains("no data")
                || normalized.contains("khong tim thay");
    }

    /**
     * Thực hiện xử lý page state trong luồng kiểm thử.
     * @return kết quả page state sau khi xử lý
     */
    private String pageState() {
        return driver.getCurrentUrl() + "|" + bodyText().hashCode();
    }

    /**
     * Thực hiện xử lý body text trong luồng kiểm thử.
     * @return kết quả body text sau khi xử lý
     */
    private String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    /**
     * Thực hiện xử lý short text trong luồng kiểm thử.
     * @param element phần tử cần thao tác
     * @return kết quả short text sau khi xử lý
     */
    private String shortText(WebElement element) {
        String text = element.getText();
        if (text == null || text.isBlank()) {
            text = String.valueOf(element.getAttribute("outerHTML"));
        }
        text = text.replaceAll("\\s+", " ").trim();
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }
}
