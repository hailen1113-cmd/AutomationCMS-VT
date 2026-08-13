package com.vuatho.tests.transaction.fee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Kiểm tra bố cục, dữ liệu và thống kê của từng loại Phí & Doanh thu. */
public class TransactionFeeOverviewTest extends TransactionFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionFeeOverviewTest.class,
                "Lịch sử giao dịch", "Phí & Doanh thu - Tổng quan");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_003,
            dataProvider = "feeSubtypes")
    public void showsExpectedFiltersAndColumns(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = transactionPage.layout();
        List<String> expected = subtype.type() == 8
                ? List.of("Người dùng", "Loại giao dịch", "Trạng thái", "Số tiền", "Ngày tạo")
                : List.of("Người dùng", "Loại giao dịch", "Trạng thái", "Số tiền",
                        "Cổng thanh toán", "Ngày tạo");
        Assert.assertEquals(result.headers(), expected);
        result.controls().forEach((control, visible) ->
                Assert.assertTrue(visible, "Thiếu control " + control + " trên " + subtype.label()));
        Assert.assertTrue(result.url().contains("tab=fee&type=" + subtype.type()), result.url());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_004,
            dataProvider = "feeSubtypes")
    public void rowsHaveValidFormats(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        verifyRowFormats();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_015,
            dataProvider = "feeSubtypes")
    public void rowsMatchSelectedSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var rows = transactionPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Không có dữ liệu cho " + subtype.label());
        rows.forEach(row -> Assert.assertEquals(row.value("Loại giao dịch"), subtype.label(),
                "Dòng không thuộc " + subtype.label() + ": " + row.signature()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_016,
            dataProvider = "feeSubtypes")
    public void summaryShowsRevenueCountAndCollectedAmount(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        if (subtype.type() == 8) {
            var result = transactionPage.feeConnectionOverview();
            Assert.assertTrue(result.transactionCount() >= transactionPage.rows().size());
            Assert.assertTrue(result.totalRevenue().signum() >= 0);
            Assert.assertTrue(result.collected().signum() >= 0);
            Assert.assertTrue(result.debt().signum() >= 0);
        } else if (subtype.type() == 9) {
            var result = transactionPage.feeWalletLinkOverview();
            Assert.assertTrue(result.totalCount() >= transactionPage.rows().size());
            Assert.assertTrue(result.totalAmount().signum() >= 0);
        } else {
            var result = transactionPage.feeMaterialShareOverview();
            Assert.assertTrue(result.totalOrders() >= transactionPage.rows().size());
            Assert.assertTrue(result.totalCollected().signum() >= 0);
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_017)
    public void feeConnectionTotalEqualsCollectedPlusDebtAndPercentagesEqualOneHundred() {
        var result = transactionPage.feeConnectionOverview();
        Assert.assertEquals(result.totalRevenue(), result.collected().add(result.debt()),
                "Tổng doanh thu khác Đã thu + Công nợ");
        Assert.assertEquals(result.collectedPercentage().add(result.debtPercentage()),
                new BigDecimal("100.0"), "Tổng tỷ lệ Đã thu + Công nợ không bằng 100%");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_018)
    public void feeConnectionDailyChartShowsPeriodControlsAndSevenDateLabels() {
        var result = transactionPage.feeConnectionOverview();
        Assert.assertTrue(result.text().contains("Doanh thu theo ngày"));
        Assert.assertTrue(result.weekControl(), "Thiếu Tuần này");
        Assert.assertTrue(result.monthControl(), "Thiếu Tháng này");
        Assert.assertTrue(result.customControl(), "Thiếu Tuỳ chỉnh");
        Assert.assertTrue(result.dateLabels().size() >= 2, "Biểu đồ thiếu khoảng ngày");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_031)
    public void walletLinkTotalsMatchThreeStatusBreakdowns() {
        openFeeSubtype(category().subtypes().get(1));
        var result = transactionPage.feeWalletLinkOverview();
        Assert.assertEquals(result.amounts().keySet(),
                java.util.Set.of("Hoàn thành", "Đang chờ", "Từ chối"));
        Assert.assertEquals(result.counts().keySet(),
                java.util.Set.of("Hoàn thành", "Đang chờ", "Từ chối"));
        Assert.assertEquals(result.totalAmount(), result.amounts().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        Assert.assertEquals(result.totalCount(), result.counts().values().stream()
                .mapToInt(Integer::intValue).sum());
        Assert.assertEquals(sum(result.amountPercentages()), new BigDecimal("100.0"));
        Assert.assertEquals(sum(result.countPercentages()), new BigDecimal("100.0"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_032)
    public void materialShareShowsCollectedAmountAndOrderCount() {
        openFeeSubtype(category().subtypes().get(2));
        var result = transactionPage.feeMaterialShareOverview();
        Assert.assertTrue(result.text().contains("Tổng phí chia sẻ vật tư"));
        Assert.assertTrue(result.text().contains("Tổng tiền thu"));
        Assert.assertTrue(result.text().contains("Tổng số đơn"));
        Assert.assertTrue(result.totalCollected().signum() >= 0);
        Assert.assertTrue(result.totalOrders() >= transactionPage.rows().size());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_050)
    public void feeConnectionChartHoverShowsDateAndRevenueValue() {
        var result = transactionPage.hoverChartNearHeading("Doanh thu theo ngày");
        Assert.assertEquals(result.chartCount(), 1, "Không tìm thấy chart Doanh thu theo ngày");
        Assert.assertFalse(result.tooltips().isEmpty(), "Hover chart không hiển thị tooltip");
        Assert.assertTrue(result.tooltips().stream().anyMatch(tooltip ->
                        tooltip.matches("(?s).*\\d{2}[/-]\\d{2}(?:[/-]\\d{4})?.*")
                                && tooltip.matches("(?s).*\\d[\\d.,]*.*")),
                "Tooltip không có ngày và doanh thu: " + result.tooltips());
        Assert.assertTrue(result.url().contains("tab=fee&type=8"), result.url());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_076,
            dataProvider = "feeSubtypes")
    public void overviewTotalCountMatchesFullFilteredResult(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        int displayedTotal = advancedPage().totalDisplayed();
        int overviewTotal = subtype.type() == 8
                ? transactionPage.feeConnectionOverview().transactionCount()
                : subtype.type() == 9
                ? transactionPage.feeWalletLinkOverview().totalCount()
                : transactionPage.feeMaterialShareOverview().totalOrders();
        Assert.assertEquals(overviewTotal, displayedTotal,
                "Tổng overview không bằng Tổng hiển thị của " + subtype.label());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_077)
    public void feeConnectionDonutMatchesCollectedAndDebtLegend() {
        var overview = transactionPage.feeConnectionOverview();
        var pie = transactionPage.feeConnectionPieOverview();
        Assert.assertEquals(pie.chartCount(), 1);
        Set<String> expected = new java.util.LinkedHashSet<>();
        if (overview.collected().signum() > 0) expected.add("Đã thu");
        if (overview.debt().signum() > 0) expected.add("Công nợ");
        Assert.assertEquals(pie.sectors().keySet(), expected);
        if (pie.sectors().containsKey("Đã thu")) {
            Assert.assertEquals(pie.sectors().get("Đã thu"), "#10b981");
        }
        if (pie.sectors().containsKey("Công nợ")) {
            Assert.assertEquals(pie.sectors().get("Công nợ"), "#f43f5e");
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_078)
    public void feeConnectionPercentagesMatchDisplayedAmounts() {
        var result = transactionPage.feeConnectionOverview();
        if (result.totalRevenue().signum() == 0) {
            Assert.assertEquals(result.collectedPercentage(), new BigDecimal("0.0"));
            Assert.assertEquals(result.debtPercentage(), new BigDecimal("0.0"));
            return;
        }
        Assert.assertEquals(result.collectedPercentage(), percentage(result.collected(), result.totalRevenue()));
        Assert.assertEquals(result.debtPercentage(), percentage(result.debt(), result.totalRevenue()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_079)
    public void debtOverviewShowsValidWorkersAmountsDatesAndPagination() {
        var result = transactionPage.openFeeDebtOverview();
        Assert.assertTrue(result.opened());
        Assert.assertEquals(result.headers(), List.of("Thợ", "Số tiền nợ", "Ngày nợ"));
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.pagination());
        result.rows().forEach(row -> {
            Assert.assertFalse(row.workerText().isBlank());
            Assert.assertTrue(row.workerUrl().contains("/vuatho/worker?id=" + row.workerId()));
            Assert.assertTrue(row.debt().signum() <= 0, "Số tiền nợ không âm: " + row.debt());
            Assert.assertFalse(row.debtDate().isAfter(LocalDateTime.now().plusMinutes(1)));
            Assert.assertTrue(row.debtDays() >= 0);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_080)
    public void debtOverviewSortsAmountAndDateBothDirections() {
        transactionPage.openFeeDebtOverview();
        var result = transactionPage.sortFeeDebtByAmountAndDate();
        assertOppositeOrders(result.amountFirst(), result.amountSecond(), Comparator.naturalOrder());
        assertOppositeOrders(result.debtDaysFirst(), result.debtDaysSecond(), Comparator.naturalOrder());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_081)
    public void debtOverviewPaginatesRestoresFirstPageAndCloses() {
        transactionPage.openFeeDebtOverview();
        var pagination = transactionPage.paginateFeeDebtAndReturn();
        Assert.assertNotEquals(pagination.secondPage(), pagination.firstPage());
        Assert.assertEquals(pagination.restoredFirstPage(), pagination.firstPage());
        var closed = transactionPage.closeFeeDebtOverview();
        Assert.assertTrue(closed.closed());
        Assert.assertTrue(closed.url().contains("tab=fee&type=8"));
        Assert.assertTrue(closed.activeText().contains("Phí kết nối"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_082)
    public void walletLinkTwoDonutsMatchNonZeroStatusBreakdownsAndColors() {
        openFeeSubtype(category().subtypes().get(1));
        var overview = transactionPage.feeWalletLinkOverview();
        var pies = transactionPage.feeWalletLinkPieOverview();
        assertStatusPie(pies.amountChart(), overview.amounts());
        Map<String, BigDecimal> counts = new java.util.LinkedHashMap<>();
        overview.counts().forEach((key, value) -> counts.put(key, BigDecimal.valueOf(value)));
        assertStatusPie(pies.countChart(), counts);
        Assert.assertTrue(pies.url().contains("tab=fee&type=9"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_084)
    public void switchingSubtypesShowsOnlyMatchingOverviewBlocks() {
        Map<Integer, List<String>> expected = Map.of(
                8, List.of("Tổng doanh thu", "Doanh thu theo ngày"),
                9, List.of("Tổng phí liên kết ví", "Tổng số giao dịch"),
                33, List.of("Tổng phí chia sẻ vật tư"));
        for (var subtype : category().subtypes()) {
            transactionPage.open(subtype);
            Assert.assertEquals(transactionPage.visibleFeeOverviewHeadings(), expected.get(subtype.type()),
                    "Overview còn sót dữ liệu type trước trên " + subtype.label());
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_085,
            dataProvider = "feeSubtypes")
    public void refreshingSubtypeKeepsMatchingOverviewAndRoute(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        List<String> before = transactionPage.visibleFeeOverviewHeadings();
        var result = transactionPage.refreshFeeOverview();
        Assert.assertEquals(result.headings(), before);
        Assert.assertTrue(result.url().contains("tab=fee&type=" + subtype.type()), result.url());
        Assert.assertTrue(result.activeText().contains(subtype.label()), result.activeText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_088)
    public void debtWorkerLinkOpensCorrectProfileAndRestoresModal() {
        transactionPage.openFeeDebtOverview();
        var result = transactionPage.openDebtWorkerAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/worker?id="), result.expectedUrl());
        Assert.assertEquals(queryValue(result.actualUrl(), "id"), queryValue(result.expectedUrl(), "id"));
        Assert.assertFalse(result.targetText().isBlank());
        Assert.assertTrue(result.modalRestored());
        Assert.assertTrue(result.sourceUrl().contains("tab=fee&type=8"), result.sourceUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_089)
    public void materialShareOverviewTotalEqualsAllTransactionPages() {
        openFeeSubtype(category().subtypes().get(2));
        var result = transactionPage.feeMaterialTotalAgainstAllPages();
        Assert.assertTrue(result.rowCount() > 0);
        Assert.assertEquals(result.overviewTotal(), result.tableTotal(),
                "Tổng Phí chia sẻ vật tư không bằng tổng " + result.rowCount()
                        + " giao dịch trên " + result.totalPages() + " trang");
        Assert.assertTrue(result.url().contains("tab=fee&type=33"), result.url());
    }

    private BigDecimal sum(Map<String, BigDecimal> values) {
        return values.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percentage(BigDecimal value, BigDecimal total) {
        return value.multiply(new BigDecimal("100")).divide(total, 1, RoundingMode.HALF_UP);
    }

    private void assertStatusPie(TransactionCategoryPage.FeePieSnapshot pie,
                                 Map<String, BigDecimal> values) {
        Assert.assertEquals(pie.chartCount(), 1);
        Set<String> expected = new java.util.LinkedHashSet<>();
        values.forEach((status, value) -> {
            if (value.signum() > 0) expected.add(status);
        });
        Assert.assertEquals(pie.sectors().keySet(), expected);
        Map<String, String> colors = Map.of(
                "Hoàn thành", "#4ade80", "Đang chờ", "#facc15", "Từ chối", "#f87171");
        pie.sectors().forEach((status, color) -> Assert.assertEquals(color, colors.get(status)));
    }

    private <T> void assertOppositeOrders(List<T> first, List<T> second,
                                          Comparator<? super T> comparator) {
        Assert.assertTrue(first.size() > 1 && second.size() > 1,
                "Không đủ dữ liệu kiểm tra hai chiều sort");
        List<T> ascending = new ArrayList<>(first);
        ascending.sort(comparator);
        List<T> descending = new ArrayList<>(ascending);
        descending.sort(comparator.reversed());
        boolean firstAscendingSecondDescending = first.equals(ascending) && second.equals(descending);
        boolean firstDescendingSecondAscending = first.equals(descending) && second.equals(ascending);
        Assert.assertTrue(firstAscendingSecondDescending || firstDescendingSecondAscending,
                "Hai lần sort không tạo thứ tự tăng/giảm đối nghịch: " + first + " | " + second);
    }

    private String queryValue(String url, String key) {
        var matcher = java.util.regex.Pattern.compile(
                "(?:[?&])" + java.util.regex.Pattern.quote(key) + "=([^&]+)").matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }
}
