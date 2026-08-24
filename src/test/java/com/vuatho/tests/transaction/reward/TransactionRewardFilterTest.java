package com.vuatho.tests.transaction.reward;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionRewardTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Kiểm tra filter thực thi thật trên cả Hoàn Voucher và Hoàn chiến dịch. */
public class TransactionRewardFilterTest extends TransactionRewardTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionRewardFilterTest.class,
                "Lịch sử giao dịch", "Thưởng & KM - Bộ lọc nâng cao");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_045)
    public void everyStatusOptionFiltersBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().applyEveryFilterOption(TransactionHistoryPage.Filter.STATUS);
            Assert.assertEquals(result.options(), List.of("Đang chờ", "Thành công", "Thất bại"));
            result.results().forEach(option -> {
                Assert.assertTrue(option.selectedText().contains(option.value()));
                Assert.assertTrue(option.rows().stream()
                        .allMatch(row -> row.status().equals(option.value())));
            });
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_046)
    public void everyGatewayOptionFiltersBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().applyEveryFilterOption(TransactionHistoryPage.Filter.GATEWAY);
            Assert.assertEquals(result.options(), List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX"));
            result.results().forEach(option -> {
                Assert.assertTrue(option.selectedText().contains(option.value()));
                Assert.assertTrue(option.rows().stream()
                        .allMatch(row -> row.gateway().equals(option.value())));
            });
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_047)
    public void everyStatusGatewayCombinationFiltersBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().applyEveryStatusGatewayCombination();
            Assert.assertEquals(result.results().size(),
                    result.statuses().size() * result.gateways().size());
            result.results().forEach(cell -> {
                Assert.assertTrue(cell.selectedStatus().contains(cell.status()));
                Assert.assertTrue(cell.selectedGateway().contains(cell.gateway()));
                Assert.assertTrue(cell.rows().stream().allMatch(row ->
                        row.status().equals(cell.status()) && row.gateway().equals(cell.gateway())));
            });
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_048)
    public void selectedDayFiltersBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().filterSingleDay();
            Assert.assertFalse(result.rows().isEmpty());
            Assert.assertTrue(result.rows().stream().allMatch(row ->
                    row.createdAt().toLocalDate().equals(result.startDate())));
            Assert.assertTrue(result.selectedText().contains(result.startDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_049)
    public void dateStatusAndGatewayCombineOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().combineStatusGatewayAndDate("Thành công", "PAYPAL");
            Assert.assertTrue(result.selectedStatus().contains("Thành công"));
            Assert.assertTrue(result.selectedGateway().contains("PAYPAL"));
            Assert.assertTrue(result.rows().stream().allMatch(row ->
                    row.status().equals("Thành công") && row.gateway().equals("PAYPAL")
                            && row.createdAt().toLocalDate().equals(result.date())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_050)
    public void resetClearsCombinedFiltersOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().resetStatusGatewayAndDate("Thành công", "PAYPAL");
            Assert.assertTrue(result.status().contains("Chọn trạng thái"), result.status());
            Assert.assertTrue(result.gateway().contains("Chọn cổng thanh toán"), result.gateway());
            Assert.assertTrue(result.date().contains("Chọn khoảng ngày giờ"), result.date());
            Assert.assertEquals(result.page(), 1);
            assertSubtypeRoute(result.url(), subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_051)
    public void statusFilterPersistsAfterDetailOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().filterPersistsAfterDetail("Thành công");
            Assert.assertTrue(result.selectedStatus().contains("Thành công"));
            Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
            Assert.assertFalse(result.browserLocationAfterClose().contains("id="));
            assertSubtypeRoute(result.browserLocationAfterClose(), subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_052)
    public void irrelevantFiltersStayHiddenOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().specializedIrrelevantFiltersAreHidden();
            Assert.assertTrue(result.typeHidden());
            Assert.assertTrue(result.invoiceHidden());
            Assert.assertTrue(result.warrantyHidden());
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_053)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = transactionPage.unmatchedSearchAndReset();
            Assert.assertTrue(result.empty());
            Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"), result.pageText());
            Assert.assertEquals(result.restored(), result.before());
            assertSubtypeRoute(result.url(), subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_090)
    public void dismissingSelectFiltersKeepsRowsOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var status = advancedPage().dismissFilterWithoutSelection(
                    TransactionHistoryPage.Filter.STATUS);
            Assert.assertEquals(status.selectedAfter(), status.selectedBefore());
            Assert.assertEquals(status.rowsAfter(), status.rowsBefore());

            var gateway = advancedPage().dismissFilterWithoutSelection(
                    TransactionHistoryPage.Filter.GATEWAY);
            Assert.assertEquals(gateway.selectedAfter(), gateway.selectedBefore());
            Assert.assertEquals(gateway.rowsAfter(), gateway.rowsBefore());
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_091)
    public void dateControlsHaveSafeDefaultsOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var defaults = advancedPage().dateControlDefaults();
            Assert.assertEquals(defaults.startTime(), "00:00");
            Assert.assertEquals(defaults.endTime(), "23:59");
            Assert.assertTrue(defaults.applyDisabled());
            Assert.assertTrue(advancedPage().dateApplyRequiresSelection().initiallyDisabled());
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_092)
    public void sourceMinuteFiltersBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().filterSourceMinute();
            Assert.assertFalse(result.rows().isEmpty());
            Assert.assertTrue(result.rows().stream().allMatch(row ->
                    row.createdAt().toLocalDate().equals(result.startDate())
                            && !row.createdAt().toLocalTime().isBefore(result.startTime())
                            && !row.createdAt().toLocalTime().isAfter(result.endTime())));
            Assert.assertTrue(result.selectedText().contains(result.startDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_093)
    public void dismissingUnappliedDateKeepsRowsOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().dismissDateWithoutApply();
            Assert.assertEquals(result.selectedAfter(), result.selectedBefore());
            Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_094)
    public void clearingAppliedDateRestoresBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().clearAppliedDateFilter();
            Assert.assertTrue(result.filteredTotal() <= result.originalTotal());
            Assert.assertEquals(result.restoredTotal(), result.originalTotal());
            Assert.assertTrue(result.selectedDate().contains("Chọn khoảng ngày giờ"),
                    result.selectedDate());
            Assert.assertTrue(result.clearControlGone());
            Assert.assertTrue(result.rows() > 0);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_095)
    public void changingStatusAndGatewayUpdatesBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var status = advancedPage().changeStatusFilter();
            Assert.assertTrue(status.selectedText().contains(status.secondValue()));
            Assert.assertTrue(status.secondRows().stream()
                    .allMatch(row -> row.status().equals(status.secondValue())));

            openRewardSubtype(subtype);
            var gateway = advancedPage().changeGatewayFilter();
            Assert.assertTrue(gateway.selectedText().contains(gateway.secondValue()));
            Assert.assertTrue(gateway.secondRows().stream()
                    .allMatch(row -> row.gateway().equals(gateway.secondValue())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_096)
    public void filteringFromSecondPageReturnsToFirstOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().filterFromSecondPage();
            Assert.assertEquals(result.activePage(), 1);
            Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
            Assert.assertFalse(result.rows().isEmpty());
            Assert.assertTrue(result.rows().stream()
                    .allMatch(row -> row.status().equals(result.expectedStatus())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_097)
    public void totalsAndPaginationFollowStatusFilterOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().totalAndPaginationAfterStatusFilter();
            Assert.assertTrue(result.selectedStatus().contains("Thành công"));
            Assert.assertTrue(result.afterTotal() <= result.beforeTotal());
            Assert.assertTrue(result.afterTotal() >= result.rows().size());
            Assert.assertTrue(result.rows().stream()
                    .allMatch(row -> row.status().equals("Thành công")));
            if (result.afterPagination()) {
                Assert.assertTrue(result.afterTotal() > result.rows().size());
            }
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_098)
    public void recentDateRangeFiltersBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().filterRecentRange();
            Assert.assertFalse(result.rows().isEmpty());
            Assert.assertTrue(result.rows().stream().allMatch(row -> {
                var date = row.createdAt().toLocalDate();
                return !date.isBefore(result.start()) && !date.isAfter(result.end());
            }));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_099)
    public void validNameSearchFiltersAndRestoresBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            assertSearchResult(transactionPage.searchByFirstUserName(), subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_100)
    public void validPhoneSearchFiltersAndRestoresBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            assertSearchResult(transactionPage.searchByFirstUserPhone(), subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_122)
    public void futureDatesAreDisabledOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().futureDatesAreDisabled();
            Assert.assertTrue(result.disabled(), result.ariaLabel());
            Assert.assertTrue(result.disabledCount() > 0,
                    "Popup ngày không có ngày tương lai bị khóa type=" + subtype.type());
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_127)
    public void paddedNameSearchTrimsWhitespaceOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            assertSearchResult(transactionPage.searchByFirstUserNameWithPadding(), subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_128)
    public void nameSearchIgnoresCaseOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            assertSearchResult(transactionPage.searchByFirstUserNameWithToggledCase(), subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_129)
    public void partialPhoneSearchFiltersAndRestoresBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            assertSearchResult(transactionPage.searchByFirstUserPhonePartial(), subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_130)
    public void specialCharacterSearchShowsEmptyAndResetRestoresBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = transactionPage.unmatchedSearchAndReset(
                    "  !@#NO_REWARD_TRANSACTION_987654321$%^  ");
            Assert.assertTrue(result.empty(), result.pageText());
            Assert.assertEquals(result.restored(), result.before());
            assertSubtypeRoute(result.url(), subtype);
        });
    }

    private void assertSearchResult(
            com.vuatho.pages.TransactionCategoryPage.SearchSnapshot result,
            com.vuatho.pages.TransactionCategoryPage.Subtype subtype) {
        Assert.assertFalse(result.query().isBlank());
        Assert.assertFalse(result.filtered().isEmpty(), result.query());
        String query = com.vuatho.utils.TextNormalizer.normalize(result.query());
        result.filtered().forEach(row -> Assert.assertTrue(
                com.vuatho.utils.TextNormalizer.normalize(row.value("Người dùng")).contains(query),
                row.value("Người dùng")));
        Assert.assertEquals(result.restored(), result.before());
        assertSubtypeRoute(result.url(), subtype);
    }

    private void assertSubtypeRoute(String url, com.vuatho.pages.TransactionCategoryPage.Subtype subtype) {
        Assert.assertTrue(url.contains("tab=reward&type=" + subtype.type()), url);
    }
}
