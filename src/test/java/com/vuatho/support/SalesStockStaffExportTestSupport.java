package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.SalesStockStaffExportPage;
import org.testng.annotations.BeforeMethod;


/** Chuẩn bị phiên đăng nhập cho nghiệp vụ Kho bán hàng → Xuất hàng cho nhân sự. */
public abstract class SalesStockStaffExportTestSupport extends BaseTest {
    protected SalesStockStaffExportPage exportPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareSalesStockStaffExport() {
        requireAuthenticatedSession("Xuất hàng cho nhân sự");
        exportPage = new SalesStockStaffExportPage(driver);
    }
}
