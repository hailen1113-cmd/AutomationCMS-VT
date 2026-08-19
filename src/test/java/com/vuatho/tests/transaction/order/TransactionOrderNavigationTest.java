package com.vuatho.tests.transaction.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionOrderTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/** Kiểm tra sắp xếp, phân trang, Reset và Refresh của Đơn dịch vụ. */
public class TransactionOrderNavigationTest extends TransactionOrderTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionOrderNavigationTest.class,
                "Lịch sử giao dịch", "Đơn dịch vụ - Điều hướng");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_007)
    public void sortsAmountBothDirectionsAcrossAllSubtypes() {
        verifySortsAmountBothDirectionsAcrossAllSubtypesForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_125)
    public void sortsAmountBothDirectionsAcrossAllSubtypesType22() {
        verifySortsAmountBothDirectionsAcrossAllSubtypesForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_126)
    public void sortsAmountBothDirectionsAcrossAllSubtypesType24() {
        verifySortsAmountBothDirectionsAcrossAllSubtypesForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_127)
    public void sortsAmountBothDirectionsAcrossAllSubtypesType36() {
        verifySortsAmountBothDirectionsAcrossAllSubtypesForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_128)
    public void sortsAmountBothDirectionsAcrossAllSubtypesType37() {
        verifySortsAmountBothDirectionsAcrossAllSubtypesForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_129)
    public void sortsAmountBothDirectionsAcrossAllSubtypesType15() {
        verifySortsAmountBothDirectionsAcrossAllSubtypesForSubtype(subtype(15));
    }

    private void verifySortsAmountBothDirectionsAcrossAllSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        verifyAmountSort(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_009)
    public void paginationAndResetKeepCurrentSubtype() {
        verifyPaginationAndResetKeepCurrentSubtypeForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_130)
    public void paginationAndResetKeepCurrentSubtypeType22() {
        verifyPaginationAndResetKeepCurrentSubtypeForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_131)
    public void paginationAndResetKeepCurrentSubtypeType24() {
        verifyPaginationAndResetKeepCurrentSubtypeForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_132)
    public void paginationAndResetKeepCurrentSubtypeType36() {
        verifyPaginationAndResetKeepCurrentSubtypeForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_133)
    public void paginationAndResetKeepCurrentSubtypeType37() {
        verifyPaginationAndResetKeepCurrentSubtypeForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_134)
    public void paginationAndResetKeepCurrentSubtypeType15() {
        verifyPaginationAndResetKeepCurrentSubtypeForSubtype(subtype(15));
    }

    private void verifyPaginationAndResetKeepCurrentSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        verifyPaginationAndReset(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_065)
    public void sortsCreatedDateBothDirectionsAcrossAllSubtypes() {
        verifySortsCreatedDateBothDirectionsAcrossAllSubtypesForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_135)
    public void sortsCreatedDateBothDirectionsAcrossAllSubtypesType22() {
        verifySortsCreatedDateBothDirectionsAcrossAllSubtypesForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_136)
    public void sortsCreatedDateBothDirectionsAcrossAllSubtypesType24() {
        verifySortsCreatedDateBothDirectionsAcrossAllSubtypesForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_137)
    public void sortsCreatedDateBothDirectionsAcrossAllSubtypesType36() {
        verifySortsCreatedDateBothDirectionsAcrossAllSubtypesForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_138)
    public void sortsCreatedDateBothDirectionsAcrossAllSubtypesType37() {
        verifySortsCreatedDateBothDirectionsAcrossAllSubtypesForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_139)
    public void sortsCreatedDateBothDirectionsAcrossAllSubtypesType15() {
        verifySortsCreatedDateBothDirectionsAcrossAllSubtypesForSubtype(subtype(15));
    }

    private void verifySortsCreatedDateBothDirectionsAcrossAllSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var ascending = advancedPage().sort("Ngày tạo", false).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(ascending, Comparator.naturalOrder());
        var descending = advancedPage().sort("Ngày tạo", true).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(descending, Comparator.reverseOrder());
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_066)
    public void pageTwoChangesAndReturningRestoresPageOne() {
        var subtype = type22();
        openOrderSubtype(subtype);
        var result = advancedPage().pageTwoAndBack();
        Assert.assertNotEquals(signatures(result.pageTwo()), signatures(result.pageOne()));
        Assert.assertEquals(signatures(result.returnedPageOne()), signatures(result.pageOne()));
        Assert.assertEquals(result.activePage(), 1);
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_067)
    public void firstPageControlsMatchPaginationState() {
        verifyFirstPageControlsMatchPaginationStateForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_140)
    public void firstPageControlsMatchPaginationStateType22() {
        verifyFirstPageControlsMatchPaginationStateForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_141)
    public void firstPageControlsMatchPaginationStateType24() {
        verifyFirstPageControlsMatchPaginationStateForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_142)
    public void firstPageControlsMatchPaginationStateType36() {
        verifyFirstPageControlsMatchPaginationStateForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_143)
    public void firstPageControlsMatchPaginationStateType37() {
        verifyFirstPageControlsMatchPaginationStateForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_144)
    public void firstPageControlsMatchPaginationStateType15() {
        verifyFirstPageControlsMatchPaginationStateForSubtype(subtype(15));
    }

    private void verifyFirstPageControlsMatchPaginationStateForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var state = advancedPage().firstPageControlState();
        Assert.assertEquals(state.activePage(), 1);
        Assert.assertTrue(state.previousDisabled());
        Assert.assertEquals(state.nextDisabled(), state.totalPages() <= 1);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_068)
    public void activeMarkerMatchesSecondPage() {
        var subtype = type22();
        openOrderSubtype(subtype);
        var state = advancedPage().activeMarkerAfterPageChange();
        Assert.assertEquals(state.activePage(), 2);
        Assert.assertEquals(state.dataActivePage(), "2");
        Assert.assertTrue(state.ariaCurrent().contains("2"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_069)
    public void lastPageDisablesNextAndKeepsRows() {
        verifyLastPageDisablesNextAndKeepsRowsForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_145)
    public void lastPageDisablesNextAndKeepsRowsType22() {
        verifyLastPageDisablesNextAndKeepsRowsForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_146)
    public void lastPageDisablesNextAndKeepsRowsType24() {
        verifyLastPageDisablesNextAndKeepsRowsForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_147)
    public void lastPageDisablesNextAndKeepsRowsType36() {
        verifyLastPageDisablesNextAndKeepsRowsForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_148)
    public void lastPageDisablesNextAndKeepsRowsType37() {
        verifyLastPageDisablesNextAndKeepsRowsForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_149)
    public void lastPageDisablesNextAndKeepsRowsType15() {
        verifyLastPageDisablesNextAndKeepsRowsForSubtype(subtype(15));
    }

    private void verifyLastPageDisablesNextAndKeepsRowsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var state = advancedPage().lastPage();
        Assert.assertEquals(state.activePage(), state.expectedLastPage());
        Assert.assertFalse(state.rows().isEmpty());
        Assert.assertTrue(state.nextDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_070)
    public void adjacentPagesHaveDistinctRowsWithinPageSize() {
        var subtype = type22();
        openOrderSubtype(subtype);
        var state = advancedPage().adjacentPages();
        List<String> first = signatures(state.firstPage());
        List<String> second = signatures(state.secondPage());
        Assert.assertEquals(state.activePage(), 2);
        Assert.assertTrue(first.size() <= 20 && second.size() <= 20);
        Assert.assertNotEquals(second, first);
        HashSet<String> overlap = new HashSet<>(first);
        overlap.retainAll(second);
        Assert.assertTrue(overlap.isEmpty(), "Hai trang chứa giao dịch trùng nhau: " + overlap);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_071)
    public void descendingAmountPersistsAcrossPagesAndReturn() {
        var subtype = type22();
        openOrderSubtype(subtype);
        var state = advancedPage().descendingAmountAcrossPages(true);
        var first = state.firstPage().stream()
                .map(TransactionHistoryPage.TransactionRow::amountValue).toList();
        var second = state.secondPage().stream()
                .map(TransactionHistoryPage.TransactionRow::amountValue).toList();
        assertOrdered(first, Comparator.reverseOrder());
        assertOrdered(second, Comparator.reverseOrder());
        Assert.assertTrue(first.get(first.size() - 1).compareTo(second.get(0)) >= 0);
        Assert.assertEquals(signatures(state.returnedFirstPage()), signatures(state.firstPage()));
        Assert.assertEquals(state.activePage(), 1);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_072)
    public void descendingCreatedDatePersistsAcrossPagesAndReturn() {
        var subtype = type22();
        openOrderSubtype(subtype);
        var state = advancedPage().descendingCreatedDateAcrossPages(true);
        var first = state.firstPage().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        var second = state.secondPage().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(first, Comparator.reverseOrder());
        assertOrdered(second, Comparator.reverseOrder());
        Assert.assertFalse(first.get(first.size() - 1).isBefore(second.get(0)));
        Assert.assertEquals(signatures(state.returnedFirstPage()), signatures(state.firstPage()));
        Assert.assertEquals(state.activePage(), 1);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_073)
    public void successFilterPersistsAcrossPagination() {
        var subtype = type22();
        openOrderSubtype(subtype);
        var state = advancedPage().filterPersistsAcrossPages();
        Assert.assertEquals(state.selectedStatus(), state.expectedStatus());
        Assert.assertEquals(state.activePage(), 2);
        Assert.assertFalse(state.pageOne().isEmpty());
        Assert.assertFalse(state.pageTwo().isEmpty());
        Assert.assertTrue(state.pageOne().stream()
                .allMatch(row -> "Thành công".equals(row.status())));
        Assert.assertTrue(state.pageTwo().stream()
                .allMatch(row -> "Thành công".equals(row.status())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_074)
    public void resetClearsAmountSortAndReturnsFirstPage() {
        var subtype = type22();
        openOrderSubtype(subtype);
        var state = advancedPage().resetAmountSortFromSecondPage();
        Assert.assertNotEquals(state.ascendingRows(), state.descendingRows());
        Assert.assertEquals(state.pageBeforeReset(), 2);
        Assert.assertEquals(state.restoredRows(), state.baselineRows());
        Assert.assertEquals(state.pageAfterReset(), 1);
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_075)
    public void refreshSecondPageReturnsFirstPage() {
        var subtype = type22();
        openOrderSubtype(subtype);
        var state = advancedPage().refreshFromSecondPage();
        Assert.assertEquals(state.pageBeforeRefresh(), 2);
        Assert.assertEquals(state.pageAfterRefresh(), 1);
        Assert.assertFalse(state.rows().isEmpty());
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_076)
    public void nonSortableHeadersDoNotReorderRows() {
        verifyNonSortableHeadersDoNotReorderRowsForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_150)
    public void nonSortableHeadersDoNotReorderRowsType22() {
        verifyNonSortableHeadersDoNotReorderRowsForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_151)
    public void nonSortableHeadersDoNotReorderRowsType24() {
        verifyNonSortableHeadersDoNotReorderRowsForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_152)
    public void nonSortableHeadersDoNotReorderRowsType36() {
        verifyNonSortableHeadersDoNotReorderRowsForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_153)
    public void nonSortableHeadersDoNotReorderRowsType37() {
        verifyNonSortableHeadersDoNotReorderRowsForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_154)
    public void nonSortableHeadersDoNotReorderRowsType15() {
        verifyNonSortableHeadersDoNotReorderRowsForSubtype(subtype(15));
    }

    private void verifyNonSortableHeadersDoNotReorderRowsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var state = advancedPage().nonSortableHeadersDoNotChangeRows();
        Assert.assertEquals(state.nonSortableHeaders(), state.expectedHeaders());
        Assert.assertEquals(state.rowsAfter(), state.rowsBefore());
        assertSubtype(subtype);
    }

    private TransactionCategoryPage.Subtype type22() {
        return category().subtypes().stream()
                .filter(value -> value.type() == 22).findFirst().orElseThrow();
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

    private void assertSubtype(TransactionCategoryPage.Subtype subtype) {
        Assert.assertTrue(transactionPage.currentUrl().contains(
                "tab=order&type=" + subtype.type()), transactionPage.currentUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                transactionPage.activeGroupText());
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
