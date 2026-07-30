package com.vuatho.tests.uniform.catalog.read;

import com.vuatho.testcases.UniformCatalogTestCases;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformCatalogPage.CatalogCard;
import com.vuatho.support.UniformModuleTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra dữ liệu tổng quan của hai tab danh mục Đồng phục. */
public class UniformCatalogOverviewTest extends UniformModuleTestSupport {
    private static final String GROUP_TAB = "Nhóm Đồng Phục";
    private static final String ITEM_TAB = "Đồng Phục";

    public static void main(String[] args) {
        TestNgRunner.run(UniformCatalogOverviewTest.class,
                "Đồng phục", "Tổng quan danh mục");
    }

    /** Tab Nhóm Đồng Phục phải trả card có giá và số đồng phục hợp lệ. */
    @Test(groups = {"uniform", "catalog", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_004)
    public void groupTabReturnsValidCardData() {
        assertTabReturnsData(
                GROUP_TAB, "Tìm kiếm nhóm đồng phục", "Số đồng phục");
    }

    /** Tab Đồng Phục phải trả card có giá và tồn kho hợp lệ. */
    @Test(groups = {"uniform", "catalog", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_066)
    public void itemTabReturnsValidCardData() {
        assertTabReturnsData(
                ITEM_TAB, "Tìm kiếm đồng phục", "Tồn kho");
    }

    /** Card nhóm phải có đủ tên, giá, số lượng, ảnh và người cập nhật. */
    @Test(groups = {"uniform", "catalog", "card", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_005)
    public void groupCardsContainCompleteBusinessData() {
        assertCardsContainCompleteBusinessData(GROUP_TAB);
    }

    /** Card đồng phục phải có đủ tên, giá, tồn kho, ảnh và người cập nhật. */
    @Test(groups = {"uniform", "catalog", "card", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_067)
    public void itemCardsContainCompleteBusinessData() {
        assertCardsContainCompleteBusinessData(ITEM_TAB);
    }

    /** Ảnh card nhóm phải tải thành công. */
    @Test(groups = {"uniform", "catalog", "card", "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_013)
    public void groupCardImagesLoadSuccessfully() {
        assertCardImagesLoad(GROUP_TAB);
    }

    /** Ảnh card đồng phục phải tải thành công. */
    @Test(groups = {"uniform", "catalog", "card", "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_068)
    public void itemCardImagesLoadSuccessfully() {
        assertCardImagesLoad(ITEM_TAB);
    }

    private void assertTabReturnsData(
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

    private void assertCardsContainCompleteBusinessData(String tab) {
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

    private void assertCardImagesLoad(String tab) {
        catalogPage.open().selectTab(tab);
        Assert.assertTrue(catalogPage.displayedCardImagesLoaded(),
                tab + " có ảnh card không tải được.");
    }
}
