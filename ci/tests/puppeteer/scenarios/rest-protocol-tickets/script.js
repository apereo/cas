const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const tgt = await executeRequest('https://localhost:8443/cas/v1/tickets?username=casuser&password=Mellon', 'POST', 201);
    console.log(tgt);
    assert(tgt != null);

    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, 'GET', 200);
    let st = await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}?service=https://github.com/apereo/cas`, 'POST', 200)
    console.log(st);
    assert(st != null);

    await executeRequest(`https://localhost:8443/cas/v1/tickets/${st}`, "GET", 200);
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${st}`, "DELETE", 200);
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "DELETE", 200);
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, 'GET', 404);
})();

async function executeRequest(url, method, statusCode) {
    return await cas.doRequest(url, method, {
        'Accept': 'application/json',
        'Content-Type': 'application/x-www-form-urlencoded'
    }, statusCode);
}
