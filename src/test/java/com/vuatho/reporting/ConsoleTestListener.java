package com.vuatho.reporting;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;

public class ConsoleTestListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        System.out.printf("%n[RUNNING] %s%n", displayName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.printf("[PASS]    %s (%s)%n", displayName(result), duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String message = result.getThrowable() == null
                ? "Không có chi tiết lỗi"
                : result.getThrowable().getMessage();
        System.out.printf("[FAIL]    %s (%s)%n          %s%n",
                displayName(result), duration(result), message);
        System.out.printf("          Screenshot: %s%n",
                Path.of("target", "screenshots", result.getMethod().getMethodName() + ".png")
                        .toAbsolutePath());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.printf("[SKIP]    %s%n", displayName(result));
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        int total = passed + failed + skipped;

        System.out.println("\n==================================================");
        System.out.printf("TEST SUMMARY: TOTAL=%d | PASS=%d | FAIL=%d | SKIP=%d%n",
                total, passed, failed, skipped);
        System.out.println("HTML report: "
                + Path.of("test-output", "index.html").toAbsolutePath());
        System.out.println("==================================================");
    }

    private String displayName(ITestResult result) {
        String description = result.getMethod().getDescription();
        return description == null || description.isBlank()
                ? result.getMethod().getMethodName()
                : description;
    }

    private String duration(ITestResult result) {
        double seconds = (result.getEndMillis() - result.getStartMillis()) / 1000.0;
        return String.format("%.2fs", seconds);
    }
}
