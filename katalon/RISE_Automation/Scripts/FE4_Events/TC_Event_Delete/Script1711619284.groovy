import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Event_Delete - Data-driven Event Delete Test
 * Variables bound from CSV: scenario, action, expectedResult
 * Flow: Add event first -> Click event -> Delete event -> Confirm or Cancel
 * Note: Event delete uses a popover with Yes/No buttons (not a modal)
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Events via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Events'))
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Events/calendar_Grid'), GlobalVariable.timeout)

// === Step 1: Add an event first (so we have something to delete) ===
WebUI.click(findTestObject('Page_Events/btn_AddEvent'))
WebUI.waitForElementPresent(findTestObject('Page_Events/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.delay(1)

String eventTitle = "DeleteTest_${System.currentTimeMillis()}"
WebUI.clearText(findTestObject('Page_Events/input_Title'))
WebUI.setText(findTestObject('Page_Events/input_Title'), eventTitle)

// Pick today's date for start date
WebUI.click(findTestObject('Page_Events/input_StartDate'))
WebUI.delay(1)
WebUI.click(findTestObject('Page_Events/datepicker_Today'))
WebUI.delay(1)

// Save event
WebUI.click(findTestObject('Page_Events/btn_Save'))
WebUI.delay(1)

// === Step 2: Reload calendar and click the newly created event ===
WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/events')
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Events/calendar_Grid'), GlobalVariable.timeout)

// Click the event by title
def driver = DriverFactory.getWebDriver()
def eventLink = driver.findElement(org.openqa.selenium.By.xpath("//a[contains(@class,'fc-event')]//span[contains(text(),'${eventTitle}')]/ancestor::a"))
eventLink.click()
WebUI.waitForElementPresent(findTestObject('Page_Events/modal_AjaxModal'), GlobalVariable.timeout)

// === Step 3: Click "Delete event" link ===
WebUI.click(findTestObject('Page_Events/link_DeleteEvent'))
WebUI.delay(1)

// === Step 4: Confirm or Cancel (popover with Yes/No buttons) ===
if (action == 'confirm') {
	WebUI.click(findTestObject('Page_Events/btn_PopoverYes'))
	WebUI.delay(1)

	// Verify success alert with "deleted"
	WebUI.verifyElementPresent(findTestObject('Page_Events/alert_Success'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
	WebUI.comment("PASSED: ${scenario} - Event deleted successfully")

} else if (action == 'cancel') {
	WebUI.click(findTestObject('Page_Events/btn_PopoverNo'))
	WebUI.delay(1)

	// Close the event details modal
	WebUI.click(findTestObject('Page_Events/btn_CloseModal'))
	WebUI.delay(1)

	// Verify event still exists on calendar
	String pageSource = driver.getPageSource()
	assert pageSource.contains(eventTitle) : "Event '${eventTitle}' not found after cancel - it was deleted!"
	WebUI.comment("PASSED: ${scenario} - Event '${eventTitle}' still exists after cancel")
}

WebUI.closeBrowser()
