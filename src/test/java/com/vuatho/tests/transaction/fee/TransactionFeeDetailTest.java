package com.vuatho.tests.transaction.fee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Kiểm tra drawer chi tiết của từng loại Phí & Doanh thu. */
public class TransactionFeeDetailTest extends TransactionFeeTestSupport {
    private final Map<Integer, TransactionHistoryPage.DetailAuditSnapshot> detailAudits =
            new HashMap<>();
    private TransactionHistoryPage.FeeConnectionElementSnapshot feeConnectionElementAudit;

    public static void main(String[] args) {
        TestNgRunner.run(TransactionFeeDetailTest.class,
                "Lịch sử giao dịch", "Phí & Doanh thu - Chi tiết");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_008)
    public void opensAndClosesDetail() {
        verifyOpensAndClosesDetailForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_102)
    public void opensAndClosesDetailType9() {
        verifyOpensAndClosesDetailForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_103)
    public void opensAndClosesDetailType33() {
        verifyOpensAndClosesDetailForSubtype(subtype(33));
    }

    private void verifyOpensAndClosesDetailForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        verifyDetail(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_026)
    public void detailMatchesSourceTypeStatusAmountAndTime() {
        verifyDetailMatchesSourceTypeStatusAmountAndTimeForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_104)
    public void detailMatchesSourceTypeStatusAmountAndTimeType9() {
        verifyDetailMatchesSourceTypeStatusAmountAndTimeForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_105)
    public void detailMatchesSourceTypeStatusAmountAndTimeType33() {
        verifyDetailMatchesSourceTypeStatusAmountAndTimeForSubtype(subtype(33));
    }

    private void verifyDetailMatchesSourceTypeStatusAmountAndTimeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().openFirstDetail();
        String text = result.drawerText();
        String expectedTime = result.source().createdAt()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        Assert.assertTrue(text.contains("Chi tiết giao dịch"));
        Assert.assertTrue(text.contains("Thông tin giao dịch"));
        Assert.assertTrue(text.contains(result.source().type()), text);
        Assert.assertTrue(text.contains(result.source().status()), text);
        Assert.assertTrue(digits(text).contains(digits(result.source().amount())), text);
        Assert.assertTrue(text.contains(expectedTime), text);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_027)
    public void detailUrlContainsOneIdAndKeepsSubtype() {
        verifyDetailUrlContainsOneIdAndKeepsSubtypeForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_106)
    public void detailUrlContainsOneIdAndKeepsSubtypeType9() {
        verifyDetailUrlContainsOneIdAndKeepsSubtypeForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_107)
    public void detailUrlContainsOneIdAndKeepsSubtypeType33() {
        verifyDetailUrlContainsOneIdAndKeepsSubtypeForSubtype(subtype(33));
    }

    private void verifyDetailUrlContainsOneIdAndKeepsSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        String url = advancedPage().openFirstDetail().url();
        Assert.assertEquals(Pattern.compile("(^|[?&])id=[^&]+")
                .matcher(url).results().count(), 1L, url);
        Assert.assertTrue(url.contains("tab=fee&type=" + subtype.type()), url);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_028)
    public void closeIconRemovesIdAndKeepsSubtype() {
        verifyCloseIconRemovesIdAndKeepsSubtypeForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_108)
    public void closeIconRemovesIdAndKeepsSubtypeType9() {
        verifyCloseIconRemovesIdAndKeepsSubtypeForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_109)
    public void closeIconRemovesIdAndKeepsSubtypeType33() {
        verifyCloseIconRemovesIdAndKeepsSubtypeForSubtype(subtype(33));
    }

    private void verifyCloseIconRemovesIdAndKeepsSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().closeDetailWithIcon();
        Assert.assertTrue(result.openedUrl().contains("id="));
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="));
        Assert.assertTrue(result.closedUrl().contains("tab=fee&type=" + subtype.type()), result.closedUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_029)
    public void refreshKeepsFeeConnectionDetailOpen() {
        verifyRefreshKeepsDetailOpen(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_110)
    public void refreshKeepsWalletLinkDetailOpen() {
        verifyRefreshKeepsDetailOpen(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_111)
    public void refreshKeepsMaterialSharingDetailOpen() {
        verifyRefreshKeepsDetailOpen(subtype(33));
    }

    private void verifyRefreshKeepsDetailOpen(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().refreshOpenDetail();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("tab=fee&type=" + subtype.type()), result.actualUrl());
        Assert.assertTrue(result.actualUrl().contains("id="));
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_030)
    public void browserBackAndForwardRestoresFeeConnectionDetail() {
        var result = advancedPage().backAndForwardDetail();
        Assert.assertTrue(result.closedAfterBack());
        Assert.assertFalse(result.backUrl().contains("id="), result.backUrl());
        Assert.assertEquals(result.forwardUrl(), result.openedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
        Assert.assertTrue(transactionPage.activeGroupText().contains(feeSubtype(8).label()),
                transactionPage.activeGroupText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_033)
    public void detailShowsSubtypeSpecificSections() {
        verifyDetailShowsSubtypeSpecificSectionsForSubtype(subtype(8));
    }



    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_170)
    public void detailShowsSubtypeSpecificSectionsType9() {
        verifyDetailShowsSubtypeSpecificSectionsForSubtype(subtype(9));
    }



    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_171)
    public void detailShowsSubtypeSpecificSectionsType33() {
        verifyDetailShowsSubtypeSpecificSectionsForSubtype(subtype(33));
    }

    private void verifyDetailShowsSubtypeSpecificSectionsForSubtype(TransactionCategoryPage.Subtype subtype) {
        String drawerText = detailAudit(subtype).drawerText();
        Assert.assertTrue(drawerText.contains("Trạng thái"), drawerText);
        Assert.assertTrue(drawerText.contains("Thông tin giao dịch"), drawerText);
        Assert.assertTrue(drawerText.contains("Dòng tiền của"), drawerText);
        switch (subtype.type()) {
            case 8 -> assertContainsAll(drawerText, "Người gửi", "Mã đơn dịch vụ");
            case 9 -> assertContainsAll(drawerText, "Người nhận", "Số dư Ví chi phí ban đầu",
                    "Số dư Ví chi phí sau khi nạp", "Cổng thanh toán");
            case 33 -> assertContainsAll(drawerText, "Người gửi", "Biến động số dư", "Mã đơn dịch vụ");
            default -> Assert.fail("Chưa khai báo contract chi tiết cho type=" + subtype.type());
        }
    }






    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_034)
    public void escapeClosesDetailAndKeepsSubtype() {
        verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_112)
    public void escapeClosesDetailAndKeepsSubtypeType9() {
        verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_113)
    public void escapeClosesDetailAndKeepsSubtypeType33() {
        verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(subtype(33));
    }

    private void verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().closeDetailWithEscape();
        Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
        Assert.assertTrue(result.closedUrl().contains("tab=fee&type=" + subtype.type()),
                result.closedUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_035)
    public void deepLinkReopensSameDetailForEachSubtype() {
        verifyDeepLinkReopensSameDetailForEachSubtypeForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_114)
    public void deepLinkReopensSameDetailForEachSubtypeType9() {
        verifyDeepLinkReopensSameDetailForEachSubtypeForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_115)
    public void deepLinkReopensSameDetailForEachSubtypeType33() {
        verifyDeepLinkReopensSameDetailForEachSubtypeForSubtype(subtype(33));
    }

    private void verifyDeepLinkReopensSameDetailForEachSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().reopenByDeepLink();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("tab=fee&type=" + subtype.type()),
                result.actualUrl());
        Assert.assertTrue(result.actualUrl().contains("id="), result.actualUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_036)
    public void partyAndRelatedLinksAreValidAndUnique() {
        verifyPartyAndRelatedLinksAreValidAndUniqueForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_116)
    public void partyAndRelatedLinksAreValidAndUniqueType9() {
        verifyPartyAndRelatedLinksAreValidAndUniqueForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_117)
    public void partyAndRelatedLinksAreValidAndUniqueType33() {
        verifyPartyAndRelatedLinksAreValidAndUniqueForSubtype(subtype(33));
    }

    private void verifyPartyAndRelatedLinksAreValidAndUniqueForSubtype(TransactionCategoryPage.Subtype subtype) {
        var result = detailAudit(subtype);
        Assert.assertTrue(result.userHref().contains("/vuatho/user?id=")
                        || result.userHref().contains("/vuatho/worker?id="),
                "Link chủ thể không hợp lệ: " + result.userHref());
        Assert.assertEquals(new HashSet<>(result.transactionHrefs()).size(),
                result.transactionHrefs().size(), "Link dòng tiền bị trùng");
        result.transactionHrefs().forEach(href -> {
            Assert.assertTrue(href.contains("/vuatho/transaction?"), href);
            Assert.assertTrue(href.contains("tab=all"), href);
            Assert.assertTrue(href.contains("id="), href);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_037)
    public void cashFlowTotalsFinishLoadingForEachSubtype() {
        verifyCashFlowTotalsFinishLoadingForEachSubtypeForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_118)
    public void cashFlowTotalsFinishLoadingForEachSubtypeType9() {
        verifyCashFlowTotalsFinishLoadingForEachSubtypeForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_119)
    public void cashFlowTotalsFinishLoadingForEachSubtypeType33() {
        verifyCashFlowTotalsFinishLoadingForEachSubtypeForSubtype(subtype(33));
    }

    private void verifyCashFlowTotalsFinishLoadingForEachSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        var result = detailAudit(subtype);
        boolean hasIncoming = result.drawerText().contains("Tổng vào");
        boolean hasOutgoing = result.drawerText().contains("Tổng ra");
        boolean hasNet = result.drawerText().contains("Dòng tiền ròng");
        Assert.assertEquals(hasOutgoing, hasIncoming,
                "Tổng ra không đồng bộ với Tổng vào: " + result.drawerText());
        Assert.assertEquals(hasNet, hasIncoming,
                "Dòng tiền ròng không đồng bộ với Tổng vào: " + result.drawerText());
        Assert.assertFalse(result.drawerText().contains("Đang tải"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_038)
    public void feeConnectionCashFlowMarksCurrentTransaction() {
        verifyCashFlowMarksCurrentTransaction(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_120)
    public void walletLinkCashFlowMarksCurrentTransaction() {
        verifyCashFlowMarksCurrentTransaction(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_121)
    public void materialSharingCashFlowMarksCurrentTransaction() {
        verifyCashFlowMarksCurrentTransaction(subtype(33));
    }

    private void verifyCashFlowMarksCurrentTransaction(TransactionCategoryPage.Subtype subtype) {
        var result = detailAudit(subtype);
        Assert.assertTrue(result.currentMarked(), result.drawerText());
        Assert.assertTrue(result.drawerText().toLowerCase().contains("đang xem"));
        Assert.assertTrue(result.drawerText().contains(result.source().type()), result.drawerText());
        Assert.assertTrue(digits(result.drawerText()).contains(digits(result.source().amount())),
                result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_039)
    public void loadMoreAddsWalletLinkRelatedTransactions() {
        verifyLoadMoreAddsRelatedTransactions(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_192)
    public void loadMoreAddsMaterialSharingRelatedTransactions() {
        verifyLoadMoreAddsRelatedTransactions(subtype(33));
    }

    private void verifyLoadMoreAddsRelatedTransactions(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().expandRelatedHistoryOnce();
        Assert.assertTrue(result.openedUrl().contains("tab=fee&type=" + subtype.type()),
                result.openedUrl());
        Assert.assertTrue(result.afterCount() > result.beforeCount(),
                result.beforeCount() + " -> " + result.afterCount());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_040)
    public void openingAnotherRowChangesIdAndDetail() {
        verifyOpeningAnotherRowChangesIdAndDetailForSubtype(subtype(8));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_188)
    public void openingAnotherWalletLinkRowChangesIdAndDetail() {
        verifyOpeningAnotherRowChangesIdAndDetailForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_189)
    public void openingAnotherMaterialSharingRowChangesIdAndDetail() {
        verifyOpeningAnotherRowChangesIdAndDetailForSubtype(subtype(33));
    }

    private void verifyOpeningAnotherRowChangesIdAndDetailForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var result = advancedPage().openAnotherTransactionDetail();
        Assert.assertNotEquals(result.secondUrl(), result.firstUrl());
        Assert.assertNotEquals(result.secondSource().signature(), result.firstSource().signature());
        Assert.assertTrue(result.secondUrl().contains("tab=fee"), result.secondUrl());
        Assert.assertTrue(result.secondUrl().contains("id="), result.secondUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                transactionPage.activeGroupText());
        Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().type()));
        Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().status()));
    }


    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_046)
    public void feeConnectionTimelineEntriesHaveUniqueValidContracts() {
        var result = feeConnectionElementAudit();
        Assert.assertFalse(result.timeline().isEmpty(), result.drawerText());
        Set<String> hrefs = new HashSet<>();
        Pattern time = Pattern.compile("\\b\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}\\b");
        Pattern money = Pattern.compile("[+−-]?\\d[\\d.,]*₫");
        result.timeline().forEach(entry -> {
            Assert.assertTrue(entry.href().contains("/vuatho/transaction?tab=all"), entry.href());
            Assert.assertFalse(queryValue(entry.href(), "id").isBlank(), entry.href());
            Assert.assertEquals(entry.target(), "_blank");
            Assert.assertTrue(hrefs.add(entry.href()), "Link timeline bị trùng: " + entry.href());
            Assert.assertTrue(time.matcher(entry.text()).find(), entry.text());
            Assert.assertTrue(money.matcher(entry.text()).find(), entry.text());
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_172)
    public void everyVisibleStatusOpensMatchingDetailType9() {
        verifyEveryVisibleStatusOpensMatchingDetailForSubtype(subtype(9));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_041,
            dataProvider = "walletLinkStatuses")
    public void walletLinkDetailMatchesEveryAvailableStatus(String status) {
        TransactionCategoryPage.Subtype subtype = category().subtypes().stream()
                .filter(candidate -> candidate.type() == 9).findFirst().orElseThrow();
        openFeeSubtype(subtype);
        var result = advancedPage().openAndCloseVisibleDetailForStatus(status);
        Assert.assertFalse(result.empty(), "Không có dòng Phí liên kết ví trạng thái " + status);
        Assert.assertEquals(result.source().status(), status);
        Assert.assertTrue(result.drawerText().contains(status), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Phí liên kết ví"), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Cổng thanh toán"), result.drawerText());
        Assert.assertTrue(result.closed());
        Assert.assertTrue(result.closedUrl().contains("tab=fee"), result.closedUrl());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                transactionPage.activeGroupText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_042)
    public void feeConnectionDetailFromPageTwoReturnsToPageTwo() {
        var result = advancedPage().detailFromSecondPage();
        Assert.assertTrue(result.openedUrl().contains("tab=fee&type=8"), result.openedUrl());
        Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
        Assert.assertEquals(result.activePage(), 2);
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertTrue(result.closedUrl().contains("tab=fee"), result.closedUrl());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains(feeSubtype(8).label()),
                transactionPage.activeGroupText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_043)
    public void feeConnectionSenderAndOrderLinksHaveCorrectTargets() {
        var result = feeConnectionElementAudit();
        Assert.assertTrue(result.workerHref().contains("/vuatho/worker?id="), result.workerHref());
        Assert.assertEquals(result.workerTarget(), "_blank");
        Assert.assertFalse(queryValue(result.workerHref(), "id").isBlank(), result.workerHref());
        Assert.assertFalse(result.workerName().isBlank(), "Tên người gửi đang trống");
        Assert.assertTrue(Pattern.compile("^\\(\\+84\\)\\s*\\d{9,10}$")
                .matcher(result.workerPhone()).matches(), result.workerPhone());
        Assert.assertEquals(result.cashFlowHeading(), "Dòng tiền của " + result.workerName());
        Assert.assertTrue(result.orderHref().contains("/vuatho/order?id="), result.orderHref());
        Assert.assertEquals(result.orderTarget(), "_blank");
        Assert.assertFalse(result.orderText().isBlank());
        Assert.assertTrue(result.orderHref().endsWith("id=" + result.orderText()),
                result.orderHref() + " <> " + result.orderText());
        Assert.assertTrue(result.closed());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_044)
    public void feeConnectionCashFlowNetEqualsIncomingMinusOutgoing() {
        var result = feeConnectionElementAudit();
        long incoming = signedMoney(result.incoming());
        long outgoing = signedMoney(result.outgoing());
        long net = signedMoney(result.net());
        Assert.assertTrue(incoming >= 0, result.incoming());
        Assert.assertTrue(outgoing <= 0, result.outgoing());
        Assert.assertEquals(net, incoming + outgoing,
                result.incoming() + " + " + result.outgoing() + " != " + result.net());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_045)
    public void feeConnectionCurrentTimelineEntryMatchesOpenedTransaction() {
        var result = feeConnectionElementAudit();
        var current = result.timeline().stream().filter(TransactionHistoryPage.FeeTimelineEntry::current)
                .toList();
        Assert.assertEquals(current.size(), 1, "Số dòng được đánh dấu đang xem");
        String openedId = queryValue(result.openedUrl(), "id");
        Assert.assertEquals(queryValue(current.get(0).href(), "id"), openedId);
        Assert.assertTrue(current.get(0).text().contains(result.source().type()), current.get(0).text());
        Assert.assertTrue(current.get(0).text().contains(result.source().status()), current.get(0).text());
        Assert.assertTrue(digits(current.get(0).text()).contains(digits(result.source().amount())),
                current.get(0).text());
        Assert.assertTrue(current.get(0).text().contains(result.source().createdAt()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))), current.get(0).text());
    }


    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_173)
    public void everyVisibleStatusOpensMatchingDetailType33() {
        verifyEveryVisibleStatusOpensMatchingDetailForSubtype(subtype(33));
    }

    private void verifyEveryVisibleStatusOpensMatchingDetailForSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        var results = advancedPage().openAndCloseEveryVisibleStatus();
        Assert.assertFalse(results.isEmpty(), "Không có trạng thái để kiểm tra " + subtype.label());
        Assert.assertEquals(results.stream().map(TransactionHistoryPage.StatusDetailSnapshot::expectedStatus)
                .distinct().count(), (long) results.size());
        results.forEach(result -> {
            Assert.assertFalse(result.empty(), "Không mở được trạng thái " + result.expectedStatus());
            Assert.assertEquals(result.source().status(), result.expectedStatus());
            Assert.assertTrue(result.drawerText().contains(result.expectedStatus()), result.drawerText());
            Assert.assertTrue(result.drawerText().contains(subtype.label()), result.drawerText());
            Assert.assertTrue(result.closed());
        });
    }






    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_087)
    public void everyVisibleStatusOpensMatchingDetail() {
        verifyEveryVisibleStatusOpensMatchingDetailForSubtype(subtype(8));
    }




    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_090)
    public void invalidTypeDoesNotOpenOrSelectWrongFeeSubtype() {
        var result = transactionPage.openInvalidFeeRoute("type=999999");
        Assert.assertFalse(result.drawerOpen(), result.drawerText());
        Assert.assertTrue(result.url().contains("tab=fee"), result.url());
        Assert.assertTrue(category().subtypes().stream().noneMatch(subtype ->
                result.url().contains("type=" + subtype.type())
                        && result.activeText().contains(subtype.label())),
                result.url() + " | " + result.activeText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_091)
    public void nonexistentTransactionIdDoesNotOpenWrongDetail() {
        var result = transactionPage.openInvalidFeeRoute("type=8&id=AUTOMATION-NOT-FOUND-987654321");
        Assert.assertFalse(result.drawerOpen(), result.drawerText());
        Assert.assertTrue(result.url().contains("tab=fee&type=8"), result.url());
        Assert.assertTrue(result.activeText().contains(feeSubtype(8).label()), result.activeText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_092)
    public void missingTypeUsesValidDefaultWithoutOpeningDetail() {
        var result = transactionPage.openInvalidFeeRoute("");
        Assert.assertFalse(result.drawerOpen(), result.drawerText());
        Assert.assertTrue(result.url().contains("tab=fee"), result.url());
        Assert.assertTrue(category().subtypes().stream()
                .anyMatch(subtype -> result.activeText().contains(subtype.label())), result.activeText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_093)
    public void transactionIdCannotBeRenderedUnderWrongSubtype() {
        var result = transactionPage.openTransactionIdUnderWrongSubtype(
                feeSubtype(9), feeSubtype(8));
        var attempted = result.attempted();
        boolean rejected = !attempted.drawerOpen();
        boolean canonicalized = attempted.url().contains("type=" + result.sourceSubtype().type())
                && attempted.activeText().contains(result.sourceSubtype().label());
        Assert.assertTrue(rejected || canonicalized,
                "ID " + result.transactionId() + " thuộc " + result.sourceSubtype().label()
                        + " nhưng đang hiển thị dưới " + attempted.activeText()
                        + " tại " + attempted.url());
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    private long signedMoney(String value) {
        long amount = Long.parseLong(digits(value));
        return value.contains("−") || value.contains("-") ? -amount : amount;
    }

    private String queryValue(String url, String key) {
        return Pattern.compile("(?:[?&])" + Pattern.quote(key) + "=([^&]+)")
                .matcher(url).results().map(match -> match.group(1)).findFirst().orElse("");
    }

    private void assertContainsAll(String actual, String... expectedParts) {
        for (String expectedPart : expectedParts) {
            Assert.assertTrue(actual.contains(expectedPart),
                    "Thiếu nội dung '" + expectedPart + "': " + actual);
        }
    }

    private TransactionCategoryPage.Subtype feeSubtype(int type) {
        return category().subtypes().stream()
                .filter(candidate -> candidate.type() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không có loại phí type=" + type));
    }

    private TransactionHistoryPage.DetailAuditSnapshot detailAudit(
            TransactionCategoryPage.Subtype subtype) {
        TransactionHistoryPage.DetailAuditSnapshot cached = detailAudits.get(subtype.type());
        if (cached != null) {
            return cached;
        }
        openFeeSubtype(subtype);
        TransactionHistoryPage.DetailAuditSnapshot audit = advancedPage().auditFirstDetailInOneFlow();
        detailAudits.put(subtype.type(), audit);
        return audit;
    }

    private TransactionHistoryPage.FeeConnectionElementSnapshot feeConnectionElementAudit() {
        if (feeConnectionElementAudit == null) {
            openFeeSubtype(feeSubtype(8));
            feeConnectionElementAudit = advancedPage().auditFeeConnectionElement();
        }
        return feeConnectionElementAudit;
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
