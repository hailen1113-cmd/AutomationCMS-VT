package com.vuatho.tests.uniform.inventory.salesstock.receipts;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy toàn bộ testcase tab Phiếu của Kho bán hàng. */
public final class SalesReceiptSuiteRunner {
    private SalesReceiptSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run("Kho bán hàng", "Toàn bộ testcase tab Phiếu",
                com.vuatho.tests.uniform.inventory.salesstock.receipts.list.SalesReceiptOverviewTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.list.SalesReceiptFilterTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.list.SalesReceiptPaginationTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.staffexport.StaffExportFormTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.staffexport.StaffExportValidationTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipts.staffexport.StaffExportSubmissionTest.class);
    }
}
