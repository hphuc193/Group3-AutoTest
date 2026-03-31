import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Task_Edit - Data-driven Task Edit Test
 * Variables bound from CSV: scenario, newTitle, changeStatus, expectedResult
 * Flow: Add task first -> Sort ID desc -> Edit that task
 */

WebUI.comment(">>> Running: ${scenario}")

String ts = String.valueOf(System.currentTimeMillis())

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Tasks via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Tasks'))
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Tasks/table_Tasks'), GlobalVariable.timeout)

// === Step 1: Add a task first ===
WebUI.click(findTestObject('Page_Tasks/btn_AddTask'))
WebUI.waitForElementPresent(findTestObject('Page_Tasks/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.delay(1)

String originalTitle = "EditTask_${ts}"
WebUI.clearText(findTestObject('Page_Tasks/input_Title'))
WebUI.setText(findTestObject('Page_Tasks/input_Title'), originalTitle)

// Assign to second option (real person)
WebUI.click(findTestObject('Page_Tasks/select2_AssignTo'))
WebUI.delay(1)
def driver = DriverFactory.getWebDriver()
def assignOptions = driver.findElements(org.openqa.selenium.By.cssSelector('.select2-drop-active .select2-results .select2-result'))
if (assignOptions.size() > 1) {
	assignOptions.get(1).click()
} else {
	assignOptions.get(0).click()
}
WebUI.delay(1)

WebUI.click(findTestObject('Page_Tasks/btn_Save'))
WebUI.delay(1)

// === Step 2: Go back to tasks list, sort by ID desc ===
WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/tasks/all_tasks')
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Tasks/table_Tasks'), GlobalVariable.timeout)

boolean alreadyDesc = WebUI.verifyElementPresent(findTestObject('Page_Tasks/th_ID_Desc'), 3, FailureHandling.OPTIONAL)
if (!alreadyDesc) {
	WebUI.click(findTestObject('Page_Tasks/th_ID'))
	WebUI.delay(1)
	if (!WebUI.verifyElementPresent(findTestObject('Page_Tasks/th_ID_Desc'), 3, FailureHandling.OPTIONAL)) {
		WebUI.click(findTestObject('Page_Tasks/th_ID'))
		WebUI.delay(1)
	}
}

// === Step 3: Click edit on first row ===
WebUI.click(findTestObject('Page_Tasks/btn_EditFirstTask'))
WebUI.waitForElementPresent(findTestObject('Page_Tasks/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.waitForElementPresent(findTestObject('Page_Tasks/input_Title'), GlobalVariable.timeout)

// === Step 4: Modify fields ===
String actualTitle = newTitle.replace('{timestamp}', ts)

// Only modify title if this isn't a status-only change
if (changeStatus != 'yes') {
	WebUI.clearText(findTestObject('Page_Tasks/input_Title'))
	if (newTitle != '') {
		WebUI.setText(findTestObject('Page_Tasks/input_Title'), actualTitle)
	}
}

// Change status to "Done" (if applicable)
if (changeStatus == 'yes') {
	WebUI.click(findTestObject('Page_Tasks/select2_Status'))
	WebUI.delay(1)
	// Type "Done" to filter and select
	def statusSearch = driver.findElement(org.openqa.selenium.By.cssSelector('.select2-drop-active input.select2-input'))
	statusSearch.sendKeys('Done')
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Tasks/select2_StatusResult'))
	WebUI.delay(1)
}

// Click Save
WebUI.click(findTestObject('Page_Tasks/btn_Save'))
WebUI.delay(1)

// === Step 5: Verify result ===
if (expectedResult == 'success') {
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/tasks/all_tasks')
	WebUI.delay(1)

	def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput.clear()
	searchInput.sendKeys(actualTitle)
	WebUI.delay(1)

	String pageSource = driver.getPageSource()
	assert pageSource.contains(actualTitle) : "Task '${actualTitle}' not found after edit"
	WebUI.comment("PASSED: ${scenario} - Task edited to '${actualTitle}'")

} else if (expectedResult == 'modal_stays_open') {
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Tasks/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")

} else if (expectedResult == 'status_done') {
	// Navigate back to tasks list
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/tasks/all_tasks')
	WebUI.delay(1)

	// Click "Recently Updated" to show all tasks including Done
	WebUI.click(findTestObject('Page_Tasks/btn_RecentlyUpdated'))
	WebUI.delay(1)

	// Search for original title
	def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput.clear()
	searchInput.sendKeys(originalTitle)
	WebUI.delay(1)

	// Check Status column shows "Done"
	String firstRowText = driver.findElement(org.openqa.selenium.By.cssSelector('table#task-table tbody tr:first-child')).getText()
	assert firstRowText.contains('Done') : "Expected status 'Done' but row shows: ${firstRowText}"
	WebUI.comment("PASSED: ${scenario} - Task status changed to Done")
}

WebUI.closeBrowser()
