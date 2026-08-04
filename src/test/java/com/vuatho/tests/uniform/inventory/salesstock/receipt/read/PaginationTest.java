package com.vuatho.tests.uniform.inventory.salesstock.receipt.read;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockReceiptTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.IntStream;
import java.util.List;

/** Kiểm tra phân trang danh sách Phiếu Kho bán hàng. */
public class PaginationTest extends SalesStockReceiptTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(PaginationTest.class, "Kho bán hàng", "Phân trang tab Phiếu");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_060)
    public void firstPageShowsValidPaginationState() {
        var result = receiptPage.firstPagePagination();
        Assert.assertEquals(result.info().currentPage(), 1);
        Assert.assertTrue(result.info().totalPages() >= 1);
        Assert.assertTrue(result.info().totalItems() >= result.codes().size());
        Assert.assertFalse(result.codes().isEmpty());
        Assert.assertTrue(result.previousDisabled());
        Assert.assertEquals(result.nextDisabled(), result.info().totalPages() == 1);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_061)
    public void movesToNextPageAndRestoresFirstPage() {
        var result = receiptPage.nextAndPrevious();
        Assert.assertTrue(result.initial().totalPages() > 1, "Không đủ trang để kiểm tra.");
        Assert.assertFalse(result.secondCodes().isEmpty());
        Assert.assertNotEquals(result.secondCodes(), result.firstCodes(), "Trang 2 trùng dữ liệu trang 1.");
        Assert.assertEquals(result.restored().currentPage(), 1);
        Assert.assertEquals(result.restoredCodes(), result.firstCodes(), "Quay lại nhưng dữ liệu trang 1 thay đổi.");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_062)
    public void paginationKeepsSelectedReceiptFilter() {
        var result = receiptPage.paginateWhileFiltered();
        Assert.assertFalse(result.filter().isBlank(), "Không có bộ lọc đủ nhiều trang để kiểm tra.");
        Assert.assertEquals(result.secondPage().currentPage(), 2);
        Assert.assertEquals(result.selectedFilters(), List.of(result.filter()));
        Assert.assertFalse(result.secondRows().isEmpty());
        Assert.assertTrue(result.secondRows().stream()
                .allMatch(row -> row.normalizedType().contains(result.expectedType())));
        Assert.assertNotEquals(result.secondRows().stream().map(row -> row.code()).toList(), result.firstCodes());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_065)
    public void traversesEveryPageAndReturnsToFirstPage() {
        var result = receiptPage.paginateAllAndReturn();
        Assert.assertEquals(result.visitedPages(),
                IntStream.rangeClosed(1, result.initial().totalPages()).boxed().toList(),
                "Chưa duyệt đủ các trang phiếu theo đúng thứ tự.");
        Assert.assertEquals(result.allCodes().size(), result.initial().totalItems(),
                "Tổng mã phiếu duy nhất không khớp tổng số phiếu.");
        Assert.assertTrue(result.nextDisabledOnLast(), "Nút Sau chưa bị khóa ở trang cuối.");
        Assert.assertEquals(result.restored().currentPage(), 1, "Không quay về trang 1.");
        Assert.assertEquals(result.restoredCodes(), result.allCodes().subList(0, result.restoredCodes().size()),
                "Dữ liệu trang đầu thay đổi sau khi duyệt toàn bộ trang.");
        Assert.assertTrue(result.previousDisabledOnFirst(), "Nút Trước chưa bị khóa sau khi quay về trang 1.");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_066)
    public void changingFilterFromSecondPageReturnsToFirstPage() {
        var result = receiptPage.changeFilterFromSecondPage();
        Assert.assertFalse(result.sourceFilter().isBlank(), "Không có bộ lọc đủ nhiều trang để kiểm tra.");
        Assert.assertEquals(result.sourcePage(), 2);
        Assert.assertEquals(result.targetResult().pagination().currentPage(), 1,
                "Đổi bộ lọc từ trang 2 nhưng không quay về trang 1.");
        Assert.assertEquals(result.targetResult().selectedFilters(), List.of(result.targetFilter()));
        Assert.assertTrue(result.targetResult().rows().stream()
                        .allMatch(row -> row.normalizedType().contains(expectedType(result.targetFilter()))),
                "Dữ liệu sau đổi bộ lọc không đúng loại mới.");
    }

    private String expectedType(String filter) {
        return switch (filter) {
            case "Nhập từ kho tổng" -> "nhap chuyen kho";
            case "Xuất đơn" -> "xuat don";
            case "Xuất nhân sự" -> "xuat nhan su";
            default -> "";
        };
    }
}
