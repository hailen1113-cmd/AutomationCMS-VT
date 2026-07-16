package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;

public class WorkerProfileListDetailTest extends WorkerProfileTestSupport {
    private static final Duration DETAIL_DATA_VIEW_DURATION = Duration.ofSeconds(3);
    private static final String[] BASIC_WORKER_DETAIL_TABS = {
            "Tong quan",
            "Nganh nghe",
            "Giao dich",
            "Don dich vu",
            "Gioi thieu",
            "Bai dang",
            "Xu ly vi pham"
    };

    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfileListDetailTest.class,
                "Bo test danh sach va chi tiet ho so tho ERP",
                "Kiem tra danh sach va chi tiet ho so tho");
    }

    @DataProvider(name = "workerDetailTabs", parallel = false)
    public Object[][] workerDetailTabs() {
        Object[][] tabs = new Object[BASIC_WORKER_DETAIL_TABS.length][1];
        for (int index = 0; index < BASIC_WORKER_DETAIL_TABS.length; index++) {
            tabs[index][0] = BASIC_WORKER_DETAIL_TABS[index];
        }
        return tabs;
    }

    @Test(groups = { "partner-worker", "worker-profile", "worker-profile-list" },
            description = "WORKER-PROFILE-LIST-001: Danh sach ho so tho hien thi tong hop va bang")
    public void workerProfileListShowsSummaryAndTable() {
        Assert.assertTrue(workerProfilePage.hasSearchInput(),
                "Khong thay o tim kiem tho.");
        Assert.assertTrue(workerProfilePage.hasKpiSummary(),
                "Thieu phan tong hop KPI ho so tho.");
        Assert.assertTrue(workerProfilePage.hasExpectedTableHeaders(),
                "Thieu tieu de bang ho so tho.");
        Assert.assertTrue(workerProfilePage.hasWorkerRows(),
                "Bang ho so tho khong co dong hien thi.");
    }

    @Test(groups = { "partner-worker", "worker-profile", "worker-profile-detail" },
            description = "WORKER-PROFILE-DETAIL-001: Mo duoc chi tiet thong tin tho")
    public void workerInformationCanBeOpened() {
        String firstRowText = workerProfilePage.firstWorkerRowText();

        workerProfilePage.openFirstWorkerInformation();

        Assert.assertTrue(workerProfilePage.workerDetailIsOpen(),
                "Chi tiet tho khong tai duoc. Dong: " + firstRowText);
        Assert.assertFalse(workerProfilePage.workerDetailText().isBlank(),
                "Chi tiet tho da mo nhung khong co noi dung hien thi.");
    }

    @Test(dataProvider = "workerDetailTabs",
            groups = { "partner-worker", "worker-profile", "worker-profile-detail" },
            description = "WORKER-PROFILE-DETAIL-TAB-001: Mo duoc cac tab chi tiet tho")
    public void workerDetailTabsCanBeOpened(String tabLabel) {
        workerProfilePage.openFirstWorkerInformation();

        workerProfilePage.openWorkerDetailTab(tabLabel);

        Assert.assertTrue(workerProfilePage.workerDetailTabIsSelected(tabLabel),
                "Tab chi tiet tho chua duoc chon: " + tabLabel);
        Assert.assertFalse(workerProfilePage.workerDetailText().isBlank(),
                "Tab chi tiet tho khong co du lieu hien thi: " + tabLabel);
        workerProfilePage.keepWorkerDetailVisible(DETAIL_DATA_VIEW_DURATION);
        workerProfilePage.closeWorkerDetail();
        Assert.assertTrue(workerProfilePage.hasWorkerRows(),
                "Danh sach tho khong tai lai sau khi dong chi tiet.");
    }

    @Test(groups = { "partner-worker", "worker-profile", "worker-profile-detail" },
            description = "WORKER-PROFILE-DETAIL-TAB-002: Doi duoc tab chi tiet tho ma khong quay ve danh sach")
    public void workerDetailTabsCanBeSwitchedWithoutReturningToList() {
        workerProfilePage.openFirstWorkerInformation();

        for (String tabLabel : BASIC_WORKER_DETAIL_TABS) {
            workerProfilePage.openWorkerDetailTab(tabLabel);

            Assert.assertTrue(workerProfilePage.workerDetailTabIsSelected(tabLabel),
                    "Tab chi tiet tho chua duoc chon: " + tabLabel);
            Assert.assertFalse(workerProfilePage.workerDetailText().isBlank(),
                    "Tab chi tiet tho khong co du lieu hien thi: " + tabLabel);
            workerProfilePage.keepWorkerDetailVisible(DETAIL_DATA_VIEW_DURATION);
        }
    }
}
