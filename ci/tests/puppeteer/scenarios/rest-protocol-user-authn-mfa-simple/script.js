const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const service = "https://apereo.github.io";
    let value = `casuser:Mellon`;
    let buff = Buffer.alloc(value.length, value);
    let authzHeader = buff.toString('base64');

    await cas.doPost(`https://localhost:8443/cas/actuator/mfaSimple?service=${service}`, "",
        {
            "Credential": authzHeader,
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        async res => {
            assert(res.data.id !== undefined);
            assert(res.data.ttl !== undefined);
            assert(res.data.principal === "casuser");
            assert(res.data.service === service);

            let params = `username=casuser&password=Mellon&sotp=${res.data.id}`;
            await cas.log(`Authenticating user via ${params}`);
            const body = await cas.doRequest(`https://localhost:8443/cas/v1/users?${params}`, "POST",
                {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded'
                }, 200);
           
            let result = JSON.parse(body);
            console.dir(result, {depth: null, colors: true});
            
            assert(result.authentication.principal.id === "casuser");
            assert(result.authentication.attributes["authnContextClass"][0] === "mfa-simple");
        }, error => {
            throw error;
        }, {'Content-Type': "application/json"});
})();
