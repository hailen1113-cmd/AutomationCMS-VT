package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * Kiểm tra chức năng phân trang ở cuối màn hình Quản lí hồ sơ thợ.
 */
public class WorkerProfilePaginationTest extends WorkerProfileTestSupport {
    /**
     * Cho phép chạy trực tiếp testcase phân trang từ IDE.
     * @param args các tham số dòng lệnh; hiện chưa được sử dụng
     */
    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfilePaginationTest.class,
                "Bo test phan trang ho so tho ERP",
                "Kiem tra chuyen trang danh sach ho so tho");
    }

    /**
     * Kiểm tra chuyển trang kế tiếp, quay lại và mở trang cuối của danh sách hồ sơ thợ.
     */
    @Test(groups = { "partner-worker", "worker-profile", "worker-profile-pagination" },
            description = "WORKER-PROFILE-PAGINATION-001: Phan trang danh sach ho so tho hoat dong dung")
    public void workerProfilePaginationNavigatesBetweenPages() {
        Assert.assertTrue(workerProfilePage.hasVisiblePagination(),
                "Khong tim thay thanh phan trang o cuoi danh sach ho so tho.");

        workerProfilePage.openWorkerPage(1);
        int lastPage = workerProfilePage.lastWorkerPageNumber();
        if (lastPage < 2) {
            throw new SkipException("Danh sach chi co mot trang, khong du du lieu de kiem tra phan trang.");
        }

        String firstRowOnPageOne = workerProfilePage.firstWorkerRowText();
        Assert.assertEquals(workerProfilePage.currentWorkerPageNumber(), 1,
                "Trang 1 khong duoc chon khi bat dau testcase.");
        Assert.assertTrue(workerProfilePage.previousWorkerPageIsDisabled(),
                "Nut Previous phai bi khoa tai trang dau.");

        workerProfilePage.openNextWorkerPage();
        Assert.assertEquals(workerProfilePage.currentWorkerPageNumber(), 2,
                "Nut Next khong chuyen danh sach sang trang 2.");
        Assert.assertNotEquals(workerProfilePage.firstWorkerRowText(), firstRowOnPageOne,
                "Du lieu dong dau tien khong thay doi sau khi chuyen trang.");
        Assert.assertFalse(workerProfilePage.previousWorkerPageIsDisabled(),
                "Nut Previous van bi khoa sau khi da sang trang 2.");

        workerProfilePage.openPreviousWorkerPage();
        Assert.assertEquals(workerProfilePage.currentWorkerPageNumber(), 1,
                "Nut Previous khong dua danh sach ve trang 1.");
        Assert.assertEquals(workerProfilePage.firstWorkerRowText(), firstRowOnPageOne,
                "Du lieu trang 1 khong duoc khoi phuc sau khi quay lai.");

        workerProfilePage.openWorkerPage(lastPage);
        Assert.assertEquals(workerProfilePage.currentWorkerPageNumber(), lastPage,
                "Khong mo duoc trang cuoi cua danh sach ho so tho.");
        Assert.assertTrue(workerProfilePage.hasWorkerRows(),
                "Trang cuoi khong hien thi dong ho so tho nao.");
        Assert.assertTrue(workerProfilePage.nextWorkerPageIsDisabled(),
                "Nut Next phai bi khoa tai trang cuoi.");
    }
}
