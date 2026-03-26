import { test, expect } from '@playwright/test';

/**
 * FE1 — Reset Password Tests
 * TC_RESETPW_001: Reset password with valid email
 * TC_RESETPW_002: Reset password with non-existing email
 * TC_RESETPW_003: Reset password with empty email
 */
test.describe('FE1 — Reset Password', () => {
  test.setTimeout(120_000);

  test.beforeEach(async ({ page }) => {
    await page.goto('/index.php/signin/request_reset_password', { timeout: 45_000 });
  });

  test('TC_RESETPW_001 — Reset password with valid email (happy path)', async ({ page }) => {
    // Fill valid email
    await page.locator('input#email').fill('admin@demo.com');

    // Solve reCAPTCHA — click the checkbox and wait for it to complete
    const captchaFrame = page.frameLocator('iframe[title="reCAPTCHA"]');
    await captchaFrame.locator('.recaptcha-checkbox-border').click();
    await page.waitForTimeout(5_000);

    // Click Send
    await page.locator('button[type="submit"]').click();
    await page.waitForTimeout(3_000);

    // Expected: success message about sending reset instructions
    await expect(page.locator('div.app-alert-message')).toContainText('send instructions', { timeout: 15_000 });
  });

  test('TC_RESETPW_002 — Reset password with non-existing email', async ({ page }) => {
    // Fill non-existing email
    await page.locator('input#email').fill('notexist@test.com');

    // Solve reCAPTCHA
    const captchaFrame = page.frameLocator('iframe[title="reCAPTCHA"]');
    await captchaFrame.locator('.recaptcha-checkbox-border').click();
    await page.waitForTimeout(5_000);

    // Click Send
    await page.locator('button[type="submit"]').click();
    await page.waitForTimeout(3_000);

    // Expected: same message (system doesn't reveal if email exists or not)
    await expect(page.locator('div.app-alert-message')).toContainText('send instructions', { timeout: 15_000 });
  });

  test('TC_RESETPW_003 — Reset password with empty email shows error', async ({ page }) => {
    // Leave email empty, click Send
    await page.locator('button[type="submit"]').click();

    // Expected: validation error "This field is required."
    await expect(page.locator('span#email-error')).toContainText('This field is required', { timeout: 5_000 });
  });
});
