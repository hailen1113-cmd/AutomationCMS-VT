package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class ErpQuickSmokeSuiteRunner {
    private ErpQuickSmokeSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "ERP Quick Automation Suite",
                "Login, Dashboard and Menu Load Pages",
                LoginDashboardSourceAccessTest.class,
                CrossMenuSidebarNavigationTest.class);
    }
}
