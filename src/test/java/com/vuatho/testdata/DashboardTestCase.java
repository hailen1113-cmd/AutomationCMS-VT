package com.vuatho.testdata;

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

    public boolean isDirectlyAutomatable() {
        return "Automate".equalsIgnoreCase(automationFeasibility.strip());
    }

    @Override
    public String toString() {
        return id + " - " + scenario;
    }
}
