import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Contact_Add - Data-driven Contact Add Test
 * Variables bound from CSV: scenario, firstName, lastName, email, expectedResult
 * Flow: Clients -> click first client -> Add Contact modal
 * Uses cookie auth
 */

WebUI.comment(">>> Running: ${scenario}")

String ts = String.valueOf(System.currentTimeMillis())

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Clients via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Clients'))
WebUI.delay(2)

// Click Clients list tab
WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
WebUI.waitForElementPresent(findTestObject('Page_Clients/table_Clients'), GlobalVariable.timeout)

// Sort by ID desc to get latest client (only if not already sorted)
boolean alreadyDesc = WebUI.verifyElementPresent(findTestObject('Page_Clients/th_ID_Desc'), 3, FailureHandling.OPTIONAL)
if (!alreadyDesc) {
	WebUI.click(findTestObject('Page_Clients/th_ID'))
	WebUI.delay(1)
	if (!WebUI.verifyElementPresent(findTestObject('Page_Clients/th_ID_Desc'), 3, FailureHandling.OPTIONAL)) {
		WebUI.click(findTestObject('Page_Clients/th_ID'))
		WebUI.delay(1)
	}
}

// Click first client name to go to client detail page
WebUI.click(findTestObject('Page_Clients/link_FirstClientName'))
WebUI.delay(2)

// Click "Add contact" button
WebUI.click(findTestObject('Page_Contacts/btn_AddContact'))
WebUI.waitForElementPresent(findTestObject('Page_Contacts/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.delay(1)

// Fill form fields
String actualLastName = lastName.replace('{timestamp}', ts)
String actualEmail = email.replace('{timestamp}', ts)

if (firstName != '') {
	WebUI.clearText(findTestObject('Page_Contacts/input_FirstName'))
	WebUI.setText(findTestObject('Page_Contacts/input_FirstName'), firstName)
}

if (lastName != '') {
	WebUI.clearText(findTestObject('Page_Contacts/input_LastName'))
	WebUI.setText(findTestObject('Page_Contacts/input_LastName'), actualLastName)
}

if (email != '') {
	WebUI.clearText(findTestObject('Page_Contacts/input_Email'))
	WebUI.setText(findTestObject('Page_Contacts/input_Email'), actualEmail)
}

// Click Save
WebUI.click(findTestObject('Page_Contacts/btn_Save'))
WebUI.delay(3)

def driver = DriverFactory.getWebDriver()

// Verify result
if (expectedResult == 'success') {
	// Reload client detail page and check contact appears
	driver.navigate().refresh()
	WebUI.delay(3)

	String pageSource = driver.getPageSource()
	assert pageSource.contains(actualLastName) : "Contact '${actualLastName}' not found on client detail page"
	WebUI.comment("PASSED: ${scenario} - Contact '${firstName} ${actualLastName}' added")

} else if (expectedResult == 'modal_stays_open') {
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Contacts/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")
}

WebUI.closeBrowser()
