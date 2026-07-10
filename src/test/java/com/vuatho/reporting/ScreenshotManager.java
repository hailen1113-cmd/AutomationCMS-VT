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

public final class ScreenshotManager {
    private static final Path SCREENSHOT_DIRECTORY = Path.of(TestConfig.screenshotDirectory());
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private ScreenshotManager() {
    }

    public static Path capture(WebDriver driver, String testName) throws IOException {
        return capture(driver, testName, null);
    }

    public static Path capture(WebDriver driver, ITestResult result) throws IOException {
        return capture(driver, result.getMethod().getMethodName(), result.getThrowable());
    }

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

    private static String stackTrace(Throwable failure) {
        if (failure == null) {
            return "";
        }
        StringWriter buffer = new StringWriter();
        failure.printStackTrace(new PrintWriter(buffer));
        return buffer.toString();
    }

    public static Path latestFor(String testName) {
        return SCREENSHOT_DIRECTORY.resolve(safeName(testName) + ".png").toAbsolutePath();
    }

    private static String fileName(String testName) {
        Path preferred = SCREENSHOT_DIRECTORY.resolve(safeName(testName) + ".png");
        if (!Files.exists(preferred)) {
            return preferred.getFileName().toString();
        }
        return safeName(testName) + "-" + TIMESTAMP.format(LocalDateTime.now()) + ".png";
    }

    private static String safeName(String value) {
        return value == null || value.isBlank()
                ? "screenshot"
                : value.replaceAll("[^A-Za-z0-9._-]+", "_");
    }
}
