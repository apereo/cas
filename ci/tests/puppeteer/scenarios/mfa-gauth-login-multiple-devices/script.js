
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
   
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
    await cas.loginWith(page);

    // await cas.sleep(5000)

    await cas.assertVisibility(page, "#form-1");
    await cas.assertVisibility(page, "#name-RecordName");
    await cas.assertVisibility(page, "#id-1");

    await cas.assertVisibility(page, "#form-2");
    await cas.assertVisibility(page, "#name-RecordName2");
    await cas.assertVisibility(page, "#id-2");
    
    await cas.assertVisibility(page, "#register");
    await cas.sleep(1000);

    await cas.log("Deleting a registered device now");
    await cas.click(page, "#delButton-1");
    await cas.sleep(1000);

    await cas.assertInvisibility(page, "#form-1");
    await cas.assertInvisibility(page, "#name-RecordName");
    await cas.assertInvisibility(page, "#id-1");

    await cas.log("Switching to login view");
    await cas.assertVisibility(page, "#login");
    await cas.assertVisibility(page, "#cancel");
    await cas.assertVisibility(page, "#token");
    
    await browser.close();
})();
