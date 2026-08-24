package com.vuatho.tests.dashboard;

import com.vuatho.testcases.DashboardTestCases;

import com.vuatho.support.dashboard.DashboardTestSupport;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Thực thi các test case giao diện Dashboard được nạp động từ workbook.
 */
public class WorkbookUiTest extends DashboardTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkbookUiTest.class,
                "Bo test workbook UI Dashboard ERP",
                "Kiem tra cac case workbook tren Dashboard");
    }

    /**
     * Thực hiện xử lý overview cards trong luồng kiểm thử.
     * @return kết quả overview cards sau khi xử lý
     */
    @DataProvider(name = "overviewCards", parallel = false)
    public Object[][] overviewCards() {
        return new Object[][]{
                {"OVD-001", "Đơn dịch vụ"},
                {"OVD-002", "Số lượng người dùng"},
                {"OVD-003", "Số lượng thợ"},
                {"OVD-004", "Nghiệp vụ"},
                {"OVD-005", "Ngành nghề"},
                {"OVD-006", "Nền tảng Vua Thợ"}
        };
    }

    /**
     * Thực thi test “Workbook OVD-001..006: Card tong quan hien thi va co gia tri so” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param caseId giá trị case id được truyền vào
     * @param label giá trị label được truyền vào
     */
    @Test(dataProvider = "overviewCards",
            description = DashboardTestCases.DASH_021)
    public void overviewCardHasNumericValue(String caseId, String label) {
        Assert.assertTrue(dashboard.summaryCardHasNumericValue(label),
                caseId + " thieu hoac khong co gia tri so: " + label);
    }

    /**
     * Thực hiện xử lý dashboard sections trong luồng kiểm thử.
     * @return kết quả dashboard sections sau khi xử lý
     */
    @DataProvider(name = "dashboardSections", parallel = false)
    public Object[][] dashboardSections() {
        return new Object[][]{
                {"GCC-001", "Vua Thợ Trên Toàn Cầu"},
                {"UST-001", "Danh Sách Người Dùng"},
                {"INS-001", "Danh Sách Ngành Nghề"}
        };
    }

    /**
     * Thực thi test “Workbook dashboard section hien thi” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param caseId giá trị case id được truyền vào
     * @param title giá trị title được truyền vào
     */
    @Test(dataProvider = "dashboardSections",
            description = DashboardTestCases.DASH_022)
    public void dashboardSectionIsDisplayed(String caseId, String title) {
        Assert.assertTrue(dashboard.sectionIsVisible(title),
                caseId + " section khong hien thi: " + title);
    }

    /**
     * Thực hiện xử lý period selections trong luồng kiểm thử.
     * @return kết quả period selections sau khi xử lý
     */
    @DataProvider(name = "periodSelections", parallel = false)
    public Object[][] periodSelections() {
        return new Object[][]{
                {"UST-004", 0, "Ngày"}, {"UST-005", 0, "Tuần"},
                {"UST-006", 0, "Tháng"}, {"UST-007", 0, "Quý"},
                {"UST-008", 0, "Năm"}, {"INS-004", 1, "Ngày"},
                {"INS-005", 1, "Tuần"}, {"INS-006", 1, "Tháng"},
                {"INS-007", 1, "Quý"}, {"INS-008", 1, "Năm"}
        };
    }

    /**
     * Thực thi test “Workbook period selector doi duoc active state” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param caseId giá trị case id được truyền vào
     * @param groupIndex giá trị group index được truyền vào
     * @param label giá trị label được truyền vào
     */
    @Test(dataProvider = "periodSelections",
            description = DashboardTestCases.DASH_023)
    public void dashboardPeriodCanBeSelected(String caseId, int groupIndex, String label) {
        dashboard.selectPeriod(groupIndex, label);
        Assert.assertTrue(dashboard.periodIsSelected(groupIndex, label),
                caseId + " period khong active: " + label);
    }
}
