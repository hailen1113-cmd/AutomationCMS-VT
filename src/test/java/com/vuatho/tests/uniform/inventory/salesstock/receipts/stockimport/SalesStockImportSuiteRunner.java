package com.vuatho.tests.uniform.inventory.salesstock.receipts.stockimport;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy riêng toàn bộ testcase Nhập hàng về Kho bán hàng. */
public final class SalesStockImportSuiteRunner {
    private SalesStockImportSuiteRunner() {}

    public static void main(String[] args) {
        TestNgRunner.run("Kho bán hàng", "Toàn bộ testcase Nhập hàng",
                SalesStockImportFormTest.class,
                SalesStockImportValidationTest.class,
                SalesStockImportSubmissionTest.class);
    }
}
