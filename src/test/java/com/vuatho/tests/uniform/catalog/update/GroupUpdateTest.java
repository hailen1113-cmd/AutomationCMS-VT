package com.vuatho.tests.uniform.catalog.update;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformGroupUpdateTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Testcase cập nhật hợp lệ của Nhóm Đồng Phục trên dữ liệu sandbox hiện có.
 *
 * <p>Không tạo dữ liệu chuẩn bị và không xóa sau khi cập nhật.</p>
 */
public class GroupUpdateTest extends UniformGroupUpdateTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(GroupUpdateTest.class,
                "Đồng phục", "Cập nhật Nhóm Đồng Phục");
    }

    /** Đổi tên nhóm thật và xác minh danh sách trả tên mới. */
    @Test(groups = {"uniform", "catalog", "group-update", "edit", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_025)
    public void renameGroupPersists() {
        String oldName = requireExistingGroup();
        String newName = uniqueCatalogName("AUTO-GROUP-EDIT");
        Assert.assertTrue(catalogPage.renameItem(GROUP_TAB, oldName, newName),
                "Đổi tên nhóm nhưng danh sách không trả tên mới.");
    }

    /** Đổi trạng thái hết hàng rồi mở lại để xác minh dữ liệu được lưu. */
    @Test(groups = {"uniform", "catalog", "group-update", "status", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_031)
    public void groupOutOfStockStatusPersists() {
        String name = requireExistingGroup();
        Assert.assertTrue(catalogPage.toggleGroupOutOfStockPersists(name),
                "Đổi trạng thái hết hàng nhưng mở lại không giữ giá trị.");
    }

    /** Drawer cập nhật nhóm phải có đủ trường và nút theo element nghiệp vụ. */
    @Test(groups = {"uniform", "catalog", "group-update", "form", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_113)
    public void groupUpdateFormContainsRequiredControls() {
        var form = catalogPage.groupUpdateFormSnapshot(requireExistingGroup());
        Assert.assertEquals(form.businessTextInputCount(), 2,
                "Drawer phải có đúng ô Tên nhóm và Giá bán.");
        Assert.assertEquals(form.comboboxCount(), 2,
                "Drawer phải có tài khoản thanh toán và package.");
        Assert.assertTrue(form.imageUpload() && form.multipleUpload(),
                "Drawer thiếu upload ảnh hợp lệ hoặc thuộc tính multiple.");
        Assert.assertTrue(form.outOfStockToggle(),
                "Drawer thiếu công tắc hết hàng.");
        Assert.assertTrue(form.deleteButton() && form.cancelButton()
                        && form.confirmButton(),
                "Drawer thiếu nút Xóa, Hủy hoặc Xác nhận.");
    }

    /** Đổi giá bán nhóm và đọc lại giá khi mở lại drawer. */
    @Test(groups = {"uniform", "catalog", "group-update", "price", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_114)
    public void updateGroupPricePersists() {
        Assert.assertTrue(catalogPage.updateGroupPricePersists(
                        requireExistingGroup(), "245000"),
                "Đổi giá nhóm nhưng mở lại không giữ giá mới.");
    }

    /** Đổi tài khoản thanh toán nếu có lựa chọn khác và đọc lại sau khi lưu. */
    @Test(groups = {"uniform", "catalog", "group-update", "payment", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_115)
    public void updateGroupPaymentAccountPersists() {
        var result = catalogPage.updateGroupPaymentAccountPersists(
                requireExistingGroup());
        if (!result.alternativeAvailable()) {
            throw new SkipException("[THIẾU DỮ LIỆU TEST] Không có tài khoản "
                    + "thanh toán thứ hai để kiểm tra đổi lựa chọn.");
        }
        Assert.assertTrue(result.persisted(),
                "Tài khoản đã chọn không được lưu: " + result.selectedValue());
    }

    /** Thay ảnh PNG thật và xác minh ảnh tải lại thành công trong chi tiết. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_116)
    public void updateGroupImagePersists() {
        var result = catalogPage.updateGroupImagePersists(
                requireExistingGroup(), createRasterTestImage());
        Assert.assertTrue(result.previewCount() >= 1,
                "Ảnh mới không hiển thị preview hợp lệ.");
        Assert.assertTrue(result.submitted(),
                "Drawer không đóng sau khi xác nhận thay ảnh.");
        Assert.assertTrue(result.persistedImageCount() >= 1,
                "Mở lại chi tiết nhưng ảnh nhóm không tải được.");
    }

    /** Chọn một đồng phục cho package và đọc lại tên sau khi lưu. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "package", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_117)
    public void updateGroupPackagePersists() {
        var result = catalogPage.updateGroupPackagePersists(
                requireExistingGroup());
        if (!result.alternativeAvailable()) {
            throw new SkipException("[THIẾU DỮ LIỆU TEST] Không có đồng phục "
                    + "để thêm vào package.");
        }
        Assert.assertTrue(result.persisted(),
                "Package không lưu đồng phục " + result.selectedValue());
    }

    /** Nhập tên nháp rồi Hủy phải giữ nguyên tên cũ. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "cancel", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_118)
    public void cancelGroupUpdateDoesNotSaveDraft() {
        Assert.assertTrue(catalogPage.discardGroupNameUpdate(
                        requireExistingGroup(),
                        uniqueCatalogName("AUTO-GROUP-CANCEL-DRAFT"), false),
                "Bấm Hủy nhưng thay đổi tên nháp vẫn được lưu.");
    }

    /** Nhập tên nháp rồi bấm X phải giữ nguyên tên cũ. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "close", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_119)
    public void closeGroupUpdateDoesNotSaveDraft() {
        Assert.assertTrue(catalogPage.discardGroupNameUpdate(
                        requireExistingGroup(),
                        uniqueCatalogName("AUTO-GROUP-CLOSE-DRAFT"), true),
                "Bấm X nhưng thay đổi tên nháp vẫn được lưu.");
    }

    /** Đổi đồng thời tên và giá rồi đọc lại cả hai trường. */
    @Test(groups = {"uniform", "catalog", "group-update",
            "multiple-fields", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_120)
    public void updateGroupNameAndPricePersists() {
        String newName = uniqueCatalogName("AUTO-GROUP-MULTI-EDIT");
        var result = catalogPage.updateGroupNameAndPricePersists(
                requireExistingGroup(), newName, "265000");
        Assert.assertTrue(result.namePersisted(),
                "Tên nhóm mới không xuất hiện trong danh sách.");
        Assert.assertTrue(result.pricePersisted(),
                "Giá mới không được lưu trong drawer chi tiết.");
    }

    /** Tạo ảnh PNG raster thật trong target làm dữ liệu thay ảnh nhóm. */
    private Path createRasterTestImage() {
        Path directory = Path.of("target", "test-generated");
        Path imagePath = directory.resolve("uniform-group-update.png");
        try {
            Files.createDirectories(directory);
            BufferedImage image = new BufferedImage(
                    120, 120, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setColor(new Color(16, 185, 129));
                graphics.fillRect(0, 0, 120, 120);
                graphics.setColor(Color.WHITE);
                graphics.fillRect(30, 30, 60, 60);
            } finally {
                graphics.dispose();
            }
            ImageIO.write(image, "png", imagePath.toFile());
            return imagePath;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Không tạo được ảnh PNG cập nhật nhóm.", exception);
        }
    }
}
