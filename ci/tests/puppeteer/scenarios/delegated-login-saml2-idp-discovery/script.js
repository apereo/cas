const cas = require("../../cas.js");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.click(page, "#saml2IdPDiscovery");
    await cas.sleep(5000);
    await cas.type(page, "#idpSelectInput", "simplesaml");
    await cas.sleep(1000);
    await cas.pressEnter(page);
    await cas.sleep(4000);
    await cas.log(`Page url: ${ await page.url()}`);

    await cas.loginWith(page, "user1", "password");
    await cas.sleep(2000);

    await cas.log(`Page url: ${ await page.url()}`);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await browser.close();
})();
