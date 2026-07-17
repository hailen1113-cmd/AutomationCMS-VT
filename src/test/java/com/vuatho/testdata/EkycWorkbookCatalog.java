package com.vuatho.testdata;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Đọc workbook eKYC, ánh xạ từng dòng thành test case và kiểm tra dữ liệu bắt buộc.
 */
public final class EkycWorkbookCatalog {
    private static final String DEFAULT_WORKBOOK =
            "C:\\Users\\Lenovo\\Downloads\\Testcase_eKYC_Senior_Automation_574_Cases_Chi_Tiet_7_Truong.xlsx";
    private static final String SHEET_NAME = "Tất_cả_testcase";

    /**
     * Khởi tạo EkycWorkbookCatalog với các phụ thuộc cần thiết.
     */
    private EkycWorkbookCatalog() {
    }

    /**
     * Thực hiện xử lý data provider rows trong luồng kiểm thử.
     * @return kết quả data provider rows sau khi xử lý
     */
    public static Object[][] dataProviderRows() {
        List<EkycWorkbookCase> cases = filteredLoad();
        Object[][] rows = new Object[cases.size()][1];
        for (int index = 0; index < cases.size(); index++) {
            rows[index][0] = cases.get(index);
        }
        return rows;
    }

    /**
     * Đọc  trong luồng kiểm thử.
     * @return kết quả load sau khi xử lý
     */
    public static List<EkycWorkbookCase> load() {
        Path workbookPath = workbookPath();
        if (!Files.isRegularFile(workbookPath)) {
            throw new IllegalStateException("Cannot find eKYC testcase workbook: " + workbookPath);
        }

        DataFormatter formatter = new DataFormatter();
        List<EkycWorkbookCase> cases = new ArrayList<>();
        try (InputStream input = Files.newInputStream(workbookPath);
             Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw new IllegalStateException("Workbook does not contain sheet: " + SHEET_NAME);
            }
            for (int rowIndex = 4; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                String id = cell(row, 0, formatter);
                if (id.isBlank() || "Mã testcase".equals(id)) {
                    continue;
                }
                cases.add(new EkycWorkbookCase(
                        id,
                        cell(row, 1, formatter),
                        cell(row, 2, formatter),
                        cell(row, 3, formatter),
                        cell(row, 4, formatter),
                        cell(row, 5, formatter),
                        cell(row, 6, formatter),
                        cell(row, 7, formatter),
                        cell(row, 8, formatter),
                        cell(row, 9, formatter),
                        cell(row, 10, formatter),
                        cell(row, 11, formatter),
                        cell(row, 12, formatter),
                        cell(row, 13, formatter),
                        cell(row, 14, formatter),
                        cell(row, 15, formatter),
                        cell(row, 16, formatter),
                        cell(row, 20, formatter)));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read eKYC testcase workbook: " + workbookPath, exception);
        }
        return cases;
    }

    /**
     * Thực hiện xử lý filtered load trong luồng kiểm thử.
     * @return kết quả filtered load sau khi xử lý
     */
    public static List<EkycWorkbookCase> filteredLoad() {
        List<EkycWorkbookCase> cases = load();
        String caseId = configured("ekyc.case.id", "EKYC_CASE_ID");
        String family = configured("ekyc.case.family", "EKYC_CASE_FAMILY");
        String runGroup = configured("ekyc.run.group", "EKYC_RUN_GROUP");
        return cases.stream()
                .filter(testCase -> caseId.isBlank() || testCase.id().equalsIgnoreCase(caseId))
                .filter(testCase -> family.isBlank() || testCase.family().equalsIgnoreCase(family))
                .filter(testCase -> runGroup.isBlank() || testCase.runGroup().equalsIgnoreCase(runGroup))
                .toList();
    }

    /**
     * Thực hiện xử lý workbook path trong luồng kiểm thử.
     * @return kết quả workbook path sau khi xử lý
     */
    private static Path workbookPath() {
        String configured = configured("ekyc.testcase.workbook", "EKYC_TESTCASE_WORKBOOK");
        if (configured == null || configured.isBlank()) {
            configured = DEFAULT_WORKBOOK;
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    /**
     * Thực hiện xử lý configured trong luồng kiểm thử.
     * @param property giá trị property được truyền vào
     * @param environment giá trị environment được truyền vào
     * @return kết quả configured sau khi xử lý
     */
    private static String configured(String property, String environment) {
        String configured = System.getProperty(property);
        if (configured == null || configured.isBlank() || configured.startsWith("${")) {
            configured = System.getenv(environment);
        }
        return configured == null ? "" : configured.trim();
    }

    /**
     * Thực hiện xử lý cell trong luồng kiểm thử.
     * @param row giá trị row được truyền vào
     * @param index giá trị index được truyền vào
     * @param formatter giá trị formatter được truyền vào
     * @return kết quả cell sau khi xử lý
     */
    private static String cell(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).replaceAll("\\s+", " ").trim();
    }
}
