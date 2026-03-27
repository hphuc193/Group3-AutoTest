import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model for the RISE Projects page.
 * URL: /index.php/projects/all_projects
 *
 * Actions: Add project (ajax modal), Edit project (ajax modal), Delete project (confirmation modal).
 */
export class ProjectsPage {
  readonly page: Page;
  readonly projectTable: Locator;
  readonly tableRows: Locator;

  constructor(page: Page) {
    this.page = page;
    this.projectTable = page.locator('table#project-table');
    this.tableRows = page.locator('table#project-table tbody tr');
  }

  /** Navigate to all projects list */
  async goto() {
    await this.page.goto('/index.php/projects/all_projects', { timeout: 45_000 });
    await this.projectTable.waitFor({ state: 'visible', timeout: 15_000 });
  }

  /** Get current row count */
  async getRowCount(): Promise<number> {
    return this.tableRows.count();
  }

  // --- Add Project ---

  /** Open the "Add project" modal */
  async openAddModal() {
    await this.page.locator('a[title="Add project"]').click();
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    // Wait for form to load inside modal
    await this.page.locator('#ajaxModal input#title').waitFor({ state: 'visible', timeout: 10_000 });
  }

  /** Fill add/edit project form fields */
  async fillProjectForm(data: {
    title?: string;
    projectType?: string;
    client?: string;
    description?: string;
    startDate?: string;
    deadline?: string;
    price?: string;
  }) {
    if (data.title !== undefined) {
      const titleInput = this.page.locator('#ajaxModal input#title');
      await titleInput.clear();
      await titleInput.fill(data.title);
    }
    if (data.projectType !== undefined) {
      await this.page.locator('#ajaxModal select#project_type').selectOption({ label: data.projectType });
    }
    if (data.client !== undefined) {
      // Client uses Select2 — container id is "s2id_project_client_id"
      await this.page.locator('#s2id_project_client_id .select2-choice').click();
      await this.page.waitForTimeout(500);
      // Type in the search box to filter, then pick the matching option
      await this.page.locator('.select2-drop-active input.select2-input').fill(data.client);
      await this.page.waitForTimeout(1_000);
      await this.page.locator('.select2-results .select2-result-label').filter({ hasText: data.client }).first().click();
    }
    if (data.description !== undefined) {
      // Summernote rich text editor — contenteditable div
      const editor = this.page.locator('div.note-editable[contenteditable="true"]');
      await editor.click();
      await editor.fill(data.description);
    }
    if (data.startDate !== undefined) {
      await this.page.locator('#ajaxModal input#start_date').evaluate(
        (el, val) => { (el as HTMLInputElement).value = val; }, data.startDate
      );
    }
    if (data.deadline !== undefined) {
      await this.page.locator('#ajaxModal input#deadline').evaluate(
        (el, val) => { (el as HTMLInputElement).value = val; }, data.deadline
      );
    }
    if (data.price !== undefined) {
      const priceInput = this.page.locator('#ajaxModal input#price');
      await priceInput.clear();
      await priceInput.fill(data.price);
    }
  }

  /** Click Save button (type="submit") in the add/edit modal */
  async clickSave() {
    await this.page.locator('#ajaxModal button[type="submit"].btn-primary').click();
    await this.page.waitForTimeout(2_000);
  }

  /** Click "Save & continue" button */
  async clickSaveAndContinue() {
    await this.page.locator('#ajaxModal').getByRole('button', { name: 'Save & continue' }).click();
    await this.page.waitForTimeout(2_000);
  }

  // --- Edit Project ---

  /** Click edit button on the first project row */
  async clickEditFirstProject() {
    await this.tableRows.first().locator('a.edit').click();
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await this.page.locator('#ajaxModal input#title').waitFor({ state: 'visible', timeout: 10_000 });
  }

  // --- Delete Project ---

  /** Click delete button on the first project row */
  async clickDeleteFirstProject() {
    await this.tableRows.first().locator('a.delete').click();
    // Wait for confirmation modal
    await this.page.locator('#confirmationModal').waitFor({ state: 'visible', timeout: 5_000 });
  }

  /** Confirm deletion by clicking "Delete" in the confirmation modal */
  async confirmDelete() {
    await this.page.locator('button#confirmDeleteButton').click();
    await this.page.waitForTimeout(2_000);
  }

  /** Cancel deletion by clicking "Cancel" in the confirmation modal */
  async cancelDelete() {
    await this.page.locator('#confirmationModal').getByRole('button', { name: 'Cancel' }).click();
    await this.page.waitForTimeout(1_000);
  }

  /** Check if modal is still open */
  async isModalVisible(): Promise<boolean> {
    return this.page.locator('#ajaxModal.show').isVisible();
  }

  /** Sort table by "Start date" ascending (newest without date appear first) */
  async sortByStartDateAsc() {
    const header = this.page.locator('th', { hasText: 'Start date' });
    await header.click();
    await this.page.waitForTimeout(1_500);
    // If already desc (dt-ordering-desc), click again to get asc
    const isAsc = await header.evaluate(el => el.classList.contains('dt-ordering-asc'));
    if (!isAsc) {
      await header.click();
      await this.page.waitForTimeout(1_500);
    }
  }

  // --- Search ---

  /** Type a keyword into the DataTable search box and wait for results */
  async search(keyword: string) {
    const searchInput = this.page.locator('input#dt-search-0[type="search"]');
    await searchInput.clear();
    await searchInput.fill(keyword);
    await this.page.waitForTimeout(2_000);
  }

  /** Check table shows "No matching records" or empty */
  async expectNoResults() {
    const emptyRow = this.page.locator('table#project-table tbody td.dataTables_empty, table#project-table tbody .dt-empty');
    const rowCount = await this.tableRows.count();
    // Either empty message or zero data rows
    const hasEmpty = await emptyRow.count();
    expect(hasEmpty > 0 || rowCount === 0).toBe(true);
  }

  // --- View Project ---

  /** Click on a project title link to open project detail page */
  async clickProjectTitle(title: string) {
    await this.page.locator('table#project-table tbody a').filter({ hasText: title }).first().click();
    await this.page.waitForTimeout(2_000);
  }

  /** Check that a project title exists in the table */
  async expectProjectVisible(title: string) {
    await expect(this.projectTable).toContainText(title, { timeout: 10_000 });
  }

  /** Check success alert message */
  async expectSuccessAlert(text: string) {
    await expect(this.page.locator('.app-alert-message')).toContainText(text, { timeout: 5_000 });
  }
}
