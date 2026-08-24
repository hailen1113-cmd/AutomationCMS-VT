package com.vuatho.tests.transaction.system;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionSystemTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Kiểm tra filter và tìm kiếm thật trên tab Hệ thống. */
public class TransactionSystemFilterTest extends TransactionSystemTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionSystemFilterTest.class,
                "Lịch sử giao dịch", "Hệ thống - Bộ lọc nâng cao");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_031)
    public void everyStatusOptionFiltersRows() {
        var result = advancedPage().applyEveryFilterOption(TransactionHistoryPage.Filter.STATUS);
        Assert.assertEquals(result.options(), List.of("Đang chờ", "Thành công", "Thất bại"));
        result.results().forEach(option -> {
            Assert.assertTrue(option.selectedText().contains(option.value()));
            Assert.assertTrue(!option.rows().isEmpty() || option.empty(), option.pageText());
            Assert.assertTrue(option.rows().stream()
                    .allMatch(row -> row.status().equals(option.value())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_032)
    public void everyGatewayOptionFiltersRows() {
        var result = advancedPage().applyEveryFilterOption(TransactionHistoryPage.Filter.GATEWAY);
        Assert.assertEquals(result.options(), List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX"));
        result.results().forEach(option -> {
            Assert.assertTrue(option.selectedText().contains(option.value()));
            Assert.assertTrue(!option.rows().isEmpty() || option.empty(), option.pageText());
            Assert.assertTrue(option.rows().stream()
                    .allMatch(row -> row.gateway().equals(option.value())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_033)
    public void everyStatusGatewayCombinationFiltersRows() {
        var result = advancedPage().applyEveryStatusGatewayCombination();
        Assert.assertEquals(result.results().size(),
                result.statuses().size() * result.gateways().size());
        result.results().forEach(cell -> {
            Assert.assertTrue(cell.selectedStatus().contains(cell.status()));
            Assert.assertTrue(cell.selectedGateway().contains(cell.gateway()));
            Assert.assertTrue(!cell.rows().isEmpty() || cell.empty(), cell.pageText());
            Assert.assertTrue(cell.rows().stream().allMatch(row ->
                    row.status().equals(cell.status()) && row.gateway().equals(cell.gateway())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_034)
    public void selectedDayFiltersRows() {
        var result = advancedPage().filterSingleDay();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())));
        Assert.assertTrue(result.selectedText().contains(result.startDate().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_035)
    public void dateStatusAndGatewayCombine() {
        var source = advancedPage().rows().stream()
                .filter(row -> !row.status().isBlank() && !row.gateway().isBlank())
                .findFirst().orElseThrow();
        var result = advancedPage().combineStatusGatewayAndDate(source.status(), source.gateway());
        Assert.assertTrue(result.selectedStatus().contains(source.status()));
        Assert.assertTrue(result.selectedGateway().contains(source.gateway()));
        Assert.assertFalse(result.rows().isEmpty(), result.pageText());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.status().equals(source.status()) && row.gateway().equals(source.gateway())
                        && row.createdAt().toLocalDate().equals(result.date())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_036)
    public void resetClearsCombinedFilters() {
        var result = advancedPage().resetStatusGatewayAndDate("Thành công", "PAYPAL");
        Assert.assertTrue(result.status().contains("Chọn trạng thái"), result.status());
        Assert.assertTrue(result.gateway().contains("Chọn cổng thanh toán"), result.gateway());
        Assert.assertTrue(result.date().contains("Chọn khoảng ngày giờ"), result.date());
        Assert.assertEquals(result.page(), 1);
        assertSystemRoute(result.url(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_037)
    public void statusFilterPersistsAfterDetail() {
        var result = advancedPage().filterPersistsAfterDetail("Thành công");
        Assert.assertTrue(result.selectedStatus().contains("Thành công"));
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertFalse(result.browserLocationAfterClose().contains("id="));
        assertSystemRoute(result.browserLocationAfterClose(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_038)
    public void irrelevantFiltersStayHidden() {
        var result = advancedPage().specializedIrrelevantFiltersAreHidden();
        Assert.assertTrue(result.typeHidden());
        Assert.assertTrue(result.invoiceHidden());
        Assert.assertTrue(result.warrantyHidden());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_039)
    public void unmatchedSearchShowsEmptyStateAndResetRestores() {
        var result = transactionPage.unmatchedSearchAndReset("NO_SYSTEM_TRANSACTION_987654321");
        Assert.assertTrue(result.empty());
        Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"), result.pageText());
        Assert.assertEquals(result.restored(), result.before());
        assertSystemRoute(result.url(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_040)
    public void dismissingSelectFiltersKeepsRows() {
        var status = advancedPage().dismissFilterWithoutSelection(TransactionHistoryPage.Filter.STATUS);
        Assert.assertEquals(status.selectedAfter(), status.selectedBefore());
        Assert.assertEquals(status.rowsAfter(), status.rowsBefore());

        var gateway = advancedPage().dismissFilterWithoutSelection(TransactionHistoryPage.Filter.GATEWAY);
        Assert.assertEquals(gateway.selectedAfter(), gateway.selectedBefore());
        Assert.assertEquals(gateway.rowsAfter(), gateway.rowsBefore());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_041)
    public void dateControlsHaveSafeDefaults() {
        var defaults = advancedPage().dateControlDefaults();
        Assert.assertEquals(defaults.startTime(), "00:00");
        Assert.assertEquals(defaults.endTime(), "23:59");
        Assert.assertTrue(defaults.applyDisabled());
        Assert.assertTrue(advancedPage().dateApplyRequiresSelection().initiallyDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_042)
    public void sourceMinuteFiltersRows() {
        var result = advancedPage().filterSourceMinute();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())
                        && !row.createdAt().toLocalTime().isBefore(result.startTime())
                        && !row.createdAt().toLocalTime().isAfter(result.endTime())));
        Assert.assertTrue(result.selectedText().contains(result.startDate().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_043)
    public void dismissingUnappliedDateKeepsRows() {
        var result = advancedPage().dismissDateWithoutApply();
        Assert.assertEquals(result.selectedAfter(), result.selectedBefore());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_044)
    public void clearingAppliedDateRestoresRows() {
        var result = advancedPage().clearAppliedDateFilter();
        Assert.assertTrue(result.filteredTotal() <= result.originalTotal());
        Assert.assertEquals(result.restoredTotal(), result.originalTotal());
        Assert.assertTrue(result.selectedDate().contains("Chọn khoảng ngày giờ"),
                result.selectedDate());
        Assert.assertTrue(result.clearControlGone());
        Assert.assertTrue(result.rows() > 0);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_045)
    public void changingStatusAndGatewayUpdatesRows() {
        var status = advancedPage().changeStatusFilter();
        Assert.assertTrue(status.selectedText().contains(status.secondValue()));
        Assert.assertTrue(status.secondRows().stream()
                .allMatch(row -> row.status().equals(status.secondValue())));

        openSystemSubtype();
        var gateway = advancedPage().changeGatewayFilter();
        Assert.assertTrue(gateway.selectedText().contains(gateway.secondValue()));
        Assert.assertTrue(gateway.secondRows().stream()
                .allMatch(row -> row.gateway().equals(gateway.secondValue())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_046)
    public void filteringFromSecondPageReturnsToFirst() {
        var result = advancedPage().filterFromSecondPage();
        Assert.assertEquals(result.activePage(), 1);
        Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream()
                .allMatch(row -> row.status().equals(result.expectedStatus())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_047)
    public void totalsAndPaginationFollowStatusFilter() {
        var result = advancedPage().totalAndPaginationAfterStatusFilter();
        Assert.assertTrue(result.selectedStatus().contains("Thành công"));
        Assert.assertTrue(result.afterTotal() <= result.beforeTotal());
        Assert.assertTrue(result.afterTotal() >= result.rows().size());
        Assert.assertTrue(result.rows().stream()
                .allMatch(row -> row.status().equals("Thành công")));
        if (result.afterPagination()) {
            Assert.assertTrue(result.afterTotal() > result.rows().size());
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_048)
    public void recentDateRangeFiltersRows() {
        var result = advancedPage().filterRecentRange();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> {
            var date = row.createdAt().toLocalDate();
            return !date.isBefore(result.start()) && !date.isAfter(result.end());
        }));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_049)
    public void validNameSearchFiltersAndRestores() {
        assertSearchResult(transactionPage.searchByFirstUserName());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_050)
    public void validPhoneSearchFiltersAndRestores() {
        assertSearchResult(transactionPage.searchByFirstUserPhone());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_051)
    public void futureDatesAreDisabled() {
        var result = advancedPage().futureDatesAreDisabled();
        Assert.assertTrue(result.disabled(), result.ariaLabel());
        Assert.assertTrue(result.disabledCount() > 0,
                "Popup ngày không có ngày tương lai bị khóa type=7");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_052)
    public void paddedNameSearchTrimsWhitespace() {
        assertSearchResult(transactionPage.searchByFirstUserNameWithPadding());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_053)
    public void nameSearchIgnoresCase() {
        assertSearchResult(transactionPage.searchByFirstUserNameWithToggledCase());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_054)
    public void partialPhoneSearchFiltersAndRestores() {
        assertSearchResult(transactionPage.searchByFirstUserPhonePartial());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_055)
    public void specialCharacterSearchShowsEmptyAndResetRestores() {
        var result = transactionPage.unmatchedSearchAndReset(
                "  !@#NO_SYSTEM_TRANSACTION_987654321$%^  ");
        Assert.assertTrue(result.empty(), result.pageText());
        Assert.assertEquals(result.restored(), result.before());
        assertSystemRoute(result.url(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_103)
    public void statusAndGatewayPopupsHaveListboxSemantics() {
        var status = advancedPage().filterPopupSemantics(TransactionHistoryPage.Filter.STATUS);
        Assert.assertEquals(status.listboxRole(), "listbox");
        Assert.assertFalse(status.labelledBy().isBlank());
        Assert.assertEquals(status.options(), List.of("Đang chờ", "Thành công", "Thất bại"));
        Assert.assertEquals(status.expandedAfter(), "true");

        var gateway = advancedPage().filterPopupSemantics(TransactionHistoryPage.Filter.GATEWAY);
        Assert.assertEquals(gateway.listboxRole(), "listbox");
        Assert.assertFalse(gateway.labelledBy().isBlank());
        Assert.assertEquals(gateway.options(), List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX"));
        Assert.assertEquals(gateway.expandedAfter(), "true");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_104)
    public void calendarShowsTwoMonthsAndPreviousUpdatesBoth() {
        var result = advancedPage().calendarPreviousMonthUpdatesBothPanels();
        Assert.assertEquals(result.monthsBefore().size(), 2, result.monthsBefore().toString());
        Assert.assertEquals(result.monthsAfter().size(), 2, result.monthsAfter().toString());
        Assert.assertNotEquals(result.monthsAfter(), result.monthsBefore());
        Assert.assertEquals(result.timeInputs(), 2);
        Assert.assertTrue(result.popupText().contains("Từ (giờ)"), result.popupText());
        Assert.assertTrue(result.popupText().contains("Đến (giờ)"), result.popupText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_105)
    public void crossMonthDateRangeFiltersRows() {
        var result = advancedPage().filterAcrossVisibleMonths();
        Assert.assertTrue(!result.rows().isEmpty() || result.empty(), result.pageText());
        Assert.assertTrue(result.rows().stream().allMatch(row -> {
            var date = row.createdAt().toLocalDate();
            return !date.isBefore(result.start()) && !date.isAfter(result.end());
        }));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_106)
    public void invalidTimeRangeCannotApply() {
        var result = advancedPage().invalidTimeRangeCannotApply();
        Assert.assertEquals(result.startTime(), "23:59");
        Assert.assertEquals(result.endTime(), "00:00");
        Assert.assertFalse(result.applyEnabled(),
                "Popup vẫn cho Áp dụng khi giờ bắt đầu lớn hơn giờ kết thúc: " + result.popupText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_107)
    public void searchStatusGatewayAndDateCombine() {
        var result = advancedPage().combineSearchStatusGatewayAndDateFromSource();
        Assert.assertFalse(result.query().isBlank());
        Assert.assertTrue(result.selectedStatus().contains(result.source().status()));
        Assert.assertFalse(result.selectedGateway().contains("Chọn cổng thanh toán"),
                result.selectedGateway());
        Assert.assertTrue(!result.rows().isEmpty() || result.empty(), result.pageText());
        String query = TextNormalizer.normalize(result.query());
        String gateway = List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX").stream()
                .filter(result.selectedGateway()::contains).findFirst().orElseThrow();
        result.rows().forEach(row -> {
            Assert.assertTrue(row.status().equals(result.source().status()));
            Assert.assertEquals(row.gateway(), gateway);
            Assert.assertTrue(row.createdAt().toLocalDate().equals(result.source().createdAt().toLocalDate()));
            Assert.assertTrue(TextNormalizer.normalize(row.signature()).contains(query), row.signature());
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_108)
    public void resetClearsSearchStatusGatewayAndDate() {
        var result = advancedPage().resetCombinedSearchStatusGatewayAndDate();
        Assert.assertTrue(result.query().isBlank(), result.query());
        Assert.assertTrue(result.selectedStatus().contains("Chọn trạng thái"), result.selectedStatus());
        Assert.assertTrue(result.selectedGateway().contains("Chọn cổng thanh toán"), result.selectedGateway());
        Assert.assertTrue(result.selectedDate().contains("Chọn khoảng ngày giờ"), result.selectedDate());
        Assert.assertFalse(result.rows().isEmpty());
        assertSystemRoute(result.url(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_109)
    public void combinedFiltersPersistAfterDetail() {
        var result = advancedPage().combinedFiltersPersistAfterDetail();
        Assert.assertTrue(result.restoredOrPersisted());
        Assert.assertFalse(result.query().isBlank());
        Assert.assertTrue(result.selectedStatus().contains(result.source().status()));
        Assert.assertTrue(result.selectedGateway().contains("Chọn cổng thanh toán"),
                result.selectedGateway());
        Assert.assertFalse(result.url().contains("id="), result.url());
        assertSystemRoute(result.url(), false);
    }

    private void assertSearchResult(TransactionCategoryPage.SearchSnapshot result) {
        Assert.assertFalse(result.query().isBlank());
        Assert.assertFalse(result.filtered().isEmpty(), result.query());
        String query = TextNormalizer.normalize(result.query());
        result.filtered().forEach(row -> Assert.assertTrue(
                TextNormalizer.normalize(row.value("Người dùng")).contains(query),
                row.value("Người dùng")));
        Assert.assertEquals(result.restored(), result.before());
        assertSystemRoute(result.url(), false);
    }
}
