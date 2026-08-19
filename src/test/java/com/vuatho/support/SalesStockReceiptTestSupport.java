package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.SalesStockReceiptPage;
import org.testng.annotations.BeforeMethod;


/** Chuẩn bị phiên đăng nhập cho testcase Kho bán hàng → Phiếu. */
public abstract class SalesStockReceiptTestSupport extends BaseTest {
    protected SalesStockReceiptPage receiptPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareSalesStockReceipts() {
        requireAuthenticatedSession("Phiếu Kho bán hàng");
        receiptPage = new SalesStockReceiptPage(driver);
    }
}
