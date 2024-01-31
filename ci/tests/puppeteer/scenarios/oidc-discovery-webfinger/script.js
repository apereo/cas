const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const resource1 = "okta:acct:joe.stormtrooper@localhost";
    const rel = "http://openid.net/specs/connect/1.0/issuer";

    await cas.doGet(`https://localhost:8443/cas/oidc/.well-known/webfinger?resource=${resource1}&rel=${rel}`,
        (res) => {
            assert(res.status === 200);
            assert(res.data.subject === resource1);
            assert(res.data.links[0].rel === "http://openid.net/specs/connect/1.0/issuer");
            assert(res.data.links[0].href === "https://localhost:8443/cas/oidc");
        }, (error) => {
            throw `Operation failed ${error}`;
        });

    const resource2 = "okta:acct:joe.stormtrooper@example.org";
    await cas.doGet(`https://localhost:8443/cas/oidc/.well-known/webfinger?resource=${resource2}&rel=${rel}`,
        (res) => {
            throw `Operation failed ${res}`;
        }, (error) => assert(error.response.status === 404));
})();

