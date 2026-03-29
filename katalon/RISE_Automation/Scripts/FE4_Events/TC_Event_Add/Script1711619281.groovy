import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Event_Add - Data-driven Event Add Test
 * Variables bound from CSV: scenario, title, description, startDate, endDate, shareMember, expectedResult
 * Uses cookie auth (no CAPTCHA needed)
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Events via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Events'))
WebUI.delay(2)

// Wait for calendar to render
WebUI.waitForElementPresent(findTestObject('Page_Events/calendar_Grid'), GlobalVariable.timeout)

// Click "Add event" button
WebUI.click(findTestObject('Page_Events/btn_AddEvent'))
WebUI.waitForElementPresent(findTestObject('Page_Events/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.delay(1)

// Generate unique title
String actualTitle = title.replace('{timestamp}', String.valueOf(System.currentTimeMillis()))

// Fill title (if provided)
if (title != '') {
	WebUI.clearText(findTestObject('Page_Events/input_Title'))
	WebUI.setText(findTestObject('Page_Events/input_Title'), actualTitle)
}

// Fill description (if provided)
if (description != '') {
	// Click textarea to activate Summernote, then fill note-editable
	WebUI.click(findTestObject('Page_Events/textarea_Description'))
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Events/div_NoteEditable'))
	WebUI.setText(findTestObject('Page_Events/div_NoteEditable'), description)
}

// Set start date by clicking datepicker and selecting today
def driver = DriverFactory.getWebDriver()
if (startDate == 'today' || (expectedResult == 'success' && startDate == '')) {
	// Click start date input to open datepicker, then click today's date
	WebUI.click(findTestObject('Page_Events/input_StartDate'))
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Events/datepicker_Today'))
	WebUI.delay(1)
} else if (startDate != '') {
	// Set specific date via JS (for invalid date tests)
	driver.executeScript("document.getElementById('start_date').value = arguments[0]; document.getElementById('start_date').dispatchEvent(new Event('change'));", startDate)
}

if (endDate != '') {
	driver.executeScript("document.getElementById('end_date').value = arguments[0]; document.getElementById('end_date').dispatchEvent(new Event('change'));", endDate)
}

// Share with member (if applicable - for TC_EVENT_ADD_004)
if (shareMember != '' && shareMember != 'no') {
	// Check "Specific members and teams" checkbox first
	WebUI.click(findTestObject('Page_Events/checkbox_SpecificMembers'))
	WebUI.delay(1)
	// Click the Select2 input and pick a member
	WebUI.click(findTestObject('Page_Events/select2_ShareMembers'))
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Events/select2_ShareResult'))
	WebUI.delay(1)
}

// Click Save
WebUI.click(findTestObject('Page_Events/btn_Save'))
WebUI.delay(3)

// Verify expected result
if (expectedResult == 'success') {
	// Reload calendar and verify event appears
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/events')
	WebUI.delay(2)

	String pageSource = driver.getPageSource()
	assert pageSource.contains(actualTitle) : "Event '${actualTitle}' not found on calendar"
	WebUI.comment("PASSED: ${scenario} - Event '${actualTitle}' created")

} else if (expectedResult == 'modal_stays_open') {
	// Modal should still be open (validation error)
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Events/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")
}

WebUI.closeBrowser()
