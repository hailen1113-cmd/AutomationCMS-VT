package com.vuatho.listeners;

import com.vuatho.config.TestConfig;
import com.vuatho.testcases.TestCaseCatalog;
import com.vuatho.testcases.TestCaseDefinition;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Chỉ cho phép chạy test đăng xuất khi cờ cấu hình tương ứng được bật.
 */
public class LogoutTestsEnabled implements IAnnotationTransformer {
    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        TestGroupConventionTransformer.applyGroups(
                annotation, testClass, testConstructor, testMethod);
        if (testMethod != null
                && testMethod.getDeclaringClass().getPackageName()
                .startsWith("com.vuatho.tests.")) {
            TestCaseDefinition testCase = TestCaseCatalog
                    .findByMethod(testMethod.getDeclaringClass(), testMethod.getName())
                    .orElseThrow(() -> new IllegalStateException(
                            "Missing TestCaseCatalog mapping for "
                                    + testMethod.getDeclaringClass().getName()
                                    + "#" + testMethod.getName()));
            annotation.setDescription(testCase.id() + " - " + testCase.scenario());
        }
        if (testMethod == null || TestConfig.runLogoutTests()) {
            return;
        }
        String methodName = testMethod.getName();
        if ("logoutSuccessfully".equals(methodName)
                || "dashboardCannotBeAccessedAfterLogout".equals(methodName)) {
            annotation.setEnabled(false);
        }
    }
}
