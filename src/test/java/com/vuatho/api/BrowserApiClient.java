package com.vuatho.api;

import com.vuatho.config.TestConfig;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class BrowserApiClient {
    private static final String REQUEST_SCRIPT = """
            const done = arguments[arguments.length - 1];
            const method = arguments[0];
            const url = arguments[1];
            const body = arguments[2];
            const authenticated = arguments[3];
            const token = (() => {
              if (!authenticated) return '';
              const stores = [window.localStorage, window.sessionStorage];
              for (const store of stores) {
                for (let i = 0; i < store.length; i++) {
                  const key = store.key(i);
                  const value = store.getItem(key);
                  if (!value) continue;
                  if (/bearer\\s+[a-z0-9._-]+/i.test(value)) {
                    return value.match(/bearer\\s+([a-z0-9._-]+)/i)[1];
                  }
                  if (/token|access|auth|jwt/i.test(key) && /^[A-Za-z0-9._-]{20,}$/.test(value)) return value;
                  const nested = value.match(/"(?:accessToken|token|jwt|access_token)"\\s*:\\s*"([^"]{20,})"/i);
                  if (nested) return nested[1];
                }
              }
              return '';
            })();
            const headers = { Accept: 'application/json' };
            if (token) headers.Authorization = `Bearer ${token}`;
            if (body !== null && body !== undefined) headers['Content-Type'] = 'application/json';
            fetch(url, { method, headers, body, credentials: 'omit' })
              .then(async response => done({
                status: response.status,
                ok: response.ok,
                contentType: response.headers.get('content-type') || '',
                body: await response.text()
              }))
              .catch(error => done({ status: 0, ok: false, contentType: '', body: String(error) }));
            """;

    private final WebDriver driver;
    private final String apiBaseUrl;

    /**
     * Khởi tạo BrowserApiClient với các phụ thuộc cần thiết.
     * @param driver WebDriver đang điều khiển trình duyệt
     */
    public BrowserApiClient(WebDriver driver) {
        this.driver = driver;
        this.apiBaseUrl = normalizeBaseUrl(TestConfig.apiBaseUrl());
    }

    /**
     * Trả về get từ trạng thái hiện tại.
     * @param pathAndQuery giá trị path and query được truyền vào
     * @return kết quả get sau khi xử lý
     */
    public ApiResponse get(String pathAndQuery) {
        return request("GET", pathAndQuery, null);
    }

    /**
     * Thực hiện xử lý post trong luồng kiểm thử.
     * @param pathAndQuery giá trị path and query được truyền vào
     * @param body giá trị body được truyền vào
     * @return kết quả post sau khi xử lý
     */
    public ApiResponse post(String pathAndQuery, String body) {
        return request("POST", pathAndQuery, body);
    }

    /**
     * Thực hiện xử lý put trong luồng kiểm thử.
     * @param pathAndQuery giá trị path and query được truyền vào
     * @param body giá trị body được truyền vào
     * @return kết quả put sau khi xử lý
     */
    public ApiResponse put(String pathAndQuery, String body) {
        return request("PUT", pathAndQuery, body);
    }

    /**
     * Trả về get without auth từ trạng thái hiện tại.
     * @param pathAndQuery giá trị path and query được truyền vào
     * @return kết quả get without auth sau khi xử lý
     */
    public ApiResponse getWithoutAuth(String pathAndQuery) {
        return requestWithoutAuth("GET", pathAndQuery, null);
    }

    /**
     * Thực hiện xử lý request trong luồng kiểm thử.
     * @param method giá trị method được truyền vào
     * @param pathAndQuery giá trị path and query được truyền vào
     * @param body giá trị body được truyền vào
     * @return kết quả request sau khi xử lý
     */
    private ApiResponse request(String method, String pathAndQuery, String body) {
        return request(method, pathAndQuery, body, true);
    }

    /**
     * Thực hiện xử lý request without auth trong luồng kiểm thử.
     * @param method giá trị method được truyền vào
     * @param pathAndQuery giá trị path and query được truyền vào
     * @param body giá trị body được truyền vào
     * @return kết quả request without auth sau khi xử lý
     */
    private ApiResponse requestWithoutAuth(String method, String pathAndQuery, String body) {
        return request(method, pathAndQuery, body, false);
    }

    /**
     * Thực hiện xử lý request trong luồng kiểm thử.
     * @param method giá trị method được truyền vào
     * @param pathAndQuery giá trị path and query được truyền vào
     * @param body giá trị body được truyền vào
     * @param authenticated giá trị authenticated được truyền vào
     * @return kết quả request sau khi xử lý
     */
    private ApiResponse request(String method, String pathAndQuery, String body, boolean authenticated) {
        Object result = ((JavascriptExecutor) driver).executeAsyncScript(
                REQUEST_SCRIPT,
                method, absoluteUrl(pathAndQuery), body, authenticated);

        if (!(result instanceof Map<?, ?> raw)) {
            throw new IllegalStateException("Unexpected API response shape: " + result);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        raw.forEach((key, value) -> response.put(String.valueOf(key), value));
        int status = ((Number) response.getOrDefault("status", 0)).intValue();
        boolean ok = Boolean.TRUE.equals(response.get("ok"));
        String contentType = String.valueOf(response.getOrDefault("contentType", ""));
        String responseBody = String.valueOf(response.getOrDefault("body", ""));
        return new ApiResponse(status, ok, contentType, responseBody);
    }

    /**
     * Thực hiện xử lý absolute url trong luồng kiểm thử.
     * @param pathAndQuery giá trị path and query được truyền vào
     * @return kết quả absolute url sau khi xử lý
     */
    private String absoluteUrl(String pathAndQuery) {
        if (pathAndQuery.startsWith("http://") || pathAndQuery.startsWith("https://")) {
            return pathAndQuery;
        }
        String normalizedPath = pathAndQuery.startsWith("/") ? pathAndQuery : "/" + pathAndQuery;
        return apiBaseUrl + normalizedPath;
    }

    /**
     * Thực hiện xử lý normalize base url trong luồng kiểm thử.
     * @param baseUrl giá trị base url được truyền vào
     * @return kết quả normalize base url sau khi xử lý
     */
    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl;
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        URI.create(normalized);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
