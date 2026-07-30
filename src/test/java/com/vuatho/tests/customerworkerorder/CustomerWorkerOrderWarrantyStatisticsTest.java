package com.vuatho.tests.customerworkerorder;

import com.vuatho.testcases.CustomerWorkerOrderTestCases;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.customerworkerorder.CustomerWorkerOrderTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Kiểm tra popup Thống kê Bảo hành 5K.
 *
 * <p>Bao phủ KPI, biểu đồ, tooltip khi trỏ chuột vào cột, thống kê theo dịch
 * vụ, các mốc Tuần/Tháng/Quý và khoảng ngày tự chọn. Nhóm này chỉ đọc dữ liệu
 * báo cáo, không tạo hay cập nhật bảo hành.</p>
 */
public class CustomerWorkerOrderWarrantyStatisticsTest
        extends CustomerWorkerOrderTestSupport {
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Chạy toàn bộ file hoặc riêng group truyền qua {@code customer.order.group}. */
    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(CustomerWorkerOrderWarrantyStatisticsTest.class,
                    "Đơn Khách - Thợ", "Thống kê Bảo hành 5K");
        } else {
            TestNgRunner.runGroup("Đơn Khách - Thợ",
                    "Thống kê Bảo hành - " + group,
                    group, CustomerWorkerOrderWarrantyStatisticsTest.class);
        }
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_062)
    /** Kiểm tra popup có đủ KPI, hai chart, bộ lọc thời gian và dịch vụ. */
    public void popupReturnsKpisChartsAndServices() {
        orderPage.openStatistic("Bảo hành 5K");
        String text = orderPage.statisticsText();

        for (String expected : List.of(
                "Thống kê Bảo hành 5K", "Tổng đơn bảo hành",
                "Tổng thu phí BH", "TB đơn/ngày", "TB phí BH/ngày",
                "Tuần này", "Tháng này", "Quý này",
                "Số lượng đơn theo ngày", "Doanh thu bảo hành",
                "Theo dịch vụ")) {
            Assert.assertTrue(text.contains(expected),
                    "Popup Bảo hành thiếu " + expected);
        }
        Assert.assertEquals(orderPage.statisticsInputValues().size(), 2);
        Assert.assertEquals(orderPage.statisticsChartCount(), 2);
        Assert.assertTrue(orderPage.statisticsBarCount("order_count") > 0);
        Assert.assertTrue(orderPage.statisticsBarCount("revenue") > 0);
        assertAllKpisNumeric();
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-period", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_063)
    /** Chuyển Tuần/Tháng/Quý và đối chiếu input ngày với range kỳ vọng. */
    public void supportsEveryPeriod() {
        orderPage.openStatistic("Bảo hành 5K");
        for (String period : List.of("Tuần này", "Tháng này", "Quý này")) {
            orderPage.clickStatisticsButton(period);
            Assert.assertTrue(orderPage.statisticsButtonSelected(period));
            Assert.assertEquals(orderPage.statisticsInputValues(),
                    expectedRange(period));
            Assert.assertEquals(orderPage.statisticsChartCount(), 2,
                    "Popup không reload chart tại " + period);
        }
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "chart-hover", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_064)
    /** Hover cột có dữ liệu của chart số lượng và kiểm tra tooltip thật. */
    public void orderCountChartReturnsRealTooltip() {
        orderPage.openStatistic("Bảo hành 5K");
        String tooltip = orderPage.hoverStatisticsBar("order_count");
        Assert.assertFalse(tooltip.isBlank());
        Assert.assertTrue(tooltip.matches("(?s).*\\d.*"));
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-scroll", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_065)
    /** Cuộn tới thống kê dịch vụ, đóng và mở lại popup ở trạng thái sạch. */
    public void servicesCanBeObservedAndDialogReopened() {
        orderPage.openStatistic("Bảo hành 5K");
        orderPage.scrollStatisticsTo("Theo dịch vụ");
        Assert.assertFalse(orderPage.waitStatisticsTextMatches(
                        "(?s).*Theo dịch vụ\\s+\\d+ dịch vụ.*\\d+ đơn.*")
                        .isBlank());

        orderPage.closeStatistics();
        orderPage.openStatistic("Bảo hành 5K");
        Assert.assertEquals(orderPage.statisticsChartCount(), 2);
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "chart-hover", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_066)
    /** Hover cột có dữ liệu của chart doanh thu và kiểm tra tooltip thật. */
    public void revenueChartReturnsRealTooltip() {
        orderPage.openStatistic("Bảo hành 5K");
        String tooltip = orderPage.hoverStatisticsBar("revenue");
        Assert.assertFalse(tooltip.isBlank());
        Assert.assertTrue(tooltip.matches("(?s).*\\d.*"));
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-custom-date",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_067)
    /** Nhập thủ công khoảng ngày và đối chiếu input/range sau khi áp dụng. */
    public void supportsCustomDateRange() {
        orderPage.openStatistic("Bảo hành 5K");
        List<String> range = customDateRange();
        orderPage.setStatisticsCustomDateRange(
                range.get(0), range.get(1));

        Assert.assertEquals(orderPage.statisticsInputValues(), range);
        assertAllKpisNumeric();
        Assert.assertEquals(orderPage.statisticsChartCount(), 2);
        Assert.assertFalse(orderPage.waitStatisticsTextMatches(
                        "(?s).*Theo dịch vụ\\s+\\d+ dịch vụ.*").isBlank());
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-date-boundary",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_068)
    /** Lọc thống kê trong đúng một ngày bằng hai đầu mốc giống nhau. */
    public void supportsSameDayRange() {
        orderPage.openStatistic("Bảo hành 5K");
        String date = LocalDate.now().minusDays(1).format(DATE);
        orderPage.setStatisticsCustomDateRange(date, date);
        Assert.assertEquals(orderPage.statisticsInputValues(),
                List.of(date, date));
        assertAllKpisNumeric();
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-date-validation",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_069)
    /** Nhập ngày bắt đầu sau ngày kết thúc và kiểm tra validation. */
    public void rejectsReversedDateRange() {
        orderPage.openStatistic("Bảo hành 5K");
        assertRawRangeRejected(
                LocalDate.now().minusDays(1).format(DATE),
                LocalDate.now().minusDays(6).format(DATE));
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-date-validation",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_070)
    /** Kiểm tra popup từ chối ngày sai định dạng và khoảng ngày để trống. */
    public void rejectsMalformedAndBlankDates() {
        orderPage.openStatistic("Bảo hành 5K");
        assertRawRangeRejected("32/13/2026", "abc");
        assertRawRangeRejected("", "");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-date-validation",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_071)
    /** Chọn ngày tương lai và kiểm tra UI xử lý rõ ràng khi không có dữ liệu. */
    public void handlesFutureDateRange() {
        orderPage.openStatistic("Bảo hành 5K");
        List<String> baseline = orderPage.statisticsInputValues();
        String from = LocalDate.now().plusDays(1).format(DATE);
        String to = LocalDate.now().plusDays(5).format(DATE);
        List<String> entered =
                orderPage.enterRawStatisticsDateRange(from, to);
        List<String> expected = List.of(from, to);

        if (entered.equals(expected)) {
            assertNumericBlock("Tổng đơn bảo hành");
            Assert.assertEquals(orderPage.statisticsChartCount(), 2);
        } else {
            Assert.assertTrue(entered.equals(baseline)
                            || !orderPage.statisticsDateValidationText()
                            .isBlank(),
                    "Khoảng tương lai bị đổi nhưng không revert/cảnh báo.");
        }
    }

    /** Nhập raw range không hợp lệ và xác nhận có validation. */
    private void assertRawRangeRejected(String from, String to) {
        List<String> baseline = orderPage.statisticsInputValues();
        List<String> entered =
                orderPage.enterRawStatisticsDateRange(from, to);
        Assert.assertTrue(entered.equals(baseline)
                        || !orderPage.statisticsDateValidationText().isBlank(),
                "Ngày không hợp lệ không bị revert và không có cảnh báo: "
                        + entered);
    }

    /** Kiểm tra toàn bộ KPI chính đều chứa dữ liệu số. */
    private void assertAllKpisNumeric() {
        for (String metric : List.of(
                "Tổng đơn bảo hành", "Tổng thu phí BH",
                "TB đơn/ngày", "TB phí BH/ngày")) {
            assertNumericBlock(metric);
        }
    }

    /** Kiểm tra block KPI theo nhãn chứa ít nhất một giá trị số. */
    private void assertNumericBlock(String label) {
        Assert.assertTrue(orderPage.statisticsBlockText(label)
                        .matches("(?s).*\\d.*"),
                "Khối " + label + " không trả số liệu.");
    }

    /** Tạo khoảng ngày quá khứ ổn định cho testcase lọc tùy chỉnh. */
    private List<String> customDateRange() {
        return List.of(
                LocalDate.now().minusDays(6).format(DATE),
                LocalDate.now().minusDays(1).format(DATE));
    }

    /** Tính hai đầu mốc Tuần/Tháng/Quý theo ngày chạy hiện tại. */
    private List<String> expectedRange(String period) {
        LocalDate today = LocalDate.now();
        LocalDate from = switch (period.replace(" này", "")) {
            case "Tuần" -> today.with(
                    TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case "Tháng" -> today.withDayOfMonth(1);
            case "Quý" -> today.withMonth(
                    ((today.getMonthValue() - 1) / 3) * 3 + 1)
                    .withDayOfMonth(1);
            default -> throw new IllegalArgumentException(period);
        };
        return List.of(from.format(DATE), today.format(DATE));
    }
}
