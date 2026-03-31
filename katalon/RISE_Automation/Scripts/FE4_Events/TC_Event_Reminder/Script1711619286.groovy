import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import helpers.AuthHelper
import java.text.SimpleDateFormat

/**
 * TC_Event_Reminder - Data-driven Event Reminder Test
 * Variables bound from CSV: scenario, reminderTitle, addSecond, expectedResult
 * Flow: Click first event -> Add reminder -> Fill form -> Click Add button
 */

WebUI.comment(">>> Running: ${scenario}")

// Load admin auth cookies
AuthHelper.loadAuth('admin')

// Navigate to Events via sidebar
WebUI.waitForElementPresent(findTestObject('Page_Dashboard/verify_Dashboard'), GlobalVariable.timeout, FailureHandling.OPTIONAL)
WebUI.click(findTestObject('Page_Dashboard/link_Events'))
WebUI.delay(1)
WebUI.waitForElementPresent(findTestObject('Page_Events/calendar_Grid'), GlobalVariable.timeout)

// Click the first event on calendar
WebUI.click(findTestObject('Page_Events/link_FirstEvent'))
WebUI.waitForElementPresent(findTestObject('Page_Events/modal_AjaxModal'), GlobalVariable.timeout)

// Click "Add reminder" link
WebUI.click(findTestObject('Page_Events/link_AddReminder'))
WebUI.delay(1)

// Calculate tomorrow's date for reminder
def cal = Calendar.getInstance()
cal.add(Calendar.DAY_OF_MONTH, 1)
String tomorrowDate = new SimpleDateFormat('MM-dd-yyyy').format(cal.getTime())

// Generate unique title
String actualTitle = reminderTitle.replace('{timestamp}', String.valueOf(System.currentTimeMillis()))

// Fill reminder form
def driver = DriverFactory.getWebDriver()

if (reminderTitle != '') {
	WebUI.clearText(findTestObject('Page_Events/input_ReminderTitle'))
	WebUI.setText(findTestObject('Page_Events/input_ReminderTitle'), actualTitle)
}

// Click date input to open datepicker, then select today
WebUI.click(findTestObject('Page_Events/input_ReminderDate'))
WebUI.delay(1)
WebUI.click(findTestObject('Page_Events/datepicker_Today'))
WebUI.delay(1)

// Type time directly into the time input
WebUI.click(findTestObject('Page_Events/input_ReminderTime'))
WebUI.clearText(findTestObject('Page_Events/input_ReminderTime'))
WebUI.setText(findTestObject('Page_Events/input_ReminderTime'), '09:00 AM')

// Click "Add" button in reminder section
WebUI.click(findTestObject('Page_Events/btn_AddReminder'))
WebUI.delay(1)

// Verify result
if (expectedResult == 'success') {
	// Close the modal
	WebUI.click(findTestObject('Page_Events/btn_CloseModal'))
	WebUI.delay(1)

	// Click the same event again to reopen
	def eventLink2 = driver.findElement(org.openqa.selenium.By.xpath("//a[contains(@class,'fc-event')]"))
	eventLink2.click()
	WebUI.waitForElementPresent(findTestObject('Page_Events/modal_AjaxModal'), GlobalVariable.timeout)
	WebUI.delay(1)

	// Verify reminder persists in reminders table
	WebUI.verifyElementPresent(findTestObject('Page_Events/table_Reminders'), GlobalVariable.timeout)
	String tableContent = driver.findElement(org.openqa.selenium.By.id('event-reminders-table')).getText()
	assert tableContent.contains(actualTitle) : "Reminder '${actualTitle}' not found after reopening event"
	WebUI.comment("PASSED: ${scenario} - Reminder '${actualTitle}' persists after close & reopen")

} else if (expectedResult == 'form_stays_open') {
	// Verify "This field is required." error message appears
	WebUI.verifyElementPresent(findTestObject('Page_Events/label_ReminderTitleError'), GlobalVariable.timeout)
	WebUI.comment("PASSED: ${scenario} - 'This field is required.' error displayed")

} else if (expectedResult == 'success_multiple') {
	// First reminder added — modal stays open, just fill second reminder directly
	String secondTitle = "Rem2_${System.currentTimeMillis()}"
	WebUI.clearText(findTestObject('Page_Events/input_ReminderTitle'))
	WebUI.setText(findTestObject('Page_Events/input_ReminderTitle'), secondTitle)

	WebUI.click(findTestObject('Page_Events/input_ReminderDate'))
	WebUI.delay(1)
	WebUI.click(findTestObject('Page_Events/datepicker_Today'))
	WebUI.delay(1)

	WebUI.click(findTestObject('Page_Events/input_ReminderTime'))
	WebUI.clearText(findTestObject('Page_Events/input_ReminderTime'))
	WebUI.setText(findTestObject('Page_Events/input_ReminderTime'), '10:00 AM')

	WebUI.click(findTestObject('Page_Events/btn_AddReminder'))
	WebUI.delay(1)

	// Close modal, reopen event, verify both reminders persist
	WebUI.click(findTestObject('Page_Events/btn_CloseModal'))
	WebUI.delay(1)

	def eventLink3 = driver.findElement(org.openqa.selenium.By.xpath("//a[contains(@class,'fc-event')]"))
	eventLink3.click()
	WebUI.waitForElementPresent(findTestObject('Page_Events/modal_AjaxModal'), GlobalVariable.timeout)
	WebUI.delay(1)

	WebUI.verifyElementPresent(findTestObject('Page_Events/table_Reminders'), GlobalVariable.timeout)
	String tableContent = driver.findElement(org.openqa.selenium.By.id('event-reminders-table')).getText()
	assert tableContent.contains(actualTitle) : "First reminder '${actualTitle}' not found after reopen"
	assert tableContent.contains(secondTitle) : "Second reminder '${secondTitle}' not found after reopen"
	WebUI.comment("PASSED: ${scenario} - Both reminders persist after close & reopen")
}

WebUI.closeBrowser()
