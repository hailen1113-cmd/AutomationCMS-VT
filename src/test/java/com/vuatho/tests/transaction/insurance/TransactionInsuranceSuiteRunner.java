package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;

/** Chạy toàn bộ suite chuyên biệt của nhóm VT Care. */
public final class TransactionInsuranceSuiteRunner {
    private TransactionInsuranceSuiteRunner() {}

    public static void main(String[] args) {
        TestNgRunner.run("Lịch sử giao dịch", "VT Care",
                TransactionInsuranceDropdownTest.class,
                TransactionInsuranceOverviewTest.class,
                TransactionInsuranceFilterTest.class,
                TransactionInsuranceNavigationTest.class,
                TransactionInsuranceDetailTest.class,
                TransactionInsuranceExportTest.class);
    }
}
