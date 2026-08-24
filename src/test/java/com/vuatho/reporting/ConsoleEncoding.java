package com.vuatho.reporting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Cấu hình encoding của console để log tiếng Việt hiển thị nhất quán trên các môi trường.
 */
public final class ConsoleEncoding {
    private static boolean configured;
    private static boolean testReportOnly;

    /**
     * Khởi tạo ConsoleEncoding với các phụ thuộc cần thiết.
     */
    private ConsoleEncoding() {
    }

    /**
     * Thực hiện xử lý use utf8 trong luồng kiểm thử.
     */
    public static synchronized void useUtf8() {
        if (configured) {
            return;
        }
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        System.setProperty("file.encoding", StandardCharsets.UTF_8.name());
        System.setProperty("sun.stdout.encoding", StandardCharsets.UTF_8.name());
        System.setProperty("sun.stderr.encoding", StandardCharsets.UTF_8.name());
        configured = true;
    }

    /**
     * Chỉ cho phép terminal hiển thị testcase đang chạy.
     * Các log thao tác, WebDriver và kết quả vẫn được xử lý trong report nhưng không làm
     * nhiễu màn hình quan sát của tester.
     */
    public static synchronized void showOnlyTestReport() {
        useUtf8();
        if (testReportOnly) {
            return;
        }
        System.setOut(testReportOnly(System.out));
        System.setErr(testReportOnly(System.err));
        testReportOnly = true;
    }

    private static PrintStream testReportOnly(PrintStream destination) {
        return new PrintStream(
                new TestReportOutputStream(destination),
                true,
                StandardCharsets.UTF_8);
    }

    /** Gom output theo từng dòng và chỉ chuyển tiếp các dòng thuộc console report TestNG. */
    private static final class TestReportOutputStream extends OutputStream {
        private final PrintStream destination;
        private final ByteArrayOutputStream line = new ByteArrayOutputStream();

        private TestReportOutputStream(PrintStream destination) {
            this.destination = destination;
        }

        @Override
        public synchronized void write(int value) {
            if (value == '\n') {
                publishLine();
                return;
            }
            line.write(value);
        }

        @Override
        public synchronized void write(byte[] values, int offset, int length) {
            for (int index = offset; index < offset + length; index++) {
                write(values[index]);
            }
        }

        @Override
        public synchronized void flush() {
            destination.flush();
        }

        @Override
        public synchronized void close() throws IOException {
            line.reset();
            destination.flush();
        }

        private void publishLine() {
            String text = line.toString(StandardCharsets.UTF_8)
                    .replaceFirst("\\r$", "");
            line.reset();
            if (isReportLine(text)) {
                byte[] utf8 = (text + System.lineSeparator())
                        .getBytes(StandardCharsets.UTF_8);
                destination.write(utf8, 0, utf8.length);
                destination.flush();
            }
        }

        private boolean isReportLine(String text) {
            String trimmed = text == null ? "" : text.trim();
            return trimmed.startsWith("=")
                    || trimmed.startsWith("STARTING:")
                    || trimmed.startsWith("RUNNING:")
                    || trimmed.startsWith("Scenario:")
                    || trimmed.startsWith("Class   :")
                    || trimmed.startsWith("Method  :")
                    || trimmed.startsWith("[PASS]")
                    || trimmed.startsWith("[FAIL]")
                    || trimmed.startsWith("[SKIP]")
                    || trimmed.startsWith("Reason:")
                    || trimmed.startsWith("Total :")
                    || trimmed.startsWith("PASS  :")
                    || trimmed.startsWith("FAIL  :")
                    || trimmed.startsWith("SKIP  :")
                    || trimmed.startsWith("Mo WebDriver")
                    || trimmed.startsWith("Dong WebDriver")
                    || trimmed.startsWith("Dung lai WebDriver")
                    || trimmed.startsWith("Su dung Chrome")
                    || trimmed.startsWith("Giu WebDriver")
                    || trimmed.startsWith("FAILED CONFIGURATION")
                    || trimmed.startsWith("SKIPPED CONFIGURATION")
                    || trimmed.startsWith("Please complete")
                    || trimmed.startsWith("Google dang")
                    || trimmed.contains("Exception")
                    || trimmed.contains("Caused by:")
                    || trimmed.contains("user data directory");
        }
    }
}
