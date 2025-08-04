const assert = require("assert");
const cas = require("../../cas.js");
const querystring = require("querystring");

(async () => {
    const service = "https://example.org";
    const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    const formData = {
        username: "casuser",
        password: "Mellon",
        gauthotp: `${scratch}`,
        gauthacct: "1"
    };
    const postData = querystring.stringify(formData);

    const tgt = await executeRequest("https://localhost:8443/cas/v1/tickets", "POST", 201, "application/x-www-form-urlencoded", postData);
    await cas.log(tgt);

    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "GET", 200);
    const st = await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "POST", 200, "application/x-www-form-urlencoded", `service=${service}`);
    await cas.log(st);

    const json = await cas.validateTicket(service, st);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.firstname[0] === "CAS");
    assert(authenticationSuccess.attributes.lastname[0] === "User");
    assert(authenticationSuccess.attributes.username[0] === "casuser");
    assert(authenticationSuccess.attributes.uid[0] === "casuser");
})();

async function executeRequest(url, method, statusCode,
    contentType = "application/x-www-form-urlencoded",
    requestBody = undefined) {
    return cas.doRequest(url, method,
        {
            "Accept": "application/json",
            "Content-Length": requestBody === undefined ? 0 : Buffer.byteLength(requestBody),
            "Content-Type": contentType
        },
        statusCode, requestBody);
}
