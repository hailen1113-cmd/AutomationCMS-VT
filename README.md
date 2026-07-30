# AutomationCMS-VT

Selenium + TestNG automation tests for `https://erp-sandbox.vuatho.com/`.

## Run

```powershell
mvn test
```

Show the browser while testing:

```powershell
mvn test -Dheadless=false
```

## Attach to a manually authenticated Chrome

Google can reject sign-in when Chrome was launched by automation. Open a normal
Chrome window with a dedicated debugging profile, finish Vercel/Google sign-in,
and leave that Chrome window open:

```powershell
.\start-manual-chrome.ps1
```

After the Dashboard is visible, open a second terminal and attach Selenium to
that same browser:

```powershell
mvn test "-Dtest=LoginDashboardSourceAccessTest" `
  "-Dchrome.debugger.address=127.0.0.1:9222" `
  "-Dkeep.browser.open=true"
```

Chrome 136 and newer require remote debugging to use a non-default user-data
directory. The script uses `.selenium/chrome-profile-manual-attach`, which is
already excluded from Git.

## Vercel protection

The sandbox currently uses Vercel Deployment Protection. Ask the project owner for an
automation bypass secret, then keep it only in an environment variable:

```powershell
$env:VERCEL_AUTOMATION_BYPASS_SECRET = "your-secret"
mvn test
```

Never commit the secret. Failed-test screenshots are written to `target/screenshots/`.

## Dashboard test-case catalog

The supplied 222-case dashboard workbook is versioned at
`src/test/resources/testcases/Test_Cases_Dashboard_Vua_Tho_Full.xlsx`.
`DashboardTestCaseCatalogTest` verifies the exact case count, unique IDs,
required fields, module totals, and automation-feasibility totals without
starting a browser:

```powershell
mvn -q -Dtest=DashboardTestCaseCatalogTest test
```

The catalog is the traceability source. A case being present in the workbook
does not by itself mean that its Selenium implementation is complete.

Run the focused Dashboard suite (catalog, framework-quality checks, and live
Selenium assertions) with:

```powershell
mvn clean -Pdashboard -Dheadless=true test
```

Use `-Dheadless=false` when you want to watch Chrome. Reports are generated at
`test-output/index.html` and `target/reports/test-summary.html`.

## Uniform Catalog test-case catalog

`UniformCatalogTestCases.java` is the readable testcase list. The JSON file
`src/test/resources/testcases/uniform-catalog.json` supplies data for the 16
read-only/safe logical cases and their 32 executions. Two additional critical
mutation cases (`UNI-CAT-019` and `UNI-CAT-020`) execute full CRUD lifecycles
for groups and uniform products.

`UniformCatalogTestCaseCatalog` validates the JSON and supplies its executions
directly to the five data-driven TestNG classes. CRUD uses unique `AUTO-*`
fixtures and best-effort cleanup in `finally`. Run catalog/list synchronization
validation without opening a browser:

```powershell
mvn -q "-Dtest=UniformCatalogJsonCatalogTest,TestCaseCatalogValidationTest" test
```

The live suite writes generated output to:

- `target/reports/uniform-catalog-results.csv`
- `target/reports/uniform-catalog-summary.txt`

Run the complete live Uniform Catalog suite with the manual Chrome session.
This suite creates, updates, and deletes temporary sandbox records:

```powershell
mvn test -Puniform-catalog `
  "-Dchrome.debugger.address=127.0.0.1:9222" `
  "-Dkeep.browser.open=true" `
  "-Dheadless=true"
```

## Login test

The default test email is `hailv@vuatho.com`. Store the password in an environment
variable instead of source code:

```powershell
$env:GOOGLE_PASSWORD = "your-google-password"
$env:VERCEL_AUTOMATION_BYPASS_SECRET = "your-vercel-secret"
mvn test -Dheadless=false
```

Override the email only when needed with `ERP_EMAIL`.

When running `ErpLoginTest.main()` with **Run Java**, Chrome uses the persistent local
profile `.selenium/chrome-profile`. On the first run, complete Vercel authentication in
the opened browser within two minutes. That session is reused on later runs. The
profile directory is excluded from Git.

If Google displays **This browser or app may not be secure**, stop the running test and
Run Java on `GoogleSessionSetup.main()`. Sign in to Google manually in that normal
Chrome window, close all its windows, then run tests with the same persistent profile:

```powershell
mvn test -Dheadless=false -Dselenium.profile.dir=.selenium/chrome-profile
```

Google credentials must not be automated or committed to source control.

## Standard TestNG suites

The maintained suite entry points are under `src/test/resources/suites/`:

- `smoke.xml`
- `regression.xml`
- `dashboard.xml`
- `ekyc.xml`
- `uniform.xml`
- `customer-worker-order.xml`

Run them through Maven profiles, for example:

```powershell
mvn test -Psmoke
mvn test -Pregression
mvn test -Pekyc
mvn test -Puniform
mvn test -Pcustomerworkerorder
```

Legacy root XML files remain available through `legacy-dashboard`,
`legacy-uniform-management`, and `legacy-customer-worker-order` while callers
are migrated. IDE `main` entry points are kept under
`com.vuatho.suites.runners`; business test classes remain under
`com.vuatho.tests.<module>.<feature>`.

The shared WebDriver is released only from `DriverLifecycleListener` after the
entire TestNG execution. With `keep.browser.open=true`, Selenium intentionally
leaves the externally opened Chrome session running. If an attached manual ERP
session expires, sign in again in that Chrome window before rerunning; Google
credentials are not bypassed by the framework.

## Java testcase lists and catalog reports

Open the module files under `src/test/java/com/vuatho/testcases/*TestCases.java`
to read the complete business testcase list directly in VS Code without running
the project. Each public constant owns one permanent `ID - Scenario` string.

Test management follows this package boundary:

```text
src/test/java/com/vuatho/
├── tests/                 # Business @Test methods only
│   ├── dashboard/
│   ├── ekyc/
│   │   ├── api/
│   │   ├── information/
│   │   └── review/
│   ├── uniform/
│   │   ├── catalog/
│   │   ├── inventory/
│   │   └── order/
│   └── customerworkerorder/
├── testcases/             # Readable module lists, report scanner and validation
├── quality/               # Automation-framework quality tests
└── validation/            # Workbook/catalog/test-data validation tests
```

The traceability flow is:

```text
EkycTestCases.EKYC_001
    -> @Test(description = EkycTestCases.EKYC_001)
    -> business test method
    -> TestCaseCatalog
    -> TXT / CSV
```

The `*TestCases.java` constants are the readable source of truth. Business
`@Test` methods reuse those constants instead of duplicating their ID/scenario:

```java
public final class EkycTestCases {
    public static final String EKYC_001 =
            "EKYC-001 - Duyệt hồ sơ eKYC hợp lệ";
}

@Test(
    description = EkycTestCases.EKYC_001,
    groups = {"ekyc", "regression", "critical"}
)
public void approveValidEkyc() {
}
```

`TestCaseCatalog` still scans only `com.vuatho.tests` to generate TXT/CSV
reports. Framework validation tests, tools, listeners, and runners are
excluded. Validation fails when an ID is missing/duplicated, a managed constant
is unused, or a business `@Test` does not reference a managed constant. IDs are
never generated from CRC32, class names, or method names.

Generated reports:

- `target/reports/test-case-catalog.txt`
- `target/reports/test-case-catalog.csv`

Validate missing mappings, duplicate IDs, duplicate class-method mappings, and
required metadata with:

```powershell
mvn test "-Dtest=TestCaseCatalogValidationTest"
```
