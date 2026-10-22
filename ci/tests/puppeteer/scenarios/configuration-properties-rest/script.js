const cas = require("../../cas.js");
const assert = require("assert");

const ACTUATOR = "https://localhost:8443/cas/actuator";
const BACKEND = "http://localhost:5618/config";
const SOURCE = "bootstrapProperties-org.apereo.cas.config.RestfulPropertySource";

async function assertProperty(name, value) {
    const json = JSON.parse(await cas.doRequest(`${ACTUATOR}/env/${name}`, "GET", {"Accept": "application/json"}, 200));
    await cas.log(json.property);
    assert(json.property.source === SOURCE);
    assert(json.property.value === value);
}

async function login(page, username, success) {
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, username, "Mellon");
    await cas.sleep(1000);
    await cas.assertCookie(page, success);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.logg("Properties are read from the REST backend at startup");
        await assertProperty("cas.authn.accept.users", "restuser::Mellon");
        await assertProperty("cas.server.scope", "example.org");
        await login(page, "restuser", true);

        await cas.logg("A value changed in the backend is picked up after a refresh");
        await cas.doRequest(`${BACKEND}?name=cas.authn.accept.users&value=${encodeURIComponent("restuser2::Mellon")}`, "POST", {}, 200);
        await cas.refreshContext();
        await cas.sleep(2000);
        await assertProperty("cas.authn.accept.users", "restuser2::Mellon");
        await login(page, "restuser", false);
        await login(page, "restuser2", true);

        await cas.logg("A value updated through CAS is written to the backend");
        const updated = JSON.parse(await cas.doRequest(`${ACTUATOR}/casConfig/update`, "POST",
            {"Content-Type": "application/json", "Accept": "application/json"}, 200,
            JSON.stringify([{name: "cas.server.scope", value: "example.com"}])));
        await cas.log(updated);
        assert(updated.some((source) => source.endsWith("RestfulPropertySource")));
        assert(await cas.doRequest(`${BACKEND}?name=cas.server.scope`, "GET", {}, 200) === "example.com");
        await cas.refreshContext();
        await cas.sleep(2000);
        await assertProperty("cas.server.scope", "example.com");
    } finally {
        await cas.closeBrowser(browser);
    }
})();
