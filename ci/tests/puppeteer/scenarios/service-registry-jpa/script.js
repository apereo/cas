const assert = require('assert');
const cas = require('../../cas.js');
const fs = require('fs');
const path = require("path");
const puppeteer = require("puppeteer");

const TOTAL = 10;
const BASE_URL = "https://localhost:8443/cas/actuator/registeredServices";

async function fetchServices() {
    await cas.log("Fetching services from CAS");
    let body = await cas.doRequest(BASE_URL, "GET",
        {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }, 200);
    await cas.log(body);
    await cas.log("===================================");
    return body;
}

async function verifyServices() {
    await cas.log("Verifying services from CAS");
    for (let i = 1; i <= TOTAL; i++) {
        await cas.doGet(`${BASE_URL}/${i}`,
            async res => {
            }, async error => {
                throw error;
            }, {'Content-Type': "application/json"});
    }
}


async function importServices() {
    let template = path.join(__dirname, 'registered-service.json');
    let contents = fs.readFileSync(template, 'utf8');
    for (let i = 1; i <= TOTAL; i++) {
        let serviceBody = contents.replaceAll("${id}", String(i));
        await cas.log(`Import registered service:\n${serviceBody}`);
        await cas.doRequest(`${BASE_URL}/import`, "POST", {
            'Accept': 'application/json',
            'Content-Length': serviceBody.length,
            'Content-Type': 'application/json'
        }, 201, serviceBody);
    }
}

(async () => {
    let mysql = await cas.dockerContainer("mysql-server");

    await importServices();
    await fetchServices();

    await cas.log("Pausing MySQL docker container");
    await mysql.pause();

    await verifyServices();

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://apereo.github.io";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    let ticket = await cas.assertTicketParameter(page);
    let body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    let json = JSON.parse(body);
    let authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    await cas.gotoLogout(page);
    await browser.close();

    await cas.log("Unpausing MySQL docker container");
    await mysql.unpause();

    await cas.sleep(2000);
    await fetchServices();
    await verifyServices();
    await cas.logg("All CAS services are available");
    await process.exit(0);
})();
