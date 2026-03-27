import { test, expect } from '@playwright/test';
import { LeadsPage } from '../../pages/leads-page';

/**
 * FE5 — Lead Search & View Tests
 * TC_LEAD_SEARCH_001: Search by lead name
 * TC_LEAD_SEARCH_002: Search non-existing keyword
 * TC_LEAD_SEARCH_003: Search by owner name
 * TC_LEAD_VIEW_001: View lead detail page
 * TC_LEAD_VIEW_002: Convert lead to client
 */
test.describe.serial('FE5 — Lead Search & View', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let leadsPage: LeadsPage;

  test.beforeEach(async ({ page }) => {
    leadsPage = new LeadsPage(page);
    await leadsPage.goto();
  });

  // --- Search ---

  test('TC_LEAD_SEARCH_001 — Search lead by name', async () => {
    // Get a lead name from the first row to use as search keyword
    const firstRowText = await leadsPage.tableRows.first().locator('td').first().innerText();
    const searchKeyword = firstRowText.trim();

    await leadsPage.search(searchKeyword);

    // Expected: list shows matching lead
    await leadsPage.expectLeadVisible(searchKeyword);
    const count = await leadsPage.getRowCount();
    expect(count).toBeGreaterThan(0);
  });

  test('TC_LEAD_SEARCH_002 — Search with non-existing keyword shows no results', async () => {
    await leadsPage.search('xyzkhongtonqui123');

    // Expected: empty list or "no matching records" message
    await leadsPage.expectNoResults();
  });

  test('TC_LEAD_SEARCH_003 — Search lead by owner name', async () => {
    // Search by owner name "John Doe" (admin)
    await leadsPage.search('John Doe');

    // Expected: only leads with owner matching keyword
    const count = await leadsPage.getRowCount();
    expect(count).toBeGreaterThan(0);
    await leadsPage.expectLeadVisible('John Doe');
  });

  // --- View ---

  test('TC_LEAD_VIEW_001 — View lead detail page', async ({ page }) => {
    // Get first lead name
    const firstLeadName = await leadsPage.tableRows.first().locator('td').first().innerText();

    // Click on lead name to open detail
    await leadsPage.clickLeadTitle(firstLeadName.trim());

    // Expected: lead detail page URL
    await expect(page).toHaveURL(/leads\/view\/\d+/, { timeout: 10_000 });

    // Verify key sections: Status, Owner, Source, Contacts, Events, Tasks, Notes
    await expect(page.locator('body')).toContainText('Contacts', { timeout: 5_000 });
    await expect(page.locator('body')).toContainText('Events', { timeout: 5_000 });
    await expect(page.locator('body')).toContainText('Tasks', { timeout: 5_000 });
    await expect(page.locator('body')).toContainText('Notes', { timeout: 5_000 });
    // "Convert to client" button should be visible
    await expect(page.locator('a[title="Convert to client"]')).toBeVisible({ timeout: 5_000 });
  });

  test('TC_LEAD_VIEW_002 — Convert lead to client', async ({ page }) => {
    // First create a fresh lead to convert (avoid converting important leads)
    await leadsPage.openAddModal();
    const leadName = `ConvertLead_${Date.now()}`;
    await leadsPage.fillLeadForm({
      companyName: leadName,
      status: 'Won',
      owner: 'John Doe',
      source: 'Google',
    });
    await leadsPage.clickSave();

    // Navigate back to leads and find the new lead
    await leadsPage.goto();
    await leadsPage.clickLeadTitle(leadName);

    // Click "Convert to client" button on lead detail page
    await leadsPage.clickConvertToClient();

    // Press Save to finalize conversion
    await leadsPage.confirmConvertToClient();

    // Expected: redirected to client view page with lead data preserved
    await expect(page).toHaveURL(/clients\/view\/\d+/, { timeout: 15_000 });
    await expect(page.locator('body')).toContainText(leadName, { timeout: 10_000 });
  });
});
