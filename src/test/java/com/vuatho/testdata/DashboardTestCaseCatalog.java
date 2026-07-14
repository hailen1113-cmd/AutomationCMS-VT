package com.vuatho.testdata;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DashboardTestCaseCatalog {
    public static final String RESOURCE =
            "/testcases/Test_Cases_Dashboard_Vua_Tho_Full.xlsx";
    public static final String SHEET = "All_Test_Cases";
    public static final int EXPECTED_CASE_COUNT = 222;

    private static final List<DashboardTestCase> CASES = loadCases();

    private DashboardTestCaseCatalog() {
    }

    public static List<DashboardTestCase> all() {
        return CASES;
    }

    public static List<DashboardTestCase> directlyAutomatable() {
        return CASES.stream().filter(DashboardTestCase::isDirectlyAutomatable).toList();
    }

    public static Map<String, Long> countByModule() {
        Map<String, Long> counts = new LinkedHashMap<>();
        CASES.forEach(testCase -> counts.merge(testCase.module(), 1L,
                (currentCount, increment) -> currentCount + increment));
        return Collections.unmodifiableMap(counts);
    }

    public static Map<String, Long> countByAutomationFeasibility() {
        Map<String, Long> counts = new LinkedHashMap<>();
        CASES.forEach(testCase -> counts.merge(testCase.automationFeasibility(), 1L,
                (currentCount, increment) -> currentCount + increment));
        return Collections.unmodifiableMap(counts);
    }

    private static List<DashboardTestCase> loadCases() {
        try (InputStream input = DashboardTestCaseCatalog.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing dashboard test-case workbook: " + RESOURCE);
            }
            try (Workbook workbook = new XSSFWorkbook(input)) {
                Sheet sheet = workbook.getSheet(SHEET);
                if (sheet == null) {
                    throw new IllegalStateException("Missing workbook sheet: " + SHEET);
                }
                return Collections.unmodifiableList(readCases(sheet));
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read dashboard test-case workbook", exception);
        }
    }

    private static List<DashboardTestCase> readCases(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();
        int headerIndex = findHeaderRow(sheet, formatter);
        List<DashboardTestCase> result = new ArrayList<>();
        for (int index = headerIndex + 1; index <= sheet.getLastRowNum(); index++) {
            Row row = sheet.getRow(index);
            if (row == null || value(row, 0, formatter).isBlank()) {
                continue;
            }
            result.add(new DashboardTestCase(
                    value(row, 0, formatter), value(row, 1, formatter),
                    value(row, 2, formatter), value(row, 3, formatter),
                    value(row, 4, formatter), value(row, 5, formatter),
                    value(row, 6, formatter), value(row, 7, formatter),
                    value(row, 8, formatter), value(row, 9, formatter),
                    value(row, 10, formatter), value(row, 11, formatter),
                    value(row, 12, formatter), value(row, 13, formatter)));
        }
        return result;
    }

    private static int findHeaderRow(Sheet sheet, DataFormatter formatter) {
        for (Row row : sheet) {
            if ("Test Case ID".equals(value(row, 0, formatter))) {
                return row.getRowNum();
            }
        }
        throw new IllegalStateException("Cannot find 'Test Case ID' header in " + SHEET);
    }

    private static String value(Row row, int column, DataFormatter formatter) {
        if (row.getCell(column) == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(column)).strip();
    }
}
