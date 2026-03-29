import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Lead_Add - Data-driven Lead Add Test
 * Variables bound from CSV: scenario, leadName, selectOwner, selectSource, selectManager, expectedResult
 * Uses cookie auth
 */

WebUI.comment(">>> Running: ${scenario}")

String ts = String.valueOf(System.currentTimeMillis())

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Leads via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Leads'))
WebUI.delay(2)

// Click "Add lead" button
WebUI.click(findTestObject('Page_Leads/btn_AddLead'))
WebUI.waitForElementPresent(findTestObject('Page_Leads/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Leads/input_CompanyName'), GlobalVariable.timeout)

// Generate unique name
String actualName = leadName.replace('{timestamp}', ts)

// Fill lead name (if provided)
if (leadName != '') {
	WebUI.clearText(findTestObject('Page_Leads/input_CompanyName'))
	WebUI.setText(findTestObject('Page_Leads/input_CompanyName'), actualName)
}

// Select Owner via Select2 (if applicable)
if (selectOwner == 'yes') {
	WebUI.click(findTestObject('Page_Leads/select2_Owner'))
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Leads/select2_OwnerResult'))
	WebUI.delay(1)
}

// Select Source via Select2 (if applicable)
if (selectSource == 'yes') {
	WebUI.click(findTestObject('Page_Leads/select2_Source'))
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Leads/select2_SourceResult'))
	WebUI.delay(1)
}

// Select Manager via Select2 multi-select (if applicable)
if (selectManager == 'yes') {
	WebUI.click(findTestObject('Page_Leads/select2_Managers'))
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Leads/select2_ManagerResult'))
	WebUI.delay(1)
}

// Click Save
WebUI.click(findTestObject('Page_Leads/btn_Save'))
WebUI.delay(3)

def driver = DriverFactory.getWebDriver()

// Verify expected result
if (expectedResult == 'success') {
	// Navigate back to leads list, search for the new lead
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/leads')
	WebUI.delay(2)

	def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput.clear()
	searchInput.sendKeys(actualName)
	WebUI.delay(2)

	String pageSource = driver.getPageSource()
	assert pageSource.contains(actualName) : "Lead '${actualName}' not found in leads list"
	WebUI.comment("PASSED: ${scenario} - Lead '${actualName}' created")

} else if (expectedResult == 'modal_stays_open') {
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Leads/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")
}

WebUI.closeBrowser()
