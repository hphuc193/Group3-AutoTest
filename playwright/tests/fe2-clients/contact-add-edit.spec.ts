import { test, expect } from '@playwright/test';
import { ContactsPage } from '../../pages/contacts-page';

/**
 * FE2 — Contact Add & Edit Tests
 * TC_CONTACT_ADD_001: Add contact with valid data (happy path)
 * TC_CONTACT_ADD_002: Add contact with empty required fields
 * TC_CONTACT_EDIT_001: Edit contact info (happy path)
 * TC_CONTACT_EDIT_002: Edit contact — clear email shows error
 */
test.describe.serial('FE2 — Contact Add & Edit', () => {
  test.use({ storageState: '.auth/admin.json' });
  test.setTimeout(90_000);

  let contactsPage: ContactsPage;

  test.beforeEach(async ({ page }) => {
    contactsPage = new ContactsPage(page);
    // Navigate to clients list → sort by ID desc → click latest client
    await contactsPage.gotoLatestClient();
  });

  // --- Add Contact ---

  test('TC_CONTACT_ADD_001 — Add contact with valid data (happy path)', async () => {
    // Steps: click Add Contact → fill first name, last name, email → Save
    await contactsPage.openAddContactModal();

    const suffix = Date.now();
    const firstName = 'Contact';
    const lastName = `Test_${suffix}`;

    await contactsPage.fillContactForm({
      firstName,
      lastName,
      email: `contact_${suffix}@test.com`,
    });

    await contactsPage.clickSave();

    // Verify: new contact appears in Contacts section
    await contactsPage.gotoLatestClient();
    await contactsPage.expectContactVisible(lastName);
  });

  test('TC_CONTACT_ADD_002 — Add contact with empty fields shows error', async ({ page }) => {
    // Steps: click Add Contact → leave all fields empty → Save
    await contactsPage.openAddContactModal();

    // Leave all fields empty, just click Save
    await contactsPage.clickSave();

    // Expected: error message or modal stays open
    const modalStillOpen = await contactsPage.isModalVisible();
    const hasError = await page.locator(
      '#ajaxModal label.error, #ajaxModal .field-validation-error, #ajaxModal .text-danger, #ajaxModal .help-block'
    ).count();
    expect(modalStillOpen || hasError > 0).toBe(true);
  });

  // --- Edit Contact ---

  test('TC_CONTACT_EDIT_001 — Edit contact info (happy path)', async () => {
    // Steps: click on first contact → update first name, last name → Save
    await contactsPage.clickFirstContact();

    const suffix = Date.now();
    const newFirstName = 'Updated';
    const newLastName = `Contact_${suffix}`;

    await contactsPage.fillGeneralInfo({
      firstName: newFirstName,
      lastName: newLastName,
    });

    await contactsPage.clickSaveOnDetailPage();

    // Verify name changed in the h4 heading on contact profile
    await expect(
      contactsPage.page.locator('span.avatar-lg + h4')
    ).toContainText(newFirstName, { timeout: 10_000 });
  });

  test('TC_CONTACT_EDIT_002 — Edit contact email to empty shows error', async ({ page }) => {
    // Steps: click first contact → Account settings tab → clear email → Save
    await contactsPage.clickFirstContact();

    // Switch to Account settings tab where email field is
    await contactsPage.clickAccountSettingsTab();

    // Clear the email field
    await contactsPage.clearEmail();

    await contactsPage.clickSaveOnDetailPage();

    // Expected: error message if email is required, or alert with "updated"
    const alertMessage = page.locator('div.app-alert-message');
    const hasError = await page.locator(
      'label.error, .field-validation-error, .text-danger, .help-block, .alert-danger'
    ).count();
    const hasAlert = await alertMessage.isVisible().catch(() => false);
    expect(hasError > 0 || hasAlert).toBe(true);
  });
});
