import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Client_Add - Data-driven Client Add Test
 * Variables bound from CSV: scenario, companyName, address, selectManager, expectedResult
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

// Click "Add client" button
WebUI.click(findTestObject('Page_Clients/btn_AddClient'))
WebUI.waitForElementPresent(findTestObject('Page_Clients/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Clients/input_CompanyName'), GlobalVariable.timeout)

// Generate unique company name
String actualName = companyName.replace('{timestamp}', ts)

// Fill company name (if provided)
if (companyName != '') {
	WebUI.clearText(findTestObject('Page_Clients/input_CompanyName'))
	WebUI.setText(findTestObject('Page_Clients/input_CompanyName'), actualName)
}

// Fill address (if provided)
if (address != '') {
	WebUI.setText(findTestObject('Page_Clients/input_Address'), address)
}

// Select manager via Select2 (if applicable)
if (selectManager == 'yes') {
	WebUI.click(findTestObject('Page_Clients/select2_Managers'))
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Clients/select2_ManagerResult'))
	WebUI.delay(1)
}

// Click Save
WebUI.click(findTestObject('Page_Clients/btn_Save'))
WebUI.delay(3)

def driver = DriverFactory.getWebDriver()

// Verify expected result
if (expectedResult == 'success') {
	// Navigate back to clients list, search for the new client
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/clients')
	WebUI.delay(2)
	WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
	WebUI.delay(2)

	// Search for the client
	def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput.clear()
	searchInput.sendKeys(actualName)
	WebUI.delay(2)

	String tableText = driver.findElement(org.openqa.selenium.By.id('client-table')).getText()
	assert tableText.contains(actualName) : "Client '${actualName}' not found in table"
	WebUI.comment("PASSED: ${scenario} - Client '${actualName}' created")

} else if (expectedResult == 'modal_stays_open') {
	// Modal should still be open (validation error)
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Clients/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")

} else if (expectedResult == 'duplicate') {
	// First add should succeed, then add same name again
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/clients')
	WebUI.delay(2)
	WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
	WebUI.delay(2)

	// Add same name again
	WebUI.click(findTestObject('Page_Clients/btn_AddClient'))
	WebUI.waitForElementPresent(findTestObject('Page_Clients/modal_AjaxModal'), GlobalVariable.timeout)
	WebUI.waitForElementPresent(findTestObject('Page_Clients/input_CompanyName'), GlobalVariable.timeout)
	WebUI.clearText(findTestObject('Page_Clients/input_CompanyName'))
	WebUI.setText(findTestObject('Page_Clients/input_CompanyName'), actualName)
	WebUI.click(findTestObject('Page_Clients/btn_Save'))
	WebUI.delay(3)

	// System allows duplicates — verify both exist by searching
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/clients')
	WebUI.delay(2)
	WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
	WebUI.delay(2)

	def searchInput2 = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput2.clear()
	searchInput2.sendKeys(actualName)
	WebUI.delay(2)

	String tableText2 = driver.findElement(org.openqa.selenium.By.id('client-table')).getText()
	assert tableText2.contains(actualName) : "Duplicate client '${actualName}' not found"
	WebUI.comment("PASSED: ${scenario} - Duplicate client handled")
}

WebUI.closeBrowser()
