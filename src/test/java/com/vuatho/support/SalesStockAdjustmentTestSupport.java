package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.SalesStockAdjustmentPage;
import org.testng.annotations.BeforeMethod;


/** Chuẩn bị phiên đăng nhập cho nghiệp vụ Kho bán hàng → Điều chỉnh tồn. */
public abstract class SalesStockAdjustmentTestSupport extends BaseTest {
    protected SalesStockAdjustmentPage adjustmentPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareSalesStockAdjustment() {
        requireAuthenticatedSession("Điều chỉnh tồn Kho bán hàng");
        adjustmentPage = new SalesStockAdjustmentPage(driver);
    }
}
