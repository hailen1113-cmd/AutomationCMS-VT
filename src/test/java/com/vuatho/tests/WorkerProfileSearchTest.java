package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.WorkerProfilePage;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class WorkerProfileSearchTest extends BaseTest {
    private static final int SEARCH_SAMPLE_SIZE = 5;

    private WorkerProfilePage workerProfilePage;

    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfileSearchTest.class,
                "Bo test tim kiem ho so tho ERP",
                "Kiem tra tim kiem ho so tho");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareWorkerProfilePage() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Khong dang nhap duoc truoc khi kiem tra tim kiem ho so tho.");
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
            System.out.println("[WorkerProfileSearch] Bo qua don dep; testcase tiep theo se mo lai trang ho so tho.");
        }
    }

    @Test(priority = 6, groups = { "partner-worker", "worker-profile", "worker-search" },
            description = "WORKER-PROFILE-SEARCH-001: O tim kiem tho co the reset")
    public void workerSearchCanBeReset() {
        workerProfilePage.searchAndReset(workerProfilePage.firstVisibleWorkerName());

        Assert.assertTrue(workerProfilePage.searchInputIsEmpty(),
                "Reset tim kiem tho khong xoa noi dung o nhap.");
    }

    @Test(priority = 2, groups = { "partner-worker", "worker-profile", "worker-search" },
            description = "WORKER-PROFILE-SEARCH-MODE-001: Tim kiem tho co tuy chon ten va SDT")
    public void workerSearchModeOptionsAreAvailable() {
        Assert.assertTrue(workerProfilePage.hasSearchModeOptions(),
                "Dropdown kieu tim kiem tho thieu tuy chon ten hoac SDT.");
    }

    @Test(priority = 3, groups = { "partner-worker", "worker-profile", "worker-search" },
            description = "WORKER-PROFILE-SEARCH-MODE-002: Kieu tim kiem tho doi duoc giua ten va SDT")
    public void workerSearchModeCanSwitchBetweenNameAndPhone() {
        workerProfilePage.selectSearchMode("ten");
        Assert.assertTrue(TextNormalizer.normalize(workerProfilePage.selectedSearchMode()).contains("ten"),
                "Kieu tim kiem tho khong chuyen sang ten.");

        workerProfilePage.selectSearchMode("SDT");
        String selectedMode = TextNormalizer.normalize(workerProfilePage.selectedSearchMode());
        Assert.assertTrue(selectedMode.contains("sdt"),
                "Kieu tim kiem tho khong chuyen sang SDT.");
    }

    @Test(priority = 4, groups = { "partner-worker", "worker-profile", "worker-search" },
            description = "WORKER-PROFILE-SEARCH-MODE-003: Tim kiem duoc 5 ten tho dau tien")
    public void workerCanSearchFirstFiveNames() {
        List<String> workerNames = workerProfilePage.firstVisibleWorkerNames(SEARCH_SAMPLE_SIZE);
        Assert.assertEquals(workerNames.size(), SEARCH_SAMPLE_SIZE,
                "Khong lay du dung 5 ten tho truoc khi tim kiem. Ten: " + workerNames);

        for (int index = 0; index < workerNames.size(); index++) {
            String workerName = workerNames.get(index);
            System.out.printf("[WorkerSearch][Name] %d/%d: %s%n", index + 1, workerNames.size(), workerName);
            workerProfilePage.searchByMode("ten", workerName);
            Assert.assertTrue(workerProfilePage.visibleRowsAllContainSearchTerm(workerName),
                    "Ket qua tim theo ten tho khong phai dong nao cung chua: " + workerName);
            workerProfilePage.clearSearch();
        }

        Assert.assertTrue(workerProfilePage.searchInputIsEmpty(),
                "O tim kiem tho khong duoc xoa sau khi tim theo ten.");
    }

    @Test(priority = 5, groups = { "partner-worker", "worker-profile", "worker-search" },
            description = "WORKER-PROFILE-SEARCH-MODE-004: Tim kiem duoc 5 SDT tho dau tien")
    public void workerCanSearchFirstFivePhoneNumbers() {
        List<String> workerPhones = workerProfilePage.firstVisibleWorkerPhoneSearchTerms(SEARCH_SAMPLE_SIZE);
        Assert.assertEquals(workerPhones.size(), SEARCH_SAMPLE_SIZE,
                "Khong lay du dung 5 SDT tho truoc khi tim kiem. SDT: " + workerPhones);

        for (int index = 0; index < workerPhones.size(); index++) {
            String workerPhone = workerPhones.get(index);
            System.out.printf("[WorkerSearch][Phone] %d/%d: %s%n", index + 1, workerPhones.size(), workerPhone);
            workerProfilePage.searchByMode("SDT", workerPhone);
            Assert.assertTrue(workerProfilePage.visibleRowsAllContainSearchTerm(workerPhone),
                    "Ket qua tim theo SDT tho khong phai dong nao cung chua: " + workerPhone);
            workerProfilePage.clearSearch();
        }

        Assert.assertTrue(workerProfilePage.searchInputIsEmpty(),
                "O tim kiem tho khong duoc xoa sau khi tim theo ten/SDT.");
    }
}
