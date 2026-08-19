package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.UniformCatalogPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/**
 * Setup/cleanup dùng chung của menu Quản lí Đồng Phục.
 *
 * <p>Đây là lớp hỗ trợ, không chứa {@code @Test}. Mỗi file testcase tự mở đúng
 * route nghiệp vụ của mình nên có thể chạy độc lập từ IDE.</p>
 */
public abstract class UniformModuleTestSupport extends BaseTest {
    protected UniformCatalogPage catalogPage;

    /** Đảm bảo đã đăng nhập và khởi tạo Page Object Quản lí Đồng Phục. */
    @BeforeMethod(alwaysRun = true)
    public void prepareUniformModule() {
        requireAuthenticatedSession("module Đồng phục");
        catalogPage = new UniformCatalogPage(driver);
    }

    /** Đóng drawer/dialog còn mở để testcase sau không bị overlay chặn. */
    @AfterMethod(alwaysRun = true)
    public void closeUniformOverlay() {
        if (driver == null) {
            return;
        }
        try {
            if (driver.getCurrentUrl().contains("/uniform")) {
                catalogPage.closeOverlay();
            }
        } catch (RuntimeException ignored) {
            // Test kế tiếp luôn mở lại route nên cleanup không che lỗi nghiệp vụ.
        }
    }
}
