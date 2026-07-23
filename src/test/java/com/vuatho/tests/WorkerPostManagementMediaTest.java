package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerPostManagementPage.ImageTransformResult;
import com.vuatho.pages.WorkerPostManagementPage.ImageState;
import com.vuatho.pages.WorkerPostManagementPage.MediaNavigationResult;
import com.vuatho.pages.WorkerPostManagementPage.MediaType;
import com.vuatho.pages.WorkerPostManagementPage.Status;
import com.vuatho.pages.WorkerPostManagementPage.VideoState;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Collectors;

/** Kiểm tra modal xem ảnh/video và các control không làm thay đổi dữ liệu. */
public class WorkerPostManagementMediaTest extends WorkerPostManagementTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerPostManagementMediaTest.class,
                "Quản lý bài đăng thợ", "Ảnh và video bài đăng");
    }

    @DataProvider(name = "workerPostStatuses")
    public Object[][] workerPostStatuses() {
        return new Object[][]{
                {Status.PENDING},
                {Status.APPROVED},
                {Status.REJECTED},
                {Status.DELETED}
        };
    }

    @Test(groups = {"worker-post-management", "read-only", "media", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-010: Click thumbnail mở modal đủ thông tin và đóng được")
    public void mediaDialogOpensWithPostInformationAndCloses() {
        workerPostManagementPage.openFirstPostWithMultipleMedia();
        Assert.assertTrue(workerPostManagementPage.isMediaDialogOpen(), "Click media nhưng modal không mở.");
        Assert.assertTrue(workerPostManagementPage.mediaCounter().matches("\\d+/\\d+"),
                "Modal thiếu bộ đếm media.");
        Assert.assertTrue(workerPostManagementPage.mediaDialogHasPostInformation(),
                "Modal thiếu Ngành nghề hoặc Thời gian đăng.");
        Assert.assertTrue(workerPostManagementPage.mediaDialogHasPendingActions(),
                "Modal bài Chờ duyệt thiếu Duyệt bài hoặc Từ chối.");

        Set<String> controls = workerPostManagementPage.mediaControlTitles().stream()
                .map(TextNormalizer::normalize).collect(Collectors.toSet());
        for (String expected : Set.of("Thu nhỏ", "Phóng to", "Xoay trái", "Xoay phải")) {
            Assert.assertTrue(controls.contains(TextNormalizer.normalize(expected)),
                    "Modal thiếu control " + expected + ", actual=" + controls);
        }

        workerPostManagementPage.closeMediaDialog();
        Assert.assertFalse(workerPostManagementPage.isMediaDialogOpen(), "Nhấn ESC nhưng modal chưa đóng.");
    }

    @Test(groups = {"worker-post-management", "read-only", "media", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-011: Nút next/previous thay đổi và phục hồi media")
    public void mediaNextAndPreviousControlsWork() {
        workerPostManagementPage.openFirstPostWithMultipleMedia();
        MediaNavigationResult result = workerPostManagementPage.exerciseMediaNavigation();
        Assert.assertNotEquals(result.nextCounter(), result.initialCounter(),
                "Nút Next không thay đổi bộ đếm media.");
        Assert.assertEquals(result.previousCounter(), result.initialCounter(),
                "Nút Previous không quay lại media ban đầu.");
    }

    @Test(groups = {"worker-post-management", "read-only", "media", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-012: Zoom và xoay ảnh cập nhật đúng transform")
    public void imageZoomAndRotationControlsWork() {
        workerPostManagementPage.openFirstPostWithMultipleMedia();
        ImageTransformResult result = workerPostManagementPage.exerciseImageTransforms();
        Assert.assertNotEquals(result.zoomed(), result.initial(),
                "Phóng to không thay đổi transform ảnh.");
        Assert.assertEquals(result.zoomRestored(), result.initial(),
                "Thu nhỏ không phục hồi transform ban đầu.");
        Assert.assertNotEquals(result.rotated(), result.initial(),
                "Xoay phải không thay đổi transform ảnh.");
        Assert.assertEquals(result.rotationRestored(), result.initial(),
                "Xoay trái không phục hồi transform ban đầu.");
    }

    @Test(
            dataProvider = "workerPostStatuses",
            groups = {"worker-post-management", "read-only", "media", "image", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-022: Xem ảnh thành công ở từng tab trạng thái")
    public void imageCanBeViewedInEveryStatus(Status status) {
        workerPostManagementPage.openFirstPostContainingMediaAcrossPages(status, MediaType.IMAGE);
        ImageState image = workerPostManagementPage.navigateToImage();

        Assert.assertFalse(image.source().isBlank(),
                "Ảnh trong tab " + status.label() + " không có source.");
        Assert.assertTrue(image.counter().matches("\\d+/\\d+"),
                "Ảnh trong tab " + status.label() + " không đồng bộ với bộ đếm media.");
    }

    @Test(
            dataProvider = "workerPostStatuses",
            groups = {"worker-post-management", "read-only", "media", "video", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-017: Xem video thành công ở từng tab trạng thái")
    public void videoCanBeViewedInEveryStatus(Status status) {
        workerPostManagementPage.openFirstPostContainingMediaAcrossPages(status, MediaType.VIDEO);
        VideoState video = workerPostManagementPage.navigateToVideo();

        Assert.assertFalse(video.source().isBlank(),
                "Video trong tab " + status.label() + " không có source.");
        Assert.assertTrue(video.controls(),
                "Video trong tab " + status.label() + " không bật playback controls.");
        Assert.assertTrue(video.paused(),
                "Không thể đưa video trong tab " + status.label() + " về trạng thái pause.");
        Assert.assertTrue(video.counter().matches("\\d+/\\d+"),
                "Video trong tab " + status.label() + " không đồng bộ với bộ đếm media.");
    }

    @Test(groups = {"worker-post-management", "read-only", "media", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-015: Nút X đóng modal media")
    public void mediaDialogCloseButtonWorks() {
        workerPostManagementPage.openFirstPostWithMultipleMedia();
        Assert.assertTrue(workerPostManagementPage.isMediaDialogOpen(), "Modal media chưa mở.");
        workerPostManagementPage.closeMediaDialogWithButton();
        Assert.assertFalse(workerPostManagementPage.isMediaDialogOpen(),
                "Click nút X nhưng modal vẫn mở.");
    }

    @Test(groups = {"worker-post-management", "read-only", "media", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-016: Thumbnail +N mở viewer media")
    public void overflowThumbnailOpensMediaViewer() {
        workerPostManagementPage.selectStatus(Status.APPROVED)
                .openOverflowMediaThumbnail();
        Assert.assertTrue(workerPostManagementPage.isMediaDialogOpen(),
                "Click thumbnail +N nhưng modal không mở.");
        Assert.assertTrue(workerPostManagementPage.mediaCounter().matches("\\d+/\\d+"),
                "Modal mở từ +N không có bộ đếm media.");
    }
}
