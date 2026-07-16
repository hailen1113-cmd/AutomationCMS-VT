package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.UserProfilePage;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class UserProfileSearchTest extends BaseTest {
    private static final int SEARCH_SAMPLE_SIZE = 5;

    private UserProfilePage userProfilePage;

    public static void main(String[] args) {
        TestNgRunner.run(UserProfileSearchTest.class,
                "Bo test tim kiem ho so nguoi dung ERP",
                "Kiem tra tim kiem ho so nguoi dung");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareUserProfilePage() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Khong dang nhap duoc truoc khi kiem tra tim kiem nguoi dung.");
        userProfilePage = new UserProfilePage(driver).openFromMenu();
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUserProfileState() {
        try {
            if (userProfilePage != null) {
                userProfilePage.restoreDefaultListIfNeeded();
            }
        } catch (RuntimeException exception) {
            System.out.println("[UserProfileSearch] Bo qua don dep; testcase tiep theo se mo lai trang nguoi dung.");
        }
    }

    @Test(priority = 1, groups = { "user-profile", "user-search" },
            description = "USER-PROFILE-SEARCH-MODE-001: Tim kiem nguoi dung co tuy chon ten va SDT")
    public void userSearchModeOptionsAreAvailable() {
        Assert.assertTrue(userProfilePage.hasSearchModeOptions(),
                "Dropdown kieu tim kiem nguoi dung thieu tuy chon ten hoac SDT.");
    }

    @Test(priority = 2, groups = { "user-profile", "user-search" },
            description = "USER-PROFILE-SEARCH-MODE-002: Kieu tim kiem nguoi dung doi duoc giua ten va SDT")
    public void userSearchModeCanSwitchBetweenNameAndPhone() {
        userProfilePage.selectSearchMode("ten");
        Assert.assertTrue(TextNormalizer.normalize(userProfilePage.selectedSearchMode()).contains("ten"),
                "Kieu tim kiem nguoi dung khong chuyen sang ten.");

        userProfilePage.selectSearchMode("SDT");
        String selectedMode = TextNormalizer.normalize(userProfilePage.selectedSearchMode());
        Assert.assertTrue(selectedMode.contains("sdt"),
                "Kieu tim kiem nguoi dung khong chuyen sang SDT.");
    }

    @Test(priority = 3, groups = { "user-profile", "user-search" },
            description = "USER-PROFILE-SEARCH-MODE-003: Tim kiem duoc 5 ten nguoi dung dau tien")
    public void userCanSearchFirstFiveNames() {
        List<String> userNames = userProfilePage.firstVisibleUserNames(SEARCH_SAMPLE_SIZE);
        Assert.assertEquals(userNames.size(), SEARCH_SAMPLE_SIZE,
                "Khong lay du dung 5 ten nguoi dung truoc khi tim kiem. Ten: " + userNames);

        searchAllTermsByMode("ten", "Ten", userNames);

        Assert.assertTrue(userProfilePage.searchInputIsEmpty(),
                "O tim kiem nguoi dung khong duoc xoa sau khi tim theo ten.");
    }

    @Test(priority = 4, groups = { "user-profile", "user-search" },
            description = "USER-PROFILE-SEARCH-MODE-004: Tim kiem duoc 5 SDT nguoi dung dau tien")
    public void userCanSearchFirstFivePhoneNumbers() {
        List<String> userPhones = userProfilePage.firstVisibleUserPhoneSearchTerms(SEARCH_SAMPLE_SIZE);
        Assert.assertEquals(userPhones.size(), SEARCH_SAMPLE_SIZE,
                "Khong lay du dung 5 SDT nguoi dung truoc khi tim kiem. SDT: " + userPhones);

        searchAllTermsByMode("SDT", "SDT", userPhones);

        Assert.assertTrue(userProfilePage.searchInputIsEmpty(),
                "O tim kiem nguoi dung khong duoc xoa sau khi tim theo SDT.");
    }

    private void searchAllTermsByMode(String modeLabel, String logLabel, List<String> searchTerms) {
        SoftAssert softAssert = new SoftAssert();
        System.out.printf("[UserSearch][%s] Danh sach can tim: %s%n", logLabel, searchTerms);

        for (int index = 0; index < searchTerms.size(); index++) {
            String searchTerm = searchTerms.get(index);
            System.out.printf("[UserSearch][%s] Bat dau %d/%d: %s%n",
                    logLabel, index + 1, searchTerms.size(), searchTerm);
            try {
                userProfilePage.restoreDefaultListIfNeeded();
                userProfilePage.searchByMode(modeLabel, searchTerm);
                softAssert.assertTrue(userProfilePage.visibleRowsContainSearchTerm(searchTerm),
                        "Ket qua tim theo " + logLabel
                                + " khong co dong nao chua gia tri: " + searchTerm);
            } finally {
                userProfilePage.clearSearch();
                System.out.printf("[UserSearch][%s] Hoan tat %d/%d: %s%n",
                        logLabel, index + 1, searchTerms.size(), searchTerm);
            }
        }

        softAssert.assertAll();
    }
}
