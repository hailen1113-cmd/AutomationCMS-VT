package com.vuatho.testdata;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Cung cấp dữ liệu hợp lệ, dữ liệu xóa và kỳ vọng cho các test chỉnh sửa eKYC.
 */
public final class EkycInformationTestData {
    private static final List<EkycInformationCase> CASES = List.of(
            informationCase(
                    "EDIT-001",
                    "Sua thong tin KYC khi form da co du lieu",
                    EkycInformationAction.EDIT,
                    EkycInformationDataState.HAS_DATA,
                    allFields(),
                    true),
            informationCase(
                    "EDIT-002",
                    "Nhap thong tin KYC khi form chua co du lieu",
                    EkycInformationAction.EDIT,
                    EkycInformationDataState.NO_DATA,
                    allFields(),
                    true),
            informationCase(
                    "EDIT-014",
                    "Mo form co du lieu roi dong khong luu",
                    EkycInformationAction.EDIT,
                    EkycInformationDataState.HAS_DATA,
                    allFields(),
                    false),
            informationCase(
                    "CLEAR-008",
                    "Xoa toan bo thong tin KYC khi form da co du lieu roi luu",
                    EkycInformationAction.CLEAR,
                    EkycInformationDataState.HAS_DATA,
                    allFields(),
                    true),
            informationCase(
                    "CLEAR-009",
                    "Xoa toan bo thong tin KYC khi form da co du lieu roi huy",
                    EkycInformationAction.CLEAR,
                    EkycInformationDataState.HAS_DATA,
                    allFields(),
                    false),
            informationCase(
                    "CLEAR-010",
                    "Xoa mot truong KYC roi nhap lai gia tri hop le",
                    EkycInformationAction.CLEAR,
                    EkycInformationDataState.HAS_DATA,
                    List.of(EkycInformationField.DOCUMENT_NUMBER),
                    true,
                    true),
            informationCase(
                    "CLEAR-011",
                    "Mo form khong co du lieu va dong khong luu",
                    EkycInformationAction.CLEAR,
                    EkycInformationDataState.NO_DATA,
                    allFields(),
                    false),
            informationCase(
                    "CLEAR-023",
                    "Xoa toan bo thong tin KYC roi nhap lai va luu",
                    EkycInformationAction.CLEAR,
                    EkycInformationDataState.HAS_DATA,
                    allFields(),
                    true,
                    true));

    /**
     * Khởi tạo EkycInformationTestData với các phụ thuộc cần thiết.
     */
    private EkycInformationTestData() {
    }

    /**
     * Thực hiện xử lý cases trong luồng kiểm thử.
     * @return kết quả cases sau khi xử lý
     */
    public static List<EkycInformationCase> cases() {
        return CASES;
    }

    /**
     * Cập nhật cases trong luồng kiểm thử.
     * @return kết quả edit cases sau khi xử lý
     */
    public static List<EkycInformationCase> editCases() {
        return CASES.stream()
                .filter(testCase -> testCase.action() == EkycInformationAction.EDIT)
                .toList();
    }

    /**
     * Xóa hoặc đặt lại cases trong luồng kiểm thử.
     * @return kết quả clear cases sau khi xử lý
     */
    public static List<EkycInformationCase> clearCases() {
        return CASES.stream()
                .filter(testCase -> testCase.action() == EkycInformationAction.CLEAR)
                .toList();
    }

    /**
     * Tìm by id trong luồng kiểm thử.
     * @param id giá trị id được truyền vào
     * @return kết quả find by id sau khi xử lý
     */
    public static Optional<EkycInformationCase> findById(String id) {
        return CASES.stream()
                .filter(testCase -> testCase.id().equalsIgnoreCase(id))
                .findFirst();
    }

    /**
     * Thực hiện xử lý contains id trong luồng kiểm thử.
     * @param id giá trị id được truyền vào
     * @return kết quả contains id sau khi xử lý
     */
    public static boolean containsId(String id) {
        return findById(id).isPresent();
    }

    /**
     * Thực hiện xử lý all fields trong luồng kiểm thử.
     * @return kết quả all fields sau khi xử lý
     */
    private static List<EkycInformationField> allFields() {
        Set<EkycInformationField> fields = EnumSet.allOf(EkycInformationField.class);
        return List.copyOf(fields);
    }

    private static EkycInformationCase informationCase(
            String id,
            String scenario,
            EkycInformationAction action,
            EkycInformationDataState dataState,
            List<EkycInformationField> fields,
            boolean saveChanges) {
        return informationCase(id, scenario, action, dataState, fields, saveChanges, false);
    }

    private static EkycInformationCase informationCase(
            String id,
            String scenario,
            EkycInformationAction action,
            EkycInformationDataState dataState,
            List<EkycInformationField> fields,
            boolean saveChanges,
            boolean refillAfterClear) {
        return new EkycInformationCase(id, scenario, action, dataState, fields, saveChanges, refillAfterClear);
    }
}
