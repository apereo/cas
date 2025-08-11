
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.sleep(2000);
    await cas.assertTextContent(page, "#accountSignUpLink", "Sign Up");
    await cas.submitForm(page, "#accountMgmtSignupForm");
    await cas.sleep(1000);

    await cas.assertInnerText(page, "#content h2", "Account Registration");
    await cas.type(page,"#username", "casuser");
    await cas.type(page,"#firstName", "CAS");
    await cas.type(page,"#lastName", "Person");
    await cas.type(page,"#email", "cas@example.org");
    await cas.type(page,"#phone", "+1 347 745 1234");
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
    await cas.sleep(5000);
    await cas.assertInnerText(page, "#content h2", "Account Registration");
    await cas.assertInnerTextStartsWith(page, "#content p", "Thank you! Your account is now activated");
    await cas.closeBrowser(browser);
})();

async function typePassword(page, pswd, confirm) {
    await page.focus("#password");
    await cas.type(page,"#password", pswd);
    await page.$eval("#password", (e) => e.blur());
    await page.focus("#confirmedPassword");
    await cas.type(page,"#confirmedPassword", confirm);
    await page.$eval("#confirmedPassword", (e) => e.blur());
}
