package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.TransactionHistoryPage;
import org.testng.annotations.BeforeMethod;


/** Chuẩn bị phiên đăng nhập và tab Tất cả của Lịch sử giao dịch. */
public abstract class TransactionHistoryTestSupport extends BaseTest {
    protected TransactionHistoryPage transactionPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareTransactionHistory() {
        requireAuthenticatedSession("Lịch sử giao dịch");
        transactionPage = new TransactionHistoryPage(driver);
        transactionPage.openAllTab();
    }
}
