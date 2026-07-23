package com.vuatho.config;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class TestConfig {
    private static final String DEFAULT_BASE_URL = "https://erp-sandbox.vuatho.com/";

    /**
     * Khởi tạo TestConfig với các phụ thuộc cần thiết.
     */
    private TestConfig() {
    }

    /**
     * Thực hiện xử lý base url trong luồng kiểm thử.
     * @return kết quả base url sau khi xử lý
     */
    public static String baseUrl() {
        String url = value("baseUrl", "BASE_URL", DEFAULT_BASE_URL).trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }

    /**
     * Thực hiện xử lý base host trong luồng kiểm thử.
     * @return kết quả base host sau khi xử lý
     */
    public static String baseHost() {
        String host = URI.create(baseUrl()).getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("baseUrl khong hop le: " + baseUrl());
        }
        return host;
    }

    /**
     * Thực hiện xử lý headless trong luồng kiểm thử.
     * @return kết quả headless sau khi xử lý
     */
    public static boolean headless() {
        return Boolean.parseBoolean(value("headless", "HEADLESS", "true"));
    }

    /**
     * Thực hiện xử lý interactive trong luồng kiểm thử.
     * @return kết quả interactive sau khi xử lý
     */
    public static boolean interactive() {
        return Boolean.parseBoolean(value("interactive", "INTERACTIVE", "false"));
    }

    /**
     * Thực hiện xử lý pause on failure trong luồng kiểm thử.
     * @return kết quả pause on failure sau khi xử lý
     */
    public static boolean pauseOnFailure() {
        return Boolean.parseBoolean(value("pause.on.failure", "PAUSE_ON_FAILURE", "false"));
    }

    /**
     * Thu thập screenshots trong luồng kiểm thử.
     * @return kết quả capture screenshots sau khi xử lý
     */
    public static boolean captureScreenshots() {
        return Boolean.parseBoolean(value("capture.screenshots", "CAPTURE_SCREENSHOTS", "false"));
    }

    /**
     * Thực hiện xử lý keep browser open trong luồng kiểm thử.
     * @return kết quả keep browser open sau khi xử lý
     */
    public static boolean keepBrowserOpen() {
        // Mặc định phải quit browser sau test cuối cùng; chỉ giữ lại khi bật
        // keep.browser.open=true để debug.
        return Boolean.parseBoolean(value("keep.browser.open", "KEEP_BROWSER_OPEN",
                "false"));
    }

    /**
     * Thực thi logout tests trong luồng kiểm thử.
     * @return kết quả run logout tests sau khi xử lý
     */
    public static boolean runLogoutTests() {
        return Boolean.parseBoolean(value("run.logout.tests", "RUN_LOGOUT_TESTS", "false"));
    }

    /**
     * Thực hiện xử lý page load sla trong luồng kiểm thử.
     * @return kết quả page load sla sau khi xử lý
     */
    public static Duration pageLoadSla() {
        return seconds("page.load.sla.seconds", "PAGE_LOAD_SLA_SECONDS", 30);
    }

    /**
     * Thực hiện xử lý page load timeout trong luồng kiểm thử.
     * @return kết quả page load timeout sau khi xử lý
     */
    public static Duration pageLoadTimeout() {
        return seconds("page.load.timeout.seconds", "PAGE_LOAD_TIMEOUT_SECONDS", 30);
    }

    /**
     * Thực hiện xử lý script timeout trong luồng kiểm thử.
     * @return kết quả script timeout sau khi xử lý
     */
    public static Duration scriptTimeout() {
        return seconds("script.timeout.seconds", "SCRIPT_TIMEOUT_SECONDS", 30);
    }

    /**
     * Thực hiện xử lý default wait timeout trong luồng kiểm thử.
     * @return kết quả default wait timeout sau khi xử lý
     */
    public static Duration defaultWaitTimeout() {
        return seconds("wait.timeout.seconds", "WAIT_TIMEOUT_SECONDS", 15);
    }

    /**
     * Thực hiện xử lý long wait timeout trong luồng kiểm thử.
     * @return kết quả long wait timeout sau khi xử lý
     */
    public static Duration longWaitTimeout() {
        return seconds("long.wait.timeout.seconds", "LONG_WAIT_TIMEOUT_SECONDS", 45);
    }

    /**
     * Thực hiện xử lý screenshot directory trong luồng kiểm thử.
     * @return kết quả screenshot directory sau khi xử lý
     */
    public static String screenshotDirectory() {
        return value("screenshot.dir", "SCREENSHOT_DIR", "target/screenshots");
    }

    /**
     * Thực hiện xử lý summary report path trong luồng kiểm thử.
     * @return kết quả summary report path sau khi xử lý
     */
    public static String summaryReportPath() {
        return value("summary.report.path", "SUMMARY_REPORT_PATH", "target/reports/test-summary.html");
    }

    /**
     * Thực hiện xử lý selenium profile directory trong luồng kiểm thử.
     * @return kết quả selenium profile directory sau khi xử lý
     */
    public static String seleniumProfileDirectory() {
        String configured = value("selenium.profile.dir", "SELENIUM_PROFILE_DIR", "");
        if (!configured.isBlank()) {
            return configured;
        }
        // Mọi cách chạy (IDE, Maven, headless hay có giao diện) đều dùng chung profile
        // automation cố định để cookie ERP, Google và Vercel không bị mất theo PID.
        return persistentSeleniumProfileDirectory();
    }

    /**
     * Thực hiện xử lý persistent selenium profile directory trong luồng kiểm thử.
     * @return kết quả persistent selenium profile directory sau khi xử lý
     */
    public static String persistentSeleniumProfileDirectory() {
        return ".selenium/chrome-profile";
    }

    /**
     * Kiểm tra điều kiện has vercel bypass secret.
     * @return kết quả has vercel bypass secret sau khi xử lý
     */
    public static boolean hasVercelBypassSecret() {
        return !value("vercel.bypass.secret", "VERCEL_AUTOMATION_BYPASS_SECRET", "").isBlank();
    }

    /**
     * Thực hiện xử lý login email trong luồng kiểm thử.
     * @return kết quả login email sau khi xử lý
     */
    public static String loginEmail() {
        return value("erp.email", "ERP_EMAIL", "hailen1113@gmail.com");
    }

    /**
     * Thực hiện xử lý login password trong luồng kiểm thử.
     * @return kết quả login password sau khi xử lý
     */
    public static String loginPassword() {
        return value("google.password", "GOOGLE_PASSWORD",
                value("erp.password", "ERP_PASSWORD", ""));
    }

    /**
     * Thực hiện xử lý entry url trong luồng kiểm thử.
     * @return kết quả entry url sau khi xử lý
     */
    public static String entryUrl() {
        String secret = value("vercel.bypass.secret", "VERCEL_AUTOMATION_BYPASS_SECRET", "");
        if (secret.isBlank()) {
            return baseUrl();
        }

        String separator = baseUrl().contains("?") ? "&" : "?";
        return baseUrl() + separator
                + "x-vercel-protection-bypass="
                + URLEncoder.encode(secret, StandardCharsets.UTF_8)
                + "&x-vercel-set-bypass-cookie=true";
    }

    /**
     * Thực hiện xử lý api base url trong luồng kiểm thử.
     * @return kết quả api base url sau khi xử lý
     */
    public static String apiBaseUrl() {
        String configured = value("api.baseUrl", "API_BASE_URL", "").trim();
        if (!configured.isBlank()) {
            return configured;
        }
        if ("erp-sandbox.vuatho.com".equalsIgnoreCase(baseHost())) {
            return "https://sandbox-api-cms.vuatho.com";
        }
        return baseUrl();
    }

    /**
     * Thực thi mutating api tests trong luồng kiểm thử.
     * @return kết quả run mutating api tests sau khi xử lý
     */
    public static boolean runMutatingApiTests() {
        return Boolean.parseBoolean(value("run.mutating.api.tests", "RUN_MUTATING_API_TESTS", "false"));
    }

    public static boolean runWorkerPostMutations() {
        return Boolean.parseBoolean(value(
                "run.worker.post.mutations", "RUN_WORKER_POST_MUTATIONS", "true"));
    }

    public static String workerPostApproveMarker() {
        return value("worker.post.approve.marker", "WORKER_POST_APPROVE_MARKER", "");
    }

    public static String workerPostRejectMarker() {
        return value("worker.post.reject.marker", "WORKER_POST_REJECT_MARKER", "");
    }

    /**
     * Thực hiện xử lý value trong luồng kiểm thử.
     * @param property giá trị property được truyền vào
     * @param environment giá trị environment được truyền vào
     * @param defaultValue giá trị default value được truyền vào
     * @return kết quả value sau khi xử lý
     */
    private static String value(String property, String environment, String defaultValue) {
        String systemValue = System.getProperty(property);
        if (systemValue != null && !systemValue.isBlank() && !systemValue.startsWith("${")) {
            return systemValue;
        }
        String environmentValue = System.getenv(environment);
        return environmentValue == null || environmentValue.isBlank() ? defaultValue : environmentValue;
    }

    /**
     * Thực hiện xử lý seconds trong luồng kiểm thử.
     * @param property giá trị property được truyền vào
     * @param environment giá trị environment được truyền vào
     * @param defaultValue giá trị default value được truyền vào
     * @return kết quả seconds sau khi xử lý
     */
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
