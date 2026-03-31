# RISE Automation Testing

Playwright (TypeScript) & Katalon Studio automation testing for **RISE - Ultimate Project Manager & CRM**.

**Test site:** https://rise.fairsketch.com (demo resets every 6 hours)

## Team

| Member | Student ID | Features |
|--------|-----------|----------|
| Sơn Tân | 2201700053 | FE1 Auth, FE2 Clients (CRUD, Search, View/Add/Edit Contacts) |
| Nguyễn Ngọc Thạch | 2201700077 | FE2 Clients (Filter Clients), FE3 Projects, FE4 Events (Add Event, Label, View Details) |
| Lê Công Hoàng Phúc | 2201700083 | FE4 Events (Edit/Delete Event, Add Reminder), FE5 Leads |

## Tech Stack

- **Playwright** with TypeScript — Page Object Model (POM) pattern
- **Katalon Studio** with Groovy — Object Repository + Data-driven (CSV)
- **GitHub Actions** for CI/CD

## Project Structure

```
├── Playwright/                 # Playwright test project
│   ├── pages/                  # Page Object classes
│   ├── tests/
│   │   ├── auth.setup.ts       # Auth setup (manual reCAPTCHA)
│   │   ├── fe1-auth/           # Authentication tests
│   │   ├── fe2-clients/        # Client management tests
│   │   ├── fe3-projects/       # Project management tests
│   │   ├── fe4-events/         # Event tests
│   │   └── fe5-leads/          # Lead tests
│   ├── .auth/                  # Saved auth state (gitignored)
│   ├── data/                   # Test case JSON files
│   └── playwright.config.ts
│
├── Katalon/RISE_Automation/    # Katalon Studio project
│   ├── Object Repository/      # Spied web elements per page
│   │   ├── Page_Login/
│   │   ├── Page_Register/
│   │   ├── Page_ResetPassword/
│   │   ├── Page_Dashboard/
│   │   ├── Page_Clients/
│   │   ├── Page_Contacts/
│   │   ├── Page_Projects/
│   │   ├── Page_Tasks/
│   │   ├── Page_Events/
│   │   └── Page_Leads/
│   ├── Test Cases/             # Groovy test scripts per feature
│   │   ├── Common/Setup_Auth
│   │   ├── FE1_Auth/
│   │   ├── FE2_Clients/
│   │   ├── FE3_Projects/
│   │   ├── FE4_Events/
│   │   └── FE5_Leads/
│   ├── Data Files/             # CSV data files for data-driven testing
│   ├── Test Suites/            # One Test Suite per feature (FE1-FE5)
│   ├── Keywords/helpers/       # AuthHelper for cookie-based auth
│   └── Profiles/default.glbl   # Global variables (baseUrl, timeout)
```

## Playwright

### Setup

```bash
cd Playwright
npm install
npx playwright install
```

### Authentication

The demo site uses reCAPTCHA. Auth setup opens a headed browser for manual solving.

```bash
# Generate auth state (first time or after session expires)
npx playwright test tests/auth.setup.ts --headed --project=auth-setup

# Reset expired auth and regenerate
rm -rf .auth && npx playwright test tests/auth.setup.ts --headed --project=auth-setup

npx playwright test --project=auth-setup --headed -g "admin"
npx playwright test --project=auth-setup --headed -g "client"
```

Two browsers will open sequentially. In each one:
1. Click the Admin/Client credential card
2. Solve the reCAPTCHA
3. Click Sign In
4. Script auto-saves the session once dashboard loads

Auth files (`.auth/admin.json`, `.auth/client.json`) last until the demo site resets (~6 hours).

### Manual Cookie Auth (reCAPTCHA banned workaround)

If reCAPTCHA blocks the Playwright browser, you can log in manually via a regular browser and export cookies:

1. Open https://rise.fairsketch.com in Chrome, log in as Admin
2. Open DevTools → Application → Cookies → `rise.fairsketch.com`
3. Copy `ci_session` and `rise_csrf_cookie` values
4. Create `.auth/admin.json` with the following structure:

```json
{
  "cookies": [
    {
      "name": "ci_session",
      "value": "YOUR_CI_SESSION_VALUE",
      "domain": "rise.fairsketch.com",
      "path": "/",
      "expires": 1774581349,
      "httpOnly": true,
      "secure": true,
      "sameSite": "Lax"
    },
    {
      "name": "rise_csrf_cookie",
      "value": "YOUR_CSRF_COOKIE_VALUE",
      "domain": "rise.fairsketch.com",
      "path": "/",
      "expires": 1774581349,
      "httpOnly": true,
      "secure": true,
      "sameSite": "Lax"
    }
  ],
  "origins": []
}
```

> Set `expires` to a future Unix timestamp (e.g. current time + 7200 for 2 hours). You can get one via `date -v+2H +%s` (macOS) or `date -d '+2 hours' +%s` (Linux). Replace the cookie values with your actual values. Repeat for `.auth/client.json` after logging in as Client.

## Running Tests

```bash
# Run ALL tests (headless) — requires valid .auth files
npx playwright test --workers=1

# Run a specific feature
npx playwright test tests/fe1-auth/ --workers=1
npx playwright test tests/fe2-clients/ --workers=1

# Run a specific test case by ID
npx playwright test --grep "TC_LOGIN_001" --workers=1

# Run in headed mode (see the browser)
npx playwright test tests/fe1-auth/ --headed --workers=1

# Run positive login tests headed (you solve reCAPTCHA)
npx playwright test --grep "TC_LOGIN_001|TC_LOGIN_005" --headed --workers=1

# Run only negative tests (no reCAPTCHA needed, works headless)
npx playwright test --grep "TC_LOGIN_002|TC_LOGIN_003|TC_LOGIN_004" --workers=1
```

> **Note:** Use `--workers=1` to avoid session conflicts (demo site shares one session cookie).

### Reports

```bash
# View the HTML test report
npx playwright show-report

# Generate report explicitly
npx playwright test --reporter=html --workers=1
```

## Katalon Studio

### Setup

1. Open Katalon Studio (v10.4.3+)
2. Open project: `Katalon/RISE_Automation/`
3. Run `Test Cases/Common/Setup_Auth` to save admin cookies (manual CAPTCHA)
4. Re-run `Setup_Auth` when cookies expire (~6 hours)

### Authentication

- **FE1 Auth TCs (Login, Register, ResetPassword):** Manual CAPTCHA via popup dialog
- **FE2-FE5 TCs:** Cookie-based auth via `AuthHelper.groovy` (no CAPTCHA)

### Running Tests

1. Open a Test Suite (e.g., `TS_FE1_Auth`)
2. Click Run (green play button)
3. Data-driven TCs iterate through CSV rows automatically

### Test Cases (31 TCs, data-driven)

| FE | Test Cases | Data Rows |
|----|-----------|-----------|
| **FE1 Auth** | TC_Login (5), TC_Logout (1), TC_Register (4), TC_ResetPassword (3) | 13 |
| **FE2 Clients** | TC_Client_Add (3), TC_Client_Edit (2), TC_Client_Delete (2), TC_Client_Search (2), TC_Client_View (1), TC_Contact_Add (2), TC_Contact_Edit (2), TC_Client_Filter (3) | 17 |
| **FE3 Projects** | TC_Project_Add (3), TC_Project_Edit (2), TC_Project_Delete (2), TC_Project_Search (3), TC_Project_View (1), TC_Project_Member (2), TC_Task_Add (3), TC_Task_Edit (3) | 19 |
| **FE4 Events** | TC_Event_Add (4), TC_Event_Edit (3), TC_Event_Delete (2), TC_Event_Label (2), TC_Event_View (1), TC_Event_Reminder (3) | 15 |
| **FE5 Leads** | TC_Lead_Add (4), TC_Lead_Edit (3), TC_Lead_Delete (2), TC_Lead_Search (3), TC_Lead_View (2) | 14 |
| **Total** | **31 Test Cases** | **78 scenarios** |

## Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@demo.com | riseDemo |
| Client | client@demo.com | riseDemo |

## Test Cases

Test cases are defined in `Playwright/data/RISE_TestCases.json`, organized by feature:

- **FE1** — Authentication (TC_LOGIN, TC_LOGOUT, TC_REGISTER, TC_RESETPW)
- **FE2** — Client Management (TC_CLIENT_ADD, TC_CLIENT_EDIT, TC_CLIENT_DELETE, TC_CLIENT_SEARCH, TC_CLIENT_FILTER, TC_CONTACT_ADD, TC_CONTACT_EDIT)
- **FE3** — Project Management (TC_PROJECT_*, TC_TASK_*)
- **FE4** — Events (TC_EVENT_*)
- **FE5** — Leads (TC_LEAD_*)
