package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.testdata.UserProfileCase;
import com.vuatho.testdata.UserProfileFeature;
import com.vuatho.testdata.UserProfileTestData;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class UserProfileNameUpdateWorkflowTest extends UserProfileTestSupport {
    private static final String NAME_OTHER_REJECT_REASON =
            "Tu dong tu choi cap nhat ho ten voi ly do khac.";

    public static void main(String[] args) {
        TestNgRunner.run(UserProfileNameUpdateWorkflowTest.class,
                "Bo test cap nhat ho ten nguoi dung ERP",
                "Kiem tra duyet va tu choi cap nhat ho ten");
    }

    @DataProvider(name = "nameUpdateCases", parallel = false)
    public Object[][] nameUpdateCases() {
        List<UserProfileCase> cases = UserProfileTestData.cases().stream()
                .filter(testCase -> testCase.feature() == UserProfileFeature.NAME_UPDATE)
                .filter(this::matchesConfiguredCase)
                .toList();
        Object[][] rows = new Object[cases.size()][1];
        for (int index = 0; index < cases.size(); index++) {
            rows[index][0] = cases.get(index);
        }
        return rows;
    }

    @Test(dataProvider = "nameUpdateCases",
            groups = {"user-profile", "user-name-update"},
            description = "USER-PROFILE-NAME-UPDATE: Duyet va tu choi yeu cau cap nhat ho ten")
    public void runNameUpdateCase(UserProfileCase testCase) {
        userProfilePage.openFromMenu();
        userProfilePage.openUserWithPendingNameUpdateRequest();
        if (!userProfilePage.hasPendingNameUpdateRequest()) {
            throw new SkipException(testCase.id() + ": Khong co yeu cau cap nhat ho ten dang cho.");
        }
        if (testCase.approvesUpdate()) {
            userProfilePage.approveNameUpdateRequest();
        } else if (testCase.rejectsUpdate()) {
            rejectNameUpdate(testCase);
        } else {
            throw new SkipException(testCase.id() + ": Quyet dinh cap nhat ho ten chua duoc ho tro.");
        }
    }

    private void rejectNameUpdate(UserProfileCase testCase) {
        if (testCase.usesOtherRejectReason()) {
            userProfilePage.rejectNameUpdateRequestWithOtherReason(NAME_OTHER_REJECT_REASON);
        } else {
            userProfilePage.rejectNameUpdateRequestWithDefaultReason();
        }
    }
}
