const cas = require('../../cas.js');
const assert = require("assert");
const YAML = require('yaml');
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
    })

    const baseUrl2 = "https://localhost:8444/cas/actuator/registeredServices";
    await cas.doGet(baseUrl2, res => {
        assert(res.status === 200)
        console.log(`Services found: ${res.data[1].length}`);
        assert(res.data[1].length === 2)
    }, err => {
        throw err;
    })

    let s1Path = path.join(__dirname, "services/Sample-1.yml");
    console.log(`Parsing YAML file ${s1Path}`)
    let s1 = YAML.parse(fs.readFileSync(s1Path, 'utf8'));

    let s2Path = path.join(__dirname, "services/Sample-2.yml");
    console.log(`Parsing YAML file ${s2Path}`)
    let s2 = YAML.parse(fs.readFileSync(s2Path, 'utf8'));

    let description = (Math.random() + 1).toString(36).substring(4);
    await update(s1, description, s1Path);
    await update(s2, description, s2Path);

    await cas.sleep(1000)
    
})();

async function update(service, description, yamlFile) {
    service.description = description;
    const newConfig = YAML.stringify(service);
    console.log(`Updated service configuration:\n${newConfig}`);
    await fs.writeFileSync(yamlFile, newConfig);
    console.log(`Wrote changes to ${yamlFile}`);
}
