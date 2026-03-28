import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import javax.swing.JOptionPane

/**
 * TC_Login - Data-driven Login Test
 * Variables bound from CSV via Test Suite: scenario, email, password, isPositive, expectedResult
 * CAPTCHA: manual pause for all scenarios except empty fields
 */

WebUI.comment(">>> Running: ${scenario}")

// Open login page
WebUI.openBrowser('')
WebUI.navigateToUrl(GlobalVariable.baseUrl)
WebUI.waitForElementPresent(findTestObject('Page_Login/input_Email'), GlobalVariable.timeout)

// Clear auto-filled fields (page auto-fills admin credentials)
WebUI.clearText(findTestObject('Page_Login/input_Email'))
WebUI.clearText(findTestObject('Page_Login/input_Password'))

// Fill credentials (leave empty for empty-fields scenario)
if (email != '') {
	WebUI.setText(findTestObject('Page_Login/input_Email'), email)
}

if (password != '') {
	WebUI.setText(findTestObject('Page_Login/input_Password'), password)
}

// CAPTCHA pause for all scenarios except empty fields
if (expectedResult != 'stay_on_login') {
	JOptionPane.showMessageDialog(
		null,
		"Solve the reCAPTCHA in the browser, then click OK.\n\nScenario: ${scenario}",
		"CAPTCHA Required",
		JOptionPane.INFORMATION_MESSAGE
	)
}

// Click Sign In
WebUI.click(findTestObject('Page_Login/btn_SignIn'))
WebUI.delay(2)

// Verify expected result
if (expectedResult == 'dashboard') {
	// Positive: should redirect to dashboard
	WebUI.waitForElementPresent(findTestObject('Page_Login/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
	String currentUrl = WebUI.getUrl()
	assert currentUrl.contains('dashboard') : "Expected URL to contain 'dashboard' but got: ${currentUrl}"
	WebUI.comment("PASSED: ${scenario}")

} else if (expectedResult == 'error_alert') {
	// Negative: should show error alert
	WebUI.verifyElementPresent(findTestObject('Page_Login/alert_Error'), GlobalVariable.timeout)
	WebUI.comment("PASSED: ${scenario}")

} else if (expectedResult == 'stay_on_login') {
	// Negative (empty): should show required field validation
	WebUI.verifyElementPresent(findTestObject('Page_Login/alert_Required_field'), GlobalVariable.timeout)
	WebUI.comment("PASSED: ${scenario}")
}

WebUI.closeBrowser()
