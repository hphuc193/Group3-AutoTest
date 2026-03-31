import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import javax.swing.JOptionPane

/**
 * TC_Logout - Admin Logout Test
 * Covers: TC_LOGOUT_001
 * Steps: Login (manual CAPTCHA) -> Logout -> Verify redirect -> Verify can't access dashboard
 */

// Step 1: Open login page and login
WebUI.openBrowser('')
WebUI.navigateToUrl(GlobalVariable.baseUrl)
WebUI.waitForElementPresent(findTestObject('Page_Login/input_Email'), GlobalVariable.timeout)

// Clear auto-filled fields
WebUI.clearText(findTestObject('Page_Login/input_Email'))
WebUI.clearText(findTestObject('Page_Login/input_Password'))

// Fill admin credentials
WebUI.setText(findTestObject('Page_Login/input_Email'), 'admin@demo.com')
WebUI.setText(findTestObject('Page_Login/input_Password'), 'riseDemo')

// Pause for manual CAPTCHA solving
JOptionPane.showMessageDialog(
	null,
	"Solve the reCAPTCHA in the browser, then click OK.\n\nScenario: TC_LOGOUT_001 - Admin Logout",
	"CAPTCHA Required",
	JOptionPane.INFORMATION_MESSAGE
)

// Click Sign In
WebUI.click(findTestObject('Page_Login/btn_SignIn'))
WebUI.delay(1)

// Verify login succeeded
String currentUrl = WebUI.getUrl()
assert currentUrl.contains('dashboard') : "Login failed - expected dashboard but got: ${currentUrl}"
WebUI.comment("Login successful, proceeding to logout")

// Step 2: Click user profile dropdown (top-right)
WebUI.click(findTestObject('Page_Dashboard/btn_UserDropdown'))
WebUI.delay(1)

// Step 3: Click Sign Out
WebUI.click(findTestObject('Page_Dashboard/link_SignOut'))
WebUI.delay(1)

// Step 4: Verify redirect to login page
currentUrl = WebUI.getUrl()
assert currentUrl.contains('signin') : "Expected redirect to signin but got: ${currentUrl}"
WebUI.comment("PASSED: Redirected to login page after logout")

// Step 5: Verify can't access dashboard without re-authenticating
WebUI.navigateToUrl(GlobalVariable.baseUrl + '/index.php/dashboard')
WebUI.delay(1)
currentUrl = WebUI.getUrl()
assert currentUrl.contains('signin') : "Expected redirect to signin when accessing dashboard, but got: ${currentUrl}"
WebUI.comment("PASSED: Cannot access dashboard after logout - TC_LOGOUT_001 complete")

WebUI.closeBrowser()
