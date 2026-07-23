package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerStopRequestPage.RequestRow;
import com.vuatho.pages.WorkerStopRequestPage.Status;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra tìm kiếm, lọc trạng thái và Reset. */
public class WorkerStopRequestSearchFilterTest extends WorkerStopRequestTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerStopRequestSearchFilterTest.class,
                "Yêu cầu ngưng hợp tác", "Tìm kiếm và lọc trạng thái");
    }

    @DataProvider(name = "requestStatuses")
    public Object[][] requestStatuses() {
        return new Object[][]{
                {Status.PENDING}, {Status.APPROVED},
                {Status.REJECTED}, {Status.SKIPPED}
        };
    }

    @Test(groups = {"worker-stop-request", "search", "data-interaction"},
            description = "WORKER-STOP-REQUEST-002: Tìm kiếm theo tên trả đúng dữ liệu và Reset phục hồi")
    public void searchByWorkerNameAndResetWork() {
        List<String> original = stopRequestPage.rows().stream().map(RequestRow::id).toList();
        String workerName = stopRequestPage.rows().get(0).workerName();
        String keyword = workerName.split("\\s+")[0];

        stopRequestPage.search(keyword);
        Assert.assertFalse(stopRequestPage.rows().isEmpty(), "Tìm kiếm không trả dữ liệu.");
        Assert.assertTrue(stopRequestPage.rows().stream().allMatch(row ->
                        TextNormalizer.normalize(row.workerName())
                                .contains(TextNormalizer.normalize(keyword))),
                "Kết quả có thợ không khớp từ khóa " + keyword);

        stopRequestPage.reset();
        Assert.assertTrue(stopRequestPage.searchValue().isBlank(), "Reset không xóa từ khóa.");
        Assert.assertEquals(
                stopRequestPage.rows().stream().map(RequestRow::id).toList(),
                original,
                "Reset không phục hồi dữ liệu ban đầu.");
    }

    @Test(
            dataProvider = "requestStatuses",
            groups = {"worker-stop-request", "filter", "data-interaction"},
            description = "WORKER-STOP-REQUEST-003: Mỗi bộ lọc chỉ trả đúng trạng thái")
    public void eachStatusFilterReturnsMatchingRows(Status status) {
        stopRequestPage.selectStatus(status);
        List<RequestRow> rows = stopRequestPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Bộ lọc " + status.label() + " không có dữ liệu test.");
        Assert.assertTrue(rows.stream().allMatch(row -> row.status() == status),
                "Bộ lọc " + status.label() + " trả sai trạng thái.");
    }
}
