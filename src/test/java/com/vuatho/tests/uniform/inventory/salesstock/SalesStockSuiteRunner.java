package com.vuatho.tests.uniform.inventory.salesstock;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy toàn bộ testcase của hai tab Tồn kho và Phiếu thuộc Kho bán hàng. */
public final class SalesStockSuiteRunner {
    private SalesStockSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run("Kho bán hàng", "Toàn bộ testcase Kho bán hàng",
                com.vuatho.tests.uniform.inventory.salesstock.stock.SalesStockOverviewTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.stock.SalesStockSearchTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.stock.SalesStockViewTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.stock.SalesStockDetailTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.stockadjustment.SalesStockAdjustmentFormTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.stockadjustment.SalesStockAdjustmentValidationTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.stockadjustment.SalesStockAdjustmentSubmissionTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.list.SalesReceiptOverviewTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.list.SalesReceiptFilterTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.list.SalesReceiptPaginationTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.staffexport.StaffExportFormTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.staffexport.StaffExportValidationTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.staffexport.StaffExportSubmissionTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.stockimport.SalesStockImportFormTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.stockimport.SalesStockImportValidationTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.stockimport.SalesStockImportSubmissionTest.class);
    }
}
