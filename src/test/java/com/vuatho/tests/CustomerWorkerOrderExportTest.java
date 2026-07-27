package com.vuatho.tests;

import com.vuatho.config.TestConfig;
import com.vuatho.core.TestNgRunner;
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

/** Kiểm tra toàn bộ chức năng xuất file của Đơn Khách - Thợ. */
public class CustomerWorkerOrderExportTest
        extends CustomerWorkerOrderTestSupport {

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

    @Test(groups = {"customer-worker-order", "export", "export-menu",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-013: Menu Excel có đúng hai loại báo cáo")
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

    @Test(groups = {"customer-worker-order", "export", "export-detail",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-047: Xuất chi tiết tải file thật")
    public void detailedOrderExportDownloadsRealFile() {
        assertExcelDownloaded(
                orderPage.exportExcel("Xuất chi tiết đơn hàng"),
                "Xuất chi tiết đơn hàng");
    }

    @Test(groups = {"customer-worker-order", "export", "export-daily",
            "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-048: Xuất tổng hợp ngày tải file thật")
    public void dailySummaryExportDownloadsRealFile() {
        assertExcelDownloaded(
                orderPage.exportExcel("Xuất tổng hợp theo ngày"),
                "Xuất tổng hợp theo ngày");
    }

    @Test(groups = {"customer-worker-order", "export",
            "statistics-export", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-063: Xuất thống kê tab Hoàn thành theo ngày tùy chỉnh")
    public void completedStatusStatisticsExportUsesActiveRange() {
        assertStatusStatisticsExport(
                "ĐƠN HOÀN THÀNH", "hoàn thành");
    }

    @Test(groups = {"customer-worker-order", "export",
            "statistics-export", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-064: Xuất thống kê tab Đơn hủy theo ngày tùy chỉnh")
    public void cancelledStatusStatisticsExportUsesActiveRange() {
        assertStatusStatisticsExport("ĐƠN HỦY", "hủy");
    }

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
