package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.CustomerWorkerOrderPage;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Kiểm tra tìm kiếm, toàn bộ nhóm filter và Reset. */
public class CustomerWorkerOrderSearchFilterTest extends CustomerWorkerOrderTestSupport {
    public static void main(String[] args) {
        String group = System.getProperty("customer.order.group", "").trim();
        if (group.isBlank()) {
            TestNgRunner.run(CustomerWorkerOrderSearchFilterTest.class,
                    "Đơn Khách - Thợ", "Tìm kiếm và bộ lọc");
        } else {
            TestNgRunner.runGroup(
                    "Đơn Khách - Thợ", "Filter " + group, group,
                    CustomerWorkerOrderSearchFilterTest.class);
        }
    }

    @Test(groups = {"customer-worker-order", "search", "search-reset", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-005: Tìm mã đơn trả đúng đơn")
    public void searchByOrderIdReturnsExactOrder() {
        String id = orderPage.rows().get(0).id();
        orderPage.search(id);
        Assert.assertFalse(orderPage.rows().isEmpty());
        Assert.assertTrue(orderPage.rows().stream()
                .allMatch(row -> row.id().contains(id)));
    }

    @Test(groups = {"customer-worker-order", "search", "reset", "search-reset", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-006: Từ khóa không tồn tại và Reset phục hồi dữ liệu")
    public void noResultAndResetRestoreOrders() {
        int total = orderPage.totalDisplayed();
        orderPage.search("999999999999999");
        Assert.assertTrue(orderPage.rows().isEmpty(),
                "Mã không tồn tại vẫn trả đơn.");
        orderPage.reset();
        Assert.assertTrue(orderPage.searchValue().isBlank());
        Assert.assertFalse(orderPage.rows().isEmpty());
        Assert.assertEquals(orderPage.totalDisplayed(), total);
    }

    @DataProvider(name = "orderStatuses")
    public Object[][] orderStatuses() {
        return filterValues(CustomerWorkerOrderPage.ORDER_STATUSES).stream()
                .map(value -> new Object[]{value}).toArray(Object[][]::new);
    }

    @Test(dataProvider = "orderStatuses",
            groups = {"customer-worker-order", "filter", "order-status-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-007: Áp từng trạng thái đơn và kiểm tra dữ liệu")
    public void eachOrderStatusReturnsMatchingRows(String status) {
        orderPage.selectOrderStatus(status);
        Assert.assertTrue(orderPage.rows().isEmpty()
                        ? orderPage.totalDisplayed() == 0
                        : orderPage.rows().stream()
                        .allMatch(row -> row.status().equals(status)),
                "Filter trạng thái đơn trả dòng không khớp " + status);
    }

    @DataProvider(name = "agreementStatuses")
    public Object[][] agreementStatuses() {
        return filterValues(CustomerWorkerOrderPage.AGREEMENT_STATUSES).stream()
                .map(value -> new Object[]{value}).toArray(Object[][]::new);
    }

    @Test(dataProvider = "agreementStatuses",
            groups = {"customer-worker-order", "filter", "agreement-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-008: Áp từng trạng thái thỏa thuận và kiểm tra dữ liệu")
    public void eachAgreementStatusReturnsMatchingRows(String agreement) {
        orderPage.selectAgreementStatus(agreement);
                Assert.assertTrue(orderPage.rows().isEmpty()
                        ? orderPage.totalDisplayed() == 0
                        : orderPage.rows().stream().allMatch(row ->
                        orderPage.rowMatchesStatusGroup(
                                row, "Thỏa thuận giá", agreement)),
                "Filter thỏa thuận trả dòng không khớp " + agreement
                        + ". Status không khớp đầu tiên: "
                        + TextNormalizer.normalize(
                        orderPage.rows().stream()
                                .filter(row -> !orderPage.rowMatchesStatusGroup(
                                        row, "Thỏa thuận giá", agreement))
                                .map(CustomerWorkerOrderPage.OrderRow::statusDetails)
                                .findFirst().orElse("")));
    }

    @DataProvider(name = "directFilters")
    public Object[][] directFilters() {
        Object[][] values = new Object[][]{
                {"Thợ bảo hành", "Không có"},
                {"Thợ bảo hành", "Còn hạn"},
                {"Thợ bảo hành", "Hết hạn"},
                {"Thợ bảo hành", "Huỷ bảo hành"},
                {"Bảo hành đơn", "Có"},
                {"Bảo hành đơn", "Không"},
                {"Xuất hoá đơn", "Có yêu cầu"},
                {"Xuất hoá đơn", "Không"},
                {"Ghi nhận thông tin HĐ", "Đã ghi nhận"},
                {"Ghi nhận thông tin HĐ", "Chưa ghi nhận"}
        };
        String requested = System.getProperty(
                "customer.order.filter.value", "").trim()
                .replaceAll("^[\"']|[\"']$", "");
        if (requested.isBlank()) return values;
        return java.util.Arrays.stream(values)
                .filter(row -> TextNormalizer.normalize(row[1].toString())
                        .equals(TextNormalizer.normalize(requested))
                        || TextNormalizer.normalize(row[0] + " > " + row[1])
                        .equals(TextNormalizer.normalize(requested)))
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "directFilters",
            groups = {"customer-worker-order", "filter", "direct-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-009: Các lựa chọn bảo hành và hóa đơn trả dữ liệu")
    public void warrantyAndInvoiceFiltersReturnData(String group, String value) {
        int defaultTotal = orderPage.totalDisplayed();
        orderPage.selectDirectFilter(group, value);
        Assert.assertTrue(orderPage.totalDisplayed() <= defaultTotal,
                "Filter " + group + " làm tổng dữ liệu tăng.");
        if ("Ghi nhận thông tin HĐ".equals(group)) {
            return;
        }
        Assert.assertTrue(orderPage.rows().isEmpty()
                        ? orderPage.totalDisplayed() == 0
                        : orderPage.rows().stream().allMatch(row ->
                        orderPage.rowMatchesStatusGroup(row, group, value)),
                "Filter " + group + " trả dòng không khớp " + value
                        + ". Status không khớp đầu tiên: "
                        + TextNormalizer.normalize(orderPage.rows().stream()
                        .filter(row -> !orderPage.rowMatchesStatusGroup(
                                row, group, value))
                        .map(CustomerWorkerOrderPage.OrderRow::statusDetails)
                        .findFirst().orElse("")));
    }

    @Test(groups = {"customer-worker-order", "filter", "filter-inventory", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-010: Popover hiển thị đủ nhóm filter và lịch")
    public void filterPanelContainsAllControls() {
        String text = orderPage.filterText();
        for (String label : List.of(
                "Dịch vụ", "Trạng thái đơn dịch vụ", "Trạng thái thỏa thuận giá",
                "Thợ bảo hành", "Bảo hành đơn", "Xuất hoá đơn",
                "Ghi nhận thông tin HĐ", "THỜI GIAN YÊU CẦU", "Đặt lại")) {
            Assert.assertTrue(text.contains(label), "Filter thiếu " + label);
        }
    }

    @Test(groups = {"customer-worker-order", "filter", "reset", "filter-reset", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-011: Đặt lại trong Filter phục hồi tổng dữ liệu")
    public void resetInsideFilterRestoresDefaultOrders() {
        int total = orderPage.totalDisplayed();
        orderPage.selectDirectFilter("Bảo hành đơn", "Có");
        Assert.assertTrue(orderPage.totalDisplayed() <= total);
        orderPage.resetInsideFilter();
        Assert.assertEquals(orderPage.totalDisplayed(), total);
    }

    @Test(groups = {"customer-worker-order", "filter", "service-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-024: Lọc Dịch vụ trả đúng dịch vụ và cập nhật tổng")
    public void serviceFilterReturnsOnlySelectedService() {
        int total = orderPage.totalDisplayed();
        String service = "Sửa máy lạnh";
        orderPage.selectService(service);
        Assert.assertFalse(orderPage.rows().isEmpty(),
                "Dịch vụ phổ biến " + service + " không trả dữ liệu.");
        Assert.assertTrue(orderPage.totalDisplayed() < total,
                "Filter Dịch vụ không làm thay đổi tổng dữ liệu.");
        Assert.assertTrue(orderPage.rows().stream()
                        .allMatch(row -> TextNormalizer.normalize(row.info())
                                .contains(TextNormalizer.normalize(service))),
                "Filter Dịch vụ trả đơn thuộc dịch vụ khác.");
    }

    @Test(groups = {"customer-worker-order", "filter", "date-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-025: Lọc một ngày trả đúng thời gian yêu cầu")
    public void singleRequestDateReturnsOnlyThatDate() {
        LocalDate date = requestedDate(orderPage.rows().get(0));
        orderPage.selectRequestDateRange(date, date);
        Assert.assertFalse(orderPage.rows().isEmpty(),
                "Ngày lấy từ đơn đang hiển thị lại không trả dữ liệu.");
        Assert.assertTrue(orderPage.rows().stream()
                        .allMatch(row -> requestedDate(row).equals(date)),
                "Filter một ngày trả đơn ngoài ngày " + date);
    }

    @Test(groups = {"customer-worker-order", "filter", "date-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-026: Lọc khoảng ngày trả dữ liệu nằm trong biên")
    public void requestDateRangeReturnsRowsInsideBoundaries() {
        List<LocalDate> dates = orderPage.rows().stream()
                .map(CustomerWorkerOrderSearchFilterTest::requestedDate)
                .distinct().sorted().toList();
        LocalDate from = dates.get(0);
        LocalDate to = dates.get(dates.size() - 1);
        orderPage.selectRequestDateRange(from, to);
        Assert.assertFalse(orderPage.rows().isEmpty(),
                "Khoảng ngày lấy từ dữ liệu hiện tại không trả đơn.");
        Assert.assertTrue(orderPage.rows().stream().allMatch(row -> {
            LocalDate actual = requestedDate(row);
            return !actual.isBefore(from) && !actual.isAfter(to);
        }), "Filter khoảng ngày trả đơn nằm ngoài biên.");
    }

    @Test(groups = {"customer-worker-order", "filter", "combined-filter",
            "direct-combination", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-027: Kết hợp nhiều filter trả dòng thỏa tất cả điều kiện")
    public void multipleFiltersUseAndCondition() {
        orderPage.selectDirectFilter("Bảo hành đơn", "Có");
        orderPage.selectDirectFilter("Xuất hoá đơn", "Không");
        Assert.assertFalse(orderPage.rows().isEmpty(),
                "Tổ hợp filter phổ biến không trả dữ liệu.");
        Assert.assertTrue(orderPage.rows().stream().allMatch(row ->
                        orderPage.rowMatchesStatusGroup(row, "Bảo hành đơn", "Có")
                                && orderPage.rowMatchesStatusGroup(
                                row, "Xuất hoá đơn", "Không")),
                "Kết hợp filter không áp dụng điều kiện AND.");
    }

    @Test(groups = {"customer-worker-order", "filter", "search-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-028: Search trong kết quả đã lọc giữ nguyên điều kiện filter")
    public void searchInsideFilteredResultsKeepsFilter() {
        orderPage.selectDirectFilter("Bảo hành đơn", "Có");
        Assert.assertFalse(orderPage.rows().isEmpty());
        String id = orderPage.rows().get(0).id();
        orderPage.search(id);
        Assert.assertEquals(orderPage.rows().size(), 1);
        Assert.assertEquals(orderPage.rows().get(0).id(), id);
        Assert.assertTrue(orderPage.rowMatchesStatusGroup(
                orderPage.rows().get(0), "Bảo hành đơn", "Có"));
    }

    @Test(groups = {"customer-worker-order", "filter", "recorded-info-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-029: Hai trạng thái ghi nhận HĐ phân hoạch toàn bộ đơn")
    public void recordedInvoiceInformationPartitionsAllOrders() {
        int total = orderPage.totalDisplayed();
        orderPage.selectDirectFilter("Ghi nhận thông tin HĐ", "Đã ghi nhận");
        int recorded = orderPage.totalDisplayed();
        orderPage.resetInsideFilter();
        orderPage.selectDirectFilter("Ghi nhận thông tin HĐ", "Chưa ghi nhận");
        int unrecorded = orderPage.totalDisplayed();
        Assert.assertEquals(recorded + unrecorded, total,
                "Đã ghi nhận và Chưa ghi nhận không phân hoạch đúng tổng đơn.");
    }

    @DataProvider(name = "orderAgreementCombinations")
    public Object[][] orderAgreementCombinations() {
        return new Object[][]{
                {"Tìm kiếm thợ", "Chưa có"},
                {"Yêu cầu giá", "Chờ đợi"},
                {"Hoàn thành đơn", "Chấp nhận"},
                {"Hủy đơn", "Từ chối"}
        };
    }

    @Test(dataProvider = "orderAgreementCombinations",
            groups = {"customer-worker-order", "filter", "combined-filter",
                    "combined-filter-matrix", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-030: Ma trận trạng thái đơn và thỏa thuận áp dụng điều kiện AND")
    public void orderAndAgreementCombinationMatrix(
            String orderStatus, String agreementStatus) {
        orderPage.selectOrderStatus(orderStatus);
        orderPage.selectAgreementStatus(agreementStatus);

        List<CustomerWorkerOrderPage.OrderRow> rows = orderPage.rows();
        if (rows.isEmpty()) {
            Assert.assertEquals(orderPage.totalDisplayed(), 0,
                    "Tổ hợp không có dòng nhưng tổng hiển thị khác 0.");
            return;
        }
        String mismatch = rows.stream().filter(row ->
                        !row.status().equals(orderStatus)
                                || !orderPage.rowMatchesStatusGroup(
                                row, "Thỏa thuận giá", agreementStatus))
                .map(row -> "Dòng #" + row.id()
                        + " có trạng thái đơn=" + row.status()
                        + ", thỏa thuận=" + orderPage.statusGroupValue(
                        row, "Thỏa thuận giá"))
                .findFirst().orElse("");
        Assert.assertTrue(rows.stream().allMatch(row ->
                        row.status().equals(orderStatus)
                                && orderPage.rowMatchesStatusGroup(
                                row, "Thỏa thuận giá", agreementStatus)),
                "Tổ hợp trạng thái đơn và thỏa thuận không áp dụng điều kiện AND. "
                        + mismatch);
    }

    @Test(groups = {"customer-worker-order", "filter", "combined-filter",
            "service-date-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-031: Kết hợp Dịch vụ và ngày yêu cầu trả đúng giao")
    public void serviceAndRequestDateReturnTheirIntersection() {
        String service = "Sửa máy lạnh";
        orderPage.selectService(service);
        Assert.assertFalse(orderPage.rows().isEmpty(),
                "Filter dịch vụ nền không trả dữ liệu để kết hợp ngày.");
        LocalDate date = requestedDate(orderPage.rows().get(0));

        orderPage.selectRequestDateRange(date, date);

        Assert.assertFalse(orderPage.rows().isEmpty(),
                "Dịch vụ kết hợp ngày lấy từ chính kết quả lại không trả dữ liệu.");
        Assert.assertTrue(orderPage.rows().stream().allMatch(row ->
                        TextNormalizer.normalize(row.info())
                                .contains(TextNormalizer.normalize(service))
                                && requestedDate(row).equals(date)),
                "Dịch vụ kết hợp ngày trả dòng ngoài giao của hai điều kiện.");
    }

    @Test(groups = {"customer-worker-order", "filter", "combined-filter",
            "status-date-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-032: Kết hợp trạng thái đơn và ngày yêu cầu")
    public void orderStatusAndRequestDateReturnTheirIntersection() {
        CustomerWorkerOrderPage.OrderRow seed = orderPage.rows().get(0);
        String status = seed.status();
        LocalDate date = requestedDate(seed);

        orderPage.selectRequestDateRange(date, date);
        orderPage.selectOrderStatus(status);

        Assert.assertFalse(orderPage.rows().isEmpty(),
                "Trạng thái và ngày lấy từ cùng một đơn lại không trả dữ liệu.");
        Assert.assertTrue(orderPage.rows().stream().allMatch(row ->
                        row.status().equals(status)
                                && requestedDate(row).equals(date)),
                "Trạng thái kết hợp ngày không áp dụng điều kiện AND.");
    }

    @Test(groups = {"customer-worker-order", "filter", "combined-filter",
            "search-filter", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-033: Search kết hợp trạng thái đơn và thỏa thuận")
    public void searchWithOrderAndAgreementFiltersKeepsAllConditions() {
        CustomerWorkerOrderPage.OrderRow seed = orderPage.rows().get(0);
        String id = seed.id();
        String status = seed.status();
        String agreement = orderPage.statusGroupValue(
                seed, "Thỏa thuận giá");

        orderPage.selectOrderStatus(status);
        orderPage.selectAgreementStatus(agreement);
        orderPage.search(id);

        Assert.assertEquals(orderPage.rows().size(), 1,
                "Search kết hợp hai trạng thái không trả đúng một đơn.");
        CustomerWorkerOrderPage.OrderRow actual = orderPage.rows().get(0);
        Assert.assertEquals(actual.id(), id);
        Assert.assertEquals(actual.status(), status);
        Assert.assertTrue(orderPage.rowMatchesStatusGroup(
                actual, "Thỏa thuận giá", agreement));
    }

    @Test(groups = {"customer-worker-order", "filter", "combined-filter",
            "all-filter-types", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-034: Kết hợp Dịch vụ, trạng thái, thỏa thuận, bảo hành và ngày")
    public void allMajorFilterTypesReturnOnlyMatchingRows() {
        String service = "Sửa máy lạnh";
        orderPage.selectService(service);
        Assert.assertFalse(orderPage.rows().isEmpty());
        CustomerWorkerOrderPage.OrderRow seed = orderPage.rows().get(0);
        String status = seed.status();
        String agreement = orderPage.statusGroupValue(
                seed, "Thỏa thuận giá");
        String warranty = orderPage.statusGroupValue(
                seed, "Bảo hành đơn");
        LocalDate date = requestedDate(seed);

        orderPage.selectRequestDateRange(date, date);
        orderPage.selectOrderStatus(status);
        orderPage.selectAgreementStatus(agreement);
        orderPage.selectDirectFilter("Bảo hành đơn", warranty);

        Assert.assertFalse(orderPage.rows().isEmpty(),
                "Năm điều kiện lấy từ cùng một đơn lại không trả dữ liệu.");
        List<CustomerWorkerOrderPage.OrderRow> actualRows = orderPage.rows();
        String mismatch = actualRows.stream().filter(row ->
                        !(TextNormalizer.normalize(row.info())
                                .contains(TextNormalizer.normalize(service))
                                && row.status().equals(status)
                                && orderPage.rowMatchesStatusGroup(
                                row, "Thỏa thuận giá", agreement)
                                && orderPage.rowMatchesStatusGroup(
                                row, "Bảo hành đơn", warranty)
                                && requestedDate(row).equals(date)))
                .map(row -> "Dòng #" + row.id()
                        + " | service=" + TextNormalizer.normalize(row.info())
                        .contains(TextNormalizer.normalize(service))
                        + " | status=" + row.status().equals(status)
                        + " | agreement=" + orderPage.rowMatchesStatusGroup(
                        row, "Thỏa thuận giá", agreement)
                        + " | warranty=" + orderPage.rowMatchesStatusGroup(
                        row, "Bảo hành đơn", warranty)
                        + " | date=" + requestedDate(row).equals(date)
                        + " | data=" + TextNormalizer.normalize(row.rawText()))
                .findFirst().orElse("");
        Assert.assertTrue(actualRows.stream().allMatch(row ->
                        TextNormalizer.normalize(row.info())
                                .contains(TextNormalizer.normalize(service))
                                && row.status().equals(status)
                                && orderPage.rowMatchesStatusGroup(
                                row, "Thỏa thuận giá", agreement)
                                && orderPage.rowMatchesStatusGroup(
                                row, "Bảo hành đơn", warranty)
                                && requestedDate(row).equals(date)),
                "Tổ hợp năm loại filter trả dòng không thỏa toàn bộ điều kiện. "
                        + mismatch);
    }

    @Test(groups = {"customer-worker-order", "filter", "combined-filter",
            "empty-result", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-035: Search không tồn tại sau nhiều filter trả rỗng ngay")
    public void impossibleSearchAfterFiltersReturnsZeroWithoutHanging() {
        orderPage.selectService("Sửa máy lạnh");
        CustomerWorkerOrderPage.OrderRow seed = orderPage.rows().get(0);
        orderPage.selectOrderStatus(seed.status());

        orderPage.search("999999999999999");

        Assert.assertTrue(orderPage.rows().isEmpty());
        Assert.assertEquals(orderPage.totalDisplayed(), 0,
                "Màn hình rỗng sau tổ hợp filter phải có tổng bằng 0.");
    }

    @Test(groups = {"customer-worker-order", "filter", "combined-filter",
            "filter-reset", "data-interaction"},
            description = "CUSTOMER-WORKER-ORDER-036: Reset xóa đồng thời nhiều điều kiện filter")
    public void resetClearsAllCombinedFilters() {
        int defaultTotal = orderPage.totalDisplayed();
        orderPage.selectService("Sửa máy lạnh");
        Assert.assertFalse(orderPage.rows().isEmpty());
        orderPage.selectOrderStatus(orderPage.rows().get(0).status());

        orderPage.resetInsideFilter();

        Assert.assertEquals(orderPage.totalDisplayed(), defaultTotal);
        Assert.assertFalse(orderPage.rows().isEmpty());
    }

    private static List<String> filterValues(List<String> values) {
        String requested = System.getProperty(
                "customer.order.filter.value", "").trim()
                .replaceAll("^[\"']|[\"']$", "");
        if (requested.isBlank()) return values;
        String expected = TextNormalizer.normalize(requested);
        return values.stream()
                .filter(value -> TextNormalizer.normalize(value).equals(expected))
                .toList();
    }

    private static LocalDate requestedDate(
            CustomerWorkerOrderPage.OrderRow row) {
        return LocalDate.parse(row.requestedAt(),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }
}
