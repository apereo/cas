const assert = require("assert");
const cas = require("../../cas.js");
const querystring = require("querystring");

(async () => {
    const formData = {
        username: "casuser",
        password: "Mellon"
    };
    const postData = querystring.stringify(formData);
    const body = await cas.doRequest("https://localhost:8443/cas/v1/users",
        "POST",
        {
            "Accept": "application/json",
            "Content-Length": Buffer.byteLength(postData),
            "Content-Type": "application/x-www-form-urlencoded"
        },
        200,
        postData);
    await cas.log(body);
    const result = JSON.parse(body);
    assert(result.authentication.principal.id === "casuser");
})();
