const assert = require("assert");
const cas = require("../../cas.js");

const ACTUATOR_URL = "https://localhost:8443/cas/actuator/registeredServices";

const JSON_SERVICE = "https://localhost:9859/anything/json";
const YAML_SERVICE = "https://localhost:9859/anything/yaml";
const SHARED_SERVICE = "https://localhost:9859/anything/shared";
const UNKNOWN_SERVICE = "https://localhost:9859/anything/unknown";

const DESCRIPTIONS = {
    json: "Registered in the JSON service registry",
    yaml: "Registered in the YAML service registry"
};

/*
 * Both registries define service 3000. Registries are chained by their configured
 * order and the services manager keeps the definition that is loaded last, so the
 * registry with the higher order value owns the shared service.
 */
const SHARED_SERVICE_OWNERS = {
    YamlFirst: "json",
    JsonFirst: "yaml"
};

async function verifyServiceTicket(page, service, registry) {
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    const success = json.serviceResponse.authenticationSuccess;
    assert(success !== undefined, `Service ticket ${ticket} failed validation for ${service}`);
    assert.equal(success.user, "casuser");
    assert.equal(success.attributes.registry[0], registry);
}

async function verifyRegisteredService(id, name, registry) {
    await cas.doGet(`${ACTUATOR_URL}/${id}`, (res) => {
        assert.equal(res.status, 200);
        assert.equal(res.data.id, id);
        assert.equal(res.data.name, name);
        assert.equal(res.data.description, DESCRIPTIONS[registry]);
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });
}

(async () => {
    const variation = process.env.SCENARIO_VARIATION;
    const sharedServiceOwner = SHARED_SERVICE_OWNERS[variation];
    assert(sharedServiceOwner !== undefined, `Unknown scenario variation: ${variation}`);
    await cas.log(`Variation ${variation}: service 3000 is expected from the ${sharedServiceOwner} registry`);

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.log("Logging into the application from the JSON service registry");
        await cas.gotoLogin(page, JSON_SERVICE);
        await cas.loginWith(page);
        await verifyServiceTicket(page, JSON_SERVICE, "json");

        await cas.separator();
        await cas.log("Using the SSO session for the application from the YAML service registry");
        await cas.gotoLogin(page, YAML_SERVICE);
        await verifyServiceTicket(page, YAML_SERVICE, "yaml");

        await cas.separator();
        await cas.log("Using the SSO session for the application defined in both service registries");
        await cas.gotoLogin(page, SHARED_SERVICE);
        await verifyServiceTicket(page, SHARED_SERVICE, sharedServiceOwner);

        await cas.separator();
        await cas.gotoLogin(page, UNKNOWN_SERVICE);
        await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    } finally {
        await cas.closeBrowser(browser);
    }

    await cas.separator();
    await cas.doGet(ACTUATOR_URL, (res) => {
        assert.equal(res.status, 200);
        const ids = res.data[1].map((service) => service.id).sort((a, b) => a - b);
        cas.log(`Registered services: ${ids}`);
        assert.deepStrictEqual(ids, [1001, 2001, 3000]);
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });

    await verifyRegisteredService(1001, "JsonApplication", "json");
    await verifyRegisteredService(2001, "YamlApplication", "yaml");
    await verifyRegisteredService(3000, "SharedApplication", sharedServiceOwner);
})();
