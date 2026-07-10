package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class ErpQuickFlowTest {
    private ErpQuickFlowTest() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "ERP Quick Automation Suite",
                "Login, Dashboard and Menu Load Pages",
                ErpLoginTest.class,
                MenuNavigationTest.class);
    }
}
