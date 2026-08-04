package com.vuatho.tests.uniform.inventory.salesstock;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy toàn bộ testcase của hai tab Tồn kho và Phiếu thuộc Kho bán hàng. */
public final class SuiteRunner {
    private SuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run("Kho bán hàng", "Toàn bộ testcase Kho bán hàng",
                com.vuatho.tests.uniform.inventory.salesstock.stock.OverviewTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.stock.SearchTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.stock.ViewTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.stock.DetailTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.read.OverviewTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.read.FilterTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.read.PaginationTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.export.FormTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.export.ValidationTest.class,
                com.vuatho.tests.uniform.inventory.salesstock.receipt.export.SubmissionTest.class);
    }
}
