package com.vuatho.reporting;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Chụp và lưu ảnh trình duyệt khi test thất bại, đồng thời tạo tên file an toàn.
 */
public final class ScreenshotManager {
    private static final Path SCREENSHOT_DIRECTORY = Path.of(TestConfig.screenshotDirectory());
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    /**
     * Khởi tạo ScreenshotManager với các phụ thuộc cần thiết.
     */
    private ScreenshotManager() {
    }

    /**
     * Thu thập  trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     * @param testName giá trị test name được truyền vào
     * @return kết quả capture sau khi xử lý
     */
    public static Path capture(WebDriver driver, String testName) throws IOException {
        return capture(driver, testName, null);
    }

    /**
     * Thu thập  trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     * @param result giá trị result được truyền vào
     * @return kết quả capture sau khi xử lý
     */
    public static Path capture(WebDriver driver, ITestResult result) throws IOException {
        return capture(driver, result.getMethod().getMethodName(), result.getThrowable());
    }

    /**
     * Thu thập  trong luồng kiểm thử.
     * @param driver WebDriver đang điều khiển trình duyệt
     * @param testName giá trị test name được truyền vào
     * @param failure giá trị failure được truyền vào
     * @return kết quả capture sau khi xử lý
     */
    private static Path capture(WebDriver driver, String testName, Throwable failure) throws IOException {
        Files.createDirectories(SCREENSHOT_DIRECTORY);
        Path destination = SCREENSHOT_DIRECTORY.resolve(fileName(testName));
        byte[] image = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Files.write(destination, image);
        Path evidence = destination.resolveSibling(destination.getFileName() + ".txt");
        Files.writeString(evidence, "timestamp=" + LocalDateTime.now() + System.lineSeparator()
                + "url=" + driver.getCurrentUrl() + System.lineSeparator()
                + "test=" + Objects.toString(testName, "") + System.lineSeparator()
                + "failure=" + stackTrace(failure));
        return destination.toAbsolutePath();
    }

    /**
     * Thực hiện xử lý stack trace trong luồng kiểm thử.
     * @param failure giá trị failure được truyền vào
     * @return kết quả stack trace sau khi xử lý
     */
    private static String stackTrace(Throwable failure) {
        if (failure == null) {
            return "";
        }
        StringWriter buffer = new StringWriter();
        failure.printStackTrace(new PrintWriter(buffer));
        return buffer.toString();
    }

    /**
     * Thực hiện xử lý latest for trong luồng kiểm thử.
     * @param testName giá trị test name được truyền vào
     * @return kết quả latest for sau khi xử lý
     */
    public static Path latestFor(String testName) {
        return SCREENSHOT_DIRECTORY.resolve(safeName(testName) + ".png").toAbsolutePath();
    }

    /**
     * Thực hiện xử lý file name trong luồng kiểm thử.
     * @param testName giá trị test name được truyền vào
     * @return kết quả file name sau khi xử lý
     */
    private static String fileName(String testName) {
        Path preferred = SCREENSHOT_DIRECTORY.resolve(safeName(testName) + ".png");
        if (!Files.exists(preferred)) {
            return preferred.getFileName().toString();
        }
        return safeName(testName) + "-" + TIMESTAMP.format(LocalDateTime.now()) + ".png";
    }

    /**
     * Thực hiện xử lý safe name trong luồng kiểm thử.
     * @param value giá trị đầu vào
     * @return kết quả safe name sau khi xử lý
     */
    private static String safeName(String value) {
        return value == null || value.isBlank()
                ? "screenshot"
                : value.replaceAll("[^A-Za-z0-9._-]+", "_");
    }
}
