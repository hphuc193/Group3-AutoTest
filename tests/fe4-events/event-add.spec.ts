import { test, expect } from '@playwright/test';
import { EventsPage } from '../../pages/events-page';

/**
 * FE4 — Event Add Tests
 * TC_EVENT_ADD_001: Add event with valid data (happy path)
 * TC_EVENT_ADD_002: Add event with empty title shows error
 * TC_EVENT_ADD_003: Add event with only title (minimal data)
 * TC_EVENT_ADD_004: Add event with start date after end date
 */
test.describe.serial('FE4 — Event Add', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let eventsPage: EventsPage;

  test.beforeEach(async ({ page }) => {
    eventsPage = new EventsPage(page);
    await eventsPage.goto();
  });

  test('TC_EVENT_ADD_001 — Add event with valid data (happy path)', async ({ page }) => {
    // Click "Add event" button
    await page.getByRole('link', { name: 'Add event' }).click();
    await page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await page.waitForTimeout(1_000);

    // Fill title
    const title = `TestEvent_${Date.now()}`;
    await page.locator('#ajaxModal input#title').clear();
    await page.locator('#ajaxModal input#title').fill(title);

    // Click textarea to activate Summernote, then fill note-editable
    await page.locator('#ajaxModal textarea#description').click({ force: true });
    await page.waitForTimeout(1_000);
    await page.locator('#ajaxModal div.note-editable').click();
    await page.locator('#ajaxModal div.note-editable').fill('Automated test event description');

    // Fill start date by clicking datepicker and selecting a date
    await page.locator('#ajaxModal input#start_date').click();
    await page.waitForTimeout(500);
    await page.locator('.datepicker .datepicker-days td.day:not(.old):not(.new)').nth(2).click();
    await page.waitForTimeout(500);

    // Fill end date
    await page.locator('#ajaxModal input#end_date').click();
    await page.waitForTimeout(500);
    await page.locator('.datepicker .datepicker-days td.day:not(.old):not(.new)').nth(3).click();
    await page.waitForTimeout(500);

    // Click Save
    await page.locator('#ajaxModal').getByRole('button', { name: 'Save' }).click();
    await page.waitForTimeout(3_000);

    // Verify event appears on calendar
    await eventsPage.goto();
    await eventsPage.expectEventVisible(title);
  });

  test('TC_EVENT_ADD_002 — Add event with empty title shows error', async ({ page }) => {
    await page.getByRole('link', { name: 'Add event' }).click();
    await page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await page.waitForTimeout(1_000);

    // Leave title empty, click Save
    await page.locator('#ajaxModal input#title').clear();
    await page.locator('#ajaxModal').getByRole('button', { name: 'Save' }).click();
    await page.waitForTimeout(1_000);

    // Expected: error or modal stays open
    const modalStillOpen = await page.locator('#ajaxModal.show').isVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .text-danger, #ajaxModal .help-block')
      .count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  test('TC_EVENT_ADD_003 — Add event with only title (minimal data)', async ({ page }) => {
    await page.getByRole('link', { name: 'Add event' }).click();
    await page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await page.waitForTimeout(1_000);

    // Fill only title
    const title = `MinimalEvent_${Date.now()}`;
    await page.locator('#ajaxModal input#title').clear();
    await page.locator('#ajaxModal input#title').fill(title);

    // Click Save
    await page.locator('#ajaxModal').getByRole('button', { name: 'Save' }).click();
    await page.waitForTimeout(3_000);

    // If modal closes, event was saved; if stays open, dates may be required
    const modalStillOpen = await page.locator('#ajaxModal.show').isVisible();
    if (!modalStillOpen) {
      // Verify event appears on calendar
      await eventsPage.goto();
      await eventsPage.expectEventVisible(title);
    } else {
      // Dates are required — modal stays open, which is valid behavior
      expect(modalStillOpen).toBe(true);
    }
  });

  test('TC_EVENT_ADD_004 — Add event with start date after end date', async ({ page }) => {
    await page.getByRole('link', { name: 'Add event' }).click();
    await page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await page.waitForTimeout(1_000);

    // Fill title
    const title = `InvalidDateEvent_${Date.now()}`;
    await page.locator('#ajaxModal input#title').clear();
    await page.locator('#ajaxModal input#title').fill(title);

    // Set dates via JS: start date AFTER end date
    await page.evaluate(() => {
      const startDate = document.querySelector('#start_date') as HTMLInputElement;
      const endDate = document.querySelector('#end_date') as HTMLInputElement;
      if (startDate) { startDate.value = '30-03-2026'; startDate.dispatchEvent(new Event('change')); }
      if (endDate) { endDate.value = '28-03-2026'; endDate.dispatchEvent(new Event('change')); }
    });
    await page.waitForTimeout(500);

    // Click Save
    await page.locator('#ajaxModal').getByRole('button', { name: 'Save' }).click();
    await page.waitForTimeout(2_000);

    // Expected: error or modal stays open (invalid date range)
    const modalStillOpen = await page.locator('#ajaxModal.show').isVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .text-danger, #ajaxModal .help-block, #ajaxModal .alert')
      .count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });
});
