package com.vuatho.tests.transaction.assistant.penalty;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionAssistantPenaltyTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/** Kiểm tra drawer chi tiết giao dịch của loại Tiền phạt. */
public class TransactionAssistantPenaltyDetailTest extends TransactionAssistantPenaltyTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPenaltyDetailTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Tiền phạt - Chi tiết");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_067)
    public void opensAndClosesDetail() {
        verifyDetail(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_068)
    public void filterPersistsAfterDetail() {
        var result = advancedPage().filterPersistsAfterDetail();
        Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        assertPenaltyUrl(result.url(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_069)
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

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_087)
    public void detailMatchesSelectedRowData() {
        var result = advancedPage().openFirstDetail();
        Assert.assertFalse(result.source().status().isBlank());
        Assert.assertTrue(result.drawerText().contains(result.source().status()), result.drawerText());
        String expected = digits(result.source().amount());
        Assert.assertFalse(expected.isBlank());
        Assert.assertTrue(digits(result.drawerText()).contains(expected), result.drawerText());
        String expectedTime = result.source().createdAt()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        Assert.assertTrue(result.drawerText().contains(expectedTime), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_073)
    public void detailHasValidSenderLink() {
        var result = advancedPage().detailLinks();
        Assert.assertFalse(result.userHref().isBlank());
        Assert.assertTrue(result.userHref().contains("/vuatho/user?id="), result.userHref());
        Assert.assertTrue(result.drawerText().contains("Người gửi"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_074)
    public void detailUrlContainsOneIdAndKeepsPenaltySubtype() {
        String url = advancedPage().openFirstDetail().url();
        long ids = Pattern.compile("(^|[?&])id=[^&]+").matcher(url).results().count();
        Assert.assertEquals(ids, 1L, url);
        assertPenaltyUrl(url, true);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_075)
    public void refreshKeepsOpenDetail() {
        var result = advancedPage().refreshOpenDetail();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
        Assert.assertTrue(result.drawerText().contains("Tiền phạt thợ phụ"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_076)
    public void deepLinkReopensSameDetail() {
        var result = advancedPage().reopenByDeepLink();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
        Assert.assertTrue(result.drawerText().contains("Tiền phạt thợ phụ"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_077)
    public void browserBackAndForwardRestoresDetail() {
        var result = advancedPage().backAndForwardDetail();
        Assert.assertTrue(result.closedAfterBack());
        Assert.assertFalse(result.backUrl().contains("id="), result.backUrl());
        assertPenaltyUrl(result.backUrl(), false);
        Assert.assertEquals(result.forwardUrl(), result.openedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_088)
    public void relatedHistoryLoadsAndShowsCashFlowTotals() {
        var result = advancedPage().waitForCashFlowTotals();
        Assert.assertTrue(result.loaded(), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Dòng tiền của"));
        Assert.assertFalse(result.drawerText().contains("Đang tải"), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Tổng vào"), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Tổng ra"), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Dòng tiền ròng"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_089)
    public void currentCashFlowEntryMatchesSource() {
        var result = advancedPage().loadRelatedHistoryUntilCurrent();
        Assert.assertTrue(result.loaded(), result.drawerText());
        Assert.assertTrue(result.currentMarked(), result.drawerText());
        Assert.assertTrue(result.drawerText().toLowerCase().contains("đang xem"));
        Assert.assertTrue(result.drawerText().contains(result.source().type()), result.drawerText());
        Assert.assertTrue(result.drawerText().contains(result.source().status()), result.drawerText());
        Assert.assertTrue(digits(result.drawerText()).contains(digits(result.source().amount())),
                result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_082)
    public void openingAnotherRowChangesDetail() {
        var result = advancedPage().openAnotherTransactionDetail();
        Assert.assertNotEquals(result.secondUrl(), result.firstUrl());
        Assert.assertNotEquals(result.secondSource().signature(), result.firstSource().signature());
        assertPenaltyUrl(result.secondUrl(), true);
        Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().type()));
        Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().status()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_083)
    public void relatedLinkOpensExpectedTransaction() {
        var result = advancedPage().openFirstRelatedTransaction();
        Assert.assertNotEquals(result.actualUrl(), result.sourceUrl());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("tab=all"));
        Assert.assertTrue(result.actualUrl().contains("id="));
        Assert.assertFalse(result.sourceText().isBlank());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_084)
    public void closeIconRemovesIdAndKeepsPenaltySubtype() {
        var result = advancedPage().closeDetailWithIcon();
        Assert.assertTrue(result.openedUrl().contains("id="));
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="));
        assertPenaltyUrl(result.closedUrl(), false);
    }

    private void assertPenaltyUrl(String url, boolean detailExpected) {
        Assert.assertTrue(url.contains("tab=assistant"), url);
        Assert.assertTrue(url.contains("type=31"), url);
        Assert.assertEquals(url.contains("id="), detailExpected, url);
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }
}
