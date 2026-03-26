import { test, expect } from '@playwright/test';
import { LeadsPage } from '../../pages/leads-page';

/**
 * FE5 — Lead Add & Edit Tests
 * Matches test cases from data/RISE_TestCases.json → FE5 section.
 */
test.describe.serial('FE5 — Lead Add & Edit', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let leadsPage: LeadsPage;

  test.beforeEach(async ({ page }) => {
    leadsPage = new LeadsPage(page);
    await leadsPage.goto();
  });

  // --- Add Lead ---

  test('TC_LEAD_ADD_001 — Add lead with valid data (happy path)', async () => {
    // Steps: Menu Leads → Add Lead → fill all info → Save
    await leadsPage.openAddModal();

    const name = `TestLead_${Date.now()}`;
    await leadsPage.fillLeadForm({
      companyName: name,
      status: 'New',
      owner: 'John Doe',
      source: 'Site',
    });

    await leadsPage.clickSave();

    // Expected: lead created, shown in list
    await leadsPage.goto();
    await leadsPage.expectLeadVisible(name);
  });

  test('TC_LEAD_ADD_002 — Add lead with empty name shows error', async ({ page }) => {
    // Steps: Menu Leads → Add Lead → leave Name empty → Save
    await leadsPage.openAddModal();

    await leadsPage.fillLeadForm({
      companyName: '',
    });

    await leadsPage.clickSave();

    // Expected: error message requiring Name
    const modalStillOpen = await leadsPage.isModalVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger')
      .count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  test('TC_LEAD_ADD_003 — Add lead without selecting Owner', async () => {
    // Steps: Menu Leads → Add Lead → fill info → don't change Owner → Save
    // Owner defaults to logged-in admin (John Doe), cannot be cleared
    await leadsPage.openAddModal();

    const name = `TestLead_NoOwner_${Date.now()}`;
    await leadsPage.fillLeadForm({
      companyName: name,
      status: 'New',
      source: 'Google',
      // Owner: not explicitly set — defaults to admin
    });

    await leadsPage.clickSave();

    // Expected: lead created with Owner defaulting to creator (John Doe)
    await leadsPage.goto();
    await leadsPage.expectLeadVisible(name);
  });

  test('TC_LEAD_ADD_004 — Add lead with Managers (happy path)', async ({ page }) => {
    // Steps: Menu Leads → Add Lead → fill all info including Managers → Save
    await leadsPage.openAddModal();

    const name = `TestLead_WithMgr_${Date.now()}`;
    await leadsPage.fillLeadForm({
      companyName: name,
      owner: 'John Doe',
      source: 'Youtube',
    });

    // Select a manager from the Managers Select2 multi-select (#s2id_managers)
    await page.locator('#s2id_managers .select2-search-field input').click();
    await page.waitForTimeout(1_000);
    await page.locator('.select2-drop-active .select2-results .select2-result-label')
      .filter({ hasText: 'John Doe' }).first().click();

    await leadsPage.clickSave();

    // Expected: lead created with Managers
    await leadsPage.goto();
    await leadsPage.expectLeadVisible(name);
  });

  // --- Edit Lead ---

  test('TC_LEAD_EDIT_001 — Edit lead name, status, source (happy path)', async () => {
    // Steps: Menu Leads → click Edit → update Name, Status, Source → Save
    await leadsPage.clickEditFirstLead();

    const newName = `TestLead_Updated_${Date.now()}`;
    await leadsPage.fillLeadForm({
      companyName: newName,
      status: 'Qualified',
      source: 'Elsewhere',
    });

    await leadsPage.clickSave();

    // Expected: lead updated, new info in list
    await leadsPage.goto();
    await leadsPage.expectLeadVisible(newName);
  });

  test('TC_LEAD_EDIT_002 — Edit lead with empty name shows error', async ({ page }) => {
    // Steps: Edit lead → clear Name → Save
    await leadsPage.clickEditFirstLead();

    await leadsPage.fillLeadForm({ companyName: '' });
    await leadsPage.clickSave();

    // Expected: error message requiring Name
    const modalStillOpen = await leadsPage.isModalVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger')
      .count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  test('TC_LEAD_EDIT_003 — Edit lead status change (happy path)', async ({ page }) => {
    // Steps: Edit lead → change Status to different value → Save
    await leadsPage.clickEditFirstLead();

    await leadsPage.fillLeadForm({
      status: 'Negotiation',
    });

    await leadsPage.clickSave();

    // Expected: status updated in list
    await leadsPage.goto();
    // Verify the status label changed — check first row has new status
    await expect(page.locator('table#lead-table tbody tr').first()).toContainText('Negotiation', { timeout: 10_000 });
  });
});
