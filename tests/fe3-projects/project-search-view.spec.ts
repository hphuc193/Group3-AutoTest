import { test, expect } from '@playwright/test';
import { ProjectsPage } from '../../pages/projects-page';

/**
 * FE3 — Project Search & View Tests
 * Matches test cases from data/RISE_TestCases.json → FE3 section.
 */
test.describe.serial('FE3 — Project Search & View', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let projectsPage: ProjectsPage;

  test.beforeEach(async ({ page }) => {
    projectsPage = new ProjectsPage(page);
    await projectsPage.goto();
  });

  // --- Search ---

  test('TC_PROJECT_SEARCH_001 — Search project by name', async () => {
    // Steps: Menu Projects → type existing project name in Search → observe results
    await projectsPage.search('SEO Optimization Strategy');

    // Expected: list shows matching project
    await projectsPage.expectProjectVisible('SEO Optimization Strategy');
    const count = await projectsPage.getRowCount();
    expect(count).toBeGreaterThan(0);
  });

  test('TC_PROJECT_SEARCH_002 — Search project by client name', async () => {
    // Steps: Menu Projects → type client name in Search → observe results
    await projectsPage.search('Birdie Erdman');

    // Expected: list shows projects belonging to that client
    await projectsPage.expectProjectVisible('Birdie Erdman');
    const count = await projectsPage.getRowCount();
    expect(count).toBeGreaterThan(0);
  });

  test('TC_PROJECT_SEARCH_003 — Search with non-existing keyword shows no results', async () => {
    // Steps: Menu Projects → type non-existing keyword → observe results
    await projectsPage.search('xyzkhongtonqui123');

    // Expected: empty list or "no matching records" message
    await projectsPage.expectNoResults();
  });

  // --- View ---

  test('TC_PROJECT_VIEW_001 — View project detail page', async ({ page }) => {
    // Steps: Menu Projects → click on project name
    await projectsPage.clickProjectTitle('SEO Optimization Strategy');

    // Expected: project detail page with Tasks, Members, Milestones, Progress, Activity
    await expect(page).toHaveURL(/projects\/view\/\d+/, { timeout: 10_000 });

    // Verify key sections are visible on the project detail page
    await expect(page.locator('text=Overview')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('text=Tasks List')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('text=Milestones')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('text=Activity')).toBeVisible({ timeout: 5_000 });
    // Progress bar / chart area
    await expect(page.locator('text=Project members')).toBeVisible({ timeout: 5_000 });
  });
});
