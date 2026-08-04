package com.vuatho.tests.uniform.inventory.stockreceipt;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.StockReceiptTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/** Testcase tạo phiếu Nhập kho tổng thật trên dữ liệu sandbox. */
public class StockReceiptSubmissionTest extends StockReceiptTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockReceiptSubmissionTest.class,
                "Kho Đồng phục", "Nhập kho tổng thật");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_091)
    public void createsRealReceiptAndNewStockLots() {
        var result = receiptPage.submitRealReceipt();
        Assert.assertFalse(result.productName().isBlank());
        Assert.assertFalse(result.lotCodes().isEmpty());
        Assert.assertEquals(result.submittedSummary().validLots(),
                result.submittedSummary().totalLots());
        Assert.assertTrue(result.receiptContainsAllLots(),
                "Phiếu vừa tạo không hiển thị đủ mã lô đã nhập.");
        Assert.assertTrue(result.stockContainsFirstLot(),
                "Tồn kho không hiển thị mã lô mới: " + result.lotCodes().get(0));
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_105)
    public void createsRealReceiptForMultipleProducts() {
        var result = receiptPage.submitMultipleProductsReal();
        Assert.assertEquals(result.productCount(), 2);
        Assert.assertFalse(result.lotCodes().isEmpty());
        Assert.assertEquals(result.submittedSummary().validLots(),
                result.submittedSummary().totalLots());
        Assert.assertTrue(result.receiptContainsAllLots(),
                "Phiếu không hiển thị đủ mã lô của hai sản phẩm.");
        Assert.assertEquals(result.stockMatchCount(), result.lotCodes().size(),
                "Không tìm thấy toàn bộ lô mới trong Tồn kho.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_106)
    public void createsRealReceiptWithZeroPrice() {
        var result = receiptPage.submitZeroPriceReceiptReal();
        Assert.assertFalse(result.lotCodes().isEmpty());
        Assert.assertEquals(result.submittedSummary().validLots(),
                result.submittedSummary().totalLots());
        Assert.assertEquals(result.submittedSummary().totalAmount(), 0L,
                "Phiếu giá 0 nhưng tổng tiền không bằng 0.");
        Assert.assertTrue(result.receiptContainsAllLots());
        Assert.assertEquals(result.stockMatchCount(), result.lotCodes().size());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation", "old-lot"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_107)
    public void addsStockToExistingLot() {
        var result = receiptPage.submitExistingLotAndVerifyIncrease();
        if (!result.candidateAvailable()) {
            throw new SkipException("Không có sản phẩm test nào hiện có lô cũ.");
        }
        Assert.assertFalse(result.lotCode().isBlank());
        Assert.assertTrue(result.receiptContainsLot(),
                "Phiếu nhập thêm không hiển thị lô cũ.");
        Assert.assertEquals(result.quantityAfter(),
                result.quantityBefore() + result.addedQuantity(),
                "Tồn kho lô cũ không tăng đúng số lượng đã nhập.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation", "idempotency"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_108)
    public void doubleClickCreatesOnlyOneReceipt() {
        var result = receiptPage.doubleClickCreatesOnlyOneReceipt();
        Assert.assertFalse(result.lotCode().isBlank());
        Assert.assertEquals(result.receiptRowCount(), 1,
                "Sau khi nhấp đôi phải có đúng một dòng phiếu cho mã lô.");
        Assert.assertEquals(result.stockQuantity(), 1,
                "Nhấp đôi làm tồn kho tăng nhiều hơn một lần.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation", "bulk-fill"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_120)
    public void createsRealReceiptForAllVariantsUsingBulkFill() {
        var result = receiptPage.submitMultiVariantWithBulkFillReal();
        Assert.assertTrue(result.rowCount() >= 2);
        Assert.assertEquals(result.lotCodes().size(), result.rowCount());
        Assert.assertEquals(result.submittedSummary().validLots(), result.rowCount());
        Assert.assertTrue(result.receiptCreated(), "Không tìm thấy phiếu vừa tạo.");
        Assert.assertTrue(result.stockQuantities().stream().allMatch(value -> value == 2),
                "Tồn của một hoặc nhiều biến thể không bằng 2.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation", "date"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_121)
    public void createsRealReceiptWithManualImportDate() {
        var result = receiptPage.submitWithManualDateReal();
        Assert.assertFalse(result.lotCode().isBlank());
        Assert.assertFalse(result.receiptRowText().isBlank());
        Assert.assertTrue(result.receiptCreated(),
                "Không tìm thấy phiếu tạo bằng ngày nhập thủ công.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation", "old-lot"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_122)
    public void createsOneReceiptWithOldAndNewLots() {
        var result = receiptPage.submitOldAndNewLotsTogetherReal();
        if (!result.candidateAvailable()) {
            throw new SkipException("Sản phẩm nhiều biến thể hiện tại không có lô cũ.");
        }
        Assert.assertFalse(result.newLotCodes().isEmpty());
        Assert.assertTrue(result.receiptCreated(),
                "Không tìm thấy đúng một dòng phiếu chứa lô mới đại diện.");
        Assert.assertEquals(result.oldQuantityAfter(),
                result.oldQuantityBefore() + result.oldAddedQuantity(),
                "Tồn lô cũ không tăng đúng số lượng đã nhập.");
        Assert.assertTrue(result.newLotQuantities().stream().allMatch(value -> value == 1),
                "Một hoặc nhiều lô mới không có tồn bằng 1.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_123)
    public void newLotsIncreaseStockByExactSubmittedQuantity() {
        var result = receiptPage.submitNewLotsWithExactQuantityReal();
        Assert.assertFalse(result.lotCodes().isEmpty());
        Assert.assertTrue(result.receiptContainsAllLots());
        Assert.assertEquals(result.actualQuantities().size(), result.lotCodes().size());
        Assert.assertTrue(result.actualQuantities().stream()
                .allMatch(value -> value == result.expectedQuantity()),
                "Số tồn không bằng số lượng đã nhập " + result.expectedQuantity());
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation", "receipt"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_124)
    public void realReceiptShowsCodeTypeDateAndLots() {
        var result = receiptPage.submitAndReadReceiptMetadataReal();
        Assert.assertFalse(result.receiptCode().isBlank(), "Dòng phiếu không có mã NK.");
        Assert.assertTrue(result.receiptTypeVisible(), "Dòng phiếu không có loại Nhập kho.");
        Assert.assertTrue(result.receiptDateVisible(),
                "Dòng phiếu không có ngày " + result.expectedDate());
        Assert.assertTrue(result.receiptContainsAllLots(),
                "Dòng phiếu không hiển thị đủ chi tiết lô.");
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation", "variant"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_125)
    public void removedVariantIsExcludedFromRealReceipt() {
        var result = receiptPage.removeVariantThenSubmitReal();
        Assert.assertEquals(result.rowsSubmitted(), result.rowsBefore() - 1);
        Assert.assertEquals(result.lotCodes().size(), result.rowsSubmitted());
        Assert.assertTrue(result.receiptContainsAllLots());
        Assert.assertTrue(result.stockQuantities().stream().allMatch(value -> value == 2));
    }

    @Test(groups = {"uniform", "inventory", "stock-receipt", "submission", "mutation", "reset"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_RECEIPT_126)
    public void sequentialSubmissionsDoNotLeakPreviousFormData() {
        var result = receiptPage.submitTwiceWithoutFormLeakReal();
        Assert.assertTrue(result.secondFormReset(),
                "Form lần hai vẫn giữ dữ liệu của phiếu thứ nhất.");
        Assert.assertFalse(result.firstLotCodes().isEmpty());
        Assert.assertFalse(result.secondLotCodes().isEmpty());
        Assert.assertTrue(result.firstLotCodes().stream()
                .noneMatch(result.secondLotCodes()::contains));
        Assert.assertEquals(result.firstReceiptRows(), 1);
        Assert.assertEquals(result.secondReceiptRows(), 1);
    }
}
