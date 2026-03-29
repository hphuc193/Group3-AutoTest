import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Event_Edit - Data-driven Event Edit Test
 * Variables bound from CSV: scenario, newTitle, changeColor, expectedResult
 * Flow: Add event first -> Click event -> Edit event -> Modify -> Save
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Events via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Events'))
WebUI.delay(2)
WebUI.waitForElementPresent(findTestObject('Page_Events/calendar_Grid'), GlobalVariable.timeout)

// === Step 1: Add an event first ===
WebUI.click(findTestObject('Page_Events/btn_AddEvent'))
WebUI.waitForElementPresent(findTestObject('Page_Events/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.delay(1)

String originalTitle = "EditTest_${System.currentTimeMillis()}"
WebUI.clearText(findTestObject('Page_Events/input_Title'))
WebUI.setText(findTestObject('Page_Events/input_Title'), originalTitle)

// Pick today's date
WebUI.click(findTestObject('Page_Events/input_StartDate'))
WebUI.delay(1)
WebUI.click(findTestObject('Page_Events/datepicker_Today'))
WebUI.delay(1)

WebUI.click(findTestObject('Page_Events/btn_Save'))
WebUI.delay(3)

// === Step 2: Reload calendar and click the event ===
WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/events')
WebUI.delay(2)
WebUI.waitForElementPresent(findTestObject('Page_Events/calendar_Grid'), GlobalVariable.timeout)

def driver = DriverFactory.getWebDriver()
def eventLink = driver.findElement(org.openqa.selenium.By.xpath("//a[contains(@class,'fc-event')]//span[contains(text(),'${originalTitle}')]/ancestor::a"))
eventLink.click()
WebUI.waitForElementPresent(findTestObject('Page_Events/modal_AjaxModal'), GlobalVariable.timeout)

// === Step 3: Click "Edit event" ===
WebUI.click(findTestObject('Page_Events/link_EditEvent'))
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Events/input_Title'), GlobalVariable.timeout)

// === Step 4: Modify based on scenario ===
String actualTitle = newTitle.replace('{timestamp}', String.valueOf(System.currentTimeMillis()))

// Only modify title if this scenario requires it (not for color-only change)
if (changeColor != 'yes') {
	WebUI.clearText(findTestObject('Page_Events/input_Title'))
	if (newTitle != '') {
		WebUI.setText(findTestObject('Page_Events/input_Title'), actualTitle)
	}
}

// Change color (if applicable)
if (changeColor == 'yes') {
	// Scroll modal to reveal color palette
	driver.executeScript("document.querySelector('#ajaxModal .modal-body').scrollTop = document.querySelector('#ajaxModal .modal-body').scrollHeight")
	WebUI.delay(1)
	// Click a different (non-active) color swatch
	WebUI.click(findTestObject('Page_Events/colorTag_Inactive'))
	WebUI.delay(1)
}

// Click Save
WebUI.click(findTestObject('Page_Events/btn_Save'))
WebUI.delay(2)

// === Step 5: Verify result ===
if (expectedResult == 'success') {
	// Reload calendar and verify event exists
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/events')
	WebUI.delay(2)

	String pageSource = driver.getPageSource()
	if (newTitle != '') {
		assert pageSource.contains(actualTitle) : "Updated event '${actualTitle}' not found on calendar"
		WebUI.comment("PASSED: ${scenario} - Event updated to '${actualTitle}'")
	} else {
		// Color change: original title should still exist
		assert pageSource.contains(originalTitle) : "Event '${originalTitle}' not found after color change"
		WebUI.comment("PASSED: ${scenario} - Event color changed")
	}

} else if (expectedResult == 'modal_stays_open') {
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Events/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")
}

WebUI.closeBrowser()
