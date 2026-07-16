package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.WorkerProfilePage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
public class WorkerProfileTenCriteriaTest extends BaseTest {
        private WorkerProfilePage workerProfilePage;

        public static void main(String[] args) {
                TestNgRunner.run(WorkerProfileTenCriteriaTest.class,
                                "ERP Worker Profile Ten Criteria Suite",
                                "Bộ testcase tab 10 Tiêu chí Hồ Sơ Thợ");
        }

        @Override
        protected boolean reuseDriverBetweenTestMethods() {
                return true;
        }

        @BeforeMethod(alwaysRun = true)
        public void prepareWorkerCriteriaTab() {
                LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
                Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                                "Không thể đăng nhập trước khi kiểm tra tab 10 Tiêu chí.");
                workerProfilePage = new WorkerProfilePage(driver).openFromMenu();
                workerProfilePage.openFirstWorkerInformation();
                workerProfilePage.openTenCriteriaTab();
        }

        @AfterMethod(alwaysRun = true)
        public void cleanWorkerCriteriaState() {
                try {
                        if (workerProfilePage != null) {
                                workerProfilePage.closeWorkerDetailIfOpen();
                                workerProfilePage.restoreDefaultListIfNeeded();
                        }
                } catch (RuntimeException exception) {
                        System.out.println(
                                        "[WorkerProfileCriteria] Cleanup skipped; next testcase will reopen the criteria tab.");
                }
        }

        @Test(groups = { "partner-worker",
                        "worker-profile-criteria" }, description = "WORKER-PROFILE-CRITERIA-001: Tab 10 Tiêu chí hiển thị đủ bảng, tổng hợp và có nút Cập nhật")
        public void workerTenCriteriaTabShowsSummaryRowsAndEditableRules() {
                Assert.assertTrue(workerProfilePage.hasTenCriteriaSection(),
                                "Tab 10 Tiêu chí chưa hiển thị đúng nội dung chính.");
                Assert.assertEquals(workerProfilePage.tenCriteriaRowCount(), 10,
                                "Bảng 10 Tiêu chí không có đủ 10 dòng.");
                Assert.assertTrue(workerProfilePage.tenCriteriaRowsHaveStatuses(),
                                "Có tiêu chí chưa hiển thị trạng thái Đạt/Chưa đạt/Chờ xác nhận.");
                Assert.assertTrue(workerProfilePage.tenCriteriaSummaryCountsAreVisible(),
                                "Thiếu badge tổng hợp Đạt/Chưa đạt/Chờ xác nhận.");
                Assert.assertTrue(workerProfilePage.hasVisibleCriteriaUpdateButton(),
                                "Không tìm thấy nút Cập nhật nào trong tab 10 Tiêu chí.");
        }

        @Test(groups = { "partner-worker",
                        "worker-profile-criteria" }, description = "WORKER-PROFILE-CRITERIA-002: Thấy nút Cập nhật thì bấm và mở popup trạng thái")
        public void visibleCriteriaUpdateButtonCanBeClicked() {
                clickVisibleCriteriaUpdateButtonAndAssertDialog();
        }

        @Test(groups = { "partner-worker",
                        "worker-profile-criteria" }, description = "WORKER-PROFILE-CRITERIA-DEMO-001: Bấm Cập nhật và giữ popup trạng thái để quan sát")
        public void workerCriteriaUpdateButtonShowsPopupForVisualCheck() {
                workerProfilePage.clickFirstVisibleCriteriaUpdateButton();

                Assert.assertTrue(workerProfilePage.criteriaStatusDialogHasOptions(),
                                "Popup cập nhật trạng thái chưa mở sau khi bấm Cập nhật.");
                workerProfilePage.keepCriteriaStatusDialogVisible(Duration.ofSeconds(20));
                workerProfilePage.closeCriteriaStatusUpdate();
        }

        @Test(groups = { "partner-worker",
                        "worker-profile-criteria" }, description = "WORKER-PROFILE-CRITERIA-005: Hủy cập nhật trạng thái không làm đổi tiêu chí")
        public void workerCriteriaStatusCancelDoesNotChangeCurrentStatus() {
                String originalStatus = workerProfilePage.firstVisibleUpdateCriteriaStatus();
                String targetStatus = oppositeCriteriaStatus(originalStatus);

                workerProfilePage.clickFirstVisibleCriteriaUpdateButton();
                workerProfilePage.selectCriteriaStatus(targetStatus);
                workerProfilePage.cancelCriteriaStatusUpdate();

                Assert.assertEquals(workerProfilePage.firstVisibleUpdateCriteriaStatus(), originalStatus,
                                "Bấm Hủy nhưng trạng thái tiêu chí có nút Cập nhật vẫn bị thay đổi.");
        }

        @Test(groups = { "partner-worker",
                        "worker-profile-criteria" }, description = "WORKER-PROFILE-CRITERIA-006: Đóng popup cập nhật trạng thái không làm đổi tiêu chí")
        public void workerCriteriaStatusCloseDoesNotChangeCurrentStatus() {
                String originalStatus = workerProfilePage.firstVisibleUpdateCriteriaStatus();
                String targetStatus = oppositeCriteriaStatus(originalStatus);

                workerProfilePage.clickFirstVisibleCriteriaUpdateButton();
                workerProfilePage.selectCriteriaStatus(targetStatus);
                workerProfilePage.closeCriteriaStatusUpdate();

                Assert.assertEquals(workerProfilePage.firstVisibleUpdateCriteriaStatus(), originalStatus,
                                "Đóng popup nhưng trạng thái tiêu chí có nút Cập nhật vẫn bị thay đổi.");
        }

        @Test(groups = { "partner-worker",
                        "worker-profile-criteria" }, description = "WORKER-PROFILE-CRITERIA-007: Xác nhận cập nhật trạng thái làm reload và đổi trạng thái tiêu chí")
        public void workerCriteriaStatusCanBeUpdatedAndRestored() {
                String originalStatus = workerProfilePage.firstVisibleUpdateCriteriaStatus();
                String targetStatus = oppositeCriteriaStatus(originalStatus);

                workerProfilePage.updateFirstVisibleCriteriaStatus(targetStatus);

                Assert.assertEquals(workerProfilePage.firstVisibleUpdateCriteriaStatus(), targetStatus,
                                "Xác nhận cập nhật nhưng trạng thái tiêu chí có nút Cập nhật chưa đổi.");

                if (originalStatus.equals("Đạt") || originalStatus.equals("Chưa đạt")) {
                        workerProfilePage.updateFirstVisibleCriteriaStatus(originalStatus);
                        Assert.assertEquals(workerProfilePage.firstVisibleUpdateCriteriaStatus(), originalStatus,
                                        "Không khôi phục được trạng thái ban đầu của tiêu chí có nút Cập nhật.");
                }
        }

        @Test(groups = { "partner-worker",
                        "worker-profile-criteria" }, description = "WORKER-PROFILE-CRITERIA-008: Cập nhật nhiều tiêu chí có nút Cập nhật và khôi phục trạng thái ban đầu")
        public void multipleVisibleCriteriaStatusesCanBeUpdatedAndRestored() {
                int updateButtonCount = workerProfilePage.visibleCriteriaUpdateButtonCount();
                Assert.assertTrue(updateButtonCount > 1,
                                "Cần ít nhất 2 nút Cập nhật visible để kiểm tra cập nhật nhiều tiêu chí.");

                int updatedCriteriaCount = 0;
                for (int buttonIndex = 0; buttonIndex < updateButtonCount; buttonIndex++) {
                        String originalStatus = workerProfilePage.visibleUpdateCriteriaStatusAt(buttonIndex);
                        if (!canRestoreCriteriaStatus(originalStatus)) {
                                continue;
                        }

                        String targetStatus = oppositeCriteriaStatus(originalStatus);
                        workerProfilePage.updateVisibleCriteriaStatusAt(buttonIndex, targetStatus);
                        Assert.assertEquals(workerProfilePage.visibleUpdateCriteriaStatusAt(buttonIndex), targetStatus,
                                        "Tiêu chí có nút Cập nhật thứ " + (buttonIndex + 1)
                                                        + " chưa đổi sang trạng thái mới.");

                        workerProfilePage.updateVisibleCriteriaStatusAt(buttonIndex, originalStatus);
                        Assert.assertEquals(workerProfilePage.visibleUpdateCriteriaStatusAt(buttonIndex), originalStatus,
                                        "Không khôi phục được trạng thái ban đầu cho tiêu chí có nút Cập nhật thứ "
                                                        + (buttonIndex + 1) + ".");
                        updatedCriteriaCount++;
                }

                Assert.assertTrue(updatedCriteriaCount > 1,
                                "Chưa cập nhật được nhiều hơn 1 tiêu chí có thể restore trạng thái.");
        }

        private void clickVisibleCriteriaUpdateButtonAndAssertDialog() {
                workerProfilePage.clickFirstVisibleCriteriaUpdateButton();

                Assert.assertTrue(workerProfilePage.criteriaStatusDialogHasOptions(),
                                "Popup cập nhật trạng thái thiếu lựa chọn Đạt/Chưa đạt hoặc nút Hủy/Xác nhận.");
                workerProfilePage.closeCriteriaStatusUpdate();
        }

        private String oppositeCriteriaStatus(String currentStatus) {
                return "Đạt".equals(currentStatus) ? "Chưa đạt" : "Đạt";
        }

        private boolean canRestoreCriteriaStatus(String status) {
                return "Đạt".equals(status) || "Chưa đạt".equals(status);
        }
}
