const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    await cas.doGet("https://info:password@localhost:8443/cas/actuator/info",
        (res) => assert(res.status === 200), (err) => {
            throw err;
        });
    await cas.doGet("https://info:password@localhost:8443/cas/actuator/ssoSessions",
        () => {
            throw "Should not be authorized to access ssoSessions";
        }, (err) => {
            cas.log(`Expected failure: ${err}`);
        });

    await cas.separator();
    await cas.doGet("https://sso:password@localhost:8443/cas/actuator/info",
        () => {
            throw "Should not be authorized to access info";
        }, (err) => {
            cas.log(`Expected failure: ${err}`);
        });
    await cas.doGet("https://sso:password@localhost:8443/cas/actuator/ssoSessions",
        (res) => assert(res.status === 200), (err) => {
            throw err;
        });

    await cas.separator();
    await cas.doGet("https://info:password@localhost:8443/cas/actuator/health",
        (res) => assert(res.status === 200), (err) => {
            throw err;
        });
    await cas.doGet("https://sso:password@localhost:8443/cas/actuator/health",
        (res) => assert(res.status === 200), (err) => {
            throw err;
        });

    await cas.separator();
    await cas.doGet("https://other:password@localhost:8443/cas/actuator/info",
        () => {
            throw "Should not be authorized to access info";
        }, (err) => {
            cas.log(`Expected failure: ${err}`);
        });
    await cas.doGet("https://other:password@localhost:8443/cas/actuator/health",
        () => {
            throw "Should not be authorized to access info";
        }, (err) => {
            cas.log(`Expected failure: ${err}`);
        });
    await cas.doGet("https://other:password@localhost:8443/cas/actuator/ssoSessions",
        () => {
            throw "Should not be authorized to access info";
        }, (err) => {
            cas.log(`Expected failure: ${err}`);
        });
})();
