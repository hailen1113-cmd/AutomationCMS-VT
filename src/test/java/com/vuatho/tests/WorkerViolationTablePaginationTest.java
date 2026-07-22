package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

/** Chỉ kiểm tra chức năng phân trang danh sách thợ vi phạm. */
public class WorkerViolationTablePaginationTest extends WorkerViolationTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerViolationTablePaginationTest.class,
                "Bo test phan trang tho vi pham",
                "Kiem tra pagination danh sach tho vi pham");
    }

    @Test(groups = {"violation-worker", "pagination"},
            description = "WORKER-VIOLATION-PAGE-001: Trang dau active va Previous bi khoa")
    public void firstPageHasCorrectBoundaryState() {
        workerViolationPage.scrollToPagination();
        Assert.assertEquals(workerViolationPage.activePage(), 1,
                "Mac dinh danh sach khong o trang 1.");
        Assert.assertTrue(workerViolationPage.previousDisabled(),
                "Nut Previous phai bi khoa tai trang dau.");
    }

    @Test(groups = {"violation-worker", "pagination"},
            description = "WORKER-VIOLATION-PAGE-002: Next va Previous chuyen dung mot trang")
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
            description = "WORKER-VIOLATION-PAGE-003: Chon truc tiep mot so trang")
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
            description = "WORKER-VIOLATION-PAGE-004: Tong ban ghi khong doi khi chuyen trang")
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
