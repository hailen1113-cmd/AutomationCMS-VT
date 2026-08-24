package com.vuatho.tests.transaction.system;

import com.vuatho.core.TestNgRunner;

/** Chạy toàn bộ testcase của nhóm Hệ thống. */
public final class TransactionSystemSuiteRunner {
    private TransactionSystemSuiteRunner() {}

    public static void main(String[] args) {
        TestNgRunner.run("Lịch sử giao dịch", "Hệ thống",
                TransactionSystemTest.class,
                TransactionSystemFilterTest.class,
                TransactionSystemNavigationTest.class,
                TransactionSystemDetailTest.class);
    }
}
