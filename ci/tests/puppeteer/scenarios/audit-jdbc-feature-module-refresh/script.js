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
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "unknown", "Mellon");

    await cas.doPost("https://localhost:8443/cas/actuator/auditLog", {}, {
        'Content-Type': 'application/json'
    }, res => {
        assert(res.data.length === 0);
    }, error => {
        throw(error);
    })

    let name = (Math.random() + 1).toString(36).substring(4);
    console.log("Updating configuration and waiting for changes to reload...")
    await updateConfig(configFile, configFilePath, `jdbc:hsqldb:file:/tmp/db/${name}`);
    await page.waitForTimeout(5000)

    let response = await cas.doRequest("https://localhost:8443/cas/actuator/refresh", "POST");
    console.log(response)

    await cas.goto(page, "https://localhost:8443/cas/login");
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
        await updateConfig(configFile, configFilePath, "");
    }
    await browser.close();
})();


async function updateConfig(configFile, configFilePath, data) {
    let config = {
        cas: {
            audit: {
                jdbc: {
                    url: data
                }
            }
        }
    }
    const newConfig = YAML.stringify(config);
    console.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    console.log(`Wrote changes to ${configFilePath}`);
}
