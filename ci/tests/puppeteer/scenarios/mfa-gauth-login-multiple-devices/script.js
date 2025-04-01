
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
   
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.assertVisibility(page, "#form-1");
    await cas.assertVisibility(page, "#name-RecordName");
    await cas.assertVisibility(page, "#id-1");

    await cas.assertVisibility(page, "#form-2");
    await cas.assertVisibility(page, "#name-RecordName2");
    await cas.assertVisibility(page, "#id-2");
    
    await cas.assertVisibility(page, "#register");
    await cas.sleep(1000);

    await cas.log("Deleting registered device 1");
    await cas.click(page, "#delButton-1");
    await cas.sleep(2000);
    let scratchCode = await cas.fetchGoogleAuthenticatorScratchCode("casuser", 1);
    await cas.logg(`Retrieved scratch code ${scratchCode}`);
    await cas.type(page, "#token", scratchCode);
    await cas.sleep(2000);
    await cas.click(page, "#confirmAcctDeleteBtn");
    await cas.sleep(2000);

    await cas.click(page, "#selectDeviceButton");
    await cas.sleep(2000);
    await cas.log("Deleting registered device 2");
    await cas.click(page, "#delButton-2");
    await cas.sleep(2000);
    scratchCode = await cas.fetchGoogleAuthenticatorScratchCode("casuser", 2);
    await cas.logg(`Retrieved scratch code ${scratchCode}`);
    await cas.type(page, "#token", scratchCode);
    await cas.sleep(2000);
    await cas.click(page, "#confirmAcctDeleteBtn");
    await cas.sleep(2000);

    await cas.assertInnerTextStartsWith(page, "#login h2", "Your account is not registered");
    await cas.assertVisibility(page, "img#imageQRCode");
    await cas.assertVisibility(page, "#seckeypanel pre");
    await cas.assertVisibility(page, "#scratchcodes");
    assert((await page.$$("#scratchcodes div.mdc-chip")).length === 5);
    
    await browser.close();
})();
