import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Client_Edit - Data-driven Client Edit Test
 * Variables bound from CSV: scenario, newCompanyName, newAddress, expectedResult
 * Flow: Add client first -> Sort ID desc -> Edit that client
 */

WebUI.comment(">>> Running: ${scenario}")

String ts = String.valueOf(System.currentTimeMillis())

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

String originalName = "EditClient_${ts}"
WebUI.setText(findTestObject('Page_Clients/input_CompanyName'), originalName)
WebUI.click(findTestObject('Page_Clients/btn_Save'))
WebUI.delay(3)

// === Step 2: Go back to client list, sort by ID desc ===
WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/clients')
WebUI.delay(2)
WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
WebUI.delay(2)

// Sort by ID desc (only if not already sorted)
boolean alreadyDesc = WebUI.verifyElementPresent(findTestObject('Page_Clients/th_ID_Desc'), 3, FailureHandling.OPTIONAL)
if (!alreadyDesc) {
	WebUI.click(findTestObject('Page_Clients/th_ID'))
	WebUI.delay(1)
	if (!WebUI.verifyElementPresent(findTestObject('Page_Clients/th_ID_Desc'), 3, FailureHandling.OPTIONAL)) {
		WebUI.click(findTestObject('Page_Clients/th_ID'))
		WebUI.delay(1)
	}
}

// === Step 3: Click edit on first row (our newly created client) ===
WebUI.click(findTestObject('Page_Clients/btn_EditFirstClient'))
WebUI.waitForElementPresent(findTestObject('Page_Clients/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Clients/input_CompanyName'), GlobalVariable.timeout)

// === Step 4: Modify fields ===
String actualName = newCompanyName.replace('{timestamp}', ts)

// Clear company name
WebUI.clearText(findTestObject('Page_Clients/input_CompanyName'))

// Fill new company name (if provided, empty for negative test)
if (newCompanyName != '') {
	WebUI.setText(findTestObject('Page_Clients/input_CompanyName'), actualName)
}

// Fill new address (if provided)
if (newAddress != '') {
	WebUI.clearText(findTestObject('Page_Clients/input_Address'))
	WebUI.setText(findTestObject('Page_Clients/input_Address'), newAddress)
}

// Click Save
WebUI.click(findTestObject('Page_Clients/btn_Save'))
WebUI.delay(3)

// === Step 5: Verify result ===
def driver = DriverFactory.getWebDriver()

if (expectedResult == 'success') {
	// Search for updated client
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/clients')
	WebUI.delay(2)
	WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
	WebUI.delay(2)

	def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput.clear()
	searchInput.sendKeys(actualName)
	WebUI.delay(2)

	String tableText = driver.findElement(org.openqa.selenium.By.id('client-table')).getText()
	assert tableText.contains(actualName) : "Updated client '${actualName}' not found"
	WebUI.comment("PASSED: ${scenario} - Client updated to '${actualName}'")

} else if (expectedResult == 'modal_stays_open') {
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Clients/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")
}

WebUI.closeBrowser()
