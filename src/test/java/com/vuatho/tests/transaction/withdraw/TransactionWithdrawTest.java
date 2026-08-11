package com.vuatho.tests.transaction.withdraw;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionCategoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra chuyên biệt nhóm Tiền rút trong Lịch sử giao dịch. */
public class TransactionWithdrawTest extends TransactionCategoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionWithdrawTest.class, "Lịch sử giao dịch", "Tiền rút");
    }

    @Override
    protected TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.WITHDRAW;
    }

    @DataProvider(name = "subtypes")
    public Object[][] subtypes() {
        return subtypeRows();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_002,
            dataProvider = "subtypes")
    public void opensEverySubtypeRoute(TransactionCategoryPage.Subtype subtype) {
        verifySubtypeRoute(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_003)
    public void showsExpectedFiltersAndColumns() {
        verifyLayout();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_004)
    public void rowsHaveValidFormats() {
        verifyRowFormats();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_005)
    public void searchesUserAndRestoresRows() {
        verifySearchAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_006)
    public void filterOptionsKeepCurrentTab() {
        verifyFilterOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_007)
    public void sortsAmountBothDirections() {
        verifyAmountSort();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_008)
    public void opensAndClosesDetail() {
        verifyDetail();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_009)
    public void paginationAndResetKeepCurrentTab() {
        verifyPaginationAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_WITHDRAW_010)
    public void exportsCurrentSubtype() {
        verifyExport();
    }
}

