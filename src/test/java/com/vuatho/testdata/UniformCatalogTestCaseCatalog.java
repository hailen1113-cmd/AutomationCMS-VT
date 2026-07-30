package com.vuatho.testdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Đọc file JSON quản lý và cấp dữ liệu chạy cho testcase Uniform Catalog.
 */
public final class UniformCatalogTestCaseCatalog {
    public static final String RESOURCE = "/testcases/uniform-catalog.json";
    public static final int EXPECTED_LOGICAL_CASES = 16;
    public static final int EXPECTED_EXECUTIONS = 32;

    private static final Catalog CATALOG = load();
    private static final List<Execution> ALL = flattenAndValidate(CATALOG);

    private UniformCatalogTestCaseCatalog() {
    }

    public static List<TestCase> testCases() {
        return CATALOG.testCases();
    }

    public static List<Execution> all() {
        return ALL;
    }

    public static List<Execution> enabled(String id) {
        return ALL.stream()
                .filter(execution -> execution.id().equals(id))
                .filter(Execution::enabled)
                .toList();
    }

    public static Object[][] dataProvider(String id) {
        return enabled(id).stream()
                .map(execution -> new Object[]{execution})
                .toArray(Object[][]::new);
    }

    public static Set<String> logicalCaseIds() {
        Set<String> ids = new LinkedHashSet<>();
        CATALOG.testCases().forEach(testCase -> ids.add(testCase.id()));
        return Collections.unmodifiableSet(ids);
    }

    private static Catalog load() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream input =
                     UniformCatalogTestCaseCatalog.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException(
                        "Thiếu JSON Uniform Catalog: " + RESOURCE);
            }
            Catalog catalog = mapper.readValue(input, Catalog.class);
            if (catalog.testCases() == null || catalog.testCases().isEmpty()) {
                throw new IllegalStateException(
                        "JSON Uniform Catalog không có testCases.");
            }
            return catalog;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Không đọc được JSON Uniform Catalog", exception);
        }
    }

    private static List<Execution> flattenAndValidate(Catalog catalog) {
        List<Execution> result = new ArrayList<>();
        Set<String> ids = new LinkedHashSet<>();
        Set<String> executionKeys = new LinkedHashSet<>();
        for (TestCase testCase : catalog.testCases()) {
            require(testCase.id(), "Test case thiếu id.");
            require(testCase.title(), testCase.id() + " thiếu title.");
            if (!ids.add(testCase.id())) {
                throw new IllegalStateException(
                        "Test Case ID bị trùng: " + testCase.id());
            }
            if (testCase.executions() == null || testCase.executions().isEmpty()) {
                throw new IllegalStateException(
                        testCase.id() + " không có execution.");
            }
            for (ExecutionData data : testCase.executions()) {
                require(data.name(), testCase.id() + " có execution thiếu name.");
                String key = testCase.id() + "::" + data.name();
                if (!executionKeys.add(key)) {
                    throw new IllegalStateException(
                            "Execution bị trùng: " + key);
                }
                result.add(new Execution(
                        testCase.id(),
                        testCase.title(),
                        testCase.enabled() && data.isEnabled(),
                        testCase.priority(),
                        safe(testCase.tags()),
                        data.name(),
                        data.tab(),
                        data.searchPlaceholder(),
                        data.quantityLabel(),
                        data.filterStatus(),
                        data.drawerTitle(),
                        safe(data.expectedUiLabels()),
                        safe(data.expectedValidation()),
                        safe(data.expectedVariantFields()),
                        data.notes()));
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    private static List<String> safe(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record Catalog(List<TestCase> testCases) {
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record TestCase(
            String id,
            String title,
            boolean enabled,
            String priority,
            List<String> tags,
            List<ExecutionData> executions) {
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public record ExecutionData(
            String name,
            @JsonProperty("enabled") Boolean active,
            String tab,
            String searchPlaceholder,
            String quantityLabel,
            String filterStatus,
            String drawerTitle,
            List<String> expectedUiLabels,
            List<String> expectedValidation,
            List<String> expectedVariantFields,
            String notes) {

        public boolean isEnabled() {
            return active == null || active;
        }

        public ExecutionData {
            tab = value(tab);
            searchPlaceholder = value(searchPlaceholder);
            quantityLabel = value(quantityLabel);
            filterStatus = value(filterStatus);
            drawerTitle = value(drawerTitle);
            notes = value(notes);
        }
    }

    public record Execution(
            String id,
            String title,
            boolean enabled,
            String priority,
            List<String> tags,
            String execution,
            String tab,
            String searchPlaceholder,
            String quantityLabel,
            String filterStatus,
            String drawerTitle,
            List<String> expectedUiLabels,
            List<String> expectedValidation,
            List<String> expectedVariantFields,
            String notes) {

        @Override
        public String toString() {
            return id + "[" + execution + "]";
        }
    }
}
