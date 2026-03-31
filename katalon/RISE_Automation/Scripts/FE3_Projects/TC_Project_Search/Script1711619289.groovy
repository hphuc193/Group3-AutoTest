import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Project_Search - Data-driven Project Search Test
 * Variables bound from CSV: scenario, keyword, expectedResult
 * Uses cookie auth, searches via DataTable search box
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Projects via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Projects'))
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Projects/table_Projects'), GlobalVariable.timeout)

// Type keyword into DataTable search box
WebUI.click(findTestObject('Page_Projects/input_Search'))
WebUI.clearText(findTestObject('Page_Projects/input_Search'))
WebUI.setText(findTestObject('Page_Projects/input_Search'), keyword)
WebUI.delay(1)

// Verify result
def driver = DriverFactory.getWebDriver()

if (expectedResult == 'has_results') {
	// Table should contain the keyword
	int rowCount = driver.findElements(org.openqa.selenium.By.cssSelector('table#project-table tbody tr')).size()
	assert rowCount > 0 : "Expected results for '${keyword}' but got 0 rows"

	String tableText = driver.findElement(org.openqa.selenium.By.id('project-table')).getText()
	assert tableText.contains(keyword) : "Keyword '${keyword}' not found in table"
	WebUI.comment("PASSED: ${scenario} - Found ${rowCount} results for '${keyword}'")

} else if (expectedResult == 'no_results') {
	// Table should show empty or "No matching records"
	String tbodyText = driver.findElement(org.openqa.selenium.By.cssSelector('table#project-table tbody')).getText()
	boolean isEmpty = tbodyText.contains('No matching') || tbodyText.contains('No data') || tbodyText.contains('No record') || tbodyText.trim() == ''
	assert isEmpty : "Expected no results for '${keyword}' but table has content: ${tbodyText}"
	WebUI.comment("PASSED: ${scenario} - No results for '${keyword}'")
}

WebUI.closeBrowser()
