package com.vuatho.tests.transaction.assistant.platformfee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionAssistantPlatformFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/** Kiểm tra drawer chi tiết giao dịch của tab Thợ phụ. */
public class TransactionAssistantPlatformFeeDetailTest extends TransactionAssistantPlatformFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPlatformFeeDetailTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Phí nền tảng - Chi tiết");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_008)
    public void opensAndClosesDetail() {
        verifyDetail();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_022)
    public void filterPersistsAfterDetail() {
        var result = advancedPage().filterPersistsAfterDetail();
        Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertTrue(result.url().contains("tab=assistant"));
        Assert.assertFalse(result.url().contains("id="));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_033)
    public void detailShowsAllInformationSections() {
        String text = advancedPage().openFirstDetail().drawerText();
        Assert.assertTrue(text.contains("Chi tiết giao dịch"));
        Assert.assertTrue(text.contains("Trạng thái"));
        Assert.assertTrue(text.contains("Người gửi"));
        Assert.assertTrue(text.contains("Biến động số dư"));
        Assert.assertTrue(text.contains("Thông tin giao dịch"));
        Assert.assertTrue(text.contains("Số tiền"));
        Assert.assertTrue(text.contains("Loại"));
        Assert.assertTrue(text.contains("Thời gian"));
        Assert.assertTrue(text.contains("Dòng tiền của"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_035)
    public void detailMatchesSelectedStatus() {
        var result = advancedPage().openFirstDetail();
        Assert.assertFalse(result.source().status().isBlank());
        Assert.assertTrue(result.drawerText().contains(result.source().status()), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_036)
    public void detailMatchesSelectedAmount() {
        var result = advancedPage().openFirstDetail();
        String expected = digits(result.source().amount());
        Assert.assertFalse(expected.isBlank());
        Assert.assertTrue(digits(result.drawerText()).contains(expected), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_037)
    public void detailMatchesSelectedTime() {
        var result = advancedPage().openFirstDetail();
        String expected = result.source().createdAt()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        Assert.assertTrue(result.drawerText().contains(expected), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_038)
    public void detailHasValidSenderLink() {
        var result = advancedPage().detailLinks();
        Assert.assertFalse(result.userHref().isBlank());
        Assert.assertTrue(result.userHref().contains("/vuatho/user?id="), result.userHref());
        Assert.assertTrue(result.drawerText().contains("Người gửi"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_039)
    public void detailUrlContainsOneIdAndKeepsSubtype() {
        String url = advancedPage().openFirstDetail().url();
        long ids = Pattern.compile("(^|[?&])id=[^&]+").matcher(url).results().count();
        Assert.assertEquals(ids, 1L, url);
        Assert.assertTrue(url.contains("tab=assistant"), url);
        Assert.assertTrue(url.contains("type=30"), url);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_040)
    public void refreshKeepsOpenDetail() {
        var result = advancedPage().refreshOpenDetail();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_041)
    public void deepLinkReopensSameDetail() {
        var result = advancedPage().reopenByDeepLink();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
        Assert.assertTrue(result.drawerText().contains("Phí nền tảng thợ phụ"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_042)
    public void browserBackAndForwardRestoresDetail() {
        var result = advancedPage().backAndForwardDetail();
        Assert.assertTrue(result.closedAfterBack());
        Assert.assertFalse(result.backUrl().contains("id="), result.backUrl());
        Assert.assertTrue(result.backUrl().contains("tab=assistant"), result.backUrl());
        Assert.assertEquals(result.forwardUrl(), result.openedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_043)
    public void detailShowsCashFlowTotals() {
        String text = advancedPage().waitForRelatedHistory().drawerText();
        Assert.assertTrue(text.contains("Tổng vào"), text);
        Assert.assertTrue(text.contains("Tổng ra"), text);
        Assert.assertTrue(text.contains("Dòng tiền ròng"), text);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_044)
    public void relatedHistoryFinishesLoading() {
        var result = advancedPage().waitForRelatedHistory();
        Assert.assertTrue(result.loaded(), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Dòng tiền của"));
        Assert.assertFalse(result.drawerText().contains("Đang tải"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_045)
    public void loadMoreReachesCurrentTransaction() {
        var result = advancedPage().loadRelatedHistoryUntilCurrent();
        Assert.assertTrue(result.loaded(), result.drawerText());
        Assert.assertTrue(result.currentMarked(), result.drawerText());
        Assert.assertTrue(result.drawerText().toLowerCase().contains("đang xem"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_046)
    public void currentCashFlowEntryMatchesSource() {
        var result = advancedPage().loadRelatedHistoryUntilCurrent();
        Assert.assertTrue(result.currentMarked(), result.drawerText());
        Assert.assertTrue(result.drawerText().contains(result.source().type()), result.drawerText());
        Assert.assertTrue(result.drawerText().contains(result.source().status()), result.drawerText());
        Assert.assertTrue(digits(result.drawerText()).contains(digits(result.source().amount())),
                result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_047)
    public void openingAnotherRowChangesDetail() {
        var result = advancedPage().openAnotherTransactionDetail();
        Assert.assertNotEquals(result.secondUrl(), result.firstUrl());
        Assert.assertNotEquals(result.secondSource().signature(), result.firstSource().signature());
        Assert.assertTrue(result.secondUrl().contains("id="));
        Assert.assertTrue(result.secondUrl().contains("tab=assistant"));
        Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().type()));
        Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().status()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_048)
    public void relatedLinkOpensExpectedTransaction() {
        var result = advancedPage().openFirstRelatedTransaction();
        Assert.assertNotEquals(result.actualUrl(), result.sourceUrl());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("tab=all"));
        Assert.assertTrue(result.actualUrl().contains("id="));
        Assert.assertFalse(result.sourceText().isBlank());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_049)
    public void closeIconRemovesIdAndKeepsSubtype() {
        var result = advancedPage().closeDetailWithIcon();
        Assert.assertTrue(result.openedUrl().contains("id="));
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="));
        Assert.assertTrue(result.closedUrl().contains("tab=assistant"));
        Assert.assertTrue(result.closedUrl().contains("type=30"));
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }
}
