package com.vuatho.testcases;

import java.util.List;

/**
 * One catalog row linked directly to one business TestNG {@code @Test} method.
 */
public record TestCaseDefinition(
        String id,
        String module,
        String feature,
        String scenario,
        String severity,
        String testType,
        String className,
        String methodName,
        String automationStatus,
        List<String> groups) {

    public TestCaseDefinition {
        groups = List.copyOf(groups);
    }

    public String mappingKey() {
        return className + "#" + methodName;
    }
}
