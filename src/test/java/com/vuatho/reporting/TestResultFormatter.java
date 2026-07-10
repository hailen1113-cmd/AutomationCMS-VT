package com.vuatho.reporting;

import org.testng.ITestResult;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class TestResultFormatter {
    private TestResultFormatter() {
    }

    public static String displayName(ITestResult result) {
        String description = result.getMethod().getDescription();
        String name = description == null || description.isBlank()
                ? result.getMethod().getMethodName()
                : description;
        if (result.getParameters() == null || result.getParameters().length == 0) {
            return name;
        }
        String parameters = Arrays.stream(result.getParameters())
                .filter(value -> value != null && !String.valueOf(value).isBlank())
                .map(String::valueOf)
                .collect(Collectors.joining(" > "));
        return parameters.isBlank() ? name : name + " [" + parameters + "]";
    }

    public static String duration(ITestResult result) {
        double seconds = (result.getEndMillis() - result.getStartMillis()) / 1000.0;
        return String.format("%.2fs", seconds);
    }

    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
