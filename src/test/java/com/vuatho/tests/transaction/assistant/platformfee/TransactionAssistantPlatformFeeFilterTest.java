package com.vuatho.tests.transaction.assistant.platformfee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionAssistantPlatformFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra tìm kiếm và bộ lọc của tab Thợ phụ. */
public class TransactionAssistantPlatformFeeFilterTest extends TransactionAssistantPlatformFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPlatformFeeFilterTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Phí nền tảng - Bộ lọc");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_005)
    public void searchesUserAndRestoresRows() {
        verifySearchAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_006)
    public void filterOptionsKeepCurrentTab() {
        verifyFilterOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_012)
    public void statusOptionsAreComplete() {
        Assert.assertEquals(advancedPage().filterOptions(TransactionHistoryPage.Filter.STATUS),
                List.of("Đang chờ", "Thành công", "Thất bại"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_013)
    public void gatewayOptionsAreComplete() {
        Assert.assertEquals(advancedPage().filterOptions(TransactionHistoryPage.Filter.GATEWAY),
                List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_014)
    public void filtersRowsByRealStatus() {
        var result = advancedPage().filterByFirstRow(TransactionHistoryPage.Filter.STATUS);
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.selectedText().contains(result.value()));
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.status().equals(result.value())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_015)
    public void combinesStatusAndGateway() {
        var result = advancedPage().combineStatusAndFirstGateway();
        Assert.assertTrue(result.selectedStatus().contains(result.status()));
        Assert.assertTrue(result.selectedGateway().contains(result.gateway()));
        if (result.empty()) {
            Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"));
        } else {
            Assert.assertFalse(result.rows().isEmpty());
            Assert.assertTrue(result.rows().stream().allMatch(row ->
                    row.status().equals(result.status()) && row.gateway().equals(result.gateway())));
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_016)
    public void checksEveryStatusOption() {
        var result = advancedPage().applyEveryFilterOption(TransactionHistoryPage.Filter.STATUS);
        Assert.assertEquals(result.options(), List.of("Đang chờ", "Thành công", "Thất bại"));
        result.results().forEach(option -> {
            Assert.assertTrue(option.selectedText().contains(option.value()));
            Assert.assertTrue(option.rows().stream()
                    .allMatch(row -> row.status().equals(option.value())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_017)
    public void checksEveryGatewayOption() {
        var result = advancedPage().applyEveryFilterOption(TransactionHistoryPage.Filter.GATEWAY);
        Assert.assertEquals(result.options(), List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX"));
        result.results().forEach(option -> {
            Assert.assertTrue(option.selectedText().contains(option.value()));
            Assert.assertTrue(option.rows().stream()
                    .allMatch(row -> row.gateway().equals(option.value())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_018)
    public void dismissingFilterKeepsRows() {
        var result = advancedPage().dismissFilterWithoutSelection(TransactionHistoryPage.Filter.STATUS);
        Assert.assertEquals(result.selectedAfter(), result.selectedBefore());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_019)
    public void futureDatesCannotBeSelected() {
        var result = advancedPage().futureDatesAreDisabled();
        Assert.assertTrue(result.disabled());
        Assert.assertTrue(result.disabledCount() > 0);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_020)
    public void datePickerDefaultsAndRequiresDate() {
        var defaults = advancedPage().dateControlDefaults();
        Assert.assertEquals(defaults.startTime(), "00:00");
        Assert.assertEquals(defaults.endTime(), "23:59");
        Assert.assertTrue(defaults.applyDisabled());
        Assert.assertTrue(advancedPage().dateApplyRequiresSelection().initiallyDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_021)
    public void filtersRowsWithinSingleDay() {
        var result = advancedPage().filterSingleDay();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_023)
    public void unmatchedSearchShowsEmptyState() {
        var result = transactionPage.unmatchedSearchAndReset();
        Assert.assertTrue(result.empty());
        Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"));
        Assert.assertTrue(result.pageText().contains("Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_024)
    public void resetFromEmptyStateRestoresRowsAndSubtype() {
        var result = transactionPage.unmatchedSearchAndReset();
        Assert.assertEquals(signatures(result.restored()), signatures(result.before()));
        Assert.assertTrue(result.url().contains("tab=assistant"));
        Assert.assertTrue(result.url().contains("type=30"));
        Assert.assertTrue(result.activeText().contains("Thợ phụ"));
        Assert.assertTrue(result.activeText().contains("Phí nền tảng"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_025)
    public void totalAndPaginationUpdateAfterStatusFilter() {
        var result = advancedPage().totalAndPaginationAfterStatusFilter();
        Assert.assertTrue(result.afterTotal() <= result.beforeTotal());
        Assert.assertTrue(result.selectedStatus().contains("Thành công"));
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.status().equals("Thành công")));
        Assert.assertEquals(result.afterPagination(), result.afterTotal() > 20);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_026)
    public void filtersRowsWithinSourceMinute() {
        var result = advancedPage().filterSourceMinute();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())
                        && !row.createdAt().toLocalTime().isBefore(result.startTime())
                        && !row.createdAt().toLocalTime().isAfter(result.endTime())));
    }
}
