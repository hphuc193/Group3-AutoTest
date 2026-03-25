import { type Page, expect } from '@playwright/test';

/**
 * Page Object Model for the RISE Events page (FullCalendar).
 *
 * Flow: Calendar → click event → "Event details" modal
 *   → "Delete event" / "Edit event" / "Close"
 * Edit modal: Title, Description, Start/End date, Color, Save/Close
 */
export class EventsPage {
  readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  /** Navigate to the events calendar */
  async goto() {
    await this.page.goto('/index.php/events', { timeout: 45_000 });
    // Wait for FullCalendar to render
    await this.page.locator('.fc-daygrid').waitFor({ state: 'visible', timeout: 15_000 });
  }

  /** Click the first visible event on the calendar */
  async clickFirstEvent() {
    await this.page.locator('a.fc-event').first().click();
    // Wait for the "Event details" modal to appear
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
  }

  /** Click a specific event by partial title text */
  async clickEventByTitle(title: string) {
    await this.page.locator('a.fc-event').filter({ hasText: title }).first().click();
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
  }

  /** Get the title shown in the Event details modal */
  async getModalEventTitle(): Promise<string> {
    return (await this.page.locator('#ajaxModal .modal-body').textContent()) || '';
  }

  // --- Event details modal actions ---

  /** Click "Delete event" link in the Event details modal */
  async clickDeleteEvent() {
    await this.page.getByRole('link', { name: 'Delete event' }).click();
  }

  /** Click "Edit event" link in the Event details modal */
  async clickEditEvent() {
    await this.page.getByRole('link', { name: 'Edit event' }).click();
    // Wait for the edit form to load inside the modal
    await this.page.locator('input#title').waitFor({ state: 'visible', timeout: 10_000 });
  }

  /** Click "Close" button in the modal footer */
  async clickCloseModal() {
    await this.page.locator('button.close-modal').click();
    await this.page.waitForTimeout(500);
  }

  // --- Confirmation popover (app-popover with "Are you sure?") ---

  /** Confirm deletion by clicking "Yes" in the popover */
  async confirmDelete() {
    await this.page.getByRole('button', { name: 'Yes' }).waitFor({ state: 'visible', timeout: 5_000 });
    await this.page.getByRole('button', { name: 'Yes' }).click();
    await this.page.waitForTimeout(2_000);
  }

  /** Cancel deletion by clicking "No" in the popover */
  async cancelDelete() {
    await this.page.getByRole('button', { name: 'No' }).waitFor({ state: 'visible', timeout: 5_000 });
    await this.page.getByRole('button', { name: 'No' }).click();
    await this.page.waitForTimeout(1_000);
  }

  // --- Edit event form ---

  /** Clear and fill the Title field */
  async fillTitle(title: string) {
    const titleInput = this.page.locator('input#title');
    await titleInput.clear();
    await titleInput.fill(title);
  }

  /** Clear and fill the Description field (Summernote rich text editor) */
  async fillDescription(text: string) {
    const editor = this.page.locator('div.note-editable');
    await editor.click();
    await editor.fill(text);
  }

  /** Click a color swatch by its data-color attribute (hex without #) */
  async selectColor(hexColor: string) {
    await this.page.locator(`span.color-tag[data-color="#${hexColor}"]`).click();
  }

  /** Click the Save button in the edit form */
  async clickSave() {
    await this.page.getByRole('button', { name: 'Save' }).click();
    await this.page.waitForTimeout(2_000);
  }

  /** Check if a specific event title exists on the calendar */
  async expectEventVisible(title: string) {
    await expect(this.page.locator('a.fc-event').filter({ hasText: title }).first())
      .toBeVisible({ timeout: 10_000 });
  }

  /** Check that a specific event title does NOT exist on the calendar */
  async expectEventNotVisible(title: string) {
    await expect(this.page.locator('a.fc-event').filter({ hasText: title }))
      .toHaveCount(0, { timeout: 10_000 });
  }

  /** Check that the modal is closed */
  async expectModalClosed() {
    await expect(this.page.locator('#ajaxModal.show')).toBeHidden({ timeout: 5_000 });
  }
}
