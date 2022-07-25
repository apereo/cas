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
    console.log("Updating configuration and waiting for changes to reload...");
    updateConfig(configFile, configFilePath, leak);
    await cas.sleep(2000);

    let response = await cas.doRequest("https://localhost:8443/cas/actuator/refresh", "POST");
    console.log(response);
    await cas.sleep(5000);

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    response = await cas.goto(page, "https://localhost:8443/cas/login");
    await page.waitForTimeout(1000);
    console.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    await cas.goto(page, "https://localhost:8443/cas/logout");

    let url = await page.url();
    console.log(`Page url: ${url}`);
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
    console.log(`Updated configuration:\n${newConfig}`);
    fs.writeFileSync(configFilePath, newConfig);
    console.log(`Wrote changes to ${configFilePath}`);
}
