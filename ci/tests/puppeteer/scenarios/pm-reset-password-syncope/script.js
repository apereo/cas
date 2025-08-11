const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const username = "syncopepasschange";
    const currentPassword = "Sync0pe";
    let newPassword = "Jv!e0mKD&dCNl^Q";

    await cas.gotoLogout(page);
    await cas.sleep(1000);
    await cas.gotoLogin(page);
    await cas.loginWith(page, username, currentPassword);
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#pwdmain h3", `Hello, ${username}. You must change your password.`);
    await cas.type(page, "#currentPassword", "wrongCurrentPassword");
    await cas.type(page, "#password", newPassword);
    await cas.type(page, "#confirmedPassword", newPassword);
    await cas.pressEnter(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#pwdmain h3", `Hello, ${username}. You must change your password.`);
    await cas.type(page, "#currentPassword", currentPassword);
    await cas.type(page, "#password", newPassword);
    await cas.type(page, "#confirmedPassword", newPassword);
    await cas.pressEnter(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, username, newPassword);
    await cas.sleep(2000);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);

    await cas.gotoLogin(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
    await cas.click(page, "#forgotPasswordLink");
    await cas.sleep(2000);

    await cas.type(page, "#username", username);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");

    const link = await cas.extractFromEmail(browser);
    await cas.goto(page, link);
    await cas.sleep(2000);

    await cas.assertInnerText(page, "#content h2", "Answer Security Questions");

    await cas.type(page, "#q0", "Rome", true);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#pwdmain h3", `Hello, ${username}. You must change your password.`);

    newPassword = await cas.randomWord();
    await cas.type(page, "#password", newPassword, true);
    await cas.type(page, "#confirmedPassword", newPassword, true);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    await cas.gotoLogout(page);

    await cas.closeBrowser(browser);
})();

