package com.vuatho.tests.transaction.assistant.platformfee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionAssistantPlatformFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;

/** Kiểm tra xuất Excel của tab Thợ phụ. */
public class TransactionAssistantPlatformFeeExportTest extends TransactionAssistantPlatformFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPlatformFeeExportTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Phí nền tảng - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_010)
    public void exportsCurrentSubtype() {
        verifyExport();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_028)
    public void exportsFilteredStatusToARealFile() {
        var result = advancedPage().exportFilteredStatus();
        Assert.assertNotNull(result.file());
        Assert.assertTrue(Files.isRegularFile(result.file()));
        Assert.assertEquals(result.filterValue(), "Thành công");
        Assert.assertTrue(result.totalRows() >= result.visibleRows());
    }
}
