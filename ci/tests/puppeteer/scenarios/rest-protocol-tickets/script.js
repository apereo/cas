const assert = require("assert");
const cas = require("../../cas.js");
const querystring = require("querystring");

(async () => {
    const formData = {
        username: "casuser",
        password: "Mellon"
    };
    const postData = querystring.stringify(formData);

    const tgt = await executeRequest("https://localhost:8443/cas/v1/tickets", "POST", 201, "application/x-www-form-urlencoded", postData);
    await cas.log(tgt);
    assert(tgt !== undefined);

    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "GET", 200);
    const st = await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "POST", 200, "application/x-www-form-urlencoded", "service=https://github.com/apereo/cas");
    await cas.log(st);
    assert(st !== undefined);

    await executeRequest(`https://localhost:8443/cas/v1/tickets/${st}`, "GET", 200);
    await cas.doDelete(`https://localhost:8443/cas/v1/tickets/${st}`);
    await cas.sleep(2000);
    await cas.doDelete(`https://localhost:8443/cas/v1/tickets/${tgt}`);
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "GET", 404);
})();

async function executeRequest(url, method, statusCode, contentType = "application/x-www-form-urlencoded", requestBody = undefined) {
    return cas.doRequest(url, method,
        {
            "Accept": "application/json",
            "Content-Length": requestBody === undefined ? 0 : Buffer.byteLength(requestBody),
            "Content-Type": contentType
        },
        statusCode, requestBody);
}
