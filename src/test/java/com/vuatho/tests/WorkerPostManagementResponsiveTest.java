package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.openqa.selenium.Dimension;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra khả năng sử dụng menu quản lý bài đăng trên các kích thước màn hình. */
public class WorkerPostManagementResponsiveTest extends WorkerPostManagementTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerPostManagementResponsiveTest.class,
                "Quản lý bài đăng thợ", "Responsive menu quản lý bài đăng");
    }

    @Test(groups = {"worker-post-management", "read-only", "responsive"},
            description = "WORKER-POST-MANAGEMENT-019: Tab và card vẫn dùng được ở viewport tablet")
    public void tabletViewportKeepsCoreContentUsable() {
        Dimension original = driver.manage().window().getSize();
        try {
            driver.manage().window().setSize(new Dimension(768, 900));
            workerPostManagementPage.openPendingDirectly();
            Assert.assertEquals(workerPostManagementPage.tabLabels().size(), 4,
                    "Viewport tablet làm mất tab trạng thái.");
            Assert.assertFalse(workerPostManagementPage.visiblePostCards().isEmpty(),
                    "Viewport tablet không hiển thị card bài đăng.");
            Assert.assertTrue(workerPostManagementPage.hasPendingActionsOnEveryCard(),
                    "Viewport tablet làm mất action quản lý bài đăng.");
        } finally {
            driver.manage().window().setSize(original);
        }
    }
}
