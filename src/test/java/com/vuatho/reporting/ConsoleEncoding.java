package com.vuatho.reporting;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Cấu hình encoding của console để log tiếng Việt hiển thị nhất quán trên các môi trường.
 */
public final class ConsoleEncoding {
    private static boolean configured;

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
}
