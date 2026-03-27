import { test } from '@playwright/test';
import { LoginPage } from '../../pages/login-page';

/**
 * FE1 — Login Tests
 *
 * Positive tests: fill credentials → user solves reCAPTCHA manually →
 * script auto-detects completion and clicks Sign In.
 * Run with: npx playwright test tests/fe1-auth/login.spec.ts --headed
 *
 * Negative tests: submit invalid data, verify error handling.
 */

test.describe('FE1 — Login (positive)', () => {
  // 3 minutes to allow manual reCAPTCHA solving during demos
  test.setTimeout(180_000);

  test('TC_LOGIN_001 — Admin login with valid credentials', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    // Fill credentials, wait for reCAPTCHA, auto-click Sign In
    await loginPage.loginWithRecaptcha('admin@demo.com', 'riseDemo');
    await loginPage.expectDashboard();
  });

  test('TC_LOGIN_005 — Client login with valid credentials', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    await loginPage.loginWithRecaptcha('client@demo.com', 'riseDemo');
    await loginPage.expectDashboard();
  });
});

test.describe('FE1 — Login (negative)', () => {
  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    await loginPage.goto();
  });

  test('TC_LOGIN_002 — Login with wrong email', async () => {
    await loginPage.login('wrong@email.com', 'riseDemo');
    await loginPage.expectAlertVisible();
    await loginPage.expectStillOnLoginPage();
  });

  test('TC_LOGIN_003 — Login with wrong password', async () => {
    await loginPage.login('admin@demo.com', 'wrongPass123');
    await loginPage.expectAlertVisible();
    await loginPage.expectStillOnLoginPage();
  });

  test('TC_LOGIN_004 — Login with empty fields', async () => {
    await loginPage.submitEmpty();
    await loginPage.expectStillOnLoginPage();
  });
});
