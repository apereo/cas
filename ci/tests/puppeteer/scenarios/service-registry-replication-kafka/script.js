const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const path = require("path");

(async () => {

    await cas.log("Checking for services in server 1");
    const baseUrl1 = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl1, (res) => {
        assert(res.status === 200);
        cas.log(`Services found on server 1: ${res.data[1].length}`);
        assert(res.data[1].length === 1);
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });

    await cas.sleep(2000);

    await cas.log("Checking for services in server 2");
    const baseUrl2 = "https://localhost:8444/cas/actuator/registeredServices";
    await cas.doGet(baseUrl2, (res) => {
        assert(res.status === 200);
        cas.log(`Services found on server 2: ${res.data[1].length}`);
        assert(res.data[1].length === 1);
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });
    
    const s1Path = path.join(__dirname, "services/Sample-1.json");
    await cas.log(`Parsing JSON file ${s1Path}`);
    const s1 = JSON.parse(fs.readFileSync(s1Path, "utf8"));

    const description = (Math.random() + 1).toString(36).substring(4);
    await cas.log(`Generated new description: ${description}`);
    await update(s1, description, s1Path);

    await cas.sleep(5000);

    await cas.log("Checking for service updates in server 1");
    await cas.doGet(baseUrl1, (res) => {
        cas.log(`Services found in server 1: ${res.data[1].length}`);
        res.data[1].forEach((svc) => {
            cas.log(`Checking service ${svc.name}-${svc.id}`);
            assert(svc.description === description);
        });
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });

    await cas.sleep(3000);

    await cas.log("Checking for service updates in server 2");
    await cas.doGet(baseUrl2, (res) => {
        cas.log(`Services found in server 2: ${res.data[1].length}`);
        res.data[1].forEach((svc) => {
            cas.log(`Checking service ${svc.name}-${svc.id}`);
            assert(svc.description === description);
        });
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });
})();

async function update(service, description, jsonFile) {
    service.description = description;
    const newConfig = JSON.stringify(service, undefined, 2);
    await cas.log(`Updated service configuration:\n${newConfig}`);
    await fs.writeFileSync(jsonFile, newConfig);
    await cas.log(`Wrote changes to ${jsonFile}`);
}
