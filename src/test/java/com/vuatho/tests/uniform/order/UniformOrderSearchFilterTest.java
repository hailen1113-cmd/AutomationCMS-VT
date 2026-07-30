package com.vuatho.tests.uniform.order;

import com.vuatho.testcases.UniformOrderTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformOrderPage.OrderRow;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Predicate;

/** Kiểm tra tìm kiếm, từng bộ lọc và các tổ hợp bộ lọc đơn hàng. */
public class UniformOrderSearchFilterTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformOrderSearchFilterTest.class,
                "Đồng phục", "Tìm kiếm và bộ lọc đơn hàng");
    }

    /** Tìm theo tên khách được lấy động từ dòng dữ liệu thật. */
    @Test(groups = {"uniform", "order", "search", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_006)
    public void searchByCustomerNameReturnsMatchingRows() {
        uniformOrderPage.open();
        String customer = asciiCustomerSeed();
        uniformOrderPage.selectSearchMode("theo tên").search(customer);
        assertRowsMatch(row -> contains(row.customer(), customer),
                "tên khách " + customer);
    }

    /** Tìm theo SĐT được lấy động từ dòng dữ liệu thật. */
    @Test(groups = {"uniform", "order", "search", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_007)
    public void searchByPhoneReturnsMatchingRows() {
        uniformOrderPage.open();
        String phone = uniformOrderPage.rows().get(0).phone();
        String digits = phone.replaceAll("\\D", "");
        String searchDigits = digits.substring(Math.max(0, digits.length() - 9));
        uniformOrderPage.selectSearchMode("theo SĐT").search(searchDigits);
        assertRowsMatch(row -> row.phone().replaceAll("\\D", "").contains(searchDigits),
                "SĐT " + searchDigits);
    }

    /** Popup lọc phải công bố đủ ba nhóm điều kiện nghiệp vụ. */
    @Test(groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_008)
    public void filterPopupContainsAllOptions() {
        String popup = uniformOrderPage.open().openFilter();
        for (String option : new String[]{
                "Chờ xác nhận", "Đã giao hàng cho bên vận chuyển",
                "Đã hoàn tất", "Đã hủy",
                "Chưa thanh toán", "Đã thanh toán",
                "COD", "Chuyển khoản ngân hàng",
                "Thanh toán trực tiếp tại VP"}) {
            Assert.assertTrue(popup.contains(option),
                    "Popup lọc thiếu " + option);
        }
    }

    /** Mỗi trạng thái đơn phải chỉ trả dòng đúng trạng thái đã chọn. */
    @Test(dataProvider = "orderStatuses",
            groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_009)
    public void eachOrderStatusFiltersRows(String status) {
        uniformOrderPage.open().openFilter();
        uniformOrderPage.chooseFilter(status);
        List<OrderRow> rows = uniformOrderPage.rows();
        Assert.assertTrue(rows.stream().allMatch(
                        row -> row.orderStatus().equals(status)),
                "Có dòng sai trạng thái " + status + ": " + rows);
    }

    /** Mỗi trạng thái thanh toán phải chỉ trả dòng đúng điều kiện. */
    @Test(dataProvider = "paymentStatuses",
            groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_010)
    public void eachPaymentStatusFiltersRows(String status) {
        uniformOrderPage.open().openFilter();
        uniformOrderPage.chooseFilter(status);
        Assert.assertTrue(uniformOrderPage.rows().stream().allMatch(
                        row -> row.paymentStatus().equals(status)),
                "Có dòng sai trạng thái thanh toán " + status);
    }

    /** Mỗi phương thức thanh toán phải chỉ trả dòng đúng điều kiện. */
    @Test(dataProvider = "paymentMethods",
            groups = {"uniform", "order", "filter", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_011)
    public void eachPaymentMethodFiltersRows(String method) {
        uniformOrderPage.open().openFilter();
        uniformOrderPage.chooseFilter(method);
        Assert.assertTrue(uniformOrderPage.rows().stream().allMatch(
                        row -> row.paymentMethod().equals(method)),
                "Có dòng sai phương thức thanh toán " + method);
    }

    /** Kết hợp ba nhóm lọc bằng dữ liệu của dòng thật để chắc chắn có kết quả. */
    @Test(groups = {"uniform", "order", "filter-combination", "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_012)
    public void combinedFiltersReturnOnlyMatchingRows() {
        uniformOrderPage.open();
        OrderRow seed = uniformOrderPage.rows().stream()
                .filter(row -> !row.orderStatus().isBlank()
                        && !row.paymentStatus().isBlank()
                        && !row.paymentMethod().isBlank())
                .findFirst().orElseThrow();

        uniformOrderPage.openFilter();
        uniformOrderPage.chooseFilter(seed.orderStatus())
                .chooseFilter(seed.paymentStatus())
                .chooseFilter(seed.paymentMethod());

        List<OrderRow> rows = uniformOrderPage.rows();
        Assert.assertFalse(rows.isEmpty(),
                "Tổ hợp lấy từ đơn #" + seed.id() + " lại không trả dữ liệu.");
        Assert.assertTrue(rows.stream().allMatch(row ->
                        row.orderStatus().equals(seed.orderStatus())
                                && row.paymentStatus().equals(seed.paymentStatus())
                                && row.paymentMethod().equals(seed.paymentMethod())),
                "Tổ hợp bộ lọc trả dòng không đúng điều kiện.");
    }

    /** Từ khóa và trạng thái đơn phải kết hợp đồng thời, reset phải xóa điều kiện. */
    @Test(groups = {"uniform", "order", "filter-combination", "search",
            "data-interaction"},
            description = UniformOrderTestCases.UNI_ORD_013)
    public void searchAndStatusCombinationCanBeReset() {
        uniformOrderPage.open();
        String customer = asciiCustomerSeed();
        uniformOrderPage.selectSearchMode("theo tên").search(customer);
        /*
         * Chọn trạng thái từ chính tập kết quả sau tìm kiếm. Cùng một khách có
         * thể có nhiều đơn/trạng thái và thứ tự backend không giống trang mặc định.
         */
        OrderRow seed = uniformOrderPage.rows().stream()
                .filter(row -> contains(row.customer(), customer))
                .findFirst().orElseThrow();
        uniformOrderPage.openFilter();
        uniformOrderPage.chooseFilter(seed.orderStatus());

        assertRowsMatch(row -> contains(row.customer(), seed.customer())
                        && row.orderStatus().equals(seed.orderStatus()),
                "tên + trạng thái");
        uniformOrderPage.reset();
        Assert.assertTrue(uniformOrderPage.totalDisplayed() > 0);
    }

    private void assertRowsMatch(Predicate<OrderRow> condition, String conditionName) {
        List<OrderRow> rows = uniformOrderPage.rows();
        Assert.assertFalse(rows.isEmpty(),
                "Không có kết quả cho " + conditionName);
        List<String> mismatches = rows.stream()
                .filter(row -> !condition.test(row))
                .map(row -> "#" + row.id()
                        + " | customer=" + TextNormalizer.normalize(row.customer())
                        + " | status=" + TextNormalizer.normalize(row.orderStatus()))
                .toList();
        Assert.assertTrue(rows.stream().allMatch(condition),
                "Có dòng không khớp " + TextNormalizer.normalize(conditionName)
                        + ": " + mismatches);
    }

    /** Chọn tên chỉ chứa ASCII để không phụ thuộc cách backend chuẩn hóa dấu. */
    private String asciiCustomerSeed() {
        return uniformOrderPage.rows().stream()
                .map(OrderRow::customer)
                .filter(name -> name.matches("[A-Za-z0-9 ]+"))
                .filter(name -> name.length() >= 3)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có tên khách ASCII để kiểm tra tìm kiếm."));
    }

    private static boolean contains(String actual, String expected) {
        return TextNormalizer.normalize(actual)
                .contains(TextNormalizer.normalize(expected));
    }

    @DataProvider(name = "orderStatuses")
    public Object[][] orderStatuses() {
        return new Object[][]{
                {"Chờ xác nhận"},
                {"Đã giao hàng cho bên vận chuyển"},
                {"Đã hoàn tất"},
                {"Đã hủy"}
        };
    }

    @DataProvider(name = "paymentStatuses")
    public Object[][] paymentStatuses() {
        return new Object[][]{{"Chưa thanh toán"}, {"Đã thanh toán"}};
    }

    @DataProvider(name = "paymentMethods")
    public Object[][] paymentMethods() {
        return new Object[][]{
                {"COD"},
                {"Chuyển khoản ngân hàng"},
                {"Thanh toán trực tiếp tại VP"}
        };
    }
}
