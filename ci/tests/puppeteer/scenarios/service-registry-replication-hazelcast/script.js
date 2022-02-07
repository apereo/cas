const cas = require('../../cas.js');
const assert = require("assert");
const fs = require("fs");
const path = require('path');

(async () => {

    const baseUrl1 = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl1, res => {
        assert(res.status === 200)
        console.log(`Services found: ${res.data[1].length}`);
        assert(res.data[1].length === 2)
    }, err => {
        throw err;
    }, {
        'Content-Type': 'application/json'
    })

    const baseUrl2 = "https://localhost:8444/cas/actuator/registeredServices";
    await cas.doGet(baseUrl2, res => {
        assert(res.status === 200)
        console.log(`Services found: ${res.data[1].length}`);
        assert(res.data[1].length === 2)
    }, err => {
        throw err;
    }, {
        'Content-Type': 'application/json'
    })

    let s1Path = path.join(__dirname, "services/Sample-1.json");
    console.log(`Parsing JSON file ${s1Path}`)
    let s1 = JSON.parse(fs.readFileSync(s1Path, 'utf8'));

    let s2Path = path.join(__dirname, "services/Sample-2.json");
    console.log(`Parsing JSON file ${s2Path}`)
    let s2 = JSON.parse(fs.readFileSync(s2Path, 'utf8'));

    let description = (Math.random() + 1).toString(36).substring(4);
    console.log(`Generated new description: ${description}`)
    await update(s1, description, s1Path);
    await update(s2, description, s2Path);

    await cas.sleep(2000)

    await cas.doGet(baseUrl1, res => {
        console.log(`Services found: ${res.data[1].length}`);
        res.data[1].forEach(svc => {
            console.log(`Checking service ${svc.name}-${svc.id}`)
            assert(svc.description === description)
        })
    }, err => {
        throw err;
    }, {
        'Content-Type': 'application/json'
    })

    await cas.doGet(baseUrl2, res => {
        console.log(`Services found: ${res.data[1].length}`);
        res.data[1].forEach(svc => {
            console.log(`Checking service ${svc.name}-${svc.id}`)
            assert(svc.description === description)
        })
    }, err => {
        throw err;
    }, {
        'Content-Type': 'application/json'
    })
})();

async function update(service, description, jsonFile) {
    service.description = description;
    const newConfig = JSON.stringify(service);
    console.log(`Updated service configuration:\n${newConfig}`);
    await fs.writeFileSync(jsonFile, newConfig);
    console.log(`Wrote changes to ${jsonFile}`);
}
