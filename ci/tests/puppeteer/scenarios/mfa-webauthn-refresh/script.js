
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/login?authn_method=mfa-webauthn";
    await cas.goto(page, url);
    await cas.loginWith(page);
    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");

    await cas.log("Updating configuration and waiting for changes to reload...");
    
    await cas.updateYamlConfigurationSource(__dirname, {
        cas: {
            authn: {
                mfa: {
                    "web-authn": {
                        core: {
                            enabled: true
                        }
                    }
                }
            }
        }
    });
    await cas.sleep(5000);

    try {
        await cas.refreshContext();
        await cas.sleep(2000);

        await cas.gotoLogout(page);
        await cas.goto(page, url);
        await cas.loginWith(page);
        await cas.sleep(4000);
        await cas.assertTextContent(page, "#status", "Login with FIDO2-enabled Device");

        await cas.log("Checking for presence of errors...");
        const errorPanel = await page.$("#errorPanel");
        assert(await errorPanel === null);

        await cas.log("Checking page elements for visibility");
        await cas.assertVisibility(page, "#messages");
        await cas.assertInvisibility(page, "#deviceTable");
        await cas.assertVisibility(page, "#authnButton");

        const endpoints = ["health", "webAuthnDevices/casuser"];
        const baseUrl = "https://localhost:8443/cas/actuator/";
        for (let i = 0; i < endpoints.length; i++) {
            const url = baseUrl + endpoints[i];
            await cas.log(`Checking response status from ${url}`);
            const response = await cas.goto(page, url);
            await cas.log(`${response.status()} ${response.statusText()}`);
            assert(response.ok());
        }
    } finally {
        await cas.updateYamlConfigurationSource(__dirname, {
            cas: {
                authn: {
                    mfa: {
                        "web-authn": {
                            core: {
                                enabled: false
                            }
                        }
                    }
                }
            }
        });
    }
    
    await cas.closeBrowser(browser);
})();
