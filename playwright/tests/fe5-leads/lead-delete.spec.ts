import { test, expect } from '@playwright/test';
import { LeadsPage } from '../../pages/leads-page';

/**
 * FE5 — Lead Delete Tests
 * TC_LEAD_DEL_001: Delete lead (confirm)
 * TC_LEAD_DEL_002: Delete lead (cancel)
 */
test.describe.serial('FE5 — Lead Delete', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let leadsPage: LeadsPage;

  test.beforeEach(async ({ page }) => {
    leadsPage = new LeadsPage(page);
    await leadsPage.goto();
  });

  test('TC_LEAD_DEL_001 — Delete lead and confirm deletion', async ({ page }) => {
    // Click delete on first lead and confirm
    await leadsPage.clickDeleteFirstLead();
    await leadsPage.confirmDelete();

    // Verify "The record has been deleted." alert message
    await expect(page.locator('div.app-alert-message')).toContainText('The record has been deleted', { timeout: 10_000 });
  });

  test('TC_LEAD_DEL_002 — Cancel lead deletion keeps lead in list', async ({ page }) => {
    // Click delete then cancel
    await leadsPage.clickDeleteFirstLead();
    await leadsPage.cancelDelete();

    // Verify no delete message appears
    await expect(page.locator('div.app-alert-message')).not.toBeVisible({ timeout: 3_000 });
  });
});
