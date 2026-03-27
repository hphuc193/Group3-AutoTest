import { test, expect } from '@playwright/test';

/**
 * FE1 — Register Tests
 * TC_REGISTER_001 requires manual reCAPTCHA solving (run --headed).
 * Negative tests (002-004) submit without reCAPTCHA — validation fires first.
 */
test.describe.serial('FE1 — Register', () => {
  test.setTimeout(180_000);

  /** Helper: fill signup form fields by input IDs */
  async function fillSignupForm(
    page: import('@playwright/test').Page,
    data: { firstName?: string; lastName?: string; company?: string; email?: string; password?: string; retypePassword?: string }
  ) {
    if (data.firstName !== undefined) await page.locator('input#first_name').fill(data.firstName);
    if (data.lastName !== undefined) await page.locator('input#last_name').fill(data.lastName);
    if (data.company !== undefined) await page.locator('input[name="company_name"]').fill(data.company);
    if (data.email !== undefined) await page.locator('input#email').fill(data.email);
    if (data.password !== undefined) await page.locator('input#password').fill(data.password);
    if (data.retypePassword !== undefined) await page.locator('input#retype_password').fill(data.retypePassword);
  }

  test('TC_REGISTER_001 — Register with valid data', async ({ page }) => {
    await page.goto('/index.php/signup', { timeout: 45_000 });

    const uniqueId = Date.now();
    await fillSignupForm(page, {
      firstName: 'Test',
      lastName: 'User',
      company: `TestCo_${uniqueId}`,
      email: `testuser_${uniqueId}@test.com`,
      password: 'Test@1234',
      retypePassword: 'Test@1234',
    });

    // Wait for user to solve reCAPTCHA manually (up to 2 min)
    await page.waitForFunction(
      () => {
        const ta = document.querySelector('textarea[name="g-recaptcha-response"]');
        return ta && (ta as HTMLTextAreaElement).value.length > 0;
      },
      { timeout: 120_000 }
    );

    await page.getByRole('button', { name: 'Sign up' }).click();
    await page.waitForTimeout(3_000);

    // Should show success or redirect to signin
    const url = page.url();
    const hasAlert = await page.locator('.alert, [role="alert"]').count();
    expect(url.includes('signin') || hasAlert > 0).toBe(true);
  });

  test('TC_REGISTER_002 — Register with existing email', async ({ page }) => {
    await page.goto('/index.php/signup', { timeout: 45_000 });

    await fillSignupForm(page, {
      firstName: 'Test',
      lastName: 'User',
      company: 'TestCo',
      email: 'admin@demo.com',
      password: 'Test@1234',
      retypePassword: 'Test@1234',
    });

    // Need reCAPTCHA to reach server-side duplicate email check
    await page.waitForFunction(
      () => {
        const ta = document.querySelector('textarea[name="g-recaptcha-response"]');
        return ta && (ta as HTMLTextAreaElement).value.length > 0;
      },
      { timeout: 120_000 }
    );

    await page.getByRole('button', { name: 'Sign up' }).click();
    await page.waitForTimeout(3_000);

    // Should show duplicate email error and stay on signup
    const alertVisible = await page.locator('[role="alert"], .alert').count();
    const hasHelpBlock = await page.locator('span.help-block').count();
    const stillOnSignup = page.url().includes('signup');
    expect((alertVisible > 0 || hasHelpBlock > 0) && stillOnSignup).toBe(true);
  });

  test('TC_REGISTER_003 — Register with empty fields', async ({ page }) => {
    await page.goto('/index.php/signup', { timeout: 45_000 });

    // Submit empty form
    await page.getByRole('button', { name: 'Sign up' }).click();
    await page.waitForTimeout(1_000);

    // Should stay on signup with validation errors (span.help-block "This field is required.")
    expect(page.url()).toContain('signup');
    const helpBlocks = await page.locator('span.help-block').count();
    expect(helpBlocks).toBeGreaterThan(0);
  });

  test('TC_REGISTER_004 — Register with mismatched passwords', async ({ page }) => {
    await page.goto('/index.php/signup', { timeout: 45_000 });

    await fillSignupForm(page, {
      firstName: 'Test',
      lastName: 'User',
      company: 'TestCo',
      email: `test_${Date.now()}@test.com`,
      password: 'Test@1234',
      retypePassword: 'Test@5678',
    });

    await page.getByRole('button', { name: 'Sign up' }).click();
    await page.waitForTimeout(1_000);

    // Should show "Please enter the same value again." error
    expect(page.url()).toContain('signup');
    const helpBlocks = await page.locator('span.help-block').count();
    expect(helpBlocks).toBeGreaterThan(0);
  });
});
