package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WorkerProfileFilterTest extends WorkerProfileTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfileFilterTest.class,
                "Bo test bo loc ho so tho ERP",
                "Kiem tra bo loc ho so tho");
    }

    @DataProvider(name = "kycStatusFilters", parallel = false)
    public Object[][] kycStatusFilters() {
        return new Object[][] {
                { "Chua KYC" },
                { "Cho KYC" },
                { "Da KYC" },
                { "Tu choi" }
        };
    }

    @Test(groups = { "partner-worker", "worker-profile", "worker-profile-filter" },
            description = "WORKER-PROFILE-FILTER-001: Bo loc hien thi trang thai KYC va chon ngay")
    public void workerFilterShowsKycStatusAndDateControls() {
        workerProfilePage.openFilter();

        Assert.assertTrue(workerProfilePage.hasKycStatusFilterOptions(),
                "Thieu tuy chon loc trang thai KYC.");
        Assert.assertTrue(workerProfilePage.hasDateFilterCalendar(),
                "Thieu lich loc theo ngay.");
    }

    @Test(dataProvider = "kycStatusFilters",
            groups = { "partner-worker", "worker-profile", "worker-profile-filter" },
            description = "WORKER-PROFILE-FILTER-002: Chon duoc bo loc trang thai KYC cua tho")
    public void workerKycStatusFilterCanBeSelected(String statusLabel) {
        workerProfilePage.openFilter();
        workerProfilePage.selectKycStatus(statusLabel);

        Assert.assertTrue(workerProfilePage.hasExpectedTableHeaders(),
                "Thieu tieu de bang tho sau khi loc theo trang thai: " + statusLabel);
        Assert.assertTrue(workerProfilePage.hasWorkerRows(),
                "Dong tho khong tai sau khi loc theo trang thai: " + statusLabel);
        Assert.assertTrue(workerProfilePage.visibleRowsMatchKycStatus(statusLabel),
                "Dong tho khong khop bo loc trang thai KYC: " + statusLabel);
    }

    @Test(groups = { "partner-worker", "worker-profile", "worker-profile-filter" },
            description = "WORKER-PROFILE-FILTER-003: Reset duoc bo loc trang thai KYC cua tho")
    public void workerKycStatusFilterCanBeResetSafely() {
        workerProfilePage.openFilter();
        workerProfilePage.selectKycStatus("Da KYC");

        workerProfilePage.resetFilter();
        workerProfilePage.openFilter();

        Assert.assertTrue(workerProfilePage.hasKycStatusFilterOptions(),
                "Thieu tuy chon trang thai KYC sau khi reset.");
        Assert.assertTrue(workerProfilePage.hasWorkerRows(),
                "Thieu dong tho sau khi reset.");
    }

    @Test(groups = { "partner-worker", "worker-profile", "worker-profile-filter" },
            description = "WORKER-PROFILE-FILTER-004: Lich loc theo ngay co the chuyen thang")
    public void workerDateFilterCanNavigateMonth() {
        workerProfilePage.openFilter();

        Assert.assertTrue(workerProfilePage.dateFilterCanNavigateMonth(),
                "Lich loc theo ngay khong chuyen thang duoc.");
    }

    @Test(groups = { "partner-worker", "worker-profile", "worker-profile-filter" },
            description = "WORKER-PROFILE-FILTER-005: Bo loc ngay khong chon ngay tuong lai")
    public void workerDateFilterDoesNotUseFutureDate() {
        workerProfilePage.openFilter();

        Assert.assertTrue(workerProfilePage.latestPastDateIsAvailableForFiltering(),
                "Khong co ngay qua khu hop le de loc.");
    }

    @Test(groups = { "partner-worker", "worker-profile", "worker-profile-filter" },
            description = "WORKER-PROFILE-FILTER-006: Ho so tho loc duoc theo mot ngay")
    public void workerDateFilterCanApplySingleDay() {
        workerProfilePage.openFilter();
        workerProfilePage.selectSinglePastDateInDateFilter();

        Assert.assertTrue(workerProfilePage.hasExpectedTableHeaders(),
                "Thieu tieu de bang tho sau khi loc theo mot ngay.");
        Assert.assertTrue(workerProfilePage.hasDateFilterResultLoaded(),
                "Ket qua loc mot ngay chua tai xong.");
    }

    @Test(groups = { "partner-worker", "worker-profile", "worker-profile-filter" },
            description = "WORKER-PROFILE-FILTER-007: Ho so tho loc duoc theo nhieu ngay")
    public void workerDateFilterCanApplyMultipleDays() {
        workerProfilePage.openFilter();
        workerProfilePage.selectMultiplePastDatesInDateFilter();

        Assert.assertTrue(workerProfilePage.hasMultipleDateSelectionResult(),
                "Ket qua loc nhieu ngay khong khop trang thai danh sach tho mong doi.");
    }
}
