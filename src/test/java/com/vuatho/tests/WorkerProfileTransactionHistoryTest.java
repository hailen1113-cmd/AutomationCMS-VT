package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerProfilePage.TransactionDetailResult;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Kiểm tra lịch sử giao dịch trong tab Giao dịch của hồ sơ thợ.
 */
public class WorkerProfileTransactionHistoryTest extends WorkerProfileTestSupport {
    private static final int TRANSACTIONS_TO_OPEN = 3;
    private static final Duration TRANSACTION_TAB_VIEW_DURATION = Duration.ofSeconds(6);
    private static final Duration TRANSACTION_TABLE_VIEW_DURATION = Duration.ofSeconds(4);
    private static final Duration TRANSACTION_PAGE_VIEW_DURATION = Duration.ofSeconds(5);
    private static final Duration FINAL_VIEW_DURATION = Duration.ofSeconds(8);

    /**
     * Cho phép chạy trực tiếp testcase từ IDE.
     * @param args các tham số dòng lệnh; hiện chưa được sử dụng
     */
    public static void main(String[] args) {
        TestNgRunner.run(WorkerProfileTransactionHistoryTest.class,
                "Bo test lich su giao dich ho so tho ERP",
                "Kiem tra mo giao dich o tab moi");
    }

    /**
     * Mở lần lượt ba giao dịch đầu trong tab mới và quay lại tab hồ sơ thợ.
     */
    @Test(priority = 1,
            groups = { "partner-worker", "worker-profile", "worker-transaction" },
            description = "WORKER-PROFILE-TRANSACTION-001: Mo 3 giao dich dau trong tab moi")
    public void firstThreeTransactionsOpenInNewTabs() {
        openTransactionHistory();
        if (workerProfilePage.transactionHistoryRowCount() < TRANSACTIONS_TO_OPEN) {
            throw new SkipException("Ho so tho co it hon 3 giao dich de kiem tra.");
        }

        for (int index = 0; index < TRANSACTIONS_TO_OPEN; index++) {
            System.out.println("[WORKER TRANSACTION] Mo giao dich thu " + (index + 1)
                    + " va giu tab chi tiet trong " + TRANSACTION_TAB_VIEW_DURATION.toSeconds() + " giay.");
            TransactionDetailResult result = workerProfilePage
                    .openTransactionInNewTabAndReturn(index, TRANSACTION_TAB_VIEW_DURATION);

            Assert.assertTrue(result.detailUrl().contains("/vuatho/transaction"),
                    "Giao dich thu " + (index + 1) + " khong mo dung trang chi tiet.");
            Assert.assertTrue(result.detailText().contains("Chi tiết giao dịch"),
                    "Tab moi khong hien thi noi dung Chi tiet giao dich.");
            Assert.assertFalse(result.sourceRowText().isBlank(),
                    "Dong giao dich nguon khong co noi dung de doi chieu.");
        }

        System.out.println("[WORKER TRANSACTION] Da mo xong 3 giao dich; giu man hinh trong "
                + FINAL_VIEW_DURATION.toSeconds() + " giay.");
        workerProfilePage.keepWorkerDetailVisible(FINAL_VIEW_DURATION);
    }

    /**
     * Thực hiện đúng luồng trong video: click từ trang 1 sang trang 2,
     * sau đó click trực tiếp số 1 để quay lại trang đầu.
     */
    @Test(priority = 2,
            groups = { "partner-worker", "worker-profile", "worker-transaction" },
            description = "WORKER-PROFILE-TRANSACTION-002: Chuyen trang 1 sang 2 va quay lai trang 1")
    public void transactionHistoryMovesToPageTwoAndBackToPageOne() {
        openTransactionHistory();

        Assert.assertTrue(workerProfilePage.hasTransactionHistoryPageButton(2),
                "Khong tim thay button trang 2 cua bang Lich su giao dich.");
        Assert.assertEquals(workerProfilePage.currentTransactionHistoryPage(), 1,
                "Bang Lich su giao dich khong bat dau o trang 1.");

        workerProfilePage.keepWorkerDetailVisible(TRANSACTION_PAGE_VIEW_DURATION);
        System.out.println("[WORKER TRANSACTION PAGINATION] CLICK button trang 2.");
        workerProfilePage.clickTransactionHistoryPage(2);
        Assert.assertEquals(workerProfilePage.currentTransactionHistoryPage(), 2,
                "Khong chuyen duoc bang Lich su giao dich sang trang 2.");
        System.out.println("[WORKER TRANSACTION PAGINATION] Da chuyen sang trang 2.");

        workerProfilePage.keepWorkerDetailVisible(TRANSACTION_PAGE_VIEW_DURATION);
        System.out.println("[WORKER TRANSACTION PAGINATION] CLICK button trang 1.");
        workerProfilePage.clickTransactionHistoryPage(1);
        Assert.assertEquals(workerProfilePage.currentTransactionHistoryPage(), 1,
                "Khong quay lai duoc trang 1 cua bang Lich su giao dich.");
        System.out.println("[WORKER TRANSACTION PAGINATION] Da quay lai trang 1.");
        workerProfilePage.keepWorkerDetailVisible(TRANSACTION_PAGE_VIEW_DURATION);
    }

    /**
     * Mở hồ sơ thợ đầu tiên, chọn tab Giao dịch và xác nhận bảng lịch sử đã tải.
     */
    private void openTransactionHistory() {
        workerProfilePage.openFirstWorkerInformation();
        workerProfilePage.openWorkerDetailTab("Giao dịch");
        Assert.assertTrue(workerProfilePage.hasTransactionHistoryTable(),
                "Khong tim thay bang Lich su giao dich trong chi tiet tho.");
        // Dừng ở bảng giao dịch để người chạy kịp nhận biết trạng thái bắt đầu.
        workerProfilePage.keepWorkerDetailVisible(TRANSACTION_TABLE_VIEW_DURATION);
    }
}
