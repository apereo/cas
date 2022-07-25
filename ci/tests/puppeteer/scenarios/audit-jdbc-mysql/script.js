const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const YAML = require("yaml");
const fs = require("fs");
const path = require("path");
const assert = require("assert");

async function callRegisteredServices() {
    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, res => {
        assert(res.status === 200);
        console.log(`Services found: ${res.data[1].length}`);
    }, err => {
        throw err;
    }, {
        'Content-Type': 'application/json'
    })
}

async function callAuditLog() {
    await cas.doPost("https://localhost:8443/cas/actuator/auditLog", {}, {
        'Content-Type': 'application/json'
    }, res => {
        console.log(`Found ${res.data.length} audit records`);
    }, error => {
        throw(error);
    })
}

(async () => {
    let configFilePath = path.join(__dirname, 'config.yml');
    const file = fs.readFileSync(configFilePath, 'utf8');
    const configFile = YAML.parse(file);

    const browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await page.close();

    await callAuditLog();

    console.log("Updating configuration...");
    let number = await cas.randomNumber();
    await updateConfig(configFile, configFilePath, number);
    await page.waitForTimeout(3000);

    console.log("Refreshing CAS...");
    let response = await cas.doRequest("https://localhost:8443/cas/actuator/refresh", "POST");
    console.log(response);

    console.log("Testing authentication after refresh...");
    page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertTicketParameter(page);

    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.assertCookie(page);

    await callAuditLog();
    await callRegisteredServices();

    console.log("Waiting for audit log cleaner to resume...");
    await cas.sleep(2000);
    
    await browser.close();

})();

async function updateConfig(configFile, configFilePath, data) {
    let config = {
        cas: {
            audit: {
                jdbc: {
                    "max-age-days": data
                }
            }
        }
    };
    const newConfig = YAML.stringify(config);
    console.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    console.log(`Wrote changes to ${configFilePath}`);
}
