package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.EkycPage;
import com.vuatho.pages.EkycPage.KycSide;
import com.vuatho.pages.LoginPage;
import com.vuatho.testdata.EkycInformationCase;
import com.vuatho.testdata.EkycWorkbookCase;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.util.List;

abstract class EkycWorkflowTestSupport extends BaseTest {
    protected static final List<String> FOCUSED_FAMILIES = List.of("REVIEW");
    protected static final List<String> EXCLUDED_CASE_KEYWORDS = List.of(
            "thieu",
            "rong",
            "chi spaces",
            "chi khoang trang",
            "khoang trang dau/cuoi",
            "spaces dau/cuoi",
            "ky tu",
            "khong hop le",
            "sai dinh dang",
            "khong ton tai",
            "tuong lai",
            "hien tai",
            "ngay nhuan",
            "qua ngan",
            "qua dai",
            "rat dai",
            "rat xa",
            "nhieu dong",
            "html",
            "xss",
            "trung",
            "double click",
            "mat mang",
            "hai admin",
            "viewer",
            "permission",
            "status",
            "mapping",
            "doi chieu",
            "khong bi doi",
            "giu so 0",
            "khong co trong danh sach",
            "other",
            "pending");

    protected EkycPage ekycPage;

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareAuthenticatedSession() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Khong dang nhap duoc truoc khi kiem tra eKYC.");
        ekycPage = new EkycPage(driver);
        System.out.println("[eKYC] Dang nhap xong, san sang mo menu eKYC.");
    }

    protected void runReviewCase(EkycWorkbookCase testCase) {
        openFirstKycDrawer(testCase, "review");

        if (isRejectReviewCase(testCase)) {
            KycSide rejectedSide = reviewSide(testCase);
            ekycPage.markAvailableKycSidesValidExcept(rejectedSide);
            ekycPage.rejectKycSideAndSelectReason(rejectedSide);
            Assert.assertTrue(TextNormalizer.normalize(ekycPage.drawerText()).contains("tu choi"),
                    testCase.id() + ": Trang thai tu choi phai duoc giu.");
            Assert.assertTrue(ekycPage.hasRejectReasons(),
                    testCase.id() + ": Flow tu choi phai hien thi va chon ly do tu choi.");
        } else {
            ekycPage.markAvailableKycSidesValid();
            Assert.assertTrue(TextNormalizer.normalize(ekycPage.drawerText()).contains("hop le"),
                    testCase.id() + ": Trang thai hop le phai duoc giu.");
        }

        System.out.printf("[%s] Da chon quyet dinh review. Bam Xac nhan.%n", testCase.id());
        ekycPage.submitDrawerDecision();
    }

    protected void runEditInformationCase(EkycInformationCase testCase) {
        openFirstKycDrawer(testCase, "edit thong tin KYC");

        if (!ekycPage.hasEditInformationAction()) {
            throw new SkipException(testCase.id()
                    + ": Drawer hien tai khong co hanh dong sua thong tin.");
        }

        ekycPage.openEditInformationIfAvailable();
        Assert.assertTrue(ekycPage.kycInformationEditorIsOpen(),
                testCase.id() + ": Editor thong tin KYC phai mo truoc khi sua.");
        ekycPage.editKycInformationFields(testCase.fields());
        if (testCase.cancelsChanges()) {
            System.out.printf("[%s] Da sua thong tin KYC. Bam Huy.%n", testCase.id());
            ekycPage.cancelKycInformationChanges();
        } else {
            System.out.printf("[%s] Da sua thong tin KYC. Bam Luu thay doi.%n", testCase.id());
            ekycPage.saveKycInformationChanges();
        }
    }

    protected void runClearInformationCase(EkycInformationCase testCase) {
        openFirstKycDrawer(testCase, "clear/delete thong tin KYC");

        if (!ekycPage.hasKycInformationPreview()) {
            throw new SkipException(testCase.id()
                    + ": Drawer hien tai khong co preview anh KYC de mo editor.");
        }

        ekycPage.openClearKycIfAvailable();
        Assert.assertTrue(ekycPage.kycInformationEditorIsOpen(),
                testCase.id() + ": Editor thong tin KYC phai mo truoc khi clear/delete.");
        if (testCase.clearsMultipleFields()) {
            ekycPage.clearKycInformationFields(testCase.fields());
            System.out.printf("[%s] Da clear nhieu field KYC.%n", testCase.id());
        } else {
            ekycPage.clearKycInformationFields(testCase.fields());
            System.out.printf("[%s] Da clear field KYC khop case.%n", testCase.id());
        }

        if (testCase.refillsAfterClear()) {
            ekycPage.editKycInformationFields(testCase.fields());
            System.out.printf("[%s] Da nhap lai field KYC sau khi clear truoc khi luu.%n", testCase.id());
        }

        if (testCase.cancelsChanges()) {
            System.out.printf("[%s] Bam Huy sau khi clear.%n", testCase.id());
            ekycPage.cancelKycInformationChanges();
        } else {
            System.out.printf("[%s] Bam Luu thay doi sau khi clear.%n", testCase.id());
            ekycPage.saveKycInformationChanges();
        }
    }

    protected void openFirstKycDrawer(EkycInformationCase testCase, String action) {
        System.out.printf("[eKYC] Chay %s cho %s%n", action, testCase.id());
        ekycPage.openFromMenu();
        if (ekycPage.firstApplicantId().isBlank()) {
            throw new SkipException(testCase.id() + ": Khong co dong applicant de chay " + action + ".");
        }
        ekycPage.openFirstDrawer();
        Assert.assertTrue(ekycPage.drawerHasTitle(),
                testCase.id() + ": Thieu title Chi tiet eKYC trong drawer.");
    }

    protected void openFirstKycDrawer(EkycWorkbookCase testCase, String action) {
        System.out.printf("[eKYC] Chay %s cho %s%n", action, testCase.id());
        ekycPage.openFromMenu();
        if (ekycPage.firstApplicantId().isBlank()) {
            throw new SkipException(testCase.id() + ": Khong co dong applicant de chay " + action + ".");
        }
        ekycPage.openFirstDrawer();
        Assert.assertTrue(ekycPage.drawerHasTitle(),
                testCase.id() + ": Thieu title Chi tiet eKYC trong drawer.");
    }

    protected boolean isFocusedSimpleDataCase(EkycWorkbookCase testCase) {
        String text = normalizedCaseTitle(testCase);
        if ("REVIEW".equals(testCase.family())) {
            return List.of(
                    "pending",
                    "thieu",
                    "other",
                    "huy",
                    "dong x",
                    "double click",
                    "list/stat",
                    "viewer",
                    "permission")
                    .stream()
                    .noneMatch(text::contains);
        }
        return EXCLUDED_CASE_KEYWORDS.stream().noneMatch(text::contains);
    }

    protected String configured(String property, String environment) {
        String configured = System.getProperty(property);
        if (configured == null || configured.isBlank() || configured.startsWith("${")) {
            configured = System.getenv(environment);
        }
        return configured == null ? "" : configured.trim();
    }

    private boolean isRejectReviewCase(EkycWorkbookCase testCase) {
        String text = normalizedCaseText(testCase);
        return text.contains("reject")
                || text.contains("tu choi")
                || text.contains("rejected")
                || text.contains("khong hop le")
                || text.contains("ly do");
    }

    private KycSide reviewSide(EkycWorkbookCase testCase) {
        String text = normalizedCaseText(testCase);
        if (text.contains("selfie") || text.contains("chan dung")) {
            return KycSide.SELFIE;
        }
        if (text.contains("back") || text.contains("mat sau")) {
            return KycSide.BACK;
        }
        return KycSide.FRONT;
    }

    private String normalizedCaseText(EkycWorkbookCase testCase) {
        return TextNormalizer.normalize(String.join(" ",
                testCase.id(),
                testCase.scenario(),
                testCase.steps(),
                testCase.expected(),
                testCase.rule()));
    }

    private String normalizedCaseTitle(EkycWorkbookCase testCase) {
        return TextNormalizer.normalize(String.join(" ",
                testCase.id(),
                testCase.scenario()));
    }
}
