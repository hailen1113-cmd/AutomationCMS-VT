package com.vuatho.tests.uniform.inventory.stockreceipt;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy toàn bộ testcase Nhập kho tổng. */
public final class StockReceiptSuiteRunner {
    private StockReceiptSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Kho Đồng phục", "Toàn bộ testcase Nhập kho tổng",
                StockReceiptFormTest.class,
                StockReceiptValidationTest.class,
                StockReceiptBulkFillTest.class,
                StockReceiptSubmissionTest.class);
    }
}
