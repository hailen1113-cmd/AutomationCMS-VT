package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import com.vuatho.testdata.UserProfileCase;
import com.vuatho.testdata.UserProfileFeature;
import com.vuatho.testdata.UserProfileTestData;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class UserProfileAvatarUpdateWorkflowTest extends UserProfileTestSupport {
    private static final String AVATAR_OTHER_REJECT_REASON =
            "Tu dong tu choi cap nhat anh dai dien voi ly do khac.";

    public static void main(String[] args) {
        TestNgRunner.run(UserProfileAvatarUpdateWorkflowTest.class,
                "Bo test cap nhat anh dai dien nguoi dung ERP",
                "Kiem tra duyet va tu choi cap nhat anh dai dien");
    }

    @DataProvider(name = "avatarUpdateCases", parallel = false)
    public Object[][] avatarUpdateCases() {
        List<UserProfileCase> cases = UserProfileTestData.cases().stream()
                .filter(testCase -> testCase.feature() == UserProfileFeature.AVATAR_UPDATE)
                .filter(this::matchesConfiguredCase)
                .toList();
        Object[][] rows = new Object[cases.size()][1];
        for (int index = 0; index < cases.size(); index++) {
            rows[index][0] = cases.get(index);
        }
        return rows;
    }

    @Test(dataProvider = "avatarUpdateCases",
            groups = {"user-profile", "user-avatar-update"},
            description = "USER-PROFILE-AVATAR-UPDATE: Duyet va tu choi yeu cau cap nhat anh dai dien")
    public void runAvatarUpdateCase(UserProfileCase testCase) {
        userProfilePage.openFromMenu();
        userProfilePage.openUserWithPendingAvatarUpdateRequest();
        if (!userProfilePage.hasPendingAvatarUpdateRequest()) {
            throw new SkipException(testCase.id() + ": Khong co yeu cau cap nhat anh dai dien dang cho.");
        }
        if (testCase.approvesUpdate()) {
            userProfilePage.approveAvatarUpdateRequest();
        } else if (testCase.rejectsUpdate()) {
            rejectAvatarUpdate(testCase);
        } else {
            throw new SkipException(testCase.id() + ": Quyet dinh cap nhat anh dai dien chua duoc ho tro.");
        }
    }

    private void rejectAvatarUpdate(UserProfileCase testCase) {
        if (testCase.usesOtherRejectReason()) {
            userProfilePage.rejectAvatarUpdateRequestWithOtherReason(AVATAR_OTHER_REJECT_REASON);
        } else {
            userProfilePage.rejectAvatarUpdateRequestWithDefaultReason();
        }
    }
}
