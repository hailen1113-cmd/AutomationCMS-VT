package com.vuatho.tests.uniform.catalog.create;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformCatalogCrudTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Toàn bộ testcase tạo Nhóm Đồng Phục hợp lệ: form, lựa chọn và lưu dữ liệu thật.
 */
public class UniformGroupCreateTest extends UniformCatalogCrudTestSupport {
    private static final String GROUP_TAB = "Nhóm Đồng Phục";
    private static final Path IMAGE_DIRECTORY = Path.of(
            "src", "test", "resources", "uniform-images");

    public static void main(String[] args) {
        TestNgRunner.run(UniformGroupCreateTest.class,
                "Đồng phục", "Tạo nhóm đồng phục");
    }

    /** Kiểm tra đầy đủ trường, combobox, upload, công tắc và nút thao tác theo DOM drawer. */
    @Test(groups = {"uniform", "catalog", "group-create", "form", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_001)
    public void groupCreateFormContainsRequiredControls() {
        String form = catalogPage.open().selectTab(GROUP_TAB).openCreateDrawer();
        for (String field : new String[]{
                "Tên nhóm", "Giá bán", "Tài khoản thanh toán",
                "Ảnh đại diện", "Trạng thái hết hàng", "Packages",
                "Chọn đồng phục"}) {
            Assert.assertTrue(form.contains(field), "Form tạo nhóm thiếu " + field);
        }

        var controls = catalogPage.groupCreateFormSnapshot();
        Assert.assertEquals(controls.businessTextInputCount(), 2,
                "Form phải có đúng ô Tên nhóm và Giá bán.");
        Assert.assertEquals(controls.comboboxCount(), 2,
                "Form phải có combobox tài khoản thanh toán và package.");
        Assert.assertTrue(controls.imageUpload() && controls.multipleUpload(),
                "Upload ảnh nhóm phải nhận tệp ảnh và hỗ trợ chọn nhiều tệp.");
        Assert.assertTrue(controls.outOfStockToggle(),
                "Form thiếu công tắc Trạng thái hết hàng.");
        Assert.assertTrue(controls.emptyPackageMessage(),
                "Package rỗng phải hiển thị thông báo chưa có đồng phục được chọn.");
        Assert.assertTrue(controls.cancelButton() && controls.confirmButton(),
                "Form thiếu nút Hủy hoặc Xác nhận.");
    }

    /** Nhập tên nháp rồi Hủy phải đóng drawer và không tạo dữ liệu. */
    @Test(groups = {"uniform", "catalog", "group-create", "cancel", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_015)
    public void cancelClosesGroupCreateFormWithoutSaving() {
        assertCloseDoesNotSave(false);
    }

    /** Nhập tên nháp rồi bấm X phải đóng drawer và không tạo dữ liệu. */
    @Test(groups = {"uniform", "catalog", "group-create", "close", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_016)
    public void closeIconClosesGroupCreateFormWithoutSaving() {
        assertCloseDoesNotSave(true);
    }

    /** Chọn được tài khoản nhận tiền trên form tạo nhóm. */
    @Test(groups = {"uniform", "catalog", "group-create", "payment", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_017)
    public void paymentAccountCanBeSelectedForGroup() {
        catalogPage.open().selectTab(GROUP_TAB).openCreateDrawer();
        String form = catalogPage.selectFirstPaymentAccount();
        Assert.assertFalse(form.contains("Chọn tài khoản thanh toán"),
                "Combobox vẫn giữ placeholder sau khi chọn tài khoản.");
    }

    /** Chọn được đồng phục đầu tiên cho package trên form nháp. */
    @Test(groups = {"uniform", "catalog", "group-create", "package", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_018)
    public void uniformCanBeAddedToGroupPackage() {
        catalogPage.open().selectTab(GROUP_TAB).openCreateDrawer();
        String form = catalogPage.selectFirstUniformForPackage();
        Assert.assertFalse(form.contains("Chưa có đồng phục được chọn"),
                "Chọn đồng phục nhưng package vẫn rỗng.");
    }

    /** Upload ảnh đại diện phải hiển thị ảnh xem trước. */
    @Test(groups = {"uniform", "catalog", "group-create", "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_019)
    public void groupImageUploadShowsPreview() {
        catalogPage.open().selectTab(GROUP_TAB).openCreateDrawer();
        int previews = catalogPage.uploadCreateImages(
                List.of(IMAGE_DIRECTORY.resolve("uniform-1.svg")));
        Assert.assertTrue(previews >= 1,
                "Upload ảnh nhóm không hiển thị bản xem trước.");
    }

    /** Công tắc Hết hàng phải đổi được trạng thái rồi đổi lại trên form nháp. */
    @Test(groups = {"uniform", "catalog", "group-create", "status", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_074)
    public void groupOutOfStockToggleCanChangeDraftState() {
        catalogPage.open().selectTab(GROUP_TAB).openCreateDrawer();
        Assert.assertTrue(catalogPage.toggleGroupOutOfStockDraft(),
                "Công tắc không đổi trạng thái sau lần bấm thứ nhất.");
        Assert.assertTrue(catalogPage.toggleGroupOutOfStockDraft(),
                "Công tắc không trở về trạng thái ban đầu.");
    }

    /** Tạo nhóm hợp lệ và tìm lại được trong danh sách. */
    @Test(groups = {"uniform", "catalog", "group-create", "mutation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_023)
    public void createGroupPersistsInList() {
        String name = uniqueCatalogName("AUTO-GROUP");
        Assert.assertTrue(catalogPage.createGroup(name, "125000"),
                "Tạo nhóm xong nhưng không tìm thấy " + name);
    }

    /** Tạo nhóm ở trạng thái hết hàng và xác minh trạng thái được lưu. */
    @Test(groups = {"uniform", "catalog", "group-create", "mutation",
            "status", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_081)
    public void createOutOfStockGroupPersistsStatus() {
        String name = uniqueCatalogName("AUTO-GROUP-OUT-OF-STOCK");
        Assert.assertTrue(catalogPage.createOutOfStockGroup(name, "145000"),
                "Tạo nhóm hết hàng nhưng trạng thái không được lưu.");
    }

    /** Tạo nhóm có ảnh PNG và xác minh ảnh được lưu trong chi tiết. */
    @Test(groups = {"uniform", "catalog", "group-create", "mutation",
            "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_082)
    public void createGroupWithImagePersistsImage() {
        String name = uniqueCatalogName("AUTO-GROUP-IMAGE");
        Path image = createRasterTestImage();
        catalogPage.prepareGroupCreate(name, "155000");
        int previews = catalogPage.uploadCreateImages(List.of(image));
        Assert.assertTrue(previews >= 1,
                "Chọn ảnh PNG nhưng form không hiển thị bản xem trước.");
        Assert.assertTrue(catalogPage.submitOpenedGroupAndVerifyImage(name),
                "Tạo nhóm có ảnh nhưng ảnh không được lưu trong chi tiết.");
    }

    /** Tạo nhóm có package và xác minh đồng phục đã chọn được lưu. */
    @Test(groups = {"uniform", "catalog", "group-create", "mutation",
            "package", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_083)
    public void createGroupWithPackagePersistsSelectedUniform() {
        String name = uniqueCatalogName("AUTO-GROUP-PACKAGE");
        var result = catalogPage.createGroupWithPackage(name, "165000");
        Assert.assertTrue(result.created(), "Không tạo được nhóm có package.");
        Assert.assertFalse(result.selectedUniform().isBlank(),
                "Không đọc được tên đồng phục đã chọn cho package.");
        Assert.assertTrue(result.detailContainsSelectedUniform(),
                "Chi tiết nhóm không chứa đồng phục package "
                        + result.selectedUniform());
    }

    /** Nhóm vừa tạo phải vẫn tồn tại sau khi tải lại trang. */
    @Test(groups = {"uniform", "catalog", "group-create", "mutation",
            "reload", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_084)
    public void createdGroupPersistsAfterPageReload() {
        String name = uniqueCatalogName("AUTO-GROUP-RELOAD");
        Assert.assertTrue(catalogPage.createGroup(name, "175000"),
                "Không tạo được nhóm chuẩn bị kiểm tra reload.");
        Assert.assertTrue(catalogPage.itemPersistsAfterRefresh(GROUP_TAB, name),
                "Nhóm vừa tạo biến mất sau khi tải lại trang.");
    }

    /** Dùng chung kiểm tra đóng form nhóm nhưng vẫn giữ hai testcase có ID độc lập. */
    private void assertCloseDoesNotSave(boolean byIcon) {
        String draftName = "AUTO-GROUP-DRAFT-" + Long.toString(
                System.nanoTime(), 36).toUpperCase();
        catalogPage.open().selectTab(GROUP_TAB).openCreateDrawer();
        catalogPage.fillCreateName(draftName);
        boolean closed = byIcon
                ? catalogPage.closeCreateDrawerByIcon()
                : catalogPage.cancelCreateDrawer();
        Assert.assertTrue(closed, "Không đóng được form tạo nhóm.");
        Assert.assertFalse(catalogPage.itemExists(GROUP_TAB, draftName),
                "Đóng form nhưng vẫn tạo dữ liệu " + draftName);
    }

    /** Tạo ảnh PNG raster hợp lệ làm dữ liệu upload của testcase tạo có ảnh. */
    private Path createRasterTestImage() {
        Path directory = Path.of("target", "test-generated");
        Path imagePath = directory.resolve("uniform-group-create.png");
        try {
            Files.createDirectories(directory);
            BufferedImage image = new BufferedImage(
                    80, 80, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setColor(new Color(64, 90, 183));
                graphics.fillRect(0, 0, 80, 80);
                graphics.setColor(Color.WHITE);
                graphics.fillRect(22, 22, 36, 36);
            } finally {
                graphics.dispose();
            }
            ImageIO.write(image, "png", imagePath.toFile());
            return imagePath;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Không tạo được ảnh PNG dữ liệu test.", exception);
        }
    }
}
