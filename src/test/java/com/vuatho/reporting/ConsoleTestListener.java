package com.vuatho.reporting;

import com.vuatho.config.TestConfig;
import org.testng.ITestContext;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsoleTestListener implements ITestListener, IExecutionListener {
    private static final List<ITestResult> RESULTS = new CopyOnWriteArrayList<>();
    private final HtmlSummaryReporter summaryReporter = new HtmlSummaryReporter();

    @Override
    public void onExecutionStart() {
        RESULTS.clear();
    }
    @Override
    public void onTestStart(ITestResult result) {
        System.out.printf("%n[RUNNING] %s%n", TestResultFormatter.displayName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        RESULTS.add(result);
        System.out.printf("[PASS]    %s (%s)%n", TestResultFormatter.displayName(result),
                TestResultFormatter.duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        RESULTS.add(result);
        String message = result.getThrowable() == null
                ? "Không có chi tiết lỗi"
                : result.getThrowable().getMessage();
        System.out.printf("[FAIL]    %s (%s)%n          %s%n",
                TestResultFormatter.displayName(result), TestResultFormatter.duration(result), message);
        if (TestConfig.captureScreenshots()) {
            System.out.printf("          Screenshot: %s%n",
                    ScreenshotManager.latestFor(result.getMethod().getMethodName()));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        RESULTS.add(result);
        System.out.printf("[SKIP]    %s%n", TestResultFormatter.displayName(result));
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

    @Override
    public void onExecutionFinish() {
        try {
            System.out.println("Summary report: " + summaryReporter.write(RESULTS));
        } catch (IOException exception) {
            System.err.println("Cannot create summary report: " + exception.getMessage());
        }
    }

}
