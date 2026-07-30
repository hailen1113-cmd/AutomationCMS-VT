package com.vuatho.tests.uniform.catalog.update;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformCatalogCrudTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra cập nhật thật tên và trạng thái danh mục Đồng phục. */
public class UniformCatalogUpdateTest extends UniformCatalogCrudTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformCatalogUpdateTest.class,
                "Đồng phục", "Cập nhật nhóm và sản phẩm Đồng phục");
    }

    /** Tạo nhóm chuẩn bị, đổi tên thật và xác minh tên mới được lưu. */
    @Test(groups = {"uniform", "catalog", "mutation", "edit", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_025)
    public void renameGroupPersists() {
        String oldName = uniqueCatalogName("AUTO-GROUP-EDIT");
        String newName = oldName + "-NEW";
        Assert.assertTrue(catalogPage.createGroup(oldName, "135000"),
                "Không tạo được nhóm chuẩn bị chỉnh sửa.");
        try {
            Assert.assertTrue(catalogPage.renameItem(
                            "Nhóm Đồng Phục", oldName, newName),
                    "Đổi tên nhóm nhưng danh sách không trả tên mới.");
        } finally {
            safeDeleteCatalogItem("Nhóm Đồng Phục", newName);
            safeDeleteCatalogItem("Nhóm Đồng Phục", oldName);
        }
    }

    /** Tạo đồng phục chuẩn bị, đổi tên thật và xác minh tên mới được lưu. */
    @Test(groups = {"uniform", "catalog", "mutation", "edit", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_026)
    public void renameUniformPersists() {
        String oldName = uniqueCatalogName("AUTO-UNIFORM-EDIT");
        String newName = oldName + "-NEW";
        Assert.assertTrue(catalogPage.createUniformWithoutVariant(oldName, "165000"),
                "Không tạo được đồng phục chuẩn bị chỉnh sửa.");
        try {
            Assert.assertTrue(catalogPage.renameItem("Đồng Phục", oldName, newName),
                    "Đổi tên đồng phục nhưng danh sách không trả tên mới.");
        } finally {
            safeDeleteCatalogItem("Đồng Phục", newName);
            safeDeleteCatalogItem("Đồng Phục", oldName);
        }
    }

    /** Bật/tắt trạng thái hết hàng rồi mở lại để xác minh dữ liệu được lưu. */
    @Test(groups = {"uniform", "catalog", "mutation", "status", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_031)
    public void groupOutOfStockStatusPersists() {
        String name = uniqueCatalogName("AUTO-GROUP-STATUS");
        Assert.assertTrue(catalogPage.createGroup(name, "155000"),
                "Không tạo được nhóm chuẩn bị đổi trạng thái.");
        try {
            Assert.assertTrue(catalogPage.toggleGroupOutOfStockPersists(name),
                    "Đổi trạng thái hết hàng nhưng mở lại không giữ giá trị.");
        } finally {
            safeDeleteCatalogItem("Nhóm Đồng Phục", name);
        }
    }
}
