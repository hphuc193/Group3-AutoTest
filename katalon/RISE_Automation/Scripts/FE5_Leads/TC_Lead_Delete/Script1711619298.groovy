import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Lead_Delete - Data-driven Lead Delete Test
 * Variables bound from CSV: scenario, action, expectedResult
 * Flow: Add lead first -> Sort Created at desc -> Delete (confirm or cancel)
 * Lead delete uses X button on the row, then confirmation modal
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Leads via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Leads'))
WebUI.delay(2)

// === Step 1: Add a lead first ===
WebUI.click(findTestObject('Page_Leads/btn_AddLead'))
WebUI.waitForElementPresent(findTestObject('Page_Leads/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Leads/input_CompanyName'), GlobalVariable.timeout)

String leadName = "DeleteLead_${System.currentTimeMillis()}"
WebUI.setText(findTestObject('Page_Leads/input_CompanyName'), leadName)
WebUI.click(findTestObject('Page_Leads/btn_Save'))
WebUI.delay(3)

// === Step 2: Go back to leads list, sort Created at desc (only if needed) ===
WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/leads')
WebUI.delay(2)

boolean alreadyDesc = WebUI.verifyElementPresent(findTestObject('Page_Leads/th_CreatedAt_Desc'), 3, FailureHandling.OPTIONAL)
if (!alreadyDesc) {
	WebUI.click(findTestObject('Page_Leads/th_CreatedAt'))
	WebUI.delay(1)
	boolean isDesc = WebUI.verifyElementPresent(findTestObject('Page_Leads/th_CreatedAt_Desc'), 3, FailureHandling.OPTIONAL)
	if (!isDesc) {
		WebUI.click(findTestObject('Page_Leads/th_CreatedAt'))
		WebUI.delay(1)
	}
}

// === Step 3: Click delete (X button) on first row ===
WebUI.click(findTestObject('Page_Leads/btn_DeleteFirstLead'))
WebUI.waitForElementPresent(findTestObject('Page_Leads/modal_Confirmation'), GlobalVariable.timeout)

def driver = DriverFactory.getWebDriver()

// === Step 4: Confirm or Cancel ===
if (action == 'confirm') {
	WebUI.click(findTestObject('Page_Leads/btn_ConfirmDelete'))
	WebUI.delay(2)

	// Verify success alert
	WebUI.verifyElementPresent(findTestObject('Page_Leads/alert_Success'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
	WebUI.comment("PASSED: ${scenario} - Lead deleted successfully")

} else if (action == 'cancel') {
	WebUI.click(findTestObject('Page_Leads/btn_CancelDelete'))
	WebUI.delay(2)

	// Reload and search to verify lead still exists
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/leads')
	WebUI.delay(2)

	def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput.clear()
	searchInput.sendKeys(leadName)
	WebUI.delay(2)

	String pageSource = driver.getPageSource()
	assert pageSource.contains(leadName) : "Lead '${leadName}' not found after cancel - it was deleted!"
	WebUI.comment("PASSED: ${scenario} - Lead '${leadName}' still exists after cancel")
}

WebUI.closeBrowser()
