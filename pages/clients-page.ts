import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model for the RISE Clients page.
 * The Clients tab has a filter bar with buttons: "Has due", "Has open projects", etc.
 * There's also a "smart filter" dropdown ("All clients" button) for saved filters.
 */
export class ClientsPage {
  readonly page: Page;
  readonly clientTable: Locator;
  readonly tableRows: Locator;

  constructor(page: Page) {
    this.page = page;
    this.clientTable = page.locator('table#client-table');
    this.tableRows = page.locator('table#client-table tbody tr');
  }

  /** Navigate to the clients page and switch to the Clients list tab */
  async goto() {
    await this.page.goto('/index.php/clients', { timeout: 45_000 });
    // Click the "Clients" tab (data-bs-toggle tab)
    await this.page.locator('#client-tabs a[data-bs-target="#clients_list"]').click();
    // Wait for table to appear
    await this.clientTable.waitFor({ state: 'visible', timeout: 15_000 });
  }

  /** Sort by ID descending to see newest clients first */
  async sortByIdDesc() {
    const idHeader = this.page.locator('table#client-table thead th').first();
    // Click until aria-sort="descending" (arrow down icon)
    for (let i = 0; i < 3; i++) {
      const sortDir = await idHeader.getAttribute('aria-sort');
      if (sortDir === 'descending') break;
      await idHeader.click();
      await this.page.waitForTimeout(1_500);
    }
  }

  /** Click a bookmarked filter button by name (e.g. "Has due", "Has open projects") */
  async applyFilter(filterName: string) {
    // Target the bookmarked filter button specifically (not the dropdown toggle)
    await this.page.locator('button.bookmarked-filter-button').filter({ hasText: filterName }).click();
    await this.page.waitForTimeout(2_000);
  }

  /** Clear filter by opening the dropdown and clicking "All clients" */
  async clearFilters() {
    // The dropdown toggle button shows the active filter name (e.g. "Has open projects")
    // It's the first button with [aria-expanded] inside the filter section
    await this.page.locator('button[data-bs-toggle="dropdown"][aria-expanded]').first().click();
    await this.page.waitForTimeout(500);
    // Click "All clients" link to reset
    await this.page.getByText('All clients').click();
    await this.page.waitForTimeout(2_000);
  }

  /** Get current table row count */
  async getRowCount(): Promise<number> {
    return this.tableRows.count();
  }

  /** Assert table has rows */
  async expectTableHasRows() {
    const count = await this.getRowCount();
    expect(count).toBeGreaterThan(0);
  }

  // --- Add Client ---

  /** Open the "Add client" modal */
  async openAddModal() {
    await this.page.locator('a[title="Add client"]').click();
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await this.page.locator('#ajaxModal input#company_name').waitFor({ state: 'visible', timeout: 10_000 });
  }

  /** Fill add/edit client form fields */
  async fillClientForm(data: {
    companyName?: string;
    address?: string;
    city?: string;
  }) {
    if (data.companyName !== undefined) {
      const nameInput = this.page.locator('#ajaxModal input#company_name');
      await nameInput.clear();
      await nameInput.fill(data.companyName);
    }
    if (data.address !== undefined) {
      const addressInput = this.page.locator('#ajaxModal textarea#address, #ajaxModal input#address');
      await addressInput.clear();
      await addressInput.fill(data.address);
    }
    if (data.city !== undefined) {
      const cityInput = this.page.locator('#ajaxModal input#city');
      await cityInput.clear();
      await cityInput.fill(data.city);
    }
  }

  /** Select a manager from the Managers Select2 multi-select */
  async selectManager(name: string) {
    await this.page.locator('#s2id_managers .select2-search-field input').click();
    await this.page.waitForTimeout(1_000);
    await this.page.locator('.select2-drop-active .select2-results .select2-result-label')
      .filter({ hasText: name }).first().click();
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

  /** Check that a client name exists in the table */
  async expectClientVisible(name: string) {
    await expect(this.clientTable).toContainText(name, { timeout: 10_000 });
  }

  // --- Edit Client ---

  /** Click edit button on the first client row */
  async clickEditFirstClient() {
    await this.tableRows.first().locator('a.edit').click();
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await this.page.locator('#ajaxModal input#company_name').waitFor({ state: 'visible', timeout: 10_000 });
  }

  // --- Delete Client ---

  /** Click delete button on the first client row */
  async clickDeleteFirstClient() {
    await this.tableRows.first().locator('a.delete').click();
    await this.page.locator('#confirmationModal').waitFor({ state: 'visible', timeout: 5_000 });
  }

  /** Confirm deletion */
  async confirmDelete() {
    await this.page.locator('button#confirmDeleteButton').click();
    await this.page.waitForTimeout(2_000);
  }

  /** Cancel deletion */
  async cancelDelete() {
    await this.page.locator('#confirmationModal').getByRole('button', { name: 'Cancel' }).click();
    await this.page.waitForTimeout(1_000);
  }
}
