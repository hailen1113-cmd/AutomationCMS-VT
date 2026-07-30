package com.vuatho.tests.uniform.catalog;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * CRUD end-to-end cho hai loại dữ liệu của menu Quản lí Đồng phục.
 *
 * <p>Mỗi testcase tự tạo dữ liệu prefix AUTO-, xác minh qua UI và cleanup trong
 * {@code finally}; không phụ thuộc dữ liệu sandbox có sẵn.</p>
 */
public class UniformCatalogCrudTest extends UniformModuleTestSupport {
    private static final String GROUP_TAB = "Nhóm Đồng Phục";
    private static final String UNIFORM_TAB = "Đồng Phục";
    private static final String CREATED_PRICE = "150000";
    private static final String UPDATED_PRICE = "175000";

    public static void main(String[] args) {
        TestNgRunner.run(
                UniformCatalogCrudTest.class,
                "Đồng phục",
                "CRUD danh mục đồng phục");
    }

    @Test(
            groups = {"uniform", "catalog", "crud", "mutation", "critical",
                    "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_019)
    public void groupCanBeCreatedReadUpdatedAndDeleted() {
        String originalName = uniqueName("AUTO-GROUP");
        String updatedName = originalName + "-UPDATED";

        try {
            catalogPage.createMinimalItem(GROUP_TAB, originalName, CREATED_PRICE);
            Assert.assertTrue(catalogPage.itemExists(GROUP_TAB, originalName),
                    "Không đọc lại được nhóm vừa tạo: " + originalName);
            assertPersistedDetail(GROUP_TAB, originalName, CREATED_PRICE);

            catalogPage.updateItem(
                    GROUP_TAB, originalName, updatedName, UPDATED_PRICE);
            Assert.assertTrue(catalogPage.itemExists(GROUP_TAB, updatedName),
                    "Tên nhóm chưa được cập nhật: " + updatedName);
            Assert.assertFalse(catalogPage.itemExists(GROUP_TAB, originalName),
                    "Tên nhóm cũ vẫn còn sau update: " + originalName);
            assertPersistedDetail(GROUP_TAB, updatedName, UPDATED_PRICE);

            catalogPage.deleteItem(GROUP_TAB, updatedName);
            Assert.assertFalse(catalogPage.itemExists(GROUP_TAB, updatedName),
                    "Nhóm vẫn còn sau delete: " + updatedName);
        } finally {
            catalogPage.deleteAutomationItemIfPresent(GROUP_TAB, updatedName);
            catalogPage.deleteAutomationItemIfPresent(GROUP_TAB, originalName);
        }
    }

    @Test(
            groups = {"uniform", "catalog", "crud", "mutation", "critical",
                    "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_020)
    public void uniformCanBeCreatedReadUpdatedAndDeleted() {
        String originalName = uniqueName("AUTO-UNIFORM");
        String updatedName = originalName + "-UPDATED";

        try {
            catalogPage.createMinimalItem(
                    UNIFORM_TAB, originalName, CREATED_PRICE);
            Assert.assertTrue(catalogPage.itemExists(UNIFORM_TAB, originalName),
                    "Không đọc lại được đồng phục vừa tạo: " + originalName);
            assertPersistedDetail(UNIFORM_TAB, originalName, CREATED_PRICE);

            catalogPage.updateItem(
                    UNIFORM_TAB, originalName, updatedName, UPDATED_PRICE);
            Assert.assertTrue(catalogPage.itemExists(UNIFORM_TAB, updatedName),
                    "Tên đồng phục chưa được cập nhật: " + updatedName);
            Assert.assertFalse(catalogPage.itemExists(UNIFORM_TAB, originalName),
                    "Tên đồng phục cũ vẫn còn sau update: " + originalName);
            assertPersistedDetail(UNIFORM_TAB, updatedName, UPDATED_PRICE);

            catalogPage.deleteItem(UNIFORM_TAB, updatedName);
            Assert.assertFalse(catalogPage.itemExists(UNIFORM_TAB, updatedName),
                    "Đồng phục vẫn còn sau delete: " + updatedName);
        } finally {
            catalogPage.deleteAutomationItemIfPresent(UNIFORM_TAB, updatedName);
            catalogPage.deleteAutomationItemIfPresent(UNIFORM_TAB, originalName);
        }
    }

    private String uniqueName(String prefix) {
        String suffix = Long.toUnsignedString(System.nanoTime(), 36).toUpperCase();
        return prefix + "-" + suffix;
    }

    private void assertPersistedDetail(
            String tab, String expectedName, String expectedPrice) {
        String detail = catalogPage.readItemDetail(tab, expectedName);
        Assert.assertTrue(detail.contains(expectedName),
                "Drawer chi tiết không lưu tên: " + expectedName);
        String digits = detail.replaceAll("[^0-9]", "");
        Assert.assertTrue(digits.contains(expectedPrice),
                "Drawer chi tiết không lưu giá " + expectedPrice
                        + " | actual=" + detail);
        catalogPage.closeDrawer();
    }
}
