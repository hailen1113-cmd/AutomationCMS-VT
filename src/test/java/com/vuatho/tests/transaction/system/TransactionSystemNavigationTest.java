package com.vuatho.tests.transaction.system;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionSystemTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Kiểm tra sort, pagination và trạng thái điều hướng trên tab Hệ thống. */
public class TransactionSystemNavigationTest extends TransactionSystemTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionSystemNavigationTest.class,
                "Lịch sử giao dịch", "Hệ thống - Điều hướng nâng cao");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_056)
    public void createdDateSortsBothDirections() {
        var ascending = advancedPage().sort("Ngày tạo", false).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        var descending = advancedPage().sort("Ngày tạo", true).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(ascending, Comparator.naturalOrder());
        assertOrdered(descending, Comparator.reverseOrder());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_057)
    public void nonSortableHeadersKeepRows() {
        List<String> headers = List.of("Trạng thái", "Cổng thanh toán");
        var result = advancedPage().nonSortableHeadersDoNotChangeRows(headers);
        Assert.assertEquals(result.nonSortableHeaders(), result.expectedHeaders());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_058)
    public void firstPageControlsAreCorrect() {
        var result = advancedPage().firstPageControlState();
        Assert.assertEquals(result.activePage(), 1);
        Assert.assertTrue(result.previousDisabled());
        Assert.assertEquals(result.nextDisabled(), result.totalPages() <= 1);
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_059)
    public void adjacentPagesContainDifferentRows() {
        var result = advancedPage().adjacentPages();
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertNotEquals(signatures(result.secondPage()), signatures(result.firstPage()));
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_060)
    public void amountSortStaysOrderedAcrossPages() {
        var result = advancedPage().descendingAmountAcrossPages(true);
        List<java.math.BigDecimal> amounts = new ArrayList<>();
        result.firstPage().forEach(row -> amounts.add(row.amountValue()));
        result.secondPage().forEach(row -> amounts.add(row.amountValue()));
        assertOrdered(amounts, Comparator.reverseOrder());
        Assert.assertEquals(result.returnedFirstPage(), result.firstPage());
        Assert.assertEquals(result.activePage(), 1);
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_061)
    public void statusFilterPersistsAcrossPages() {
        var result = advancedPage().filterPersistsAcrossPages();
        Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
        Assert.assertTrue(result.pageOne().stream().allMatch(row ->
                row.status().equals(result.expectedStatus())));
        Assert.assertTrue(result.pageTwo().stream().allMatch(row ->
                row.status().equals(result.expectedStatus())));
        Assert.assertEquals(result.activePage(), 2);
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_062)
    public void detailFromSecondPageReturnsToSameRows() {
        var result = advancedPage().detailFromSecondPage();
        Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertFalse(result.closedUrl().contains("id="));
        assertSystemRoute(result.closedUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_063)
    public void refreshReturnsToFirstPage() {
        var result = advancedPage().refreshFromSecondPage();
        Assert.assertEquals(result.pageBeforeRefresh(), 2);
        Assert.assertEquals(result.pageAfterRefresh(), 1);
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertEquals(result.rows(), result.baselineRows());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_064)
    public void resetSortedSecondPageRestoresBaseline() {
        var result = advancedPage().resetAmountSortFromSecondPage();
        Assert.assertNotEquals(result.ascendingRows(), result.descendingRows());
        Assert.assertEquals(result.restoredRows(), result.baselineRows());
        Assert.assertEquals(result.pageBeforeReset(), 2);
        Assert.assertEquals(result.pageAfterReset(), 1);
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_065)
    public void nextControlMovesToSecondPage() {
        var result = advancedPage().nextControlChangesPage();
        Assert.assertEquals(result.pageBefore(), 1);
        Assert.assertEquals(result.pageAfter(), 2);
        Assert.assertNotEquals(signatures(result.rowsAfter()), signatures(result.rowsBefore()));
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_066)
    public void previousControlReturnsToFirstPage() {
        var result = advancedPage().previousControlReturnsPage();
        Assert.assertEquals(result.pageBefore(), 2);
        Assert.assertEquals(result.pageAfter(), 1);
        Assert.assertNotEquals(signatures(result.rowsAfter()), signatures(result.rowsBefore()));
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_067)
    public void activePageMarkersAreSynchronized() {
        var result = advancedPage().activeMarkerAfterPageChange();
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertEquals(result.dataActivePage(), "2");
        Assert.assertTrue(result.ariaCurrent().contains("2"), result.ariaCurrent());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_068)
    public void lastPageDisablesNext() {
        var result = advancedPage().lastPage();
        Assert.assertEquals(result.activePage(), result.expectedLastPage());
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.nextDisabled());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_069)
    public void createdDateSortStaysOrderedAcrossPages() {
        var result = advancedPage().descendingCreatedDateAcrossPages(true);
        List<java.time.LocalDateTime> dates = new ArrayList<>();
        result.firstPage().forEach(row -> dates.add(row.createdAt()));
        result.secondPage().forEach(row -> dates.add(row.createdAt()));
        assertOrdered(dates, Comparator.reverseOrder());
        Assert.assertEquals(result.returnedFirstPage(), result.firstPage());
        Assert.assertEquals(result.activePage(), 1);
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_070)
    public void directPageTwoAndBackRestoresFirstPage() {
        var result = advancedPage().pageTwoAndBack();
        Assert.assertNotEquals(signatures(result.pageTwo()), signatures(result.pageOne()));
        Assert.assertEquals(result.returnedPageOne(), result.pageOne());
        Assert.assertEquals(result.activePage(), 1);
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_071)
    public void browserHistoryRestoresPages() {
        var result = advancedPage().browserBackAndForwardPages();
        Assert.assertNotEquals(result.pageTwoRows(), result.pageOneRows());
        Assert.assertEquals(result.backUrl(), result.pageOneUrl());
        Assert.assertEquals(result.backRows(), result.pageOneRows());
        Assert.assertEquals(result.backActivePage(), 1);
        Assert.assertEquals(result.forwardUrl(), result.pageTwoUrl());
        Assert.assertEquals(result.forwardRows(), result.pageTwoRows());
        Assert.assertEquals(result.activePage(), 2);
        assertSystemRoute(result.forwardUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_072)
    public void gatewayFilterPersistsAcrossPages() {
        var result = advancedPage().gatewayFilterPaginationStates();
        Assert.assertEquals(result.results().size(), result.gateways().size());
        result.results().forEach(cell -> {
            Assert.assertTrue(cell.selectedGateway().contains(cell.gateway()));
            Assert.assertTrue(cell.pageOne().stream()
                    .allMatch(row -> row.gateway().equals(cell.gateway())));
            Assert.assertTrue(cell.pageTwo().stream()
                    .allMatch(row -> row.gateway().equals(cell.gateway())));
            Assert.assertEquals(cell.pageTwo().isEmpty(), !cell.pageTwoAvailable());
        });
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_073)
    public void refreshClearsSortedFilteredSecondPage() {
        var result = advancedPage().refreshSortedFilteredSecondPage();
        Assert.assertEquals(result.activePage(), 1);
        Assert.assertTrue(result.selectedStatus().contains("Chọn trạng thái"),
                result.selectedStatus());
        Assert.assertEquals(result.rowsAfter(), result.expectedAfterRefresh());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_074)
    public void paginationDotsJumpForward() {
        var result = advancedPage().jumpWithDots();
        Assert.assertEquals(result.pageBefore(), 1);
        Assert.assertTrue(result.pageAfter() > result.pageBefore());
        Assert.assertFalse(result.rows().isEmpty());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_110)
    public void amountAscendingStaysOrderedAcrossPages() {
        var result = advancedPage().ascendingAmountAcrossPages(true);
        List<java.math.BigDecimal> amounts = new ArrayList<>();
        result.firstPage().forEach(row -> amounts.add(row.amountValue()));
        result.secondPage().forEach(row -> amounts.add(row.amountValue()));
        assertOrdered(amounts, Comparator.naturalOrder());
        Assert.assertEquals(result.returnedFirstPage(), result.firstPage());
        Assert.assertEquals(result.activePage(), 1);
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_111)
    public void createdDateAscendingStaysOrderedAcrossPages() {
        var result = advancedPage().ascendingCreatedDateAcrossPages(true);
        List<java.time.LocalDateTime> dates = new ArrayList<>();
        result.firstPage().forEach(row -> dates.add(row.createdAt()));
        result.secondPage().forEach(row -> dates.add(row.createdAt()));
        assertOrdered(dates, Comparator.naturalOrder());
        Assert.assertEquals(result.returnedFirstPage(), result.firstPage());
        Assert.assertEquals(result.activePage(), 1);
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_112)
    public void paginationDotsNavigateBothDirections() {
        var result = advancedPage().paginationDotsJumpForwardAndBack();
        Assert.assertEquals(result.startPage(), 1);
        Assert.assertTrue(result.forwardPage() > result.startPage());
        Assert.assertTrue(result.backwardPage() < result.forwardPage());
        Assert.assertFalse(result.rows().isEmpty());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_113)
    public void previousFromLastPageReturnsPenultimatePage() {
        var result = advancedPage().previousFromLastPage();
        Assert.assertTrue(result.nextDisabled());
        Assert.assertFalse(result.previousDisabled());
        Assert.assertEquals(result.pageAfterPrevious(), result.lastPage() - 1);
        Assert.assertFalse(result.lastPageRows().isEmpty());
        Assert.assertFalse(result.previousPageRows().isEmpty());
        Assert.assertNotEquals(result.previousPageRows(), result.lastPageRows());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_114)
    public void totalRowsAndPagesAreSynchronized() {
        var result = advancedPage().paginationGeometry();
        Assert.assertTrue(result.totalRows() > 0);
        Assert.assertTrue(result.totalPages() > 0);
        Assert.assertTrue(result.firstPageRows() > 0);
        Assert.assertTrue(result.lastPageRows() > 0);
        int expectedTotal = result.totalPages() == 1
                ? result.firstPageRows()
                : (result.totalPages() - 1) * result.firstPageRows() + result.lastPageRows();
        Assert.assertEquals(result.totalRows(), expectedTotal);
        Assert.assertEquals(result.activePage(), result.totalPages());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_115)
    public void statusAndGatewayPersistAcrossPages() {
        var result = advancedPage().combinedStatusGatewayPersistsAcrossPages();
        Assert.assertTrue(result.selectedStatus().contains(result.status()));
        Assert.assertTrue(result.selectedGateway().contains(result.gateway()));
        Assert.assertFalse(result.firstPage().isEmpty());
        Assert.assertFalse(result.secondPage().isEmpty());
        result.firstPage().forEach(row -> {
            Assert.assertEquals(row.status(), result.status());
            Assert.assertEquals(row.gateway(), result.gateway());
        });
        result.secondPage().forEach(row -> {
            Assert.assertEquals(row.status(), result.status());
            Assert.assertEquals(row.gateway(), result.gateway());
        });
        Assert.assertEquals(result.activePage(), 2);
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_116)
    public void emptySearchHidesPagination() {
        var result = advancedPage().impossibleSearchHidesPagination();
        Assert.assertTrue(result.empty(), result.pageText());
        Assert.assertEquals(result.totalRows(), 0);
        Assert.assertFalse(result.paginationVisible());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_117)
    public void paginationControlsExposeAccessibleSemantics() {
        var result = advancedPage().paginationSemantics();
        Assert.assertEquals(result.navigationLabel(), "pagination navigation");
        Assert.assertEquals(result.previousRole(), "button");
        Assert.assertEquals(result.previousLabel(), "previous page button");
        Assert.assertEquals(result.nextRole(), "button");
        Assert.assertEquals(result.nextLabel(), "next page button");
        Assert.assertTrue(result.pageItemCount() >= 1);
        Assert.assertTrue(result.pageItemCount() <= result.totalPages());
        Assert.assertTrue(result.allPageItemsNamed());
        Assert.assertTrue(result.allDotsNamed());
        assertSystemRoute(transactionPage.currentUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_118)
    public void amountSortAndPagePersistAfterDetail() {
        var result = advancedPage().amountSortPersistsAfterDetailOnSecondPage();
        Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        var amounts = result.rowsAfter().stream()
                .map(TransactionHistoryPage.TransactionRow::amountValue).toList();
        assertOrdered(amounts, Comparator.reverseOrder());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
        assertSystemRoute(result.closedUrl(), false);
    }

    private List<String> signatures(List<TransactionHistoryPage.TransactionRow> rows) {
        return rows.stream().map(TransactionHistoryPage.TransactionRow::signature).toList();
    }

    private <T> void assertOrdered(List<T> actual, Comparator<? super T> comparator) {
        Assert.assertTrue(actual.size() > 1, "Không đủ dữ liệu để kiểm tra sắp xếp");
        List<T> expected = new ArrayList<>(actual);
        expected.sort(comparator);
        Assert.assertEquals(actual, expected);
    }
}
