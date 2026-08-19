package com.vuatho.tests.transaction.deposit;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionDepositTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.regex.Pattern;

/** Kiểm tra drawer chi tiết của từng loại Tiền nạp. */
public class TransactionDepositDetailTest extends TransactionDepositTestSupport {
    private TransactionHistoryPage.DetailAuditSnapshot representativeAudit;

    public static void main(String[] args) {
        TestNgRunner.run(TransactionDepositDetailTest.class,
                "Lịch sử giao dịch", "Tiền nạp - Chi tiết");
    }

    @Override
    protected boolean openInitialSubtypeBeforeEachTest() {
        return false;
    }


    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_008)
    public void opensAndClosesDetail() {
        var subtype = initialSubtype();
        openDepositSubtype(subtype);
        verifyDetail(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_017)
    public void detailMatchesSourceAndShowsDepositInformation() {
        openDepositSubtype(initialSubtype());
        representativeAudit = advancedPage().auditFirstDetailInOneFlow();
        var result = representativeAudit;
        String text = result.drawerText();
        String expectedTime = result.source().createdAt()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        Assert.assertTrue(result.openedUrl().contains("tab=deposit"), result.openedUrl());
        Assert.assertEquals(Pattern.compile("(^|[?&])id=[^&]+")
                .matcher(result.openedUrl()).results().count(), 1L, result.openedUrl());
        Assert.assertTrue(text.contains("Chi tiết giao dịch"), text);
        Assert.assertTrue(text.contains("Thông tin giao dịch"), text);
        Assert.assertTrue(text.contains("Trạng thái"), text);
        Assert.assertTrue(text.contains("Số tiền"), text);
        Assert.assertTrue(text.contains("Loại"), text);
        Assert.assertTrue(text.contains("Thời gian"), text);
        Assert.assertTrue(text.contains("Phí"), text);
        Assert.assertTrue(text.contains("Người nạp"), text);
        Assert.assertTrue(text.contains("Số dư Ví chi phí ban đầu"), text);
        Assert.assertTrue(text.contains("Số dư Ví chi phí sau khi nạp"), text);
        Assert.assertTrue(text.contains(result.source().type()), text);
        Assert.assertTrue(text.contains(result.source().status()), text);
        Assert.assertTrue(digits(text).contains(digits(result.source().amount())), text);
        Assert.assertTrue(text.contains(result.source().gateway()), text);
        Assert.assertTrue(text.contains(expectedTime), text);
        Assert.assertTrue(result.closed(), "Drawer không đóng sau khi hoàn tất audit tổng hợp");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_018,
            dependsOnMethods = "detailMatchesSourceAndShowsDepositInformation")
    public void relatedHistoryLoadsAndMatchesCurrentTransaction() {
        var result = audit();
        String text = result.drawerText();

        Assert.assertTrue(result.currentMarked(), text);
        Assert.assertFalse(text.contains("Đang tải"), text);
        Assert.assertTrue(text.contains("Tổng vào"), text);
        Assert.assertTrue(text.contains("Tổng ra"), text);
        Assert.assertTrue(text.contains("Dòng tiền ròng"), text);
        Assert.assertTrue(text.toLowerCase().contains("đang xem"), text);
        Assert.assertTrue(text.contains(result.source().type()), text);
        Assert.assertTrue(text.contains(result.source().status()), text);
        Assert.assertTrue(digits(text).contains(digits(result.source().amount())), text);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_019,
            dependsOnMethods = "detailMatchesSourceAndShowsDepositInformation")
    public void detailLinksAreValid() {
        var result = audit();

        Assert.assertTrue(result.userHref().contains("/vuatho/user?id="), result.userHref());
        Assert.assertFalse(result.transactionHrefs().isEmpty(), result.drawerText());
        Assert.assertEquals(new HashSet<>(result.transactionHrefs()).size(),
                result.transactionHrefs().size(), "Link giao dịch liên quan bị trùng");
        result.transactionHrefs().forEach(href -> {
            Assert.assertTrue(href.contains("/vuatho/transaction?"), href);
            Assert.assertTrue(href.contains("tab=all"), href);
            Assert.assertTrue(href.contains("id="), href);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_020)
    public void deepLinkReopensSameDetail() {
        openDepositSubtype(initialSubtype());
        var result = advancedPage().reopenByDeepLink();

        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("tab=deposit"), result.actualUrl());
        Assert.assertTrue(result.actualUrl().contains("id="), result.actualUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_021)
    public void browserBackAndForwardRestoresDetail() {
        openDepositSubtype(initialSubtype());
        var result = advancedPage().backAndForwardDetail();

        Assert.assertTrue(result.closedAfterBack());
        Assert.assertFalse(result.backUrl().contains("id="), result.backUrl());
        Assert.assertTrue(result.backUrl().contains("tab=deposit"), result.backUrl());
        Assert.assertEquals(result.forwardUrl(), result.openedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_022,
            dependsOnMethods = "detailMatchesSourceAndShowsDepositInformation")
    public void loadMoreExpandsRelatedHistory() {
        var result = audit();

        Assert.assertTrue(result.openedUrl().contains("tab=deposit"), result.openedUrl());
        Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
        Assert.assertTrue(result.afterRelatedCount() > result.beforeRelatedCount(),
                "Xem thêm không tăng số giao dịch: " + result.beforeRelatedCount()
                        + " -> " + result.afterRelatedCount());
        Assert.assertTrue(result.drawerText().contains("Dòng tiền"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_023)
    public void relatedLinkOpensExpectedTransaction() {
        openDepositSubtype(initialSubtype());
        var result = advancedPage().openFirstRelatedTransaction();

        Assert.assertNotEquals(result.actualUrl(), result.sourceUrl());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("tab=all"), result.actualUrl());
        Assert.assertTrue(result.actualUrl().contains("id="), result.actualUrl());
        Assert.assertFalse(result.sourceText().isBlank());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_024)
    public void thirdPartyDetailShowsBothPartiesAndBalanceChange() {
        var subtype = subtype(10);
        openDepositSubtype(subtype);
        var result = advancedPage().openFirstDetail();
        String text = result.drawerText();

        Assert.assertTrue(result.url().contains("tab=deposit"), result.url());
        Assert.assertTrue(result.url().contains("type=10"), result.url());
        Assert.assertTrue(text.contains("Người gửi"), text);
        Assert.assertTrue(text.contains("Người nạp"), text);
        Assert.assertTrue(text.contains("Biến động số dư"), text);
        Assert.assertTrue(text.contains("Số dư Ví chi phí ban đầu"), text);
        Assert.assertTrue(text.contains("Số dư Ví chi phí sau khi nạp"), text);
        Assert.assertTrue(text.contains("Thanh toán bởi bên thứ 3"), text);
        Assert.assertTrue(text.contains(result.source().status()), text);
        Assert.assertTrue(digits(text).contains(digits(result.source().amount())), text);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_025)
    public void enterpriseDepositDetailShowsWalletBalances() {
        var subtype = subtype(19);
        openDepositSubtype(subtype);
        var result = advancedPage().openFirstDetail();
        String text = result.drawerText();

        Assert.assertTrue(result.url().contains("tab=deposit"), result.url());
        Assert.assertTrue(result.url().contains("type=19"), result.url());
        Assert.assertTrue(text.contains("Người nạp"), text);
        Assert.assertTrue(text.contains("Số dư Ví ban đầu"), text);
        Assert.assertTrue(text.contains("Số dư Ví sau khi nạp"), text);
        Assert.assertTrue(text.contains("Tiền nạp từ doanh nghiệp"), text);
        Assert.assertTrue(text.contains(result.source().status()), text);
        Assert.assertTrue(digits(text).contains(digits(result.source().amount())), text);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_026)
    public void bankMarginDepositDetailShowsRecipientAndCashFlow() {
        var subtype = subtype(34);
        openDepositSubtype(subtype);
        var result = advancedPage().loadRelatedHistoryUntilCurrent();
        String text = result.drawerText();

        Assert.assertTrue(transactionPage.currentUrl().contains("tab=deposit"),
                transactionPage.currentUrl());
        Assert.assertTrue(transactionPage.currentUrl().contains("type=34"),
                transactionPage.currentUrl());
        Assert.assertTrue(text.contains("Người nhận"), text);
        Assert.assertTrue(text.contains("Số dư Ví chi phí ban đầu"), text);
        Assert.assertTrue(text.contains("Số dư Ví chi phí sau khi nạp"), text);
        Assert.assertTrue(text.contains("Nạp tiền vào số dư nền tảng qua chuyển khoản ngân hàng"), text);
        Assert.assertTrue(text.contains("Dòng tiền ròng"), text);
        Assert.assertTrue(text.toLowerCase().contains("đang xem"), text);
        Assert.assertTrue(result.currentMarked(), text);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_027)
    public void marginDepositDetailShowsSenderAndBalanceChange() {
        var subtype = subtype(20);
        openDepositSubtype(subtype);
        var result = advancedPage().waitForCashFlowTotals();
        String text = result.drawerText();

        Assert.assertTrue(transactionPage.currentUrl().contains("tab=deposit"),
                transactionPage.currentUrl());
        Assert.assertTrue(transactionPage.currentUrl().contains("type=20"),
                transactionPage.currentUrl());
        Assert.assertTrue(text.contains("Người gửi"), text);
        Assert.assertTrue(text.contains("Biến động số dư"), text);
        Assert.assertTrue(text.contains("Tiền nạp vào ví ký quỹ"), text);
        Assert.assertTrue(text.contains("Ví Chi Phí"), text);
        Assert.assertTrue(text.contains("Dòng tiền ròng"), text);
        Assert.assertTrue(text.contains(result.source().status()), text);
        Assert.assertTrue(digits(text).contains(digits(result.source().amount())), text);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_028)
    public void RegularSuccessOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(0), "Thành công");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_193)
    public void RegularPendingOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(0), "Đang chờ");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_194)
    public void RegularCancelledOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(0), "Đã hủy");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_195)
    public void ThirdPartySuccessOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(10), "Thành công");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_196)
    public void ThirdPartyPendingOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(10), "Đang chờ");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_197)
    public void ThirdPartyCancelledOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(10), "Đã hủy");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_198)
    public void EnterpriseSuccessOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(19), "Thành công");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_199)
    public void EnterprisePendingOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(19), "Đang chờ");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_200)
    public void EnterpriseCancelledOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(19), "Đã hủy");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_201)
    public void DepositWalletSuccessOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(20), "Thành công");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_202)
    public void DepositWalletPendingOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(20), "Đang chờ");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_203)
    public void DepositWalletCancelledOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(20), "Đã hủy");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_204)
    public void BankTransferSuccessOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(34), "Thành công");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_205)
    public void BankTransferPendingOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(34), "Đang chờ");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_206)
    public void BankTransferCancelledOpensAndClosesCorrectDetail() {
        verifyDetailStatus(subtype(34), "Đã hủy");
    }

    private void verifyDetailStatus(TransactionCategoryPage.Subtype subtype, String status) {
        openDepositSubtype(subtype);
        var result = advancedPage().openAndCloseFirstDetailForStatus(status);
        String combination = subtype.label() + " / " + status;

        Assert.assertFalse(result.empty(), "Không có dữ liệu cho tổ hợp: " + combination);
        Assert.assertNotNull(result.source(), "Không đọc được dòng cho tổ hợp: " + combination);
        Assert.assertTrue(result.selectedStatus().contains(status), result.selectedStatus());
        Assert.assertEquals(result.source().status(), status, combination);
        Assert.assertTrue(result.openedUrl().contains("tab=deposit"), result.openedUrl());
        if (subtype.type() != 0) {
            Assert.assertTrue(result.openedUrl().contains("type=" + subtype.type()), result.openedUrl());
        }
        Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"), result.drawerText());
        String expectedDetailStatus = status.equals("Đã hủy") ? "Thất bại" : status;
        Assert.assertTrue(result.drawerText().contains(expectedDetailStatus), result.drawerText());
        Assert.assertTrue(result.drawerText().contains(result.source().type()), result.drawerText());
        Assert.assertTrue(digits(result.drawerText()).contains(digits(result.source().amount())),
                result.drawerText());
        Assert.assertTrue(result.closed(), "Không đóng được drawer: " + combination);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_029)
    public void headerIconClosesDetailAndKeepsSubtype() {
        openDepositSubtype(initialSubtype());
        var result = advancedPage().closeDetailWithHeaderIcon();

        Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
        Assert.assertTrue(result.closedUrl().contains("tab=deposit"), result.closedUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_030)
    public void escapeClosesDetailAndKeepsSubtype() {
        openDepositSubtype(initialSubtype());
        var result = advancedPage().closeDetailWithEscape();

        Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
        Assert.assertTrue(result.closedUrl().contains("tab=deposit"), result.closedUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_031)
    public void partyLinkOpensExpectedProfile() {
        openDepositSubtype(initialSubtype());
        var result = advancedPage().openFirstDetailPartyProfile();

        Assert.assertFalse(result.sourceText().isBlank());
        Assert.assertNotEquals(result.actualUrl(), result.sourceUrl());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("/vuatho/user?id=")
                || result.actualUrl().contains("/vuatho/worker?id="), result.actualUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_032)
    public void refreshKeepsOpenDetail() {
        openDepositSubtype(initialSubtype());
        var result = advancedPage().refreshOpenDetail();

        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("tab=deposit"), result.actualUrl());
        Assert.assertTrue(result.actualUrl().contains("id="), result.actualUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"), result.drawerText());
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(candidate -> candidate.type() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không có subtype Tiền nạp type=" + type));
    }

    private TransactionHistoryPage.DetailAuditSnapshot audit() {
        if (representativeAudit == null) {
            throw new IllegalStateException("Audit detail đại diện chưa được khởi tạo");
        }
        return representativeAudit;
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }
}
