package com.vuatho.hub;

import java.nio.file.Path;

/**
 * Mở giao diện quản lý testcase: catalog, flow và kết quả lần chạy gần nhất.
 *
 * <p>Chạy {@code main} từ IDE. Sau mỗi suite TestNG, cùng báo cáo được ghi ra
 * {@code target/reports/test-summary.html} và {@code target/reports/testcase-report.csv}.</p>
 */
public final class TestCaseHub {
    private TestCaseHub() {}

    public static void main(String[] args) throws Exception {
        Path report = TestCaseHubReporter.writeLatestAndMaybeOpen();
        System.out.println("Bao cao testcase: " + report.toAbsolutePath());
        System.out.println("CSV: " + report.resolveSibling("testcase-report.csv").toAbsolutePath());
    }
}
