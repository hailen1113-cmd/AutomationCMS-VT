package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.TransactionCategoryPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/** Chuẩn bị và cung cấp assertion dùng chung cho các nhóm Lịch sử giao dịch. */
public abstract class TransactionCategoryTestSupport extends BaseTest {
    private static final DateTimeFormatter ROW_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    protected TransactionCategoryPage transactionPage;

    protected abstract TransactionCategoryPage.Category category();

    protected TransactionCategoryPage.Subtype initialSubtype() {
        return category().subtypes().get(0);
    }

    /** Trả về subtype theo mã type và báo lỗi rõ ràng nếu catalog cấu hình thiếu. */
    protected final TransactionCategoryPage.Subtype subtypeByType(int type) {
        return category().subtypes().stream()
                .filter(candidate -> candidate.type() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Thiếu subtype type=" + type + " trong nhóm " + category().label()));
    }

    /** Chỉ điều hướng khi route hoặc subtype hiện tại chưa đúng, tránh reload trang dư thừa. */
    protected final void openSubtype(TransactionCategoryPage.Subtype subtype) {
        String url = transactionPage.currentUrl();
        boolean correctRoute = url.contains("tab=" + subtype.tab());
        boolean defaultDepositRoute = category() == TransactionCategoryPage.Category.DEPOSIT
                && subtype.type() == 0 && !url.contains("type=");
        boolean correctType = url.contains("type=" + subtype.type()) || defaultDepositRoute;
        boolean correctActiveLabel = category() != TransactionCategoryPage.Category.ORDER
                || transactionPage.activeGroupText().contains(subtype.label());
        if (!correctRoute || !correctType || !correctActiveLabel) {
            transactionPage.open(subtype);
        }
    }

    /** Page object cho các thao tác nâng cao dùng chung của lịch sử giao dịch. */
    protected final com.vuatho.pages.TransactionHistoryPage advancedPage() {
        return new com.vuatho.pages.TransactionHistoryPage(driver);
    }

    protected boolean openInitialSubtypeBeforeEachTest() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareTransactionCategory() {
        requireAuthenticatedSession("Lịch sử giao dịch");
        transactionPage = new TransactionCategoryPage(driver, category());
        if (openInitialSubtypeBeforeEachTest()) {
            transactionPage.open(initialSubtype());
        }
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
        Assert.assertFalse(result.drawerText().isBlank(),
                "Drawer chi tiết chưa tải nội dung type=" + subtype.type());
        Assert.assertTrue(result.drawerText().contains("Trạng thái"),
                "Drawer chi tiết thiếu Trạng thái type=" + subtype.type() + ": " + result.drawerText());
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
        boolean orderRouteKeepsSubtypeInUiState = category() == TransactionCategoryPage.Category.ORDER
                && !result.closedUrl().contains("type=");
        Assert.assertTrue(defaultDepositRoute || feeRouteKeepsSubtypeInUiState
                        || insuranceRouteKeepsSubtypeInUiState || orderRouteKeepsSubtypeInUiState
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
        requireFilteredRows(subtype, "không áp dụng bộ lọc");
        var result = transactionPage.exportCurrentSubtype();
        Assert.assertNotNull(result.file());
        Assert.assertTrue(Files.isRegularFile(result.file()));
        Assert.assertTrue(result.file().getFileName().toString().matches("(?i).+\\.(xlsx|xls|csv)$"));
        assertSubtypeUrl(result.url(), subtype);
    }

    protected void verifyExportAfterSearch(TransactionCategoryPage.Subtype subtype) {
        var filter = transactionPage.applySearchFromFirstUser();
        requireFilteredRows(subtype, "tên/SĐT dòng đầu=" + filter.query());
        var export = transactionPage.exportCurrentSubtype();
        var workbook = assertExportContainsAllFilteredRows(export.file());
        String query = normalize(filter.query());
        Assert.assertTrue(workbook.values("Người dùng").stream()
                .map(this::normalize).allMatch(value -> value.contains(query)),
                "File có người dùng không khớp từ khóa " + filter.query());
        assertSubtypeUrl(export.url(), subtype);
    }

    protected void verifyExportAfterStatus(TransactionCategoryPage.Subtype subtype) {
        verifyExportAfterStatus(subtype, "Thành công");
    }

    protected void verifyExportAfterStatus(TransactionCategoryPage.Subtype subtype,
                                           String status) {
        transactionPage.selectOption("trạng thái-filter", status);
        requireFilteredRows(subtype, "trạng thái=" + status);
        var export = transactionPage.exportCurrentSubtype();
        var workbook = assertExportContainsAllFilteredRows(export.file());
        Assert.assertTrue(workbook.values("Trạng thái").stream()
                .allMatch(value -> normalize(value).equals(normalize(status))),
                "File có trạng thái khác " + status);
        assertSubtypeUrl(export.url(), subtype);
    }

    protected void verifyExportAfterGateway(TransactionCategoryPage.Subtype subtype) {
        String gateway = "PAYPAL";
        transactionPage.selectOption("cổng thanh toán-filter", gateway);
        requireFilteredRows(subtype, "cổng=" + gateway);
        var export = transactionPage.exportCurrentSubtype();
        var workbook = assertExportContainsAllFilteredRows(export.file());
        Assert.assertTrue(workbook.values("Cổng thanh toán").stream()
                .allMatch(value -> normalize(value).equals(normalize(gateway))),
                "File có cổng thanh toán khác " + gateway);
        assertSubtypeUrl(export.url(), subtype);
    }

    protected void verifyExportAfterStatusAndGateway(TransactionCategoryPage.Subtype subtype,
                                                     String status, String gateway) {
        transactionPage.selectOption("trạng thái-filter", status);
        transactionPage.selectOption("cổng thanh toán-filter", gateway);
        requireFilteredRows(subtype, "trạng thái=" + status + ", cổng=" + gateway);
        var export = transactionPage.exportCurrentSubtype();
        var workbook = assertExportContainsAllFilteredRows(export.file());
        Assert.assertTrue(workbook.values("Trạng thái").stream()
                .allMatch(value -> normalize(value).equals(normalize(status))),
                "File có trạng thái khác " + status);
        Assert.assertTrue(workbook.values("Cổng thanh toán").stream()
                .allMatch(value -> normalize(value).equals(normalize(gateway))),
                "File có cổng thanh toán khác " + gateway);
        assertSubtypeUrl(export.url(), subtype);
    }

    protected void verifyExportMatrixCellOnFirstSubtype(String status, String gateway) {
        TransactionCategoryPage.Subtype subtype = category().subtypes().get(0);
        openSubtypeIfNeeded(subtype);
        verifyExportAfterStatusAndGateway(subtype, status, gateway);
    }

    protected void verifyStatusMatrixCellOnFirstSubtype(String status) {
        TransactionCategoryPage.Subtype subtype = category().subtypes().get(0);
        openSubtypeIfNeeded(subtype);
        verifyExportAfterStatus(subtype, status);
    }

    private void openSubtypeIfNeeded(TransactionCategoryPage.Subtype subtype) {
        String url = transactionPage.currentUrl();
        if (!url.contains("tab=" + subtype.tab())
                || !url.contains("type=" + subtype.type())) {
            transactionPage.open(subtype);
        }
    }

    protected void verifyExportAfterDate(TransactionCategoryPage.Subtype subtype) {
        var dateFilter = new com.vuatho.pages.TransactionHistoryPage(driver).filterSingleDay();
        requireFilteredRows(subtype, "ngày tạo=" + dateFilter.startDate());
        var export = transactionPage.exportCurrentSubtype();
        var workbook = assertExportContainsAllFilteredRows(export.file());
        assertDates(workbook, dateFilter.startDate());
        assertSubtypeUrl(export.url(), subtype);
    }

    protected void verifyExportAfterCombinedFilters(TransactionCategoryPage.Subtype subtype) {
        var search = transactionPage.applySearchFromFirstUser();
        var dateFilter = new com.vuatho.pages.TransactionHistoryPage(driver).filterSingleDay();
        String status = "Thành công";
        String gateway = "PAYPAL";
        transactionPage.selectOption("trạng thái-filter", status);
        transactionPage.selectOption("cổng thanh toán-filter", gateway);
        requireFilteredRows(subtype, "tên/SĐT dòng đầu, trạng thái=" + status
                + ", cổng=" + gateway + ", ngày tạo dòng đầu");
        var export = transactionPage.exportCurrentSubtype();
        var workbook = assertExportContainsAllFilteredRows(export.file());
        String query = normalize(search.query());
        Assert.assertTrue(workbook.values("Người dùng").stream()
                .map(this::normalize).allMatch(value -> value.contains(query)));
        Assert.assertTrue(workbook.values("Trạng thái").stream()
                .allMatch(value -> normalize(value).equals(normalize(status))));
        Assert.assertTrue(workbook.values("Cổng thanh toán").stream()
                .allMatch(value -> normalize(value).equals(normalize(gateway))));
        assertDates(workbook, dateFilter.startDate());
        assertSubtypeUrl(export.url(), subtype);
    }

    protected void verifyExportAfterSearchAndDate(TransactionCategoryPage.Subtype subtype) {
        var search = transactionPage.applySearchFromFirstUser();
        var dateFilter = new com.vuatho.pages.TransactionHistoryPage(driver).filterSingleDay();
        requireFilteredRows(subtype, "tên/SĐT=" + search.query()
                + ", ngày tạo=" + dateFilter.startDate());
        var export = transactionPage.exportCurrentSubtype();
        var workbook = assertExportContainsAllFilteredRows(export.file());
        String query = normalize(search.query());
        Assert.assertTrue(workbook.values("Người dùng").stream()
                .map(this::normalize).allMatch(value -> value.contains(query)),
                "File có người dùng không khớp từ khóa " + search.query());
        assertDates(workbook, dateFilter.startDate());
        assertSubtypeUrl(export.url(), subtype);
    }

    protected void verifyExportAfterOption(TransactionCategoryPage.Subtype subtype,
                                           String ariaLabel, String option) {
        transactionPage.selectOption(ariaLabel, option);
        requireFilteredRows(subtype, ariaLabel + "=" + option);
        var export = transactionPage.exportCurrentSubtype();
        var workbook = assertExportContainsAllFilteredRows(export.file());
        String column = ariaLabel.replace("-filter", "");
        Assert.assertTrue(workbook.hasHeader(column),
                "File export thiếu cột " + column + " để xác minh bộ lọc " + option);
        Assert.assertTrue(workbook.values(column).stream().allMatch(value ->
                        normalize(value).equals(normalize(option))),
                "File có giá trị " + column + " khác " + option);
        assertSubtypeUrl(export.url(), subtype);
    }

    protected void verifyExportAfterSearchStatusAndDate(TransactionCategoryPage.Subtype subtype) {
        var search = transactionPage.applySearchFromFirstUser();
        var dateFilter = new com.vuatho.pages.TransactionHistoryPage(driver).filterSingleDay();
        String status = "Thành công";
        transactionPage.selectOption("trạng thái-filter", status);
        requireFilteredRows(subtype, "tên/SĐT dòng đầu, trạng thái=" + status
                + ", ngày tạo dòng đầu");
        var export = transactionPage.exportCurrentSubtype();
        var workbook = assertExportContainsAllFilteredRows(export.file());
        String query = normalize(search.query());
        Assert.assertTrue(workbook.values("Người dùng").stream()
                .map(this::normalize).allMatch(value -> value.contains(query)));
        Assert.assertTrue(workbook.values("Trạng thái").stream()
                .allMatch(value -> normalize(value).equals(normalize(status))));
        assertDates(workbook, dateFilter.startDate());
        assertSubtypeUrl(export.url(), subtype);
    }

    protected void verifyExportForSubtype(int type,
                                          Consumer<TransactionCategoryPage.Subtype> verification) {
        TransactionCategoryPage.Subtype subtype = category().subtypes().stream()
                .filter(candidate -> candidate.type() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Thiếu subtype type=" + type));
        transactionPage.open(subtype);
        verification.accept(subtype);
    }

    private void requireFilteredRows(TransactionCategoryPage.Subtype subtype, String filters) {
        int matchingRows = new com.vuatho.pages.TransactionHistoryPage(driver).totalDisplayed();
        if (matchingRows == 0) {
            throw new SkipException("Không có dữ liệu thật cho type=" + subtype.type()
                    + ", " + filters + "; không thể xác minh nội dung file export.");
        }
    }

    private TransactionExportWorkbook.Snapshot assertExportContainsAllFilteredRows(java.nio.file.Path file) {
        Assert.assertNotNull(file);
        Assert.assertTrue(Files.isRegularFile(file));
        TransactionExportWorkbook.Snapshot workbook = TransactionExportWorkbook.read(file);
        int expected = new com.vuatho.pages.TransactionHistoryPage(driver).totalDisplayed();
        if (expected == 0) {
            throw new SkipException("Bộ lọc không có dữ liệu thật; không thể xác minh nội dung file export.");
        }
        Assert.assertEquals(workbook.rows().size(), expected,
                "File phải chứa toàn bộ dữ liệu phù hợp filter, không chỉ trang hiện tại.");
        return workbook;
    }

    private void assertDates(TransactionExportWorkbook.Snapshot workbook, LocalDate expected) {
        String digits = expected.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        Assert.assertTrue(workbook.values("Ngày tạo").stream()
                .map(value -> value.replaceAll("[^0-9]", ""))
                .allMatch(value -> value.startsWith(digits)),
                "File có ngày tạo ngoài ngày " + expected);
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
