package com.vuatho.tests.uniform.catalog;

import com.vuatho.testcases.UniformCatalogTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformCatalogPage.CatalogCard;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog.Execution;
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
    @Test(dataProvider = "case001",
            groups = {"uniform", "catalog", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_006)
    public void eachCatalogTabReturnsData(Execution testCase) {
        String tab = testCase.tab();
        catalogPage.open().selectTab(tab);

        Assert.assertEquals(catalogPage.selectedTab(), tab);
        Assert.assertEquals(
                catalogPage.searchPlaceholder(), testCase.searchPlaceholder());
        Assert.assertTrue(catalogPage.totalDisplayed() > 0,
                tab + " không trả bản ghi.");
        Assert.assertTrue(catalogPage.mainText().contains("Giá bán"),
                tab + " thiếu giá bán trên card.");
        Assert.assertTrue(catalogPage.mainText().contains(testCase.quantityLabel()),
                tab + " thiếu trường " + testCase.quantityLabel() + ".");
    }

    /** Mỗi card phải có đủ tên, giá, số lượng/tồn kho, ảnh và người cập nhật. */
    @Test(dataProvider = "case007",
            groups = {"uniform", "catalog", "card", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_007)
    public void eachCardContainsCompleteBusinessData(Execution testCase) {
        String tab = testCase.tab();
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
            Assert.assertTrue(card.hasAuditInfo(),
                    card.name() + " thiếu thông tin người tạo/cập nhật.");
        }
    }

    @DataProvider(name = "case001")
    public Object[][] case001() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-001");
    }

    @DataProvider(name = "case007")
    public Object[][] case007() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-007");
    }
}
