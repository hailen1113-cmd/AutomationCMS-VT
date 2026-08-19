package com.vuatho.tests.transaction.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionOrderTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/** Kiểm tra validation trước khi gửi hành động trong drawer Đơn dịch vụ. */
public class TransactionOrderSubmissionTest extends TransactionOrderTestSupport {
    private static final byte[] ONE_PIXEL_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");

    public static void main(String[] args) {
        TestNgRunner.run(TransactionOrderSubmissionTest.class,
                "Lịch sử giao dịch", "Đơn dịch vụ - Gửi xử lý");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_091)
    public void pendingWarrantyPaymentRequiresBillThenEnablesConfirmation() {
        openOrderSubtype(subtype(37));
        var result = advancedPage().uploadPendingWarrantyPaymentBill(createValidBill());
        Assert.assertEquals(result.source().status(), "Đang chờ");
        Assert.assertTrue(result.openedUrl().contains("tab=order&type=37"), result.openedUrl());
        Assert.assertEquals(result.accept(), "image/*");
        Assert.assertTrue(result.disabledBeforeUpload(),
                "Chưa upload bill nhưng nút Xác nhận đã chuyển khoản vẫn bật");
        Assert.assertTrue(result.enabledAfterUpload(),
                "Upload bill hợp lệ nhưng nút Xác nhận đã chuyển khoản không bật");
        Assert.assertTrue(result.closed());
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_092)
    public void cancellingPendingWarrantyIncomeLeavesTransactionPending() {
        openOrderSubtype(subtype(36));
        var result = advancedPage().cancelFirstPendingOrderDetail();
        Assert.assertEquals(result.source().status(), "Đang chờ");
        Assert.assertTrue(result.openedUrl().contains("tab=order&type=36"), result.openedUrl());
        Assert.assertTrue(result.closed(), "Bấm Hủy không đóng drawer");
        Assert.assertFalse(result.closedUrl().contains("id="), result.closedUrl());
        Assert.assertTrue(result.sourceStillPending(),
                "Bấm Hủy làm mất hoặc thay đổi giao dịch Đang chờ");
    }

    private Path createValidBill() {
        try {
            Path directory = Path.of("target", "transaction-order-fixtures");
            Files.createDirectories(directory);
            Path bill = directory.resolve("warranty-transfer-bill.png");
            Files.write(bill, ONE_PIXEL_PNG);
            return bill;
        } catch (IOException exception) {
            throw new AssertionError("Không tạo được ảnh bill kiểm thử", exception);
        }
    }

    private com.vuatho.pages.TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream().filter(value -> value.type() == type)
                .findFirst().orElseThrow();
    }
}
