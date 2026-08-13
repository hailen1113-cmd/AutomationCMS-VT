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

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_003,
            dataProvider = "depositSubtypes")
    public void showsExpectedFiltersAndColumns(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        verifyLayout(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_004,
            dataProvider = "depositSubtypes")
    public void rowsHaveValidFormats(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        verifyRowFormats();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_062,
            dataProvider = "depositSubtypes")
    public void rowsMatchSelectedDepositSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        String expectedType = expectedTransactionType(subtype.type());
        var rows = transactionPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Không có dòng để kiểm tra loại " + subtype.label());
        rows.forEach(row -> Assert.assertEquals(row.value("Loại giao dịch"), expectedType,
                "Dòng không thuộc " + subtype.label() + ": " + row.signature()));
        assertSubtypeUrl(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_063,
            dataProvider = "depositSubtypes")
    public void rowsUseAllowedStatusesAndGateways(TransactionCategoryPage.Subtype subtype) {
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

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_064,
            dataProvider = "depositSubtypes")
    public void showsSummaryVariantMatchingDepositSubtype(TransactionCategoryPage.Subtype subtype) {
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

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_065,
            dataProvider = "costWalletGatewaySubtypes")
    public void costWalletTotalMatchesEveryDisplayedGateway(
            TransactionCategoryPage.Subtype subtype) {
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

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_067,
            dataProvider = "depositSubtypes")
    public void initialOverviewFinishesLoadingWithinPageSize(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        var result = transactionPage.initialRender();
        Assert.assertFalse(result.loading(), "Màn hình vẫn còn trạng thái Đang tải dữ liệu");
        Assert.assertTrue(result.visibleRows() <= 20,
                "Trang hiển thị quá 20 dòng: " + result.visibleRows());
        Assert.assertTrue(result.visibleRows() > 0
                        || result.empty() && result.text().contains("Chưa có dữ liệu"),
                "Không có dòng nhưng cũng không hiển thị trạng thái rỗng hợp lệ");
        assertSubtypeUrl(subtype);
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
}
