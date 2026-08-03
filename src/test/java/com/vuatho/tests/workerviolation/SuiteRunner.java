package com.vuatho.tests.workerviolation;

import com.vuatho.core.TestNgRunner;

/** Runner doc lap cho toan bo testcase menu Quan li tho vi pham. */
public final class SuiteRunner {
    private SuiteRunner() { }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test Quan li tho vi pham ERP",
                "Chay day du testcase read-only cho menu Quan li tho vi pham",
                OverviewTest.class,
                StatisticsTest.class,
                SearchFilterTest.class,
                TablePaginationTest.class,
                DetailStateTest.class);
    }
}
