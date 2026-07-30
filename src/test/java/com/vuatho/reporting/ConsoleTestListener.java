package com.vuatho.reporting;

import com.vuatho.config.TestConfig;
import org.testng.ITestContext;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * Lắng nghe vòng đời TestNG và in trạng thái bắt đầu, thành công, bỏ qua hoặc thất bại của từng test.
 */
public class ConsoleTestListener implements ITestListener, IExecutionListener {
    private static final String SEPARATOR =
            "============================================================";
    private static final List<ITestResult> RESULTS = new CopyOnWriteArrayList<>();
    private final HtmlSummaryReporter summaryReporter = new HtmlSummaryReporter();

    /**
     * Thực hiện xử lý on execution start trong luồng kiểm thử.
     */
    @Override
    public void onExecutionStart() {
        ConsoleEncoding.showOnlyTestReport();
        RESULTS.clear();
    }
    /**
     * Thực hiện xử lý on test start trong luồng kiểm thử.
     * @param result giá trị result được truyền vào
     */
    @Override
    public void onTestStart(ITestResult result) {
        TestResultFormatter.TestCaseDescriptor testCase =
                TestResultFormatter.testCase(result);
        System.out.println(SEPARATOR);
        System.out.println("RUNNING: " + testCase.id());
        System.out.println("Scenario: "
                + TestResultFormatter.consoleMessage(testCase.scenario()));
        System.out.println("Class   : " + result.getTestClass().getRealClass().getSimpleName());
        System.out.println("Method  : " + result.getMethod().getMethodName());
        System.out.println(SEPARATOR);
    }

    /**
     * Thực hiện xử lý on test success trong luồng kiểm thử.
     * @param result giá trị result được truyền vào
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        RESULTS.add(result);
        printStatus("PASS", result);
    }

    /**
     * Thực hiện xử lý on test failure trong luồng kiểm thử.
     * @param result giá trị result được truyền vào
     */
    @Override
    public void onTestFailure(ITestResult result) {
        RESULTS.add(result);
        printStatus("FAIL", result);
        System.out.println("Reason: " + failureReason(result));
        if (TestConfig.captureScreenshots()) {
            // ScreenshotManager vẫn quản lý bằng chứng lỗi; đường dẫn nằm trong HTML report.
            ScreenshotManager.latestFor(result.getMethod().getMethodName());
        }
    }

    /**
     * Thực hiện xử lý on test skipped trong luồng kiểm thử.
     * @param result giá trị result được truyền vào
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        RESULTS.add(result);
        printStatus("SKIP", result);
    }

    /**
     * Thực hiện xử lý on finish trong luồng kiểm thử.
     * @param context giá trị context được truyền vào
     */
    @Override
    public void onFinish(ITestContext context) {
        Map<String, Integer> logicalStatuses = new LinkedHashMap<>();
        Stream.of(
                        context.getPassedTests().getAllResults(),
                        context.getFailedTests().getAllResults(),
                        context.getSkippedTests().getAllResults())
                .flatMap(java.util.Collection::stream)
                .forEach(result -> {
                    String id = TestResultFormatter.testCase(result).id();
                    int status = result.getStatus();
                    logicalStatuses.merge(id, status,
                            ConsoleTestListener::strongerStatus);
                });

        long passed = logicalStatuses.values().stream()
                .filter(status -> status == ITestResult.SUCCESS)
                .count();
        long failed = logicalStatuses.values().stream()
                .filter(status -> status == ITestResult.FAILURE)
                .count();
        long skipped = logicalStatuses.size() - passed - failed;

        System.out.println("================ TEST SUMMARY ================");
        System.out.println("Total : " + logicalStatuses.size());
        System.out.println("PASS  : " + passed);
        System.out.println("FAIL  : " + failed);
        System.out.println("SKIP  : " + skipped);
        System.out.println("==============================================");
    }

    /**
     * Thực hiện xử lý on execution finish trong luồng kiểm thử.
     */
    @Override
    public void onExecutionFinish() {
        try {
            summaryReporter.write(RESULTS);
        } catch (IOException exception) {
            // Không in log phụ ra terminal theo chế độ chỉ hiển thị testcase đang chạy.
        }
    }

    private void printStatus(String status, ITestResult result) {
        TestResultFormatter.TestCaseDescriptor testCase =
                TestResultFormatter.testCase(result);
        System.out.printf("[%s] %s - %s%n",
                status,
                testCase.id(),
                TestResultFormatter.consoleMessage(testCase.scenario()));
    }

    private String failureReason(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable == null) {
            return "Không có thông tin nguyên nhân.";
        }
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            message = throwable.toString();
        }
        return TestResultFormatter.consoleMessage(
                message.replaceAll("\\s+", " ").trim());
    }

    private static int strongerStatus(int current, int candidate) {
        if (current == ITestResult.FAILURE || candidate == ITestResult.FAILURE) {
            return ITestResult.FAILURE;
        }
        if (current == ITestResult.SUCCESS || candidate == ITestResult.SUCCESS) {
            return ITestResult.SUCCESS;
        }
        return ITestResult.SKIP;
    }

}

