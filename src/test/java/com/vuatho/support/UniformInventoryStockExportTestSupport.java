package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.UniformInventoryStockExportPage;
import org.testng.annotations.BeforeMethod;


/** Setup dùng chung cho testcase Kho tổng → Phiếu xuất kho. */
public abstract class UniformInventoryStockExportTestSupport extends BaseTest {
    protected UniformInventoryStockExportPage stockExportPage;

    /** Đảm bảo đăng nhập và khởi tạo Page Object trước mỗi testcase. */
    @BeforeMethod(alwaysRun = true)
    public void prepareUniformInventoryStockExport() {
        requireAuthenticatedSession("tab Phiếu xuất kho");
        stockExportPage = new UniformInventoryStockExportPage(driver);
    }
}
