const cas = require("../../cas.js");
const assert = require("assert");

function assertAccount(res, scratchCodes) {
    assert(res.data.length === 1);
    assert(res.data[0].scratchCodes.length === 3);
    assert(res.data[0].scratchCodes.some((item) => scratchCodes.includes(item)) === false);
}

(async () => {
    await cas.doRequest("https://localhost:8443/cas/actuator/gauthCredentialRepository", "DELETE");
    await cas.sleep(1000);
    
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.screenshot(page);

    const scratchCodes = await cas.innerTexts(page, "span[name='gauth-scratchcode']");
    await cas.log(`Scratch codes: ${scratchCodes}`);

    const confirm = await page.$("#confirm");
    await confirm.click();
    await cas.sleep(1000);

    await cas.log(`Attempting to register with ${scratchCodes[1]}`);
    await cas.type(page, "#token", scratchCodes[0]);
    await cas.sleep(1000);
    await cas.click(page, "#registerButton");
    await cas.sleep(3000);

    await cas.log(`Attempting to login with ${scratchCodes[1]}`);
    await cas.type(page, "#token", scratchCodes[1]);
    await cas.sleep(2000);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);

    await cas.doGet("https://localhost:8443/cas/actuator/gauthCredentialRepository",
        (res) => {
            assertAccount(res, scratchCodes);
        },
        (error) => {
            throw error;
        }, {
            "Content-Type": "application/json"
        });
    await cas.doGet("https://localhost:8443/cas/actuator/gauthCredentialRepository/casuser",
        (res) => {
            assertAccount(res, scratchCodes);
        },
        (error) => {
            throw error;
        }, {
            "Content-Type": "application/json"
        });

    for (const code of [scratchCodes[0], scratchCodes[1]]) {
        await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
        await cas.loginWith(page);
        await cas.sleep(1000);
        await cas.type(page, "#token", code);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(1000);
        await cas.assertCookie(page, false);
    }

    await cas.doRequest("https://localhost:8443/cas/actuator/gauthCredentialRepository", "DELETE");
    await cas.closeBrowser(browser);
})();
