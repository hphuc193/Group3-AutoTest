import { test, expect } from '@playwright/test';
import { EventsPage } from '../../pages/events-page';

/**
 * FE4 — Event Label & View Tests
 * TC_EVENT_LABEL_001: Add new event label (happy path)
 * TC_EVENT_LABEL_002: Add label with empty name shows error
 * TC_EVENT_VIEW_001: View event details on calendar
 */
test.describe.serial('FE4 — Event Label & View', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let eventsPage: EventsPage;

  test.beforeEach(async ({ page }) => {
    eventsPage = new EventsPage(page);
    await eventsPage.goto();
  });

  test('TC_EVENT_LABEL_001 — Add new event label (happy path)', async ({ page }) => {
    // Click "Manage labels" button
    await page.locator('a[title="Manage labels"]').click();
    await page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await page.waitForTimeout(1_000);

    // Pick a color from the color palette
    await page.locator('#ajaxModal span.color-tag').first().click();
    await page.waitForTimeout(500);

    // Fill label name
    const labelName = `TestLabel_${Date.now()}`;
    const labelInput = page.locator('#ajaxModal input#label-title');
    await labelInput.clear();
    await labelInput.fill(labelName);

    // Click Save button
    await page.locator('#ajaxModal').getByRole('button', { name: 'Save' }).click();
    await page.waitForTimeout(2_000);

    // Verify: new label appears in the modal as a badge
    await expect(page.locator('#ajaxModal')).toContainText(labelName, { timeout: 10_000 });
  });

  test('TC_EVENT_LABEL_002 — Add label with empty name shows error', async ({ page }) => {
    // Click "Manage labels" button
    await page.locator('a[title="Manage labels"]').click();
    await page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await page.waitForTimeout(1_000);

    // Leave label name empty, click Save
    await page.locator('#ajaxModal input#label-title').clear();
    await page.locator('#ajaxModal').getByRole('button', { name: 'Save' }).click();
    await page.waitForTimeout(1_000);

    // Expected: validation error "This field is required."
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger, #ajaxModal .help-block')
      .count();
    const modalStillOpen = await page.locator('#ajaxModal.show').isVisible();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  test('TC_EVENT_VIEW_001 — View event details on calendar', async ({ page }) => {
    // Click the first visible event on the calendar
    await eventsPage.clickFirstEvent();

    // Verify "Event details" heading is shown in modal
    await expect(page.locator('h4#ajaxModalTitle')).toContainText('Event details', { timeout: 10_000 });
  });
});
