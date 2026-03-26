import { test, expect } from '@playwright/test';
import { ClientsPage } from '../../pages/clients-page';

/**
 * FE2 — Client Add, Edit & Delete Tests
 * TC_CLIENT_ADD_001: Add client with valid data
 * TC_CLIENT_ADD_002: Add client with empty company name
 * TC_CLIENT_ADD_003: Add client with duplicate name
 * TC_CLIENT_EDIT_001: Edit client info
 * TC_CLIENT_EDIT_002: Edit client with empty company name
 * TC_CLIENT_DEL_001: Delete client (confirm)
 * TC_CLIENT_DEL_002: Delete client (cancel)
 */
test.describe.serial('FE2 — Client Add, Edit & Delete', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let clientsPage: ClientsPage;

  test.beforeEach(async ({ page }) => {
    clientsPage = new ClientsPage(page);
    await clientsPage.goto();
  });

  // --- Add Client ---

  test('TC_CLIENT_ADD_001 — Add client with valid data (happy path)', async () => {
    await clientsPage.openAddModal();

    const name = `TestClient_${Date.now()}`;
    await clientsPage.fillClientForm({
      companyName: name,
      address: '123 Test Street',
    });
    await clientsPage.selectManager('John Doe');
    await clientsPage.clickSave();

    // Verify: navigate to clients, sort by ID desc, check new client visible
    await clientsPage.goto();
    await clientsPage.sortByIdDesc();
    await clientsPage.expectClientVisible(name);
  });

  test('TC_CLIENT_ADD_002 — Add client with empty company name shows error', async ({ page }) => {
    await clientsPage.openAddModal();

    await clientsPage.fillClientForm({ companyName: '' });
    await clientsPage.clickSave();

    // Expected: error or modal stays open
    const modalStillOpen = await clientsPage.isModalVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger, #ajaxModal .help-block')
      .count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  test('TC_CLIENT_ADD_003 — Add client with duplicate name (edge case)', async () => {
    // Use a known existing client name
    await clientsPage.openAddModal();

    const name = `DuplicateClient_${Date.now()}`;
    await clientsPage.fillClientForm({ companyName: name });
    await clientsPage.clickSave();

    // Create same name again
    await clientsPage.goto();
    await clientsPage.openAddModal();
    await clientsPage.fillClientForm({ companyName: name });
    await clientsPage.clickSave();

    // Expected: system allows duplicate or shows warning — verify client exists
    await clientsPage.goto();
    await clientsPage.sortByIdDesc();
    await clientsPage.expectClientVisible(name);
  });

  // --- Edit Client ---

  test('TC_CLIENT_EDIT_001 — Edit client info (happy path)', async () => {
    // Sort by ID desc to get newest client
    await clientsPage.sortByIdDesc();
    await clientsPage.clickEditFirstClient();

    const newName = `TestClient_Updated_${Date.now()}`;
    await clientsPage.fillClientForm({
      companyName: newName,
      address: '456 New Street',
    });
    await clientsPage.clickSave();

    // Name updates in the table after save
    await clientsPage.expectClientVisible(newName);
  });

  test('TC_CLIENT_EDIT_002 — Edit client with empty company name shows error', async ({ page }) => {
    await clientsPage.sortByIdDesc();
    await clientsPage.clickEditFirstClient();

    await clientsPage.fillClientForm({ companyName: '' });
    await clientsPage.clickSave();

    // Expected: error or modal stays open
    const modalStillOpen = await clientsPage.isModalVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger, #ajaxModal .help-block')
      .count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  // --- Delete Client ---

  test('TC_CLIENT_DEL_001 — Delete client and confirm deletion', async ({ page }) => {
    await clientsPage.sortByIdDesc();
    await clientsPage.clickDeleteFirstClient();
    await clientsPage.confirmDelete();

    // Verify delete alert message
    await expect(page.locator('div.app-alert-message')).toContainText('deleted', { timeout: 10_000 });
  });

  test('TC_CLIENT_DEL_002 — Cancel client deletion', async ({ page }) => {
    await clientsPage.sortByIdDesc();
    await clientsPage.clickDeleteFirstClient();
    await clientsPage.cancelDelete();

    // Verify no delete message appears
    await expect(page.locator('div.app-alert-message')).not.toBeVisible({ timeout: 3_000 });
  });
});
