package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerTestManagementPage.DetailSnapshot;
import com.vuatho.pages.WorkerTestManagementPage.Status;
import com.vuatho.pages.WorkerTestManagementPage.TestRow;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra drawer chi tiết và toàn bộ dữ liệu bài làm trả về. */
public class WorkerTestManagementDetailTest extends WorkerTestManagementTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerTestManagementDetailTest.class,
                "Bài kiểm tra", "Chi tiết bài kiểm tra");
    }

    @Test(groups = {"worker-test-management", "detail", "data-interaction"},
            description = "WORKER-TESTED-009: Click dòng mở nội dung câu hỏi và đáp án")
    public void detailReturnsTestContentQuestionsAndAnswers() {
        DetailSnapshot detail = workerTestPage.openFirstRow();
        for (String label : new String[]{
                "Chi tiết bài kiểm tra", "Nội dung bài kiểm tra",
                "Tiêu đề:", "Tên nghiệp vụ:", "Mô tả:", "Bộ câu hỏi:"}) {
            Assert.assertTrue(detail.text().contains(label),
                    "Chi tiết thiếu trường: " + label);
        }
        Assert.assertTrue(detail.questionCount() > 0,
                "Chi tiết không trả câu hỏi.");
        Assert.assertEquals(detail.answerCount(), detail.questionCount(),
                "Số đáp án không bằng số câu hỏi.");
    }

    @Test(groups = {"worker-test-management", "detail", "history", "data-interaction"},
            description = "WORKER-TESTED-010: Drawer trả lịch sử làm bài và thông tin tài khoản")
    public void detailReturnsAttemptHistoryAndAccountInformation() {
        TestRow row = workerTestPage.rows().get(0);
        DetailSnapshot detail = workerTestPage.openFirstRow();
        for (String label : new String[]{
                "Lịch sử làm bài", "Lần 1", "Bắt đầu:", "Kết thúc:",
                "Kết quả:", "Điểm:", "Tài khoản", "ID:", "Họ tên:", "Số điện thoại:"}) {
            Assert.assertTrue(detail.text().contains(label),
                    "Chi tiết thiếu dữ liệu: " + label);
        }
        Assert.assertTrue(detail.attemptHistoryCount() > 0,
                "Chi tiết không trả lịch sử lần làm.");
        Assert.assertEquals(detail.attemptHistoryCount(), Integer.parseInt(row.attempts()),
                "Số lịch sử trong drawer khác Số lần làm bài trên bảng.");
        if (!row.score().isBlank()) {
            Assert.assertTrue(detail.text().contains("(" + row.score() + ")"),
                    "Phần trăm trong lịch sử khác Tỉ lệ đúng lần cuối trên bảng.");
        }
    }

    @Test(groups = {"worker-test-management", "detail", "data-interaction"},
            description = "WORKER-TESTED-011: Drawer chỉ xem và đóng được bằng nút đóng")
    public void detailIsReadOnlyAndCanBeClosed() {
        DetailSnapshot detail = workerTestPage.openFirstRow();
        Assert.assertEquals(detail.visibleFormFieldCount(), 0,
                "Drawer xuất hiện trường có thể chỉnh sửa.");
        Assert.assertTrue(detail.visibleButtonTexts().isEmpty(),
                "Drawer xuất hiện action có chữ ngoài nút đóng: "
                        + detail.visibleButtonTexts());
        workerTestPage.closeDrawer();
    }

    @Test(groups = {"worker-test-management", "detail", "data-interaction"},
            description = "WORKER-TESTED-015: Dữ liệu nghiệp vụ và tài khoản khớp giữa bảng và drawer")
    public void tableAndDetailReturnConsistentData() {
        TestRow row = workerTestPage.rows().get(0);
        DetailSnapshot detail = workerTestPage.openRowById(row.id());
        Assert.assertTrue(detail.text().contains(row.service()),
                "Drawer không chứa nghiệp vụ của dòng #" + row.id());
        Assert.assertTrue(detail.text().contains(row.account()),
                "Drawer không chứa tài khoản của dòng #" + row.id());
    }

    @DataProvider(name = "detailStatuses")
    public Object[][] detailStatuses() {
        return new Object[][]{
                {Status.INITIALIZED},
                {Status.IN_PROGRESS},
                {Status.PENDING},
                {Status.PASSED},
                {Status.FAILED}
        };
    }

    @Test(
            dataProvider = "detailStatuses",
            groups = {"worker-test-management", "detail", "filter", "data-interaction"},
            description = "WORKER-TESTED-012: Mở được chi tiết ở từng trạng thái bài kiểm tra")
    public void detailOpensForEveryStatus(Status status) {
        workerTestPage.selectStatus(status);
        TestRow row = workerTestPage.rows().stream().findFirst().orElse(null);
        if (row == null) {
            throw new SkipException(
                    "Hiện không có dữ liệu trạng thái " + status.label()
                            + " để mở chi tiết.");
        }
        DetailSnapshot detail = workerTestPage.openRowById(row.id());
        Assert.assertEquals(detail.id(), row.id());
        Assert.assertTrue(detail.text().contains("Chi tiết bài kiểm tra"));
        Assert.assertTrue(detail.text().contains("Tài khoản"),
                "Chi tiết " + status.label() + " thiếu thông tin tài khoản.");
    }
}
