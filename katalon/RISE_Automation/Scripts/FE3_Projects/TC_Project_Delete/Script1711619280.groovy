import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Project_Delete - Data-driven Project Delete Test
 * Variables bound from CSV: scenario, action, expectedResult
 * Flow: Add a project first -> Sort ID desc -> Delete (confirm or cancel)
 * Uses cookie auth (no CAPTCHA needed)
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Projects via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Projects'))
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Projects/table_Projects'), GlobalVariable.timeout)

// === Step 1: Add a new project first (so we have something to delete) ===
WebUI.click(findTestObject('Page_Projects/btn_AddProject'))
WebUI.waitForElementPresent(findTestObject('Page_Projects/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Projects/input_Title'), GlobalVariable.timeout)

String projectTitle = "DeleteTest_${System.currentTimeMillis()}"
WebUI.setText(findTestObject('Page_Projects/input_Title'), projectTitle)

// Select a client
WebUI.click(findTestObject('Page_Projects/select2_Client'))
WebUI.delay(1)
def driver = DriverFactory.getWebDriver()
def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('.select2-drop-active input.select2-input'))
searchInput.sendKeys('Birdie Erdman')
WebUI.delay(1)
WebUI.click(findTestObject('Page_Projects/select2_ClientResult'))
WebUI.delay(1)

WebUI.click(findTestObject('Page_Projects/btn_Save'))
WebUI.delay(1)

// === Step 2: Go back to project list ===
WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/projects/all_projects')
WebUI.delay(1)
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

// === Step 4: Click delete on first row (our newly created project) ===
WebUI.click(findTestObject('Page_Projects/btn_DeleteFirstProject'))
WebUI.waitForElementPresent(findTestObject('Page_Projects/modal_Confirmation'), GlobalVariable.timeout)

// === Step 5: Confirm or Cancel ===
if (action == 'confirm') {
	WebUI.click(findTestObject('Page_Projects/btn_ConfirmDelete'))
	WebUI.delay(1)

	// Verify: success alert with "deleted"
	WebUI.verifyElementPresent(findTestObject('Page_Projects/alert_Success'), GlobalVariable.timeout)
	WebUI.comment("PASSED: ${scenario} - Project deleted successfully")

} else if (action == 'cancel') {
	WebUI.click(findTestObject('Page_Projects/btn_CancelDelete'))
	WebUI.delay(1)

	// Reload and search for project to verify it still exists
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/projects/all_projects')
	WebUI.delay(1)
	WebUI.waitForElementPresent(findTestObject('Page_Projects/table_Projects'), GlobalVariable.timeout)

	def searchBox = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchBox.clear()
	searchBox.sendKeys(projectTitle)
	WebUI.delay(2)

	String pageSource = driver.getPageSource()
	assert pageSource.contains(projectTitle) : "Project '${projectTitle}' not found after cancel - it was deleted!"
	WebUI.comment("PASSED: ${scenario} - Project '${projectTitle}' still exists after cancel")
}

WebUI.closeBrowser()
