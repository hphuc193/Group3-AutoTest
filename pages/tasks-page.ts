import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model for the RISE Tasks page.
 * URL: /index.php/tasks/all_tasks
 * Uses DataTable with Select2 dropdowns for Assign to & Status.
 */
export class TasksPage {
  readonly page: Page;
  readonly taskTable: Locator;
  readonly tableRows: Locator;

  constructor(page: Page) {
    this.page = page;
    this.taskTable = page.locator('table#task-table');
    this.tableRows = page.locator('table#task-table tbody tr');
  }

  /** Navigate to the tasks list page */
  async goto() {
    await this.page.goto('/index.php/tasks/all_tasks', { timeout: 45_000 });
    await this.taskTable.waitFor({ state: 'visible', timeout: 15_000 });
    await this.page.waitForTimeout(2_000);
  }

  /** Sort by ID descending (click until arrow down icon appears) */
  async sortByIdDesc() {
    const idHeader = this.page.locator('table#task-table thead th').first();
    for (let i = 0; i < 3; i++) {
      const sortDir = await idHeader.getAttribute('aria-sort');
      if (sortDir === 'descending') break;
      await idHeader.click();
      await this.page.waitForTimeout(1_500);
    }
  }

  /** Get current table row count */
  async getRowCount(): Promise<number> {
    return this.tableRows.count();
  }

  // --- Add Task ---

  /** Open the "Add task" modal */
  async openAddModal() {
    await this.page.getByRole('link', { name: 'Add task' }).click();
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await this.page.waitForTimeout(1_000);
  }

  /** Fill the Title field */
  async fillTitle(title: string) {
    const input = this.page.locator('#ajaxModal input#title');
    await input.clear();
    await input.fill(title);
  }

  /** Fill the Description field (clicks textarea to activate Summernote, then fills) */
  async fillDescription(desc: string) {
    // Click the textarea first to activate the Summernote rich text editor
    await this.page.locator('#ajaxModal textarea#description').click({ force: true });
    await this.page.waitForTimeout(1_000);
    // Now fill the Summernote editable div
    const editor = this.page.locator('#ajaxModal div.note-editable');
    await editor.click();
    await editor.fill(desc);
  }

  /** Select "Assign to" from Select2 dropdown by visible text */
  async selectAssignTo(name: string) {
    await this.page.locator('#s2id_assigned_to .select2-choice').click();
    await this.page.waitForTimeout(1_000);
    await this.page.locator('.select2-drop-active .select2-results .select2-result-label')
      .filter({ hasText: name }).first().click();
    await this.page.waitForTimeout(500);
  }

  /** Select "Status" from Select2 dropdown by visible text */
  async selectStatus(status: string) {
    await this.page.locator('#s2id_task_status_id .select2-choice').click();
    await this.page.waitForTimeout(1_000);
    await this.page.locator('.select2-drop-active .select2-results .select2-result-label')
      .filter({ hasText: status }).first().click();
    await this.page.waitForTimeout(500);
  }

  /** Click Save button in the modal */
  async clickSave() {
    await this.page.locator('#ajaxModal button[type="submit"].btn-primary').click();
    await this.page.waitForTimeout(3_000);
  }

  /** Check if modal is still open */
  async isModalVisible(): Promise<boolean> {
    return this.page.locator('#ajaxModal.show').isVisible();
  }

  /** Assert task title exists in the table */
  async expectTaskVisible(title: string) {
    await expect(this.taskTable).toContainText(title, { timeout: 10_000 });
  }

  // --- Edit Task ---

  /** Click edit button on the first task row */
  async clickEditFirstTask() {
    await this.tableRows.first().locator('a.edit').click();
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await this.page.waitForTimeout(1_000);
  }

  /** Get the current value of the Title input in the modal */
  async getModalTitle(): Promise<string> {
    return this.page.locator('#ajaxModal input#title').inputValue();
  }
}
