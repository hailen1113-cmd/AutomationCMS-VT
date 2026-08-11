package com.vuatho.tests.transaction.deposit;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionDepositTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra xuất Excel của từng loại Tiền nạp. */
public class TransactionDepositExportTest extends TransactionDepositTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionDepositExportTest.class,
                "Lịch sử giao dịch", "Tiền nạp - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_010,
            dataProvider = "depositSubtypes")
    public void exportsCurrentSubtype(TransactionCategoryPage.Subtype subtype) {
        openDepositSubtype(subtype);
        verifyExport(subtype);
    }
}
