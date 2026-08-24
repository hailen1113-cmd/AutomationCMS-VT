package com.vuatho.tests.transaction.reward;

import com.vuatho.core.TestNgRunner;

/** Chạy toàn bộ testcase của nhóm Thưởng & KM. */
public final class TransactionRewardSuiteRunner {
    private TransactionRewardSuiteRunner() {}

    public static void main(String[] args) {
        TestNgRunner.run("Lịch sử giao dịch", "Thưởng & KM",
                TransactionRewardOverviewAndExportTest.class,
                TransactionRewardFilterTest.class,
                TransactionRewardNavigationTest.class,
                TransactionRewardDetailTest.class);
    }
}
