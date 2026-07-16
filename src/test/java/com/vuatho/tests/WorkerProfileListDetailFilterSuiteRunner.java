package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class WorkerProfileListDetailFilterSuiteRunner {
    private WorkerProfileListDetailFilterSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test danh sach chi tiet bo loc ho so tho ERP",
                "Chay tat ca nhom testcase danh sach chi tiet bo loc ho so tho",
                WorkerProfileListDetailTest.class,
                WorkerProfileFilterTest.class);
    }
}
