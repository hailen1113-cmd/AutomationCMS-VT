package com.vuatho.tests.transaction.assistant.common;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionAssistantTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra hợp đồng dữ liệu dùng chung cho cả Phí nền tảng và Tiền phạt. */
public class TransactionAssistantSubtypeContractTest extends TransactionAssistantTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantSubtypeContractTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Hợp đồng hai loại");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_011)
    public void platformFeeShowsMatchingRows() {
        verifySubtypeShowsMatchingRows(category().subtypes().get(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_129)
    public void penaltyShowsMatchingRows() {
        verifySubtypeShowsMatchingRows(category().subtypes().get(1));
    }

    private void verifySubtypeShowsMatchingRows(TransactionCategoryPage.Subtype subtype) {
        transactionPage.open(subtype);
        var rows = transactionPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Loại giao dịch không có dữ liệu: " + subtype.label());
        String expected = normalize(subtype.label());
        Assert.assertTrue(rows.stream().allMatch(row ->
                        normalize(row.value("Loại giao dịch")).contains(expected)),
                "Có dòng không thuộc loại giao dịch " + subtype.label());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_034)
    public void platformFeeDetailMatchesSubtype() {
        verifyDetailMatchesSubtype(category().subtypes().get(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_130)
    public void penaltyDetailMatchesSubtype() {
        verifyDetailMatchesSubtype(category().subtypes().get(1));
    }

    private void verifyDetailMatchesSubtype(TransactionCategoryPage.Subtype subtype) {
        transactionPage.open(subtype);
        var result = advancedPage().openFirstDetail();
        Assert.assertTrue(result.url().contains("type=" + subtype.type()));
        Assert.assertFalse(result.source().type().isBlank());
        Assert.assertTrue(result.drawerText().contains(result.source().type()), result.drawerText());
        Assert.assertTrue(normalize(result.source().type()).contains(normalize(subtype.label())));
    }
}
