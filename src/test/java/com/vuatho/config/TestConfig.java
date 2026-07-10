package com.vuatho.config;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class TestConfig {
    private static final String DEFAULT_BASE_URL = "https://erp-sandbox.vuatho.com/";

    private TestConfig() {
    }

    public static String baseUrl() {
        String url = value("baseUrl", "BASE_URL", DEFAULT_BASE_URL).trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }

    public static String baseHost() {
        String host = URI.create(baseUrl()).getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("baseUrl khong hop le: " + baseUrl());
        }
        return host;
    }

    public static boolean headless() {
        return Boolean.parseBoolean(value("headless", "HEADLESS", "true"));
    }

    public static boolean interactive() {
        return Boolean.parseBoolean(value("interactive", "INTERACTIVE", "false"));
    }

    public static boolean pauseOnFailure() {
        return Boolean.parseBoolean(value("pause.on.failure", "PAUSE_ON_FAILURE", "false"));
    }

    public static boolean captureScreenshots() {
        return Boolean.parseBoolean(value("capture.screenshots", "CAPTURE_SCREENSHOTS", "false"));
    }

    public static boolean keepBrowserOpen() {
        // Mặc định phải quit browser sau test cuối cùng; chỉ giữ lại khi bật
        // keep.browser.open=true để debug.
        return Boolean.parseBoolean(value("keep.browser.open", "KEEP_BROWSER_OPEN",
                "false"));
    }

    public static boolean runLogoutTests() {
        return Boolean.parseBoolean(value("run.logout.tests", "RUN_LOGOUT_TESTS", "false"));
    }

    public static Duration pageLoadSla() {
        return seconds("page.load.sla.seconds", "PAGE_LOAD_SLA_SECONDS", 30);
    }

    public static Duration pageLoadTimeout() {
        return seconds("page.load.timeout.seconds", "PAGE_LOAD_TIMEOUT_SECONDS", 30);
    }

    public static Duration scriptTimeout() {
        return seconds("script.timeout.seconds", "SCRIPT_TIMEOUT_SECONDS", 30);
    }

    public static Duration defaultWaitTimeout() {
        return seconds("wait.timeout.seconds", "WAIT_TIMEOUT_SECONDS", 15);
    }

    public static Duration longWaitTimeout() {
        return seconds("long.wait.timeout.seconds", "LONG_WAIT_TIMEOUT_SECONDS", 45);
    }

    public static String screenshotDirectory() {
        return value("screenshot.dir", "SCREENSHOT_DIR", "target/screenshots");
    }

    public static String summaryReportPath() {
        return value("summary.report.path", "SUMMARY_REPORT_PATH", "target/reports/test-summary.html");
    }

    public static String seleniumProfileDirectory() {
        return value("selenium.profile.dir", "SELENIUM_PROFILE_DIR", ".selenium/chrome-profile");
    }

    public static String loginEmail() {
        return value("erp.email", "ERP_EMAIL", "hailen1113@gmail.com");
    }

    public static String loginPassword() {
        return value("google.password", "GOOGLE_PASSWORD",
                value("erp.password", "ERP_PASSWORD", ""));
    }

    public static String entryUrl() {
        String secret = value("vercel.bypass.secret", "VERCEL_AUTOMATION_BYPASS_SECRET", "");
        if (secret == null || secret.isBlank()) {
            return baseUrl();
        }

        String separator = baseUrl().contains("?") ? "&" : "?";
        return baseUrl() + separator
                + "x-vercel-protection-bypass="
                + URLEncoder.encode(secret, StandardCharsets.UTF_8)
                + "&x-vercel-set-bypass-cookie=true";
    }

    private static String value(String property, String environment, String defaultValue) {
        String systemValue = System.getProperty(property);
        if (systemValue != null && !systemValue.isBlank() && !systemValue.startsWith("${")) {
            return systemValue;
        }
        String environmentValue = System.getenv(environment);
        return environmentValue == null || environmentValue.isBlank() ? defaultValue : environmentValue;
    }

    private static Duration seconds(String property, String environment, int defaultValue) {
        String configured = value(property, environment, String.valueOf(defaultValue));
        try {
            return Duration.ofSeconds(Integer.parseInt(configured));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    property + " must be a whole number of seconds, but was: " + configured, exception);
        }
    }
}
