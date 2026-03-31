import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import javax.swing.JOptionPane

/**
 * TC_Register - Data-driven Register Test
 * Variables bound from CSV: scenario, firstName, lastName, company, email, password, retypePassword, needCaptcha, expectedResult
 * CAPTCHA: manual pause for TC_REGISTER_001 and TC_REGISTER_002
 */

WebUI.comment(">>> Running: ${scenario}")

String ts = String.valueOf(System.currentTimeMillis())

// Open login page, then click "Sign up" link
WebUI.openBrowser('')
WebUI.navigateToUrl(GlobalVariable.baseUrl)
WebUI.waitForElementPresent(findTestObject('Page_Login/input_Email'), GlobalVariable.timeout)
WebUI.click(findTestObject('Page_Login/link_SignUp'))
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Register/input_FirstName'), GlobalVariable.timeout)

// Fill form fields (skip empty ones for empty-fields test)
if (firstName != '') {
	WebUI.setText(findTestObject('Page_Register/input_FirstName'), firstName)
}
if (lastName != '') {
	WebUI.setText(findTestObject('Page_Register/input_LastName'), lastName)
}
if (company != '') {
	String actualCompany = company.replace('{timestamp}', ts)
	WebUI.setText(findTestObject('Page_Register/input_Company'), actualCompany)
}
if (email != '') {
	String actualEmail = email.replace('{timestamp}', ts)
	WebUI.setText(findTestObject('Page_Register/input_Email'), actualEmail)
}
if (password != '') {
	WebUI.setText(findTestObject('Page_Register/input_Password'), password)
}
if (retypePassword != '') {
	WebUI.setText(findTestObject('Page_Register/input_RetypePassword'), retypePassword)
}

// CAPTCHA pause (for positive and duplicate email tests)
if (needCaptcha == 'yes') {
	JOptionPane.showMessageDialog(
		null,
		"Solve the reCAPTCHA in the browser, then click OK.\n\nScenario: ${scenario}",
		"CAPTCHA Required",
		JOptionPane.INFORMATION_MESSAGE
	)
}

// Click Sign Up button
WebUI.click(findTestObject('Page_Register/btn_SignUp'))
WebUI.delay(1)

// Verify expected result
if (expectedResult == 'success') {
	// Should show success alert with "successfully" on signup page
	WebUI.verifyElementPresent(findTestObject('Page_Register/alert_Message'), GlobalVariable.timeout)
	def driver = com.kms.katalon.core.webui.driver.DriverFactory.getWebDriver()
	String alertText = driver.findElement(org.openqa.selenium.By.cssSelector('.alert')).getText()
	assert alertText.contains('successfully') : "Expected 'successfully' in alert but got: ${alertText}"
	WebUI.comment("PASSED: ${scenario} - Registration successful")

} else if (expectedResult == 'error_duplicate') {
	// Should show "Account already exists" alert
	WebUI.verifyElementPresent(findTestObject('Page_Register/alert_Message'), GlobalVariable.timeout)
	def driver2 = com.kms.katalon.core.webui.driver.DriverFactory.getWebDriver()
	String alertText2 = driver2.findElement(org.openqa.selenium.By.cssSelector('.app-alert-message')).getText()
	assert alertText2.contains('already exists') : "Expected 'already exists' but got: ${alertText2}"
	WebUI.comment("PASSED: ${scenario} - Duplicate email error shown")

} else if (expectedResult == 'error_required') {
	// Should stay on signup with "This field is required." errors
	String currentUrl = WebUI.getUrl()
	assert currentUrl.contains('signup') : "Expected to stay on signup page but got: ${currentUrl}"
	WebUI.verifyElementPresent(findTestObject('Page_Register/label_FieldError'), GlobalVariable.timeout)
	WebUI.comment("PASSED: ${scenario} - Required field errors shown")

} else if (expectedResult == 'error_mismatch') {
	// Should show "Please enter the same value again." error on retype password
	WebUI.verifyElementPresent(findTestObject('Page_Register/label_RetypePasswordError'), GlobalVariable.timeout)
	WebUI.comment("PASSED: ${scenario} - Password mismatch error shown")
}

WebUI.closeBrowser()
