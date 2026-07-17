package com.vuatho.reporting;

import org.testng.ITestResult;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Chuẩn hóa tên, trạng thái, thời lượng và thông báo lỗi trước khi đưa kết quả vào báo cáo.
 */
public final class TestResultFormatter {
    private TestResultFormatter() {
    }

    /**
     * Thực hiện xử lý display name trong luồng kiểm thử.
     * @param result giá trị result được truyền vào
     * @return kết quả display name sau khi xử lý
     */
    public static String displayName(ITestResult result) {
        String description = result.getMethod().getDescription();
        String name = description == null || description.isBlank()
                ? result.getMethod().getMethodName()
                : description;
        if (result.getParameters() == null || result.getParameters().length == 0) {
            return name;
        }
        String parameters = Arrays.stream(result.getParameters())
                .filter(value -> value != null && !String.valueOf(value).isBlank())
                .map(String::valueOf)
                .collect(Collectors.joining(" > "));
        return parameters.isBlank() ? name : name + " [" + parameters + "]";
    }

    /**
     * Thực hiện xử lý console display name trong luồng kiểm thử.
     * @param result giá trị result được truyền vào
     * @return kết quả console display name sau khi xử lý
     */
    public static String consoleDisplayName(ITestResult result) {
        return ascii(displayName(result));
    }

    /**
     * Thực hiện xử lý console message trong luồng kiểm thử.
     * @param value giá trị đầu vào
     * @return kết quả console message sau khi xử lý
     */
    public static String consoleMessage(String value) {
        return ascii(value);
    }

    /**
     * Thực hiện xử lý duration trong luồng kiểm thử.
     * @param result giá trị result được truyền vào
     * @return kết quả duration sau khi xử lý
     */
    public static String duration(ITestResult result) {
        double seconds = (result.getEndMillis() - result.getStartMillis()) / 1000.0;
        return String.format("%.2fs", seconds);
    }

    /**
     * Thực hiện xử lý escape html trong luồng kiểm thử.
     * @param value giá trị đầu vào
     * @return kết quả escape html sau khi xử lý
     */
    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    /**
     * Thực hiện xử lý ascii trong luồng kiểm thử.
     * @param value giá trị đầu vào
     * @return kết quả ascii sau khi xử lý
     */
    private static String ascii(String value) {
        if (value == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return withoutAccents.replaceAll("[^\\x20-\\x7E\\r\\n\\t]", "");
    }
}
