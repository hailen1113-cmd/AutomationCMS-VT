package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionInsuranceTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Kiểm tra tìm kiếm và toàn bộ bộ lọc VT Care. */
public class TransactionInsuranceFilterTest extends TransactionInsuranceTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionInsuranceFilterTest.class,
                "Lịch sử giao dịch", "VT Care - Bộ lọc");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_005,
            dataProvider = "insuranceSubtypes")
    public void searchesUserAndRestoresRows(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifySearchAndReset(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_006,
            dataProvider = "insuranceSelectFilters")
    public void selectFiltersExposeExpectedOptions(String ariaLabel, List<String> expected) {
        var result = transactionPage.optionsForFilter(ariaLabel);
        Assert.assertEquals(result.ariaLabel(), ariaLabel);
        Assert.assertEquals(result.options(), expected);
        Assert.assertTrue(result.beforeResetUrl().contains("tab=insurance&type=25"),
                result.beforeResetUrl());
        Assert.assertTrue(result.activeText().contains("Trừ phí VT Care"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_017,
            dataProvider = "insuranceFilterSelections")
    public void everyFilterOptionKeepsSelectedSubtype(TransactionCategoryPage.Subtype subtype,
                                                       String ariaLabel, String option) {
        openInsuranceSubtype(subtype);
        var result = transactionPage.selectOption(ariaLabel, option);
        Assert.assertTrue(result.selectedText().contains(option), result.selectedText());
        Assert.assertTrue(!result.rows().isEmpty() || result.empty(), result.pageText());
        Assert.assertTrue(result.url().contains("tab=insurance&type=" + subtype.type()), result.url());
        Assert.assertTrue(result.activeText().contains(subtype.label()), result.activeText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_018)
    public void futureDatesCannotBeSelectedAndDateRequiresSelection() {
        var future = advancedPage().futureDatesAreDisabled();
        Assert.assertTrue(future.disabled());
        Assert.assertTrue(future.disabledCount() > 0);
        var defaults = advancedPage().dateControlDefaults();
        Assert.assertEquals(defaults.startTime(), "00:00");
        Assert.assertEquals(defaults.endTime(), "23:59");
        Assert.assertTrue(defaults.applyDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_019,
            dataProvider = "insuranceSubtypes")
    public void singleDayOnlyReturnsRowsFromSelectedDate(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().filterSingleDay();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())));
        String date = result.startDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Assert.assertTrue(result.selectedText().contains(date));
        Assert.assertTrue(result.selectedText().contains("00:00"));
        Assert.assertTrue(result.selectedText().contains("23:59"));
        Assert.assertTrue(transactionPage.currentUrl().contains("type=" + subtype.type()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_020,
            dataProvider = "insuranceSubtypes")
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtype(
            TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = transactionPage.unmatchedSearchAndReset("NO_VT_CARE_TRANSACTION_987654321");
        Assert.assertTrue(result.empty());
        Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"));
        Assert.assertEquals(result.restored().stream().map(TransactionCategoryPage.TransactionRow::signature).toList(),
                result.before().stream().map(TransactionCategoryPage.TransactionRow::signature).toList());
        Assert.assertTrue(result.url().contains("tab=insurance&type=" + subtype.type()), result.url());
        Assert.assertTrue(result.activeText().contains(subtype.label()));
    }
}
