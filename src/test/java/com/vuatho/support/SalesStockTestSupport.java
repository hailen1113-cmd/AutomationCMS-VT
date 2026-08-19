package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.SalesStockPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/** Setup và cleanup dùng chung cho testcase Kho bán hàng → Tồn kho. */
public abstract class SalesStockTestSupport extends BaseTest {
    protected SalesStockPage salesStockPage;

    /** Đảm bảo có phiên đăng nhập và khởi tạo Page Object đúng kho. */
    @BeforeMethod(alwaysRun = true)
    public void prepareSalesStock() {
        requireAuthenticatedSession("Kho bán hàng");
        salesStockPage = new SalesStockPage(driver);
    }

    /** Đóng drawer còn mở để testcase sau bắt đầu từ trạng thái sạch. */
    @AfterMethod(alwaysRun = true)
    public void closeSalesStockOverlay() {
        if (driver == null || salesStockPage == null) {
            return;
        }
        try {
            salesStockPage.closeOverlay();
        } catch (RuntimeException ignored) {
            // Testcase sau mở lại route nên cleanup không che lỗi nghiệp vụ.
        }
    }
}
