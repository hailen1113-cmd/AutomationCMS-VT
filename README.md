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
Chrome window, close all its windows, then run `ErpLoginTest.main()` again. Google
credentials must not be automated or committed to source control.
