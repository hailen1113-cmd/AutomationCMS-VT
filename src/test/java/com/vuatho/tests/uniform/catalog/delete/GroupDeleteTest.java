package com.vuatho.tests.uniform.catalog.delete;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformDeleteTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Testcase popup, hủy và xác nhận xóa thật Nhóm Đồng Phục. */
public class GroupDeleteTest extends UniformDeleteTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(GroupDeleteTest.class,
                "Đồng phục", "Xóa Nhóm Đồng Phục");
    }

    /** Hủy popup phải giữ nhóm, sau đó xác nhận phải xóa thật nhóm đó. */
    @Test(groups = {"uniform", "catalog", "group-delete",
            "mutation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_027)
    public void cancelThenConfirmDeleteGroupWorks() {
        String name = prepareGroupForDelete();
        try {
            Assert.assertTrue(catalogPage.cancelDeleteItem(GROUP_TAB, name),
                    "Hủy xóa nhưng nhóm đã biến mất.");
            Assert.assertTrue(catalogPage.deleteItem(GROUP_TAB, name),
                    "Xác nhận xóa nhưng nhóm vẫn còn.");
        } finally {
            safeDeleteCatalogItem(GROUP_TAB, name);
        }
    }

    /** Popup phải có cảnh báo, nút Hủy và nút xác nhận; Hủy không làm mất nhóm. */
    @Test(groups = {"uniform", "catalog", "group-delete",
            "dialog", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_150)
    public void groupDeleteDialogContainsWarningAndActions() {
        String name = prepareGroupForDelete();
        try {
            var dialog = catalogPage.inspectDeleteDialogAndCancel(
                    GROUP_TAB, name);
            Assert.assertFalse(dialog.content().isBlank(),
                    "Popup xóa nhóm không có nội dung cảnh báo.");
            Assert.assertTrue(dialog.content().contains("Xóa"),
                    "Popup xóa nhóm không thể hiện mục đích xóa.");
            Assert.assertTrue(dialog.cancelButton() && dialog.confirmButton(),
                    "Popup xóa nhóm thiếu nút Hủy hoặc Xác nhận.");
            Assert.assertTrue(dialog.itemStillExists(),
                    "Nhóm biến mất sau khi chỉ kiểm tra và Hủy popup.");
        } finally {
            safeDeleteCatalogItem(GROUP_TAB, name);
        }
    }

    /** Xóa thật phải đưa số bản ghi khớp chính xác từ một về không. */
    @Test(groups = {"uniform", "catalog", "group-delete",
            "count", "mutation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_151)
    public void deletingGroupRemovesRecordAndReducesResultCount() {
        String name = prepareGroupForDelete();
        try {
            var result = catalogPage.deleteItemAndMeasure(GROUP_TAB, name);
            Assert.assertEquals(result.beforeCount(), 1,
                    "Trước khi xóa phải tìm thấy đúng một nhóm.");
            Assert.assertTrue(result.removed(),
                    "Xác nhận xóa nhưng nhóm vẫn tồn tại.");
            Assert.assertEquals(result.afterCount(), 0,
                    "Sau khi xóa vẫn còn nhóm khớp đúng tên.");
        } finally {
            safeDeleteCatalogItem(GROUP_TAB, name);
        }
    }
}
