package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/** Chạy toàn bộ testcase có thao tác và dữ liệu trả về của menu Bài kiểm tra. */
public final class WorkerTestManagementSuiteRunner {
    private WorkerTestManagementSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.runGroup(
                "Bài kiểm tra",
                "Toàn bộ testcase dữ liệu",
                "data-interaction",
                WorkerTestManagementOverviewTest.class,
                WorkerTestManagementSearchFilterTest.class,
                WorkerTestManagementPaginationTest.class,
                WorkerTestManagementDetailTest.class);
    }
}
