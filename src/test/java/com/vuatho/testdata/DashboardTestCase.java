package com.vuatho.testdata;

/**
 * Mô tả một test case Dashboard lấy từ workbook gồm mã, nhóm, thao tác và kỳ vọng.
 */
public record DashboardTestCase(
        String id,
        String module,
        String scenario,
        String precondition,
        String steps,
        String expectedResult,
        String severity,
        String priority,
        String testType,
        String automationFeasibility,
        String actualResult,
        String status,
        String tester,
        String notes) {

    /**
     * Kiểm tra điều kiện is directly automatable.
     * @return kết quả is directly automatable sau khi xử lý
     */
    public boolean isDirectlyAutomatable() {
        return "Automate".equalsIgnoreCase(automationFeasibility.strip());
    }

    /**
     * Thực hiện xử lý to string trong luồng kiểm thử.
     * @return kết quả to string sau khi xử lý
     */
    @Override
    public String toString() {
        return id + " - " + scenario;
    }
}
