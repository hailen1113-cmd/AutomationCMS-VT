package com.vuatho.tools;

import com.vuatho.config.TestConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class GoogleSessionSetup {
    private GoogleSessionSetup() {
    }

    public static void main(String[] args) throws IOException {
        Path chrome = findChrome();
        Path profile = Path.of(TestConfig.seleniumProfileDirectory())
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(profile);

        new ProcessBuilder(
                chrome.toString(),
                "--user-data-dir=" + profile,
                "--profile-directory=Default",
                "https://accounts.google.com/")
                .start();

        System.out.println("Chrome thuong da duoc mo bang profile Selenium: " + profile);
        System.out.println("1. Dang nhap Google thu cong bang hailen1113@gmail.com.");
        System.out.println("2. Dong TOAN BO cua so Chrome cua profile nay.");
        System.out.println("3. Run Java lai ErpLoginTest.main().");
    }

    private static Path findChrome() {
        List<Path> candidates = List.of(
                pathFromEnvironment("ProgramFiles", "Google", "Chrome", "Application", "chrome.exe"),
                pathFromEnvironment("ProgramFiles(x86)", "Google", "Chrome", "Application", "chrome.exe"),
                pathFromEnvironment("LOCALAPPDATA", "Google", "Chrome", "Application", "chrome.exe"));

        return candidates.stream()
                .filter(path -> path != null && Files.isRegularFile(path))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Khong tim thay Google Chrome tren may."));
    }

    private static Path pathFromEnvironment(String variable, String... children) {
        String root = System.getenv(variable);
        if (root == null || root.isBlank()) {
            return null;
        }
        return Path.of(root, children);
    }
}
