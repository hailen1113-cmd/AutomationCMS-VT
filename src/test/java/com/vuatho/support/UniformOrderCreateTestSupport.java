package com.vuatho.support;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.UniformOrderPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.Set;

/** Setup, cleanup và assertion chung cho testcase tạo Đơn hàng Đồng phục. */
public abstract class UniformOrderCreateTestSupport extends BaseTest {
    protected UniformOrderPage orderPage;

    /** Đảm bảo session đăng nhập trước khi thao tác drawer tạo đơn. */
    @BeforeMethod(alwaysRun = true)
    public void prepareUniformOrderCreate() {
        requireAuthenticatedSession("tạo Đơn hàng Đồng phục");
        orderPage = new UniformOrderPage(driver);
    }

    /** Đóng drawer còn mở để testcase sau không kế thừa form cũ. */
    @AfterMethod(alwaysRun = true)
    public void closeCreateDrawer() {
        if (driver == null || orderPage == null) {
            return;
        }
        try {
            orderPage.closeOverlay();
        } catch (RuntimeException ignored) {
            // Cleanup không che lỗi nghiệp vụ của testcase vừa chạy.
        }
    }

    /** Xác minh đúng các field lỗi, drawer còn mở và không phát sinh đơn mới. */
    protected void assertRequiredValidation(
            UniformOrderPage.CreateValidationResult result,
            UniformOrderPage.CreateRequiredField... expectedFields) {
        Assert.assertTrue(result.drawerOpen(),
                "Form thiếu dữ liệu bắt buộc nhưng drawer đã đóng.");
        Assert.assertEquals(result.totalAfter(), result.totalBefore(),
                "Form thiếu dữ liệu nhưng tổng đơn đã thay đổi.");
        Set<UniformOrderPage.CreateRequiredField> expected =
                Set.of(expectedFields);
        Assert.assertEquals(result.invalidFields(), expected,
                "Validation không hiển thị đúng field đang thiếu. Mong đợi: "
                        + expected + "; thực tế: " + result.invalidFields());
    }
}
