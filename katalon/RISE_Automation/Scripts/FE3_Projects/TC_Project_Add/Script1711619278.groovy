import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Project_Add - Data-driven Project Add Test
 * Variables bound from CSV: scenario, title, clientName, startDate, expectedResult
 * Uses cookie auth (no CAPTCHA needed)
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to dashboard then Projects via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Projects'))
WebUI.delay(1)

// Wait for project table to load
WebUI.waitForElementPresent(findTestObject('Page_Projects/table_Projects'), GlobalVariable.timeout)

// Click Add Project button
WebUI.click(findTestObject('Page_Projects/btn_AddProject'))
WebUI.waitForElementPresent(findTestObject('Page_Projects/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Projects/input_Title'), GlobalVariable.timeout)

// Generate unique title with timestamp
String actualTitle = title.replace('{timestamp}', String.valueOf(System.currentTimeMillis()))

// Fill title (if provided)
if (title != '') {
	WebUI.clearText(findTestObject('Page_Projects/input_Title'))
	WebUI.setText(findTestObject('Page_Projects/input_Title'), actualTitle)
}

// Select client via Select2 dropdown (if provided)
if (clientName != '') {
	WebUI.click(findTestObject('Page_Projects/select2_Client'))
	WebUI.delay(1)
	// Type client name in search box
	def driver = DriverFactory.getWebDriver()
	def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('.select2-drop-active input.select2-input'))
	searchInput.sendKeys(clientName)
	WebUI.delay(1)
	// Click the first matching result
	WebUI.click(findTestObject('Page_Projects/select2_ClientResult'))
	WebUI.delay(1)
}

// Set start date (if provided)
if (startDate != '') {
	def driver2 = DriverFactory.getWebDriver()
	driver2.executeScript("document.getElementById('start_date').value = arguments[0]", startDate)
}

// Click Save
WebUI.click(findTestObject('Page_Projects/btn_Save'))
WebUI.delay(1)

// Verify expected result
if (expectedResult == 'success') {
	// Navigate back to project list and verify project exists
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/projects/all_projects')
	WebUI.delay(1)

	// Search for the project by title
	def driver3 = DriverFactory.getWebDriver()
	String pageSource = driver3.getPageSource()
	assert pageSource.contains(actualTitle) : "Project '${actualTitle}' not found in project list"
	WebUI.comment("PASSED: ${scenario} - Project '${actualTitle}' created")

} else if (expectedResult == 'modal_stays_open') {
	// Modal should still be open (validation error)
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Projects/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")
}

WebUI.closeBrowser()
