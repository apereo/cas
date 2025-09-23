const cas = require("../../cas.js");
const assert = require("assert");

const fs = require("fs");
const path = require("path");

(async () => {
    const configFilePath = path.join(__dirname, "config.yml");
    const file = fs.readFileSync(configFilePath, "utf8");
    const configFile = await cas.parseYAML(file);

    let browser = await cas.newBrowser(cas.browserOptions());
    let page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);
    await cas.closeBrowser(browser);

    await cas.doGet("https://localhost:8443/cas/actuator/auditLog",
        (res) => {
            cas.log(`Found ${res.data.length} audit records`);
            assert(res.data.length >= 4);
            assert(res.data[0].principal !== undefined);
            assert(res.data[0].actionPerformed !== undefined);
            assert(res.data[0].applicationCode !== undefined);
            assert(res.data[0].auditableResource !== undefined);
            assert(res.data[0].whenActionWasPerformed !== undefined);
            assert(res.data[0].clientInfo.clientIpAddress !== undefined);
            assert(res.data[0].clientInfo.serverIpAddress !== undefined);
            assert(res.data[0].clientInfo.userAgent !== undefined);
            assert(res.data[0].clientInfo.locale !== undefined);
        }, (error) => {
            throw (error);
        }, {
            "Content-Type": "application/json"
        });

    await cas.doGet("https://localhost:8443/cas/actuator/auditevents",
        async (res) => {
            await cas.log(`Found ${res.data.events.length} audit records`);
            assert(res.data.events.length >= 4);
            assert(res.data.events[0].principal !== undefined);
            assert(res.data.events[0].timestamp !== undefined);
            assert(res.data.events[0].type !== undefined);
        }, async (err) => {
            throw (err);
        });

    await cas.log("Updating configuration...");
    const number = await cas.randomNumber();
    await updateConfig(configFile, configFilePath, number);
    await cas.sleep(6000);
    await cas.refreshContext();
    await cas.sleep(3000);

    await cas.log("Testing authentication after refresh...");
    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await page.close();
    await cas.closeBrowser(browser);

})();

async function updateConfig(configFile, configFilePath, data) {
    const config = {
        cas: {
            audit: {
                jdbc: {
                    "max-age-days": data
                }
            }
        }
    };
    const newConfig = await cas.toYAML(config);
    await cas.log(`Updated configuration:\n${newConfig}`);
    await fs.writeFileSync(configFilePath, newConfig);
    await cas.log(`Wrote changes to ${configFilePath}`);
}
