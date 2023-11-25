const assert = require('assert');
const cas = require('../../cas.js');
const querystring = require('querystring');

(async () => {
    let formData = {
        username: 'casuser',
        password: 'Mellon'
    };
    let postData = querystring.stringify(formData);

    const tgt = await executeRequest('https://localhost:8443/cas/v1/tickets', 'POST', 201, "application/x-www-form-urlencoded", postData);
    await cas.log(tgt);
    assert(tgt != null);

    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, 'GET', 200);
    let st = await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, 'POST', 200, "application/x-www-form-urlencoded", "service=https://github.com/apereo/cas");
    await cas.log(st);
    assert(st != null);

    await executeRequest(`https://localhost:8443/cas/v1/tickets/${st}`, "GET", 200);
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${st}`, "DELETE", 200, "application/json");
    await cas.sleep(2000);
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "DELETE", 200, "application/json");
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, 'GET', 404);
})();

async function executeRequest(url, method, statusCode, contentType = "application/x-www-form-urlencoded", requestBody = undefined) {
    return await cas.doRequest(url, method,
        {
            'Accept': 'application/json',
            'Content-Length': requestBody !== undefined ? Buffer.byteLength(requestBody) : 0,
            'Content-Type': contentType
        },
        statusCode, requestBody);
}
