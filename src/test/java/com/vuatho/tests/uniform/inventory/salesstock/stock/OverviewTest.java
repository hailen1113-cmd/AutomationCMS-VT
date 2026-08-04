package com.vuatho.tests.uniform.inventory.salesstock.stock;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Testcase tổng quan và cảnh báo của Kho bán hàng → Tồn kho. */
public class OverviewTest extends SalesStockTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(OverviewTest.class, "Kho bán hàng", "Tổng quan Tồn kho");
    }

    /** Xác nhận route, tab kho và mục nghiệp vụ đang được chọn đúng. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "overview"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_001)
    public void opensSalesWarehouseStockTab() {
        var screen = salesStockPage.observeSelectedStockTab();
        Assert.assertTrue(screen.url().contains("inventory-uniform?tab=sub"));
        Assert.assertTrue(screen.mainWarehouseSelected(), "Tab Kho bán hàng chưa được chọn.");
        Assert.assertTrue(screen.stockSectionSelected(), "Mục Tồn kho chưa được chọn.");
    }

    /** Kiểm tra các điều khiển chính mà người dùng có thể thao tác trong tab. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "overview", "ui"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_002)
    public void showsStockControls() {
        var screen = salesStockPage.observeStockControls();
        Assert.assertTrue(screen.searchInput(), "Thiếu ô Tìm mã lô.");
        Assert.assertTrue(screen.monthlyGridButton(), "Thiếu nút Lưới tháng.");
        Assert.assertTrue(screen.listButton(), "Thiếu nút Danh sách.");
        for (String text : List.of("tong ton kho", "phieu xuat hom nay", "lo sap het")) {
            Assert.assertTrue(screen.normalizedContent().contains(text), "Thiếu vùng dữ liệu: " + text);
        }
    }

    /** Hai bộ đếm phiếu xuất phải đọc được và không âm. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "summary", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_004)
    public void exportCardShowsValidCounts() {
        var overview = salesStockPage.salesOverviewSnapshot();
        Assert.assertTrue(overview.todayExportCount() >= 0, "Số phiếu xuất hôm nay không hợp lệ.");
        Assert.assertTrue(overview.monthExportCount() >= 0, "Số phiếu xuất tháng này không hợp lệ.");
        Assert.assertTrue(overview.todayExportCount() <= overview.monthExportCount(),
                "Số phiếu hôm nay lớn hơn tổng phiếu tháng này.");
    }

    /** Vùng cảnh báo có scrollbar phải cuộn được xuống cuối và trở lại đầu. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "warning", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_006)
    public void scrollsLowStockListDownAndBackUp() {
        Assert.assertTrue(salesStockPage.scrollLowStockListDownAndBackUp(),
                "Danh sách lô sắp hết không cuộn được dù có nhiều dữ liệu.");
    }

    /** Lưới tháng phải có cột nghiệp vụ, tháng và ít nhất một biến động. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "grid", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_007)
    public void monthlyGridShowsMovements() {
        var grid = salesStockPage.salesGridSnapshot();
        Assert.assertFalse(grid.rows().isEmpty(), "Lưới tháng không có dữ liệu lô.");
        Assert.assertFalse(grid.months().isEmpty(), "Lưới tháng không có cột tháng.");
        Assert.assertEquals(new HashSet<>(grid.months()).size(), grid.months().size(),
                "Có tiêu đề tháng trùng.");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        List<YearMonth> parsed = grid.months().stream()
                .map(value -> YearMonth.parse(value, formatter)).toList();
        for (int index = 1; index < parsed.size(); index++) {
            Assert.assertFalse(parsed.get(index).isBefore(parsed.get(index - 1)),
                    "Các cột tháng không tăng dần theo thời gian.");
        }
        Assert.assertTrue(grid.rows().stream().flatMap(row -> row.monthlyMovements().stream())
                        .anyMatch(value -> value.contains("+") || value.contains("−") || value.matches(".*-\\d+.*")),
                "Không có biến động nhập hoặc xuất theo tháng.");
        for (var row : grid.rows()) {
            for (String cell : row.monthlyMovements()) {
                String compact = cell.replaceAll("\\s+", "");
                boolean emptyMovement = compact.isBlank() || compact.equals("·") || compact.equals(".");
                boolean signedMovements = compact.matches("(?:[+−-]\\d+)+");
                Assert.assertTrue(emptyMovement || signedMovements,
                        "Ô biến động không đúng định dạng ở mã " + row.code() + ": " + cell);
            }
        }
    }

    /** Tổng trên thẻ phải được tính từ đúng toàn bộ dòng, kể cả mã lô tự động. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "summary", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_038)
    public void totalStockMatchesAllGridRows() {
        var overview = salesStockPage.salesOverviewSnapshot();
        var rows = salesStockPage.salesGridRows();
        Assert.assertTrue(overview.totalStock() >= 0, "Tổng tồn kho không hợp lệ.");
        Assert.assertFalse(rows.isEmpty(), "Không có dòng tồn kho để đối chiếu.");
        Assert.assertEquals(rows.stream().mapToInt(row -> row.stock()).sum(), overview.totalStock(),
                "Tổng tồn kho không bằng tổng cột Tồn trong bảng.");
        salesStockPage.observeStockTable("Cuộn quan sát dữ liệu dùng để tính tổng tồn kho");
    }

    /** Mỗi cảnh báo phải trỏ đúng một dòng bảng và khớp chính xác số tồn. */
    @Test(groups = {"uniform", "inventory", "sales-stock", "warning", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_040)
    public void lowStockEntriesMatchGridData() {
        var overview = salesStockPage.salesOverviewSnapshot();
        var entries = overview.lowStockEntries();
        Map<String, com.vuatho.pages.SalesStockPage.SalesGridRow> rows =
                salesStockPage.salesGridRows().stream().collect(Collectors.toMap(
                        row -> row.code(), Function.identity()));
        Assert.assertFalse(entries.isEmpty(), "Không có cảnh báo để đối chiếu.");
        Assert.assertEquals(entries.size(), overview.lowStockCount(),
                "Số dòng cảnh báo không khớp badge Lô sắp hết.");
        Assert.assertEquals(new HashSet<>(entries.stream().map(entry -> entry.code()).toList()).size(),
                entries.size(), "Danh sách cảnh báo có mã lô trùng.");
        for (var entry : entries) {
            Assert.assertTrue(entry.remaining() > 0 && entry.remaining() <= 10,
                    "Số tồn cảnh báo ngoài ngưỡng 1-10 ở mã " + entry.code());
            Assert.assertTrue(rows.containsKey(entry.code()),
                    "Mã cảnh báo không có trong bảng: " + entry.code());
            Assert.assertEquals(rows.get(entry.code()).stock(), entry.remaining(),
                    "Số tồn cảnh báo không khớp bảng ở mã " + entry.code());
        }
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "grid", "validation"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_042)
    public void everyMonthlyGridRowContainsValidBusinessData() {
        var snapshot = salesStockPage.salesGridSnapshot();
        Assert.assertFalse(snapshot.rows().isEmpty(), "Lưới tháng không có dữ liệu.");
        for (var row : snapshot.rows()) {
            Assert.assertFalse(row.code().isBlank(), "Dòng dữ liệu thiếu mã lô.");
            Assert.assertFalse(row.name().isBlank(), "Dòng " + row.code() + " thiếu tên sản phẩm.");
            Assert.assertTrue(row.stock() >= 0, "Dòng " + row.code() + " có tồn âm.");
            Assert.assertEquals(row.monthlyMovements().size(), snapshot.months().size(),
                    "Dòng " + row.code() + " thiếu ô dữ liệu tháng.");
        }
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "grid", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_045)
    public void scrollsToLastGridRowAndBackToFirst() {
        var result = salesStockPage.scrollGridToLastRowAndBack();
        Assert.assertTrue(result.rowCount() > 1, "Không đủ dòng để kiểm tra cuộn bảng.");
        Assert.assertTrue(result.reachedLastRow(), "Chưa cuộn đến dòng cuối.");
        Assert.assertTrue(result.returnedFirstRow(), "Chưa quay lại dòng đầu.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "grid", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_046)
    public void scrollsHorizontallyToLastMonthAndBack() {
        var result = salesStockPage.scrollGridHorizontallyAndBack();
        Assert.assertTrue(result.visibleMonthCount() > 0, "Bảng không có cột tháng.");
        if (result.overflowAvailable()) {
            Assert.assertTrue(result.rightPosition() > 0, "Có overflow nhưng chưa cuộn sang phải.");
        }
        Assert.assertEquals(result.returnedPosition(), 0L, "Chưa cuộn ngang trở lại cột đầu.");
    }
}
