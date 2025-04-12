const cas = require("../../cas.js");

const fs = require("fs");
const path = require("path");
const assert = require("assert");

async function callRegisteredServices() {
    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, async (res) => {
        assert(res.status === 200);
        await cas.log(`Services found: ${res.data[1].length}`);
    }, async (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });
}

async function callAuditLog() {
    await cas.doGet("https://localhost:8443/cas/actuator/auditLog",
        (res) => cas.log(`Found ${res.data.length} audit records`),
        (error) => {
            throw (error);
        }, {
            "Content-Type": "application/json"
        });
}

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
    await cas.sleep(6000);
    await browser.close();

    await callAuditLog();

    await cas.log("Updating configuration...");
    const number = await cas.randomNumber();
    await updateConfig(configFile, configFilePath, number);
    await cas.sleep(5000);
    await cas.refreshContext();

    await cas.log("Testing authentication after refresh...");
    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page);
    await cas.assertCookie(page);

    await callAuditLog();
    await callRegisteredServices();

    await cas.log("Waiting for audit log cleaner to resume...");
    await cas.sleep(5000);

    await browser.close();

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
