const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const YAML = require('yaml');
const fs = require('fs');
const path = require('path');

(async () => {
    let configFilePath = path.join(__dirname, 'config.yml');
    const file = fs.readFileSync(configFilePath, 'utf8');
    const configFile = YAML.parse(file);
    const users = configFile.cas.authn.accept.users;
    console.log(`Current users: ${users}`);

    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        console.log("Updating configuration and waiting for changes to reload...");
        await updateConfig(configFile, configFilePath, "casrefresh::p@$$word");
        await page.waitForTimeout(5000);
        await cas.refreshBusContext();
        console.log("Attempting to login with new updated credentials...");
        await cas.goto(page, "https://localhost:8444/cas/logout");
        await cas.goto(page, "https://localhost:8444/cas/login");
        await cas.loginWith(page, "casrefresh", "p@$$word");
        await cas.assertCookie(page);
    } finally {
        await updateConfig(configFile, configFilePath, users);
        await browser.close();
    }
})();

async function updateConfig(configFile, configFilePath, data) {
    configFile.cas.authn.accept.users = data;

    const newConfig = YAML.stringify(configFile);
    console.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    console.log(`Wrote changes to ${configFilePath}`);
}
