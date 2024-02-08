const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    let params = "grant_type=client_credentials&";
    params += "scope=openid";

    const url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, (res) => {
        cas.log(res.data);
        assert(res.data.access_token !== undefined);
        assert(res.data.refresh_token !== undefined);

        introspect(res.data.access_token, successHandler);
        introspect(res.data.refresh_token, successHandler);

    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    await cas.log("Introspecting invalid token...");
    await introspect("AT-1234567890", (res, token) => {
        cas.logb(`Received token: ${token}`);
        assert(res.data.active === false);
        assert(res.data.scope === "CAS");
        assert(res.data.tokenType === undefined);
        assert(res.data.client_id === undefined);
    });

})();

function successHandler(res, token) {
    assert(res.data.active === true);
    assert(res.data.iat !== undefined);
    assert(res.data.exp !== undefined);
    assert(res.data.aud === "client");
    assert(res.data.uniqueSecurityName === "client");
    assert(res.data.scope === "CAS");
    assert(res.data.sub === "client");
    assert(res.data.tokenType === "Bearer");
    assert(res.data.client_id === "client");
    assert(res.data.token === token);
}

async function introspect(token, handlerOnSuccess) {
    const value = "client:secret";
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    await cas.log(`Introspecting token ${token}`);
    await cas.doGet(`https://localhost:8443/cas/oauth2.0/introspect?token=${token}`,
        (res) => handlerOnSuccess(res, token),
        (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": authzHeader,
            "Content-Type": "application/json"
        });
}
