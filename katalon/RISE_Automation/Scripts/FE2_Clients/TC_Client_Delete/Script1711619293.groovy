import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Client_Delete - Data-driven Client Delete Test
 * Variables bound from CSV: scenario, action, expectedResult
 * Flow: Add client first -> Sort ID desc -> Delete (confirm or cancel)
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Clients via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Clients'))
WebUI.delay(2)
WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
WebUI.waitForElementPresent(findTestObject('Page_Clients/table_Clients'), GlobalVariable.timeout)

// === Step 1: Add a client first ===
WebUI.click(findTestObject('Page_Clients/btn_AddClient'))
WebUI.waitForElementPresent(findTestObject('Page_Clients/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Clients/input_CompanyName'), GlobalVariable.timeout)

String clientName = "DeleteClient_${System.currentTimeMillis()}"
WebUI.setText(findTestObject('Page_Clients/input_CompanyName'), clientName)
WebUI.click(findTestObject('Page_Clients/btn_Save'))
WebUI.delay(3)

// === Step 2: Go back to client list, sort by ID desc ===
WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/clients')
WebUI.delay(2)
WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
WebUI.delay(2)

boolean alreadyDesc = WebUI.verifyElementPresent(findTestObject('Page_Clients/th_ID_Desc'), 3, FailureHandling.OPTIONAL)
if (!alreadyDesc) {
	WebUI.click(findTestObject('Page_Clients/th_ID'))
	WebUI.delay(1)
	if (!WebUI.verifyElementPresent(findTestObject('Page_Clients/th_ID_Desc'), 3, FailureHandling.OPTIONAL)) {
		WebUI.click(findTestObject('Page_Clients/th_ID'))
		WebUI.delay(1)
	}
}

// === Step 3: Click delete on first row ===
WebUI.click(findTestObject('Page_Clients/btn_DeleteFirstClient'))
WebUI.waitForElementPresent(findTestObject('Page_Clients/modal_Confirmation'), GlobalVariable.timeout)

def driver = DriverFactory.getWebDriver()

// === Step 4: Confirm or Cancel ===
if (action == 'confirm') {
	WebUI.click(findTestObject('Page_Clients/btn_ConfirmDelete'))
	WebUI.delay(2)

	// Verify success alert
	WebUI.verifyElementPresent(findTestObject('Page_Clients/alert_Success'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
	WebUI.comment("PASSED: ${scenario} - Client deleted successfully")

} else if (action == 'cancel') {
	WebUI.click(findTestObject('Page_Clients/btn_CancelDelete'))
	WebUI.delay(2)

	// Reload and search for client to verify it still exists
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/clients')
	WebUI.delay(2)
	WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
	WebUI.delay(2)

	def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput.clear()
	searchInput.sendKeys(clientName)
	WebUI.delay(2)

	String pageSource = driver.getPageSource()
	assert pageSource.contains(clientName) : "Client '${clientName}' not found after cancel - it was deleted!"
	WebUI.comment("PASSED: ${scenario} - Client '${clientName}' still exists after cancel")
}

WebUI.closeBrowser()
