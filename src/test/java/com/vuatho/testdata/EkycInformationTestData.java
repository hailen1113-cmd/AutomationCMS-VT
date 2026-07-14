package com.vuatho.testdata;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private EkycInformationTestData() {
    }

    public static List<EkycInformationCase> cases() {
        return CASES;
    }

    public static List<EkycInformationCase> editCases() {
        return CASES.stream()
                .filter(testCase -> testCase.action() == EkycInformationAction.EDIT)
                .toList();
    }

    public static List<EkycInformationCase> clearCases() {
        return CASES.stream()
                .filter(testCase -> testCase.action() == EkycInformationAction.CLEAR)
                .toList();
    }

    public static Optional<EkycInformationCase> findById(String id) {
        return CASES.stream()
                .filter(testCase -> testCase.id().equalsIgnoreCase(id))
                .findFirst();
    }

    public static boolean containsId(String id) {
        return findById(id).isPresent();
    }

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
