package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.WorkerPostManagementPage.PostCard;
import com.vuatho.pages.WorkerPostManagementPage.Status;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra cấu trúc, dữ liệu và nghiệp vụ hiển thị theo trạng thái bài đăng. */
public class WorkerPostManagementOverviewTest extends WorkerPostManagementTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerPostManagementOverviewTest.class,
                "Quản lý bài đăng thợ", "Tổng quan và dữ liệu bài đăng");
    }

    @Test(groups = {"worker-post-management", "read-only"},
            description = "WORKER-POST-MANAGEMENT-001: Trang có đủ bốn tab trạng thái và mặc định Chờ duyệt")
    public void pageHasFourRequiredStatusTabs() {
        Assert.assertTrue(workerPostManagementPage.isLoaded(), "Trang Quản lí bài đăng chưa tải xong.");
        List<String> normalizedTabs = workerPostManagementPage.tabLabels().stream()
                .map(TextNormalizer::normalize).toList();
        for (Status status : Status.values()) {
            Assert.assertTrue(normalizedTabs.contains(TextNormalizer.normalize(status.label())),
                    "Thiếu tab trạng thái: " + status.label());
        }
        Assert.assertEquals(workerPostManagementPage.selectedStatus().orElse(null), Status.PENDING,
                "Route pending nhưng tab Chờ duyệt không được chọn.");
        Assert.assertTrue(driver.getCurrentUrl().contains("tab=pending"),
                "URL không giữ trạng thái pending: " + driver.getCurrentUrl());
    }

    @Test(groups = {"worker-post-management", "read-only"},
            description = "WORKER-POST-MANAGEMENT-002: Card Chờ duyệt hiển thị đủ thông tin bài đăng")
    public void pendingCardsHaveCoreInformation() {
        List<PostCard> cards = workerPostManagementPage.visiblePostCards();
        Assert.assertFalse(cards.isEmpty(), "Tab Chờ duyệt không có card để kiểm tra.");
        Assert.assertTrue(workerPostManagementPage.totalPosts() >= cards.size(),
                "Tổng hiển thị nhỏ hơn số card đang có.");

        for (PostCard card : cards) {
            Assert.assertFalse(card.workerName().isBlank(), "Card thiếu tên thợ.");
            Assert.assertTrue(card.workerHref().matches(".*?/vuatho/worker\\?id=\\d+.*"),
                    "Link hồ sơ thợ không hợp lệ: " + card.workerHref());
            Assert.assertTrue(card.hasOccupation(), "Card thiếu Ngành nghề: " + card.workerName());
            Assert.assertTrue(card.hasPostedAt(), "Card thiếu Thời gian đăng: " + card.workerName());
        }
    }

    @Test(groups = {"worker-post-management", "read-only"},
            description = "WORKER-POST-MANAGEMENT-003: Mỗi bài Chờ duyệt có nút Duyệt bài và Từ chối")
    public void pendingCardsExposeModerationActions() {
        Assert.assertTrue(workerPostManagementPage.hasPendingActionsOnEveryCard(),
                "Có card Chờ duyệt thiếu nút Duyệt bài hoặc Từ chối.");
    }

    @Test(groups = {"worker-post-management", "read-only", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-004: Bài Đã duyệt hiển thị người và ngày duyệt")
    public void approvedCardsHaveReviewAuditInformation() {
        workerPostManagementPage.selectStatus(Status.APPROVED);
        List<PostCard> cards = workerPostManagementPage.visiblePostCards();
        Assert.assertFalse(cards.isEmpty(), "Tab Đã duyệt không có dữ liệu.");
        Assert.assertTrue(cards.stream().allMatch(PostCard::hasReviewer),
                "Có bài Đã duyệt thiếu Người duyệt.");
        Assert.assertTrue(cards.stream().allMatch(PostCard::hasReviewedAt),
                "Có bài Đã duyệt thiếu Ngày duyệt.");
        Assert.assertTrue(workerPostManagementPage.hasNoModerationActions(),
                "Bài đã duyệt vẫn còn action Duyệt/Từ chối.");
    }

    @Test(groups = {"worker-post-management", "read-only", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-005: Bài Từ chối hiển thị audit và lý do từ chối")
    public void rejectedCardsHaveAuditAndRejectReason() {
        workerPostManagementPage.selectStatus(Status.REJECTED);
        List<PostCard> cards = workerPostManagementPage.visiblePostCards();
        Assert.assertFalse(cards.isEmpty(), "Tab Từ chối không có dữ liệu.");
        Assert.assertTrue(cards.stream().allMatch(PostCard::hasReviewer),
                "Có bài Từ chối thiếu Người duyệt.");
        Assert.assertTrue(cards.stream().allMatch(PostCard::hasReviewedAt),
                "Có bài Từ chối thiếu Ngày duyệt.");
        Assert.assertTrue(cards.stream().anyMatch(PostCard::hasRejectReason),
                "Trang Từ chối hiện tại không có bài nào hiển thị Lý do từ chối.");
        Assert.assertTrue(workerPostManagementPage.hasNoModerationActions(),
                "Bài bị từ chối vẫn còn action Duyệt/Từ chối.");
    }

    @Test(groups = {"worker-post-management", "read-only", "data-interaction"},
            description = "WORKER-POST-MANAGEMENT-006: Bài Đã xóa là trạng thái chỉ đọc")
    public void deletedCardsAreReadOnly() {
        workerPostManagementPage.selectStatus(Status.DELETED);
        Assert.assertFalse(workerPostManagementPage.visiblePostCards().isEmpty(),
                "Tab Đã xóa không có dữ liệu.");
        Assert.assertTrue(workerPostManagementPage.hasNoModerationActions(),
                "Bài đã xóa vẫn còn action Duyệt/Từ chối.");
    }

    @Test(groups = {"worker-post-management", "read-only"},
            description = "WORKER-POST-MANAGEMENT-013: Card có timestamp và tổng media hợp lệ")
    public void cardsHaveValidTimestampAndMediaSummary() {
        Assert.assertTrue(workerPostManagementPage.cardsHaveValidTimestampAndMediaSummary(),
                "Có card sai định dạng thời gian hoặc bộ đếm ảnh/video.");
    }
}
