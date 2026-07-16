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
        ConsoleEncoding.useUtf8();
        RESULTS.clear();
    }
    @Override
    public void onTestStart(ITestResult result) {
        System.out.printf("%n[DANG CHAY] %s%n", TestResultFormatter.consoleDisplayName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        RESULTS.add(result);
        System.out.printf("[DAT]      %s (%s)%n", TestResultFormatter.consoleDisplayName(result),
                TestResultFormatter.duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        RESULTS.add(result);
        String message = result.getThrowable() == null
                ? "Khong co chi tiet loi"
                : result.getThrowable().getMessage();
        System.out.printf("[LOI]      %s (%s)%n          %s%n",
                TestResultFormatter.consoleDisplayName(result),
                TestResultFormatter.duration(result),
                TestResultFormatter.consoleMessage(message));
        if (TestConfig.captureScreenshots()) {
            System.out.printf("          Anh loi: %s%n",
                    ScreenshotManager.latestFor(result.getMethod().getMethodName()));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        RESULTS.add(result);
        System.out.printf("[BO QUA]   %s%n", TestResultFormatter.consoleDisplayName(result));
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        int total = passed + failed + skipped;

        System.out.println("\n==================================================");
        System.out.printf("TONG KET TEST: TONG=%d | DAT=%d | LOI=%d | BO QUA=%d%n",
                total, passed, failed, skipped);
        System.out.println("Bao cao HTML: "
                + Path.of("test-output", "index.html").toAbsolutePath());
        System.out.println("==================================================");
    }

    @Override
    public void onExecutionFinish() {
        try {
            System.out.println("Bao cao tong ket: " + summaryReporter.write(RESULTS));
        } catch (IOException exception) {
            System.err.println("Khong tao duoc bao cao tong ket: " + exception.getMessage());
        }
    }

}

