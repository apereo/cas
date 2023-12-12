const assert = require("assert");
const cas = require("../../cas.js");
const fs = require("fs");
const path = require("path");
const os = require("os");

(async () => {
    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";

    const template = path.join(__dirname, "registered-service.json");
    const contents = fs.readFileSync(template, "utf8");
    const totalCount = 20;
    for (let i = 1; i <= totalCount; i++) {
        const serviceBody = contents.replace("${id}", String(i));
        await cas.log(`Import registered service:\n${serviceBody}`);
        await cas.doRequest(`${baseUrl}/import`, "POST", {
            "Accept": "application/json",
            "Content-Length": serviceBody.length,
            "Content-Type": "application/json"
        }, 201, serviceBody);
    }

    const body = JSON.parse(await cas.doRequest(baseUrl, "GET", {
        "Content-Type": "application/json"
    }, 200));
    assert(body[1].length === totalCount);

    await cas.doGet(`${baseUrl}/export`,
        (res) => {
            const tempDir = os.tmpdir();
            const exported = path.join(tempDir, "services.zip");
            res.data.pipe(fs.createWriteStream(exported));
            cas.log(`Exported services are at ${exported}`);
        },
        (error) => {
            throw error;
        }, {}, "stream");
})();
