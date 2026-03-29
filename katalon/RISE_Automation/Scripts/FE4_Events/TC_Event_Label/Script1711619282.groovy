import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper

/**
 * TC_Event_Label - Data-driven Event Label Test
 * Variables bound from CSV: scenario, labelName, selectColor, expectedResult
 * Uses cookie auth (no CAPTCHA needed)
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Events via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Events'))
WebUI.delay(2)
WebUI.waitForElementPresent(findTestObject('Page_Events/calendar_Grid'), GlobalVariable.timeout)

// Click "Manage labels" button
WebUI.click(findTestObject('Page_Events/btn_ManageLabels'))
WebUI.waitForElementPresent(findTestObject('Page_Events/modal_AjaxModal'), GlobalVariable.timeout)
WebUI.delay(1)

// Select a color (if applicable)
if (selectColor == 'yes') {
	WebUI.click(findTestObject('Page_Events/colorTag_First'))
	WebUI.delay(1)
}

// Generate unique label name
String actualLabelName = labelName.replace('{timestamp}', String.valueOf(System.currentTimeMillis()))

// Fill label name (if provided)
if (labelName != '') {
	WebUI.clearText(findTestObject('Page_Events/input_LabelTitle'))
	WebUI.setText(findTestObject('Page_Events/input_LabelTitle'), actualLabelName)
}

// Click Save
WebUI.click(findTestObject('Page_Events/btn_SaveLabel'))
WebUI.delay(2)

// Verify expected result
if (expectedResult == 'success') {
	// Verify label appears in the modal as a badge
	def driver = DriverFactory.getWebDriver()
	String modalContent = driver.findElement(org.openqa.selenium.By.id('ajaxModal')).getText()
	assert modalContent.contains(actualLabelName) : "Label '${actualLabelName}' not found in modal"
	WebUI.comment("PASSED: ${scenario} - Label '${actualLabelName}' created")

} else if (expectedResult == 'modal_stays_open') {
	// Modal should still be open (validation error)
	boolean modalOpen = WebUI.verifyElementPresent(
		findTestObject('Page_Events/modal_AjaxModal'), 5, FailureHandling.OPTIONAL)
	assert modalOpen : "Expected modal to stay open but it closed"
	WebUI.comment("PASSED: ${scenario} - Modal stayed open (validation error)")
}

WebUI.closeBrowser()
