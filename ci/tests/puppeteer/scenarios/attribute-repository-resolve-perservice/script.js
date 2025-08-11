
const assert = require("assert");
const cas = require("../../cas.js");
const fs = require("fs");
const path = require("path");

(async () => {
    await cas.doGet("https://localhost:8443/cas/actuator/resolveAttributes/casuser",
        async (res) => {
            assert(res.data.username !== undefined);
            assert(res.data.attributes !== undefined);
            assert(Object.keys(res.data.attributes).length === 0);
        }, async (error) => {
            throw error;
        }, { "Content-Type": "application/json" });

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);

    await cas.loginWith(page);
    await cas.logPage(page);
    const ticket = await cas.assertTicketParameter(page);
    let json = await cas.validateTicket(service, ticket);
    let attributes = json.serviceResponse.authenticationSuccess.attributes;
    assert(attributes.lastName[0] === "Johnson");
    assert(attributes.employeeNumber[0] === "123456");
    assert(attributes.firstName[0] !== undefined);
    assert(attributes.displayName === undefined);
    
    const newFirstName = (Math.random() + 1).toString(36).substring(4);
    await cas.log(`Generated new first name ${newFirstName}`);
    const configFilePath = path.join(__dirname, "/attribute-repository.json");
    const config = JSON.parse(fs.readFileSync(configFilePath));
    config.casuser.firstName[0] = newFirstName;
    await fs.writeFileSync(configFilePath, JSON.stringify(config, undefined, 2));
    await cas.sleep(2000);

    await cas.log("Validating again to get attribute updates...");
    json = await cas.validateTicket(service, ticket);
    attributes = json.serviceResponse.authenticationSuccess.attributes;
    assert(attributes.firstName[0] === newFirstName);
    
    await cas.closeBrowser(browser);
    await fs.unlinkSync(configFilePath);
})();
