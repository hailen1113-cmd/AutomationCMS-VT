package com.vuatho.testdata;

import java.util.List;

public record EkycInformationCase(
        String id,
        String scenario,
        EkycInformationAction action,
        EkycInformationDataState dataState,
        List<EkycInformationField> fields,
        boolean saveChanges,
        boolean refillAfterClear) {

    public boolean clearsMultipleFields() {
        return action == EkycInformationAction.CLEAR && fields.size() > 1;
    }

    public boolean cancelsChanges() {
        return !saveChanges;
    }

    public boolean refillsAfterClear() {
        return action == EkycInformationAction.CLEAR && refillAfterClear;
    }
}
