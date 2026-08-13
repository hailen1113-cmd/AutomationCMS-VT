package com.vuatho.tests.transaction.fee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra xuất Excel của từng loại Phí & Doanh thu. */
public class TransactionFeeExportTest extends TransactionFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionFeeExportTest.class,
                "Lịch sử giao dịch", "Phí & Doanh thu - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_010,
            dataProvider = "feeSubtypes")
    public void exportsCurrentSubtype(TransactionCategoryPage.Subtype subtype) {
        openFeeSubtype(subtype);
        verifyExport(subtype);
    }
}
