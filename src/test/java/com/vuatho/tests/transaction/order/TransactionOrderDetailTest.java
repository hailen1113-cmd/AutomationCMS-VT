package com.vuatho.tests.transaction.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionOrderTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Kiểm tra drawer chi tiết của sáu loại giao dịch Đơn dịch vụ. */
public class TransactionOrderDetailTest extends TransactionOrderTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionOrderDetailTest.class,
                "Lịch sử giao dịch", "Đơn dịch vụ - Chi tiết");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_008)
    public void opensAndClosesAllSubtypeDetails() {
        verifyOpensAndClosesAllSubtypeDetailsForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_155)
    public void opensAndClosesAllSubtypeDetailsType22() {
        verifyOpensAndClosesAllSubtypeDetailsForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_156)
    public void opensAndClosesAllSubtypeDetailsType24() {
        verifyOpensAndClosesAllSubtypeDetailsForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_157)
    public void opensAndClosesAllSubtypeDetailsType36() {
        verifyOpensAndClosesAllSubtypeDetailsForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_158)
    public void opensAndClosesAllSubtypeDetailsType37() {
        verifyOpensAndClosesAllSubtypeDetailsForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_159)
    public void opensAndClosesAllSubtypeDetailsType15() {
        verifyOpensAndClosesAllSubtypeDetailsForSubtype(subtype(15));
    }

    private void verifyOpensAndClosesAllSubtypeDetailsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        verifyDetail(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_055)
    public void detailMatchesSourceRowAcrossAllSubtypes() {
        verifyDetailMatchesSourceRowAcrossAllSubtypesForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_160)
    public void detailMatchesSourceRowAcrossAllSubtypesType22() {
        verifyDetailMatchesSourceRowAcrossAllSubtypesForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_161)
    public void detailMatchesSourceRowAcrossAllSubtypesType24() {
        verifyDetailMatchesSourceRowAcrossAllSubtypesForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_162)
    public void detailMatchesSourceRowAcrossAllSubtypesType36() {
        verifyDetailMatchesSourceRowAcrossAllSubtypesForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_163)
    public void detailMatchesSourceRowAcrossAllSubtypesType37() {
        verifyDetailMatchesSourceRowAcrossAllSubtypesForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_164)
    public void detailMatchesSourceRowAcrossAllSubtypesType15() {
        verifyDetailMatchesSourceRowAcrossAllSubtypesForSubtype(subtype(15));
    }

    private void verifyDetailMatchesSourceRowAcrossAllSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = advancedPage().openFirstDetail();
        Assert.assertTrue(result.url().contains("tab=order&type=" + subtype.type()), result.url());
        Assert.assertTrue(result.url().contains("id="), result.url());
        Assert.assertTrue(result.drawerText().contains(result.source().status()), result.drawerText());
        Assert.assertTrue(matchesSubtypeContent(result.drawerText(), result.source().type(), subtype),
                "Loại giao dịch không khớp type=" + subtype.type() + ": " + result.drawerText());
        Assert.assertTrue(drawerContainsAmount(result.drawerText(), result.source().amountValue()),
                "Số tiền dòng nguồn " + result.source().amount() + " không có trong drawer type="
                        + subtype.type() + ": " + result.drawerText());
        String dashTime = result.source().createdAt().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String slashTime = result.source().createdAt().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        Assert.assertTrue(result.drawerText().contains(dashTime)
                        || result.drawerText().contains(slashTime),
                "Thời gian dòng nguồn không có trong drawer type=" + subtype.type());
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_056)
    public void deepLinkReopensSameDetailAcrossAllSubtypes() {
        verifyDeepLinkReopensSameDetailAcrossAllSubtypesForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_165)
    public void deepLinkReopensSameDetailAcrossAllSubtypesType22() {
        verifyDeepLinkReopensSameDetailAcrossAllSubtypesForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_166)
    public void deepLinkReopensSameDetailAcrossAllSubtypesType24() {
        verifyDeepLinkReopensSameDetailAcrossAllSubtypesForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_167)
    public void deepLinkReopensSameDetailAcrossAllSubtypesType36() {
        verifyDeepLinkReopensSameDetailAcrossAllSubtypesForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_168)
    public void deepLinkReopensSameDetailAcrossAllSubtypesType37() {
        verifyDeepLinkReopensSameDetailAcrossAllSubtypesForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_169)
    public void deepLinkReopensSameDetailAcrossAllSubtypesType15() {
        verifyDeepLinkReopensSameDetailAcrossAllSubtypesForSubtype(subtype(15));
    }

    private void verifyDeepLinkReopensSameDetailAcrossAllSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = advancedPage().reopenByDeepLink();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("type=" + subtype.type()));
        Assert.assertTrue(result.drawerText().contains("Thông tin giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_057)
    public void headerIconClosesDetailAndKeepsSubtype() {
        verifyHeaderIconClosesDetailAndKeepsSubtypeForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_170)
    public void headerIconClosesDetailAndKeepsSubtypeType22() {
        verifyHeaderIconClosesDetailAndKeepsSubtypeForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_171)
    public void headerIconClosesDetailAndKeepsSubtypeType24() {
        verifyHeaderIconClosesDetailAndKeepsSubtypeForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_172)
    public void headerIconClosesDetailAndKeepsSubtypeType36() {
        verifyHeaderIconClosesDetailAndKeepsSubtypeForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_173)
    public void headerIconClosesDetailAndKeepsSubtypeType37() {
        verifyHeaderIconClosesDetailAndKeepsSubtypeForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_174)
    public void headerIconClosesDetailAndKeepsSubtypeType15() {
        verifyHeaderIconClosesDetailAndKeepsSubtypeForSubtype(subtype(15));
    }

    private void verifyHeaderIconClosesDetailAndKeepsSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = advancedPage().closeDetailWithHeaderIcon();
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="));
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_058)
    public void enterprisePaymentCashFlowNetEqualsIncomingPlusOutgoing() {
        openType22();
        String text = advancedPage().waitForCashFlowTotals().drawerText();
        BigDecimal incoming = moneyAfterLabel(text, "Tổng vào");
        BigDecimal outgoing = moneyAfterLabel(text, "Tổng ra");
        BigDecimal net = moneyAfterLabel(text, "Dòng tiền ròng");
        Assert.assertTrue(incoming.signum() >= 0, incoming.toPlainString());
        Assert.assertTrue(outgoing.signum() <= 0, outgoing.toPlainString());
        Assert.assertEquals(net, incoming.add(outgoing),
                incoming + " + " + outgoing + " != " + net);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_059)
    public void enterprisePaymentPartyLinkOpensProfileAndReturns() {
        openType22();
        var result = advancedPage().openPartyProfileAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/user?id=")
                || result.expectedUrl().contains("/vuatho/worker?id="), result.expectedUrl());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertFalse(result.targetText().isBlank());
        Assert.assertTrue(result.sourceRestored());
        Assert.assertEquals(result.returnedUrl(), result.sourceUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_060)
    public void enterprisePaymentOrderLinkOpensOrderAndReturns() {
        openType22();
        var result = advancedPage().openOrderAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/order?id="), result.expectedUrl());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertFalse(result.targetText().isBlank());
        Assert.assertTrue(result.sourceRestored());
        Assert.assertEquals(result.returnedUrl(), result.sourceUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_061)
    public void enterprisePaymentTimelineLinkOpensDifferentTransactionAndReturns() {
        openType22();
        var result = advancedPage().openRelatedTransactionAndReturn();
        Assert.assertTrue(result.expectedUrl().contains("/vuatho/transaction?"), result.expectedUrl());
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.targetText().contains("Chi tiết giao dịch"), result.targetText());
        Assert.assertTrue(result.sourceRestored());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_062)
    public void escapeClosesDetailAndKeepsSubtype() {
        verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_175)
    public void escapeClosesDetailAndKeepsSubtypeType22() {
        verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_176)
    public void escapeClosesDetailAndKeepsSubtypeType24() {
        verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_177)
    public void escapeClosesDetailAndKeepsSubtypeType36() {
        verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_178)
    public void escapeClosesDetailAndKeepsSubtypeType37() {
        verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_179)
    public void escapeClosesDetailAndKeepsSubtypeType15() {
        verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(subtype(15));
    }

    private void verifyEscapeClosesDetailAndKeepsSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = advancedPage().closeDetailWithEscape();
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_063)
    public void refreshKeepsSameDetailAndSubtype() {
        verifyRefreshKeepsSameDetailAndSubtypeForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_180)
    public void refreshKeepsSameDetailAndSubtypeType22() {
        verifyRefreshKeepsSameDetailAndSubtypeForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_181)
    public void refreshKeepsSameDetailAndSubtypeType24() {
        verifyRefreshKeepsSameDetailAndSubtypeForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_182)
    public void refreshKeepsSameDetailAndSubtypeType36() {
        verifyRefreshKeepsSameDetailAndSubtypeForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_183)
    public void refreshKeepsSameDetailAndSubtypeType37() {
        verifyRefreshKeepsSameDetailAndSubtypeForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_184)
    public void refreshKeepsSameDetailAndSubtypeType15() {
        verifyRefreshKeepsSameDetailAndSubtypeForSubtype(subtype(15));
    }

    private void verifyRefreshKeepsSameDetailAndSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = advancedPage().refreshOpenDetail();
        Assert.assertEquals(result.actualUrl(), result.expectedUrl());
        Assert.assertTrue(result.actualUrl().contains("tab=order&type=" + subtype.type()));
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_064)
    public void serviceOrderBackAndForwardRestoresSameDetail() {
        assertBackAndForwardRestoresDetail(2);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_093)
    public void enterprisePaymentBackAndForwardRestoresSameDetail() {
        assertBackAndForwardRestoresDetail(22);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_094)
    public void warrantyFeeBackAndForwardRestoresSameDetail() {
        assertBackAndForwardRestoresDetail(24);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_095)
    public void warrantyIncomeBackAndForwardRestoresSameDetail() {
        assertBackAndForwardRestoresDetail(36);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_096)
    public void warrantyPaymentBackAndForwardRestoresSameDetail() {
        assertBackAndForwardRestoresDetail(37);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_097)
    public void penaltyBackAndForwardRestoresSameDetail() {
        assertBackAndForwardRestoresDetail(15);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_077)
    public void serviceOrderSuccessDetailShowsRecipientBalancesOrderAndCashFlow() {
        var result = inspectType(2, "Thành công");
        assertCommonDetail(result, 2, "Thành công");
        assertContainsAll(result.drawerText(), "Người nhận", "Số dư Ví chi phí ban đầu",
                "Số dư Ví chi phí sau khi nạp", "Thông tin giao dịch", "Số tiền",
                "Loại", "Đơn dịch vụ", "Thời gian", "Mã đơn dịch vụ", "Dòng tiền của");
        assertCashFlow(result.drawerText());
        assertLinks(result.userLinks(), "/vuatho/user?id=");
        assertLinks(result.orderLinks(), "/vuatho/order?id=");
        assertLinks(result.transactionLinks(), "/vuatho/transaction?");
        Assert.assertTrue(result.rejectPresent());
        Assert.assertFalse(result.rejectVisible());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_078)
    public void enterprisePaymentSuccessDetailShowsBothPartiesBalancesOrderAndCashFlow() {
        var result = inspectType(22, "Thành công");
        assertCommonDetail(result, 22, "Thành công");
        assertContainsAll(result.drawerText(), "Người nạp", "Số dư Ví ban đầu",
                "Số dư Ví sau khi nạp", "Người nhận", "Số dư Ví chi phí ban đầu",
                "Số dư Ví chi phí sau khi nạp", "Doanh nghiệp thanh toán đơn dịch vụ",
                "Mã đơn dịch vụ", "Dòng tiền của");
        assertCashFlow(result.drawerText());
        assertLinks(result.userLinks(), "/vuatho/user?id=");
        assertLinks(result.orderLinks(), "/vuatho/order?id=");
        assertLinks(result.transactionLinks(), "/vuatho/transaction?");
        Assert.assertTrue(result.rejectPresent());
        Assert.assertFalse(result.rejectVisible());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_079)
    public void warrantyFeeSuccessDetailShowsWarrantyPeriodOrderAndCashFlow() {
        var result = inspectType(24, "Thành công");
        assertCommonDetail(result, 24, "Thành công");
        assertContainsAll(result.drawerText(), "Người gửi", "Biến động số dư",
                "Phí bảo hành đơn dịch vụ", "Thời hạn bảo hành (theo đơn)",
                "Đang bảo hành", "Mã đơn dịch vụ", "Dòng tiền của");
        assertCashFlow(result.drawerText());
        assertLinks(result.userLinks(), "/vuatho/user?id=");
        assertLinks(result.orderLinks(), "/vuatho/order?id=");
        assertLinks(result.transactionLinks(), "/vuatho/transaction?");
        Assert.assertTrue(result.currentMarked());
        Assert.assertTrue(result.rejectPresent());
        Assert.assertFalse(result.rejectVisible());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_080)
    public void warrantyIncomeSuccessDetailShowsBreakdownCreatorHistoryAndCashFlow() {
        var result = inspectType(36, "Thành công");
        assertCommonDetail(result, 36, "Thành công");
        assertContainsAll(result.drawerText(), "Người gửi", "Biến động số dư",
                "Số tiền thu từ thợ", "Trừ ký quỹ", "Trừ chi phí", "Thợ:",
                "Đơn BH:", "Tạo bởi", "Dòng tiền của", "Lịch sử cập nhật",
                "Người cập nhật", "Phản hồi thu bảo hành");
        assertCashFlow(result.drawerText());
        assertLinks(result.userLinks(), "/vuatho/user?id=");
        assertLinks(result.orderLinks(), "/vuatho/order?id=");
        assertLinks(result.transactionLinks(), "/vuatho/transaction?");
        Assert.assertTrue(result.currentMarked());
        Assert.assertTrue(result.rejectPresent());
        Assert.assertFalse(result.rejectVisible());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_081)
    public void pendingWarrantyPaymentShowsBankTransferQrUploadAndActionStates() {
        var result = inspectType(37, "Đang chờ");
        assertCommonDetail(result, 37, "Đang chờ");
        assertContainsAll(result.drawerText(), "Số tiền cần chuyển", "Đền bù khách (bảo hành)",
                "Đơn liên quan", "Chuyển khoản tới", "Ngân hàng", "Số tài khoản",
                "Nội dung CK", "Hoàn BH cho khách", "Bill chuyển khoản",
                "bắt buộc trước khi xác nhận", "Tải ảnh lên");
        assertLinks(result.orderLinks(), "/vuatho/order?id=");
        assertLinks(result.withdrawalLinks(), "/vuatho/withdraw-qr-request?id=");
        Assert.assertTrue(result.visibleCopyButtons() >= 2,
                "Thiếu hai nút Copy số tài khoản và nội dung chuyển khoản");
        Assert.assertTrue(result.qrButtonVisible(), "Thiếu nút Hiện mã QR");
        Assert.assertTrue(result.imageAccepts().contains("image/*"), result.imageAccepts().toString());
        Assert.assertTrue(result.rejectPresent());
        Assert.assertTrue(result.rejectVisible());
        Assert.assertTrue(result.confirmPresent());
        Assert.assertTrue(result.confirmVisible());
        Assert.assertTrue(result.confirmDisabled(),
                "Chưa upload bill nhưng Xác nhận đã chuyển khoản vẫn được bật");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_082)
    public void penaltySuccessDetailShowsHandlingReasonNotesAndCashFlow() {
        var result = inspectType(15, "Thành công");
        assertCommonDetail(result, 15, "Thành công");
        assertContainsAll(result.drawerText(), "Người gửi", "Số tiền phạt", "Cách xử lý",
                "Ghi nhận đã thu (gỡ phạt)", "không trừ số dư thợ", "Thợ:",
                "Lý do / tiêu đề:", "Chi tiết:", "Ghi chú:", "Dòng tiền của");
        assertCashFlow(result.drawerText());
        assertLinks(result.userLinks(), "/vuatho/user?id=");
        assertLinks(result.transactionLinks(), "/vuatho/transaction?");
        Assert.assertTrue(result.currentMarked());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_083)
    public void warrantyIncomeHandlesAllThreeStatuses() {
        assertAllThreeStatusOutcomes(36);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_260)
    public void warrantyPaymentHandlesAllThreeStatuses() {
        assertAllThreeStatusOutcomes(37);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_084)
    public void serviceOrderLoadMoreIncreasesCashFlowHistory() {
        openOrderSubtype(subtype(2));
        var result = advancedPage().expandRelatedHistoryOnce();
        Assert.assertTrue(result.beforeCount() > 0, "Chưa có lịch sử trước khi bấm Xem thêm");
        Assert.assertTrue(result.afterCount() > result.beforeCount(),
                result.beforeCount() + " -> " + result.afterCount());
        Assert.assertTrue(result.drawerText().contains("Dòng tiền"), result.drawerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_085)
    public void serviceOrderHandlesPendingSuccessAndFailedDetails() {
        assertAllThreeStatusOutcomes(2);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_086)
    public void enterprisePaymentHandlesPendingSuccessAndFailedDetails() {
        assertAllThreeStatusOutcomes(22);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_087)
    public void warrantyFeeHandlesPendingSuccessAndFailedDetails() {
        assertAllThreeStatusOutcomes(24);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_088)
    public void penaltyHandlesPendingSuccessAndFailedDetails() {
        assertAllThreeStatusOutcomes(15);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_089)
    public void pendingWarrantyIncomeShowsAmountsWarningAndEnabledActions() {
        var result = inspectType(36, "Đang chờ");
        assertCommonDetail(result, 36, "Đang chờ");
        assertContainsAll(result.drawerText(), "Người gửi", "Biến động số dư",
                "Số tiền thu từ thợ", "Trừ ký quỹ", "Trừ chi phí", "Thợ:",
                "Đơn BH:", "Tạo bởi", "Chưa trừ ví", "cho phép âm",
                "Từ chối thì không đụng tiền");
        assertLinks(result.userLinks(), "/vuatho/user?id=");
        assertLinks(result.orderLinks(), "/vuatho/order?id=");
        Assert.assertTrue(result.rejectPresent(), "Thiếu nút Từ chối");
        Assert.assertTrue(result.rejectVisible(), "Nút Từ chối không hiển thị");
        Assert.assertTrue(result.walletConfirmPresent(), "Thiếu nút Xác nhận trừ ví thợ");
        Assert.assertTrue(result.walletConfirmVisible(), "Nút Xác nhận trừ ví thợ không hiển thị");
        Assert.assertFalse(result.walletConfirmDisabled(),
                "Nút Xác nhận trừ ví thợ đang bị khóa dù phiếu hợp lệ");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_090)
    public void pendingWarrantyPaymentCopyAndQrKeepSourceDrawerOpen() {
        openOrderSubtype(subtype(37));
        var result = advancedPage().exercisePendingWarrantyPaymentQrAndCopy();
        Assert.assertEquals(result.source().status(), "Đang chờ");
        Assert.assertTrue(result.openedUrl().contains("tab=order&type=37"), result.openedUrl());
        Assert.assertTrue(result.copyButtonCount() >= 2,
                "Thiếu hai nút Copy tài khoản và nội dung chuyển khoản");
        Assert.assertEquals(result.copyClicks(), result.copyButtonCount());
        Assert.assertTrue(result.drawerStayedOpenAfterCopy(),
                "Bấm Copy làm đóng drawer hoặc đổi giao dịch");
        Assert.assertTrue(result.qrOpened(), "Bấm Hiện mã QR không làm giao diện QR thay đổi");
        Assert.assertTrue(result.drawerStayedOpenAfterQr(), "Đóng QR làm mất drawer nguồn");
        Assert.assertTrue(result.closed(), "Không đóng được drawer sau khi kiểm tra QR");
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
    }

    private void openType22() {
        TransactionCategoryPage.Subtype subtype = category().subtypes().stream()
                .filter(value -> value.type() == 22).findFirst().orElseThrow();
        openOrderSubtype(subtype);
    }

    private void assertBackAndForwardRestoresDetail(int type) {
        openOrderSubtype(subtype(type));
        var result = advancedPage().backAndForwardDetail();
        Assert.assertTrue(result.closedAfterBack(), "Back không đóng drawer type=" + type);
        Assert.assertFalse(result.backUrl().contains("id="), result.backUrl());
        Assert.assertEquals(result.forwardUrl(), result.openedUrl(),
                "Forward không khôi phục đúng ID type=" + type);
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"),
                "Forward không mở lại drawer type=" + type);
    }

    private TransactionHistoryPage.OrderDetailElementSnapshot inspectType(int type, String status) {
        openOrderSubtype(subtype(type));
        return advancedPage().inspectOrderDetailElements(status);
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream().filter(value -> value.type() == type)
                .findFirst().orElseThrow();
    }

    private void assertCommonDetail(TransactionHistoryPage.OrderDetailElementSnapshot result,
                                    int type, String status) {
        Assert.assertEquals(result.source().status(), status);
        Assert.assertTrue(result.openedUrl().contains("tab=order&type=" + type), result.openedUrl());
        Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
        Assert.assertTrue(result.drawerText().contains("Chi tiết giao dịch"), result.drawerText());
        Assert.assertTrue(result.drawerText().contains("Trạng thái"), result.drawerText());
        Assert.assertTrue(result.drawerText().contains(status), result.drawerText());
        Assert.assertTrue(result.visibleButtons().contains("Hủy"), result.visibleButtons().toString());
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype(type).label()),
                transactionPage.activeGroupText());
    }

    private void assertContainsAll(String text, String... expectedValues) {
        String normalizedText = normalizeForAssertion(text);
        for (String expected : expectedValues) {
            Assert.assertTrue(normalizedText.contains(normalizeForAssertion(expected)),
                    "Thiếu '" + expected + "': " + text);
        }
    }

    private String normalizeForAssertion(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ").trim();
    }

    private void assertCashFlow(String text) {
        assertContainsAll(text, "Tổng vào", "Tổng ra", "Dòng tiền ròng");
    }

    private void assertLinks(List<TransactionHistoryPage.DetailElementLink> links,
                             String expectedPath) {
        Assert.assertFalse(links.isEmpty(), "Thiếu link " + expectedPath);
        links.forEach(link -> {
            Assert.assertTrue(link.href().contains(expectedPath), link.href());
            Assert.assertEquals(link.target(), "_blank", link.href());
            Assert.assertFalse(link.text().isBlank(), link.href());
        });
    }

    private void assertAllThreeStatusOutcomes(int type) {
        TransactionCategoryPage.Subtype subtype = subtype(type);
        openOrderSubtype(subtype);
            for (String status : List.of("Đang chờ", "Thành công", "Thất bại")) {
                var result = advancedPage().openAndCloseFirstDetailForStatus(status);
                Assert.assertTrue(result.selectedStatus().contains(status),
                        "type=" + type + ", trạng thái=" + status + ": " + result.selectedStatus());
                if (result.empty()) {
                    Assert.assertTrue(result.drawerText().contains("Chưa có dữ liệu"),
                            "type=" + type + ", trạng thái=" + status
                                    + " không có dòng nhưng thiếu empty state: " + result.drawerText());
                } else {
                    Assert.assertEquals(result.source().status(), status,
                            "Sai trạng thái dòng nguồn type=" + type);
                    Assert.assertTrue(result.openedUrl().contains("tab=order"), result.openedUrl());
                    Assert.assertTrue(result.openedUrl().contains("id="), result.openedUrl());
                    Assert.assertTrue(result.drawerText().contains(status),
                            "Drawer type=" + type + " không hiển thị " + status);
                    Assert.assertTrue(result.closed(), "Không đóng được drawer type=" + type);
                    Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
                }
                Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                        "Sai active type=" + type + ": " + transactionPage.activeGroupText());
            }
    }

    private void assertSubtype(TransactionCategoryPage.Subtype subtype) {
        Assert.assertTrue(transactionPage.currentUrl().contains(
                "tab=order&type=" + subtype.type()), transactionPage.currentUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                transactionPage.activeGroupText());
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

    private boolean drawerContainsAmount(String text, BigDecimal expected) {
        Matcher matcher = Pattern.compile("([+−–-]?[0-9][0-9.,]*)\\s*₫").matcher(text);
        while (matcher.find()) {
            if (parseDisplayedAmount(matcher.group(1)).compareTo(expected) == 0) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal parseDisplayedAmount(String value) {
        String raw = value.trim().replace('−', '-').replace('–', '-');
        boolean negative = raw.startsWith("-");
        String digits = raw.replaceAll("[^0-9]", "");
        BigDecimal amount = digits.isBlank() ? BigDecimal.ZERO : new BigDecimal(digits);
        return negative ? amount.negate() : amount;
    }

    private boolean matchesSubtypeContent(String drawerText, String sourceType,
                                           TransactionCategoryPage.Subtype subtype) {
        String text = drawerText.toLowerCase(Locale.ROOT);
        if (text.contains(sourceType.toLowerCase(Locale.ROOT))
                || text.contains(subtype.label().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return switch (subtype.type()) {
            case 24 -> text.contains("phí bảo hành") || text.contains("bảo hành");
            case 36 -> text.contains("thu bảo hành") || text.contains("bảo hành");
            case 37 -> text.contains("đền bù khách") || text.contains("hoàn bh cho khách");
            case 15 -> text.contains("xử phạt") || text.contains("tiền phạt");
            default -> false;
        };
    }
}
