package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.TransactionCategoryPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Chuẩn bị và cung cấp assertion dùng chung cho các nhóm Lịch sử giao dịch. */
public abstract class TransactionCategoryTestSupport extends BaseTest {
    private static final DateTimeFormatter ROW_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    protected TransactionCategoryPage transactionPage;

    protected abstract TransactionCategoryPage.Category category();

    protected TransactionCategoryPage.Subtype initialSubtype() {
        return category().subtypes().get(0);
    }

    protected boolean openInitialSubtypeBeforeEachTest() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareTransactionCategory() {
        if (driver == null) {
            throw new SkipException("WebDriver không khởi tạo được.");
        }
        if (!driver.getCurrentUrl().contains("/vuatho/")) {
            LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
            Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                    "Không đăng nhập được trước khi kiểm tra Lịch sử giao dịch.");
        }
        transactionPage = new TransactionCategoryPage(driver, category());
        if (openInitialSubtypeBeforeEachTest()) {
            transactionPage.open(initialSubtype());
        }
    }

    protected Object[][] subtypeRows() {
        return category().subtypes().stream().map(subtype -> new Object[]{subtype}).toArray(Object[][]::new);
    }

    protected void verifyGroupOptions() {
        List<String> actual = transactionPage.groupOptions();
        List<String> expected = category().subtypes().stream()
                .map(TransactionCategoryPage.Subtype::label).toList();
        Assert.assertEquals(actual, expected);
    }

    protected void verifySubtypeRoute(TransactionCategoryPage.Subtype subtype) {
        transactionPage.open(subtype);
        assertSubtypeUrl(transactionPage.currentUrl(), subtype);
        Assert.assertTrue(transactionPage.activeGroupText().contains(category().label()));
        long sameRouteCount = category().subtypes().stream()
                .filter(candidate -> candidate.route().equals(subtype.route())).count();
        if (category() != TransactionCategoryPage.Category.SYSTEM && sameRouteCount == 1) {
            Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                    "Tab không hiển thị loại đang chọn: " + subtype.label());
        }
    }

    protected void verifyLayout() {
        verifyLayout(category().subtypes().get(0));
    }

    protected void verifyLayout(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.layout();
        Assert.assertEquals(result.headers(), category().headers());
        result.controls().forEach((control, visible) ->
                Assert.assertTrue(visible, "Thiếu control: " + control));
        assertSubtypeUrl(result.url(), subtype);
    }

    protected void verifyRowFormats() {
        var rows = transactionPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Loại đại diện không có dữ liệu để kiểm tra.");
        rows.forEach(row -> {
            Assert.assertFalse(row.value("Người dùng").isBlank());
            Assert.assertFalse(row.value("Trạng thái").isBlank());
            Assert.assertTrue(row.value("Số tiền").contains("₫"), "Sai định dạng tiền: " + row.value("Số tiền"));
            Assert.assertNotNull(row.amount("Số tiền"));
            LocalDateTime createdAt = LocalDateTime.parse(row.value("Ngày tạo"), ROW_DATE);
            Assert.assertFalse(createdAt.isAfter(LocalDateTime.now().plusMinutes(1)));
        });
    }

    protected void verifySearchAndReset() {
        verifySearchAndReset(category().subtypes().get(0));
    }

    protected void verifySearchAndReset(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.searchByFirstUser();
        Assert.assertFalse(result.query().isBlank());
        Assert.assertFalse(result.filtered().isEmpty(),
                "Không có kết quả khi tìm từ khóa lấy từ dòng đầu: " + result.query());
        String normalizedQuery = normalize(result.query());
        result.filtered().forEach(row -> Assert.assertTrue(
                normalize(row.value("Người dùng")).contains(normalizedQuery),
                "Kết quả không khớp người dùng: " + row.value("Người dùng")));
        Assert.assertEquals(signatures(result.restored()), signatures(result.before()));
        assertSubtypeUrl(result.url(), subtype);
    }

    protected void verifyFilterOptions() {
        verifyFilterOptions(category().subtypes().get(0));
    }

    protected void verifyFilterOptions(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.firstAvailableFilter();
        Assert.assertFalse(result.options().isEmpty());
        Assert.assertEquals(result.beforeResetUrl(), result.afterResetUrl());
        assertSubtypeUrl(result.afterResetUrl(), subtype);
        Assert.assertTrue(result.activeText().contains(category().label()));
    }

    protected void verifyAmountSort() {
        verifyAmountSort(category().subtypes().get(0));
    }

    protected void verifyAmountSort(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.sortAmountBothDirections();
        assertOrdered(result.ascending(), Comparator.naturalOrder());
        assertOrdered(result.descending(), Comparator.reverseOrder());
        assertSubtypeUrl(result.url(), subtype);
    }

    protected void verifyDetail() {
        verifyDetail(category().subtypes().get(0));
    }

    protected void verifyDetail(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.openAndCloseFirstDetail();
        Assert.assertTrue(result.openedUrl().contains("id="), "URL chi tiết không có mã giao dịch.");
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
        Assert.assertTrue(result.drawerText().contains("Số tiền"));
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="));
        Assert.assertTrue(result.closedUrl().contains("/vuatho/transaction"));
        Assert.assertTrue(result.closedUrl().contains("tab=" + subtype.tab()),
                "Đóng chi tiết làm mất nhóm giao dịch: " + result.closedUrl());
        boolean defaultDepositRoute = category() == TransactionCategoryPage.Category.DEPOSIT
                && subtype.type() == 0
                && !result.closedUrl().contains("type=");
        boolean feeRouteKeepsSubtypeInUiState = category() == TransactionCategoryPage.Category.FEE
                && !result.closedUrl().contains("type=");
        boolean insuranceRouteKeepsSubtypeInUiState =
                category() == TransactionCategoryPage.Category.INSURANCE
                        && !result.closedUrl().contains("type=");
        Assert.assertTrue(defaultDepositRoute || feeRouteKeepsSubtypeInUiState
                        || insuranceRouteKeepsSubtypeInUiState
                        || result.closedUrl().contains("type=" + subtype.type()),
                "Đóng chi tiết làm mất loại giao dịch: " + result.closedUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                "Đóng chi tiết hiển thị sai loại giao dịch: " + transactionPage.activeGroupText());
    }

    protected void verifyPaginationAndReset() {
        verifyPaginationAndReset(category().subtypes().get(0));
    }

    protected void verifyPaginationAndReset(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.paginationAndReset();
        Assert.assertEquals(result.beforePage(), 1);
        Assert.assertTrue(result.previousDisabled());
        if (!result.nextDisabled()) {
            Assert.assertEquals(result.afterNextPage(), 2);
        }
        Assert.assertEquals(result.afterResetPage(), 1);
        assertSubtypeUrl(result.url(), subtype);
        Assert.assertTrue(result.activeText().contains(category().label()));
    }

    protected void verifyExport() {
        verifyExport(category().subtypes().get(0));
    }

    protected void verifyExport(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.exportCurrentSubtype();
        Assert.assertNotNull(result.file());
        Assert.assertTrue(Files.isRegularFile(result.file()));
        Assert.assertTrue(result.file().getFileName().toString().matches("(?i).+\\.(xlsx|xls|csv)$"));
        assertSubtypeUrl(result.url(), subtype);
    }

    private void assertOrdered(List<BigDecimal> actual, Comparator<BigDecimal> comparator) {
        Assert.assertTrue(actual.size() > 1, "Không đủ dữ liệu để kiểm tra sắp xếp.");
        List<BigDecimal> expected = new ArrayList<>(actual);
        expected.sort(comparator);
        Assert.assertEquals(actual, expected);
    }

    private void assertRepresentativeUrl(String url) {
        assertSubtypeUrl(url, category().subtypes().get(0));
    }

    private void assertSubtypeUrl(String url, TransactionCategoryPage.Subtype subtype) {
        Assert.assertTrue(url.contains("/vuatho/transaction"), "Sai màn hình: " + url);
        Assert.assertTrue(url.contains("tab=" + subtype.tab()), "Sai tab: " + url);
        Assert.assertTrue(url.contains("type=" + subtype.type()), "Sai loại giao dịch: " + url);
    }

    private List<String> signatures(List<TransactionCategoryPage.TransactionRow> rows) {
        return rows.stream().map(TransactionCategoryPage.TransactionRow::signature).toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("[^0-9A-Za-zÀ-ỹ]", "").toLowerCase();
    }
}
