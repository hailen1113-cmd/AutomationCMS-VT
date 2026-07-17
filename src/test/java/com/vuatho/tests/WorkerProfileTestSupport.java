package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.WorkerProfilePage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Cung cấp thiết lập, dữ liệu và thao tác dùng chung cho các test hồ sơ nhân sự.
 */
abstract class WorkerProfileTestSupport extends BaseTest {
    protected WorkerProfilePage workerProfilePage;

    /**
     * Cho biết có tái sử dụng cùng một WebDriver giữa các phương thức test hay không.
     * @return kết quả reuse driver between test methods sau khi xử lý
     */
    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    /**
     * Thực hiện xử lý prepare worker profile page trong luồng kiểm thử.
     */
    @BeforeMethod(alwaysRun = true)
    public void prepareWorkerProfilePage() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Khong dang nhap duoc truoc khi kiem tra ho so tho.");
        workerProfilePage = new WorkerProfilePage(driver).openFromMenu();
    }

    /**
     * Thực hiện xử lý clean worker profile state trong luồng kiểm thử.
     */
    @AfterMethod(alwaysRun = true)
    public void cleanWorkerProfileState() {
        try {
            if (workerProfilePage != null) {
                workerProfilePage.closeWorkerDetailIfOpen();
                workerProfilePage.restoreDefaultListIfNeeded();
            }
        } catch (RuntimeException exception) {
            System.out.println("[WorkerProfile] Bo qua don dep; testcase tiep theo se mo lai trang ho so tho.");
        }
    }
}
