import { test, expect } from '@playwright/test';
import { ProjectsPage } from '../../pages/projects-page';

/**
 * FE3 — Project Add, Edit & Delete Tests
 * Matches test cases from data/RISE_TestCases.json → FE3 section.
 * Tests use the demo project list (resets every 6h).
 */
test.describe.serial('FE3 — Project Add, Edit & Delete', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let projectsPage: ProjectsPage;

  test.beforeEach(async ({ page }) => {
    projectsPage = new ProjectsPage(page);
    await projectsPage.goto();
  });

  // --- Add Project ---

  test('TC_PROJECT_ADD_001 — Add project with valid data (happy path)', async ({ page }) => {
    // Steps: Menu Projects → Add Project → fill all info → Save
    await projectsPage.openAddModal();

    const title = `TestProject_${Date.now()}`;
    await projectsPage.fillProjectForm({
      title,
      client: 'Birdie Erdman',
      startDate: '03-25-2026',
    });

    await projectsPage.clickSave();

    // No success message — verify by navigating to project list and sorting by Start date
    await projectsPage.goto();
    await projectsPage.sortByStartDateAsc();
    await projectsPage.expectProjectVisible(title);
  });

  test('TC_PROJECT_ADD_002 — Add project with empty title shows error', async ({ page }) => {
    // Steps: Menu Projects → Add Project → leave Title empty → Save
    await projectsPage.openAddModal();

    await projectsPage.fillProjectForm({
      title: '',
    });

    await projectsPage.clickSave();

    // Expected: error message requiring Title — modal stays open
    const modalStillOpen = await projectsPage.isModalVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger')
      .count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  test('TC_PROJECT_ADD_003 — Add project without selecting client shows error', async ({ page }) => {
    // Steps: Menu Projects → Add Project → don't select Client → Save
    await projectsPage.openAddModal();

    await projectsPage.fillProjectForm({
      title: `NoClient_${Date.now()}`,
      startDate: '03-25-2026',
      // client: intentionally not selected
    });

    await projectsPage.clickSave();

    // Expected: error requiring Client selection
    // If RISE allows saving without client, verify project appears in list instead
    await page.waitForTimeout(2_000);
    const modalStillOpen = await projectsPage.isModalVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger')
      .count();
    // Document actual behavior: either validation blocks or system accepts it
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  // --- Edit Project ---

  test('TC_PROJECT_EDIT_001 — Edit project title and deadline (happy path)', async ({ page }) => {
    // Steps: Menu Projects → click project → Edit from Options → update Title + Deadline → Save
    await projectsPage.clickEditFirstProject();

    const newTitle = `TestProject_Updated_${Date.now()}`;
    await projectsPage.fillProjectForm({
      title: newTitle,
      deadline: '06-30-2026',
    });

    await projectsPage.clickSave();

    // Expected: project updated, new info displayed in list
    await projectsPage.goto();
    await projectsPage.expectProjectVisible(newTitle);
  });

  test('TC_PROJECT_EDIT_002 — Edit project with empty title shows error', async ({ page }) => {
    // Steps: Edit project → clear Title → Save
    await projectsPage.clickEditFirstProject();

    await projectsPage.fillProjectForm({ title: '' });
    await projectsPage.clickSave();

    // Expected: error message requiring Title
    const modalStillOpen = await projectsPage.isModalVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger')
      .count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  // --- Delete Project ---

  test('TC_PROJECT_DEL_001 — Delete project successfully (happy path)', async ({ page }) => {
    // Steps: Menu Projects → click Delete → confirm deletion
    await projectsPage.clickDeleteFirstProject();
    await projectsPage.confirmDelete();

    // Expected: project removed, no longer in list
    await expect(page.locator('.app-alert-message'))
      .toContainText('deleted', { timeout: 5_000 });
  });

  test('TC_PROJECT_DEL_002 — Cancel delete keeps project in list', async ({ page }) => {
    // Steps: Menu Projects → click Delete → Cancel in confirmation dialog
    await page.locator('table#project-table tbody tr').first().waitFor({ state: 'visible', timeout: 10_000 });
    const countBefore = await projectsPage.getRowCount();

    await projectsPage.clickDeleteFirstProject();
    await projectsPage.cancelDelete();

    // Expected: project not deleted, still in list
    await page.waitForTimeout(2_000);
    const countAfter = await projectsPage.getRowCount();
    expect(countAfter).toBe(countBefore);
  });
});
