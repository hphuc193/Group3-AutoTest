import { test, expect } from '@playwright/test';
import { ClientsPage } from '../../pages/clients-page';

/**
 * FE2 — Client Search & View Tests
 * TC_CLIENT_SEARCH_001: Search by client name
 * TC_CLIENT_SEARCH_002: Search non-existing keyword
 * TC_CLIENT_VIEW_001: View client detail page
 */
test.describe.serial('FE2 — Client Search & View', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let clientsPage: ClientsPage;

  test.beforeEach(async ({ page }) => {
    clientsPage = new ClientsPage(page);
    await clientsPage.goto();
  });

  test('TC_CLIENT_SEARCH_001 — Search client by name', async () => {
    // Use first row's name as search keyword
    await clientsPage.sortByIdDesc();
    const firstRowName = await clientsPage.tableRows.first().locator('td').nth(1).innerText();
    const keyword = firstRowName.trim();

    await clientsPage.search(keyword);

    await clientsPage.expectClientVisible(keyword);
    const count = await clientsPage.getRowCount();
    expect(count).toBeGreaterThan(0);
  });

  test('TC_CLIENT_SEARCH_002 — Search with non-existing keyword shows no results', async () => {
    await clientsPage.search('xyzkhongtonqui123');

    await clientsPage.expectNoResults();
  });

  test('TC_CLIENT_VIEW_001 — View client detail page', async ({ page }) => {
    // Sort by ID desc and click first client name
    await clientsPage.sortByIdDesc();
    const firstName = await clientsPage.tableRows.first().locator('td').nth(1).innerText();

    await clientsPage.clickClientName(firstName.trim());

    // Expected: client detail page
    await expect(page).toHaveURL(/clients\/view\/\d+/, { timeout: 10_000 });

    // Verify key sections visible
    await expect(page.locator('body')).toContainText('Client info', { timeout: 5_000 });
    await expect(page.locator('body')).toContainText('Tasks', { timeout: 5_000 });
    await expect(page.locator('body')).toContainText('Notes', { timeout: 5_000 });
    await expect(page.locator('body')).toContainText('Contacts', { timeout: 5_000 });
  });
});
