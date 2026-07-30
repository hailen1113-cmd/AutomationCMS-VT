package com.vuatho.tests.conditions;

import com.vuatho.reporting.TestResultFormatter;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.List;

/**
 * Chỉ giữ testcase có ID trùng với {@code -DtestCaseId=...}.
 *
 * <p>ID được đọc từ phần đầu {@code @Test(description = "ID - Scenario")};
 * không dùng reflection để sinh danh sách và không thay đổi nội dung testcase.</p>
 */
public final class TestCaseIdFilter implements IMethodInterceptor {
    @Override
    public List<IMethodInstance> intercept(
            List<IMethodInstance> methods, ITestContext context) {
        String requestedId = System.getProperty("testCaseId", "").trim();
        if (requestedId.isBlank()) {
            return methods;
        }

        List<IMethodInstance> selected = methods.stream()
                .filter(method -> TestResultFormatter.testCase(
                                method.getMethod().getDescription(),
                                method.getMethod().getMethodName())
                        .id().equalsIgnoreCase(requestedId))
                .toList();
        if (selected.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không tìm thấy testcase có ID " + requestedId);
        }
        return selected;
    }
}
