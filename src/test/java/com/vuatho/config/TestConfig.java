package com.vuatho.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class TestConfig {
    private static final String DEFAULT_BASE_URL = "https://erp-sandbox.vuatho.com/";

    private TestConfig() {
    }

    public static String baseUrl() {
        return value("baseUrl", "BASE_URL", DEFAULT_BASE_URL);
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

    public static Duration pageLoadSla() {
        int seconds = Integer.parseInt(
                value("page.load.sla.seconds", "PAGE_LOAD_SLA_SECONDS", "30"));
        return Duration.ofSeconds(seconds);
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
        String secret = System.getenv("VERCEL_AUTOMATION_BYPASS_SECRET");
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
}
