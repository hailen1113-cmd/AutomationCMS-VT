package com.vuatho.tests.workerstoprequest;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy tổng cho các testcase có thao tác và dữ liệu trả về. */
public final class SuiteRunner {
    private SuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.runGroup(
                "Yêu cầu ngưng hợp tác",
                "Testcase có thao tác và dữ liệu trả về",
                "data-interaction",
                OverviewTest.class,
                SearchFilterTest.class,
                PaginationTest.class,
                DetailTest.class,
                ApprovalRejectionTest.class);
    }
}
