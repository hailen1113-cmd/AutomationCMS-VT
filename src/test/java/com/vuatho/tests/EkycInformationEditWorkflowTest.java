package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.testdata.EkycInformationAction;
import com.vuatho.testdata.EkycInformationCase;
import com.vuatho.testdata.EkycInformationTestData;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Kiểm tra chỉnh sửa dữ liệu từng trường eKYC và xác nhận giá trị được lưu.
 */
public class EkycInformationEditWorkflowTest extends EkycWorkflowTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(EkycInformationEditWorkflowTest.class,
                "Bo test sua thong tin eKYC ERP",
                "Kiem tra edit thong tin eKYC");
    }

    /**
     * Thực hiện xử lý ekyc edit information cases trong luồng kiểm thử.
     * @return kết quả ekyc edit information cases sau khi xử lý
     */
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

    /**
     * Thực thi test “EKYC-INFORMATION-EDIT: Sua thong tin eKYC” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param testCase test case đang thực thi
     */
    @Test(dataProvider = "ekycEditInformationCases",
            groups = {"ekyc", "information", "edit"},
            description = "EKYC-INFORMATION-EDIT: Sua thong tin eKYC")
    public void runFocusedKycInformationEditCase(EkycInformationCase testCase) {
        if (testCase.action() != EkycInformationAction.EDIT) {
            throw new SkipException(testCase.id() + ": Case khong phai EDIT.");
        }
        runEditInformationCase(testCase);
    }

    /**
     * Thực hiện xử lý matches configured information case trong luồng kiểm thử.
     * @param testCase test case đang thực thi
     * @return kết quả matches configured information case sau khi xử lý
     */
    private boolean matchesConfiguredInformationCase(EkycInformationCase testCase) {
        String configuredCaseId = configured("ekyc.case.id", "EKYC_CASE_ID");
        if (!configuredCaseId.isBlank() && !testCase.id().equalsIgnoreCase(configuredCaseId)) {
            return false;
        }
        String configuredFamily = configured("ekyc.case.family", "EKYC_CASE_FAMILY");
        return configuredFamily.isBlank() || "EDIT".equalsIgnoreCase(configuredFamily);
    }
}
