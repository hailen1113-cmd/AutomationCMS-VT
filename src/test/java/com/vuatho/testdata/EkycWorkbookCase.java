package com.vuatho.testdata;

import com.vuatho.utils.TextNormalizer;

public record EkycWorkbookCase(
        String id,
        String module,
        String scenario,
        String precondition,
        String accountCode,
        String dataCode,
        String steps,
        String expected,
        String rule,
        String locators,
        String runGroup,
        String changesData,
        String recovery,
        String readiness,
        String severity,
        String priority,
        String testType,
        String implementationNote) {

    public boolean isDataChanging() {
        return "co".equalsIgnoreCase(TextNormalizer.normalize(changesData));
    }

    public String family() {
        int separator = id.indexOf('-');
        return separator < 0 ? id : id.substring(0, separator);
    }

    public String managementKey() {
        return family() + " | " + module + " | " + runGroup + " | " + changesData;
    }

    @Override
    public String toString() {
        return id + " - " + scenario;
    }
}
