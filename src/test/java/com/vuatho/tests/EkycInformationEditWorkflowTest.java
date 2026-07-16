package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.testdata.EkycInformationAction;
import com.vuatho.testdata.EkycInformationCase;
import com.vuatho.testdata.EkycInformationTestData;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class EkycInformationEditWorkflowTest extends EkycWorkflowTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(EkycInformationEditWorkflowTest.class,
                "Bo test sua thong tin eKYC ERP",
                "Kiem tra edit thong tin eKYC");
    }

    @DataProvider(name = "ekycEditInformationCases", parallel = false)
    public Object[][] ekycEditInformationCases() {
        List<EkycInformationCase> cases = EkycInformationTestData.cases().stream()
                .filter(testCase -> testCase.action() == EkycInformationAction.EDIT)
                .filter(this::matchesConfiguredInformationCase)
                .toList();
        Object[][] rows = new Object[cases.size()][1];
        for (int index = 0; index < cases.size(); index++) {
            rows[index][0] = cases.get(index);
        }
        return rows;
    }

    @Test(dataProvider = "ekycEditInformationCases",
            groups = {"ekyc", "information", "edit"},
            description = "EKYC-INFORMATION-EDIT: Sua thong tin eKYC")
    public void runFocusedKycInformationEditCase(EkycInformationCase testCase) {
        if (testCase.action() != EkycInformationAction.EDIT) {
            throw new SkipException(testCase.id() + ": Case khong phai EDIT.");
        }
        runEditInformationCase(testCase);
    }

    private boolean matchesConfiguredInformationCase(EkycInformationCase testCase) {
        String configuredCaseId = configured("ekyc.case.id", "EKYC_CASE_ID");
        if (!configuredCaseId.isBlank() && !testCase.id().equalsIgnoreCase(configuredCaseId)) {
            return false;
        }
        String configuredFamily = configured("ekyc.case.family", "EKYC_CASE_FAMILY");
        return configuredFamily.isBlank() || "EDIT".equalsIgnoreCase(configuredFamily);
    }
}
