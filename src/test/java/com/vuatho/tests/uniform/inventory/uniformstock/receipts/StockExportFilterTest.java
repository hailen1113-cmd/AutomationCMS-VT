package com.vuatho.tests.uniform.inventory.uniformstock.receipts;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformInventoryStockExportPage;
import com.vuatho.support.UniformInventoryStockExportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Testcase lọc danh sách Phiếu theo từng loại nghiệp vụ. */
public class StockExportFilterTest
        extends UniformInventoryStockExportTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockExportFilterTest.class,
                "Kho Đồng phục", "Bộ lọc tab Phiếu xuất kho");
    }

    /** Nhập kho chỉ được trả phiếu có loại Nhập kho. */
    @Test(groups = {"uniform", "inventory", "stock-export", "filter",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_032)
    public void filtersImportStockExports() {
        assertFilter("Nhập kho", "nhap kho");
    }

    /** Chuyển sang bán được hiển thị bằng loại nghiệp vụ Xuất chuyển kho. */
    @Test(groups = {"uniform", "inventory", "stock-export", "filter",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_033)
    public void filtersWarehouseTransferStockExports() {
        assertFilter("Chuyển sang bán", "xuat chuyen kho");
    }

    /** Điều chỉnh tồn chỉ được trả các phiếu điều chỉnh tăng hoặc giảm. */
    @Test(groups = {"uniform", "inventory", "stock-export", "filter",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_034)
    public void filtersStockAdjustmentStockExports() {
        var result = stockExportPage.filter("Điều chỉnh tồn");
        Assert.assertTrue(result.selected(),
                "Bộ lọc Điều chỉnh tồn chưa được chọn.");
        Assert.assertTrue(!result.rows().isEmpty() || result.emptyState(),
                "Bộ lọc Điều chỉnh tồn không trả dữ liệu hoặc empty-state.");
        for (var row : result.rows()) {
            Assert.assertTrue(row.normalizedType().contains("dieu chinh"),
                    "Bộ lọc Điều chỉnh tồn trả sai loại: " + row.type());
        }
    }

    /** Chuyển liên tiếp không làm lẫn loại và Tất cả phải khôi phục tập trang đầu. */
    @Test(groups = {"uniform", "inventory", "stock-export", "filter", "reset",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_035)
    public void switchingFiltersAndAllRestoresRows() {
        var sequence = stockExportPage.cycleFiltersAndRestoreAll();
        Assert.assertFalse(sequence.initialCodes().isEmpty(),
                "Tất cả không có dữ liệu ban đầu.");
        Assert.assertEquals(sequence.filters().size(), 3,
                "Chưa thao tác đủ ba bộ lọc loại phiếu.");
        Assert.assertTrue(sequence.filters().stream()
                        .allMatch(UniformInventoryStockExportPage.StockExportFilterSnapshot::selected),
                "Có bộ lọc không được chọn sau thao tác.");
        Assert.assertTrue(sequence.restored().selected(),
                "Không chọn lại được Tất cả.");
        Assert.assertEquals(
                sequence.restored().rows().stream().map(row -> row.code()).toList(),
                sequence.initialCodes(),
                "Dữ liệu trang đầu không được khôi phục khi quay lại Tất cả.");
        Assert.assertEquals(sequence.restored().pagination(),
                sequence.initialPagination(),
                "Phân trang và tổng số phiếu không được khôi phục khi quay lại Tất cả.");
    }

    /** Mỗi lần đổi loại chỉ đúng một nút active và dữ liệu không bị lẫn loại cũ. */
    @Test(groups = {"uniform", "inventory", "stock-export", "filter",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_038)
    public void switchingTypesKeepsOnlyOneActiveFilter() {
        var sequence = stockExportPage.cycleFiltersAndRestoreAll();
        for (var result : sequence.filters()) {
            Assert.assertEquals(result.selectedFilters(), List.of(result.filter()),
                    "Không chỉ có bộ lọc " + result.filter() + " đang được chọn.");
            String expected = switch (result.filter()) {
                case "Nhập kho" -> "nhap kho";
                case "Chuyển sang bán" -> "xuat chuyen kho";
                default -> "dieu chinh";
            };
            Assert.assertTrue(result.rows().stream()
                            .allMatch(row -> row.normalizedType().contains(expected)),
                    "Dữ liệu còn lẫn loại cũ sau khi chọn " + result.filter());
        }
        Assert.assertEquals(sequence.restored().selectedFilters(), List.of("Tất cả"),
                "Quay lại Tất cả nhưng vẫn còn bộ lọc loại khác active.");
    }

    /** Ba loại nghiệp vụ phải phân hoạch đúng toàn bộ số phiếu. */
    @Test(groups = {"uniform", "inventory", "stock-export", "filter",
            "summary", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_039)
    public void filterTotalsMatchAllTotal() {
        var totals = stockExportPage.filterTotals();
        int classifiedTotal = totals.filters().stream()
                .mapToInt(result -> result.pagination().totalItems()).sum();
        Assert.assertEquals(classifiedTotal, totals.all().totalItems(),
                "Tổng Nhập kho, Chuyển sang bán và Điều chỉnh tồn không khớp Tất cả.");
    }

    /** Sang trang khi lọc phải giữ nguyên nút active và loại của từng dòng. */
    @Test(groups = {"uniform", "inventory", "stock-export", "filter",
            "pagination", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_040)
    public void paginationKeepsCurrentFilter() {
        var result = stockExportPage.paginateWhileFiltered();
        Assert.assertFalse(result.filter().isBlank(),
                "Không tìm thấy loại phiếu có nhiều trang để kiểm tra.");
        Assert.assertEquals(result.secondPage().currentPage(), 2,
                "Không sang được trang 2 khi đang lọc.");
        Assert.assertEquals(result.selectedFilters(), List.of(result.filter()),
                "Bộ lọc không được giữ sau khi sang trang.");
        Assert.assertFalse(result.secondRows().isEmpty(),
                "Trang 2 của bộ lọc không có dữ liệu.");
        Assert.assertTrue(result.secondRows().stream().allMatch(row ->
                        row.normalizedType().contains(result.expectedType())),
                "Trang 2 trả lẫn loại phiếu khác.");
        Assert.assertNotEquals(
                result.secondRows().stream().map(row -> row.code()).toList(),
                result.firstCodes(),
                "Sang trang nhưng danh sách mã phiếu không thay đổi.");
    }

    /** Đổi loại khi đang ở trang 2 phải tải trang đầu của loại mới. */
    @Test(groups = {"uniform", "inventory", "stock-export", "filter",
            "pagination", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_041)
    public void changingFilterFromLaterPageReturnsToFirstPage() {
        var result = stockExportPage.changeFilterFromLaterPage();
        Assert.assertEquals(result.sourcePage(), 2,
                "Chưa chuẩn bị được trạng thái ở trang 2.");
        Assert.assertFalse(result.targetFilter().isBlank(),
                "Không xác định được bộ lọc đích.");
        Assert.assertEquals(result.targetResult().pagination().currentPage(), 1,
                "Đổi bộ lọc nhưng không trở về trang 1.");
        Assert.assertEquals(result.targetResult().selectedFilters(),
                List.of(result.targetFilter()),
                "Bộ lọc đích không phải lựa chọn duy nhất.");
    }

    /** Bấm lại cùng bộ lọc không được làm mất, thêm hoặc nhân đôi phiếu. */
    @Test(groups = {"uniform", "inventory", "stock-export", "filter",
            "stability", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_042)
    public void selectingSameFilterAgainKeepsDataStable() {
        var result = stockExportPage.repeatSameFilter();
        List<String> firstCodes = result.first().rows().stream()
                .map(row -> row.code()).toList();
        List<String> secondCodes = result.second().rows().stream()
                .map(row -> row.code()).toList();
        Assert.assertFalse(firstCodes.isEmpty(),
                "Bộ lọc Nhập kho không có dữ liệu để kiểm tra.");
        Assert.assertEquals(secondCodes, firstCodes,
                "Dữ liệu thay đổi khi bấm lại cùng bộ lọc.");
        Assert.assertEquals(secondCodes.stream().distinct().count(),
                (long) secondCodes.size(),
                "Dữ liệu bị nhân đôi khi bấm lại cùng bộ lọc.");
        Assert.assertEquals(result.second().pagination(), result.first().pagination(),
                "Phân trang thay đổi khi bấm lại cùng bộ lọc.");
    }

    private void assertFilter(String filter, String expectedType) {
        var result = stockExportPage.filter(filter);
        Assert.assertTrue(result.selected(),
                "Bộ lọc " + filter + " chưa được chọn.");
        Assert.assertFalse(result.rows().isEmpty(),
                "Bộ lọc " + filter + " không trả dữ liệu để kiểm tra.");
        Assert.assertTrue(result.rows().stream()
                        .allMatch(row -> row.normalizedType().contains(expectedType)),
                "Bộ lọc " + filter + " trả lẫn loại phiếu khác.");
        Assert.assertTrue(result.rows().stream()
                        .allMatch(row -> !row.lotCodes().isEmpty()
                                && !row.quantities().isEmpty()),
                "Có phiếu lọc thiếu chi tiết lô hoặc số lượng.");
    }
}
