import { test, expect } from '@playwright/test';

/**
 * FE3 — Project Member Tests
 * TC_PROJECT_MEMBER_001: Add member to project (happy path)
 * TC_PROJECT_MEMBER_002: Already-joined member not shown in dropdown
 */
test.describe.serial('FE3 — Project Members', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  test.beforeEach(async ({ page }) => {
    // Navigate to projects list, then click the first project title
    await page.goto('/index.php/projects/all_projects', { timeout: 45_000 });
    await page.locator('table#project-table').waitFor({ state: 'visible', timeout: 15_000 });
    await page.locator('table#project-table tbody tr').first().locator('td').nth(1).locator('a').first().click();
    await page.waitForTimeout(3_000);
  });

  test('TC_PROJECT_MEMBER_001 — Add member to project (happy path)', async ({ page }) => {
    // First, remove a non-admin member (if any) to ensure there's someone to add
    const deleteButtons = page.locator('table#project-member-table a.delete[title="Delete member"]');
    if (await deleteButtons.count() > 0) {
      await deleteButtons.first().click();
      await page.waitForTimeout(2_000);
    }

    // Click "Add member" button on the project overview
    await page.locator('a.add-member-button[title="Add member"]').click();
    await page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await page.waitForTimeout(1_000);

    // Open the Member Select2 dropdown and pick an available member
    await page.locator('#s2id_user_id .select2-choice').click();
    await page.waitForTimeout(1_000);

    // Pick the first available member from the dropdown
    const firstOption = page.locator('.select2-drop-active .select2-results .select2-result-label').first();
    const memberName = await firstOption.innerText();
    await firstOption.click();
    await page.waitForTimeout(500);

    // Click Save
    await page.locator('#ajaxModal button[type="submit"].btn-primary').click();
    await page.waitForTimeout(3_000);

    // Verify member appears in the project members table
    await expect(page.locator('table#project-member-table')).toContainText(memberName, { timeout: 10_000 });
  });

  test('TC_PROJECT_MEMBER_002 — Already-joined member not shown in dropdown', async ({ page }) => {
    // Get existing members from the project members table
    const membersTable = page.locator('table#project-member-table');
    await membersTable.waitFor({ state: 'visible', timeout: 10_000 });
    const existingMembers = await membersTable.innerText();

    // Click "Add member" to open the modal
    await page.locator('a.add-member-button[title="Add member"]').click();
    await page.locator('#ajaxModal.show').waitFor({ state: 'visible', timeout: 10_000 });
    await page.waitForTimeout(1_000);

    // Open the Member Select2 dropdown
    await page.locator('#s2id_user_id .select2-choice').click();
    await page.waitForTimeout(1_000);

    // Get all available options in the dropdown
    const options = page.locator('.select2-drop-active .select2-results .select2-result-label');
    const optionCount = await options.count();

    // Verify that "John Doe" (existing member/admin) is NOT in the dropdown options
    for (let i = 0; i < optionCount; i++) {
      const optionText = await options.nth(i).innerText();
      expect(optionText).not.toBe('John Doe');
    }
  });
});
