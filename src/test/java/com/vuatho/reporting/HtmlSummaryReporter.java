package com.vuatho.reporting;

import com.vuatho.config.TestConfig;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public final class HtmlSummaryReporter {
    private static final Path REPORT_PATH = Path.of(TestConfig.summaryReportPath());

    public Path write(List<ITestResult> results) throws IOException {
        Path report = REPORT_PATH.toAbsolutePath();
        Files.createDirectories(report.getParent());
        Files.writeString(report, render(results), StandardCharsets.UTF_8);
        return report;
    }

    private String render(List<ITestResult> results) {
        long passed = count(results, ITestResult.SUCCESS);
        long failed = count(results, ITestResult.FAILURE);
        long skipped = count(results, ITestResult.SKIP);
        long elapsed = results.stream()
                .mapToLong(r -> Math.max(0, r.getEndMillis() - r.getStartMillis())).sum();
        StringBuilder rows = new StringBuilder();
        for (ITestResult result : results) {
            String status = status(result);
            String error = result.getThrowable() == null ? "" : result.getThrowable().getMessage();
            String screenshot = screenshotCell(result);
            rows.append("<tr class='").append(status.toLowerCase()).append("'><td>")
                    .append(status).append("</td><td>")
                    .append(TestResultFormatter.escapeHtml(TestResultFormatter.displayName(result)))
                    .append("</td><td>").append(TestResultFormatter.duration(result)).append("</td><td>")
                    .append(TestResultFormatter.escapeHtml(error)).append("</td><td>")
                    .append(screenshot).append("</td></tr>");
        }
        return "<!doctype html><html><head><meta charset='UTF-8'><title>Test Summary</title>"
                + "<style>body{font:14px Arial;margin:32px;color:#243043}.cards{display:flex;gap:12px;margin:20px 0}"
                + ".card{padding:14px 22px;border-radius:8px;background:#eef2f7}.pass{color:#16803c}.fail{color:#c62828}"
                + ".skip{color:#9a6700}table{border-collapse:collapse;width:100%}th,td{padding:10px;border-bottom:1px solid #ddd;text-align:left}</style>"
                + "</head><body><h1>Automation Test Summary</h1><p>Generated: " + LocalDateTime.now() + "</p>"
                + "<div class='cards'><div class='card'>Total: " + results.size() + "</div><div class='card pass'>Pass: "
                + passed + "</div><div class='card fail'>Fail: " + failed + "</div><div class='card skip'>Skip: "
                + skipped + "</div><div class='card'>Duration: " + String.format("%.2fs", elapsed / 1000.0)
                + "</div></div><table><thead><tr><th>Status</th><th>Test case</th><th>Duration</th><th>Error</th><th>Screenshot</th>"
                + "</tr></thead><tbody>" + rows + "</tbody></table></body></html>";
    }

    private long count(List<ITestResult> results, int status) {
        return results.stream().filter(result -> result.getStatus() == status).count();
    }

    private String status(ITestResult result) {
        return switch (result.getStatus()) {
            case ITestResult.SUCCESS -> "PASS";
            case ITestResult.FAILURE -> "FAIL";
            default -> "SKIP";
        };
    }

    private String screenshotCell(ITestResult result) {
        if (result.getStatus() != ITestResult.FAILURE || !TestConfig.captureScreenshots()) {
            return "";
        }
        Path screenshot = ScreenshotManager.latestFor(result.getMethod().getMethodName());
        if (!Files.exists(screenshot)) {
            return "";
        }
        return "<a href='" + screenshot.toUri() + "'>open</a>";
    }
}
