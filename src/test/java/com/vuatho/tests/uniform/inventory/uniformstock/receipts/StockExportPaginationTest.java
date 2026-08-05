package com.vuatho.tests.uniform.inventory.uniformstock.receipts;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformInventoryStockExportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

/** Testcase phân trang của danh sách Phiếu. */
public class StockExportPaginationTest
        extends UniformInventoryStockExportTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockExportPaginationTest.class,
                "Kho Đồng phục", "Phân trang tab Phiếu xuất kho");
    }

    /** Trang đầu phải có tổng số hợp lệ, khóa Trước và bật Sau khi còn trang. */
    @Test(groups = {"uniform", "inventory", "stock-export", "pagination",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_036)
    public void firstPageShowsValidPaginationState() {
        var page = stockExportPage.firstPagePagination();
        Assert.assertEquals(page.info().currentPage(), 1,
                "Danh sách không bắt đầu ở trang 1.");
        Assert.assertTrue(page.info().totalPages() >= 1,
                "Tổng số trang không hợp lệ.");
        Assert.assertTrue(page.info().totalItems() >= page.codes().size(),
                "Tổng số phiếu nhỏ hơn số dòng đang hiển thị.");
        Assert.assertFalse(page.codes().isEmpty(),
                "Trang đầu không có phiếu.");
        Assert.assertTrue(page.previousDisabled(),
                "Nút Trước chưa bị khóa ở trang đầu.");
        Assert.assertEquals(page.nextDisabled(), page.info().totalPages() == 1,
                "Trạng thái nút Sau không khớp tổng số trang.");
    }

    /** Phải đi đủ mọi trang, không trùng mã và quay lại đúng dữ liệu trang đầu. */
    @Test(groups = {"uniform", "inventory", "stock-export", "pagination",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_EXPORT_037)
    public void traversesAllPagesAndReturnsToFirst() {
        var journey = stockExportPage.paginateAllAndReturn();
        Assert.assertEquals(journey.visitedPages(),
                IntStream.rangeClosed(1, journey.initial().totalPages())
                        .boxed().toList(),
                "Không đi qua đủ các trang theo đúng thứ tự.");
        Assert.assertEquals(journey.allCodes().size(),
                journey.initial().totalItems(),
                "Tổng mã phiếu duy nhất không khớp tổng số phiếu.");
        Assert.assertTrue(journey.nextDisabledOnLast(),
                "Nút Sau chưa bị khóa ở trang cuối.");
        Assert.assertEquals(journey.restored().currentPage(), 1,
                "Không quay lại được trang đầu.");
        Assert.assertEquals(journey.restoredCodes(),
                journey.allCodes().subList(0, journey.restoredCodes().size()),
                "Dữ liệu trang đầu thay đổi sau khi quay lại.");
        Assert.assertTrue(journey.previousDisabledAfterReturn(),
                "Nút Trước chưa bị khóa sau khi quay về trang đầu.");
    }
}
