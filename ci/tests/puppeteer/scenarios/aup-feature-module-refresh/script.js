
const cas = require("../../cas.js");

const fs = require("fs");
const path = require("path");

(async () => {
    const service = "https://localhost:9859/anything/cas";

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Starting out with acceptable usage policy feature disabled...");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(3000);
    await cas.logPage(page);
    await cas.assertTicketParameter(page);

    await cas.log("Updating configuration and waiting for changes to reload...");
    const configFilePath = path.join(__dirname, "config.yml");
    const file = fs.readFileSync(configFilePath, "utf8");
    const configFile = await cas.parseYAML(file);
    await updateConfig(configFile, configFilePath, true);
    await cas.sleep(5000);

    await cas.refreshContext();

    await cas.log("Starting out with acceptable usage policy feature enabled...");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.assertTextContent(page, "#main-content #login #fm1 h3", "Acceptable Usage Policy");
    await cas.assertVisibility(page, "button[name=submit]");
    await cas.click(page, "#aupSubmit");
    await cas.waitForNavigation(page);
    await cas.sleep(3000);
    await cas.assertTicketParameter(page);
    await cas.log(page);
    await cas.assertPageUrlHost(page, "localhost:9859");
    await updateConfig(configFile, configFilePath, false);
    await cas.closeBrowser(browser);
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
    const newConfig = await cas.toYAML(config);
    await cas.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    await cas.log(`Wrote changes to ${configFilePath}`);
}
