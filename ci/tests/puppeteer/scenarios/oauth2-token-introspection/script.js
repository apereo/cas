const assert = require('assert');
const cas = require('../../cas.js');

(async () => {

    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "grant_type=client_credentials&";
    params += "scope=openid";

    let url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.log(`Calling ${url}`);

    let accessToken = null;
    let refreshToken = null;
    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, res => {
        cas.log(res.data);
        assert(res.data.access_token !== null);
        assert(res.data.refresh_token !== null);

        introspect(res.data.access_token, successHandler);
        introspect(res.data.refresh_token, successHandler);

    }, error => {
        throw `Operation failed: ${error}`;
    });

    await cas.log("Introspecting invalid token...");
    await introspect("AT-1234567890", (res, token) => {
        assert(res.data.active === false);
        assert(res.data.scope === "CAS");
        assert(res.data.tokenType === undefined);
        assert(res.data.client_id === undefined)
    });

})();

function successHandler(res, token) {
    assert(res.data.active === true);
    assert(res.data.iat !== null);
    assert(res.data.exp !== null);
    assert(res.data.aud === "client");
    assert(res.data.uniqueSecurityName === "client");
    assert(res.data.scope === "CAS");
    assert(res.data.sub === "client");
    assert(res.data.tokenType === "Bearer");
    assert(res.data.client_id === "client");
    assert(res.data.token === token)
}

async function introspect(token, handlerOnSuccess) {
    let value = `client:secret`;
    let buff = Buffer.alloc(value.length, value);
    let authzHeader = `Basic ${buff.toString('base64')}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    await cas.log(`Introspecting token ${token}`);
    await cas.doGet(`https://localhost:8443/cas/oauth2.0/introspect?token=${token}`,
        res => handlerOnSuccess(res, token),
        error => {
            throw `Introspection operation failed: ${error}`;
        }, {
            'Authorization': authzHeader,
            'Content-Type': 'application/json'
        });
}
