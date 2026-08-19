package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.StockReceiptPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/** Chuẩn bị đăng nhập, Page Object và cleanup cho testcase Nhập kho tổng. */
public abstract class StockReceiptTestSupport extends BaseTest {
    protected StockReceiptPage receiptPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareStockReceipt() {
        requireAuthenticatedSession("Nhập kho tổng");
        receiptPage = new StockReceiptPage(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void closeStockReceiptOverlay() {
        if (driver == null || receiptPage == null) {
            return;
        }
        try {
            receiptPage.closeOverlay();
        } catch (RuntimeException ignored) {
            // Testcase sau mở lại route nên cleanup không che lỗi nghiệp vụ chính.
        }
    }
}
