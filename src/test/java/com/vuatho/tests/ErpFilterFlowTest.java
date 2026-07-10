package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class ErpFilterFlowTest {
    private ErpFilterFlowTest() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "ERP Filter Automation Suite",
                "All Search Filter Menus",
                ErpLoginTest.class,
                MenuNavigationTest.class,
                FilterBehaviorTest.class);
    }
}
