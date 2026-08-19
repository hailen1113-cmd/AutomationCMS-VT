package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionInsuranceTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Kiểm tra bố cục, dữ liệu bảng và hai biểu đồ tổng quan VT Care. */
public class TransactionInsuranceOverviewTest extends TransactionInsuranceTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionInsuranceOverviewTest.class,
                "Lịch sử giao dịch", "VT Care - Tổng quan");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_003)
    public void showsExpectedFiltersAndColumns() {
        verifyShowsExpectedFiltersAndColumnsForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_118)
    public void showsExpectedFiltersAndColumnsType26() {
        verifyShowsExpectedFiltersAndColumnsForSubtype(subtype(26));
    }

    private void verifyShowsExpectedFiltersAndColumnsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifyLayout(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_004)
    public void rowsHaveValidFormats() {
        verifyRowsHaveValidFormatsForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_119)
    public void rowsHaveValidFormatsType26() {
        verifyRowsHaveValidFormatsForSubtype(subtype(26));
    }

    private void verifyRowsHaveValidFormatsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifyRowFormats();
        transactionPage.rows().forEach(row -> Assert.assertEquals(
                row.value("Loại giao dịch"), subtype.type() == 25
                        ? "Trừ phí VT Care hàng ngày/tháng"
                        : "Hoàn phí VT Care khi hủy gói"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_015)
    public void amountAndCountTotalsMatchThreeStatusBreakdowns() {
        verifyAmountAndCountTotalsMatchThreeStatusBreakdownsForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_120)
    public void amountAndCountTotalsMatchThreeStatusBreakdownsType26() {
        verifyAmountAndCountTotalsMatchThreeStatusBreakdownsForSubtype(subtype(26));
    }

    private void verifyAmountAndCountTotalsMatchThreeStatusBreakdownsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = transactionPage.insuranceOverview(subtype);
        Assert.assertEquals(result.amounts().keySet(), Set.of("Hoàn thành", "Đang chờ", "Từ chối"));
        Assert.assertEquals(result.counts().keySet(), Set.of("Hoàn thành", "Đang chờ", "Từ chối"));
        Assert.assertEquals(result.totalAmount(), result.amounts().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        Assert.assertEquals(result.totalCount(), result.counts().values().stream()
                .mapToInt(Integer::intValue).sum());
        assertPercentTotal(result.amountPercentages().values(), result.totalAmount());
        assertPercentTotal(result.countPercentages().values(), BigDecimal.valueOf(result.totalCount()));
        Assert.assertTrue(result.url().contains("tab=insurance&type=" + subtype.type()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_016)
    public void overviewCountMatchesFullFilteredResult() {
        verifyOverviewCountMatchesFullFilteredResultForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_121)
    public void overviewCountMatchesFullFilteredResultType26() {
        verifyOverviewCountMatchesFullFilteredResultForSubtype(subtype(26));
    }

    private void verifyOverviewCountMatchesFullFilteredResultForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        Assert.assertEquals(transactionPage.insuranceOverview(subtype).totalCount(),
                advancedPage().totalDisplayed());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_098)
    public void headingsAndTwoDonutChartsRenderForBothSubtypes() {
        verifyHeadingsAndTwoDonutChartsRenderForBothSubtypesForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_122)
    public void headingsAndTwoDonutChartsRenderForBothSubtypesType26() {
        verifyHeadingsAndTwoDonutChartsRenderForBothSubtypesForSubtype(subtype(26));
    }

    private void verifyHeadingsAndTwoDonutChartsRenderForBothSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        List<String> headings = transactionPage.visibleInsuranceOverviewHeadings();
        String expectedAmount = subtype.type() == 25
                ? "Tổng trừ phí vt care hàng ngày/tháng"
                : "Tổng hoàn phí vt care khi hủy gói";
        Assert.assertEquals(headings, List.of(expectedAmount, "Tổng số giao dịch"));
        var charts = transactionPage.insurancePieOverview(subtype);
        Assert.assertEquals(charts.amountChart().chartCount(), 1);
        Assert.assertEquals(charts.countChart().chartCount(), 1);
        Assert.assertTrue(charts.url().contains("tab=insurance&type=" + subtype.type()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_099)
    public void donutSectorsMatchNonZeroBreakdownsAndColors() {
        verifyDonutSectorsMatchNonZeroBreakdownsAndColorsForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_123)
    public void donutSectorsMatchNonZeroBreakdownsAndColorsType26() {
        verifyDonutSectorsMatchNonZeroBreakdownsAndColorsForSubtype(subtype(26));
    }

    private void verifyDonutSectorsMatchNonZeroBreakdownsAndColorsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var overview = transactionPage.insuranceOverview(subtype);
        var charts = transactionPage.insurancePieOverview(subtype);
        assertPie(charts.amountChart(), overview.amounts());
        Map<String, BigDecimal> counts = new java.util.LinkedHashMap<>();
        overview.counts().forEach((key, value) -> counts.put(key, BigDecimal.valueOf(value)));
        assertPie(charts.countChart(), counts);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_100)
    public void valuesAreNonNegativeAndIndividualPercentagesAreCorrect() {
        verifyValuesAreNonNegativeAndIndividualPercentagesAreCorrectForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_124)
    public void valuesAreNonNegativeAndIndividualPercentagesAreCorrectType26() {
        verifyValuesAreNonNegativeAndIndividualPercentagesAreCorrectForSubtype(subtype(26));
    }

    private void verifyValuesAreNonNegativeAndIndividualPercentagesAreCorrectForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = transactionPage.insuranceOverview(subtype);
        result.amounts().forEach((status, value) -> {
            Assert.assertTrue(value.signum() >= 0, "Tiền âm tại " + status);
            Assert.assertEquals(result.amountPercentages().get(status),
                    percentage(value, result.totalAmount()), "Sai tỷ lệ tiền " + status);
        });
        result.counts().forEach((status, value) -> {
            Assert.assertTrue(value >= 0, "Số lượng âm tại " + status);
            Assert.assertEquals(result.countPercentages().get(status),
                    percentage(BigDecimal.valueOf(value), BigDecimal.valueOf(result.totalCount())),
                    "Sai tỷ lệ số giao dịch " + status);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_101)
    public void switchingSubtypesReplacesOverviewWithoutStaleHeading() {
        var type25 = category().subtypes().stream().filter(value -> value.type() == 25).findFirst().orElseThrow();
        var type26 = category().subtypes().stream().filter(value -> value.type() == 26).findFirst().orElseThrow();
        openInsuranceSubtype(type25);
        Assert.assertEquals(transactionPage.visibleInsuranceOverviewHeadings(), List.of(
                "Tổng trừ phí vt care hàng ngày/tháng", "Tổng số giao dịch"));
        openInsuranceSubtype(type26);
        Assert.assertEquals(transactionPage.visibleInsuranceOverviewHeadings(), List.of(
                "Tổng hoàn phí vt care khi hủy gói", "Tổng số giao dịch"));
        openInsuranceSubtype(type25);
        Assert.assertEquals(transactionPage.visibleInsuranceOverviewHeadings(), List.of(
                "Tổng trừ phí vt care hàng ngày/tháng", "Tổng số giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_102)
    public void refreshKeepsOverviewDataRouteAndSubtype() {
        verifyRefreshKeepsOverviewDataRouteAndSubtypeForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_125)
    public void refreshKeepsOverviewDataRouteAndSubtypeType26() {
        verifyRefreshKeepsOverviewDataRouteAndSubtypeForSubtype(subtype(26));
    }

    private void verifyRefreshKeepsOverviewDataRouteAndSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var before = transactionPage.insuranceOverview(subtype);
        var refreshed = transactionPage.refreshInsuranceOverview(subtype);
        Assert.assertEquals(refreshed.overview().totalAmount(), before.totalAmount());
        Assert.assertEquals(refreshed.overview().totalCount(), before.totalCount());
        Assert.assertEquals(refreshed.overview().amounts(), before.amounts());
        Assert.assertEquals(refreshed.overview().counts(), before.counts());
        Assert.assertTrue(refreshed.url().contains("tab=insurance&type=" + subtype.type()));
        Assert.assertTrue(refreshed.activeText().contains(subtype.label()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_103)
    public void initialOverviewFinishesLoadingWithinPageSize() {
        verifyInitialOverviewFinishesLoadingWithinPageSizeForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_126)
    public void initialOverviewFinishesLoadingWithinPageSizeType26() {
        verifyInitialOverviewFinishesLoadingWithinPageSizeForSubtype(subtype(26));
    }

    private void verifyInitialOverviewFinishesLoadingWithinPageSizeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = transactionPage.initialRender();
        Assert.assertFalse(result.loading());
        Assert.assertTrue(result.visibleRows() <= 20);
        Assert.assertTrue(result.visibleRows() > 0
                || result.empty() && result.text().contains("Chưa có dữ liệu"));
    }

    private BigDecimal sum(java.util.Collection<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void assertPercentTotal(java.util.Collection<BigDecimal> values, BigDecimal totalValue) {
        BigDecimal total = sum(values);
        if (totalValue.signum() == 0) {
            Assert.assertEquals(total, BigDecimal.ZERO.setScale(1));
            return;
        }
        Assert.assertTrue(total.compareTo(new BigDecimal("99.9")) >= 0
                && total.compareTo(new BigDecimal("100.1")) <= 0,
                "Tổng tỷ lệ không xấp xỉ 100%: " + total);
    }

    private BigDecimal percentage(BigDecimal value, BigDecimal total) {
        return total.signum() == 0 ? BigDecimal.ZERO.setScale(1)
                : value.multiply(new BigDecimal("100"))
                        .divide(total, 1, RoundingMode.HALF_UP);
    }

    private void assertPie(TransactionCategoryPage.FeePieSnapshot pie,
                           Map<String, BigDecimal> values) {
        Assert.assertEquals(pie.chartCount(), 1);
        Set<String> expected = new LinkedHashSet<>();
        values.forEach((status, value) -> {
            if (value.signum() > 0) expected.add(status);
        });
        Assert.assertEquals(pie.sectors().keySet(), expected);
        Map<String, String> colors = Map.of(
                "Hoàn thành", "#4ade80", "Đang chờ", "#facc15", "Từ chối", "#f87171");
        pie.sectors().forEach((status, color) -> Assert.assertEquals(color, colors.get(status)));
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
