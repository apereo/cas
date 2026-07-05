const cas = require("../../cas");
const assert = require("assert");
const querystring = require("querystring");

async function runRestRequest(port, status) {
    const formData = {
        username: "casuser",
        password: "Mellon"
    };
    const postData = querystring.stringify(formData);
    const body = await cas.doRequest(`https://localhost:${port}/cas/v1/users`,
        "POST",
        {
            "Accept": "application/json",
            "Content-Length": Buffer.byteLength(postData),
            "Content-Type": "application/x-www-form-urlencoded"
        },
        status,
        postData);
    if (status === 200) {
        await cas.log(body);
        const result = JSON.parse(body);
        assert(result.authentication.principal.id === "casuser");
    }
}

(async () => {

    await cas.doGet("https://localhost:8443/cas/actuator/info",
        () => {
            throw "Actuator support must not be available for this CAS server";
        },
        (err) => {
            assert(err.status === 403);
        });
    await cas.doGet("https://localhost:8444/cas/actuator/info",
        (res) => {
            assert(res.status === 200);
        },
        (err) => {
            throw err;
        });
    
    await runRestRequest(8443, 200);
    await runRestRequest(8444, 403);
})();
