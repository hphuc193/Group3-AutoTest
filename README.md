# RISE Automation Testing

Playwright (TypeScript) test automation for **RISE - Ultimate Project Manager & CRM**.

**Test site:** https://rise.fairsketch.com (demo resets every 6 hours)

## Team

| Member | Student ID | Features |
|--------|-----------|----------|
| Sơn Tân | 2201700053 | FE1 Auth, FE2 Clients (CRUD, Search, View/Add/Edit Contacts) |
| Nguyễn Ngọc Thạch | 2201700077 | FE2 Clients (Filter Clients), FE3 Projects, FE4 Events (Add Event, Label, View Details) |
| Lê Công Hoàng Phúc | 2201700083 | FE4 Events (Edit/Delete Event, Add Reminder), FE5 Leads |

## Tech Stack

- **Playwright** with TypeScript
- **Page Object Model** (POM) pattern
- **GitHub Actions** for CI/CD

## Project Structure

```
├── pages/              # Page Object classes
├── tests/
│   ├── auth.setup.ts   # Auth setup (manual reCAPTCHA)
│   ├── fe1-auth/       # Authentication tests
│   ├── fe2-clients/    # Client management tests
│   ├── fe3-projects/   # Project management tests
│   ├── fe4-events/     # Event tests
│   └── fe5-leads/      # Lead tests
├── .auth/              # Saved auth state (gitignored)
├── data/               # Test case JSON files
└── playwright.config.ts
```

## Setup

```bash
# Install dependencies
npm install

# Install Playwright browsers
npx playwright install
```

## Authentication

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

## Reports

```bash
# View the HTML test report
npx playwright show-report

# Generate report explicitly
npx playwright test --reporter=html --workers=1
```

## Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@demo.com | riseDemo |
| Client | client@demo.com | riseDemo |

## Test Cases

Test cases are defined in `data/RISE_TestCases.json`, organized by feature:

- **FE1** — Authentication (TC_LOGIN, TC_LOGOUT, TC_REGISTER, TC_RESETPW)
- **FE2** — Client Management (TC_CLIENT_ADD, TC_CLIENT_EDIT, TC_CLIENT_DELETE, TC_CLIENT_SEARCH, TC_CLIENT_FILTER)
- **FE3** — Project Management (TC_PROJECT_*)
- **FE4** — Events (TC_EVENT_*)
- **FE5** — Leads (TC_LEAD_*)
