package com.vuatho.testdata;

public enum EkycInformationField {
    FULL_NAME("ho ten", "Automation KYC Test"),
    BIRTH_DATE("ngay sinh", "14/07/2000"),
    GENDER("gioi tinh", "Nam"),
    NATIONALITY("quoc tich", "Viet Nam"),
    DOCUMENT_NUMBER("so giay to", "000000000001"),
    ORIGIN("que quan", "Automation Origin"),
    RESIDENCE("thuong tru", "Automation Residence");

    private final String normalizedLabel;
    private final String updateValue;

    EkycInformationField(String normalizedLabel, String updateValue) {
        this.normalizedLabel = normalizedLabel;
        this.updateValue = updateValue;
    }

    public String normalizedLabel() {
        return normalizedLabel;
    }

    public String updateValue() {
        return updateValue;
    }
}
