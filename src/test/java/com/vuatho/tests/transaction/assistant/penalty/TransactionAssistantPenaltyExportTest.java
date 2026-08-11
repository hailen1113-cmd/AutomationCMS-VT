package com.vuatho.tests.transaction.assistant.penalty;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionAssistantPenaltyTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;

/** Kiểm tra xuất Excel của loại Tiền phạt. */
public class TransactionAssistantPenaltyExportTest extends TransactionAssistantPenaltyTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPenaltyExportTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Tiền phạt - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_085)
    public void exportsCurrentSubtype() {
        verifyExport(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_086)
    public void exportsFilteredStatusToARealFile() {
        var result = advancedPage().exportFilteredStatus();
        Assert.assertNotNull(result.file());
        Assert.assertTrue(Files.isRegularFile(result.file()));
        Assert.assertEquals(result.filterValue(), "Thành công");
        Assert.assertTrue(result.totalRows() >= result.visibleRows());
        Assert.assertTrue(result.file().getFileName().toString().matches("(?i).+\\.(xlsx|xls|csv)$"));
    }
}
