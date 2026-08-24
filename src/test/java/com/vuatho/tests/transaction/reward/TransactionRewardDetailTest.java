package com.vuatho.tests.transaction.reward;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionRewardTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

/** Kiểm tra drawer, deep-link và dòng tiền của cả hai subtype Thưởng & KM. */
public class TransactionRewardDetailTest extends TransactionRewardTestSupport {
    private final Map<Integer, TransactionHistoryPage.DetailAuditSnapshot> audits = new HashMap<>();

    public static void main(String[] args) {
        TestNgRunner.run(TransactionRewardDetailTest.class,
                "Lịch sử giao dịch", "Thưởng & KM - Chi tiết nâng cao");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_063)
    public void detailMatchesSourceOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().openFirstDetail();
            String text = result.drawerText();
            String expectedTime = result.source().createdAt().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            Assert.assertTrue(text.contains("Chi tiết giao dịch"), text);
            Assert.assertTrue(text.contains("Thông tin giao dịch"), text);
            Assert.assertTrue(text.contains(result.source().type()), text);
            Assert.assertTrue(text.contains(result.source().status()), text);
            Assert.assertTrue(digits(text).contains(digits(result.source().amount())), text);
            Assert.assertTrue(text.contains(expectedTime), text);
            assertSubtype(result.url(), subtype, true);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_064)
    public void detailUrlContainsSingleIdOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            String url = advancedPage().openFirstDetail().url();
            Assert.assertEquals(Pattern.compile("(^|[?&])id=[^&]+")
                    .matcher(url).results().count(), 1L, url);
            assertSubtype(url, subtype, true);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_065)
    public void closeIconRemovesIdAndKeepsBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().closeDetailWithIcon();
            Assert.assertTrue(result.closed());
            Assert.assertTrue(result.openedUrl().contains("id="));
            assertSubtype(result.closedUrl(), subtype, false);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_066)
    public void escapeRemovesIdAndKeepsBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().closeDetailWithEscape();
            Assert.assertTrue(result.closed());
            Assert.assertTrue(result.openedUrl().contains("id="));
            assertSubtype(result.closedUrl(), subtype, false);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_067)
    public void refreshKeepsDetailOpenOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().refreshOpenDetail();
            Assert.assertEquals(result.actualUrl(), result.expectedUrl());
            Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
            assertSubtype(result.actualUrl(), subtype, true);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_068)
    public void browserBackAndForwardRestoresBothSubtypeDetails() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().backAndForwardDetail();
            Assert.assertTrue(result.closedAfterBack());
            Assert.assertFalse(result.backUrl().contains("id="), result.backUrl());
            Assert.assertEquals(result.forwardUrl(), result.openedUrl());
            Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
            assertSubtype(result.forwardUrl(), subtype, true);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_069)
    public void deepLinkReopensBothSubtypeDetails() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().reopenByDeepLink();
            Assert.assertEquals(result.actualUrl(), result.expectedUrl());
            Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
            assertSubtype(result.actualUrl(), subtype, true);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_070)
    public void cashFlowAndLinksAreValidOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            var result = audit(subtype);
            Assert.assertFalse(result.drawerText().contains("Đang tải"), result.drawerText());
            Assert.assertTrue(result.drawerText().contains("Dòng tiền của"), result.drawerText());
            Assert.assertTrue(result.drawerText().contains(result.source().type()),
                    result.drawerText());
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
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_071)
    public void partyProfileOpensAndReturnsOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().openPartyProfileAndReturn();
            Assert.assertTrue(result.expectedUrl().contains("/vuatho/user?id=")
                    || result.expectedUrl().contains("/vuatho/worker?id="), result.expectedUrl());
            Assert.assertEquals(result.actualUrl(), result.expectedUrl());
            Assert.assertFalse(result.targetText().isBlank());
            Assert.assertTrue(result.sourceRestored());
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_072)
    public void relatedTransactionOpensAndReturnsOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().openRelatedTransactionAndReturn();
            Assert.assertTrue(result.expectedUrl().contains("/vuatho/transaction?"),
                    result.expectedUrl());
            Assert.assertEquals(result.actualUrl(), result.expectedUrl());
            Assert.assertTrue(result.sourceRestored());
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_073)
    public void everyVisibleStatusOpensMatchingDetailOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var results = advancedPage().openAndCloseEveryVisibleStatus();
            Assert.assertFalse(results.isEmpty(), "Không có trạng thái cho " + subtype.label());
            results.forEach(result -> {
                Assert.assertFalse(result.empty(), result.expectedStatus());
                Assert.assertEquals(result.source().status(), result.expectedStatus());
                Assert.assertTrue(result.drawerText().contains(result.expectedStatus()));
                Assert.assertTrue(result.closed());
            });
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_074)
    public void openingAnotherRowChangesDetailOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().openAnotherTransactionDetail();
            Assert.assertNotEquals(result.secondUrl(), result.firstUrl());
            Assert.assertNotEquals(result.secondSource().signature(), result.firstSource().signature());
            Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().type()));
            Assert.assertTrue(result.secondDrawerText().contains(result.secondSource().status()));
            assertSubtype(result.secondUrl(), subtype, true);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_125)
    public void campaignRelatedHistoryExpandsWithMoreRows() {
        var subtype = rewardSubtype(18);
        openRewardSubtype(subtype);
        var result = advancedPage().expandRelatedHistoryOnce();
        Assert.assertTrue(result.afterCount() > result.beforeCount());
        Assert.assertTrue(result.drawerText().contains("Dòng tiền"), result.drawerText());
        assertSubtype(result.openedUrl(), subtype, true);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_REWARD_126)
    public void closeButtonHasAccessibleNameOnBothSubtypes() {
        rewardSubtypes().forEach(subtype -> {
            openRewardSubtype(subtype);
            var result = advancedPage().inspectDetailCloseAccessibility();
            Assert.assertTrue(!result.ariaLabel().isBlank()
                            || !result.title().isBlank() || !result.text().isBlank(),
                    "Nút đóng drawer không có accessible name type=" + subtype.type());
            Assert.assertTrue(result.closed());
        });
    }

    private TransactionHistoryPage.DetailAuditSnapshot audit(TransactionCategoryPage.Subtype subtype) {
        TransactionHistoryPage.DetailAuditSnapshot cached = audits.get(subtype.type());
        if (cached != null) {
            return cached;
        }
        openRewardSubtype(subtype);
        var result = advancedPage().auditFirstDetailInOneFlow();
        audits.put(subtype.type(), result);
        return result;
    }

    private void assertSubtype(String url, TransactionCategoryPage.Subtype subtype, boolean hasId) {
        Assert.assertTrue(url.contains("tab=reward&type=" + subtype.type()), url);
        Assert.assertEquals(url.contains("id="), hasId, url);
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }
}
