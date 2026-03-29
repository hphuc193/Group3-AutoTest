import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Client_View - View Client Detail Page
 * Covers: TC_CLIENT_VIEW_001
 * Click a client name, verify detail page with Overview, Projects, Invoices, Client info.
 */

WebUI.comment(">>> Running: TC_CLIENT_VIEW_001 - View client details")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Clients via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Clients'))
WebUI.delay(2)
WebUI.click(findTestObject('Page_Clients/tab_ClientsList'))
WebUI.waitForElementPresent(findTestObject('Page_Clients/table_Clients'), GlobalVariable.timeout)

// Click on first client name link
WebUI.click(findTestObject('Page_Clients/link_FirstClientName'))
WebUI.delay(2)

// Verify URL contains clients/view
def driver = DriverFactory.getWebDriver()
String currentUrl = WebUI.getUrl()
assert currentUrl.contains('clients/view') : "Expected client detail URL but got: ${currentUrl}"

// Verify key tabs/sections are visible
String pageSource = driver.getPageSource()
assert pageSource.contains('Overview') : "Overview tab not found"
assert pageSource.contains('Projects') : "Projects tab not found"
assert pageSource.contains('Invoices') : "Invoices tab not found"
assert pageSource.contains('Payments') : "Payments tab not found"
assert pageSource.contains('Client info') : "Client info section not found"

WebUI.comment("PASSED: TC_CLIENT_VIEW_001 - Client detail page shows all tabs and sections")

WebUI.closeBrowser()
