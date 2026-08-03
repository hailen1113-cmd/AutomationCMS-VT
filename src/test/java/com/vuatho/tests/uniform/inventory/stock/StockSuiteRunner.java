package com.vuatho.tests.uniform.inventory.stock;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy toàn bộ testcase Kho tổng → Tồn kho. */
public final class StockSuiteRunner {
    private StockSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Kho Đồng phục",
                "Toàn bộ testcase tab Tồn kho",
                StockOverviewTest.class,
                StockSearchTest.class,
                StockViewTest.class,
                StockDetailTest.class);
    }
}
