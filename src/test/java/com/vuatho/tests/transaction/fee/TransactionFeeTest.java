package com.vuatho.tests.transaction.fee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionCategoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra chuyên biệt nhóm Phí và doanh thu trong Lịch sử giao dịch. */
public class TransactionFeeTest extends TransactionCategoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionFeeTest.class, "Lịch sử giao dịch", "Phí và doanh thu");
    }

    @Override
    protected TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.FEE;
    }

    @DataProvider(name = "subtypes")
    public Object[][] subtypes() {
        return subtypeRows();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_002,
            dataProvider = "subtypes")
    public void opensEverySubtypeRoute(TransactionCategoryPage.Subtype subtype) {
        verifySubtypeRoute(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_003)
    public void showsExpectedFiltersAndColumns() {
        verifyLayout();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_004)
    public void rowsHaveValidFormats() {
        verifyRowFormats();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_005)
    public void searchesUserAndRestoresRows() {
        verifySearchAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_006)
    public void filterOptionsKeepCurrentTab() {
        verifyFilterOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_007)
    public void sortsAmountBothDirections() {
        verifyAmountSort();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_008)
    public void opensAndClosesDetail() {
        verifyDetail();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_009)
    public void paginationAndResetKeepCurrentTab() {
        verifyPaginationAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_010)
    public void exportsCurrentSubtype() {
        verifyExport();
    }
}

