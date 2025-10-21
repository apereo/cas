const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
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
        await cas.refreshBusContext();
        await cas.log("Attempting to login with new updated credentials...");
        await cas.goto(page, "https://localhost:8444/cas/logout");
        await cas.goto(page, "https://localhost:8444/cas/login");
        await cas.loginWith(page, "casrefresh", "p@$$word");
        await cas.assertCookie(page);
    } finally {
        await cas.closeBrowser(browser);
    }
})();

