package com.vuatho.testcases;

/**
 * Readable, compile-time testcase list for this business module.
 * Each constant is used directly by exactly one business {@code @Test}.
 */
public final class SmokeTestCases {
    private SmokeTestCases() {
    }

    public static final String SMOKE_001 = "SMOKE-001 - ERP sandbox responds on the expected domain";
    public static final String SMOKE_002 = "SMOKE-002 - Automation can pass Vercel protection and reach the ERP application";
}
