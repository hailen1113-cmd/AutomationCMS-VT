package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class ErpFullFlowTest {
    private ErpFullFlowTest() {
    }

    public static void main(String[] args) {
        runSuite();
    }

    public static void runSuite() {
        TestNgRunner.run(
                "ERP Full Automation Suite",
                "Smoke, Login, Dashboard, Load Pages and Filters",
                ErpSmokeTest.class,
                ErpLoginTest.class,
                DashboardTest.class,
                MenuNavigationTest.class,
                FilterBehaviorTest.class,
                FeatureDiscoveryTest.class,
                DeepFeatureDiscoveryTest.class,
                FilterDiscoveryTest.class,
                ReadOnlyFeatureTest.class,
                DeepReadOnlyFeatureTest.class);
    }
}
