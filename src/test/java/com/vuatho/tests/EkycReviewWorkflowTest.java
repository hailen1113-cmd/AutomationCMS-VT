package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.testdata.EkycWorkbookCatalog;
import com.vuatho.testdata.EkycWorkbookCase;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class EkycReviewWorkflowTest extends EkycWorkflowTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(EkycReviewWorkflowTest.class,
                "Bo test review eKYC ERP",
                "Kiem tra approve va reject eKYC");
    }

    @DataProvider(name = "ekycReviewCases", parallel = false)
    public Object[][] ekycReviewCases() {
        List<EkycWorkbookCase> cases = EkycWorkbookCatalog.filteredLoad().stream()
                .filter(testCase -> FOCUSED_FAMILIES.contains(testCase.family()))
                .filter(this::isFocusedSimpleDataCase)
                .toList();
        Object[][] rows = new Object[cases.size()][1];
        for (int index = 0; index < cases.size(); index++) {
            rows[index][0] = cases.get(index);
        }
        return rows;
    }

    @Test(dataProvider = "ekycReviewCases",
            groups = {"ekyc", "workbook", "review"},
            description = "EKYC-REVIEW: Approve va reject testcase tu workbook")
    public void runFocusedKycTestcase(EkycWorkbookCase testCase) {
        switch (testCase.family()) {
            case "REVIEW" -> runReviewCase(testCase);
            default -> throw new SkipException(testCase.id()
                    + ": Suite nay chi chay REVIEW case tu workbook.");
        }
    }
}
