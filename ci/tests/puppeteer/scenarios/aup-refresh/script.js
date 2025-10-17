const cas = require("../../cas.js");

(async () => {
    const service = "https://localhost:9859/anything/cas";

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Starting out with acceptable usage policy feature disabled...");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(3000);
    await cas.logPage(page);
    await cas.assertTicketParameter(page);

    await cas.updateYamlConfigurationSource(__dirname, {
        cas: {
            "acceptable-usage-policy": {
                core: {
                    enabled: true
                }
            }
        }
    });
    await cas.sleep(5000);

    await cas.refreshContext();

    await cas.log("Starting out with acceptable usage policy feature enabled...");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.assertTextContent(page, "#main-content #login #fm1 h3", "Acceptable Usage Policy");
    await cas.assertVisibility(page, "button[name=submit]");
    await cas.click(page, "#aupSubmit");
    await cas.waitForNavigation(page);
    await cas.sleep(3000);
    await cas.assertTicketParameter(page);
    await cas.log(page);
    await cas.assertPageUrlHost(page, "localhost:9859");
    await cas.updateYamlConfigurationSource(__dirname, {
        cas: {
            "acceptable-usage-policy": {
                core: {
                    enabled: false
                }
            }
        }
    });
    await cas.closeBrowser(browser);
})();

