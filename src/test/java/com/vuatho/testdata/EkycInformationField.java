package com.vuatho.testdata;

/**
 * Liệt kê các trường thông tin eKYC và metadata cần để định vị, nhập hoặc xóa dữ liệu.
 */
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

    /**
     * Thực hiện xử lý normalized label trong luồng kiểm thử.
     * @return kết quả normalized label sau khi xử lý
     */
    public String normalizedLabel() {
        return normalizedLabel;
    }

    /**
     * Cập nhật value trong luồng kiểm thử.
     * @return kết quả update value sau khi xử lý
     */
    public String updateValue() {
        return updateValue;
    }
}
