package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/** Kiểm tra popup Thống kê Bảo hành 5K. */
public class CustomerWorkerOrderWarrantyStatisticsTest
        extends CustomerWorkerOrderTestSupport {
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
            description = "CUSTOMER-WORKER-ORDER-041: Popup Bảo hành có đủ KPI, chart và dịch vụ")
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
            description = "CUSTOMER-WORKER-ORDER-042: Lọc đúng Tuần/Tháng/Quý")
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
            description = "CUSTOMER-WORKER-ORDER-043: Hover chart số lượng trả tooltip thật")
    public void orderCountChartReturnsRealTooltip() {
        orderPage.openStatistic("Bảo hành 5K");
        String tooltip = orderPage.hoverStatisticsBar("order_count");
        Assert.assertFalse(tooltip.isBlank());
        Assert.assertTrue(tooltip.matches("(?s).*\\d.*"));
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-scroll", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-044: Cuộn tới dịch vụ và mở lại popup")
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
            description = "CUSTOMER-WORKER-ORDER-045: Hover chart doanh thu trả tooltip thật")
    public void revenueChartReturnsRealTooltip() {
        orderPage.openStatistic("Bảo hành 5K");
        String tooltip = orderPage.hoverStatisticsBar("revenue");
        Assert.assertFalse(tooltip.isBlank());
        Assert.assertTrue(tooltip.matches("(?s).*\\d.*"));
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-custom-date",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-051: Lọc khoảng ngày tùy chỉnh")
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
            description = "CUSTOMER-WORKER-ORDER-056: Lọc được trong cùng một ngày")
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
            description = "CUSTOMER-WORKER-ORDER-058: Từ chối khoảng ngày đảo ngược")
    public void rejectsReversedDateRange() {
        orderPage.openStatistic("Bảo hành 5K");
        assertRawRangeRejected(
                LocalDate.now().minusDays(1).format(DATE),
                LocalDate.now().minusDays(6).format(DATE));
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-date-validation",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-060: Từ chối ngày sai định dạng và để trống")
    public void rejectsMalformedAndBlankDates() {
        orderPage.openStatistic("Bảo hành 5K");
        assertRawRangeRejected("32/13/2026", "abc");
        assertRawRangeRejected("", "");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "warranty-statistics", "statistics-date-validation",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-062: Xử lý rõ ràng khoảng ngày tương lai")
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

    private void assertRawRangeRejected(String from, String to) {
        List<String> baseline = orderPage.statisticsInputValues();
        List<String> entered =
                orderPage.enterRawStatisticsDateRange(from, to);
        Assert.assertTrue(entered.equals(baseline)
                        || !orderPage.statisticsDateValidationText().isBlank(),
                "Ngày không hợp lệ không bị revert và không có cảnh báo: "
                        + entered);
    }

    private void assertAllKpisNumeric() {
        for (String metric : List.of(
                "Tổng đơn bảo hành", "Tổng thu phí BH",
                "TB đơn/ngày", "TB phí BH/ngày")) {
            assertNumericBlock(metric);
        }
    }

    private void assertNumericBlock(String label) {
        Assert.assertTrue(orderPage.statisticsBlockText(label)
                        .matches("(?s).*\\d.*"),
                "Khối " + label + " không trả số liệu.");
    }

    private List<String> customDateRange() {
        return List.of(
                LocalDate.now().minusDays(6).format(DATE),
                LocalDate.now().minusDays(1).format(DATE));
    }

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
