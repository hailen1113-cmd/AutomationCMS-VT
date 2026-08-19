package com.vuatho.tests.transaction.deposit;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionDepositTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Kiểm tra sắp xếp và phân trang của từng loại Tiền nạp. */
public class TransactionDepositNavigationTest extends TransactionDepositTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionDepositNavigationTest.class,
                "Lịch sử giao dịch", "Tiền nạp - Điều hướng");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_007)
    public void sortsAmountBothDirections() {
        verifySortsAmountBothDirectionsForSubtype(subtype(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_176)
    public void sortsAmountBothDirectionsType10() {
        verifySortsAmountBothDirectionsForSubtype(subtype(10));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_177)
    public void sortsAmountBothDirectionsType19() {
        verifySortsAmountBothDirectionsForSubtype(subtype(19));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_178)
    public void sortsAmountBothDirectionsType20() {
        verifySortsAmountBothDirectionsForSubtype(subtype(20));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_179)
    public void sortsAmountBothDirectionsType34() {
        verifySortsAmountBothDirectionsForSubtype(subtype(34));
    }

    private void verifySortsAmountBothDirectionsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        verifyAmountSort(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_009)
    public void paginationAndResetKeepCurrentTab() {
        verifyPaginationAndResetKeepCurrentTabForSubtype(subtype(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_180)
    public void paginationAndResetKeepCurrentTabType10() {
        verifyPaginationAndResetKeepCurrentTabForSubtype(subtype(10));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_181)
    public void paginationAndResetKeepCurrentTabType19() {
        verifyPaginationAndResetKeepCurrentTabForSubtype(subtype(19));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_182)
    public void paginationAndResetKeepCurrentTabType20() {
        verifyPaginationAndResetKeepCurrentTabForSubtype(subtype(20));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_183)
    public void paginationAndResetKeepCurrentTabType34() {
        verifyPaginationAndResetKeepCurrentTabForSubtype(subtype(34));
    }

    private void verifyPaginationAndResetKeepCurrentTabForSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        verifyPaginationAndReset(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_049)
    public void sortsCreatedDateBothDirections() {
        var ascending = advancedPage().sort("Ngày tạo", false).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(ascending, Comparator.naturalOrder());
        var descending = advancedPage().sort("Ngày tạo", true).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(descending, Comparator.reverseOrder());
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_050)
    public void nonSortableHeadersDoNotChangeRows() {
        var result = advancedPage().nonSortableHeadersDoNotChangeRows();
        Assert.assertEquals(result.nonSortableHeaders(), result.expectedHeaders());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_051)
    public void pageTwoChangesAndReturningRestoresPageOne() {
        var result = advancedPage().pageTwoAndBack();
        Assert.assertNotEquals(signatures(result.pageTwo()), signatures(result.pageOne()));
        Assert.assertEquals(signatures(result.returnedPageOne()), signatures(result.pageOne()));
        Assert.assertEquals(result.activePage(), 1);
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_052)
    public void statusFilterPersistsAcrossPagination() {
        var result = advancedPage().filterPersistsAcrossPages();
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
        Assert.assertTrue(result.pageOne().stream()
                .allMatch(row -> row.status().equals(result.expectedStatus())));
        Assert.assertTrue(result.pageTwo().stream()
                .allMatch(row -> row.status().equals(result.expectedStatus())));
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_053)
    public void previousIsDisabledOnFirstPage() {
        var result = advancedPage().firstPageControlState();
        Assert.assertEquals(result.activePage(), 1);
        Assert.assertTrue(result.previousDisabled());
        Assert.assertFalse(result.nextDisabled());
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_054)
    public void nextActiveMarkerAndPreviousWorkTogether() {
        var page = advancedPage();
        var next = page.nextControlChangesPage();
        Assert.assertEquals(next.pageBefore(), 1);
        Assert.assertEquals(next.pageAfter(), 2);
        Assert.assertNotEquals(signatures(next.rowsAfter()), signatures(next.rowsBefore()));

        var marker = page.activeMarkerAfterPageChange();
        Assert.assertEquals(marker.activePage(), 2);
        Assert.assertEquals(marker.dataActivePage(), "2");
        Assert.assertTrue(marker.ariaCurrent().contains("pagination item 2"));
        Assert.assertTrue(marker.ariaCurrent().contains("active"));

        var previous = page.previousControlReturnsPage();
        Assert.assertEquals(previous.pageBefore(), 2);
        Assert.assertEquals(previous.pageAfter(), 1);
        Assert.assertFalse(previous.rowsAfter().isEmpty());
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_055)
    public void lastPageDisablesNextAndRespectsPageSize() {
        var result = advancedPage().lastPage();
        Assert.assertTrue(result.expectedLastPage() > 1);
        Assert.assertEquals(result.activePage(), result.expectedLastPage());
        Assert.assertTrue(result.nextDisabled());
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().size() <= 20);
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_056)
    public void adjacentPagesDoNotDuplicateTransactions() {
        var result = advancedPage().adjacentPages();
        Set<String> duplicates = new HashSet<>(signatures(result.firstPage()));
        duplicates.retainAll(new HashSet<>(signatures(result.secondPage())));
        Assert.assertTrue(duplicates.isEmpty(),
                "Hai trang Tiền nạp chứa giao dịch trùng nhau: " + duplicates);
        Assert.assertEquals(result.activePage(), 2);
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_057)
    public void amountSortPersistsOnPageTwoAndAfterReturning() {
        var result = advancedPage().descendingAmountAcrossPages(true);
        assertOrdered(result.firstPage().stream()
                        .map(TransactionHistoryPage.TransactionRow::amountValue).toList(),
                Comparator.reverseOrder());
        assertOrdered(result.secondPage().stream()
                        .map(TransactionHistoryPage.TransactionRow::amountValue).toList(),
                Comparator.reverseOrder());
        Assert.assertEquals(signatures(result.returnedFirstPage()), signatures(result.firstPage()));
        Assert.assertEquals(result.activePage(), 1);
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_058)
    public void changingStatusFromPageTwoReturnsToFirstPage() {
        var result = advancedPage().filterFromSecondPage();
        Assert.assertEquals(result.activePage(), 1);
        Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
        Assert.assertTrue(result.rows().stream()
                .allMatch(row -> row.status().equals(result.expectedStatus())));
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_059)
    public void detailFromPageTwoKeepsPageRowsAndSubtype() {
        var result = advancedPage().detailFromSecondPage();
        Assert.assertTrue(result.openedUrl().contains("id="));
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertFalse(result.closedUrl().contains("id="));
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_060)
    public void resetAfterAmountSortRestoresDefaultOrder() {
        var result = advancedPage().resetAfterAmountSort();
        Assert.assertNotEquals(result.sortedRows(), result.baselineRows());
        Assert.assertEquals(result.restoredRows(), result.baselineRows());
        Assert.assertEquals(result.activePage(), 1);
        assertRepresentativeSubtype();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_061)
    public void dotsJumpToLaterPageGroup() {
        var result = advancedPage().jumpWithDots();
        Assert.assertTrue(result.pageAfter() > result.pageBefore());
        Assert.assertFalse(result.rows().isEmpty());
        assertRepresentativeSubtype();
    }

    private void assertRepresentativeSubtype() {
        TransactionCategoryPage.Subtype subtype = initialSubtype();
        String url = transactionPage.currentUrl();
        Assert.assertTrue(url.contains("/vuatho/transaction"), "Sai màn hình: " + url);
        Assert.assertTrue(url.contains("tab=" + subtype.tab()), "Sai tab: " + url);
        Assert.assertTrue(url.contains("type=" + subtype.type()), "Sai loại giao dịch: " + url);
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()));
    }

    private <T> void assertOrdered(List<T> actual, Comparator<T> comparator) {
        Assert.assertTrue(actual.size() > 1, "Không đủ dữ liệu để kiểm tra sắp xếp");
        List<T> expected = new ArrayList<>(actual);
        expected.sort(comparator);
        Assert.assertEquals(actual, expected);
    }

    private List<String> signatures(List<TransactionHistoryPage.TransactionRow> rows) {
        return rows.stream().map(TransactionHistoryPage.TransactionRow::signature).toList();
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
