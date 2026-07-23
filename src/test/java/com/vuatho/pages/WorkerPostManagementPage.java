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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Page Object cho menu Quản lí bài đăng của thợ. */
public class WorkerPostManagementPage {
    public static final String ROUTE = "/vuatho/profile-post";

    private static final By TABS = By.cssSelector(
            "button[role='tab'][data-slot='tab'][data-key]");
    private static final By POST_CARDS = By.cssSelector("div.p-5.rounded-md.bg-white");
    private static final By WORKER_LINK = By.cssSelector(
            "a[target='_blank'][href^='/vuatho/worker?id=']");
    private static final By MEDIA_BUTTON = By.cssSelector(
            "button.relative.size-24");
    private static final By PAGINATION = By.cssSelector(
            "nav[aria-label='pagination navigation'][data-slot='base']");
    private static final By DIALOG = By.cssSelector(
            "section[role='dialog'][aria-modal='true'][data-open='true'],"
                    + "[role='dialog'][aria-modal='true']");
    private static final Pattern TOTAL = Pattern.compile(
            "Tổng\\s+hiển\\s+thị\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MEDIA_TOTAL = Pattern.compile(
            "(\\d+)\\s*ảnh\\s*,\\s*(\\d+)\\s*video", Pattern.CASE_INSENSITIVE);
    private static final Pattern MEDIA_COUNTER = Pattern.compile("(\\d+)/(\\d+)");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WorkerPostManagementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.wait.pollingEvery(Duration.ofMillis(250));
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    public WorkerPostManagementPage openPendingDirectly() {
        driver.get(TestConfig.baseUrl().replaceAll("/+$", "") + ROUTE + "?tab=pending");
        return waitUntilLoaded();
    }

    public WorkerPostManagementPage waitUntilLoaded() {
        wait.until(d -> d.getCurrentUrl().contains(ROUTE));
        wait.until(d -> visibleTabs().size() == Status.values().length);
        waitForPostResults(Status.PENDING);
        return this;
    }

    public boolean isLoaded() {
        return driver.getCurrentUrl().contains(ROUTE)
                && visibleTabs().size() == Status.values().length
                && selectedStatus().isPresent();
    }

    public List<String> tabLabels() {
        return visibleTabs().stream().map(WebElement::getText).map(String::trim).toList();
    }

    public Optional<Status> selectedStatus() {
        return visibleTabs().stream()
                .filter(tab -> "true".equalsIgnoreCase(tab.getAttribute("aria-selected")))
                .findFirst()
                .flatMap(tab -> Status.fromQueryValue(tab.getAttribute("data-key")));
    }

    public WorkerPostManagementPage selectStatus(Status status) {
        if (selectedStatus().orElse(null) == status
                && driver.getCurrentUrl().contains("tab=" + status.queryValue())) {
            waitForPostResults(status);
            return this;
        }
        List<String> before = visiblePostCards().stream().map(PostCard::text).toList();
        WebElement tab = visibleTabs().stream()
                .filter(item -> status.queryValue().equals(item.getAttribute("data-key")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tab " + status.label()));
        observeElement(tab);
        tab.click();
        wait.until(d -> selectedStatus().orElse(null) == status
                && d.getCurrentUrl().contains("tab=" + status.queryValue()));
        wait.until(d -> {
            List<String> current = visiblePostCards().stream().map(PostCard::text).toList();
            return current.isEmpty() || !current.equals(before);
        });
        waitForPostResults(status);
        return this;
    }

    public int totalPosts() {
        Matcher matcher = TOTAL.matcher(driver.findElement(By.tagName("body")).getText());
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    public List<PostCard> visiblePostCards() {
        List<PostCard> result = new ArrayList<>();
        for (WebElement card : postCardElements()) {
            String text = card.getText().trim();
            WebElement worker = card.findElements(WORKER_LINK).stream()
                    .filter(WebElement::isDisplayed).findFirst().orElse(null);
            if (worker == null) continue;

            Matcher media = MEDIA_TOTAL.matcher(text);
            int images = 0;
            int videos = 0;
            if (media.find()) {
                images = Integer.parseInt(media.group(1));
                videos = Integer.parseInt(media.group(2));
            }
            result.add(new PostCard(
                    worker.getText().trim(),
                    worker.getAttribute("href"),
                    text,
                    images,
                    videos,
                    containsNormalized(text, "ngành nghề"),
                    containsNormalized(text, "thời gian đăng"),
                    containsNormalized(text, "người duyệt"),
                    containsNormalized(text, "ngày duyệt"),
                    containsNormalized(text, "lý do từ chối"),
                    hasButton(card, "Duyệt bài"),
                    hasButton(card, "Từ chối")));
        }
        return result;
    }

    public boolean hasPendingActionsOnEveryCard() {
        List<PostCard> cards = visiblePostCards();
        return !cards.isEmpty() && cards.stream()
                .allMatch(card -> card.hasApproveAction() && card.hasRejectAction());
    }

    public boolean hasNoModerationActions() {
        return visiblePostCards().stream()
                .noneMatch(card -> card.hasApproveAction() || card.hasRejectAction());
    }

    public List<String> visibleWorkerHrefs() {
        return visiblePostCards().stream().map(PostCard::workerHref).toList();
    }

    public String firstWorkerHref() {
        return visiblePostCards().stream().map(PostCard::workerHref)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Danh sách không có link hồ sơ thợ."));
    }

    public String firstPendingPostMarker() {
        WebElement card = postCardElements().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Tab Chờ duyệt không còn bài để chạy mutation."));
        Matcher timestamp = Pattern.compile(
                "\\b\\d{2}-\\d{2}-\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\b")
                .matcher(card.getText());
        if (!timestamp.find()) {
            throw new IllegalStateException(
                    "Không lấy được timestamp định danh của bài Chờ duyệt đầu tiên.");
        }
        return timestamp.group();
    }

    public int activePage() {
        WebElement pagination = visiblePagination();
        if (pagination != null) {
            String active = pagination.getAttribute("data-active-page");
            if (active != null && active.matches("\\d+")) {
                return Integer.parseInt(active);
            }
        }
        return driver.findElements(By.cssSelector("[role='button'][aria-current='true'],"
                        + "[role='button'][aria-current='page']")).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> text.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(1);
    }

    public int lastVisiblePageNumber() {
        WebElement pagination = visiblePagination();
        if (pagination != null) {
            String total = pagination.getAttribute("data-total");
            if (total != null && total.matches("\\d+")) {
                return Integer.parseInt(total);
            }
        }
        return paginationButtons().stream()
                .map(WebElement::getText).map(String::trim)
                .filter(text -> text.matches("\\d+"))
                .mapToInt(Integer::parseInt).max().orElse(1);
    }

    public boolean previousPageDisabled() {
        WebElement previous = paginationControl("previous page button");
        return previous == null || isDisabled(previous);
    }

    public boolean nextPageDisabled() {
        WebElement next = paginationControl("next page button");
        return next == null || isDisabled(next);
    }

    public WorkerPostManagementPage goToPage(int page) {
        List<String> before = visibleWorkerHrefs();
        WebElement target = paginationButtons().stream()
                .filter(item -> ("pagination item " + page)
                        .equalsIgnoreCase(item.getAttribute("aria-label")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy nút trang " + page));
        observeElement(target);
        target.click();
        wait.until(d -> {
            List<String> current = visibleWorkerHrefs();
            return activePage() == page && !current.isEmpty() && !current.equals(before);
        });
        scrollToPaginationAndObserve();
        return this;
    }

    public WorkerPostManagementPage goToNextPage() {
        return changePageWithControl("next page button", activePage() + 1);
    }

    public WorkerPostManagementPage goToPreviousPage() {
        return changePageWithControl("previous page button", activePage() - 1);
    }

    public WorkerPostManagementPage resetList() {
        List<WebElement> resetCandidates = driver.findElements(By.cssSelector(
                "button[type='button'][title='Reset']"));
        WebElement reset = resetCandidates.stream().filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy nút Reset."));
        observeElement(reset);
        reset.click();
        wait.until(d -> activePage() == 1 && !visiblePostCards().isEmpty());
        scrollToPaginationAndObserve();
        return this;
    }

    public WorkerProfileNavigation clickFirstWorkerProfileLink() {
        WebElement link = postCardElements().stream()
                .flatMap(card -> card.findElements(WORKER_LINK).stream())
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Danh sách không có link hồ sơ thợ."));
        String href = link.getAttribute("href");
        Set<String> beforeHandles = driver.getWindowHandles();
        observeElement(link);
        link.click();
        wait.until(d -> d.getWindowHandles().size() > beforeHandles.size()
                || d.getCurrentUrl().contains("/vuatho/worker?id="));
        Set<String> afterHandles = driver.getWindowHandles();
        afterHandles.stream().filter(handle -> !beforeHandles.contains(handle))
                .findFirst().ifPresent(driver.switchTo()::window);
        wait.until(d -> d.getCurrentUrl().contains("/vuatho/worker?id="));
        new Actions(driver).pause(Duration.ofSeconds(2)).perform();
        return new WorkerProfileNavigation(href, driver.getCurrentUrl());
    }

    public WorkerPostManagementPage openFirstPostWithMultipleMedia() {
        WebElement card = postCardElements().stream()
                .filter(item -> {
                    Matcher matcher = MEDIA_TOTAL.matcher(item.getText());
                    return matcher.find()
                            && Integer.parseInt(matcher.group(1))
                            + Integer.parseInt(matcher.group(2)) > 1;
                })
                .filter(item -> !item.findElements(MEDIA_BUTTON).isEmpty())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Trang hiện tại không có bài đăng với nhiều media."));
        WebElement media = card.findElements(MEDIA_BUTTON).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thumbnail media."));
        media.click();
        wait.until(d -> mediaDialogIsStable());
        return this;
    }

    public WorkerPostManagementPage openOverflowMediaThumbnail() {
        WebElement overflow = postCardElements().stream()
                .flatMap(card -> card.findElements(By.xpath(
                        ".//button[.//div[starts-with(normalize-space(.), '+')]]")).stream())
                .filter(WebElement::isDisplayed)
                .filter(button -> button.getText().trim().matches("\\+\\d+"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Trang hiện tại không có thumbnail +N."));
        overflow.click();
        wait.until(d -> mediaDialogIsStable());
        return this;
    }

    public WorkerPostManagementPage openFirstVideoPost() {
        WebElement card = postCardElements().stream()
                .filter(item -> {
                    Matcher matcher = MEDIA_TOTAL.matcher(item.getText());
                    return matcher.find() && Integer.parseInt(matcher.group(2)) > 0;
                })
                .filter(item -> !item.findElements(By.xpath(".//button[.//video]")).isEmpty())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Trang hiện tại không có bài đăng chứa video."));
        WebElement media = card.findElements(By.xpath(".//button[.//video]")).stream()
                .filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Bài có video nhưng không có thumbnail."));
        media.click();
        wait.until(d -> mediaDialogIsStable());
        return this;
    }

    public WorkerPostManagementPage openFirstPostContainingMediaAcrossPages(
            Status status, MediaType mediaType) {
        selectStatus(status);
        int lastPage = Math.max(1, lastVisiblePageNumber());

        for (int page = activePage(); page <= lastPage; page++) {
            WebElement card = postCardElements().stream()
                    .filter(item -> {
                        Matcher matcher = MEDIA_TOTAL.matcher(item.getText());
                        if (!matcher.find()) return false;
                        int count = mediaType == MediaType.IMAGE
                                ? Integer.parseInt(matcher.group(1))
                                : Integer.parseInt(matcher.group(2));
                        return count > 0;
                    })
                    .filter(item -> item.findElements(MEDIA_BUTTON).stream()
                            .anyMatch(WebElement::isDisplayed))
                    .findFirst()
                    .orElse(null);

            if (card != null) {
                WebElement thumbnail = card.findElements(MEDIA_BUTTON).stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElseThrow();
                observeElement(thumbnail);
                thumbnail.click();
                wait.until(d -> mediaDialogIsStable());
                return this;
            }

            if (page < lastPage) {
                goToNextPage();
            }
        }

        throw new IllegalStateException(
                "Không tìm thấy bài có " + mediaType.label()
                        + " trong tab " + status.label()
                        + " sau khi kiểm tra " + lastPage + " trang.");
    }

    public boolean isMediaDialogOpen() {
        return visibleDialog() != null;
    }

    public String mediaCounter() {
        WebElement dialog = requiredDialog();
        return dialog.findElements(By.cssSelector("span,div")).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(value -> MEDIA_COUNTER.matcher(value).matches())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Modal không hiển thị bộ đếm media."));
    }

    public boolean mediaDialogHasPostInformation() {
        String text = requiredDialog().getText();
        return containsNormalized(text, "ngành nghề")
                && containsNormalized(text, "thời gian đăng");
    }

    public Set<String> mediaControlTitles() {
        Set<String> titles = new LinkedHashSet<>();
        for (WebElement button : requiredDialog().findElements(By.tagName("button"))) {
            if (!button.isDisplayed()) continue;
            String title = button.getAttribute("title");
            if (title != null && !title.isBlank()) titles.add(title.trim());
        }
        return titles;
    }

    public boolean mediaDialogHasPendingActions() {
        WebElement dialog = requiredDialog();
        return hasButton(dialog, "Duyệt bài") && hasButton(dialog, "Từ chối");
    }

    public ImageState navigateToImage() {
        int total = mediaCounterTotal();
        for (int attempt = 0; attempt < total; attempt++) {
            WebElement image = requiredDialog()
                    .findElements(By.cssSelector("img[alt='profile-post'][draggable='false']"))
                    .stream().findFirst().orElse(null);
            if (image != null) {
                return new ImageState(
                        Optional.ofNullable(image.getAttribute("src")).orElse(""),
                        mediaCounter());
            }
            String before = mediaCounter();
            requiredDialog().findElement(By.cssSelector("button.absolute.right-4")).click();
            wait.until(d -> !mediaCounter().equals(before));
        }
        throw new IllegalStateException("Đã duyệt hết media nhưng không tìm thấy ảnh.");
    }

    public VideoState navigateToVideo() {
        int total = mediaCounterTotal();
        for (int attempt = 0; attempt < total; attempt++) {
            WebElement video = requiredDialog().findElements(By.tagName("video")).stream()
                    .findFirst().orElse(null);
            if (video != null) {
                String source = Optional.ofNullable(video.getAttribute("src")).orElse("");
                if (source.isBlank()) {
                    source = video.findElements(By.tagName("source")).stream()
                            .map(item -> Optional.ofNullable(item.getAttribute("src")).orElse(""))
                            .filter(value -> !value.isBlank())
                            .findFirst().orElse("");
                }
                boolean controls = video.getAttribute("controls") != null;
                ((JavascriptExecutor) driver).executeScript("arguments[0].pause();", video);
                boolean paused = Boolean.TRUE.equals(((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].paused;", video));
                return new VideoState(source, controls, paused, mediaCounter());
            }
            String before = mediaCounter();
            requiredDialog().findElement(By.cssSelector("button.absolute.right-4")).click();
            wait.until(d -> !mediaCounter().equals(before));
        }
        throw new IllegalStateException("Đã duyệt hết media nhưng không tìm thấy video.");
    }

    public MediaNavigationResult exerciseMediaNavigation() {
        WebElement dialog = requiredDialog();
        String initialCounter = mediaCounter();
        String initialSource = currentMediaSource();
        WebElement next = dialog.findElements(By.cssSelector("button.absolute.right-4")).stream()
                .filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy nút media tiếp theo."));
        next.click();
        wait.until(d -> !mediaCounter().equals(initialCounter)
                && !currentMediaSource().equals(initialSource));
        String nextCounter = mediaCounter();

        WebElement previous = requiredDialog()
                .findElement(By.cssSelector("button.absolute.left-4"));
        previous.click();
        wait.until(d -> mediaCounter().equals(initialCounter));
        return new MediaNavigationResult(initialCounter, nextCounter, mediaCounter());
    }

    public ImageTransformResult exerciseImageTransforms() {
        String initial = currentImageTransform();
        clickMediaControl("Phóng to");
        wait.until(d -> !currentImageTransform().equals(initial));
        String zoomed = currentImageTransform();

        clickMediaControl("Thu nhỏ");
        wait.until(d -> currentImageTransform().equals(initial));
        String zoomRestored = currentImageTransform();

        clickMediaControl("Xoay phải");
        wait.until(d -> !currentImageTransform().equals(initial));
        String rotated = currentImageTransform();

        clickMediaControl("Xoay trái");
        wait.until(d -> currentImageTransform().equals(initial));
        return new ImageTransformResult(
                initial, zoomed, zoomRestored, rotated, currentImageTransform());
    }

    public WorkerPostManagementPage closeMediaDialog() {
        if (visibleDialog() == null) return this;
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        wait.until(d -> visibleDialog() == null);
        return this;
    }

    public WorkerPostManagementPage closeMediaDialogWithButton() {
        WebElement dialog = requiredDialog();
        WebElement close = dialog.findElements(By.cssSelector(
                        "button.absolute.top-3.right-3, button.absolute.top-4.right-4"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseGet(() -> dialog.findElements(By.tagName("button")).stream()
                        .filter(WebElement::isDisplayed)
                        .filter(button -> button.getText().isBlank())
                        .reduce((first, second) -> second)
                        .orElseThrow(() -> new IllegalStateException(
                                "Không tìm thấy nút X đóng modal.")));
        close.click();
        wait.until(d -> visibleDialog() == null);
        return this;
    }

    public WorkerPostManagementPage openFirstRejectDialog() {
        WebElement card = postCardElements().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Không có bài Chờ duyệt."));
        clickCardButton(card, "Từ chối");
        wait.until(d -> rejectDialog() != null);
        return this;
    }

    public boolean rejectDialogIsOpen() {
        return rejectDialog() != null;
    }

    public boolean rejectReasonIsRequired() {
        WebElement dialog = requiredRejectDialog();
        WebElement textarea = dialog.findElements(By.tagName("textarea")).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null);
        if (textarea == null) return false;
        String text = normalized(dialog.getText());
        String placeholder = normalized(textarea.getAttribute("placeholder"));
        return text.contains("ly do tu choi")
                && (dialog.getText().contains("*")
                || textarea.getAttribute("required") != null)
                && placeholder.contains("nhap ly do tu choi");
    }

    public WorkerPostManagementPage submitBlankRejectReason() {
        WebElement dialog = requiredRejectDialog();
        WebElement textarea = dialog.findElement(By.tagName("textarea"));
        textarea.clear();
        clickDialogButton(dialog, "Từ chối");
        wait.until(d -> rejectDialog() != null);
        return this;
    }

    public WorkerPostManagementPage cancelRejectDialog() {
        clickDialogButton(requiredRejectDialog(), "Hủy");
        wait.until(d -> rejectDialog() == null);
        return this;
    }

    public boolean cardsHaveValidTimestampAndMediaSummary() {
        Pattern timestamp = Pattern.compile(
                "\\b\\d{2}-\\d{2}-\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\b");
        for (WebElement card : postCardElements()) {
            String text = card.getText();
            if (!timestamp.matcher(text).find()) return false;
            boolean hasMediaElement = !card.findElements(MEDIA_BUTTON).isEmpty();
            Matcher summary = MEDIA_TOTAL.matcher(text);
            if (hasMediaElement && (!summary.find()
                    || Integer.parseInt(summary.group(1))
                    + Integer.parseInt(summary.group(2)) <= 0)) {
                return false;
            }
        }
        return !postCardElements().isEmpty();
    }

    public boolean approvePostByMarker(String marker) {
        int beforeTotal = totalPosts();
        WebElement card = postCardByMarker(marker);
        clickCardButton(card, "Duyệt bài");
        wait.until(d -> totalPosts() == beforeTotal - 1
                && visiblePostCards().stream().noneMatch(post -> post.text().contains(marker)));
        selectStatus(Status.APPROVED);
        return visiblePostCards().stream().anyMatch(post -> post.text().contains(marker));
    }

    public boolean rejectPostByMarker(String marker, String reason) {
        int beforeTotal = totalPosts();
        WebElement card = postCardByMarker(marker);
        clickCardButton(card, "Từ chối");
        wait.until(d -> rejectDialog() != null);
        WebElement dialog = requiredRejectDialog();
        dialog.findElement(By.tagName("textarea")).sendKeys(reason);
        clickDialogButton(dialog, "Từ chối");
        wait.until(d -> rejectDialog() == null && totalPosts() == beforeTotal - 1);
        selectStatus(Status.REJECTED);
        return visiblePostCards().stream()
                .anyMatch(post -> post.text().contains(marker)
                        && post.text().contains(reason));
    }

    private void clickMediaControl(String expectedTitle) {
        WebElement button = requiredDialog().findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> normalized(item.getAttribute("title"))
                        .equals(normalized(expectedTitle)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy control " + expectedTitle));
        button.click();
    }

    private WorkerPostManagementPage changePageWithControl(String label, int expectedPage) {
        List<String> before = visibleWorkerHrefs();
        WebElement control = paginationControl(label);
        if (control == null || isDisabled(control)) {
            throw new IllegalStateException("Control phân trang không khả dụng: " + label);
        }
        observeElement(control);
        control.click();
        wait.until(d -> activePage() == expectedPage
                && !visibleWorkerHrefs().isEmpty()
                && !visibleWorkerHrefs().equals(before));
        scrollToPaginationAndObserve();
        return this;
    }

    private void scrollToPaginationAndObserve() {
        WebElement pagination = wait.until(d -> visiblePagination());
        observeElement(pagination);
    }

    private void observeElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("""
                arguments[0].scrollIntoView({
                  behavior: 'smooth',
                  block: 'center',
                  inline: 'nearest'
                });
                """, element);
        new Actions(driver).pause(Duration.ofSeconds(2)).perform();
    }

    private int mediaCounterTotal() {
        Matcher matcher = MEDIA_COUNTER.matcher(mediaCounter());
        if (!matcher.matches()) throw new IllegalStateException("Bộ đếm media không hợp lệ.");
        return Integer.parseInt(matcher.group(2));
    }

    private WebElement postCardByMarker(String marker) {
        return postCardElements().stream()
                .filter(card -> card.getText().contains(marker))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy bài test có marker: " + marker));
    }

    private void clickCardButton(WebElement card, String label) {
        WebElement button = card.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> normalized(item.getText()).equals(normalized(label)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Card không có nút " + label));
        observeElement(button);
        button.click();
    }

    private WebElement rejectDialog() {
        for (WebElement dialog : driver.findElements(DIALOG)) {
            try {
                if (dialog.isDisplayed()
                        && normalized(dialog.getText()).contains("tu choi bai dang")) {
                    return dialog;
                }
            } catch (StaleElementReferenceException ignored) {
                // Dialog đang animation.
            }
        }
        return null;
    }

    private WebElement requiredRejectDialog() {
        WebElement dialog = rejectDialog();
        if (dialog == null) throw new IllegalStateException("Dialog Từ chối chưa mở.");
        return dialog;
    }

    private void clickDialogButton(WebElement dialog, String label) {
        WebElement button = dialog.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(item -> normalized(item.getText()).equals(normalized(label)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Dialog không có nút " + label));
        button.click();
    }

    private String currentImageTransform() {
        WebElement image = currentImage();
        return String.valueOf(((JavascriptExecutor) driver)
                .executeScript("return arguments[0].style.transform || '';", image));
    }

    private String currentMediaSource() {
        WebElement image = currentImage();
        return Optional.ofNullable(image.getAttribute("src")).orElse("");
    }

    private WebElement currentImage() {
        return requiredDialog()
                .findElements(By.cssSelector("img[alt='profile-post'][draggable='false']"))
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Modal không có ảnh đang hiển thị."));
    }

    private WebElement requiredDialog() {
        WebElement dialog = visibleDialog();
        if (dialog == null) throw new IllegalStateException("Modal media chưa mở.");
        return dialog;
    }

    private WebElement visibleDialog() {
        for (WebElement dialog : driver.findElements(DIALOG)) {
            try {
                if (dialog.isDisplayed()) return dialog;
            } catch (StaleElementReferenceException ignored) {
                // Animation của modal vừa thay node; vòng poll kế tiếp sẽ lấy node mới.
            }
        }
        return null;
    }

    private boolean mediaDialogIsStable() {
        WebElement dialog = visibleDialog();
        if (dialog == null) return false;
        try {
            boolean hasCounter = dialog.findElements(By.cssSelector("span,div")).stream()
                    .map(WebElement::getText)
                    .map(String::trim)
                    .anyMatch(value -> MEDIA_COUNTER.matcher(value).matches());
            boolean hasMedia = !dialog.findElements(By.cssSelector(
                    "img[alt='profile-post'][draggable='false'], video")).isEmpty();
            return hasCounter && hasMedia;
        } catch (StaleElementReferenceException ignored) {
            return false;
        }
    }

    private void waitForPostResults(Status expectedStatus) {
        wait.until(d -> {
            Matcher totalMatcher = TOTAL.matcher(
                    driver.findElement(By.tagName("body")).getText());
            if (!totalMatcher.find()) return false;
            int total = Integer.parseInt(totalMatcher.group(1));
            List<PostCard> cards = visiblePostCards();
            if (total == 0) return cards.isEmpty();
            if (cards.size() < Math.min(total, 5)) return false;
            return switch (expectedStatus) {
                case PENDING -> cards.stream()
                        .allMatch(card -> card.hasApproveAction() && card.hasRejectAction());
                case APPROVED, REJECTED -> cards.stream()
                        .allMatch(card -> card.hasReviewer() && card.hasReviewedAt()
                                && !card.hasApproveAction() && !card.hasRejectAction());
                case DELETED -> cards.stream()
                        .noneMatch(card -> card.hasApproveAction() || card.hasRejectAction());
            };
        });
    }

    private List<WebElement> visibleTabs() {
        return driver.findElements(TABS).stream().filter(WebElement::isDisplayed).toList();
    }

    private List<WebElement> postCardElements() {
        return driver.findElements(POST_CARDS).stream()
                .filter(WebElement::isDisplayed)
                .filter(card -> !card.findElements(WORKER_LINK).isEmpty())
                .toList();
    }

    private List<WebElement> paginationButtons() {
        WebElement pagination = visiblePagination();
        if (pagination == null) return List.of();
        return pagination.findElements(By.cssSelector(
                        "[role='button'][data-slot='prev'],"
                                + "[role='button'][data-slot='item'],"
                                + "[role='button'][data-slot='next']")).stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    private WebElement paginationControl(String label) {
        return paginationButtons().stream()
                .filter(item -> label.equalsIgnoreCase(item.getAttribute("aria-label")))
                .findFirst().orElse(null);
    }

    private WebElement visiblePagination() {
        return driver.findElements(PAGINATION).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null);
    }

    private static boolean isDisabled(WebElement element) {
        return !element.isEnabled()
                || element.getAttribute("disabled") != null
                || "true".equalsIgnoreCase(element.getAttribute("aria-disabled"));
    }

    private static boolean hasButton(WebElement root, String label) {
        return root.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(button -> normalized(button.getText()).equals(normalized(label)));
    }

    private static boolean containsNormalized(String value, String expected) {
        return normalized(value).contains(normalized(expected));
    }

    private static String normalized(String value) {
        return TextNormalizer.normalize(value == null ? "" : value);
    }

    public enum Status {
        PENDING("Chờ duyệt", "pending"),
        APPROVED("Đã duyệt", "approve"),
        REJECTED("Từ chối", "reject"),
        DELETED("Đã xóa", "delete");

        private final String label;
        private final String queryValue;

        Status(String label, String queryValue) {
            this.label = label;
            this.queryValue = queryValue;
        }

        public String label() {
            return label;
        }

        public String queryValue() {
            return queryValue;
        }

        public static Optional<Status> fromLabel(String label) {
            String actual = normalized(label);
            for (Status status : values()) {
                if (normalized(status.label).equals(actual)) return Optional.of(status);
            }
            return Optional.empty();
        }

        public static Optional<Status> fromQueryValue(String queryValue) {
            for (Status status : values()) {
                if (status.queryValue.equals(queryValue)) return Optional.of(status);
            }
            return Optional.empty();
        }
    }

    public enum MediaType {
        IMAGE("ảnh"),
        VIDEO("video");

        private final String label;

        MediaType(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public record PostCard(
            String workerName,
            String workerHref,
            String text,
            int imageCount,
            int videoCount,
            boolean hasOccupation,
            boolean hasPostedAt,
            boolean hasReviewer,
            boolean hasReviewedAt,
            boolean hasRejectReason,
            boolean hasApproveAction,
            boolean hasRejectAction) {
    }

    public record MediaNavigationResult(
            String initialCounter,
            String nextCounter,
            String previousCounter) {
    }

    public record ImageTransformResult(
            String initial,
            String zoomed,
            String zoomRestored,
            String rotated,
            String rotationRestored) {
    }

    public record ImageState(
            String source,
            String counter) {
    }

    public record VideoState(
            String source,
            boolean controls,
            boolean paused,
            String counter) {
    }

    public record WorkerProfileNavigation(
            String expectedHref,
            String actualUrl) {
    }
}
