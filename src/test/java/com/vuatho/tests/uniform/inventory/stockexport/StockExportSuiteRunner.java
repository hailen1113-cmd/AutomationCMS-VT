package com.vuatho.tests.uniform.inventory.stockexport;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy toàn bộ testcase Kho tổng → Phiếu xuất kho. */
public final class StockExportSuiteRunner {
    private StockExportSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Kho Đồng phục",
                "Toàn bộ testcase tab Phiếu xuất kho",
                StockExportFilterTest.class,
                StockExportPaginationTest.class);
    }
}
