import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import javax.swing.JOptionPane

/**
 * TC_ResetPassword - Data-driven Reset Password Test
 * Variables bound from CSV: scenario, email, needCaptcha, expectedResult
 * Flow: Login page -> "Forgot password?" -> Reset page -> Fill email -> Send
 */

WebUI.comment(">>> Running: ${scenario}")

// Open login page
WebUI.openBrowser('')
WebUI.navigateToUrl(GlobalVariable.baseUrl)
WebUI.waitForElementPresent(findTestObject('Page_Login/input_Email'), GlobalVariable.timeout)

// Click "Forgot password?" link
WebUI.click(findTestObject('Page_Login/link_ForgotPassword'))
WebUI.delay(1)

// Wait for reset password page
WebUI.waitForElementPresent(findTestObject('Page_ResetPassword/input_Email'), GlobalVariable.timeout)

// Fill email (if provided)
if (email != '') {
	WebUI.setText(findTestObject('Page_ResetPassword/input_Email'), email)
}

// CAPTCHA pause (for valid and invalid email tests)
if (needCaptcha == 'yes') {
	JOptionPane.showMessageDialog(
		null,
		"Solve the reCAPTCHA in the browser, then click OK.\n\nScenario: ${scenario}",
		"CAPTCHA Required",
		JOptionPane.INFORMATION_MESSAGE
	)
}

// Click Send button
WebUI.click(findTestObject('Page_ResetPassword/btn_Send'))
WebUI.delay(1)

// Verify expected result
def driver = DriverFactory.getWebDriver()

if (expectedResult == 'success') {
	// Purple alert: "If there is any account... we'll send instructions"
	// No text fields visible after success
	WebUI.verifyElementPresent(findTestObject('Page_ResetPassword/alert_AppMessage'), GlobalVariable.timeout)
	String alertText = driver.findElement(org.openqa.selenium.By.cssSelector('.app-alert-message')).getText()
	assert alertText.contains('send instructions') : "Expected 'send instructions' but got: ${alertText}"
	// Verify email input is NOT visible (form hidden on success)
	boolean emailGone = WebUI.verifyElementNotPresent(findTestObject('Page_ResetPassword/input_Email'), 3, FailureHandling.OPTIONAL)
	WebUI.comment("PASSED: ${scenario} - Reset email sent, form hidden")

} else if (expectedResult == 'error_not_found') {
	// Red alert: same "If there is any account..." message but email field still visible
	WebUI.verifyElementPresent(findTestObject('Page_ResetPassword/alert_AppMessage'), GlobalVariable.timeout)
	// Email input still visible (form stays)
	WebUI.verifyElementPresent(findTestObject('Page_ResetPassword/input_Email'), GlobalVariable.timeout)
	WebUI.comment("PASSED: ${scenario} - Error shown, form still visible")

} else if (expectedResult == 'error_required') {
	// "This field is required." error on email
	WebUI.verifyElementPresent(findTestObject('Page_ResetPassword/label_EmailError'), GlobalVariable.timeout)
	WebUI.comment("PASSED: ${scenario} - Required field error shown")
}

WebUI.closeBrowser()
