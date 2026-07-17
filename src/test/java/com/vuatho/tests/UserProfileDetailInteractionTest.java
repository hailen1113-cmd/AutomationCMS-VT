package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Kiểm tra màn hình chi tiết của một người dùng từ danh sách hồ sơ ERP.
 *
 * <p>Lớp xác nhận drawer/trang chi tiết mở được, có dữ liệu và từng tab nghiệp vụ
 * đều hiển thị nội dung sau khi người dùng chuyển tab.</p>
 */
public class UserProfileDetailInteractionTest extends UserProfileTestSupport {
    // Danh sách tab được kiểm tra theo đúng nhãn đang hiển thị trên giao diện.
    private static final List<String> USER_DETAIL_TABS = List.of(
            "Thong tin co ban",
            "Tho",
            "Khach",
            "Gioi thieu",
            "Giao dich",
            "Xu ly vi pham",
            "Tho yeu thich",
            "Voucher 1 Trieu");

    /** Cho phép chạy riêng lớp test từ IDE mà không cần TestNG XML. */
    public static void main(String[] args) {
        TestNgRunner.run(UserProfileDetailInteractionTest.class,
                "Bo test chi tiet nguoi dung ERP",
                "Kiem tra chi tiet va tab nguoi dung");
    }

    /**
     * Mở dòng người dùng đầu tiên và xác nhận khu vực chi tiết có nội dung.
     * Test được bỏ qua khi môi trường không có dữ liệu người dùng để tránh báo lỗi giả.
     */
    @Test(priority = 1,
            groups = {"user-profile", "user-detail"},
            description = "USER-PROFILE-DETAIL-001: Mo duoc chi tiet nguoi dung dau tien")
    public void firstUserDetailCanBeOpened() {
        // Luôn mở lại danh sách để test có thể chạy độc lập.
        userProfilePage.openFromMenu();
        if (!userProfilePage.hasUserRows()) {
            throw new SkipException("Khong co dong nguoi dung nao de mo chi tiet.");
        }

        // Lưu nội dung dòng để thông báo lỗi chỉ rõ bản ghi không mở được.
        String firstRowText = userProfilePage.firstUserRowText();
        userProfilePage.openFirstUserDetail();
        Assert.assertTrue(userProfilePage.userDetailIsOpen(),
                "Chi tiet nguoi dung khong mo duoc. Dong: " + firstRowText);
        Assert.assertFalse(userProfilePage.detailText().isBlank(),
                "Chi tiet nguoi dung da mo nhung khong co noi dung hien thi.");
    }

    /**
     * Mở lần lượt mọi tab chi tiết, xác nhận nội dung và cuộn hết tab để kích hoạt
     * cả phần dữ liệu được tải lười ở cuối trang.
     */
    @Test(priority = 2,
            groups = {"user-profile", "user-detail", "user-detail-tabs"},
            description = "USER-PROFILE-DETAIL-TAB-001: Bam lan luot cac tab chi tiet nguoi dung va doi load xong")
    public void userDetailTabsCanBeOpenedOneByOne() {
        userProfilePage.openFromMenu();
        if (!userProfilePage.hasUserRows()) {
            throw new SkipException("Khong co dong nguoi dung nao de mo chi tiet.");
        }

        String firstRowText = userProfilePage.firstUserRowText();
        userProfilePage.openFirstUserDetail();
        Assert.assertTrue(userProfilePage.userDetailIsOpen(),
                "Chi tiet nguoi dung khong mo duoc. Dong: " + firstRowText);

        // Mỗi vòng lặp kiểm tra một tab như một người dùng thực đang duyệt hồ sơ.
        for (String tabLabel : USER_DETAIL_TABS) {
            userProfilePage.openUserDetailTab(tabLabel);
            Assert.assertTrue(userProfilePage.userDetailHasVisibleContent(),
                    "Tab chi tiet nguoi dung chua co noi dung hien thi: " + tabLabel);
            userProfilePage.scrollCurrentUserDetailTabToBottomThenTop(tabLabel);
        }
    }
}
