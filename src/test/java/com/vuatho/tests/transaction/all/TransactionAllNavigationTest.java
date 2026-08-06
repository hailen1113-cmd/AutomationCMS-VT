package com.vuatho.tests.transaction.all;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionHistoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Kiểm tra sắp xếp, phân trang và khả năng giữ trạng thái tab Tất cả. */
public class TransactionAllNavigationTest extends TransactionHistoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAllNavigationTest.class,
                "Lịch sử giao dịch", "Tab Tất cả - Sắp xếp và phân trang");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_020)
    public void sortsAmountAscending() {
        var values = transactionPage.sort("Số tiền", false).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::amountValue).toList();
        assertOrdered(values, Comparator.naturalOrder());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_021)
    public void sortsAmountDescending() {
        var values = transactionPage.sort("Số tiền", true).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::amountValue).toList();
        assertOrdered(values, Comparator.reverseOrder());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_022)
    public void sortsCreatedDateBothDirections() {
        var ascending = transactionPage.sort("Ngày tạo", false).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(ascending, Comparator.naturalOrder());
        var descending = transactionPage.sort("Ngày tạo", true).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(descending, Comparator.reverseOrder());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_023)
    public void nonSortableHeadersDoNotChangeRows() {
        var result = transactionPage.nonSortableHeadersDoNotChangeRows();
        Assert.assertEquals(result.nonSortableHeaders(), result.expectedHeaders());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_024)
    public void pageTwoChangesDataset() {
        var result = transactionPage.pageTwoAndBack();
        Assert.assertNotEquals(signatures(result.pageTwo()), signatures(result.pageOne()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_025)
    public void returningToPageOneRestoresDataset() {
        var result = transactionPage.pageTwoAndBack();
        Assert.assertEquals(signatures(result.returnedPageOne()), signatures(result.pageOne()));
        Assert.assertEquals(result.activePage(), 1);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_026)
    public void filterPersistsAcrossPagination() {
        var result = transactionPage.filterPersistsAcrossPages();
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
        Assert.assertTrue(result.pageOne().stream().allMatch(row -> row.status().equals(result.expectedStatus())));
        Assert.assertTrue(result.pageTwo().stream().allMatch(row -> row.status().equals(result.expectedStatus())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_027)
    public void resetFromLaterPageReturnsToFirstPage() {
        var result = transactionPage.resetFromSecondPage();
        Assert.assertEquals(result.page(), 1);
        Assert.assertTrue(result.allSelected());
        Assert.assertTrue(result.rows() > 0);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_062)
    public void previousIsDisabledOnFirstPage() {
        var result = transactionPage.firstPageControlState();
        Assert.assertEquals(result.activePage(), 1);
        Assert.assertTrue(result.previousDisabled());
        Assert.assertFalse(result.nextDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_063)
    public void nextMovesToFollowingPage() {
        var result = transactionPage.nextControlChangesPage();
        Assert.assertEquals(result.pageBefore(), 1);
        Assert.assertEquals(result.pageAfter(), 2);
        Assert.assertNotEquals(signatures(result.rowsAfter()), signatures(result.rowsBefore()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_064)
    public void previousReturnsToPriorPage() {
        var result = transactionPage.previousControlReturnsPage();
        Assert.assertEquals(result.pageBefore(), 2);
        Assert.assertEquals(result.pageAfter(), 1);
        Assert.assertFalse(result.rowsAfter().isEmpty());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_065)
    public void currentPageHasActiveMarker() {
        var result = transactionPage.activeMarkerAfterPageChange();
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertEquals(result.dataActivePage(), "2");
        Assert.assertTrue(result.ariaCurrent().contains("pagination item 2"));
        Assert.assertTrue(result.ariaCurrent().contains("active"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_066)
    public void reachesLastPageAndDisablesNext() {
        var result = transactionPage.lastPage();
        Assert.assertTrue(result.expectedLastPage() > 1);
        Assert.assertEquals(result.activePage(), result.expectedLastPage());
        Assert.assertTrue(result.nextDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_067)
    public void lastPageRespectsPageSize() {
        var result = transactionPage.lastPage();
        Assert.assertTrue(result.rows().size() > 0);
        Assert.assertTrue(result.rows().size() <= 20);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_068)
    public void adjacentPagesDoNotDuplicateTransactions() {
        var result = transactionPage.adjacentPages();
        Set<String> first = new HashSet<>(signatures(result.firstPage()));
        Set<String> second = new HashSet<>(signatures(result.secondPage()));
        first.retainAll(second);
        Assert.assertTrue(first.isEmpty(), "Hai trang đang chứa giao dịch trùng nhau: " + first);
        Assert.assertEquals(result.activePage(), 2);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_069)
    public void amountSortPersistsAcrossPagination() {
        var result = transactionPage.descendingAmountAcrossPages(false);
        assertOrdered(result.firstPage().stream()
                .map(TransactionHistoryPage.TransactionRow::amountValue).toList(),
                Comparator.reverseOrder());
        assertOrdered(result.secondPage().stream()
                .map(TransactionHistoryPage.TransactionRow::amountValue).toList(),
                Comparator.reverseOrder());
        Assert.assertEquals(result.activePage(), 2);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_070)
    public void returningToFirstPageKeepsSortDirection() {
        var result = transactionPage.descendingAmountAcrossPages(true);
        Assert.assertEquals(signatures(result.returnedFirstPage()), signatures(result.firstPage()));
        assertOrdered(result.returnedFirstPage().stream()
                .map(TransactionHistoryPage.TransactionRow::amountValue).toList(),
                Comparator.reverseOrder());
        Assert.assertEquals(result.activePage(), 1);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_071)
    public void changingFilterFromLaterPageReturnsFirstPage() {
        var result = transactionPage.filterFromSecondPage();
        Assert.assertEquals(result.activePage(), 1);
        Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
        Assert.assertTrue(result.rows().stream()
                .allMatch(row -> row.status().equals(result.expectedStatus())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_072)
    public void detailFromLaterPageKeepsCurrentPage() {
        var result = transactionPage.detailFromSecondPage();
        Assert.assertTrue(result.openedUrl().contains("id="));
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertFalse(result.closedUrl().contains("id="));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_073)
    public void resetAfterSortingRestoresDefaultOrder() {
        var result = transactionPage.resetAfterAmountSort();
        Assert.assertNotEquals(result.sortedRows(), result.baselineRows());
        Assert.assertEquals(result.restoredRows(), result.baselineRows());
        Assert.assertEquals(result.activePage(), 1);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_074)
    public void dotsJumpToLaterPageGroup() {
        var result = transactionPage.jumpWithDots();
        Assert.assertTrue(result.pageAfter() > result.pageBefore());
        Assert.assertFalse(result.rows().isEmpty());
    }

    private <T> void assertOrdered(List<T> actual, Comparator<T> comparator) {
        Assert.assertTrue(actual.size() > 1);
        List<T> expected = new ArrayList<>(actual);
        expected.sort(comparator);
        Assert.assertEquals(actual, expected);
    }

    private List<String> signatures(List<TransactionHistoryPage.TransactionRow> rows) {
        return rows.stream().map(TransactionHistoryPage.TransactionRow::signature).toList();
    }
}
