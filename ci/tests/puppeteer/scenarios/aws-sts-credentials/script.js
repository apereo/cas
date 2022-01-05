const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    await cas.doPost("https://localhost:8443/cas/actuator/awsSts?duration=PT15S",
        "username=casuser&password=Mellon", {
            'Content-Type': "application/x-www-form-urlencoded"
        },
        res => {
            assert(res.status === 200)

            let data = res.data.toString();
            assert(data.includes("aws_access_key_id"));
            assert(data.includes("aws_secret_access_key"));
            assert(data.includes("aws_session_token"));
        },
        () => {
            throw 'Unable to fetch credentials';
        });
})();
