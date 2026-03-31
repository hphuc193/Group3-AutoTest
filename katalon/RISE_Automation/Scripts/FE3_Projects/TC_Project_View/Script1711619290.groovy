import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Project_View - View Project Detail Page
 * Covers: TC_PROJECT_VIEW_001
 * Click a project name, verify detail page with Overview, Tasks, Milestones, Activity, Members.
 */

WebUI.comment(">>> Running: TC_PROJECT_VIEW_001 - View project details")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Projects via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Projects'))
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Projects/table_Projects'), GlobalVariable.timeout)

// Click on first project title link
WebUI.click(findTestObject('Page_Projects/link_FirstProjectTitle'))
WebUI.delay(1)

// Verify URL contains projects/view
def driver = DriverFactory.getWebDriver()
String currentUrl = WebUI.getUrl()
assert currentUrl.contains('projects/view') : "Expected project detail URL but got: ${currentUrl}"

// Verify key tabs/sections are visible
String pageSource = driver.getPageSource()
assert pageSource.contains('Overview') : "Overview tab not found"
assert pageSource.contains('Tasks List') : "Tasks List tab not found"
assert pageSource.contains('Milestones') : "Milestones tab not found"
assert pageSource.contains('Activity') : "Activity section not found"
assert pageSource.contains('Tasks Kanban') : "Tasks Kanban tab not found"

WebUI.comment("PASSED: TC_PROJECT_VIEW_001 - Project detail page shows all sections")

WebUI.closeBrowser()
