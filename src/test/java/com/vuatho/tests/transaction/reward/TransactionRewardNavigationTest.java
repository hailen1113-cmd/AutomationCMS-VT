package com.vuatho.tests.transaction.reward;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionRewardTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Kiểm tra sort, pagination và trạng thái điều hướng trên cả hai subtype Thưởng & KM. */
public class TransactionRewardNavigationTest extends TransactionRewardTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionRewardNavigationTest.class,
                "Lịch sử giao dịch", "Thưởng & KM - Điều hướng nâng cao");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_054)
    public void createdDateSortsBothDirectionsOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var ascending = advancedPage().sort("Ngày tạo", false).rows().stream()
                    .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
            var descending = advancedPage().sort("Ngày tạo", true).rows().stream()
                    .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
            assertOrdered(ascending, Comparator.naturalOrder());
            assertOrdered(descending, Comparator.reverseOrder());
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_055)
    public void nonSortableHeadersKeepRowsOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().nonSortableHeadersDoNotChangeRows();
            Assert.assertEquals(result.nonSortableHeaders(), result.expectedHeaders());
            Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_056)
    public void firstPageControlsAreCorrectOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().firstPageControlState();
            Assert.assertEquals(result.activePage(), 1);
            Assert.assertTrue(result.previousDisabled());
            Assert.assertEquals(result.nextDisabled(), result.totalPages() <= 1);
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_057)
    public void adjacentPagesContainDifferentRowsOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().adjacentPages();
            Assert.assertEquals(result.activePage(), 2);
            Assert.assertNotEquals(signatures(result.secondPage()), signatures(result.firstPage()));
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_058)
    public void amountSortStaysOrderedAcrossPagesOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().descendingAmountAcrossPages(true);
            List<java.math.BigDecimal> amounts = new ArrayList<>();
            result.firstPage().forEach(row -> amounts.add(row.amountValue()));
            result.secondPage().forEach(row -> amounts.add(row.amountValue()));
            assertOrdered(amounts, Comparator.reverseOrder());
            Assert.assertEquals(result.returnedFirstPage(), result.firstPage());
            Assert.assertEquals(result.activePage(), 1);
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_059)
    public void statusFilterPersistsAcrossPagesOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().filterPersistsAcrossPages();
            Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
            Assert.assertTrue(result.pageOne().stream().allMatch(row ->
                    row.status().equals(result.expectedStatus())));
            Assert.assertTrue(result.pageTwo().stream().allMatch(row ->
                    row.status().equals(result.expectedStatus())));
            Assert.assertEquals(result.activePage(), 2);
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_060)
    public void detailFromSecondPageReturnsToSameRowsOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().detailFromSecondPage();
            Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
            Assert.assertEquals(result.activePage(), 2);
            Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
            Assert.assertFalse(result.closedUrl().contains("id="));
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_061)
    public void refreshReturnsToFirstPageAndKeepsSubtype() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().refreshFromSecondPage();
            Assert.assertEquals(result.pageBeforeRefresh(), 2);
            Assert.assertEquals(result.pageAfterRefresh(), 1);
            Assert.assertFalse(result.rows().isEmpty());
            Assert.assertEquals(result.rows(), result.baselineRows());
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_062)
    public void resetSortedSecondPageRestoresBaselineOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().resetAmountSortFromSecondPage();
            Assert.assertNotEquals(result.ascendingRows(), result.descendingRows());
            Assert.assertEquals(result.restoredRows(), result.baselineRows());
            Assert.assertEquals(result.pageBeforeReset(), 2);
            Assert.assertEquals(result.pageAfterReset(), 1);
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_101)
    public void nextControlMovesToSecondPageOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().nextControlChangesPage();
            Assert.assertEquals(result.pageBefore(), 1);
            Assert.assertEquals(result.pageAfter(), 2);
            Assert.assertNotEquals(signatures(result.rowsAfter()), signatures(result.rowsBefore()));
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_102)
    public void previousControlReturnsToFirstPageOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().previousControlReturnsPage();
            Assert.assertEquals(result.pageBefore(), 2);
            Assert.assertEquals(result.pageAfter(), 1);
            Assert.assertNotEquals(signatures(result.rowsAfter()), signatures(result.rowsBefore()));
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_103)
    public void activePageMarkersAreSynchronizedOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().activeMarkerAfterPageChange();
            Assert.assertEquals(result.activePage(), 2);
            Assert.assertEquals(result.dataActivePage(), "2");
            Assert.assertTrue(result.ariaCurrent().contains("2"), result.ariaCurrent());
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_104)
    public void lastPageDisablesNextOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().lastPage();
            Assert.assertEquals(result.activePage(), result.expectedLastPage());
            Assert.assertFalse(result.rows().isEmpty());
            Assert.assertTrue(result.nextDisabled());
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_105)
    public void createdDateSortStaysOrderedAcrossPagesOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().descendingCreatedDateAcrossPages(true);
            List<java.time.LocalDateTime> dates = new ArrayList<>();
            result.firstPage().forEach(row -> dates.add(row.createdAt()));
            result.secondPage().forEach(row -> dates.add(row.createdAt()));
            assertOrdered(dates, Comparator.reverseOrder());
            Assert.assertEquals(result.returnedFirstPage(), result.firstPage());
            Assert.assertEquals(result.activePage(), 1);
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_106)
    public void directPageTwoAndBackRestoresFirstPageOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().pageTwoAndBack();
            Assert.assertNotEquals(signatures(result.pageTwo()), signatures(result.pageOne()));
            Assert.assertEquals(result.returnedPageOne(), result.pageOne());
            Assert.assertEquals(result.activePage(), 1);
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_107)
    public void browserHistoryRestoresPagesOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().browserBackAndForwardPages();
            Assert.assertNotEquals(result.pageTwoRows(), result.pageOneRows());
            Assert.assertNotEquals(result.backUrl(), result.pageTwoUrl());
            Assert.assertEquals(result.forwardUrl(), result.pageTwoUrl());
            Assert.assertEquals(result.forwardRows(), result.pageTwoRows());
            Assert.assertEquals(result.activePage(), 2);
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_108)
    public void gatewayFilterPersistsAcrossPagesOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
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
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_109)
    public void refreshKeepsSortedFilteredSecondPageOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().refreshSortedFilteredSecondPage();
            Assert.assertEquals(result.activePage(), 1);
            Assert.assertTrue(result.selectedStatus().contains("Chọn trạng thái"),
                    result.selectedStatus());
            Assert.assertEquals(result.rowsAfter(), result.expectedAfterRefresh());
            assertSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_123)
    public void voucherPaginationDotsJumpForward() {
        var subtype = rewardSubtype(12);
        openRewardSubtype(subtype);
        var result = advancedPage().jumpWithDots();
        Assert.assertEquals(result.pageBefore(), 1);
        Assert.assertTrue(result.pageAfter() > result.pageBefore());
        Assert.assertFalse(result.rows().isEmpty());
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_124)
    public void campaignPaginationDotsJumpForward() {
        var subtype = rewardSubtype(18);
        openRewardSubtype(subtype);
        var result = advancedPage().jumpWithDots();
        Assert.assertEquals(result.pageBefore(), 1);
        Assert.assertTrue(result.pageAfter() > result.pageBefore());
        Assert.assertFalse(result.rows().isEmpty());
        assertSubtype(subtype);
    }

    private void assertSubtype(com.vuatho.pages.TransactionCategoryPage.Subtype subtype) {
        Assert.assertTrue(transactionPage.currentUrl().contains("tab=reward&type=" + subtype.type()),
                transactionPage.currentUrl());
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
