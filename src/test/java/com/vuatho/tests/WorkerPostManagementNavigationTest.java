package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerPostManagementPage.Status;
import com.vuatho.pages.WorkerPostManagementPage.WorkerProfileNavigation;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra điều hướng giữa các tab trạng thái và sang hồ sơ thợ. */
public class WorkerPostManagementNavigationTest extends WorkerPostManagementTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerPostManagementNavigationTest.class,
                "Quản lý bài đăng thợ", "Điều hướng bài đăng");
    }

    @Test(groups = {"worker-post-management", "read-only", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-007: Chuyển trạng thái cập nhật tab và query URL")
    public void switchingStatusUpdatesSelectionAndUrl() {
        for (Status status : Status.values()) {
            workerPostManagementPage.selectStatus(status);
            Assert.assertEquals(workerPostManagementPage.selectedStatus().orElse(null), status,
                    "Tab được chọn không đúng: " + status.label());
            Assert.assertTrue(driver.getCurrentUrl().contains("tab=" + status.queryValue()),
                    "URL không cập nhật theo tab " + status.label() + ": "
                            + driver.getCurrentUrl());
            Assert.assertTrue(workerPostManagementPage.totalPosts() >=
                            workerPostManagementPage.visiblePostCards().size(),
                    "Tổng hiển thị không hợp lệ tại tab " + status.label());
        }
    }

    @Test(groups = {"worker-post-management", "read-only", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-009: Tên thợ điều hướng đến đúng hồ sơ")
    public void workerNameLinksToWorkerProfile() {
        WorkerProfileNavigation navigation =
                workerPostManagementPage.clickFirstWorkerProfileLink();
        Assert.assertTrue(navigation.expectedHref().matches(
                        ".*?/vuatho/worker\\?id=\\d+.*"),
                "Link thợ không đúng route hồ sơ: " + navigation.expectedHref());
        Assert.assertTrue(navigation.actualUrl().equals(navigation.expectedHref())
                        || navigation.actualUrl().startsWith(navigation.expectedHref() + "&"),
                "Điều hướng tên thợ không đến đúng hồ sơ. expected="
                        + navigation.expectedHref() + ", actual=" + navigation.actualUrl());
    }
}
