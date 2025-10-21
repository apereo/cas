const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const leak = await cas.randomNumber() * 100;
    await cas.log("Updating configuration and waiting for changes to reload...");
    await cas.updateYamlConfigurationSource(__dirname, {
        cas: {
            ticket: {
                registry: {
                    jpa: {
                        "leak-threshold": leak
                    }
                }
            }
        }
    });
    await cas.sleep(2000);
    await cas.refreshContext();
    await cas.sleep(5000);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    await cas.gotoLogout(page);

    await cas.logPage(page);
    await cas.assertPageUrl(page, "https://localhost:8443/cas/logout");

    await cas.sleep(1000);
    await cas.assertCookie(page, false);

    await cas.closeBrowser(browser);
})();

