package com.vuatho.tests.transaction.assistant.platformfee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionAssistantPlatformFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;

/** Kiểm tra sắp xếp và phân trang của tab Thợ phụ. */
public class TransactionAssistantPlatformFeeNavigationTest extends TransactionAssistantPlatformFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPlatformFeeNavigationTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Phí nền tảng - Sắp xếp và phân trang");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_007)
    public void sortsAmountBothDirections() {
        verifyAmountSort();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_009)
    public void paginationAndResetKeepCurrentTab() {
        verifyPaginationAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_027)
    public void amountSortStillWorksAfterStatusFilter() {
        TransactionHistoryPage page = advancedPage();
        var filter = page.filterByFirstRow(TransactionHistoryPage.Filter.STATUS);
        var sorted = page.sort("Số tiền", false);
        Assert.assertTrue(sorted.rows().stream().allMatch(row -> row.status().equals(filter.value())));
        var actual = sorted.rows().stream().map(TransactionHistoryPage.TransactionRow::amountValue).toList();
        var expected = new ArrayList<>(actual);
        expected.sort(Comparator.naturalOrder());
        Assert.assertEquals(actual, expected);
    }
}
