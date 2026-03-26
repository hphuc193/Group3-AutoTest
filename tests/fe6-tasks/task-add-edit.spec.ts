import { test, expect } from '@playwright/test';
import { TasksPage } from '../../pages/tasks-page';

/**
 * FE6 — Task Add & Edit Tests
 * TC_TASK_ADD_001: Add task with valid data (happy path)
 * TC_TASK_ADD_002: Add task with empty title shows error
 * TC_TASK_ADD_003: Add task with only title (minimal data)
 * TC_TASK_EDIT_001: Edit task title (happy path)
 * TC_TASK_EDIT_002: Edit task with empty title shows error
 * TC_TASK_EDIT_003: Edit task status and assignee
 */
test.describe.serial('FE6 — Task Add & Edit', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(60_000);

  let tasksPage: TasksPage;

  test.beforeEach(async ({ page }) => {
    tasksPage = new TasksPage(page);
    await tasksPage.goto();
  });

  // --- Add Task ---

  test('TC_TASK_ADD_001 — Add task with valid data (happy path)', async () => {
    await tasksPage.openAddModal();

    const title = `TestTask_${Date.now()}`;
    await tasksPage.fillTitle(title);
    await tasksPage.fillDescription('Automated test task description');
    await tasksPage.selectAssignTo('John Doe');
    await tasksPage.selectStatus('To do');
    await tasksPage.clickSave();

    // Sort by ID desc so newest task appears first
    await tasksPage.goto();
    await tasksPage.sortByIdDesc();
    await tasksPage.expectTaskVisible(title);
  });

  test('TC_TASK_ADD_002 — Add task with empty title shows error', async ({ page }) => {
    await tasksPage.openAddModal();

    await tasksPage.fillTitle('');
    await tasksPage.clickSave();

    // Expected: error or modal stays open
    const modalStillOpen = await tasksPage.isModalVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger, #ajaxModal .help-block')
      .count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  test('TC_TASK_ADD_003 — Add task with only title (minimal data)', async () => {
    await tasksPage.openAddModal();

    const title = `MinimalTask_${Date.now()}`;
    await tasksPage.fillTitle(title);
    await tasksPage.clickSave();

    // Sort by ID desc so newest task appears first
    await tasksPage.goto();
    await tasksPage.sortByIdDesc();
    await tasksPage.expectTaskVisible(title);
  });

  // --- Edit Task ---

  test('TC_TASK_EDIT_001 — Edit task title (happy path)', async () => {
    await tasksPage.sortByIdDesc();
    await tasksPage.clickEditFirstTask();

    const newTitle = `EditedTask_${Date.now()}`;
    await tasksPage.fillTitle(newTitle);
    await tasksPage.clickSave();

    // Verify updated title in table
    await tasksPage.goto();
    await tasksPage.sortByIdDesc();
    await tasksPage.expectTaskVisible(newTitle);
  });

  test('TC_TASK_EDIT_002 — Edit task with empty title shows error', async ({ page }) => {
    await tasksPage.sortByIdDesc();
    await tasksPage.clickEditFirstTask();

    await tasksPage.fillTitle('');
    await tasksPage.clickSave();

    // Expected: error or modal stays open
    const modalStillOpen = await tasksPage.isModalVisible();
    const hasError = await page.locator('#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger, #ajaxModal .help-block')
      .count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  test('TC_TASK_EDIT_003 — Edit task status and assignee', async () => {
    await tasksPage.sortByIdDesc();
    await tasksPage.clickEditFirstTask();

    const newTitle = `StatusChanged_${Date.now()}`;
    await tasksPage.fillTitle(newTitle);
    await tasksPage.selectAssignTo('John Doe');
    await tasksPage.selectStatus('In progress');
    await tasksPage.clickSave();

    // Verify updated task in table
    await tasksPage.goto();
    await tasksPage.sortByIdDesc();
    await tasksPage.expectTaskVisible(newTitle);
  });
});
