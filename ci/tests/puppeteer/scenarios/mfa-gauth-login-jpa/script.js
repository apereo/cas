const cas = require("../../cas.js");

(async () => {
    await cas.doRequest("https://localhost:8443/cas/actuator/gauthCredentialRepository", "DELETE");

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.screenshot(page);

    const scratchCodes = await cas.innerTexts(page, "span[name='gauth-scratchcode']");
    await cas.log(`Scratch codes: ${scratchCodes}`);

    const scratchCode = scratchCodes[0];
    const confirm = await page.$("#confirm");
    await confirm.click();
    await cas.assertVisibility(page, "#confirm-reg-dialog #notif-dialog-title");
    await cas.assertVisibility(page, "#token");
    await cas.assertVisibility(page, "#accountName");

    await cas.type(page, "#token", scratchCode);
    await cas.sleep(1000);
    await cas.click(page, "#registerButton");
    /*
     * The register button posts once over AJAX to have the token checked and then submits the
     * form for real, so the page that follows arrives after two round trips. Wait for the login
     * form, which only the token prompt carries, rather than for a fixed interval.
     */
    await cas.waitForElement(page, "#fm1");

    await cas.type(page, "#token", scratchCodes[1]);
    await cas.sleep(2000);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.log(`Login panel: ${await cas.innerTexts(page, "#login")}`);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);

    await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.type(page, "#token", scratchCodes[2]);
    await cas.sleep(2000);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);

    await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
    await cas.loginWith(page);
    await cas.sleep(2000);

    for (let i = 0; i < 3; i++) {
        await cas.type(page, "#token", "657465");
        await cas.sleep(1000);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
    }
    await cas.type(page, "#token", "234231");
    await cas.sleep(1000);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#login div h2", "Blocked Multifactor Authentication Attempt");
    await cas.assertInnerTextStartsWith(page, "#login div p", "Your multifactor authentication attempt is blocked");
    await cas.closeBrowser(browser);
})();
