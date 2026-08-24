package com.vuatho.hub;

import com.vuatho.hub.TestCaseHubModels.RunResult;
import com.vuatho.reporting.TestResultFormatter;
import org.testng.ITestResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Đọc kết quả lần chạy từ ITestResult hoặc testng-results.xml. */
final class TestNgResultReader {
    private TestNgResultReader() {}

    static Map<String, RunResult> fromLive(List<ITestResult> results) {
        Map<String, RunResult> mapped = new LinkedHashMap<>();
        if (results == null) {
            return mapped;
        }
        for (ITestResult result : results) {
            TestResultFormatter.TestCaseDescriptor descriptor = TestResultFormatter.testCase(result);
            String error = "";
            if (result.getThrowable() != null && result.getThrowable().getMessage() != null) {
                error = result.getThrowable().getMessage().replaceAll("\\s+", " ").trim();
            }
            String parameters = "";
            if (result.getParameters() != null && result.getParameters().length > 0) {
                StringBuilder builder = new StringBuilder();
                for (Object parameter : result.getParameters()) {
                    if (parameter == null || String.valueOf(parameter).isBlank()) {
                        continue;
                    }
                    if (!builder.isEmpty()) {
                        builder.append(" > ");
                    }
                    builder.append(parameter);
                }
                parameters = builder.toString();
            }
            String suite = result.getTestContext() == null ? "" : result.getTestContext().getSuite().getName();
            mapped.put(descriptor.id(), new RunResult(
                    descriptor.id(),
                    status(result.getStatus()),
                    TestResultFormatter.duration(result),
                    error,
                    result.getTestClass().getRealClass().getSimpleName(),
                    result.getMethod().getMethodName(),
                    suite,
                    parameters));
        }
        return mapped;
    }

    static Map<String, RunResult> fromXml(Path xml) {
        Map<String, RunResult> mapped = new LinkedHashMap<>();
        if (xml == null || !Files.isRegularFile(xml)) {
            return mapped;
        }
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.toFile());
            document.getDocumentElement().normalize();
            String suite = firstAttr(document.getElementsByTagName("suite"), "name");
            NodeList methods = document.getElementsByTagName("test-method");
            for (int index = 0; index < methods.getLength(); index++) {
                Element method = (Element) methods.item(index);
                if ("true".equals(method.getAttribute("is-config"))) {
                    continue;
                }
                String description = method.getAttribute("description");
                if (description == null || description.isBlank()) {
                    continue;
                }
                TestResultFormatter.TestCaseDescriptor descriptor =
                        TestResultFormatter.testCase(description, method.getAttribute("name"));
                String error = exceptionMessage(method);
                long durationMs = parseLong(method.getAttribute("duration-ms"));
                mapped.put(descriptor.id(), new RunResult(
                        descriptor.id(),
                        xmlStatus(method.getAttribute("status")),
                        String.format("%.2fs", durationMs / 1000.0),
                        error,
                        classSimpleName(method),
                        method.getAttribute("name"),
                        suite,
                        ""));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Không đọc được " + xml, exception);
        }
        return mapped;
    }

    static Path latestResultsXml() {
        Path testOutput = Path.of("test-output/testng-results.xml");
        Path surefire = Path.of("target/surefire-reports/testng-results.xml");
        boolean hasOutput = Files.isRegularFile(testOutput);
        boolean hasSurefire = Files.isRegularFile(surefire);
        if (hasOutput && hasSurefire) {
            try {
                return Files.getLastModifiedTime(testOutput).compareTo(Files.getLastModifiedTime(surefire)) >= 0
                        ? testOutput : surefire;
            } catch (Exception ignored) {
                return testOutput;
            }
        }
        if (hasOutput) {
            return testOutput;
        }
        return hasSurefire ? surefire : null;
    }

    private static String status(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "PASS";
            case ITestResult.FAILURE -> "FAIL";
            default -> "SKIP";
        };
    }

    private static String xmlStatus(String status) {
        if ("PASS".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
            return "PASS";
        }
        if ("FAIL".equalsIgnoreCase(status) || "FAILURE".equalsIgnoreCase(status)) {
            return "FAIL";
        }
        return "SKIP";
    }

    private static String exceptionMessage(Element method) {
        NodeList exceptions = method.getElementsByTagName("message");
        if (exceptions.getLength() == 0) {
            return "";
        }
        String text = exceptions.item(0).getTextContent();
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }

    private static String classSimpleName(Element method) {
        Element parent = (Element) method.getParentNode();
        if (parent == null || !"class".equals(parent.getTagName())) {
            return "";
        }
        String name = parent.getAttribute("name");
        int lastDot = name.lastIndexOf('.');
        return lastDot < 0 ? name : name.substring(lastDot + 1);
    }

    private static String firstAttr(NodeList nodes, String attribute) {
        if (nodes.getLength() == 0) {
            return "";
        }
        return ((Element) nodes.item(0)).getAttribute(attribute);
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }
}
