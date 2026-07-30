package com.vuatho.reporting;

import com.vuatho.testcases.TestCaseCatalog;
import com.vuatho.testcases.TestCaseDefinition;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog.Execution;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Xuất kết quả Uniform Catalog thành CSV chi tiết và TXT tổng hợp.
 */
public final class UniformCatalogResultReporter implements IReporter {
    private static final Path REPORT_DIRECTORY = Path.of("target", "reports");
    private static final Path CSV = REPORT_DIRECTORY.resolve(
            "uniform-catalog-results.csv");
    private static final Path SUMMARY = REPORT_DIRECTORY.resolve(
            "uniform-catalog-summary.txt");
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    @Override
    public void generateReport(
            List<XmlSuite> xmlSuites,
            List<ISuite> suites,
            String outputDirectory) {
        List<ResultRow> rows = collect(suites);
        if (rows.isEmpty()) {
            return;
        }
        try {
            Files.createDirectories(REPORT_DIRECTORY);
            Files.writeString(CSV, csv(rows), StandardCharsets.UTF_8);
            Files.writeString(SUMMARY, summary(rows), StandardCharsets.UTF_8);
            System.out.println("Uniform Catalog CSV: " + CSV.toAbsolutePath());
            System.out.println("Uniform Catalog summary: " + SUMMARY.toAbsolutePath());
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Không xuất được báo cáo Uniform Catalog", exception);
        }
    }

    private static List<ResultRow> collect(List<ISuite> suites) {
        List<ITestResult> results = new ArrayList<>();
        for (ISuite suite : suites) {
            for (ISuiteResult suiteResult : suite.getResults().values()) {
                ITestContext context = suiteResult.getTestContext();
                results.addAll(context.getPassedTests().getAllResults());
                results.addAll(context.getFailedTests().getAllResults());
                results.addAll(context.getSkippedTests().getAllResults());
            }
        }
        return results.stream()
                .map(UniformCatalogResultReporter::row)
                .filter(row -> row != null)
                .sorted(Comparator.comparingLong(ResultRow::startedAt))
                .toList();
    }

    private static ResultRow row(ITestResult result) {
        Execution execution = null;
        for (Object parameter : result.getParameters()) {
            if (parameter instanceof Execution value) {
                execution = value;
                break;
            }
        }
        if (execution == null) {
            return managedCatalogRow(result);
        }
        String message = result.getThrowable() == null
                ? "" : result.getThrowable().getMessage();
        return new ResultRow(
                execution.id(),
                execution.title(),
                execution.execution(),
                execution.tab(),
                execution.priority(),
                String.join("|", execution.tags()),
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName(),
                status(result.getStatus()),
                Math.max(0, result.getEndMillis() - result.getStartMillis()),
                result.getStartMillis(),
                message == null ? "" : message);
    }

    private static ResultRow managedCatalogRow(ITestResult result) {
        Class<?> type = result.getTestClass().getRealClass();
        if (!type.getPackageName().startsWith(
                "com.vuatho.tests.uniform.catalog")) {
            return null;
        }
        TestCaseDefinition testCase = TestCaseCatalog
                .findByMethod(type, result.getMethod().getMethodName())
                .orElse(null);
        if (testCase == null) {
            return null;
        }
        String message = result.getThrowable() == null
                ? "" : result.getThrowable().getMessage();
        return new ResultRow(
                testCase.id(),
                testCase.scenario(),
                "crud-lifecycle",
                inferredTab(result.getMethod().getMethodName()),
                priority(testCase.severity()),
                String.join("|", testCase.groups()),
                type.getSimpleName(),
                result.getMethod().getMethodName(),
                status(result.getStatus()),
                Math.max(0, result.getEndMillis() - result.getStartMillis()),
                result.getStartMillis(),
                message == null ? "" : message);
    }

    private static String inferredTab(String methodName) {
        if (methodName.startsWith("group")) {
            return "Nhóm Đồng Phục";
        }
        if (methodName.startsWith("uniform")) {
            return "Đồng Phục";
        }
        return "";
    }

    private static String priority(String severity) {
        return switch (severity) {
            case "Critical", "High" -> "P0";
            case "Low" -> "P2";
            default -> "P1";
        };
    }

    private static String csv(List<ResultRow> rows) {
        StringBuilder result = new StringBuilder();
        result.append('\uFEFF');
        result.append("Timestamp,Test Case ID,Title,Execution,Tab,Priority,Tags,")
                .append("Class,Method,Status,DurationMs,Message\n");
        for (ResultRow row : rows) {
            result.append(csv(TIMESTAMP.format(Instant.ofEpochMilli(row.startedAt()))))
                    .append(',').append(csv(row.id()))
                    .append(',').append(csv(row.title()))
                    .append(',').append(csv(row.execution()))
                    .append(',').append(csv(row.tab()))
                    .append(',').append(csv(row.priority()))
                    .append(',').append(csv(row.tags()))
                    .append(',').append(csv(row.className()))
                    .append(',').append(csv(row.method()))
                    .append(',').append(csv(row.status()))
                    .append(',').append(row.durationMs())
                    .append(',').append(csv(row.message()))
                    .append('\n');
        }
        return result.toString();
    }

    private static String summary(List<ResultRow> rows) {
        long passed = count(rows, "PASS");
        long failed = count(rows, "FAIL");
        long skipped = count(rows, "SKIP");
        long duration = rows.stream().mapToLong(ResultRow::durationMs).sum();
        StringBuilder result = new StringBuilder("""
                UNIFORM CATALOG TEST SUMMARY
                ============================
                Generated: %s
                Total: %d
                Passed: %d
                Failed: %d
                Skipped: %d
                Total test duration: %.1f seconds
                """.formatted(
                TIMESTAMP.format(Instant.now()), rows.size(), passed, failed,
                skipped, duration / 1000.0));
        if (failed > 0) {
            result.append("\nFAILED\n------\n");
            rows.stream()
                    .filter(row -> row.status().equals("FAIL"))
                    .forEach(row -> result.append(row.id())
                            .append(" [").append(row.execution()).append("] ")
                            .append(row.message()).append('\n'));
        }
        return result.toString();
    }

    private static long count(List<ResultRow> rows, String status) {
        return rows.stream().filter(row -> row.status().equals(status)).count();
    }

    private static String status(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "PASS";
            case ITestResult.FAILURE -> "FAIL";
            case ITestResult.SKIP -> "SKIP";
            default -> "UNKNOWN";
        };
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private record ResultRow(
            String id,
            String title,
            String execution,
            String tab,
            String priority,
            String tags,
            String className,
            String method,
            String status,
            long durationMs,
            long startedAt,
            String message) {
    }
}
