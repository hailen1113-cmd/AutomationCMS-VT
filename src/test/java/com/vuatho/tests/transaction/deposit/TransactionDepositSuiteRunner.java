package com.vuatho.tests.transaction.deposit;

import com.vuatho.core.TestNgRunner;

/** Chạy toàn bộ suite chuyên biệt của nhóm Tiền nạp. */
public final class TransactionDepositSuiteRunner {
    private TransactionDepositSuiteRunner() {}

    public static void main(String[] args) {
        TestNgRunner.run("Lịch sử giao dịch", "Tiền nạp",
                TransactionDepositDropdownTest.class,
                TransactionDepositOverviewTest.class,
                TransactionDepositFilterTest.class,
                TransactionDepositNavigationTest.class,
                TransactionDepositDetailTest.class,
                TransactionDepositExportTest.class);
    }
}
