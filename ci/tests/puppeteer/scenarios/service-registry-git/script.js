const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, res => {
        assert(res.status === 200)
        assert(res.data[1].length === 2)
    }, err => {
        throw err;
    }, {
        'Content-Type': 'application/json'
    })
})();
