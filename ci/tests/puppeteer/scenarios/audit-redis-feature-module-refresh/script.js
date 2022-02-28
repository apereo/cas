const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");
const path = require("path");
const fs = require("fs");
const YAML = require("yaml");

(async () => {
    let configFilePath = path.join(__dirname, 'config.yml');
    const file = fs.readFileSync(configFilePath, 'utf8')
    const configFile = YAML.parse(file);
    
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "unknown", "Mellon");

    await cas.doPost("https://localhost:8443/cas/actuator/auditLog", {}, {
        'Content-Type': 'application/json'
    }, res => {
        assert(res.data.length === 0);
    }, error => {
        throw(error);
    })

    console.log("Updating configuration and waiting for changes to reload...")
    await updateConfig(configFile, configFilePath, true);
    await page.waitForTimeout(5000)

    let response = await cas.doRequest("https://localhost:8443/cas/actuator/refresh", "POST");
    console.log(response)

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "unknown", "Mellon");

    try {
        await cas.doPost("https://localhost:8443/cas/actuator/auditLog", {}, {
            'Content-Type': 'application/json'
        }, res => {
            assert(res.data.length === 2);
        }, error => {
            throw(error);
        })
    } finally {
        await updateConfig(configFile, configFilePath, false);
    }
    await browser.close();
})();


async function updateConfig(configFile, configFilePath, data) {
    let config = {
        cas: {
            audit: {
                redis: {
                    enabled: data
                }
            }
        }
    }
    const newConfig = YAML.stringify(config);
    console.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    console.log(`Wrote changes to ${configFilePath}`);
}
