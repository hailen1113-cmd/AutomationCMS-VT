package com.vuatho.tests.uniform.inventory.uniformstock.stockadjustment;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy toàn bộ testcase Điều chỉnh tồn. */
public final class StockAdjustmentSuiteRunner {
    private StockAdjustmentSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run("Kho Đồng phục", "Toàn bộ testcase Điều chỉnh tồn",
                StockAdjustmentFormTest.class,
                StockAdjustmentValidationTest.class,
                StockAdjustmentSubmissionTest.class);
    }
}
