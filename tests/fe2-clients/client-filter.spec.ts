import { test, expect } from '@playwright/test';
import { ClientsPage } from '../../pages/clients-page';

test.describe.serial('FE2 — Client Filter', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let clientsPage: ClientsPage;

  test.beforeEach(async ({ page }) => {
    clientsPage = new ClientsPage(page);
    await clientsPage.goto();
  });

  test('TC_CLIENT_FILTER_001 — Filter by Has open projects', async ({ page }) => {
    await clientsPage.applyFilter('Has open projects');
    await clientsPage.expectTableHasRows();
    // After filter, the filter dropdown toggle shows "Has open projects"
    await expect(page.getByRole('button', { name: 'Has open projects' }).first()).toBeVisible();
  });

  test('TC_CLIENT_FILTER_002 — Filter by Has due', async ({ page }) => {
    await clientsPage.applyFilter('Has due');
    await clientsPage.expectTableHasRows();
    await expect(page.getByRole('button', { name: 'Has due' }).first()).toBeVisible();
  });

  test('TC_CLIENT_FILTER_003 — Clear filter resets client list', async () => {
    // Apply a filter first
    await clientsPage.applyFilter('Has open projects');
    const filteredCount = await clientsPage.getRowCount();
    expect(filteredCount).toBeGreaterThan(0);

    // Clear by selecting "All clients"
    await clientsPage.clearFilters();

    // Full list should have >= filtered count
    const allCount = await clientsPage.getRowCount();
    expect(allCount).toBeGreaterThanOrEqual(filteredCount);
  });
});
