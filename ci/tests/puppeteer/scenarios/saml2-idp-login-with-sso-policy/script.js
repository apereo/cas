
const path = require("path");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(2000);
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");

    await cas.closeBrowser(browser);
})();

