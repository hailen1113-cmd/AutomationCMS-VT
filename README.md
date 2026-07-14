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

## Login test

The default test email is `hailen1113@gmail.com`. Store the password in an environment
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
