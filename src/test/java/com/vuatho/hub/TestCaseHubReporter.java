package com.vuatho.hub;

import com.vuatho.config.TestConfig;
import com.vuatho.hub.TestCaseHubModels.CatalogEntry;
import com.vuatho.hub.TestCaseHubModels.HubCase;
import com.vuatho.hub.TestCaseHubModels.HubSnapshot;
import com.vuatho.hub.TestCaseHubModels.Implementation;
import com.vuatho.hub.TestCaseHubModels.RunResult;
import org.testng.ITestResult;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ghi báo cáo HTML/CSV: testcase, flow và kết quả. */
public final class TestCaseHubReporter {
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private TestCaseHubReporter() {}

    public static Path write(List<ITestResult> liveResults) throws IOException {
        HubSnapshot snapshot = build(liveResults);
        Path html = Path.of(TestConfig.summaryReportPath()).toAbsolutePath();
        Path csv = html.resolveSibling("testcase-report.csv");
        Files.createDirectories(html.getParent());
        Files.writeString(html, renderHtml(snapshot), StandardCharsets.UTF_8);
        Files.writeString(csv, renderCsv(snapshot), StandardCharsets.UTF_8);
        Path hubCopy = html.resolveSibling("testcase-hub.html");
        if (!html.equals(hubCopy)) {
            Files.writeString(hubCopy, renderHtml(snapshot), StandardCharsets.UTF_8);
        }
        return html;
    }

    public static Path writeLatestAndMaybeOpen() throws IOException {
        Path html = write(List.of());
        maybeOpen(html);
        return html;
    }

    static HubSnapshot build(List<ITestResult> liveResults) {
        Path sourceRoot = Path.of("src/test/java");
        TestCaseCatalogScanner scanner = new TestCaseCatalogScanner(sourceRoot);
        List<CatalogEntry> catalog = scanner.catalog();
        Map<String, CatalogEntry> byConst = scanner.catalogByConstantName();
        Map<String, Implementation> implementations = scanner.implementations(byConst);
        Map<String, RunResult> results = TestNgResultReader.fromLive(liveResults);
        if (results.isEmpty()) {
            results = TestNgResultReader.fromXml(TestNgResultReader.latestResultsXml());
        }

        List<HubCase> cases = new ArrayList<>();
        int implemented = 0;
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        int notRun = 0;
        String suiteName = "";
        for (CatalogEntry entry : catalog) {
            Implementation implementation = implementations.get(entry.id());
            RunResult result = results.get(entry.id());
            boolean hasImpl = implementation != null;
            if (hasImpl) {
                implemented++;
            }
            String status = result == null ? "CHUA_CHAY" : result.status();
            switch (status) {
                case "PASS" -> passed++;
                case "FAIL" -> failed++;
                case "SKIP" -> skipped++;
                default -> notRun++;
            }
            if (result != null && suiteName.isBlank()) {
                suiteName = result.suite();
            }
            String className = firstNonBlank(
                    result == null ? "" : result.className(),
                    hasImpl ? implementation.className() : "");
            String methodName = firstNonBlank(
                    result == null ? "" : result.methodName(),
                    hasImpl ? implementation.methodName() : "");
            String flowType = hasImpl ? implementation.flowType() : "Chưa gắn UI test";
            String flow = flowType + " · " + entry.scenario();
            if (result != null && !result.parameters().isBlank()) {
                flow += " [" + result.parameters() + "]";
            }
            String flowDetail = hasImpl && !implementation.flowNote().isBlank()
                    ? implementation.flowNote()
                    : entry.scenario();
            cases.add(new HubCase(
                    entry.id(),
                    entry.module(),
                    entry.scenario(),
                    flow,
                    flowDetail,
                    hasImpl,
                    className,
                    methodName,
                    status,
                    result == null ? "" : result.duration(),
                    result == null ? "" : result.error(),
                    result == null ? "" : result.suite()));
        }
        return new HubSnapshot(
                LocalDateTime.now().format(TIME),
                suiteName,
                catalog.size(),
                implemented,
                passed,
                failed,
                skipped,
                notRun,
                cases);
    }

    public static void maybeOpen(Path html) {
        if (!TestConfig.interactive() || TestConfig.headless()) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(html.toUri());
            }
        } catch (Exception ignored) {
            // Báo cáo vẫn nằm trên đĩa nếu máy không mở được trình duyệt.
        }
    }

    private static String renderHtml(HubSnapshot snapshot) throws IOException {
        String template;
        try (InputStream input = TestCaseHubReporter.class.getResourceAsStream("/testcase-hub/index.html")) {
            if (input == null) {
                throw new IllegalStateException("Thiếu template /testcase-hub/index.html");
            }
            template = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        return template.replace("__TESTCASE_HUB_DATA__", toJson(snapshot));
    }

    private static String renderCsv(HubSnapshot snapshot) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Module,Testcase,Flow test,Chi tiết flow,Kết quả,Thời gian,Class,Method,Ghi chú\n");
        for (HubCase item : snapshot.cases()) {
            csv.append(csvRow(item.id(), item.module(), item.testcase(), item.flow(),
                    item.flowDetail(), statusLabel(item.status()), item.duration(),
                    item.className(), item.methodName(), item.error()));
        }
        return csv.toString();
    }

    private static String csvRow(String... columns) {
        StringBuilder row = new StringBuilder();
        for (int index = 0; index < columns.length; index++) {
            if (index > 0) {
                row.append(',');
            }
            row.append('"').append(value(columns[index]).replace("\"", "\"\"")).append('"');
        }
        return row.append('\n').toString();
    }

    private static String toJson(HubSnapshot snapshot) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        field(json, "generatedAt", snapshot.generatedAt(), true);
        field(json, "suiteName", snapshot.suiteName(), true);
        json.append("\"catalogCount\":").append(snapshot.catalogCount()).append(',');
        json.append("\"implementedCount\":").append(snapshot.implementedCount()).append(',');
        json.append("\"passed\":").append(snapshot.passed()).append(',');
        json.append("\"failed\":").append(snapshot.failed()).append(',');
        json.append("\"skipped\":").append(snapshot.skipped()).append(',');
        json.append("\"notRun\":").append(snapshot.notRun()).append(',');
        json.append("\"cases\":[");
        for (int index = 0; index < snapshot.cases().size(); index++) {
            HubCase item = snapshot.cases().get(index);
            if (index > 0) {
                json.append(',');
            }
            json.append("{");
            field(json, "id", item.id(), true);
            field(json, "module", item.module(), true);
            field(json, "testcase", item.testcase(), true);
            field(json, "flow", item.flow(), true);
            field(json, "flowDetail", item.flowDetail(), true);
            json.append("\"implemented\":").append(item.implemented()).append(',');
            field(json, "className", item.className(), true);
            field(json, "methodName", item.methodName(), true);
            field(json, "status", item.status(), true);
            field(json, "duration", item.duration(), true);
            field(json, "error", item.error(), true);
            field(json, "suite", item.suite(), false);
            json.append("}");
        }
        json.append("]}");
        return json.toString();
    }

    private static void field(StringBuilder json, String name, String value, boolean comma) {
        json.append('"').append(name).append("\":").append(quote(value));
        if (comma) {
            json.append(',');
        }
    }

    private static String quote(String value) {
        String safe = value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("</", "<\\/");
        return "\"" + safe + "\"";
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "PASS" -> "Pass";
            case "FAIL" -> "Fail";
            case "SKIP" -> "Skip";
            default -> "Chưa chạy";
        };
    }

    private static String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? value(second) : first;
    }

    private static String value(String text) {
        return text == null ? "" : text;
    }
}
