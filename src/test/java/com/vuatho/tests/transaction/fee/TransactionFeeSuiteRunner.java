package com.vuatho.tests.transaction.fee;

import com.vuatho.core.TestNgRunner;

/** Chạy toàn bộ suite chuyên biệt của nhóm Phí & Doanh thu. */
public final class TransactionFeeSuiteRunner {
    private TransactionFeeSuiteRunner() {}

    public static void main(String[] args) {
        TestNgRunner.run("Lịch sử giao dịch", "Phí & Doanh thu",
                TransactionFeeDropdownTest.class,
                TransactionFeeOverviewTest.class,
                TransactionFeeFilterTest.class,
                TransactionFeeNavigationTest.class,
                TransactionFeeDetailTest.class,
                TransactionFeeExportTest.class);
    }
}
