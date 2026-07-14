package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.EkycPage;
import com.vuatho.pages.EkycPage.KycSide;
import com.vuatho.pages.LoginPage;
import com.vuatho.testdata.EkycInformationAction;
import com.vuatho.testdata.EkycInformationCase;
import com.vuatho.testdata.EkycInformationTestData;
import com.vuatho.testdata.EkycWorkbookCatalog;
import com.vuatho.testdata.EkycWorkbookCase;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class EkycApiTest extends BaseTest {
    private static final List<String> FOCUSED_FAMILIES = List.of("REVIEW");
    private static final List<String> EXCLUDED_CASE_KEYWORDS = List.of(
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

    private EkycPage ekycPage;

    public static void main(String[] args) {
        TestNgRunner.run(EkycApiTest.class, "ERP eKYC Review Suite",
                "eKYC approve, reject, edit, and clear testcases");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @DataProvider(name = "ekycReviewCases", parallel = false)
    public Object[][] ekycReviewCases() {
        List<EkycWorkbookCase> cases = EkycWorkbookCatalog.filteredLoad().stream()
                .filter(testCase -> FOCUSED_FAMILIES.contains(testCase.family()))
                .filter(this::isFocusedSimpleDataCase)
                .toList();
        Object[][] rows = new Object[cases.size()][1];
        for (int index = 0; index < cases.size(); index++) {
            rows[index][0] = cases.get(index);
        }
        return rows;
    }

    @DataProvider(name = "ekycInformationCases", parallel = false)
    public Object[][] ekycInformationCases() {
        List<EkycInformationCase> cases = EkycInformationTestData.cases().stream()
                .filter(this::matchesConfiguredInformationCase)
                .toList();
        Object[][] rows = new Object[cases.size()][1];
        for (int index = 0; index < cases.size(); index++) {
            rows[index][0] = cases.get(index);
        }
        return rows;
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareAuthenticatedSession() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Cannot sign in before checking eKYC.");
        ekycPage = new EkycPage(driver);
        System.out.println("[eKYC] Login completed. Ready to navigate through sidebar.");
    }

    @Test(dataProvider = "ekycReviewCases",
            groups = {"ekyc", "workbook", "review"},
            description = "eKYC approve and reject workbook testcase")
    public void runFocusedKycTestcase(EkycWorkbookCase testCase) {
        switch (testCase.family()) {
            case "REVIEW" -> runReviewCase(testCase);
            default -> throw new SkipException(testCase.id()
                    + ": This suite only runs REVIEW cases from workbook.");
        }
    }

    @Test(dataProvider = "ekycInformationCases",
            groups = {"ekyc", "information"},
            description = "eKYC edit and clear information testcase")
    public void runFocusedKycInformationCase(EkycInformationCase testCase) {
        switch (testCase.action()) {
            case EDIT -> runEditInformationCase(testCase);
            case CLEAR -> runClearInformationCase(testCase);
            default -> throw new SkipException(testCase.id()
                    + ": Unsupported eKYC information action.");
        }
    }

    private void runReviewCase(EkycWorkbookCase testCase) {
        openFirstKycDrawer(testCase, "review");

        if (isRejectReviewCase(testCase)) {
            KycSide rejectedSide = reviewSide(testCase);
            ekycPage.markAvailableKycSidesValidExcept(rejectedSide);
            ekycPage.rejectKycSideAndSelectReason(rejectedSide);
            Assert.assertTrue(TextNormalizer.normalize(ekycPage.drawerText()).contains("tu choi"),
                    testCase.id() + ": Reject action should stay selected.");
            Assert.assertTrue(ekycPage.hasRejectReasons(),
                    testCase.id() + ": Reject flow should show and select a rejection reason.");
        } else {
            ekycPage.markAvailableKycSidesValid();
            Assert.assertTrue(TextNormalizer.normalize(ekycPage.drawerText()).contains("hop le"),
                    testCase.id() + ": Approve action should stay selected.");
        }

        System.out.printf("[%s] Review decision selected. Clicking Xac nhan.%n", testCase.id());
        ekycPage.submitDrawerDecision();
    }

    private void runEditInformationCase(EkycInformationCase testCase) {
        openFirstKycDrawer(testCase, "edit KYC information");

        if (!ekycPage.hasEditInformationAction()) {
            throw new SkipException(testCase.id()
                    + ": Current drawer UI does not expose an edit information action.");
        }

        ekycPage.openEditInformationIfAvailable();
        Assert.assertTrue(ekycPage.kycInformationEditorIsOpen(),
                testCase.id() + ": KYC information editor should open before editing.");
        ekycPage.editKycInformationFields(testCase.fields());
        if (testCase.cancelsChanges()) {
            System.out.printf("[%s] KYC information edited. Clicking Huy.%n", testCase.id());
            ekycPage.cancelKycInformationChanges();
        } else {
            System.out.printf("[%s] KYC information edited. Clicking Luu thay doi.%n", testCase.id());
            ekycPage.saveKycInformationChanges();
        }
    }

    private void runClearInformationCase(EkycInformationCase testCase) {
        openFirstKycDrawer(testCase, "clear/delete KYC information");

        if (!ekycPage.hasKycInformationPreview()) {
            throw new SkipException(testCase.id()
                    + ": Current drawer UI does not expose a KYC image preview to open the editor.");
        }

        ekycPage.openClearKycIfAvailable();
        Assert.assertTrue(ekycPage.kycInformationEditorIsOpen(),
                testCase.id() + ": KYC information editor should open before canceling clear/delete flow.");
        if (testCase.clearsMultipleFields()) {
            ekycPage.clearKycInformationFields(testCase.fields());
            System.out.printf("[%s] Multiple KYC fields cleared.%n", testCase.id());
        } else {
            ekycPage.clearKycInformationFields(testCase.fields());
            System.out.printf("[%s] Matching KYC field cleared.%n", testCase.id());
        }

        if (testCase.refillsAfterClear()) {
            ekycPage.editKycInformationFields(testCase.fields());
            System.out.printf("[%s] Cleared KYC fields refilled before saving.%n", testCase.id());
        }

        if (testCase.cancelsChanges()) {
            System.out.printf("[%s] Clicking Huy after clear.%n", testCase.id());
            ekycPage.cancelKycInformationChanges();
        } else {
            System.out.printf("[%s] Clicking Luu thay doi after clear.%n", testCase.id());
            ekycPage.saveKycInformationChanges();
        }
    }

    private void openFirstKycDrawer(EkycInformationCase testCase, String action) {
        System.out.printf("[eKYC] Running %s for %s%n", action, testCase.id());
        ekycPage.openFromMenu();
        if (ekycPage.firstApplicantId().isBlank()) {
            throw new SkipException(testCase.id() + ": No applicant row available for " + action + ".");
        }
        ekycPage.openFirstDrawer();
        Assert.assertTrue(ekycPage.drawerHasTitle(),
                testCase.id() + ": Drawer title Chi tiet eKYC is missing.");
    }

    private void openFirstKycDrawer(EkycWorkbookCase testCase, String action) {
        System.out.printf("[eKYC] Running %s for %s%n", action, testCase.id());
        ekycPage.openFromMenu();
        if (ekycPage.firstApplicantId().isBlank()) {
            throw new SkipException(testCase.id() + ": No applicant row available for " + action + ".");
        }
        ekycPage.openFirstDrawer();
        Assert.assertTrue(ekycPage.drawerHasTitle(),
                testCase.id() + ": Drawer title Chi tiet eKYC is missing.");
    }

    private boolean isRejectReviewCase(EkycWorkbookCase testCase) {
        String text = TextNormalizer.normalize(String.join(" ",
                testCase.id(),
                testCase.scenario(),
                testCase.steps(),
                testCase.expected(),
                testCase.rule()));
        return text.contains("reject")
                || text.contains("tu choi")
                || text.contains("rejected")
                || text.contains("khong hop le")
                || text.contains("ly do");
    }

    private boolean isFocusedSimpleDataCase(EkycWorkbookCase testCase) {
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
        String text = TextNormalizer.normalize(String.join(" ",
                testCase.id(),
                testCase.scenario(),
                testCase.steps(),
                testCase.expected(),
                testCase.rule()));
        return text;
    }

    private String normalizedCaseTitle(EkycWorkbookCase testCase) {
        return TextNormalizer.normalize(String.join(" ",
                testCase.id(),
                testCase.scenario()));
    }

    private boolean matchesConfiguredInformationCase(EkycInformationCase testCase) {
        String configuredCaseId = configured("ekyc.case.id", "EKYC_CASE_ID");
        String configuredFamily = configured("ekyc.case.family", "EKYC_CASE_FAMILY");
        if (!configuredCaseId.isBlank() && !testCase.id().equalsIgnoreCase(configuredCaseId)) {
            return false;
        }
        if (configuredFamily.isBlank()) {
            return true;
        }
        if ("EDIT".equalsIgnoreCase(configuredFamily)) {
            return testCase.action() == EkycInformationAction.EDIT;
        }
        if ("CLEAR".equalsIgnoreCase(configuredFamily)) {
            return testCase.action() == EkycInformationAction.CLEAR;
        }
        return false;
    }

    private String configured(String property, String environment) {
        String configured = System.getProperty(property);
        if (configured == null || configured.isBlank() || configured.startsWith("${")) {
            configured = System.getenv(environment);
        }
        return configured == null ? "" : configured.trim();
    }
}
