
const cas = require("../../cas.js");

const fs = require("fs");
const path = require("path");

(async () => {
    const configFilePath = path.join(__dirname, "config.yml");
    const file = fs.readFileSync(configFilePath, "utf8");
    const configFile = await cas.parseYAML(file);
    const users = configFile.cas.authn.accept.users;
    await cas.log(`Current users: ${users}`);

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.log("Updating configuration and waiting for changes to reload...");
        await updateConfig(configFile, configFilePath, "casrefresh::p@$$word");
        await cas.sleep(5000);
        await cas.refreshBusContext();
        await cas.log("Attempting to login with new updated credentials...");
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

    const newConfig = await cas.toYAML(configFile);
    await cas.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    await cas.log(`Wrote changes to ${configFilePath}`);
}
