package com.vuatho.reporting;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class ConsoleEncoding {
    private static boolean configured;

    private ConsoleEncoding() {
    }

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
