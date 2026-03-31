import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Event_View - View Event Details on Calendar
 * Covers: TC_EVENT_VIEW_001
 * Click an event on the calendar, verify "Event details" modal appears.
 */

WebUI.comment(">>> Running: TC_EVENT_VIEW_001 - View event details")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Events via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Events'))
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Events/calendar_Grid'), GlobalVariable.timeout)

// Click the first visible event on the calendar
WebUI.click(findTestObject('Page_Events/link_FirstEvent'))
WebUI.waitForElementPresent(findTestObject('Page_Events/modal_AjaxModal'), GlobalVariable.timeout)

// Verify "Event details" heading is shown in modal
WebUI.verifyElementPresent(findTestObject('Page_Events/heading_EventDetails'), GlobalVariable.timeout)
WebUI.comment("PASSED: TC_EVENT_VIEW_001 - Event details modal displayed")

WebUI.closeBrowser()
