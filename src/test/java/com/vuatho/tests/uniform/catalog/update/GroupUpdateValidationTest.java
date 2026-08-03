package com.vuatho.tests.uniform.catalog.update;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformCatalogPage.GroupUpdateSubmissionSnapshot;
import com.vuatho.support.UniformGroupUpdateTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Testcase validation riêng của drawer cập nhật Nhóm Đồng Phục. */
public class GroupUpdateValidationTest
        extends UniformGroupUpdateTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(GroupUpdateValidationTest.class,
                "Đồng phục", "Validation cập nhật Nhóm Đồng Phục");
    }

    /** Xóa rỗng tên nhóm phải giữ drawer và báo lỗi bắt buộc. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_121)
    public void emptyNameCannotUpdateGroup() {
        assertRequiredValidation(
                catalogPage.submitGroupUpdateDraft(
                        requireExistingGroup(), "", "245000"),
                "Nhập tên nhóm");
    }

    /** Tên chỉ có khoảng trắng phải bị xem là chưa nhập. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_122)
    public void whitespaceNameCannotUpdateGroup() {
        assertRequiredValidation(
                catalogPage.submitGroupUpdateDraft(
                        requireExistingGroup(), "   ", "245000"),
                "Nhập tên nhóm");
    }

    /** Xóa rỗng giá bán phải giữ drawer và báo lỗi bắt buộc. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_123)
    public void emptyPriceCannotUpdateGroup() {
        String name = requireExistingGroup();
        assertRequiredValidation(
                catalogPage.submitGroupUpdateDraft(name, name, ""),
                "Nhập giá bán");
    }

    /** Giá chỉ có khoảng trắng phải bị xem là chưa nhập. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_124)
    public void whitespacePriceCannotUpdateGroup() {
        String name = requireExistingGroup();
        assertRequiredValidation(
                catalogPage.submitGroupUpdateDraft(name, name, "   "),
                "Nhập giá bán");
    }

    /** Giá bằng 0 không được phép lưu khi cập nhật. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_125)
    public void zeroPriceCannotUpdateGroup() {
        assertInvalidPriceRejected("0");
    }

    /** Giá âm không được phép lưu khi cập nhật. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_126)
    public void negativePriceCannotUpdateGroup() {
        assertInvalidPriceRejected("-245000");
    }

    /** Giá chứa chữ không được phép lưu khi cập nhật. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_127)
    public void alphabeticPriceCannotUpdateGroup() {
        assertInvalidPriceRejected("abc");
    }

    /** Giá thập phân không được phép lưu với đơn vị đồng Việt Nam. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_128)
    public void decimalPriceCannotUpdateGroup() {
        assertInvalidPriceRejected("245000.5");
    }

    /** Chuỗi số vượt giới hạn phải bị từ chối an toàn. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_129)
    public void oversizedPriceCannotUpdateGroup() {
        assertInvalidPriceRejected(
                "999999999999999999999999999999999999");
    }

    /** Kiểm tra thông báo đúng và drawer không đóng khi thiếu trường bắt buộc. */
    private void assertRequiredValidation(
            GroupUpdateSubmissionSnapshot result, String expectedMessage) {
        Assert.assertTrue(result.drawerOpen(),
                "Drawer đã đóng dù thiếu trường bắt buộc.");
        Assert.assertTrue(result.content().contains(expectedMessage),
                "Drawer thiếu validation: " + expectedMessage);
    }

    /** Giá sai phải giữ drawer đang mở; nếu đóng nghĩa là hệ thống đã nhận sai. */
    private void assertInvalidPriceRejected(String price) {
        String name = requireExistingGroup();
        GroupUpdateSubmissionSnapshot result =
                catalogPage.submitGroupUpdateDraft(name, name, price);
        Assert.assertTrue(result.drawerOpen(),
                "Drawer đã đóng sau khi nhập giá không hợp lệ: " + price);
    }
}
