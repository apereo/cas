const assert = require("assert");
const cas = require("../../cas.js");
const querystring = require("querystring");

(async () => {
    await cas.log("Creating ticket-granting ticket as JWT");
    let formData = {
        username: "casuser",
        password: "Mellon",
        token: true
    };
    let postData = querystring.stringify(formData);

    let tgt = await executeRequest("https://localhost:8443/cas/v1/tickets", 201, postData);
    await assertSignedJwt(tgt);

    await cas.log("Creating service ticket as JWT");
    formData = {
        username: "casuser",
        password: "Mellon"
    };
    postData = querystring.stringify(formData);
    tgt = await executeRequest("https://localhost:8443/cas/v1/tickets", 201, postData);
    await cas.log(tgt);
    assert(tgt !== null);

    const st = await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}?service=https://github.com/apereo/cas`, 200);
    await assertSignedJwt(st);
})();

/**
 * Tickets issued as JWTs are signed; unsigned JWTs are never issued.
 *
 * @param token the ticket as a JWT
 */
async function assertSignedJwt(token) {
    const decoded = await cas.decodeJwt(token, true);
    assert.notEqual(decoded.header.alg, "none");
    assert(decoded.payload.sub !== undefined);
}

async function executeRequest(url, statusCode, requestBody = undefined) {
    return cas.doRequest(url, "POST",
        {
            "Accept": "application/json",
            "Content-Length": requestBody !== undefined ? Buffer.byteLength(requestBody) : 0,
            "Content-Type": "application/x-www-form-urlencoded"
        }, statusCode, requestBody);
}
