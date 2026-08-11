package com.vuatho.tests.transaction.assistant.penalty;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionAssistantPenaltyTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra tìm kiếm và bộ lọc của loại Tiền phạt. */
public class TransactionAssistantPenaltyFilterTest extends TransactionAssistantPenaltyTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPenaltyFilterTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Tiền phạt - Bộ lọc");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_052)
    public void searchesUserAndRestoresRows() {
        verifySearchAndReset(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_053)
    public void filterOptionsKeepPenaltySubtype() {
        verifyFilterOptions(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_054)
    public void filtersRowsByRealStatus() {
        var result = advancedPage().filterByFirstRow(TransactionHistoryPage.Filter.STATUS);
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.selectedText().contains(result.value()));
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.status().equals(result.value())));
        assertPenaltyUrl(result.url());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_055)
    public void combinesStatusAndGateway() {
        var result = advancedPage().combineStatusAndFirstGateway();
        Assert.assertTrue(result.selectedStatus().contains(result.status()));
        Assert.assertTrue(result.selectedGateway().contains(result.gateway()));
        if (result.empty()) {
            Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"));
        } else {
            Assert.assertTrue(result.rows().stream().allMatch(row ->
                    row.status().equals(result.status()) && row.gateway().equals(result.gateway())));
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_056)
    public void checksEveryStatusOption() {
        var result = advancedPage().applyEveryFilterOption(TransactionHistoryPage.Filter.STATUS);
        result.results().forEach(option -> {
            Assert.assertTrue(option.selectedText().contains(option.value()));
            Assert.assertTrue(option.rows().stream()
                    .allMatch(row -> row.status().equals(option.value())));
        });
        assertPenaltyUrl(driver.getCurrentUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_057)
    public void checksEveryGatewayOption() {
        var result = advancedPage().applyEveryFilterOption(TransactionHistoryPage.Filter.GATEWAY);
        result.results().forEach(option -> {
            Assert.assertTrue(option.selectedText().contains(option.value()));
            Assert.assertTrue(option.rows().stream()
                    .allMatch(row -> row.gateway().equals(option.value())));
        });
        assertPenaltyUrl(driver.getCurrentUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_058)
    public void dismissingFilterKeepsRows() {
        var result = advancedPage().dismissFilterWithoutSelection(TransactionHistoryPage.Filter.STATUS);
        Assert.assertEquals(result.selectedAfter(), result.selectedBefore());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        assertPenaltyUrl(driver.getCurrentUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_059)
    public void filtersRowsWithinSingleDay() {
        var result = advancedPage().filterSingleDay();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())));
        assertPenaltyUrl(driver.getCurrentUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_060)
    public void unmatchedSearchShowsEmptyState() {
        var result = transactionPage.unmatchedSearchAndReset();
        Assert.assertTrue(result.empty());
        Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"));
        Assert.assertTrue(result.pageText().contains("Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_061)
    public void resetFromEmptyStateRestoresRowsAndPenaltySubtype() {
        var result = transactionPage.unmatchedSearchAndReset();
        Assert.assertEquals(signatures(result.restored()), signatures(result.before()));
        assertPenaltyUrl(result.url());
        Assert.assertTrue(result.activeText().contains("Tiền phạt"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_062)
    public void totalAndPaginationUpdateAfterStatusFilter() {
        var result = advancedPage().totalAndPaginationAfterStatusFilter();
        Assert.assertTrue(result.afterTotal() <= result.beforeTotal());
        Assert.assertTrue(result.selectedStatus().contains("Thành công"));
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.status().equals("Thành công")));
        Assert.assertTrue(result.afterPagination(), "UI phải duy trì điều khiển phân trang.");
        Assert.assertEquals(result.rows().size(), Math.min(result.afterTotal(), 20));
        assertPenaltyUrl(driver.getCurrentUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_063)
    public void filtersRowsWithinSourceMinute() {
        var result = advancedPage().filterSourceMinute();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())
                        && !row.createdAt().toLocalTime().isBefore(result.startTime())
                        && !row.createdAt().toLocalTime().isAfter(result.endTime())));
        assertPenaltyUrl(driver.getCurrentUrl());
    }

    private void assertPenaltyUrl(String url) {
        Assert.assertTrue(url.contains("tab=assistant"), url);
        Assert.assertTrue(url.contains("type=31"), url);
    }
}
