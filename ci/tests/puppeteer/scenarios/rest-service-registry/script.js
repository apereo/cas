const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    console.log("Starting HTTP server...")
    await cas.httpServer(__dirname);

    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, res => {
        assert(res.status === 200)
        console.log(`Services found: ${res.data[1].length}`);
        assert(res.data[1].length === 2)
    }, err => {
        throw err;
    }, {
        'Content-Type': 'application/json'
    })

    await process.exit(0);
})();
