package com.vuatho.tests.customerworkerorder;

import com.vuatho.testcases.CustomerWorkerOrderTestCases;
import com.vuatho.config.TestConfig;
import com.vuatho.core.TestNgRunner;
import com.vuatho.support.customerworkerorder.CustomerWorkerOrderTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipFile;

/**
 * Kiểm tra toàn bộ chức năng xuất file của Đơn Khách - Thợ.
 *
 * <p>Bao phủ menu xuất chi tiết, xuất tổng hợp theo ngày và xuất Excel trong
 * popup thống kê. Test chờ file tải hoàn tất rồi kiểm tra định dạng/nội dung
 * cơ bản. Tác động duy nhất là tạo file trong thư mục download, không cập nhật
 * dữ liệu đơn trên hệ thống.</p>
 */
public class CustomerWorkerOrderExportTest
        extends CustomerWorkerOrderTestSupport {

    /** Chạy toàn bộ file hoặc riêng group truyền qua {@code customer.order.group}. */
    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(CustomerWorkerOrderExportTest.class,
                    "Đơn Khách - Thợ", "Xuất Excel");
        } else {
            TestNgRunner.runGroup("Đơn Khách - Thợ",
                    "Xuất Excel - " + group,
                    group, CustomerWorkerOrderExportTest.class);
        }
    }

    /** Kiểm tra menu có đúng hai loại báo cáo và đúng mô tả nghiệp vụ. */
    @Test(groups = {"customer-worker-order", "export", "export-menu",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_016)
    public void excelMenuContainsExactlyTwoExports() {
        List<String> options = orderPage.excelExportMenuOptions();
        Assert.assertEquals(options.size(), 2);
        Assert.assertTrue(options.get(0).contains("Xuất chi tiết đơn hàng"));
        Assert.assertTrue(options.get(0)
                .contains("Danh sách từng đơn dịch vụ"));
        Assert.assertTrue(options.get(0).contains("cột xuất hoá đơn"));
        Assert.assertTrue(options.get(1).contains("Xuất tổng hợp theo ngày"));
        Assert.assertTrue(options.get(1)
                .contains("Tổng hợp theo ngày hoàn thành trong kỳ"));
    }

    /** Tải báo cáo chi tiết thật rồi kiểm tra file Excel có dữ liệu. */
    @Test(groups = {"customer-worker-order", "export", "export-detail",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_017)
    public void detailedOrderExportDownloadsRealFile() {
        assertExcelDownloaded(
                orderPage.exportExcel("Xuất chi tiết đơn hàng"),
                "Xuất chi tiết đơn hàng");
    }

    /** Tải báo cáo tổng hợp theo ngày thật rồi kiểm tra file Excel. */
    @Test(groups = {"customer-worker-order", "export", "export-daily",
            "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_018)
    public void dailySummaryExportDownloadsRealFile() {
        assertExcelDownloaded(
                orderPage.exportExcel("Xuất tổng hợp theo ngày"),
                "Xuất tổng hợp theo ngày");
    }

    /** Xuất tab Hoàn thành với khoảng ngày tùy chỉnh đang được áp dụng. */
    @Test(groups = {"customer-worker-order", "export",
            "statistics-export", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_019)
    public void completedStatusStatisticsExportUsesActiveRange() {
        assertStatusStatisticsExport(
                "ĐƠN HOÀN THÀNH", "hoàn thành");
    }

    /** Xuất tab Đơn hủy với khoảng ngày tùy chỉnh đang được áp dụng. */
    @Test(groups = {"customer-worker-order", "export",
            "statistics-export", "data-interaction"},
            description = CustomerWorkerOrderTestCases.CWO_020)
    public void cancelledStatusStatisticsExportUsesActiveRange() {
        assertStatusStatisticsExport("ĐƠN HỦY", "hủy");
    }

    /** Thiết lập tab/ngày, tải file thống kê và đối chiếu dấu vết nội dung. */
    private void assertStatusStatisticsExport(
            String tab, String statusKeyword) {
        orderPage.openStatistic("Trạng thái đơn");
        orderPage.clickStatisticsButton(tab);
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String from = LocalDate.now().minusDays(6).format(formatter);
        String to = LocalDate.now().minusDays(1).format(formatter);
        orderPage.setStatisticsCustomDateRange(from, to);

        String downloaded = orderPage.exportCurrentStatisticsExcel();
        Path file = assertExcelDownloaded(downloaded,
                "Xuất thống kê " + tab);
        String workbook = workbookText(file).toLowerCase(Locale.ROOT);
        Assert.assertTrue(workbook.contains(statusKeyword),
                "File thống kê không thể hiện đúng tab " + tab);
        Assert.assertTrue(workbook.contains(from) || workbook.contains(to),
                "File thống kê không thể hiện khoảng ngày đang áp dụng.");
    }

    /** Xác nhận export tạo file mới, tồn tại đúng định dạng và không rỗng. */
    private Path assertExcelDownloaded(
            String downloaded, String action) {
        Assert.assertFalse(downloaded.isBlank(),
                action + " không tạo file tải xuống.");
        String fileName = downloaded.substring(
                0, downloaded.indexOf('|'));
        Assert.assertTrue(fileName.matches("(?i).+\\.(xlsx|xls|csv)$"),
                action + " trả sai định dạng file: " + fileName);
        Path file = Path.of(TestConfig.downloadDirectory())
                .toAbsolutePath().normalize().resolve(fileName);
        try {
            Assert.assertTrue(Files.size(file) > 0,
                    action + " tạo file rỗng.");
        } catch (IOException exception) {
            throw new AssertionError(
                    "Không đọc được file " + file, exception);
        }
        return file;
    }

    /** Đọc text CSV/XLSX để testcase có thể kiểm tra nội dung báo cáo. */
    private String workbookText(Path file) {
        if (file.getFileName().toString().toLowerCase(Locale.ROOT)
                .endsWith(".csv")) {
            try {
                return Files.readString(file);
            } catch (IOException exception) {
                throw new AssertionError(
                        "Không đọc được CSV " + file, exception);
            }
        }
        try (ZipFile zip = new ZipFile(file.toFile())) {
            StringBuilder text = new StringBuilder();
            zip.stream().filter(entry -> entry.getName().matches(
                            "xl/(sharedStrings|workbook)\\.xml"))
                    .forEach(entry -> {
                        try (var input = zip.getInputStream(entry)) {
                            text.append(new String(
                                    input.readAllBytes(),
                                    StandardCharsets.UTF_8));
                        } catch (IOException exception) {
                            throw new IllegalStateException(exception);
                        }
                    });
            Assert.assertFalse(text.isEmpty(),
                    "Workbook không có metadata hoặc shared strings.");
            return text.toString();
        } catch (IOException exception) {
            throw new AssertionError(
                    "File Excel không mở được: " + file, exception);
        }
    }
}
