package com.vuatho.tests.uniform.catalog.delete;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformDeleteTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Testcase popup, hủy và xác nhận xóa thật Đồng Phục. */
public class ItemDeleteTest extends UniformDeleteTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(ItemDeleteTest.class,
                "Đồng phục", "Xóa Đồng Phục");
    }

    /** Hủy popup phải giữ đồng phục, sau đó xác nhận phải xóa thật bản ghi đó. */
    @Test(groups = {"uniform", "catalog", "item-delete",
            "mutation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_028)
    public void cancelThenConfirmDeleteUniformWorks() {
        String name = prepareItemForDelete();
        try {
            Assert.assertTrue(catalogPage.cancelDeleteItem(ITEM_TAB, name),
                    "Hủy xóa nhưng đồng phục đã biến mất.");
            Assert.assertTrue(catalogPage.deleteItem(ITEM_TAB, name),
                    "Xác nhận xóa nhưng đồng phục vẫn còn.");
        } finally {
            safeDeleteCatalogItem(ITEM_TAB, name);
        }
    }

    /** Popup phải có cảnh báo, nút Hủy và nút xác nhận; Hủy không làm mất dữ liệu. */
    @Test(groups = {"uniform", "catalog", "item-delete",
            "dialog", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_152)
    public void itemDeleteDialogContainsWarningAndActions() {
        String name = prepareItemForDelete();
        try {
            var dialog = catalogPage.inspectDeleteDialogAndCancel(
                    ITEM_TAB, name);
            Assert.assertTrue(dialog.content().contains("Xóa đồng phục"),
                    "Popup thiếu tiêu đề Xóa đồng phục.");
            Assert.assertTrue(dialog.content().contains(
                            "Bạn có chắc muốn xóa đồng phục?"),
                    "Popup thiếu câu cảnh báo xác nhận xóa đồng phục.");
            Assert.assertTrue(dialog.cancelButton() && dialog.confirmButton(),
                    "Popup xóa đồng phục thiếu nút Trở về hoặc Xác nhận.");
            Assert.assertTrue(dialog.closeButton(),
                    "Popup xóa đồng phục thiếu nút X ở header.");
            Assert.assertTrue(dialog.modal() && dialog.dismissable(),
                    "Popup phải là modal và cho phép đóng an toàn.");
            Assert.assertTrue(dialog.itemStillExists(),
                    "Đồng phục biến mất sau khi chỉ kiểm tra và Hủy popup.");
        } finally {
            safeDeleteCatalogItem(ITEM_TAB, name);
        }
    }

    /** Xóa thật phải đưa số bản ghi khớp chính xác từ một về không. */
    @Test(groups = {"uniform", "catalog", "item-delete",
            "count", "mutation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_153)
    public void deletingItemRemovesRecordAndReducesResultCount() {
        String name = prepareItemForDelete();
        try {
            var result = catalogPage.deleteItemAndMeasure(ITEM_TAB, name);
            Assert.assertEquals(result.beforeCount(), 1,
                    "Trước khi xóa phải tìm thấy đúng một đồng phục.");
            Assert.assertTrue(result.removed(),
                    "Xác nhận xóa nhưng đồng phục vẫn tồn tại.");
            Assert.assertEquals(result.afterCount(), 0,
                    "Sau khi xóa vẫn còn đồng phục khớp đúng tên.");
        } finally {
            safeDeleteCatalogItem(ITEM_TAB, name);
        }
    }

    /** Nút X trên header chỉ đóng popup và không được xóa đồng phục. */
    @Test(groups = {"uniform", "catalog", "item-delete",
            "dialog", "close", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_154)
    public void closeDeleteDialogByXKeepsItem() {
        String name = prepareItemForDelete();
        try {
            Assert.assertTrue(catalogPage.closeDeleteDialogWithHeaderX(
                            ITEM_TAB, name),
                    "Bấm X đóng popup nhưng đồng phục đã bị xóa.");
        } finally {
            safeDeleteCatalogItem(ITEM_TAB, name);
        }
    }
}
