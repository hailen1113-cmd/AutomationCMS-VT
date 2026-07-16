package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DashboardWorkbookUiTest extends DashboardTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(DashboardWorkbookUiTest.class,
                "Bo test workbook UI Dashboard ERP",
                "Kiem tra cac case workbook tren Dashboard");
    }

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

    @Test(dataProvider = "overviewCards",
            description = "Workbook OVD-001..006: Card tong quan hien thi va co gia tri so")
    public void overviewCardHasNumericValue(String caseId, String label) {
        Assert.assertTrue(dashboard.summaryCardHasNumericValue(label),
                caseId + " thieu hoac khong co gia tri so: " + label);
    }

    @DataProvider(name = "dashboardSections", parallel = false)
    public Object[][] dashboardSections() {
        return new Object[][]{
                {"GCC-001", "Vua Thá»£ TrÃªn ToÃ n Cáº§u"},
                {"UST-001", "Danh SÃ¡ch NgÆ°á»i DÃ¹ng"},
                {"INS-001", "Danh SÃ¡ch NgÃ nh Nghá»"}
        };
    }

    @Test(dataProvider = "dashboardSections",
            description = "Workbook dashboard section hien thi")
    public void dashboardSectionIsDisplayed(String caseId, String title) {
        Assert.assertTrue(dashboard.sectionIsVisible(title),
                caseId + " section khong hien thi: " + title);
    }

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

    @Test(dataProvider = "periodSelections",
            description = "Workbook period selector doi duoc active state")
    public void dashboardPeriodCanBeSelected(String caseId, int groupIndex, String label) {
        dashboard.selectPeriod(groupIndex, label);
        Assert.assertTrue(dashboard.periodIsSelected(groupIndex, label),
                caseId + " period khong active: " + label);
    }
}
