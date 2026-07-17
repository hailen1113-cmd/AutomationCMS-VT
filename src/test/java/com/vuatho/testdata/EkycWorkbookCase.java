package com.vuatho.testdata;

import com.vuatho.utils.TextNormalizer;

/**
 * Mô tả một test case eKYC được đọc từ workbook cùng mã, thao tác và kết quả mong đợi.
 */
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

    /**
     * Kiểm tra điều kiện is data changing.
     * @return kết quả is data changing sau khi xử lý
     */
    public boolean isDataChanging() {
        return "co".equalsIgnoreCase(TextNormalizer.normalize(changesData));
    }

    /**
     * Thực hiện xử lý family trong luồng kiểm thử.
     * @return kết quả family sau khi xử lý
     */
    public String family() {
        int separator = id.indexOf('-');
        return separator < 0 ? id : id.substring(0, separator);
    }

    /**
     * Thực hiện xử lý management key trong luồng kiểm thử.
     * @return kết quả management key sau khi xử lý
     */
    public String managementKey() {
        return family() + " | " + module + " | " + runGroup + " | " + changesData;
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
