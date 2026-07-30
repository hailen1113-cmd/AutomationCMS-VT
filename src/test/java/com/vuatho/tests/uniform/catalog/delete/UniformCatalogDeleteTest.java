package com.vuatho.tests.uniform.catalog.delete;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformCatalogCrudTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra hủy xóa và xác nhận xóa thật dữ liệu danh mục Đồng phục. */
public class UniformCatalogDeleteTest extends UniformCatalogCrudTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformCatalogDeleteTest.class,
                "Đồng phục", "Xóa nhóm và sản phẩm Đồng phục");
    }

    /** Hủy popup xóa phải giữ nhóm, sau đó xác nhận phải xóa thật. */
    @Test(groups = {"uniform", "catalog", "mutation", "delete", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_027)
    public void cancelThenConfirmDeleteGroupWorks() {
        String name = uniqueCatalogName("AUTO-GROUP-DELETE");
        Assert.assertTrue(catalogPage.createGroup(name, "145000"),
                "Không tạo được nhóm chuẩn bị xóa.");
        try {
            Assert.assertTrue(catalogPage.cancelDeleteItem(
                            "Nhóm Đồng Phục", name),
                    "Hủy xóa nhưng nhóm đã biến mất.");
            Assert.assertTrue(catalogPage.deleteItem("Nhóm Đồng Phục", name),
                    "Xác nhận xóa nhưng nhóm vẫn còn.");
        } finally {
            safeDeleteCatalogItem("Nhóm Đồng Phục", name);
        }
    }

    /** Hủy popup xóa phải giữ đồng phục, sau đó xác nhận phải xóa thật. */
    @Test(groups = {"uniform", "catalog", "mutation", "delete", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_028)
    public void cancelThenConfirmDeleteUniformWorks() {
        String name = uniqueCatalogName("AUTO-UNIFORM-DELETE");
        Assert.assertTrue(catalogPage.createUniformWithoutVariant(name, "175000"),
                "Không tạo được đồng phục chuẩn bị xóa.");
        try {
            Assert.assertTrue(catalogPage.cancelDeleteItem("Đồng Phục", name),
                    "Hủy xóa nhưng đồng phục đã biến mất.");
            Assert.assertTrue(catalogPage.deleteItem("Đồng Phục", name),
                    "Xác nhận xóa nhưng đồng phục vẫn còn.");
        } finally {
            safeDeleteCatalogItem("Đồng Phục", name);
        }
    }
}
