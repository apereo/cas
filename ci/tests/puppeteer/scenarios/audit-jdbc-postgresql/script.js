const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
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
    await cas.updateYamlConfigurationSource(__dirname, {
        cas: {
            audit: {
                jdbc: {
                    "max-age-days": number
                }
            }
        }
    });
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
