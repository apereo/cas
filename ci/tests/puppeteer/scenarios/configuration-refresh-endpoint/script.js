const cas = require("../../cas.js");

(async () => {

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Attempting to login with default credentials...");
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casuser", "p@$$word");
    await cas.assertCookie(page);

    await cas.log("Updating configuration and waiting for changes to reload...");
    await cas.updateYamlConfigurationSource(__dirname, {
        cas: {
            authn: {
                accept: {
                    users: "casrefresh::p@$$word"
                }
            }
        }
    });
    await cas.sleep(5000);

    await cas.refreshContext();

    await cas.log("Attempting to login with new updated credentials...");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casrefresh", "p@$$word");
    await cas.assertCookie(page);

    await cas.closeBrowser(browser);
})();
