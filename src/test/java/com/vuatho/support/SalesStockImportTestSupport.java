package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.SalesStockImportPage;
import org.testng.annotations.BeforeMethod;


/** Chuẩn bị phiên đăng nhập cho nghiệp vụ Kho bán hàng → Nhập hàng. */
public abstract class SalesStockImportTestSupport extends BaseTest {
    protected SalesStockImportPage importPage;

    @BeforeMethod(alwaysRun = true)
    public void prepareSalesStockImport() {
        requireAuthenticatedSession("Nhập hàng");
        importPage = new SalesStockImportPage(driver);
    }
}
