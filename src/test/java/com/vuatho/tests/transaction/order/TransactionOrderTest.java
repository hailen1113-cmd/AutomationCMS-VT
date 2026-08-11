package com.vuatho.tests.transaction.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionCategoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra chuyên biệt nhóm Đơn dịch vụ trong Lịch sử giao dịch. */
public class TransactionOrderTest extends TransactionCategoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionOrderTest.class, "Lịch sử giao dịch", "Đơn dịch vụ");
    }

    @Override
    protected TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.ORDER;
    }

    @DataProvider(name = "subtypes")
    public Object[][] subtypes() {
        return subtypeRows();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_002,
            dataProvider = "subtypes")
    public void opensEverySubtypeRoute(TransactionCategoryPage.Subtype subtype) {
        verifySubtypeRoute(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_003)
    public void showsExpectedFiltersAndColumns() {
        verifyLayout();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_004)
    public void rowsHaveValidFormats() {
        verifyRowFormats();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_005)
    public void searchesUserAndRestoresRows() {
        verifySearchAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_006)
    public void filterOptionsKeepCurrentTab() {
        verifyFilterOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_007)
    public void sortsAmountBothDirections() {
        verifyAmountSort();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_008)
    public void opensAndClosesDetail() {
        verifyDetail();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_009)
    public void paginationAndResetKeepCurrentTab() {
        verifyPaginationAndReset();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_010)
    public void exportsCurrentSubtype() {
        verifyExport();
    }
}

