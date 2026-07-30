package com.vuatho.tests.uniform.catalog;

import com.vuatho.testcases.UniformCatalogTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog.Execution;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra tìm kiếm, lọc tồn kho và reset của danh mục Đồng phục. */
public class UniformCatalogSearchFilterTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformCatalogSearchFilterTest.class,
                "Đồng phục", "Tìm kiếm và bộ lọc danh mục");
    }

    /** Từ khóa lấy động từ card đầu tiên phải trả lại đúng item. */
    @Test(dataProvider = "case002",
            groups = {"uniform", "catalog", "search", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_016)
    public void searchReturnsMatchingCards(Execution testCase) {
        String tab = testCase.tab();
        catalogPage.open().selectTab(tab);
        String keyword = catalogPage.firstItemName();
        Assert.assertFalse(keyword.isBlank(), "Không lấy được tên item để tìm kiếm.");

        catalogPage.search(keyword);
        List<String> result = catalogPage.displayedItemNames();
        Assert.assertFalse(result.isEmpty(), "Tìm kiếm không trả card dữ liệu.");
        String normalizedKeyword = TextNormalizer.normalize(keyword);
        Assert.assertTrue(result.stream().allMatch(name ->
                        TextNormalizer.normalize(name).contains(normalizedKeyword)),
                "Kết quả có item không khớp từ khóa " + keyword + ": " + result);
    }

    /** Mỗi trạng thái tồn phải thao tác được và reset phải phục hồi danh sách. */
    @Test(dataProvider = "case003",
            groups = {"uniform", "catalog", "filter", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_017)
    public void inventoryStatusFilterAndResetWork(Execution testCase) {
        String tab = testCase.tab();
        String status = testCase.filterStatus();
        catalogPage.open().selectTab(tab);
        int before = catalogPage.totalDisplayed();
        String popup = catalogPage.openFilter();
        Assert.assertTrue(popup.contains("Còn hàng") && popup.contains("Hết hàng"),
                "Bộ lọc " + tab + " thiếu trạng thái tồn kho.");

        catalogPage.chooseFilter(status);
        Assert.assertTrue(catalogPage.totalDisplayed() <= before,
                "Lọc " + status + " làm tăng tổng bản ghi bất thường.");

        catalogPage.reset();
        Assert.assertEquals(catalogPage.totalDisplayed(), before,
                "Reset không phục hồi tổng dữ liệu ban đầu.");
    }

    /** Tìm kiếm và lọc Còn hàng phải cùng giữ hiệu lực trên tập kết quả. */
    @Test(dataProvider = "case008",
            groups = {"uniform", "catalog", "search", "filter-combination",
                    "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_018)
    public void searchAndInventoryFilterWorkTogether(Execution testCase) {
        String tab = testCase.tab();
        catalogPage.open().selectTab(tab);
        String keyword = catalogPage.firstItemName();
        catalogPage.search(keyword).openFilter();
        catalogPage.chooseFilter(testCase.filterStatus());

        List<String> result = catalogPage.displayedItemNames();
        Assert.assertFalse(result.isEmpty(),
                "Tìm kiếm + lọc Còn hàng không trả dữ liệu cho " + keyword);
        String normalizedKeyword = TextNormalizer.normalize(keyword);
        Assert.assertTrue(result.stream().allMatch(name ->
                        TextNormalizer.normalize(name).contains(normalizedKeyword)),
                "Bộ lọc làm mất điều kiện tìm kiếm " + keyword + ": " + result);
    }

    @DataProvider(name = "case002")
    public Object[][] case002() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-002");
    }

    @DataProvider(name = "case003")
    public Object[][] case003() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-003");
    }

    @DataProvider(name = "case008")
    public Object[][] case008() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-008");
    }
}
