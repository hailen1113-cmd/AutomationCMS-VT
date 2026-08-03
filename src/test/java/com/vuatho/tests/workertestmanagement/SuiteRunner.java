package com.vuatho.tests.workertestmanagement;

import com.vuatho.core.TestNgRunner;

/** Chạy toàn bộ testcase có thao tác và dữ liệu trả về của menu Bài kiểm tra. */
public final class SuiteRunner {
    private SuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.runGroup(
                "Bài kiểm tra",
                "Toàn bộ testcase dữ liệu",
                "data-interaction",
                OverviewTest.class,
                SearchFilterTest.class,
                PaginationTest.class,
                DetailTest.class);
    }
}
