import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model for RISE Contacts (within a Client detail page).
 * Contacts are in div#client-view-contacts-section on /index.php/clients/view/{clientId}
 * Contact profile: /index.php/clients/contact_profile/{contactId}
 */
export class ContactsPage {
  readonly page: Page;
  readonly contactsSection: Locator;
  readonly contactsTable: Locator;

  constructor(page: Page) {
    this.page = page;
    this.contactsSection = page.locator('#client-view-contacts-section');
    this.contactsTable = page.locator('table#client-details-page-contact-table');
  }

  /** Navigate to a specific client's detail page */
  async gotoClient(clientId: number) {
    await this.page.goto(`/index.php/clients/view/${clientId}`, { timeout: 45_000 });
    await this.page.waitForTimeout(2_000);
  }

  /** Navigate to clients list, sort by ID desc, click the latest client */
  async gotoLatestClient() {
    await this.page.goto('/index.php/clients', { timeout: 45_000 });
    // Click the "Clients" tab
    await this.page.locator('#client-tabs a[data-bs-target="#clients_list"]').click();
    await this.page.locator('table#client-table').waitFor({ state: 'visible', timeout: 15_000 });

    // Sort by ID descending — click the ID column header (th[data-dt-column="0"])
    const idHeader = this.page.locator('table#client-table thead th[data-dt-column="0"]');
    for (let i = 0; i < 3; i++) {
      const sortDir = await idHeader.getAttribute('aria-sort');
      if (sortDir === 'descending') break;
      await idHeader.click();
      await this.page.waitForTimeout(2_000);
    }

    // Click on the first client's name link (second column)
    await this.page.locator('table#client-table tbody tr').first()
      .locator('td').nth(1).locator('a').first().click();
    await this.page.waitForTimeout(2_000);
  }

  /** Open the "Add contact" modal */
  async openAddContactModal() {
    await this.page.locator('a[title="Add contact"]').click();
    await this.page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await this.page.locator('#ajaxModal input#first_name').waitFor({ state: 'visible', timeout: 10_000 });
  }

  /** Fill the add contact modal form (form#contact-form) */
  async fillContactForm(data: {
    firstName?: string;
    lastName?: string;
    email?: string;
    phone?: string;
    jobTitle?: string;
  }) {
    if (data.firstName !== undefined) {
      const input = this.page.locator('#ajaxModal input#first_name');
      await input.clear();
      await input.fill(data.firstName);
    }
    if (data.lastName !== undefined) {
      const input = this.page.locator('#ajaxModal input#last_name');
      await input.clear();
      await input.fill(data.lastName);
    }
    if (data.email !== undefined) {
      const input = this.page.locator('#ajaxModal input#email');
      await input.clear();
      await input.fill(data.email);
    }
    if (data.phone !== undefined) {
      const input = this.page.locator('#ajaxModal input#phone');
      await input.clear();
      await input.fill(data.phone);
    }
    if (data.jobTitle !== undefined) {
      const input = this.page.locator('#ajaxModal input#job_title');
      await input.clear();
      await input.fill(data.jobTitle);
    }
  }

  /** Click Save button in the add-contact modal */
  async clickSave() {
    // Scroll the modal to make Save button visible, then click
    const saveBtn = this.page.locator('#ajaxModal button[type="submit"]');
    await saveBtn.scrollIntoViewIfNeeded();
    await saveBtn.click();
    await this.page.waitForTimeout(3_000);
  }

  /** Check if modal is still open */
  async isModalVisible(): Promise<boolean> {
    return this.page.locator('#ajaxModal.show').isVisible();
  }

  /** Verify a contact name appears in the contacts section */
  async expectContactVisible(name: string) {
    await expect(this.contactsSection).toContainText(name, { timeout: 10_000 });
  }

  /** Click on the first contact link in the contacts section */
  async clickFirstContact() {
    await this.contactsSection.locator('a[href*="contact_profile"]').first().click();
    await this.page.waitForTimeout(3_000);
  }

  // --- Contact Profile Page (/index.php/clients/contact_profile/{id}) ---

  /** Click "Account settings" tab on contact profile page */
  async clickAccountSettingsTab() {
    await this.page.locator('#client-contact-tabs a').filter({ hasText: 'Account settings' }).click();
    await this.page.waitForTimeout(2_000);
  }

  /** Fill general info fields on the contact profile page (General Info tab) */
  async fillGeneralInfo(data: {
    firstName?: string;
    lastName?: string;
    phone?: string;
    jobTitle?: string;
  }) {
    if (data.firstName !== undefined) {
      const input = this.page.locator('input#first_name');
      await input.clear();
      await input.fill(data.firstName);
    }
    if (data.lastName !== undefined) {
      const input = this.page.locator('input#last_name');
      await input.clear();
      await input.fill(data.lastName);
    }
    if (data.phone !== undefined) {
      const input = this.page.locator('input#phone');
      await input.clear();
      await input.fill(data.phone);
    }
    if (data.jobTitle !== undefined) {
      const input = this.page.locator('input#job_title');
      await input.clear();
      await input.fill(data.jobTitle);
    }
  }

  /** Clear the email field in Account Settings tab */
  async clearEmail() {
    const input = this.page.locator('form#account-info-form input#email');
    await input.click();
    await this.page.waitForTimeout(500);
    // Select all text and delete it
    await input.press('Control+a');
    await input.press('Meta+a');
    await input.press('Backspace');
    await this.page.waitForTimeout(500);
  }

  /** Click Save on the contact profile page (scrolls to the visible Save button) */
  async clickSaveOnDetailPage() {
    const saveBtn = this.page.locator('form#account-info-form button[type="submit"].btn-primary, form#general-form button[type="submit"].btn-primary').first();
    await saveBtn.scrollIntoViewIfNeeded();
    await saveBtn.click();
    await this.page.waitForTimeout(3_000);
  }
}
