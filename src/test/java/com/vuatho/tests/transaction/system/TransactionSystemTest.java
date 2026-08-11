package com.vuatho.tests.transaction.system;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionCategoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra chuyên biệt nhóm Hệ thống trong Lịch sử giao dịch. */
public class TransactionSystemTest extends TransactionCategoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionSystemTest.class, "Lịch sử giao dịch", "Hệ thống");
    }

    @Override
    protected TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.SYSTEM;
    }

    @DataProvider(name = "subtypes")
    public Object[][] subtypes() {
        return subtypeRows();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_002,
            dataProvider = "subtypes")
    public void opensEverySubtypeRoute(TransactionCategoryPage.Subtype subtype) {
        verifySubtypeRoute(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_003)
    public void showsExpectedFiltersAndColumns() {
        verifyLayout();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_004)
    public void rowsHaveValidFormats() {
        verifyRowFormats();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_005)
    public void searchesUserAndRestoresRows() {
        verifySearchAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_006)
    public void filterOptionsKeepCurrentTab() {
        verifyFilterOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_007)
    public void sortsAmountBothDirections() {
        verifyAmountSort();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_008)
    public void opensAndClosesDetail() {
        verifyDetail();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_009)
    public void paginationAndResetKeepCurrentTab() {
        verifyPaginationAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_SYSTEM_010)
    public void exportsCurrentSubtype() {
        verifyExport();
    }
}

