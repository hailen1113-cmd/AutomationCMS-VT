package com.vuatho.testdata;

import com.vuatho.navigation.MenuTarget;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.vuatho.navigation.MenuTarget.childOf;

/**
 * Cung cấp catalog và DataProvider cho các màn hình thuộc nhóm Đối tác - Thợ.
 */
public final class PartnerWorkerTestData {
    public static final String PARENT_MENU = "Đối Tác - Thợ";
    public static final MenuTarget WORKER_PROFILE = childOf(PARENT_MENU, "Quản Lí Hồ Sơ Thợ");
    public static final MenuTarget VIOLATION_WORKER = childOf(PARENT_MENU, "Quản Lí Thợ Vi Phạm");
    public static final MenuTarget TRAINING = childOf(PARENT_MENU, "Quản Lí Bài Training");
    public static final MenuTarget PROFILE_POST = childOf(PARENT_MENU, "Quản Lí Bài Đăng");
    public static final MenuTarget STOP_REQUEST = childOf(PARENT_MENU, "Yêu Cầu Ngưng Hợp Tác");

    private static final List<PartnerWorkerCase> CASES = List.of(
            caseOf(
                    "PARTNER-WORKER-PROFILE-001",
                    "Mở danh sách hồ sơ thợ",
                    WORKER_PROFILE,
                    "Tìm kiếm thợ"),
            caseOf(
                    "PARTNER-WORKER-VIOLATION-001",
                    "Mở danh sách thợ vi phạm",
                    VIOLATION_WORKER,
                    "Tìm kiếm thợ"),
            caseOf(
                    "PARTNER-WORKER-TRAINING-001",
                    "Mở danh sách bài training",
                    TRAINING,
                    "Tìm kiếm bài training"),
            caseOf(
                    "PARTNER-WORKER-POST-001",
                    "Mở danh sách bài đăng của thợ",
                    PROFILE_POST,
                    ""),
            caseOf(
                    "PARTNER-WORKER-STOP-001",
                    "Mở danh sách yêu cầu ngưng hợp tác",
                    STOP_REQUEST,
                    "Tìm kiếm thợ theo tên"));

    /**
     * Khởi tạo PartnerWorkerTestData với các phụ thuộc cần thiết.
     */
    private PartnerWorkerTestData() {
    }

    /**
     * Thực hiện xử lý cases trong luồng kiểm thử.
     * @return kết quả cases sau khi xử lý
     */
    public static List<PartnerWorkerCase> cases() {
        return CASES;
    }

    /**
     * Tìm by id trong luồng kiểm thử.
     * @param id giá trị id được truyền vào
     * @return kết quả find by id sau khi xử lý
     */
    public static Optional<PartnerWorkerCase> findById(String id) {
        return CASES.stream()
                .filter(testCase -> testCase.id().equalsIgnoreCase(id))
                .findFirst();
    }

    /**
     * Thực hiện xử lý menu pages trong luồng kiểm thử.
     * @return kết quả menu pages sau khi xử lý
     */
    public static List<MenuTarget> menuPages() {
        return CASES.stream()
                .map(PartnerWorkerCase::page)
                .toList();
    }

    /**
     * Thực hiện xử lý search filters trong luồng kiểm thử.
     * @return kết quả search filters sau khi xử lý
     */
    public static List<FilterTarget> searchFilters() {
        return CASES.stream()
                .filter(PartnerWorkerCase::hasSearchFilter)
                .map(testCase -> new FilterTarget(testCase.page(), testCase.searchPlaceholder()))
                .toList();
    }

    /**
     * Thực hiện xử lý data provider rows trong luồng kiểm thử.
     * @return kết quả data provider rows sau khi xử lý
     */
    public static Object[][] dataProviderRows() {
        String filter = System.getProperty("partner.worker.case.id", "").trim().toLowerCase(Locale.ROOT);
        return CASES.stream()
                .filter(testCase -> filter.isBlank()
                        || testCase.id().toLowerCase(Locale.ROOT).contains(filter)
                        || testCase.scenario().toLowerCase(Locale.ROOT).contains(filter)
                        || testCase.page().toString().toLowerCase(Locale.ROOT).contains(filter))
                .map(testCase -> new Object[]{testCase})
                .toArray(Object[][]::new);
    }

    private static PartnerWorkerCase caseOf(
            String id,
            String scenario,
            MenuTarget page,
            String searchPlaceholder) {
        return new PartnerWorkerCase(id, scenario, page, searchPlaceholder);
    }
}
