package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.DashboardPage;
import com.vuatho.pages.LoginPage;
import com.vuatho.pages.SourceEfficiencyPage;
import com.vuatho.quality.PageHealthChecker;
import com.vuatho.quality.PageHealthReport;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

/**
 * Kiểm tra luồng truy cập chính của CMS sau khi đăng nhập bằng Google.
 *
 * <p>Các test được chạy theo thứ tự để xác nhận lần lượt:</p>
 * <ol>
 *     <li>Người dùng đăng nhập và nhìn thấy Dashboard.</li>
 *     <li>Dashboard tải được các chỉ số nghiệp vụ.</li>
 *     <li>Trang Hiệu quả và chi phí nguồn có thể được mở thành công.</li>
 * </ol>
 */
public class LoginDashboardSourceAccessTest extends BaseTest {
    /**
     * Cho phép chạy trực tiếp bộ test này từ IDE mà không cần file TestNG XML.
     *
     * @param args tham số dòng lệnh; hiện tại bộ chạy không sử dụng
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "ERP Login and Dashboard Suite",
                "Login and Dashboard Checks",
                LoginDashboardSourceAccessTest.class);
    }

    /**
     * Giữ nguyên phiên trình duyệt giữa các test để tái sử dụng phiên đăng nhập
     * và giảm thời gian khởi tạo WebDriver.
     */
    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    /**
     * Thực thi test “CMS-DASH-001: Login CMS successfully with Google” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 1, description = "CMS-DASH-001: Login CMS successfully with Google")
    public void loginSuccessfully() {
        // Bắt đầu thu thập lỗi trình duyệt trước khi thực hiện thao tác đăng nhập.
        PageHealthChecker healthChecker = new PageHealthChecker(driver);
        healthChecker.startObservation();

        // Mở ứng dụng, đăng nhập bằng tài khoản Google đã cấu hình và chờ trang đích.
        LoginPage loginPage = openAndLogin();

        // Dashboard phải xuất hiện trong thời gian cho phép và trang không có lỗi nghiêm trọng.
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Login did not reach Dashboard.");
        assertHealthy(healthChecker.inspect());
    }

    /**
     * Thực thi test “CMS-DASH-LOAD-001: Dashboard loads successfully” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 2, description = "CMS-DASH-LOAD-001: Dashboard loads successfully")
    public void dashboardLoadsSuccessfully() {
        // Đảm bảo test có phiên đăng nhập hợp lệ, kể cả khi chạy riêng lẻ từ IDE.
        openAndLogin();

        // Chỉ theo dõi sức khỏe trang từ thời điểm bắt đầu kiểm tra Dashboard.
        PageHealthChecker healthChecker = new PageHealthChecker(driver);
        healthChecker.startObservation();

        // Mở Dashboard và đợi các API/widget hoàn tất việc hiển thị số liệu.
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.openDashboardAndWaitForMetrics();
        List<String> metrics = dashboard.loadedMetrics();

        // In dữ liệu đã đọc để báo cáo chạy test dễ kiểm tra khi có lỗi.
        System.out.printf("%n[DASHBOARD METRICS] Loaded %d values:%n", metrics.size());
        metrics.forEach(metric -> System.out.println("  - " + metric));

        // Có ít nhất một chỉ số là tín hiệu Dashboard đã tải nội dung nghiệp vụ.
        Assert.assertFalse(metrics.isEmpty(), "Dashboard opened but metrics were not displayed.");
        assertHealthy(healthChecker.inspect());
    }

    /**
     * Thực thi test “CMS-SOURCE-001: Source efficiency and cost page loads successfully” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(priority = 3,
            description = "CMS-SOURCE-001: Source efficiency and cost page loads successfully")
    public void sourceEfficiencyPageLoadsSuccessfully() {
        // Dashboard là điểm xuất phát bắt buộc trước khi điều hướng sang trang nguồn.
        openAndLogin();
        DashboardPage dashboard = new DashboardPage(driver);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard is not ready before opening source efficiency page.");

        // Theo dõi lỗi phát sinh riêng trong quá trình mở trang Hiệu quả và chi phí nguồn.
        PageHealthChecker healthChecker = new PageHealthChecker(driver);
        healthChecker.startObservation();
        SourceEfficiencyPage sourceEfficiencyPage = new SourceEfficiencyPage(driver)
                .openAndWaitUntilLoaded();

        // Xác nhận cả trạng thái giao diện và tình trạng kỹ thuật của trang đích.
        Assert.assertTrue(sourceEfficiencyPage.isLoaded(), "Source efficiency page did not load successfully.");
        assertHealthy(healthChecker.inspect());
        System.out.println("[PAGE LOADED] Source efficiency and cost");
    }

    /**
     * Gom thao tác mở ứng dụng và đăng nhập để các test dùng chung một luồng ổn định.
     *
     * @return Page Object của trang đăng nhập sau khi luồng xác thực hoàn tất
     */
    private LoginPage openAndLogin() {
        return new AuthenticationFlow(driver).openApplicationAndLogin();
    }

    /**
     * Chuyển kết quả kiểm tra sức khỏe trang thành assertion TestNG dễ đọc trong báo cáo.
     *
     * @param health báo cáo gồm lỗi console, tài nguyên hoặc trạng thái trang đã ghi nhận
     */
    private void assertHealthy(PageHealthReport health) {
        Assert.assertTrue(health.isHealthy(), health.summary());
    }
}
