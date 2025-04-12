const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");

(async () => {
    const configFilePath = path.join(__dirname, "config.yml");
    const file = fs.readFileSync(configFilePath, "utf8");
    const configFile = await cas.parseYAML(file);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "unknown", "Mellon");

    await cas.doGet("https://localhost:8443/cas/actuator/auditLog",
        (res) => assert(res.data.length === 0),
        (error) => {
            throw (error);
        }, {
            "Content-Type": "application/json"
        });

    await cas.log(`Updating configuration ${configFilePath} and waiting for changes to reload...`);
    await updateConfig(configFile, configFilePath, true);
    await cas.sleep(5000);

    await cas.refreshContext();

    await cas.gotoLogin(page);
    await cas.loginWith(page, "unknown", "Mellon");

    try {
        await cas.doGet("https://localhost:8443/cas/actuator/auditLog",
            (res) => assert(res.data.length >= 1),
            (error) => {
                throw (error);
            }, {
                "Content-Type": "application/json"
            });
    } finally {
        await updateConfig(configFile, configFilePath, false);
    }
    await browser.close();
})();

async function updateConfig(configFile, configFilePath, data) {
    const config = {
        cas: {
            audit: {
                redis: {
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
