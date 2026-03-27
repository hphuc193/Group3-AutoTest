import { test, expect } from '@playwright/test';
import { LoginPage } from '../../pages/login-page';

/**
 * FE1 — Logout Test
 * Must run --headed so user can solve reCAPTCHA during login step.
 */
test.describe('FE1 — Logout', () => {
  test.setTimeout(180_000); // 3 min for reCAPTCHA

  test('TC_LOGOUT_001 — Admin logout successfully', async ({ page }) => {
    // Step 1: Login first (with manual reCAPTCHA)
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.loginWithRecaptcha('admin@demo.com', 'riseDemo');
    await loginPage.expectDashboard();

    // Step 2: Click user profile dropdown (top-right)
    await page.locator('a#user-dropdown').click();
    await page.waitForTimeout(500);

    // Step 3: Click "Sign Out"
    await page.getByRole('link', { name: 'Sign Out' }).click();

    // Step 4: Should redirect to login page
    await expect(page).toHaveURL(/signin/i, { timeout: 15_000 });

    // Step 5: Verify we can't access dashboard without re-authenticating
    await page.goto('/index.php/dashboard');
    await expect(page).toHaveURL(/signin/i, { timeout: 15_000 });
  });
});
