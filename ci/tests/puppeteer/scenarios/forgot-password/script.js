
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.sleep(2000);
    await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
    
    await cas.click(page, "#forgotPasswordLink");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#reset #fm1 h3", "Reset your password");
    await cas.assertVisibility(page, "#username");
    await cas.attributeValue(page, "#username", "autocapitalize", "none");
    await cas.attributeValue(page, "#username", "spellcheck", "false");
    await cas.attributeValue(page, "#username", "autocomplete", "username");

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);

    await cas.sleep(1000);

    await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");
    await cas.assertInnerTextStartsWith(page, "#content p", "You should shortly receive a message");

    const link = await cas.extractFromEmail(browser);
    await cas.goto(page, link);
    await cas.sleep(1000);

    await cas.assertInnerText(page, "#content h2", "Answer Security Questions");

    await cas.type(page,"#q0", "answer1");
    await cas.type(page,"#q1", "answer2");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);

    await typePassword(page, "EaP8R&iX$eK4nb8eAI", "EaP8R&iX$eK4nb8eAI");
    await cas.sleep(1000);
    await cas.assertInvisibility(page, "#password-confirm-mismatch-msg");
    await cas.assertInvisibility(page, "#password-policy-violation-msg");

    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    await cas.assertInnerText(page, "#content p", "Your account password is successfully updated.");
    await cas.closeBrowser(browser);
})();

async function typePassword(page, pswd, confirm) {
    await cas.type(page,"#password", pswd);
    await cas.type(page,"#confirmedPassword", confirm);
}
