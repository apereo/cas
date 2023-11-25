const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    let bypassCodes = await cas.fetchDuoSecurityBypassCodes("casuser");
    await cas.log(`Duo Security: Retrieved bypass codes ${bypassCodes}`);
    let bypassCode = `${String(bypassCodes[0])}`;

    await cas.doPost("https://localhost:8443/cas/actuator/awsSts",
        `username=casuser&password=Mellon&duration=PT15S&passcode=${bypassCode}`, {
            'Content-Type': "application/x-www-form-urlencoded"
        }, res => {
            assert(res.status === 200);

            let data = res.data.toString();
            assert(data.includes("aws_access_key_id"));
            assert(data.includes("aws_secret_access_key"));
            assert(data.includes("aws_session_token"));
        }, error => {
            throw `Unable to fetch credentials: ${error}`;
        });

})();
