package com.vuatho.hub;

import java.util.List;

/** Mô hình dữ liệu dùng chung cho catalog, implementation và kết quả chạy. */
final class TestCaseHubModels {
    private TestCaseHubModels() {}

    record CatalogEntry(String id, String scenario, String module, String catalogFile) {}

    record Implementation(
            String id,
            String className,
            String methodName,
            String flowType,
            String flowNote) {}

    record RunResult(
            String id,
            String status,
            String duration,
            String error,
            String className,
            String methodName,
            String suite,
            String parameters) {}

    record HubCase(
            String id,
            String module,
            String testcase,
            String flow,
            String flowDetail,
            boolean implemented,
            String className,
            String methodName,
            String status,
            String duration,
            String error,
            String suite) {}

    record HubSnapshot(
            String generatedAt,
            String suiteName,
            int catalogCount,
            int implementedCount,
            int passed,
            int failed,
            int skipped,
            int notRun,
            List<HubCase> cases) {}
}
