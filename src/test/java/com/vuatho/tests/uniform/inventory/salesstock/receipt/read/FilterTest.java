package com.vuatho.tests.uniform.inventory.salesstock.receipt.read;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.SalesStockReceiptPage;
import com.vuatho.support.SalesStockReceiptTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra các bộ lọc loại phiếu của Kho bán hàng. */
public class FilterTest extends SalesStockReceiptTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(FilterTest.class, "Kho bán hàng", "Bộ lọc tab Phiếu");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_054)
    public void filtersReceiptsImportedFromMainWarehouse() {
        assertFilter("Nhập từ kho tổng", "nhap chuyen kho", false);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_055)
    public void filtersOrderExportReceipts() {
        assertFilter("Xuất đơn", "xuat don", false);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_056)
    public void filtersStaffExportReceipts() {
        assertFilter("Xuất nhân sự", "xuat nhan su", false);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_057)
    public void filtersAdjustmentReceiptsOrShowsEmptyState() {
        assertFilter("Điều chỉnh tồn", "dieu chinh", true);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_058)
    public void switchingFiltersKeepsOneActiveAndAllRestoresRows() {
        var sequence = receiptPage.cycleFiltersAndRestoreAll();
        Assert.assertFalse(sequence.initialCodes().isEmpty());
        Assert.assertEquals(sequence.filters().size(), 4);
        for (var result : sequence.filters()) {
            Assert.assertEquals(result.selectedFilters(), List.of(result.filter()),
                    "Không chỉ có bộ lọc hiện tại được chọn: " + result.filter());
        }
        Assert.assertEquals(sequence.restored().selectedFilters(), List.of("Tất cả"));
        Assert.assertEquals(sequence.restored().rows().stream().map(row -> row.code()).toList(),
                sequence.initialCodes(), "Quay về Tất cả nhưng dữ liệu trang đầu không được khôi phục.");
        Assert.assertEquals(sequence.restored().pagination(), sequence.initialPage());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_059)
    public void filterTotalsMatchAllReceiptTotal() {
        var totals = receiptPage.filterTotals();
        int classified = totals.filters().stream()
                .map(SalesStockReceiptPage.FilterSnapshot::pagination)
                .mapToInt(SalesStockReceiptPage.PaginationInfo::totalItems).sum();
        Assert.assertEquals(classified, totals.all().totalItems(),
                "Tổng từng loại phiếu không khớp tổng Tất cả.");
    }

    private void assertFilter(String filter, String expectedType, boolean allowEmpty) {
        var result = receiptPage.filter(filter);
        Assert.assertTrue(result.selected(), "Bộ lọc chưa active: " + filter);
        Assert.assertEquals(result.selectedFilters(), List.of(filter));
        Assert.assertTrue(!result.rows().isEmpty() || allowEmpty && result.emptyState(),
                "Bộ lọc không trả dữ liệu hoặc trạng thái rỗng: " + filter);
        Assert.assertTrue(result.rows().stream()
                        .allMatch(row -> row.normalizedType().contains(expectedType)),
                "Bộ lọc trả lẫn loại phiếu khác: " + filter);
    }
}
