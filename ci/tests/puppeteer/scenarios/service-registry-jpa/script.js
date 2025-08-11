const assert = require("assert");
const cas = require("../../cas.js");
const fs = require("fs");
const path = require("path");

const TOTAL = 10;
const ACTUATOR_URL = "https://localhost:8443/cas/actuator";
const BASE_URL = `${ACTUATOR_URL}/registeredServices`;

async function fetchServices() {
    await cas.log("Fetching services from CAS");
    const body = await cas.doRequest(BASE_URL, "GET",
        {
            "Content-Type": "application/json",
            "Accept": "application/json"
        }, 200);
    await cas.log(body);
    await cas.log("===================================");
    return body;
}

async function verifyServices() {
    await cas.log("Verifying services from CAS");
    for (let i = 1; i <= TOTAL; i++) {
        await cas.doGet(`${BASE_URL}/${i}`,
            async () => {
            }, async (error) => {
                throw error;
            }, {"Content-Type": "application/json"});
    }
}

async function importServices() {
    const template = path.join(__dirname, "registered-service.json");
    const contents = fs.readFileSync(template, "utf8");
    for (let i = 1; i <= TOTAL; i++) {
        const serviceId = String(i);
        const serviceBody = contents.replaceAll("${id}", serviceId);
        await cas.log(`Import registered service:\n${serviceBody}`);
        await cas.doRequest(`${BASE_URL}/import`, "POST", {
            "Accept": "application/json",
            "Content-Length": serviceBody.length,
            "Content-Type": "application/json"
        }, 201, serviceBody);

        await cas.doRequest(`${ACTUATOR_URL}/entityHistory/registeredServices/${serviceId}`, "GET", {
            "Accept": "application/json",
            "Content-Type": "application/json"
        });
        await cas.doRequest(`${ACTUATOR_URL}/entityHistory/registeredServices/${serviceId}/changelog`, "GET", {
            "Accept": "text/plain",
            "Content-Type": "application/json"
        });
    }
}

(async () => {
    let failed = false;
    try {
        const mysql = await cas.dockerContainer("mysql-server");

        await importServices();
        await fetchServices();

        await cas.log("Pausing MySQL docker container");
        await mysql.pause();

        await verifyServices();

        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        const service = "https://localhost:9859/anything/cas";
        await cas.gotoLogin(page, service);
        await cas.loginWith(page);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user === "casuser");
        await cas.gotoLogout(page);
        await cas.closeBrowser(browser);

        await cas.log("Unpausing MySQL docker container");
        await mysql.unpause();

        await cas.sleep(2000);
        await fetchServices();
        await verifyServices();
        await cas.logg("All CAS services are available");

    } catch (e) {
        failed = true;
        throw e;
    } finally {
        if (!failed) {
            await process.exit(0);
        }
    }
})();
