package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.UniformInventoryPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/** Setup và cleanup dùng chung cho testcase Kho tổng → Tồn kho. */
public abstract class UniformInventoryStockTestSupport extends BaseTest {
    protected UniformInventoryPage inventoryPage;

    /** Đảm bảo đã đăng nhập và khởi tạo đúng Page Object của kho Đồng phục. */
    @BeforeMethod(alwaysRun = true)
    public void prepareUniformInventoryStock() {
        requireAuthenticatedSession("kho Đồng phục");
        inventoryPage = new UniformInventoryPage(driver);
    }

    /** Đóng drawer còn mở để testcase tiếp theo luôn bắt đầu từ UI sạch. */
    @AfterMethod(alwaysRun = true)
    public void closeInventoryOverlay() {
        if (driver == null || inventoryPage == null) {
            return;
        }
        try {
            inventoryPage.closeOverlay();
        } catch (RuntimeException ignored) {
            // Route được mở lại ở testcase sau nên cleanup không che lỗi nghiệp vụ.
        }
    }
}
