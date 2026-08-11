package com.vuatho.tests.transaction.assistant.penalty;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionAssistantPenaltyTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;

/** Kiểm tra sắp xếp và phân trang của loại Tiền phạt. */
public class TransactionAssistantPenaltyNavigationTest extends TransactionAssistantPenaltyTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPenaltyNavigationTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Tiền phạt - Sắp xếp và phân trang");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_064)
    public void sortsAmountBothDirections() {
        verifyAmountSort(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_065)
    public void paginationAndResetKeepPenaltySubtype() {
        verifyPaginationAndReset(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_066)
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
