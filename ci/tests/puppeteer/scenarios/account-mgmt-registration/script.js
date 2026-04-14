const cas = require("../../cas.js");
const assert = require("assert");

async function verifyAccountRegistrationWithOidcService() {
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);

    const encodedScopes = encodeURIComponent("openid profile");
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=${encodedScopes}&redirect_uri=${service}`;
    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);
    await submitAccountRegistrationRequest(page, browser);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.sleep(2000);
    await cas.assertPageUrlStartsWith(page, service);
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await cas.closeBrowser(browser);
}

async function submitAccountRegistrationRequest(page, browser) {
    await cas.sleep(2000);
    await cas.assertTextContent(page, "#accountSignUpLink", "Sign Up");
    await cas.submitForm(page, "#accountMgmtSignupForm");
    await cas.sleep(1000);

    await cas.assertInnerText(page, "#content h2", "Account Registration");
    await cas.type(page, "#username", "casuser");
    await cas.type(page, "#firstName", "CAS");
    await cas.type(page, "#lastName", "Person");
    await cas.type(page, "#email", "cas@example.org");
    await cas.type(page, "#phone", "+1 347 745 1234");
    await cas.screenshot(page);
    await cas.click(page, "#submit");
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#content h2", "Account Registration");
    await cas.assertInnerTextStartsWith(page, "#content p", "Account activation instructions are successfully sent");

    const link = await cas.extractFromEmail(browser);
    assert(link !== undefined);
    await cas.goto(page, link);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content h2", "Account Registration");
    await cas.assertInnerTextStartsWith(page, "#content p", "Welcome back!");

    await typePassword(page, "EaP8R&iX$eK4nb8eAI", "EaP8R&iX$eK4nb8eAI");
    await cas.sleep(1000);

    for (let i = 1; i <= 2; i++) {
        await cas.type(page, `#securityquestion${i}`, `Security question ${i}`);
        await cas.type(page, `#securityanswer${i}`, `Security answer ${i}`);
    }
    await cas.click(page, "#submit");
    await cas.sleep(500);
    await cas.assertInnerText(page, "#content h2", "Account Registration");
    await cas.assertInnerTextStartsWith(page, "#content p", "Thank you! Your account is now activated");
    await cas.sleep(5000);
    await cas.logPage(page);
}

async function verifyAccountRegistrationWithCasService() {
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await submitAccountRegistrationRequest(page, browser);
    await cas.assertPageUrlStartsWith(page, service);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await cas.closeBrowser(browser);
}

(async () => {
    await verifyAccountRegistrationWithCasService();
    await verifyAccountRegistrationWithOidcService();
})();

async function typePassword(page, pswd, confirm) {
    await page.focus("#password");
    await cas.type(page, "#password", pswd);
    await page.$eval("#password", (e) => e.blur());
    await page.focus("#confirmedPassword");
    await cas.type(page, "#confirmedPassword", confirm);
    await page.$eval("#confirmedPassword", (e) => e.blur());
}
