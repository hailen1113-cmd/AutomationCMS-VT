package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class EkycWorkflowSuiteRunner {
    private EkycWorkflowSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test workflow eKYC ERP",
                "Chay tat ca nhom testcase workflow eKYC",
                EkycReviewWorkflowTest.class,
                EkycInformationEditWorkflowTest.class,
                EkycInformationClearWorkflowTest.class);
    }
}
