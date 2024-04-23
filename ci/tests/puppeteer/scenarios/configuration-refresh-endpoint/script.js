
const cas = require("../../cas.js");
const YAML = require("yaml");
const fs = require("fs");
const path = require("path");

(async () => {

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Attempting to login with default credentials...");
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casuser", "p@$$word");
    await cas.assertCookie(page);

    const configFilePath = path.join(__dirname, "config.yml");
    const file = fs.readFileSync(configFilePath, "utf8");
    const configFile = YAML.parse(file);
    const users = configFile.cas.authn.accept.users;
    await cas.log(`Current users: ${users}`);

    await cas.log("Updating configuration and waiting for changes to reload...");
    await updateConfig(configFile, configFilePath, "casrefresh::p@$$word");
    await cas.sleep(5000);

    await cas.refreshContext();

    await cas.log("Attempting to login with new updated credentials...");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casrefresh", "p@$$word");
    await cas.assertCookie(page);

    await updateConfig(configFile, configFilePath, users);
    await browser.close();
})();

async function updateConfig(configFile, configFilePath, data) {
    configFile.cas.authn.accept.users = data;

    const newConfig = YAML.stringify(configFile);
    await cas.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    await cas.log(`Wrote changes to ${configFilePath}`);
}
