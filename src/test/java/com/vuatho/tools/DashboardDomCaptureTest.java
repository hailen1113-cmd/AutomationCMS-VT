package com.vuatho.tools;

import com.vuatho.config.GoogleCredentialProvider;
import com.vuatho.config.TestConfig;
import com.vuatho.core.BaseTest;
import com.vuatho.pages.DashboardPage;
import com.vuatho.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Chụp toàn bộ DOM đang hiển thị của Dashboard để phân tích giao diện ngoại tuyến.
 *
 * <p>File HTML được lưu trong {@code target/analysis}; công cụ tự đăng nhập nếu
 * phiên trình duyệt chưa ở Dashboard và chỉ ghi file sau khi các summary card tải xong.</p>
 */
public class DashboardDomCaptureTest extends BaseTest {
    /**
     * Mở Dashboard, chờ dữ liệu ổn định rồi lưu {@code outerHTML} làm bằng chứng phân tích.
     *
     * @throws IOException khi không thể tạo thư mục hoặc ghi file HTML
     */
    @Test(description = "Capture the complete live Dashboard DOM for evidence-based analysis")
    public void captureDashboardOuterHtml() throws IOException {
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.open();
        // Chỉ đăng nhập khi marker Dashboard chưa xuất hiện để tận dụng phiên có sẵn.
        if (!dashboard.hasDashboardMarker()) {
            new LoginPage(driver).loginWithGoogle(
                    TestConfig.loginEmail(),
                    GoogleCredentialProvider::password);
        }
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard must be loaded before capturing its DOM.");
        dashboard.openDashboardAndWaitForSummaryCards();
        Assert.assertTrue(dashboard.hasValidDashboardUrl(),
                "Dashboard route must be active before capturing its DOM.");

        // Lưu dưới target để artifact có thể được xóa/tạo lại cùng mỗi lần build.
        Path output = Path.of("target", "analysis", "dashboard-outerHTML.html");
        Files.createDirectories(output.getParent());
        Files.writeString(output, driver.getPageSource(), StandardCharsets.UTF_8);
        Assert.assertTrue(Files.size(output) > 0, "Captured Dashboard outerHTML must not be empty.");
        System.out.println("[DOM CAPTURED] " + output.toAbsolutePath()
                + " | URL=" + driver.getCurrentUrl()
                + " | bytes=" + Files.size(output));
    }
}
