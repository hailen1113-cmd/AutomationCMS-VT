package com.vuatho.tests.uniform.catalog;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
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
    @Test(dataProvider = "tabs",
            groups = {"uniform", "catalog", "search", "data-interaction"},
            description = "UNIFORM-CATALOG-002: Tìm tên nhóm/sản phẩm trả đúng dữ liệu")
    public void searchReturnsMatchingCards(String tab) {
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
    @Test(dataProvider = "tabAndInventoryStatus",
            groups = {"uniform", "catalog", "filter", "data-interaction"},
            description = "UNIFORM-CATALOG-003: Lọc Còn hàng/Hết hàng và reset")
    public void inventoryStatusFilterAndResetWork(String tab, String status) {
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
    @Test(dataProvider = "tabs",
            groups = {"uniform", "catalog", "search", "filter-combination",
                    "data-interaction"},
            description = "UNIFORM-CATALOG-008: Kết hợp tìm kiếm và lọc tồn kho")
    public void searchAndInventoryFilterWorkTogether(String tab) {
        catalogPage.open().selectTab(tab);
        String keyword = catalogPage.firstItemName();
        catalogPage.search(keyword).openFilter();
        catalogPage.chooseFilter("Còn hàng");

        List<String> result = catalogPage.displayedItemNames();
        Assert.assertFalse(result.isEmpty(),
                "Tìm kiếm + lọc Còn hàng không trả dữ liệu cho " + keyword);
        String normalizedKeyword = TextNormalizer.normalize(keyword);
        Assert.assertTrue(result.stream().allMatch(name ->
                        TextNormalizer.normalize(name).contains(normalizedKeyword)),
                "Bộ lọc làm mất điều kiện tìm kiếm " + keyword + ": " + result);
    }

    @DataProvider(name = "tabs")
    public Object[][] tabs() {
        return new Object[][]{{"Nhóm Đồng Phục"}, {"Đồng Phục"}};
    }

    @DataProvider(name = "tabAndInventoryStatus")
    public Object[][] tabAndInventoryStatus() {
        return new Object[][]{
                {"Nhóm Đồng Phục", "Còn hàng"},
                {"Nhóm Đồng Phục", "Hết hàng"},
                {"Đồng Phục", "Còn hàng"},
                {"Đồng Phục", "Hết hàng"}
        };
    }
}
