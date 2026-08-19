package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.StockAdjustmentPage;
import org.testng.annotations.BeforeMethod;


/** Chuẩn bị đăng nhập và Page Object cho testcase Điều chỉnh tồn. */
public abstract class StockAdjustmentTestSupport extends BaseTest {
    protected StockAdjustmentPage adjustmentPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareStockAdjustment() {
        requireAuthenticatedSession("Điều chỉnh tồn");
        adjustmentPage = new StockAdjustmentPage(driver);
    }
}
