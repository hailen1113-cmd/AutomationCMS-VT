package com.vuatho.tests.conditions;

import com.vuatho.config.TestConfig;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class LogoutTestsEnabled implements IAnnotationTransformer {
    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
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
