import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model for the RISE Leads page.
 * URL: /index.php/leads
 *
 * Actions: Add lead (ajax modal), Edit lead (ajax modal), Delete lead (confirmation modal).
 */
export class LeadsPage {
  readonly page: Page;
  readonly leadsTable: Locator;
  readonly tableRows: Locator;

  constructor(page: Page) {
    this.page = page;
    this.leadsTable = page.locator('table#lead-table');
    this.tableRows = page.locator('table#lead-table tbody tr');
  }

  /** Navigate to leads list */
  async goto() {
    await this.page.goto('/index.php/leads', { timeout: 45_000 });
    // Click Leads tab to ensure list view
    const leadsTab = this.page.locator('li.js-leads-cookie-tab[data-tab="leads_list"] a, [data-tab="leads_list"]');
    if (await leadsTab.count() > 0) {
      await leadsTab.first().click();
    }
    await this.page.waitForTimeout(2_000);
  }

  /** Get current row count */
  async getRowCount(): Promise<number> {
    return this.tableRows.count();
  }

  // --- Add Lead ---

  /** Open the "Add lead" modal */
  async openAddModal() {
    await this.page.locator('a[data-title="Add lead"], a[title="Add lead"]').first().click();
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    // Wait for form to load
    await this.page.locator('#ajaxModal input#company_name').waitFor({ state: 'visible', timeout: 10_000 });
  }

  /** Fill add/edit lead form fields */
  async fillLeadForm(data: {
    companyName?: string;
    status?: string;
    owner?: string;
    source?: string;
    address?: string;
    city?: string;
    phone?: string;
  }) {
    if (data.companyName !== undefined) {
      const nameInput = this.page.locator('#ajaxModal input#company_name');
      await nameInput.clear();
      await nameInput.fill(data.companyName);
    }
    if (data.status !== undefined) {
      // Status is a Select2 dropdown (options: New, Qualified, Discussion, Negotiation, Won, Lost)
      await this.page.locator('#ajaxModal label[for="lead_status_id"]').locator('..').locator('..').locator('.select2-choice').click();
      await this.page.waitForTimeout(500);
      await this.page.locator('.select2-drop-active .select2-results .select2-result-label')
        .filter({ hasText: data.status }).first().click();
      await this.page.waitForTimeout(300);
    }
    if (data.owner !== undefined) {
      // Owner is a Select2 dropdown — container id "s2id_owner_id"
      await this.page.locator('#s2id_owner_id .select2-choice').click();
      await this.page.waitForTimeout(500);
      await this.page.locator('.select2-drop-active .select2-results .select2-result-label')
        .filter({ hasText: data.owner }).first().click();
      await this.page.waitForTimeout(500);
    }
    if (data.source !== undefined) {
      // Source is a Select2 dropdown
      await this.page.locator('#ajaxModal label[for="lead_source_id"]').locator('..').locator('.select2-choice').click();
      await this.page.waitForTimeout(500);
      await this.page.locator('.select2-drop-active .select2-results .select2-result-label')
        .filter({ hasText: data.source }).first().click();
      await this.page.waitForTimeout(500);
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
    if (data.phone !== undefined) {
      const phoneInput = this.page.locator('#ajaxModal input#phone');
      await phoneInput.clear();
      await phoneInput.fill(data.phone);
    }
  }

  /** Clear the Owner dropdown via the Select2 close button */
  async clearOwner() {
    const closeBtn = this.page.locator('#s2id_owner_id .select2-search-choice-close');
    if (await closeBtn.count() > 0) {
      await closeBtn.click();
      await this.page.waitForTimeout(500);
    }
  }

  /** Click Save button in the modal */
  async clickSave() {
    await this.page.locator('#ajaxModal button[type="submit"].btn-primary').click();
    await this.page.waitForTimeout(2_000);
  }

  // --- Edit Lead ---

  /** Click edit button on the first lead row */
  async clickEditFirstLead() {
    await this.tableRows.first().locator('a.edit').click();
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await this.page.locator('#ajaxModal input#company_name').waitFor({ state: 'visible', timeout: 10_000 });
  }

  // --- Delete Lead ---

  /** Click delete button on the first lead row */
  async clickDeleteFirstLead() {
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

  /** Check if modal is still open */
  async isModalVisible(): Promise<boolean> {
    return this.page.locator('#ajaxModal.show').isVisible();
  }

  /** Check that a lead name exists in the leads table */
  async expectLeadVisible(name: string) {
    await expect(this.page.locator('table#lead-table')).toContainText(name, { timeout: 10_000 });
  }
}
