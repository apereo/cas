const assert = require('assert');
const jwt = require('jsonwebtoken');
const cas = require('../../cas.js');

(async () => {
    console.log("Creating ticket-granting ticket as JWT")
    let tgt = await executeRequest('https://localhost:8443/cas/v1/tickets?username=casuser&password=Mellon&token=true', 201);
    console.log(tgt);
    let decoded = jwt.decode(tgt);
    console.log(decoded);
    assert(decoded != null);

    console.log("Creating service ticket as JWT")
    tgt = await executeRequest('https://localhost:8443/cas/v1/tickets?username=casuser&password=Mellon', 201);
    console.log(tgt);
    assert(tgt !== null);

    let st = await executeRequest('https://localhost:8443/cas/v1/tickets/' + tgt + '?service=https://github.com/apereo/cas', 200);
    console.log(st);
    decoded = jwt.decode(st);
    console.log(decoded);
    assert(decoded != null);
})();


async function executeRequest(url, statusCode) {
    return await cas.doRequest(url, "POST", {
        'Accept': 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded'
    }, statusCode);
}
