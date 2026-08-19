package com.vuatho.tests.transaction.deposit;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionDepositTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/** Kiểm tra bố cục của từng loại Tiền nạp. */
public class TransactionDepositOverviewTest extends TransactionDepositTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionDepositOverviewTest.class,
                "Lịch sử giao dịch", "Tiền nạp - Tổng quan");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_003)
    public void showsExpectedFiltersAndColumns() {
        verifyShowsExpectedFiltersAndColumnsForSubtype(subtype(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_152)
    public void showsExpectedFiltersAndColumnsType10() {
        verifyShowsExpectedFiltersAndColumnsForSubtype(subtype(10));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_153)
    public void showsExpectedFiltersAndColumnsType19() {
        verifyShowsExpectedFiltersAndColumnsForSubtype(subtype(19));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_154)
    public void showsExpectedFiltersAndColumnsType20() {
        verifyShowsExpectedFiltersAndColumnsForSubtype(subtype(20));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_155)
    public void showsExpectedFiltersAndColumnsType34() {
        verifyShowsExpectedFiltersAndColumnsForSubtype(subtype(34));
    }

    private void verifyShowsExpectedFiltersAndColumnsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        verifyLayout(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_004)
    public void rowsHaveValidFormats() {
        verifyRowsHaveValidFormatsForSubtype(subtype(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_156)
    public void rowsHaveValidFormatsType10() {
        verifyRowsHaveValidFormatsForSubtype(subtype(10));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_157)
    public void rowsHaveValidFormatsType19() {
        verifyRowsHaveValidFormatsForSubtype(subtype(19));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_158)
    public void rowsHaveValidFormatsType20() {
        verifyRowsHaveValidFormatsForSubtype(subtype(20));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_159)
    public void rowsHaveValidFormatsType34() {
        verifyRowsHaveValidFormatsForSubtype(subtype(34));
    }

    private void verifyRowsHaveValidFormatsForSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        verifyRowFormats();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_062)
    public void rowsMatchSelectedDepositSubtype() {
        verifyRowsMatchSelectedDepositSubtypeForSubtype(subtype(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_160)
    public void rowsMatchSelectedDepositSubtypeType10() {
        verifyRowsMatchSelectedDepositSubtypeForSubtype(subtype(10));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_161)
    public void rowsMatchSelectedDepositSubtypeType19() {
        verifyRowsMatchSelectedDepositSubtypeForSubtype(subtype(19));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_162)
    public void rowsMatchSelectedDepositSubtypeType20() {
        verifyRowsMatchSelectedDepositSubtypeForSubtype(subtype(20));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_163)
    public void rowsMatchSelectedDepositSubtypeType34() {
        verifyRowsMatchSelectedDepositSubtypeForSubtype(subtype(34));
    }

    private void verifyRowsMatchSelectedDepositSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        String expectedType = expectedTransactionType(subtype.type());
        var rows = transactionPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Không có dòng để kiểm tra loại " + subtype.label());
        rows.forEach(row -> Assert.assertEquals(row.value("Loại giao dịch"), expectedType,
                "Dòng không thuộc " + subtype.label() + ": " + row.signature()));
        assertSubtypeUrl(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_063)
    public void rowsUseAllowedStatusesAndGateways() {
        verifyRowsUseAllowedStatusesAndGatewaysForSubtype(subtype(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_164)
    public void rowsUseAllowedStatusesAndGatewaysType10() {
        verifyRowsUseAllowedStatusesAndGatewaysForSubtype(subtype(10));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_165)
    public void rowsUseAllowedStatusesAndGatewaysType19() {
        verifyRowsUseAllowedStatusesAndGatewaysForSubtype(subtype(19));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_166)
    public void rowsUseAllowedStatusesAndGatewaysType20() {
        verifyRowsUseAllowedStatusesAndGatewaysForSubtype(subtype(20));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_167)
    public void rowsUseAllowedStatusesAndGatewaysType34() {
        verifyRowsUseAllowedStatusesAndGatewaysForSubtype(subtype(34));
    }

    private void verifyRowsUseAllowedStatusesAndGatewaysForSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        Set<String> allowedStatuses = Set.of(
                "Đang chờ", "Thành công", "Hoàn thành", "Đã hủy", "Từ chối", "Thất bại");
        Set<String> allowedGateways = allowedGateways(subtype.type());
        var rows = transactionPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Không có dòng để kiểm tra " + subtype.label());
        rows.forEach(row -> {
            Assert.assertTrue(allowedStatuses.contains(row.value("Trạng thái")),
                    "Trạng thái không hợp lệ: " + row.signature());
            String gateway = row.value("Cổng thanh toán").trim().toUpperCase();
            Assert.assertTrue(allowedGateways.contains(gateway),
                    "Cổng thanh toán không hợp lệ trên " + subtype.label() + ": " + row.signature());
        });
        assertSubtypeUrl(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_064)
    public void showsSummaryVariantMatchingDepositSubtype() {
        verifyShowsSummaryVariantMatchingDepositSubtypeForSubtype(subtype(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_168)
    public void showsSummaryVariantMatchingDepositSubtypeType10() {
        verifyShowsSummaryVariantMatchingDepositSubtypeForSubtype(subtype(10));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_169)
    public void showsSummaryVariantMatchingDepositSubtypeType19() {
        verifyShowsSummaryVariantMatchingDepositSubtypeForSubtype(subtype(19));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_170)
    public void showsSummaryVariantMatchingDepositSubtypeType20() {
        verifyShowsSummaryVariantMatchingDepositSubtypeForSubtype(subtype(20));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_171)
    public void showsSummaryVariantMatchingDepositSubtypeType34() {
        verifyShowsSummaryVariantMatchingDepositSubtypeForSubtype(subtype(34));
    }

    private void verifyShowsSummaryVariantMatchingDepositSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        if (subtype.type() == 34) {
            var result = transactionPage.bankTransferDepositOverview();
            Assert.assertTrue(result.amountText().contains(
                    "Tổng nạp tiền vào số dư nền tảng qua chuyển khoản ngân hàng"));
            Assert.assertTrue(result.countText().contains("Tổng số giao dịch"));
            for (String status : List.of("Hoàn thành", "Đang chờ", "Từ chối")) {
                Assert.assertTrue(result.amountText().contains(status));
                Assert.assertTrue(result.countText().contains(status));
            }
        } else {
            var result = transactionPage.costWalletDepositOverview();
            Assert.assertTrue(result.text().contains("Tổng tiền nạp vào Ví chi phí"));
            for (String gateway : List.of("Paypal", "MoMo", "OnePay", "Banking")) {
                Assert.assertTrue(result.text().contains(gateway), "Thiếu thống kê " + gateway);
            }
        }
        assertSubtypeUrl(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_065)
    public void costWalletTotalMatchesEveryDisplayedGateway() {
        verifyCostWalletTotalMatchesEveryDisplayedGatewayForSubtype(subtype(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_184)
    public void costWalletTotalMatchesEveryDisplayedGatewayType10() {
        verifyCostWalletTotalMatchesEveryDisplayedGatewayForSubtype(subtype(10));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_185)
    public void costWalletTotalMatchesEveryDisplayedGatewayType19() {
        verifyCostWalletTotalMatchesEveryDisplayedGatewayForSubtype(subtype(19));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_186)
    public void costWalletTotalMatchesEveryDisplayedGatewayType20() {
        verifyCostWalletTotalMatchesEveryDisplayedGatewayForSubtype(subtype(20));
    }

    private void verifyCostWalletTotalMatchesEveryDisplayedGatewayForSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        var filters = transactionPage.resetAndReadOverviewFilters();
        Assert.assertTrue(filters.status().contains("Chọn trạng thái"),
                "Reset chưa bỏ filter trạng thái: " + filters.status());
        Assert.assertTrue(filters.gateway().contains("Chọn cổng thanh toán"),
                "Reset chưa bỏ filter cổng thanh toán: " + filters.gateway());
        var result = transactionPage.costWalletDepositOverview();
        BigDecimal displayedGatewayTotal = result.gateways().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Assert.assertTrue(filters.gatewayOptions().contains("NEOX"),
                "Dropdown thiếu cổng NEOX: " + filters.gatewayOptions());
        Assert.assertFalse(result.sectorNames().contains("NEOX"),
                "Biểu đồ bất ngờ có sector NEOX nhưng không có card số tiền NEOX");
        Assert.assertEquals(displayedGatewayTotal, result.total(),
                "Tổng tiền không khớp breakdown biểu đồ trên " + subtype.label()
                        + "; tổng=" + result.total()
                        + "; các cổng biểu đồ=" + result.gateways()
                        + "; tổng breakdown=" + displayedGatewayTotal
                        + "; chênh lệch=" + result.total().subtract(displayedGatewayTotal)
                        + "; sector=" + result.sectorNames()
                        + "; dropdown có NEOX nhưng biểu đồ không có card/sector NEOX");
        assertPercentagesTotalOneHundred(result.percentages(), subtype.label());
        assertSubtypeUrl(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_066)
    public void bankTransferSummaryHasValidAmountsCountsAndPercentages() {
        TransactionCategoryPage.Subtype subtype = category().subtypes().stream()
                .filter(candidate -> candidate.type() == 34).findFirst().orElseThrow();
        openDepositSubtype(subtype);
        var result = transactionPage.bankTransferDepositOverview();
        Assert.assertEquals(result.amounts().keySet(), Set.of("Hoàn thành", "Đang chờ", "Từ chối"));
        Assert.assertEquals(result.counts().keySet(), Set.of("Hoàn thành", "Đang chờ", "Từ chối"));
        result.amounts().forEach((status, amount) -> Assert.assertTrue(amount.signum() >= 0,
                "Số tiền âm ở trạng thái " + status + ": " + amount));
        result.counts().forEach((status, count) -> Assert.assertTrue(count >= 0,
                "Số giao dịch âm ở trạng thái " + status + ": " + count));
        int totalTransactions = result.counts().values().stream().mapToInt(Integer::intValue).sum();
        Assert.assertTrue(totalTransactions >= transactionPage.rows().size(),
                "Tổng giao dịch thống kê nhỏ hơn số dòng đang hiển thị");
        assertPercentagesTotalOneHundred(List.copyOf(result.amountPercentages().values()),
                "số tiền Nạp ký quỹ qua NH");
        assertPercentagesTotalOneHundred(List.copyOf(result.countPercentages().values()),
                "số giao dịch Nạp ký quỹ qua NH");
        assertSubtypeUrl(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_067)
    public void initialOverviewFinishesLoadingWithinPageSize() {
        verifyInitialOverviewFinishesLoadingWithinPageSizeForSubtype(subtype(0));
    }





    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_189)
    public void initialOverviewFinishesLoadingWithinPageSizeType10() {
        verifyInitialOverviewFinishesLoadingWithinPageSizeForSubtype(subtype(10));
    }





    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_190)
    public void initialOverviewFinishesLoadingWithinPageSizeType19() {
        verifyInitialOverviewFinishesLoadingWithinPageSizeForSubtype(subtype(19));
    }





    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_191)
    public void initialOverviewFinishesLoadingWithinPageSizeType20() {
        verifyInitialOverviewFinishesLoadingWithinPageSizeForSubtype(subtype(20));
    }





    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_192)
    public void initialOverviewFinishesLoadingWithinPageSizeType34() {
        verifyInitialOverviewFinishesLoadingWithinPageSizeForSubtype(subtype(34));
    }

    private void verifyInitialOverviewFinishesLoadingWithinPageSizeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        var result = transactionPage.initialRender();
        Assert.assertFalse(result.loading());
        Assert.assertTrue(result.visibleRows() <= 20);
        Assert.assertTrue(result.visibleRows() > 0
                || result.empty() && result.text().contains("Chưa có dữ liệu"));
    }








    private String expectedTransactionType(int type) {
        return switch (type) {
            case 0 -> "Tiền nạp";
            case 10 -> "Thanh toán bởi bên thứ 3";
            case 19 -> "Tiền nạp từ doanh nghiệp";
            case 20 -> "Tiền nạp vào ví ký quỹ";
            case 34 -> "Nạp tiền vào số dư nền tảng qua chuyển khoản ngân hàng";
            default -> throw new IllegalArgumentException("Chưa khai báo loại Tiền nạp: " + type);
        };
    }

    private Set<String> allowedGateways(int type) {
        return switch (type) {
            case 0, 10 -> Set.of("PAYPAL", "MOMO", "ONEPAY", "BANKING", "NEOX");
            case 19 -> Set.of("");
            case 20 -> Set.of("VÍ CHI PHÍ");
            case 34 -> Set.of("BANKING");
            default -> throw new IllegalArgumentException("Chưa khai báo cổng cho type=" + type);
        };
    }

    private void assertPercentagesTotalOneHundred(List<BigDecimal> percentages, String context) {
        Assert.assertFalse(percentages.isEmpty(), "Không đọc được tỷ lệ của " + context);
        percentages.forEach(value -> Assert.assertTrue(
                value.compareTo(BigDecimal.ZERO) >= 0 && value.compareTo(new BigDecimal("100")) <= 0,
                "Tỷ lệ không hợp lệ của " + context + ": " + value));
        BigDecimal total = percentages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Assert.assertTrue(total.compareTo(new BigDecimal("99.9")) >= 0
                        && total.compareTo(new BigDecimal("100.1")) <= 0,
                "Tổng tỷ lệ của " + context + " không xấp xỉ 100%: " + total);
    }

    private void assertSubtypeUrl(TransactionCategoryPage.Subtype subtype) {
        String url = transactionPage.currentUrl();
        Assert.assertTrue(url.contains("/vuatho/transaction"), "Sai màn hình: " + url);
        Assert.assertTrue(url.contains("tab=" + subtype.tab()), "Sai tab: " + url);
        Assert.assertTrue(url.contains("type=" + subtype.type()), "Sai loại: " + url);
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
