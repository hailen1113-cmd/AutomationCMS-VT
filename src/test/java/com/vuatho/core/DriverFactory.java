package com.vuatho.core;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.http.ClientConfig;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;

public final class DriverFactory {
    /**
     * Khởi tạo DriverFactory với các phụ thuộc cần thiết.
     */
    private DriverFactory() {
    }

    /**
     * Tạo chrome driver trong luồng kiểm thử.
     * @return kết quả create chrome driver sau khi xử lý
     */
    public static WebDriver createChromeDriver() {
        // Dùng profile Chrome riêng cho Selenium để cookie/token ERP được giữ giữa các test.
        Path profileDirectory = Path.of(TestConfig.seleniumProfileDirectory())
                .toAbsolutePath()
                .normalize();

        ChromeOptions options = new ChromeOptions();
        System.out.println("Su dung Chrome profile luu session: " + profileDirectory);
        options.addArguments("--user-data-dir=" + profileDirectory);
        options.addArguments("--profile-directory=Default");
        // Giữ cookie trong profile nhưng không khôi phục các tab của phiên Selenium bị crash.
        // Restore tab cũ làm số process Chrome tăng dần và khiến WebDriver mất phản hồi.
        options.setExperimentalOption("prefs", Map.of(
                "session.restore_on_startup", 0,
                "profile.exit_type", "Normal",
                "profile.exited_cleanly", true));
        options.addArguments("--disable-session-crashed-bubble");
        options.addArguments("--homepage=about:blank");
        options.addArguments("--disable-notifications");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");

        // Chỉ lấy log browser mức SEVERE; log info/warning quá nhiều sẽ làm console khó đọc.
        LoggingPreferences logging = new LoggingPreferences();
        logging.enable(LogType.BROWSER, Level.SEVERE);
        options.setCapability("goog:loggingPrefs", logging);

        // Headless cần kích thước cửa sổ cố định vì responsive layout có thể đổi vị trí element.
        if (TestConfig.headless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1440,1000");
            options.addArguments("--remote-debugging-port=0");
        } else {
            options.addArguments("--start-maximized");
        }

        // Giới hạn cả thời gian kết nối và đọc response của ChromeDriver. WebDriverWait
        // không thể ngắt một HTTP command bị treo, nên thiếu timeout này có thể giữ suite vô hạn.
        ClientConfig clientConfig = ClientConfig.defaultConfig()
                .connectionTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(30));
        ChromeDriverService service = new ChromeDriverService.Builder().build();
        WebDriver driver = new ChromeDriver(service, options, clientConfig);
        if (!TestConfig.headless()) {
            // Maximize thêm lần nữa vì một số máy có thể bỏ qua --start-maximized lúc khởi động.
            driver.manage().window().maximize();
        }
        return driver;
    }
}
