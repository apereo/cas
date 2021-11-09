const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const YAML = require('yaml');
const fs = require('fs');
const path = require('path');

(async () => {

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    console.log("Attempting to login with default credentials...")
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "p@$$word");
    await cas.assertTicketGrantingCookie(page);

    let configFilePath = path.join(__dirname, 'config.yml');
    const file = fs.readFileSync(configFilePath, 'utf8')
    const configFile = YAML.parse(file);
    const users = configFile.cas.authn.accept.users;
    console.log(`Current users: ${users}`);

    console.log("Updating configuration and waiting for changes to reload...")
    await updateConfig(configFile, configFilePath, "casrefresh::p@$$word");
    await page.waitForTimeout(5000)

    let response = await cas.doRequest("https://localhost:8443/cas/actuator/refresh", "POST");
    console.log(response)

    console.log("Attempting to login with new updated credentials...")
    await page.goto("https://localhost:8443/cas/logout");
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casrefresh", "p@$$word");
    await cas.assertTicketGrantingCookie(page);

    await updateConfig(configFile, configFilePath, users);
    await browser.close();
})();

async function updateConfig(configFile, configFilePath, data) {
    configFile.cas.authn.accept.users = data;

    const newConfig = YAML.stringify(configFile);
    console.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    console.log(`Wrote changes to ${configFilePath}`);
}
