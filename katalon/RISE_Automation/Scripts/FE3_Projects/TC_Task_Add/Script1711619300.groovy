import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Task_Add - Data-driven Task Add Test
 * Variables bound from CSV: scenario, taskTitle, selectAssignee, expectedResult
 * Uses cookie auth, navigates to Tasks page (sidebar)
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

// Click "Add task" button
WebUI.click(findTestObject('Page_Tasks/btn_AddTask'))
WebUI.waitForElementPresent(findTestObject('Page_Tasks/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.delay(1)

// Generate unique title
String actualTitle = taskTitle.replace('{timestamp}', ts)

// Fill title (if provided)
if (taskTitle != '') {
	WebUI.clearText(findTestObject('Page_Tasks/input_Title'))
	WebUI.setText(findTestObject('Page_Tasks/input_Title'), actualTitle)
}

// Select assignee via Select2
if (selectAssignee == 'yes') {
	// Pick the second option (actual person, skip first "-" option)
	WebUI.click(findTestObject('Page_Tasks/select2_AssignTo'))
	WebUI.delay(1)
	def driver0 = DriverFactory.getWebDriver()
	def assignOptions = driver0.findElements(org.openqa.selenium.By.cssSelector('.select2-drop-active .select2-results .select2-result'))
	if (assignOptions.size() > 1) {
		assignOptions.get(1).click()  // Second option (first real person)
	} else {
		assignOptions.get(0).click()  // Fallback to first if only one
	}
	WebUI.delay(1)
} else if (selectAssignee == 'none') {
	// Explicitly select "-" (no assignee) - first option
	WebUI.click(findTestObject('Page_Tasks/select2_AssignTo'))
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Tasks/select2_AssignToResult'))
	WebUI.delay(1)
}

// Click Save
WebUI.click(findTestObject('Page_Tasks/btn_Save'))
WebUI.delay(1)

def driver = DriverFactory.getWebDriver()

// Verify result
if (expectedResult == 'success') {
	// Navigate back to tasks list and search
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/tasks/all_tasks')
	WebUI.delay(1)

	def searchInput = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput.clear()
	searchInput.sendKeys(actualTitle)
	WebUI.delay(1)

	String pageSource = driver.getPageSource()
	assert pageSource.contains(actualTitle) : "Task '${actualTitle}' not found in tasks list"
	WebUI.comment("PASSED: ${scenario} - Task '${actualTitle}' created")

} else if (expectedResult == 'modal_stays_open') {
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Tasks/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")

} else if (expectedResult == 'no_record') {
	// Task created but with no assignee — won't show in "My Tasks" search
	WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/tasks/all_tasks')
	WebUI.delay(1)

	def searchInput2 = driver.findElement(org.openqa.selenium.By.cssSelector('input[type="search"]'))
	searchInput2.clear()
	searchInput2.sendKeys(actualTitle)
	WebUI.delay(1)

	String tbodyText = driver.findElement(org.openqa.selenium.By.cssSelector('table#task-table tbody')).getText()
	boolean noRecord = tbodyText.contains('No record') || tbodyText.contains('No matching') || tbodyText.contains('No data')
	assert noRecord : "Expected 'No record found' for unassigned task but got: ${tbodyText}"
	WebUI.comment("PASSED: ${scenario} - Task with no assignee shows 'No record found'")
}

WebUI.closeBrowser()
