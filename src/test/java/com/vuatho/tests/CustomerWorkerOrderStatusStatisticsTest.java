package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/** Kiểm tra popup Thống kê trạng thái đơn dịch vụ. */
public class CustomerWorkerOrderStatusStatisticsTest
        extends CustomerWorkerOrderTestSupport {
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(CustomerWorkerOrderStatusStatisticsTest.class,
                    "Đơn Khách - Thợ", "Thống kê trạng thái đơn");
        } else {
            TestNgRunner.runGroup("Đơn Khách - Thợ",
                    "Thống kê trạng thái - " + group,
                    group, CustomerWorkerOrderStatusStatisticsTest.class);
        }
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-037: Popup Trạng thái có đủ cấu trúc và dữ liệu")
    public void popupReturnsCompleteStructureAndData() {
        orderPage.openStatistic("Trạng thái đơn");
        orderPage.clickStatisticsButton("ĐƠN HOÀN THÀNH");
        String text = orderPage.statisticsText();

        for (String expected : List.of(
                "Thống kê trạng thái đơn dịch vụ",
                "ĐƠN HOÀN THÀNH", "ĐƠN HỦY", "Tuần", "Tháng",
                "Xuất Excel", "Tổng số đơn hoàn thành",
                "Tổng phí kết nối", "Tổng giá trị đơn dịch vụ",
                "Danh sách dịch vụ đã được yêu cầu")) {
            Assert.assertTrue(text.contains(expected),
                    "Popup Trạng thái thiếu " + expected);
        }
        Assert.assertEquals(orderPage.statisticsInputValues().size(), 1);
        Assert.assertEquals(orderPage.statisticsChartCount(), 0,
                "Popup Trạng thái không được nhận nhầm thành popup chart.");
        assertNumericBlock("Tổng số đơn hoàn thành");
        assertNumericBlock("Tổng phí kết nối");
        assertNumericBlock("Tổng giá trị đơn dịch vụ");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-period", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-039: Tab Hoàn thành lọc đúng Tuần và Tháng")
    public void completedStatusSupportsEveryPeriod() {
        orderPage.openStatistic("Trạng thái đơn");
        verifyEveryPeriod("ĐƠN HOÀN THÀNH", "hoàn thành");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-period", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-046: Tab Đơn hủy lọc đúng Tuần và Tháng")
    public void cancelledStatusSupportsEveryPeriod() {
        orderPage.openStatistic("Trạng thái đơn");
        verifyEveryPeriod("ĐƠN HỦY", "hủy");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-scroll", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-040: Danh sách dịch vụ cuộn được và popup mở lại sạch")
    public void serviceListCanBeObservedAndDialogReopened() {
        orderPage.openStatistic("Trạng thái đơn");
        orderPage.clickStatisticsButton("ĐƠN HOÀN THÀNH");
        orderPage.scrollStatisticsTo("Danh sách dịch vụ đã được yêu cầu");
        Assert.assertTrue(orderPage.statisticsText()
                        .matches("(?s).*\\d+\\.\\s+.+Tổng số đơn:.*"),
                "Danh sách không trả dịch vụ và tổng số đơn.");

        orderPage.closeStatistics();
        orderPage.openStatistic("Trạng thái đơn");
        Assert.assertTrue(orderPage.statisticsText()
                .contains("Thống kê trạng thái đơn dịch vụ"));
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-custom-date",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-049: Tab Hoàn thành lọc khoảng ngày tùy chỉnh")
    public void completedStatusSupportsCustomDateRange() {
        orderPage.openStatistic("Trạng thái đơn");
        verifyCustomDateRange("ĐƠN HOÀN THÀNH", "hoàn thành");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-custom-date",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-050: Tab Đơn hủy lọc khoảng ngày tùy chỉnh")
    public void cancelledStatusSupportsCustomDateRange() {
        orderPage.openStatistic("Trạng thái đơn");
        verifyCustomDateRange("ĐƠN HỦY", "hủy");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-calendar-date",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-052: Icon calendar mở và chọn được khoảng ngày")
    public void calendarIconSelectsDateRange() {
        orderPage.openStatistic("Trạng thái đơn");
        orderPage.clickStatisticsButton("ĐƠN HOÀN THÀNH");
        LocalDate to = LocalDate.now().minusDays(1);
        LocalDate from = to.minusDays(3);
        orderPage.selectStatusStatisticsDateRangeFromCalendar(from, to);
        List<String> range = List.of(from.format(DATE), to.format(DATE));

        Assert.assertEquals(orderPage.statisticsInputValues(),
                List.of(range.get(0) + " - " + range.get(1)));
        assertAppliedRange(range);
        assertNumericBlock("Tổng số đơn");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-custom-date",
            "statistics-tabs", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-054: Đổi tab cập nhật nội dung và giữ khoảng ngày")
    public void customDateRangeIsPreservedWhenStatusTabChanges() {
        orderPage.openStatistic("Trạng thái đơn");
        orderPage.clickStatisticsButton("ĐƠN HOÀN THÀNH");
        List<String> range = customDateRange();
        String expected = range.get(0) + " - " + range.get(1);
        orderPage.setStatisticsCustomDateRange(range.get(0), range.get(1));
        String completed = orderPage.statisticsText();

        orderPage.clickStatisticsButton("ĐƠN HỦY");
        String cancelled = orderPage.statisticsText();
        Assert.assertNotEquals(cancelled, completed,
                "Đổi tab nhưng nội dung báo cáo không đổi.");
        Assert.assertTrue(cancelled.toLowerCase().contains("hủy"),
                "Tab Đơn hủy không trả đúng nhãn dữ liệu.");
        Assert.assertEquals(orderPage.statisticsInputValues(),
                List.of(expected),
                "Đổi tab làm mất khoảng ngày tùy chỉnh.");
        assertAppliedRange(range);

        orderPage.clickStatisticsButton("ĐƠN HOÀN THÀNH");
        Assert.assertTrue(orderPage.statisticsButtonSelected(
                "ĐƠN HOÀN THÀNH"));
        Assert.assertEquals(orderPage.statisticsInputValues(),
                List.of(expected));
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-date-boundary",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-055: Hai tab lọc được trong cùng một ngày")
    public void bothTabsSupportSameDayRange() {
        orderPage.openStatistic("Trạng thái đơn");
        String date = LocalDate.now().minusDays(1).format(DATE);
        for (String tab : List.of("ĐƠN HOÀN THÀNH", "ĐƠN HỦY")) {
            orderPage.clickStatisticsButton(tab);
            orderPage.setStatisticsCustomDateRange(date, date);
            Assert.assertEquals(orderPage.statisticsInputValues(),
                    List.of(date + " - " + date));
            assertAppliedRange(List.of(date, date));
            assertNumericBlock("Tổng số đơn");
        }
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-date-validation",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-057: Từ chối khoảng ngày đảo ngược")
    public void rejectsReversedDateRange() {
        orderPage.openStatistic("Trạng thái đơn");
        assertRawRangeRejected(
                LocalDate.now().minusDays(1).format(DATE),
                LocalDate.now().minusDays(6).format(DATE));
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-date-validation",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-059: Từ chối ngày sai định dạng và để trống")
    public void rejectsMalformedAndBlankDates() {
        orderPage.openStatistic("Trạng thái đơn");
        assertRawRangeRejected("32/13/2026", "abc");
        assertRawRangeRejected("", "");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-date-validation",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-061: Xử lý rõ ràng khoảng ngày tương lai")
    public void handlesFutureDateRange() {
        orderPage.openStatistic("Trạng thái đơn");
        List<String> baseline = orderPage.statisticsInputValues();
        String from = LocalDate.now().plusDays(1).format(DATE);
        String to = LocalDate.now().plusDays(5).format(DATE);
        List<String> entered =
                orderPage.enterRawStatisticsDateRange(from, to);
        List<String> expected = List.of(from + " - " + to);

        if (entered.equals(expected)) {
            assertAppliedRange(List.of(from, to));
            assertNumericBlock("Tổng số đơn");
        } else {
            Assert.assertTrue(entered.equals(baseline)
                            || !orderPage.statisticsDateValidationText()
                            .isBlank(),
                    "Khoảng tương lai bị đổi nhưng không revert/cảnh báo.");
        }
    }

    private void verifyEveryPeriod(
            String tab, String statusKeyword) {
        orderPage.clickStatisticsButton(tab);
        for (String period : List.of("Tuần", "Tháng")) {
            orderPage.clickStatisticsButton(period);
            Assert.assertTrue(orderPage.statisticsButtonSelected(tab));
            Assert.assertTrue(orderPage.statisticsButtonSelected(period));
            Assert.assertEquals(orderPage.statisticsInputValues(),
                    List.of(expectedRange(period)));
            String report = orderPage.waitStatisticsTextMatches(
                    "(?s).*Tổng số đơn.*");
            Assert.assertTrue(report.toLowerCase()
                    .contains(statusKeyword.toLowerCase()));
            assertNumericBlock("Tổng số đơn");
        }
    }

    private void verifyCustomDateRange(
            String tab, String statusKeyword) {
        orderPage.clickStatisticsButton(tab);
        List<String> range = customDateRange();
        orderPage.setStatisticsCustomDateRange(
                range.get(0), range.get(1));

        Assert.assertTrue(orderPage.statisticsButtonSelected(tab));
        Assert.assertEquals(orderPage.statisticsInputValues(),
                List.of(range.get(0) + " - " + range.get(1)));
        assertAppliedRange(range);
        String report = orderPage.waitStatisticsTextMatches(
                "(?s).*Tổng số đơn.*");
        Assert.assertTrue(report.toLowerCase()
                .contains(statusKeyword.toLowerCase()));
        assertNumericBlock("Tổng số đơn");
        if (tab.contains("HOÀN THÀNH")) {
            Assert.assertTrue(report.contains(
                    "Danh sách dịch vụ đã được yêu cầu"));
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

    private void assertNumericBlock(String label) {
        Assert.assertTrue(orderPage.statisticsBlockText(label)
                        .matches("(?s).*\\d.*"),
                "Khối " + label + " không trả số liệu.");
    }

    private void assertAppliedRange(List<String> range) {
        Assert.assertEquals(orderPage.statisticsAppliedRangeText(),
                "*Áp dụng từ " + range.get(0) + " đến " + range.get(1));
    }

    private List<String> customDateRange() {
        return List.of(
                LocalDate.now().minusDays(6).format(DATE),
                LocalDate.now().minusDays(1).format(DATE));
    }

    private String expectedRange(String period) {
        LocalDate today = LocalDate.now();
        LocalDate from = period.equals("Tuần")
                ? today.with(TemporalAdjusters.previousOrSame(
                DayOfWeek.SUNDAY))
                : today.withDayOfMonth(1);
        return from.format(DATE) + " - " + today.format(DATE);
    }
}
