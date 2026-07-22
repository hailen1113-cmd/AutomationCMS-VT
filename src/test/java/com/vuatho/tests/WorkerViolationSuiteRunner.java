package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/** Runner doc lap cho toan bo testcase menu Quan li tho vi pham. */
public final class WorkerViolationSuiteRunner {
    private WorkerViolationSuiteRunner() { }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test Quan li tho vi pham ERP",
                "Chay day du testcase read-only cho menu Quan li tho vi pham",
                WorkerViolationOverviewTest.class,
                WorkerViolationStatisticsTest.class,
                WorkerViolationSearchFilterTest.class,
                WorkerViolationTablePaginationTest.class,
                WorkerViolationDetailStateTest.class);
    }
}
