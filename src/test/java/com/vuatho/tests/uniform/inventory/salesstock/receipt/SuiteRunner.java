package com.vuatho.tests.uniform.inventory.salesstock.receipt;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy toàn bộ testcase tab Phiếu của Kho bán hàng. */
public final class SuiteRunner {
    private SuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run("Kho bán hàng", "Toàn bộ testcase tab Phiếu",
                com.vuatho.tests.uniform.inventory.salesstock.receipt.read.OverviewTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.read.FilterTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.read.PaginationTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.export.FormTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.export.ValidationTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.export.SubmissionTest.class);
    }
}
