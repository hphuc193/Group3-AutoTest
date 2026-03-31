import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Project_Member - Data-driven Project Member Test
 * Variables bound from CSV: scenario, action, expectedResult
 * Flow: Navigate to first project detail -> Add member / Check duplicate
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Projects via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Projects'))
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Projects/table_Projects'), GlobalVariable.timeout)

// Click first project title to go to detail page
WebUI.click(findTestObject('Page_Projects/link_FirstProjectTitle'))
WebUI.delay(1)

def driver = DriverFactory.getWebDriver()

if (action == 'add_member') {
	// === Add member flow ===

	// Delete a member only if delete button exists (to free up a slot)
	boolean hasDeleteBtn = WebUI.verifyElementPresent(findTestObject('Page_Projects/btn_DeleteMember'), 3, FailureHandling.OPTIONAL)
	if (hasDeleteBtn) {
		WebUI.click(findTestObject('Page_Projects/btn_DeleteMember'))
		WebUI.delay(1)
	}

	// Click "Add member" button
	WebUI.click(findTestObject('Page_Projects/btn_AddMember'))
	WebUI.waitForElementPresent(findTestObject('Page_Projects/modal_AjaxModal'), GlobalVariable.timeout)
	WebUI.delay(1)

	// Open the Member Select2 dropdown and pick first available
	WebUI.click(findTestObject('Page_Projects/select2_Member'))
	WebUI.delay(1)

	def firstOption = driver.findElement(org.openqa.selenium.By.cssSelector('.select2-drop-active .select2-results .select2-result-label'))
	String memberName = firstOption.getText()
	firstOption.click()
	WebUI.delay(1)

	// Click Save
	WebUI.click(findTestObject('Page_Projects/btn_Save'))
	WebUI.delay(1)

	// Verify member appears in project members table
	String membersTableText = driver.findElement(org.openqa.selenium.By.id('project-member-table')).getText()
	assert membersTableText.contains(memberName) : "Member '${memberName}' not found in project members table"
	WebUI.comment("PASSED: ${scenario} - Member '${memberName}' added to project")

} else if (action == 'check_duplicate') {
	// === Check duplicate member flow ===
	// Delete a member first to ensure availability (only if delete button exists)
	boolean hasDeleteBtn2 = WebUI.verifyElementPresent(findTestObject('Page_Projects/btn_DeleteMember'), 3, FailureHandling.OPTIONAL)
	if (hasDeleteBtn2) {
		WebUI.click(findTestObject('Page_Projects/btn_DeleteMember'))
		WebUI.delay(1)
	}

	// Step 1: Add a member first
	WebUI.click(findTestObject('Page_Projects/btn_AddMember'))
	WebUI.waitForElementPresent(findTestObject('Page_Projects/modal_AjaxModal'), GlobalVariable.timeout)
	WebUI.delay(1)

	WebUI.click(findTestObject('Page_Projects/select2_Member'))
	WebUI.delay(1)

	def addOption = driver.findElement(org.openqa.selenium.By.cssSelector('.select2-drop-active .select2-results .select2-result-label'))
	String addedMemberName = addOption.getText()
	addOption.click()
	WebUI.delay(1)

	WebUI.click(findTestObject('Page_Projects/btn_Save'))
	WebUI.delay(1)

	// Step 2: Try to add the same member again — open Add member modal
	WebUI.click(findTestObject('Page_Projects/btn_AddMember'))
	WebUI.waitForElementPresent(findTestObject('Page_Projects/modal_AjaxModal'), GlobalVariable.timeout)
	WebUI.delay(1)

	WebUI.click(findTestObject('Page_Projects/select2_Member'))
	WebUI.delay(1)

	// Verify the just-added member is NOT in the dropdown
	def options = driver.findElements(org.openqa.selenium.By.cssSelector('.select2-drop-active .select2-results .select2-result-label'))
	boolean memberFound = false
	for (opt in options) {
		if (opt.getText().trim() == addedMemberName) {
			memberFound = true
			break
		}
	}
	assert !memberFound : "'${addedMemberName}' should NOT be in the dropdown after being added, but was found"
	WebUI.comment("PASSED: ${scenario} - '${addedMemberName}' not shown in dropdown after being added (${options.size()} options remaining)")
}

WebUI.closeBrowser()
