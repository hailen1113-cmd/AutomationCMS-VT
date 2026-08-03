package com.vuatho.tests.workerviolation;

import com.vuatho.testcases.WorkerViolationTestCases;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.workerviolation.WorkerViolationTestSupport;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

/** Chỉ kiểm tra chức năng phân trang danh sách thợ vi phạm. */
public class TablePaginationTest extends WorkerViolationTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TablePaginationTest.class,
                "Bo test phan trang tho vi pham",
                "Kiem tra pagination danh sach tho vi pham");
    }

    @Test(groups = {"violation-worker", "pagination"},
            description = WorkerViolationTestCases.WORKER_VIOLATION_032)
    public void firstPageHasCorrectBoundaryState() {
        workerViolationPage.scrollToPagination();
        Assert.assertEquals(workerViolationPage.activePage(), 1,
                "Mac dinh danh sach khong o trang 1.");
        Assert.assertTrue(workerViolationPage.previousDisabled(),
                "Nut Previous phai bi khoa tai trang dau.");
    }

    @Test(groups = {"violation-worker", "pagination"},
            description = WorkerViolationTestCases.WORKER_VIOLATION_033)
    public void nextAndPreviousNavigateExactlyOnePage() {
        if (workerViolationPage.availablePages().stream().noneMatch(page -> page == 2)) {
            throw new SkipException("Du lieu chi co mot trang, khong the test Next/Previous.");
        }

        workerViolationPage.nextPage();
        Assert.assertEquals(workerViolationPage.activePage(), 2,
                "Nut Next khong chuyen den trang 2.");

        workerViolationPage.previousPage();
        Assert.assertEquals(workerViolationPage.activePage(), 1,
                "Nut Previous khong quay ve trang 1.");
    }

    @Test(groups = {"violation-worker", "pagination"},
            description = WorkerViolationTestCases.WORKER_VIOLATION_034)
    public void directPageSelectionWorks() {
        List<Integer> pages = workerViolationPage.availablePages();
        int targetPage = pages.stream()
                .filter(page -> page > 1)
                .findFirst()
                .orElseThrow(() -> new SkipException("Khong co trang thu hai de chon truc tiep."));

        workerViolationPage.goToPage(targetPage);
        Assert.assertEquals(workerViolationPage.activePage(), targetPage,
                "Danh sach khong active dung so trang da chon.");
    }

    @Test(groups = {"violation-worker", "pagination"},
            description = WorkerViolationTestCases.WORKER_VIOLATION_035)
    public void totalRecordCountRemainsStableAcrossPages() {
        if (workerViolationPage.availablePages().stream().noneMatch(page -> page == 2)) {
            throw new SkipException("Du lieu chi co mot trang, khong the doi chieu tong ban ghi.");
        }

        long totalBefore = workerViolationPage.totalDisplayed();
        Assert.assertTrue(totalBefore >= 0, "Khong doc duoc Tong hien thi tai trang 1.");

        workerViolationPage.goToPage(2);
        Assert.assertEquals(workerViolationPage.totalDisplayed(), totalBefore,
                "Tong hien thi thay doi sau khi chuyen trang.");
    }
}
