package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Thực thi các test case giao diện Dashboard được nạp động từ workbook.
 */
public class DashboardWorkbookUiTest extends DashboardTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(DashboardWorkbookUiTest.class,
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
                {"OVD-001", "ÄÆ¡n dá»‹ch vá»¥"},
                {"OVD-002", "Sá»‘ lÆ°á»£ng ngÆ°á»i dÃ¹ng"},
                {"OVD-003", "Sá»‘ lÆ°á»£ng thá»£"},
                {"OVD-004", "Nghiá»‡p vá»¥"},
                {"OVD-005", "NgÃ nh nghá»"},
                {"OVD-006", "Ná»n táº£ng Vua Thá»£"}
        };
    }

    /**
     * Thực thi test “Workbook OVD-001..006: Card tong quan hien thi va co gia tri so” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param caseId giá trị case id được truyền vào
     * @param label giá trị label được truyền vào
     */
    @Test(dataProvider = "overviewCards",
            description = "Workbook OVD-001..006: Card tong quan hien thi va co gia tri so")
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
                {"GCC-001", "Vua Thá»£ TrÃªn ToÃ n Cáº§u"},
                {"UST-001", "Danh SÃ¡ch NgÆ°á»i DÃ¹ng"},
                {"INS-001", "Danh SÃ¡ch NgÃ nh Nghá»"}
        };
    }

    /**
     * Thực thi test “Workbook dashboard section hien thi” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param caseId giá trị case id được truyền vào
     * @param title giá trị title được truyền vào
     */
    @Test(dataProvider = "dashboardSections",
            description = "Workbook dashboard section hien thi")
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
                {"UST-004", 0, "NgÃ y"}, {"UST-005", 0, "Tuáº§n"},
                {"UST-006", 0, "ThÃ¡ng"}, {"UST-007", 0, "QuÃ½"},
                {"UST-008", 0, "NÄƒm"}, {"INS-004", 1, "NgÃ y"},
                {"INS-005", 1, "Tuáº§n"}, {"INS-006", 1, "ThÃ¡ng"},
                {"INS-007", 1, "QuÃ½"}, {"INS-008", 1, "NÄƒm"}
        };
    }

    /**
     * Thực thi test “Workbook period selector doi duoc active state” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param caseId giá trị case id được truyền vào
     * @param groupIndex giá trị group index được truyền vào
     * @param label giá trị label được truyền vào
     */
    @Test(dataProvider = "periodSelections",
            description = "Workbook period selector doi duoc active state")
    public void dashboardPeriodCanBeSelected(String caseId, int groupIndex, String label) {
        dashboard.selectPeriod(groupIndex, label);
        Assert.assertTrue(dashboard.periodIsSelected(groupIndex, label),
                caseId + " period khong active: " + label);
    }
}
