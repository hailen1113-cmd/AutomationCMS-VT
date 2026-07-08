package com.vuatho.reporting;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScreenshotManager {
    private static final Path SCREENSHOT_DIRECTORY = Path.of("target", "screenshots");

    private ScreenshotManager() {
    }

    public static Path capture(WebDriver driver, String testName) throws IOException {
        Files.createDirectories(SCREENSHOT_DIRECTORY);
        Path destination = SCREENSHOT_DIRECTORY.resolve(testName + ".png");
        byte[] image = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Files.write(destination, image);
        return destination.toAbsolutePath();
    }
}
