package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionInsuranceTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Set;

/** Kiểm tra bố cục, dữ liệu bảng và hai biểu đồ tổng quan VT Care. */
public class TransactionInsuranceOverviewTest extends TransactionInsuranceTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionInsuranceOverviewTest.class,
                "Lịch sử giao dịch", "VT Care - Tổng quan");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_003,
            dataProvider = "insuranceSubtypes")
    public void showsExpectedFiltersAndColumns(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifyLayout(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_004,
            dataProvider = "insuranceSubtypes")
    public void rowsHaveValidFormats(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifyRowFormats();
        transactionPage.rows().forEach(row -> Assert.assertEquals(
                row.value("Loại giao dịch"), subtype.type() == 25
                        ? "Trừ phí VT Care hàng ngày/tháng"
                        : "Hoàn phí VT Care khi hủy gói"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_015,
            dataProvider = "insuranceSubtypes")
    public void amountAndCountTotalsMatchThreeStatusBreakdowns(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = transactionPage.insuranceOverview(subtype);
        Assert.assertEquals(result.amounts().keySet(), Set.of("Hoàn thành", "Đang chờ", "Từ chối"));
        Assert.assertEquals(result.counts().keySet(), Set.of("Hoàn thành", "Đang chờ", "Từ chối"));
        Assert.assertEquals(result.totalAmount(), result.amounts().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        Assert.assertEquals(result.totalCount(), result.counts().values().stream()
                .mapToInt(Integer::intValue).sum());
        Assert.assertEquals(sum(result.amountPercentages().values()), new BigDecimal("100.0"));
        Assert.assertEquals(sum(result.countPercentages().values()), new BigDecimal("100.0"));
        Assert.assertTrue(result.url().contains("tab=insurance&type=" + subtype.type()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_016,
            dataProvider = "insuranceSubtypes")
    public void overviewCountMatchesFullFilteredResult(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        Assert.assertEquals(transactionPage.insuranceOverview(subtype).totalCount(),
                advancedPage().totalDisplayed());
    }

    private BigDecimal sum(java.util.Collection<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
