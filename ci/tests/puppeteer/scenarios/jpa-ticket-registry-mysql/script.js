
const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");

(async () => {
    const configFilePath = path.join(__dirname, "config.yml");
    const file = fs.readFileSync(configFilePath, "utf8");
    const configFile = await cas.parseYAML(file);

    const leak = await cas.randomNumber() * 100;
    await cas.log("Updating configuration and waiting for changes to reload...");
    await updateConfig(configFile, configFilePath, leak);
    await cas.sleep(2000);

    await cas.refreshContext();
    await cas.sleep(5000);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    await cas.gotoLogout(page);

    await cas.logPage(page);
    const url = await page.url();
    assert(url === "https://localhost:8443/cas/logout");

    await cas.sleep(1000);
    await cas.assertCookie(page, false);

    await browser.close();
})();

async function updateConfig(configFile, configFilePath, data) {
    const config = {
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
    const newConfig = await cas.toYAML(config);
    await cas.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    await cas.log(`Wrote changes to ${configFilePath}`);
}
