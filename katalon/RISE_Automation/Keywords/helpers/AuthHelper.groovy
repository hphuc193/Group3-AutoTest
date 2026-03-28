package helpers

import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.webui.driver.DriverFactory
import org.openqa.selenium.Cookie
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import internal.GlobalVariable
import javax.swing.JOptionPane

/**
 * AuthHelper - Handles cookie-based authentication for RISE demo site.
 * Saves/loads browser cookies to skip CAPTCHA for non-auth test cases.
 */
class AuthHelper {

	/**
	 * Login with CAPTCHA (manual), then save cookies to JSON file.
	 * Call this once to generate auth files.
	 * @param role "admin" or "client"
	 */
	static void loginAndSaveCookies(String role) {
		String email = (role == 'admin') ? 'admin@demo.com' : 'client@demo.com'
		String password = 'riseDemo'

		WebUI.openBrowser('')
		WebUI.navigateToUrl(GlobalVariable.baseUrl)
		WebUI.delay(2)

		// Clear and fill credentials
		WebUI.comment("Logging in as ${role}...")

		// Wait for page to load, then use Selenium directly for reliability
		def driver = DriverFactory.getWebDriver()
		def emailField = driver.findElement(org.openqa.selenium.By.id('email'))
		def passwordField = driver.findElement(org.openqa.selenium.By.id('password'))
		emailField.clear()
		emailField.sendKeys(email)
		passwordField.clear()
		passwordField.sendKeys(password)

		// Pause for CAPTCHA
		JOptionPane.showMessageDialog(
			null,
			"Solve the reCAPTCHA for ${role}, then click OK.",
			"CAPTCHA - ${role}",
			JOptionPane.INFORMATION_MESSAGE
		)

		// Click Sign In
		driver.findElement(org.openqa.selenium.By.cssSelector('button[type="submit"]')).click()
		WebUI.delay(3)

		// Save cookies
		def cookies = driver.manage().getCookies()
		def cookieList = cookies.collect { cookie ->
			[
				name: cookie.getName(),
				value: cookie.getValue(),
				domain: cookie.getDomain(),
				path: cookie.getPath(),
				expiry: cookie.getExpiry()?.time,
				secure: cookie.isSecure(),
				httpOnly: cookie.isHttpOnly()
			]
		}

		String filePath = RunConfiguration.getProjectDir() + "/Data Files/auth_${role}.json"
		new File(filePath).text = JsonOutput.prettyPrint(JsonOutput.toJson(cookieList))
		WebUI.comment("Cookies saved to ${filePath}")
		WebUI.closeBrowser()
	}

	/**
	 * Load saved cookies and navigate to a page as authenticated user.
	 * @param role "admin" or "client"
	 */
	static void loadAuth(String role) {
		String filePath = RunConfiguration.getProjectDir() + "/Data Files/auth_${role}.json"
		File authFile = new File(filePath)

		if (!authFile.exists()) {
			throw new RuntimeException("Auth file not found: ${filePath}. Run Setup_Auth first.")
		}

		def cookieList = new JsonSlurper().parse(authFile)

		// Open browser and navigate to site first (needed to set cookies on the domain)
		WebUI.openBrowser('')
		WebUI.navigateToUrl(GlobalVariable.baseUrl)
		WebUI.delay(1)

		// Set cookies via Selenium
		def driver = DriverFactory.getWebDriver()
		driver.manage().deleteAllCookies()

		for (cookie in cookieList) {
			def builder = new Cookie.Builder(cookie.name, cookie.value)
				.domain(cookie.domain)
				.path(cookie.path)
				.isSecure(cookie.secure)
				.isHttpOnly(cookie.httpOnly)

			if (cookie.expiry) {
				builder.expiresOn(new Date(cookie.expiry))
			}

			driver.manage().addCookie(builder.build())
		}

		// Refresh to apply cookies
		WebUI.navigateToUrl(GlobalVariable.baseUrl)
		WebUI.delay(2)
		WebUI.comment("Loaded auth for ${role}")
	}
}
