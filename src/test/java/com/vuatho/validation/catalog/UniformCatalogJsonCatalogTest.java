package com.vuatho.validation.catalog;

import com.vuatho.testdata.UniformCatalogTestCaseCatalog;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog.Execution;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Kiểm tra JSON quản lý trước khi Selenium sử dụng dữ liệu. */
public class UniformCatalogJsonCatalogTest {

    @Test(description = "UNI-CAT-004 - Uniform Catalog JSON có đủ ID và execution hợp lệ")
    public void jsonContainsExpectedCatalog() {
        Set<String> expectedIds = IntStream.rangeClosed(1, 16)
                .mapToObj(number -> "UNIFORM-CATALOG-%03d".formatted(number))
                .collect(Collectors.toSet());

        Assert.assertEquals(
                UniformCatalogTestCaseCatalog.logicalCaseIds(), expectedIds);
        Assert.assertEquals(
                UniformCatalogTestCaseCatalog.all().size(),
                UniformCatalogTestCaseCatalog.EXPECTED_EXECUTIONS);
        Assert.assertTrue(
                UniformCatalogTestCaseCatalog.all().stream()
                        .allMatch(execution -> !execution.execution().isBlank()),
                "JSON có execution thiếu tên.");
    }

    @Test(description = "UNI-CAT-005 - Mọi testcase trong JSON có ít nhất một execution enabled")
    public void everyLogicalCaseHasEnabledExecution() {
        for (var testCase : UniformCatalogTestCaseCatalog.testCases()) {
            if (!testCase.enabled()) {
                continue;
            }
            Assert.assertTrue(UniformCatalogTestCaseCatalog.all().stream()
                            .filter(Execution::enabled)
                            .anyMatch(execution ->
                                    execution.id().equals(testCase.id())),
                    testCase.id() + " đang enabled nhưng không có execution enabled.");
        }
    }
}
