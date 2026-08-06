package com.vuatho.tests.transaction.all;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionHistoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import com.vuatho.utils.TextNormalizer;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Kiểm tra file Excel của tab Tất cả và dữ liệu đã lọc. */
public class TransactionAllExportTest extends TransactionHistoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAllExportTest.class,
                "Lịch sử giao dịch", "Tab Tất cả - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_034)
    public void exportsAllTransactionsToARealFile() throws IOException {
        var result = transactionPage.exportAll();
        Assert.assertNotNull(result.file());
        Assert.assertTrue(Files.isRegularFile(result.file()));
        Assert.assertTrue(Files.size(result.file()) > 0);
        Assert.assertTrue(result.file().getFileName().toString().matches("(?i).+\\.(xlsx|xls|csv)$"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_035)
    public void exportedWorkbookContainsHeadersAndRows() {
        var result = transactionPage.exportAll();
        WorkbookSnapshot workbook = readWorkbook(result.file());
        String normalized = TextNormalizer.normalize(String.join(" ", workbook.cells()));
        Assert.assertTrue(normalized.contains("loai giao dich"));
        Assert.assertTrue(normalized.contains("trang thai"));
        Assert.assertTrue(normalized.contains("so tien"));
        Assert.assertTrue(normalized.contains("ngay tao"));
        Assert.assertTrue(workbook.rowCount() > 1);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_036)
    public void filteredExportContainsAllMatchingRows() {
        var result = transactionPage.exportFilteredStatus();
        WorkbookSnapshot workbook = readWorkbook(result.file());
        String normalized = TextNormalizer.normalize(String.join(" ", workbook.cells()));
        Assert.assertTrue(normalized.contains(TextNormalizer.normalize(result.filterValue())));
        Assert.assertTrue(workbook.rowCount() - 1 > result.visibleRows(),
                "Excel chỉ chứa dữ liệu của trang hiện tại thay vì toàn bộ dữ liệu phù hợp filter.");
    }

    private WorkbookSnapshot readWorkbook(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".csv")) {
            try {
                List<String> lines = Files.readAllLines(file);
                return new WorkbookSnapshot(lines.size(), lines);
            } catch (IOException exception) {
                throw new AssertionError("Không đọc được CSV " + file, exception);
            }
        }
        try (var input = Files.newInputStream(file);
             var workbook = WorkbookFactory.create(input)) {
            var sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            List<String> cells = new ArrayList<>();
            sheet.forEach(row -> row.forEach(cell -> cells.add(formatter.formatCellValue(cell))));
            return new WorkbookSnapshot(sheet.getLastRowNum() + 1, cells);
        } catch (IOException exception) {
            throw new AssertionError("Không đọc được Excel " + file, exception);
        }
    }

    private record WorkbookSnapshot(int rowCount, List<String> cells) {}
}
