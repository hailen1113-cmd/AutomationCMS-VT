package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerViolationPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

/** Test popup Tien phat theo ngay va cac khoang thong ke. */
public class WorkerViolationStatisticsTest extends WorkerViolationTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerViolationStatisticsTest.class,
                "Bo test thong ke tien phat", "Kiem tra popup thong ke");
    }

    @Test(groups = {"violation-worker", "statistics"},
            description = "WORKER-VIOLATION-STAT-001: Popup co du cau truc thong ke bat buoc")
    public void statisticsDialogHasRequiredStructure() {
        workerViolationPage.openStatistics();
        Assert.assertTrue(workerViolationPage.isStatisticsDialogOpen(), "Popup Tien phat theo ngay khong mo.");
        Assert.assertTrue(workerViolationPage.statisticsHasCoreContent(),
                "Popup thieu tieu de hoac chu giai Da thu/Chua thu.");
        Assert.assertTrue(workerViolationPage.statisticsHasPeriodControls(),
                "Popup thieu mot trong ba control Tuan nay, Thang nay, Tuy chinh.");
        Assert.assertTrue(workerViolationPage.statisticsDateRange().isPresent(),
                "Popup khong hien thi khoang ngay dd/MM/yyyy hop le.");
    }

    @Test(groups = {"violation-worker", "statistics"},
            description = "WORKER-VIOLATION-STAT-002: Tuan nay bao phu dung bay ngay")
    public void currentWeekUsesSevenDayRange() {
        workerViolationPage.openStatistics().selectStatisticsPeriod("Tuần này");
        WorkerViolationPage.StatisticsDateRange range = requiredDateRange();
        Assert.assertEquals(range.inclusiveDays(), 7L,
                "Khoang Tuan nay phai gom dung 7 ngay: " + range);
    }

    @Test(groups = {"violation-worker", "statistics"},
            description = "WORKER-VIOLATION-STAT-003: Thang nay bao phu mot thang lich")
    public void currentMonthUsesCalendarMonthRange() {
        workerViolationPage.openStatistics().selectStatisticsPeriod("Tháng này");
        WorkerViolationPage.StatisticsDateRange range = requiredDateRange();
        Assert.assertEquals(range.from().getDayOfMonth(), 1, "Thang nay phai bat dau tu ngay 01.");
        Assert.assertEquals(range.from().getMonth(), range.to().getMonth(),
                "Khoang Thang nay khong duoc vuot qua thang ke tiep.");
        Assert.assertTrue(range.inclusiveDays() >= 28 && range.inclusiveDays() <= 31,
                "Khoang Thang nay phai co 28-31 ngay: " + range);
    }

    @Test(groups = {"violation-worker", "statistics"},
            description = "WORKER-VIOLATION-STAT-004: Chuyen Tuan-Thang-Tuan cap nhat va phuc hoi range")
    public void switchingPeriodsUpdatesAndRestoresDateRange() {
        workerViolationPage.openStatistics().selectStatisticsPeriod("Tuần này");
        WorkerViolationPage.StatisticsDateRange week = requiredDateRange();
        workerViolationPage.selectStatisticsPeriod("Tháng này");
        WorkerViolationPage.StatisticsDateRange month = requiredDateRange();
        Assert.assertNotEquals(month, week, "Click Thang nay khong cap nhat khoang ngay.");

        workerViolationPage.selectStatisticsPeriod("Tuần này");
        Assert.assertEquals(requiredDateRange(), week, "Quay lai Tuan nay khong phuc hoi khoang ngay ban dau.");
    }

    @Test(groups = {"violation-worker", "statistics"},
            description = "WORKER-VIOLATION-STAT-005: Tuy chinh hien thi control chon ngay")
    public void customPeriodShowsDateControls() {
        workerViolationPage.openStatistics().selectStatisticsPeriod("Tùy chỉnh");
        Assert.assertTrue(workerViolationPage.statisticsCustomDateControlsVisible(),
                "Click Tuy chinh khong hien thi control chon khoang ngay.");
    }

    @Test(groups = {"violation-worker", "statistics"},
            description = "WORKER-VIOLATION-STAT-006: Tong phat sinh bang Da thu cong Chua thu")
    public void moneySummaryIsNonNegativeAndReconciled() {
        workerViolationPage.openStatistics();
        WorkerViolationPage.StatisticsMoney money = workerViolationPage.statisticsMoney()
                .orElseThrow(() -> new AssertionError("Khong doc duoc ba KPI Tong phat sinh/Da thu/Chua thu."));
        Assert.assertTrue(money.total() >= 0 && money.collected() >= 0 && money.uncollected() >= 0,
                "KPI tien khong duoc la so am: " + money);
        Assert.assertEquals(money.collected() + money.uncollected(), money.total(),
                "Tong phat sinh khong bang Da thu + Chua thu: " + money);
    }

    @Test(groups = {"violation-worker", "statistics"},
            description = "WORKER-VIOLATION-STAT-007: Bieu do thong ke duoc render")
    public void statisticsChartIsActuallyRendered() {
        workerViolationPage.openStatistics();
        Assert.assertTrue(workerViolationPage.statisticsChartIsRendered(),
                "Popup co chu giai nhung khong render vung bieu do du kich thuoc.");

        String tooltip = hoverLargestStatisticsColumn(true);

        Assert.assertNotNull(tooltip,
                "Hover vao cot du lieu nhung tooltip khong hien ngay, Da thu va Chua thu.");
    }

    @Test(groups = {"violation-worker", "statistics"},
            description = "WORKER-VIOLATION-STAT-008: Dong popup quay lai danh sach")
    public void statisticsDialogCanBeClosed() {
        workerViolationPage.openStatistics();
        workerViolationPage.closeDialog();
        Assert.assertFalse(workerViolationPage.isStatisticsDialogOpen(), "Popup van con mo sau thao tac dong.");
        Assert.assertTrue(workerViolationPage.hasExpectedHeaders(), "Danh sach khong con san sang sau khi dong popup.");
    }

    @Test(groups = {"violation-worker", "statistics", "custom-date"},
            description = "WORKER-VIOLATION-STAT-009: Range tuy chinh co rang buoc min/max hop le")
    public void customDateInputsExposeValidConstraints() {
        workerViolationPage.openStatistics().selectStatisticsPeriod("Tùy chỉnh");
        WorkerViolationPage.CustomDateState state = requiredCustomDateState();
        Assert.assertNotNull(state.toMax(), "Ngay ket thuc thieu gioi han max.");
        Assert.assertFalse(state.toMax().isAfter(java.time.LocalDate.now()),
                "Ngay ket thuc cho phep vuot qua ngay hien tai: " + state.toMax());
        Assert.assertEquals(state.toMin(), state.from(),
                "Min cua ngay ket thuc phai bang ngay bat dau dang chon.");
    }

    @Test(groups = {"violation-worker", "statistics", "custom-date"},
            description = "WORKER-VIOLATION-STAT-010: Ap dung range tuy chinh nhieu ngay")
    public void validMultiDayCustomRangeUpdatesChart() {
        workerViolationPage.openStatistics().selectStatisticsPeriod("Tùy chỉnh");
        java.time.LocalDate to = requiredCustomDateState().toMax();
        java.time.LocalDate from = to.minusDays(2);
        workerViolationPage.setStatisticsCustomDateRange(from, to);
        Assert.assertTrue(workerViolationPage.statisticsCustomApplyEnabled(),
                "Nut Ap dung khong mo khoa voi range hop le.");
        workerViolationPage.applyStatisticsCustomDateRange();
        Assert.assertTrue(workerViolationPage.statisticsChartCovers(from, to),
                "Truc X bieu do khong khop range tuy chinh " + from + " -> " + to
                        + ", labels=" + workerViolationPage.statisticsChartDateLabels());
    }

    @Test(groups = {"violation-worker", "statistics", "custom-date"},
            description = "WORKER-VIOLATION-STAT-011: Ap dung range tuy chinh mot ngay")
    public void validSingleDayCustomRangeUpdatesChart() {
        workerViolationPage.openStatistics().selectStatisticsPeriod("Tùy chỉnh");
        java.time.LocalDate date = requiredCustomDateState().toMax().minusDays(1);
        workerViolationPage.setStatisticsCustomDateRange(date, date);
        Assert.assertTrue(workerViolationPage.statisticsCustomApplyEnabled(),
                "Nut Ap dung khong mo khoa voi range mot ngay.");
        workerViolationPage.applyStatisticsCustomDateRange();
        Assert.assertTrue(workerViolationPage.statisticsChartCovers(date, date),
                "Bieu do range mot ngay khong hien dung mot moc: "
                        + workerViolationPage.statisticsChartDateLabels());
    }

    @Test(groups = {"violation-worker", "statistics", "custom-date"},
            description = "WORKER-VIOLATION-STAT-012: Khong ap dung ngay bat dau lon hon ngay ket thuc")
    public void reversedCustomRangeCannotBeApplied() {
        workerViolationPage.openStatistics().selectStatisticsPeriod("Tùy chỉnh");
        java.time.LocalDate endLimit = requiredCustomDateState().toMax();
        java.time.LocalDate from = endLimit.minusDays(1);
        java.time.LocalDate invalidTo = from.minusDays(1);
        workerViolationPage.attemptStatisticsCustomDateRange(from, invalidTo);
        WorkerViolationPage.CustomDateState actual = requiredCustomDateState();
        boolean rejectedByControl = !from.equals(actual.from()) || !invalidTo.equals(actual.to());
        Assert.assertTrue(rejectedByControl || !workerViolationPage.statisticsCustomApplyEnabled(),
                "Range dao nguoc van cho phep Ap dung: " + actual);
    }

    @Test(groups = {"violation-worker", "statistics", "custom-date"},
            description = "WORKER-VIOLATION-STAT-013: Khong ap dung ngay ket thuc trong tuong lai")
    public void futureCustomRangeCannotBeApplied() {
        workerViolationPage.openStatistics().selectStatisticsPeriod("Tùy chỉnh");
        java.time.LocalDate max = requiredCustomDateState().toMax();
        java.time.LocalDate future = max.plusDays(1);
        workerViolationPage.attemptStatisticsCustomDateRange(max, future);
        WorkerViolationPage.CustomDateState actual = requiredCustomDateState();
        boolean rejectedByControl = !future.equals(actual.to());
        Assert.assertTrue(rejectedByControl || !workerViolationPage.statisticsCustomApplyEnabled(),
                "Ngay tuong lai van cho phep Ap dung: " + actual);
    }

    private WorkerViolationPage.StatisticsDateRange requiredDateRange() {
        return workerViolationPage.statisticsDateRange()
                .orElseThrow(() -> new AssertionError("Khong doc duoc khoang ngay thong ke."));
    }

    private WorkerViolationPage.CustomDateState requiredCustomDateState() {
        return workerViolationPage.statisticsCustomDateState()
                .orElseThrow(() -> new AssertionError("Khong doc duoc hai input ngay tuy chinh va rang buoc min/max."));
    }

    /**
     * Moi testcase de popup chart mo se hover vao cot du lieu truoc khi
     * WorkerViolationTestSupport dong popup. Nho vay tooltip duoc hien thi
     * trong tat ca case co du lieu, khong chi rieng testcase STAT-007.
     */
    @AfterMethod(alwaysRun = true)
    public void hoverStatisticsColumnWhenPresent() {
        if (workerViolationPage == null || !workerViolationPage.isStatisticsDialogOpen()) return;
        try {
            hoverLargestStatisticsColumn(false);
        } catch (RuntimeException ignored) {
            // Khong che mat ket qua testcase neu chart khong co cot du lieu.
        }
    }

    private String hoverLargestStatisticsColumn(boolean required) {
        WebElement dataColumn = visibleStatisticsColumns().stream()
                .max(Comparator.comparingInt(column -> column.getRect().getHeight()))
                .orElse(null);
        if (dataColumn == null) {
            if (required) {
                throw new AssertionError("Bieu do khong co cot du lieu de thuc hien hover.");
            }
            return null;
        }

        new Actions(driver)
                .moveToElement(dataColumn)
                .pause(Duration.ofSeconds(2))
                .perform();

        if (!required) return null;
        return new WebDriverWait(driver, Duration.ofSeconds(5)).until(d ->
                d.findElements(By.cssSelector(".recharts-tooltip-wrapper")).stream()
                        .filter(WebElement::isDisplayed)
                        .map(WebElement::getText)
                        .filter(text -> text != null
                                && text.matches("(?s).*\\d{2}/\\d{2}/\\d{4}.*")
                                && text.contains("\u0110\u00e3 thu")
                                && text.contains("Ch\u01b0a thu"))
                        .findFirst()
                        .orElse(null));
    }

    private List<WebElement> visibleStatisticsColumns() {
        return driver.findElements(By.cssSelector(
                        ".recharts-bar-rectangle path, .recharts-bar-rectangle rect"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(column -> column.getRect().getWidth() > 1
                        && column.getRect().getHeight() > 1)
                .toList();
    }
}
