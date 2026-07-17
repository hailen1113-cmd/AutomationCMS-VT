package com.vuatho.core;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.nio.file.Path;
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
        // Khôi phục phiên trước, bao gồm các session-cookie mà Chrome chỉ giữ khi
        // trình duyệt được cấu hình tiếp tục phiên làm việc cũ.
        options.addArguments("--restore-last-session");
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

        WebDriver driver = new ChromeDriver(options);
        if (!TestConfig.headless()) {
            // Maximize thêm lần nữa vì một số máy có thể bỏ qua --start-maximized lúc khởi động.
            driver.manage().window().maximize();
        }
        return driver;
    }
}
