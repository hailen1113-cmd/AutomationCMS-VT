package com.vuatho.tests.transaction.deposit;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionDepositTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

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
}
