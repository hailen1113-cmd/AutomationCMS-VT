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
                "ERP Login and eKYC Suite",
                "Login and eKYC Checks",
                ErpLoginTest.class,
                EkycWorkbookCatalogTest.class,
                EkycApiTest.class);
    }
}
