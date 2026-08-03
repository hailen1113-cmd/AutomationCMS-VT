package com.vuatho.tests.uniform.inventory.stock;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformInventoryStockTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Testcase dữ liệu tổng quan và Lưới tháng của tab Tồn kho. */
public class StockOverviewTest
        extends UniformInventoryStockTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockOverviewTest.class,
                "Kho Đồng phục", "Tổng quan tab Tồn kho");
    }

    /** Route phải mở đúng Kho tổng và nút Tồn kho đang được chọn. */
    @Test(groups = {"uniform", "inventory", "stock", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_001)
    public void opensMainWarehouseStockTab() {
        var screen = inventoryPage.screenSnapshot();
        Assert.assertTrue(screen.url().contains("inventory-uniform?tab=main"));
        Assert.assertTrue(screen.mainWarehouseSelected(),
                "Tab Kho tổng chưa được chọn.");
        Assert.assertTrue(screen.stockSectionSelected(),
                "Mục Tồn kho chưa được chọn.");
    }

    /** Các điều khiển chỉ thuộc Tồn kho phải xuất hiện đầy đủ. */
    @Test(groups = {"uniform", "inventory", "stock", "ui", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_002)
    public void stockTabShowsMainControls() {
        var screen = inventoryPage.screenSnapshot();
        Assert.assertTrue(screen.searchInput(), "Thiếu ô Tìm mã lô.");
        Assert.assertTrue(screen.monthlyGridButton() && screen.listButton(),
                "Thiếu nút Lưới tháng hoặc Danh sách.");
        for (String expected : List.of(
                "tong ton kho", "nhap gan nhat", "xuat gan nhat",
                "phieu", "dieu chinh ton", "nhap kho")) {
            Assert.assertTrue(screen.normalizedContent().contains(expected),
                    "Thiếu điều khiển hoặc vùng dữ liệu: " + expected);
        }
    }

    /** Ba thẻ tổng quan phải trả số lượng và ngày tháng hợp lệ. */
    @Test(groups = {"uniform", "inventory", "stock", "summary", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_003)
    public void overviewCardsShowValidData() {
        var overview = inventoryPage.overviewSnapshot();
        Assert.assertTrue(overview.totalStock() >= 0,
                "Tổng tồn kho không hợp lệ.");
        Assert.assertTrue(overview.positiveLotCount() >= 0,
                "Số lô tồn kho không hợp lệ.");
        Assert.assertFalse(overview.latestImportDate().isBlank(),
                "Thẻ Nhập gần nhất thiếu ngày.");
        Assert.assertTrue(overview.latestImportQuantity() > 0,
                "Thẻ Nhập gần nhất thiếu số lượng.");
        Assert.assertFalse(overview.latestExportDate().isBlank(),
                "Thẻ Xuất gần nhất thiếu ngày.");
        Assert.assertTrue(overview.latestExportQuantity() > 0,
                "Thẻ Xuất gần nhất thiếu số lượng.");
    }

    /** Tổng số cái và số lô phải được tính đúng từ dữ liệu đang hiển thị. */
    @Test(groups = {"uniform", "inventory", "stock", "summary", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_004)
    public void summaryMatchesDisplayedStockRows() {
        var overview = inventoryPage.overviewSnapshot();
        var rows = inventoryPage.stockRows();
        Assert.assertFalse(rows.isEmpty(), "Không có dòng tồn kho để đối chiếu.");
        inventoryPage.observeStockTable(
                "Cuộn xuống đối chiếu tổng tồn kho với các dòng dữ liệu");
        int stockTotal = rows.stream().mapToInt(row -> row.stock()).sum();
        long positiveLots = rows.stream().filter(row -> row.stock() > 0).count();
        Assert.assertEquals(overview.totalStock(), stockTotal,
                "Tổng tồn kho không khớp tổng các dòng.");
        Assert.assertEquals((long) overview.positiveLotCount(), positiveLots,
                "Số lô trên thẻ không khớp số lô còn hàng.");
    }

    /** Danh sách cảnh báo phải đúng các lô còn từ 1 đến 10 sản phẩm. */
    @Test(groups = {"uniform", "inventory", "stock", "warning", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_005)
    public void lowStockWarningMatchesRows() {
        var overview = inventoryPage.overviewSnapshot();
        var rows = inventoryPage.stockRows();
        Map<String, com.vuatho.pages.UniformInventoryPage.StockRow> byCode =
                rows.stream().collect(Collectors.toMap(
                        row -> row.code(), Function.identity()));
        List<String> warningCodes = inventoryPage.lowStockCodes();
        inventoryPage.observeStockTable(
                "Cuộn xuống đối chiếu các mã lô sắp hết trong bảng tồn kho");
        Assert.assertEquals(warningCodes.size(), overview.lowStockCount(),
                "Số mã cảnh báo không khớp số lô sắp hết.");
        Assert.assertFalse(warningCodes.isEmpty(),
                "Không có lô sắp hết để kiểm tra dữ liệu.");
        for (String code : warningCodes) {
            Assert.assertTrue(byCode.containsKey(code),
                    "Mã cảnh báo không tồn tại trong bảng: " + code);
            int stock = byCode.get(code).stock();
            Assert.assertTrue(stock > 0 && stock <= 10,
                    "Lô cảnh báo không thuộc ngưỡng 1-10: " + code + "=" + stock);
        }
    }

    /** Lưới tháng phải có các cột tháng và ít nhất một biến động nhập/xuất. */
    @Test(groups = {"uniform", "inventory", "stock", "grid", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_006)
    public void monthlyGridShowsStockMovements() {
        var grid = inventoryPage.gridSnapshot();
        List<String> normalizedHeaders = grid.headers().stream()
                .map(com.vuatho.utils.TextNormalizer::normalize)
                .toList();
        Assert.assertTrue(normalizedHeaders.containsAll(List.of("san pham", "ton")),
                "Lưới tháng thiếu cột Sản phẩm hoặc Tồn.");
        Assert.assertFalse(grid.months().isEmpty(),
                "Lưới tháng không hiển thị cột tháng nào.");
        Assert.assertFalse(grid.rows().isEmpty(),
                "Lưới tháng không có dữ liệu lô.");
        Assert.assertTrue(grid.hasMovement(),
                "Không tìm thấy dữ liệu nhập/xuất theo tháng.");
    }
}
