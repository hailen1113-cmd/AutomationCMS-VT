package com.vuatho.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Applies project-wide module and execution-level groups without duplicating
 * group declarations across every test method.
 *
 * <p>This transformer only adds metadata. It does not change priorities,
 * dependencies, enabled flags, data providers, or the test implementation.</p>
 */
public final class TestGroupConventionTransformer implements IAnnotationTransformer {
    private static final String TEST_PACKAGE = "com.vuatho.tests.";

    private static final Set<String> SMOKE_CLASSES = Set.of(
            "com.vuatho.tests.smoke.ErpSandboxAccessSmokeTest",
            "com.vuatho.tests.dashboard.LoginDashboardSourceAccessTest",
            "com.vuatho.tests.crossmenu.CrossMenuSidebarNavigationTest");

    private static final Set<String> REGRESSION_EXCLUSIONS = Set.of(
            "com.vuatho.tests.dashboard.DashboardLogoutTest",
            "com.vuatho.tests.ekyc.api.EkycMutationApiContractTest",
            "com.vuatho.tests.ekyc.information.EkycInformationEditWorkflowTest",
            "com.vuatho.tests.ekyc.information.EkycInformationClearWorkflowTest",
            "com.vuatho.tests.ekyc.review.EkycReviewWorkflowTest",
            "com.vuatho.tests.uniform.catalog.UniformCatalogCrudTest",
            "com.vuatho.tests.customerworkerorder.CustomerWorkerOrderWorkflowTest");

    @Override
    public void transform(
            ITestAnnotation annotation,
            Class testClass,
            Constructor testConstructor,
            Method testMethod) {
        applyGroups(annotation, testClass, testConstructor, testMethod);
    }

    static void applyGroups(
            ITestAnnotation annotation,
            Class testClass,
            Constructor testConstructor,
            Method testMethod) {
        Class<?> declaringClass = resolveDeclaringClass(testClass, testConstructor, testMethod);
        if (declaringClass == null) {
            return;
        }

        LinkedHashSet<String> groups = new LinkedHashSet<>(
                groupsFor(declaringClass, annotation.getGroups()));
        annotation.setGroups(groups.toArray(String[]::new));
    }

    /**
     * Returns the same effective groups used by TestNG so reporting/catalog
     * code does not maintain a second classification rule.
     */
    public static Set<String> groupsFor(Class<?> declaringClass, String[] declaredGroups) {
        String className = declaringClass.getName();
        LinkedHashSet<String> groups = new LinkedHashSet<>(Arrays.asList(declaredGroups));
        addModuleAndFeatureGroups(groups, className);

        if (SMOKE_CLASSES.contains(className)) {
            groups.add("smoke");
        }
        if (isSafeRegressionClass(className)) {
            groups.add("regression");
        }
        return Set.copyOf(groups);
    }

    private static Class<?> resolveDeclaringClass(
            Class<?> testClass,
            Constructor<?> testConstructor,
            Method testMethod) {
        if (testMethod != null) {
            return testMethod.getDeclaringClass();
        }
        if (testConstructor != null) {
            return testConstructor.getDeclaringClass();
        }
        return testClass;
    }

    private static void addModuleAndFeatureGroups(Set<String> groups, String className) {
        if (className.startsWith(TEST_PACKAGE + "dashboard.")) {
            groups.add("dashboard");
            return;
        }
        if (className.startsWith(TEST_PACKAGE + "ekyc.")) {
            groups.add("ekyc");
            addFeatureFromSubpackage(groups, className, TEST_PACKAGE + "ekyc.");
            return;
        }
        if (className.startsWith(TEST_PACKAGE + "uniform.")) {
            groups.add("uniform");
            addFeatureFromSubpackage(groups, className, TEST_PACKAGE + "uniform.");
            return;
        }
        if (className.startsWith(TEST_PACKAGE + "customerworkerorder.")) {
            groups.add("customerworkerorder");
            groups.add("customer-worker-order");
        }
    }

    private static void addFeatureFromSubpackage(
            Set<String> groups,
            String className,
            String packagePrefix) {
        String remainder = className.substring(packagePrefix.length());
        int separator = remainder.indexOf('.');
        if (separator > 0) {
            groups.add(remainder.substring(0, separator));
        }
    }

    private static boolean isSafeRegressionClass(String className) {
        if (REGRESSION_EXCLUSIONS.contains(className)) {
            return false;
        }
        return className.startsWith(TEST_PACKAGE + "dashboard.")
                || className.startsWith(TEST_PACKAGE + "uniform.")
                || className.startsWith(TEST_PACKAGE + "customerworkerorder.")
                || className.startsWith(TEST_PACKAGE + "ekyc.api.");
    }
}
