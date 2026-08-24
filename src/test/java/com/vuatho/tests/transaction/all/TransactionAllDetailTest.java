package com.vuatho.tests.transaction.all;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionHistoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/** Kiểm tra drawer và deep-link chi tiết giao dịch từ tab Tất cả. */
public class TransactionAllDetailTest extends TransactionHistoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAllDetailTest.class,
                "Lịch sử giao dịch", "Tab Tất cả - Chi tiết");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_028)
    public void opensTransactionDetailFromRow() {
        var result = transactionPage.openFirstDetail();
        Assert.assertTrue(result.url().contains("tab=all"));
        Assert.assertTrue(result.url().contains("id="));
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_029)
    public void detailShowsTransactionInformation() {
        String text = transactionPage.openFirstDetail().drawerText();
        Assert.assertTrue(text.contains("Trạng thái"));
        Assert.assertTrue(text.contains("Thông tin giao dịch"));
        Assert.assertTrue(text.contains("Số tiền"));
        Assert.assertTrue(text.contains("Loại"));
        Assert.assertTrue(text.contains("Thời gian"));
        Assert.assertTrue(text.contains("Phí"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_030)
    public void detailShowsCashFlowTotals() {
        var result = transactionPage.waitForRelatedHistory();
        Assert.assertTrue(result.loaded(), result.drawerText());
        String text = result.drawerText();
        Assert.assertTrue(text.contains("Dòng tiền"));
        Assert.assertTrue(text.contains("Tổng vào"));
        Assert.assertTrue(text.contains("Tổng ra"));
        Assert.assertTrue(text.contains("Dòng tiền ròng"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_031)
    public void detailMarksCurrentRelatedTransaction() {
        var result = transactionPage.waitForRelatedHistory();
        Assert.assertTrue(result.currentMarked(), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_032)
    public void closingDetailRemovesIdFromUrl() {
        var result = transactionPage.closeFirstDetail();
        Assert.assertTrue(result.openedUrl().contains("id="));
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="));
        Assert.assertTrue(result.closedUrl().contains("tab=all"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_033)
    public void deepLinkReopensSameDetail() {
        var result = transactionPage.reopenByDeepLink();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_037)
    public void detailMatchesSelectedTransactionType() {
        var result = transactionPage.openFirstDetail();
        Assert.assertFalse(result.source().type().isBlank());
        Assert.assertTrue(result.drawerText().contains(result.source().type()), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_038)
    public void detailMatchesSelectedTransactionStatus() {
        var result = transactionPage.openFirstDetail();
        Assert.assertFalse(result.source().status().isBlank());
        Assert.assertTrue(result.drawerText().contains(result.source().status()), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_039)
    public void detailMatchesSelectedTransactionAmount() {
        var result = transactionPage.openFirstDetail();
        String expectedDigits = digits(result.source().amount());
        Assert.assertFalse(expectedDigits.isBlank());
        Assert.assertTrue(digits(result.drawerText()).contains(expectedDigits), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_040)
    public void detailMatchesGatewayAndCreatedTime() {
        var result = transactionPage.openFirstDetail();
        String expectedTime = result.source().createdAt()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        Assert.assertFalse(result.source().gateway().isBlank());
        Assert.assertTrue(result.drawerText().contains(result.source().gateway()), result.drawerText());
        Assert.assertTrue(result.drawerText().contains(expectedTime), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_041)
    public void detailUrlContainsExactlyOneTransactionId() {
        String url = transactionPage.openFirstDetail().url();
        long idParameters = Pattern.compile("(^|[?&])id=[^&]+").matcher(url).results().count();
        Assert.assertEquals(idParameters, 1L, url);
        Assert.assertTrue(url.contains("tab=all"), url);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_042)
    public void refreshKeepsOpenDetail() {
        var result = transactionPage.refreshOpenDetail();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_043)
    public void browserBackAndForwardRestoresDetail() {
        var result = transactionPage.backAndForwardDetail();
        Assert.assertTrue(result.closedAfterBack());
        Assert.assertFalse(result.backUrl().contains("id="), result.backUrl());
        Assert.assertEquals(result.forwardUrl(), result.openedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_044)
    public void relatedHistoryFinishesLoading() {
        var result = transactionPage.waitForRelatedHistory();
        Assert.assertTrue(result.loaded(), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Dòng tiền"), result.drawerText());
        Assert.assertFalse(result.drawerText().contains("Đang tải"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_045)
    public void currentCashFlowEntryMatchesSourceTransaction() {
        var result = transactionPage.waitForRelatedHistory();
        Assert.assertTrue(result.loaded(), result.drawerText());
        Assert.assertTrue(result.drawerText().contains(result.source().type()), result.drawerText());
        Assert.assertTrue(result.drawerText().contains(result.source().status()), result.drawerText());
        Assert.assertTrue(digits(result.drawerText()).contains(digits(result.source().amount())),
                result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_046)
    public void openingAnotherTransactionChangesDetail() {
        var result = transactionPage.openAnotherTransactionDetail();
        Assert.assertNotEquals(result.secondUrl(), result.firstUrl());
        Assert.assertNotEquals(result.secondSource().signature(), result.firstSource().signature());
        Assert.assertTrue(result.secondUrl().contains("id="));
        Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().type()),
                result.secondDrawerText());
        Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().status()),
                result.secondDrawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_094)
    public void detailCloseButtonHasAccessibleName() {
        var result = transactionPage.inspectDetailCloseAccessibility();
        boolean hasAccessibleName = !result.ariaLabel().isBlank()
                || !result.title().isBlank() || !result.text().isBlank();

        Assert.assertTrue(hasAccessibleName,
                "Nút đóng drawer cần aria-label, title hoặc nội dung chữ");
        Assert.assertTrue(result.closed());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_095)
    public void rejectedTransactionLinkOpensExpectedDetailFromAllTab() {
        var result = transactionPage.openRejectedTransactionLink();

        Assert.assertTrue(result.sourceUrl().contains("tab=all"), result.sourceUrl());
        Assert.assertTrue(result.linkText().contains("Xem giao dịch bị từ chối"),
                result.linkText());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("/vuatho/transaction?"), result.actualUrl());
        Assert.assertTrue(result.actualUrl().contains("id="), result.actualUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"),
                result.drawerText());
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }
}
