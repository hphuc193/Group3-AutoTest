import { chromium } from '@playwright/test';

/**
 * Opens a headed browser for manual login, then saves the authenticated
 * session (cookies + localStorage) to .auth/admin.json and .auth/client.json.
 *
 * Usage: npx playwright test utils/generate-auth-state.ts --headed
 * Or:    npx tsx utils/generate-auth-state.ts
 */
async function generateAuthState() {
  const browser = await chromium.launch({ headless: false });

  // --- Admin auth ---
  console.log('=== Login as ADMIN ===');
  console.log('A browser will open. Please login as Admin and solve reCAPTCHA.');
  console.log('The script will continue once you reach the dashboard.\n');

  const adminContext = await browser.newContext();
  const adminPage = await adminContext.newPage();
  await adminPage.goto('https://rise.fairsketch.com/index.php/signin');

  // Wait for user to manually login and reach dashboard
  await adminPage.waitForURL('**/dashboard**', { timeout: 120_000 });
  console.log('Admin login detected! Saving state...');
  await adminContext.storageState({ path: '.auth/admin.json' });
  await adminContext.close();

  // --- Client auth ---
  console.log('\n=== Login as CLIENT ===');
  console.log('Please login as Client and solve reCAPTCHA.\n');

  const clientContext = await browser.newContext();
  const clientPage = await clientContext.newPage();
  await clientPage.goto('https://rise.fairsketch.com/index.php/signin');

  await clientPage.waitForURL('**/dashboard**', { timeout: 120_000 });
  console.log('Client login detected! Saving state...');
  await clientContext.storageState({ path: '.auth/client.json' });
  await clientContext.close();

  await browser.close();
  console.log('\nDone! Auth states saved to .auth/admin.json and .auth/client.json');
}

generateAuthState().catch(console.error);
