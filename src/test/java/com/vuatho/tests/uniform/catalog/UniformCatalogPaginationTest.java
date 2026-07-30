package com.vuatho.tests.uniform.catalog;

import com.vuatho.testcases.UniformCatalogTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformCatalogPage.PaginationSnapshot;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog.Execution;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Kiểm tra tổng bản ghi và trạng thái điều hướng phân trang của hai tab
 * trong menu Quản lí Đồng phục.
 */
public class UniformCatalogPaginationTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformCatalogPaginationTest.class,
                "Đồng phục", "Phân trang danh mục");
    }

    /** Điều khiển trang phải thống nhất với tổng số card mà UI công bố. */
    @Test(dataProvider = "case009",
            groups = {"uniform", "catalog", "pagination", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_008)
    public void paginationMatchesDisplayedData(Execution testCase) {
        String tab = testCase.tab();
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

    @DataProvider(name = "case009")
    public Object[][] case009() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-009");
    }
}
