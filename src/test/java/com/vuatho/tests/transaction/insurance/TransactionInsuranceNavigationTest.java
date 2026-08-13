package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionInsuranceTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Kiểm tra sắp xếp và phân trang VT Care. */
public class TransactionInsuranceNavigationTest extends TransactionInsuranceTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionInsuranceNavigationTest.class,
                "Lịch sử giao dịch", "VT Care - Điều hướng");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_007,
            dataProvider = "insuranceSubtypes")
    public void sortsAmountBothDirections(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifyAmountSort(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_009,
            dataProvider = "insuranceSubtypes")
    public void paginationAndResetKeepCurrentSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifyPaginationAndReset(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_021,
            dataProvider = "insuranceSubtypes")
    public void sortsCreatedDateBothDirections(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var ascending = advancedPage().sort("Ngày tạo", false).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(ascending, Comparator.naturalOrder());
        var descending = advancedPage().sort("Ngày tạo", true).rows().stream()
                .map(TransactionHistoryPage.TransactionRow::createdAt).toList();
        assertOrdered(descending, Comparator.reverseOrder());
        Assert.assertTrue(transactionPage.currentUrl().contains("type=" + subtype.type()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_022)
    public void pageTwoChangesAndReturningRestoresPageOne() {
        var result = advancedPage().pageTwoAndBack();
        Assert.assertNotEquals(signatures(result.pageTwo()), signatures(result.pageOne()));
        Assert.assertEquals(signatures(result.returnedPageOne()), signatures(result.pageOne()));
        Assert.assertEquals(result.activePage(), 1);
        Assert.assertTrue(transactionPage.currentUrl().contains("tab=insurance&type=25"));
    }

    private <T> void assertOrdered(List<T> actual, Comparator<T> comparator) {
        Assert.assertTrue(actual.size() > 1);
        List<T> expected = new ArrayList<>(actual);
        expected.sort(comparator);
        Assert.assertEquals(actual, expected);
    }

    private List<String> signatures(List<TransactionHistoryPage.TransactionRow> rows) {
        return rows.stream().map(TransactionHistoryPage.TransactionRow::signature).toList();
    }
}
