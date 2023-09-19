const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");
const YAML = require("yaml");
const fs = require("fs");
const path = require("path");

(async () => {
    let configFilePath = path.join(__dirname, 'config.yml');
    const file = fs.readFileSync(configFilePath, 'utf8');
    const configFile = YAML.parse(file);
    
    let browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await page.close();
    await browser.close();

    await cas.doPost("https://localhost:8443/cas/actuator/auditLog", {}, {
        'Content-Type': 'application/json'
    }, res => {
        cas.log(`Found ${res.data.length} audit records`);
        assert(res.data.length >= 4);
        assert(res.data[0].principal !== null);
        assert(res.data[0].actionPerformed !== null);
        assert(res.data[0].applicationCode !== null);
        assert(res.data[0].clientIpAddress !== null);
        assert(res.data[0].serverIpAddress !== null);
        assert(res.data[0].resourceOperatedUpon !== null)
    }, error => {
        throw(error);
    });

    await cas.doGet("https://localhost:8443/cas/actuator/auditevents",
        res => {
            cas.log(`Found ${res.data.events.length} audit records`);
            assert(res.data.events.length >= 4);
            assert(res.data.events[0].principal !== null);
            assert(res.data.events[0].timestamp !== null);
            assert(res.data.events[0].type !== null);
            assert(res.data.events[0].data.source !== null)
        }, err => {
            throw(err);
        });

    await cas.log("Updating configuration...");
    let number = await cas.randomNumber();
    await updateConfig(configFile, configFilePath, number);
    await cas.sleep(6000);
    await cas.refreshContext();
    await cas.sleep(3000);

    await cas.log("Testing authentication after refresh...");
    browser = await puppeteer.launch(cas.browserOptions());
    page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await page.close();
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
    await cas.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    await cas.log(`Wrote changes to ${configFilePath}`);
}
