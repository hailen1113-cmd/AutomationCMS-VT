package com.vuatho.tests.transaction.system;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionSystemTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/** Kiểm tra drawer, deep-link và dòng tiền của tab Hệ thống. */
public class TransactionSystemDetailTest extends TransactionSystemTestSupport {
    private TransactionHistoryPage.DetailAuditSnapshot audit;

    public static void main(String[] args) {
        TestNgRunner.run(TransactionSystemDetailTest.class,
                "Lịch sử giao dịch", "Hệ thống - Chi tiết nâng cao");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_075)
    public void detailMatchesSource() {
        var result = advancedPage().openFirstDetail();
        String text = result.drawerText();
        String expectedTime = result.source().createdAt().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        Assert.assertTrue(text.contains("Chi tiết giao dịch"), text);
        Assert.assertTrue(text.contains("Thông tin giao dịch"), text);
        Assert.assertTrue(text.contains(result.source().status()), text);
        Assert.assertTrue(digits(text).contains(digits(result.source().amount())), text);
        Assert.assertTrue(text.contains(expectedTime), text);
        assertSystemRoute(result.url(), true);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_076)
    public void detailUrlContainsSingleId() {
        String url = advancedPage().openFirstDetail().url();
        Assert.assertEquals(Pattern.compile("(^|[?&])id=[^&]+")
                .matcher(url).results().count(), 1L, url);
        assertSystemRoute(url, true);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_077)
    public void closeIconRemovesIdAndKeepsTab() {
        var result = advancedPage().closeDetailWithIcon();
        Assert.assertTrue(result.closed());
        Assert.assertTrue(result.openedUrl().contains("id="));
        assertSystemRoute(result.closedUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_078)
    public void escapeRemovesIdAndKeepsTab() {
        var result = advancedPage().closeDetailWithEscape();
        Assert.assertTrue(result.closed());
        Assert.assertTrue(result.openedUrl().contains("id="));
        assertSystemRoute(result.closedUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_079)
    public void refreshKeepsDetailOpen() {
        var result = advancedPage().refreshOpenDetail();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
        assertSystemRoute(result.actualUrl(), true);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_080)
    public void browserBackAndForwardRestoresDetail() {
        var result = advancedPage().backAndForwardDetail();
        Assert.assertTrue(result.closedAfterBack());
        Assert.assertFalse(result.backUrl().contains("id="), result.backUrl());
        Assert.assertEquals(result.forwardUrl(), result.openedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
        assertSystemRoute(result.forwardUrl(), true);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_081)
    public void deepLinkReopensDetail() {
        var result = advancedPage().reopenByDeepLink();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
        assertSystemRoute(result.actualUrl(), true);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_082)
    public void cashFlowAndLinksAreValid() {
        var result = audit();
        Assert.assertFalse(result.drawerText().contains("Đang tải"), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Dòng tiền của"), result.drawerText());
        Assert.assertTrue(result.drawerText().contains(result.source().status()),
                result.drawerText());
        Assert.assertTrue(digits(result.drawerText()).contains(
                digits(result.source().amount())), result.drawerText());
        Assert.assertTrue(result.userHref().contains("/vuatho/user?id=")
                || result.userHref().contains("/vuatho/worker?id="), result.userHref());
        Assert.assertEquals(new HashSet<>(result.transactionHrefs()).size(),
                result.transactionHrefs().size(), "Link dòng tiền bị trùng");
        result.transactionHrefs().forEach(href -> {
            Assert.assertTrue(href.contains("/vuatho/transaction?"), href);
            Assert.assertTrue(href.contains("id="), href);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_083)
    public void partyProfileOpensAndReturns() {
        var result = advancedPage().openPartyProfileAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/user?id=")
                || result.expectedUrl().contains("/vuatho/worker?id="), result.expectedUrl());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertFalse(result.targetText().isBlank());
        Assert.assertTrue(result.sourceRestored());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_084)
    public void relatedTransactionOpensAndReturns() {
        var result = advancedPage().openRelatedTransactionAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/transaction?"),
                result.expectedUrl());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.sourceRestored());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_085)
    public void everyVisibleStatusOpensMatchingDetail() {
        var results = advancedPage().openAndCloseEveryVisibleStatus();
        Assert.assertFalse(results.isEmpty(), "Không có trạng thái cho Hệ thống");
        results.forEach(result -> {
            Assert.assertFalse(result.empty(), result.expectedStatus());
            Assert.assertEquals(result.source().status(), result.expectedStatus());
            Assert.assertTrue(result.drawerText().contains(result.expectedStatus()));
            Assert.assertTrue(result.closed());
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_086)
    public void openingAnotherRowChangesDetail() {
        var result = advancedPage().openAnotherTransactionDetail();
        Assert.assertNotEquals(result.secondUrl(), result.firstUrl());
        Assert.assertNotEquals(result.secondSource().signature(), result.firstSource().signature());
        Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().status()));
        assertSystemRoute(result.secondUrl(), true);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_087)
    public void relatedHistoryExpandsWithMoreRows() {
        var result = advancedPage().expandRelatedHistoryOnce();
        Assert.assertTrue(result.afterCount() > result.beforeCount());
        Assert.assertTrue(result.drawerText().contains("Dòng tiền"), result.drawerText());
        assertSystemRoute(result.openedUrl(), true);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_088)
    public void closeButtonHasAccessibleName() {
        var result = advancedPage().inspectDetailCloseAccessibility();
        Assert.assertTrue(!result.ariaLabel().isBlank()
                        || !result.title().isBlank() || !result.text().isBlank(),
                "Nút đóng drawer không có accessible name type=7");
        Assert.assertTrue(result.closed());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_096)
    public void receiverAndExpenseWalletBalancesAreComplete() {
        var result = advancedPage().inspectSystemDetailContaining(
                "Người nhận", "Số dư Ví chi phí ban đầu", "Số dư Ví chi phí sau khi nạp");
        Assert.assertTrue(normalizeWhitespace(result.drawerText()).contains("Loại Hệ thống"),
                result.drawerText());
        assertParticipant(result, "Người nhận");
        assertMoneyAfterLabel(result.drawerText(), "Số dư Ví chi phí ban đầu");
        assertMoneyAfterLabel(result.drawerText(), "Số dư Ví chi phí sau khi nạp");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_097)
    public void senderAndBalanceChangeAreComplete() {
        var result = advancedPage().inspectSystemDetailContaining("Người gửi", "Biến động số dư");
        assertParticipant(result, "Người gửi");
        assertMoneyAfterLabel(result.drawerText(), "Biến động số dư");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_098)
    public void systemDescriptionAndConnectionFeeAreComplete() {
        var result = advancedPage().inspectSystemDetailContaining(
                "Thông tin hệ thống", "Mô tả hệ thống", "Phí kết nối");
        String description = textBetween(result.drawerText(), "Mô tả hệ thống",
                List.of("Phí kết nối", "Giao dịch liên quan", "Hủy"));
        Assert.assertFalse(description.isBlank(), "Mô tả hệ thống đang rỗng");
        assertMoneyAfterLabel(result.drawerText(), "Phí kết nối");
        Assert.assertTrue(result.closed());
        assertSystemRoute(result.closedUrl(), false);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_099)
    public void cashFlowTotalsAreComplete() {
        var result = advancedPage().inspectSystemDetailContaining(
                "Tổng vào", "Tổng ra", "Dòng tiền ròng");
        assertMoneyAfterLabel(result.drawerText(), "Tổng vào");
        assertMoneyAfterLabel(result.drawerText(), "Tổng ra");
        assertMoneyAfterLabel(result.drawerText(), "Dòng tiền ròng");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_100)
    public void relatedTransactionCountMatchesLinks() {
        var result = advancedPage().inspectSystemDetailContaining("Giao dịch liên quan (");
        var matcher = Pattern.compile("Giao dịch liên quan \\((\\d+)\\)")
                .matcher(result.drawerText());
        Assert.assertTrue(matcher.find(), result.drawerText());
        int expected = Integer.parseInt(matcher.group(1));
        long actual = result.transactionLinks().stream()
                .map(TransactionHistoryPage.DetailElementLink::text)
                .filter(text -> text.matches("\\d+\\s+[0-9a-fA-F-]{36}"))
                .count();
        Assert.assertTrue(expected > 0, "Số giao dịch liên quan phải lớn hơn 0");
        Assert.assertEquals(actual, expected, "Số link không khớp tiêu đề giao dịch liên quan");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_101)
    public void currentTransactionIsMarkedInCashFlow() {
        var result = advancedPage().loadRelatedHistoryUntilCurrent();
        Assert.assertTrue(result.loaded(), result.drawerText());
        Assert.assertTrue(!result.currentListed() || result.currentMarked(),
                "Giao dịch hiện tại có trong dòng tiền nhưng thiếu marker Đang xem");
        Assert.assertTrue(result.drawerText().contains("Dòng tiền"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_102)
    public void cashFlowEntriesHaveRequiredFields() {
        var result = advancedPage().inspectSystemDetailContaining(
                "Tổng vào", "Tổng ra", "Dòng tiền ròng");
        List<String> entries = result.transactionLinks().stream()
                .map(TransactionHistoryPage.DetailElementLink::text)
                .filter(text -> DATE_IN_TEXT.matcher(text).find())
                .toList();
        Assert.assertFalse(entries.isEmpty(), "Không có dòng lịch sử để kiểm tra");
        entries.forEach(text -> {
            String normalized = normalizeWhitespace(text);
            Assert.assertTrue(Pattern.compile(
                            "^.+\\s[+\\-−]?[0-9][0-9.,]*₫\\s+").matcher(normalized).find(),
                    "Dòng tiền thiếu loại giao dịch: " + text);
            Assert.assertTrue(normalized.contains("₫"), "Dòng tiền thiếu số tiền: " + text);
            Assert.assertTrue(normalized.contains("Thành công") || normalized.contains("Đang chờ")
                            || normalized.contains("Thất bại"),
                    "Dòng tiền thiếu trạng thái: " + text);
            Assert.assertTrue(DATE_IN_TEXT.matcher(text).find(),
                    "Dòng tiền thiếu thời gian: " + text);
        });
    }

    private static final Pattern DATE_IN_TEXT = Pattern.compile(
            "\\b\\d{2}-\\d{2}-\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\b");

    private void assertParticipant(TransactionHistoryPage.SystemDetailElementSnapshot result,
                                   String role) {
        Assert.assertTrue(result.drawerText().contains(role), result.drawerText());
        Assert.assertFalse(result.userText().isBlank(), "Thiếu tên chủ thể trong drawer");
        Assert.assertTrue(result.userHref().contains("/vuatho/user?id=")
                || result.userHref().contains("/vuatho/worker?id="), result.userHref());
        Assert.assertTrue(result.source().signature().contains(result.userText()),
                "Chủ thể drawer không khớp dòng nguồn: " + result.userText());
        Assert.assertTrue(Pattern.compile("\\(\\+84\\)\\s*[0-9 ]{8,}")
                .matcher(result.drawerText()).find(), "Thiếu hoặc sai SĐT chủ thể");
        Assert.assertTrue(result.closed());
        assertSystemRoute(result.closedUrl(), false);
    }

    private void assertMoneyAfterLabel(String text, String label) {
        Pattern money = Pattern.compile(Pattern.quote(label)
                + "\\s+[+\\-−]?[0-9][0-9.,]*₫");
        Assert.assertTrue(money.matcher(text).find(),
                "Thiếu hoặc sai định dạng tiền của " + label + ": " + text);
    }

    private String textBetween(String text, String label, List<String> nextLabels) {
        int labelStart = text.indexOf(label);
        if (labelStart < 0) {
            return "";
        }
        final int contentStart = labelStart + label.length();
        int end = nextLabels.stream()
                .mapToInt(nextLabel -> text.indexOf(nextLabel, contentStart))
                .filter(index -> index >= contentStart).min().orElse(text.length());
        return text.substring(contentStart, end).trim();
    }

    private String normalizeWhitespace(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private TransactionHistoryPage.DetailAuditSnapshot audit() {
        if (audit == null) {
            audit = advancedPage().auditFirstDetailInOneFlow();
        }
        return audit;
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }
}
