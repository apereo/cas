const assert = require('assert');
const cas = require('../../cas.js');
const querystring = require("querystring");

(async () => {
    const service = "https://apereo.github.io";
    let value = `casuser:Mellon`;
    let buff = Buffer.alloc(value.length, value);
    let authzHeader = buff.toString('base64');

    let body = JSON.parse(await cas.doRequest(`https://localhost:8443/cas/actuator/mfaSimple?service=${service}`, "GET",
        {
            "Credential": authzHeader,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        200));

    assert(body.id !== undefined);
    assert(body.ttl !== undefined);
    assert(body.principal === "casuser");
    assert(body.service === service);
    
    let formData = {
        username: 'casuser',
        password: 'Mellon',
        sotp: body.id
    };
    let postData = querystring.stringify(formData);
    await cas.log(`Authenticating user via ${postData}`);
    let result = JSON.parse(await cas.doRequest("https://localhost:8443/cas/v1/users", "POST",
        {
            'Content-Length': Buffer.byteLength(postData),
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        200, postData));
    console.dir(result, {depth: null, colors: true});

    assert(result.authentication.principal.id === "casuser");
    assert(result.authentication.attributes["authnContextClass"][0] === "mfa-simple");

})();
