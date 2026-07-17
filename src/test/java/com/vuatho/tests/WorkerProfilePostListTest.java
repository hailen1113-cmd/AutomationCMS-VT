package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerProfilePage;
import com.vuatho.pages.WorkerProfilePage.WorkerPostDetailResult;
import com.vuatho.pages.WorkerProfilePage.WorkerPostViewerControlResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Kiểm tra các bài đăng trong hồ sơ thợ có thể mở phần xem chi tiết.
 */
public class WorkerProfilePostListTest extends WorkerProfileTestSupport {
    /** Hồ sơ cố định có sẵn nhiều bài đăng để testcase luôn có dữ liệu kiểm tra. */
    private static final String SOURCE_WORKER_NAME = "LEN VAN HAI LVH";
    private static final int MAX_POSTS_TO_OPEN = 3;
    private static final Duration POST_LIST_VIEW_DURATION = Duration.ofSeconds(4);
    private static final Duration POST_DETAIL_VIEW_DURATION = Duration.ofSeconds(6);
    private static final Duration FINAL_VIEW_DURATION = Duration.ofSeconds(6);

    /** Cho phép chạy trực tiếp testcase từ IDE. */
    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfilePostListTest.class,
                "Bo test bai dang trong ho so tho ERP",
                "Click cac bai dang dau tien va kiem tra phan xem chi tiet");
    }

    /**
     * Tìm đúng hồ sơ nguồn, mở tab Bài đăng và lần lượt xem chi tiết tối đa ba bài đầu tiên.
     */
    @Test(groups = { "partner-worker", "worker-profile", "worker-post" },
            description = "WORKER-PROFILE-POST-001: Mo chi tiet cac bai dang dau tien")
    public void firstWorkerPostsOpenDetails() {
        System.out.println("[WORKER POST] Tim va mo ho so: " + SOURCE_WORKER_NAME);
        workerProfilePage.searchAndOpenWorkerInformationByName(SOURCE_WORKER_NAME);
        Assert.assertTrue(workerProfilePage.workerDetailIsOpen(),
                "Khong mo duoc chi tiet user: " + SOURCE_WORKER_NAME + ".");

        workerProfilePage.openWorkerDetailTab("Bài đăng");
        Assert.assertTrue(workerProfilePage.hasWorkerPostList(),
                "Tab Bai dang khong co bai nao co the click xem chi tiet.");

        int postCount = workerProfilePage.workerPostCount();
        Assert.assertTrue(postCount > 0, "Danh sach Bai dang dang rong.");
        workerProfilePage.keepWorkerDetailVisible(POST_LIST_VIEW_DURATION);

        int postsToOpen = Math.min(MAX_POSTS_TO_OPEN, postCount);
        for (int index = 0; index < postsToOpen; index++) {
            System.out.println("[WORKER POST] CLICK bai dang thu " + (index + 1) + ".");
            WorkerPostDetailResult result = workerProfilePage
                    .openWorkerPostDetailAndReturn(index, POST_DETAIL_VIEW_DURATION);

            Assert.assertFalse(result.sourcePostText().isBlank(),
                    "Card bai dang thu " + (index + 1) + " khong co noi dung.");
            Assert.assertTrue(result.detailVisible(),
                    "Click bai dang thu " + (index + 1) + " nhung chi tiet khong hien thi.");
            System.out.println("[WORKER POST] Da xem chi tiet bai dang thu "
                    + (index + 1) + " va quay lai danh sach.");
        }

        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }

    /**
     * Kiểm tra các nút chuyển ảnh, phóng to, thu nhỏ, xoay phải và xoay trái trong modal bài đăng.
     */
    @Test(groups = { "partner-worker", "worker-profile", "worker-post" },
            description = "WORKER-PROFILE-POST-002: Kiem tra control trong modal bai dang")
    public void postViewerControlsChangeMediaAndImageTransform() {
        // Làm mới state React để testcase độc lập với drawer/modal của case chạy trước.
        driver.navigate().refresh();
        workerProfilePage = new WorkerProfilePage(driver).openFromMenu();
        System.out.println("[WORKER POST CONTROL] Tim va mo ho so: " + SOURCE_WORKER_NAME);
        workerProfilePage.searchAndOpenWorkerInformationByName(SOURCE_WORKER_NAME);
        Assert.assertTrue(workerProfilePage.workerDetailIsOpen(),
                "Khong mo duoc chi tiet user: " + SOURCE_WORKER_NAME + ".");

        workerProfilePage.openWorkerDetailTab("Bài đăng");
        Assert.assertTrue(workerProfilePage.hasWorkerPostList(),
                "Tab Bai dang khong co bai nao de kiem tra control.");

        WorkerPostViewerControlResult result = workerProfilePage
                .exerciseFirstWorkerPostViewerControls(Duration.ofSeconds(3));

        Assert.assertNotEquals(result.nextCounter(), result.initialCounter(),
                "Nut anh tiep theo khong thay doi bo dem media.");
        Assert.assertEquals(result.previousCounter(), result.initialCounter(),
                "Nut anh truoc khong quay lai media ban dau.");
        Assert.assertNotEquals(result.zoomedTransform(), result.initialTransform(),
                "Nut Phong to khong thay doi transform cua anh.");
        Assert.assertEquals(result.restoredZoomTransform(), result.initialTransform(),
                "Nut Thu nho khong dua anh ve ti le ban dau.");
        Assert.assertNotEquals(result.rotatedTransform(), result.initialTransform(),
                "Nut Xoay phai khong thay doi transform cua anh.");
        Assert.assertEquals(result.restoredRotationTransform(), result.initialTransform(),
                "Nut Xoay trai khong dua anh ve huong ban dau.");

        System.out.println("[WORKER POST CONTROL] Chuyen anh: "
                + result.initialCounter() + " -> " + result.nextCounter()
                + " -> " + result.previousCounter() + ".");
        System.out.println("[WORKER POST CONTROL] Zoom va xoay anh hoat dong dung.");
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }
}
