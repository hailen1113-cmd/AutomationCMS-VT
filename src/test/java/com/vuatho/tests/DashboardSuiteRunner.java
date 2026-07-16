package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class DashboardSuiteRunner {
    private DashboardSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test Dashboard ERP",
                "Chay tat ca nhom testcase Dashboard",
                DashboardCoreUiTest.class,
                DashboardSummaryCardsTest.class,
                DashboardWorkbookUiTest.class,
                DashboardLogoutTest.class);
    }
}
