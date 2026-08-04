package com.vuatho.tests.uniform.inventory.salesstock.stock;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy toàn bộ testcase Kho bán hàng → Tồn kho. */
public final class SuiteRunner {
    private SuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run("Kho bán hàng", "Toàn bộ testcase Tồn kho",
                OverviewTest.class,
                SearchTest.class,
                ViewTest.class,
                DetailTest.class);
    }
}
