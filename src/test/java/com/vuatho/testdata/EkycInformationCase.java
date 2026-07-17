package com.vuatho.testdata;

import java.util.List;

/**
 * Mô tả một test case chỉnh sửa thông tin eKYC gồm trường, dữ liệu và kết quả mong đợi.
 */
public record EkycInformationCase(
        String id,
        String scenario,
        EkycInformationAction action,
        EkycInformationDataState dataState,
        List<EkycInformationField> fields,
        boolean saveChanges,
        boolean refillAfterClear) {

    /**
     * Xóa hoặc đặt lại multiple fields trong luồng kiểm thử.
     * @return kết quả clears multiple fields sau khi xử lý
     */
    public boolean clearsMultipleFields() {
        return action == EkycInformationAction.CLEAR && fields.size() > 1;
    }

    /**
     * Kiểm tra điều kiện cancels changes.
     * @return kết quả cancels changes sau khi xử lý
     */
    public boolean cancelsChanges() {
        return !saveChanges;
    }

    /**
     * Thực hiện xử lý refills after clear trong luồng kiểm thử.
     * @return kết quả refills after clear sau khi xử lý
     */
    public boolean refillsAfterClear() {
        return action == EkycInformationAction.CLEAR && refillAfterClear;
    }
}
