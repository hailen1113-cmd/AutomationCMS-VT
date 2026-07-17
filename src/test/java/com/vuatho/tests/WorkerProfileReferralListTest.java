package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerProfilePage.ReferredWorkerDetailResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Kiểm tra các thợ trong phần Đã giới thiệu có thể mở đúng hồ sơ ở tab mới.
 */
public class WorkerProfileReferralListTest extends WorkerProfileTestSupport {
    /** Tạm thời cố định hồ sơ nguồn có dữ liệu Đã giới thiệu để testcase luôn dùng đúng người. */
    private static final String SOURCE_WORKER_NAME = "LEN VAN HAI LVH";
    private static final int MAX_WORKERS_TO_OPEN = 3;
    private static final Duration REFERRAL_LIST_VIEW_DURATION = Duration.ofSeconds(4);
    private static final Duration REFERRED_WORKER_VIEW_DURATION = Duration.ofSeconds(6);
    private static final Duration FINAL_VIEW_DURATION = Duration.ofSeconds(6);

    /** Cho phép chạy trực tiếp testcase từ IDE. */
    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfileReferralListTest.class,
                "Bo test danh sach gioi thieu trong ho so tho ERP",
                "Click cac tho da gioi thieu va kiem tra tab ho so");
    }

    /**
     * Click tối đa ba thợ đầu tiên, xác nhận đúng ID trên URL và quay lại danh sách sau mỗi lần.
     */
    @Test(groups = { "partner-worker", "worker-profile", "worker-referral" },
            description = "WORKER-PROFILE-REFERRAL-001: Mo cac tho dau tien trong danh sach Da gioi thieu")
    public void firstReferredWorkersOpenMatchingProfilesInNewTabs() {
        System.out.println("[WORKER REFERRAL] Tim ho so nguon: " + SOURCE_WORKER_NAME);
        System.out.println("[WORKER REFERRAL] CLICK mo chi tiet: " + SOURCE_WORKER_NAME);
        workerProfilePage.searchAndOpenWorkerInformationByName(SOURCE_WORKER_NAME);
        Assert.assertTrue(workerProfilePage.workerDetailIsOpen(),
                "Khong mo duoc chi tiet user: " + SOURCE_WORKER_NAME + ".");
        System.out.println("[WORKER REFERRAL] Da mo chi tiet: " + SOURCE_WORKER_NAME);
        workerProfilePage.openWorkerDetailTab("Giới thiệu");

        Assert.assertTrue(workerProfilePage.hasReferredWorkerList(),
                "Khong tim thay danh sach Da gioi thieu hoac danh sach khong co tho.");
        int workerCount = workerProfilePage.referredWorkerCount();
        Assert.assertTrue(workerCount > 0,
                "Phan Da gioi thieu khong co tho nao de click.");
        workerProfilePage.keepWorkerDetailVisible(REFERRAL_LIST_VIEW_DURATION);

        int workersToOpen = Math.min(MAX_WORKERS_TO_OPEN, workerCount);
        for (int index = 0; index < workersToOpen; index++) {
            System.out.println("[WORKER REFERRAL] CLICK tho thu " + (index + 1) + ".");
            ReferredWorkerDetailResult result = workerProfilePage
                    .openReferredWorkerInNewTabAndReturn(index, REFERRED_WORKER_VIEW_DURATION);

            Assert.assertTrue(result.workerId().matches("\\d+"),
                    "Khong doc duoc ID cua tho thu " + (index + 1) + ".");
            Assert.assertFalse(result.workerName().isBlank(),
                    "Card tho thu " + (index + 1) + " khong co ten.");
            Assert.assertTrue(result.detailUrl().contains("/vuatho/user")
                            && result.detailUrl().matches(".*[?&]id=" + result.workerId() + "(?:&.*)?$"),
                    "Tab moi khong mo dung ho so tho #" + result.workerId() + ".");
            Assert.assertFalse(result.detailText().isBlank(),
                    "Trang ho so tho #" + result.workerId() + " khong co noi dung.");
            System.out.println("[WORKER REFERRAL] Da mo ho so " + result.workerName()
                    + " #" + result.workerId() + " va quay lai danh sach.");
        }

        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }
}
