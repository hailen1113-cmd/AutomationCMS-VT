package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionInsuranceTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/** Kiểm tra sắp xếp và phân trang VT Care. */
public class TransactionInsuranceNavigationTest extends TransactionInsuranceTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionInsuranceNavigationTest.class,
                "Lịch sử giao dịch", "VT Care - Điều hướng");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_007)
    public void sortsAmountBothDirections() {
        verifySortsAmountBothDirectionsForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_112)
    public void sortsAmountBothDirectionsType26() {
        verifySortsAmountBothDirectionsForSubtype(subtype(26));
    }

    private void verifySortsAmountBothDirectionsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifyAmountSort(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_009)
    public void paginationAndResetKeepCurrentSubtype() {
        verifyPaginationAndResetKeepCurrentSubtypeForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_113)
    public void paginationAndResetKeepCurrentSubtypeType26() {
        verifyPaginationAndResetKeepCurrentSubtypeForSubtype(subtype(26));
    }

    private void verifyPaginationAndResetKeepCurrentSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifyPaginationAndReset(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_021)
    public void sortsCreatedDateBothDirections() {
        verifySortsCreatedDateBothDirectionsForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_114)
    public void sortsCreatedDateBothDirectionsType26() {
        verifySortsCreatedDateBothDirectionsForSubtype(subtype(26));
    }

    private void verifySortsCreatedDateBothDirectionsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var ascending = advancedPage().sort("Ngày tạo", false).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(ascending, Comparator.naturalOrder());
        var descending = advancedPage().sort("Ngày tạo", true).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(descending, Comparator.reverseOrder());
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_022)
    public void pageTwoChangesAndReturningRestoresPageOne() {
        var subtype = category().subtypes().get(0);
        openInsuranceSubtype(subtype);
        var result = advancedPage().pageTwoAndBack();
        Assert.assertNotEquals(signatures(result.pageTwo()), signatures(result.pageOne()));
        Assert.assertEquals(signatures(result.returnedPageOne()), signatures(result.pageOne()));
        Assert.assertEquals(result.activePage(), 1);
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_088)
    public void firstPageControlsMatchPaginationState() {
        verifyFirstPageControlsMatchPaginationStateForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_115)
    public void firstPageControlsMatchPaginationStateType26() {
        verifyFirstPageControlsMatchPaginationStateForSubtype(subtype(26));
    }

    private void verifyFirstPageControlsMatchPaginationStateForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var state = advancedPage().firstPageControlState();
        Assert.assertEquals(state.activePage(), 1);
        Assert.assertTrue(state.previousDisabled(),
                "Previous chưa bị khóa ở trang 1 trên type=" + subtype.type());
        Assert.assertEquals(state.nextDisabled(), state.totalPages() <= 1,
                "Trạng thái Next không khớp tổng số trang trên type=" + subtype.type()
                        + ", totalPages=" + state.totalPages());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_089)
    public void activeMarkerMatchesSecondPage() {
        var subtype = category().subtypes().get(0);
        openInsuranceSubtype(subtype);
        var state = advancedPage().activeMarkerAfterPageChange();
        Assert.assertEquals(state.activePage(), 2);
        Assert.assertEquals(state.dataActivePage(), "2");
        Assert.assertTrue(state.ariaCurrent().contains("2"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_090)
    public void lastPageDisablesNextAndKeepsRows() {
        verifyLastPageDisablesNextAndKeepsRowsForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_116)
    public void lastPageDisablesNextAndKeepsRowsType26() {
        verifyLastPageDisablesNextAndKeepsRowsForSubtype(subtype(26));
    }

    private void verifyLastPageDisablesNextAndKeepsRowsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var state = advancedPage().lastPage();
        Assert.assertEquals(state.activePage(), state.expectedLastPage());
        Assert.assertFalse(state.rows().isEmpty(),
                "Trang cuối không có dữ liệu trên type=" + subtype.type());
        Assert.assertTrue(state.nextDisabled(),
                "Next chưa bị khóa ở trang cuối trên type=" + subtype.type());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_091)
    public void adjacentPagesHaveDistinctRowsWithinPageSize() {
        var subtype = category().subtypes().get(0);
        openInsuranceSubtype(subtype);
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

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_092)
    public void descendingAmountPersistsAcrossPagesAndReturn() {
        var subtype = category().subtypes().get(0);
        openInsuranceSubtype(subtype);
        var state = advancedPage().descendingAmountAcrossPages(true);
        var first = state.firstPage().stream().map(TransactionHistoryPage.TransactionRow::amountValue).toList();
        var second = state.secondPage().stream().map(TransactionHistoryPage.TransactionRow::amountValue).toList();
        assertOrdered(first, Comparator.reverseOrder());
        assertOrdered(second, Comparator.reverseOrder());
        Assert.assertTrue(first.get(first.size() - 1).compareTo(second.get(0)) >= 0);
        Assert.assertEquals(signatures(state.returnedFirstPage()), signatures(state.firstPage()));
        Assert.assertEquals(state.activePage(), 1);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_093)
    public void descendingCreatedDatePersistsAcrossPagesAndReturn() {
        var subtype = category().subtypes().get(0);
        openInsuranceSubtype(subtype);
        var state = advancedPage().descendingCreatedDateAcrossPages(true);
        var first = state.firstPage().stream().map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        var second = state.secondPage().stream().map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(first, Comparator.reverseOrder());
        assertOrdered(second, Comparator.reverseOrder());
        Assert.assertFalse(first.get(first.size() - 1).isBefore(second.get(0)));
        Assert.assertEquals(signatures(state.returnedFirstPage()), signatures(state.firstPage()));
        Assert.assertEquals(state.activePage(), 1);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_094)
    public void successFilterPersistsAcrossPagination() {
        var subtype = category().subtypes().get(0);
        openInsuranceSubtype(subtype);
        var state = advancedPage().filterPersistsAcrossPages();
        Assert.assertEquals(state.selectedStatus(), state.expectedStatus());
        Assert.assertEquals(state.activePage(), 2);
        Assert.assertFalse(state.pageOne().isEmpty());
        Assert.assertFalse(state.pageTwo().isEmpty());
        Assert.assertTrue(state.pageOne().stream().allMatch(row -> "Thành công".equals(row.status())));
        Assert.assertTrue(state.pageTwo().stream().allMatch(row -> "Thành công".equals(row.status())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_095)
    public void resetClearsAmountSortAndReturnsFirstPage() {
        var subtype = category().subtypes().get(0);
        openInsuranceSubtype(subtype);
        var state = advancedPage().resetAmountSortFromSecondPage();
        Assert.assertNotEquals(state.ascendingRows(), state.descendingRows());
        Assert.assertEquals(state.pageBeforeReset(), 2);
        Assert.assertEquals(state.restoredRows(), state.baselineRows(), "Reset chưa xóa sort");
        Assert.assertEquals(state.pageAfterReset(), 1, "Reset chưa về trang 1");
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_096)
    public void refreshSecondPageReturnsFirstPage() {
        var subtype = category().subtypes().get(0);
        openInsuranceSubtype(subtype);
        var state = advancedPage().refreshFromSecondPage();
        Assert.assertEquals(state.pageBeforeRefresh(), 2);
        Assert.assertEquals(state.pageAfterRefresh(), 1, "Refresh chưa về trang 1");
        Assert.assertFalse(state.rows().isEmpty());
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_097)
    public void nonSortableHeadersDoNotReorderRows() {
        verifyNonSortableHeadersDoNotReorderRowsForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_117)
    public void nonSortableHeadersDoNotReorderRowsType26() {
        verifyNonSortableHeadersDoNotReorderRowsForSubtype(subtype(26));
    }

    private void verifyNonSortableHeadersDoNotReorderRowsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var state = advancedPage().nonSortableHeadersDoNotChangeRows();
        Assert.assertEquals(state.nonSortableHeaders(), state.expectedHeaders());
        Assert.assertEquals(state.rowsAfter(), state.rowsBefore(),
                "Cột không hỗ trợ sort đã làm đổi dữ liệu trên type=" + subtype.type());
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

    private void assertSubtype(TransactionCategoryPage.Subtype subtype) {
        String url = transactionPage.currentUrl();
        Assert.assertTrue(url.contains("tab=insurance"), "Mất tab VT Care: " + url);
        boolean correctType = subtype.type() == 25
                ? !url.contains("type=") || url.contains("type=25")
                : url.contains("type=" + subtype.type());
        Assert.assertTrue(correctType, "Mất subtype type=" + subtype.type() + ": " + url);
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
