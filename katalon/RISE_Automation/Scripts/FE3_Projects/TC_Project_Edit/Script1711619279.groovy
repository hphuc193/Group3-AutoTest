import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Project_Edit - Data-driven Project Edit Test
 * Variables bound from CSV: scenario, newTitle, deadline, expectedResult
 * Flow: Add a project first -> Sort by ID desc -> Edit the newly created project
 * Uses cookie auth (no CAPTCHA needed)
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Projects via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Projects'))
WebUI.delay(2)
WebUI.waitForElementPresent(findTestObject('Page_Projects/table_Projects'), GlobalVariable.timeout)

// === Step 1: Add a new project first ===
WebUI.click(findTestObject('Page_Projects/btn_AddProject'))
WebUI.waitForElementPresent(findTestObject('Page_Projects/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Projects/input_Title'), GlobalVariable.timeout)

String tempTitle = "EditTest_${System.currentTimeMillis()}"
WebUI.setText(findTestObject('Page_Projects/input_Title'), tempTitle)

// Select a client
WebUI.click(findTestObject('Page_Projects/select2_Client'))
WebUI.delay(1)
def driver = DriverFactory.getWebDriver()
def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('.select2-drop-active input.select2-input'))
searchInput.sendKeys('Birdie Erdman')
WebUI.delay(1)
WebUI.click(findTestObject('Page_Projects/select2_ClientResult'))
WebUI.delay(1)

// Save new project
WebUI.click(findTestObject('Page_Projects/btn_Save'))
WebUI.delay(2)

// === Step 2: Go back to project list ===
WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/projects/all_projects')
WebUI.delay(2)
WebUI.waitForElementPresent(findTestObject('Page_Projects/table_Projects'), GlobalVariable.timeout)

// === Step 3: Sort by ID desc (only if not already sorted) ===
boolean alreadyDesc = WebUI.verifyElementPresent(findTestObject('Page_Projects/th_ID_Desc'), 3, FailureHandling.OPTIONAL)
if (!alreadyDesc) {
	WebUI.click(findTestObject('Page_Projects/th_ID'))
	WebUI.delay(1)
	if (!WebUI.verifyElementPresent(findTestObject('Page_Projects/th_ID_Desc'), 3, FailureHandling.OPTIONAL)) {
		WebUI.click(findTestObject('Page_Projects/th_ID'))
		WebUI.delay(1)
	}
}

// === Step 4: Edit the first project (which is now the newest one we just created) ===
WebUI.click(findTestObject('Page_Projects/btn_EditFirstProject'))
WebUI.waitForElementPresent(findTestObject('Page_Projects/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Projects/input_Title'), GlobalVariable.timeout)

// Generate unique title with timestamp
String actualTitle = newTitle.replace('{timestamp}', String.valueOf(System.currentTimeMillis()))

// Clear title field
WebUI.clearText(findTestObject('Page_Projects/input_Title'))

// Fill new title (if provided, empty for negative test)
if (newTitle != '') {
	WebUI.setText(findTestObject('Page_Projects/input_Title'), actualTitle)
}

// Set deadline (if provided)
if (deadline != '') {
	driver.executeScript("document.getElementById('deadline').value = arguments[0]", deadline)
}

// Click Save
WebUI.click(findTestObject('Page_Projects/btn_Save'))
WebUI.delay(2)

// === Step 5: Verify result ===
if (expectedResult == 'success') {
	// Navigate back and verify updated title exists
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/projects/all_projects')
	WebUI.delay(2)

	String pageSource = driver.getPageSource()
	assert pageSource.contains(actualTitle) : "Updated project '${actualTitle}' not found in project list"
	WebUI.comment("PASSED: ${scenario} - Project updated to '${actualTitle}'")

} else if (expectedResult == 'modal_stays_open') {
	// Modal should still be open (validation error)
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Projects/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")
}

WebUI.closeBrowser()
