package com.vuatho.tests.transaction.all;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionHistoryTestSupport;
import com.vuatho.support.TransactionExportWorkbook;
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

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_075)
    public void gatewayFilteredExportContainsAllMatchingRows() {
        var result = transactionPage.exportFilteredGateway();
        var workbook = TransactionExportWorkbook.read(result.file());
        Assert.assertEquals(workbook.rows().size(), result.totalRows());
        Assert.assertTrue(workbook.values("Cổng thanh toán").stream()
                .allMatch(value -> TextNormalizer.normalize(value)
                        .equals(TextNormalizer.normalize(result.filterValue()))));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_076)
    public void dateFilteredExportContainsAllMatchingRows() {
        var result = transactionPage.exportFilteredDate();
        var workbook = TransactionExportWorkbook.read(result.file());
        Assert.assertEquals(workbook.rows().size(), result.totalRows());
        String date = result.date().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
        Assert.assertTrue(workbook.values("Ngày tạo").stream()
                .map(value -> value.replaceAll("[^0-9]", ""))
                .allMatch(value -> value.startsWith(date)));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_077)
    public void combinedFilteredExportContainsAllMatchingRows() {
        var result = transactionPage.exportCombinedFilters();
        var workbook = TransactionExportWorkbook.read(result.file());
        Assert.assertEquals(workbook.rows().size(), result.totalRows());
        Assert.assertTrue(workbook.values("Loại giao dịch").stream().allMatch(value ->
                TextNormalizer.normalize(value).equals(TextNormalizer.normalize(result.type()))));
        Assert.assertTrue(workbook.values("Trạng thái").stream().allMatch(value ->
                TextNormalizer.normalize(value).equals(TextNormalizer.normalize(result.status()))));
        Assert.assertTrue(workbook.values("Cổng thanh toán").stream().allMatch(value ->
                TextNormalizer.normalize(value).equals(TextNormalizer.normalize(result.gateway()))));
        String date = result.date().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
        Assert.assertTrue(workbook.values("Ngày tạo").stream()
                .map(value -> value.replaceAll("[^0-9]", ""))
                .allMatch(value -> value.startsWith(date)));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_078)
    public void exportsPendingMomoMatrixCell() { verifyMatrix("Đang chờ", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_079)
    public void exportsPendingPaypalMatrixCell() { verifyMatrix("Đang chờ", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_080)
    public void exportsPendingOnepayMatrixCell() { verifyMatrix("Đang chờ", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_081)
    public void exportsPendingBankingMatrixCell() { verifyMatrix("Đang chờ", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_082)
    public void exportsPendingNeoxMatrixCell() { verifyMatrix("Đang chờ", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_083)
    public void exportsSuccessMomoMatrixCell() { verifyMatrix("Thành công", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_084)
    public void exportsSuccessPaypalMatrixCell() { verifyMatrix("Thành công", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_085)
    public void exportsSuccessOnepayMatrixCell() { verifyMatrix("Thành công", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_086)
    public void exportsSuccessBankingMatrixCell() { verifyMatrix("Thành công", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_087)
    public void exportsSuccessNeoxMatrixCell() { verifyMatrix("Thành công", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_088)
    public void exportsFailedMomoMatrixCell() { verifyMatrix("Thất bại", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_089)
    public void exportsFailedPaypalMatrixCell() { verifyMatrix("Thất bại", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_090)
    public void exportsFailedOnepayMatrixCell() { verifyMatrix("Thất bại", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_091)
    public void exportsFailedBankingMatrixCell() { verifyMatrix("Thất bại", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_092)
    public void exportsFailedNeoxMatrixCell() { verifyMatrix("Thất bại", "NEOX"); }

    private void verifyMatrix(String status, String gateway) {
        var result = transactionPage.exportFilteredStatusAndGateway(status, gateway);
        var workbook = TransactionExportWorkbook.read(result.file());
        Assert.assertEquals(workbook.rows().size(), result.totalRows(),
                "File phải chứa toàn bộ dữ liệu phù hợp bộ lọc.");
        Assert.assertTrue(workbook.values("Trạng thái").stream().allMatch(value ->
                TextNormalizer.normalize(value).equals(TextNormalizer.normalize(status))),
                "File có trạng thái khác " + status);
        Assert.assertTrue(workbook.values("Cổng thanh toán").stream().allMatch(value ->
                TextNormalizer.normalize(value).equals(TextNormalizer.normalize(gateway))),
                "File có cổng thanh toán khác " + gateway);
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
