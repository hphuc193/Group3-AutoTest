import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Contact_Edit - Data-driven Contact Edit Test
 * Variables bound from CSV: scenario, newFirstName, newLastName, clearEmail, expectedResult
 * Flow: Add contact first -> click contact profile -> edit General Info or Account Settings
 */

WebUI.comment(">>> Running: ${scenario}")

String ts = String.valueOf(System.currentTimeMillis())

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Clients via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Clients'))
WebUI.delay(2)

// Click Clients list tab, sort by ID desc
WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
WebUI.waitForElementPresent(findTestObject('Page_Clients/table_Clients'), GlobalVariable.timeout)

boolean alreadyDesc = WebUI.verifyElementPresent(findTestObject('Page_Clients/th_ID_Desc'), 3, FailureHandling.OPTIONAL)
if (!alreadyDesc) {
	WebUI.click(findTestObject('Page_Clients/th_ID'))
	WebUI.delay(1)
	if (!WebUI.verifyElementPresent(findTestObject('Page_Clients/th_ID_Desc'), 3, FailureHandling.OPTIONAL)) {
		WebUI.click(findTestObject('Page_Clients/th_ID'))
		WebUI.delay(1)
	}
}

// Click first client to go to detail page
WebUI.click(findTestObject('Page_Clients/link_FirstClientName'))
WebUI.delay(2)

// === Step 1: Add a contact first (so we have one to edit) ===
WebUI.click(findTestObject('Page_Contacts/btn_AddContact'))
WebUI.waitForElementPresent(findTestObject('Page_Contacts/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.delay(1)

String contactFirstName = "EditMe"
String contactLastName = "Contact_${ts}"
String contactEmail = "editme_${ts}@test.com"

WebUI.setText(findTestObject('Page_Contacts/input_FirstName'), contactFirstName)
WebUI.setText(findTestObject('Page_Contacts/input_LastName'), contactLastName)
WebUI.setText(findTestObject('Page_Contacts/input_Email'), contactEmail)
WebUI.click(findTestObject('Page_Contacts/btn_Save'))
WebUI.delay(3)

// === Step 2: Reload page and click the first contact to go to profile ===
def driver = DriverFactory.getWebDriver()
driver.navigate().refresh()
WebUI.delay(3)

WebUI.click(findTestObject('Page_Contacts/link_FirstContact'))
WebUI.delay(3)

// === Step 3: Edit based on scenario ===
if (clearEmail == 'yes') {
	// TC_CONTACT_EDIT_002: Switch to Account settings tab, clear email
	WebUI.click(findTestObject('Page_Contacts/tab_AccountSettings'))
	WebUI.delay(2)

	WebUI.clearText(findTestObject('Page_Contacts/input_ProfileEmail'))

	WebUI.click(findTestObject('Page_Contacts/btn_SaveAccountSettings'))
	WebUI.delay(3)

	// Verify: error or alert appears
	boolean hasError = WebUI.verifyElementPresent(findTestObject('Page_Contacts/label_Error'), 5, FailureHandling.OPTIONAL)
	boolean hasAlert = WebUI.verifyElementPresent(findTestObject('Page_Contacts/alert_Message'), 5, FailureHandling.OPTIONAL)
	assert hasError || hasAlert : "Expected error or alert when clearing email"
	WebUI.comment("PASSED: ${scenario} - Error/alert shown when clearing email")

} else {
	// TC_CONTACT_EDIT_001: Click General Info tab first, then edit name
	WebUI.click(findTestObject('Page_Contacts/tab_GeneralInfo'))
	WebUI.delay(1)

	String actualLastName = newLastName.replace('{timestamp}', ts)

	WebUI.clearText(findTestObject('Page_Contacts/input_ProfileFirstName'))
	WebUI.setText(findTestObject('Page_Contacts/input_ProfileFirstName'), newFirstName)

	WebUI.clearText(findTestObject('Page_Contacts/input_ProfileLastName'))
	WebUI.setText(findTestObject('Page_Contacts/input_ProfileLastName'), actualLastName)

	WebUI.click(findTestObject('Page_Contacts/btn_SaveGeneralInfo'))
	WebUI.delay(3)

	// Verify: name updated in the profile heading
	String pageSource = driver.getPageSource()
	assert pageSource.contains(newFirstName) : "Updated first name '${newFirstName}' not found on profile"
	WebUI.comment("PASSED: ${scenario} - Contact updated to '${newFirstName} ${actualLastName}'")
}

WebUI.closeBrowser()
