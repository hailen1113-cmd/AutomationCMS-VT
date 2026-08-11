package com.vuatho.tests.transaction.assistant.common;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionAssistantTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra hợp đồng dữ liệu dùng chung cho cả Phí nền tảng và Tiền phạt. */
public class TransactionAssistantSubtypeContractTest extends TransactionAssistantTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantSubtypeContractTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Hợp đồng hai loại");
    }

    @DataProvider(name = "subtypes")
    public Object[][] subtypes() {
        return subtypeRows();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_011,
            dataProvider = "subtypes")
    public void everySubtypeShowsMatchingRows(TransactionCategoryPage.Subtype subtype) {
        transactionPage.open(subtype);
        var rows = transactionPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Loại giao dịch không có dữ liệu: " + subtype.label());
        String expected = normalize(subtype.label());
        Assert.assertTrue(rows.stream().allMatch(row ->
                        normalize(row.value("Loại giao dịch")).contains(expected)),
                "Có dòng không thuộc loại giao dịch " + subtype.label());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_034,
            dataProvider = "subtypes")
    public void detailMatchesEverySubtype(TransactionCategoryPage.Subtype subtype) {
        transactionPage.open(subtype);
        var result = advancedPage().openFirstDetail();
        Assert.assertTrue(result.url().contains("type=" + subtype.type()));
        Assert.assertFalse(result.source().type().isBlank());
        Assert.assertTrue(result.drawerText().contains(result.source().type()), result.drawerText());
        Assert.assertTrue(normalize(result.source().type()).contains(normalize(subtype.label())));
    }
}
