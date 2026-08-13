package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionInsuranceTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra xuất Excel VT Care. */
public class TransactionInsuranceExportTest extends TransactionInsuranceTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionInsuranceExportTest.class,
                "Lịch sử giao dịch", "VT Care - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_010,
            dataProvider = "insuranceSubtypes")
    public void exportsCurrentSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifyExport(subtype);
    }
}
