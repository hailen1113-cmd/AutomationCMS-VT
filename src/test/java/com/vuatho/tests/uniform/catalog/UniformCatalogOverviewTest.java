package com.vuatho.tests.uniform.catalog;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformCatalogPage.CatalogCard;
import com.vuatho.support.UniformModuleTestSupport;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra dữ liệu tổng quan của hai tab danh mục Đồng phục. */
public class UniformCatalogOverviewTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformCatalogOverviewTest.class,
                "Đồng phục", "Tổng quan danh mục");
    }

    /** Danh sách hai tab phải trả card có giá và số lượng/tồn kho hợp lệ. */
    @Test(dataProvider = "catalogTabs",
            groups = {"uniform", "catalog", "data-interaction"},
            description = "UNIFORM-CATALOG-001: Hai tab danh mục trả dữ liệu card hợp lệ")
    public void eachCatalogTabReturnsData(
            String tab, String searchPlaceholder, String quantityLabel) {
        catalogPage.open().selectTab(tab);

        Assert.assertEquals(catalogPage.selectedTab(), tab);
        Assert.assertEquals(catalogPage.searchPlaceholder(), searchPlaceholder);
        Assert.assertTrue(catalogPage.totalDisplayed() > 0,
                tab + " không trả bản ghi.");
        Assert.assertTrue(catalogPage.mainText().contains("Giá bán"),
                tab + " thiếu giá bán trên card.");
        Assert.assertTrue(catalogPage.mainText().contains(quantityLabel),
                tab + " thiếu trường " + quantityLabel + ".");
    }

    /** Mỗi card phải có đủ tên, giá, số lượng/tồn kho, ảnh và người cập nhật. */
    @Test(dataProvider = "catalogTabs",
            groups = {"uniform", "catalog", "card", "data-interaction"},
            description = "UNIFORM-CATALOG-007: Card có đủ dữ liệu nghiệp vụ")
    public void eachCardContainsCompleteBusinessData(
            String tab, String ignoredPlaceholder, String ignoredQuantityLabel) {
        catalogPage.open().selectTab(tab);
        List<CatalogCard> cards = catalogPage.displayedCards();
        Assert.assertFalse(cards.isEmpty(), tab + " không có card để kiểm tra.");
        for (CatalogCard card : cards) {
            Assert.assertFalse(card.name().isBlank(),
                    tab + " có card thiếu tên.");
            Assert.assertTrue(card.price().matches("[\\d.,]+"),
                    card.name() + " có giá bán không hợp lệ: " + card.price());
            Assert.assertTrue(card.quantity().matches("[\\d.]+"),
                    card.name() + " có số lượng/tồn kho không hợp lệ: "
                            + card.quantity());
            Assert.assertTrue(card.hasImage(),
                    card.name() + " thiếu element ảnh.");
            Assert.assertTrue(card.hasUpdater(),
                    card.name() + " thiếu thông tin cập nhật.");
        }
    }

    @DataProvider(name = "catalogTabs")
    public Object[][] catalogTabs() {
        return new Object[][]{
                {"Nhóm Đồng Phục", "Tìm kiếm nhóm đồng phục", "Số đồng phục"},
                {"Đồng Phục", "Tìm kiếm đồng phục", "Tồn kho"}
        };
    }
}
