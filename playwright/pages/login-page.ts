import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model for the RISE login page.
 * Auth is handled via stored session state (see auth.setup.ts).
 * This page object is used for UI interaction and negative test assertions.
 */
export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly signInButton: Locator;
  readonly signUpLink: Locator;
  readonly forgotPasswordLink: Locator;
  readonly alertMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.getByRole('textbox', { name: 'Email' });
    this.passwordInput = page.getByRole('textbox', { name: 'Password' });
    this.signInButton = page.getByRole('button', { name: /sign in/i });
    this.signUpLink = page.getByRole('link', { name: /sign up/i });
    this.forgotPasswordLink = page.getByRole('link', { name: /forgot password/i });
    this.alertMessage = page.locator('[role="alert"], .alert');
  }

  /** Navigate to the login page */
  async goto() {
    await this.page.goto('/', { waitUntil: 'domcontentloaded' });
    await this.emailInput.waitFor({ state: 'visible', timeout: 15_000 });
  }

  /** Fill email and password, then click Sign In */
  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.signInButton.click();
  }

  /**
   * Wait for user to solve reCAPTCHA manually (up to 60s).
   * Polls the g-recaptcha-response textarea — it gets a value when solved.
   */
  async waitForRecaptchaSolved(timeoutMs = 120_000) {
    await this.page.waitForFunction(
      () => {
        const textarea = document.querySelector('textarea[name="g-recaptcha-response"]');
        return textarea && (textarea as HTMLTextAreaElement).value.length > 0;
      },
      { timeout: timeoutMs }
    );
  }

  /**
   * Full login flow with reCAPTCHA: fill credentials, wait for manual
   * reCAPTCHA solving, then auto-click Sign In.
   */
  async loginWithRecaptcha(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    // Wait for user to manually solve reCAPTCHA
    await this.waitForRecaptchaSolved();
    // reCAPTCHA solved — click Sign In
    await this.signInButton.click();
  }

  /** Submit the form without filling any fields */
  async submitEmpty() {
    await this.signInButton.click();
  }

  /** Assert that we landed on the dashboard after login */
  async expectDashboard() {
    await expect(this.page).toHaveURL(/dashboard/i, { timeout: 15_000 });
  }

  /** Assert that an alert/error message is visible */
  async expectAlertVisible() {
    await expect(this.alertMessage.first()).toBeVisible({ timeout: 5_000 });
  }

  /** Assert alert contains specific text */
  async expectAlertContains(text: string | RegExp) {
    await expect(this.alertMessage.first()).toContainText(text, { timeout: 5_000 });
  }

  /** Assert we are still on the login page (login did not succeed) */
  async expectStillOnLoginPage() {
    await this.page.waitForTimeout(1_500);
    expect(this.page.url()).toContain('signin');
  }
}
