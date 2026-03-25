import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model for the RISE Clients page.
 * The Clients tab has a filter bar with buttons: "Has due", "Has open projects", etc.
 * There's also a "smart filter" dropdown ("All clients" button) for saved filters.
 */
export class ClientsPage {
  readonly page: Page;
  readonly tableRows: Locator;

  constructor(page: Page) {
    this.page = page;
    this.tableRows = page.locator('table tbody tr');
  }

  /** Navigate to the clients page and switch to the Clients list tab */
  async goto() {
    await this.page.goto('/index.php/clients', { timeout: 45_000 });
    // Click the "Clients" tab (data-bs-toggle tab)
    await this.page.locator('#client-tabs a[data-bs-target="#clients_list"]').click();
    // Wait for table to appear
    await this.page.locator('table').waitFor({ state: 'visible', timeout: 15_000 });
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
}
