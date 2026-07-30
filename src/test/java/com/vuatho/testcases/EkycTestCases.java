package com.vuatho.testcases;

/**
 * Readable, compile-time testcase list for this business module.
 * Each constant is used directly by exactly one business {@code @Test}.
 */
public final class EkycTestCases {
    private EkycTestCases() {
    }

    public static final String EKYC_001 = "EKYC-001 - Dashboard Kyc Returns Statistic Contract";
    public static final String EKYC_002 = "EKYC-002 - Detail Returns Applicant Personal Info Images And Decision State";
    public static final String EKYC_003 = "EKYC-003 - Detail Rejects Invalid Or Unknown Applicant Id";
    public static final String EKYC_004 = "EKYC-004 - List Default Returns Paginated Applicant Contract";
    public static final String EKYC_005 = "EKYC-005 - List Limit Is Capped At Fifty";
    public static final String EKYC_006 = "EKYC-006 - List Supports Status Filter";
    public static final String EKYC_007 = "EKYC-007 - List Supports Document Type Filter";
    public static final String EKYC_008 = "EKYC-008 - List Supports Search By User Id";
    public static final String EKYC_009 = "EKYC-009 - List Supports Search Not Found";
    public static final String EKYC_010 = "EKYC-010 - List Supports Date Range Query";
    public static final String EKYC_011 = "EKYC-011 - Pagination Next Page Changes Applicant Window When Available";
    public static final String EKYC_012 = "EKYC-012 - List Handles Invalid Query Without Server Error";
    public static final String EKYC_013 = "EKYC-013 - Update Rejects Invalid Review Payload";
    public static final String EKYC_014 = "EKYC-014 - Update Can Approve All Document Sides For Seed Applicant";
    public static final String EKYC_015 = "EKYC-015 - Update Can Reject With Document Reason For Seed Applicant";
    public static final String EKYC_016 = "EKYC-016 - Update Info Can Patch Seven Editable Fields For Seed Applicant";
    public static final String EKYC_017 = "EKYC-017 - Rerun Ai Dispatches Or Returns Business Validation For Seed Applicant";
    public static final String EKYC_018 = "EKYC-018 - Kyc Apis Require Authentication";
    public static final String EKYC_020 = "EKYC-020 - EKYC-INFORMATION-CLEAR: Clear/delete thong tin eKYC";
    public static final String EKYC_021 = "EKYC-021 - EKYC-INFORMATION-EDIT: Sua thong tin eKYC";
    public static final String EKYC_023 = "EKYC-023 - EKYC-REVIEW: Approve va reject testcase tu workbook";
}
