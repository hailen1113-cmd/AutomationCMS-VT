package com.vuatho.tests.uniform.catalog.read;

import com.vuatho.testcases.UniformCatalogTestCases;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformCatalogPage.PaginationSnapshot;
import com.vuatho.pages.UniformCatalogPage.PaginationWindow;
import com.vuatho.support.UniformModuleTestSupport;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Kiểm tra tổng bản ghi và trạng thái điều hướng phân trang của hai tab
 * trong menu Quản lí Đồng phục.
 */
public class CatalogPaginationTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(CatalogPaginationTest.class,
                "Đồng phục", "Phân trang danh mục");
    }

    /** Điều khiển trang phải thống nhất với tổng số card mà UI công bố. */
    @Test(dataProvider = "tabs",
            groups = {"uniform", "catalog", "pagination", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_006)
    public void paginationMatchesDisplayedData(String tab) {
        catalogPage.open().selectTab(tab);
        int total = catalogPage.totalDisplayed();
        int visibleCards = catalogPage.displayedCards().size();
        PaginationSnapshot pagination = catalogPage.pagination();

        Assert.assertEquals(pagination.activePage(), 1,
                tab + " không khởi tạo ở trang đầu.");
        Assert.assertTrue(pagination.previousDisabled(),
                tab + " cho phép lùi khi đang ở trang đầu.");
        Assert.assertTrue(total >= visibleCards,
                tab + " hiển thị nhiều card hơn tổng bản ghi.");
        if (total == visibleCards) {
            Assert.assertEquals(pagination.pageCount(), 1);
            Assert.assertTrue(pagination.nextDisabled(),
                    tab + " vẫn cho sang trang dù toàn bộ dữ liệu ở trang 1.");
        } else {
            Assert.assertTrue(pagination.pageCount() > 1);
            Assert.assertFalse(pagination.nextDisabled(),
                    tab + " khóa sang trang dù còn dữ liệu.");
        }
    }

    /** Khi có nhiều trang, phải bấm Next/Previous thật và cửa sổ dữ liệu phải đổi. */
    @Test(dataProvider = "tabs",
            groups = {"uniform", "catalog", "pagination", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_014)
    public void nextAndPreviousPageChangeData(String tab) {
        catalogPage.open().selectTab(tab);
        PaginationSnapshot snapshot = catalogPage.pagination();
        if (snapshot.pageCount() <= 1 || snapshot.nextDisabled()) {
            throw new SkipException("[THIẾU DỮ LIỆU TEST] " + tab
                    + " hiện chỉ có một trang.");
        }

        PaginationWindow next = catalogPage.goToNextPage();
        Assert.assertEquals(next.afterPage(), next.beforePage() + 1,
                "Next không tăng đúng số trang.");
        Assert.assertNotEquals(next.afterNames(), next.beforeNames(),
                "Next không thay đổi tập card.");

        PaginationWindow previous = catalogPage.goToPreviousPage();
        Assert.assertEquals(previous.afterPage(), previous.beforePage() - 1,
                "Previous không giảm đúng số trang.");
        Assert.assertEquals(previous.afterNames(), next.beforeNames(),
                "Quay lại trang đầu không phục hồi tập card.");
    }

    @DataProvider(name = "tabs")
    public Object[][] tabs() {
        return new Object[][]{{"Nhóm Đồng Phục"}, {"Đồng Phục"}};
    }
}
