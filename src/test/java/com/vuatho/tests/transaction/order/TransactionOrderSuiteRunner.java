package com.vuatho.tests.transaction.order;

import com.vuatho.core.TestNgRunner;

/** Chạy toàn bộ suite chuyên biệt của nhóm Đơn dịch vụ. */
public final class TransactionOrderSuiteRunner {
    private TransactionOrderSuiteRunner() {}

    public static void main(String[] args) {
        TestNgRunner.run("Lịch sử giao dịch", "Đơn dịch vụ",
                TransactionOrderDropdownTest.class,
                TransactionOrderOverviewTest.class,
                TransactionOrderFilterTest.class,
                TransactionOrderNavigationTest.class,
                TransactionOrderDetailTest.class,
                TransactionOrderSubmissionTest.class,
                TransactionOrderExportTest.class);
    }
}
