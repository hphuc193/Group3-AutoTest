import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Lead_View - Data-driven Lead View Test
 * Variables bound from CSV: scenario, action, expectedResult
 * TC1: Click lead name, verify detail page sections
 * TC2: Add lead, convert to client, verify client page
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Leads via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Leads'))
WebUI.delay(2)

def driver = DriverFactory.getWebDriver()

if (action == 'view') {
	// === TC_LEAD_VIEW_001: Click first lead, verify detail page ===
	WebUI.click(findTestObject('Page_Leads/link_FirstLeadName'))
	WebUI.delay(2)

	// Verify URL contains leads/view
	String currentUrl = WebUI.getUrl()
	assert currentUrl.contains('leads/view') : "Expected lead detail URL but got: ${currentUrl}"

	// Verify key sections visible
	String pageSource = driver.getPageSource()
	assert pageSource.contains('Contacts') : "Contacts section not found"
	assert pageSource.contains('Events') : "Events section not found"
	assert pageSource.contains('Tasks') : "Tasks section not found"
	assert pageSource.contains('Notes') : "Notes section not found"

	// Verify "Convert to client" button visible
	WebUI.verifyElementPresent(findTestObject('Page_Leads/btn_ConvertToClient'), GlobalVariable.timeout)
	WebUI.comment("PASSED: ${scenario} - Lead detail page shows all sections")

} else if (action == 'convert') {
	// === TC_LEAD_VIEW_002: Add lead, then convert to client ===

	// Add a fresh lead to convert
	WebUI.click(findTestObject('Page_Leads/btn_AddLead'))
	WebUI.waitForElementPresent(findTestObject('Page_Leads/modal_AjaxModal'), GlobalVariable.timeout)
	WebUI.waitForElementPresent(findTestObject('Page_Leads/input_CompanyName'), GlobalVariable.timeout)

	String leadName = "ConvertLead_${System.currentTimeMillis()}"
	WebUI.setText(findTestObject('Page_Leads/input_CompanyName'), leadName)
	WebUI.click(findTestObject('Page_Leads/btn_Save'))
	WebUI.delay(3)

	// Go back to leads list and find the new lead
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/leads')
	WebUI.delay(2)

	// Search for the lead
	WebUI.click(findTestObject('Page_Leads/input_Search'))
	WebUI.clearText(findTestObject('Page_Leads/input_Search'))
	WebUI.setText(findTestObject('Page_Leads/input_Search'), leadName)
	WebUI.delay(2)

	// Click the lead name to open detail
	WebUI.click(findTestObject('Page_Leads/link_FirstLeadName'))
	WebUI.delay(2)

	// Click "Convert to client"
	WebUI.click(findTestObject('Page_Leads/btn_ConvertToClient'))
	WebUI.delay(2)

	// Click Next if present (multi-step modal), otherwise Save directly
	boolean hasNext = WebUI.verifyElementPresent(findTestObject('Page_Leads/btn_NextConvert'), 3, FailureHandling.OPTIONAL)
	if (hasNext) {
		WebUI.click(findTestObject('Page_Leads/btn_NextConvert'))
		WebUI.delay(2)
	}

	// Click Save to finalize conversion
	WebUI.click(findTestObject('Page_Leads/btn_SaveConvert'))
	WebUI.delay(3)

	// Verify redirected to client view page
	String currentUrl = WebUI.getUrl()
	assert currentUrl.contains('clients/view') : "Expected client view URL but got: ${currentUrl}"

	// Verify lead name appears on client page
	String pageSource = driver.getPageSource()
	assert pageSource.contains(leadName) : "Lead name '${leadName}' not found on client page"
	WebUI.comment("PASSED: ${scenario} - Lead '${leadName}' converted to client")
}

WebUI.closeBrowser()
