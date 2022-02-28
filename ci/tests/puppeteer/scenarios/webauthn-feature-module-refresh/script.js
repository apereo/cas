const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');
const YAML = require("yaml");
const fs = require("fs");
const path = require("path");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    let url = "https://localhost:8443/cas/login?authn_method=mfa-webauthn";
    await page.goto(url);
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed")

    console.log("Updating configuration and waiting for changes to reload...")
    let configFilePath = path.join(__dirname, 'config.yml');
    const file = fs.readFileSync(configFilePath, 'utf8')
    const configFile = YAML.parse(file);
    await updateConfig(configFile, configFilePath, true);
    await page.waitForTimeout(5000)

    try {
        let response = await cas.doRequest("https://localhost:8443/cas/actuator/refresh", "POST");
        console.log(response)
        await page.waitForTimeout(2000)

        await page.goto("https://localhost:8443/cas/logout");
        await page.goto(url);
        await cas.loginWith(page, "casuser", "Mellon");
        await page.waitForTimeout(4000)
        await cas.assertTextContent(page, "#status", "Login with FIDO2-enabled Device");

        console.log("Checking for presence of errors...")
        let errorPanel = await page.$('#errorPanel');
        assert(await errorPanel == null);

        console.log("Checking page elements for visibility")
        await cas.assertVisibility(page, '#messages')
        await cas.assertInvisibility(page, '#deviceTable')
        await cas.assertVisibility(page, '#authnButton')

        const endpoints = ["health", "webAuthnDevices/casuser"];
        const baseUrl = "https://localhost:8443/cas/actuator/"
        for (let i = 0; i < endpoints.length; i++) {
            let url = baseUrl + endpoints[i];
            console.log(`Checking response status from ${url}`)
            const response = await page.goto(url);
            console.log(`${response.status()} ${response.statusText()}`)
            assert(response.ok())
        }
    } finally {
        await updateConfig(configFile, configFilePath, false);
    }
    
    await browser.close();
})();


async function updateConfig(configFile, configFilePath, data) {
    let config = {
        cas: {
            authn: {
                mfa: {
                    'web-authn': {
                        core: {
                            enabled: data
                        }
                    }
                }
            }
        }
    }

    const newConfig = YAML.stringify(config);
    console.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    console.log(`Wrote changes to ${configFilePath}`);
}
