import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Client_Filter - Data-driven Client Filter Test
 * Variables bound from CSV: scenario, filterName, expectedResult
 * Uses cookie auth (no CAPTCHA needed)
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies (skip login/CAPTCHA)
AuthHelper.loadAuth('admin')

// Verify on dashboard after auth
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)

// Navigate to Clients via sidebar link
WebUI.click(findTestObject('Page_Dashboard/link_Clients'))
WebUI.delay(1)

// Click the Clients list tab
WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
WebUI.waitForElementPresent(findTestObject('Page_Clients/table_Clients'), GlobalVariable.timeout)

if (filterName == 'clear_filter') {
	// FILTER_003: Apply a filter first, then clear it
	// Apply "Has open projects" filter
	WebUI.click(findTestObject('Page_Clients/btn_Filter_HasOpenProjects'))
	WebUI.delay(1)

	// Count filtered rows
	def driver = DriverFactory.getWebDriver()
	int filteredCount = driver.findElements(org.openqa.selenium.By.cssSelector('table#client-table tbody tr')).size()
	WebUI.comment("Filtered row count: ${filteredCount}")

	// Clear filter - click dropdown toggle then "All clients"
	WebUI.click(findTestObject('Page_Clients/btn_FilterDropdown'))
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Clients/link_AllClients'))
	WebUI.delay(1)

	// Verify all clients shown (count >= filtered count)
	int allCount = driver.findElements(org.openqa.selenium.By.cssSelector('table#client-table tbody tr')).size()
	assert allCount >= filteredCount : "Expected all clients (${allCount}) >= filtered (${filteredCount})"
	WebUI.comment("PASSED: ${scenario} - All: ${allCount}, Filtered: ${filteredCount}")

} else {
	// FILTER_001 & FILTER_002: Apply named filter, verify rows exist
	if (filterName == 'Has open projects') {
		WebUI.click(findTestObject('Page_Clients/btn_Filter_HasOpenProjects'))
	} else if (filterName == 'Has due') {
		WebUI.click(findTestObject('Page_Clients/btn_Filter_HasDue'))
	}
	WebUI.delay(1)

	// Verify table has rows
	def driver = DriverFactory.getWebDriver()
	int rowCount = driver.findElements(org.openqa.selenium.By.cssSelector('table#client-table tbody tr')).size()
	assert rowCount > 0 : "Expected rows after filtering by '${filterName}' but got 0"

	// Verify the filter button is active/visible
	WebUI.verifyElementPresent(findTestObject('Page_Clients/btn_ActiveFilter'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
	WebUI.comment("PASSED: ${scenario} - ${rowCount} rows found")
}

WebUI.closeBrowser()
