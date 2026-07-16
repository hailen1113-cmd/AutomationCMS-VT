package com.vuatho.testdata;

import com.vuatho.navigation.MenuTarget;

public record PartnerWorkerCase(
        String id,
        String scenario,
        MenuTarget page,
        String searchPlaceholder) {

    public boolean hasSearchFilter() {
        return searchPlaceholder != null && !searchPlaceholder.isBlank();
    }

    @Override
    public String toString() {
        return id + " - " + scenario;
    }
}
