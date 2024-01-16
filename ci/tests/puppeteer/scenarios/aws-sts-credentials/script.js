const assert = require("assert");
const cas = require("../../cas.js");

(async () =>
    cas.doPost("https://localhost:8443/cas/actuator/awsSts",
        "username=casuser&password=Mellon&duration=PT15S", {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        (res) => {
            assert(res.status === 200);

            const data = res.data.toString();
            assert(data.includes("aws_access_key_id"));
            assert(data.includes("aws_secret_access_key"));
            assert(data.includes("aws_session_token"));
        },
        () => {
            throw "Unable to fetch credentials";
        }))();
