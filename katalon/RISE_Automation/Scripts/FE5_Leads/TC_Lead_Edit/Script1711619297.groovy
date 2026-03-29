import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Lead_Edit - Data-driven Lead Edit Test
 * Variables bound from CSV: scenario, newName, changeStatus, expectedResult
 * Flow: Add lead first -> Sort ID desc -> Edit that lead
 */

WebUI.comment(">>> Running: ${scenario}")

String ts = String.valueOf(System.currentTimeMillis())

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

String originalName = "EditLead_${ts}"
WebUI.setText(findTestObject('Page_Leads/input_CompanyName'), originalName)
WebUI.click(findTestObject('Page_Leads/btn_Save'))
WebUI.delay(3)

// === Step 2: Go back to leads list, sort Created at desc (only if not already desc) ===
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

// === Step 3: Click edit on first row ===
WebUI.click(findTestObject('Page_Leads/btn_EditFirstLead'))
WebUI.waitForElementPresent(findTestObject('Page_Leads/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Leads/input_CompanyName'), GlobalVariable.timeout)

// === Step 4: Modify fields ===
String actualName = newName.replace('{timestamp}', ts)

// Only modify name if this isn't a status-only change
if (changeStatus != 'yes') {
	WebUI.clearText(findTestObject('Page_Leads/input_CompanyName'))
	if (newName != '') {
		WebUI.setText(findTestObject('Page_Leads/input_CompanyName'), actualName)
	}
}

// Change status to "Negotiation" (if applicable)
if (changeStatus == 'yes') {
	WebUI.click(findTestObject('Page_Leads/select2_Status'))
	WebUI.delay(1)
	// Type "Negotiation" to filter and select it
	def driver2 = DriverFactory.getWebDriver()
	def statusSearch = driver2.findElement(org.openqa.selenium.By.cssSelector('.select2-drop-active input.select2-input'))
	statusSearch.sendKeys('Negotiation')
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Leads/select2_StatusResult'))
	WebUI.delay(1)
}

// Click Save
WebUI.click(findTestObject('Page_Leads/btn_Save'))
WebUI.delay(3)

// === Step 5: Verify result ===
def driver = DriverFactory.getWebDriver()

if (expectedResult == 'success') {
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/leads')
	WebUI.delay(2)

	def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput.clear()

	// Search by new name or original name (for status change)
	String searchName = (newName != '') ? actualName : originalName
	searchInput.sendKeys(searchName)
	WebUI.delay(2)

	String pageSource = driver.getPageSource()
	assert pageSource.contains(searchName) : "Lead '${searchName}' not found after edit"

	// For status change, verify the status badge shows "Negotiation"
	if (changeStatus == 'yes') {
		// Check the status badge in first row
		String firstRowText = driver.findElement(org.openqa.selenium.By.cssSelector('table#lead-table tbody tr:first-child')).getText()
		assert firstRowText.contains('Negotiation') : "Expected status 'Negotiation' but row shows: ${firstRowText}"
		WebUI.comment("PASSED: ${scenario} - Status changed to Negotiation")
	} else {
		WebUI.comment("PASSED: ${scenario} - Lead edited successfully")
	}

} else if (expectedResult == 'modal_stays_open') {
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Leads/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")
}

WebUI.closeBrowser()
