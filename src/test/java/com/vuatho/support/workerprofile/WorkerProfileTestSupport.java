package com.vuatho.support.workerprofile;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.WorkerProfilePage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/**
 * Cung cấp thiết lập, dữ liệu và thao tác dùng chung cho các test hồ sơ nhân sự.
 */
public abstract class WorkerProfileTestSupport extends BaseTest {
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
        requireAuthenticatedSession("hồ sơ thợ");
        if (preserveWorkerProfileStateBetweenMethods()
                && workerProfilePage != null
                && driver.getCurrentUrl().contains("/vuatho/worker")) {
            return;
        }
        WorkerProfilePage page = new WorkerProfilePage(driver);
        workerProfilePage = driver.getCurrentUrl().contains("/vuatho/worker")
                ? page.reloadWorkerList()
                : page.openFromMenu();
    }

    /**
     * Thực hiện xử lý clean worker profile state trong luồng kiểm thử.
     */
    @AfterMethod(alwaysRun = true)
    public void cleanWorkerProfileState() {
        if (preserveWorkerProfileStateBetweenMethods()) {
            return;
        }
        try {
            if (workerProfilePage != null) {
                workerProfilePage.closeWorkerDetailIfOpen();
                workerProfilePage.restoreDefaultListIfNeeded();
            }
        } catch (RuntimeException exception) {
            System.out.println("[WorkerProfile] Bo qua don dep; testcase tiep theo se mo lai trang ho so tho.");
        }
    }

    /** Cho phép workflow nhiều bước giữ cùng drawer/thợ giữa các test method. */
    protected boolean preserveWorkerProfileStateBetweenMethods() {
        return false;
    }
}
