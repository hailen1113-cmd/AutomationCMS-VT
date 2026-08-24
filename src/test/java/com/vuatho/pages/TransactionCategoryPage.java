package com.vuatho.pages;

import com.vuatho.utils.Waits;
import com.vuatho.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Thao tác dùng chung cho tám nhóm chuyên biệt của màn hình Lịch sử giao dịch. */
public class TransactionCategoryPage extends UniformUiPage {
    private static final By GRID = By.cssSelector(
            "[role='grid'][aria-label='Table about Transaction Management']");
    private static final By DATA_ROWS = By.cssSelector("tbody tr");
    private static final Pattern PHONE = Pattern.compile("\\+?[0-9]{9,12}");
    private static final DateTimeFormatter CHART_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern CHART_RANGE = Pattern.compile(
            "(\\d{2}/\\d{2}/\\d{4})\\s*→\\s*(\\d{2}/\\d{2}/\\d{4})");

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

    public DropdownFocusSnapshot closeDropdownByClickingOutside() {
        WebElement trigger = groupButton(category.label());
        click(trigger, "Mở dropdown để kiểm tra click ra ngoài");
        String controls = groupButton(category.label()).getAttribute("aria-controls");
        wait.until(d -> d.findElements(By.id(controls)).stream().anyMatch(WebElement::isDisplayed));
        String beforeUrl = currentUrl();
        String beforeText = activeGroupText();
        new Actions(driver).moveToElement(visible(By.tagName("main")), 5, 5).click().perform();
        boolean closed = wait.until(d -> d.findElements(By.id(controls)).stream()
                .noneMatch(WebElement::isDisplayed));
        WebElement active = driver.switchTo().activeElement();
        return new DropdownFocusSnapshot(beforeUrl, currentUrl(), beforeText, activeGroupText(),
                closed, active.getTagName(), active.getAttribute("id"));
    }

    public DropdownKeyboardSnapshot selectNextSubtypeWithKeyboard() {
        WebElement trigger = groupButton(category.label());
        String triggerId = trigger.getAttribute("id");
        click(trigger, "Mở dropdown để chọn loại kế tiếp bằng bàn phím");
        String controls = groupButton(category.label()).getAttribute("aria-controls");
        WebElement menu = wait.until(d -> d.findElements(By.id(controls)).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null));
        WebElement selected = menu.findElements(By.cssSelector(
                        "[role='menuitemradio'][aria-checked='true']"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElseThrow();
        String beforeKey = selected.getAttribute("data-key");
        selected.sendKeys(Keys.ARROW_DOWN);
        driver.switchTo().activeElement().sendKeys(Keys.ENTER);
        waitForTable();
        boolean closed = wait.until(d -> d.findElements(By.id(controls)).stream()
                .noneMatch(WebElement::isDisplayed));
        WebElement focused = driver.switchTo().activeElement();
        return new DropdownKeyboardSnapshot(beforeKey, currentUrl(), activeGroupText(), closed,
                triggerId, focused.getAttribute("id"));
    }

    public DropdownReloadSnapshot refreshCurrentSubtype() {
        String beforeUrl = currentUrl();
        String beforeText = activeGroupText();
        driver.navigate().refresh();
        waitForTable();
        return new DropdownReloadSnapshot(beforeUrl, currentUrl(), beforeText, activeGroupText(),
                dropdownSemantics().options());
    }

    public DropdownInvalidTypeSnapshot openInvalidSubtype(int type) {
        openRoute("/vuatho/transaction?tab=" + category.subtypes().get(0).tab() + "&type=" + type);
        waitForTable();
        return new DropdownInvalidTypeSnapshot(type, currentUrl(), activeGroupText(),
                dropdownSemantics().options());
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

    public CostWalletOverviewSnapshot costWalletDepositOverview() {
        String text = overviewBlockText("Tổng tiền nạp vào Ví chi phí",
                List.of("Paypal", "MoMo", "OnePay", "Banking"));
        Map<String, BigDecimal> gateways = new LinkedHashMap<>();
        for (String gateway : List.of("Paypal", "MoMo", "OnePay", "Banking")) {
            gateways.put(gateway, moneyNearLabel(text, gateway));
        }
        return new CostWalletOverviewSnapshot(moneyNearLabel(text, "Tổng số tiền"), gateways,
                percentages(overviewBlockProperty("Tổng tiền nạp vào Ví chi phí",
                        List.of("Paypal", "MoMo", "OnePay", "Banking"), "textContent")),
                overviewSectorNames(), text, currentUrl());
    }

    public BankTransferOverviewSnapshot bankTransferDepositOverview() {
        List<String> statuses = List.of("Hoàn thành", "Đang chờ", "Từ chối");
        String amountText = overviewBlockText(
                "Tổng nạp tiền vào số dư nền tảng qua chuyển khoản ngân hàng", statuses);
        String countText = overviewBlockText("Tổng số giao dịch", statuses);
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, BigDecimal> amountPercentages = new LinkedHashMap<>();
        Map<String, BigDecimal> countPercentages = new LinkedHashMap<>();
        for (String status : statuses) {
            amounts.put(status, moneyNearLabel(amountText, status));
            counts.put(status, countNearLabel(countText, status));
            amountPercentages.put(status, percentageNearLabel(amountText, status));
            countPercentages.put(status, percentageNearLabel(countText, status));
        }
        return new BankTransferOverviewSnapshot(amounts, counts, amountPercentages,
                countPercentages, amountText, countText, currentUrl());
    }

    public InsuranceOverviewSnapshot insuranceOverview(Subtype subtype) {
        String amountHeading = insuranceAmountHeading(subtype);
        List<String> statuses = List.of("Hoàn thành", "Đang chờ", "Từ chối");
        String amountText = overviewBlockText(amountHeading, statuses);
        String countText = overviewBlockText("Tổng số giao dịch", statuses);
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, BigDecimal> amountPercentages = new LinkedHashMap<>();
        Map<String, BigDecimal> countPercentages = new LinkedHashMap<>();
        for (String status : statuses) {
            amounts.put(status, moneyNearLabel(amountText, status));
            counts.put(status, countNearLabel(countText, status));
            amountPercentages.put(status, percentageNearLabel(amountText, status));
            countPercentages.put(status, percentageNearLabel(countText, status));
        }
        return new InsuranceOverviewSnapshot(subtype, moneyNearLabel(amountText, "Tổng cộng"),
                countNearLabel(countText, "Tổng cộng"), amounts, counts,
                amountPercentages, countPercentages, amountText, countText, currentUrl());
    }

    public InsurancePieOverviewSnapshot insurancePieOverview(Subtype subtype) {
        FeePieSnapshot amount = pieSnapshot(insuranceAmountHeading(subtype));
        FeePieSnapshot count = pieSnapshot("Tổng số giao dịch");
        return new InsurancePieOverviewSnapshot(amount, count, currentUrl());
    }

    public List<String> visibleInsuranceOverviewHeadings() {
        return visibleElements(By.cssSelector("main h4")).stream()
                .map(this::elementText).map(String::trim)
                .filter(value -> value.equals("Tổng số giao dịch")
                        || value.toLowerCase(Locale.ROOT).contains("vt care"))
                .toList();
    }

    public InsuranceOverviewRefreshSnapshot refreshInsuranceOverview(Subtype subtype) {
        driver.navigate().refresh();
        waitForTable();
        return new InsuranceOverviewRefreshSnapshot(visibleInsuranceOverviewHeadings(),
                insuranceOverview(subtype), currentUrl(), activeGroupText());
    }

    private String insuranceAmountHeading(Subtype subtype) {
        return subtype.type() == 25
                ? "Tổng trừ phí vt care hàng ngày/tháng"
                : "Tổng hoàn phí vt care khi hủy gói";
    }

    /** Đọc hai khối tổng quan dùng chung của các nhóm giao dịch theo trạng thái. */
    public CategoryStatusOverviewSnapshot categoryStatusOverview() {
        List<String> headings = categoryOverviewHeadings();
        if (headings.isEmpty()) {
            throw new IllegalStateException("Thiếu khối tổng quan của " + category.label()
                    + ": " + headings);
        }
        String amountHeading = headings.get(0);
        String countHeading = headings.stream()
                .filter(value -> value.equals("Tổng số giao dịch"))
                .findFirst().orElse("");
        List<String> statuses = List.of("Hoàn thành", "Đang chờ", "Từ chối");
        String amountText = overviewBlockText(amountHeading, statuses);
        String countText = countHeading.isBlank() ? "" : overviewBlockText(countHeading, statuses);
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, BigDecimal> amountPercentages = new LinkedHashMap<>();
        Map<String, BigDecimal> countPercentages = new LinkedHashMap<>();
        for (String status : statuses) {
            amounts.put(status, moneyNearLabel(amountText, status));
            if (!countHeading.isBlank()) {
                counts.put(status, countNearLabel(countText, status));
                countPercentages.put(status, percentageNearLabel(countText, status));
            }
            amountPercentages.put(status, percentageNearLabel(amountText, status));
        }
        return new CategoryStatusOverviewSnapshot(amountHeading, countHeading,
                moneyNearLabel(amountText, "Tổng cộng"),
                countHeading.isBlank() ? null : countNearLabel(countText, "Tổng cộng"), amounts, counts,
                amountPercentages, countPercentages, currentUrl());
    }

    public CategoryStatusPieSnapshot categoryStatusPieOverview() {
        List<String> headings = categoryOverviewHeadings();
        String amountHeading = headings.stream()
                .filter(value -> !value.equals("Tổng số giao dịch"))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Thiếu khối tổng tiền của " + category.label()));
        FeePieSnapshot countChart = headings.contains("Tổng số giao dịch")
                ? pieSnapshot("Tổng số giao dịch") : null;
        return new CategoryStatusPieSnapshot(pieSnapshot(amountHeading), countChart, currentUrl());
    }

    public List<String> categoryOverviewHeadings() {
        return visibleElements(By.cssSelector("main h4")).stream()
                .map(this::elementText).map(String::trim)
                .filter(value -> value.startsWith("Tổng"))
                .limit(2).toList();
    }

    public CategoryStatusRefreshSnapshot refreshCategoryStatusOverview() {
        driver.navigate().refresh();
        waitForTable();
        return new CategoryStatusRefreshSnapshot(categoryOverviewHeadings(),
                categoryStatusOverview(), currentUrl(), activeGroupText());
    }

    public FeeConnectionOverviewSnapshot feeConnectionOverview() {
        String text = overviewBlockText("Tổng doanh thu",
                List.of("Tổng số giao dịch", "Đã thu", "Công nợ", "Doanh thu theo ngày"));
        Matcher dateMatcher = Pattern.compile("\\d{2}/\\d{2}/\\d{4}").matcher(text);
        List<String> dates = new ArrayList<>();
        while (dateMatcher.find()) {
            dates.add(dateMatcher.group());
        }
        Matcher countMatcher = Pattern.compile("Tổng số giao dịch:\\s*([0-9.]+)").matcher(text);
        if (!countMatcher.find()) {
            throw new IllegalStateException("Không đọc được Tổng số giao dịch: " + text);
        }
        int transactionCount = Integer.parseInt(countMatcher.group(1).replace(".", ""));
        return new FeeConnectionOverviewSnapshot(
                moneyNearLabel(text, "Tổng cộng"), transactionCount,
                moneyNearLabel(text, "Đã thu"),
                percentageNearLabel(text, "Đã thu"),
                moneyNearLabel(text, "Công nợ"),
                percentageNearLabel(text, "Công nợ"),
                dates.stream().distinct().toList(),
                text.contains("Tuần này"), text.contains("Tháng này"),
                text.contains("Tuỳ chỉnh"), text, currentUrl());
    }

    public RevenueChartSnapshot selectFeeRevenuePeriod(String period) {
        if (!List.of("Tuần này", "Tháng này", "Tuỳ chỉnh").contains(period)) {
            throw new IllegalArgumentException("Khoảng biểu đồ không hợp lệ: " + period);
        }
        WebElement root = revenueChartRoot();
        WebElement button = root.findElements(By.xpath(
                        ".//button[normalize-space()=" + xpathLiteral(period) + "]"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy nút " + period));
        click(button, "Chọn biểu đồ Doanh thu theo ngày: " + period);
        wait.until(d -> {
            WebElement freshRoot = revenueChartRoot();
            return freshRoot.findElements(By.xpath(
                            ".//button[normalize-space()=" + xpathLiteral(period) + "]"))
                    .stream().filter(WebElement::isDisplayed)
                    .anyMatch(item -> String.valueOf(item.getAttribute("class"))
                            .contains("bg-primary-blue"));
        });
        settle(350);
        return revenueChartSnapshot();
    }

    public RevenueChartSnapshot openFeeRevenueCustomCalendar() {
        selectFeeRevenuePeriod("Tuỳ chỉnh");
        WebElement input = visible(By.cssSelector("input[placeholder='Chọn khoảng ngày']"));
        click(input, "Mở lịch Tuỳ chỉnh của biểu đồ Doanh thu theo ngày");
        wait.until(d -> visibleElements(By.cssSelector(".react-datepicker")).size() == 1);
        return revenueChartSnapshot();
    }

    public RevenueChartSnapshot selectFeeRevenueCustomRange(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end) || end.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Khoảng ngày chart phải hợp lệ và không vượt hôm nay");
        }
        openFeeRevenueCustomCalendar();
        moveRevenueCalendarTo(YearMonth.from(start));
        chooseRevenueCalendarDay(start);
        if (!YearMonth.from(start).equals(YearMonth.from(end))) {
            moveRevenueCalendarTo(YearMonth.from(end));
        }
        chooseRevenueCalendarDay(end);
        String expected = start.format(CHART_DATE) + " - " + end.format(CHART_DATE);
        wait.until(d -> d.findElements(By.cssSelector("input[placeholder='Chọn khoảng ngày']"))
                .stream().filter(WebElement::isDisplayed)
                .anyMatch(input -> expected.equals(input.getAttribute("value"))));
        settle(450);
        return revenueChartSnapshot();
    }

    public RevenueChartSnapshot feeRevenueChartSnapshot() {
        return revenueChartSnapshot();
    }

    private RevenueChartSnapshot revenueChartSnapshot() {
        WebDriverWait chartWait = Waits.withTimeout(driver, Duration.ofSeconds(12));
        return chartWait.until(d -> readRevenueChartSnapshot());
    }

    private RevenueChartSnapshot readRevenueChartSnapshot() {
        WebElement root = revenueChartRoot();
        String text = elementText(root);
        Matcher range = CHART_RANGE.matcher(text);
        if (!range.find()) {
            throw new IllegalStateException("Không đọc được khoảng Doanh thu theo ngày: " + text);
        }
        String activePeriod = root.findElements(By.tagName("button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(button -> String.valueOf(button.getAttribute("class"))
                        .contains("bg-primary-blue"))
                .map(this::elementText).map(String::trim)
                .filter(List.of("Tuần này", "Tháng này", "Tuỳ chỉnh")::contains)
                .findFirst().orElse("");
        List<WebElement> inputs = root.findElements(By.cssSelector(
                        "input[placeholder='Chọn khoảng ngày']"))
                .stream().filter(WebElement::isDisplayed).toList();
        List<String> ticks = root.findElements(By.cssSelector(
                        ".recharts-xAxis .recharts-cartesian-axis-tick-value"))
                .stream().filter(WebElement::isDisplayed)
                .map(this::elementText).map(String::trim).filter(value -> !value.isBlank()).toList();
        Number barCount = (Number) ((JavascriptExecutor) driver).executeScript(
                "return Array.from(arguments[0].querySelectorAll('.recharts-bar-rectangle path'))"
                        + ".filter(function(element){var rect=element.getBoundingClientRect();"
                        + "return rect.width>0&&rect.height>0;}).length;", root);
        int bars = barCount.intValue();
        int disabledFutureDays = (int) visibleElements(By.cssSelector(
                        ".react-datepicker__day--disabled"))
                .stream().count();
        return new RevenueChartSnapshot(activePeriod,
                LocalDate.parse(range.group(1), CHART_DATE),
                LocalDate.parse(range.group(2), CHART_DATE),
                !inputs.isEmpty(), inputs.isEmpty() ? "" : inputs.get(0).getAttribute("value"),
                ticks, bars, rows().stream().map(TransactionRow::signature).toList(),
                !visibleElements(By.cssSelector(".react-datepicker")).isEmpty(),
                disabledFutureDays, currentUrl());
    }

    private WebElement revenueChartRoot() {
        return visible(By.xpath("//*[normalize-space()='Doanh thu theo ngày']"
                + "/ancestor::div[contains(@class,'col-span-2')][1]"));
    }

    private void moveRevenueCalendarTo(YearMonth target) {
        for (int attempt = 0; attempt < 24; attempt++) {
            WebElement month = visible(By.cssSelector(
                    ".react-datepicker__month[aria-label^='month ']"));
            Matcher yearMonth = Pattern.compile("(\\d{4}-\\d{2})")
                    .matcher(String.valueOf(month.getAttribute("aria-label")));
            if (!yearMonth.find()) {
                throw new IllegalStateException("Không đọc được tháng của lịch chart: "
                        + month.getAttribute("aria-label"));
            }
            YearMonth current = YearMonth.parse(yearMonth.group(1));
            if (current.equals(target)) {
                return;
            }
            String direction = current.isAfter(target) ? "Previous Month" : "Next Month";
            click(visible(By.cssSelector("button[aria-label=" + cssString(direction) + "]")),
                    "Chuyển lịch chart đến " + target);
            settle(120);
        }
        throw new IllegalStateException("Không chuyển được lịch chart đến " + target);
    }

    private void chooseRevenueCalendarDay(LocalDate date) {
        String selector = String.format(".react-datepicker__day--%03d", date.getDayOfMonth())
                + ":not(.react-datepicker__day--outside-month)"
                + ":not(.react-datepicker__day--disabled)";
        click(visible(By.cssSelector(selector)), "Chọn ngày chart " + date.format(CHART_DATE));
        settle(120);
    }

    public FeeWalletLinkOverviewSnapshot feeWalletLinkOverview() {
        List<String> statuses = List.of("Hoàn thành", "Đang chờ", "Từ chối");
        String amountText = overviewBlockText("Tổng phí liên kết ví", statuses);
        String countText = overviewBlockText("Tổng số giao dịch", statuses);
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, BigDecimal> amountPercentages = new LinkedHashMap<>();
        Map<String, BigDecimal> countPercentages = new LinkedHashMap<>();
        for (String status : statuses) {
            amounts.put(status, moneyNearLabel(amountText, status));
            counts.put(status, countNearLabel(countText, status));
            amountPercentages.put(status, percentageNearLabel(amountText, status));
            countPercentages.put(status, percentageNearLabel(countText, status));
        }
        return new FeeWalletLinkOverviewSnapshot(moneyNearLabel(amountText, "Tổng cộng"),
                countNearLabel(countText, "Tổng cộng"), amounts, counts,
                amountPercentages, countPercentages, amountText, countText, currentUrl());
    }

    public FeeMaterialShareOverviewSnapshot feeMaterialShareOverview() {
        String text = overviewBlockText("Tổng phí chia sẻ vật tư",
                List.of("Tổng tiền thu", "Tổng số đơn"));
        return new FeeMaterialShareOverviewSnapshot(
                moneyNearLabel(text, "Tổng tiền thu"),
                countNearLabel(text, "Tổng số đơn"), text, currentUrl());
    }

    public FeePieSnapshot feeConnectionPieOverview() {
        return pieSnapshot("Tổng doanh thu");
    }

    public FeeDualPieSnapshot feeWalletLinkPieOverview() {
        FeePieSnapshot amount = pieSnapshot("Tổng phí liên kết ví");
        FeePieSnapshot count = pieSnapshot("Tổng số giao dịch");
        return new FeeDualPieSnapshot(amount, count, currentUrl());
    }

    public List<String> visibleFeeOverviewHeadings() {
        return visibleElements(By.cssSelector("main h4")).stream()
                .map(this::elementText).map(String::trim)
                .filter(value -> value.startsWith("Tổng ") || value.equals("Doanh thu theo ngày"))
                .toList();
    }

    public FeeOverviewRefreshSnapshot refreshFeeOverview() {
        driver.navigate().refresh();
        waitForTable();
        return new FeeOverviewRefreshSnapshot(visibleFeeOverviewHeadings(),
                currentUrl(), activeGroupText());
    }

    public FeeDebtSnapshot openFeeDebtOverview() {
        By button = By.xpath("//*[normalize-space()='Xem chi tiết công nợ:']"
                + "/following-sibling::button[1]");
        click(visible(button), "Mở chi tiết Công nợ thợ");
        WebElement root = feeDebtRoot();
        wait.until(d -> !elementText(root).contains("Đang tải dữ liệu"));
        return feeDebtSnapshot();
    }

    public FeeDebtSortSnapshot sortFeeDebtByAmountAndDate() {
        feeDebtRoot();
        List<BigDecimal> amountBefore = feeDebtRows().stream().map(FeeDebtRow::debt).toList();
        clickFeeDebtHeader("totalDebt", "Sort Số tiền nợ lần một");
        List<BigDecimal> amountFirst = feeDebtRows().stream().map(FeeDebtRow::debt).toList();
        clickFeeDebtHeader("totalDebt", "Sort Số tiền nợ lần hai");
        List<BigDecimal> amountSecond = feeDebtRows().stream().map(FeeDebtRow::debt).toList();
        clickFeeDebtHeader("latestTransactions", "Sort Ngày nợ lần một");
        List<Integer> debtDaysFirst = feeDebtRows().stream().map(FeeDebtRow::debtDays).toList();
        clickFeeDebtHeader("latestTransactions", "Sort Ngày nợ lần hai");
        List<Integer> debtDaysSecond = feeDebtRows().stream().map(FeeDebtRow::debtDays).toList();
        return new FeeDebtSortSnapshot(amountBefore, amountFirst, amountSecond,
                debtDaysFirst, debtDaysSecond, currentUrl());
    }

    public FeeDebtPaginationSnapshot paginateFeeDebtAndReturn() {
        WebElement root = feeDebtRoot();
        List<String> first = feeDebtRows().stream().map(FeeDebtRow::signature).toList();
        WebElement pageTwo = root.findElements(By.cssSelector(
                        "nav[aria-label='pagination navigation'] [aria-label='pagination item 2']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException("Công nợ thợ không có trang 2"));
        click(pageTwo, "Mở trang 2 Công nợ thợ");
        waitForFeeDebtRowsToChange(first);
        List<String> second = feeDebtRows().stream().map(FeeDebtRow::signature).toList();
        WebElement pageOne = feeDebtRoot().findElements(By.cssSelector(
                        "nav[aria-label='pagination navigation'] [aria-label='pagination item 1']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException("Công nợ thợ không có trang 1"));
        click(pageOne, "Quay lại trang 1 Công nợ thợ");
        waitForFeeDebtRowsToChange(second);
        List<String> restored = feeDebtRows().stream().map(FeeDebtRow::signature).toList();
        return new FeeDebtPaginationSnapshot(first, second, restored, currentUrl());
    }

    public FeeDebtCloseSnapshot closeFeeDebtOverview() {
        WebElement root = feeDebtRoot();
        List<WebElement> headerButtons = root.findElements(By.xpath(
                        ".//h3[normalize-space()='Công nợ thợ']/following-sibling::div//button"))
                .stream().filter(WebElement::isDisplayed).toList();
        if (headerButtons.size() < 2) {
            throw new IllegalStateException("Modal Công nợ thợ thiếu nút đóng");
        }
        click(headerButtons.get(headerButtons.size() - 1), "Đóng modal Công nợ thợ");
        boolean closed = wait.until(d -> visibleElements(By.xpath(
                "//h3[normalize-space()='Công nợ thợ']")).isEmpty());
        return new FeeDebtCloseSnapshot(closed, currentUrl(), activeGroupText());
    }

    public FeeDebtWorkerNavigationSnapshot openDebtWorkerAndReturn() {
        WebElement root = feeDebtRoot();
        WebElement link = root.findElements(By.cssSelector("a[href*='/vuatho/worker?id=']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException("Công nợ thợ không có link hồ sơ"));
        String expectedUrl = link.getAttribute("href");
        String sourceHandle = driver.getWindowHandle();
        Set<String> before = new HashSet<>(driver.getWindowHandles());
        click(link, "Mở hồ sơ thợ từ Công nợ thợ");
        String targetHandle = wait.until(d -> d.getWindowHandles().stream()
                .filter(handle -> !before.contains(handle)).findFirst().orElse(null));
        driver.switchTo().window(targetHandle);
        wait.until(d -> d.getCurrentUrl().contains("/vuatho/worker?id="));
        String actualUrl = currentUrl();
        String targetText = wait.until(d -> {
            String text = elementText(d.findElement(By.tagName("body"))).trim();
            return text.isBlank() ? null : text;
        });
        driver.close();
        driver.switchTo().window(sourceHandle);
        boolean modalRestored = wait.until(d -> !d.findElements(By.xpath(
                        "//h3[normalize-space()='Công nợ thợ']"))
                .stream().filter(WebElement::isDisplayed).toList().isEmpty());
        return new FeeDebtWorkerNavigationSnapshot(expectedUrl, actualUrl, targetText,
                modalRestored, currentUrl());
    }

    public FeeMaterialTotalSnapshot feeMaterialTotalAgainstAllPages() {
        FeeMaterialShareOverviewSnapshot overview = feeMaterialShareOverview();
        int totalPages = visibleElements(By.cssSelector("nav[aria-label='pagination navigation']"))
                .stream().map(nav -> nav.getAttribute("data-total"))
                .filter(value -> value != null && value.matches("\\d+"))
                .mapToInt(Integer::parseInt).findFirst().orElse(1);
        BigDecimal sum = BigDecimal.ZERO;
        int rowCount = 0;
        for (int page = 1; page <= totalPages; page++) {
            if (page > 1) {
                WebElement button = visible(By.cssSelector(
                        "nav[aria-label='pagination navigation'] [aria-label='pagination item "
                                + page + "']"));
                click(button, "Cộng Phí chia sẻ vật tư trang " + page);
                waitForTable();
            }
            List<TransactionRow> current = rows();
            rowCount += current.size();
            sum = sum.add(current.stream().map(row -> row.amount("Số tiền"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return new FeeMaterialTotalSnapshot(overview.totalCollected(), sum, rowCount,
                totalPages, currentUrl());
    }

    public InvalidFeeRouteSnapshot openInvalidFeeRoute(String query) {
        openRoute("/vuatho/transaction?tab=fee&" + query);
        boolean drawer = !visibleElements(By.cssSelector(
                "[aria-label='drawer-Chi tiết giao dịch']")).isEmpty();
        String drawerText = drawer ? elementText(visible(By.cssSelector(
                "[aria-label='drawer-Chi tiết giao dịch']"))) : "";
        return new InvalidFeeRouteSnapshot(currentUrl(), activeGroupText(), drawer,
                drawerText, mainText());
    }

    public MismatchedFeeRouteSnapshot openTransactionIdUnderWrongSubtype(
            Subtype sourceSubtype, Subtype wrongSubtype) {
        open(sourceSubtype);
        DetailSnapshot source = openAndCloseFirstDetail();
        Matcher matcher = Pattern.compile("(?:[?&])id=([^&]+)").matcher(source.openedUrl());
        if (!matcher.find()) {
            throw new IllegalStateException("URL chi tiết không có id: " + source.openedUrl());
        }
        String id = matcher.group(1);
        InvalidFeeRouteSnapshot attempted = openInvalidFeeRoute(
                "type=" + wrongSubtype.type() + "&id=" + id);
        return new MismatchedFeeRouteSnapshot(sourceSubtype, wrongSubtype, id,
                source.drawerText(), attempted);
    }

    private WebElement overviewRoot(String heading) {
        return visible(By.xpath("//main//*[normalize-space()=" + xpathLiteral(heading) + "]"
                + "/ancestor::div[contains(@class,'col-span-2')][1]"));
    }

    @SuppressWarnings("unchecked")
    private FeePieSnapshot pieSnapshot(String heading) {
        wait.until(d -> {
            WebElement root = overviewRoot(heading);
            List<WebElement> visibleCharts = root.findElements(By.cssSelector(".recharts-wrapper")).stream()
                    .filter(WebElement::isDisplayed).toList();
            if (visibleCharts.isEmpty()) {
                return null;
            }
            boolean rendered = visibleCharts.stream().anyMatch(chart ->
                    !chart.findElements(By.cssSelector(
                            ".recharts-pie-sector path[name]")).isEmpty());
            return rendered || elementText(root).matches("(?s).*Tổng cộng\\s*0(?:[.,]0+)?(?:₫)?.*")
                    ? true : null;
        });
        // Recharts vẽ từng sector theo animation; đợi hoàn tất rồi đọc đủ các phần.
        settle(900);
        WebElement root = overviewRoot(heading);
        List<Map<String, Object>> chartData = (List<Map<String, Object>>) ((JavascriptExecutor) driver)
                .executeScript("return Array.from(arguments[0].querySelectorAll('.recharts-wrapper'))"
                        + ".filter(chart=>chart.offsetWidth>0&&chart.offsetHeight>0)"
                        + ".map(chart=>({sectors:Array.from(chart.querySelectorAll("
                        + "'.recharts-pie-sector path[name]')).map(path=>({"
                        + "name:path.getAttribute('name'),fill:path.getAttribute('fill')}))}));", root);
        Map<String, String> sectors = new LinkedHashMap<>();
        for (Map<String, Object> chart : chartData) {
            for (Map<String, Object> sector : (List<Map<String, Object>>) chart.get("sectors")) {
                String name = String.valueOf(sector.get("name"));
                if (!name.isBlank()) {
                    sectors.put(name, String.valueOf(sector.get("fill")));
                }
            }
        }
        return new FeePieSnapshot(chartData.size(), sectors, elementText(root), currentUrl());
    }

    private WebElement feeDebtRoot() {
        return wait.until(d -> d.findElements(By.xpath(
                        "//h3[normalize-space()='Công nợ thợ']"
                                + "/ancestor::div[contains(@class,'min-h-[50vh]')][1]"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElse(null));
    }

    private WebElement feeDebtGrid() {
        return feeDebtRoot().findElements(By.cssSelector(
                        "[role='grid'][aria-label='Table about Debt Management']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy bảng Công nợ thợ"));
    }

    private FeeDebtSnapshot feeDebtSnapshot() {
        WebElement root = feeDebtRoot();
        WebElement grid = feeDebtGrid();
        List<String> headers = grid.findElements(By.cssSelector("thead th")).stream()
                .filter(WebElement::isDisplayed).map(this::elementText).map(String::trim)
                .filter(value -> !value.isBlank()).toList();
        boolean pagination = !root.findElements(By.cssSelector(
                "nav[aria-label='pagination navigation']")).isEmpty();
        return new FeeDebtSnapshot(elementText(root).contains("Công nợ thợ"), headers,
                feeDebtRows(), pagination, currentUrl());
    }

    private List<FeeDebtRow> feeDebtRows() {
        WebElement grid = feeDebtGrid();
        wait.until(d -> !elementText(grid).contains("Đang tải dữ liệu"));
        return grid.findElements(By.cssSelector("tbody tr")).stream()
                .filter(WebElement::isDisplayed)
                .map(this::toFeeDebtRow).filter(row -> row != null).toList();
    }

    private FeeDebtRow toFeeDebtRow(WebElement row) {
        List<WebElement> cells = row.findElements(By.cssSelector("th,td"));
        if (cells.size() < 3) {
            return null;
        }
        String workerText = elementText(cells.get(0)).trim();
        WebElement link = cells.get(0).findElements(By.cssSelector("a[href*='/vuatho/worker?id=']"))
                .stream().findFirst().orElse(null);
        String href = link == null ? "" : link.getAttribute("href");
        Matcher workerId = Pattern.compile("[?&]id=(\\d+)").matcher(href);
        String debtText = elementText(cells.get(1));
        Matcher date = Pattern.compile("(\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2})")
                .matcher(elementText(cells.get(2)));
        Matcher days = Pattern.compile("Đã nợ\\s+(\\d+)\\s+ngày")
                .matcher(elementText(cells.get(2)));
        if (!workerId.find() || debtText.isBlank() || !date.find() || !days.find()) {
            throw new IllegalStateException("Dòng Công nợ thợ không đúng định dạng: "
                    + elementText(row));
        }
        return new FeeDebtRow(workerText, workerId.group(1), href,
                parseLocalizedMoney(debtText), LocalDateTime.parse(date.group(1),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                Integer.parseInt(days.group(1)));
    }

    private BigDecimal parseLocalizedMoney(String text) {
        String value = text.replaceAll("[^0-9.,-]", "");
        int dot = value.lastIndexOf('.');
        int comma = value.lastIndexOf(',');
        int decimal = Math.max(dot, comma);
        if (decimal >= 0 && value.length() - decimal - 1 != 3) {
            String integer = value.substring(0, decimal).replace(".", "").replace(",", "");
            String fraction = value.substring(decimal + 1).replace(".", "").replace(",", "");
            value = integer + "." + fraction;
        } else {
            value = value.replace(".", "").replace(",", "");
        }
        return new BigDecimal(value);
    }

    private void clickFeeDebtHeader(String key, String step) {
        WebElement button = feeDebtGrid().findElements(By.cssSelector(
                        "thead th[data-key='" + key + "'] button"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException("Thiếu cột sort Công nợ: " + key));
        click(button, step);
        settle(800);
        wait.until(d -> !elementText(feeDebtGrid()).contains("Đang tải dữ liệu"));
    }

    private void waitForFeeDebtRowsToChange(List<String> before) {
        settle(350);
        wait.until(d -> {
            List<String> current = feeDebtRows().stream().map(FeeDebtRow::signature).toList();
            return !current.equals(before);
        });
    }

    public ChartTooltipSnapshot hoverChartNearHeading(String heading) {
        By chartLocator = By.xpath("//*[normalize-space()=" + xpathLiteral(heading)
                + "]/following::*[contains(concat(' ',normalize-space(@class),' '),"
                + "' recharts-wrapper ')][1]");
        settle(500);
        String tooltip = "";
        for (int attempt = 0; attempt < 4 && tooltip.isBlank(); attempt++) {
            try {
                tooltip = hoverChartUntilTooltip(visible(chartLocator));
            } catch (StaleElementReferenceException ignored) {
                // Recharts có thể thay SVG khi hover; lấy lại đúng chart theo tiêu đề.
                settle(200);
            }
        }
        return new ChartTooltipSnapshot(1,
                tooltip.isBlank() ? List.of() : List.of(tooltip), currentUrl());
    }

    private String hoverChartUntilTooltip(WebElement chart) {
        List<WebElement> targets = chart.findElements(By.cssSelector(
                        ".recharts-pie-sector path,.recharts-bar-rectangle path,"
                                + ".recharts-dot,.recharts-active-dot"))
                .stream().filter(WebElement::isDisplayed).toList();
        for (WebElement target : targets) {
            new Actions(driver).moveToElement(target).pause(Duration.ofMillis(150)).perform();
            String tooltip = visibleChartTooltip();
            if (!tooltip.isBlank()) {
                return tooltip;
            }
        }

        WebElement surface = chart.findElements(By.cssSelector("svg.recharts-surface"))
                .stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
        if (surface == null) {
            return "";
        }
        int width = Math.max(surface.getRect().getWidth(), 1);
        int height = Math.max(surface.getRect().getHeight(), 1);
        int[] horizontalOffsets = {-width / 3, -width / 6, 0, width / 6, width / 3};
        int[] verticalOffsets = {-height / 4, 0, height / 4};
        for (int y : verticalOffsets) {
            for (int x : horizontalOffsets) {
                new Actions(driver).moveToElement(surface, x, y)
                        .pause(Duration.ofMillis(120)).perform();
                String tooltip = visibleChartTooltip();
                if (!tooltip.isBlank()) {
                    return tooltip;
                }
            }
        }
        return "";
    }

    private String visibleChartTooltip() {
        return visibleElements(By.cssSelector(
                        ".recharts-tooltip-wrapper,[role='tooltip']"))
                .stream().map(this::elementText).map(String::trim)
                .filter(value -> !value.isBlank()).findFirst().orElse("");
    }

    public SelectOptionSnapshot selectOption(String ariaLabel, String optionText) {
        WebElement button = visible(By.cssSelector(
                "button[aria-label=" + cssString(ariaLabel) + "]"));
        click(button, "Mở bộ lọc " + ariaLabel);
        wait.until(d -> {
            try {
                WebElement option = visibleElements(By.cssSelector("li[role='option']"))
                        .stream().filter(element -> elementText(element).trim().equals(optionText))
                        .findFirst().orElse(null);
                if (option == null) {
                    return false;
                }
                ((JavascriptExecutor) d).executeScript("arguments[0].click();", option);
                return true;
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
        wait.until(d -> elementText(visible(By.cssSelector(
                "button[aria-label=" + cssString(ariaLabel) + "]"))).contains(optionText));
        waitForResult();
        waitForTable();
        return new SelectOptionSnapshot(elementText(visible(By.cssSelector(
                "button[aria-label=" + cssString(ariaLabel) + "]"))).trim(),
                rows(), isEmptyState(), mainText(), currentUrl(), activeGroupText());
    }

    public InitialRenderSnapshot initialRender() {
        String text = mainText();
        return new InitialRenderSnapshot(rows().size(), text.contains("Đang tải dữ liệu"),
                isEmptyState(), text, currentUrl());
    }

    public OverviewFilterState resetAndReadOverviewFilters() {
        resetFilters();
        String status = elementText(visible(By.cssSelector(
                "button[aria-label=" + cssString("trạng thái-filter") + "]"))).trim();
        String gateway = elementText(visible(By.cssSelector(
                "button[aria-label=" + cssString("cổng thanh toán-filter") + "]"))).trim();
        WebElement gatewayButton = visible(By.cssSelector(
                "button[aria-label=" + cssString("cổng thanh toán-filter") + "]"));
        click(gatewayButton, "Mở danh sách toàn bộ cổng thanh toán sau Reset");
        List<String> gatewayOptions = wait.until(d -> {
            List<String> options = visibleElements(By.cssSelector("li[role='option']")).stream()
                    .map(this::elementText).map(String::trim)
                    .filter(value -> !value.isBlank()).toList();
            return options.isEmpty() ? null : options;
        });
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        return new OverviewFilterState(status, gateway, gatewayOptions, currentUrl());
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
        List<TransactionRow> filtered = waitForSearchRows(query);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        List<TransactionRow> restored = waitForRestoredRows(before);
        return new SearchSnapshot(query, before, filtered, restored, currentUrl());
    }

    public SearchSnapshot searchByFirstUserName() {
        List<TransactionRow> before = rows();
        TransactionRow source = before.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có giao dịch để kiểm tra tìm kiếm theo tên."));
        String withoutPhone = PHONE.matcher(source.value("Người dùng")).replaceAll(" ")
                .replace("(", " ").replace(")", " ").trim();
        if (withoutPhone.isBlank()) {
            throw new IllegalStateException("Dòng giao dịch không có tên người dùng để tìm kiếm.");
        }
        return searchAndRestore(firstSearchToken(withoutPhone), before,
                "Tìm giao dịch theo tên người dùng thật");
    }

    public SearchSnapshot searchByFirstUserPhone() {
        List<TransactionRow> before = rows();
        TransactionRow source = before.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có giao dịch để kiểm tra tìm kiếm theo SĐT."));
        Matcher matcher = PHONE.matcher(source.value("Người dùng"));
        if (!matcher.find()) {
            throw new IllegalStateException("Dòng giao dịch không có SĐT để tìm kiếm.");
        }
        return searchAndRestore(matcher.group(), before,
                "Tìm giao dịch theo SĐT người dùng thật");
    }

    public SearchSnapshot searchByFirstUserNameWithPadding() {
        List<TransactionRow> before = rows();
        TransactionRow source = before.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có giao dịch để kiểm tra khoảng trắng tìm kiếm."));
        String withoutPhone = PHONE.matcher(source.value("Người dùng")).replaceAll(" ")
                .replace("(", " ").replace(")", " ").trim();
        if (withoutPhone.isBlank()) {
            throw new IllegalStateException("Dòng giao dịch không có tên người dùng để tìm kiếm.");
        }
        return searchAndRestore("  " + firstSearchToken(withoutPhone) + "  ", before,
                "Tìm tên người dùng có khoảng trắng đầu cuối");
    }

    public SearchSnapshot searchByFirstUserNameWithToggledCase() {
        List<TransactionRow> before = rows();
        TransactionRow source = before.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có giao dịch để kiểm tra chữ hoa thường."));
        String withoutPhone = PHONE.matcher(source.value("Người dùng")).replaceAll(" ")
                .replace("(", " ").replace(")", " ").trim();
        if (withoutPhone.isBlank()) {
            throw new IllegalStateException("Dòng giao dịch không có tên người dùng để tìm kiếm.");
        }
        String token = firstSearchToken(withoutPhone);
        String toggled = token.equals(token.toUpperCase(Locale.ROOT))
                ? token.toLowerCase(Locale.ROOT) : token.toUpperCase(Locale.ROOT);
        return searchAndRestore(toggled, before, "Tìm tên người dùng không phân biệt hoa thường");
    }

    public SearchSnapshot searchByFirstUserPhonePartial() {
        List<TransactionRow> before = rows();
        TransactionRow source = before.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có giao dịch để kiểm tra SĐT một phần."));
        Matcher matcher = PHONE.matcher(source.value("Người dùng"));
        if (!matcher.find()) {
            throw new IllegalStateException("Dòng giao dịch không có SĐT để tìm kiếm.");
        }
        String phone = matcher.group();
        String partial = phone.substring(Math.max(0, phone.length() - 6));
        return searchAndRestore(partial, before, "Tìm giao dịch theo một phần SĐT");
    }

    private SearchSnapshot searchAndRestore(String query, List<TransactionRow> before, String step) {
        WebElement input = visible(By.cssSelector("[aria-label='search-name-phone-filter']"));
        fill(input, query, step);
        List<TransactionRow> filtered = waitForSearchRows(query);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        List<TransactionRow> restored = waitForRestoredRows(before);
        return new SearchSnapshot(query, before, filtered, restored, currentUrl());
    }

    public AppliedSearchSnapshot applySearchFromFirstUser() {
        TransactionRow source = rows().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có giao dịch để áp dụng tìm kiếm người dùng thật."));
        String userText = source.value("Người dùng");
        Matcher matcher = PHONE.matcher(userText);
        String query = matcher.find() ? matcher.group() : firstSearchToken(userText);
        fill(visible(By.cssSelector("[aria-label='search-name-phone-filter']")), query,
                "Lọc theo tên hoặc SĐT thật " + query);
        return new AppliedSearchSnapshot(query, waitForSearchRows(query), currentUrl());
    }

    public InvoiceTransitionSnapshot switchInvoiceYesNoAndAll() {
        List<String> baseline = rows().stream().map(TransactionRow::signature).toList();
        SelectOptionSnapshot yes = selectOption("xuất hoá đơn-filter", "Có");
        List<String> yesRows = yes.rows().stream().map(TransactionRow::signature).toList();
        SelectOptionSnapshot no = selectOption("xuất hoá đơn-filter", "Không");
        List<String> noRows = no.rows().stream().map(TransactionRow::signature).toList();
        SelectOptionSnapshot all = selectOption("xuất hoá đơn-filter", "Tất cả");
        List<String> restored = all.rows().stream().map(TransactionRow::signature).toList();
        return new InvoiceTransitionSnapshot(yes.selectedText(), no.selectedText(),
                all.selectedText(), baseline, yesRows, noRows, restored,
                currentUrl(), activeGroupText());
    }

    public InvoiceEvidenceSnapshot verifyFilteredInvoiceEvidence(String option) {
        SelectOptionSnapshot selected = selectOption("xuất hoá đơn-filter", option);
        List<WebElement> elements = grid().findElements(DATA_ROWS).stream()
                .filter(WebElement::isDisplayed).toList();
        if (elements.isEmpty()) {
            return new InvoiceEvidenceSnapshot(option, selected.selectedText(), 0,
                    List.of(), true, currentUrl(), activeGroupText());
        }
        List<Integer> indexes = elements.size() == 1 ? List.of(0)
                : List.of(0, elements.size() - 1);
        List<Boolean> evidence = new ArrayList<>();
        for (int index : indexes) {
            elements = grid().findElements(DATA_ROWS).stream()
                    .filter(WebElement::isDisplayed).toList();
            click(elements.get(index), "Mở chi tiết phí để kiểm tra hóa đơn " + option);
            WebElement drawer = visible(By.cssSelector(
                    "[aria-label='drawer-Chi tiết giao dịch']"));
            WebElement order = wait.until(d -> {
                List<WebElement> drawers = d.findElements(By.cssSelector(
                                "[aria-label='drawer-Chi tiết giao dịch']"))
                        .stream().filter(WebElement::isDisplayed).toList();
                if (drawers.isEmpty()) {
                    return null;
                }
                String text = normalizeText(elementText(drawers.get(0)));
                if (text.contains("dang tai") || !text.contains("thong tin giao dich")) {
                    return null;
                }
                return drawers.get(0).findElements(By.cssSelector("a[href*='/vuatho/order?id=']"))
                        .stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
            });
            String sourceHandle = driver.getWindowHandle();
            Set<String> before = new HashSet<>(driver.getWindowHandles());
            click(order, "Mở đơn liên quan để xác minh hóa đơn");
            String target = wait.until(d -> d.getWindowHandles().stream()
                    .filter(handle -> !before.contains(handle)).findFirst().orElse(null));
            driver.switchTo().window(target);
            wait.until(d -> d.getCurrentUrl().contains("/vuatho/order?id="));
            String normalized = wait.until(d -> d.findElements(By.id("order-section-invoice"))
                    .stream().filter(WebElement::isDisplayed)
                    .map(this::elementText).map(this::normalizeText)
                    .filter(text -> !text.isBlank()).findFirst().orElse(null));
            evidence.add(!normalized.contains("chua co thong tin hoa don"));
            driver.close();
            driver.switchTo().window(sourceHandle);
            drawer = visible(By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']"));
            List<WebElement> drawerButtons = drawer.findElements(By.tagName("button")).stream()
                    .filter(WebElement::isDisplayed).filter(WebElement::isEnabled).toList();
            WebElement close = drawerButtons.stream()
                    .filter(button -> "Hủy".equals(elementText(button).trim()))
                    .findFirst().orElseGet(() -> drawerButtons.stream().findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                    "Không thấy nút đóng chi tiết phí")));
            click(close, "Đóng chi tiết phí sau khi kiểm tra hóa đơn");
            wait.until(d -> visibleElements(By.cssSelector(
                    "[aria-label='drawer-Chi tiết giao dịch']")).isEmpty());
        }
        return new InvoiceEvidenceSnapshot(option, selected.selectedText(), indexes.size(),
                evidence, false, currentUrl(), activeGroupText());
    }

    public FeeFilterState readFeeFilterState() {
        return new FeeFilterState(
                visible(By.cssSelector("[aria-label='search-name-phone-filter']"))
                        .getAttribute("value"),
                elementText(visible(By.cssSelector(
                        "button[aria-label=" + cssString("xuất hoá đơn-filter") + "]"))).trim(),
                elementText(visible(By.cssSelector(
                        "button[aria-label=" + cssString("Chọn khoảng ngày giờ giao dịch") + "]"))).trim(),
                rows().stream().map(TransactionRow::signature).toList(),
                mainText(), currentUrl(), activeGroupText());
    }

    public FeeFilterState resetAndReadFeeFilterState() {
        resetFilters();
        return readFeeFilterState();
    }

    public FeeDetailFilterPersistenceSnapshot openCloseDetailAndReadFeeFilters() {
        FeeFilterState before = readFeeFilterState();
        DetailSnapshot detail = openAndCloseFirstDetail();
        FeeFilterState after = readFeeFilterState();
        return new FeeDetailFilterPersistenceSnapshot(before, after,
                detail.openedUrl(), detail.closedUrl(), detail.closed());
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

    /** Giữ nguyên trạng thái tìm kiếm rỗng để testcase có thể xuất chính tập kết quả đó. */
    public AppliedEmptySearchSnapshot applyUnmatchedSearchForExport() {
        String query = "NO_REWARD_TRANSACTION_EXPORT_987654321";
        WebElement input = visible(By.cssSelector("[aria-label='search-name-phone-filter']"));
        fill(input, query, "Tạo kết quả rỗng trước khi xuất Excel");
        waitForTable();
        return new AppliedEmptySearchSnapshot(query, isEmptyState(), mainText(), currentUrl());
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

    public FilterSnapshot optionsForFilter(String ariaLabel) {
        WebElement button = visible(By.cssSelector(
                "button[aria-label=" + cssString(ariaLabel) + "]"));
        click(button, "Mở bộ lọc " + ariaLabel);
        List<String> options = wait.until(d -> {
            List<String> values = visibleElements(By.cssSelector("li[role='option']")).stream()
                    .map(this::elementText).map(String::trim)
                    .filter(value -> !value.isBlank()).toList();
            return values.isEmpty() ? null : values;
        });
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        return new FilterSnapshot(ariaLabel, options, currentUrl(), currentUrl(), activeGroupText());
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
                .findFirst().orElse(null));
        drawer = wait.until(d -> d.findElements(
                        By.cssSelector("[aria-label='drawer-Chi tiết giao dịch']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> {
                    String text = normalizeText(elementText(element));
                    return !text.isBlank() && !text.contains("dang tai")
                            && text.contains("trang thai");
                }).findFirst().orElse(null));
        String openedUrl = currentUrl();
        String drawerText = elementText(drawer);
        click(overlayCloseButton(drawer), "Đóng chi tiết giao dịch");
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

    public CategoryNonSortableSnapshot nonSortableHeadersDoNotChangeRows() {
        List<String> expected = List.of("Loại giao dịch", "Trạng thái");
        List<String> before = rows().stream().map(TransactionRow::signature).toList();
        List<String> actual = new ArrayList<>();
        for (String header : expected) {
            WebElement button = headerButton(header);
            if (!button.getAttribute("class").contains("cursor-pointer")) {
                actual.add(header);
            }
            click(button, "Thử click cột không hỗ trợ sort: " + header);
            settle(250);
        }
        return new CategoryNonSortableSnapshot(expected, actual, before,
                rows().stream().map(TransactionRow::signature).toList(), currentUrl());
    }

    public LaterPageInvoiceSnapshot invoiceFilterFromSecondPage(String option) {
        goToSecondPage();
        int before = activePage();
        SelectOptionSnapshot selected = selectOption("xuất hoá đơn-filter", option);
        return new LaterPageInvoiceSnapshot(before, activePage(), selected.selectedText(),
                selected.rows(), selected.empty(), currentUrl(), activeGroupText());
    }

    public CategoryResetPageSnapshot resetFromSecondPage() {
        goToSecondPage();
        int before = activePage();
        resetFilters();
        return new CategoryResetPageSnapshot(before, activePage(),
                rows().stream().map(TransactionRow::signature).toList(),
                currentUrl(), activeGroupText());
    }

    private void goToSecondPage() {
        if (activePage() != 2) {
            WebElement next = visible(By.cssSelector(
                    "nav[aria-label='pagination navigation'] [aria-label='next page button']"));
            if ("true".equals(next.getAttribute("aria-disabled"))
                    || "true".equals(next.getAttribute("data-disabled"))) {
                throw new IllegalStateException("Không có trang 2 để kiểm tra navigation.");
            }
            click(next, "Chuyển sang trang 2");
            waitForTable();
        }
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
            Path file = Waits.withTimeout(driver, TestConfig.exportDownloadTimeout())
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

    private String overviewBlockText(String heading, List<String> markers) {
        return overviewBlockProperty(heading, markers, "innerText");
    }

    private String overviewBlockProperty(String heading, List<String> markers, String property) {
        String script = "const heading=arguments[0], markers=arguments[1];"
                + "const nodes=Array.from(document.querySelectorAll('main *'));"
                + "const matches=nodes.filter(node=>{const text=(node.innerText||'').trim();"
                + "return text.includes(heading)&&markers.every(marker=>text.includes(marker));});"
                + "matches.sort((left,right)=>(left.innerText||'').length-(right.innerText||'').length);"
                + "return matches.length?String(matches[0][arguments[2]]||'').trim():'';";
        String text = String.valueOf(((JavascriptExecutor) driver)
                .executeScript(script, heading, markers, property));
        if (text.isBlank()) {
            throw new IllegalStateException("Không tìm thấy khối tổng quan: " + heading);
        }
        return text;
    }

    @SuppressWarnings("unchecked")
    private List<String> overviewSectorNames() {
        String script = "return Array.from(document.querySelectorAll("
                + "'main .recharts-pie-sector path[name]'))"
                + ".map(path=>path.getAttribute('name')).filter(Boolean);";
        return ((List<Object>) ((JavascriptExecutor) driver).executeScript(script)).stream()
                .map(String::valueOf).distinct().toList();
    }

    private BigDecimal moneyNearLabel(String text, String label) {
        String value = valueAfterLabel(text, label, "[0-9][0-9.,]*.*");
        return new BigDecimal(value.replaceAll("[^0-9]", ""));
    }

    private int countNearLabel(String text, String label) {
        return Integer.parseInt(valueAfterLabel(text, label, "[0-9][0-9.,]*")
                .replaceAll("[^0-9]", ""));
    }

    private BigDecimal percentageNearLabel(String text, String label) {
        String value = valueAfterLabel(text, label, "[0-9]+(?:[.,][0-9]+)?%");
        return new BigDecimal(value.replace("%", "").replace(',', '.'));
    }

    private String valueAfterLabel(String text, String label, String valuePattern) {
        String[] lines = text.split("\\R");
        for (int index = 0; index < lines.length; index++) {
            if (!lines[index].trim().equalsIgnoreCase(label)) {
                continue;
            }
            for (int candidate = index + 1; candidate < Math.min(lines.length, index + 6); candidate++) {
                String value = lines[candidate].trim();
                if (value.matches(valuePattern)) {
                    return value;
                }
            }
            break;
        }
        throw new IllegalStateException("Không đọc được giá trị cạnh " + label + ": " + text);
    }

    private List<BigDecimal> percentages(String text) {
        Matcher matcher = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)%")
                .matcher(text);
        List<BigDecimal> values = new ArrayList<>();
        while (matcher.find()) {
            values.add(new BigDecimal(matcher.group(1).replace(',', '.')));
        }
        return values;
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

    /** Chờ debounce/API tìm kiếm hoàn tất và không đọc nhầm các dòng cũ của bảng. */
    private List<TransactionRow> waitForSearchRows(String query) {
        String expected = normalizeSearchValue(query);
        WebDriverWait resultWait = Waits.withTimeout(driver, Duration.ofSeconds(12));
        resultWait.pollingEvery(Duration.ofMillis(250));
        try {
            return resultWait.until(d -> {
                if (tableIsLoading()) {
                    return null;
                }
                List<TransactionRow> current = currentRowsWithoutWait();
                if (current.isEmpty()) {
                    return isEmptyState() ? current : null;
                }
                boolean allMatch = current.stream().allMatch(row ->
                        normalizeSearchValue(row.value("Người dùng")).contains(expected));
                return allMatch ? current : null;
            });
        } catch (TimeoutException exception) {
            // Trả trạng thái thật để assertion nghiệp vụ báo chính xác dòng không khớp.
            return currentRowsWithoutWait();
        }
    }

    /** Chờ xóa từ khóa khôi phục đúng danh sách trước khi assertion. */
    private List<TransactionRow> waitForRestoredRows(List<TransactionRow> baseline) {
        List<String> expected = baseline.stream().map(TransactionRow::signature).toList();
        WebDriverWait resultWait = Waits.withTimeout(driver, Duration.ofSeconds(12));
        resultWait.pollingEvery(Duration.ofMillis(250));
        try {
            return resultWait.until(d -> {
                if (tableIsLoading()) {
                    return null;
                }
                List<TransactionRow> current = currentRowsWithoutWait();
                List<String> actual = current.stream().map(TransactionRow::signature).toList();
                return actual.equals(expected) ? current : null;
            });
        } catch (TimeoutException exception) {
            return currentRowsWithoutWait();
        }
    }

    private boolean tableIsLoading() {
        List<WebElement> grids = driver.findElements(GRID).stream()
                .filter(WebElement::isDisplayed).toList();
        return grids.isEmpty() || elementText(grids.get(0)).contains("Đang tải dữ liệu");
    }

    private List<TransactionRow> currentRowsWithoutWait() {
        List<WebElement> grids = driver.findElements(GRID).stream()
                .filter(WebElement::isDisplayed).toList();
        if (grids.isEmpty()) {
            return List.of();
        }
        return grids.get(0).findElements(DATA_ROWS).stream()
                .filter(WebElement::isDisplayed)
                .map(this::toRow)
                .filter(row -> row != null)
                .toList();
    }

    private String normalizeSearchValue(String value) {
        return value == null ? ""
                : value.replaceAll("[^0-9A-Za-zÀ-ỹ]", "").toLowerCase();
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
                new Subtype("Thu bảo hành", "order", 36),
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
                List.of("search-name-phone-filter", "trạng thái-filter", "cổng thanh toán-filter",
                        "Chọn khoảng ngày giờ giao dịch")),
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
        public LocalDateTime createdAt() {
            return LocalDateTime.parse(value("Ngày tạo"),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        }
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
    public record CostWalletOverviewSnapshot(BigDecimal total,
                                             Map<String, BigDecimal> gateways,
                                             List<BigDecimal> percentages,
                                             List<String> sectorNames,
                                             String text, String url) {}
    public record BankTransferOverviewSnapshot(Map<String, BigDecimal> amounts,
                                               Map<String, Integer> counts,
                                               Map<String, BigDecimal> amountPercentages,
                                               Map<String, BigDecimal> countPercentages,
                                               String amountText, String countText,
                                               String url) {}
    public record InsuranceOverviewSnapshot(Subtype subtype, BigDecimal totalAmount,
                                            int totalCount,
                                            Map<String, BigDecimal> amounts,
                                            Map<String, Integer> counts,
                                            Map<String, BigDecimal> amountPercentages,
                                            Map<String, BigDecimal> countPercentages,
                                            String amountText, String countText, String url) {}
    public record InsurancePieOverviewSnapshot(FeePieSnapshot amountChart,
                                               FeePieSnapshot countChart,
                                               String url) {}
    public record InsuranceOverviewRefreshSnapshot(List<String> headings,
                                                   InsuranceOverviewSnapshot overview,
                                                   String url, String activeText) {}
    public record CategoryStatusOverviewSnapshot(String amountHeading, String countHeading,
                                                 BigDecimal totalAmount, Integer totalCount,
                                                 Map<String, BigDecimal> amounts,
                                                 Map<String, Integer> counts,
                                                 Map<String, BigDecimal> amountPercentages,
                                                 Map<String, BigDecimal> countPercentages,
                                                 String url) {}
    public record CategoryStatusPieSnapshot(FeePieSnapshot amountChart,
                                            FeePieSnapshot countChart,
                                            String url) {}
    public record CategoryStatusRefreshSnapshot(List<String> headings,
                                                CategoryStatusOverviewSnapshot overview,
                                                String url, String activeText) {}
    public record FeeConnectionOverviewSnapshot(BigDecimal totalRevenue, int transactionCount,
                                                BigDecimal collected, BigDecimal collectedPercentage,
                                                BigDecimal debt, BigDecimal debtPercentage,
                                                List<String> dateLabels, boolean weekControl,
                                                boolean monthControl, boolean customControl,
                                                String text, String url) {}
    public record RevenueChartSnapshot(String activePeriod, LocalDate start, LocalDate end,
                                       boolean customInputVisible, String customInputValue,
                                       List<String> dateTicks, int visibleBars,
                                       List<String> tableRows, boolean calendarVisible,
                                       int disabledFutureDays, String url) {}
    public record FeeWalletLinkOverviewSnapshot(BigDecimal totalAmount, int totalCount,
                                                Map<String, BigDecimal> amounts,
                                                Map<String, Integer> counts,
                                                Map<String, BigDecimal> amountPercentages,
                                                Map<String, BigDecimal> countPercentages,
                                                String amountText, String countText, String url) {}
    public record FeeMaterialShareOverviewSnapshot(BigDecimal totalCollected, int totalOrders,
                                                   String text, String url) {}
    public record FeePieSnapshot(int chartCount, Map<String, String> sectors,
                                 String text, String url) {}
    public record FeeDualPieSnapshot(FeePieSnapshot amountChart, FeePieSnapshot countChart,
                                     String url) {}
    public record FeeDebtRow(String workerText, String workerId, String workerUrl,
                             BigDecimal debt, LocalDateTime debtDate, int debtDays) {
        public String signature() {
            return workerId + "|" + debt + "|" + debtDate;
        }
    }
    public record FeeDebtSnapshot(boolean opened, List<String> headers,
                                  List<FeeDebtRow> rows, boolean pagination,
                                  String url) {}
    public record FeeDebtSortSnapshot(List<BigDecimal> amountBefore,
                                       List<BigDecimal> amountFirst,
                                       List<BigDecimal> amountSecond,
                                       List<Integer> debtDaysFirst,
                                       List<Integer> debtDaysSecond,
                                       String url) {}
    public record FeeDebtPaginationSnapshot(List<String> firstPage,
                                            List<String> secondPage,
                                            List<String> restoredFirstPage,
                                            String url) {}
    public record FeeDebtCloseSnapshot(boolean closed, String url, String activeText) {}
    public record FeeDebtWorkerNavigationSnapshot(String expectedUrl, String actualUrl,
                                                   String targetText, boolean modalRestored,
                                                   String sourceUrl) {}
    public record FeeMaterialTotalSnapshot(BigDecimal overviewTotal, BigDecimal tableTotal,
                                            int rowCount, int totalPages, String url) {}
    public record InvalidFeeRouteSnapshot(String url, String activeText, boolean drawerOpen,
                                          String drawerText, String pageText) {}
    public record MismatchedFeeRouteSnapshot(Subtype sourceSubtype, Subtype wrongSubtype,
                                             String transactionId, String sourceDrawerText,
                                             InvalidFeeRouteSnapshot attempted) {}
    public record FeeOverviewRefreshSnapshot(List<String> headings, String url,
                                             String activeText) {}
    public record ChartTooltipSnapshot(int chartCount, List<String> tooltips, String url) {}
    public record InitialRenderSnapshot(int visibleRows, boolean loading,
                                        boolean empty, String text, String url) {}
    public record OverviewFilterState(String status, String gateway,
                                      List<String> gatewayOptions, String url) {}
    public record SearchSnapshot(String query, List<TransactionRow> before,
                                 List<TransactionRow> filtered, List<TransactionRow> restored,
                                 String url) {}
    public record AppliedSearchSnapshot(String query, List<TransactionRow> rows, String url) {}
    public record InvoiceTransitionSnapshot(String yesText, String noText, String allText,
                                            List<String> baseline, List<String> yesRows,
                                            List<String> noRows, List<String> restored,
                                              String url, String activeText) {}
    public record InvoiceEvidenceSnapshot(String expectedOption, String selectedText,
                                          int sampledRows, List<Boolean> hasInvoice,
                                          boolean empty, String url, String activeText) {}
    public record FeeFilterState(String search, String invoice, String date,
                                 List<String> rows, String pageText,
                                 String url, String activeText) {}
    public record FeeDetailFilterPersistenceSnapshot(FeeFilterState before,
                                                      FeeFilterState after,
                                                      String openedUrl,
                                                      String closedUrl,
                                                      boolean closed) {}
    public record EmptySearchSnapshot(String query, boolean empty, String pageText,
                                       List<TransactionRow> before, List<TransactionRow> restored,
                                       String url, String activeText) {}
    public record AppliedEmptySearchSnapshot(String query, boolean empty,
                                             String pageText, String url) {}
    public record FilterSnapshot(String ariaLabel, List<String> options,
                                 String beforeResetUrl, String afterResetUrl, String activeText) {}
    public record SelectOptionSnapshot(String selectedText, List<TransactionRow> rows,
                                       boolean empty, String pageText, String url,
                                       String activeText) {}
    public record DropdownOption(String label, String key, String checked, String selected) {}
    public record DropdownSemanticsSnapshot(String expandedBefore, String expandedAfter, String hasPopup,
                                             String controls, String menuId, String menuLabel,
                                            List<DropdownOption> options) {}
    public record DropdownCloseSnapshot(String expandedAfter, boolean menuClosed,
                                        String url, String activeText) {}
    public record DropdownSelectionSnapshot(String url, String triggerText, String checked,
                                             String selected, String optionText,
                                             long selectedCount, boolean menuClosed) {}
    public record DropdownFocusSnapshot(String beforeUrl, String afterUrl,
                                        String beforeText, String afterText,
                                        boolean menuClosed, String focusedTag,
                                        String focusedId) {}
    public record DropdownKeyboardSnapshot(String beforeKey, String url, String activeText,
                                           boolean menuClosed, String triggerId,
                                           String focusedId) {}
    public record DropdownReloadSnapshot(String beforeUrl, String afterUrl,
                                         String beforeText, String afterText,
                                         List<DropdownOption> options) {}
    public record DropdownInvalidTypeSnapshot(int invalidType, String url,
                                              String activeText,
                                              List<DropdownOption> options) {}
    public record SubtypeChangeSnapshot(String url, String triggerText) {}
    public record SortSnapshot(List<BigDecimal> ascending, List<BigDecimal> descending, String url) {}
    public record DetailSnapshot(String source, String openedUrl, String drawerText,
                                 boolean closed, String closedUrl) {}
    public record PaginationSnapshot(int beforePage, int afterNextPage, int afterResetPage,
                                     boolean previousDisabled, boolean nextDisabled,
                                     String url, String activeText) {}
    public record CategoryNonSortableSnapshot(List<String> expectedHeaders,
                                              List<String> nonSortableHeaders,
                                              List<String> rowsBefore,
                                              List<String> rowsAfter, String url) {}
    public record LaterPageInvoiceSnapshot(int pageBefore, int pageAfter,
                                           String selectedText, List<TransactionRow> rows,
                                           boolean empty, String url, String activeText) {}
    public record CategoryResetPageSnapshot(int pageBefore, int pageAfter,
                                            List<String> rows, String url,
                                            String activeText) {}
    public record ExportSnapshot(Path file, int visibleRows, String url) {}
    private record FileFingerprint(long size, long modifiedAt) {}
}
