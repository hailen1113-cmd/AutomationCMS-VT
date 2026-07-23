package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy tổng cho các testcase có thao tác và dữ liệu trả về. */
public final class WorkerStopRequestSuiteRunner {
    private WorkerStopRequestSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.runGroup(
                "Yêu cầu ngưng hợp tác",
                "Testcase có thao tác và dữ liệu trả về",
                "data-interaction",
                WorkerStopRequestOverviewTest.class,
                WorkerStopRequestSearchFilterTest.class,
                WorkerStopRequestPaginationTest.class,
                WorkerStopRequestDetailTest.class,
                WorkerStopRequestApprovalRejectionTest.class);
    }
}
