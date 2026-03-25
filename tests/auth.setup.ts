import { test as setup, expect } from '@playwright/test';
import * as fs from 'fs';

const ADMIN_AUTH_FILE = '.auth/admin.json';
const CLIENT_AUTH_FILE = '.auth/client.json';

setup.use({ actionTimeout: 0 });

/**
 * Auth setup — opens the login page in headed mode.
 * The user must manually solve reCAPTCHA.
 * Skips if auth state files already exist.
 *
 * To regenerate: delete .auth/*.json and run with --headed
 */
setup('authenticate as admin', async ({ browser }) => {
  if (fs.existsSync(ADMIN_AUTH_FILE)) {
    console.log('Admin auth state already exists, skipping.');
    return;
  }
  setup.setTimeout(120_000);

  const context = await browser.newContext();
  const page = await context.newPage();
  await page.goto('https://rise.fairsketch.com/index.php/signin');

  console.log('\n>>> Please login as ADMIN (solve reCAPTCHA manually) <<<\n');
  await expect(page).toHaveURL(/dashboard/i, { timeout: 120_000 });

  await context.storageState({ path: ADMIN_AUTH_FILE });
  await context.close();
  console.log('Admin auth state saved!');
});

setup('authenticate as client', async ({ browser }) => {
  if (fs.existsSync(CLIENT_AUTH_FILE)) {
    console.log('Client auth state already exists, skipping.');
    return;
  }
  setup.setTimeout(120_000);

  const context = await browser.newContext();
  const page = await context.newPage();
  await page.goto('https://rise.fairsketch.com/index.php/signin');

  console.log('\n>>> Please login as CLIENT (solve reCAPTCHA manually) <<<\n');
  await expect(page).toHaveURL(/dashboard/i, { timeout: 120_000 });

  await context.storageState({ path: CLIENT_AUTH_FILE });
  await context.close();
  console.log('Client auth state saved!');
});
