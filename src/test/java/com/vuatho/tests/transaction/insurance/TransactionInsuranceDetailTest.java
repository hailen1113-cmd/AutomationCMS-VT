package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionInsuranceTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Kiểm tra drawer chi tiết của hai loại VT Care. */
public class TransactionInsuranceDetailTest extends TransactionInsuranceTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionInsuranceDetailTest.class,
                "Lịch sử giao dịch", "VT Care - Chi tiết");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_008)
    public void opensAndClosesBothSubtypeDetails() {
        for (TransactionCategoryPage.Subtype subtype : category().subtypes()) {
            openInsuranceSubtype(subtype);
            verifyDetail(subtype);
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_023,
            dataProvider = "insuranceSubtypes")
    public void detailMatchesSourceRow(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().openFirstDetail();
        Assert.assertTrue(result.url().contains("tab=insurance&type=" + subtype.type()));
        Assert.assertTrue(result.url().contains("id="));
        Assert.assertTrue(result.drawerText().contains(result.source().status()), result.drawerText());
        Assert.assertTrue(result.drawerText().contains(result.source().type()), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
        Assert.assertTrue(result.drawerText().contains("Số tiền"));
        Assert.assertTrue(result.drawerText().contains("Thời gian"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_024,
            dataProvider = "insuranceSubtypes")
    public void deepLinkReopensSameDetail(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().reopenByDeepLink();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("type=" + subtype.type()));
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_025,
            dataProvider = "insuranceSubtypes")
    public void headerIconClosesDetailAndKeepsSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().closeDetailWithHeaderIcon();
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="));
        Assert.assertTrue(result.closedUrl().contains("tab=insurance"));
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_026)
    public void dailyFeeDetailShowsCompleteCashFlowAndCurrentTimeline() {
        TransactionCategoryPage.Subtype subtype = category().subtypes().stream()
                .filter(candidate -> candidate.type() == 25).findFirst().orElseThrow();
        openInsuranceSubtype(subtype);
        var result = advancedPage().auditFirstDetailInOneFlow();
        String text = result.drawerText();
        Assert.assertTrue(text.contains("Người gửi"), text);
        Assert.assertTrue(text.contains("Biến động số dư"), text);
        Assert.assertTrue(text.contains("Tổng vào"), text);
        Assert.assertTrue(text.contains("Tổng ra"), text);
        Assert.assertTrue(text.contains("Dòng tiền ròng"), text);
        Assert.assertTrue(result.currentMarked(), text);
        Assert.assertTrue(result.afterRelatedCount() >= result.beforeRelatedCount());
        Assert.assertEquals(new HashSet<>(result.transactionHrefs()).size(),
                result.transactionHrefs().size(), "Link timeline bị trùng");
        Assert.assertTrue(result.closed());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_027,
            dataProvider = "insuranceSubtypes")
    public void cashFlowNetEqualsIncomingPlusOutgoing(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        String text = advancedPage().waitForCashFlowTotals().drawerText();
        BigDecimal incoming = moneyAfterLabel(text, "Tổng vào");
        BigDecimal outgoing = moneyAfterLabel(text, "Tổng ra");
        BigDecimal net = moneyAfterLabel(text, "Dòng tiền ròng");
        Assert.assertTrue(incoming.signum() >= 0, incoming.toPlainString());
        Assert.assertTrue(outgoing.signum() <= 0, outgoing.toPlainString());
        Assert.assertEquals(net, incoming.add(outgoing),
                incoming + " + " + outgoing + " != " + net);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_028,
            dataProvider = "insuranceSubtypes")
    public void senderLinkOpensProfileAndReturnsToSameDrawer(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().openPartyProfileAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/user?id=")
                || result.expectedUrl().contains("/vuatho/worker?id="), result.expectedUrl());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertFalse(result.targetText().isBlank());
        Assert.assertTrue(result.sourceRestored());
        Assert.assertEquals(result.returnedUrl(), result.sourceUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_029,
            dataProvider = "insuranceSubtypes")
    public void timelineLinkOpensDifferentTransactionAndReturns(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().openRelatedTransactionAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/transaction?tab=all"),
                result.expectedUrl());
        Assert.assertNotEquals(queryValue(result.expectedUrl(), "id"),
                queryValue(result.sourceUrl(), "id"));
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.targetText().contains("Chi tiết giao dịch"), result.targetText());
        Assert.assertTrue(result.sourceRestored());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_030,
            dataProvider = "insuranceSubtypes")
    public void escapeClosesDetailAndKeepsSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().closeDetailWithEscape();
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
        Assert.assertTrue(result.closedUrl().contains("tab=insurance&type=" + subtype.type()),
                result.closedUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_031,
            dataProvider = "insuranceSubtypes")
    public void refreshKeepsSameDetailAndSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().refreshOpenDetail();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("tab=insurance&type=" + subtype.type()));
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_032,
            dataProvider = "insuranceSubtypes")
    public void backAndForwardRestoresSameDetail(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().backAndForwardDetail();
        Assert.assertTrue(result.closedAfterBack());
        Assert.assertFalse(result.backUrl().contains("id="), result.backUrl());
        Assert.assertEquals(result.forwardUrl(), result.openedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_033)
    public void completedDailyFeeShowsOnlyValidActions() {
        TransactionCategoryPage.Subtype subtype = category().subtypes().stream()
                .filter(candidate -> candidate.type() == 25).findFirst().orElseThrow();
        openInsuranceSubtype(subtype);
        var result = advancedPage().inspectFirstDetailActions();
        Assert.assertEquals(result.source().status(), "Thành công");
        Assert.assertTrue(result.cancelVisible(), "Nút Hủy phải hiển thị");
        Assert.assertTrue(result.rejectPresent(), "Nút Từ chối phải tồn tại trong DOM");
        Assert.assertFalse(result.rejectVisible(), "Nút Từ chối không được hiển thị khi đã Thành công");
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
    }

    private BigDecimal moneyAfterLabel(String text, String label) {
        Matcher matcher = Pattern.compile(Pattern.quote(label)
                + "\\s*([+−-]?[0-9][0-9.,]*)₫").matcher(text);
        Assert.assertTrue(matcher.find(), "Không tìm thấy số tiền sau " + label + ": " + text);
        String raw = matcher.group(1).replace("−", "-");
        boolean decimalComma = raw.matches(".*,[0-9]{1,2}$");
        boolean decimalDot = raw.matches(".*\\.[0-9]{1,2}$");
        String normalized;
        if (decimalComma) {
            normalized = raw.replace(".", "").replace(',', '.');
        } else if (decimalDot) {
            normalized = raw.replace(",", "");
        } else {
            normalized = raw.replace(".", "").replace(",", "");
        }
        return new BigDecimal(normalized);
    }

    private String queryValue(String url, String key) {
        Matcher matcher = Pattern.compile("(?:^|[?&])" + Pattern.quote(key) + "=([^&]+)")
                .matcher(url == null ? "" : url);
        return matcher.find() ? matcher.group(1) : "";
    }
}
