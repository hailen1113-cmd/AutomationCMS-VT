package com.vuatho.reporting;

import com.vuatho.hub.TestCaseHubReporter;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Ghi báo cáo quản lý testcase sau khi suite hoàn tất: catalog, flow và kết quả.
 */
public final class HtmlSummaryReporter {
    /**
     * Ghi báo cáo HTML/CSV sau khi suite chạy xong.
     * @param results kết quả TestNG của lần chạy hiện tại
     * @return đường dẫn HTML đã ghi
     */
    public Path write(List<ITestResult> results) throws IOException {
        Path report = TestCaseHubReporter.write(results);
        TestCaseHubReporter.maybeOpen(report);
        return report;
    }
}
