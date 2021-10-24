const assert = require('assert');
const cas = require('../../cas.js');
const fs = require('fs');
const path = require("path");
const os = require("os");

(async () => {
    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";

    let template = path.join(__dirname, 'registered-service.json');
    let contents = fs.readFileSync(template, 'utf8')
    for (let i = 1; i <= 100; i++) {
        let serviceBody = contents.replace("${id}", String(i))
        console.log(`Import registered service:\n${serviceBody}`);
        await cas.doRequest(`${baseUrl}/import`, "POST", {
            'Accept': 'application/json',
            'Content-Length': serviceBody.length,
            'Content-Type': 'application/json'
        }, 201, serviceBody);
    }

    await cas.doGet(baseUrl,
        function(res) {
            assert(res.status === 200)
            assert(res.data[1].length === 100);
        },
        function(error) {
            throw error;
        })

    await cas.doGet(`${baseUrl}/export`,
        function(res) {
            const tempDir = os.tmpdir();
            let exported = path.join(tempDir, 'services.zip');
            res.data.pipe(fs.createWriteStream(exported));
            console.log(`Exported services are at ${exported}`);
            assert(fs.existsSync(exported) === true)
        },
        function(error) {
            throw error;
        }, {}, "stream")
})();
