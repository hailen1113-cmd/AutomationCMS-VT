package com.vuatho.core;

import com.vuatho.reporting.ConsoleTestListener;
import org.testng.TestNG;

public final class TestNgRunner {
    private TestNgRunner() {
    }

    public static void run(Class<?> testClass, String suiteName, String testName) {
        run(suiteName, testName, testClass);
    }

    public static void run(String suiteName, String testName, Class<?>... testClasses) {
        System.setProperty("headless", System.getProperty("headless", "false"));
        System.setProperty("interactive", System.getProperty("interactive", "true"));
        System.setProperty("pause.on.failure", System.getProperty("pause.on.failure", "false"));

        TestNG testNG = new TestNG();
        testNG.setDefaultSuiteName(suiteName);
        testNG.setDefaultTestName(testName);
        testNG.setTestClasses(testClasses);
        testNG.addListener(new ConsoleTestListener());
        testNG.run();

        if (testNG.hasFailure()) {
            throw new IllegalStateException("TestNG execution failed.");
        }
    }
}
