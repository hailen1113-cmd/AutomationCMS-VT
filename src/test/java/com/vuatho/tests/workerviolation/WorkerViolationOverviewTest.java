package com.vuatho.tests.workerviolation;

import com.vuatho.testcases.WorkerViolationTestCases;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.workerviolation.WorkerViolationTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Test dieu huong, tong quan va ba KPI cua Quan li tho vi pham. */
public class WorkerViolationOverviewTest extends WorkerViolationTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerViolationOverviewTest.class,
                "Bo test tong quan tho vi pham", "Kiem tra trang va KPI");
    }

    @Test(groups = {"violation-worker", "overview"},
            description = WorkerViolationTestCases.WORKER_VIOLATION_005)
    public void pageOpensAtExpectedRoute() {
        Assert.assertTrue(workerViolationPage.isLoaded(), "Trang Quan li tho vi pham chua tai dung.");
    }

    @Test(groups = {"violation-worker", "overview"},
            description = WorkerViolationTestCases.WORKER_VIOLATION_006)
    public void allSummaryCardsAreVisible() {
        Assert.assertTrue(workerViolationPage.hasSummaryCards(), "Thieu KPI Tho bi phat, Tong tien phat hoac Ti le thu hoi.");
    }

    @Test(groups = {"violation-worker", "overview"},
            description = WorkerViolationTestCases.WORKER_VIOLATION_007)
    public void penalizedWorkerTotalIsValid() {
        Assert.assertTrue(workerViolationPage.summaryValueAfter("Thợ bị phạt") >= 0,
                "Tong tho bi phat khong phai so khong am.");
    }

    @Test(groups = {"violation-worker", "overview"},
            description = WorkerViolationTestCases.WORKER_VIOLATION_008)
    public void penaltyMoneyTotalIsValid() {
        Assert.assertTrue(workerViolationPage.summaryValueAfter("Tổng tiền phạt") >= 0,
                "Tong tien phat khong phai so khong am.");
    }

    @Test(groups = {"violation-worker", "overview"},
            description = WorkerViolationTestCases.WORKER_VIOLATION_009)
    public void recoveryRateIsWithinPercentageRange() {
        double percentage = workerViolationPage.recoveryPercentage();
        Assert.assertTrue(percentage >= 0 && percentage <= 100,
                "Ti le thu hoi phai nam trong khoang 0-100%, thuc te: " + percentage);
    }

    @Test(groups = {"violation-worker", "overview"},
            description = WorkerViolationTestCases.WORKER_VIOLATION_010)
    public void tableAndResultTotalAreLoaded() {
        Assert.assertTrue(workerViolationPage.hasExpectedHeaders(), "Thieu cot bang tho vi pham.");
        Assert.assertTrue(workerViolationPage.totalDisplayed() >= 0, "Khong doc duoc Tong hien thi.");
    }
}
