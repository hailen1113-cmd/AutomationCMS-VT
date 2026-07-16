package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.WorkerProfilePage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

abstract class WorkerProfileTestSupport extends BaseTest {
    protected WorkerProfilePage workerProfilePage;

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareWorkerProfilePage() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Khong dang nhap duoc truoc khi kiem tra ho so tho.");
        workerProfilePage = new WorkerProfilePage(driver).openFromMenu();
    }

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
