import { test, expect } from '@playwright/test';
import { EventsPage } from '../../pages/events-page';

/**
 * FE4 — Event Edit & Delete Tests
 * Tests use existing events on the demo calendar (resets every 6h).
 * Run sequentially to avoid session conflicts.
 */
test.describe.serial('FE4 — Event Edit & Delete', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let eventsPage: EventsPage;

  test.beforeEach(async ({ page }) => {
    eventsPage = new EventsPage(page);
    await eventsPage.goto();
  });

  test('TC_EVENT_EDIT_001 — Edit event title and description', async ({ page }) => {
    // Click any existing event on the calendar
    await eventsPage.clickFirstEvent();

    // Open the edit form
    await eventsPage.clickEditEvent();

    // Update title with dynamic data
    const newTitle = `TestEvent_Updated_${Date.now()}`;
    await eventsPage.fillTitle(newTitle);
    await eventsPage.fillDescription('Updated description for testing');

    // Save changes
    await eventsPage.clickSave();

    // Verify the updated event appears on the calendar
    await eventsPage.goto();
    await eventsPage.expectEventVisible(newTitle);
  });

  test('TC_EVENT_EDIT_002 — Edit event with empty title shows error', async ({ page }) => {
    await eventsPage.clickFirstEvent();
    await eventsPage.clickEditEvent();

    // Clear the title field
    await eventsPage.fillTitle('');

    // Try to save
    await eventsPage.clickSave();

    // jQuery validation shows a label.error next to the field, or the form stays open
    // The form should still be visible (save rejected)
    await expect(page.locator('input#title')).toBeVisible();

    // Check for jQuery validation error label or the form not closing
    const hasError = await page.locator('label.error, .form-error, .text-danger, .field-validation-error')
      .filter({ hasText: /required|field/i }).count();
    // Even if no visible error label, the form staying open proves validation blocked the save
    const modalStillOpen = await page.locator('#ajaxModal.show').isVisible();
    expect(hasError > 0 || modalStillOpen).toBe(true);
  });

  test('TC_EVENT_EDIT_003 — Edit event color', async ({ page }) => {
    await eventsPage.clickFirstEvent();
    await eventsPage.clickEditEvent();

    // Scroll the edit form to reveal the color palette at the bottom
    await page.locator('#ajaxModal .modal-body').evaluate(el => el.scrollTop = el.scrollHeight);
    await page.waitForTimeout(500);

    // Target event color swatches in the .color-palet div (not theme colors)
    const colorTags = page.locator('.color-palet span.color-tag:not(.active)');
    const count = await colorTags.count();
    expect(count).toBeGreaterThan(0);
    const targetColor = await colorTags.first().getAttribute('data-color');
    await colorTags.first().click({ force: true });

    // Save
    await eventsPage.clickSave();

    // Verify event still exists on calendar after color change
    await eventsPage.goto();
    const firstEvent = page.locator('a.fc-event').first();
    await expect(firstEvent).toBeVisible();
    // The event color on calendar should match the selected color
    if (targetColor) {
      const eventStyle = await firstEvent.locator('span').first().getAttribute('style');
      expect(eventStyle).toContain(targetColor);
    }
  });

  test('TC_EVENT_DEL_002 — Cancel delete keeps event', async ({ page }) => {
    // Wait for events to load on calendar
    await page.locator('a.fc-event').first().waitFor({ state: 'visible', timeout: 10_000 });
    const eventsBefore = await page.locator('a.fc-event').count();

    await eventsPage.clickFirstEvent();
    await eventsPage.clickDeleteEvent();

    // Cancel the confirmation — click "No"
    await eventsPage.cancelDelete();

    // Close the event details modal
    await eventsPage.clickCloseModal();

    // Events should still be there (no deletion alert)
    await page.waitForTimeout(2_000);
    const eventsAfter = await page.locator('a.fc-event').count();
    expect(eventsAfter).toBe(eventsBefore);
  });

  test('TC_EVENT_DEL_001 — Delete event successfully', async ({ page }) => {
    await eventsPage.clickFirstEvent();
    await eventsPage.clickDeleteEvent();
    await eventsPage.confirmDelete();

    // Verify success alert "The event has been deleted."
    await expect(page.locator('.app-alert-message'))
      .toContainText('deleted', { timeout: 5_000 });
  });
});
