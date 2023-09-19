const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");
const path = require("path");
const fs = require("fs");
const YAML = require("yaml");

(async () => {
    let configFilePath = path.join(__dirname, 'config.yml');
    const file = fs.readFileSync(configFilePath, 'utf8');
    const configFile = YAML.parse(file);

    let leak = await cas.randomNumber() * 100;
    await cas.log("Updating configuration and waiting for changes to reload...");
    updateConfig(configFile, configFilePath, leak);
    await cas.sleep(2000);

    await cas.refreshContext();
    await cas.sleep(5000);

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    let response = await cas.goto(page, "https://localhost:8443/cas/login");
    await page.waitForTimeout(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    await cas.goto(page, "https://localhost:8443/cas/logout");

    let url = await page.url();
    await cas.log(`Page url: ${url}`);
    assert(url === "https://localhost:8443/cas/logout");

    await page.waitForTimeout(1000);
    await cas.assertCookie(page, false);

    await browser.close();
})();



function updateConfig(configFile, configFilePath, data) {
    let config = {
        cas: {
            ticket: {
                registry: {
                    jpa: {
                        "leak-threshold": data
                    }
                }
            }
        }
    };
    const newConfig = YAML.stringify(config);
    cas.log(`Updated configuration:\n${newConfig}`);
    fs.writeFileSync(configFilePath, newConfig);
    cas.log(`Wrote changes to ${configFilePath}`);
}
