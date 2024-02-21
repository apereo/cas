const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const YAML = require("yaml");
const fs = require("fs");
const path = require("path");
const assert = require("assert");

(async () => {
    const service = "https://apereo.github.io";

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Starting out with acceptable usage policy feature disabled...");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.waitForTimeout(page, 2000);
    await cas.assertTicketParameter(page);

    await cas.log("Updating configuration and waiting for changes to reload...");
    const configFilePath = path.join(__dirname, "config.yml");
    const file = fs.readFileSync(configFilePath, "utf8");
    const configFile = YAML.parse(file);
    await updateConfig(configFile, configFilePath, true);
    await cas.waitForTimeout(page, 5000);

    await cas.refreshContext();
    await cas.log("Starting out with acceptable usage policy feature enabled...");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.assertTextContent(page, "#main-content #login #fm1 h3", "Acceptable Usage Policy");
    await cas.assertVisibility(page, "button[name=submit]");
    await cas.click(page, "#aupSubmit");
    await page.waitForNavigation();
    await cas.waitForTimeout(page, 2000);

    await cas.assertTicketParameter(page);
    const result = new URL(page.url());
    assert(result.host === "apereo.github.io");
    await updateConfig(configFile, configFilePath, false);
    await browser.close();
})();

async function updateConfig(configFile, configFilePath, data) {
    const config = {
        cas: {
            "acceptable-usage-policy": {
                core: {
                    enabled: data
                }
            }
        }
    };
    const newConfig = YAML.stringify(config);
    await cas.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    await cas.log(`Wrote changes to ${configFilePath}`);
}
