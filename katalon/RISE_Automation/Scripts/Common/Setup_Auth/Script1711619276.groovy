import helpers.AuthHelper

/**
 * Setup_Auth - Save browser cookies for admin and client roles.
 * Run this TC manually before running FE2-FE5 Test Suites.
 * Two browser windows will open sequentially - solve CAPTCHA in each.
 * Cookies saved to Data Files/auth_admin.json and Data Files/auth_client.json.
 * Re-run when cookies expire (~6 hours, demo site resets).
 */

// Save admin cookies
AuthHelper.loginAndSaveCookies('admin')

// Save client cookies
//AuthHelper.loginAndSaveCookies('client')
