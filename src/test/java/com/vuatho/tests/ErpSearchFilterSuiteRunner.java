package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class ErpSearchFilterSuiteRunner {
    private ErpSearchFilterSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "ERP Filter Automation Suite",
                "All Search Filter Menus",
                LoginDashboardSourceAccessTest.class,
                CrossMenuSidebarNavigationTest.class,
                UserProfileSearchTest.class,
                WorkerProfileSearchTest.class,
                WorkerProfileFilterTest.class,
                CrossMenuSearchFilterResetTest.class);
    }
}
