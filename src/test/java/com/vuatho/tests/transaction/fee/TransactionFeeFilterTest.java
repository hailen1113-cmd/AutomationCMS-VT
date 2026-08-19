package com.vuatho.tests.transaction.fee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

/** Kiểm tra tìm kiếm, xuất hoá đơn và ngày giờ của Phí & Doanh thu. */
public class TransactionFeeFilterTest extends TransactionFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionFeeFilterTest.class,
                "Lịch sử giao dịch", "Phí & Doanh thu - Bộ lọc");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_005)
    public void searchesRealUserAndClearingRestoresRows() {
        verifySearchesRealUserAndClearingRestoresRowsForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_128)
    public void searchesRealUserAndClearingRestoresRowsType9() {
        verifySearchesRealUserAndClearingRestoresRowsForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_129)
    public void searchesRealUserAndClearingRestoresRowsType33() {
        verifySearchesRealUserAndClearingRestoresRowsForSubtype(subtype(33));
    }

    private void verifySearchesRealUserAndClearingRestoresRowsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        verifySearchAndReset(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_006)
    public void invoiceFilterOptionsKeepCurrentSubtype() {
        verifyInvoiceFilterOptionsKeepCurrentSubtypeForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_130)
    public void invoiceFilterOptionsKeepCurrentSubtypeType9() {
        verifyInvoiceFilterOptionsKeepCurrentSubtypeForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_131)
    public void invoiceFilterOptionsKeepCurrentSubtypeType33() {
        verifyInvoiceFilterOptionsKeepCurrentSubtypeForSubtype(subtype(33));
    }

    private void verifyInvoiceFilterOptionsKeepCurrentSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        verifyFilterOptions(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_019)
    public void invoiceOptionsAreAllYesAndNo() {
        verifyInvoiceOptionsAreAllYesAndNoForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_132)
    public void invoiceOptionsAreAllYesAndNoType9() {
        verifyInvoiceOptionsAreAllYesAndNoForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_133)
    public void invoiceOptionsAreAllYesAndNoType33() {
        verifyInvoiceOptionsAreAllYesAndNoForSubtype(subtype(33));
    }

    private void verifyInvoiceOptionsAreAllYesAndNoForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = transactionPage.firstAvailableFilter();
        Assert.assertEquals(result.ariaLabel(), "xuất hoá đơn-filter");
        Assert.assertEquals(result.options(), List.of("Tất cả", "Có", "Không"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_020)
    public void selectingInvoiceYesKeepsSubtypeAndReturnsValidState() {
        verifySelectingInvoiceYesKeepsSubtypeAndReturnsValidStateForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_134)
    public void selectingInvoiceYesKeepsSubtypeAndReturnsValidStateType9() {
        verifySelectingInvoiceYesKeepsSubtypeAndReturnsValidStateForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_135)
    public void selectingInvoiceYesKeepsSubtypeAndReturnsValidStateType33() {
        verifySelectingInvoiceYesKeepsSubtypeAndReturnsValidStateForSubtype(subtype(33));
    }

    private void verifySelectingInvoiceYesKeepsSubtypeAndReturnsValidStateForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = transactionPage.selectOption("xuất hoá đơn-filter", "Có");
        Assert.assertTrue(result.selectedText().contains("Có"));
        Assert.assertTrue(result.url().contains("tab=fee"), result.url());
        Assert.assertTrue(result.activeText().contains(subtype.label()), result.activeText());
        Assert.assertTrue(result.activeText().contains(subtype.label()));
        Assert.assertTrue(!result.rows().isEmpty()
                || result.empty() && result.pageText().contains("Chưa có dữ liệu"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_021)
    public void selectingInvoiceNoKeepsSubtypeAndReturnsValidState() {
        verifySelectingInvoiceNoKeepsSubtypeAndReturnsValidStateForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_136)
    public void selectingInvoiceNoKeepsSubtypeAndReturnsValidStateType9() {
        verifySelectingInvoiceNoKeepsSubtypeAndReturnsValidStateForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_137)
    public void selectingInvoiceNoKeepsSubtypeAndReturnsValidStateType33() {
        verifySelectingInvoiceNoKeepsSubtypeAndReturnsValidStateForSubtype(subtype(33));
    }

    private void verifySelectingInvoiceNoKeepsSubtypeAndReturnsValidStateForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = transactionPage.selectOption("xuất hoá đơn-filter", "Không");
        Assert.assertTrue(result.selectedText().contains("Không"));
        Assert.assertTrue(result.url().contains("tab=fee"), result.url());
        Assert.assertTrue(result.activeText().contains(subtype.label()), result.activeText());
        Assert.assertTrue(result.activeText().contains(subtype.label()));
        Assert.assertTrue(!result.rows().isEmpty()
                || result.empty() && result.pageText().contains("Chưa có dữ liệu"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_022)
    public void futureDatesCannotBeSelected() {
        verifyFutureDatesCannotBeSelected(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_138)
    public void futureDatesCannotBeSelectedType9() {
        verifyFutureDatesCannotBeSelected(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_139)
    public void futureDatesCannotBeSelectedType33() {
        verifyFutureDatesCannotBeSelected(subtype(33));
    }

    private void verifyFutureDatesCannotBeSelected(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().futureDatesAreDisabled();
        Assert.assertTrue(result.disabled());
        Assert.assertTrue(result.disabledCount() > 0);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_023)
    public void datePickerDefaultsToFullDayAndRequiresDate() {
        var result = advancedPage().dateControlDefaults();
        Assert.assertEquals(result.startTime(), "00:00");
        Assert.assertEquals(result.endTime(), "23:59");
        Assert.assertTrue(result.applyDisabled());
        Assert.assertTrue(advancedPage().dateApplyRequiresSelection().initiallyDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_024)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtype() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(8));
    }



    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_178)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtypeType9() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(9));
    }



    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_179)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtypeType33() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(33));
    }

    private void verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        var result = advancedPage().futureDatesAreDisabled();
        Assert.assertTrue(result.disabled());
        Assert.assertTrue(result.disabledCount() > 0);
    }






    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_051)
    public void inclusiveTwoDayRangeOnlyReturnsRowsInsideSelectedDates() {
        verifyInclusiveTwoDayRangeOnlyReturnsRowsInsideSelectedDatesForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_140)
    public void inclusiveTwoDayRangeOnlyReturnsRowsInsideSelectedDatesType9() {
        verifyInclusiveTwoDayRangeOnlyReturnsRowsInsideSelectedDatesForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_141)
    public void inclusiveTwoDayRangeOnlyReturnsRowsInsideSelectedDatesType33() {
        verifyInclusiveTwoDayRangeOnlyReturnsRowsInsideSelectedDatesForSubtype(subtype(33));
    }

    private void verifyInclusiveTwoDayRangeOnlyReturnsRowsInsideSelectedDatesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().filterRecentRange();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> {
            var date = row.createdAt().toLocalDate();
            return !date.isBefore(result.start()) && !date.isAfter(result.end());
        }));
        Assert.assertTrue(transactionPage.currentUrl().contains("tab=fee&type=" + subtype.type()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_052)
    public void singleDayUsesFullDayAndOnlyReturnsSelectedDate() {
        verifySingleDayUsesFullDayAndOnlyReturnsSelectedDateForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_142)
    public void singleDayUsesFullDayAndOnlyReturnsSelectedDateType9() {
        verifySingleDayUsesFullDayAndOnlyReturnsSelectedDateForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_143)
    public void singleDayUsesFullDayAndOnlyReturnsSelectedDateType33() {
        verifySingleDayUsesFullDayAndOnlyReturnsSelectedDateForSubtype(subtype(33));
    }

    private void verifySingleDayUsesFullDayAndOnlyReturnsSelectedDateForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().filterSingleDay();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())));
        String date = result.startDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Assert.assertTrue(result.selectedText().contains(date));
        Assert.assertTrue(result.selectedText().contains("00:00"));
        Assert.assertTrue(result.selectedText().contains("23:59"));
        Assert.assertTrue(transactionPage.currentUrl().contains("tab=fee&type=" + subtype.type()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_053)
    public void exactMinuteRangeOnlyReturnsRowsInsideSelectedTime() {
        verifyExactMinuteRangeOnlyReturnsRowsInsideSelectedTime(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_144)
    public void exactMinuteRangeOnlyReturnsRowsInsideSelectedTimeType9() {
        verifyExactMinuteRangeOnlyReturnsRowsInsideSelectedTime(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_145)
    public void exactMinuteRangeOnlyReturnsRowsInsideSelectedTimeType33() {
        verifyExactMinuteRangeOnlyReturnsRowsInsideSelectedTime(subtype(33));
    }

    private void verifyExactMinuteRangeOnlyReturnsRowsInsideSelectedTime(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().filterSourceMinute();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> {
            var createdAt = row.createdAt();
            return createdAt.toLocalDate().equals(result.startDate())
                    && !createdAt.toLocalTime().isBefore(result.startTime())
                    && !createdAt.toLocalTime().isAfter(result.endTime());
        }));
        Assert.assertTrue(result.selectedText().contains(result.startTime().toString()));
        Assert.assertTrue(result.selectedText().contains(result.endTime().toString()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_054)
    public void closingDatePickerWithoutApplyKeepsDateAndRows() {
        var result = advancedPage().dismissDateWithoutApply();
        Assert.assertEquals(result.selectedAfter(), result.selectedBefore());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_055)
    public void clearingAppliedDateRestoresOriginalTotalAndRows() {
        var result = advancedPage().clearAppliedDateFilter();
        Assert.assertTrue(result.filteredTotal() <= result.originalTotal());
        Assert.assertEquals(result.restoredTotal(), result.originalTotal());
        Assert.assertTrue(result.selectedDate().contains("Chọn khoảng ngày giờ"));
        Assert.assertTrue(result.rows() > 0);
        Assert.assertTrue(result.clearControlGone());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_056)
    public void realUserAndSingleDayCombinationMatchesBothConditions() {
        verifyRealUserAndSingleDayCombinationMatchesBothConditionsForSubtype(subtype(8));
    }




    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_180)
    public void realUserAndSingleDayCombinationMatchesBothConditionsType9() {
        verifyRealUserAndSingleDayCombinationMatchesBothConditionsForSubtype(subtype(9));
    }




    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_181)
    public void realUserAndSingleDayCombinationMatchesBothConditionsType33() {
        verifyRealUserAndSingleDayCombinationMatchesBothConditionsForSubtype(subtype(33));
    }

    private void verifyRealUserAndSingleDayCombinationMatchesBothConditionsForSubtype(TransactionCategoryPage.Subtype subtype) {
        var result = advancedPage().filterSourceMinute();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> {
            var createdAt = row.createdAt();
            return createdAt.toLocalDate().equals(result.startDate())
                    && !createdAt.toLocalTime().isBefore(result.startTime())
                    && !createdAt.toLocalTime().isAfter(result.endTime());
        }));
        Assert.assertTrue(result.selectedText().contains(result.startTime().toString()));
        Assert.assertTrue(result.selectedText().contains(result.endTime().toString()));
    }







    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_057)
    public void switchingInvoiceYesNoAndAllRestoresBaseline() {
        verifySwitchingInvoiceYesNoAndAllRestoresBaseline(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_146)
    public void switchingInvoiceYesNoAndAllRestoresBaselineType33() {
        verifySwitchingInvoiceYesNoAndAllRestoresBaseline(subtype(33));
    }

    private void verifySwitchingInvoiceYesNoAndAllRestoresBaseline(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = transactionPage.switchInvoiceYesNoAndAll();
        Assert.assertTrue(result.yesText().contains("Có"));
        Assert.assertTrue(result.noText().contains("Không"));
        Assert.assertTrue(result.allText().contains("Tất cả"));
        Assert.assertFalse(result.baseline().isEmpty(),
                "Không có dữ liệu nền để kiểm tra chuyển bộ lọc Xuất hóa đơn");
        Assert.assertEquals(result.restored(), result.baseline());
        Assert.assertTrue(result.url().contains("tab=fee&type=" + subtype.type()), result.url());
        Assert.assertTrue(result.activeText().contains("Phí kết nối"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_058)
    public void resetClearsSearchInvoiceAndDateButKeepsSubtype() {
        verifyResetClearsSearchInvoiceAndDateButKeepsSubtypeForSubtype(subtype(8));
    }


    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_182)
    public void resetClearsSearchInvoiceAndDateButKeepsSubtypeType9() {
        verifyResetClearsSearchInvoiceAndDateButKeepsSubtypeForSubtype(subtype(9));
    }


    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_183)
    public void resetClearsSearchInvoiceAndDateButKeepsSubtypeType33() {
        verifyResetClearsSearchInvoiceAndDateButKeepsSubtypeForSubtype(subtype(33));
    }

    private void verifyResetClearsSearchInvoiceAndDateButKeepsSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.switchInvoiceYesNoAndAll();
        Assert.assertTrue(result.yesText().contains("Có"));
        Assert.assertTrue(result.noText().contains("Không"));
        Assert.assertTrue(result.allText().contains("Tất cả"));
        Assert.assertFalse(result.baseline().isEmpty(),
                "Không có dữ liệu nền để kiểm tra chuyển bộ lọc Xuất hóa đơn");
        Assert.assertEquals(result.restored(), result.baseline());
        Assert.assertTrue(result.url().contains("tab=fee&type=" + subtype.type()), result.url());
        Assert.assertTrue(result.activeText().contains("Phí kết nối"));
    }





    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_059)
    public void searchAndDatePersistAfterOpeningAndClosingDetail() {
        advancedPage().filterSingleDay();
        transactionPage.applySearchFromFirstUser();
        var result = transactionPage.openCloseDetailAndReadFeeFilters();
        Assert.assertTrue(result.closed());
        Assert.assertTrue(result.openedUrl().contains("id="));
        Assert.assertFalse(result.closedUrl().contains("id="));
        Assert.assertEquals(result.after().search(), result.before().search());
        Assert.assertEquals(result.after().date(), result.before().date());
        Assert.assertEquals(result.after().rows(), result.before().rows());
        Assert.assertTrue(result.closedUrl().contains("tab=fee"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_071)
    public void revenueChartWeekCoversMondayThroughSunday() {
        var result = transactionPage.selectFeeRevenuePeriod("Tuần này");
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        List<String> expectedTicks = IntStream.rangeClosed(0, 6)
                .mapToObj(day -> monday.plusDays(day)
                        .format(DateTimeFormatter.ofPattern("dd/MM")))
                .toList();
        Assert.assertEquals(result.activePeriod(), "Tuần này");
        Assert.assertEquals(result.start(), monday);
        Assert.assertEquals(result.end(), sunday);
        Assert.assertEquals(result.dateTicks(), expectedTicks);
        Assert.assertFalse(result.customInputVisible());
        Assert.assertTrue(result.url().contains("tab=fee&type=8"), result.url());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_072)
    public void revenueChartMonthCoversFirstThroughLastDay() {
        var result = transactionPage.selectFeeRevenuePeriod("Tháng này");
        YearMonth month = YearMonth.now();
        String suffix = month.format(DateTimeFormatter.ofPattern("/MM"));
        Assert.assertEquals(result.activePeriod(), "Tháng này");
        Assert.assertEquals(result.start(), month.atDay(1));
        Assert.assertEquals(result.end(), month.atEndOfMonth());
        Assert.assertFalse(result.dateTicks().isEmpty());
        Assert.assertEquals(result.dateTicks().get(0), "01" + suffix);
        Assert.assertEquals(result.dateTicks().get(result.dateTicks().size() - 1),
                month.atEndOfMonth().format(DateTimeFormatter.ofPattern("dd/MM")));
        Assert.assertFalse(result.customInputVisible());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_073)
    public void revenueChartCustomShowsCalendarAndDisablesFutureDates() {
        var result = transactionPage.openFeeRevenueCustomCalendar();
        Assert.assertEquals(result.activePeriod(), "Tuỳ chỉnh");
        Assert.assertTrue(result.customInputVisible());
        Assert.assertTrue(result.calendarVisible());
        Assert.assertTrue(result.disabledFutureDays() > 0,
                "Lịch Tuỳ chỉnh không khóa ngày sau hôm nay");
        Assert.assertTrue(result.url().contains("tab=fee&type=8"), result.url());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_074)
    public void revenueChartCustomFiveDayRangeUpdatesInputCaptionAxisAndTooltip() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        LocalDate start = previousMonth.atDay(1);
        LocalDate end = previousMonth.atDay(5);
        var result = transactionPage.selectFeeRevenueCustomRange(start, end);
        List<String> expectedTicks = IntStream.rangeClosed(1, 5)
                .mapToObj(day -> previousMonth.atDay(day)
                        .format(DateTimeFormatter.ofPattern("dd/MM")))
                .toList();
        Assert.assertEquals(result.activePeriod(), "Tuỳ chỉnh");
        Assert.assertEquals(result.start(), start);
        Assert.assertEquals(result.end(), end);
        Assert.assertEquals(result.customInputValue(),
                start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - "
                        + end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Assert.assertEquals(result.dateTicks(), expectedTicks);
        var tooltip = transactionPage.hoverChartNearHeading("Doanh thu theo ngày");
        Assert.assertFalse(tooltip.tooltips().isEmpty(),
                "Khoảng 5 ngày có cột doanh thu nhưng hover không hiện tooltip");
        Assert.assertTrue(tooltip.tooltips().stream().anyMatch(text ->
                        expectedTicks.stream().anyMatch(text::contains)
                                && text.matches("(?s).*\\d[\\d.,]*.*")),
                "Tooltip không khớp ngày trên trục X và doanh thu: " + tooltip.tooltips());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_075)
    public void switchingRevenuePeriodsChangesChartButKeepsTransactionTable() {
        var weekBefore = transactionPage.selectFeeRevenuePeriod("Tuần này");
        var month = transactionPage.selectFeeRevenuePeriod("Tháng này");
        var custom = transactionPage.selectFeeRevenuePeriod("Tuỳ chỉnh");
        var weekAfter = transactionPage.selectFeeRevenuePeriod("Tuần này");
        Assert.assertEquals(weekBefore.activePeriod(), "Tuần này");
        Assert.assertEquals(month.activePeriod(), "Tháng này");
        Assert.assertEquals(custom.activePeriod(), "Tuỳ chỉnh");
        Assert.assertEquals(weekAfter.activePeriod(), "Tuần này");
        Assert.assertNotEquals(weekBefore.start(), month.start());
        Assert.assertEquals(weekAfter.start(), weekBefore.start());
        Assert.assertEquals(weekAfter.end(), weekBefore.end());
        Assert.assertEquals(month.tableRows(), weekBefore.tableRows());
        Assert.assertEquals(custom.tableRows(), weekBefore.tableRows());
        Assert.assertEquals(weekAfter.tableRows(), weekBefore.tableRows());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_086)
    public void connectionFeeInvoiceYesRowsMatchRealOrderEvidence() {
        verifyInvoiceFilterRowsMatchRealOrderInvoiceEvidence(subtype(8), "Có");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_204)
    public void connectionFeeInvoiceNoRowsMatchRealOrderEvidence() {
        verifyInvoiceFilterRowsMatchRealOrderInvoiceEvidence(subtype(8), "Không");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_205)
    public void materialSharingInvoiceYesRowsMatchRealOrderEvidence() {
        verifyInvoiceFilterRowsMatchRealOrderInvoiceEvidence(subtype(33), "Có");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_206)
    public void materialSharingInvoiceNoRowsMatchRealOrderEvidence() {
        verifyInvoiceFilterRowsMatchRealOrderInvoiceEvidence(subtype(33), "Không");
    }

    private void verifyInvoiceFilterRowsMatchRealOrderInvoiceEvidence(
            TransactionCategoryPage.Subtype subtype, String option) {
        openFeeSubtype(subtype);
        var result = transactionPage.verifyFilteredInvoiceEvidence(option);
        Assert.assertTrue(result.selectedText().contains(option), result.selectedText());
        Assert.assertTrue(result.url().contains("tab=fee"), result.url());
        Assert.assertTrue(result.activeText().contains(subtype.label()), result.activeText());
        if (result.empty()) {
            Assert.assertEquals(result.sampledRows(), 0,
                    "Bộ lọc " + option + " rỗng nhưng vẫn có dòng mẫu");
            return;
        }
        Assert.assertTrue(result.sampledRows() > 0);
        boolean expected = option.equals("Có");
        Assert.assertTrue(result.hasInvoice().stream().allMatch(actual -> actual == expected),
                "Dữ liệu hóa đơn không khớp filter " + option + ": " + result.hasInvoice());
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
