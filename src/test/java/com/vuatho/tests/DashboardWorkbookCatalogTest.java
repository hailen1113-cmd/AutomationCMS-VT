package com.vuatho.tests;

import com.vuatho.testdata.DashboardTestCase;
import com.vuatho.testdata.DashboardTestCaseCatalog;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kiểm tra workbook Dashboard được đọc đúng và catalog không thiếu dữ liệu bắt buộc.
 */
public class DashboardWorkbookCatalogTest {

    /**
     * Thực thi test “The supplied dashboard workbook contains exactly 222 unique test cases” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "The supplied dashboard workbook contains exactly 222 unique test cases")
    public void workbookContainsAllExpectedTestCases() {
        List<DashboardTestCase> cases = DashboardTestCaseCatalog.all();
        Set<String> ids = cases.stream().map(DashboardTestCase::id).collect(Collectors.toSet());

        Assert.assertEquals(cases.size(), DashboardTestCaseCatalog.EXPECTED_CASE_COUNT,
                "Unexpected number of dashboard test cases");
        Assert.assertEquals(ids.size(), cases.size(), "Dashboard test-case IDs must be unique");
        Assert.assertTrue(cases.stream().noneMatch(testCase -> testCase.module().isBlank()),
                "Every dashboard test case must have a module");
        Assert.assertTrue(cases.stream().noneMatch(testCase -> testCase.scenario().isBlank()),
                "Every dashboard test case must have a scenario");
        Assert.assertTrue(cases.stream().noneMatch(testCase -> testCase.expectedResult().isBlank()),
                "Every dashboard test case must have an expected result");
    }

    /**
     * Thực thi test “The dashboard automation feasibility inventory is explicit and stable” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(dependsOnMethods = "workbookContainsAllExpectedTestCases",
            description = "The dashboard automation feasibility inventory is explicit and stable")
    public void automationFeasibilityIsAccountedFor() {
        Map<String, Long> counts = DashboardTestCaseCatalog.countByAutomationFeasibility();

        Assert.assertEquals(counts.get("Automate"), Long.valueOf(143));
        Assert.assertEquals(counts.get("Need more DOM"), Long.valueOf(36));
        Assert.assertEquals(counts.get("Need API/Database"), Long.valueOf(28));
        Assert.assertEquals(counts.get("Need business requirement"), Long.valueOf(13));
        Assert.assertEquals(counts.get("Need API/Network simulation"), Long.valueOf(2));
        Assert.assertEquals(counts.values().stream().mapToLong(value -> value.longValue()).sum(),
                DashboardTestCaseCatalog.EXPECTED_CASE_COUNT);

        System.out.println("[DASHBOARD TEST CASES] modules="
                + DashboardTestCaseCatalog.countByModule());
        System.out.println("[DASHBOARD TEST CASES] automation=" + counts);
    }
}
