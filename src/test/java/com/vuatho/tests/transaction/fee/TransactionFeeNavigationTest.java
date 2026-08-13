package com.vuatho.tests.transaction.fee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Kiểm tra sắp xếp và phân trang của từng loại Phí & Doanh thu. */
public class TransactionFeeNavigationTest extends TransactionFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionFeeNavigationTest.class,
                "Lịch sử giao dịch", "Phí & Doanh thu - Điều hướng");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_007,
            dataProvider = "feeSubtypes")
    public void sortsAmountBothDirections(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        verifyAmountSort(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_009,
            dataProvider = "feeSubtypes")
    public void paginationAndResetKeepCurrentSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        verifyPaginationAndReset(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_025,
            dataProvider = "feeSubtypes")
    public void sortsCreatedDateBothDirections(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var ascending = advancedPage().sort("Ngày tạo", false).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(ascending, Comparator.naturalOrder());
        var descending = advancedPage().sort("Ngày tạo", true).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(descending, Comparator.reverseOrder());
        Assert.assertTrue(transactionPage.currentUrl().contains("tab=fee&type=" + subtype.type()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_047,
            dataProvider = "feeSubtypes")
    public void senderLinkOpensCorrectProfileAndReturnsToSourceDetail(
            TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().openPartyProfileAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/worker?id=")
                        || result.expectedUrl().contains("/vuatho/user?id="), result.expectedUrl());
        Assert.assertEquals(queryValue(result.actualUrl(), "id"),
                queryValue(result.expectedUrl(), "id"));
        Assert.assertFalse(result.linkText().isBlank());
        Assert.assertTrue(result.targetText().contains("Quản Lí Thợ")
                        || result.targetText().contains("Thông tin thợ")
                        || result.targetText().contains("Quản lí người dùng")
                        || result.targetText().contains("Quản Lí Người Dùng")
                        || result.targetText().contains("Chi tiết người dùng"), result.targetText());
        Assert.assertTrue(result.sourceRestored());
        Assert.assertEquals(result.returnedUrl(), result.sourceUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_048,
            dataProvider = "feeOrderSubtypes")
    public void orderLinkOpensCorrectOrderAndReturnsToSourceDetail(
            TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().openOrderAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/order?id="), result.expectedUrl());
        Assert.assertEquals(queryValue(result.actualUrl(), "id"),
                queryValue(result.expectedUrl(), "id"));
        Assert.assertFalse(result.linkText().isBlank());
        Assert.assertTrue(result.targetText().contains("Quản lí đơn dịch vụ")
                        || result.targetText().contains("Thông tin đơn dịch vụ"), result.targetText());
        Assert.assertTrue(result.sourceRestored());
        Assert.assertEquals(result.returnedUrl(), result.sourceUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_049,
            dataProvider = "feeSubtypes")
    public void timelineLinkOpensCorrectTransactionAndReturnsToSourceDetail(
            TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().openRelatedTransactionAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/transaction?tab=all"),
                result.expectedUrl());
        Assert.assertEquals(queryValue(result.actualUrl(), "id"),
                queryValue(result.expectedUrl(), "id"));
        Assert.assertNotEquals(queryValue(result.actualUrl(), "id"),
                queryValue(result.sourceUrl(), "id"));
        Assert.assertTrue(result.targetText().contains("Chi tiết giao dịch"), result.targetText());
        Assert.assertTrue(result.targetText().contains(result.linkText().lines().findFirst().orElse("")),
                result.targetText());
        Assert.assertTrue(result.sourceRestored());
        Assert.assertEquals(result.returnedUrl(), result.sourceUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_060)
    public void nonSortableHeadersDoNotChangeRows() {
        var result = transactionPage.nonSortableHeadersDoNotChangeRows();
        Assert.assertEquals(result.nonSortableHeaders(), result.expectedHeaders());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertTrue(result.url().contains("tab=fee&type=8"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_061)
    public void pageTwoChangesAndReturningRestoresPageOne() {
        var result = advancedPage().pageTwoAndBack();
        Assert.assertNotEquals(signatures(result.pageTwo()), signatures(result.pageOne()));
        Assert.assertEquals(signatures(result.returnedPageOne()), signatures(result.pageOne()));
        Assert.assertEquals(result.activePage(), 1);
        assertFeeConnectionRoute();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_062)
    public void nextActiveMarkerAndPreviousControlsWorkTogether() {
        var page = advancedPage();
        var first = page.firstPageControlState();
        Assert.assertEquals(first.activePage(), 1);
        Assert.assertTrue(first.previousDisabled());
        Assert.assertFalse(first.nextDisabled());

        var next = page.nextControlChangesPage();
        Assert.assertEquals(next.pageAfter(), 2);
        Assert.assertNotEquals(signatures(next.rowsAfter()), signatures(next.rowsBefore()));
        var marker = page.activeMarkerAfterPageChange();
        Assert.assertEquals(marker.activePage(), 2);
        Assert.assertEquals(marker.dataActivePage(), "2");
        Assert.assertTrue(marker.ariaCurrent().contains("pagination item 2"));
        var previous = page.previousControlReturnsPage();
        Assert.assertEquals(previous.pageAfter(), 1);
        assertFeeConnectionRoute();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_063)
    public void lastPageDisablesNextAndContainsAtMostTwentyRows() {
        var result = advancedPage().lastPage();
        Assert.assertTrue(result.expectedLastPage() > 1);
        Assert.assertEquals(result.activePage(), result.expectedLastPage());
        Assert.assertTrue(result.nextDisabled());
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().size() <= 20);
        assertFeeConnectionRoute();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_064)
    public void adjacentPagesDoNotDuplicateTransactions() {
        var result = advancedPage().adjacentPages();
        Set<String> duplicates = new HashSet<>(signatures(result.firstPage()));
        duplicates.retainAll(new HashSet<>(signatures(result.secondPage())));
        Assert.assertTrue(duplicates.isEmpty(), "Hai trang Fee có giao dịch trùng: " + duplicates);
        Assert.assertEquals(result.activePage(), 2);
        assertFeeConnectionRoute();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_065)
    public void amountDescendingPersistsAcrossPageTwoAndReturn() {
        var result = advancedPage().descendingAmountAcrossPages(true);
        assertOrdered(result.firstPage().stream()
                .map(TransactionHistoryPage.TransactionRow::amountValue).toList(),
                Comparator.reverseOrder());
        assertOrdered(result.secondPage().stream()
                .map(TransactionHistoryPage.TransactionRow::amountValue).toList(),
                Comparator.reverseOrder());
        Assert.assertEquals(signatures(result.returnedFirstPage()), signatures(result.firstPage()));
        Assert.assertEquals(result.activePage(), 1);
        assertFeeConnectionRoute();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_066)
    public void createdDateDescendingPersistsAcrossPageTwoAndReturn() {
        var result = advancedPage().descendingCreatedDateAcrossPages(true);
        assertOrdered(result.firstPage().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList(),
                Comparator.reverseOrder());
        assertOrdered(result.secondPage().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList(),
                Comparator.reverseOrder());
        Assert.assertEquals(signatures(result.returnedFirstPage()), signatures(result.firstPage()));
        Assert.assertEquals(result.activePage(), 1);
        assertFeeConnectionRoute();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_067)
    public void invoiceFilterFromPageTwoReturnsToFirstPage() {
        var result = transactionPage.invoiceFilterFromSecondPage("Có");
        Assert.assertEquals(result.pageBefore(), 2);
        Assert.assertEquals(result.pageAfter(), 1);
        Assert.assertTrue(result.selectedText().contains("Có"));
        Assert.assertTrue(!result.rows().isEmpty() || result.empty());
        Assert.assertTrue(result.url().contains("tab=fee&type=8"));
        Assert.assertTrue(result.activeText().contains("Phí kết nối"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_068)
    public void openingDetailFromPageTwoKeepsPageAndRows() {
        var result = advancedPage().detailFromSecondPage();
        Assert.assertTrue(result.openedUrl().contains("id="));
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertFalse(result.closedUrl().contains("id="));
        Assert.assertTrue(result.closedUrl().contains("tab=fee"), result.closedUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains("Phí kết nối"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_069)
    public void resetAfterAmountSortRestoresDefaultOrderAndPageOne() {
        var result = advancedPage().resetAfterAmountSort();
        Assert.assertNotEquals(result.sortedRows(), result.baselineRows());
        Assert.assertEquals(result.restoredRows(), result.baselineRows());
        Assert.assertEquals(result.activePage(), 1);
        assertFeeConnectionRoute();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_070)
    public void resetFromPageTwoReturnsToPageOneAndKeepsSubtype() {
        var result = transactionPage.resetFromSecondPage();
        Assert.assertEquals(result.pageBefore(), 2);
        Assert.assertEquals(result.pageAfter(), 1);
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.url().contains("tab=fee&type=8"));
        Assert.assertTrue(result.activeText().contains("Phí kết nối"));
    }

    private void assertFeeConnectionRoute() {
        Assert.assertTrue(transactionPage.currentUrl().contains("tab=fee&type=8"),
                transactionPage.currentUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains("Phí kết nối"));
    }

    private List<String> signatures(List<TransactionHistoryPage.TransactionRow> rows) {
        return rows.stream().map(TransactionHistoryPage.TransactionRow::signature).toList();
    }

    private String queryValue(String url, String key) {
        var matcher = java.util.regex.Pattern.compile(
                "(?:[?&])" + java.util.regex.Pattern.quote(key) + "=([^&]+)").matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }

    private <T> void assertOrdered(java.util.List<T> actual, Comparator<? super T> comparator) {
        Assert.assertTrue(actual.size() > 1, "Không đủ dữ liệu để kiểm tra sắp xếp");
        var expected = new ArrayList<>(actual);
        expected.sort(comparator);
        Assert.assertEquals(actual, expected);
    }
}
