package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class ErpLoginEkycSuiteRunner {
    private ErpLoginEkycSuiteRunner() {
    }

    public static void main(String[] args) {
        runSuite();
    }

    public static void runSuite() {
        TestNgRunner.run(
                "ERP Login and eKYC Suite",
                "Login and eKYC Checks",
                LoginDashboardSourceAccessTest.class,
                EkycWorkbookCatalogTest.class,
                EkycReviewWorkflowTest.class,
                EkycInformationEditWorkflowTest.class,
                EkycInformationClearWorkflowTest.class);
    }
}
