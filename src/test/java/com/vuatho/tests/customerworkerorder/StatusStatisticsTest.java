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
 * Kiểm tra popup Thống kê trạng thái đơn dịch vụ.
 *
 * <p>Bao phủ hai tab Đơn hoàn thành/Đơn hủy, các mốc Tuần/Tháng, khoảng ngày
 * tự chọn, KPI, danh sách dịch vụ và nút xuất Excel. Các case chỉ thay đổi
 * điều kiện hiển thị thống kê, không thay đổi trạng thái đơn.</p>
 */
public class StatusStatisticsTest
        extends CustomerWorkerOrderTestSupport {
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Chạy toàn bộ file hoặc riêng group truyền qua {@code customer.order.group}. */
    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(StatusStatisticsTest.class,
                    "Đơn Khách - Thợ", "Thống kê trạng thái đơn");
        } else {
            TestNgRunner.runGroup("Đơn Khách - Thợ",
                    "Thống kê trạng thái - " + group,
                    group, StatusStatisticsTest.class);
        }
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_048)
    /** Kiểm tra popup có đủ tab, KPI, danh sách dịch vụ và action xuất file. */
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
            description = CustomerWorkerOrderTestCases.CWO_049)
    /** Kiểm tra tab Hoàn thành với từng mốc Tuần và Tháng. */
    public void completedStatusSupportsEveryPeriod() {
        orderPage.openStatistic("Trạng thái đơn");
        verifyEveryPeriod("ĐƠN HOÀN THÀNH", "hoàn thành");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-period", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_050)
    /** Kiểm tra tab Đơn hủy với từng mốc Tuần và Tháng. */
    public void cancelledStatusSupportsEveryPeriod() {
        orderPage.openStatistic("Trạng thái đơn");
        verifyEveryPeriod("ĐƠN HỦY", "hủy");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-scroll", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_051)
    /** Cuộn quan sát danh sách dịch vụ, đóng rồi mở lại popup ở trạng thái sạch. */
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
            description = CustomerWorkerOrderTestCases.CWO_052)
    /** Nhập khoảng ngày thủ công trên tab Hoàn thành và đối chiếu range áp dụng. */
    public void completedStatusSupportsCustomDateRange() {
        orderPage.openStatistic("Trạng thái đơn");
        verifyCustomDateRange("ĐƠN HOÀN THÀNH", "hoàn thành");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-custom-date",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_053)
    /** Nhập khoảng ngày thủ công trên tab Đơn hủy và đối chiếu range áp dụng. */
    public void cancelledStatusSupportsCustomDateRange() {
        orderPage.openStatistic("Trạng thái đơn");
        verifyCustomDateRange("ĐƠN HỦY", "hủy");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-calendar-date",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_054)
    /** Mở calendar bằng icon, chọn hai ngày và kiểm tra input/range cập nhật. */
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
            description = CustomerWorkerOrderTestCases.CWO_055)
    /** Đổi tab sau khi lọc ngày và xác nhận khoảng ngày vẫn được giữ. */
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
            description = CustomerWorkerOrderTestCases.CWO_056)
    /** Áp cùng ngày bắt đầu/kết thúc lần lượt trên cả hai tab trạng thái. */
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
            description = CustomerWorkerOrderTestCases.CWO_057)
    /** Nhập ngày bắt đầu sau ngày kết thúc và kiểm tra validation. */
    public void rejectsReversedDateRange() {
        orderPage.openStatistic("Trạng thái đơn");
        assertRawRangeRejected(
                LocalDate.now().minusDays(1).format(DATE),
                LocalDate.now().minusDays(6).format(DATE));
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-date-validation",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_058)
    /** Kiểm tra popup từ chối ngày sai định dạng và khoảng ngày để trống. */
    public void rejectsMalformedAndBlankDates() {
        orderPage.openStatistic("Trạng thái đơn");
        assertRawRangeRejected("32/13/2026", "abc");
        assertRawRangeRejected("", "");
    }

    @Test(groups = {"customer-worker-order", "statistics",
            "order-status-statistics", "statistics-date-validation",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_059)
    /** Chọn khoảng ngày tương lai và kiểm tra UI trả rỗng/thông báo rõ ràng. */
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

    /** Chuyển từng mốc thời gian và đối chiếu selected state cùng range kỳ vọng. */
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

    /** Dùng chung flow chọn tab, nhập khoảng tùy chỉnh và kiểm tra dữ liệu. */
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

    /** Kiểm tra block KPI theo nhãn chứa ít nhất một giá trị số. */
    private void assertNumericBlock(String label) {
        Assert.assertTrue(orderPage.statisticsBlockText(label)
                        .matches("(?s).*\\d.*"),
                "Khối " + label + " không trả số liệu.");
    }

    /** Đối chiếu hai input ngày với dòng mô tả range đang áp dụng. */
    private void assertAppliedRange(List<String> range) {
        Assert.assertEquals(orderPage.statisticsAppliedRangeText(),
                "*Áp dụng từ " + range.get(0) + " đến " + range.get(1));
    }

    /** Tạo khoảng ngày quá khứ ổn định để dùng cho testcase tùy chỉnh. */
    private List<String> customDateRange() {
        return List.of(
                LocalDate.now().minusDays(6).format(DATE),
                LocalDate.now().minusDays(1).format(DATE));
    }

    /** Tính range Tuần/Tháng kỳ vọng theo ngày chạy hiện tại. */
    private String expectedRange(String period) {
        LocalDate today = LocalDate.now();
        LocalDate from = period.equals("Tuần")
                ? today.with(TemporalAdjusters.previousOrSame(
                DayOfWeek.SUNDAY))
                : today.withDayOfMonth(1);
        return from.format(DATE) + " - " + today.format(DATE);
    }
}
