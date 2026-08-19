package com.vuatho.tests.transaction.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionOrderTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Kiểm tra bố cục, dữ liệu bảng và hai biểu đồ tổng quan Đơn dịch vụ. */
public class TransactionOrderOverviewTest extends TransactionOrderTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionOrderOverviewTest.class,
                "Lịch sử giao dịch", "Đơn dịch vụ - Tổng quan");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_003)
    public void showsExpectedFiltersAndColumnsAcrossAllSubtypes() {
        verifyShowsExpectedFiltersAndColumnsAcrossAllSubtypesForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_108)
    public void showsExpectedFiltersAndColumnsAcrossAllSubtypesType22() {
        verifyShowsExpectedFiltersAndColumnsAcrossAllSubtypesForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_109)
    public void showsExpectedFiltersAndColumnsAcrossAllSubtypesType24() {
        verifyShowsExpectedFiltersAndColumnsAcrossAllSubtypesForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_110)
    public void showsExpectedFiltersAndColumnsAcrossAllSubtypesType36() {
        verifyShowsExpectedFiltersAndColumnsAcrossAllSubtypesForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_111)
    public void showsExpectedFiltersAndColumnsAcrossAllSubtypesType37() {
        verifyShowsExpectedFiltersAndColumnsAcrossAllSubtypesForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_112)
    public void showsExpectedFiltersAndColumnsAcrossAllSubtypesType15() {
        verifyShowsExpectedFiltersAndColumnsAcrossAllSubtypesForSubtype(subtype(15));
    }

    private void verifyShowsExpectedFiltersAndColumnsAcrossAllSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = transactionPage.layout();
        Assert.assertTrue(result.headers().containsAll(category().headers()),
                "Thiếu cột bắt buộc trên type=" + subtype.type() + ": " + result.headers());
        Assert.assertEquals(result.headers().stream().distinct().count(),
                (long) result.headers().size(), "Cột bị trùng: " + result.headers());
        result.controls().forEach((control, visible) ->
                Assert.assertTrue(visible, "Thiếu control " + control + " trên type=" + subtype.type()));
        Assert.assertTrue(result.url().contains("tab=order&type=" + subtype.type()), result.url());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_004)
    public void rowsHaveValidFormatsAcrossAllSubtypes() {
        verifyRowsHaveValidFormatsAcrossAllSubtypesForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_113)
    public void rowsHaveValidFormatsAcrossAllSubtypesType22() {
        verifyRowsHaveValidFormatsAcrossAllSubtypesForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_114)
    public void rowsHaveValidFormatsAcrossAllSubtypesType24() {
        verifyRowsHaveValidFormatsAcrossAllSubtypesForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_115)
    public void rowsHaveValidFormatsAcrossAllSubtypesType36() {
        verifyRowsHaveValidFormatsAcrossAllSubtypesForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_116)
    public void rowsHaveValidFormatsAcrossAllSubtypesType37() {
        verifyRowsHaveValidFormatsAcrossAllSubtypesForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_117)
    public void rowsHaveValidFormatsAcrossAllSubtypesType15() {
        verifyRowsHaveValidFormatsAcrossAllSubtypesForSubtype(subtype(15));
    }

    private void verifyRowsHaveValidFormatsAcrossAllSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        verifyRowFormats();
        transactionPage.rows().forEach(row ->
                Assert.assertFalse(row.value("Loại giao dịch").isBlank(), row.signature()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_038)
    public void amountAndCountTotalsMatchThreeStatusBreakdowns() {
        verifyAmountAndCountTotalsMatchThreeStatusBreakdownsForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_118)
    public void amountAndCountTotalsMatchThreeStatusBreakdownsType22() {
        verifyAmountAndCountTotalsMatchThreeStatusBreakdownsForSubtype(subtype(22));
    }

    private void verifyAmountAndCountTotalsMatchThreeStatusBreakdownsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = transactionPage.categoryStatusOverview();
        Assert.assertEquals(result.amounts().keySet(),
                Set.of("Hoàn thành", "Đang chờ", "Từ chối"));
        Assert.assertEquals(result.totalAmount(), sum(result.amounts().values()));
        assertPercentTotal(result.amountPercentages().values(), result.totalAmount());
        if (result.totalCount() != null) {
            Assert.assertEquals(result.counts().keySet(),
                    Set.of("Hoàn thành", "Đang chờ", "Từ chối"));
            Assert.assertEquals(result.totalCount().intValue(), result.counts().values().stream()
                    .mapToInt(Integer::intValue).sum());
            assertPercentTotal(result.countPercentages().values(),
                    BigDecimal.valueOf(result.totalCount()));
        } else {
            Assert.assertTrue(result.counts().isEmpty());
            Assert.assertTrue(result.countHeading().isBlank());
        }
        Assert.assertTrue(result.url().contains("tab=order&type=" + subtype.type()), result.url());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_039)
    public void donutSectorsMatchNonZeroBreakdownsAndColors() {
        verifyDonutSectorsMatchNonZeroBreakdownsAndColorsForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_119)
    public void donutSectorsMatchNonZeroBreakdownsAndColorsType22() {
        verifyDonutSectorsMatchNonZeroBreakdownsAndColorsForSubtype(subtype(22));
    }

    private void verifyDonutSectorsMatchNonZeroBreakdownsAndColorsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var overview = transactionPage.categoryStatusOverview();
        var charts = transactionPage.categoryStatusPieOverview();
        assertPie(charts.amountChart(), overview.amounts());
        if (overview.totalCount() == null) {
            Assert.assertNull(charts.countChart());
        } else {
            Map<String, BigDecimal> counts = new java.util.LinkedHashMap<>();
            overview.counts().forEach((key, value) -> counts.put(key, BigDecimal.valueOf(value)));
            assertPie(charts.countChart(), counts);
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_040)
    public void refreshKeepsOverviewDataRouteAndSubtype() {
        verifyRefreshKeepsOverviewDataRouteAndSubtypeForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_120)
    public void refreshKeepsOverviewDataRouteAndSubtypeType22() {
        verifyRefreshKeepsOverviewDataRouteAndSubtypeForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_121)
    public void refreshKeepsOverviewDataRouteAndSubtypeType24() {
        verifyRefreshKeepsOverviewDataRouteAndSubtypeForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_122)
    public void refreshKeepsOverviewDataRouteAndSubtypeType36() {
        verifyRefreshKeepsOverviewDataRouteAndSubtypeForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_123)
    public void refreshKeepsOverviewDataRouteAndSubtypeType37() {
        verifyRefreshKeepsOverviewDataRouteAndSubtypeForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_124)
    public void refreshKeepsOverviewDataRouteAndSubtypeType15() {
        verifyRefreshKeepsOverviewDataRouteAndSubtypeForSubtype(subtype(15));
    }

    private void verifyRefreshKeepsOverviewDataRouteAndSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var beforeHeadings = transactionPage.categoryOverviewHeadings();
        Assert.assertFalse(beforeHeadings.isEmpty(), "Thiếu tiêu đề tổng quan type=" + subtype.type());
        if (subtype.type() == 2 || subtype.type() == 22) {
            var before = transactionPage.categoryStatusOverview();
            var refreshed = transactionPage.refreshCategoryStatusOverview();
            Assert.assertEquals(refreshed.headings(), beforeHeadings);
            Assert.assertEquals(refreshed.overview().totalAmount(), before.totalAmount());
            Assert.assertEquals(refreshed.overview().totalCount(), before.totalCount());
            Assert.assertEquals(refreshed.overview().amounts(), before.amounts());
            Assert.assertEquals(refreshed.overview().counts(), before.counts());
            Assert.assertTrue(refreshed.url().contains("tab=order&type=" + subtype.type()),
                    refreshed.url());
            Assert.assertTrue(refreshed.activeText().contains(subtype.label()),
                    refreshed.activeText());
        } else {
            driver.navigate().refresh();
            transactionPage.rows();
            Assert.assertEquals(transactionPage.categoryOverviewHeadings(), beforeHeadings);
            Assert.assertTrue(transactionPage.currentUrl().contains(
                    "tab=order&type=" + subtype.type()), transactionPage.currentUrl());
            Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()));
        }
    }

    private BigDecimal sum(java.util.Collection<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void assertPercentTotal(java.util.Collection<BigDecimal> values, BigDecimal totalValue) {
        BigDecimal total = sum(values);
        if (totalValue.signum() == 0) {
            Assert.assertEquals(total.compareTo(BigDecimal.ZERO), 0);
            return;
        }
        Assert.assertTrue(total.compareTo(new BigDecimal("99.9")) >= 0
                        && total.compareTo(new BigDecimal("100.1")) <= 0,
                "Tổng tỷ lệ không xấp xỉ 100%: " + total);
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
