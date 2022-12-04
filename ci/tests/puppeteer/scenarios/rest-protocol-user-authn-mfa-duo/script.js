const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const codes = await cas.fetchDuoSecurityBypassCodes("casuser");
    const url = `https://localhost:8443/cas/v1/users`;
    const body = await cas.doRequest(`${url}?username=casuser&password=Mellon&passcode=${codes[0]}`, "POST",
        {
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded'
        }, 200);
    let result = JSON.parse(body);
    console.dir(result, {depth: null, colors: true});
    assert(result.authentication.authenticationDate !== undefined);
    assert(result.authentication.principal.id === "casuser");
    assert(result.authentication.attributes.authnContextClass[0] === "mfa-duo");

    assert(result.authentication.successes["STATIC"].principal.id === "casuser");
    assert(result.authentication.successes["STATIC"].credential.credentialMetaData.credentialClass.includes("UsernamePasswordCredential"));

    const handler = result.authentication.successes["DuoSecurityAuthenticationHandler"];
    assert(handler.credentialMetaData.credentialClass.includes("DuoSecurityPasscodeCredential"));
    assert(handler.principal.id === "casuser");
})();
