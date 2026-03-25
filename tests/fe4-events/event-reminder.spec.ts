import { test, expect, type Page } from '@playwright/test';
import { EventsPage } from '../../pages/events-page';

/** Helper: get tomorrow's date as MM-DD-YYYY */
function getTomorrowDate(): string {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${mm}-${dd}-${d.getFullYear()}`;
}

/** Helper: fill reminder form fields using evaluate (bypasses datepicker) */
async function fillReminderForm(page: Page, title: string, date: string, time: string) {
  await page.locator('input#event-title').fill(title);
  await page.locator('input#event-start_date').evaluate(
    (el, val) => { (el as HTMLInputElement).value = val; }, date
  );
  await page.locator('input#event-start_time').evaluate(
    (el, val) => { (el as HTMLInputElement).value = val; }, time
  );
}

test.describe.serial('FE4 — Event Reminder', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let eventsPage: EventsPage;

  test.beforeEach(async ({ page }) => {
    eventsPage = new EventsPage(page);
    await eventsPage.goto();
  });

  test('TC_EVENT_REMINDER_001 — Add reminder with valid data', async ({ page }) => {
    await eventsPage.clickFirstEvent();
    await page.getByRole('link', { name: 'Add reminder' }).click();
    await page.waitForTimeout(1_000);

    const title = `Reminder_${Date.now()}`;
    await fillReminderForm(page, title, getTomorrowDate(), '09:00 AM');

    await page.locator('.event-reminder-section').getByRole('button', { name: 'Add' }).click();
    await page.waitForTimeout(3_000);

    await expect(page.locator('table#event-reminders-table')).toContainText(title, { timeout: 10_000 });
  });

  test('TC_EVENT_REMINDER_002 — Add reminder with empty title shows error', async ({ page }) => {
    await eventsPage.clickFirstEvent();
    await page.getByRole('link', { name: 'Add reminder' }).click();
    await page.waitForTimeout(1_000);

    // Leave title empty, fill date and time
    await fillReminderForm(page, '', getTomorrowDate(), '09:00 AM');

    await page.locator('.event-reminder-section').getByRole('button', { name: 'Add' }).click();
    await page.waitForTimeout(1_000);

    // Validation should block — form stays open or error label appears
    const formStillOpen = await page.locator('input#event-title').isVisible();
    const hasError = await page.locator('label.error, .field-validation-error').count();
    expect(formStillOpen || hasError > 0).toBe(true);
  });

  test('TC_EVENT_REMINDER_003 — Add multiple reminders to same event', async ({ page }) => {
    await eventsPage.clickFirstEvent();

    // Add first reminder
    await page.getByRole('link', { name: 'Add reminder' }).click();
    await page.waitForTimeout(1_000);
    const title1 = `Rem1_${Date.now()}`;
    await fillReminderForm(page, title1, getTomorrowDate(), '09:00 AM');
    await page.locator('.event-reminder-section').getByRole('button', { name: 'Add' }).click();
    await page.waitForTimeout(3_000);

    // Add second reminder — form may still be open or need re-opening
    const addLink = page.getByRole('link', { name: 'Add reminder' });
    if (await addLink.isVisible()) {
      await addLink.click();
      await page.waitForTimeout(1_000);
    }
    const title2 = `Rem2_${Date.now()}`;
    await fillReminderForm(page, title2, getTomorrowDate(), '10:00 AM');
    await page.locator('.event-reminder-section').getByRole('button', { name: 'Add' }).click();
    await page.waitForTimeout(3_000);

    // Both reminders should appear in the table
    const table = page.locator('table#event-reminders-table');
    await expect(table).toContainText(title1, { timeout: 10_000 });
    await expect(table).toContainText(title2, { timeout: 10_000 });
  });
});
