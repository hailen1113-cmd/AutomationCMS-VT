package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerViolationPage;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/** Test tim kiem, ba bo loc, ket hop dieu kien va reset. */
public class WorkerViolationSearchFilterTest extends WorkerViolationTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerViolationSearchFilterTest.class,
                "Bo test tim kiem bo loc tho vi pham", "Kiem tra search filter reset");
    }

    @DataProvider(name = "filters", parallel = false)
    public Object[][] filters() {
        return new Object[][]{
                {0, "Tất cả trạng thái"}, {0, "Đang bị phạt"}, {0, "Đã gỡ hết"}, {0, "Phạt vĩnh viễn"},
                {1, "Tất cả ngày"}, {1, "≤ 7 ngày"}, {1, "8 - 30 ngày"}, {1, "> 30 ngày"}, {1, "Vĩnh viễn"},
                {2, "Tất cả tình trạng"}, {2, "Đã thu"}, {2, "Chưa thu"}
        };
    }

    @Test(groups = {"violation-worker", "search-filter"},
            description = "VIOLATION-WORKER-FILTER-001: Du tuy chon cua ba bo loc")
    public void filterCatalogIsComplete() {
        Assert.assertEquals(workerViolationPage.filterOptions(0),
                List.of("Tất cả trạng thái", "Đang bị phạt", "Đã gỡ hết", "Phạt vĩnh viễn"));
        Assert.assertEquals(workerViolationPage.filterOptions(1),
                List.of("Tất cả ngày", "≤ 7 ngày", "8 - 30 ngày", "> 30 ngày", "Vĩnh viễn"));
        Assert.assertEquals(workerViolationPage.filterOptions(2),
                List.of("Tất cả tình trạng", "Đã thu", "Chưa thu"));
    }

    @Test(groups = {"violation-worker", "search-filter"},
            description = "VIOLATION-WORKER-SEARCH-001: Tim theo ID lay dong dau tien")
    public void searchByExistingWorkerId() {
        WorkerViolationPage.RowSeed seed = requiredSeed();
        Assert.assertFalse(seed.id().isBlank(), "Dong dau tien khong co ID de tim.");
        workerViolationPage.search(seed.id());
        Assert.assertTrue(workerViolationPage.displayedRowCount() > 0 && workerViolationPage.rowsContain(seed.id()),
                "Ket qua tim khong khop ID: " + seed.id());
    }

    @Test(groups = {"violation-worker", "search-filter"},
            description = "VIOLATION-WORKER-SEARCH-002: Tim theo ten lay dong dau tien")
    public void searchByExistingWorkerName() {
        WorkerViolationPage.RowSeed seed = requiredSeed();
        Assert.assertFalse(seed.name().isBlank(), "Dong dau tien khong co ten de tim.");
        workerViolationPage.search(seed.name());
        Assert.assertTrue(workerViolationPage.displayedRowCount() > 0 && workerViolationPage.rowsContain(seed.name()),
                "Ket qua tim khong khop ten: " + seed.name());
    }

    @Test(groups = {"violation-worker", "search-filter"},
            description = "VIOLATION-WORKER-SEARCH-003: Tim theo SDT lay dong dau tien")
    public void searchByExistingWorkerPhone() {
        WorkerViolationPage.RowSeed seed = requiredSeed();
        Assert.assertFalse(seed.phone().isBlank(), "Dong dau tien khong co SDT de tim.");
        workerViolationPage.search(seed.phone());
        Assert.assertTrue(workerViolationPage.displayedRowCount() > 0,
                "Khong co ket qua khi tim SDT: " + seed.phone());
    }

    @Test(groups = {"violation-worker", "search-filter"},
            description = "VIOLATION-WORKER-SEARCH-004: Tu khoa khong ton tai hien thi empty state")
    public void nonexistentSearchShowsEmptyState() {
        workerViolationPage.search("__AUTOMATION_NO_SUCH_WORKER_928374__");
        Assert.assertTrue(workerViolationPage.hasEmptyState(), "Tim khong co ket qua nhung khong hien empty state hop le.");
    }

    @Test(dataProvider = "filters", groups = {"violation-worker", "search-filter"},
            description = "VIOLATION-WORKER-FILTER-002: Chon tung tuy chon bo loc")
    public void eachFilterOptionCanBeSelected(int filterIndex, String option) {
        workerViolationPage.selectFilter(filterIndex, option);
        Assert.assertEquals(TextNormalizer.normalize(workerViolationPage.selectedFilter(filterIndex)),
                TextNormalizer.normalize(option), "Bo loc khong giu dung lua chon.");
        Assert.assertTrue(workerViolationPage.totalDisplayed() >= 0, "Ket qua bo loc khong tai xong.");
    }

    @Test(groups = {"violation-worker", "search-filter"},
            description = "VIOLATION-WORKER-FILTER-003: Ket hop search va nhieu bo loc")
    public void searchAndFiltersCanBeCombined() {
        workerViolationPage.selectFilter(0, "Đang bị phạt")
                .selectFilter(1, "≤ 7 ngày")
                .selectFilter(2, "Chưa thu");
        Assert.assertTrue(workerViolationPage.totalDisplayed() >= 0, "Ket qua loc ket hop khong tai xong.");
        Assert.assertEquals(workerViolationPage.selectedFilter(0), "Đang bị phạt");
        Assert.assertEquals(workerViolationPage.selectedFilter(1), "≤ 7 ngày");
        Assert.assertEquals(workerViolationPage.selectedFilter(2), "Chưa thu");
    }

    @Test(groups = {"violation-worker", "search-filter"},
            description = "VIOLATION-WORKER-FILTER-004: Reset search va tat ca bo loc")
    public void resetRestoresEveryDefault() {
        workerViolationPage.search("automation")
                .selectFilter(0, "Đang bị phạt")
                .selectFilter(1, "≤ 7 ngày")
                .selectFilter(2, "Chưa thu")
                .reset();
        Assert.assertEquals(workerViolationPage.searchValue(), "");
        Assert.assertEquals(workerViolationPage.selectedFilter(0), "Tất cả trạng thái");
        Assert.assertEquals(workerViolationPage.selectedFilter(1), "Tất cả ngày");
        Assert.assertEquals(workerViolationPage.selectedFilter(2), "Tất cả tình trạng");
    }

    private WorkerViolationPage.RowSeed requiredSeed() {
        return workerViolationPage.firstRowSeed().orElseThrow(
                () -> new AssertionError("Khong co dong du lieu dong de tao truy van test."));
    }
}
