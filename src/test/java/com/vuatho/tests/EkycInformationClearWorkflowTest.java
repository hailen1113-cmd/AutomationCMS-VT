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
 * Kiểm tra xóa dữ liệu từng trường eKYC và xác nhận trạng thái sau khi lưu.
 */
public class EkycInformationClearWorkflowTest extends EkycWorkflowTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(EkycInformationClearWorkflowTest.class,
                "Bo test clear thong tin eKYC ERP",
                "Kiem tra clear/delete thong tin eKYC");
    }

    /**
     * Thực hiện xử lý ekyc clear information cases trong luồng kiểm thử.
     * @return kết quả ekyc clear information cases sau khi xử lý
     */
    @DataProvider(name = "ekycClearInformationCases", parallel = false)
    public Object[][] ekycClearInformationCases() {
        List<EkycInformationCase> cases = EkycInformationTestData.cases().stream()
                .filter(testCase -> testCase.action() == EkycInformationAction.CLEAR)
                .filter(this::matchesConfiguredInformationCase)
                .toList();
        Object[][] rows = new Object[cases.size()][1];
        for (int index = 0; index < cases.size(); index++) {
            rows[index][0] = cases.get(index);
        }
        return rows;
    }

    /**
     * Thực thi test “EKYC-INFORMATION-CLEAR: Clear/delete thong tin eKYC” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param testCase test case đang thực thi
     */
    @Test(dataProvider = "ekycClearInformationCases",
            groups = {"ekyc", "information", "clear"},
            description = "EKYC-INFORMATION-CLEAR: Clear/delete thong tin eKYC")
    public void runFocusedKycInformationClearCase(EkycInformationCase testCase) {
        if (testCase.action() != EkycInformationAction.CLEAR) {
            throw new SkipException(testCase.id() + ": Case khong phai CLEAR.");
        }
        runClearInformationCase(testCase);
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
        return configuredFamily.isBlank() || "CLEAR".equalsIgnoreCase(configuredFamily);
    }
}
